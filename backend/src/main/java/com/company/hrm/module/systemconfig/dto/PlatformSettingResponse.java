package com.company.hrm.module.systemconfig.dto;

public record PlatformSettingResponse(
        Long platformSettingId,
        String settingKey,
        String settingName,
        String settingValue,
        String valueType,
        boolean editable,
        String status,
        String description
) {
}
