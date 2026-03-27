package com.company.hrm.module.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRoleHistoryResponse(
        UUID userRoleId,
        String roleCode,
        String roleName,
        boolean primaryRole,
        String status,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveTo
) {
}
