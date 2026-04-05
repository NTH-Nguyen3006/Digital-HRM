package com.company.hrm.module.leave.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateLeaveRequestRequest(
        @NotNull(message = "leaveTypeId là bắt buộc.")
        Long leaveTypeId,

        @NotNull(message = "startDate là bắt buộc.")
        LocalDate startDate,

        @NotNull(message = "endDate là bắt buộc.")
        LocalDate endDate,

        @DecimalMin(value = "0.01", message = "requestedUnits phải lớn hơn 0 nếu có.")
        BigDecimal requestedUnits,

        @NotBlank(message = "reason là bắt buộc.")
        @Size(max = 1000, message = "reason tối đa 1000 ký tự.")
        String reason,

        @Size(max = 500, message = "attachmentRef tối đa 500 ký tự.")
        String attachmentRef,

        boolean submit
) {
}
