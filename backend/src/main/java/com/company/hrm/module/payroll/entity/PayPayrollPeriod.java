package com.company.hrm.module.payroll.entity;

import com.company.hrm.common.constant.PayrollPeriodStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.attendance.entity.AttAttendancePeriod;
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
@Table(name = "pay_payroll_period")
public class PayPayrollPeriod extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_period_id", nullable = false, updatable = false)
    private Long payrollPeriodId;

    @Column(name = "period_code", nullable = false, length = 20)
    private String periodCode;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_period_id", nullable = false)
    private AttAttendancePeriod attendancePeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_version_id", nullable = false)
    private PayFormulaVersion formulaVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_status", nullable = false, length = 20)
    private PayrollPeriodStatus periodStatus = PayrollPeriodStatus.DRAFT;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private SecUserAccount generatedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private SecUserAccount approvedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private SecUserAccount publishedBy;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "total_employee_count", nullable = false)
    private Integer totalEmployeeCount = 0;

    @Column(name = "manager_confirmed_count", nullable = false)
    private Integer managerConfirmedCount = 0;

    @Column(name = "total_gross_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalGrossAmount = BigDecimal.ZERO;

    @Column(name = "total_net_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalNetAmount = BigDecimal.ZERO;
}
