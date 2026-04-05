package com.company.hrm.module.attendance.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.attendance.dto.*;
import com.company.hrm.module.attendance.service.AttendanceService;
import com.company.hrm.module.audit.support.RequestTraceContext;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/attendance")
public class SelfAttendanceController {

    private final AttendanceService attendanceService;

    public SelfAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAuthority('attendance.log.create')")
    public ApiResponse<AttendanceLogResponse> checkIn(@Valid @RequestBody CreateAttendanceLogRequest request) {
        return ApiResponse.success("ATTENDANCE_CHECKIN_SUCCESS", "Check-in thành công.",
                attendanceService.createSelfLog(request, com.company.hrm.common.constant.AttendanceLogEventType.CHECK_IN),
                null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAuthority('attendance.log.create')")
    public ApiResponse<AttendanceLogResponse> checkOut(@Valid @RequestBody CreateAttendanceLogRequest request) {
        return ApiResponse.success("ATTENDANCE_CHECKOUT_SUCCESS", "Check-out thành công.",
                attendanceService.createSelfLog(request, com.company.hrm.common.constant.AttendanceLogEventType.CHECK_OUT),
                null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('attendance.log.create')")
    public ApiResponse<List<AttendanceLogResponse>> listMyLogs(
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate
    ) {
        return ApiResponse.success("ATTENDANCE_MY_LOG_LIST_SUCCESS", "Lấy danh sách log chấm công cá nhân thành công.",
                attendanceService.listMyLogs(fromDate, toDate), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/adjustment-requests")
    @PreAuthorize("hasAuthority('attendance.adjust.request')")
    public ApiResponse<List<AttendanceAdjustmentListItemResponse>> listMyAdjustmentRequests() {
        return ApiResponse.success("ATTENDANCE_MY_ADJUSTMENT_LIST_SUCCESS", "Lấy danh sách yêu cầu điều chỉnh công cá nhân thành công.",
                attendanceService.listMyAdjustmentRequests(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/adjustment-requests")
    @PreAuthorize("hasAuthority('attendance.adjust.request')")
    public ApiResponse<AttendanceAdjustmentDetailResponse> createAdjustmentRequest(@Valid @RequestBody CreateAttendanceAdjustmentRequest request) {
        return ApiResponse.success("ATTENDANCE_ADJUSTMENT_CREATE_SUCCESS", "Tạo yêu cầu điều chỉnh công thành công.",
                attendanceService.createAdjustmentRequest(request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/adjustment-requests/{adjustmentRequestId}")
    @PreAuthorize("hasAuthority('attendance.adjust.update_cancel')")
    public ApiResponse<AttendanceAdjustmentDetailResponse> updateAdjustmentRequest(
            @PathVariable Long adjustmentRequestId,
            @Valid @RequestBody UpdateAttendanceAdjustmentRequest request
    ) {
        return ApiResponse.success("ATTENDANCE_ADJUSTMENT_UPDATE_SUCCESS", "Cập nhật yêu cầu điều chỉnh công thành công.",
                attendanceService.updateAdjustmentRequest(adjustmentRequestId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/adjustment-requests/{adjustmentRequestId}/cancel")
    @PreAuthorize("hasAuthority('attendance.adjust.update_cancel')")
    public ApiResponse<AttendanceAdjustmentDetailResponse> cancelAdjustmentRequest(
            @PathVariable Long adjustmentRequestId,
            @Valid @RequestBody CancelAttendanceAdjustmentRequest request
    ) {
        return ApiResponse.success("ATTENDANCE_ADJUSTMENT_CANCEL_SUCCESS", "Hủy yêu cầu điều chỉnh công thành công.",
                attendanceService.cancelAdjustmentRequest(adjustmentRequestId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/overtime-requests")
    @PreAuthorize("hasAuthority('attendance.ot.request')")
    public ApiResponse<List<OvertimeListItemResponse>> listMyOvertimeRequests() {
        return ApiResponse.success("ATTENDANCE_MY_OT_LIST_SUCCESS", "Lấy danh sách yêu cầu OT cá nhân thành công.",
                attendanceService.listMyOvertimeRequests(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/overtime-requests")
    @PreAuthorize("hasAuthority('attendance.ot.request')")
    public ApiResponse<OvertimeListItemResponse> createOvertimeRequest(@Valid @RequestBody CreateOvertimeRequest request) {
        return ApiResponse.success("ATTENDANCE_OT_CREATE_SUCCESS", "Tạo yêu cầu OT thành công.",
                attendanceService.createOvertimeRequest(request), null, RequestTraceContext.getTraceId());
    }
}
