SET NOCOUNT ON;

/* =========================================================
   R__storage_permissions.sql
   Scope:
   - generic storage upload/view/download permissions
   ========================================================= */

UPDATE p
SET p.module_code = s.module_code,
    p.action_code = s.action_code,
    p.permission_name = s.permission_name,
    p.description = s.description,
    p.status = 'ACTIVE',
    p.updated_at = SYSDATETIME()
FROM dbo.sec_permission p
INNER JOIN (
    VALUES
        ('storage.file.upload', 'STORAGE_FILE', 'UPLOAD', N'Tải file lên storage', N'Cho phép upload file vật lý vào storage nội bộ'),
        ('storage.file.view', 'STORAGE_FILE', 'VIEW', N'Xem metadata file storage', N'Cho phép xem metadata file vật lý đã upload'),
        ('storage.file.download', 'STORAGE_FILE', 'DOWNLOAD', N'Tải xuống file storage', N'Cho phép tải file vật lý từ storage nội bộ')
) s(permission_code, module_code, action_code, permission_name, description)
ON p.permission_code = s.permission_code
WHERE p.is_deleted = 0;

INSERT INTO dbo.sec_permission (
    permission_code, module_code, action_code, permission_name, description, status, created_at, is_deleted
)
SELECT s.permission_code, s.module_code, s.action_code, s.permission_name, s.description, 'ACTIVE', SYSDATETIME(), 0
FROM (
    VALUES
        ('storage.file.upload', 'STORAGE_FILE', 'UPLOAD', N'Tải file lên storage', N'Cho phép upload file vật lý vào storage nội bộ'),
        ('storage.file.view', 'STORAGE_FILE', 'VIEW', N'Xem metadata file storage', N'Cho phép xem metadata file vật lý đã upload'),
        ('storage.file.download', 'STORAGE_FILE', 'DOWNLOAD', N'Tải xuống file storage', N'Cho phép tải file vật lý từ storage nội bộ')
) s(permission_code, module_code, action_code, permission_name, description)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_permission p
    WHERE p.permission_code = s.permission_code
      AND p.is_deleted = 0
);

DECLARE @rolePermissionSeed TABLE (
    role_code VARCHAR(30),
    permission_code VARCHAR(100)
);

INSERT INTO @rolePermissionSeed(role_code, permission_code)
VALUES
    ('ADMIN', 'storage.file.upload'), ('ADMIN', 'storage.file.view'), ('ADMIN', 'storage.file.download'),
    ('HR', 'storage.file.upload'), ('HR', 'storage.file.view'), ('HR', 'storage.file.download'),
    ('MANAGER', 'storage.file.upload'), ('MANAGER', 'storage.file.view'), ('MANAGER', 'storage.file.download'),
    ('EMPLOYEE', 'storage.file.upload'), ('EMPLOYEE', 'storage.file.view'), ('EMPLOYEE', 'storage.file.download');

UPDATE rp
SET rp.is_allowed = 1
FROM dbo.sec_role_permission rp
INNER JOIN dbo.sec_role r ON rp.role_id = r.role_id
INNER JOIN dbo.sec_permission p ON rp.permission_id = p.permission_id
INNER JOIN @rolePermissionSeed s ON s.role_code = r.role_code AND s.permission_code = p.permission_code;

INSERT INTO dbo.sec_role_permission (role_id, permission_id, is_allowed, created_at)
SELECT r.role_id, p.permission_id, 1, SYSDATETIME()
FROM @rolePermissionSeed s
INNER JOIN dbo.sec_role r ON r.role_code = s.role_code AND r.is_deleted = 0
INNER JOIN dbo.sec_permission p ON p.permission_code = s.permission_code AND p.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_role_permission rp
    WHERE rp.role_id = r.role_id
      AND rp.permission_id = p.permission_id
);

SET NOCOUNT OFF;
