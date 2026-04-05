package com.company.hrm.module.leave.dto;

import java.util.List;

public record LeaveTypeDetailResponse(
        Long leaveTypeId,
        String leaveTypeCode,
        String leaveTypeName,
        String status,
        String description,
        LeaveTypeRuleResponse currentRule,
        List<LeaveTypeRuleResponse> ruleHistory
) {
}
