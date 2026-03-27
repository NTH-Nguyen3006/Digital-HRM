package com.company.hrm.module.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserDetailResponse(
        UUID userId,
        Long employeeId,
        String username,
        String email,
        String phoneNumber,
        String status,
        boolean mustChangePassword,
        boolean mfaEnabled,
        LocalDateTime passwordChangedAt,
        LocalDateTime lastLoginAt,
        String lastLoginIp,
        int failedLoginCount,
        LocalDateTime lockedUntil,
        List<UserRoleHistoryResponse> roleHistory
) {
}
