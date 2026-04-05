package com.company.hrm.module.leave.entity;

import com.company.hrm.common.constant.LeaveBalanceTransactionType;
import com.company.hrm.common.entity.CreatedAuditEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lea_leave_balance_txn")
public class LeaLeaveBalanceTransaction extends CreatedAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_balance_txn_id", nullable = false, updatable = false)
    private Long leaveBalanceTxnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_balance_id", nullable = false)
    private LeaLeaveBalance leaveBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaLeaveType leaveType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_request_id")
    private LeaLeaveRequest leaveRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private LeaveBalanceTransactionType transactionType;

    @Column(name = "quantity_delta", nullable = false, precision = 9, scale = 2)
    private BigDecimal quantityDelta;

    @Column(name = "balance_before", nullable = false, precision = 9, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 9, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "attachment_ref", length = 500)
    private String attachmentRef;
}
