package com.company.hrm.module.attendance.controller;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.attendance.dto.*;
import com.company.hrm.module.attendance.service.AttendanceService;
import com.company.hrm.module.audit.support.RequestTraceContext;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/attendance")
public class AdminAttendanceShiftController {

    private final AttendanceService attendanceService;

    public AdminAttendanceShiftController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/shifts")
    @PreAuthorize("hasAuthority('attendance.shift.view')")
    public ApiResponse<PageResponse<ShiftListItemResponse>> listShifts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("ATTENDANCE_SHIFT_LIST_SUCCESS", "Lấy danh sách ca làm việc thành công.",
                attendanceService.listShifts(keyword, status, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/shifts/{shiftId}")
    @PreAuthorize("hasAuthority('attendance.shift.view')")
    public ApiResponse<ShiftDetailResponse> getShiftDetail(@PathVariable Long shiftId) {
        return ApiResponse.success("ATTENDANCE_SHIFT_DETAIL_SUCCESS", "Lấy chi tiết ca làm việc thành công.",
                attendanceService.getShiftDetail(shiftId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/shifts")
    @PreAuthorize("hasAuthority('attendance.shift.create_update')")
    public ApiResponse<ShiftDetailResponse> createShift(@Valid @RequestBody UpsertShiftRequest request) {
        return ApiResponse.success("ATTENDANCE_SHIFT_CREATE_SUCCESS", "Tạo ca làm việc thành công.",
                attendanceService.upsertShift(null, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/shifts/{shiftId}")
    @PreAuthorize("hasAuthority('attendance.shift.create_update')")
    public ApiResponse<ShiftDetailResponse> updateShift(@PathVariable Long shiftId, @Valid @RequestBody UpsertShiftRequest request) {
        return ApiResponse.success("ATTENDANCE_SHIFT_UPDATE_SUCCESS", "Cập nhật ca làm việc thành công.",
                attendanceService.upsertShift(shiftId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/shift-assignments")
    @PreAuthorize("hasAuthority('attendance.shift.assign')")
    public ApiResponse<PageResponse<ShiftAssignmentResponse>> listAssignments(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(required = false) Long shiftId,
            @RequestParam(required = false) java.time.LocalDate attendanceDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("ATTENDANCE_ASSIGNMENT_LIST_SUCCESS", "Lấy danh sách phân ca thành công.",
                attendanceService.listAssignments(employeeId, orgUnitId, shiftId, attendanceDate, page, size), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/shift-assignments")
    @PreAuthorize("hasAuthority('attendance.shift.assign')")
    public ApiResponse<java.util.List<ShiftAssignmentResponse>> assignShift(@Valid @RequestBody AssignShiftRequest request) {
        return ApiResponse.success("ATTENDANCE_ASSIGNMENT_CREATE_SUCCESS", "Gán ca cho nhân viên thành công.",
                attendanceService.assignShift(request), null, RequestTraceContext.getTraceId());
    }
}
