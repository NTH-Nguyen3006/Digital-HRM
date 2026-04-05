package com.company.hrm.module.onboarding.dto;

import com.company.hrm.common.constant.DocumentCategory;
import com.company.hrm.common.constant.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OnboardingDocumentRequest(
        @NotBlank(message = "documentName là bắt buộc.")
        @Size(max = 255, message = "documentName tối đa 255 ký tự.")
        String documentName,

        @NotNull(message = "documentCategory là bắt buộc.")
        DocumentCategory documentCategory,

        @NotBlank(message = "storagePath là bắt buộc.")
        @Size(max = 500, message = "storagePath tối đa 500 ký tự.")
        String storagePath,

        @Size(max = 100, message = "mimeType tối đa 100 ký tự.")
        String mimeType,

        Long fileSizeBytes,

        @NotNull(message = "status là bắt buộc.")
        DocumentStatus status,

        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
