package com.company.hrm.module.payroll.entity;

import com.company.hrm.common.constant.PayrollItemStatus;
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
@Table(name = "pay_payroll_item")
public class PayPayrollItem extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_item_id", nullable = false, updatable = false)
    private Long payrollItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayPayrollPeriod payrollPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Column(name = "scheduled_day_count", nullable = false)
    private Integer scheduledDayCount = 0;

    @Column(name = "present_day_count", nullable = false)
    private Integer presentDayCount = 0;

    @Column(name = "paid_leave_day_count", nullable = false)
    private Integer paidLeaveDayCount = 0;

    @Column(name = "unpaid_leave_day_count", nullable = false)
    private Integer unpaidLeaveDayCount = 0;

    @Column(name = "absent_day_count", nullable = false)
    private Integer absentDayCount = 0;

    @Column(name = "approved_ot_minutes", nullable = false)
    private Integer approvedOtMinutes = 0;

    @Column(name = "base_salary_monthly", nullable = false, precision = 18, scale = 2)
    private BigDecimal baseSalaryMonthly = BigDecimal.ZERO;

    @Column(name = "base_salary_prorated", nullable = false, precision = 18, scale = 2)
    private BigDecimal baseSalaryProrated = BigDecimal.ZERO;

    @Column(name = "fixed_earning_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal fixedEarningTotal = BigDecimal.ZERO;

    @Column(name = "fixed_deduction_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal fixedDeductionTotal = BigDecimal.ZERO;

    @Column(name = "employee_insurance_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal employeeInsuranceAmount = BigDecimal.ZERO;

    @Column(name = "personal_deduction_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal personalDeductionAmount = BigDecimal.ZERO;

    @Column(name = "dependent_deduction_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal dependentDeductionAmount = BigDecimal.ZERO;

    @Column(name = "taxable_income", nullable = false, precision = 18, scale = 2)
    private BigDecimal taxableIncome = BigDecimal.ZERO;

    @Column(name = "pit_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal pitAmount = BigDecimal.ZERO;

    @Column(name = "gross_income", nullable = false, precision = 18, scale = 2)
    private BigDecimal grossIncome = BigDecimal.ZERO;

    @Column(name = "net_pay", nullable = false, precision = 18, scale = 2)
    private BigDecimal netPay = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false, length = 20)
    private PayrollItemStatus itemStatus = PayrollItemStatus.DRAFT;

    @Column(name = "manager_confirmation_required", nullable = false)
    private boolean managerConfirmationRequired = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_confirmed_by")
    private SecUserAccount managerConfirmedBy;

    @Column(name = "manager_confirmed_at")
    private LocalDateTime managerConfirmedAt;

    @Column(name = "manager_confirm_note", length = 1000)
    private String managerConfirmNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_approved_by")
    private SecUserAccount hrApprovedBy;

    @Column(name = "hr_approved_at")
    private LocalDateTime hrApprovedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private SecUserAccount publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "adjustment_note", length = 1000)
    private String adjustmentNote;
}
