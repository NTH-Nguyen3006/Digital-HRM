SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__base_roles_admin.sql
   Scope:
   - seed fixed roles
   - seed permissions
   - seed role-permission mapping
   - seed default role data scope
   Note:
   - default admin user is bootstrapped by application layer
   ========================================================= */

UPDATE r
SET
    r.role_name = s.role_name,
    r.description = s.description,
    r.status = 'ACTIVE',
    r.is_system_role = 1,
    r.sort_order = s.sort_order,
    r.updated_at = SYSDATETIME()
FROM dbo.sec_role r
INNER JOIN (
    VALUES
        ('ADMIN', N'Administrator', N'Quản trị hệ thống', 1),
        ('HR', N'Human Resources', N'Nhân sự', 2),
        ('MANAGER', N'Manager', N'Quản lý', 3),
        ('EMPLOYEE', N'Employee', N'Nhân viên', 4)
) s(role_code, role_name, description, sort_order)
    ON r.role_code = s.role_code
WHERE r.is_deleted = 0;
GO

INSERT INTO dbo.sec_role (
    role_code, role_name, description, status, is_system_role, sort_order, created_at, is_deleted
)
SELECT s.role_code, s.role_name, s.description, 'ACTIVE', 1, s.sort_order, SYSDATETIME(), 0
FROM (
    VALUES
        ('ADMIN', N'Administrator', N'Quản trị hệ thống', 1),
        ('HR', N'Human Resources', N'Nhân sự', 2),
        ('MANAGER', N'Manager', N'Quản lý', 3),
        ('EMPLOYEE', N'Employee', N'Nhân viên', 4)
) s(role_code, role_name, description, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_role r WHERE r.role_code = s.role_code AND r.is_deleted = 0
);
GO

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
        ('auth.login', 'AUTH', 'LOGIN', N'Đăng nhập hệ thống', N'Cho phép đăng nhập hệ thống'),
        ('auth.logout', 'AUTH', 'LOGOUT', N'Đăng xuất an toàn', N'Cho phép đăng xuất phiên hiện tại'),
        ('auth.forgot_password', 'AUTH', 'FORGOT_PASSWORD', N'Quên mật khẩu', N'Cho phép yêu cầu đặt lại mật khẩu'),
        ('auth.change_password', 'AUTH', 'CHANGE_PASSWORD', N'Đổi mật khẩu', N'Cho phép đổi mật khẩu'),
        ('user.view', 'USER', 'VIEW', N'Xem danh sách user', N'Cho phép xem danh sách và chi tiết user'),
        ('user.create', 'USER', 'CREATE', N'Tạo mới user', N'Cho phép tạo user mới'),
        ('user.update', 'USER', 'UPDATE', N'Cập nhật user', N'Cho phép sửa user'),
        ('user.lock_unlock', 'USER', 'LOCK_UNLOCK', N'Khóa / Mở khóa user', N'Cho phép khóa hoặc mở khóa user'),
        ('user.assign_role', 'USER', 'ASSIGN_ROLE', N'Gán role cho user', N'Cho phép đổi role chính của user'),
        ('role.view', 'ROLE', 'VIEW', N'Xem danh sách role', N'Cho phép xem danh sách và chi tiết role'),
        ('role.create', 'ROLE', 'CREATE', N'Tạo role tùy biến', N'Cho phép tạo role ngoài 4 role hệ thống'),
        ('role.update', 'ROLE', 'UPDATE', N'Cập nhật role', N'Cho phép cập nhật metadata, permission và data scope của role'),
        ('audit.view', 'AUDIT', 'VIEW', N'Xem audit log', N'Cho phép xem audit log hệ thống')
) s(permission_code, module_code, action_code, permission_name, description)
    ON p.permission_code = s.permission_code
WHERE p.is_deleted = 0;
GO

INSERT INTO dbo.sec_permission (
    permission_code, module_code, action_code, permission_name, description, status, created_at, is_deleted
)
SELECT
    s.permission_code, s.module_code, s.action_code, s.permission_name, s.description,
    'ACTIVE', SYSDATETIME(), 0
