package com.company.hrm.module.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "currentPassword là bắt buộc.")
        String currentPassword,

        @NotBlank(message = "newPassword là bắt buộc.")
        String newPassword,

        @NotBlank(message = "confirmPassword là bắt buộc.")
        String confirmPassword
) {
}
