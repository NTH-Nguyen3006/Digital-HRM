SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__109__seed_manager_workspace.sql
   Scope:
   - Prepare stable manager demo scope for CI/CD environments
   - Seed direct reports for HCMC Sales manager
   - Seed leave / attendance / payroll data inside manager scope
   ========================================================= */

-- 1. Anchor manager scope for Sales HCMC
DECLARE @SalesDeptId BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_SALES_HCM');
DECLARE @ManagerEmp8 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP008');
DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');

IF @SalesDeptId IS NOT NULL AND @ManagerEmp8 IS NOT NULL
BEGIN
    UPDATE dbo.hr_org_unit
    SET manager_employee_id = @ManagerEmp8,
        updated_at = SYSDATETIME()
    WHERE org_unit_id = @SalesDeptId
      AND (manager_employee_id IS NULL OR manager_employee_id <> @ManagerEmp8);

    UPDATE dbo.hr_employee
    SET manager_employee_id = @ManagerEmp8,
        updated_at = SYSDATETIME()
    WHERE employee_id = @Emp3
      AND (manager_employee_id IS NULL OR manager_employee_id <> @ManagerEmp8);
END
GO

-- 2. Seed direct reports for manager EMP008
DECLARE @DeptSalesHcmId BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_SALES_HCM');
DECLARE @TitleExecSalesId BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'EXEC_SALES');
DECLARE @ManagerEmp8 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP008');

IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP010')
BEGIN
    INSERT INTO dbo.hr_employee (
        employee_code, org_unit_id, job_title_id, manager_employee_id,
        full_name, work_email, work_phone, gender_code, date_of_birth,
        hire_date, employment_status, work_location, personal_email, mobile_phone, note
    )
    VALUES (
        'EMP010', @DeptSalesHcmId, @TitleExecSalesId, @ManagerEmp8,
        N'Võ Minh An', 'an.vo@digitalhrm.com', '0911001001', 'FEMALE', '1999-09-17',
        '2025-08-05', 'ACTIVE', N'HCMC Office', 'vo.minh.an@gmail.com', '0911001001',
        N'Nhân sự kinh doanh seed cho manager workspace.'
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP011')
BEGIN
    INSERT INTO dbo.hr_employee (
        employee_code, org_unit_id, job_title_id, manager_employee_id,
        full_name, work_email, work_phone, gender_code, date_of_birth,
        hire_date, employment_status, work_location, personal_email, mobile_phone, note
    )
    VALUES (
        'EMP011', @DeptSalesHcmId, @TitleExecSalesId, @ManagerEmp8,
        N'Bùi Gia Huy', 'huy.bui@digitalhrm.com', '0911001002', 'MALE', '2000-01-23',
        '2025-11-12', 'ACTIVE', N'HCMC Office', 'bui.gia.huy@gmail.com', '0911001002',
        N'Nhân sự kinh doanh seed cho manager workspace.'
    );
END
GO

DECLARE @SalesDeptId BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_SALES_HCM');
DECLARE @ManagerEmp8 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP008');

UPDATE dbo.hr_employee
SET org_unit_id = @SalesDeptId,
    manager_employee_id = @ManagerEmp8,
    work_location = N'HCMC Office',
    updated_at = SYSDATETIME()
WHERE employee_code IN ('EMP010', 'EMP011')
  AND @SalesDeptId IS NOT NULL
  AND @ManagerEmp8 IS NOT NULL
  AND (
      org_unit_id <> @SalesDeptId
      OR manager_employee_id IS NULL
      OR manager_employee_id <> @ManagerEmp8
      OR ISNULL(work_location, '') <> N'HCMC Office'
  );
GO

DECLARE @Emp10 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP010');
DECLARE @Emp11 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP011');

IF @Emp10 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.hr_employee_profile WHERE employee_id = @Emp10)
BEGIN
    INSERT INTO dbo.hr_employee_profile (
        employee_id, first_name, last_name, marital_status, nationality, ethnic_group, education_level, major
    )
    VALUES (@Emp10, N'An', N'Võ Minh', 'SINGLE', N'Vietnam', N'Kinh', N'Bachelor', N'Business Administration');
END

IF @Emp11 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.hr_employee_profile WHERE employee_id = @Emp11)
BEGIN
    INSERT INTO dbo.hr_employee_profile (
        employee_id, first_name, last_name, marital_status, nationality, ethnic_group, education_level, major
    )
    VALUES (@Emp11, N'Huy', N'Bùi Gia', 'SINGLE', N'Vietnam', N'Kinh', N'Bachelor', N'Marketing');
