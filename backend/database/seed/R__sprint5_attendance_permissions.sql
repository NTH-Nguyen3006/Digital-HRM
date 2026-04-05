SET NOCOUNT ON;

/* =========================================================
   R__sprint5_attendance_permissions.sql
   Scope:
   - attendance permission seed
   - attendance platform settings seed
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
        ('attendance.shift.view', 'ATTENDANCE_SHIFT', 'VIEW', N'Xem danh mục ca làm việc', N'Cho phép xem danh mục và chi tiết ca làm việc'),
        ('attendance.shift.create_update', 'ATTENDANCE_SHIFT', 'CREATE_UPDATE', N'Tạo cập nhật ca làm việc', N'Cho phép tạo mới hoặc version hóa ca làm việc'),
        ('attendance.shift.assign', 'ATTENDANCE_SHIFT_ASSIGNMENT', 'ASSIGN', N'Gán ca cho nhân viên', N'Cho phép gán ca làm việc theo khoảng hiệu lực cho nhân viên'),
        ('attendance.log.create', 'ATTENDANCE_LOG', 'CREATE', N'Ghi nhận check-in check-out', N'Cho phép nhân viên ghi nhận log chấm công tự phục vụ'),
        ('attendance.daily.view', 'ATTENDANCE_DAILY', 'VIEW', N'Xem bảng công ngày', N'Cho phép HR xem bảng công ngày và drill-down log gốc'),
        ('attendance.adjust.request', 'ATTENDANCE_ADJUSTMENT', 'REQUEST', N'Tạo yêu cầu điều chỉnh công', N'Cho phép nhân viên tạo yêu cầu điều chỉnh công'),
        ('attendance.adjust.update_cancel', 'ATTENDANCE_ADJUSTMENT', 'UPDATE_CANCEL', N'Sửa hoặc hủy yêu cầu điều chỉnh công', N'Cho phép nhân viên sửa hoặc hủy yêu cầu điều chỉnh công trước khi chốt'),
        ('attendance.adjust.approve', 'ATTENDANCE_ADJUSTMENT', 'APPROVE', N'Duyệt điều chỉnh công', N'Cho phép manager duyệt hoặc từ chối yêu cầu điều chỉnh công'),
        ('attendance.adjust.finalize', 'ATTENDANCE_ADJUSTMENT', 'FINALIZE', N'Chốt điều chỉnh công', N'Cho phép HR chốt điều chỉnh công và cập nhật bảng công'),
        ('attendance.ot.request', 'ATTENDANCE_OT', 'REQUEST', N'Tạo yêu cầu làm thêm giờ', N'Cho phép nhân viên tạo yêu cầu OT'),
        ('attendance.ot.approve', 'ATTENDANCE_OT', 'APPROVE', N'Duyệt yêu cầu làm thêm giờ', N'Cho phép manager duyệt hoặc từ chối OT'),
        ('attendance.period.close', 'ATTENDANCE_PERIOD', 'CLOSE', N'Tính và chốt kỳ công', N'Cho phép HR tính bảng công tháng và chốt kỳ công'),
        ('attendance.period.reopen', 'ATTENDANCE_PERIOD', 'REOPEN', N'Mở lại kỳ công', N'Cho phép HR mở lại kỳ công đã chốt'),
        ('attendance.anomaly.export', 'ATTENDANCE_REPORT', 'EXPORT', N'Xuất báo cáo bất thường chấm công', N'Cho phép HR export báo cáo bất thường chấm công')
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
        ('attendance.shift.view', 'ATTENDANCE_SHIFT', 'VIEW', N'Xem danh mục ca làm việc', N'Cho phép xem danh mục và chi tiết ca làm việc'),
        ('attendance.shift.create_update', 'ATTENDANCE_SHIFT', 'CREATE_UPDATE', N'Tạo cập nhật ca làm việc', N'Cho phép tạo mới hoặc version hóa ca làm việc'),
        ('attendance.shift.assign', 'ATTENDANCE_SHIFT_ASSIGNMENT', 'ASSIGN', N'Gán ca cho nhân viên', N'Cho phép gán ca làm việc theo khoảng hiệu lực cho nhân viên'),
        ('attendance.log.create', 'ATTENDANCE_LOG', 'CREATE', N'Ghi nhận check-in check-out', N'Cho phép nhân viên ghi nhận log chấm công tự phục vụ'),
        ('attendance.daily.view', 'ATTENDANCE_DAILY', 'VIEW', N'Xem bảng công ngày', N'Cho phép HR xem bảng công ngày và drill-down log gốc'),
        ('attendance.adjust.request', 'ATTENDANCE_ADJUSTMENT', 'REQUEST', N'Tạo yêu cầu điều chỉnh công', N'Cho phép nhân viên tạo yêu cầu điều chỉnh công'),
        ('attendance.adjust.update_cancel', 'ATTENDANCE_ADJUSTMENT', 'UPDATE_CANCEL', N'Sửa hoặc hủy yêu cầu điều chỉnh công', N'Cho phép nhân viên sửa hoặc hủy yêu cầu điều chỉnh công trước khi chốt'),
        ('attendance.adjust.approve', 'ATTENDANCE_ADJUSTMENT', 'APPROVE', N'Duyệt điều chỉnh công', N'Cho phép manager duyệt hoặc từ chối yêu cầu điều chỉnh công'),
        ('attendance.adjust.finalize', 'ATTENDANCE_ADJUSTMENT', 'FINALIZE', N'Chốt điều chỉnh công', N'Cho phép HR chốt điều chỉnh công và cập nhật bảng công'),
        ('attendance.ot.request', 'ATTENDANCE_OT', 'REQUEST', N'Tạo yêu cầu làm thêm giờ', N'Cho phép nhân viên tạo yêu cầu OT'),
        ('attendance.ot.approve', 'ATTENDANCE_OT', 'APPROVE', N'Duyệt yêu cầu làm thêm giờ', N'Cho phép manager duyệt hoặc từ chối OT'),
        ('attendance.period.close', 'ATTENDANCE_PERIOD', 'CLOSE', N'Tính và chốt kỳ công', N'Cho phép HR tính bảng công tháng và chốt kỳ công'),
        ('attendance.period.reopen', 'ATTENDANCE_PERIOD', 'REOPEN', N'Mở lại kỳ công', N'Cho phép HR mở lại kỳ công đã chốt'),
        ('attendance.anomaly.export', 'ATTENDANCE_REPORT', 'EXPORT', N'Xuất báo cáo bất thường chấm công', N'Cho phép HR export báo cáo bất thường chấm công')
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
        ('HR', 'attendance.shift.view'),
        ('HR', 'attendance.shift.create_update'),
        ('HR', 'attendance.shift.assign'),
        ('HR', 'attendance.daily.view'),
        ('HR', 'attendance.adjust.finalize'),
        ('HR', 'attendance.period.close'),
        ('HR', 'attendance.period.reopen'),
        ('HR', 'attendance.anomaly.export'),

        ('MANAGER', 'attendance.adjust.approve'),
        ('MANAGER', 'attendance.ot.approve'),

        ('EMPLOYEE', 'attendance.log.create'),
        ('EMPLOYEE', 'attendance.adjust.request'),
        ('EMPLOYEE', 'attendance.adjust.update_cancel'),
        ('EMPLOYEE', 'attendance.ot.request')
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
        ('HR', 'attendance.shift.view'),
        ('HR', 'attendance.shift.create_update'),
        ('HR', 'attendance.shift.assign'),
        ('HR', 'attendance.daily.view'),
        ('HR', 'attendance.adjust.finalize'),
        ('HR', 'attendance.period.close'),
        ('HR', 'attendance.period.reopen'),
        ('HR', 'attendance.anomaly.export'),

        ('MANAGER', 'attendance.adjust.approve'),
        ('MANAGER', 'attendance.ot.approve'),

        ('EMPLOYEE', 'attendance.log.create'),
        ('EMPLOYEE', 'attendance.adjust.request'),
        ('EMPLOYEE', 'attendance.adjust.update_cancel'),
        ('EMPLOYEE', 'attendance.ot.request')
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

MERGE dbo.sys_platform_setting AS target
USING (
    SELECT 'attendance.ot.max_minutes_per_day' AS setting_key, N'Giới hạn OT theo ngày' AS setting_name, N'240' AS setting_value, 'NUMBER' AS value_type, 1 AS is_editable, 'ACTIVE' AS status, N'Ngưỡng OT theo ngày do hệ thống attendance sử dụng khi manager duyệt.' AS description
    UNION ALL
    SELECT 'attendance.ot.max_minutes_per_month', N'Giới hạn OT theo tháng', N'1200', 'NUMBER', 1, 'ACTIVE', N'Ngưỡng OT theo tháng do hệ thống attendance sử dụng khi manager duyệt.'
) AS source
ON target.setting_key = source.setting_key AND target.is_deleted = 0
WHEN MATCHED THEN
    UPDATE SET
        target.setting_name = source.setting_name,
        target.setting_value = source.setting_value,
        target.value_type = source.value_type,
        target.is_editable = source.is_editable,
        target.status = source.status,
        target.description = source.description,
        target.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (
        setting_key, setting_name, setting_value, value_type, is_editable, status, description, created_at, is_deleted
    )
    VALUES (
        source.setting_key, source.setting_name, source.setting_value, source.value_type, source.is_editable, source.status, source.description, SYSDATETIME(), 0
    );

SET NOCOUNT OFF;