FROM (
    VALUES
        ('auth.login', 'AUTH', 'LOGIN', N'Đăng nhập hệ thống', N'Cho phép đăng nhập hệ thống'),
        ('auth.logout', 'AUTH', 'LOGOUT', N'Đăng xuất an toàn', N'Cho phép đăng xuất phiên hiện tại'),
        ('auth.forgot_password', 'AUTH', 'FORGOT_PASSWORD', N'Quên mật khẩu', N'Cho phép yêu cầu đặt lại mật khẩu'),
        ('auth.change_password', 'AUTH', 'CHANGE_PASSWORD', N'Đổi mật khẩu', N'Cho phép đổi mật khẩu'),
        ('user.view', 'USER', 'VIEW', N'Xem danh sách user', N'Cho phép xem danh sách và chi tiết user'),
        ('user.create', 'USER', 'CREATE', N'Tạo mới user', N'Cho phép tạo user mới'),
        ('user.update', 'USER', 'UPDATE', N'Cập nhật user', N'Cho phép sửa user'),
        ('user.lock_unlock', 'USER', 'LOCK_UNLOCK', N'Khóa / Mở khóa user', N'Cho phép khóa hoặc mở khóa user'),
        ('user.assign_role', 'USER', 'ASSIGN_ROLE', N'Gán role cho user', N'Cho phép đổi role chính của user'),
        ('role.view', 'ROLE', 'VIEW', N'Xem danh sách role', N'Cho phép xem danh sách và chi tiết role'),
        ('role.create', 'ROLE', 'CREATE', N'Tạo role tùy biến', N'Cho phép tạo role ngoài 4 role hệ thống'),
        ('role.update', 'ROLE', 'UPDATE', N'Cập nhật role', N'Cho phép cập nhật metadata, permission và data scope của role'),
        ('audit.view', 'AUDIT', 'VIEW', N'Xem audit log', N'Cho phép xem audit log hệ thống')
) s(permission_code, module_code, action_code, permission_name, description)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_permission p WHERE p.permission_code = s.permission_code AND p.is_deleted = 0
);
GO

DECLARE @rolePermissionSeed TABLE (
    role_code VARCHAR(30),
    permission_code VARCHAR(100)
);

INSERT INTO @rolePermissionSeed(role_code, permission_code)
VALUES
    ('ADMIN', 'auth.login'), ('ADMIN', 'auth.logout'), ('ADMIN', 'auth.forgot_password'), ('ADMIN', 'auth.change_password'),
    ('ADMIN', 'user.view'), ('ADMIN', 'user.create'), ('ADMIN', 'user.update'), ('ADMIN', 'user.lock_unlock'),
    ('ADMIN', 'user.assign_role'), ('ADMIN', 'role.view'), ('ADMIN', 'role.create'), ('ADMIN', 'role.update'), ('ADMIN', 'audit.view'),
    ('HR', 'auth.login'), ('HR', 'auth.logout'), ('HR', 'auth.forgot_password'), ('HR', 'auth.change_password'),
    ('MANAGER', 'auth.login'), ('MANAGER', 'auth.logout'), ('MANAGER', 'auth.forgot_password'), ('MANAGER', 'auth.change_password'),
    ('EMPLOYEE', 'auth.login'), ('EMPLOYEE', 'auth.logout'), ('EMPLOYEE', 'auth.forgot_password'), ('EMPLOYEE', 'auth.change_password');

UPDATE rp
SET rp.is_allowed = 1
FROM dbo.sec_role_permission rp
INNER JOIN dbo.sec_role r ON rp.role_id = r.role_id
INNER JOIN dbo.sec_permission p ON rp.permission_id = p.permission_id
INNER JOIN @rolePermissionSeed s ON s.role_code = r.role_code AND s.permission_code = p.permission_code;
GO

DECLARE @rolePermissionSeedInsert TABLE (
    role_code VARCHAR(30),
    permission_code VARCHAR(100)
);

