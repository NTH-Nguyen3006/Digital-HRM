package com.company.hrm.common.dto;

import java.util.List;

public record ImportResultResponse(
        int totalRows,
        int createdCount,
        int updatedCount,
        int skippedCount,
        List<String> messages
) {
}
