package com.company.hrm.module.onboarding.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.hrm.common.constant.EmploymentStatus;
import com.company.hrm.common.constant.NotificationChannel;
import com.company.hrm.common.constant.OnboardingNotificationDeliveryStatus;
import com.company.hrm.common.constant.OnboardingStatus;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.ForbiddenException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.exception.UnauthorizedException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.auth.service.MailService;
import com.company.hrm.module.contract.dto.LaborContractDetailResponse;
import com.company.hrm.module.contract.dto.LaborContractUpsertRequest;
import com.company.hrm.module.contract.entity.CtLaborContract;
import com.company.hrm.module.contract.repository.CtLaborContractRepository;
import com.company.hrm.module.contract.service.LaborContractService;
import com.company.hrm.module.employee.dto.CreateEmployeeRequest;
import com.company.hrm.module.employee.dto.EmployeeDetailResponse;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.employee.service.EmployeeService;
import com.company.hrm.module.jobtitle.entity.HrJobTitle;
import com.company.hrm.module.jobtitle.repository.HrJobTitleRepository;
import com.company.hrm.module.onboarding.dto.CompleteOnboardingRequest;
import com.company.hrm.module.onboarding.dto.ConfirmOrientationRequest;
import com.company.hrm.module.onboarding.dto.CreateInitialContractFromOnboardingRequest;
import com.company.hrm.module.onboarding.dto.CreateOnboardingUserRequest;
import com.company.hrm.module.onboarding.dto.OnboardingAssetRequest;
import com.company.hrm.module.onboarding.dto.OnboardingAssetResponse;
import com.company.hrm.module.onboarding.dto.OnboardingChecklistRequest;
import com.company.hrm.module.onboarding.dto.OnboardingChecklistResponse;
import com.company.hrm.module.onboarding.dto.OnboardingDetailResponse;
import com.company.hrm.module.onboarding.dto.OnboardingDocumentRequest;
import com.company.hrm.module.onboarding.dto.OnboardingDocumentResponse;
import com.company.hrm.module.onboarding.dto.OnboardingListItemResponse;
import com.company.hrm.module.onboarding.dto.OnboardingNotificationResponse;
import com.company.hrm.module.onboarding.dto.OnboardingUpsertRequest;
import com.company.hrm.module.onboarding.dto.SendOnboardingNotificationRequest;
import com.company.hrm.module.onboarding.entity.OnbOnboarding;
import com.company.hrm.module.onboarding.entity.OnbOnboardingAsset;
import com.company.hrm.module.onboarding.entity.OnbOnboardingChecklist;
import com.company.hrm.module.onboarding.entity.OnbOnboardingDocument;
import com.company.hrm.module.onboarding.entity.OnbOnboardingNotificationLog;
import com.company.hrm.module.onboarding.repository.OnbOnboardingAssetRepository;
import com.company.hrm.module.onboarding.repository.OnbOnboardingChecklistRepository;
import com.company.hrm.module.onboarding.repository.OnbOnboardingDocumentRepository;
import com.company.hrm.module.onboarding.repository.OnbOnboardingNotificationLogRepository;
import com.company.hrm.module.onboarding.repository.OnbOnboardingRepository;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.orgunit.repository.HrOrgUnitRepository;
import com.company.hrm.module.storage.dto.StoredFileReference;
import com.company.hrm.module.storage.service.StorageFileService;
import com.company.hrm.module.systemconfig.entity.SysNotificationTemplate;
import com.company.hrm.module.systemconfig.repository.SysNotificationTemplateRepository;
import com.company.hrm.module.user.dto.CreateUserRequest;
import com.company.hrm.module.user.dto.UserDetailResponse;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.module.user.service.UserAdminService;
import com.company.hrm.security.SecurityUserContext;
import com.company.hrm.security.SecurityUserPrincipal;

@Service
public class OnboardingService {

    private final OnbOnboardingRepository onboardingRepository;
    private final OnbOnboardingChecklistRepository checklistRepository;
    private final OnbOnboardingDocumentRepository documentRepository;
    private final OnbOnboardingAssetRepository assetRepository;
    private final OnbOnboardingNotificationLogRepository notificationLogRepository;
    private final HrOrgUnitRepository orgUnitRepository;
    private final HrJobTitleRepository jobTitleRepository;
    private final HrEmployeeRepository employeeRepository;
    private final SecUserAccountRepository userAccountRepository;
    private final CtLaborContractRepository laborContractRepository;
    private final SysNotificationTemplateRepository notificationTemplateRepository;
    private final EmployeeService employeeService;
    private final UserAdminService userAdminService;
    private final LaborContractService laborContractService;
    private final MailService mailService;
    private final AuditLogService auditLogService;
    private final StorageFileService storageFileService;

