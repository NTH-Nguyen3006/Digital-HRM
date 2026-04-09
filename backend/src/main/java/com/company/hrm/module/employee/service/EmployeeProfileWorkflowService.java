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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeProfileWorkflowService {

    private static final Set<String> SUPPORTED_PROFILE_CHANGE_FIELDS = Set.of(
            "personalEmail",
            "mobilePhone",
            "avatarUrl",
            "taxCode",
            "nationality",
            "placeOfBirth",
            "ethnicGroup",
            "religion",
            "maritalStatus",
            "educationLevel",
            "major",
            "emergencyNote"
    );

    private static final Map<String, String> LEGACY_PROFILE_FIELD_ALIASES = buildLegacyFieldAliases();

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
        assertPayloadHasChanges(request.payload());
        HrEmployeeProfileChangeRequest entity = new HrEmployeeProfileChangeRequest();
        entity.setEmployee(employee);
        entity.setRequesterUser(currentUser);
        entity.setRequestType("PROFILE_UPDATE");
        entity.setRequestStatus(ProfileChangeRequestStatus.PENDING);
        entity.setPayloadJson(toJson(request.payload()));
        entity.setSubmittedAt(LocalDateTime.now());
        entity = changeRequestRepository.save(entity);
        ProfileChangeRequestResponse response = toResponse(entity);
        recordTimelineSafely(employee.getEmployeeId(), "REQUEST_SUBMITTED", "Nhân viên gửi yêu cầu cập nhật hồ sơ", entity.getPayloadJson());
        auditLogSafely("SUBMIT_REQUEST", "EMPLOYEE_PROFILE_REQUEST", "hr_employee_profile_change_request", entity.getProfileChangeRequestId().toString(), response, request.reason());
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
        ProfileChangeRequestPayloadRequest payload = fromJson(entity.getPayloadJson());
        assertPayloadHasChanges(payload);
        if (request.approved()) {
            applyPayload(entity.getEmployee(), profile, payload);
            profileRepository.save(profile);
            employeeRepository.save(entity.getEmployee());
            entity.setRequestStatus(ProfileChangeRequestStatus.APPROVED);
            recordTimelineSafely(entity.getEmployee().getEmployeeId(), "REQUEST_APPROVED", "HR duyệt yêu cầu cập nhật hồ sơ", entity.getPayloadJson());
        } else {
            entity.setRequestStatus(ProfileChangeRequestStatus.REJECTED);
            recordTimelineSafely(entity.getEmployee().getEmployeeId(), "REQUEST_REJECTED", "HR từ chối yêu cầu cập nhật hồ sơ", entity.getPayloadJson());
        }
        entity.setReviewerUser(getCurrentUser());
        entity.setReviewedAt(LocalDateTime.now());
        entity.setReviewNote(blankToNull(request.reviewNote()));
        entity = changeRequestRepository.save(entity);
        ProfileChangeRequestResponse response = toResponse(entity);
        auditLogSafely("REVIEW_REQUEST", "EMPLOYEE_PROFILE_REQUEST", "hr_employee_profile_change_request", requestId.toString(), response, request.reviewNote());
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
        if (payload.personalEmail() != null) employee.setPersonalEmail(blankToNull(payload.personalEmail()));
        if (payload.mobilePhone() != null) employee.setMobilePhone(blankToNull(payload.mobilePhone()));
        if (payload.avatarUrl() != null) employee.setAvatarUrl(blankToNull(payload.avatarUrl()));
        if (payload.taxCode() != null) employee.setTaxCode(blankToNull(payload.taxCode()));
        if (payload.nationality() != null) profile.setNationality(blankToNull(payload.nationality()));
        if (payload.placeOfBirth() != null) profile.setPlaceOfBirth(blankToNull(payload.placeOfBirth()));
        if (payload.ethnicGroup() != null) profile.setEthnicGroup(blankToNull(payload.ethnicGroup()));
        if (payload.religion() != null) profile.setReligion(blankToNull(payload.religion()));
        if (payload.maritalStatus() != null) {
            profile.setMaritalStatus(normalizeMaritalStatus(payload.maritalStatus()));
        }
        if (payload.educationLevel() != null) profile.setEducationLevel(blankToNull(payload.educationLevel()));
        if (payload.major() != null) profile.setMajor(blankToNull(payload.major()));
        if (payload.emergencyNote() != null) profile.setEmergencyNote(blankToNull(payload.emergencyNote()));
    }

    private ProfileChangeRequestResponse toResponse(HrEmployeeProfileChangeRequest entity) {
        return new ProfileChangeRequestResponse(entity.getProfileChangeRequestId(), entity.getEmployee().getEmployeeId(), entity.getEmployee().getEmployeeCode(), entity.getEmployee().getFullName(), entity.getRequesterUser().getUsername(), entity.getRequestType(), entity.getRequestStatus().name(), entity.getPayloadJson(), entity.getSubmittedAt(), entity.getReviewedAt(), entity.getReviewerUser() == null ? null : entity.getReviewerUser().getUsername(), entity.getReviewNote());
    }

    private ProfileChangeRequestPayloadRequest fromJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root == null || !root.isObject()) {
                throw new BusinessException("EMPLOYEE_PROFILE_REQUEST_PAYLOAD_INVALID", "payloadJson không hợp lệ.", HttpStatus.BAD_REQUEST);
            }
            if (containsSupportedField(root)) {
                return objectMapper.treeToValue(root, ProfileChangeRequestPayloadRequest.class);
            }
            if (root.hasNonNull("fieldName") && root.has("newValue")) {
                String fieldName = normalizeLegacyField(root.path("fieldName").asText(null));
                String newValue = root.path("newValue").isNull() ? null : root.path("newValue").asText(null);
                if (fieldName == null) {
                    throw new BusinessException("EMPLOYEE_PROFILE_REQUEST_FIELD_UNSUPPORTED", "Trường thông tin cần cập nhật chưa được hệ thống hỗ trợ.", HttpStatus.BAD_REQUEST);
                }
                return toPayloadFromSingleField(fieldName, newValue);
            }
            throw new BusinessException("EMPLOYEE_PROFILE_REQUEST_PAYLOAD_INVALID", "payloadJson không hợp lệ.", HttpStatus.BAD_REQUEST);
        } catch (BusinessException exception) {
            throw exception;
        } catch (JsonProcessingException exception) {
            throw new BusinessException("EMPLOYEE_PROFILE_REQUEST_PAYLOAD_INVALID", "payloadJson không hợp lệ.", HttpStatus.BAD_REQUEST);
        }
    }

    private void assertPayloadHasChanges(ProfileChangeRequestPayloadRequest payload) {
        if (payload == null) {
            throw new BusinessException("EMPLOYEE_PROFILE_REQUEST_PAYLOAD_EMPTY", "Yêu cầu cập nhật hồ sơ không có dữ liệu thay đổi.", HttpStatus.BAD_REQUEST);
        }
        boolean hasChanges = payload.personalEmail() != null
                || payload.mobilePhone() != null
                || payload.avatarUrl() != null
                || payload.taxCode() != null
                || payload.nationality() != null
                || payload.placeOfBirth() != null
                || payload.ethnicGroup() != null
                || payload.religion() != null
                || payload.maritalStatus() != null
                || payload.educationLevel() != null
                || payload.major() != null
                || payload.emergencyNote() != null;
        if (!hasChanges) {
            throw new BusinessException("EMPLOYEE_PROFILE_REQUEST_PAYLOAD_EMPTY", "Yêu cầu cập nhật hồ sơ không có dữ liệu thay đổi.", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean containsSupportedField(JsonNode root) {
        return SUPPORTED_PROFILE_CHANGE_FIELDS.stream().anyMatch(root::has);
    }

    private ProfileChangeRequestPayloadRequest toPayloadFromSingleField(String fieldName, String newValue) {
        return new ProfileChangeRequestPayloadRequest(
                "personalEmail".equals(fieldName) ? newValue : null,
                "mobilePhone".equals(fieldName) ? newValue : null,
                "avatarUrl".equals(fieldName) ? newValue : null,
                "taxCode".equals(fieldName) ? newValue : null,
                "nationality".equals(fieldName) ? newValue : null,
                "placeOfBirth".equals(fieldName) ? newValue : null,
                "ethnicGroup".equals(fieldName) ? newValue : null,
                "religion".equals(fieldName) ? newValue : null,
                "maritalStatus".equals(fieldName) ? newValue : null,
                "educationLevel".equals(fieldName) ? newValue : null,
                "major".equals(fieldName) ? newValue : null,
                "emergencyNote".equals(fieldName) ? newValue : null
        );
    }

    private String normalizeLegacyField(String rawFieldName) {
        if (rawFieldName == null || rawFieldName.isBlank()) {
            return null;
        }
        String trimmed = rawFieldName.trim();
        if (SUPPORTED_PROFILE_CHANGE_FIELDS.contains(trimmed)) {
            return trimmed;
        }
        return LEGACY_PROFILE_FIELD_ALIASES.get(normalizeAlias(trimmed));
    }

    private MaritalStatus normalizeMaritalStatus(String rawValue) {
        String normalized = blankToNull(rawValue);
        if (normalized == null) {
            return null;
        }
        return switch (normalizeAlias(normalized)) {
            case "single", "doc than", "docthan" -> MaritalStatus.SINGLE;
            case "married", "da ket hon", "dakethon" -> MaritalStatus.MARRIED;
            case "divorced", "ly hon", "lyhon" -> MaritalStatus.DIVORCED;
            case "widowed", "goa" -> MaritalStatus.WIDOWED;
            default -> {
                try {
                    yield MaritalStatus.valueOf(normalized.toUpperCase());
                } catch (IllegalArgumentException exception) {
                    throw new BusinessException("EMPLOYEE_PROFILE_REQUEST_MARITAL_STATUS_INVALID", "Tình trạng hôn nhân không hợp lệ.", HttpStatus.BAD_REQUEST);
                }
            }
        };
    }

    private void recordTimelineSafely(Long employeeId, String eventType, String summary, String detailJson) {
        try {
            timelineService.record(employeeId, eventType, summary, detailJson);
        } catch (RuntimeException ignored) {
        }
    }

    private void auditLogSafely(String actionCode, String moduleCode, String entityName, String entityId, Object newData, String message) {
        try {
            auditLogService.logSuccess(actionCode, moduleCode, entityName, entityId, null, newData, message);
        } catch (RuntimeException ignored) {
        }
    }

    private static Map<String, String> buildLegacyFieldAliases() {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("email ca nhan", "personalEmail");
        aliases.put("personal email", "personalEmail");
        aliases.put("email", "personalEmail");
        aliases.put("so dien thoai", "mobilePhone");
        aliases.put("dien thoai", "mobilePhone");
        aliases.put("mobile phone", "mobilePhone");
        aliases.put("anh dai dien", "avatarUrl");
        aliases.put("avatar", "avatarUrl");
        aliases.put("ma so thue", "taxCode");
        aliases.put("mst", "taxCode");
        aliases.put("quoc tich", "nationality");
        aliases.put("noi sinh", "placeOfBirth");
        aliases.put("dan toc", "ethnicGroup");
        aliases.put("ton giao", "religion");
        aliases.put("tinh trang hon nhan", "maritalStatus");
        aliases.put("hon nhan", "maritalStatus");
        aliases.put("hoc van", "educationLevel");
        aliases.put("trinh do hoc van", "educationLevel");
        aliases.put("chuyen nganh", "major");
        aliases.put("ghi chu khan cap", "emergencyNote");
        aliases.put("ghi chu lien he khan cap", "emergencyNote");
        return aliases;
    }

    private static String normalizeAlias(String value) {
        return value == null
                ? null
                : java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                        .replaceAll("\\p{M}+", "")
                        .replaceAll("[^A-Za-z0-9]+", " ")
                        .trim()
                        .toLowerCase();
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
