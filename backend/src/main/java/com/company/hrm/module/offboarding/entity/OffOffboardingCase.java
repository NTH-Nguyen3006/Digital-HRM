package com.company.hrm.module.offboarding.entity;

import com.company.hrm.common.constant.OffboardingStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.payroll.entity.PayPayrollItem;
import com.company.hrm.module.payroll.entity.PayPayrollPeriod;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "off_offboarding_case")
public class OffOffboardingCase extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offboarding_case_id", nullable = false, updatable = false)
    private Long offboardingCaseId;

    @Column(name = "offboarding_code", nullable = false, length = 30)
    private String offboardingCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id")
    private SecUserAccount requestedByUser;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "requested_last_working_date")
    private LocalDate requestedLastWorkingDate;

    @Column(name = "request_reason", nullable = false, length = 1000)
    private String requestReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OffboardingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_reviewed_by")
    private SecUserAccount managerReviewedBy;

    @Column(name = "manager_reviewed_at")
    private LocalDateTime managerReviewedAt;

    @Column(name = "manager_review_note", length = 1000)
    private String managerReviewNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_finalized_by")
    private SecUserAccount hrFinalizedBy;

    @Column(name = "hr_finalized_at")
    private LocalDateTime hrFinalizedAt;

    @Column(name = "effective_last_working_date")
    private LocalDate effectiveLastWorkingDate;

    @Column(name = "hr_finalize_note", length = 1000)
    private String hrFinalizeNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_revoked_by")
    private SecUserAccount accessRevokedBy;

    @Column(name = "access_revoked_at")
    private LocalDateTime accessRevokedAt;

    @Column(name = "access_revoke_note", length = 1000)
    private String accessRevokeNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_prepared_by")
    private SecUserAccount settlementPreparedBy;

    @Column(name = "settlement_prepared_at")
    private LocalDateTime settlementPreparedAt;

    @Column(name = "final_attendance_year")
    private Integer finalAttendanceYear;

    @Column(name = "final_attendance_month")
    private Integer finalAttendanceMonth;

    @Column(name = "leave_settlement_units", precision = 18, scale = 2)
    private BigDecimal leaveSettlementUnits;

    @Column(name = "leave_settlement_amount", precision = 18, scale = 2)
    private BigDecimal leaveSettlementAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_payroll_period_id")
    private PayPayrollPeriod finalPayrollPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_payroll_item_id")
    private PayPayrollItem finalPayrollItem;

    @Column(name = "settlement_note", length = 1000)
    private String settlementNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    private SecUserAccount closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "close_note", length = 1000)
    private String closeNote;
}
