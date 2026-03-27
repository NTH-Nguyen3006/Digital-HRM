package com.company.hrm.module.role.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateRoleRequest(
        @NotBlank(message = "roleCode là bắt buộc.")
        @Size(max = 30, message = "roleCode không được vượt quá 30 ký tự.")
        String roleCode,

        @NotBlank(message = "roleName là bắt buộc.")
        @Size(max = 100, message = "roleName không được vượt quá 100 ký tự.")
        String roleName,

        @Size(max = 500, message = "description không được vượt quá 500 ký tự.")
        String description,

        Integer sortOrder,

        RecordStatus status,

        @NotEmpty(message = "permissionCodes không được để trống.")
        List<String> permissionCodes,

        List<DataScopeRequest> dataScopes
) {
}
