package com.company.hrm.module.user.controller;

import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.constant.UserStatus;
import com.company.hrm.module.user.dto.AssignPrimaryRoleRequest;
import com.company.hrm.module.user.dto.CreateUserRequest;
import com.company.hrm.module.user.dto.LockUnlockUserRequest;
import com.company.hrm.module.user.dto.UpdateUserRequest;
import com.company.hrm.module.user.dto.UserDetailResponse;
import com.company.hrm.module.user.dto.UserListItemResponse;
import com.company.hrm.module.user.service.UserAdminService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserAdminService userAdminService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user.view')")
    public ApiResponse<PageResponse<UserListItemResponse>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) RoleCode roleCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<UserListItemResponse> response = userAdminService.listUsers(keyword, status, roleCode, page, size);
        return ApiResponse.success("USER_LIST_SUCCESS", "Lấy danh sách user thành công.", response, null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user.view')")
    public ApiResponse<UserDetailResponse> getUserDetail(@PathVariable UUID userId) {
        UserDetailResponse response = userAdminService.getUserDetail(userId);
        return ApiResponse.success("USER_DETAIL_SUCCESS", "Lấy chi tiết user thành công.", response, null, RequestTraceContext.getTraceId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user.create')")
    public ApiResponse<UserDetailResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDetailResponse response = userAdminService.createUser(request);
        return ApiResponse.success("USER_CREATE_SUCCESS", "Tạo mới user thành công.", response, null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('user.update')")
    public ApiResponse<UserDetailResponse> updateUser(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        UserDetailResponse response = userAdminService.updateUser(userId, request);
        return ApiResponse.success("USER_UPDATE_SUCCESS", "Cập nhật user thành công.", response, null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{userId}/lock-state")
    @PreAuthorize("hasAuthority('user.lock_unlock')")
    public ApiResponse<UserDetailResponse> lockOrUnlockUser(@PathVariable UUID userId, @Valid @RequestBody LockUnlockUserRequest request) {
        UserDetailResponse response = userAdminService.lockOrUnlockUser(userId, request);
        return ApiResponse.success("USER_LOCK_STATE_SUCCESS", "Cập nhật trạng thái khóa user thành công.", response, null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{userId}/primary-role")
    @PreAuthorize("hasAuthority('user.assign_role')")
    public ApiResponse<UserDetailResponse> assignPrimaryRole(@PathVariable UUID userId, @Valid @RequestBody AssignPrimaryRoleRequest request) {
        UserDetailResponse response = userAdminService.assignPrimaryRole(userId, request);
        return ApiResponse.success("USER_ASSIGN_ROLE_SUCCESS", "Gán role chính cho user thành công.", response, null, RequestTraceContext.getTraceId());
    }
}
