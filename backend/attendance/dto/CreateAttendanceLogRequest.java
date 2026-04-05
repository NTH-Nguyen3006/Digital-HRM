package com.company.hrm.module.attendance.dto;

import com.company.hrm.common.constant.AttendanceLogSourceType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateAttendanceLogRequest(
        @NotNull(message = "sourceType là bắt buộc.")
        AttendanceLogSourceType sourceType,

        LocalDate attendanceDate,

        @Digits(integer = 3, fraction = 7, message = "latitude không hợp lệ.")
        BigDecimal latitude,

        @Digits(integer = 3, fraction = 7, message = "longitude không hợp lệ.")
        BigDecimal longitude,

        @Size(max = 100, message = "deviceRef tối đa 100 ký tự.")
        String deviceRef,

        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
