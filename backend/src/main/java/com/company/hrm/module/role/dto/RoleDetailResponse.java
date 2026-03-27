package com.company.hrm.module.role.dto;

import java.util.List;
import java.util.UUID;

public record RoleDetailResponse(
        UUID roleId,
        String roleCode,
        String roleName,
        String description,
        String status,
        boolean systemRole,
        int sortOrder,
        long activeUserCount,
        List<PermissionSummaryResponse> permissions,
        List<DataScopeResponse> dataScopes
) {
}