END

IF @Emp10 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.hr_employee_address WHERE employee_id = @Emp10 AND address_type = 'CURRENT')
BEGIN
    INSERT INTO dbo.hr_employee_address (employee_id, address_type, address_line, province_name, is_primary)
    VALUES (@Emp10, 'CURRENT', N'12 Nguyễn Hữu Cảnh, Bình Thạnh', N'Hồ Chí Minh', 1);
END

IF @Emp11 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.hr_employee_address WHERE employee_id = @Emp11 AND address_type = 'CURRENT')
BEGIN
    INSERT INTO dbo.hr_employee_address (employee_id, address_type, address_line, province_name, is_primary)
    VALUES (@Emp11, 'CURRENT', N'88 Hoàng Văn Thụ, Phú Nhuận', N'Hồ Chí Minh', 1);
END

IF @Emp10 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.hr_employee_identification WHERE employee_id = @Emp10 AND document_type = 'CCCD')
BEGIN
    INSERT INTO dbo.hr_employee_identification (employee_id, document_type, document_number, status, is_primary)
    VALUES (@Emp10, 'CCCD', '079099001010', 'VALID', 1);
END

IF @Emp11 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.hr_employee_identification WHERE employee_id = @Emp11 AND document_type = 'CCCD')
BEGIN
    INSERT INTO dbo.hr_employee_identification (employee_id, document_type, document_number, status, is_primary)
    VALUES (@Emp11, 'CCCD', '079000001011', 'VALID', 1);
END

IF @Emp10 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.hr_employee_bank_account WHERE employee_id = @Emp10 AND is_primary = 1)
BEGIN
    INSERT INTO dbo.hr_employee_bank_account (employee_id, bank_name, account_number, account_holder_name, status, is_primary)
    VALUES (@Emp10, 'ACB', '250160101010', 'VO MINH AN', 'ACTIVE', 1);
END

IF @Emp11 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.hr_employee_bank_account WHERE employee_id = @Emp11 AND is_primary = 1)
BEGIN
    INSERT INTO dbo.hr_employee_bank_account (employee_id, bank_name, account_number, account_holder_name, status, is_primary)
    VALUES (@Emp11, 'Techcombank', '190366601011', 'BUI GIA HUY', 'ACTIVE', 1);
END
GO

-- 3. User accounts for seeded direct reports
DECLARE @DefaultPasswordHash VARCHAR(255) = '$2a$10$gaSEymcGFJ74WopEYuob2OLUqXy3IZuTdmv5vDDWVYzVcp7DTic32';

MERGE INTO dbo.sec_user_account AS TARGET
USING (
    VALUES
        ('EMP010', 'sales_an', 'an.vo@digitalhrm.com'),
        ('EMP011', 'sales_huy', 'huy.bui@digitalhrm.com')
) AS SOURCE (employee_code, username, email)
ON TARGET.employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = SOURCE.employee_code)
WHEN MATCHED THEN
    UPDATE SET
        TARGET.username = SOURCE.username,
        TARGET.email = SOURCE.email,
        TARGET.password_hash = @DefaultPasswordHash,
        TARGET.status = 'ACTIVE',
        TARGET.is_deleted = 0,
        TARGET.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (user_id, employee_id, username, password_hash, status, must_change_password, email, created_at, is_deleted)
    VALUES (
        NEWID(),
        (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = SOURCE.employee_code),
        SOURCE.username,
        @DefaultPasswordHash,
        'ACTIVE',
        0,
        SOURCE.email,
        SYSDATETIME(),
        0
    );

INSERT INTO dbo.sec_user_role (user_id, role_id, is_primary_role, status, created_at)
SELECT u.user_id, r.role_id, 1, 'ACTIVE', SYSDATETIME()
FROM dbo.sec_user_account u
INNER JOIN dbo.sec_role r ON r.role_code = 'EMPLOYEE'
WHERE u.username IN ('sales_an', 'sales_huy')
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.sec_user_role ur
      WHERE ur.user_id = u.user_id
        AND ur.role_id = r.role_id
  );
GO

-- 4. Contracts and compensation for manager team members
DECLARE @TypeINDEF BIGINT = (SELECT contract_type_id FROM dbo.ct_contract_type WHERE contract_type_code = 'INDEF');
DECLARE @AllowanceId BIGINT = (SELECT salary_component_id FROM dbo.pay_salary_component WHERE component_code = 'ALLOWANCE');
DECLARE @Emp10 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP010');
DECLARE @Emp11 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP011');

