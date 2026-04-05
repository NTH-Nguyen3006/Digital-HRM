package com.company.hrm.module.employee.entity;

import com.company.hrm.common.constant.RelationshipCode;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hr_employee_emergency_contact")
public class HrEmployeeEmergencyContact extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emergency_contact_id", nullable = false, updatable = false)
    private Long emergencyContactId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Column(name = "contact_name", nullable = false, length = 200)
    private String contactName;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_code", nullable = false, length = 30)
    private RelationshipCode relationshipCode;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "address_line", length = 300)
    private String addressLine;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "note", length = 500)
    private String note;
}
