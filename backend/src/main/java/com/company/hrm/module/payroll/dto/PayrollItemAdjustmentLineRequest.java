package com.company.hrm.module.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PayrollItemAdjustmentLineRequest(
        @NotBlank(message = "componentCode là bắt buộc.")
        @Size(max = 30, message = "componentCode tối đa 30 ký tự.")
        String componentCode,

        @NotBlank(message = "componentName là bắt buộc.")
        @Size(max = 200, message = "componentName tối đa 200 ký tự.")
        String componentName,

        @NotBlank(message = "componentCategory là bắt buộc.")
        String componentCategory,

        @NotNull(message = "lineAmount là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = true, message = "lineAmount không hợp lệ.")
        BigDecimal lineAmount,

        boolean taxable,

        @NotNull(message = "displayOrder là bắt buộc.")
        Integer displayOrder,

        @Size(max = 500, message = "lineNote tối đa 500 ký tự.")
        String lineNote
) {
}
