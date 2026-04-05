package com.company.hrm.module.systemconfig.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoleMenuConfigRequest(
        @NotBlank(message = "menuKey là bắt buộc.")
        @Size(max = 100, message = "menuKey tối đa 100 ký tự.")
        String menuKey,

        @NotBlank(message = "menuName là bắt buộc.")
        @Size(max = 200, message = "menuName tối đa 200 ký tự.")
        String menuName,

        @NotBlank(message = "routePath là bắt buộc.")
        @Size(max = 255, message = "routePath tối đa 255 ký tự.")
        String routePath,

        @Size(max = 100, message = "iconName tối đa 100 ký tự.")
        String iconName,

        @Size(max = 100, message = "parentMenuKey tối đa 100 ký tự.")
        String parentMenuKey,

        Integer sortOrder,
        boolean visible,

        @NotNull(message = "status là bắt buộc.")
        RecordStatus status
) {
}
