package com.company.hrm.module.leave.controller;

import com.company.hrm.common.constant.LeaveRequestStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.leave.dto.*;
import com.company.hrm.module.leave.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminLeaveController {

    private final LeaveService leaveService;

    public AdminLeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping("/leave-balances")
    @PreAuthorize("hasAuthority('leave.type.view')")
    public ApiResponse<PageResponse<LeaveBalanceListItemResponse>> listBalances(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) Integer leaveYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("LEAVE_BALANCE_LIST_SUCCESS", "Lấy danh sách số dư phép thành công.", leaveService.listBalances(keyword, leaveTypeId, leaveYear, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/leave-balances/{leaveBalanceId}")
    @PreAuthorize("hasAuthority('leave.type.view')")
    public ApiResponse<LeaveBalanceResponse> getBalanceDetail(@PathVariable Long leaveBalanceId) {
        return ApiResponse.success("LEAVE_BALANCE_DETAIL_SUCCESS", "Lấy chi tiết số dư phép thành công.", leaveService.getBalanceDetail(leaveBalanceId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/leave-balances/adjustments")
    @PreAuthorize("hasAuthority('leave.balance.adjust')")
    public ApiResponse<LeaveBalanceResponse> adjustBalance(@Valid @RequestBody LeaveBalanceAdjustmentRequest request) {
        return ApiResponse.success("LEAVE_BALANCE_ADJUST_SUCCESS", "Điều chỉnh số dư phép thành công.", leaveService.adjustBalance(request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/leave-requests")
    @PreAuthorize("hasAuthority('leave.request.finalize')")
    public ApiResponse<PageResponse<LeaveRequestListItemResponse>> listRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("LEAVE_REQUEST_LIST_SUCCESS", "Lấy danh sách đơn nghỉ thành công.", leaveService.listAdminRequests(keyword, status, leaveTypeId, employeeId, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/leave-requests/{leaveRequestId}")
    @PreAuthorize("hasAuthority('leave.request.finalize')")
    public ApiResponse<LeaveRequestDetailResponse> getRequestDetail(@PathVariable Long leaveRequestId) {
        return ApiResponse.success("LEAVE_REQUEST_DETAIL_SUCCESS", "Lấy chi tiết đơn nghỉ thành công.", leaveService.getRequestDetail(leaveRequestId), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/leave-requests/{leaveRequestId}/finalize")
    @PreAuthorize("hasAuthority('leave.request.finalize')")
    public ApiResponse<LeaveRequestDetailResponse> finalizeRequest(@PathVariable Long leaveRequestId, @Valid @RequestBody FinalizeLeaveRequestRequest request) {
        return ApiResponse.success("LEAVE_REQUEST_FINALIZE_SUCCESS", "Xử lý chốt hiệu lực nghỉ phép thành công.", leaveService.finalizeByHr(leaveRequestId, request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/leave-periods/close")
    @PreAuthorize("hasAuthority('leave.period.close')")
    public ApiResponse<Integer> closePeriod(@Valid @RequestBody LeavePeriodCloseRequest request) {
        return ApiResponse.success("LEAVE_PERIOD_CLOSE_SUCCESS", "Chốt kỳ nghỉ phép thành công.", leaveService.closePeriod(request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/leave-settlements")
    @PreAuthorize("hasAuthority('leave.balance.settlement')")
    public ApiResponse<LeaveSettlementResponse> settleRemainingLeave(@Valid @RequestBody LeaveSettlementRequest request) {
        return ApiResponse.success("LEAVE_SETTLEMENT_SUCCESS", "Quyết toán phép còn lại thành công.", leaveService.settleRemainingLeave(request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping(value = "/leave-reports/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('leave.report.export')")
    public ResponseEntity<String> exportRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) Long employeeId
    ) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leave-requests.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(leaveService.exportRequestsCsv(keyword, status, leaveTypeId, employeeId));
    }
}
