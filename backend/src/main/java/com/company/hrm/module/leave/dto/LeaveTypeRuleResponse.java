package com.company.hrm.module.leave.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LeaveTypeRuleResponse(
        Long leaveTypeRuleId,
        Integer versionNo,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String unitType,
        BigDecimal defaultEntitlementUnits,
        BigDecimal carryForwardMaxUnits,
        boolean paid,
        boolean requiresBalanceCheck,
        boolean requiresAttachment,
        String approvalRoleCode,
        boolean allowNegativeBalance,
        Integer minNoticeDays,
        BigDecimal maxConsecutiveUnits,
        String note
) {
}
