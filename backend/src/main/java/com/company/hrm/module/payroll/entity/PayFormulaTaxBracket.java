package com.company.hrm.module.payroll.entity;

import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pay_formula_tax_bracket")
public class PayFormulaTaxBracket extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formula_tax_bracket_id", nullable = false, updatable = false)
    private Long formulaTaxBracketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_version_id", nullable = false)
    private PayFormulaVersion formulaVersion;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    @Column(name = "income_from", nullable = false, precision = 18, scale = 2)
    private BigDecimal incomeFrom;

    @Column(name = "income_to", precision = 18, scale = 2)
    private BigDecimal incomeTo;

    @Column(name = "tax_rate", nullable = false, precision = 9, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "quick_deduction", nullable = false, precision = 18, scale = 2)
    private BigDecimal quickDeduction;
}