IF @Emp10 IS NOT NULL AND @TypeINDEF IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.ct_labor_contract WHERE contract_number = 'HDLD-EMP010-01')
BEGIN
    INSERT INTO dbo.ct_labor_contract (
        employee_id, contract_type_id, contract_number, sign_date, effective_date, end_date,
        job_title_id, org_unit_id, base_salary, salary_currency, working_type, contract_status
    )
    VALUES (
        @Emp10, @TypeINDEF, 'HDLD-EMP010-01', '2025-08-01', '2025-08-05', NULL,
        (SELECT job_title_id FROM dbo.hr_employee WHERE employee_id = @Emp10),
        (SELECT org_unit_id FROM dbo.hr_employee WHERE employee_id = @Emp10),
        18000000, 'VND', 'FULL_TIME', 'ACTIVE'
    );
END

IF @Emp11 IS NOT NULL AND @TypeINDEF IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.ct_labor_contract WHERE contract_number = 'HDLD-EMP011-01')
BEGIN
    INSERT INTO dbo.ct_labor_contract (
        employee_id, contract_type_id, contract_number, sign_date, effective_date, end_date,
        job_title_id, org_unit_id, base_salary, salary_currency, working_type, contract_status
    )
    VALUES (
        @Emp11, @TypeINDEF, 'HDLD-EMP011-01', '2025-11-08', '2025-11-12', NULL,
        (SELECT job_title_id FROM dbo.hr_employee WHERE employee_id = @Emp11),
        (SELECT org_unit_id FROM dbo.hr_employee WHERE employee_id = @Emp11),
        20000000, 'VND', 'FULL_TIME', 'ACTIVE'
    );
END

IF @Emp10 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.pay_employee_compensation WHERE employee_id = @Emp10 AND status = 'ACTIVE')
BEGIN
    INSERT INTO dbo.pay_employee_compensation (
        employee_id, effective_from, base_salary, insurance_salary_base, bank_account_name, bank_account_no, bank_name
    )
    VALUES (@Emp10, '2026-01-01', 18000000, 18000000, 'VO MINH AN', '250160101010', 'ACB');

    DECLARE @CompEmp10 BIGINT = SCOPE_IDENTITY();
    IF @AllowanceId IS NOT NULL
    BEGIN
        INSERT INTO dbo.pay_employee_compensation_item (employee_compensation_id, salary_component_id, amount_value)
        VALUES (@CompEmp10, @AllowanceId, 730000);
    END
END

IF @Emp11 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.pay_employee_compensation WHERE employee_id = @Emp11 AND status = 'ACTIVE')
BEGIN
    INSERT INTO dbo.pay_employee_compensation (
        employee_id, effective_from, base_salary, insurance_salary_base, bank_account_name, bank_account_no, bank_name
    )
    VALUES (@Emp11, '2026-01-01', 20000000, 20000000, 'BUI GIA HUY', '190366601011', 'Techcombank');

    DECLARE @CompEmp11 BIGINT = SCOPE_IDENTITY();
    IF @AllowanceId IS NOT NULL
    BEGIN
        INSERT INTO dbo.pay_employee_compensation_item (employee_compensation_id, salary_component_id, amount_value)
        VALUES (@CompEmp11, @AllowanceId, 730000);
    END
END
GO

-- 5. Leave balances and pending requests inside manager scope
DECLARE @ALTypeId BIGINT = (SELECT leave_type_id FROM dbo.lea_leave_type WHERE leave_type_code = 'AL');
DECLARE @ALRuleId BIGINT = (SELECT leave_type_rule_id FROM dbo.lea_leave_type_rule WHERE leave_type_id = @ALTypeId AND version_no = 1);
DECLARE @Emp10 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP010');
DECLARE @Emp11 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP011');

IF @Emp10 IS NOT NULL AND @ALTypeId IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.lea_leave_balance WHERE employee_id = @Emp10 AND leave_type_id = @ALTypeId AND leave_year = 2026)
BEGIN
    INSERT INTO dbo.lea_leave_balance (
        employee_id, leave_type_id, leave_year, opening_units, accrued_units, used_units, available_units, balance_status
    )
    VALUES (@Emp10, @ALTypeId, 2026, 0.0, 12.0, 1.0, 11.0, 'OPEN');
