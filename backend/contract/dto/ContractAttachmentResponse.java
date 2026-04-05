package com.company.hrm.module.contract.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContractAttachmentResponse(
        Long contractAttachmentId,
        String attachmentType,
        String fileName,
        String storagePath,
        String mimeType,
        Long fileSizeBytes,
        LocalDateTime uploadedAt,
        UUID uploadedByUserId,
        String uploadedByUsername,
        String status
) {
}
