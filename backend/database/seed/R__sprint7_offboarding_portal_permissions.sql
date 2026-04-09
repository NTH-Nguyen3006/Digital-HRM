SET NOCOUNT ON;

/* =========================================================
   R__sprint7_offboarding_portal_permissions.sql
   Scope:
   - offboarding permissions
   - employee portal permissions
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
        ('offboarding.case.view', 'OFFBOARDING_CASE', 'VIEW', N'Xem hồ sơ offboarding', N'Cho phép HR xem danh sách và chi tiết offboarding'),
        ('offboarding.request.submit', 'OFFBOARDING_CASE', 'SUBMIT', N'Gửi yêu cầu nghỉ việc', N'Cho phép nhân viên gửi yêu cầu nghỉ việc'),
        ('offboarding.review', 'OFFBOARDING_CASE', 'REVIEW', N'Duyệt yêu cầu nghỉ việc', N'Cho phép manager duyệt hoặc từ chối yêu cầu nghỉ việc'),
        ('offboarding.finalize', 'OFFBOARDING_CASE', 'FINALIZE', N'Chốt ngày hiệu lực nghỉ việc', N'Cho phép HR chốt ngày hiệu lực nghỉ việc'),
        ('offboarding.checklist.manage', 'OFFBOARDING_CHECKLIST', 'MANAGE', N'Quản lý checklist bàn giao', N'Cho phép manager tạo và cập nhật checklist bàn giao'),
        ('offboarding.access.revoke', 'OFFBOARDING_ACCESS', 'REVOKE', N'Thu hồi quyền truy cập', N'Cho phép Admin khóa user và thu hồi session khi offboarding'),
        ('offboarding.asset.manage', 'OFFBOARDING_ASSET', 'MANAGE', N'Quản lý thu hồi tài sản', N'Cho phép HR theo dõi thu hồi tài sản'),
        ('offboarding.settlement.prepare', 'OFFBOARDING_SETTLEMENT', 'PREPARE', N'Chuẩn bị settlement offboarding', N'Cho phép HR chốt công, phép và lương cuối cùng'),
        ('offboarding.close', 'OFFBOARDING_CASE', 'CLOSE', N'Đóng hồ sơ offboarding', N'Cho phép HR đóng hồ sơ offboarding sau khi hoàn tất settlement'),
        ('portal.dashboard.view', 'PORTAL_DASHBOARD', 'VIEW', N'Xem dashboard cá nhân', N'Cho phép nhân viên xem dashboard cổng thông tin cá nhân'),
        ('portal.profile.view_self', 'PORTAL_PROFILE', 'VIEW_SELF', N'Xem hồ sơ cá nhân', N'Cho phép nhân viên xem hồ sơ cá nhân từ portal'),
        ('portal.leave.view_self', 'PORTAL_LEAVE', 'VIEW_SELF', N'Xem nghỉ phép cá nhân', N'Cho phép nhân viên xem số dư phép và lịch sử đơn nghỉ'),
        ('portal.leave.request_self', 'PORTAL_LEAVE', 'REQUEST_SELF', N'Tạo đơn nghỉ từ portal', N'Cho phép nhân viên tạo đơn nghỉ từ portal'),
        ('portal.attendance.view_self', 'PORTAL_ATTENDANCE', 'VIEW_SELF', N'Xem chấm công cá nhân', N'Cho phép nhân viên xem log công và lịch sử điều chỉnh'),
        ('portal.attendance.checkin_self', 'PORTAL_ATTENDANCE', 'CHECKIN_SELF', N'Tự chấm công từ portal', N'Cho phép nhân viên check-in/check-out từ portal'),
        ('portal.attendance.adjust_self', 'PORTAL_ATTENDANCE', 'ADJUST_SELF', N'Tạo điều chỉnh công từ portal', N'Cho phép nhân viên gửi yêu cầu điều chỉnh công từ portal'),
        ('portal.contract.view_self', 'PORTAL_CONTRACT', 'VIEW_SELF', N'Xem hợp đồng cá nhân', N'Cho phép nhân viên xem hợp đồng của chính mình'),
        ('portal.payroll.view_self', 'PORTAL_PAYROLL', 'VIEW_SELF', N'Xem phiếu lương cá nhân từ portal', N'Cho phép nhân viên xem phiếu lương cá nhân từ portal'),
        ('portal.inbox.view_self', 'PORTAL_INBOX', 'VIEW_SELF', N'Xem thông báo và task cá nhân', N'Cho phép nhân viên xem inbox thông báo và task cá nhân'),
        ('portal.inbox.mark_read', 'PORTAL_INBOX', 'MARK_READ', N'Đánh dấu đã đọc inbox', N'Cho phép nhân viên đánh dấu đã đọc thông báo trong portal'),
        ('portal.tasks.view_self', 'PORTAL_TASK', 'VIEW_SELF', N'Xem nhiệm vụ cá nhân', N'Cho phép nhân viên xem nhiệm vụ được giao trong portal'),
        ('portal.tasks.update_self', 'PORTAL_TASK', 'UPDATE_SELF', N'Cập nhật nhiệm vụ cá nhân', N'Cho phép nhân viên cập nhật trạng thái nhiệm vụ của chính mình'),
        ('portal.offboarding.view_self', 'PORTAL_OFFBOARDING', 'VIEW_SELF', N'Xem yêu cầu nghỉ việc cá nhân', N'Cho phép nhân viên xem yêu cầu nghỉ việc của chính mình'),
        ('portal.offboarding.request_self', 'PORTAL_OFFBOARDING', 'REQUEST_SELF', N'Tạo yêu cầu nghỉ việc từ portal', N'Cho phép nhân viên gửi yêu cầu nghỉ việc từ portal')
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
        ('offboarding.case.view', 'OFFBOARDING_CASE', 'VIEW', N'Xem hồ sơ offboarding', N'Cho phép HR xem danh sách và chi tiết offboarding'),
        ('offboarding.request.submit', 'OFFBOARDING_CASE', 'SUBMIT', N'Gửi yêu cầu nghỉ việc', N'Cho phép nhân viên gửi yêu cầu nghỉ việc'),
        ('offboarding.review', 'OFFBOARDING_CASE', 'REVIEW', N'Duyệt yêu cầu nghỉ việc', N'Cho phép manager duyệt hoặc từ chối yêu cầu nghỉ việc'),
        ('offboarding.finalize', 'OFFBOARDING_CASE', 'FINALIZE', N'Chốt ngày hiệu lực nghỉ việc', N'Cho phép HR chốt ngày hiệu lực nghỉ việc'),
        ('offboarding.checklist.manage', 'OFFBOARDING_CHECKLIST', 'MANAGE', N'Quản lý checklist bàn giao', N'Cho phép manager tạo và cập nhật checklist bàn giao'),
        ('offboarding.access.revoke', 'OFFBOARDING_ACCESS', 'REVOKE', N'Thu hồi quyền truy cập', N'Cho phép Admin khóa user và thu hồi session khi offboarding'),
        ('offboarding.asset.manage', 'OFFBOARDING_ASSET', 'MANAGE', N'Quản lý thu hồi tài sản', N'Cho phép HR theo dõi thu hồi tài sản'),
        ('offboarding.settlement.prepare', 'OFFBOARDING_SETTLEMENT', 'PREPARE', N'Cho phép HR chốt công, phép và lương cuối cùng', N'Cho phép HR chốt công, phép và lương cuối cùng'),
        ('offboarding.close', 'OFFBOARDING_CASE', 'CLOSE', N'Đóng hồ sơ offboarding', N'Cho phép HR đóng hồ sơ offboarding sau khi hoàn tất settlement'),
        ('portal.dashboard.view', 'PORTAL_DASHBOARD', 'VIEW', N'Xem dashboard cá nhân', N'Cho phép nhân viên xem dashboard cổng thông tin cá nhân'),
        ('portal.profile.view_self', 'PORTAL_PROFILE', 'VIEW_SELF', N'Xem hồ sơ cá nhân', N'Cho phép nhân viên xem hồ sơ cá nhân từ portal'),
        ('portal.leave.view_self', 'PORTAL_LEAVE', 'VIEW_SELF', N'Xem nghỉ phép cá nhân', N'Cho phép nhân viên xem số dư phép và lịch sử đơn nghỉ'),
        ('portal.leave.request_self', 'PORTAL_LEAVE', 'REQUEST_SELF', N'Tạo đơn nghỉ từ portal', N'Cho phép nhân viên tạo đơn nghỉ từ portal'),
        ('portal.attendance.view_self', 'PORTAL_ATTENDANCE', 'VIEW_SELF', N'Xem chấm công cá nhân', N'Cho phép nhân viên xem log công và lịch sử điều chỉnh'),
        ('portal.attendance.checkin_self', 'PORTAL_ATTENDANCE', 'CHECKIN_SELF', N'Tự chấm công từ portal', N'Cho phép nhân viên check-in/check-out từ portal'),
        ('portal.attendance.adjust_self', 'PORTAL_ATTENDANCE', 'ADJUST_SELF', N'Tạo điều chỉnh công từ portal', N'Cho phép nhân viên gửi yêu cầu điều chỉnh công từ portal'),
        ('portal.contract.view_self', 'PORTAL_CONTRACT', 'VIEW_SELF', N'Xem hợp đồng cá nhân', N'Cho phép nhân viên xem hợp đồng của chính mình'),
        ('portal.payroll.view_self', 'PORTAL_PAYROLL', 'VIEW_SELF', N'Xem phiếu lương cá nhân từ portal', N'Cho phép nhân viên xem phiếu lương cá nhân từ portal'),
        ('portal.inbox.view_self', 'PORTAL_INBOX', 'VIEW_SELF', N'Xem thông báo và task cá nhân', N'Cho phép nhân viên xem inbox thông báo và task cá nhân'),
        ('portal.inbox.mark_read', 'PORTAL_INBOX', 'MARK_READ', N'Đánh dấu đã đọc inbox', N'Cho phép nhân viên đánh dấu đã đọc thông báo trong portal'),
        ('portal.tasks.view_self', 'PORTAL_TASK', 'VIEW_SELF', N'Xem nhiệm vụ cá nhân', N'Cho phép nhân viên xem nhiệm vụ được giao trong portal'),
        ('portal.tasks.update_self', 'PORTAL_TASK', 'UPDATE_SELF', N'Cập nhật nhiệm vụ cá nhân', N'Cho phép nhân viên cập nhật trạng thái nhiệm vụ của chính mình'),
        ('portal.offboarding.view_self', 'PORTAL_OFFBOARDING', 'VIEW_SELF', N'Xem yêu cầu nghỉ việc cá nhân', N'Cho phép nhân viên xem yêu cầu nghỉ việc của chính mình'),
        ('portal.offboarding.request_self', 'PORTAL_OFFBOARDING', 'REQUEST_SELF', N'Tạo yêu cầu nghỉ việc từ portal', N'Cho phép nhân viên gửi yêu cầu nghỉ việc từ portal')
) s(permission_code, module_code, action_code, permission_name, description)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_permission p
    WHERE p.permission_code = s.permission_code AND p.is_deleted = 0
);

;WITH role_permission_seed(role_code, permission_code) AS (
    SELECT * FROM (VALUES
        ('HR', 'offboarding.case.view'),
        ('HR', 'offboarding.finalize'),
        ('HR', 'offboarding.asset.manage'),
        ('HR', 'offboarding.settlement.prepare'),
        ('HR', 'offboarding.close'),

        ('MANAGER', 'offboarding.review'),
        ('MANAGER', 'offboarding.checklist.manage'),

        ('ADMIN', 'offboarding.access.revoke'),

        ('EMPLOYEE', 'offboarding.request.submit'),
        ('EMPLOYEE', 'portal.dashboard.view'),
        ('EMPLOYEE', 'portal.profile.view_self'),
        ('EMPLOYEE', 'portal.leave.view_self'),
        ('EMPLOYEE', 'portal.leave.request_self'),
        ('EMPLOYEE', 'portal.attendance.view_self'),
        ('EMPLOYEE', 'portal.attendance.checkin_self'),
        ('EMPLOYEE', 'portal.attendance.adjust_self'),
        ('EMPLOYEE', 'portal.contract.view_self'),
        ('EMPLOYEE', 'portal.payroll.view_self'),
        ('EMPLOYEE', 'portal.inbox.view_self'),
        ('EMPLOYEE', 'portal.inbox.mark_read'),
        ('EMPLOYEE', 'portal.tasks.view_self'),
        ('EMPLOYEE', 'portal.tasks.update_self'),
        ('EMPLOYEE', 'portal.offboarding.view_self'),
        ('EMPLOYEE', 'portal.offboarding.request_self')
    ) v(role_code, permission_code)
)
UPDATE rp
SET rp.is_allowed = 1
FROM dbo.sec_role_permission rp
INNER JOIN dbo.sec_role r ON rp.role_id = r.role_id
INNER JOIN dbo.sec_permission p ON rp.permission_id = p.permission_id
INNER JOIN role_permission_seed s ON s.role_code = r.role_code AND s.permission_code = p.permission_code;

;WITH role_permission_seed(role_code, permission_code) AS (
    SELECT * FROM (VALUES
        ('HR', 'offboarding.case.view'),
        ('HR', 'offboarding.finalize'),
        ('HR', 'offboarding.asset.manage'),
        ('HR', 'offboarding.settlement.prepare'),
        ('HR', 'offboarding.close'),

        ('MANAGER', 'offboarding.review'),
        ('MANAGER', 'offboarding.checklist.manage'),

        ('ADMIN', 'offboarding.access.revoke'),

        ('EMPLOYEE', 'offboarding.request.submit'),
        ('EMPLOYEE', 'portal.dashboard.view'),
        ('EMPLOYEE', 'portal.profile.view_self'),
        ('EMPLOYEE', 'portal.leave.view_self'),
        ('EMPLOYEE', 'portal.leave.request_self'),
        ('EMPLOYEE', 'portal.attendance.view_self'),
        ('EMPLOYEE', 'portal.attendance.checkin_self'),
        ('EMPLOYEE', 'portal.attendance.adjust_self'),
        ('EMPLOYEE', 'portal.contract.view_self'),
        ('EMPLOYEE', 'portal.payroll.view_self'),
        ('EMPLOYEE', 'portal.inbox.view_self'),
        ('EMPLOYEE', 'portal.inbox.mark_read'),
        ('EMPLOYEE', 'portal.tasks.view_self'),
        ('EMPLOYEE', 'portal.tasks.update_self'),
        ('EMPLOYEE', 'portal.offboarding.view_self'),
        ('EMPLOYEE', 'portal.offboarding.request_self')
    ) v(role_code, permission_code)
)
INSERT INTO dbo.sec_role_permission (
    role_id, permission_id, is_allowed, created_at
)
SELECT r.role_id, p.permission_id, 1, SYSDATETIME()
FROM role_permission_seed s
INNER JOIN dbo.sec_role r ON r.role_code = s.role_code AND r.is_deleted = 0
INNER JOIN dbo.sec_permission p ON p.permission_code = s.permission_code AND p.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_role_permission rp
    WHERE rp.role_id = r.role_id
      AND rp.permission_id = p.permission_id
);

SET NOCOUNT OFF;
