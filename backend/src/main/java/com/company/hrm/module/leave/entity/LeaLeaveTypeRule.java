package com.company.hrm.module.leave.entity;

import com.company.hrm.common.constant.LeaveUnitType;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lea_leave_type_rule")
public class LeaLeaveTypeRule extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_type_rule_id", nullable = false, updatable = false)
    private Long leaveTypeRuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaLeaveType leaveType;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 10)
    private LeaveUnitType unitType;

    @Column(name = "default_entitlement_units", nullable = false, precision = 9, scale = 2)
    private BigDecimal defaultEntitlementUnits;

    @Column(name = "carry_forward_max_units", nullable = false, precision = 9, scale = 2)
    private BigDecimal carryForwardMaxUnits;

    @Column(name = "is_paid", nullable = false)
    private boolean paid;

    @Column(name = "requires_balance_check", nullable = false)
    private boolean requiresBalanceCheck;

    @Column(name = "requires_attachment", nullable = false)
    private boolean requiresAttachment;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_role_code", nullable = false, length = 20)
    private RoleCode approvalRoleCode;

    @Column(name = "allow_negative_balance", nullable = false)
    private boolean allowNegativeBalance;

    @Column(name = "min_notice_days", nullable = false)
    private Integer minNoticeDays;

    @Column(name = "max_consecutive_units", precision = 9, scale = 2)
    private BigDecimal maxConsecutiveUnits;

    @Column(name = "note", length = 500)
    private String note;
}
