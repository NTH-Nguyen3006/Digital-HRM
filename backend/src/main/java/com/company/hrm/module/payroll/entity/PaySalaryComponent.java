package com.company.hrm.module.payroll.entity;

import com.company.hrm.common.constant.PayrollAmountType;
import com.company.hrm.common.constant.PayrollComponentCategory;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pay_salary_component")
public class PaySalaryComponent extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salary_component_id", nullable = false, updatable = false)
    private Long salaryComponentId;

    @Column(name = "component_code", nullable = false, length = 30)
    private String componentCode;

    @Column(name = "component_name", nullable = false, length = 200)
    private String componentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_category", nullable = false, length = 20)
    private PayrollComponentCategory componentCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "amount_type", nullable = false, length = 20)
    private PayrollAmountType amountType;

    @Column(name = "taxable", nullable = false)
    private boolean taxable;

    @Column(name = "insurance_base_included", nullable = false)
    private boolean insuranceBaseIncluded;

    @Column(name = "payslip_visible", nullable = false)
    private boolean payslipVisible = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "description", length = 500)
    private String description;
}
