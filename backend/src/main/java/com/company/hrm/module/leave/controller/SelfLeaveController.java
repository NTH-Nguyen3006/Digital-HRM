package com.company.hrm.module.leave.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.leave.dto.*;
import com.company.hrm.module.leave.service.LeaveService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
public class SelfLeaveController {

    private final LeaveService leaveService;

    public SelfLeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping("/leave-balances")
    @PreAuthorize("hasAuthority('leave.request.create')")
    public ApiResponse<List<LeaveBalanceResponse>> getMyBalances() {
        return ApiResponse.success("LEAVE_ME_BALANCE_SUCCESS", "Lấy số dư phép cá nhân thành công.", leaveService.getMyBalances(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/leave-requests")
    @PreAuthorize("hasAuthority('leave.request.create')")
    public ApiResponse<List<LeaveRequestDetailResponse>> listMyRequests() {
        return ApiResponse.success("LEAVE_ME_LIST_SUCCESS", "Lấy danh sách đơn nghỉ cá nhân thành công.", leaveService.listMyRequests(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/leave-requests")
    @PreAuthorize("hasAuthority('leave.request.create')")
    public ApiResponse<LeaveRequestDetailResponse> create(@Valid @RequestBody CreateLeaveRequestRequest request) {
        return ApiResponse.success("LEAVE_ME_CREATE_SUCCESS", "Tạo đơn nghỉ cá nhân thành công.", leaveService.createMyRequest(request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/leave-requests/{leaveRequestId}")
    @PreAuthorize("hasAuthority('leave.request.update_cancel')")
    public ApiResponse<LeaveRequestDetailResponse> update(@PathVariable Long leaveRequestId, @Valid @RequestBody UpdateLeaveRequestRequest request) {
        return ApiResponse.success("LEAVE_ME_UPDATE_SUCCESS", "Cập nhật đơn nghỉ cá nhân thành công.", leaveService.updateMyRequest(leaveRequestId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/leave-requests/{leaveRequestId}/cancel")
    @PreAuthorize("hasAuthority('leave.request.update_cancel')")
    public ApiResponse<LeaveRequestDetailResponse> cancel(@PathVariable Long leaveRequestId, @Valid @RequestBody CancelLeaveRequestRequest request) {
        return ApiResponse.success("LEAVE_ME_CANCEL_SUCCESS", "Hủy đơn nghỉ cá nhân thành công.", leaveService.cancelMyRequest(leaveRequestId, request), null, RequestTraceContext.getTraceId());
    }
}
