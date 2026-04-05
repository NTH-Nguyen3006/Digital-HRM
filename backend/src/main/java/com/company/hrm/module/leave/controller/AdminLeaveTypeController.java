package com.company.hrm.module.leave.controller;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.leave.dto.*;
import com.company.hrm.module.leave.service.LeaveTypeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/leave-types")
public class AdminLeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    public AdminLeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('leave.type.view')")
    public ApiResponse<List<LeaveTypeListItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RecordStatus status
    ) {
        return ApiResponse.success("LEAVE_TYPE_LIST_SUCCESS", "Lấy danh sách loại nghỉ thành công.", leaveTypeService.list(keyword, status), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/{leaveTypeId}")
    @PreAuthorize("hasAuthority('leave.type.view')")
    public ApiResponse<LeaveTypeDetailResponse> getDetail(@PathVariable Long leaveTypeId) {
        return ApiResponse.success("LEAVE_TYPE_DETAIL_SUCCESS", "Lấy chi tiết loại nghỉ thành công.", leaveTypeService.getDetail(leaveTypeId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('leave.type.create_update')")
    public ApiResponse<LeaveTypeDetailResponse> create(@Valid @RequestBody LeaveTypeUpsertRequest request) {
        return ApiResponse.success("LEAVE_TYPE_CREATE_SUCCESS", "Tạo loại nghỉ thành công.", leaveTypeService.create(request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{leaveTypeId}")
    @PreAuthorize("hasAuthority('leave.type.create_update')")
    public ApiResponse<LeaveTypeDetailResponse> update(@PathVariable Long leaveTypeId, @Valid @RequestBody LeaveTypeUpsertRequest request) {
        return ApiResponse.success("LEAVE_TYPE_UPDATE_SUCCESS", "Cập nhật loại nghỉ thành công.", leaveTypeService.update(leaveTypeId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{leaveTypeId}/deactivate")
    @PreAuthorize("hasAuthority('leave.type.deactivate')")
    public ApiResponse<LeaveTypeDetailResponse> deactivate(@PathVariable Long leaveTypeId, @Valid @RequestBody DeactivateLeaveTypeRequest request) {
        return ApiResponse.success("LEAVE_TYPE_DEACTIVATE_SUCCESS", "Ngừng sử dụng loại nghỉ thành công.", leaveTypeService.deactivate(leaveTypeId, request), null, RequestTraceContext.getTraceId());
    }
}
