package com.company.hrm.module.leave.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.leave.dto.LeaveRequestDetailResponse;
import com.company.hrm.module.leave.dto.LeaveRequestListItemResponse;
import com.company.hrm.module.leave.dto.ReviewLeaveRequestRequest;
import com.company.hrm.module.leave.service.LeaveService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manager")
public class ManagerLeaveController {

    private final LeaveService leaveService;

    public ManagerLeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping("/leave-requests/pending")
    @PreAuthorize("hasAuthority('leave.request.approve')")
    public ApiResponse<List<LeaveRequestListItemResponse>> listPending() {
        return ApiResponse.success("LEAVE_MANAGER_PENDING_SUCCESS", "Lấy danh sách đơn nghỉ chờ duyệt thành công.", leaveService.listPendingForManager(), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/leave-requests/{leaveRequestId}/review")
    @PreAuthorize("hasAuthority('leave.request.approve')")
    public ApiResponse<LeaveRequestDetailResponse> review(@PathVariable Long leaveRequestId, @Valid @RequestBody ReviewLeaveRequestRequest request) {
        return ApiResponse.success("LEAVE_MANAGER_REVIEW_SUCCESS", "Xử lý duyệt đơn nghỉ thành công.", leaveService.reviewByManager(leaveRequestId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/leave-calendar")
    @PreAuthorize("hasAuthority('leave.calendar.view_team')")
    public ApiResponse<List<LeaveRequestListItemResponse>> getTeamCalendar(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
        return ApiResponse.success("LEAVE_TEAM_CALENDAR_SUCCESS", "Lấy lịch nghỉ của team thành công.", leaveService.getTeamCalendar(fromDate, toDate), null, RequestTraceContext.getTraceId());
    }
}
