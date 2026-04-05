package com.company.hrm.module.systemconfig.dto;

public record RoleMenuConfigResponse(
        Long roleMenuConfigId,
        String roleCode,
        String roleName,
        String menuKey,
        String menuName,
        String routePath,
        String iconName,
        String parentMenuKey,
        Integer sortOrder,
        boolean visible,
        String status
) {
}
