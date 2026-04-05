package com.company.hrm.module.offboarding.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OffboardingDetailResponse(
        Long offboardingCaseId,
        String offboardingCode,
        Long employeeId,
        String employeeCode,
        String employeeFullName,
        String employmentStatus,
        Long orgUnitId,
        String orgUnitName,
        Long managerEmployeeId,
        String managerEmployeeName,
        String status,
        String requestReason,
        LocalDate requestDate,
        LocalDate requestedLastWorkingDate,
        String managerReviewNote,
        LocalDateTime managerReviewedAt,
        LocalDate effectiveLastWorkingDate,
        String hrFinalizeNote,
        LocalDateTime hrFinalizedAt,
        String accessRevokeNote,
        LocalDateTime accessRevokedAt,
        Integer finalAttendanceYear,
        Integer finalAttendanceMonth,
        BigDecimal leaveSettlementUnits,
        BigDecimal leaveSettlementAmount,
        Long finalPayrollPeriodId,
        Long finalPayrollItemId,
        String settlementNote,
        LocalDateTime settlementPreparedAt,
        String closeNote,
        LocalDateTime closedAt,
        List<OffboardingChecklistItemResponse> checklistItems,
        List<OffboardingAssetReturnResponse> assetReturns,
        List<OffboardingHistoryResponse> histories
) {
}