END

IF @Emp11 IS NOT NULL AND @ALTypeId IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.lea_leave_balance WHERE employee_id = @Emp11 AND leave_type_id = @ALTypeId AND leave_year = 2026)
BEGIN
    INSERT INTO dbo.lea_leave_balance (
        employee_id, leave_type_id, leave_year, opening_units, accrued_units, used_units, available_units, balance_status
    )
    VALUES (@Emp11, @ALTypeId, 2026, 0.0, 12.0, 0.0, 12.0, 'OPEN');
END

IF @Emp10 IS NOT NULL AND @ALTypeId IS NOT NULL AND @ALRuleId IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.lea_leave_request WHERE request_code = 'REQ-2026-AL-010')
BEGIN
    INSERT INTO dbo.lea_leave_request (
        request_code, employee_id, leave_type_id, leave_type_rule_id, leave_year,
        start_date, end_date, requested_units, reason, approval_role_code, request_status
    )
    VALUES (
        'REQ-2026-AL-010', @Emp10, @ALTypeId, @ALRuleId, 2026,
        '2026-04-14', '2026-04-14', 1.0, N'Nghỉ giải quyết việc cá nhân.', 'MANAGER', 'SUBMITTED'
    );
END

IF @Emp11 IS NOT NULL AND @ALTypeId IS NOT NULL AND @ALRuleId IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.lea_leave_request WHERE request_code = 'REQ-2026-AL-011')
BEGIN
    INSERT INTO dbo.lea_leave_request (
        request_code, employee_id, leave_type_id, leave_type_rule_id, leave_year,
        start_date, end_date, requested_units, reason, approval_role_code, request_status
    )
    VALUES (
        'REQ-2026-AL-011', @Emp11, @ALTypeId, @ALRuleId, 2026,
        '2026-04-16', '2026-04-16', 1.0, N'Nghỉ tham gia việc gia đình.', 'MANAGER', 'SUBMITTED'
    );
END
GO

-- 6. Attendance snapshot and manager approval workload
IF NOT EXISTS (SELECT 1 FROM dbo.att_attendance_period WHERE period_code = '2026-04')
BEGIN
    INSERT INTO dbo.att_attendance_period (
        period_code, period_year, period_month, period_start_date, period_end_date, period_status, total_employee_count
    )
    VALUES ('2026-04', 2026, 4, '2026-04-01', '2026-04-30', 'DRAFT', 7);
END
GO

DECLARE @AttPeriodApr BIGINT = (SELECT attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-04');
DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @Emp10 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP010');
DECLARE @Emp11 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP011');
DECLARE @ManagerUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');

UPDATE dbo.att_daily_attendance
SET on_leave = 1,
    updated_at = SYSDATETIME()
WHERE employee_id = @Emp3
  AND attendance_date = '2026-04-09'
  AND daily_status = 'ON_LEAVE'
  AND ISNULL(on_leave, 0) = 0;

IF @Emp10 IS NOT NULL AND @AttPeriodApr IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.att_daily_attendance WHERE employee_id = @Emp10 AND attendance_date = '2026-04-09')
BEGIN
    INSERT INTO dbo.att_attendance_log (employee_id, attendance_date, event_type, event_time, source_type)
    VALUES
        (@Emp10, '2026-04-09', 'CHECK_IN', '2026-04-09 08:07:00', 'WEB'),
        (@Emp10, '2026-04-09', 'CHECK_OUT', '2026-04-09 17:28:00', 'WEB');

    INSERT INTO dbo.att_daily_attendance (
        employee_id, attendance_date, attendance_period_id, actual_check_in_at, actual_check_out_at, worked_minutes, daily_status
    )
    VALUES (@Emp10, '2026-04-09', @AttPeriodApr, '2026-04-09 08:07:00', '2026-04-09 17:28:00', 501, 'PRESENT');
END

IF @Emp11 IS NOT NULL AND @AttPeriodApr IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.att_daily_attendance WHERE employee_id = @Emp11 AND attendance_date = '2026-04-09')
BEGIN
    INSERT INTO dbo.att_attendance_log (employee_id, attendance_date, event_type, event_time, source_type)
    VALUES
        (@Emp11, '2026-04-09', 'CHECK_IN', '2026-04-09 08:21:00', 'MOBILE_APP'),
        (@Emp11, '2026-04-09', 'CHECK_OUT', '2026-04-09 17:49:00', 'MOBILE_APP');

    INSERT INTO dbo.att_daily_attendance (
        employee_id, attendance_date, attendance_period_id, actual_check_in_at, actual_check_out_at,
        worked_minutes, late_minutes, anomaly_count, daily_status
    )
    VALUES (@Emp11, '2026-04-09', @AttPeriodApr, '2026-04-09 08:21:00', '2026-04-09 17:49:00', 508, 21, 1, 'PRESENT');
