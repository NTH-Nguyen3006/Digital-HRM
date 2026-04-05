package com.company.hrm.module.reporting.dto;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.ReportCode;
import com.company.hrm.common.constant.ReportScheduleFrequency;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReportScheduleConfigUpsertRequest(
        @NotBlank(message = "scheduleCode là bắt buộc.")
        @Size(max = 50, message = "scheduleCode tối đa 50 ký tự.")
        String scheduleCode,

        @NotBlank(message = "scheduleName là bắt buộc.")
        @Size(max = 200, message = "scheduleName tối đa 200 ký tự.")
        String scheduleName,

        @NotNull(message = "reportCode là bắt buộc.")
        ReportCode reportCode,

        @NotNull(message = "frequencyCode là bắt buộc.")
        ReportScheduleFrequency frequencyCode,

        @Min(value = 1, message = "dayOfWeek phải từ 1 đến 7.")
        @Max(value = 7, message = "dayOfWeek phải từ 1 đến 7.")
        Integer dayOfWeek,

        @Min(value = 1, message = "dayOfMonth phải từ 1 đến 31.")
        @Max(value = 31, message = "dayOfMonth phải từ 1 đến 31.")
        Integer dayOfMonth,

        @NotNull(message = "runAtHour là bắt buộc.")
        @Min(value = 0, message = "runAtHour phải từ 0 đến 23.")
        @Max(value = 23, message = "runAtHour phải từ 0 đến 23.")
        Integer runAtHour,

        @NotNull(message = "runAtMinute là bắt buộc.")
        @Min(value = 0, message = "runAtMinute phải từ 0 đến 59.")
        @Max(value = 59, message = "runAtMinute phải từ 0 đến 59.")
        Integer runAtMinute,

        @NotEmpty(message = "recipientEmails là bắt buộc.")
        List<@Email(message = "Email nhận báo cáo không hợp lệ.") String> recipientEmails,

        String parameterJson,

        @NotNull(message = "status là bắt buộc.")
        RecordStatus status,

        @Size(max = 500, message = "description tối đa 500 ký tự.")
        String description
) {
}
