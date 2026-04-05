package com.company.hrm.module.payroll.dto;

import jakarta.validation.constraints.Size;

public record GeneratePayrollDraftRequest(
        boolean regenerate,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
