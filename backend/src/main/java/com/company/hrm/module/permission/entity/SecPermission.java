package com.company.hrm.module.permission.entity;

import com.company.hrm.common.constant.RecordStatus;
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
@Table(name = "sec_permission")
public class SecPermission extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "permission_id", nullable = false, updatable = false)
    private UUID permissionId;

    @Column(name = "permission_code", nullable = false, unique = true, length = 100)
    private String permissionCode;

    @Column(name = "module_code", nullable = false, length = 50)
    private String moduleCode;

    @Column(name = "action_code", nullable = false, length = 30)
    private String actionCode;

    @Column(name = "permission_name", nullable = false, length = 150)
    private String permissionName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;
}
