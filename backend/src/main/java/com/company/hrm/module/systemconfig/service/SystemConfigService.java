package com.company.hrm.module.systemconfig.service;

import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.permission.entity.SecPermission;
import com.company.hrm.module.permission.repository.SecPermissionRepository;
import com.company.hrm.module.role.entity.SecRole;
import com.company.hrm.module.role.repository.SecRolePermissionRepository;
import com.company.hrm.module.role.repository.SecRoleRepository;
import com.company.hrm.module.systemconfig.dto.*;
import com.company.hrm.module.systemconfig.entity.SysNotificationTemplate;
import com.company.hrm.module.systemconfig.entity.SysPlatformSetting;
import com.company.hrm.module.systemconfig.entity.SysRoleMenuConfig;
import com.company.hrm.module.systemconfig.repository.SysNotificationTemplateRepository;
import com.company.hrm.module.systemconfig.repository.SysPlatformSettingRepository;
import com.company.hrm.module.systemconfig.repository.SysRoleMenuConfigRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemConfigService {

    private final SecRoleRepository roleRepository;
    private final SecPermissionRepository permissionRepository;
    private final SecRolePermissionRepository rolePermissionRepository;
    private final SysRoleMenuConfigRepository roleMenuConfigRepository;
    private final SysNotificationTemplateRepository notificationTemplateRepository;
    private final SysPlatformSettingRepository platformSettingRepository;
    private final AuditLogService auditLogService;

    public SystemConfigService(
            SecRoleRepository roleRepository,
            SecPermissionRepository permissionRepository,
            SecRolePermissionRepository rolePermissionRepository,
            SysRoleMenuConfigRepository roleMenuConfigRepository,
            SysNotificationTemplateRepository notificationTemplateRepository,
            SysPlatformSettingRepository platformSettingRepository,
            AuditLogService auditLogService
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleMenuConfigRepository = roleMenuConfigRepository;
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.platformSettingRepository = platformSettingRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<PermissionMatrixRowResponse> getPermissionMatrix() {
        List<SecRole> roles = roleRepository.findAllByDeletedFalseOrderBySortOrderAscRoleNameAsc();
        Map<UUID, Set<String>> rolePermissionMap = new LinkedHashMap<>();
        for (SecRole role : roles) {
            rolePermissionMap.put(role.getRoleId(), new LinkedHashSet<>(rolePermissionRepository.findAllowedPermissionCodes(role.getRoleId())));
        }

        List<PermissionMatrixRowResponse> rows = new ArrayList<>();
        for (SecPermission permission : permissionRepository.findAllByDeletedFalseOrderByModuleCodeAscActionCodeAsc()) {
            Map<String, Boolean> roleAllowed = new LinkedHashMap<>();
            for (SecRole role : roles) {
                roleAllowed.put(role.getRoleCode().name(), rolePermissionMap.get(role.getRoleId()).contains(permission.getPermissionCode()));
            }
            rows.add(new PermissionMatrixRowResponse(permission.getPermissionCode(), permission.getModuleCode(), permission.getActionCode(), permission.getPermissionName(), roleAllowed));
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public List<RoleMenuConfigResponse> listRoleMenuConfigs(UUID roleId) {
        return roleMenuConfigRepository.findAllByRoleRoleIdAndDeletedFalseOrderBySortOrderAscMenuNameAsc(roleId).stream().map(this::toRoleMenuConfigResponse).toList();
    }

    @Transactional
    public List<RoleMenuConfigResponse> replaceRoleMenuConfigs(UUID roleId, List<RoleMenuConfigRequest> requests) {
        SecRole role = getRole(roleId);
        List<RoleMenuConfigResponse> oldSnapshot = listRoleMenuConfigs(roleId);
        List<SysRoleMenuConfig> existing = roleMenuConfigRepository.findAllByRoleRoleIdAndDeletedFalseOrderBySortOrderAscMenuNameAsc(roleId);
        existing.forEach(item -> item.setDeleted(true));
        roleMenuConfigRepository.saveAll(existing);
        roleMenuConfigRepository.flush();

        List<SysRoleMenuConfig> toSave = new ArrayList<>();
        for (RoleMenuConfigRequest request : requests) {
            SysRoleMenuConfig entity = new SysRoleMenuConfig();
            entity.setRole(role);
            entity.setMenuKey(request.menuKey().trim());
            entity.setMenuName(request.menuName().trim());
            entity.setRoutePath(request.routePath().trim());
            entity.setIconName(blankToNull(request.iconName()));
            entity.setParentMenuKey(blankToNull(request.parentMenuKey()));
            entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
            entity.setVisible(request.visible());
            entity.setStatus(request.status());
            toSave.add(entity);
        }

        List<RoleMenuConfigResponse> response = roleMenuConfigRepository.saveAll(toSave).stream().map(this::toRoleMenuConfigResponse).toList();
        auditLogService.logSuccess("REPLACE_MENU_CONFIG", "ROLE_MENU", "sys_role_menu_config", roleId.toString(), oldSnapshot, response, "Thay thế cấu hình menu theo role.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> listNotificationTemplates() {
        return notificationTemplateRepository.findAllByDeletedFalseOrderByTemplateCodeAsc().stream().map(this::toNotificationTemplateResponse).toList();
    }

    @Transactional
    public NotificationTemplateResponse createNotificationTemplate(NotificationTemplateRequest request) {
        if (notificationTemplateRepository.existsByTemplateCodeIgnoreCaseAndDeletedFalse(request.templateCode())) {
            throw new BusinessException("NOTIFICATION_TEMPLATE_CODE_DUPLICATE", "templateCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        SysNotificationTemplate entity = new SysNotificationTemplate();
        applyNotificationTemplate(entity, request);
        entity = notificationTemplateRepository.save(entity);
        NotificationTemplateResponse response = toNotificationTemplateResponse(entity);
        auditLogService.logSuccess("CREATE", "NOTIFICATION_TEMPLATE", "sys_notification_template", entity.getNotificationTemplateId().toString(), null, response, "Tạo mẫu thông báo.");
        return response;
    }

    @Transactional
    public NotificationTemplateResponse updateNotificationTemplate(Long templateId, NotificationTemplateRequest request) {
        SysNotificationTemplate entity = getNotificationTemplate(templateId);
        NotificationTemplateResponse oldSnapshot = toNotificationTemplateResponse(entity);
        if (notificationTemplateRepository.existsByTemplateCodeIgnoreCaseAndDeletedFalseAndNotificationTemplateIdNot(request.templateCode(), templateId)) {
            throw new BusinessException("NOTIFICATION_TEMPLATE_CODE_DUPLICATE", "templateCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        applyNotificationTemplate(entity, request);
        entity = notificationTemplateRepository.save(entity);
        NotificationTemplateResponse response = toNotificationTemplateResponse(entity);
        auditLogService.logSuccess("UPDATE", "NOTIFICATION_TEMPLATE", "sys_notification_template", templateId.toString(), oldSnapshot, response, "Cập nhật mẫu thông báo.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<PlatformSettingResponse> listPlatformSettings() {
        return platformSettingRepository.findAllByDeletedFalseOrderBySettingKeyAsc().stream().map(this::toPlatformSettingResponse).toList();
    }

    @Transactional
    public PlatformSettingResponse updatePlatformSetting(Long settingId, PlatformSettingRequest request) {
        SysPlatformSetting entity = getPlatformSetting(settingId);
        if (!entity.isEditable()) {
            throw new BusinessException("PLATFORM_SETTING_NOT_EDITABLE", "Tham số này không cho phép cập nhật.", HttpStatus.CONFLICT);
        }
        PlatformSettingResponse oldSnapshot = toPlatformSettingResponse(entity);
        entity.setSettingName(request.settingName().trim());
        entity.setSettingValue(request.settingValue());
        entity.setValueType(request.valueType());
        entity.setEditable(request.editable());
        entity.setStatus(request.status());
        entity.setDescription(blankToNull(request.description()));
        entity = platformSettingRepository.save(entity);
        PlatformSettingResponse response = toPlatformSettingResponse(entity);
        auditLogService.logSuccess("UPDATE", "PLATFORM_SETTING", "sys_platform_setting", settingId.toString(), oldSnapshot, response, "Cập nhật tham số nền tảng.");
        return response;
    }

    private void applyNotificationTemplate(SysNotificationTemplate entity, NotificationTemplateRequest request) {
        entity.setTemplateCode(request.templateCode().trim().toUpperCase());
        entity.setTemplateName(request.templateName().trim());
        entity.setChannelCode(request.channelCode());
        entity.setSubjectTemplate(blankToNull(request.subjectTemplate()));
        entity.setBodyTemplate(request.bodyTemplate().trim());
        entity.setStatus(request.status());
        entity.setDescription(blankToNull(request.description()));
    }

    private RoleMenuConfigResponse toRoleMenuConfigResponse(SysRoleMenuConfig entity) {
        return new RoleMenuConfigResponse(entity.getRoleMenuConfigId(), entity.getRole().getRoleCode().name(), entity.getRole().getRoleName(), entity.getMenuKey(), entity.getMenuName(), entity.getRoutePath(), entity.getIconName(), entity.getParentMenuKey(), entity.getSortOrder(), entity.isVisible(), entity.getStatus().name());
    }

    private NotificationTemplateResponse toNotificationTemplateResponse(SysNotificationTemplate entity) {
        return new NotificationTemplateResponse(entity.getNotificationTemplateId(), entity.getTemplateCode(), entity.getTemplateName(), entity.getChannelCode().name(), entity.getSubjectTemplate(), entity.getBodyTemplate(), entity.getStatus().name(), entity.getDescription());
    }

    private PlatformSettingResponse toPlatformSettingResponse(SysPlatformSetting entity) {
        return new PlatformSettingResponse(entity.getPlatformSettingId(), entity.getSettingKey(), entity.getSettingName(), entity.getSettingValue(), entity.getValueType().name(), entity.isEditable(), entity.getStatus().name(), entity.getDescription());
    }

    private SecRole getRole(UUID roleId) {
        return roleRepository.findByRoleIdAndDeletedFalse(roleId).orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Không tìm thấy role."));
    }

    private SysNotificationTemplate getNotificationTemplate(Long templateId) {
        return notificationTemplateRepository.findByNotificationTemplateIdAndDeletedFalse(templateId).orElseThrow(() -> new NotFoundException("NOTIFICATION_TEMPLATE_NOT_FOUND", "Không tìm thấy mẫu thông báo."));
    }

    private SysPlatformSetting getPlatformSetting(Long platformSettingId) {
        return platformSettingRepository.findByPlatformSettingIdAndDeletedFalse(platformSettingId).orElseThrow(() -> new NotFoundException("PLATFORM_SETTING_NOT_FOUND", "Không tìm thấy tham số nền tảng."));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
