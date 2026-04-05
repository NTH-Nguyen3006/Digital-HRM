package com.company.hrm.module.employee.dto;

import com.company.hrm.common.constant.DocumentCategory;
import com.company.hrm.common.constant.DocumentStatus;
import jakarta.validation.constraints.*;

public record EmployeeDocumentRequest(
        @NotNull(message = "documentCategory không được để trống.")
        DocumentCategory documentCategory,
        @NotBlank(message = "documentName không được để trống.")
        @Size(max = 255, message = "documentName tối đa 255 ký tự.")
        String documentName,
        @NotBlank(message = "storagePath không được để trống.")
        @Size(max = 500, message = "storagePath tối đa 500 ký tự.")
        String storagePath,
        @Size(max = 100, message = "mimeType tối đa 100 ký tự.")
        String mimeType,
        Long fileSizeBytes,
        @NotNull(message = "status không được để trống.")
        DocumentStatus status,
        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
