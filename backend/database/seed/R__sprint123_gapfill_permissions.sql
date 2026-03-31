SET NOCOUNT ON;

/* =========================================================
   R__sprint123_gapfill_permissions.sql
   Scope:
   - role status patch
   - permission matrix / menu config / template / platform setting
   - org import export
   - employee profile workflow
   - contract export
   - onboarding
   ========================================================= */

UPDATE p
SET
    p.module_code = s.module_code,
    p.action_code = s.action_code,
    p.permission_name = s.permission_name,
    p.description = s.description,
    p.status = 'ACTIVE',
    p.updated_at = SYSDATETIME()
    FROM dbo.sec_permission p
INNER JOIN (
    VALUES
        ('role.change_status', 'ROLE', 'CHANGE_STATUS', N'Ngừng sử dụng role', N'Cho phép đổi trạng thái hoạt động của role'),
        ('permission.matrix.view', 'PERMISSION_MATRIX', 'VIEW', N'Xem ma trận permission', N'Cho phép xem ma trận role-permission toàn hệ thống'),
        ('role.menu.manage', 'ROLE_MENU', 'MANAGE', N'Cấu hình menu theo role', N'Cho phép cấu hình menu hiển thị theo role'),
        ('notification.template.view', 'NOTIFICATION_TEMPLATE', 'VIEW', N'Xem mẫu thông báo', N'Cho phép xem danh sách và chi tiết mẫu thông báo'),
        ('notification.template.manage', 'NOTIFICATION_TEMPLATE', 'MANAGE', N'Quản lý mẫu thông báo', N'Cho phép tạo và cập nhật mẫu thông báo'),
        ('platform.setting.view', 'PLATFORM_SETTING', 'VIEW', N'Xem tham số nền tảng', N'Cho phép xem tham số cấu hình hệ thống'),
        ('platform.setting.manage', 'PLATFORM_SETTING', 'MANAGE', N'Quản lý tham số nền tảng', N'Cho phép cập nhật tham số cấu hình hệ thống'),
        ('orgunit.import_export', 'ORG_UNIT', 'IMPORT_EXPORT', N'Import/Export cơ cấu tổ chức', N'Cho phép import/export cơ cấu tổ chức và chức danh'),
        ('employee.profile.request.submit', 'EMPLOYEE_PROFILE_REQUEST', 'SUBMIT', N'Gửi yêu cầu cập nhật hồ sơ', N'Cho phép gửi yêu cầu cập nhật thông tin cá nhân'),
        ('employee.profile.request.review', 'EMPLOYEE_PROFILE_REQUEST', 'REVIEW', N'Duyệt yêu cầu cập nhật hồ sơ', N'Cho phép duyệt hoặc từ chối yêu cầu cập nhật hồ sơ'),
        ('employee.profile.lock_restore', 'EMPLOYEE_PROFILE', 'LOCK_RESTORE', N'Khóa/Khôi phục hồ sơ nhân viên', N'Cho phép khóa hoặc khôi phục hồ sơ nhân viên'),
        ('employee.profile.timeline.view', 'EMPLOYEE_PROFILE_TIMELINE', 'VIEW', N'Xem timeline hồ sơ nhân sự', N'Cho phép xem timeline thay đổi hồ sơ nhân sự'),
        ('employee.profile.import_export', 'EMPLOYEE_PROFILE', 'IMPORT_EXPORT', N'Import/Export hồ sơ nhân sự', N'Cho phép import/export hồ sơ nhân sự'),
        ('contract.export', 'LABOR_CONTRACT', 'EXPORT', N'Xuất hợp đồng và phụ lục', N'Cho phép xuất nội dung hợp đồng và phụ lục ra file HTML'),
        ('onboarding.view', 'ONBOARDING', 'VIEW', N'Xem onboarding', N'Cho phép xem danh sách và chi tiết onboarding'),
        ('onboarding.create', 'ONBOARDING', 'CREATE', N'Tạo hồ sơ onboarding', N'Cho phép tạo hồ sơ onboarding mới'),
        ('onboarding.update', 'ONBOARDING', 'UPDATE', N'Cập nhật onboarding', N'Cho phép sửa checklist, hồ sơ đầu vào và trang thiết bị onboarding'),
        ('onboarding.create_user', 'ONBOARDING', 'CREATE_USER', N'Tạo user từ onboarding', N'Cho phép tạo employee và user từ onboarding'),
        ('onboarding.link_contract', 'ONBOARDING', 'LINK_CONTRACT', N'Tạo hợp đồng đầu tiên từ onboarding', N'Cho phép tạo hợp đồng đầu tiên từ onboarding')
) s(permission_code, module_code, action_code, permission_name, description)
ON p.permission_code = s.permission_code
WHERE p.is_deleted = 0;

