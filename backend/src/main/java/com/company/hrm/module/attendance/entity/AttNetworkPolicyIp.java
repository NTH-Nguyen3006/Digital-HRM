package com.company.hrm.module.attendance.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "att_network_policy_ip")
public class AttNetworkPolicyIp extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "network_policy_ip_id", nullable = false, updatable = false)
    private Long networkPolicyIpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_policy_id", nullable = false)
    private AttNetworkPolicy networkPolicy;

    @Column(name = "cidr_or_ip", nullable = false, length = 64)
    private String cidrOrIp;

    @Column(name = "description", length = 250)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;
}
