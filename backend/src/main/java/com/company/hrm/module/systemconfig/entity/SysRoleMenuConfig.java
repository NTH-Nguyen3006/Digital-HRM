package com.company.hrm.module.systemconfig.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.role.entity.SecRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_role_menu_config")
public class SysRoleMenuConfig extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_menu_config_id", nullable = false, updatable = false)
    private Long roleMenuConfigId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private SecRole role;

    @Column(name = "menu_key", nullable = false, length = 100)
    private String menuKey;

    @Column(name = "menu_name", nullable = false, length = 200)
    private String menuName;

    @Column(name = "route_path", nullable = false, length = 255)
    private String routePath;

    @Column(name = "icon_name", length = 100)
    private String iconName;

    @Column(name = "parent_menu_key", length = 100)
    private String parentMenuKey;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_visible", nullable = false)
    private boolean visible = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;
}
