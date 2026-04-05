package com.company.hrm.module.employee.dto;

import jakarta.validation.constraints.Size;

public record RestoreEmployeeProfileRequest(
        @Size(max = 500, message = "reason tối đa 500 ký tự.")
        String reason
) {
}
