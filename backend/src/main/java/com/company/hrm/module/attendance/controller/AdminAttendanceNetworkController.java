package com.company.hrm.module.attendance.controller;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.attendance.dto.*;
import com.company.hrm.module.attendance.service.AttendanceNetworkPolicyService;
import com.company.hrm.module.audit.support.RequestTraceContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/attendance/network-policies")
public class AdminAttendanceNetworkController {

    private final AttendanceNetworkPolicyService attendanceNetworkPolicyService;

    public AdminAttendanceNetworkController(AttendanceNetworkPolicyService attendanceNetworkPolicyService) {
        this.attendanceNetworkPolicyService = attendanceNetworkPolicyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('attendance.network_policy.view')")
    public ApiResponse<PageResponse<AttendanceNetworkPolicyListItemResponse>> listPolicies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                "ATTENDANCE_NETWORK_POLICY_LIST_SUCCESS",
                "Lấy danh sách policy IP chấm công thành công.",
                attendanceNetworkPolicyService.listPolicies(keyword, status, orgUnitId, page, size),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @GetMapping("/{networkPolicyId}")
    @PreAuthorize("hasAuthority('attendance.network_policy.view')")
    public ApiResponse<AttendanceNetworkPolicyDetailResponse> getPolicyDetail(@PathVariable Long networkPolicyId) {
        return ApiResponse.success(
                "ATTENDANCE_NETWORK_POLICY_DETAIL_SUCCESS",
                "Lấy chi tiết policy IP chấm công thành công.",
                attendanceNetworkPolicyService.getPolicyDetail(networkPolicyId),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('attendance.network_policy.manage')")
    public ApiResponse<AttendanceNetworkPolicyDetailResponse> createPolicy(@Valid @RequestBody UpsertAttendanceNetworkPolicyRequest request) {
        return ApiResponse.success(
                "ATTENDANCE_NETWORK_POLICY_CREATE_SUCCESS",
                "Tạo mới policy IP chấm công thành công.",
                attendanceNetworkPolicyService.createPolicy(request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PutMapping("/{networkPolicyId}")
    @PreAuthorize("hasAuthority('attendance.network_policy.manage')")
    public ApiResponse<AttendanceNetworkPolicyDetailResponse> updatePolicy(
            @PathVariable Long networkPolicyId,
            @Valid @RequestBody UpsertAttendanceNetworkPolicyRequest request
    ) {
        return ApiResponse.success(
                "ATTENDANCE_NETWORK_POLICY_UPDATE_SUCCESS",
                "Cập nhật policy IP chấm công thành công.",
                attendanceNetworkPolicyService.updatePolicy(networkPolicyId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PatchMapping("/{networkPolicyId}/status")
    @PreAuthorize("hasAuthority('attendance.network_policy.manage')")
    public ApiResponse<AttendanceNetworkPolicyDetailResponse> updatePolicyStatus(
            @PathVariable Long networkPolicyId,
            @Valid @RequestBody UpdateAttendanceNetworkPolicyStatusRequest request
    ) {
        return ApiResponse.success(
                "ATTENDANCE_NETWORK_POLICY_STATUS_SUCCESS",
                "Cập nhật trạng thái policy IP chấm công thành công.",
                attendanceNetworkPolicyService.updatePolicyStatus(networkPolicyId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PostMapping("/{networkPolicyId}/ip-ranges")
    @PreAuthorize("hasAuthority('attendance.network_policy.manage')")
    public ApiResponse<AttendanceNetworkPolicyDetailResponse> addIpRange(
            @PathVariable Long networkPolicyId,
            @Valid @RequestBody UpsertAttendanceNetworkPolicyIpRequest request
    ) {
        return ApiResponse.success(
                "ATTENDANCE_NETWORK_POLICY_IP_CREATE_SUCCESS",
                "Thêm IP hợp lệ cho policy chấm công thành công.",
                attendanceNetworkPolicyService.addIpRange(networkPolicyId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PutMapping("/{networkPolicyId}/ip-ranges/{networkPolicyIpId}")
    @PreAuthorize("hasAuthority('attendance.network_policy.manage')")
    public ApiResponse<AttendanceNetworkPolicyDetailResponse> updateIpRange(
            @PathVariable Long networkPolicyId,
            @PathVariable Long networkPolicyIpId,
            @Valid @RequestBody UpsertAttendanceNetworkPolicyIpRequest request
    ) {
        return ApiResponse.success(
                "ATTENDANCE_NETWORK_POLICY_IP_UPDATE_SUCCESS",
                "Cập nhật IP hợp lệ cho policy chấm công thành công.",
                attendanceNetworkPolicyService.updateIpRange(networkPolicyId, networkPolicyIpId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @DeleteMapping("/{networkPolicyId}/ip-ranges/{networkPolicyIpId}")
    @PreAuthorize("hasAuthority('attendance.network_policy.manage')")
    public ApiResponse<Void> deleteIpRange(
            @PathVariable Long networkPolicyId,
            @PathVariable Long networkPolicyIpId
    ) {
        attendanceNetworkPolicyService.deleteIpRange(networkPolicyId, networkPolicyIpId);
        return ApiResponse.success(
                "ATTENDANCE_NETWORK_POLICY_IP_DELETE_SUCCESS",
                "Xóa IP hợp lệ của policy chấm công thành công.",
                null,
                null,
                RequestTraceContext.getTraceId()
        );
    }
}
