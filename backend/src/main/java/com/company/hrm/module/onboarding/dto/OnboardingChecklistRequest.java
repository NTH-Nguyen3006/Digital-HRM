package com.company.hrm.module.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record OnboardingChecklistRequest(
        @NotBlank(message = "itemCode là bắt buộc.")
        @Size(max = 50, message = "itemCode tối đa 50 ký tự.")
        String itemCode,

        @NotBlank(message = "itemName là bắt buộc.")
        @Size(max = 255, message = "itemName tối đa 255 ký tự.")
        String itemName,

        boolean required,
        boolean completed,
        LocalDate dueDate,

        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
