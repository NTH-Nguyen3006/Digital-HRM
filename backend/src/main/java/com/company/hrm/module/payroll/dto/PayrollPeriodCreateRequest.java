package com.company.hrm.module.payroll.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PayrollPeriodCreateRequest(
        @NotNull(message = "periodYear là bắt buộc.")
        @Min(value = 2000, message = "periodYear không hợp lệ.")
        Integer periodYear,

        @NotNull(message = "periodMonth là bắt buộc.")
        @Min(value = 1, message = "periodMonth không hợp lệ.")
        @Max(value = 12, message = "periodMonth không hợp lệ.")
        Integer periodMonth,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