    public OnboardingService(
            OnbOnboardingRepository onboardingRepository,
            OnbOnboardingChecklistRepository checklistRepository,
            OnbOnboardingDocumentRepository documentRepository,
            OnbOnboardingAssetRepository assetRepository,
            OnbOnboardingNotificationLogRepository notificationLogRepository,
            HrOrgUnitRepository orgUnitRepository,
            HrJobTitleRepository jobTitleRepository,
            HrEmployeeRepository employeeRepository,
            SecUserAccountRepository userAccountRepository,
            CtLaborContractRepository laborContractRepository,
            SysNotificationTemplateRepository notificationTemplateRepository,
            EmployeeService employeeService,
            UserAdminService userAdminService,
            LaborContractService laborContractService,
            MailService mailService,
            AuditLogService auditLogService,
            StorageFileService storageFileService) {
        this.onboardingRepository = onboardingRepository;
        this.checklistRepository = checklistRepository;
        this.documentRepository = documentRepository;
        this.assetRepository = assetRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.jobTitleRepository = jobTitleRepository;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
        this.laborContractRepository = laborContractRepository;
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.employeeService = employeeService;
        this.userAdminService = userAdminService;
        this.laborContractService = laborContractService;
        this.mailService = mailService;
        this.auditLogService = auditLogService;
        this.storageFileService = storageFileService;
    }