END

IF @Emp10 IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.att_adjustment_request WHERE request_code = 'ADJ-EMP010-APR09')
BEGIN
    INSERT INTO dbo.att_adjustment_request (
        request_code, employee_id, attendance_date, issue_type, proposed_check_in_at, reason, request_status
    )
    VALUES (
        'ADJ-EMP010-APR09', @Emp10, '2026-04-09', 'MISSING_CHECK_IN', '2026-04-09 08:03:00',
        N'Ứng dụng điện thoại bị treo lúc bắt đầu ngày làm việc.', 'SUBMITTED'
    );
END

IF @Emp11 IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.att_overtime_request WHERE request_code = 'OT-EMP011-APR09')
BEGIN
    INSERT INTO dbo.att_overtime_request (
        request_code, employee_id, attendance_date, overtime_start_at, overtime_end_at, requested_minutes, reason, request_status
    )
    VALUES (
        'OT-EMP011-APR09', @Emp11, '2026-04-09', '2026-04-09 18:15:00', '2026-04-09 20:15:00', 120,
        N'Chốt proposal và báo giá khách hàng khu vực HCM.', 'SUBMITTED'
    );
END

IF @Emp10 IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.att_adjustment_request WHERE request_code = 'ADJ-EMP010-APR08')
BEGIN
    INSERT INTO dbo.att_adjustment_request (
        request_code, employee_id, attendance_date, issue_type, proposed_check_out_at, reason, request_status,
        submitted_at, approved_at, approved_by, manager_note
    )
    VALUES (
        'ADJ-EMP010-APR08', @Emp10, '2026-04-08', 'MISSING_CHECK_OUT', '2026-04-08 17:32:00',
        N'Quên checkout sau buổi gặp khách hàng.', 'APPROVED',
        DATEADD(DAY, -1, SYSDATETIME()), DATEADD(HOUR, -15, SYSDATETIME()), @ManagerUser, N'Đã đối chiếu lịch công tác.'
    );
END
GO

-- 7. Payroll items in manager scope
DECLARE @ManagerUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');
DECLARE @HrUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @Emp10 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP010');
DECLARE @Emp11 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP011');
DECLARE @PayrollPeriodApr BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-04');
DECLARE @PayrollPeriodMay BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-05');

IF @PayrollPeriodApr IS NOT NULL AND @Emp10 IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodApr AND employee_id = @Emp10)
BEGIN
    INSERT INTO dbo.pay_payroll_item (
        payroll_period_id, employee_id, scheduled_day_count, present_day_count, approved_ot_minutes,
        base_salary_monthly, base_salary_prorated, fixed_earning_total, fixed_deduction_total,
        employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount,
        taxable_income, pit_amount, gross_income, net_pay, item_status
    )
    VALUES (
        @PayrollPeriodApr, @Emp10, 22, 21, 0,
        18000000, 17181818, 730000, 0,
        1890000, 11000000, 0,
        5021818, 251100, 17911818, 15770718, 'DRAFT'
    );
END

IF @PayrollPeriodApr IS NOT NULL AND @Emp11 IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodApr AND employee_id = @Emp11)
BEGIN
    INSERT INTO dbo.pay_payroll_item (
        payroll_period_id, employee_id, scheduled_day_count, present_day_count, approved_ot_minutes,
        base_salary_monthly, base_salary_prorated, fixed_earning_total, fixed_deduction_total,
        employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount,
        taxable_income, pit_amount, gross_income, net_pay, item_status,
        manager_confirmed_by, manager_confirmed_at, manager_confirm_note
    )
    VALUES (
        @PayrollPeriodApr, @Emp11, 22, 22, 60,
        20000000, 20000000, 730000, 0,
        2100000, 11000000, 0,
        7630000, 513500, 20730000, 18116500, 'MANAGER_CONFIRMED',
        @ManagerUser, DATEADD(HOUR, -9, SYSDATETIME()), N'Đã xác nhận số liệu chấm công và KPI team.'
    );
END

