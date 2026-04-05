package com.company.hrm.module.employee.entity;

import com.company.hrm.common.constant.IdentificationDocumentType;
import com.company.hrm.common.constant.IdentificationStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hr_employee_identification")
public class HrEmployeeIdentification extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_identification_id", nullable = false, updatable = false)
    private Long employeeIdentificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private IdentificationDocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 50)
    private String documentNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "issue_place", length = 200)
    private String issuePlace;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "country_of_issue", length = 100)
    private String countryOfIssue;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IdentificationStatus status;
}
