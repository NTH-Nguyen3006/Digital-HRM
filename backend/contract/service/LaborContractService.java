package com.company.hrm.module.contract.service;

import com.company.hrm.common.constant.ContractAppendixStatus;
import com.company.hrm.common.constant.ContractStatus;
import com.company.hrm.common.constant.DocumentStatus;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.contract.dto.*;
import com.company.hrm.module.contract.entity.*;
import com.company.hrm.module.contract.repository.*;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.jobtitle.entity.HrJobTitle;
import com.company.hrm.module.jobtitle.repository.HrJobTitleRepository;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.orgunit.repository.HrOrgUnitRepository;
import com.company.hrm.module.storage.dto.StoredFileReference;
import com.company.hrm.module.storage.service.StorageFileService;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.security.SecurityUserContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LaborContractService {

    private final CtLaborContractRepository laborContractRepository;
    private final CtContractTypeRepository contractTypeRepository;
    private final CtContractAppendixRepository appendixRepository;
    private final CtContractAttachmentRepository attachmentRepository;
    private final CtContractStatusHistoryRepository statusHistoryRepository;
    private final HrEmployeeRepository employeeRepository;
    private final HrJobTitleRepository jobTitleRepository;
    private final HrOrgUnitRepository orgUnitRepository;
    private final SecUserAccountRepository userAccountRepository;
    private final AuditLogService auditLogService;
    private final StorageFileService storageFileService;

    public LaborContractService(
            CtLaborContractRepository laborContractRepository,
            CtContractTypeRepository contractTypeRepository,
            CtContractAppendixRepository appendixRepository,
            CtContractAttachmentRepository attachmentRepository,
            CtContractStatusHistoryRepository statusHistoryRepository,
            HrEmployeeRepository employeeRepository,
            HrJobTitleRepository jobTitleRepository,
            HrOrgUnitRepository orgUnitRepository,
            SecUserAccountRepository userAccountRepository,
            AuditLogService auditLogService,
            StorageFileService storageFileService
    ) {
        this.laborContractRepository = laborContractRepository;
        this.contractTypeRepository = contractTypeRepository;
        this.appendixRepository = appendixRepository;
        this.attachmentRepository = attachmentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.employeeRepository = employeeRepository;
        this.jobTitleRepository = jobTitleRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditLogService = auditLogService;
        this.storageFileService = storageFileService;
    }

    @Transactional(readOnly = true)
    public PageResponse<LaborContractListItemResponse> list(
            String keyword,
            Long employeeId,
            Long contractTypeId,
            ContractStatus contractStatus,
            Integer expiringWithinDays,
            int page,
            int size
    ) {
        Specification<CtLaborContract> specification = baseContractSpecification();
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("contractNumber")), likeValue),
                    builder.like(builder.lower(root.join("employee").get("employeeCode")), likeValue),
                    builder.like(builder.lower(root.join("employee").get("fullName")), likeValue)
            ));
        }
        if (employeeId != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.join("employee").get("employeeId"), employeeId));
        }
        if (contractTypeId != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.join("contractType").get("contractTypeId"), contractTypeId));
        }
        if (contractStatus != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("contractStatus"), contractStatus));
        }
        if (expiringWithinDays != null && expiringWithinDays >= 0) {
            LocalDate today = LocalDate.now();
            LocalDate toDate = today.plusDays(expiringWithinDays);
            specification = specification.and((root, query, builder) -> builder.and(
                    builder.equal(root.get("contractStatus"), ContractStatus.ACTIVE),
                    builder.isNotNull(root.get("endDate")),
                    builder.greaterThanOrEqualTo(root.get("endDate"), today),
                    builder.lessThanOrEqualTo(root.get("endDate"), toDate)
            ));
        }

        Page<CtLaborContract> result = laborContractRepository.findAll(
                specification,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "effectiveDate").and(Sort.by(Sort.Direction.DESC, "laborContractId")))
        );
        return new PageResponse<>(
                result.getContent().stream().map(this::toListItem).toList(),
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    public List<LaborContractListItemResponse> listExpiringContracts(int days) {
        if (days < 0) {
            throw new BusinessException("CONTRACT_EXPIRY_RANGE_INVALID", "days phải >= 0.", HttpStatus.BAD_REQUEST);
        }
        LocalDate today = LocalDate.now();
        LocalDate toDate = today.plusDays(days);
        return laborContractRepository.findAllByDeletedFalseAndContractStatusAndEndDateBetweenOrderByEndDateAsc(
                        ContractStatus.ACTIVE,
                        today,
                        toDate
                )
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public LaborContractDetailResponse getDetail(Long laborContractId) {
        return toDetail(getContract(laborContractId));
    }

    @Transactional(readOnly = true)
    public List<ContractStatusHistoryResponse> getStatusHistory(Long laborContractId) {
        getContract(laborContractId);
        return statusHistoryRepository.findAllByLaborContract_LaborContractIdOrderByChangedAtDescContractStatusHistoryIdDesc(laborContractId)
                .stream()
                .map(this::toStatusHistoryResponse)
                .toList();
    }

    @Transactional
    public LaborContractDetailResponse createDraft(LaborContractUpsertRequest request) {
        if (laborContractRepository.existsByContractNumberIgnoreCaseAndDeletedFalse(request.contractNumber())) {
            throw new BusinessException("CONTRACT_NUMBER_DUPLICATE", "contractNumber đã tồn tại.", HttpStatus.CONFLICT);
        }

        CtLaborContract entity = new CtLaborContract();
        applyDraftData(entity, request, false);
        entity.setContractStatus(ContractStatus.DRAFT);
        entity.setSignedByCompanyUser(null);

        entity = laborContractRepository.save(entity);
        appendStatusHistory(entity, null, ContractStatus.DRAFT, "Khởi tạo bản nháp hợp đồng.");
        LaborContractDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CREATE_DRAFT", "LABOR_CONTRACT", "ct_labor_contract", entity.getLaborContractId().toString(), null, response, "Tạo bản nháp hợp đồng.");
        return response;
    }

    @Transactional
    public LaborContractDetailResponse updateDraft(Long laborContractId, LaborContractUpsertRequest request) {
        CtLaborContract entity = getContract(laborContractId);
        assertContractStatus(entity, ContractStatus.DRAFT);

        if (laborContractRepository.existsByContractNumberIgnoreCaseAndDeletedFalseAndLaborContractIdNot(request.contractNumber(), laborContractId)) {
            throw new BusinessException("CONTRACT_NUMBER_DUPLICATE", "contractNumber đã tồn tại.", HttpStatus.CONFLICT);
        }

        LaborContractDetailResponse oldSnapshot = toDetail(entity);
        applyDraftData(entity, request, true);
        entity = laborContractRepository.save(entity);

        LaborContractDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("UPDATE_DRAFT", "LABOR_CONTRACT", "ct_labor_contract", entity.getLaborContractId().toString(), oldSnapshot, response, "Cập nhật bản nháp hợp đồng.");
        return response;
    }

    @Transactional
    public void cancelDraft(Long laborContractId, ContractActionRequest request) {
        CtLaborContract entity = getContract(laborContractId);
        assertContractStatus(entity, ContractStatus.DRAFT);

        LaborContractDetailResponse oldSnapshot = toDetail(entity);
        entity.setDeleted(true);
        laborContractRepository.save(entity);

        auditLogService.logSuccess(
                "CANCEL_DRAFT",
                "LABOR_CONTRACT",
                "ct_labor_contract",
                entity.getLaborContractId().toString(),
                oldSnapshot,
                null,
                request == null ? "Hủy bản nháp hợp đồng." : request.note()
        );
    }

    @Transactional
    public LaborContractDetailResponse submitForSigning(Long laborContractId, ContractActionRequest request) {
        CtLaborContract entity = getContract(laborContractId);
        assertContractStatus(entity, ContractStatus.DRAFT);

        validateContractData(entity);
        ContractStatus oldStatus = entity.getContractStatus();
        LaborContractDetailResponse oldSnapshot = toDetail(entity);

        entity.setContractStatus(ContractStatus.PENDING_SIGN);
        entity = laborContractRepository.save(entity);
        appendStatusHistory(entity, oldStatus, ContractStatus.PENDING_SIGN, request == null ? null : request.note());

        LaborContractDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("SUBMIT", "LABOR_CONTRACT", "ct_labor_contract", entity.getLaborContractId().toString(), oldSnapshot, response, "Gửi hợp đồng sang bước chờ ký.");
        return response;
    }

    @Transactional
    public LaborContractDetailResponse review(Long laborContractId, ReviewContractRequest request) {
        CtLaborContract entity = getContract(laborContractId);
        assertContractStatus(entity, ContractStatus.PENDING_SIGN);
        LaborContractDetailResponse oldSnapshot = toDetail(entity);

        if (request.approved()) {
            SecUserAccount signer = resolveSigner(request.signedByCompanyUserId());
            entity.setSignedByCompanyUser(signer);
            entity = laborContractRepository.save(entity);
            appendStatusHistory(entity, ContractStatus.PENDING_SIGN, ContractStatus.PENDING_SIGN, buildReviewReason(true, request.reason()));
            LaborContractDetailResponse response = toDetail(entity);
            auditLogService.logSuccess("REVIEW_APPROVE", "LABOR_CONTRACT", "ct_labor_contract", entity.getLaborContractId().toString(), oldSnapshot, response, "Duyệt hợp đồng, chờ chốt hiệu lực.");
            return response;
        }

        entity.setSignedByCompanyUser(null);
        entity.setContractStatus(ContractStatus.DRAFT);
        entity = laborContractRepository.save(entity);
        appendStatusHistory(entity, ContractStatus.PENDING_SIGN, ContractStatus.DRAFT, buildReviewReason(false, request.reason()));

        LaborContractDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("REVIEW_REJECT", "LABOR_CONTRACT", "ct_labor_contract", entity.getLaborContractId().toString(), oldSnapshot, response, "Từ chối hợp đồng, trả về nháp.");
        return response;
    }

    @Transactional
    public LaborContractDetailResponse activate(Long laborContractId, ContractActionRequest request) {
        CtLaborContract entity = getContract(laborContractId);
        assertContractStatus(entity, ContractStatus.PENDING_SIGN);
        if (entity.getSignedByCompanyUser() == null) {
            throw new BusinessException("CONTRACT_SIGNER_REQUIRED", "Phải có người ký phía công ty trước khi chốt hiệu lực.", HttpStatus.CONFLICT);
        }
        validateContractData(entity);

        ContractStatus oldStatus = entity.getContractStatus();
        LaborContractDetailResponse oldSnapshot = toDetail(entity);

        entity.setContractStatus(ContractStatus.ACTIVE);
        entity = laborContractRepository.save(entity);
        appendStatusHistory(entity, oldStatus, ContractStatus.ACTIVE, request == null ? null : request.note());

        LaborContractDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("ACTIVATE", "LABOR_CONTRACT", "ct_labor_contract", entity.getLaborContractId().toString(), oldSnapshot, response, "Chốt hiệu lực hợp đồng.");
        return response;
    }

    @Transactional
    public LaborContractDetailResponse changeLifecycleStatus(Long laborContractId, ChangeContractLifecycleStatusRequest request) {
        CtLaborContract entity = getContract(laborContractId);
        ContractStatus currentStatus = entity.getContractStatus();
        ContractStatus targetStatus = request.targetStatus();

        if (!isValidLifecycleTransition(currentStatus, targetStatus)) {
            throw new BusinessException(
                    "CONTRACT_STATUS_TRANSITION_INVALID",
                    "Không cho phép chuyển từ trạng thái " + currentStatus + " sang " + targetStatus + ".",
                    HttpStatus.CONFLICT
            );
        }
        if (targetStatus == ContractStatus.EXPIRED) {
            if (entity.getEndDate() == null) {
                throw new BusinessException("CONTRACT_EXPIRED_DATE_REQUIRED", "Không thể đánh dấu EXPIRED cho hợp đồng không có endDate.", HttpStatus.CONFLICT);
            }
            if (entity.getEndDate().isAfter(LocalDate.now())) {
                throw new BusinessException("CONTRACT_EXPIRED_TOO_EARLY", "Chỉ được chuyển EXPIRED khi endDate đã đến hoặc đã qua.", HttpStatus.CONFLICT);
            }
        }

        LaborContractDetailResponse oldSnapshot = toDetail(entity);
        entity.setContractStatus(targetStatus);
        entity = laborContractRepository.save(entity);
        appendStatusHistory(entity, currentStatus, targetStatus, request.reason());

        LaborContractDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CHANGE_STATUS", "LABOR_CONTRACT", "ct_labor_contract", entity.getLaborContractId().toString(), oldSnapshot, response, request.reason());
        return response;
    }

    @Transactional
    public LaborContractDetailResponse createRenewalDraft(Long laborContractId, RenewLaborContractRequest request) {
        CtLaborContract source = getContract(laborContractId);
        if (laborContractRepository.existsByContractNumberIgnoreCaseAndDeletedFalse(request.contractNumber())) {
            throw new BusinessException("CONTRACT_NUMBER_DUPLICATE", "contractNumber đã tồn tại.", HttpStatus.CONFLICT);
        }

        CtLaborContract entity = new CtLaborContract();
        CtContractType contractType = getActiveContractType(request.contractTypeId());
        entity.setEmployee(source.getEmployee());
        entity.setContractType(contractType);
        entity.setContractNumber(request.contractNumber().trim());
        entity.setSignDate(request.signDate());
        entity.setEffectiveDate(request.effectiveDate());
        entity.setEndDate(request.endDate());
        entity.setJobTitle(resolveJobTitle(request.jobTitleId() == null ? source.getJobTitle().getJobTitleId() : request.jobTitleId()));
        entity.setOrgUnit(resolveOrgUnit(request.orgUnitId() == null ? source.getOrgUnit().getOrgUnitId() : request.orgUnitId()));
        entity.setWorkLocation(blankToNull(request.workLocation() == null ? source.getWorkLocation() : request.workLocation()));
        entity.setBaseSalary(scaleMoney(request.baseSalary()));
        entity.setSalaryCurrency(normalizeCurrency(request.salaryCurrency()));
        entity.setWorkingType(request.workingType());
        entity.setContractStatus(ContractStatus.DRAFT);
        entity.setSignedByCompanyUser(null);
        entity.setNote(buildRenewalNote(source, request.note()));

        validateContractData(entity);

        entity = laborContractRepository.save(entity);
        appendStatusHistory(entity, null, ContractStatus.DRAFT, "Khởi tạo hợp đồng kế nhiệm từ hợp đồng #" + source.getContractNumber());

        LaborContractDetailResponse response = toDetail(entity);
        auditLogService.logSuccess(
                "RENEW_CREATE_DRAFT",
                "LABOR_CONTRACT",
                "ct_labor_contract",
                entity.getLaborContractId().toString(),
                null,
                response,
                "Tạo bản nháp gia hạn/ký mới từ hợp đồng nguồn."
        );
        return response;
    }


    @Transactional(readOnly = true)
    public String exportContractHtml(Long laborContractId) {
        LaborContractDetailResponse detail = getDetail(laborContractId);
        return buildContractHtml(detail);
    }

    @Transactional(readOnly = true)
    public String exportAppendixHtml(Long laborContractId, Long appendixId) {
        LaborContractDetailResponse contract = getDetail(laborContractId);
        ContractAppendixResponse appendix = listAppendices(laborContractId).stream()
                .filter(item -> item.contractAppendixId().equals(appendixId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("CONTRACT_APPENDIX_NOT_FOUND", "Không tìm thấy phụ lục hợp đồng."));
        return buildAppendixHtml(contract, appendix);
    }

    @Transactional(readOnly = true)
    public List<ContractAppendixResponse> listAppendices(Long laborContractId) {
        getContract(laborContractId);
        return appendixRepository.findAllByLaborContract_LaborContractIdAndDeletedFalseOrderByEffectiveDateDescContractAppendixIdDesc(laborContractId)
                .stream()
                .map(this::toAppendixResponse)
                .toList();
    }

    @Transactional
    public ContractAppendixResponse createAppendix(Long laborContractId, ContractAppendixRequest request) {
        CtLaborContract contract = getContract(laborContractId);
        validateAppendixForContract(contract, request, null);
        if (appendixRepository.existsByLaborContract_LaborContractIdAndAppendixNumberIgnoreCaseAndDeletedFalse(laborContractId, request.appendixNumber())) {
            throw new BusinessException("CONTRACT_APPENDIX_NUMBER_DUPLICATE", "appendixNumber đã tồn tại trong hợp đồng này.", HttpStatus.CONFLICT);
        }

        CtContractAppendix entity = new CtContractAppendix();
        entity.setLaborContract(contract);
        entity.setAppendixNumber(request.appendixNumber().trim());
        entity.setAppendixName(request.appendixName().trim());
        entity.setEffectiveDate(request.effectiveDate());
        entity.setChangedFieldsJson(blankToNull(request.changedFieldsJson()));
        entity.setStatus(ContractAppendixStatus.DRAFT);
        entity.setNote(blankToNull(request.note()));

        entity = appendixRepository.save(entity);
        ContractAppendixResponse response = toAppendixResponse(entity);
        auditLogService.logSuccess("CREATE", "CONTRACT_APPENDIX", "ct_contract_appendix", entity.getContractAppendixId().toString(), null, response, "Tạo phụ lục hợp đồng.");
        return response;
    }

    @Transactional
    public ContractAppendixResponse updateAppendix(Long laborContractId, Long appendixId, ContractAppendixRequest request) {
        CtContractAppendix entity = getAppendix(laborContractId, appendixId);
        if (entity.getStatus() != ContractAppendixStatus.DRAFT) {
            throw new BusinessException("CONTRACT_APPENDIX_NOT_DRAFT", "Chỉ phụ lục ở trạng thái DRAFT mới được cập nhật.", HttpStatus.CONFLICT);
        }
        validateAppendixForContract(entity.getLaborContract(), request, appendixId);
        if (appendixRepository.existsByLaborContract_LaborContractIdAndAppendixNumberIgnoreCaseAndDeletedFalseAndContractAppendixIdNot(
                laborContractId,
                request.appendixNumber(),
                appendixId
        )) {
            throw new BusinessException("CONTRACT_APPENDIX_NUMBER_DUPLICATE", "appendixNumber đã tồn tại trong hợp đồng này.", HttpStatus.CONFLICT);
        }

        ContractAppendixResponse oldSnapshot = toAppendixResponse(entity);
        entity.setAppendixNumber(request.appendixNumber().trim());
        entity.setAppendixName(request.appendixName().trim());
        entity.setEffectiveDate(request.effectiveDate());
        entity.setChangedFieldsJson(blankToNull(request.changedFieldsJson()));
        entity.setNote(blankToNull(request.note()));

        entity = appendixRepository.save(entity);
        ContractAppendixResponse response = toAppendixResponse(entity);
        auditLogService.logSuccess("UPDATE", "CONTRACT_APPENDIX", "ct_contract_appendix", entity.getContractAppendixId().toString(), oldSnapshot, response, "Cập nhật phụ lục hợp đồng.");
        return response;
    }

    @Transactional
    public ContractAppendixResponse activateAppendix(Long laborContractId, Long appendixId, ContractActionRequest request) {
        CtContractAppendix entity = getAppendix(laborContractId, appendixId);
        if (entity.getStatus() != ContractAppendixStatus.DRAFT) {
            throw new BusinessException("CONTRACT_APPENDIX_ACTIVATE_INVALID", "Chỉ phụ lục DRAFT mới được kích hoạt.", HttpStatus.CONFLICT);
        }
        if (entity.getLaborContract().getContractStatus() == ContractStatus.TERMINATED) {
            throw new BusinessException("CONTRACT_APPENDIX_CONTRACT_TERMINATED", "Không thể kích hoạt phụ lục cho hợp đồng đã chấm dứt.", HttpStatus.CONFLICT);
        }

        ContractAppendixResponse oldSnapshot = toAppendixResponse(entity);
        entity.setStatus(ContractAppendixStatus.ACTIVE);
        if (request != null && request.note() != null && !request.note().isBlank()) {
            entity.setNote(request.note().trim());
        }
        entity = appendixRepository.save(entity);

        ContractAppendixResponse response = toAppendixResponse(entity);
        auditLogService.logSuccess("ACTIVATE", "CONTRACT_APPENDIX", "ct_contract_appendix", entity.getContractAppendixId().toString(), oldSnapshot, response, "Kích hoạt phụ lục hợp đồng.");
        return response;
    }

    @Transactional
    public ContractAppendixResponse cancelAppendix(Long laborContractId, Long appendixId, ContractActionRequest request) {
        CtContractAppendix entity = getAppendix(laborContractId, appendixId);
        if (entity.getEffectiveDate() != null && !entity.getEffectiveDate().isAfter(LocalDate.now())) {
            throw new BusinessException("CONTRACT_APPENDIX_EFFECTIVE_LOCKED", "Không thể hủy phụ lục đã đến hoặc qua ngày hiệu lực.", HttpStatus.CONFLICT);
        }

        ContractAppendixResponse oldSnapshot = toAppendixResponse(entity);
        entity.setStatus(ContractAppendixStatus.CANCELLED);
        if (request != null && request.note() != null && !request.note().isBlank()) {
            entity.setNote(request.note().trim());
        }
        entity = appendixRepository.save(entity);

        ContractAppendixResponse response = toAppendixResponse(entity);
        auditLogService.logSuccess("CANCEL", "CONTRACT_APPENDIX", "ct_contract_appendix", entity.getContractAppendixId().toString(), oldSnapshot, response, "Hủy phụ lục hợp đồng.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<ContractAttachmentResponse> listAttachments(Long laborContractId) {
        getContract(laborContractId);
        return attachmentRepository.findAllByLaborContract_LaborContractIdAndDeletedFalseOrderByUploadedAtDescContractAttachmentIdDesc(laborContractId)
                .stream()
                .map(this::toAttachmentResponse)
                .toList();
    }

    @Transactional
    public ContractAttachmentResponse createAttachment(Long laborContractId, ContractAttachmentRequest request) {
        CtLaborContract contract = getContract(laborContractId);

        CtContractAttachment entity = new CtContractAttachment();
        entity.setLaborContract(contract);
        StoredFileReference storedFile = storageFileService.requireActiveReference(request.storagePath(), "CONTRACT_ATTACHMENT_STORAGE_NOT_FOUND", "storagePath không tồn tại hoặc không còn hiệu lực.");
        entity.setAttachmentType(request.attachmentType());
        entity.setFileName(request.fileName().trim());
        entity.setStoragePath(storedFile.fileKey());
        entity.setMimeType(storedFile.mimeType());
        entity.setFileSizeBytes(storedFile.fileSizeBytes());
        entity.setUploadedAt(LocalDateTime.now());
        entity.setUploadedBy(resolveCurrentUser());
        entity.setStatus(request.status() == null ? DocumentStatus.ACTIVE : request.status());

        entity = attachmentRepository.save(entity);
        ContractAttachmentResponse response = toAttachmentResponse(entity);
        auditLogService.logSuccess("CREATE", "CONTRACT_ATTACHMENT", "ct_contract_attachment", entity.getContractAttachmentId().toString(), null, response, "Tạo metadata file hợp đồng.");
        return response;
    }

    @Transactional
    public ContractAttachmentResponse updateAttachment(Long laborContractId, Long attachmentId, ContractAttachmentRequest request) {
        CtContractAttachment entity = getAttachment(laborContractId, attachmentId);
        ContractAttachmentResponse oldSnapshot = toAttachmentResponse(entity);

        StoredFileReference storedFile = storageFileService.requireActiveReference(request.storagePath(), "CONTRACT_ATTACHMENT_STORAGE_NOT_FOUND", "storagePath không tồn tại hoặc không còn hiệu lực.");
        entity.setAttachmentType(request.attachmentType());
        entity.setFileName(request.fileName().trim());
        entity.setStoragePath(storedFile.fileKey());
        entity.setMimeType(storedFile.mimeType());
        entity.setFileSizeBytes(storedFile.fileSizeBytes());
        entity.setStatus(request.status() == null ? entity.getStatus() : request.status());

        entity = attachmentRepository.save(entity);
        ContractAttachmentResponse response = toAttachmentResponse(entity);
        auditLogService.logSuccess("UPDATE", "CONTRACT_ATTACHMENT", "ct_contract_attachment", entity.getContractAttachmentId().toString(), oldSnapshot, response, "Cập nhật metadata file hợp đồng.");
        return response;
    }

    @Transactional
    public ContractAttachmentResponse archiveAttachment(Long laborContractId, Long attachmentId, ContractActionRequest request) {
        CtContractAttachment entity = getAttachment(laborContractId, attachmentId);
        ContractAttachmentResponse oldSnapshot = toAttachmentResponse(entity);

        entity.setStatus(DocumentStatus.ARCHIVED);
        entity = attachmentRepository.save(entity);

        ContractAttachmentResponse response = toAttachmentResponse(entity);
        auditLogService.logSuccess("ARCHIVE", "CONTRACT_ATTACHMENT", "ct_contract_attachment", entity.getContractAttachmentId().toString(), oldSnapshot, response, request == null ? "Lưu trữ file hợp đồng." : request.note());
        return response;
    }

    @Transactional
    public void deleteAttachment(Long laborContractId, Long attachmentId) {
        CtContractAttachment entity = getAttachment(laborContractId, attachmentId);
        ContractAttachmentResponse oldSnapshot = toAttachmentResponse(entity);

        entity.setDeleted(true);
        attachmentRepository.save(entity);
        auditLogService.logSuccess("DELETE", "CONTRACT_ATTACHMENT", "ct_contract_attachment", entity.getContractAttachmentId().toString(), oldSnapshot, null, "Xóa mềm metadata file hợp đồng.");
    }

    private Specification<CtLaborContract> baseContractSpecification() {
        return (root, query, builder) -> builder.isFalse(root.get("deleted"));
    }


    private String buildContractHtml(LaborContractDetailResponse detail) {
        return """
                <!DOCTYPE html>
                <html lang=\"vi\">
                <head>
                    <meta charset=\"UTF-8\">
                    <title>Hợp đồng %s</title>
                    <style>body{font-family:Arial,sans-serif;line-height:1.6;margin:32px;}h1,h2{margin-bottom:8px;}table{width:100%%;border-collapse:collapse;margin-top:16px;}td,th{border:1px solid #ccc;padding:8px;} .muted{color:#666;font-size:12px;}</style>
                </head>
                <body>
                    <h1>HỢP ĐỒNG LAO ĐỘNG</h1>
                    <p class=\"muted\">Mã hợp đồng: %s</p>
                    <table>
                        <tr><th>Nhân sự</th><td>%s - %s</td></tr>
                        <tr><th>Loại hợp đồng</th><td>%s</td></tr>
                        <tr><th>Ngày ký</th><td>%s</td></tr>
                        <tr><th>Hiệu lực</th><td>%s</td></tr>
                        <tr><th>Ngày kết thúc</th><td>%s</td></tr>
                        <tr><th>Đơn vị</th><td>%s</td></tr>
                        <tr><th>Chức danh</th><td>%s</td></tr>
                        <tr><th>Nơi làm việc</th><td>%s</td></tr>
                        <tr><th>Lương cơ bản</th><td>%s %s</td></tr>
                        <tr><th>Hình thức làm việc</th><td>%s</td></tr>
                        <tr><th>Trạng thái</th><td>%s</td></tr>
                        <tr><th>Ghi chú</th><td>%s</td></tr>
                    </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(detail.contractNumber()),
                escapeHtml(detail.contractNumber()),
                escapeHtml(detail.employeeCode()),
                escapeHtml(detail.employeeFullName()),
                escapeHtml(detail.contractTypeName()),
                detail.signDate(),
                detail.effectiveDate(),
                detail.endDate(),
                escapeHtml(detail.orgUnitName()),
                escapeHtml(detail.jobTitleName()),
                escapeHtml(detail.workLocation()),
                detail.baseSalary(),
                escapeHtml(detail.salaryCurrency()),
                escapeHtml(detail.workingType()),
                escapeHtml(detail.contractStatus()),
                escapeHtml(detail.note())
        );
    }

    private String buildAppendixHtml(LaborContractDetailResponse contract, ContractAppendixResponse appendix) {
        return """
                <!DOCTYPE html>
                <html lang=\"vi\">
                <head>
                    <meta charset=\"UTF-8\">
                    <title>Phụ lục %s</title>
                    <style>body{font-family:Arial,sans-serif;line-height:1.6;margin:32px;}table{width:100%%;border-collapse:collapse;margin-top:16px;}td,th{border:1px solid #ccc;padding:8px;}</style>
                </head>
                <body>
                    <h1>PHỤ LỤC HỢP ĐỒNG</h1>
                    <table>
                        <tr><th>Số phụ lục</th><td>%s</td></tr>
                        <tr><th>Tên phụ lục</th><td>%s</td></tr>
                        <tr><th>Hợp đồng gốc</th><td>%s</td></tr>
                        <tr><th>Nhân sự</th><td>%s - %s</td></tr>
                        <tr><th>Ngày hiệu lực</th><td>%s</td></tr>
                        <tr><th>Trạng thái</th><td>%s</td></tr>
                        <tr><th>Nội dung thay đổi</th><td><pre>%s</pre></td></tr>
                        <tr><th>Ghi chú</th><td>%s</td></tr>
                    </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(appendix.appendixNumber()),
                escapeHtml(appendix.appendixNumber()),
                escapeHtml(appendix.appendixName()),
                escapeHtml(contract.contractNumber()),
                escapeHtml(contract.employeeCode()),
                escapeHtml(contract.employeeFullName()),
                appendix.effectiveDate(),
                escapeHtml(appendix.status()),
                escapeHtml(appendix.changedFieldsJson()),
                escapeHtml(appendix.note())
        );
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private CtLaborContract getContract(Long laborContractId) {
        return laborContractRepository.findByLaborContractIdAndDeletedFalse(laborContractId)
                .orElseThrow(() -> new NotFoundException("LABOR_CONTRACT_NOT_FOUND", "Không tìm thấy hợp đồng lao động."));
    }

    private CtContractAppendix getAppendix(Long laborContractId, Long appendixId) {
        return appendixRepository.findByContractAppendixIdAndLaborContract_LaborContractIdAndDeletedFalse(appendixId, laborContractId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_APPENDIX_NOT_FOUND", "Không tìm thấy phụ lục hợp đồng."));
    }

    private CtContractAttachment getAttachment(Long laborContractId, Long attachmentId) {
        return attachmentRepository.findByContractAttachmentIdAndLaborContract_LaborContractIdAndDeletedFalse(attachmentId, laborContractId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_ATTACHMENT_NOT_FOUND", "Không tìm thấy file đính kèm hợp đồng."));
    }

    private void applyDraftData(CtLaborContract entity, LaborContractUpsertRequest request, boolean keepSignerWhenPossible) {
        CtContractType contractType = getActiveContractType(request.contractTypeId());
        HrEmployee employee = resolveEmployee(request.employeeId());
        HrJobTitle jobTitle = resolveJobTitle(request.jobTitleId());
        HrOrgUnit orgUnit = resolveOrgUnit(request.orgUnitId());

        entity.setEmployee(employee);
        entity.setContractType(contractType);
        entity.setContractNumber(request.contractNumber().trim());
        entity.setSignDate(request.signDate());
        entity.setEffectiveDate(request.effectiveDate());
        entity.setEndDate(request.endDate());
        entity.setJobTitle(jobTitle);
        entity.setOrgUnit(orgUnit);
        entity.setWorkLocation(blankToNull(request.workLocation()));
        entity.setBaseSalary(scaleMoney(request.baseSalary()));
        entity.setSalaryCurrency(normalizeCurrency(request.salaryCurrency()));
        entity.setWorkingType(request.workingType());
        entity.setNote(blankToNull(request.note()));

        if (!keepSignerWhenPossible) {
            entity.setSignedByCompanyUser(null);
        }
        validateContractData(entity);
    }

    private CtContractType getActiveContractType(Long contractTypeId) {
        CtContractType contractType = contractTypeRepository.findByContractTypeIdAndDeletedFalse(contractTypeId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_TYPE_NOT_FOUND", "Không tìm thấy loại hợp đồng."));
        if (contractType.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("CONTRACT_TYPE_INACTIVE", "Loại hợp đồng đang ở trạng thái INACTIVE.", HttpStatus.CONFLICT);
        }
        return contractType;
    }

    private HrEmployee resolveEmployee(Long employeeId) {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân sự."));
    }

    private HrJobTitle resolveJobTitle(Long jobTitleId) {
        HrJobTitle jobTitle = jobTitleRepository.findByJobTitleIdAndDeletedFalse(jobTitleId)
                .orElseThrow(() -> new NotFoundException("JOB_TITLE_NOT_FOUND", "Không tìm thấy chức danh."));
        if (jobTitle.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("JOB_TITLE_INACTIVE", "Chức danh đang ở trạng thái INACTIVE.", HttpStatus.CONFLICT);
        }
        return jobTitle;
    }

    private HrOrgUnit resolveOrgUnit(Long orgUnitId) {
        HrOrgUnit orgUnit = orgUnitRepository.findByOrgUnitIdAndDeletedFalse(orgUnitId)
                .orElseThrow(() -> new NotFoundException("ORG_UNIT_NOT_FOUND", "Không tìm thấy đơn vị tổ chức."));
        if (orgUnit.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("ORG_UNIT_INACTIVE", "Đơn vị tổ chức đang ở trạng thái INACTIVE.", HttpStatus.CONFLICT);
        }
        return orgUnit;
    }

    private SecUserAccount resolveSigner(UUID requestedSignerUserId) {
        UUID signerId = requestedSignerUserId != null ? requestedSignerUserId : SecurityUserContext.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("CURRENT_USER_NOT_FOUND", "Không xác định được người dùng hiện tại.", HttpStatus.UNAUTHORIZED));
        return userAccountRepository.findById(signerId)
                .orElseThrow(() -> new NotFoundException("SIGNER_USER_NOT_FOUND", "Không tìm thấy user ký phía công ty."));
    }

    private SecUserAccount resolveCurrentUser() {
        return SecurityUserContext.getCurrentUserId()
                .flatMap(userAccountRepository::findById)
                .orElse(null);
    }

    private void validateContractData(CtLaborContract entity) {
        if (entity.getSignDate() != null && entity.getEffectiveDate() != null && entity.getSignDate().isAfter(entity.getEffectiveDate())) {
            throw new BusinessException("CONTRACT_SIGN_DATE_INVALID", "signDate không được sau effectiveDate.", HttpStatus.BAD_REQUEST);
        }

        if (entity.getEndDate() != null && entity.getEndDate().isBefore(entity.getEffectiveDate())) {
            throw new BusinessException("CONTRACT_END_DATE_INVALID", "endDate không được trước effectiveDate.", HttpStatus.BAD_REQUEST);
        }

        if (entity.getContractType().isRequiresEndDate() && entity.getEndDate() == null) {
            throw new BusinessException("CONTRACT_END_DATE_REQUIRED", "Loại hợp đồng này bắt buộc khai báo endDate.", HttpStatus.BAD_REQUEST);
        }

        if (!entity.getContractType().isRequiresEndDate() && entity.getEndDate() == null) {
            return;
        }

        Integer maxTermMonths = entity.getContractType().getMaxTermMonths();
        if (maxTermMonths != null && entity.getEndDate() != null) {
            LocalDate maxAllowedEndDate = entity.getEffectiveDate().plusMonths(maxTermMonths).minusDays(1);
            if (entity.getEndDate().isAfter(maxAllowedEndDate)) {
                throw new BusinessException(
                        "CONTRACT_TERM_EXCEED_POLICY",
                        "endDate vượt quá thời hạn tối đa của loại hợp đồng.",
                        HttpStatus.BAD_REQUEST
                );
            }
        }
    }

    private boolean isValidLifecycleTransition(ContractStatus currentStatus, ContractStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return false;
        }
        return switch (currentStatus) {
            case ACTIVE -> targetStatus == ContractStatus.SUSPENDED
                    || targetStatus == ContractStatus.TERMINATED
                    || targetStatus == ContractStatus.EXPIRED;
            case SUSPENDED -> targetStatus == ContractStatus.ACTIVE
                    || targetStatus == ContractStatus.TERMINATED;
            case PENDING_SIGN -> targetStatus == ContractStatus.EXPIRED;
            default -> false;
        };
    }

    private void validateAppendixForContract(CtLaborContract contract, ContractAppendixRequest request, Long appendixId) {
        if (contract.getContractStatus() == ContractStatus.TERMINATED) {
            throw new BusinessException("CONTRACT_TERMINATED", "Không thể thao tác phụ lục trên hợp đồng đã chấm dứt.", HttpStatus.CONFLICT);
        }
        if (request.effectiveDate().isBefore(contract.getEffectiveDate())) {
            throw new BusinessException("CONTRACT_APPENDIX_EFFECTIVE_DATE_INVALID", "effectiveDate của phụ lục không được trước effectiveDate của hợp đồng.", HttpStatus.BAD_REQUEST);
        }
        if (contract.getEndDate() != null && request.effectiveDate().isAfter(contract.getEndDate())) {
            throw new BusinessException("CONTRACT_APPENDIX_AFTER_END_DATE", "effectiveDate của phụ lục không được sau endDate của hợp đồng.", HttpStatus.BAD_REQUEST);
        }
        if (request.changedFieldsJson() != null && request.changedFieldsJson().length() > 4000) {
            throw new BusinessException("CONTRACT_APPENDIX_CHANGED_FIELDS_TOO_LONG", "changedFieldsJson quá dài, cần tối ưu payload snapshot.", HttpStatus.BAD_REQUEST);
        }
        if (appendixId == null && request.appendixNumber().isBlank()) {
            throw new BusinessException("CONTRACT_APPENDIX_NUMBER_REQUIRED", "appendixNumber là bắt buộc.", HttpStatus.BAD_REQUEST);
        }
    }

    private void appendStatusHistory(CtLaborContract contract, ContractStatus fromStatus, ContractStatus toStatus, String reason) {
        CtContractStatusHistory history = new CtContractStatusHistory();
        history.setLaborContract(contract);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(resolveCurrentUser());
        history.setReason(blankToNull(reason));
        statusHistoryRepository.save(history);
    }

    private String buildReviewReason(boolean approved, String reason) {
        String prefix = approved ? "APPROVED" : "REJECTED";
        String trimmedReason = blankToNull(reason);
        return trimmedReason == null ? prefix : prefix + ": " + trimmedReason;
    }

    private String buildRenewalNote(CtLaborContract source, String note) {
        String relation = "Kế nhiệm từ hợp đồng #" + source.getContractNumber();
        String customNote = blankToNull(note);
        return customNote == null ? relation : relation + " | " + customNote;
    }

    private void assertContractStatus(CtLaborContract entity, ContractStatus expectedStatus) {
        if (entity.getContractStatus() != expectedStatus) {
            throw new BusinessException(
                    "CONTRACT_STATUS_INVALID",
                    "Hợp đồng phải ở trạng thái " + expectedStatus + " nhưng hiện tại là " + entity.getContractStatus() + ".",
                    HttpStatus.CONFLICT
            );
        }
    }

    private LaborContractListItemResponse toListItem(CtLaborContract entity) {
        Integer expiringInDays = null;
        if (entity.getEndDate() != null) {
            expiringInDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), entity.getEndDate());
        }
        return new LaborContractListItemResponse(
                entity.getLaborContractId(),
                entity.getContractNumber(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getContractType().getContractTypeId(),
                entity.getContractType().getContractTypeCode(),
                entity.getContractType().getContractTypeName(),
                entity.getSignDate(),
                entity.getEffectiveDate(),
                entity.getEndDate(),
                entity.getJobTitle().getJobTitleId(),
                entity.getJobTitle().getJobTitleCode(),
                entity.getJobTitle().getJobTitleName(),
                entity.getOrgUnit().getOrgUnitId(),
                entity.getOrgUnit().getOrgUnitCode(),
                entity.getOrgUnit().getOrgUnitName(),
                entity.getWorkingType().name(),
                entity.getBaseSalary(),
                entity.getSalaryCurrency(),
                entity.getContractStatus().name(),
                expiringInDays
        );
    }

    private LaborContractDetailResponse toDetail(CtLaborContract entity) {
        return new LaborContractDetailResponse(
                entity.getLaborContractId(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getContractType().getContractTypeId(),
                entity.getContractType().getContractTypeCode(),
                entity.getContractType().getContractTypeName(),
                entity.getContractNumber(),
                entity.getSignDate(),
                entity.getEffectiveDate(),
                entity.getEndDate(),
                entity.getJobTitle().getJobTitleId(),
                entity.getJobTitle().getJobTitleCode(),
                entity.getJobTitle().getJobTitleName(),
                entity.getOrgUnit().getOrgUnitId(),
                entity.getOrgUnit().getOrgUnitCode(),
                entity.getOrgUnit().getOrgUnitName(),
                entity.getWorkLocation(),
                entity.getBaseSalary(),
                entity.getSalaryCurrency(),
                entity.getWorkingType().name(),
                entity.getContractStatus().name(),
                entity.getSignedByCompanyUser() == null ? null : entity.getSignedByCompanyUser().getUserId(),
                entity.getSignedByCompanyUser() == null ? null : entity.getSignedByCompanyUser().getUsername(),
                entity.getNote(),
                appendixRepository.findAllByLaborContract_LaborContractIdAndDeletedFalseOrderByEffectiveDateDescContractAppendixIdDesc(entity.getLaborContractId())
                        .stream()
                        .map(this::toAppendixResponse)
                        .toList(),
                attachmentRepository.findAllByLaborContract_LaborContractIdAndDeletedFalseOrderByUploadedAtDescContractAttachmentIdDesc(entity.getLaborContractId())
                        .stream()
                        .map(this::toAttachmentResponse)
                        .toList()
        );
    }

    private ContractAppendixResponse toAppendixResponse(CtContractAppendix entity) {
        return new ContractAppendixResponse(
                entity.getContractAppendixId(),
                entity.getAppendixNumber(),
                entity.getAppendixName(),
                entity.getEffectiveDate(),
                entity.getChangedFieldsJson(),
                entity.getStatus().name(),
                entity.getNote()
        );
    }

    private ContractAttachmentResponse toAttachmentResponse(CtContractAttachment entity) {
        return new ContractAttachmentResponse(
                entity.getContractAttachmentId(),
                entity.getAttachmentType().name(),
                entity.getFileName(),
                entity.getStoragePath(),
                entity.getMimeType(),
                entity.getFileSizeBytes(),
                entity.getUploadedAt(),
                entity.getUploadedBy() == null ? null : entity.getUploadedBy().getUserId(),
                entity.getUploadedBy() == null ? null : entity.getUploadedBy().getUsername(),
                entity.getStatus().name()
        );
    }

    private ContractStatusHistoryResponse toStatusHistoryResponse(CtContractStatusHistory entity) {
        return new ContractStatusHistoryResponse(
                entity.getContractStatusHistoryId(),
                entity.getFromStatus() == null ? null : entity.getFromStatus().name(),
                entity.getToStatus().name(),
                entity.getChangedAt(),
                entity.getChangedBy() == null ? null : entity.getChangedBy().getUserId(),
                entity.getChangedBy() == null ? null : entity.getChangedBy().getUsername(),
                entity.getReason()
        );
    }

    private String normalizeCurrency(String value) {
        return value == null || value.isBlank() ? "VND" : value.trim().toUpperCase();
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return value == null ? null : value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
