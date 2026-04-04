SET NOCOUNT ON;

/* =========================================================
   R__sprint4_leave_onboarding_permissions.sql
   Scope:
   - leave module permission seed
   - sprint 4 onboarding permission seed
   - onboarding email template seed
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
        ('leave.type.view', 'LEAVE_TYPE', 'VIEW', N'Xem loại nghỉ phép', N'Cho phép xem danh sách, chi tiết loại nghỉ phép và rule hiệu lực'),
        ('leave.type.create_update', 'LEAVE_TYPE', 'CREATE_UPDATE', N'Tạo cập nhật loại nghỉ phép', N'Cho phép tạo mới hoặc version rule của loại nghỉ phép'),
        ('leave.type.deactivate', 'LEAVE_TYPE', 'DEACTIVATE', N'Ngừng sử dụng loại nghỉ phép', N'Cho phép deactivate loại nghỉ phép'),
        ('leave.balance.adjust', 'LEAVE_BALANCE', 'ADJUST', N'Điều chỉnh số dư phép', N'Cho phép HR điều chỉnh opening hoặc adjusted units'),
        ('leave.request.create', 'LEAVE_REQUEST', 'CREATE', N'Tạo đơn nghỉ phép', N'Cho phép nhân viên tạo đơn nghỉ phép'),
        ('leave.request.update_cancel', 'LEAVE_REQUEST', 'UPDATE_CANCEL', N'Cập nhật hoặc hủy đơn nghỉ phép', N'Cho phép nhân viên sửa hoặc hủy đơn trước khi xử lý'),
        ('leave.request.approve', 'LEAVE_REQUEST', 'APPROVE', N'Duyệt hoặc từ chối đơn nghỉ phép', N'Cho phép quản lý duyệt hoặc từ chối đơn của team'),
        ('leave.request.finalize', 'LEAVE_REQUEST', 'FINALIZE', N'Chốt đơn nghỉ phép', N'Cho phép HR finalize đơn và trừ số dư phép'),
        ('leave.calendar.view_team', 'LEAVE_CALENDAR', 'VIEW_TEAM', N'Xem lịch nghỉ của team', N'Cho phép quản lý xem lịch nghỉ đã chốt trong phạm vi team'),
        ('leave.period.close', 'LEAVE_PERIOD', 'CLOSE', N'Khóa kỳ phép năm', N'Cho phép HR carry forward và khóa kỳ phép cũ'),
        ('leave.balance.settlement', 'LEAVE_BALANCE', 'SETTLEMENT', N'Tất toán phép khi nghỉ việc', N'Cho phép HR tất toán số dư phép cuối kỳ hoặc nghỉ việc'),
        ('leave.report.export', 'LEAVE_REPORT', 'EXPORT', N'Xuất báo cáo nghỉ phép', N'Cho phép HR export CSV báo cáo nghỉ phép'),
        ('onboarding.orientation.confirm', 'ONBOARDING', 'ORIENTATION_CONFIRM', N'Xác nhận orientation', N'Cho phép quản lý xác nhận đã hoàn tất orientation cho nhân sự mới'),
        ('onboarding.complete', 'ONBOARDING', 'COMPLETE', N'Hoàn tất onboarding', N'Cho phép HR chốt onboarding khi checklist và hợp đồng đầu tiên đã đạt điều kiện'),
        ('onboarding.notify', 'ONBOARDING', 'NOTIFY', N'Gửi thông báo onboarding', N'Cho phép HR gửi email thông báo kế hoạch onboarding')
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
        ('leave.type.view', 'LEAVE_TYPE', 'VIEW', N'Xem loại nghỉ phép', N'Cho phép xem danh sách, chi tiết loại nghỉ phép và rule hiệu lực'),
        ('leave.type.create_update', 'LEAVE_TYPE', 'CREATE_UPDATE', N'Tạo cập nhật loại nghỉ phép', N'Cho phép tạo mới hoặc version rule của loại nghỉ phép'),
        ('leave.type.deactivate', 'LEAVE_TYPE', 'DEACTIVATE', N'Ngừng sử dụng loại nghỉ phép', N'Cho phép deactivate loại nghỉ phép'),
        ('leave.balance.adjust', 'LEAVE_BALANCE', 'ADJUST', N'Điều chỉnh số dư phép', N'Cho phép HR điều chỉnh opening hoặc adjusted units'),
        ('leave.request.create', 'LEAVE_REQUEST', 'CREATE', N'Tạo đơn nghỉ phép', N'Cho phép nhân viên tạo đơn nghỉ phép'),
        ('leave.request.update_cancel', 'LEAVE_REQUEST', 'UPDATE_CANCEL', N'Cập nhật hoặc hủy đơn nghỉ phép', N'Cho phép nhân viên sửa hoặc hủy đơn trước khi xử lý'),
        ('leave.request.approve', 'LEAVE_REQUEST', 'APPROVE', N'Duyệt hoặc từ chối đơn nghỉ phép', N'Cho phép quản lý duyệt hoặc từ chối đơn của team'),
        ('leave.request.finalize', 'LEAVE_REQUEST', 'FINALIZE', N'Chốt đơn nghỉ phép', N'Cho phép HR finalize đơn và trừ số dư phép'),
        ('leave.calendar.view_team', 'LEAVE_CALENDAR', 'VIEW_TEAM', N'Xem lịch nghỉ của team', N'Cho phép quản lý xem lịch nghỉ đã chốt trong phạm vi team'),
        ('leave.period.close', 'LEAVE_PERIOD', 'CLOSE', N'Khóa kỳ phép năm', N'Cho phép HR carry forward và khóa kỳ phép cũ'),
        ('leave.balance.settlement', 'LEAVE_BALANCE', 'SETTLEMENT', N'Tất toán phép khi nghỉ việc', N'Cho phép HR tất toán số dư phép cuối kỳ hoặc nghỉ việc'),
        ('leave.report.export', 'LEAVE_REPORT', 'EXPORT', N'Xuất báo cáo nghỉ phép', N'Cho phép HR export CSV báo cáo nghỉ phép'),
        ('onboarding.orientation.confirm', 'ONBOARDING', 'ORIENTATION_CONFIRM', N'Xác nhận orientation', N'Cho phép quản lý xác nhận đã hoàn tất orientation cho nhân sự mới'),
        ('onboarding.complete', 'ONBOARDING', 'COMPLETE', N'Hoàn tất onboarding', N'Cho phép HR chốt onboarding khi checklist và hợp đồng đầu tiên đã đạt điều kiện'),
        ('onboarding.notify', 'ONBOARDING', 'NOTIFY', N'Gửi thông báo onboarding', N'Cho phép HR gửi email thông báo kế hoạch onboarding')
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
        ('HR', 'leave.type.view'),
        ('HR', 'leave.type.create_update'),
        ('HR', 'leave.type.deactivate'),
        ('HR', 'leave.balance.adjust'),
        ('HR', 'leave.request.finalize'),
        ('HR', 'leave.period.close'),
        ('HR', 'leave.balance.settlement'),
        ('HR', 'leave.report.export'),
        ('HR', 'onboarding.complete'),
        ('HR', 'onboarding.notify'),

        ('MANAGER', 'leave.request.approve'),
        ('MANAGER', 'leave.calendar.view_team'),
        ('MANAGER', 'onboarding.orientation.confirm'),

        ('EMPLOYEE', 'leave.request.create'),
        ('EMPLOYEE', 'leave.request.update_cancel')
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
        ('HR', 'leave.type.view'),
        ('HR', 'leave.type.create_update'),
        ('HR', 'leave.type.deactivate'),
        ('HR', 'leave.balance.adjust'),
        ('HR', 'leave.request.finalize'),
        ('HR', 'leave.period.close'),
        ('HR', 'leave.balance.settlement'),
        ('HR', 'leave.report.export'),
        ('HR', 'onboarding.complete'),
        ('HR', 'onboarding.notify'),

        ('MANAGER', 'leave.request.approve'),
        ('MANAGER', 'leave.calendar.view_team'),
        ('MANAGER', 'onboarding.orientation.confirm'),

        ('EMPLOYEE', 'leave.request.create'),
        ('EMPLOYEE', 'leave.request.update_cancel')
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

MERGE dbo.sys_notification_template AS target
USING (
    SELECT
        'ONBOARDING_PLAN_NOTIFY' AS template_code,
        N'Thông báo kế hoạch onboarding' AS template_name,
        'EMAIL' AS channel_code,
        N'Digital HRM - Kế hoạch onboarding ${onboardingCode}' AS subject_template,
        N'Xin chào ${fullName},

Kế hoạch onboarding của bạn đã được cập nhật trên Digital HRM.

- Mã onboarding: ${onboardingCode}
- Ngày đi làm dự kiến: ${plannedStartDate}
- Phòng ban: ${orgUnitName}
- Chức danh: ${jobTitleName}
- Quản lý phụ trách: ${managerName}
- Địa điểm làm việc: ${workLocation}

Vui lòng kiểm tra lại checklist và liên hệ HR nếu cần hỗ trợ thêm.' AS body_template,
        'ACTIVE' AS status,
        N'Mẫu email dùng để gửi thông báo kế hoạch onboarding sprint 4' AS description
) AS source
ON target.template_code = source.template_code AND target.is_deleted = 0
WHEN MATCHED THEN
    UPDATE SET
        target.template_name = source.template_name,
        target.channel_code = source.channel_code,
        target.subject_template = source.subject_template,
        target.body_template = source.body_template,
        target.status = source.status,
        target.description = source.description,
        target.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (
        template_code, template_name, channel_code, subject_template, body_template, status, description, created_at, is_deleted
    )
    VALUES (
        source.template_code, source.template_name, source.channel_code, source.subject_template, source.body_template,
        source.status, source.description, SYSDATETIME(), 0
    );

SET NOCOUNT OFF;