INSERT INTO dbo.sec_permission (
    permission_code, module_code, action_code, permission_name, description, status, created_at, is_deleted
)
SELECT
    s.permission_code, s.module_code, s.action_code, s.permission_name, s.description,
    'ACTIVE', SYSDATETIME(), 0
FROM (
         VALUES
             ('role.change_status', 'ROLE', 'CHANGE_STATUS', N'Ngừng sử dụng role', N'Cho phép đổi trạng thái hoạt động của role'),
             ('permission.matrix.view', 'PERMISSION_MATRIX', 'VIEW', N'Xem ma trận permission', N'Cho phép xem ma trận role-permission toàn hệ thống'),
             ('role.menu.manage', 'ROLE_MENU', 'MANAGE', N'Cấu hình menu theo role', N'Cho phép cấu hình menu hiển thị theo role'),
             ('notification.template.view', 'NOTIFICATION_TEMPLATE', 'VIEW', N'Xem mẫu thông báo', N'Cho phép xem danh sách và chi tiết mẫu thông báo'),
             ('notification.template.manage', 'NOTIFICATION_TEMPLATE', 'MANAGE', N'Quản lý mẫu thông báo', N'Cho phép tạo và cập nhật mẫu thông báo'),
             ('platform.setting.view', 'PLATFORM_SETTING', 'VIEW', N'Xem tham số nền tảng', N'Cho phép xem tham số cấu hình hệ thống'),
             ('platform.setting.manage', 'PLATFORM_SETTING', 'MANAGE', N'Quản lý tham số nền tảng', N'Cho phép cập nhật tham số cấu hình hệ thống'),
             ('orgunit.import_export', 'ORG_UNIT', 'IMPORT_EXPORT', N'Import/Export cơ cấu tổ chức', N'Cho phép import/export cơ cấu tổ chức và chức danh'),
             ('employee.profile.request.submit', 'EMPLOYEE_PROFILE_REQUEST', 'SUBMIT', N'Gửi yêu cầu cập nhật hồ sơ', N'Cho phép gửi yêu cầu cập nhật thông tin cá nhân'),
             ('employee.profile.request.review', 'EMPLOYEE_PROFILE_REQUEST', 'REVIEW', N'Duyệt yêu cầu cập nhật hồ sơ', N'Cho phép duyệt hoặc từ chối yêu cầu cập nhật hồ sơ'),
             ('employee.profile.lock_restore', 'EMPLOYEE_PROFILE', 'LOCK_RESTORE', N'Khóa/Khôi phục hồ sơ nhân viên', N'Cho phép khóa hoặc khôi phục hồ sơ nhân viên'),
             ('employee.profile.timeline.view', 'EMPLOYEE_PROFILE_TIMELINE', 'VIEW', N'Xem timeline hồ sơ nhân sự', N'Cho phép xem timeline thay đổi hồ sơ nhân sự'),
             ('employee.profile.import_export', 'EMPLOYEE_PROFILE', 'IMPORT_EXPORT', N'Import/Export hồ sơ nhân sự', N'Cho phép import/export hồ sơ nhân sự'),
             ('contract.export', 'LABOR_CONTRACT', 'EXPORT', N'Xuất hợp đồng và phụ lục', N'Cho phép xuất nội dung hợp đồng và phụ lục ra file HTML'),
             ('onboarding.view', 'ONBOARDING', 'VIEW', N'Xem onboarding', N'Cho phép xem danh sách và chi tiết onboarding'),
             ('onboarding.create', 'ONBOARDING', 'CREATE', N'Tạo hồ sơ onboarding', N'Cho phép tạo hồ sơ onboarding mới'),
             ('onboarding.update', 'ONBOARDING', 'UPDATE', N'Cập nhật onboarding', N'Cho phép sửa checklist, hồ sơ đầu vào và trang thiết bị onboarding'),
             ('onboarding.create_user', 'ONBOARDING', 'CREATE_USER', N'Tạo user từ onboarding', N'Cho phép tạo employee và user từ onboarding'),
             ('onboarding.link_contract', 'ONBOARDING', 'LINK_CONTRACT', N'Tạo hợp đồng đầu tiên từ onboarding', N'Cho phép tạo hợp đồng đầu tiên từ onboarding')
     ) s(permission_code, module_code, action_code, permission_name, description)
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.sec_permission p
    WHERE p.permission_code = s.permission_code
      AND p.is_deleted = 0
);

