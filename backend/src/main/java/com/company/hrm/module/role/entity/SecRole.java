package com.company.hrm.module.role.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sec_role")
public class SecRole extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", nullable = false, unique = true, length = 30)
    private RoleCode roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;

    @Column(name = "is_system_role", nullable = false)
    private boolean systemRole;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
