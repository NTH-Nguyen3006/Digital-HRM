package com.company.hrm.module.storage.dto;

public record StoredFileReference(
        String fileKey,
        String mimeType,
        Long fileSizeBytes
) {
}
