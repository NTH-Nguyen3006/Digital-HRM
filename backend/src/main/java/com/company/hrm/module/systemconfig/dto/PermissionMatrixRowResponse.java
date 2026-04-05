package com.company.hrm.module.systemconfig.dto;

import java.util.Map;

public record PermissionMatrixRowResponse(
        String permissionCode,
        String moduleCode,
        String actionCode,
        String permissionName,
        Map<String, Boolean> roleAllowed
) {
}
