package com.company.hrm.module.employee.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeDocumentResponse(
        Long employeeDocumentId,
        Long employeeId,
        String documentCategory,
        String documentName,
        String storagePath,
        String mimeType,
        Long fileSizeBytes,
        LocalDateTime uploadedAt,
        UUID uploadedBy,
        String status,
        String note
) {
}
