package com.company.hrm.module.contract.service;

import com.company.hrm.common.constant.ContractStatus;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.contract.dto.*;
import com.company.hrm.module.contract.entity.CtContractType;
import com.company.hrm.module.contract.repository.CtContractTypeRepository;
import com.company.hrm.module.contract.repository.CtLaborContractRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractTypeService {

    private final CtContractTypeRepository contractTypeRepository;
    private final CtLaborContractRepository laborContractRepository;
    private final AuditLogService auditLogService;

    public ContractTypeService(
            CtContractTypeRepository contractTypeRepository,
            CtLaborContractRepository laborContractRepository,
            AuditLogService auditLogService
    ) {
        this.contractTypeRepository = contractTypeRepository;
        this.laborContractRepository = laborContractRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ContractTypeListItemResponse> list(String keyword, RecordStatus status, int page, int size) {
        Specification<CtContractType> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("contractTypeCode")), likeValue),
                    builder.like(builder.lower(root.get("contractTypeName")), likeValue)
            ));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }

        Page<CtContractType> result = contractTypeRepository.findAll(
                specification,
                PageRequest.of(page, size, Sort.by("contractTypeName"))
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
    public List<ContractTypeOptionResponse> listActiveOptions() {
        return contractTypeRepository.findAllByDeletedFalseAndStatusOrderByContractTypeNameAsc(RecordStatus.ACTIVE)
                .stream()
                .map(entity -> new ContractTypeOptionResponse(
                        entity.getContractTypeId(),
                        entity.getContractTypeCode(),
                        entity.getContractTypeName(),
                        entity.isRequiresEndDate(),
                        entity.getMaxTermMonths()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ContractTypeDetailResponse getDetail(Long contractTypeId) {
        return toDetail(getContractType(contractTypeId));
    }

    @Transactional
    public ContractTypeDetailResponse create(CreateContractTypeRequest request) {
        if (contractTypeRepository.existsByContractTypeCodeIgnoreCaseAndDeletedFalse(request.contractTypeCode())) {
            throw new BusinessException("CONTRACT_TYPE_CODE_DUPLICATE", "contractTypeCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        validateRequest(request.maxTermMonths(), request.requiresEndDate());

        CtContractType entity = new CtContractType();
        entity.setContractTypeCode(normalizeCode(request.contractTypeCode()));
        entity.setContractTypeName(request.contractTypeName().trim());
        entity.setMaxTermMonths(request.maxTermMonths());
        entity.setRequiresEndDate(request.requiresEndDate());
        entity.setStatus(RecordStatus.ACTIVE);
        entity.setDescription(blankToNull(request.description()));

        entity = contractTypeRepository.save(entity);
        ContractTypeDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CREATE", "CONTRACT_TYPE", "ct_contract_type", entity.getContractTypeId().toString(), null, response, "Tạo loại hợp đồng.");
        return response;
    }

    @Transactional
    public ContractTypeDetailResponse update(Long contractTypeId, UpdateContractTypeRequest request) {
        CtContractType entity = getContractType(contractTypeId);
        ContractTypeDetailResponse oldSnapshot = toDetail(entity);

        if (contractTypeRepository.existsByContractTypeCodeIgnoreCaseAndDeletedFalseAndContractTypeIdNot(request.contractTypeCode(), contractTypeId)) {
            throw new BusinessException("CONTRACT_TYPE_CODE_DUPLICATE", "contractTypeCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        validateRequest(request.maxTermMonths(), request.requiresEndDate());

        entity.setContractTypeCode(normalizeCode(request.contractTypeCode()));
        entity.setContractTypeName(request.contractTypeName().trim());
        entity.setMaxTermMonths(request.maxTermMonths());
        entity.setRequiresEndDate(request.requiresEndDate());
        entity.setDescription(blankToNull(request.description()));

        entity = contractTypeRepository.save(entity);
        ContractTypeDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("UPDATE", "CONTRACT_TYPE", "ct_contract_type", entity.getContractTypeId().toString(), oldSnapshot, response, "Cập nhật loại hợp đồng.");
        return response;
    }

    @Transactional
    public ContractTypeDetailResponse changeStatus(Long contractTypeId, UpdateContractTypeStatusRequest request) {
        CtContractType entity = getContractType(contractTypeId);
        ContractTypeDetailResponse oldSnapshot = toDetail(entity);

        if (request.status() == RecordStatus.INACTIVE) {
            long activeContractCount = laborContractRepository.count((root, query, builder) -> builder.and(
                    builder.isFalse(root.get("deleted")),
                    builder.equal(root.get("contractType").get("contractTypeId"), contractTypeId),
                    root.get("contractStatus").in(List.of(
                            ContractStatus.DRAFT,
                            ContractStatus.PENDING_SIGN,
                            ContractStatus.ACTIVE,
                            ContractStatus.SUSPENDED
                    ))
            ));
            if (activeContractCount > 0) {
                throw new BusinessException(
                        "CONTRACT_TYPE_IN_USE",
                        "Không thể ngừng sử dụng loại hợp đồng đang được tham chiếu bởi hợp đồng còn hiệu lực hoặc đang xử lý.",
                        HttpStatus.CONFLICT
                );
            }
        }

        entity.setStatus(request.status());
        entity = contractTypeRepository.save(entity);
        ContractTypeDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CHANGE_STATUS", "CONTRACT_TYPE", "ct_contract_type", entity.getContractTypeId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    private CtContractType getContractType(Long contractTypeId) {
        return contractTypeRepository.findByContractTypeIdAndDeletedFalse(contractTypeId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_TYPE_NOT_FOUND", "Không tìm thấy loại hợp đồng."));
    }

    private void validateRequest(Integer maxTermMonths, boolean requiresEndDate) {
        if (maxTermMonths != null && maxTermMonths <= 0) {
            throw new BusinessException("CONTRACT_TYPE_MAX_TERM_INVALID", "maxTermMonths phải lớn hơn 0 nếu có khai báo.", HttpStatus.BAD_REQUEST);
        }
        if (!requiresEndDate && maxTermMonths != null) {
            throw new BusinessException(
                    "CONTRACT_TYPE_TERM_POLICY_INVALID",
                    "Loại hợp đồng không bắt buộc ngày kết thúc thì không nên khai báo maxTermMonths.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ContractTypeListItemResponse toListItem(CtContractType entity) {
        return new ContractTypeListItemResponse(
                entity.getContractTypeId(),
                entity.getContractTypeCode(),
                entity.getContractTypeName(),
                entity.getMaxTermMonths(),
                entity.isRequiresEndDate(),
                entity.getStatus().name(),
                entity.getDescription()
        );
    }

    private ContractTypeDetailResponse toDetail(CtContractType entity) {
        return new ContractTypeDetailResponse(
                entity.getContractTypeId(),
                entity.getContractTypeCode(),
                entity.getContractTypeName(),
                entity.getMaxTermMonths(),
                entity.isRequiresEndDate(),
                entity.getStatus().name(),
                entity.getDescription()
        );
    }
}
