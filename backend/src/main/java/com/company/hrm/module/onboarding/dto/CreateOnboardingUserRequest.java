package com.company.hrm.module.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOnboardingUserRequest(
        @NotBlank(message = "username là bắt buộc.")
        @Size(max = 50, message = "username tối đa 50 ký tự.")
        String username,

        @Size(max = 255, message = "initialPassword tối đa 255 ký tự.")
        String initialPassword,

        boolean sendSetupEmail
) {
}
