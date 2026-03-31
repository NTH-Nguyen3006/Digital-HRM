SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__sprint2_hr_permissions.sql
   Scope:
   - seed permission for org unit / job title / employee master
   - seed role-permission mapping for Sprint 2
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
        ('orgunit.view', 'ORG_UNIT', 'VIEW', N'Xem cơ cấu tổ chức', N'Cho phép xem danh sách, chi tiết và cây cơ cấu tổ chức'),
        ('orgunit.create', 'ORG_UNIT', 'CREATE', N'Tạo đơn vị tổ chức', N'Cho phép tạo mới đơn vị tổ chức'),
        ('orgunit.update', 'ORG_UNIT', 'UPDATE', N'Cập nhật đơn vị tổ chức', N'Cho phép sửa thông tin đơn vị tổ chức'),
        ('orgunit.change_status', 'ORG_UNIT', 'CHANGE_STATUS', N'Ngừng sử dụng đơn vị tổ chức', N'Cho phép đổi trạng thái hoạt động của đơn vị'),
        ('orgunit.assign_manager', 'ORG_UNIT', 'ASSIGN_MANAGER', N'Gán quản lý đơn vị', N'Cho phép gán quản lý phụ trách đơn vị'),
        ('jobtitle.view', 'JOB_TITLE', 'VIEW', N'Xem chức danh', N'Cho phép xem danh sách và chi tiết chức danh'),
        ('jobtitle.create', 'JOB_TITLE', 'CREATE', N'Tạo chức danh', N'Cho phép tạo mới chức danh'),
        ('jobtitle.update', 'JOB_TITLE', 'UPDATE', N'Cập nhật chức danh', N'Cho phép sửa chức danh'),
        ('jobtitle.change_status', 'JOB_TITLE', 'CHANGE_STATUS', N'Ngừng sử dụng chức danh', N'Cho phép đổi trạng thái chức danh'),
        ('employee.view', 'EMPLOYEE', 'VIEW', N'Xem hồ sơ nhân sự', N'Cho phép xem danh sách và chi tiết nhân sự'),
        ('employee.create', 'EMPLOYEE', 'CREATE', N'Tạo nhân sự', N'Cho phép tạo mới hồ sơ nhân sự'),
        ('employee.update', 'EMPLOYEE', 'UPDATE', N'Cập nhật nhân sự', N'Cho phép sửa thông tin lõi của nhân sự'),
        ('employee.change_status', 'EMPLOYEE', 'CHANGE_STATUS', N'Đổi trạng thái lao động', N'Cho phép đổi trạng thái lao động của nhân sự'),
        ('employee.transfer', 'EMPLOYEE', 'TRANSFER', N'Điều chuyển nhân sự', N'Cho phép điều chuyển nhân sự giữa đơn vị'),
        ('employee.profile.view', 'EMPLOYEE_PROFILE', 'VIEW', N'Xem hồ sơ mở rộng', N'Cho phép xem hồ sơ mở rộng và dữ liệu vệ tinh'),
        ('employee.profile.update', 'EMPLOYEE_PROFILE', 'UPDATE', N'Cập nhật hồ sơ mở rộng', N'Cho phép cập nhật hồ sơ mở rộng, địa chỉ, người liên hệ, giấy tờ, ngân hàng'),
        ('employee.document.view', 'EMPLOYEE_DOCUMENT', 'VIEW', N'Xem tài liệu hồ sơ', N'Cho phép xem metadata tài liệu hồ sơ nhân sự'),
        ('employee.document.manage', 'EMPLOYEE_DOCUMENT', 'MANAGE', N'Quản lý tài liệu hồ sơ', N'Cho phép thêm, sửa, xóa mềm metadata tài liệu hồ sơ')
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
        ('orgunit.view', 'ORG_UNIT', 'VIEW', N'Xem cơ cấu tổ chức', N'Cho phép xem danh sách, chi tiết và cây cơ cấu tổ chức'),
        ('orgunit.create', 'ORG_UNIT', 'CREATE', N'Tạo đơn vị tổ chức', N'Cho phép tạo mới đơn vị tổ chức'),
        ('orgunit.update', 'ORG_UNIT', 'UPDATE', N'Cập nhật đơn vị tổ chức', N'Cho phép sửa thông tin đơn vị tổ chức'),
        ('orgunit.change_status', 'ORG_UNIT', 'CHANGE_STATUS', N'Ngừng sử dụng đơn vị tổ chức', N'Cho phép đổi trạng thái hoạt động của đơn vị'),
        ('orgunit.assign_manager', 'ORG_UNIT', 'ASSIGN_MANAGER', N'Gán quản lý đơn vị', N'Cho phép gán quản lý phụ trách đơn vị'),
        ('jobtitle.view', 'JOB_TITLE', 'VIEW', N'Xem chức danh', N'Cho phép xem danh sách và chi tiết chức danh'),
        ('jobtitle.create', 'JOB_TITLE', 'CREATE', N'Tạo chức danh', N'Cho phép tạo mới chức danh'),
        ('jobtitle.update', 'JOB_TITLE', 'UPDATE', N'Cập nhật chức danh', N'Cho phép sửa chức danh'),
        ('jobtitle.change_status', 'JOB_TITLE', 'CHANGE_STATUS', N'Ngừng sử dụng chức danh', N'Cho phép đổi trạng thái chức danh'),
        ('employee.view', 'EMPLOYEE', 'VIEW', N'Xem hồ sơ nhân sự', N'Cho phép xem danh sách và chi tiết nhân sự'),
        ('employee.create', 'EMPLOYEE', 'CREATE', N'Tạo nhân sự', N'Cho phép tạo mới hồ sơ nhân sự'),
        ('employee.update', 'EMPLOYEE', 'UPDATE', N'Cập nhật nhân sự', N'Cho phép sửa thông tin lõi của nhân sự'),
        ('employee.change_status', 'EMPLOYEE', 'CHANGE_STATUS', N'Đổi trạng thái lao động', N'Cho phép đổi trạng thái lao động của nhân sự'),
        ('employee.transfer', 'EMPLOYEE', 'TRANSFER', N'Điều chuyển nhân sự', N'Cho phép điều chuyển nhân sự giữa đơn vị'),
        ('employee.profile.view', 'EMPLOYEE_PROFILE', 'VIEW', N'Xem hồ sơ mở rộng', N'Cho phép xem hồ sơ mở rộng và dữ liệu vệ tinh'),
        ('employee.profile.update', 'EMPLOYEE_PROFILE', 'UPDATE', N'Cập nhật hồ sơ mở rộng', N'Cho phép cập nhật hồ sơ mở rộng, địa chỉ, người liên hệ, giấy tờ, ngân hàng'),
        ('employee.document.view', 'EMPLOYEE_DOCUMENT', 'VIEW', N'Xem tài liệu hồ sơ', N'Cho phép xem metadata tài liệu hồ sơ nhân sự'),
        ('employee.document.manage', 'EMPLOYEE_DOCUMENT', 'MANAGE', N'Quản lý tài liệu hồ sơ', N'Cho phép thêm, sửa, xóa mềm metadata tài liệu hồ sơ')
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
    ('ADMIN', 'orgunit.view'), ('ADMIN', 'orgunit.create'), ('ADMIN', 'orgunit.update'), ('ADMIN', 'orgunit.change_status'), ('ADMIN', 'orgunit.assign_manager'),
    ('ADMIN', 'jobtitle.view'), ('ADMIN', 'jobtitle.create'), ('ADMIN', 'jobtitle.update'), ('ADMIN', 'jobtitle.change_status'),
    ('ADMIN', 'employee.view'), ('ADMIN', 'employee.create'), ('ADMIN', 'employee.update'), ('ADMIN', 'employee.change_status'), ('ADMIN', 'employee.transfer'),
    ('ADMIN', 'employee.profile.view'), ('ADMIN', 'employee.profile.update'), ('ADMIN', 'employee.document.view'), ('ADMIN', 'employee.document.manage'),

    ('HR', 'orgunit.view'), ('HR', 'orgunit.create'), ('HR', 'orgunit.update'), ('HR', 'orgunit.change_status'), ('HR', 'orgunit.assign_manager'),
    ('HR', 'jobtitle.view'), ('HR', 'jobtitle.create'), ('HR', 'jobtitle.update'), ('HR', 'jobtitle.change_status'),
    ('HR', 'employee.view'), ('HR', 'employee.create'), ('HR', 'employee.update'), ('HR', 'employee.change_status'), ('HR', 'employee.transfer'),
    ('HR', 'employee.profile.view'), ('HR', 'employee.profile.update'), ('HR', 'employee.document.view'), ('HR', 'employee.document.manage'),

    ('MANAGER', 'orgunit.view'), ('MANAGER', 'employee.view'), ('MANAGER', 'employee.profile.view'), ('MANAGER', 'employee.document.view');

UPDATE rp
SET rp.is_allowed = 1
FROM dbo.sec_role_permission rp
INNER JOIN dbo.sec_role r ON rp.role_id = r.role_id
INNER JOIN dbo.sec_permission p ON rp.permission_id = p.permission_id
INNER JOIN @rolePermissionSeed s ON s.role_code = r.role_code AND s.permission_code = p.permission_code;

DECLARE @rolePermissionSeedInsert TABLE (
    role_code VARCHAR(30),
    permission_code VARCHAR(100)
);

INSERT INTO @rolePermissionSeedInsert(role_code, permission_code)
SELECT role_code, permission_code FROM @rolePermissionSeed;

INSERT INTO dbo.sec_role_permission (role_id, permission_id, is_allowed, created_at)
SELECT r.role_id, p.permission_id, 1, SYSDATETIME()
FROM @rolePermissionSeedInsert s
INNER JOIN dbo.sec_role r ON r.role_code = s.role_code AND r.is_deleted = 0
INNER JOIN dbo.sec_permission p ON p.permission_code = s.permission_code AND p.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_role_permission rp WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id
);
GO

SET NOCOUNT OFF;
GO