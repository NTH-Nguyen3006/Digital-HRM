package com.company.hrm.module.contract.dto;

import com.company.hrm.common.constant.ContractAttachmentType;
import com.company.hrm.common.constant.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ContractAttachmentRequest(
        @NotNull(message = "attachmentType là bắt buộc.")
        ContractAttachmentType attachmentType,

        @NotBlank(message = "fileName là bắt buộc.")
        @Size(max = 255, message = "fileName tối đa 255 ký tự.")
        String fileName,

        @NotBlank(message = "storagePath là bắt buộc.")
        @Size(max = 500, message = "storagePath tối đa 500 ký tự.")
        String storagePath,

        @Size(max = 100, message = "mimeType tối đa 100 ký tự.")
        String mimeType,

        @PositiveOrZero(message = "fileSizeBytes phải >= 0.")
        Long fileSizeBytes,

        DocumentStatus status
) {
}
