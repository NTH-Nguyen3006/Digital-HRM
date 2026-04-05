SET NOCOUNT ON;

/* =========================================================
   R__sprint8_reporting_governance_permissions.sql
   Scope:
   - reporting permissions
   - governance permissions
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
        ('report.dashboard.headcount.view', 'REPORT_DASHBOARD', 'VIEW_HEADCOUNT', N'Xem dashboard headcount', N'Cho phép HR xem dashboard headcount toàn hệ thống'),
        ('report.dashboard.team.view', 'REPORT_DASHBOARD', 'VIEW_TEAM', N'Xem dashboard đội nhóm', N'Cho phép manager xem dashboard đội nhóm trong scope quản lý'),
        ('report.org_movement.export', 'REPORT_EXPORT', 'EXPORT_ORG_MOVEMENT', N'Xuất báo cáo biến động tổ chức', N'Cho phép HR xuất báo cáo biến động tổ chức'),
        ('report.contract_expiry.export', 'REPORT_EXPORT', 'EXPORT_CONTRACT_EXPIRY', N'Xuất báo cáo hợp đồng sắp hết hạn', N'Cho phép HR xuất báo cáo hợp đồng sắp hết hạn'),
        ('report.leave_balance.export', 'REPORT_EXPORT', 'EXPORT_LEAVE_BALANCE', N'Xuất báo cáo tồn phép', N'Cho phép HR xuất báo cáo tồn phép'),
        ('report.attendance_anomaly.export', 'REPORT_EXPORT', 'EXPORT_ATTENDANCE_ANOMALY', N'Xuất báo cáo bất thường công và OT', N'Cho phép HR xuất báo cáo bất thường công và OT'),
        ('report.payroll_summary.export', 'REPORT_EXPORT', 'EXPORT_PAYROLL_SUMMARY', N'Xuất báo cáo lương tổng hợp', N'Cho phép HR xuất báo cáo lương tổng hợp'),
        ('report.pit.export', 'REPORT_EXPORT', 'EXPORT_PIT', N'Xuất báo cáo thuế TNCN', N'Cho phép HR xuất báo cáo thuế TNCN'),
        ('report.onboarding_offboarding.export', 'REPORT_EXPORT', 'EXPORT_ONBOARDING_OFFBOARDING', N'Xuất báo cáo onboarding/offboarding', N'Cho phép HR xuất báo cáo onboarding/offboarding'),
        ('report.audit.export', 'REPORT_GOVERNANCE', 'EXPORT_AUDIT', N'Xuất audit log', N'Cho phép Admin xuất audit log'),
        ('report.system_health.view', 'REPORT_GOVERNANCE', 'VIEW_SYSTEM_HEALTH', N'Xem dashboard sức khỏe hệ thống HRM', N'Cho phép Admin xem dashboard sức khỏe hệ thống'),
        ('report.schedule.manage', 'REPORT_GOVERNANCE', 'MANAGE_SCHEDULE', N'Cấu hình xuất báo cáo định kỳ', N'Cho phép Admin cấu hình và chạy lịch xuất báo cáo định kỳ')
) s(permission_code, module_code, action_code, permission_name, description)
ON p.permission_code = s.permission_code
WHERE p.is_deleted = 0;

INSERT INTO dbo.sec_permission (
    permission_code, module_code, action_code, permission_name, description, status, created_at, is_deleted
)
SELECT
    s.permission_code, s.module_code, s.action_code, s.permission_name, s.description, 'ACTIVE', SYSDATETIME(), 0
FROM (
    VALUES
        ('report.dashboard.headcount.view', 'REPORT_DASHBOARD', 'VIEW_HEADCOUNT', N'Xem dashboard headcount', N'Cho phép HR xem dashboard headcount toàn hệ thống'),
        ('report.dashboard.team.view', 'REPORT_DASHBOARD', 'VIEW_TEAM', N'Xem dashboard đội nhóm', N'Cho phép manager xem dashboard đội nhóm trong scope quản lý'),
        ('report.org_movement.export', 'REPORT_EXPORT', 'EXPORT_ORG_MOVEMENT', N'Xuất báo cáo biến động tổ chức', N'Cho phép HR xuất báo cáo biến động tổ chức'),
        ('report.contract_expiry.export', 'REPORT_EXPORT', 'EXPORT_CONTRACT_EXPIRY', N'Xuất báo cáo hợp đồng sắp hết hạn', N'Cho phép HR xuất báo cáo hợp đồng sắp hết hạn'),
        ('report.leave_balance.export', 'REPORT_EXPORT', 'EXPORT_LEAVE_BALANCE', N'Xuất báo cáo tồn phép', N'Cho phép HR xuất báo cáo tồn phép'),
        ('report.attendance_anomaly.export', 'REPORT_EXPORT', 'EXPORT_ATTENDANCE_ANOMALY', N'Xuất báo cáo bất thường công và OT', N'Cho phép HR xuất báo cáo bất thường công và OT'),
        ('report.payroll_summary.export', 'REPORT_EXPORT', 'EXPORT_PAYROLL_SUMMARY', N'Xuất báo cáo lương tổng hợp', N'Cho phép HR xuất báo cáo lương tổng hợp'),
        ('report.pit.export', 'REPORT_EXPORT', 'EXPORT_PIT', N'Xuất báo cáo thuế TNCN', N'Cho phép HR xuất báo cáo thuế TNCN'),
        ('report.onboarding_offboarding.export', 'REPORT_EXPORT', 'EXPORT_ONBOARDING_OFFBOARDING', N'Xuất báo cáo onboarding/offboarding', N'Cho phép HR xuất báo cáo onboarding/offboarding'),
        ('report.audit.export', 'REPORT_GOVERNANCE', 'EXPORT_AUDIT', N'Xuất audit log', N'Cho phép Admin xuất audit log'),
        ('report.system_health.view', 'REPORT_GOVERNANCE', 'VIEW_SYSTEM_HEALTH', N'Xem dashboard sức khỏe hệ thống HRM', N'Cho phép Admin xem dashboard sức khỏe hệ thống'),
        ('report.schedule.manage', 'REPORT_GOVERNANCE', 'MANAGE_SCHEDULE', N'Cấu hình xuất báo cáo định kỳ', N'Cho phép Admin cấu hình và chạy lịch xuất báo cáo định kỳ')
) s(permission_code, module_code, action_code, permission_name, description)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_permission p
    WHERE p.permission_code = s.permission_code AND p.is_deleted = 0
);

;WITH role_permission_seed(role_code, permission_code) AS (
    SELECT * FROM (VALUES
        ('HR', 'report.dashboard.headcount.view'),
        ('HR', 'report.org_movement.export'),
        ('HR', 'report.contract_expiry.export'),
        ('HR', 'report.leave_balance.export'),
        ('HR', 'report.attendance_anomaly.export'),
        ('HR', 'report.payroll_summary.export'),
        ('HR', 'report.pit.export'),
        ('HR', 'report.onboarding_offboarding.export'),

        ('MANAGER', 'report.dashboard.team.view'),

        ('ADMIN', 'report.audit.export'),
        ('ADMIN', 'report.system_health.view'),
        ('ADMIN', 'report.schedule.manage')
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
        ('HR', 'report.dashboard.headcount.view'),
        ('HR', 'report.org_movement.export'),
        ('HR', 'report.contract_expiry.export'),
        ('HR', 'report.leave_balance.export'),
        ('HR', 'report.attendance_anomaly.export'),
        ('HR', 'report.payroll_summary.export'),
        ('HR', 'report.pit.export'),
        ('HR', 'report.onboarding_offboarding.export'),

        ('MANAGER', 'report.dashboard.team.view'),

        ('ADMIN', 'report.audit.export'),
        ('ADMIN', 'report.system_health.view'),
        ('ADMIN', 'report.schedule.manage')
    ) v(role_code, permission_code)
)
INSERT INTO dbo.sec_role_permission (role_id, permission_id, is_allowed, created_at)
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
