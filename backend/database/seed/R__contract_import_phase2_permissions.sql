SET NOCOUNT ON;

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
        ('contract.import.view', 'CONTRACT_IMPORT', 'VIEW', N'Xem phiên import hợp đồng', N'Cho phép xem danh sách, chi tiết và kết quả trích xuất phiên import hợp đồng'),
        ('contract.import.manage', 'CONTRACT_IMPORT', 'MANAGE', N'Quản lý import hợp đồng', N'Cho phép upload file DOCX, trích xuất dữ liệu và xác nhận lưu hợp đồng từ phiên import')
) s(permission_code, module_code, action_code, permission_name, description)
ON p.permission_code = s.permission_code
WHERE p.is_deleted = 0;

INSERT INTO dbo.sec_permission (permission_code, module_code, action_code, permission_name, description, status, created_at, is_deleted)
SELECT s.permission_code, s.module_code, s.action_code, s.permission_name, s.description, 'ACTIVE', SYSDATETIME(), 0
FROM (
    VALUES
        ('contract.import.view', 'CONTRACT_IMPORT', 'VIEW', N'Xem phiên import hợp đồng', N'Cho phép xem danh sách, chi tiết và kết quả trích xuất phiên import hợp đồng'),
        ('contract.import.manage', 'CONTRACT_IMPORT', 'MANAGE', N'Quản lý import hợp đồng', N'Cho phép upload file DOCX, trích xuất dữ liệu và xác nhận lưu hợp đồng từ phiên import')
) s(permission_code, module_code, action_code, permission_name, description)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_permission p
    WHERE p.permission_code = s.permission_code AND p.is_deleted = 0
);

;WITH role_permission_seed(role_code, permission_code) AS (
    SELECT *
    FROM (VALUES
        ('ADMIN', 'contract.import.view'),
        ('ADMIN', 'contract.import.manage'),
        ('HR', 'contract.import.view'),
        ('HR', 'contract.import.manage')
    ) v(role_code, permission_code)
)
UPDATE rp
SET rp.is_allowed = 1
FROM dbo.sec_role_permission rp
INNER JOIN dbo.sec_role r ON rp.role_id = r.role_id
INNER JOIN dbo.sec_permission p ON rp.permission_id = p.permission_id
INNER JOIN role_permission_seed s ON s.role_code = r.role_code AND s.permission_code = p.permission_code;

;WITH role_permission_seed(role_code, permission_code) AS (
    SELECT *
    FROM (VALUES
        ('ADMIN', 'contract.import.view'),
        ('ADMIN', 'contract.import.manage'),
        ('HR', 'contract.import.view'),
        ('HR', 'contract.import.manage')
    ) v(role_code, permission_code)
)
INSERT INTO dbo.sec_role_permission (role_id, permission_id, is_allowed, created_at)
SELECT r.role_id, p.permission_id, 1, SYSDATETIME()
FROM role_permission_seed s
INNER JOIN dbo.sec_role r ON r.role_code = s.role_code AND r.is_deleted = 0
INNER JOIN dbo.sec_permission p ON p.permission_code = s.permission_code AND p.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_role_permission rp
    WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id
);

SET NOCOUNT OFF;
