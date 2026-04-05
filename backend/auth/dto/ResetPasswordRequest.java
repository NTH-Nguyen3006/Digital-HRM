package com.company.hrm.module.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "token là bắt buộc.")
        String token,

        @NotBlank(message = "newPassword là bắt buộc.")
        String newPassword,

        @NotBlank(message = "confirmPassword là bắt buộc.")
        String confirmPassword
) {
}
