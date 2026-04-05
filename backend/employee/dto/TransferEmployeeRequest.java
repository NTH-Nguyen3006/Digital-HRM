package com.company.hrm.module.employee.dto;

import jakarta.validation.constraints.NotNull;

public record TransferEmployeeRequest(
        @NotNull(message = "targetOrgUnitId không được để trống.")
        Long targetOrgUnitId,
        Long targetManagerEmployeeId,
        String note
) {
}
