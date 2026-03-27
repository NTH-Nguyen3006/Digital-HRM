package com.company.hrm.module.role.dto;

import java.util.UUID;

public record RoleListItemResponse(
        UUID roleId,
        String roleCode,
        String roleName,
        String description,
        String status,
        boolean systemRole,
        int sortOrder,
        long activeUserCount
) {
}
