package com.company.hrm.module.payroll.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdjustPayrollItemRequest(
        @Valid
        List<PayrollItemAdjustmentLineRequest> manualLines,

        @NotBlank(message = "adjustmentNote là bắt buộc.")
        @Size(max = 1000, message = "adjustmentNote tối đa 1000 ký tự.")
        String adjustmentNote
) {
}
