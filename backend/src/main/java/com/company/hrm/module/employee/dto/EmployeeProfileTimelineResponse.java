package com.company.hrm.module.employee.dto;

import java.time.LocalDateTime;

public record EmployeeProfileTimelineResponse(
        Long profileTimelineId,
        Long employeeId,
        String eventType,
        String summary,
        String detailJson,
        String actorUsername,
        LocalDateTime eventAt
) {
}
