package com.company.hrm.module.role.entity;

import com.company.hrm.common.entity.CreatedAuditEntity;
import com.company.hrm.module.permission.entity.SecPermission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sec_role_permission")
public class SecRolePermission extends CreatedAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_permission_id", nullable = false, updatable = false)
    private UUID rolePermissionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private SecRole role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private SecPermission permission;

    @Column(name = "is_allowed", nullable = false)
    private boolean allowed;
}
