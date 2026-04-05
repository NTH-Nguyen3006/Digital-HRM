package com.company.hrm.module.employee.dto;

import java.time.LocalDateTime;

public record ProfileChangeRequestResponse(
        Long profileChangeRequestId,
        Long employeeId,
        String employeeCode,
        String employeeFullName,
        String requesterUsername,
        String requestType,
        String requestStatus,
        String payloadJson,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String reviewerUsername,
        String reviewNote
) {
}
