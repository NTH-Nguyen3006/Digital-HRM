package com.company.hrm.module.payroll.dto;

import com.company.hrm.common.constant.PayrollAmountType;
import com.company.hrm.common.constant.PayrollComponentCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalaryComponentUpsertRequest(
        @NotBlank(message = "componentCode là bắt buộc.")
        @Size(max = 30, message = "componentCode tối đa 30 ký tự.")
        String componentCode,

        @NotBlank(message = "componentName là bắt buộc.")
        @Size(max = 200, message = "componentName tối đa 200 ký tự.")
        String componentName,

        @NotNull(message = "componentCategory là bắt buộc.")
        PayrollComponentCategory componentCategory,

        @NotNull(message = "amountType là bắt buộc.")
        PayrollAmountType amountType,

        boolean taxable,
        boolean insuranceBaseIncluded,
        boolean payslipVisible,

        @NotNull(message = "displayOrder là bắt buộc.")
        @Max(value = 9999, message = "displayOrder tối đa 9999.")
        Integer displayOrder,

        @Size(max = 500, message = "description tối đa 500 ký tự.")
        String description
) {
}
