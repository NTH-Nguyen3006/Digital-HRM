package com.company.hrm.module.payroll.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.RelationshipCode;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pay_tax_dependent")
public class PayTaxDependent extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tax_dependent_id", nullable = false, updatable = false)
    private Long taxDependentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_code", nullable = false, length = 20)
    private RelationshipCode relationshipCode;

    @Column(name = "identification_no", length = 50)
    private String identificationNo;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "deduction_start_month", nullable = false)
    private LocalDate deductionStartMonth;

    @Column(name = "deduction_end_month")
    private LocalDate deductionEndMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "note", length = 500)
    private String note;
}
