SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__105__seed_leave.sql
   Scope:
   - Seed leave types and rules
   - Seed leave balances for 2026
   - Seed sample leave requests
   ========================================================= */

-- 1. Leave Type
IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_type WHERE leave_type_code = 'AL')
BEGIN
    INSERT INTO dbo.lea_leave_type (leave_type_code, leave_type_name, sort_order, is_system, status)
    VALUES ('AL', N'Nghỉ phép năm', 1, 1, 'ACTIVE');
END

IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_type WHERE leave_type_code = 'UPL')
BEGIN
    INSERT INTO dbo.lea_leave_type (leave_type_code, leave_type_name, sort_order, is_system, status)
    VALUES ('UPL', N'Nghỉ không lương', 2, 0, 'ACTIVE');
END
GO

-- 2. Leave Type Rules
DECLARE @ALTypeId BIGINT = (SELECT leave_type_id FROM dbo.lea_leave_type WHERE leave_type_code = 'AL');
DECLARE @UPLTypeId BIGINT = (SELECT leave_type_id FROM dbo.lea_leave_type WHERE leave_type_code = 'UPL');

IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_type_rule WHERE leave_type_id = @ALTypeId AND version_no = 1)
BEGIN
    INSERT INTO dbo.lea_leave_type_rule (leave_type_id, version_no, effective_from, unit_type, default_entitlement_units, carry_forward_max_units, is_paid, approval_role_code, requires_balance_check)
    VALUES (@ALTypeId, 1, '2020-01-01', 'DAY', 12.0, 5.0, 1, 'MANAGER', 1);
END

IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_type_rule WHERE leave_type_id = @UPLTypeId AND version_no = 1)
BEGIN
    INSERT INTO dbo.lea_leave_type_rule (leave_type_id, version_no, effective_from, unit_type, default_entitlement_units, carry_forward_max_units, is_paid, approval_role_code, requires_balance_check)
    VALUES (@UPLTypeId, 1, '2020-01-01', 'DAY', 0.0, 0.0, 0, 'MANAGER', 0);
END
GO

-- 3. Leave Balances for 2026
DECLARE @ALTypeId BIGINT = (SELECT leave_type_id FROM dbo.lea_leave_type WHERE leave_type_code = 'AL');

DECLARE @E1 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP001');
DECLARE @E2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');
DECLARE @E3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @E4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @E5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');

IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_balance WHERE employee_id = @E1 AND leave_type_id = @ALTypeId AND leave_year = 2026)
BEGIN
    INSERT INTO dbo.lea_leave_balance (employee_id, leave_type_id, leave_year, opening_units, accrued_units, used_units, available_units, balance_status)
    VALUES (@E1, @ALTypeId, 2026, 0.0, 12.0, 2.0, 10.0, 'OPEN');
END

IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_balance WHERE employee_id = @E2 AND leave_type_id = @ALTypeId AND leave_year = 2026)
BEGIN
    INSERT INTO dbo.lea_leave_balance (employee_id, leave_type_id, leave_year, opening_units, accrued_units, used_units, available_units, balance_status)
    VALUES (@E2, @ALTypeId, 2026, 0.0, 12.0, 0.0, 12.0, 'OPEN');
END

IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_balance WHERE employee_id = @E3 AND leave_type_id = @ALTypeId AND leave_year = 2026)
BEGIN
    INSERT INTO dbo.lea_leave_balance (employee_id, leave_type_id, leave_year, opening_units, accrued_units, used_units, available_units, balance_status)
    VALUES (@E3, @ALTypeId, 2026, 0.0, 12.0, 1.0, 11.0, 'OPEN');
END

IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_balance WHERE employee_id = @E4 AND leave_type_id = @ALTypeId AND leave_year = 2026)
BEGIN
    INSERT INTO dbo.lea_leave_balance (employee_id, leave_type_id, leave_year, opening_units, accrued_units, used_units, available_units, balance_status)
    VALUES (@E4, @ALTypeId, 2026, 0.0, 12.0, 0.0, 12.0, 'OPEN');
END

IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_balance WHERE employee_id = @E5 AND leave_type_id = @ALTypeId AND leave_year = 2026)
BEGIN
    INSERT INTO dbo.lea_leave_balance (employee_id, leave_type_id, leave_year, opening_units, accrued_units, used_units, available_units, balance_status)
    VALUES (@E5, @ALTypeId, 2026, 0.0, 12.0, 0.0, 12.0, 'OPEN');
END
GO

-- 4. Seed some sample Leave Requests
DECLARE @ALTypeId BIGINT = (SELECT leave_type_id FROM dbo.lea_leave_type WHERE leave_type_code = 'AL');
DECLARE @ALRuleId BIGINT = (SELECT leave_type_rule_id FROM dbo.lea_leave_type_rule WHERE leave_type_id = @ALTypeId AND version_no = 1);
DECLARE @E1 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP001');
DECLARE @E3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @U1 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');

-- A completed leave request for EMP001
IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_request WHERE request_code = 'REQ-2026-AL-001')
BEGIN
    INSERT INTO dbo.lea_leave_request (request_code, employee_id, leave_type_id, leave_type_rule_id, leave_year, start_date, end_date, requested_units, reason, approval_role_code, request_status, approved_by, approved_at)
    VALUES ('REQ-2026-AL-001', @E1, @ALTypeId, @ALRuleId, 2026, '2026-03-01', '2026-03-02', 2.0, N'Nghỉ phép cá nhân', 'MANAGER', 'APPROVED', @U1, SYSDATETIME());
END

-- A pending leave request for EMP003
IF NOT EXISTS (SELECT 1 FROM dbo.lea_leave_request WHERE request_code = 'REQ-2026-AL-002')
BEGIN
    INSERT INTO dbo.lea_leave_request (request_code, employee_id, leave_type_id, leave_type_rule_id, leave_year, start_date, end_date, requested_units, reason, approval_role_code, request_status)
    VALUES ('REQ-2026-AL-002', @E3, @ALTypeId, @ALRuleId, 2026, '2026-05-15', '2026-05-15', 1.0, N'Nghỉ đi du lịch gia đình', 'MANAGER', 'SUBMITTED');
END
GO
