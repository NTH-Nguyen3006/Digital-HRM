package com.company.hrm.module.storage.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record StoredFileResponse(
        Long storedFileId,
        String fileKey,
        String moduleCode,
        String businessCategory,
        String visibilityScope,
        String originalFileName,
        String storagePath,
        String mimeType,
        Long fileSizeBytes,
        String checksumSha256,
        String note,
        String downloadUrl,
        LocalDateTime uploadedAt,
        UUID uploadedBy
) {
}
