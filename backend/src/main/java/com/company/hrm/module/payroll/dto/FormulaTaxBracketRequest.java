package com.company.hrm.module.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FormulaTaxBracketRequest(
        @NotNull(message = "sequenceNo là bắt buộc.")
        Integer sequenceNo,

        @NotNull(message = "incomeFrom là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = true, message = "incomeFrom không hợp lệ.")
        BigDecimal incomeFrom,

        @DecimalMin(value = "0.00", inclusive = true, message = "incomeTo không hợp lệ.")
        BigDecimal incomeTo,

        @NotNull(message = "taxRate là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = false, message = "taxRate phải lớn hơn 0.")
        BigDecimal taxRate,

        @NotNull(message = "quickDeduction là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = true, message = "quickDeduction không hợp lệ.")
        BigDecimal quickDeduction
) {
}
