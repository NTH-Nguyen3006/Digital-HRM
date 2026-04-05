package com.company.hrm.module.attendance.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.attendance.dto.*;
import com.company.hrm.module.attendance.service.AttendanceService;
import com.company.hrm.module.audit.support.RequestTraceContext;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manager/attendance")
public class ManagerAttendanceController {

    private final AttendanceService attendanceService;

    public ManagerAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/adjustment-requests/pending")
    @PreAuthorize("hasAuthority('attendance.adjust.approve')")
    public ApiResponse<List<AttendanceAdjustmentListItemResponse>> listPendingAdjustmentRequests() {
        return ApiResponse.success("ATTENDANCE_MANAGER_ADJUSTMENT_PENDING_SUCCESS", "Lấy danh sách yêu cầu điều chỉnh công chờ duyệt thành công.",
                attendanceService.listPendingAdjustmentRequestsForManager(), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/adjustment-requests/{adjustmentRequestId}/review")
    @PreAuthorize("hasAuthority('attendance.adjust.approve')")
    public ApiResponse<AttendanceAdjustmentDetailResponse> reviewAdjustmentRequest(
            @PathVariable Long adjustmentRequestId,
            @Valid @RequestBody ReviewAttendanceAdjustmentRequest request
    ) {
        return ApiResponse.success("ATTENDANCE_MANAGER_ADJUSTMENT_REVIEW_SUCCESS", "Duyệt yêu cầu điều chỉnh công thành công.",
                attendanceService.reviewAdjustmentByManager(adjustmentRequestId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/overtime-requests/pending")
    @PreAuthorize("hasAuthority('attendance.ot.approve')")
    public ApiResponse<List<OvertimeListItemResponse>> listPendingOvertimeRequests() {
        return ApiResponse.success("ATTENDANCE_MANAGER_OT_PENDING_SUCCESS", "Lấy danh sách yêu cầu OT chờ duyệt thành công.",
                attendanceService.listPendingOvertimeRequestsForManager(), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/overtime-requests/{overtimeRequestId}/review")
    @PreAuthorize("hasAuthority('attendance.ot.approve')")
    public ApiResponse<OvertimeListItemResponse> reviewOvertimeRequest(
            @PathVariable Long overtimeRequestId,
            @Valid @RequestBody ReviewOvertimeRequest request
    ) {
        return ApiResponse.success("ATTENDANCE_MANAGER_OT_REVIEW_SUCCESS", "Duyệt yêu cầu OT thành công.",
                attendanceService.reviewOvertimeByManager(overtimeRequestId, request), null, RequestTraceContext.getTraceId());
    }
}
