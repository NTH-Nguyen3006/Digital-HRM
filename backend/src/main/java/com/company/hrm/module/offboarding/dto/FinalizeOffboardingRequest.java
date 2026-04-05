package com.company.hrm.module.offboarding.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record FinalizeOffboardingRequest(
        @NotNull(message = "effectiveLastWorkingDate là bắt buộc.")
        @FutureOrPresent(message = "effectiveLastWorkingDate phải từ hôm nay trở đi.")
        LocalDate effectiveLastWorkingDate,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
