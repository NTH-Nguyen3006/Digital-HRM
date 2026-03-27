package com.company.hrm.module.role.dto;

public record PermissionSummaryResponse(
        String permissionCode,
        String moduleCode,
        String actionCode,
        String permissionName,
        boolean allowed
) {
}
