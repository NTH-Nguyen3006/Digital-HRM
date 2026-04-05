package com.company.hrm.module.offboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevokeOffboardingAccessRequest(
        @NotBlank(message = "note là bắt buộc.")
        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
