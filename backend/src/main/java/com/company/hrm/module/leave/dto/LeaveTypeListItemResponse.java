package com.company.hrm.module.leave.dto;

public record LeaveTypeListItemResponse(
        Long leaveTypeId,
        String leaveTypeCode,
        String leaveTypeName,
        String status,
        String description,
        LeaveTypeRuleResponse currentRule
) {
}