    @Transactional(readOnly = true)
    public PageResponse<OnboardingListItemResponse> list(String keyword, OnboardingStatus status, int page, int size) {
        Specification<OnbOnboarding> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("onboardingCode")), likeValue),
                    builder.like(builder.lower(root.get("fullName")), likeValue),
                    builder.like(builder.lower(root.get("personalEmail")), likeValue)));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        Page<OnbOnboarding> result = onboardingRepository.findAll(specification,
                PageRequest.of(page, size, Sort.by("plannedStartDate", "onboardingCode")));
        return new PageResponse<>(result.getContent().stream().map(this::toListItem).toList(), page, size,
                result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Transactional(readOnly = true)
    public OnboardingDetailResponse getDetail(Long onboardingId) {
        return toDetail(getOnboarding(onboardingId));
    }

    @Transactional
    public OnboardingDetailResponse create(OnboardingUpsertRequest request) {
        if (onboardingRepository.existsByOnboardingCodeIgnoreCaseAndDeletedFalse(request.onboardingCode())) {
            throw new BusinessException("ONBOARDING_CODE_DUPLICATE", "onboardingCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        OnbOnboarding entity = new OnbOnboarding();
        apply(entity, request);
        entity.setStatus(OnboardingStatus.DRAFT);
        entity = onboardingRepository.save(entity);
        OnboardingDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CREATE", "ONBOARDING", "onb_onboarding", entity.getOnboardingId().toString(), null,
                response, "Tạo hồ sơ onboarding.");
        return response;
    }

    @Transactional
    public OnboardingDetailResponse update(Long onboardingId, OnboardingUpsertRequest request) {
        OnbOnboarding entity = getOnboarding(onboardingId);
        OnboardingDetailResponse oldSnapshot = toDetail(entity);
        if (onboardingRepository.existsByOnboardingCodeIgnoreCaseAndDeletedFalseAndOnboardingIdNot(
                request.onboardingCode(), onboardingId)) {
            throw new BusinessException("ONBOARDING_CODE_DUPLICATE", "onboardingCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        apply(entity, request);
        entity = onboardingRepository.save(entity);
        OnboardingDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("UPDATE", "ONBOARDING", "onb_onboarding", onboardingId.toString(), oldSnapshot,
                response, "Cập nhật hồ sơ onboarding.");
        return response;
    }

    @Transactional
    public OnboardingChecklistResponse upsertChecklist(Long onboardingId, Long checklistId,
            OnboardingChecklistRequest request) {
        OnbOnboarding onboarding = getOnboarding(onboardingId);
        OnbOnboardingChecklist entity = checklistId == null ? new OnbOnboardingChecklist()
                : getChecklist(onboardingId, checklistId);
        entity.setOnboarding(onboarding);
        entity.setItemCode(request.itemCode().trim().toUpperCase());
        entity.setItemName(request.itemName().trim());
        entity.setRequired(request.required());
        entity.setCompleted(request.completed());
        entity.setDueDate(request.dueDate());
        entity.setNote(blankToNull(request.note()));
        if (request.completed()) {
            entity.setCompletedAt(LocalDateTime.now());
            entity.setCompletedBy(getCurrentUserOrNull());
        } else {
            entity.setCompletedAt(null);
            entity.setCompletedBy(null);
        }
        entity = checklistRepository.save(entity);
        onboarding.setStatus(OnboardingStatus.IN_PROGRESS);
        onboardingRepository.save(onboarding);
        OnboardingChecklistResponse response = toChecklistResponse(entity);
        auditLogService.logSuccess("UPSERT_CHECKLIST", "ONBOARDING", "onb_onboarding_checklist",
                entity.getOnboardingChecklistId().toString(), null, response, "Cập nhật checklist onboarding.");
        return response;
    }

    @Transactional
    public OnboardingDocumentResponse upsertDocument(Long onboardingId, Long documentId,
            OnboardingDocumentRequest request) {
        OnbOnboarding onboarding = getOnboarding(onboardingId);
        OnbOnboardingDocument entity = documentId == null ? new OnbOnboardingDocument()
                : getDocument(onboardingId, documentId);
        StoredFileReference storedFile = storageFileService.requireActiveReference(
                request.storagePath(),
                "ONBOARDING_DOCUMENT_STORAGE_NOT_FOUND",
                "storagePath không tồn tại hoặc không còn hiệu lực.");
        entity.setOnboarding(onboarding);
        entity.setDocumentName(request.documentName().trim());
        entity.setDocumentCategory(request.documentCategory());
        entity.setStoragePath(storedFile.fileKey());
        entity.setMimeType(storedFile.mimeType());
        entity.setFileSizeBytes(storedFile.fileSizeBytes());
        entity.setStatus(request.status());
        entity.setNote(blankToNull(request.note()));
        entity = documentRepository.save(entity);
        onboarding.setStatus(OnboardingStatus.IN_PROGRESS);
        onboardingRepository.save(onboarding);
        OnboardingDocumentResponse response = toDocumentResponse(entity);
        auditLogService.logSuccess("UPSERT_DOCUMENT", "ONBOARDING", "onb_onboarding_document",
                entity.getOnboardingDocumentId().toString(), null, response, "Cập nhật hồ sơ đầu vào onboarding.");
        return response;
    }

    @Transactional
    public OnboardingAssetResponse upsertAsset(Long onboardingId, Long assetId, OnboardingAssetRequest request) {
        OnbOnboarding onboarding = getOnboarding(onboardingId);
        OnbOnboardingAsset entity = assetId == null ? new OnbOnboardingAsset() : getAsset(onboardingId, assetId);
        entity.setOnboarding(onboarding);
        entity.setAssetCode(request.assetCode().trim().toUpperCase());
        entity.setAssetName(request.assetName().trim());
        entity.setAssetType(request.assetType().trim());
        entity.setAssignedDate(request.assignedDate());
        entity.setReturnedDate(request.returnedDate());
        entity.setStatus(request.status());
        entity.setNote(blankToNull(request.note()));
        entity = assetRepository.save(entity);
        onboarding.setStatus(OnboardingStatus.IN_PROGRESS);
        onboardingRepository.save(onboarding);
        OnboardingAssetResponse response = toAssetResponse(entity);
        auditLogService.logSuccess("UPSERT_ASSET", "ONBOARDING", "onb_onboarding_asset",
                entity.getOnboardingAssetId().toString(), null, response,
                "Cập nhật phân bổ trang thiết bị onboarding.");
        return response;
    }

    @Transactional
    public UserDetailResponse createUserFromOnboarding(Long onboardingId, CreateOnboardingUserRequest request) {
        OnbOnboarding onboarding = getOnboarding(onboardingId);
        if (onboarding.getLinkedUser() != null) {
            throw new BusinessException("ONBOARDING_USER_ALREADY_CREATED", "Onboarding này đã có user liên kết.",
                    HttpStatus.CONFLICT);
        }
        EmployeeDetailResponse employee = ensureEmployee(onboarding);
        UserDetailResponse user = userAdminService.createUser(new CreateUserRequest(
                employee.employeeId(),
                request.username(),
                onboarding.getPersonalEmail(),
                onboarding.getPersonalPhone(),
                com.company.hrm.common.constant.UserStatus.ACTIVE,
                RoleCode.EMPLOYEE,
                request.initialPassword(),
                request.sendSetupEmail()));
        onboarding.setLinkedEmployee(getEmployeeEntity(employee.employeeId()));
        onboarding.setLinkedUser(getUserAccount(user.userId()));
        onboarding.setStatus(OnboardingStatus.READY_FOR_JOIN);
        onboardingRepository.save(onboarding);
        auditLogService.logSuccess("CREATE_USER", "ONBOARDING", "onb_onboarding", onboardingId.toString(), null, user,
                "Tạo user từ onboarding.");
        return user;
    }

    @Transactional
    public LaborContractDetailResponse createInitialContract(Long onboardingId,
            CreateInitialContractFromOnboardingRequest request) {
        OnbOnboarding onboarding = getOnboarding(onboardingId);
        if (onboarding.getFirstContract() != null) {
            throw new BusinessException("ONBOARDING_INITIAL_CONTRACT_ALREADY_CREATED",
                    "Onboarding này đã được liên kết hợp đồng đầu tiên.", HttpStatus.CONFLICT);
        }
        EmployeeDetailResponse employee = ensureEmployee(onboarding);
        LaborContractDetailResponse contract = laborContractService.createDraft(new LaborContractUpsertRequest(
                employee.employeeId(),
                request.contractTypeId(),
                request.contractNumber(),
                request.signDate(),
                request.effectiveDate(),
                request.endDate(),
                onboarding.getJobTitle().getJobTitleId(),
                onboarding.getOrgUnit().getOrgUnitId(),
                blankToNull(request.workLocation()) == null ? onboarding.getWorkLocation() : request.workLocation(),
                request.baseSalary(),
                "VND",
                request.workingType(),
                request.note()));
        onboarding.setFirstContract(getLaborContractEntity(contract.laborContractId()));
        onboarding.setStatus(OnboardingStatus.READY_FOR_JOIN);
        onboardingRepository.save(onboarding);
        auditLogService.logSuccess("LINK_CONTRACT", "ONBOARDING", "onb_onboarding", onboardingId.toString(), null,
                contract, "Tạo hợp đồng đầu tiên từ onboarding.");
        return contract;
    }

    @Transactional
    public OnboardingDetailResponse confirmOrientation(Long onboardingId, ConfirmOrientationRequest request) {
        OnbOnboarding onboarding = getOnboarding(onboardingId);
        assertManagerCanConfirm(onboarding);
        if (onboarding.getStatus() == OnboardingStatus.COMPLETED
                || onboarding.getStatus() == OnboardingStatus.CANCELLED) {
            throw new BusinessException("ONBOARDING_ORIENTATION_STATUS_INVALID",
                    "Không thể xác nhận orientation cho onboarding đã hoàn tất hoặc đã hủy.", HttpStatus.CONFLICT);
        }
        OnboardingDetailResponse oldSnapshot = toDetail(onboarding);
        onboarding.setOrientationConfirmedAt(LocalDateTime.now());
        onboarding.setOrientationConfirmedBy(getCurrentUserRequired());
        onboarding.setOrientationNote(request.note().trim());
        if (onboarding.getStatus() == OnboardingStatus.DRAFT) {
            onboarding.setStatus(OnboardingStatus.IN_PROGRESS);
        }
        onboarding = onboardingRepository.save(onboarding);
        OnboardingDetailResponse response = toDetail(onboarding);
        auditLogService.logSuccess("ORIENTATION_CONFIRM", "ONBOARDING", "onb_onboarding", onboardingId.toString(),
                oldSnapshot, response, request.note());
        return response;
    }

    @Transactional
    public OnboardingDetailResponse completeOnboarding(Long onboardingId, CompleteOnboardingRequest request) {
        OnbOnboarding onboarding = getOnboarding(onboardingId);
        if (onboarding.getStatus() == OnboardingStatus.COMPLETED) {
            throw new BusinessException("ONBOARDING_ALREADY_COMPLETED", "Onboarding này đã được chốt hoàn tất.",
                    HttpStatus.CONFLICT);
        }
        validateCompletionReadiness(onboarding);
        OnboardingDetailResponse oldSnapshot = toDetail(onboarding);
        onboarding.setStatus(OnboardingStatus.COMPLETED);
        onboarding.setCompletedAt(LocalDateTime.now());
        onboarding.setCompletedBy(getCurrentUserRequired());
        onboarding.setCompletedNote(request.note().trim());
        onboarding = onboardingRepository.save(onboarding);
        OnboardingDetailResponse response = toDetail(onboarding);
        auditLogService.logSuccess("COMPLETE", "ONBOARDING", "onb_onboarding", onboardingId.toString(), oldSnapshot,
                response, request.note());
        return response;
    }

    @Transactional
    public List<OnboardingNotificationResponse> sendOnboardingNotifications(Long onboardingId,
            SendOnboardingNotificationRequest request) {
        OnbOnboarding onboarding = getOnboarding(onboardingId);
        if (onboarding.getStatus() == OnboardingStatus.CANCELLED) {
            throw new BusinessException("ONBOARDING_NOTIFY_STATUS_INVALID",
                    "Không thể gửi thông báo cho onboarding đã hủy.", HttpStatus.CONFLICT);
        }
        SysNotificationTemplate template = notificationTemplateRepository.findByTemplateCodeIgnoreCaseAndDeletedFalse(
                blankToNull(request.templateCode()) == null ? "ONBOARDING_PLAN_NOTIFY" : request.templateCode().trim())
                .orElseThrow(() -> new NotFoundException("NOTIFICATION_TEMPLATE_NOT_FOUND",
                        "Không tìm thấy template thông báo onboarding."));
        if (template.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("NOTIFICATION_TEMPLATE_INACTIVE",
                    "Template thông báo đang ở trạng thái không hoạt động.", HttpStatus.CONFLICT);
        }

        LinkedHashMap<String, String> recipients = new LinkedHashMap<>();
        if (request.notifyNewHire() && onboarding.getPersonalEmail() != null) {
            recipients.put(onboarding.getPersonalEmail().trim().toLowerCase(), onboarding.getFullName());
        }
        if (request.notifyManager() && onboarding.getManagerEmployee() != null
                && onboarding.getManagerEmployee().getWorkEmail() != null) {
            recipients.put(onboarding.getManagerEmployee().getWorkEmail().trim().toLowerCase(),
                    onboarding.getManagerEmployee().getFullName());
        }
        if (request.notifyLinkedUser() && onboarding.getLinkedUser() != null
                && onboarding.getLinkedUser().getEmail() != null) {
            recipients.put(onboarding.getLinkedUser().getEmail().trim().toLowerCase(),
                    onboarding.getLinkedUser().getUsername());
        }
        if (request.customRecipientEmails() != null) {
            for (String email : request.customRecipientEmails()) {
                if (email != null && !email.isBlank()) {
                    recipients.put(email.trim().toLowerCase(), "Custom recipient");
                }
            }
        }
        if (recipients.isEmpty()) {
            throw new BusinessException("ONBOARDING_NOTIFY_RECIPIENT_EMPTY",
                    "Không có người nhận hợp lệ để gửi thông báo.", HttpStatus.BAD_REQUEST);
        }

        List<OnboardingNotificationResponse> responses = new ArrayList<>();
        for (Map.Entry<String, String> recipient : recipients.entrySet()) {
            String subject = render(
                    template.getSubjectTemplate() == null ? "Digital HRM - Onboarding plan ${onboardingCode}"
                            : template.getSubjectTemplate(),
                    onboarding);
            String body = render(template.getBodyTemplate(), onboarding);
            boolean mocked = mailService.sendSimpleMail(recipient.getKey(), subject, body);

            OnbOnboardingNotificationLog log = new OnbOnboardingNotificationLog();
            log.setOnboarding(onboarding);
            log.setNotificationTemplate(template);
            log.setChannelCode(NotificationChannel.EMAIL);
            log.setRecipientName(recipient.getValue());
            log.setRecipientEmail(recipient.getKey());
            log.setSubjectSnapshot(subject);
            log.setBodySnapshot(body);
            log.setDeliveryStatus(
                    mocked ? OnboardingNotificationDeliveryStatus.MOCKED : OnboardingNotificationDeliveryStatus.SENT);
            log.setSentAt(LocalDateTime.now());
            log.setSentBy(getCurrentUserOrNull());
            log.setNote(blankToNull(request.note()));
            log = notificationLogRepository.save(log);
            responses.add(toNotificationResponse(log));
        }
        auditLogService.logSuccess("NOTIFY", "ONBOARDING", "onb_onboarding_notification_log", onboardingId.toString(),
                null, responses, request.note());
        return responses;
    }

    private void validateCompletionReadiness(OnbOnboarding onboarding) {
        if (onboarding.getOrientationConfirmedAt() == null) {
            throw new BusinessException("ONBOARDING_ORIENTATION_REQUIRED",
                    "Phải xác nhận hoàn tất hướng dẫn nhập môn trước khi chốt onboarding.", HttpStatus.CONFLICT);
        }
        long incompleteRequired = checklistRepository
                .findAllByOnboardingOnboardingIdAndDeletedFalseOrderByDueDateAscOnboardingChecklistIdAsc(
                        onboarding.getOnboardingId())
                .stream()
                .filter(OnbOnboardingChecklist::isRequired)
                .filter(item -> !item.isCompleted())
                .count();
        if (incompleteRequired > 0) {
            throw new BusinessException("ONBOARDING_CHECKLIST_INCOMPLETE", "Checklist bắt buộc chưa hoàn tất.",
                    HttpStatus.CONFLICT, Map.of("incompleteRequiredCount", incompleteRequired));
        }
        if (onboarding.getFirstContract() == null) {
            throw new BusinessException("ONBOARDING_FIRST_CONTRACT_REQUIRED",
                    "Phải có hợp đồng đầu tiên trước khi chốt onboarding.", HttpStatus.CONFLICT);
        }
        CtLaborContract contract = getLaborContractEntity(onboarding.getFirstContract().getLaborContractId());
        if (contract.getContractStatus() != com.company.hrm.common.constant.ContractStatus.ACTIVE) {
            throw new BusinessException("ONBOARDING_CONTRACT_NOT_ACTIVE",
                    "Hợp đồng đầu tiên phải ở trạng thái ACTIVE trước khi chốt onboarding.", HttpStatus.CONFLICT);
        }
    }

    private void assertManagerCanConfirm(OnbOnboarding onboarding) {
        SecurityUserPrincipal principal = SecurityUserContext.currentPrincipal()
                .orElseThrow(() -> new UnauthorizedException("AUTH_UNAUTHORIZED", "Chưa xác thực người dùng."));
        if (principal.getRoleCode() != RoleCode.MANAGER) {
            throw new ForbiddenException("ONBOARDING_MANAGER_SCOPE_REQUIRED",
                    "Chỉ quản lý mới được xác nhận orientation.");
        }
        HrEmployee currentEmployee = getCurrentEmployeeRequired();
        if (onboarding.getManagerEmployee() != null
                && onboarding.getManagerEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId())) {
            return;
        }
        String currentPath = currentEmployee.getOrgUnit() == null ? null : currentEmployee.getOrgUnit().getPathCode();
        String targetPath = onboarding.getOrgUnit() == null ? null : onboarding.getOrgUnit().getPathCode();
        if (currentPath != null && targetPath != null && targetPath.startsWith(currentPath)) {
            return;
        }
        throw new ForbiddenException("ONBOARDING_ORIENTATION_SCOPE_DENIED",
                "Bạn không có quyền xác nhận onboarding này.");
    }

    private HrEmployee getCurrentEmployeeRequired() {
        SecUserAccount user = getCurrentUserRequired();
        if (user.getEmployeeId() == null) {
            throw new ForbiddenException("EMPLOYEE_LINK_REQUIRED",
                    "Tài khoản hiện tại chưa được liên kết hồ sơ nhân sự.");
        }
        return getEmployeeEntity(user.getEmployeeId());
    }

    private EmployeeDetailResponse ensureEmployee(OnbOnboarding onboarding) {
        if (onboarding.getLinkedEmployee() != null) {
            return employeeService.getDetail(onboarding.getLinkedEmployee().getEmployeeId());
        }
        EmployeeDetailResponse employee = employeeService.create(new CreateEmployeeRequest(
                blankToNull(onboarding.getEmployeeCode()) == null ? onboarding.getOnboardingCode().trim().toUpperCase()
                        : onboarding.getEmployeeCode(),
                onboarding.getOrgUnit().getOrgUnitId(),
                onboarding.getJobTitle().getJobTitleId(),
                onboarding.getManagerEmployee() == null ? null : onboarding.getManagerEmployee().getEmployeeId(),
                onboarding.getFullName(),
                null,
                null,
                onboarding.getGenderCode(),
                onboarding.getDateOfBirth(),
                onboarding.getPlannedStartDate(),
                EmploymentStatus.ACTIVE,
                onboarding.getWorkLocation(),
                null,
                onboarding.getPersonalEmail(),
                onboarding.getPersonalPhone(),
                null,
                onboarding.getNote()));
        onboarding.setLinkedEmployee(getEmployeeEntity(employee.employeeId()));
        onboardingRepository.save(onboarding);
        return employee;
    }

    private void apply(OnbOnboarding entity, OnboardingUpsertRequest request) {
        entity.setOnboardingCode(request.onboardingCode().trim().toUpperCase());
        entity.setFullName(request.fullName().trim());
        entity.setPersonalEmail(blankToNull(request.personalEmail()));
        entity.setPersonalPhone(blankToNull(request.personalPhone()));
        entity.setGenderCode(request.genderCode());
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setPlannedStartDate(request.plannedStartDate());
        entity.setEmployeeCode(blankToNull(request.employeeCode()));
        entity.setOrgUnit(getOrgUnit(request.orgUnitId()));
        entity.setJobTitle(getJobTitle(request.jobTitleId()));
        entity.setManagerEmployee(
                request.managerEmployeeId() == null ? null : getEmployeeEntity(request.managerEmployeeId()));
        entity.setWorkLocation(blankToNull(request.workLocation()));
        entity.setNote(blankToNull(request.note()));
    }

    private OnbOnboarding getOnboarding(Long onboardingId) {
        return onboardingRepository.findByOnboardingIdAndDeletedFalse(onboardingId)
                .orElseThrow(() -> new NotFoundException("ONBOARDING_NOT_FOUND", "Không tìm thấy hồ sơ onboarding."));
    }

    private OnbOnboardingChecklist getChecklist(Long onboardingId, Long checklistId) {
        OnbOnboardingChecklist entity = checklistRepository.findByOnboardingChecklistIdAndDeletedFalse(checklistId)
                .orElseThrow(() -> new NotFoundException("ONBOARDING_CHECKLIST_NOT_FOUND",
                        "Không tìm thấy checklist onboarding."));
        if (!entity.getOnboarding().getOnboardingId().equals(onboardingId)) {
            throw new BusinessException("ONBOARDING_CHECKLIST_SCOPE_INVALID",
                    "Checklist không thuộc hồ sơ onboarding này.", HttpStatus.BAD_REQUEST);
        }
        return entity;
    }

    private OnbOnboardingDocument getDocument(Long onboardingId, Long documentId) {
        OnbOnboardingDocument entity = documentRepository.findByOnboardingDocumentIdAndDeletedFalse(documentId)
                .orElseThrow(
                        () -> new NotFoundException("ONBOARDING_DOCUMENT_NOT_FOUND", "Không tìm thấy hồ sơ đầu vào."));
        if (!entity.getOnboarding().getOnboardingId().equals(onboardingId)) {
            throw new BusinessException("ONBOARDING_DOCUMENT_SCOPE_INVALID",
                    "Tài liệu không thuộc hồ sơ onboarding này.", HttpStatus.BAD_REQUEST);
        }
        return entity;
    }

    private OnbOnboardingAsset getAsset(Long onboardingId, Long assetId) {
        OnbOnboardingAsset entity = assetRepository.findByOnboardingAssetIdAndDeletedFalse(assetId)
                .orElseThrow(() -> new NotFoundException("ONBOARDING_ASSET_NOT_FOUND",
                        "Không tìm thấy tài sản onboarding."));
        if (!entity.getOnboarding().getOnboardingId().equals(onboardingId)) {
            throw new BusinessException("ONBOARDING_ASSET_SCOPE_INVALID", "Tài sản không thuộc hồ sơ onboarding này.",
                    HttpStatus.BAD_REQUEST);
        }
        return entity;
    }

    private HrOrgUnit getOrgUnit(Long orgUnitId) {
        return orgUnitRepository.findByOrgUnitIdAndDeletedFalse(orgUnitId)
                .orElseThrow(() -> new NotFoundException("ORG_UNIT_NOT_FOUND", "Không tìm thấy đơn vị tổ chức."));
    }

    private HrJobTitle getJobTitle(Long jobTitleId) {
        return jobTitleRepository.findByJobTitleIdAndDeletedFalse(jobTitleId)
                .orElseThrow(() -> new NotFoundException("JOB_TITLE_NOT_FOUND", "Không tìm thấy chức danh."));
    }

    private HrEmployee getEmployeeEntity(Long employeeId) {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân sự."));
    }

    private SecUserAccount getUserAccount(UUID userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Không tìm thấy tài khoản người dùng."));
    }

    private CtLaborContract getLaborContractEntity(Long laborContractId) {
        return laborContractRepository.findByLaborContractIdAndDeletedFalse(laborContractId)
                .orElseThrow(
                        () -> new NotFoundException("LABOR_CONTRACT_NOT_FOUND", "Không tìm thấy hợp đồng lao động."));
    }

    private SecUserAccount getCurrentUserRequired() {
        return SecurityUserContext.getCurrentUserId()
                .flatMap(userAccountRepository::findById)
                .orElseThrow(() -> new UnauthorizedException("USER_NOT_FOUND", "Không tìm thấy tài khoản hiện tại."));
    }

    private SecUserAccount getCurrentUserOrNull() {
        return SecurityUserContext.getCurrentUserId()
                .flatMap(userAccountRepository::findById)
                .orElse(null);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String render(String template, OnbOnboarding onboarding) {
        Map<String, String> data = new HashMap<>();
        data.put("onboardingCode", onboarding.getOnboardingCode());
        data.put("fullName", onboarding.getFullName());
        data.put("plannedStartDate",
                onboarding.getPlannedStartDate() == null ? "" : onboarding.getPlannedStartDate().toString());
        data.put("orgUnitName", onboarding.getOrgUnit() == null ? "" : onboarding.getOrgUnit().getOrgUnitName());
        data.put("jobTitleName", onboarding.getJobTitle() == null ? "" : onboarding.getJobTitle().getJobTitleName());
        data.put("managerName",
                onboarding.getManagerEmployee() == null ? "" : onboarding.getManagerEmployee().getFullName());
        data.put("workLocation", onboarding.getWorkLocation() == null ? "" : onboarding.getWorkLocation());

        String rendered = template;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            rendered = rendered.replace("${" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }
        return rendered;
    }

    private OnboardingListItemResponse toListItem(OnbOnboarding entity) {
        return new OnboardingListItemResponse(
                entity.getOnboardingId(),
                entity.getOnboardingCode(),
                entity.getFullName(),
                entity.getPersonalEmail(),
                entity.getPersonalPhone(),
                entity.getOrgUnit().getOrgUnitCode(),
                entity.getOrgUnit().getOrgUnitName(),
                entity.getJobTitle().getJobTitleCode(),
                entity.getJobTitle().getJobTitleName(),
                entity.getPlannedStartDate(),
                entity.getStatus().name(),
                entity.getLinkedEmployee() == null ? null : entity.getLinkedEmployee().getEmployeeId(),
                entity.getLinkedUser() == null ? null : entity.getLinkedUser().getUserId(),
                entity.getFirstContract() == null ? null : entity.getFirstContract().getLaborContractId(),
                entity.getOrientationConfirmedAt(),
                entity.getOrientationConfirmedBy() == null ? null : entity.getOrientationConfirmedBy().getUsername(),
                entity.getCompletedAt(),
                entity.getCompletedBy() == null ? null : entity.getCompletedBy().getUsername());
    }

    private OnboardingDetailResponse toDetail(OnbOnboarding entity) {
        List<OnboardingChecklistResponse> checklistItems = checklistRepository
                .findAllByOnboardingOnboardingIdAndDeletedFalseOrderByDueDateAscOnboardingChecklistIdAsc(
                        entity.getOnboardingId())
                .stream().map(this::toChecklistResponse).toList();
        List<OnboardingDocumentResponse> documents = documentRepository
                .findAllByOnboardingOnboardingIdAndDeletedFalseOrderByOnboardingDocumentIdAsc(entity.getOnboardingId())
                .stream().map(this::toDocumentResponse).toList();
        List<OnboardingAssetResponse> assets = assetRepository
                .findAllByOnboardingOnboardingIdAndDeletedFalseOrderByOnboardingAssetIdAsc(entity.getOnboardingId())
                .stream().map(this::toAssetResponse).toList();
        List<OnboardingNotificationResponse> notifications = notificationLogRepository
                .findAllByOnboardingOnboardingIdOrderBySentAtDescOnboardingNotificationLogIdDesc(
                        entity.getOnboardingId())
                .stream().map(this::toNotificationResponse).toList();

        return new OnboardingDetailResponse(
                entity.getOnboardingId(),
                entity.getOnboardingCode(),
                entity.getFullName(),
                entity.getPersonalEmail(),
                entity.getPersonalPhone(),
                entity.getGenderCode().name(),
                entity.getDateOfBirth(),
                entity.getPlannedStartDate(),
                entity.getEmployeeCode(),
                entity.getOrgUnit().getOrgUnitId(),
                entity.getOrgUnit().getOrgUnitCode(),
                entity.getOrgUnit().getOrgUnitName(),
                entity.getJobTitle().getJobTitleId(),
                entity.getJobTitle().getJobTitleCode(),
                entity.getJobTitle().getJobTitleName(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeId(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeCode(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getFullName(),
                entity.getWorkLocation(),
                entity.getLinkedEmployee() == null ? null : entity.getLinkedEmployee().getEmployeeId(),
                entity.getLinkedUser() == null ? null : entity.getLinkedUser().getUserId(),
                entity.getFirstContract() == null ? null : entity.getFirstContract().getLaborContractId(),
                entity.getStatus().name(),
                entity.getNote(),
                entity.getOrientationConfirmedAt(),
                entity.getOrientationConfirmedBy() == null ? null : entity.getOrientationConfirmedBy().getUsername(),
                entity.getOrientationNote(),
                entity.getCompletedAt(),
                entity.getCompletedBy() == null ? null : entity.getCompletedBy().getUsername(),
                entity.getCompletedNote(),
                checklistItems,
                documents,
                assets,
                notifications);
    }

    private OnboardingChecklistResponse toChecklistResponse(OnbOnboardingChecklist entity) {
        return new OnboardingChecklistResponse(entity.getOnboardingChecklistId(), entity.getItemCode(),
                entity.getItemName(), entity.isRequired(), entity.isCompleted(), entity.getDueDate(),
                entity.getCompletedAt(), entity.getCompletedBy() == null ? null : entity.getCompletedBy().getUsername(),
                entity.getNote());
    }

    private OnboardingDocumentResponse toDocumentResponse(OnbOnboardingDocument entity) {
        return new OnboardingDocumentResponse(entity.getOnboardingDocumentId(), entity.getDocumentName(),
                entity.getDocumentCategory().name(), entity.getStoragePath(), entity.getMimeType(),
                entity.getFileSizeBytes(), entity.getStatus().name(), entity.getNote());
    }

    private OnboardingAssetResponse toAssetResponse(OnbOnboardingAsset entity) {
        return new OnboardingAssetResponse(entity.getOnboardingAssetId(), entity.getAssetCode(), entity.getAssetName(),
                entity.getAssetType(), entity.getAssignedDate(), entity.getReturnedDate(), entity.getStatus().name(),
                entity.getNote());
    }

    private OnboardingNotificationResponse toNotificationResponse(OnbOnboardingNotificationLog entity) {
        return new OnboardingNotificationResponse(
                entity.getOnboardingNotificationLogId(),
                entity.getChannelCode().name(),
                entity.getRecipientName(),
                entity.getRecipientEmail(),
                entity.getDeliveryStatus().name(),
                entity.getSubjectSnapshot(),
                entity.getSentAt(),
                entity.getNote());
    }
}
