package com.company.hrm.module.systemconfig.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.systemconfig.dto.*;
import com.company.hrm.module.systemconfig.service.SystemConfigService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;

    public AdminSystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/permissions/matrix")
    @PreAuthorize("hasAuthority('permission.matrix.view')")
    public ApiResponse<List<PermissionMatrixRowResponse>> getPermissionMatrix() {
        return ApiResponse.success("PERMISSION_MATRIX_SUCCESS", "Lấy ma trận permission thành công.", systemConfigService.getPermissionMatrix(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/roles/{roleId}/menu-configs")
    @PreAuthorize("hasAuthority('role.menu.manage')")
    public ApiResponse<List<RoleMenuConfigResponse>> listRoleMenuConfigs(@PathVariable UUID roleId) {
        return ApiResponse.success("ROLE_MENU_CONFIG_LIST_SUCCESS", "Lấy cấu hình menu theo role thành công.", systemConfigService.listRoleMenuConfigs(roleId), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/roles/{roleId}/menu-configs")
    @PreAuthorize("hasAuthority('role.menu.manage')")
    public ApiResponse<List<RoleMenuConfigResponse>> replaceRoleMenuConfigs(@PathVariable UUID roleId, @Valid @RequestBody List<@Valid RoleMenuConfigRequest> requests) {
        return ApiResponse.success("ROLE_MENU_CONFIG_REPLACE_SUCCESS", "Cập nhật cấu hình menu theo role thành công.", systemConfigService.replaceRoleMenuConfigs(roleId, requests), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/notification-templates")
    @PreAuthorize("hasAuthority('notification.template.view')")
    public ApiResponse<List<NotificationTemplateResponse>> listNotificationTemplates() {
        return ApiResponse.success("NOTIFICATION_TEMPLATE_LIST_SUCCESS", "Lấy danh sách mẫu thông báo thành công.", systemConfigService.listNotificationTemplates(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/notification-templates")
    @PreAuthorize("hasAuthority('notification.template.manage')")
    public ApiResponse<NotificationTemplateResponse> createNotificationTemplate(@Valid @RequestBody NotificationTemplateRequest request) {
        return ApiResponse.success("NOTIFICATION_TEMPLATE_CREATE_SUCCESS", "Tạo mẫu thông báo thành công.", systemConfigService.createNotificationTemplate(request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/notification-templates/{templateId}")
    @PreAuthorize("hasAuthority('notification.template.manage')")
    public ApiResponse<NotificationTemplateResponse> updateNotificationTemplate(@PathVariable Long templateId, @Valid @RequestBody NotificationTemplateRequest request) {
        return ApiResponse.success("NOTIFICATION_TEMPLATE_UPDATE_SUCCESS", "Cập nhật mẫu thông báo thành công.", systemConfigService.updateNotificationTemplate(templateId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/platform-settings")
    @PreAuthorize("hasAuthority('platform.setting.view')")
    public ApiResponse<List<PlatformSettingResponse>> listPlatformSettings() {
        return ApiResponse.success("PLATFORM_SETTING_LIST_SUCCESS", "Lấy danh sách tham số nền tảng thành công.", systemConfigService.listPlatformSettings(), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/platform-settings/{settingId}")
    @PreAuthorize("hasAuthority('platform.setting.manage')")
    public ApiResponse<PlatformSettingResponse> updatePlatformSetting(@PathVariable Long settingId, @Valid @RequestBody PlatformSettingRequest request) {
        return ApiResponse.success("PLATFORM_SETTING_UPDATE_SUCCESS", "Cập nhật tham số nền tảng thành công.", systemConfigService.updatePlatformSetting(settingId, request), null, RequestTraceContext.getTraceId());
    }
}
