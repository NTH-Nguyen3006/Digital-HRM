package com.company.hrm.module.employee.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.hrm.common.constant.DocumentStatus;
import com.company.hrm.common.constant.EmploymentStatus;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.employee.dto.CreateEmployeeRequest;
import com.company.hrm.module.employee.dto.EmergencyContactRequest;
import com.company.hrm.module.employee.dto.EmergencyContactResponse;
import com.company.hrm.module.employee.dto.EmployeeAddressRequest;
import com.company.hrm.module.employee.dto.EmployeeAddressResponse;
import com.company.hrm.module.employee.dto.EmployeeBankAccountRequest;
import com.company.hrm.module.employee.dto.EmployeeBankAccountResponse;
import com.company.hrm.module.employee.dto.EmployeeDetailResponse;
import com.company.hrm.module.employee.dto.EmployeeDocumentRequest;
import com.company.hrm.module.employee.dto.EmployeeDocumentResponse;
import com.company.hrm.module.employee.dto.EmployeeIdentificationRequest;
import com.company.hrm.module.employee.dto.EmployeeIdentificationResponse;
import com.company.hrm.module.employee.dto.EmployeeListItemResponse;
import com.company.hrm.module.employee.dto.EmployeeProfileRequest;
import com.company.hrm.module.employee.dto.EmployeeProfileResponse;
import com.company.hrm.module.employee.dto.TransferEmployeeRequest;
import com.company.hrm.module.employee.dto.UpdateEmployeeRequest;
import com.company.hrm.module.employee.dto.UpdateEmploymentStatusRequest;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.entity.HrEmployeeAddress;
import com.company.hrm.module.employee.entity.HrEmployeeBankAccount;
import com.company.hrm.module.employee.entity.HrEmployeeDocument;
import com.company.hrm.module.employee.entity.HrEmployeeEmergencyContact;
import com.company.hrm.module.employee.entity.HrEmployeeIdentification;
import com.company.hrm.module.employee.entity.HrEmployeeProfile;
import com.company.hrm.module.employee.repository.HrEmployeeAddressRepository;
import com.company.hrm.module.employee.repository.HrEmployeeBankAccountRepository;
import com.company.hrm.module.employee.repository.HrEmployeeDocumentRepository;
import com.company.hrm.module.employee.repository.HrEmployeeEmergencyContactRepository;
import com.company.hrm.module.employee.repository.HrEmployeeIdentificationRepository;
import com.company.hrm.module.employee.repository.HrEmployeeProfileRepository;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.jobtitle.entity.HrJobTitle;
import com.company.hrm.module.jobtitle.repository.HrJobTitleRepository;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.orgunit.repository.HrOrgUnitRepository;
import com.company.hrm.module.storage.dto.StoredFileReference;
import com.company.hrm.module.storage.service.StorageFileService;
import com.company.hrm.security.SecurityUserContext;

@Service
public class EmployeeService {

    private final HrEmployeeRepository employeeRepository;
    private final HrEmployeeProfileRepository profileRepository;
    private final HrEmployeeAddressRepository addressRepository;
    private final HrEmployeeEmergencyContactRepository emergencyContactRepository;
    private final HrEmployeeIdentificationRepository identificationRepository;
    private final HrEmployeeBankAccountRepository bankAccountRepository;
    private final HrEmployeeDocumentRepository documentRepository;
    private final HrOrgUnitRepository orgUnitRepository;
    private final HrJobTitleRepository jobTitleRepository;
    private final EmployeeAccessScopeService accessScopeService;
    private final EmployeeProfileTimelineService timelineService;
    private final AuditLogService auditLogService;
    private final StorageFileService storageFileService;