INSERT INTO @rolePermissionSeedInsert(role_code, permission_code)
VALUES
    ('ADMIN', 'auth.login'), ('ADMIN', 'auth.logout'), ('ADMIN', 'auth.forgot_password'), ('ADMIN', 'auth.change_password'),
    ('ADMIN', 'user.view'), ('ADMIN', 'user.create'), ('ADMIN', 'user.update'), ('ADMIN', 'user.lock_unlock'),
    ('ADMIN', 'user.assign_role'), ('ADMIN', 'role.view'), ('ADMIN', 'role.create'), ('ADMIN', 'role.update'), ('ADMIN', 'audit.view'),
    ('HR', 'auth.login'), ('HR', 'auth.logout'), ('HR', 'auth.forgot_password'), ('HR', 'auth.change_password'),
    ('MANAGER', 'auth.login'), ('MANAGER', 'auth.logout'), ('MANAGER', 'auth.forgot_password'), ('MANAGER', 'auth.change_password'),
    ('EMPLOYEE', 'auth.login'), ('EMPLOYEE', 'auth.logout'), ('EMPLOYEE', 'auth.forgot_password'), ('EMPLOYEE', 'auth.change_password');

INSERT INTO dbo.sec_role_permission (role_id, permission_id, is_allowed, created_at)
SELECT r.role_id, p.permission_id, 1, SYSDATETIME()
FROM @rolePermissionSeedInsert s
INNER JOIN dbo.sec_role r ON r.role_code = s.role_code AND r.is_deleted = 0
INNER JOIN dbo.sec_permission p ON p.permission_code = s.permission_code AND p.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_role_permission rp WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id
);
GO

DECLARE @roleScopeSeed TABLE (
    role_code VARCHAR(30),
    scope_code VARCHAR(30),
    target_type VARCHAR(30),
    target_ref_id VARCHAR(50),
    priority_order INT
);

INSERT INTO @roleScopeSeed(role_code, scope_code, target_type, target_ref_id, priority_order)
VALUES
    ('ADMIN', 'ALL', 'NONE', NULL, 1),
    ('HR', 'ORG_UNIT', 'ORG_UNIT', NULL, 1),
    ('MANAGER', 'SUBTREE', 'ORG_UNIT', NULL, 1),
    ('EMPLOYEE', 'SELF', 'EMPLOYEE', NULL, 1);

UPDATE d
SET d.status = 'ACTIVE', d.is_inclusive = 1, d.priority_order = s.priority_order
FROM dbo.sec_data_scope_assignment d
INNER JOIN dbo.sec_role r ON d.subject_id = r.role_id
INNER JOIN @roleScopeSeed s
    ON s.role_code = r.role_code
   AND d.scope_code = s.scope_code
   AND ISNULL(d.target_type, '') = ISNULL(s.target_type, '')
   AND ISNULL(d.target_ref_id, '') = ISNULL(s.target_ref_id, '')
WHERE d.subject_type = 'ROLE';
GO

DECLARE @roleScopeSeedInsert TABLE (
    role_code VARCHAR(30),
    scope_code VARCHAR(30),
    target_type VARCHAR(30),
    target_ref_id VARCHAR(50),
    priority_order INT
);

INSERT INTO @roleScopeSeedInsert(role_code, scope_code, target_type, target_ref_id, priority_order)
VALUES
    ('ADMIN', 'ALL', 'NONE', NULL, 1),
    ('HR', 'ORG_UNIT', 'ORG_UNIT', NULL, 1),
    ('MANAGER', 'SUBTREE', 'ORG_UNIT', NULL, 1),
    ('EMPLOYEE', 'SELF', 'EMPLOYEE', NULL, 1);

INSERT INTO dbo.sec_data_scope_assignment (
    subject_type, subject_id, scope_code, target_type, target_ref_id,
    is_inclusive, priority_order, effective_from, status, created_at
)
SELECT
    'ROLE', r.role_id, s.scope_code, s.target_type, s.target_ref_id,
    1, s.priority_order, SYSDATETIME(), 'ACTIVE', SYSDATETIME()
FROM @roleScopeSeedInsert s
INNER JOIN dbo.sec_role r ON r.role_code = s.role_code AND r.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.sec_data_scope_assignment d
    WHERE d.subject_type = 'ROLE'
      AND d.subject_id = r.role_id
      AND d.scope_code = s.scope_code
      AND ISNULL(d.target_type, '') = ISNULL(s.target_type, '')
      AND ISNULL(d.target_ref_id, '') = ISNULL(s.target_ref_id, '')
);
GO
