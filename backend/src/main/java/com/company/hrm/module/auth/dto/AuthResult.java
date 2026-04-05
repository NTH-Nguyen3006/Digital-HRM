package com.company.hrm.module.auth.dto;

public record AuthResult(
        LoginResponse response,
        String accessToken,
        String refreshToken
) {
}
