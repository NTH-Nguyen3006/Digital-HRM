package com.company.hrm.module.employee.entity;

import com.company.hrm.common.constant.MaritalStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hr_employee_profile")
public class HrEmployeeProfile extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_profile_id", nullable = false, updatable = false)
    private Long employeeProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "place_of_birth", length = 200)
    private String placeOfBirth;

    @Column(name = "ethnic_group", length = 100)
    private String ethnicGroup;

    @Column(name = "religion", length = 100)
    private String religion;

    @Column(name = "education_level", length = 200)
    private String educationLevel;

    @Column(name = "major", length = 200)
    private String major;

    @Column(name = "emergency_note", length = 500)
    private String emergencyNote;
}
