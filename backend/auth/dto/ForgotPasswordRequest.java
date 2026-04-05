package com.company.hrm.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank(message = "email là bắt buộc.")
        @Email(message = "email không hợp lệ.")
        @Size(max = 150, message = "email không được vượt quá 150 ký tự.")
        String email
) {
}
