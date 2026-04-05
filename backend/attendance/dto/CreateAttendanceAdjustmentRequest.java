package com.company.hrm.module.attendance.dto;

import com.company.hrm.common.constant.AttendanceAdjustmentIssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateAttendanceAdjustmentRequest(
        Long copyFromAdjustmentRequestId,

        @NotNull(message = "attendanceDate là bắt buộc.")
        LocalDate attendanceDate,

        @NotNull(message = "issueType là bắt buộc.")
        AttendanceAdjustmentIssueType issueType,

        LocalDateTime proposedCheckInAt,
        LocalDateTime proposedCheckOutAt,

        @NotBlank(message = "reason là bắt buộc.")
        @Size(max = 1000, message = "reason tối đa 1000 ký tự.")
        String reason,

        @Size(max = 120, message = "evidenceFileKey tối đa 120 ký tự.")
        String evidenceFileKey,

        boolean submit
) {
}
