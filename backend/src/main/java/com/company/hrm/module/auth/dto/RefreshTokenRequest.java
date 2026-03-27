package com.company.hrm.module.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken là bắt buộc.")
        String refreshToken
) {
}