IF @PayrollPeriodMay IS NOT NULL AND @Emp10 IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodMay AND employee_id = @Emp10)
BEGIN
    INSERT INTO dbo.pay_payroll_item (
        payroll_period_id, employee_id, scheduled_day_count, present_day_count,
        base_salary_monthly, base_salary_prorated, fixed_earning_total, fixed_deduction_total,
        employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount,
        taxable_income, pit_amount, gross_income, net_pay, item_status,
        manager_confirmed_by, manager_confirmed_at, manager_confirm_note,
        hr_approved_by, hr_approved_at, published_by, published_at
    )
    VALUES (
        @PayrollPeriodMay, @Emp10, 22, 22,
        18000000, 18000000, 730000, 0,
        1890000, 11000000, 0,
        5840000, 367000, 18730000, 16473000, 'PUBLISHED',
        @ManagerUser, DATEADD(DAY, -2, SYSDATETIME()), N'Đã xác nhận doanh số tháng 05.',
        @HrUser, DATEADD(DAY, -1, SYSDATETIME()), @HrUser, DATEADD(DAY, -1, SYSDATETIME())
    );
END

IF @PayrollPeriodMay IS NOT NULL AND @Emp11 IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodMay AND employee_id = @Emp11)
BEGIN
    INSERT INTO dbo.pay_payroll_item (
        payroll_period_id, employee_id, scheduled_day_count, present_day_count, approved_ot_minutes,
        base_salary_monthly, base_salary_prorated, fixed_earning_total, fixed_deduction_total,
        employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount,
        taxable_income, pit_amount, gross_income, net_pay, item_status,
        manager_confirmed_by, manager_confirmed_at, manager_confirm_note,
        hr_approved_by, hr_approved_at, published_by, published_at
    )
    VALUES (
        @PayrollPeriodMay, @Emp11, 22, 22, 120,
        20000000, 20000000, 730000, 0,
        2100000, 11000000, 0,
        7630000, 513500, 20730000, 18116500, 'PUBLISHED',
        @ManagerUser, DATEADD(DAY, -2, SYSDATETIME()), N'Đã xác nhận kết quả xử lý khách hàng tháng 05.',
        @HrUser, DATEADD(DAY, -1, SYSDATETIME()), @HrUser, DATEADD(DAY, -1, SYSDATETIME())
    );
END

IF @PayrollPeriodApr IS NOT NULL
BEGIN
    UPDATE dbo.pay_payroll_period
    SET total_employee_count = agg.total_employee_count,
        manager_confirmed_count = agg.manager_confirmed_count,
        total_gross_amount = agg.total_gross_amount,
        total_net_amount = agg.total_net_amount,
        note = N'Kỳ lương team review có dữ liệu mẫu cho manager workspace.'
    FROM dbo.pay_payroll_period pp
    CROSS APPLY (
        SELECT
            COUNT(1) AS total_employee_count,
            SUM(CASE WHEN manager_confirmed_at IS NOT NULL THEN 1 ELSE 0 END) AS manager_confirmed_count,
            SUM(gross_income) AS total_gross_amount,
            SUM(net_pay) AS total_net_amount
        FROM dbo.pay_payroll_item
        WHERE payroll_period_id = @PayrollPeriodApr
          AND is_deleted = 0
    ) agg
    WHERE pp.payroll_period_id = @PayrollPeriodApr;
END

IF @PayrollPeriodMay IS NOT NULL
BEGIN
    UPDATE dbo.pay_payroll_period
    SET total_employee_count = agg.total_employee_count,
        manager_confirmed_count = agg.manager_confirmed_count,
        total_gross_amount = agg.total_gross_amount,
        total_net_amount = agg.total_net_amount,
        note = N'Kỳ lương published có dữ liệu lịch sử cho manager workspace.'
    FROM dbo.pay_payroll_period pp
    CROSS APPLY (
        SELECT
            COUNT(1) AS total_employee_count,
            SUM(CASE WHEN manager_confirmed_at IS NOT NULL THEN 1 ELSE 0 END) AS manager_confirmed_count,
            SUM(gross_income) AS total_gross_amount,
            SUM(net_pay) AS total_net_amount
        FROM dbo.pay_payroll_item
        WHERE payroll_period_id = @PayrollPeriodMay
          AND is_deleted = 0
    ) agg
    WHERE pp.payroll_period_id = @PayrollPeriodMay;
END
GO

SET NOCOUNT OFF;
GO
