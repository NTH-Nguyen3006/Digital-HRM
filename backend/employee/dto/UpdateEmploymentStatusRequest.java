package com.company.hrm.module.employee.dto;

import com.company.hrm.common.constant.EmploymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateEmploymentStatusRequest(
        @NotNull(message = "employmentStatus không được để trống.")
        EmploymentStatus employmentStatus,
        String note
) {
}