    public EmployeeService(
            HrEmployeeRepository employeeRepository,
            HrEmployeeProfileRepository profileRepository,
            HrEmployeeAddressRepository addressRepository,
            HrEmployeeEmergencyContactRepository emergencyContactRepository,
            HrEmployeeIdentificationRepository identificationRepository,
            HrEmployeeBankAccountRepository bankAccountRepository,
            HrEmployeeDocumentRepository documentRepository,
            HrOrgUnitRepository orgUnitRepository,
            HrJobTitleRepository jobTitleRepository,
            EmployeeAccessScopeService accessScopeService,
            EmployeeProfileTimelineService timelineService,
            AuditLogService auditLogService,
            StorageFileService storageFileService) {
        this.employeeRepository = employeeRepository;
        this.profileRepository = profileRepository;
        this.addressRepository = addressRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.identificationRepository = identificationRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.documentRepository = documentRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.jobTitleRepository = jobTitleRepository;
        this.accessScopeService = accessScopeService;
        this.timelineService = timelineService;
        this.auditLogService = auditLogService;
        this.storageFileService = storageFileService;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeListItemResponse> list(String keyword, EmploymentStatus employmentStatus,
            Long orgUnitId, Long jobTitleId, int page, int size) {
        Specification<HrEmployee> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        specification = accessScopeService.applyEmployeeReadScope(specification);
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("employeeCode")), likeValue),
                    builder.like(builder.lower(root.get("fullName")), likeValue),
                    builder.like(builder.lower(root.get("workEmail")), likeValue),
                    builder.like(builder.lower(root.get("mobilePhone")), likeValue)));
        }
        if (employmentStatus != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.get("employmentStatus"), employmentStatus));
        }
        if (orgUnitId != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.get("orgUnit").get("orgUnitId"), orgUnitId));
        }
        if (jobTitleId != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.get("jobTitle").get("jobTitleId"), jobTitleId));
        }
        Page<HrEmployee> result = employeeRepository.findAll(specification,
                PageRequest.of(page, size, Sort.by("employeeCode")));
        List<EmployeeListItemResponse> items = result.getContent().stream().map(this::toListItem).toList();
        return new PageResponse<>(items, page, size, result.getTotalElements(), result.getTotalPages(),
                result.hasNext(), result.hasPrevious());
    }

    @Transactional(readOnly = true)
    public EmployeeDetailResponse getDetail(Long employeeId) {
        HrEmployee employee = getEmployee(employeeId);
        accessScopeService.assertCanReadEmployee(employee);
        return toDetail(employee);
    }

    @Transactional
    public EmployeeDetailResponse create(CreateEmployeeRequest request) {
        validateCreateOrUpdate(null, request.employeeCode(), request.workEmail(), request.dateOfBirth(),
                request.hireDate(), request.managerEmployeeId());
        HrOrgUnit orgUnit = getOrgUnit(request.orgUnitId());
        HrJobTitle jobTitle = getJobTitle(request.jobTitleId());
        HrEmployee manager = request.managerEmployeeId() == null ? null : getEmployee(request.managerEmployeeId());

        HrEmployee employee = new HrEmployee();
        employee.setEmployeeCode(normalizeCode(request.employeeCode()));
        employee.setOrgUnit(orgUnit);
        employee.setJobTitle(jobTitle);
        employee.setManagerEmployee(manager);
        employee.setFullName(request.fullName().trim());
        employee.setWorkEmail(blankToNull(request.workEmail()));
        employee.setWorkPhone(blankToNull(request.workPhone()));
        employee.setGenderCode(request.genderCode());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setHireDate(request.hireDate());
        employee.setEmploymentStatus(request.employmentStatus());
        employee.setWorkLocation(blankToNull(request.workLocation()));
        employee.setTaxCode(blankToNull(request.taxCode()));
        employee.setPersonalEmail(blankToNull(request.personalEmail()));
        employee.setMobilePhone(blankToNull(request.mobilePhone()));
        employee.setAvatarUrl(blankToNull(request.avatarUrl()));
        employee.setNote(blankToNull(request.note()));
        employee = employeeRepository.save(employee);
        EmployeeDetailResponse response = toDetail(employee);
        auditLogService.logSuccess("CREATE", "EMPLOYEE", "hr_employee", employee.getEmployeeId().toString(), null,
                response, "Tạo mới hồ sơ nhân sự.");
        timelineService.record(employee.getEmployeeId(), "EMPLOYEE_CREATED", "Tạo mới hồ sơ nhân sự", null);
        return response;
    }

    @Transactional
    public EmployeeDetailResponse update(Long employeeId, UpdateEmployeeRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        EmployeeDetailResponse oldSnapshot = toDetail(employee);
        validateCreateOrUpdate(employeeId, request.employeeCode(), request.workEmail(), request.dateOfBirth(),
                request.hireDate(), request.managerEmployeeId());
        employee.setEmployeeCode(normalizeCode(request.employeeCode()));
        employee.setOrgUnit(getOrgUnit(request.orgUnitId()));
        employee.setJobTitle(getJobTitle(request.jobTitleId()));
        employee.setManagerEmployee(
                request.managerEmployeeId() == null ? null : getEmployee(request.managerEmployeeId()));
        employee.setFullName(request.fullName().trim());
        employee.setWorkEmail(blankToNull(request.workEmail()));
        employee.setWorkPhone(blankToNull(request.workPhone()));
        employee.setGenderCode(request.genderCode());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setHireDate(request.hireDate());
        employee.setWorkLocation(blankToNull(request.workLocation()));
        employee.setTaxCode(blankToNull(request.taxCode()));
        employee.setPersonalEmail(blankToNull(request.personalEmail()));
        employee.setMobilePhone(blankToNull(request.mobilePhone()));
        employee.setAvatarUrl(blankToNull(request.avatarUrl()));
        employee.setNote(blankToNull(request.note()));
        employee = employeeRepository.save(employee);
        EmployeeDetailResponse response = toDetail(employee);
        auditLogService.logSuccess("UPDATE", "EMPLOYEE", "hr_employee", employeeId.toString(), oldSnapshot, response,
                "Cập nhật hồ sơ nhân sự.");
        timelineService.record(employeeId, "EMPLOYEE_UPDATED", "Cập nhật hồ sơ nhân sự", null);
        return response;
    }

    @Transactional
    public EmployeeDetailResponse changeEmploymentStatus(Long employeeId, UpdateEmploymentStatusRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        EmployeeDetailResponse oldSnapshot = toDetail(employee);
        employee.setEmploymentStatus(request.employmentStatus());
        employee = employeeRepository.save(employee);
        EmployeeDetailResponse response = toDetail(employee);
        auditLogService.logSuccess("CHANGE_STATUS", "EMPLOYEE", "hr_employee", employeeId.toString(), oldSnapshot,
                response, request.note());
        timelineService.record(employeeId, "EMPLOYMENT_STATUS_CHANGED", "Cập nhật trạng thái lao động", null);
        return response;
    }

    @Transactional
    public EmployeeDetailResponse transfer(Long employeeId, TransferEmployeeRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        EmployeeDetailResponse oldSnapshot = toDetail(employee);
        employee.setOrgUnit(getOrgUnit(request.targetOrgUnitId()));
        employee.setManagerEmployee(
                request.targetManagerEmployeeId() == null ? null : getEmployee(request.targetManagerEmployeeId()));
        employee = employeeRepository.save(employee);
        EmployeeDetailResponse response = toDetail(employee);
        auditLogService.logSuccess("TRANSFER", "EMPLOYEE", "hr_employee", employeeId.toString(), oldSnapshot, response,
                request.note());
        timelineService.record(employeeId, "EMPLOYEE_TRANSFERRED", "Điều chuyển nhân sự", null);
        return response;
    }

    @Transactional(readOnly = true)
    public EmployeeProfileResponse getProfile(Long employeeId) {
        HrEmployee employee = getEmployee(employeeId);
        accessScopeService.assertCanReadEmployee(employee);
        return profileRepository.findByEmployeeEmployeeIdAndDeletedFalse(employeeId).map(this::toProfileResponse)
                .orElse(null);
    }

    @Transactional
    public EmployeeProfileResponse upsertProfile(Long employeeId, EmployeeProfileRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        HrEmployeeProfile profile = profileRepository.findByEmployeeEmployeeIdAndDeletedFalse(employeeId)
                .orElseGet(() -> {
                    HrEmployeeProfile created = new HrEmployeeProfile();
                    created.setEmployee(employee);
                    created.setProfileStatus(com.company.hrm.common.constant.ProfileStatus.ACTIVE);
                    return created;
                });
        EmployeeProfileResponse oldSnapshot = profile.getEmployeeProfileId() == null ? null
                : toProfileResponse(profile);
        applyProfile(profile, request);
        profile = profileRepository.save(profile);
        EmployeeProfileResponse response = toProfileResponse(profile);
        auditLogService.logSuccess("UPSERT_PROFILE", "EMPLOYEE_PROFILE", "hr_employee_profile", employeeId.toString(),
                oldSnapshot, response, "Cập nhật hồ sơ mở rộng.");
        timelineService.record(employeeId, "PROFILE_UPDATED", "Cập nhật hồ sơ mở rộng", null);
        return response;
    }

    @Transactional(readOnly = true)
    public List<EmployeeAddressResponse> listAddresses(Long employeeId) {
        accessScopeService.assertCanReadEmployee(getEmployee(employeeId));
        return addressRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmployeeAddressIdAsc(employeeId).stream()
                .map(this::toAddressResponse).toList();
    }

    @Transactional
    public EmployeeAddressResponse createAddress(Long employeeId, EmployeeAddressRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        HrEmployeeAddress entity = new HrEmployeeAddress();
        entity.setEmployee(employee);
        applyAddress(entity, request);
        if (request.primary()) {
            clearPrimaryAddresses(employeeId);
        }
        entity = addressRepository.save(entity);
        EmployeeAddressResponse response = toAddressResponse(entity);
        auditLogService.logSuccess("CREATE", "EMPLOYEE_PROFILE", "hr_employee_address",
                entity.getEmployeeAddressId().toString(), null, response, "Tạo địa chỉ nhân sự.");
        return response;
    }

    @Transactional
    public EmployeeAddressResponse updateAddress(Long employeeId, Long addressId, EmployeeAddressRequest request) {
        getEmployee(employeeId);
        HrEmployeeAddress entity = getAddress(addressId);
        validateOwnership(employeeId, entity.getEmployee().getEmployeeId(), "ADDRESS_NOT_BELONG_TO_EMPLOYEE");
        EmployeeAddressResponse oldSnapshot = toAddressResponse(entity);
        if (request.primary()) {
            clearPrimaryAddresses(employeeId);
        }
        applyAddress(entity, request);
        entity = addressRepository.save(entity);
        EmployeeAddressResponse response = toAddressResponse(entity);
        auditLogService.logSuccess("UPDATE", "EMPLOYEE_PROFILE", "hr_employee_address", addressId.toString(),
                oldSnapshot, response, "Cập nhật địa chỉ nhân sự.");
        return response;
    }

    @Transactional
    public void deleteAddress(Long employeeId, Long addressId) {
        getEmployee(employeeId);
        HrEmployeeAddress entity = getAddress(addressId);
        validateOwnership(employeeId, entity.getEmployee().getEmployeeId(), "ADDRESS_NOT_BELONG_TO_EMPLOYEE");
        EmployeeAddressResponse oldSnapshot = toAddressResponse(entity);
        entity.setDeleted(true);
        addressRepository.save(entity);
        auditLogService.logSuccess("DELETE_SOFT", "EMPLOYEE_PROFILE", "hr_employee_address", addressId.toString(),
                oldSnapshot, null, "Xóa mềm địa chỉ nhân sự.");
    }

    @Transactional(readOnly = true)
    public List<EmergencyContactResponse> listEmergencyContacts(Long employeeId) {
        accessScopeService.assertCanReadEmployee(getEmployee(employeeId));
        return emergencyContactRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmergencyContactIdAsc(employeeId).stream()
                .map(this::toEmergencyContactResponse).toList();
    }

    @Transactional
    public EmergencyContactResponse createEmergencyContact(Long employeeId, EmergencyContactRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        HrEmployeeEmergencyContact entity = new HrEmployeeEmergencyContact();
        entity.setEmployee(employee);
        if (request.primary()) {
            clearPrimaryEmergencyContacts(employeeId);
        }
        applyEmergencyContact(entity, request);
        entity = emergencyContactRepository.save(entity);
        EmergencyContactResponse response = toEmergencyContactResponse(entity);
        auditLogService.logSuccess("CREATE", "EMPLOYEE_PROFILE", "hr_employee_emergency_contact",
                entity.getEmergencyContactId().toString(), null, response, "Tạo liên hệ khẩn cấp.");
        return response;
    }

    @Transactional
    public EmergencyContactResponse updateEmergencyContact(Long employeeId, Long emergencyContactId,
            EmergencyContactRequest request) {
        getEmployee(employeeId);
        HrEmployeeEmergencyContact entity = getEmergencyContact(emergencyContactId);
        validateOwnership(employeeId, entity.getEmployee().getEmployeeId(), "EMERGENCY_CONTACT_NOT_BELONG_TO_EMPLOYEE");
        EmergencyContactResponse oldSnapshot = toEmergencyContactResponse(entity);
        if (request.primary()) {
            clearPrimaryEmergencyContacts(employeeId);
        }
        applyEmergencyContact(entity, request);
        entity = emergencyContactRepository.save(entity);
        EmergencyContactResponse response = toEmergencyContactResponse(entity);
        auditLogService.logSuccess("UPDATE", "EMPLOYEE_PROFILE", "hr_employee_emergency_contact",
                emergencyContactId.toString(), oldSnapshot, response, "Cập nhật liên hệ khẩn cấp.");
        return response;
    }

    @Transactional
    public void deleteEmergencyContact(Long employeeId, Long emergencyContactId) {
        getEmployee(employeeId);
        HrEmployeeEmergencyContact entity = getEmergencyContact(emergencyContactId);
        validateOwnership(employeeId, entity.getEmployee().getEmployeeId(), "EMERGENCY_CONTACT_NOT_BELONG_TO_EMPLOYEE");
        EmergencyContactResponse oldSnapshot = toEmergencyContactResponse(entity);
        entity.setDeleted(true);
        emergencyContactRepository.save(entity);
        auditLogService.logSuccess("DELETE_SOFT", "EMPLOYEE_PROFILE", "hr_employee_emergency_contact",
                emergencyContactId.toString(), oldSnapshot, null, "Xóa mềm liên hệ khẩn cấp.");
    }

    @Transactional(readOnly = true)
    public List<EmployeeIdentificationResponse> listIdentifications(Long employeeId) {
        accessScopeService.assertCanReadEmployee(getEmployee(employeeId));
        return identificationRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmployeeIdentificationIdAsc(employeeId)
                .stream().map(this::toIdentificationResponse).toList();
    }

    @Transactional
    public EmployeeIdentificationResponse createIdentification(Long employeeId, EmployeeIdentificationRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        validateIdentificationUnique(null, request);
        if (request.primary()) {
            clearPrimaryIdentifications(employeeId);
        }
        HrEmployeeIdentification entity = new HrEmployeeIdentification();
        entity.setEmployee(employee);
        applyIdentification(entity, request);
        entity = identificationRepository.save(entity);
        EmployeeIdentificationResponse response = toIdentificationResponse(entity);
        auditLogService.logSuccess("CREATE", "EMPLOYEE_PROFILE", "hr_employee_identification",
                entity.getEmployeeIdentificationId().toString(), null, response, "Tạo giấy tờ định danh.");
        return response;
    }

    @Transactional
    public EmployeeIdentificationResponse updateIdentification(Long employeeId, Long identificationId,
            EmployeeIdentificationRequest request) {
        getEmployee(employeeId);
        HrEmployeeIdentification entity = getIdentification(identificationId);
        validateOwnership(employeeId, entity.getEmployee().getEmployeeId(), "IDENTIFICATION_NOT_BELONG_TO_EMPLOYEE");
        validateIdentificationUnique(identificationId, request);
        EmployeeIdentificationResponse oldSnapshot = toIdentificationResponse(entity);
        if (request.primary()) {
            clearPrimaryIdentifications(employeeId);
        }
        applyIdentification(entity, request);
        entity = identificationRepository.save(entity);
        EmployeeIdentificationResponse response = toIdentificationResponse(entity);
        auditLogService.logSuccess("UPDATE", "EMPLOYEE_PROFILE", "hr_employee_identification",
                identificationId.toString(), oldSnapshot, response, "Cập nhật giấy tờ định danh.");
        return response;
    }

    @Transactional
    public void deleteIdentification(Long employeeId, Long identificationId) {
        getEmployee(employeeId);
        HrEmployeeIdentification entity = getIdentification(identificationId);
        validateOwnership(employeeId, entity.getEmployee().getEmployeeId(), "IDENTIFICATION_NOT_BELONG_TO_EMPLOYEE");
        EmployeeIdentificationResponse oldSnapshot = toIdentificationResponse(entity);
        entity.setDeleted(true);
        identificationRepository.save(entity);
        auditLogService.logSuccess("DELETE_SOFT", "EMPLOYEE_PROFILE", "hr_employee_identification",
                identificationId.toString(), oldSnapshot, null, "Xóa mềm giấy tờ định danh.");
    }

    @Transactional(readOnly = true)
    public List<EmployeeBankAccountResponse> listBankAccounts(Long employeeId) {
        accessScopeService.assertCanReadEmployee(getEmployee(employeeId));
        return bankAccountRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmployeeBankAccountIdAsc(employeeId)
                .stream().map(this::toBankAccountResponse).toList();
    }

    @Transactional
    public EmployeeBankAccountResponse createBankAccount(Long employeeId, EmployeeBankAccountRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        if (request.primary()) {
            clearPrimaryBankAccounts(employeeId);
        }
        HrEmployeeBankAccount entity = new HrEmployeeBankAccount();
        entity.setEmployee(employee);
        applyBankAccount(entity, request);
        entity = bankAccountRepository.save(entity);
        EmployeeBankAccountResponse response = toBankAccountResponse(entity);
        auditLogService.logSuccess("CREATE", "EMPLOYEE_PROFILE", "hr_employee_bank_account",
                entity.getEmployeeBankAccountId().toString(), null, response, "Tạo tài khoản ngân hàng.");
        return response;
    }

    @Transactional
    public EmployeeBankAccountResponse updateBankAccount(Long employeeId, Long bankAccountId,
            EmployeeBankAccountRequest request) {
        getEmployee(employeeId);
        HrEmployeeBankAccount entity = getBankAccount(bankAccountId);
        validateOwnership(employeeId, entity.getEmployee().getEmployeeId(), "BANK_ACCOUNT_NOT_BELONG_TO_EMPLOYEE");
        EmployeeBankAccountResponse oldSnapshot = toBankAccountResponse(entity);
        if (request.primary()) {
            clearPrimaryBankAccounts(employeeId);
        }
        applyBankAccount(entity, request);
        entity = bankAccountRepository.save(entity);
        EmployeeBankAccountResponse response = toBankAccountResponse(entity);
        auditLogService.logSuccess("UPDATE", "EMPLOYEE_PROFILE", "hr_employee_bank_account", bankAccountId.toString(),
                oldSnapshot, response, "Cập nhật tài khoản ngân hàng.");
        return response;
    }

    @Transactional
    public void deleteBankAccount(Long employeeId, Long bankAccountId) {
        getEmployee(employeeId);
        HrEmployeeBankAccount entity = getBankAccount(bankAccountId);
        validateOwnership(employeeId, entity.getEmployee().getEmployeeId(), "BANK_ACCOUNT_NOT_BELONG_TO_EMPLOYEE");
        EmployeeBankAccountResponse oldSnapshot = toBankAccountResponse(entity);
        entity.setDeleted(true);
        bankAccountRepository.save(entity);
        auditLogService.logSuccess("DELETE_SOFT", "EMPLOYEE_PROFILE", "hr_employee_bank_account",
                bankAccountId.toString(), oldSnapshot, null, "Xóa mềm tài khoản ngân hàng.");
    }

    @Transactional(readOnly = true)
    public List<EmployeeDocumentResponse> listDocuments(Long employeeId) {
        accessScopeService.assertCanReadEmployee(getEmployee(employeeId));
        return documentRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByUploadedAtDescEmployeeDocumentIdDesc(employeeId)
                .stream().map(this::toDocumentResponse).toList();
    }

    @Transactional
    public EmployeeDocumentResponse createDocument(Long employeeId, EmployeeDocumentRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        HrEmployeeDocument entity = new HrEmployeeDocument();
        entity.setEmployee(employee);
        entity.setUploadedAt(LocalDateTime.now());
        entity.setUploadedBy(SecurityUserContext.getCurrentUserId().orElse(null));
        applyDocument(entity, request);
        entity = documentRepository.save(entity);
        EmployeeDocumentResponse response = toDocumentResponse(entity);
        auditLogService.logSuccess("CREATE", "EMPLOYEE_DOCUMENT", "hr_employee_document",
                entity.getEmployeeDocumentId().toString(), null, response, "Tạo metadata tài liệu nhân sự.");
        timelineService.record(employeeId, "DOCUMENT_CREATED", "Thêm tài liệu đính kèm", null);
        return response;
    }

    @Transactional
    public EmployeeDocumentResponse updateDocument(Long employeeId, Long documentId, EmployeeDocumentRequest request) {
        getEmployee(employeeId);
        HrEmployeeDocument entity = getDocument(documentId);
        validateOwnership(employeeId, entity.getEmployee().getEmployeeId(), "DOCUMENT_NOT_BELONG_TO_EMPLOYEE");
        EmployeeDocumentResponse oldSnapshot = toDocumentResponse(entity);
        applyDocument(entity, request);
        entity = documentRepository.save(entity);
        EmployeeDocumentResponse response = toDocumentResponse(entity);
        auditLogService.logSuccess("UPDATE", "EMPLOYEE_DOCUMENT", "hr_employee_document", documentId.toString(),
                oldSnapshot, response, "Cập nhật metadata tài liệu nhân sự.");
        timelineService.record(employeeId, "DOCUMENT_UPDATED", "Cập nhật metadata tài liệu", null);
        return response;
    }

    @Transactional
    public void deleteDocument(Long employeeId, Long documentId) {
        getEmployee(employeeId);
        HrEmployeeDocument entity = getDocument(documentId);
        validateOwnership(employeeId, entity.getEmployee().getEmployeeId(), "DOCUMENT_NOT_BELONG_TO_EMPLOYEE");
        EmployeeDocumentResponse oldSnapshot = toDocumentResponse(entity);
        entity.setDeleted(true);
        entity.setStatus(DocumentStatus.ARCHIVED);
        documentRepository.save(entity);
        auditLogService.logSuccess("DELETE_SOFT", "EMPLOYEE_DOCUMENT", "hr_employee_document", documentId.toString(),
                oldSnapshot, null, "Xóa mềm metadata tài liệu nhân sự.");
        timelineService.record(employeeId, "DOCUMENT_ARCHIVED", "Xóa mềm metadata tài liệu", null);
    }

    private void validateCreateOrUpdate(Long employeeId, String employeeCode, String workEmail, LocalDate dateOfBirth,
            LocalDate hireDate, Long managerEmployeeId) {
        if (employeeRepository.existsByEmployeeCodeIgnoreCaseAndDeletedFalseAndEmployeeIdNot(employeeCode,
                employeeId == null ? -1L : employeeId)) {
            throw new BusinessException("EMPLOYEE_CODE_DUPLICATE", "employeeCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        if (workEmail != null && !workEmail.isBlank()) {
            boolean duplicate = employeeId == null
                    ? employeeRepository.existsByWorkEmailIgnoreCaseAndDeletedFalse(workEmail)
                    : employeeRepository.existsByWorkEmailIgnoreCaseAndDeletedFalseAndEmployeeIdNot(workEmail,
                            employeeId);
            if (duplicate) {
                throw new BusinessException("EMPLOYEE_WORK_EMAIL_DUPLICATE", "workEmail đã tồn tại.",
                        HttpStatus.CONFLICT);
            }
        }
        if (hireDate.isBefore(dateOfBirth)) {
            throw new BusinessException("EMPLOYEE_HIRE_DATE_INVALID", "hireDate không được nhỏ hơn dateOfBirth.",
                    HttpStatus.BAD_REQUEST);
        }
        if (managerEmployeeId != null && employeeId != null && managerEmployeeId.equals(employeeId)) {
            throw new BusinessException("EMPLOYEE_MANAGER_INVALID",
                    "Nhân sự không thể tự là quản lý trực tiếp của chính mình.", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateIdentificationUnique(Long identificationId, EmployeeIdentificationRequest request) {
        boolean duplicate = identificationId == null
                ? identificationRepository.existsByDocumentTypeAndDocumentNumberAndDeletedFalse(request.documentType(),
                        request.documentNumber())
                : identificationRepository
                        .existsByDocumentTypeAndDocumentNumberAndDeletedFalseAndEmployeeIdentificationIdNot(
                                request.documentType(), request.documentNumber(), identificationId);
        if (duplicate) {
            throw new BusinessException("EMPLOYEE_IDENTIFICATION_DUPLICATE",
                    "documentType + documentNumber đã tồn tại.", HttpStatus.CONFLICT);
        }
        if (request.issueDate() != null && request.expiryDate() != null
                && request.expiryDate().isBefore(request.issueDate())) {
            throw new BusinessException("EMPLOYEE_IDENTIFICATION_DATE_INVALID",
                    "expiryDate không được nhỏ hơn issueDate.", HttpStatus.BAD_REQUEST);
        }
    }

    private void applyProfile(HrEmployeeProfile entity, EmployeeProfileRequest request) {
        entity.setFirstName(blankToNull(request.firstName()));
        entity.setMiddleName(blankToNull(request.middleName()));
        entity.setLastName(blankToNull(request.lastName()));
        entity.setMaritalStatus(request.maritalStatus());
        entity.setNationality(blankToNull(request.nationality()));
        entity.setPlaceOfBirth(blankToNull(request.placeOfBirth()));
        entity.setEthnicGroup(blankToNull(request.ethnicGroup()));
        entity.setReligion(blankToNull(request.religion()));
        entity.setEducationLevel(blankToNull(request.educationLevel()));
        entity.setMajor(blankToNull(request.major()));
        entity.setEmergencyNote(blankToNull(request.emergencyNote()));
    }

    private void applyAddress(HrEmployeeAddress entity, EmployeeAddressRequest request) {
        entity.setAddressType(request.addressType());
        entity.setCountryName(blankToNull(request.countryName()));
        entity.setProvinceName(blankToNull(request.provinceName()));
        entity.setDistrictName(blankToNull(request.districtName()));
        entity.setWardName(blankToNull(request.wardName()));
        entity.setAddressLine(request.addressLine().trim());
        entity.setPostalCode(blankToNull(request.postalCode()));
        entity.setPrimary(request.primary());
    }

    private void applyEmergencyContact(HrEmployeeEmergencyContact entity, EmergencyContactRequest request) {
        entity.setContactName(request.contactName().trim());
        entity.setRelationshipCode(request.relationshipCode());
        entity.setPhoneNumber(request.phoneNumber().trim());
        entity.setEmail(blankToNull(request.email()));
        entity.setAddressLine(blankToNull(request.addressLine()));
        entity.setPrimary(request.primary());
        entity.setNote(blankToNull(request.note()));
    }

    private void applyIdentification(HrEmployeeIdentification entity, EmployeeIdentificationRequest request) {
        entity.setDocumentType(request.documentType());
        entity.setDocumentNumber(request.documentNumber().trim());
        entity.setIssueDate(request.issueDate());
        entity.setIssuePlace(blankToNull(request.issuePlace()));
        entity.setExpiryDate(request.expiryDate());
        entity.setCountryOfIssue(blankToNull(request.countryOfIssue()));
        entity.setPrimary(request.primary());
        entity.setStatus(request.status());
    }

    private void applyBankAccount(HrEmployeeBankAccount entity, EmployeeBankAccountRequest request) {
        entity.setBankName(request.bankName().trim());
        entity.setBankCode(blankToNull(request.bankCode()));
        entity.setAccountNumber(request.accountNumber().trim());
        entity.setAccountHolderName(request.accountHolderName().trim());
        entity.setBranchName(blankToNull(request.branchName()));
        entity.setPrimary(request.primary());
        entity.setStatus(request.status());
    }

    private void applyDocument(HrEmployeeDocument entity, EmployeeDocumentRequest request) {
        StoredFileReference storedFile = storageFileService.requireActiveReference(
                request.storagePath(),
                "EMPLOYEE_DOCUMENT_STORAGE_NOT_FOUND",
                "storagePath không tồn tại hoặc không còn hiệu lực.");
        entity.setDocumentCategory(request.documentCategory());
        entity.setDocumentName(request.documentName().trim());
        entity.setStoragePath(storedFile.fileKey());
        entity.setMimeType(storedFile.mimeType());
        entity.setFileSizeBytes(storedFile.fileSizeBytes());
        entity.setStatus(request.status());
        entity.setNote(blankToNull(request.note()));
    }

    private void clearPrimaryAddresses(Long employeeId) {
        addressRepository.findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmployeeAddressIdAsc(employeeId)
                .forEach(item -> item.setPrimary(false));
    }

    private void clearPrimaryEmergencyContacts(Long employeeId) {
        emergencyContactRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmergencyContactIdAsc(employeeId)
                .forEach(item -> item.setPrimary(false));
    }

    private void clearPrimaryIdentifications(Long employeeId) {
        identificationRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmployeeIdentificationIdAsc(employeeId)
                .forEach(item -> item.setPrimary(false));
    }

    private void clearPrimaryBankAccounts(Long employeeId) {
        bankAccountRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmployeeBankAccountIdAsc(employeeId)
                .forEach(item -> item.setPrimary(false));
    }

    private HrEmployee getEmployee(Long employeeId) {
        HrEmployee employee = employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân sự."));
        accessScopeService.assertCanReadEmployee(employee);
        return employee;
    }

    private HrOrgUnit getOrgUnit(Long orgUnitId) {
        return orgUnitRepository.findByOrgUnitIdAndDeletedFalse(orgUnitId)
                .orElseThrow(() -> new NotFoundException("ORG_UNIT_NOT_FOUND", "Không tìm thấy đơn vị tổ chức."));
    }

    private HrJobTitle getJobTitle(Long jobTitleId) {
        return jobTitleRepository.findByJobTitleIdAndDeletedFalse(jobTitleId)
                .orElseThrow(() -> new NotFoundException("JOB_TITLE_NOT_FOUND", "Không tìm thấy chức danh."));
    }

    private HrEmployeeAddress getAddress(Long addressId) {
        return addressRepository.findByEmployeeAddressIdAndDeletedFalse(addressId)
                .orElseThrow(
                        () -> new NotFoundException("EMPLOYEE_ADDRESS_NOT_FOUND", "Không tìm thấy địa chỉ nhân sự."));
    }

    private HrEmployeeEmergencyContact getEmergencyContact(Long id) {
        return emergencyContactRepository.findByEmergencyContactIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE_EMERGENCY_CONTACT_NOT_FOUND",
                        "Không tìm thấy liên hệ khẩn cấp."));
    }

    private HrEmployeeIdentification getIdentification(Long id) {
        return identificationRepository.findByEmployeeIdentificationIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE_IDENTIFICATION_NOT_FOUND",
                        "Không tìm thấy giấy tờ định danh."));
    }

    private HrEmployeeBankAccount getBankAccount(Long id) {
        return bankAccountRepository.findByEmployeeBankAccountIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE_BANK_ACCOUNT_NOT_FOUND",
                        "Không tìm thấy tài khoản ngân hàng."));
    }

    private HrEmployeeDocument getDocument(Long id) {
        return documentRepository.findByEmployeeDocumentIdAndDeletedFalse(id)
                .orElseThrow(
                        () -> new NotFoundException("EMPLOYEE_DOCUMENT_NOT_FOUND", "Không tìm thấy tài liệu nhân sự."));
    }

    private void validateOwnership(Long expectedEmployeeId, Long actualEmployeeId, String code) {
        if (!expectedEmployeeId.equals(actualEmployeeId)) {
            throw new BusinessException(code, "Dữ liệu không thuộc về nhân sự được chỉ định.", HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private EmployeeListItemResponse toListItem(HrEmployee entity) {
        return new EmployeeListItemResponse(
                entity.getEmployeeId(), entity.getEmployeeCode(), entity.getFullName(), entity.getWorkEmail(),
                entity.getWorkPhone(),
                entity.getGenderCode().name(), entity.getEmploymentStatus().name(),
                entity.getOrgUnit().getOrgUnitId(), entity.getOrgUnit().getOrgUnitCode(),
                entity.getOrgUnit().getOrgUnitName(),
                entity.getJobTitle().getJobTitleId(), entity.getJobTitle().getJobTitleCode(),
                entity.getJobTitle().getJobTitleName(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeId(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeCode(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getFullName());
    }

    private EmployeeDetailResponse toDetail(HrEmployee entity) {
        return new EmployeeDetailResponse(
                entity.getEmployeeId(), entity.getEmployeeCode(), entity.getFullName(), entity.getWorkEmail(),
                entity.getWorkPhone(), entity.getGenderCode().name(), entity.getDateOfBirth(), entity.getHireDate(),
                entity.getEmploymentStatus().name(), entity.getWorkLocation(), entity.getTaxCode(),
                entity.getPersonalEmail(), entity.getMobilePhone(), entity.getAvatarUrl(), entity.getNote(),
                entity.getOrgUnit().getOrgUnitId(), entity.getOrgUnit().getOrgUnitCode(),
                entity.getOrgUnit().getOrgUnitName(), entity.getOrgUnit().getOrgUnitType().name(),
                entity.getJobTitle().getJobTitleId(), entity.getJobTitle().getJobTitleCode(),
                entity.getJobTitle().getJobTitleName(), entity.getJobTitle().getJobLevelCode(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeId(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeCode(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getFullName());
    }

    private EmployeeProfileResponse toProfileResponse(HrEmployeeProfile entity) {
        return new EmployeeProfileResponse(entity.getEmployeeProfileId(), entity.getEmployee().getEmployeeId(),
                entity.getFirstName(), entity.getMiddleName(), entity.getLastName(),
                entity.getMaritalStatus() == null ? null : entity.getMaritalStatus().name(), entity.getNationality(),
                entity.getPlaceOfBirth(), entity.getEthnicGroup(), entity.getReligion(), entity.getEducationLevel(),
                entity.getMajor(), entity.getEmergencyNote(),
                entity.getProfileStatus() == null ? null : entity.getProfileStatus().name(), entity.getLockedReason(),
                entity.getLockedAt(), entity.getLockedBy(), entity.getRestoredAt(), entity.getRestoredBy());
    }

    private EmployeeAddressResponse toAddressResponse(HrEmployeeAddress entity) {
        return new EmployeeAddressResponse(entity.getEmployeeAddressId(), entity.getEmployee().getEmployeeId(),
                entity.getAddressType().name(), entity.getCountryName(), entity.getProvinceName(),
                entity.getDistrictName(), entity.getWardName(), entity.getAddressLine(), entity.getPostalCode(),
                entity.isPrimary());
    }

    private EmergencyContactResponse toEmergencyContactResponse(HrEmployeeEmergencyContact entity) {
        return new EmergencyContactResponse(entity.getEmergencyContactId(), entity.getEmployee().getEmployeeId(),
                entity.getContactName(), entity.getRelationshipCode().name(), entity.getPhoneNumber(),
                entity.getEmail(), entity.getAddressLine(), entity.isPrimary(), entity.getNote());
    }

    private EmployeeIdentificationResponse toIdentificationResponse(HrEmployeeIdentification entity) {
        return new EmployeeIdentificationResponse(entity.getEmployeeIdentificationId(),
                entity.getEmployee().getEmployeeId(), entity.getDocumentType().name(), entity.getDocumentNumber(),
                entity.getIssueDate(), entity.getIssuePlace(), entity.getExpiryDate(), entity.getCountryOfIssue(),
                entity.isPrimary(), entity.getStatus().name());
    }

    private EmployeeBankAccountResponse toBankAccountResponse(HrEmployeeBankAccount entity) {
        return new EmployeeBankAccountResponse(entity.getEmployeeBankAccountId(), entity.getEmployee().getEmployeeId(),
                entity.getBankName(), entity.getBankCode(), entity.getAccountNumber(), entity.getAccountHolderName(),
                entity.getBranchName(), entity.isPrimary(), entity.getStatus().name());
    }

    private EmployeeDocumentResponse toDocumentResponse(HrEmployeeDocument entity) {
        return new EmployeeDocumentResponse(entity.getEmployeeDocumentId(), entity.getEmployee().getEmployeeId(),
                entity.getDocumentCategory().name(), entity.getDocumentName(), entity.getStoragePath(),
                entity.getMimeType(), entity.getFileSizeBytes(), entity.getUploadedAt(), entity.getUploadedBy(),
                entity.getStatus().name(), entity.getNote());
    }
}
