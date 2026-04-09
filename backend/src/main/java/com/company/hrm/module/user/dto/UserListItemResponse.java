package com.company.hrm.module.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserListItemResponse(
        UUID userId,
        Long employeeId,
        String employeeCode,
        String username,
        String email,
        String phoneNumber,
        String status,
        boolean mustChangePassword,
        String primaryRoleCode,
        String primaryRoleName,
        LocalDateTime lastLoginAt,
        String lastLoginIp
) {
}
