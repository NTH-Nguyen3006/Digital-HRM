package com.company.hrm.module.payroll.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pay_formula_version")
public class PayFormulaVersion extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formula_version_id", nullable = false, updatable = false)
    private Long formulaVersionId;

    @Column(name = "formula_code", nullable = false, length = 30)
    private String formulaCode;

    @Column(name = "formula_name", nullable = false, length = 200)
    private String formulaName;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "personal_deduction_monthly", nullable = false, precision = 18, scale = 2)
    private BigDecimal personalDeductionMonthly;

    @Column(name = "dependent_deduction_monthly", nullable = false, precision = 18, scale = 2)
    private BigDecimal dependentDeductionMonthly;

    @Column(name = "social_insurance_employee_rate", nullable = false, precision = 9, scale = 4)
    private BigDecimal socialInsuranceEmployeeRate;

    @Column(name = "health_insurance_employee_rate", nullable = false, precision = 9, scale = 4)
    private BigDecimal healthInsuranceEmployeeRate;

    @Column(name = "unemployment_insurance_employee_rate", nullable = false, precision = 9, scale = 4)
    private BigDecimal unemploymentInsuranceEmployeeRate;

    @Column(name = "insurance_salary_cap_amount", precision = 18, scale = 2)
    private BigDecimal insuranceSalaryCapAmount;

    @Column(name = "unemployment_salary_cap_amount", precision = 18, scale = 2)
    private BigDecimal unemploymentSalaryCapAmount;

    @Column(name = "standard_work_hours_per_day", nullable = false, precision = 9, scale = 2)
    private BigDecimal standardWorkHoursPerDay;

    @Column(name = "overtime_multiplier_weekday", nullable = false, precision = 9, scale = 4)
    private BigDecimal overtimeMultiplierWeekday;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "note", length = 1000)
    private String note;
}
