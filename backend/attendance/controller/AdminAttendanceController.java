package com.company.hrm.module.attendance.controller;

import com.company.hrm.common.constant.AttendanceAdjustmentStatus;
import com.company.hrm.common.constant.AttendanceOvertimeStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.attendance.dto.*;
import com.company.hrm.module.attendance.service.AttendanceService;
import com.company.hrm.module.audit.support.RequestTraceContext;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/attendance")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;

    public AdminAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAuthority('attendance.daily.view')")
    public ApiResponse<PageResponse<DailyAttendanceListItemResponse>> listDailyAttendance(
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate,
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long shiftId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("ATTENDANCE_DAILY_LIST_SUCCESS", "Lấy bảng công ngày thành công.",
                attendanceService.listDailyAttendance(fromDate, toDate, orgUnitId, employeeId, shiftId, page, size),
                null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/daily/detail")
    @PreAuthorize("hasAuthority('attendance.daily.view')")
    public ApiResponse<DailyAttendanceDetailResponse> getDailyAttendanceDetail(
            @RequestParam Long employeeId,
            @RequestParam LocalDate attendanceDate
    ) {
        return ApiResponse.success("ATTENDANCE_DAILY_DETAIL_SUCCESS", "Lấy chi tiết bảng công ngày thành công.",
                attendanceService.getDailyAttendanceDetail(employeeId, attendanceDate),
                null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/adjustment-requests")
    @PreAuthorize("hasAuthority('attendance.adjust.finalize')")
    public ApiResponse<PageResponse<AttendanceAdjustmentListItemResponse>> listAdjustmentRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AttendanceAdjustmentStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("ATTENDANCE_ADJUSTMENT_LIST_SUCCESS", "Lấy danh sách yêu cầu điều chỉnh công thành công.",
                attendanceService.listAdminAdjustmentRequests(keyword, status, employeeId, fromDate, toDate, page, size),
                null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/adjustment-requests/{adjustmentRequestId}")
    @PreAuthorize("hasAuthority('attendance.adjust.finalize')")
    public ApiResponse<AttendanceAdjustmentDetailResponse> getAdjustmentDetail(@PathVariable Long adjustmentRequestId) {
        return ApiResponse.success("ATTENDANCE_ADJUSTMENT_DETAIL_SUCCESS", "Lấy chi tiết yêu cầu điều chỉnh công thành công.",
                attendanceService.getAdjustmentDetail(adjustmentRequestId), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/adjustment-requests/{adjustmentRequestId}/finalize")
    @PreAuthorize("hasAuthority('attendance.adjust.finalize')")
    public ApiResponse<AttendanceAdjustmentDetailResponse> finalizeAdjustment(
            @PathVariable Long adjustmentRequestId,
            @Valid @RequestBody FinalizeAttendanceAdjustmentRequest request
    ) {
        return ApiResponse.success("ATTENDANCE_ADJUSTMENT_FINALIZE_SUCCESS", "Chốt yêu cầu điều chỉnh công thành công.",
                attendanceService.finalizeAdjustment(adjustmentRequestId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/overtime-requests")
    @PreAuthorize("hasAuthority('attendance.daily.view')")
    public ApiResponse<PageResponse<OvertimeListItemResponse>> listOvertimeRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AttendanceOvertimeStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("ATTENDANCE_OT_LIST_SUCCESS", "Lấy danh sách yêu cầu làm thêm giờ thành công.",
                attendanceService.listAdminOvertimeRequests(keyword, status, employeeId, fromDate, toDate, page, size),
                null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/periods")
    @PreAuthorize("hasAuthority('attendance.period.close')")
    public ApiResponse<java.util.List<AttendancePeriodResponse>> listPeriods() {
        return ApiResponse.success("ATTENDANCE_PERIOD_LIST_SUCCESS", "Lấy danh sách kỳ công thành công.",
                attendanceService.listPeriods(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/periods/close")
    @PreAuthorize("hasAuthority('attendance.period.close')")
    public ApiResponse<AttendancePeriodResponse> closePeriod(@Valid @RequestBody AttendancePeriodCloseRequest request) {
        return ApiResponse.success("ATTENDANCE_PERIOD_CLOSE_SUCCESS", "Tính và chốt kỳ công thành công.",
                attendanceService.closePeriod(request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/periods/{attendancePeriodId}/reopen")
    @PreAuthorize("hasAuthority('attendance.period.reopen')")
    public ApiResponse<AttendancePeriodResponse> reopenPeriod(
            @PathVariable Long attendancePeriodId,
            @Valid @RequestBody AttendancePeriodReopenRequest request
    ) {
        return ApiResponse.success("ATTENDANCE_PERIOD_REOPEN_SUCCESS", "Mở lại kỳ công thành công.",
                attendanceService.reopenPeriod(attendancePeriodId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping(value = "/reports/anomalies/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('attendance.anomaly.export')")
    public ResponseEntity<String> exportAnomalyReport(
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate,
            @RequestParam(required = false) Long orgUnitId
    ) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance-anomalies.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(attendanceService.exportAnomalyCsv(fromDate, toDate, orgUnitId));
    }
}
