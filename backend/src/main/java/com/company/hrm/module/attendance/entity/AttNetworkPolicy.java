package com.company.hrm.module.attendance.entity;

import com.company.hrm.common.constant.AttendanceNetworkPolicyScopeType;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "att_network_policy")
public class AttNetworkPolicy extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "network_policy_id", nullable = false, updatable = false)
    private Long networkPolicyId;

    @Column(name = "policy_code", nullable = false, length = 30)
    private String policyCode;

    @Column(name = "policy_name", nullable = false, length = 200)
    private String policyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private AttendanceNetworkPolicyScopeType scopeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id")
    private HrOrgUnit orgUnit;

    @Column(name = "allow_check_in", nullable = false)
    private boolean allowCheckIn = true;

    @Column(name = "allow_check_out", nullable = false)
    private boolean allowCheckOut = true;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "description", length = 500)
    private String description;
}
