package com.company.hrm.module.leave.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LeavePeriodCloseRequest(
        @NotNull(message = "sourceYear là bắt buộc.")
        @Min(value = 2000, message = "sourceYear không hợp lệ.")
        Integer sourceYear,

        @NotNull(message = "targetYear là bắt buộc.")
        @Min(value = 2000, message = "targetYear không hợp lệ.")
        Integer targetYear,

        Long leaveTypeId
) {
}
