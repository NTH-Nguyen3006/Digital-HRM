package com.company.hrm.module.jobtitle.dto;

public record JobTitleListItemResponse(
        Long jobTitleId,
        String jobTitleCode,
        String jobTitleName,
        String jobLevelCode,
        String description,
        int sortOrder,
        String status
) {
}
