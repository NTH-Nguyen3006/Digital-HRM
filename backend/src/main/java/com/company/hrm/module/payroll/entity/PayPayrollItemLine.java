package com.company.hrm.module.payroll.entity;

import com.company.hrm.common.constant.PayrollComponentCategory;
import com.company.hrm.common.constant.PayrollLineSourceType;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pay_payroll_item_line")
public class PayPayrollItemLine extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_item_line_id", nullable = false, updatable = false)
    private Long payrollItemLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_item_id", nullable = false)
    private PayPayrollItem payrollItem;

    @Column(name = "component_code", nullable = false, length = 30)
    private String componentCode;

    @Column(name = "component_name", nullable = false, length = 200)
    private String componentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_category", nullable = false, length = 20)
    private PayrollComponentCategory componentCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_source_type", nullable = false, length = 20)
    private PayrollLineSourceType lineSourceType;

    @Column(name = "line_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineAmount = BigDecimal.ZERO;

    @Column(name = "taxable", nullable = false)
    private boolean taxable;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "line_note", length = 500)
    private String lineNote;
}
