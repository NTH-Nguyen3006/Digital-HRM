package com.company.hrm.module.role.service;

import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.common.constant.DataScopeSubjectType;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.config.AppProperties;
import com.company.hrm.module.role.entity.SecDataScopeAssignment;
import com.company.hrm.module.permission.entity.SecPermission;
import com.company.hrm.module.role.entity.SecRole;
import com.company.hrm.module.role.entity.SecRolePermission;
import com.company.hrm.module.role.dto.CreateRoleRequest;
import com.company.hrm.module.role.dto.DataScopeRequest;
import com.company.hrm.module.role.dto.DataScopeResponse;
import com.company.hrm.module.role.dto.PermissionSummaryResponse;
import com.company.hrm.module.role.dto.RoleDetailResponse;
import com.company.hrm.module.role.dto.RoleListItemResponse;
import com.company.hrm.module.role.dto.UpdateRoleRequest;
import com.company.hrm.module.role.dto.UpdateRoleStatusRequest;
import com.company.hrm.module.role.repository.SecDataScopeAssignmentRepository;
import com.company.hrm.module.permission.repository.SecPermissionRepository;
import com.company.hrm.module.role.repository.SecRolePermissionRepository;
import com.company.hrm.module.role.repository.SecRoleRepository;
import com.company.hrm.module.user.repository.SecUserRoleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final AppProperties appProperties;
    private final SecRoleRepository roleRepository;
    private final SecPermissionRepository permissionRepository;
    private final SecRolePermissionRepository rolePermissionRepository;
    private final SecDataScopeAssignmentRepository dataScopeAssignmentRepository;
    private final SecUserRoleRepository userRoleRepository;
    private final AuditLogService auditLogService;

    public RoleService(
            AppProperties appProperties,
            SecRoleRepository roleRepository,
            SecPermissionRepository permissionRepository,
            SecRolePermissionRepository rolePermissionRepository,
            SecDataScopeAssignmentRepository dataScopeAssignmentRepository,
            SecUserRoleRepository userRoleRepository,
            AuditLogService auditLogService
    ) {
        this.appProperties = appProperties;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.dataScopeAssignmentRepository = dataScopeAssignmentRepository;
        this.userRoleRepository = userRoleRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<RoleListItemResponse> listRoles() {
        return roleRepository.findAllByDeletedFalseOrderBySortOrderAscRoleNameAsc()
                .stream()
                .map(role -> new RoleListItemResponse(
                        role.getRoleId(),
                        role.getRoleCode().name(),
                        role.getRoleName(),
                        role.getDescription(),
                        role.getStatus().name(),
                        role.isSystemRole(),
                        role.getSortOrder(),
                        userRoleRepository.countByRoleRoleIdAndStatus(role.getRoleId(), RecordStatus.ACTIVE)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleDetail(UUID roleId) {
        SecRole role = getRole(roleId);
        long activeUserCount = userRoleRepository.countByRoleRoleIdAndStatus(role.getRoleId(), RecordStatus.ACTIVE);

        List<SecRolePermission> assignedPermissions = rolePermissionRepository.findAllByRoleRoleId(role.getRoleId());
        LinkedHashSet<String> allowedPermissionCodes = assignedPermissions.stream()
                .filter(SecRolePermission::isAllowed)
                .map(item -> item.getPermission().getPermissionCode())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<PermissionSummaryResponse> permissions = permissionRepository.findAllByDeletedFalseOrderByModuleCodeAscActionCodeAsc()
                .stream()
                .map(permission -> new PermissionSummaryResponse(
                        permission.getPermissionCode(),
                        permission.getModuleCode(),
                        permission.getActionCode(),
                        permission.getPermissionName(),
                        allowedPermissionCodes.contains(permission.getPermissionCode())
                ))
                .toList();

        List<DataScopeResponse> dataScopes = dataScopeAssignmentRepository
                .findAllBySubjectTypeAndSubjectIdOrderByPriorityOrderAsc(DataScopeSubjectType.ROLE, role.getRoleId())
                .stream()
                .map(this::toDataScopeResponse)
                .toList();

        return new RoleDetailResponse(
                role.getRoleId(),
                role.getRoleCode().name(),
                role.getRoleName(),
                role.getDescription(),
                role.getStatus().name(),
                role.isSystemRole(),
                role.getSortOrder(),
                activeUserCount,
                permissions,
                dataScopes
        );
    }

    @Transactional
    public RoleDetailResponse createRole(CreateRoleRequest request) {
        if (!appProperties.getRoleManagement().isAllowCustomRole()) {
            throw new BusinessException(
                    "ROLE_CREATE_DISABLED",
                    "Sprint 1 đang khóa chức năng tạo role tùy biến vì kiến trúc chốt chỉ dùng 4 role cố định.",
                    HttpStatus.CONFLICT
            );
        }

        RoleCode roleCode;
        try {
            roleCode = RoleCode.valueOf(request.roleCode().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("ROLE_CODE_INVALID", "roleCode không hợp lệ.", HttpStatus.BAD_REQUEST);
        }

        if (roleRepository.existsByRoleCodeAndDeletedFalse(roleCode)) {
            throw new BusinessException("ROLE_CODE_DUPLICATE", "roleCode đã tồn tại.", HttpStatus.CONFLICT);
        }

        SecRole role = new SecRole();
        role.setRoleCode(roleCode);
        role.setRoleName(request.roleName());
        role.setDescription(request.description());
        role.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        role.setStatus(request.status() == null ? RecordStatus.ACTIVE : request.status());
        role.setSystemRole(false);
        roleRepository.save(role);

        syncPermissions(role, request.permissionCodes());
        syncDataScopes(role, request.dataScopes());

        auditLogService.logSuccess("CREATE", "ROLE", "sec_role", role.getRoleId().toString(), null, getRoleDetail(role.getRoleId()), "Tạo role tùy biến.");
        return getRoleDetail(role.getRoleId());
    }

    @Transactional
    public RoleDetailResponse changeStatus(UUID roleId, UpdateRoleStatusRequest request) {
        SecRole role = getRole(roleId);
        RoleDetailResponse oldSnapshot = getRoleDetail(roleId);

        if (role.isSystemRole() && request.status() == RecordStatus.INACTIVE) {
            throw new BusinessException("ROLE_SYSTEM_DISABLE_FORBIDDEN", "Không được ngừng sử dụng 4 role hệ thống cố định.", HttpStatus.CONFLICT);
        }

        role.setStatus(request.status());
        roleRepository.save(role);
        RoleDetailResponse response = getRoleDetail(roleId);
        auditLogService.logSuccess("CHANGE_STATUS", "ROLE", "sec_role", roleId.toString(), oldSnapshot, response, request.reason());
        return response;
    }

    @Transactional
    public RoleDetailResponse updateRole(UUID roleId, UpdateRoleRequest request) {
        SecRole role = getRole(roleId);
        RoleDetailResponse oldSnapshot = getRoleDetail(roleId);

        if (request.status() == RecordStatus.INACTIVE
                && userRoleRepository.countByRoleRoleIdAndStatus(role.getRoleId(), RecordStatus.ACTIVE) > 0) {
            throw new BusinessException(
                    "ROLE_IN_USE",
                    "Không thể vô hiệu hóa role đang còn user active sử dụng.",
                    HttpStatus.CONFLICT
            );
        }

        role.setRoleName(request.roleName());
        role.setDescription(request.description());
        role.setSortOrder(request.sortOrder() == null ? role.getSortOrder() : request.sortOrder());
        role.setStatus(request.status() == null ? role.getStatus() : request.status());
        roleRepository.save(role);

        if (request.permissionCodes() != null) {
            syncPermissions(role, request.permissionCodes());
        }
        if (request.dataScopes() != null) {
            syncDataScopes(role, request.dataScopes());
        }

        RoleDetailResponse newSnapshot = getRoleDetail(roleId);
        auditLogService.logSuccess("UPDATE", "ROLE", "sec_role", roleId.toString(), oldSnapshot, newSnapshot, "Cập nhật role và permission/data scope.");
        return newSnapshot;
    }

    private void syncPermissions(SecRole role, List<String> permissionCodes) {
        List<String> normalizedCodes = permissionCodes == null ? List.of() : permissionCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        List<SecPermission> permissions = normalizedCodes.isEmpty()
                ? List.of()
                : permissionRepository.findByPermissionCodeInAndDeletedFalse(normalizedCodes);

        if (permissions.size() != normalizedCodes.size()) {
            List<String> foundCodes = permissions.stream().map(SecPermission::getPermissionCode).toList();
            List<String> missingCodes = normalizedCodes.stream().filter(code -> !foundCodes.contains(code)).toList();
            throw new BusinessException("PERMISSION_NOT_FOUND", "Permission không tồn tại: " + String.join(", ", missingCodes), HttpStatus.BAD_REQUEST);
        }

        rolePermissionRepository.deleteAllByRoleRoleId(role.getRoleId());

        List<SecRolePermission> rolePermissions = new ArrayList<>();
        permissions.stream()
                .sorted(Comparator.comparing(SecPermission::getPermissionCode))
                .forEach(permission -> {
                    SecRolePermission rolePermission = new SecRolePermission();
                    rolePermission.setRole(role);
                    rolePermission.setPermission(permission);
                    rolePermission.setAllowed(true);
                    rolePermissions.add(rolePermission);
                });

        rolePermissionRepository.saveAll(rolePermissions);
    }

    private void syncDataScopes(SecRole role, List<DataScopeRequest> dataScopes) {
        dataScopeAssignmentRepository.deleteAllBySubjectTypeAndSubjectId(DataScopeSubjectType.ROLE, role.getRoleId());

        if (dataScopes == null || dataScopes.isEmpty()) {
            return;
        }

        List<SecDataScopeAssignment> assignments = new ArrayList<>();
        for (DataScopeRequest request : dataScopes) {
            SecDataScopeAssignment assignment = new SecDataScopeAssignment();
            assignment.setSubjectType(DataScopeSubjectType.ROLE);
            assignment.setSubjectId(role.getRoleId());
            assignment.setScopeCode(request.scopeCode());
            assignment.setTargetType(request.targetType());
            assignment.setTargetRefId(request.targetRefId());
            assignment.setInclusive(request.inclusive());
            assignment.setPriorityOrder(request.priorityOrder() == null ? 0 : request.priorityOrder());
            assignment.setEffectiveFrom(request.effectiveFrom() == null ? LocalDateTime.now() : request.effectiveFrom());
            assignment.setEffectiveTo(request.effectiveTo());
            assignment.setStatus(request.status() == null ? RecordStatus.ACTIVE : request.status());
            assignments.add(assignment);
        }

        dataScopeAssignmentRepository.saveAll(assignments);
    }

    private DataScopeResponse toDataScopeResponse(SecDataScopeAssignment entity) {
        return new DataScopeResponse(
                entity.getDataScopeAssignmentId(),
                entity.getSubjectType().name(),
                entity.getScopeCode().name(),
                entity.getTargetType() == null ? null : entity.getTargetType().name(),
                entity.getTargetRefId(),
                entity.isInclusive(),
                entity.getPriorityOrder(),
                entity.getStatus().name(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo()
        );
    }

    private SecRole getRole(UUID roleId) {
        return roleRepository.findByRoleIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Không tìm thấy role."));
    }
}
