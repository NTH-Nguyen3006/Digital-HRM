package com.company.hrm.module.leave.entity;

import com.company.hrm.common.constant.LeaveBalanceStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lea_leave_balance")
public class LeaLeaveBalance extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_balance_id", nullable = false, updatable = false)
    private Long leaveBalanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaLeaveType leaveType;

    @Column(name = "leave_year", nullable = false)
    private Integer leaveYear;

    @Column(name = "opening_units", nullable = false, precision = 9, scale = 2)
    private BigDecimal openingUnits;

    @Column(name = "accrued_units", nullable = false, precision = 9, scale = 2)
    private BigDecimal accruedUnits;

    @Column(name = "used_units", nullable = false, precision = 9, scale = 2)
    private BigDecimal usedUnits;

    @Column(name = "adjusted_units", nullable = false, precision = 9, scale = 2)
    private BigDecimal adjustedUnits;

    @Column(name = "carried_forward_units", nullable = false, precision = 9, scale = 2)
    private BigDecimal carriedForwardUnits;

    @Column(name = "settled_units", nullable = false, precision = 9, scale = 2)
    private BigDecimal settledUnits;

    @Column(name = "available_units", nullable = false, precision = 9, scale = 2)
    private BigDecimal availableUnits;

    @Enumerated(EnumType.STRING)
    @Column(name = "balance_status", nullable = false, length = 20)
    private LeaveBalanceStatus balanceStatus;

    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    private SecUserAccount closedBy;

    @Column(name = "settlement_note", length = 500)
    private String settlementNote;
}
