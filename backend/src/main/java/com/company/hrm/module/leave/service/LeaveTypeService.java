package com.company.hrm.module.leave.service;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.leave.dto.*;
import com.company.hrm.module.leave.entity.LeaLeaveType;
import com.company.hrm.module.leave.entity.LeaLeaveTypeRule;
import com.company.hrm.module.leave.repository.LeaLeaveTypeRepository;
import com.company.hrm.module.leave.repository.LeaLeaveTypeRuleRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveTypeService {

    private final LeaLeaveTypeRepository leaveTypeRepository;
    private final LeaLeaveTypeRuleRepository leaveTypeRuleRepository;
    private final AuditLogService auditLogService;

    public LeaveTypeService(
            LeaLeaveTypeRepository leaveTypeRepository,
            LeaLeaveTypeRuleRepository leaveTypeRuleRepository,
            AuditLogService auditLogService
    ) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveTypeRuleRepository = leaveTypeRuleRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeListItemResponse> list(String keyword, RecordStatus status) {
        Specification<LeaLeaveType> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("leaveTypeCode")), likeValue),
                    builder.like(builder.lower(root.get("leaveTypeName")), likeValue)
            ));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        return leaveTypeRepository.findAll(specification).stream()
                .sorted((a, b) -> a.getLeaveTypeCode().compareToIgnoreCase(b.getLeaveTypeCode()))
                .map(this::toListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaveTypeDetailResponse getDetail(Long leaveTypeId) {
        return toDetail(getLeaveType(leaveTypeId));
    }

    @Transactional
    public LeaveTypeDetailResponse create(LeaveTypeUpsertRequest request) {
        validateApprovalRole(request.approvalRoleCode());
        if (leaveTypeRepository.existsByLeaveTypeCodeIgnoreCaseAndDeletedFalse(request.leaveTypeCode())) {
            throw new BusinessException("LEAVE_TYPE_CODE_DUPLICATE", "leaveTypeCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        LeaLeaveType leaveType = new LeaLeaveType();
        leaveType.setLeaveTypeCode(request.leaveTypeCode().trim().toUpperCase());
        leaveType.setLeaveTypeName(request.leaveTypeName().trim());
        leaveType.setStatus(RecordStatus.ACTIVE);
        leaveType.setDescription(blankToNull(request.description()));
        leaveType = leaveTypeRepository.save(leaveType);

        LeaLeaveTypeRule rule = buildRule(leaveType, request, 1);
        leaveTypeRuleRepository.save(rule);

        LeaveTypeDetailResponse response = toDetail(leaveType);
        auditLogService.logSuccess("CREATE", "LEAVE_TYPE", "lea_leave_type", leaveType.getLeaveTypeId().toString(), null, response, "Tạo loại nghỉ.");
        return response;
    }

    @Transactional
    public LeaveTypeDetailResponse update(Long leaveTypeId, LeaveTypeUpsertRequest request) {
        validateApprovalRole(request.approvalRoleCode());
        LeaLeaveType leaveType = getLeaveType(leaveTypeId);
        LeaveTypeDetailResponse oldSnapshot = toDetail(leaveType);
        if (leaveTypeRepository.existsByLeaveTypeCodeIgnoreCaseAndDeletedFalseAndLeaveTypeIdNot(request.leaveTypeCode(), leaveTypeId)) {
            throw new BusinessException("LEAVE_TYPE_CODE_DUPLICATE", "leaveTypeCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        LeaLeaveTypeRule currentRule = getCurrentRule(leaveTypeId, LocalDate.now());
        if (request.effectiveFrom().isBefore(currentRule.getEffectiveFrom())) {
            throw new BusinessException("LEAVE_TYPE_EFFECTIVE_FROM_INVALID", "effectiveFrom không được nhỏ hơn version hiện tại.", HttpStatus.BAD_REQUEST);
        }

        leaveType.setLeaveTypeCode(request.leaveTypeCode().trim().toUpperCase());
        leaveType.setLeaveTypeName(request.leaveTypeName().trim());
        leaveType.setDescription(blankToNull(request.description()));
        leaveTypeRepository.save(leaveType);

        if (currentRule.getEffectiveTo() == null || currentRule.getEffectiveTo().isAfter(request.effectiveFrom().minusDays(1))) {
            currentRule.setEffectiveTo(request.effectiveFrom().minusDays(1));
            leaveTypeRuleRepository.save(currentRule);
        }
        int nextVersion = leaveTypeRuleRepository.findMaxVersionNo(leaveTypeId) == null ? 1 : leaveTypeRuleRepository.findMaxVersionNo(leaveTypeId) + 1;
        leaveTypeRuleRepository.save(buildRule(leaveType, request, nextVersion));

        LeaveTypeDetailResponse response = toDetail(leaveType);
        auditLogService.logSuccess("UPDATE", "LEAVE_TYPE", "lea_leave_type", leaveTypeId.toString(), oldSnapshot, response, "Cập nhật loại nghỉ bằng version mới.");
        return response;
    }

    @Transactional
    public LeaveTypeDetailResponse deactivate(Long leaveTypeId, DeactivateLeaveTypeRequest request) {
        LeaLeaveType leaveType = getLeaveType(leaveTypeId);
        LeaveTypeDetailResponse oldSnapshot = toDetail(leaveType);
        LeaLeaveTypeRule currentRule = getCurrentRule(leaveTypeId, LocalDate.now());
        if (request.effectiveTo().isBefore(currentRule.getEffectiveFrom())) {
            throw new BusinessException("LEAVE_TYPE_DEACTIVATE_DATE_INVALID", "effectiveTo không hợp lệ.", HttpStatus.BAD_REQUEST);
        }
        leaveType.setStatus(RecordStatus.INACTIVE);
        leaveTypeRepository.save(leaveType);
        currentRule.setEffectiveTo(request.effectiveTo());
        leaveTypeRuleRepository.save(currentRule);
        LeaveTypeDetailResponse response = toDetail(leaveType);
        auditLogService.logSuccess("DEACTIVATE", "LEAVE_TYPE", "lea_leave_type", leaveTypeId.toString(), oldSnapshot, response, request.reason());
        return response;
    }

    @Transactional(readOnly = true)
    public LeaLeaveType getLeaveTypeEntity(Long leaveTypeId) {
        return getLeaveType(leaveTypeId);
    }

    @Transactional(readOnly = true)
    public LeaLeaveTypeRule getCurrentRule(Long leaveTypeId, LocalDate referenceDate) {
        return leaveTypeRuleRepository.findEffectiveRules(leaveTypeId, referenceDate).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("LEAVE_TYPE_RULE_NOT_EFFECTIVE", "Không tìm thấy rule hiệu lực cho loại nghỉ tại ngày yêu cầu.", HttpStatus.BAD_REQUEST));
    }

    private LeaLeaveType getLeaveType(Long leaveTypeId) {
        return leaveTypeRepository.findByLeaveTypeIdAndDeletedFalse(leaveTypeId)
                .orElseThrow(() -> new NotFoundException("LEAVE_TYPE_NOT_FOUND", "Không tìm thấy loại nghỉ."));
    }

    private LeaLeaveTypeRule buildRule(LeaLeaveType leaveType, LeaveTypeUpsertRequest request, int versionNo) {
        LeaLeaveTypeRule rule = new LeaLeaveTypeRule();
        rule.setLeaveType(leaveType);
        rule.setVersionNo(versionNo);
        rule.setEffectiveFrom(request.effectiveFrom());
        rule.setEffectiveTo(null);
        rule.setUnitType(request.unitType());
        rule.setDefaultEntitlementUnits(request.defaultEntitlementUnits());
        rule.setCarryForwardMaxUnits(request.carryForwardMaxUnits());
        rule.setPaid(request.paid());
        rule.setRequiresBalanceCheck(request.requiresBalanceCheck());
        rule.setRequiresAttachment(request.requiresAttachment());
        rule.setApprovalRoleCode(request.approvalRoleCode());
        rule.setAllowNegativeBalance(request.allowNegativeBalance());
        rule.setMinNoticeDays(request.minNoticeDays());
        rule.setMaxConsecutiveUnits(request.maxConsecutiveUnits());
        rule.setNote(blankToNull(request.ruleNote()));
        return rule;
    }

    private void validateApprovalRole(RoleCode roleCode) {
        if (roleCode != RoleCode.MANAGER && roleCode != RoleCode.HR) {
            throw new BusinessException("LEAVE_TYPE_APPROVAL_ROLE_INVALID", "approvalRoleCode chỉ hỗ trợ MANAGER hoặc HR.", HttpStatus.BAD_REQUEST);
        }
    }

    private LeaveTypeListItemResponse toListItem(LeaLeaveType entity) {
        LeaLeaveTypeRule currentRule = leaveTypeRuleRepository.findEffectiveRules(entity.getLeaveTypeId(), LocalDate.now()).stream().findFirst()
                .orElseGet(() -> leaveTypeRuleRepository.findAllByLeaveTypeLeaveTypeIdAndDeletedFalseOrderByVersionNoDesc(entity.getLeaveTypeId()).stream().findFirst().orElse(null));
        return new LeaveTypeListItemResponse(
                entity.getLeaveTypeId(),
                entity.getLeaveTypeCode(),
                entity.getLeaveTypeName(),
                entity.getStatus().name(),
                entity.getDescription(),
                currentRule == null ? null : toRuleResponse(currentRule)
        );
    }

    private LeaveTypeDetailResponse toDetail(LeaLeaveType entity) {
        List<LeaveTypeRuleResponse> history = leaveTypeRuleRepository.findAllByLeaveTypeLeaveTypeIdAndDeletedFalseOrderByVersionNoDesc(entity.getLeaveTypeId())
                .stream()
                .map(this::toRuleResponse)
                .toList();
        return new LeaveTypeDetailResponse(
                entity.getLeaveTypeId(),
                entity.getLeaveTypeCode(),
                entity.getLeaveTypeName(),
                entity.getStatus().name(),
                entity.getDescription(),
                history.isEmpty() ? null : history.get(0),
                history
        );
    }

    private LeaveTypeRuleResponse toRuleResponse(LeaLeaveTypeRule entity) {
        return new LeaveTypeRuleResponse(
                entity.getLeaveTypeRuleId(),
                entity.getVersionNo(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getUnitType().name(),
                entity.getDefaultEntitlementUnits(),
                entity.getCarryForwardMaxUnits(),
                entity.isPaid(),
                entity.isRequiresBalanceCheck(),
                entity.isRequiresAttachment(),
                entity.getApprovalRoleCode().name(),
                entity.isAllowNegativeBalance(),
                entity.getMinNoticeDays(),
                entity.getMaxConsecutiveUnits(),
                entity.getNote()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
