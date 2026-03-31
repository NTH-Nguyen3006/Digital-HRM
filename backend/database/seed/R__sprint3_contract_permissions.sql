SET NOCOUNT ON;

/* =========================================================
   R__sprint3_contract_permissions.sql
   Scope:
   - seed permission cho contract type / labor contract / appendix / attachment / history
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
        ('contract.type.view', 'CONTRACT_TYPE', 'VIEW', N'Xem loại hợp đồng', N'Cho phép xem danh sách, chi tiết và option loại hợp đồng'),
        ('contract.type.create', 'CONTRACT_TYPE', 'CREATE', N'Tạo loại hợp đồng', N'Cho phép tạo mới loại hợp đồng'),
        ('contract.type.update', 'CONTRACT_TYPE', 'UPDATE', N'Cập nhật loại hợp đồng', N'Cho phép chỉnh sửa loại hợp đồng'),
        ('contract.type.change_status', 'CONTRACT_TYPE', 'CHANGE_STATUS', N'Đổi trạng thái loại hợp đồng', N'Cho phép bật/tắt loại hợp đồng'),
        ('contract.view', 'LABOR_CONTRACT', 'VIEW', N'Xem hợp đồng lao động', N'Cho phép xem danh sách, chi tiết và cảnh báo sắp hết hạn'),
        ('contract.create', 'LABOR_CONTRACT', 'CREATE', N'Tạo hợp đồng lao động', N'Cho phép tạo bản nháp hợp đồng và hợp đồng kế nhiệm'),
        ('contract.update', 'LABOR_CONTRACT', 'UPDATE', N'Cập nhật bản nháp hợp đồng', N'Cho phép sửa bản nháp hợp đồng'),
        ('contract.cancel_draft', 'LABOR_CONTRACT', 'CANCEL_DRAFT', N'Hủy bản nháp hợp đồng', N'Cho phép hủy bản nháp hợp đồng'),
        ('contract.submit', 'LABOR_CONTRACT', 'SUBMIT', N'Gửi hợp đồng chờ ký', N'Cho phép chuyển hợp đồng từ nháp sang chờ ký'),
        ('contract.review', 'LABOR_CONTRACT', 'REVIEW', N'Duyệt hoặc từ chối hợp đồng', N'Cho phép duyệt/từ chối hợp đồng ở trạng thái chờ ký'),
        ('contract.activate', 'LABOR_CONTRACT', 'ACTIVATE', N'Chốt hiệu lực hợp đồng', N'Cho phép kích hoạt hợp đồng sau khi duyệt'),
        ('contract.change_status', 'LABOR_CONTRACT', 'CHANGE_STATUS', N'Đổi trạng thái vòng đời hợp đồng', N'Cho phép tạm hoãn, kích hoạt lại, hết hạn hoặc chấm dứt hợp đồng'),
        ('contract.history.view', 'LABOR_CONTRACT_HISTORY', 'VIEW', N'Xem lịch sử hợp đồng', N'Cho phép xem lịch sử chuyển trạng thái hợp đồng'),
        ('contract.appendix.manage', 'CONTRACT_APPENDIX', 'MANAGE', N'Quản lý phụ lục hợp đồng', N'Cho phép tạo, sửa, kích hoạt, hủy phụ lục hợp đồng'),
        ('contract.attachment.manage', 'CONTRACT_ATTACHMENT', 'MANAGE', N'Quản lý file hợp đồng', N'Cho phép quản lý metadata file đính kèm hợp đồng')
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
             ('contract.type.view', 'CONTRACT_TYPE', 'VIEW', N'Xem loại hợp đồng', N'Cho phép xem danh sách, chi tiết và option loại hợp đồng'),
             ('contract.type.create', 'CONTRACT_TYPE', 'CREATE', N'Tạo loại hợp đồng', N'Cho phép tạo mới loại hợp đồng'),
             ('contract.type.update', 'CONTRACT_TYPE', 'UPDATE', N'Cập nhật loại hợp đồng', N'Cho phép chỉnh sửa loại hợp đồng'),
             ('contract.type.change_status', 'CONTRACT_TYPE', 'CHANGE_STATUS', N'Đổi trạng thái loại hợp đồng', N'Cho phép bật/tắt loại hợp đồng'),
             ('contract.view', 'LABOR_CONTRACT', 'VIEW', N'Xem hợp đồng lao động', N'Cho phép xem danh sách, chi tiết và cảnh báo sắp hết hạn'),
             ('contract.create', 'LABOR_CONTRACT', 'CREATE', N'Tạo hợp đồng lao động', N'Cho phép tạo bản nháp hợp đồng và hợp đồng kế nhiệm'),
             ('contract.update', 'LABOR_CONTRACT', 'UPDATE', N'Cập nhật bản nháp hợp đồng', N'Cho phép sửa bản nháp hợp đồng'),
             ('contract.cancel_draft', 'LABOR_CONTRACT', 'CANCEL_DRAFT', N'Hủy bản nháp hợp đồng', N'Cho phép hủy bản nháp hợp đồng'),
             ('contract.submit', 'LABOR_CONTRACT', 'SUBMIT', N'Gửi hợp đồng chờ ký', N'Cho phép chuyển hợp đồng từ nháp sang chờ ký'),
             ('contract.review', 'LABOR_CONTRACT', 'REVIEW', N'Duyệt hoặc từ chối hợp đồng', N'Cho phép duyệt/từ chối hợp đồng ở trạng thái chờ ký'),
             ('contract.activate', 'LABOR_CONTRACT', 'ACTIVATE', N'Chốt hiệu lực hợp đồng', N'Cho phép kích hoạt hợp đồng sau khi duyệt'),
             ('contract.change_status', 'LABOR_CONTRACT', 'CHANGE_STATUS', N'Đổi trạng thái vòng đời hợp đồng', N'Cho phép tạm hoãn, kích hoạt lại, hết hạn hoặc chấm dứt hợp đồng'),
             ('contract.history.view', 'LABOR_CONTRACT_HISTORY', 'VIEW', N'Xem lịch sử hợp đồng', N'Cho phép xem lịch sử chuyển trạng thái hợp đồng'),
             ('contract.appendix.manage', 'CONTRACT_APPENDIX', 'MANAGE', N'Quản lý phụ lục hợp đồng', N'Cho phép tạo, sửa, kích hoạt, hủy phụ lục hợp đồng'),
             ('contract.attachment.manage', 'CONTRACT_ATTACHMENT', 'MANAGE', N'Quản lý file hợp đồng', N'Cho phép quản lý metadata file đính kèm hợp đồng')
     ) s(permission_code, module_code, action_code, permission_name, description)
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.sec_permission p
    WHERE p.permission_code = s.permission_code
      AND p.is_deleted = 0
);

;WITH role_permission_seed(role_code, permission_code) AS (
    SELECT *
    FROM (VALUES
              ('ADMIN', 'contract.type.view'),
              ('ADMIN', 'contract.type.create'),
              ('ADMIN', 'contract.type.update'),
              ('ADMIN', 'contract.type.change_status'),
              ('ADMIN', 'contract.view'),
              ('ADMIN', 'contract.create'),
              ('ADMIN', 'contract.update'),
              ('ADMIN', 'contract.cancel_draft'),
              ('ADMIN', 'contract.submit'),
              ('ADMIN', 'contract.review'),
              ('ADMIN', 'contract.activate'),
              ('ADMIN', 'contract.change_status'),
              ('ADMIN', 'contract.history.view'),
              ('ADMIN', 'contract.appendix.manage'),
              ('ADMIN', 'contract.attachment.manage'),

              ('HR', 'contract.type.view'),
              ('HR', 'contract.type.create'),
              ('HR', 'contract.type.update'),
              ('HR', 'contract.type.change_status'),
              ('HR', 'contract.view'),
              ('HR', 'contract.create'),
              ('HR', 'contract.update'),
              ('HR', 'contract.cancel_draft'),
              ('HR', 'contract.submit'),
              ('HR', 'contract.review'),
              ('HR', 'contract.activate'),
              ('HR', 'contract.change_status'),
              ('HR', 'contract.history.view'),
              ('HR', 'contract.appendix.manage'),
              ('HR', 'contract.attachment.manage')
         ) v(role_code, permission_code)
)
UPDATE rp
SET rp.is_allowed = 1
    FROM dbo.sec_role_permission rp
INNER JOIN dbo.sec_role r
ON rp.role_id = r.role_id
    INNER JOIN dbo.sec_permission p
    ON rp.permission_id = p.permission_id
    INNER JOIN role_permission_seed s
    ON s.role_code = r.role_code
    AND s.permission_code = p.permission_code;

;WITH role_permission_seed(role_code, permission_code) AS (
    SELECT *
    FROM (VALUES
              ('ADMIN', 'contract.type.view'),
              ('ADMIN', 'contract.type.create'),
              ('ADMIN', 'contract.type.update'),
              ('ADMIN', 'contract.type.change_status'),
              ('ADMIN', 'contract.view'),
              ('ADMIN', 'contract.create'),
              ('ADMIN', 'contract.update'),
              ('ADMIN', 'contract.cancel_draft'),
              ('ADMIN', 'contract.submit'),
              ('ADMIN', 'contract.review'),
              ('ADMIN', 'contract.activate'),
              ('ADMIN', 'contract.change_status'),
              ('ADMIN', 'contract.history.view'),
              ('ADMIN', 'contract.appendix.manage'),
              ('ADMIN', 'contract.attachment.manage'),

              ('HR', 'contract.type.view'),
              ('HR', 'contract.type.create'),
              ('HR', 'contract.type.update'),
              ('HR', 'contract.type.change_status'),
              ('HR', 'contract.view'),
              ('HR', 'contract.create'),
              ('HR', 'contract.update'),
              ('HR', 'contract.cancel_draft'),
              ('HR', 'contract.submit'),
              ('HR', 'contract.review'),
              ('HR', 'contract.activate'),
              ('HR', 'contract.change_status'),
              ('HR', 'contract.history.view'),
              ('HR', 'contract.appendix.manage'),
              ('HR', 'contract.attachment.manage')
         ) v(role_code, permission_code)
)
 INSERT INTO dbo.sec_role_permission (
    role_id, permission_id, is_allowed, created_at
)
SELECT
    r.role_id,
    p.permission_id,
    1,
    SYSDATETIME()
FROM role_permission_seed s
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