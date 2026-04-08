SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__106__seed_attendance.sql
   Scope:
   - Seed shifts and assignments
   - Seed attendance periods
   - Seed daily attendance and logs
   - Seed overtime requests
   ========================================================= */

-- 1. Shifts & Versions
IF NOT EXISTS (SELECT 1 FROM dbo.att_shift WHERE shift_code = 'OFFICE')
BEGIN
    INSERT INTO dbo.att_shift (shift_code, shift_name, status)
    VALUES ('OFFICE', N'Ca Hành Chính', 'ACTIVE');
    
    DECLARE @ShiftId BIGINT = SCOPE_IDENTITY();
    
    INSERT INTO dbo.att_shift_version (shift_id, version_no, effective_from, start_time, end_time, break_minutes, late_grace_minutes, early_leave_grace_minutes, ot_allowed, status)
    VALUES (@ShiftId, 1, '2020-01-01', '08:00', '17:00', 60, 5, 5, 1, 'ACTIVE');
END
GO

-- 2. Shift Assignments
DECLARE @OfficeShift BIGINT = (SELECT shift_id FROM dbo.att_shift WHERE shift_code = 'OFFICE');

DECLARE @E1 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP001');
DECLARE @E2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');
DECLARE @E3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @E4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @E5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');

IF NOT EXISTS (SELECT 1 FROM dbo.att_shift_assignment WHERE employee_id = @E1 AND shift_id = @OfficeShift AND effective_from = '2026-01-01')
   AND @OfficeShift IS NOT NULL
BEGIN
    INSERT INTO dbo.att_shift_assignment (employee_id, shift_id, effective_from) VALUES (@E1, @OfficeShift, '2026-01-01');
    INSERT INTO dbo.att_shift_assignment (employee_id, shift_id, effective_from) VALUES (@E2, @OfficeShift, '2026-01-01');
    INSERT INTO dbo.att_shift_assignment (employee_id, shift_id, effective_from) VALUES (@E3, @OfficeShift, '2026-01-01');
    INSERT INTO dbo.att_shift_assignment (employee_id, shift_id, effective_from) VALUES (@E4, @OfficeShift, '2026-01-01');
    INSERT INTO dbo.att_shift_assignment (employee_id, shift_id, effective_from) VALUES (@E5, @OfficeShift, '2026-01-01');
END
GO

-- 3. Attendance Period (e.g. May 2026)
IF NOT EXISTS (SELECT 1 FROM dbo.att_attendance_period WHERE period_code = '2026-05')
BEGIN
    INSERT INTO dbo.att_attendance_period (period_code, period_year, period_month, period_start_date, period_end_date, period_status, total_employee_count)
    VALUES ('2026-05', 2026, 5, '2026-05-01', '2026-05-31', 'DRAFT', 5);
END
GO

-- 4. Daily Attendance & Logs
DECLARE @AttPeriod BIGINT = (SELECT attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-05');
DECLARE @E2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');

IF NOT EXISTS (SELECT 1 FROM dbo.att_daily_attendance WHERE employee_id = @E2 AND attendance_date = '2026-05-10')
   AND @AttPeriod IS NOT NULL
BEGIN
    -- Log check-in / out
    INSERT INTO dbo.att_attendance_log (employee_id, attendance_date, event_type, event_time, source_type)
    VALUES (@E2, '2026-05-10', 'CHECK_IN', '2026-05-10 07:55:00', 'WEB');
    
    INSERT INTO dbo.att_attendance_log (employee_id, attendance_date, event_type, event_time, source_type)
    VALUES (@E2, '2026-05-10', 'CHECK_OUT', '2026-05-10 18:30:00', 'WEB');
    
    -- Daily record
    INSERT INTO dbo.att_daily_attendance (employee_id, attendance_date, attendance_period_id, actual_check_in_at, actual_check_out_at, worked_minutes, daily_status)
    VALUES (@E2, '2026-05-10', @AttPeriod, '2026-05-10 07:55:00', '2026-05-10 18:30:00', 480, 'PRESENT');
END

-- Missing Check-out
IF NOT EXISTS (SELECT 1 FROM dbo.att_daily_attendance WHERE employee_id = @E2 AND attendance_date = '2026-05-11')
BEGIN
    INSERT INTO dbo.att_attendance_log (employee_id, attendance_date, event_type, event_time, source_type)
    VALUES (@E2, '2026-05-11', 'CHECK_IN', '2026-05-11 08:15:00', 'MOBILE_APP');
    
    INSERT INTO dbo.att_daily_attendance (employee_id, attendance_date, attendance_period_id, actual_check_in_at, late_minutes, missing_check_out, anomaly_count, daily_status)
    VALUES (@E2, '2026-05-11', @AttPeriod, '2026-05-11 08:15:00', 15, 1, 1, 'INCOMPLETE');
    
    -- Adjustment Request for the missing checkout
    INSERT INTO dbo.att_adjustment_request (request_code, employee_id, attendance_date, issue_type, proposed_check_out_at, reason, request_status)
    VALUES ('ADJ-EMP002-001', @E2, '2026-05-11', 'MISSING_CHECK_OUT', '2026-05-11 17:05:00', N'Quên chấm công lúc về', 'SUBMITTED');
END
GO

-- 5. Overtime Request
DECLARE @E5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');
IF NOT EXISTS (SELECT 1 FROM dbo.att_overtime_request WHERE request_code = 'OT-2026-05-01')
   AND @E5 IS NOT NULL
BEGIN
    INSERT INTO dbo.att_overtime_request (request_code, employee_id, attendance_date, overtime_start_at, overtime_end_at, requested_minutes, reason, request_status)
    VALUES ('OT-2026-05-01', @E5, '2026-05-12', '2026-05-12 17:00:00', '2026-05-12 20:00:00', 180, N'Làm thêm dự án OT', 'SUBMITTED');
END
GO
