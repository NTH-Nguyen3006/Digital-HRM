package com.company.hrm.module.employee.service;

import com.company.hrm.common.constant.EmploymentStatus;
import com.company.hrm.common.constant.MaritalStatus;
import com.company.hrm.common.constant.ProfileChangeRequestStatus;
import com.company.hrm.common.constant.ProfileStatus;
import com.company.hrm.common.dto.ImportResultResponse;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.ForbiddenException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.exception.UnauthorizedException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.employee.dto.*;
import com.company.hrm.module.employee.entity.*;
import com.company.hrm.module.employee.repository.*;
import com.company.hrm.module.jobtitle.entity.HrJobTitle;
import com.company.hrm.module.jobtitle.repository.HrJobTitleRepository;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.orgunit.repository.HrOrgUnitRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.security.SecurityUserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeProfileWorkflowService {

    private final HrEmployeeRepository employeeRepository;
    private final HrEmployeeProfileRepository profileRepository;
    private final HrEmployeeProfileChangeRequestRepository changeRequestRepository;
    private final HrEmployeeProfileTimelineRepository timelineRepository;
    private final SecUserAccountRepository userAccountRepository;
    private final HrOrgUnitRepository orgUnitRepository;
    private final HrJobTitleRepository jobTitleRepository;
    private final EmployeeService employeeService;
    private final EmployeeAccessScopeService accessScopeService;
    private final EmployeeProfileTimelineService timelineService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public EmployeeProfileWorkflowService(
            HrEmployeeRepository employeeRepository,
            HrEmployeeProfileRepository profileRepository,
            HrEmployeeProfileChangeRequestRepository changeRequestRepository,
            HrEmployeeProfileTimelineRepository timelineRepository,
            SecUserAccountRepository userAccountRepository,
            HrOrgUnitRepository orgUnitRepository,
            HrJobTitleRepository jobTitleRepository,
            EmployeeService employeeService,
            EmployeeAccessScopeService accessScopeService,
            EmployeeProfileTimelineService timelineService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper
    ) {
        this.employeeRepository = employeeRepository;
        this.profileRepository = profileRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.timelineRepository = timelineRepository;
        this.userAccountRepository = userAccountRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.jobTitleRepository = jobTitleRepository;
        this.employeeService = employeeService;
        this.accessScopeService = accessScopeService;
        this.timelineService = timelineService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProfileChangeRequestResponse submitMyProfileChangeRequest(SubmitProfileChangeRequest request) {
        SecUserAccount currentUser = getCurrentUser();
        if (currentUser.getEmployeeId() == null) {
            throw new ForbiddenException("EMPLOYEE_LINK_REQUIRED", "Tài khoản hiện tại chưa được liên kết hồ sơ nhân sự.");
        }
        HrEmployee employee = getEmployee(currentUser.getEmployeeId());
        HrEmployeeProfile profile = getOrCreateProfile(employee);
        if (profile.getProfileStatus() == ProfileStatus.LOCKED) {
            throw new BusinessException("EMPLOYEE_PROFILE_LOCKED", "Hồ sơ hiện đang bị khóa, không thể gửi yêu cầu cập nhật.", HttpStatus.CONFLICT);
        }
        if (changeRequestRepository.existsByEmployeeEmployeeIdAndRequestStatusAndDeletedFalse(employee.getEmployeeId(), ProfileChangeRequestStatus.PENDING)) {
            throw new BusinessException("EMPLOYEE_PROFILE_REQUEST_PENDING", "Đã có yêu cầu đang chờ xử lý.", HttpStatus.CONFLICT);
        }
        HrEmployeeProfileChangeRequest entity = new HrEmployeeProfileChangeRequest();
        entity.setEmployee(employee);
        entity.setRequesterUser(currentUser);
        entity.setRequestType("PROFILE_UPDATE");
        entity.setRequestStatus(ProfileChangeRequestStatus.PENDING);
        entity.setPayloadJson(toJson(request.payload()));
        entity.setSubmittedAt(LocalDateTime.now());
        entity = changeRequestRepository.save(entity);
        ProfileChangeRequestResponse response = toResponse(entity);
        timelineService.record(employee.getEmployeeId(), "REQUEST_SUBMITTED", "Nhân viên gửi yêu cầu cập nhật hồ sơ", entity.getPayloadJson());
        auditLogService.logSuccess("SUBMIT_REQUEST", "EMPLOYEE_PROFILE_REQUEST", "hr_employee_profile_change_request", entity.getProfileChangeRequestId().toString(), null, response, request.reason());
        return response;
    }

    @Transactional(readOnly = true)
    public List<ProfileChangeRequestResponse> listMyProfileChangeRequests() {
        SecUserAccount currentUser = getCurrentUser();
        return changeRequestRepository.findAllByRequesterUserUserIdAndDeletedFalseOrderBySubmittedAtDesc(currentUser.getUserId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProfileChangeRequestResponse> listRequests(ProfileChangeRequestStatus status, Long employeeId, int page, int size) {
        Specification<HrEmployeeProfileChangeRequest> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("requestStatus"), status));
        }
        if (employeeId != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("employee").get("employeeId"), employeeId));
        }
        Page<HrEmployeeProfileChangeRequest> result = changeRequestRepository.findAll(specification, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt")));
        return new PageResponse<>(result.getContent().stream().map(this::toResponse).toList(), page, size, result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Transactional
    public ProfileChangeRequestResponse reviewRequest(Long requestId, ReviewProfileChangeRequest request) {
        HrEmployeeProfileChangeRequest entity = getChangeRequest(requestId);
        if (entity.getRequestStatus() != ProfileChangeRequestStatus.PENDING) {
            throw new BusinessException("EMPLOYEE_PROFILE_REQUEST_FINALIZED", "Yêu cầu đã được xử lý trước đó.", HttpStatus.CONFLICT);
        }
        HrEmployeeProfile profile = getOrCreateProfile(entity.getEmployee());
        if (request.approved()) {
            applyPayload(entity.getEmployee(), profile, fromJson(entity.getPayloadJson()));
            profileRepository.save(profile);
            employeeRepository.save(entity.getEmployee());
            entity.setRequestStatus(ProfileChangeRequestStatus.APPROVED);
            timelineService.record(entity.getEmployee().getEmployeeId(), "REQUEST_APPROVED", "HR duyệt yêu cầu cập nhật hồ sơ", entity.getPayloadJson());
        } else {
            entity.setRequestStatus(ProfileChangeRequestStatus.REJECTED);
            timelineService.record(entity.getEmployee().getEmployeeId(), "REQUEST_REJECTED", "HR từ chối yêu cầu cập nhật hồ sơ", entity.getPayloadJson());
        }
        entity.setReviewerUser(getCurrentUser());
        entity.setReviewedAt(LocalDateTime.now());
        entity.setReviewNote(blankToNull(request.reviewNote()));
        entity = changeRequestRepository.save(entity);
        ProfileChangeRequestResponse response = toResponse(entity);
        auditLogService.logSuccess("REVIEW_REQUEST", "EMPLOYEE_PROFILE_REQUEST", "hr_employee_profile_change_request", requestId.toString(), null, response, request.reviewNote());
        return response;
    }

    @Transactional
    public EmployeeProfileResponse lockProfile(Long employeeId, LockEmployeeProfileRequest request) {
        HrEmployeeProfile profile = getOrCreateProfile(getEmployee(employeeId));
        if (profile.getProfileStatus() == ProfileStatus.LOCKED) {
            throw new BusinessException("EMPLOYEE_PROFILE_ALREADY_LOCKED", "Hồ sơ nhân viên đã bị khóa.", HttpStatus.CONFLICT);
        }
        profile.setProfileStatus(ProfileStatus.LOCKED);
        profile.setLockedReason(request.reason().trim());
        profile.setLockedAt(LocalDateTime.now());
        profile.setLockedBy(getCurrentUser().getUserId());
        profile.setRestoredAt(null);
        profile.setRestoredBy(null);
        profile = profileRepository.save(profile);
        EmployeeProfileResponse response = employeeService.getProfile(employeeId);
        timelineService.record(employeeId, "PROFILE_LOCKED", "Khóa hồ sơ nhân viên", toJson(Map.of("reason", request.reason())));
        auditLogService.logSuccess("LOCK_PROFILE", "EMPLOYEE_PROFILE", "hr_employee_profile", employeeId.toString(), null, response, request.reason());
        return response;
    }

    @Transactional
    public EmployeeProfileResponse restoreProfile(Long employeeId, RestoreEmployeeProfileRequest request) {
        HrEmployeeProfile profile = getOrCreateProfile(getEmployee(employeeId));
        profile.setProfileStatus(ProfileStatus.ACTIVE);
        profile.setLockedReason(null);
        profile.setRestoredAt(LocalDateTime.now());
        profile.setRestoredBy(getCurrentUser().getUserId());
        profile = profileRepository.save(profile);
        EmployeeProfileResponse response = employeeService.getProfile(employeeId);
        String restoreReason = blankToNull(request.reason());
        timelineService.record(employeeId, "PROFILE_RESTORED", "Khôi phục hồ sơ nhân viên", toJson(Map.of("reason", restoreReason == null ? "" : restoreReason)));
        auditLogService.logSuccess("RESTORE_PROFILE", "EMPLOYEE_PROFILE", "hr_employee_profile", employeeId.toString(), null, response, request.reason());
        return response;
    }

    @Transactional(readOnly = true)
    public List<EmployeeProfileTimelineResponse> listTimeline(Long employeeId) {
        accessScopeService.assertCanReadEmployee(getEmployee(employeeId));
        return timelineRepository.findAllByEmployeeEmployeeIdOrderByEventAtDesc(employeeId).stream().map(item -> new EmployeeProfileTimelineResponse(item.getProfileTimelineId(), employeeId, item.getEventType(), item.getSummary(), item.getDetailJson(), item.getActorUser() == null ? null : item.getActorUser().getUsername(), item.getEventAt())).toList();
    }

    @Transactional(readOnly = true)
    public String exportEmployeeCsv(String keyword, EmploymentStatus employmentStatus, Long orgUnitId, Long jobTitleId) {
        PageResponse<EmployeeListItemResponse> page = employeeService.list(keyword, employmentStatus, orgUnitId, jobTitleId, 0, 10000);
        String header = "employeeCode,fullName,orgUnitCode,jobTitleCode,genderCode,dateOfBirth,hireDate,employmentStatus,workEmail,workPhone,workLocation,taxCode,personalEmail,mobilePhone,avatarUrl,note";

        return header + "\n" + page.items().stream()
                .map(item -> employeeService.getDetail(item.employeeId()))
                .map(row -> String.join(",",
                        csv(row.employeeCode()),
                        csv(row.fullName()),
                        csv(row.orgUnitCode()),
                        csv(row.jobTitleCode()),
                        csv(row.genderCode()),
                        csv(row.dateOfBirth()),
                        csv(row.hireDate()),
                        csv(row.employmentStatus()),
                        csv(row.workEmail()),
                        csv(row.workPhone()),
                        csv(row.workLocation()),
                        csv(row.taxCode()),
                        csv(row.personalEmail()),
                        csv(row.mobilePhone()),
                        csv(row.avatarUrl()),
                        csv(row.note())
                ))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    @Transactional
    public ImportResultResponse importEmployees(List<EmployeeImportRowRequest> requests) {
        int created = 0;
        int updated = 0;
        int skipped = 0;
        List<String> messages = new ArrayList<>();
        for (EmployeeImportRowRequest request : requests) {
            try {
                HrOrgUnit orgUnit = orgUnitRepository.findByOrgUnitCodeIgnoreCaseAndDeletedFalse(request.orgUnitCode()).orElseThrow(() -> new BusinessException("ORG_UNIT_NOT_FOUND", "orgUnitCode không tồn tại: " + request.orgUnitCode(), HttpStatus.BAD_REQUEST));
                HrJobTitle jobTitle = jobTitleRepository.findByJobTitleCodeIgnoreCaseAndDeletedFalse(request.jobTitleCode()).orElseThrow(() -> new BusinessException("JOB_TITLE_NOT_FOUND", "jobTitleCode không tồn tại: " + request.jobTitleCode(), HttpStatus.BAD_REQUEST));
                HrEmployee employee = employeeRepository.findOne((root, query, builder) -> builder.and(builder.isFalse(root.get("deleted")), builder.equal(builder.lower(root.get("employeeCode")), request.employeeCode().trim().toLowerCase()))).orElse(null);
                boolean isCreate = employee == null;
                if (isCreate) {
                    employee = new HrEmployee();
                    employee.setEmployeeCode(request.employeeCode().trim().toUpperCase());
                }
                employee.setFullName(request.fullName().trim());
                employee.setOrgUnit(orgUnit);
                employee.setJobTitle(jobTitle);
                employee.setManagerEmployee(request.managerEmployeeId() == null ? null : getEmployee(request.managerEmployeeId()));
                employee.setGenderCode(request.genderCode());
                employee.setDateOfBirth(request.dateOfBirth());
                employee.setHireDate(request.hireDate());
                employee.setEmploymentStatus(request.employmentStatus());
                employee.setWorkEmail(blankToNull(request.workEmail()));
                employee.setWorkPhone(blankToNull(request.workPhone()));
                employee.setWorkLocation(blankToNull(request.workLocation()));
                employee.setTaxCode(blankToNull(request.taxCode()));
                employee.setPersonalEmail(blankToNull(request.personalEmail()));
                employee.setMobilePhone(blankToNull(request.mobilePhone()));
                employee.setAvatarUrl(blankToNull(request.avatarUrl()));
                employee.setNote(blankToNull(request.note()));
                employee = employeeRepository.save(employee);

                HrEmployeeProfile profile = getOrCreateProfile(employee);
                profile.setNationality(blankToNull(request.nationality()));
                profile.setPlaceOfBirth(blankToNull(request.placeOfBirth()));
                profile.setEthnicGroup(blankToNull(request.ethnicGroup()));
                profile.setReligion(blankToNull(request.religion()));
                if (request.maritalStatus() != null && !request.maritalStatus().isBlank()) {
                    profile.setMaritalStatus(MaritalStatus.valueOf(request.maritalStatus().trim().toUpperCase()));
                }
                profile.setEducationLevel(blankToNull(request.educationLevel()));
                profile.setMajor(blankToNull(request.major()));
                profile.setEmergencyNote(blankToNull(request.emergencyNote()));
                profileRepository.save(profile);
                timelineService.record(employee.getEmployeeId(), "EMPLOYEE_IMPORT", "Import hồ sơ nhân sự", toJson(request));
                if (isCreate) created++; else updated++;
            } catch (RuntimeException ex) {
                skipped++;
                messages.add(request.employeeCode() + ": " + ex.getMessage());
            }
        }
        auditLogService.logSuccess("IMPORT", "EMPLOYEE", "hr_employee", null, null, Map.of("totalRows", requests.size(), "created", created, "updated", updated, "skipped", skipped), "Import hồ sơ nhân sự.");
        return new ImportResultResponse(requests.size(), created, updated, skipped, messages);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeListItemResponse> listManagedProfiles(String keyword, com.company.hrm.common.constant.EmploymentStatus employmentStatus, Long orgUnitId, Long jobTitleId, int page, int size) {
        return employeeService.list(keyword, employmentStatus, orgUnitId, jobTitleId, page, size);
    }

    private SecUserAccount getCurrentUser() {
        return SecurityUserContext.getCurrentUserId().flatMap(userAccountRepository::findById).orElseThrow(() -> new UnauthorizedException("AUTH_UNAUTHORIZED", "Chưa xác thực người dùng."));
    }

    private HrEmployee getEmployee(Long employeeId) {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId).orElseThrow(() -> new NotFoundException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân sự."));
    }

    private HrEmployeeProfileChangeRequest getChangeRequest(Long requestId) {
        return changeRequestRepository.findByProfileChangeRequestIdAndDeletedFalse(requestId).orElseThrow(() -> new NotFoundException("EMPLOYEE_PROFILE_REQUEST_NOT_FOUND", "Không tìm thấy yêu cầu cập nhật hồ sơ."));
    }

    private HrEmployeeProfile getOrCreateProfile(HrEmployee employee) {
        return profileRepository.findByEmployeeEmployeeIdAndDeletedFalse(employee.getEmployeeId()).orElseGet(() -> {
            HrEmployeeProfile profile = new HrEmployeeProfile();
            profile.setEmployee(employee);
            profile.setProfileStatus(ProfileStatus.ACTIVE);
            return profileRepository.save(profile);
        });
    }

    private void applyPayload(HrEmployee employee, HrEmployeeProfile profile, ProfileChangeRequestPayloadRequest payload) {
        employee.setPersonalEmail(blankToNull(payload.personalEmail()));
        employee.setMobilePhone(blankToNull(payload.mobilePhone()));
        employee.setAvatarUrl(blankToNull(payload.avatarUrl()));
        employee.setTaxCode(blankToNull(payload.taxCode()));
        profile.setNationality(blankToNull(payload.nationality()));
        profile.setPlaceOfBirth(blankToNull(payload.placeOfBirth()));
        profile.setEthnicGroup(blankToNull(payload.ethnicGroup()));
        profile.setReligion(blankToNull(payload.religion()));
        if (payload.maritalStatus() != null && !payload.maritalStatus().isBlank()) {
            profile.setMaritalStatus(MaritalStatus.valueOf(payload.maritalStatus().trim().toUpperCase()));
        }
        profile.setEducationLevel(blankToNull(payload.educationLevel()));
        profile.setMajor(blankToNull(payload.major()));
        profile.setEmergencyNote(blankToNull(payload.emergencyNote()));
    }

    private ProfileChangeRequestResponse toResponse(HrEmployeeProfileChangeRequest entity) {
        return new ProfileChangeRequestResponse(entity.getProfileChangeRequestId(), entity.getEmployee().getEmployeeId(), entity.getEmployee().getEmployeeCode(), entity.getEmployee().getFullName(), entity.getRequesterUser().getUsername(), entity.getRequestType(), entity.getRequestStatus().name(), entity.getPayloadJson(), entity.getSubmittedAt(), entity.getReviewedAt(), entity.getReviewerUser() == null ? null : entity.getReviewerUser().getUsername(), entity.getReviewNote());
    }

    private ProfileChangeRequestPayloadRequest fromJson(String json) {
        try {
            return objectMapper.readValue(json, ProfileChangeRequestPayloadRequest.class);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("EMPLOYEE_PROFILE_REQUEST_PAYLOAD_INVALID", "payloadJson không hợp lệ.", HttpStatus.BAD_REQUEST);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private String csv(Object value) {
        if (value == null) return "";
        String raw = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + raw + "\"";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
