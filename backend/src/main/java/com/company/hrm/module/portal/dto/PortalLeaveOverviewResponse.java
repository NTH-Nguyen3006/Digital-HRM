package com.company.hrm.module.portal.dto;

import com.company.hrm.module.leave.dto.LeaveBalanceResponse;
import com.company.hrm.module.leave.dto.LeaveRequestDetailResponse;
import java.util.List;

public record PortalLeaveOverviewResponse(
        List<LeaveBalanceResponse> balances,
        List<LeaveRequestDetailResponse> recentRequests
) {
}
