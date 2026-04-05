package com.company.hrm.module.leave.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record DeactivateLeaveTypeRequest(
        @NotNull(message = "effectiveTo là bắt buộc.")
        LocalDate effectiveTo,

        @Size(max = 500, message = "reason tối đa 500 ký tự.")
        String reason
) {
}
