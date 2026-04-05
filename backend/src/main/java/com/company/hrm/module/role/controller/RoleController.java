package com.company.hrm.module.role.controller;

import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.role.dto.CreateRoleRequest;
import com.company.hrm.module.role.dto.RoleDetailResponse;
import com.company.hrm.module.role.dto.RoleListItemResponse;
import com.company.hrm.module.role.dto.UpdateRoleRequest;
import com.company.hrm.module.role.dto.UpdateRoleStatusRequest;
import com.company.hrm.module.role.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('role.view')")
    public ApiResponse<List<RoleListItemResponse>> listRoles() {
        List<RoleListItemResponse> response = roleService.listRoles();
        return ApiResponse.success("ROLE_LIST_SUCCESS", "Lấy danh sách role thành công.", response, null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role.view')")
    public ApiResponse<RoleDetailResponse> getRoleDetail(@PathVariable UUID roleId) {
        RoleDetailResponse response = roleService.getRoleDetail(roleId);
        return ApiResponse.success("ROLE_DETAIL_SUCCESS", "Lấy chi tiết role thành công.", response, null, RequestTraceContext.getTraceId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role.create')")
    public ApiResponse<RoleDetailResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleDetailResponse response = roleService.createRole(request);
        return ApiResponse.success("ROLE_CREATE_SUCCESS", "Tạo role thành công.", response, null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role.update')")
    public ApiResponse<RoleDetailResponse> updateRole(@PathVariable UUID roleId, @Valid @RequestBody UpdateRoleRequest request) {
        RoleDetailResponse response = roleService.updateRole(roleId, request);
        return ApiResponse.success("ROLE_UPDATE_SUCCESS", "Cập nhật role thành công.", response, null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{roleId}/status")
    @PreAuthorize("hasAuthority('role.change_status')")
    public ApiResponse<RoleDetailResponse> changeStatus(@PathVariable UUID roleId, @Valid @RequestBody UpdateRoleStatusRequest request) {
        RoleDetailResponse response = roleService.changeStatus(roleId, request);
        return ApiResponse.success("ROLE_STATUS_SUCCESS", "Cập nhật trạng thái role thành công.", response, null, RequestTraceContext.getTraceId());
    }
}
