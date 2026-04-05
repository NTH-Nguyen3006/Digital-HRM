package com.company.hrm.module.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EmployeeCompensationItemRequest(
        @NotNull(message = "salaryComponentId là bắt buộc.")
        Long salaryComponentId,

        @DecimalMin(value = "0.00", inclusive = true, message = "amountValue không hợp lệ.")
        BigDecimal amountValue,

        @DecimalMin(value = "0.00", inclusive = true, message = "percentageValue không hợp lệ.")
        BigDecimal percentageValue,

        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
