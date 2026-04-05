package com.company.hrm.module.systemconfig.dto;

import com.company.hrm.common.constant.NotificationChannel;
import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationTemplateRequest(
        @NotBlank(message = "templateCode là bắt buộc.")
        @Size(max = 50, message = "templateCode tối đa 50 ký tự.")
        String templateCode,

        @NotBlank(message = "templateName là bắt buộc.")
        @Size(max = 200, message = "templateName tối đa 200 ký tự.")
        String templateName,

        @NotNull(message = "channelCode là bắt buộc.")
        NotificationChannel channelCode,

        @Size(max = 255, message = "subjectTemplate tối đa 255 ký tự.")
        String subjectTemplate,

        @NotBlank(message = "bodyTemplate là bắt buộc.")
        String bodyTemplate,

        @NotNull(message = "status là bắt buộc.")
        RecordStatus status,

        @Size(max = 500, message = "description tối đa 500 ký tự.")
        String description
) {
}
