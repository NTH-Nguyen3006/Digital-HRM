package com.company.hrm.module.leave.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record LeaveBalanceAdjustmentRequest(
        @NotNull(message = "employeeId là bắt buộc.")
        Long employeeId,

        @NotNull(message = "leaveTypeId là bắt buộc.")
        Long leaveTypeId,

        @NotNull(message = "leaveYear là bắt buộc.")
        @Min(value = 2000, message = "leaveYear không hợp lệ.")
        Integer leaveYear,

        @NotNull(message = "quantityDelta là bắt buộc.")
        @DecimalMin(value = "-365.00", message = "quantityDelta không hợp lệ.")
        @DecimalMax(value = "365.00", message = "quantityDelta không hợp lệ.")
        BigDecimal quantityDelta,

        boolean openingBalance,

        @NotBlank(message = "reason là bắt buộc.")
        @Size(max = 1000, message = "reason tối đa 1000 ký tự.")
        String reason,

        @Size(max = 50, message = "referenceNo tối đa 50 ký tự.")
        String referenceNo,

        @Size(max = 500, message = "attachmentRef tối đa 500 ký tự.")
        String attachmentRef
) {
}
