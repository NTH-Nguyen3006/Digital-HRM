package com.company.hrm.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "loginId là bắt buộc.")
        @Size(max = 150, message = "loginId không được vượt quá 150 ký tự.")
        String loginId,

        @NotBlank(message = "password là bắt buộc.")
        String password,

        @Size(max = 150, message = "deviceName không được vượt quá 150 ký tự.")
        String deviceName,

        @Size(max = 100, message = "deviceOs không được vượt quá 100 ký tự.")
        String deviceOs,

        @Size(max = 100, message = "browserName không được vượt quá 100 ký tự.")
        String browserName
) {
}
