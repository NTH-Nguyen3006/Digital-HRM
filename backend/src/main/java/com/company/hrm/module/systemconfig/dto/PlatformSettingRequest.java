package com.company.hrm.module.systemconfig.dto;

import com.company.hrm.common.constant.PlatformSettingValueType;
import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlatformSettingRequest(
        @NotBlank(message = "settingName là bắt buộc.")
        @Size(max = 200, message = "settingName tối đa 200 ký tự.")
        String settingName,

        String settingValue,

        @NotNull(message = "valueType là bắt buộc.")
        PlatformSettingValueType valueType,

        boolean editable,

        @NotNull(message = "status là bắt buộc.")
        RecordStatus status,

        @Size(max = 500, message = "description tối đa 500 ký tự.")
        String description
) {
}
