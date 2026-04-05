package com.company.hrm.module.storage.dto;

import java.nio.file.Path;

public record StoredFileDownload(
        String fileKey,
        String originalFileName,
        String mimeType,
        Long fileSizeBytes,
        Path absolutePath
) {
}
