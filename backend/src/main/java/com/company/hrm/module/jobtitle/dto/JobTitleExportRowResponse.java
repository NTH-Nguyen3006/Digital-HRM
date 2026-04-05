package com.company.hrm.module.jobtitle.dto;

public record JobTitleExportRowResponse(
        String jobTitleCode,
        String jobTitleName,
        String jobLevelCode,
        String description,
        Integer sortOrder,
        String status
) {
}
