package com.company.hrm.module.orgunit.entity;

import com.company.hrm.common.constant.OrgUnitType;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hr_org_unit")
public class HrOrgUnit extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "org_unit_id", nullable = false, updatable = false)
    private Long orgUnitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_org_unit_id")
    private HrOrgUnit parentOrgUnit;

    @Column(name = "org_unit_code", nullable = false, length = 30)
    private String orgUnitCode;

    @Column(name = "org_unit_name", nullable = false, length = 200)
    private String orgUnitName;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_unit_type", nullable = false, length = 30)
    private OrgUnitType orgUnitType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_employee_id")
    private HrEmployee managerEmployee;

    @Column(name = "hierarchy_level", nullable = false)
    private Integer hierarchyLevel;

    @Column(name = "path_code", length = 500)
    private String pathCode;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}
