package com.company.hrm.module.user.dto;

import com.company.hrm.common.constant.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        Long employeeId,

        @NotBlank(message = "username là bắt buộc.")
        @Size(max = 50, message = "username không được vượt quá 50 ký tự.")
        String username,

        @Email(message = "email không hợp lệ.")
        @Size(max = 150, message = "email không được vượt quá 150 ký tự.")
        String email,

        @Size(max = 20, message = "phoneNumber không được vượt quá 20 ký tự.")
        String phoneNumber,

        UserStatus status,

        boolean mustChangePassword
) {
}
