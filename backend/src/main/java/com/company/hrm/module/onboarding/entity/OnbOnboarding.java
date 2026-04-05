package com.company.hrm.module.onboarding.entity;

import com.company.hrm.common.constant.GenderCode;
import com.company.hrm.common.constant.OnboardingStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.contract.entity.CtLaborContract;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.jobtitle.entity.HrJobTitle;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "onb_onboarding")
public class OnbOnboarding extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "onboarding_id", nullable = false, updatable = false)
    private Long onboardingId;

    @Column(name = "onboarding_code", nullable = false, length = 30)
    private String onboardingCode;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Column(name = "personal_phone", length = 20)
    private String personalPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_code", nullable = false, length = 10)
    private GenderCode genderCode;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "planned_start_date", nullable = false)
    private LocalDate plannedStartDate;

    @Column(name = "employee_code", length = 30)
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

    @Column(name = "work_location", length = 200)
    private String workLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_employee_id")
    private HrEmployee linkedEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_user_id")
    private SecUserAccount linkedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_contract_id")
    private CtLaborContract firstContract;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OnboardingStatus status;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "orientation_confirmed_at")
    private LocalDateTime orientationConfirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orientation_confirmed_by")
    private SecUserAccount orientationConfirmedBy;

    @Column(name = "orientation_note", length = 1000)
    private String orientationNote;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private SecUserAccount completedBy;

    @Column(name = "completed_note", length = 1000)
    private String completedNote;
}
