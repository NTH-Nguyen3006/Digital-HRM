package com.company.hrm.module.auth.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UUID userId,
        String username,
        String email,
        String roleCode,
        List<String> permissions,
        boolean mustChangePassword,
        String homeRoute
) {
}
