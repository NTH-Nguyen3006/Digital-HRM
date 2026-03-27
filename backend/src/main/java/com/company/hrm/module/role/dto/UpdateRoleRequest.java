package com.company.hrm.module.role.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateRoleRequest(
        @NotBlank(message = "roleName là bắt buộc.")
        @Size(max = 100, message = "roleName không được vượt quá 100 ký tự.")
        String roleName,

        @Size(max = 500, message = "description không được vượt quá 500 ký tự.")
        String description,

        Integer sortOrder,

        RecordStatus status,

        List<String> permissionCodes,

        List<DataScopeRequest> dataScopes
) {
}
