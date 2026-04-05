package com.company.hrm.module.offboarding.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateOffboardingRequest(
        @FutureOrPresent(message = "requestedLastWorkingDate phải từ hôm nay trở đi.")
        LocalDate requestedLastWorkingDate,

        @NotBlank(message = "requestReason là bắt buộc.")
        @Size(max = 1000, message = "requestReason tối đa 1000 ký tự.")
        String requestReason
) {
}
