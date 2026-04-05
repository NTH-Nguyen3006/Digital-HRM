package com.company.hrm.module.employee.entity;

import com.company.hrm.common.constant.EmploymentStatus;
import com.company.hrm.common.constant.GenderCode;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.jobtitle.entity.HrJobTitle;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hr_employee")
public class HrEmployee extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id", nullable = false, updatable = false)
    private Long employeeId;

    @Column(name = "employee_code", nullable = false, length = 30)
    private String employeeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id", nullable = false)
    private HrOrgUnit orgUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id", nullable = false)
    private HrJobTitle jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_employee_id")
    private HrEmployee managerEmployee;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "work_email", length = 150)
    private String workEmail;

    @Column(name = "work_phone", length = 20)
    private String workPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_code", nullable = false, length = 10)
    private GenderCode genderCode;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 30)
    private EmploymentStatus employmentStatus;

    @Column(name = "work_location", length = 200)
    private String workLocation;

    @Column(name = "tax_code", length = 30)
    private String taxCode;

    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Column(name = "mobile_phone", length = 20)
    private String mobilePhone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "note", length = 1000)
    private String note;
}
