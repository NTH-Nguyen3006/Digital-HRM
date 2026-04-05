package com.company.hrm.module.attendance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttendancePeriodCloseRequest(
        @NotNull(message = "periodYear là bắt buộc.")
        @Min(value = 2000, message = "periodYear không hợp lệ.")
        Integer periodYear,

        @NotNull(message = "periodMonth là bắt buộc.")
        @Min(value = 1, message = "periodMonth phải từ 1 đến 12.")
        @Max(value = 12, message = "periodMonth phải từ 1 đến 12.")
        Integer periodMonth,

        boolean closeDirectly,

        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
