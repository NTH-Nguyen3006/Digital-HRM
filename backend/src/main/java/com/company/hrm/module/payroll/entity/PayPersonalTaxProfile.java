package com.company.hrm.module.payroll.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.TaxFinalizationMethod;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pay_personal_tax_profile")
public class PayPersonalTaxProfile extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personal_tax_profile_id", nullable = false, updatable = false)
    private Long personalTaxProfileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Column(name = "resident_taxpayer", nullable = false)
    private boolean residentTaxpayer = true;

    @Column(name = "family_deduction_enabled", nullable = false)
    private boolean familyDeductionEnabled = true;

    @Column(name = "tax_registration_date")
    private LocalDate taxRegistrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_finalization_method", nullable = false, length = 30)
    private TaxFinalizationMethod taxFinalizationMethod = TaxFinalizationMethod.MONTHLY_WITHHOLDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "note", length = 1000)
    private String note;
}
