package com.company.hrm.module.payroll.entity;

import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pay_employee_compensation_item")
public class PayEmployeeCompensationItem extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_compensation_item_id", nullable = false, updatable = false)
    private Long employeeCompensationItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_compensation_id", nullable = false)
    private PayEmployeeCompensation employeeCompensation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_component_id", nullable = false)
    private PaySalaryComponent salaryComponent;

    @Column(name = "amount_value", precision = 18, scale = 2)
    private BigDecimal amountValue;

    @Column(name = "percentage_value", precision = 9, scale = 4)
    private BigDecimal percentageValue;

    @Column(name = "note", length = 500)
    private String note;
}