DECLARE @rolePermissionSeed TABLE (
    role_code VARCHAR(30),
    permission_code VARCHAR(100)
);

INSERT INTO @rolePermissionSeed(role_code, permission_code)
VALUES
    ('ADMIN', 'role.change_status'), ('ADMIN', 'permission.matrix.view'), ('ADMIN', 'role.menu.manage'),
    ('ADMIN', 'notification.template.view'), ('ADMIN', 'notification.template.manage'),
    ('ADMIN', 'platform.setting.view'), ('ADMIN', 'platform.setting.manage'), ('ADMIN', 'orgunit.import_export'),
    ('ADMIN', 'employee.profile.request.review'), ('ADMIN', 'employee.profile.lock_restore'), ('ADMIN', 'employee.profile.timeline.view'),
    ('ADMIN', 'employee.profile.import_export'), ('ADMIN', 'contract.export'),
    ('ADMIN', 'onboarding.view'), ('ADMIN', 'onboarding.create'), ('ADMIN', 'onboarding.update'),
    ('ADMIN', 'onboarding.create_user'), ('ADMIN', 'onboarding.link_contract'),

    ('HR', 'permission.matrix.view'), ('HR', 'role.menu.manage'),
    ('HR', 'notification.template.view'), ('HR', 'notification.template.manage'),
    ('HR', 'platform.setting.view'), ('HR', 'platform.setting.manage'), ('HR', 'orgunit.import_export'),
    ('HR', 'employee.profile.request.review'), ('HR', 'employee.profile.lock_restore'),
    ('HR', 'employee.profile.timeline.view'), ('HR', 'employee.profile.import_export'),
    ('HR', 'contract.export'), ('HR', 'onboarding.view'), ('HR', 'onboarding.create'),
    ('HR', 'onboarding.update'), ('HR', 'onboarding.create_user'), ('HR', 'onboarding.link_contract'),

    ('MANAGER', 'employee.profile.timeline.view'), ('MANAGER', 'onboarding.view'),
    ('EMPLOYEE', 'employee.profile.request.submit'), ('EMPLOYEE', 'employee.profile.timeline.view');

UPDATE rp
SET rp.is_allowed = 1
    FROM dbo.sec_role_permission rp
INNER JOIN dbo.sec_role r ON rp.role_id = r.role_id
    INNER JOIN dbo.sec_permission p ON rp.permission_id = p.permission_id
    INNER JOIN @rolePermissionSeed s
    ON s.role_code = r.role_code
    AND s.permission_code = p.permission_code;

INSERT INTO dbo.sec_role_permission (role_id, permission_id, is_allowed, created_at)
SELECT r.role_id, p.permission_id, 1, SYSDATETIME()
FROM @rolePermissionSeed s
         INNER JOIN dbo.sec_role r
                    ON r.role_code = s.role_code
                        AND r.is_deleted = 0
         INNER JOIN dbo.sec_permission p
                    ON p.permission_code = s.permission_code
                        AND p.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.sec_role_permission rp
    WHERE rp.role_id = r.role_id
      AND rp.permission_id = p.permission_id
);

SET NOCOUNT OFF;