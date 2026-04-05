package com.company.hrm.module.leave.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record LeaveSettlementRequest(
        @NotNull(message = "employeeId là bắt buộc.")
        Long employeeId,

        @NotNull(message = "leaveTypeId là bắt buộc.")
        Long leaveTypeId,

        @NotNull(message = "settlementDate là bắt buộc.")
        LocalDate settlementDate,

        @NotBlank(message = "reason là bắt buộc.")
        @Size(max = 1000, message = "reason tối đa 1000 ký tự.")
        String reason
) {
}
