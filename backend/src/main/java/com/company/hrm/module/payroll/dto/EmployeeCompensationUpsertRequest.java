package com.company.hrm.module.payroll.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EmployeeCompensationUpsertRequest(
        @NotNull(message = "effectiveFrom là bắt buộc.")
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        @NotNull(message = "baseSalary là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = false, message = "baseSalary phải lớn hơn 0.")
        BigDecimal baseSalary,

        @DecimalMin(value = "0.00", inclusive = true, message = "insuranceSalaryBase không hợp lệ.")
        BigDecimal insuranceSalaryBase,

        @Size(max = 10, message = "salaryCurrency tối đa 10 ký tự.")
        String salaryCurrency,

        @Size(max = 200, message = "bankAccountName tối đa 200 ký tự.")
        String bankAccountName,

        @Size(max = 50, message = "bankAccountNo tối đa 50 ký tự.")
        String bankAccountNo,

        @Size(max = 200, message = "bankName tối đa 200 ký tự.")
        String bankName,

        @Size(max = 500, message = "paymentNote tối đa 500 ký tự.")
        String paymentNote,

        @Valid
        List<EmployeeCompensationItemRequest> items
) {
}
