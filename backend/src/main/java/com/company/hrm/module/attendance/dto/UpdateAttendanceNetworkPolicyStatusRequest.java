package com.company.hrm.module.attendance.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAttendanceNetworkPolicyStatusRequest(
        @NotNull(message = "status là bắt buộc.")
        RecordStatus status
) {
}
