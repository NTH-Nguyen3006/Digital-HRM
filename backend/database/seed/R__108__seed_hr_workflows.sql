SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__108__seed_hr_workflows.sql
   Scope:
   - Seed current-date attendance snapshot for HR dashboard/pages
   - Seed additional payroll items for HR payroll workspace
   - Seed onboarding/offboarding workflow data
   - Seed one expiring contract for dashboard alert visibility
   ========================================================= */

-- 0. Common ids
DECLARE @AdminUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');
DECLARE @HrUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');
DECLARE @UserMgrC UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'mgr_c');
DECLARE @UserAccD UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'acc_d');
DECLARE @UserDevE UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'dev_e');
DECLARE @UserHa UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'ha_ln');

DECLARE @Emp2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');
DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @Emp4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @Emp5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');
DECLARE @Emp6 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP006');
DECLARE @Emp8 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP008');
DECLARE @Emp9 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP009');
GO

-- 1. Make one contract expire soon so HR dashboard has alert data
UPDATE dbo.ct_labor_contract
SET end_date = '2026-04-25',
    updated_at = SYSDATETIME(),
    updated_by = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd')
WHERE contract_number = 'HDLD-EMP005-01'
  AND is_deleted = 0
  AND contract_status = 'ACTIVE'
  AND (end_date IS NULL OR end_date <> '2026-04-25');
GO

-- 2. Current attendance snapshot for 2026-04-09
IF NOT EXISTS (SELECT 1 FROM dbo.att_attendance_period WHERE period_code = '2026-04')
BEGIN
    INSERT INTO dbo.att_attendance_period (
        period_code, period_year, period_month, period_start_date, period_end_date, period_status, total_employee_count
    )
    VALUES ('2026-04', 2026, 4, '2026-04-01', '2026-04-30', 'DRAFT', 5);
END
GO

DECLARE @AttPeriodApr BIGINT = (SELECT attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-04');
DECLARE @HrUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');
DECLARE @Emp1 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP001');
DECLARE @Emp2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');
DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @Emp4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @Emp5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');

IF NOT EXISTS (SELECT 1 FROM dbo.att_daily_attendance WHERE employee_id = @Emp1 AND attendance_date = '2026-04-09')
BEGIN
    INSERT INTO dbo.att_attendance_log (employee_id, attendance_date, event_type, event_time, source_type)
    VALUES
        (@Emp1, '2026-04-09', 'CHECK_IN',  '2026-04-09 08:02:00', 'WEB'),
        (@Emp1, '2026-04-09', 'CHECK_OUT', '2026-04-09 17:31:00', 'WEB');

    INSERT INTO dbo.att_daily_attendance (
        employee_id, attendance_date, attendance_period_id, actual_check_in_at, actual_check_out_at, worked_minutes, daily_status
    )
    VALUES (@Emp1, '2026-04-09', @AttPeriodApr, '2026-04-09 08:02:00', '2026-04-09 17:31:00', 509, 'PRESENT');
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_daily_attendance WHERE employee_id = @Emp2 AND attendance_date = '2026-04-09')
BEGIN
    INSERT INTO dbo.att_attendance_log (employee_id, attendance_date, event_type, event_time, source_type)
    VALUES
        (@Emp2, '2026-04-09', 'CHECK_IN',  '2026-04-09 08:18:00', 'MOBILE_APP'),
        (@Emp2, '2026-04-09', 'CHECK_OUT', '2026-04-09 17:42:00', 'MOBILE_APP');

    INSERT INTO dbo.att_daily_attendance (
        employee_id, attendance_date, attendance_period_id, actual_check_in_at, actual_check_out_at,
        worked_minutes, late_minutes, anomaly_count, daily_status
    )
    VALUES (@Emp2, '2026-04-09', @AttPeriodApr, '2026-04-09 08:18:00', '2026-04-09 17:42:00', 504, 18, 1, 'PRESENT');
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_daily_attendance WHERE employee_id = @Emp3 AND attendance_date = '2026-04-09')
BEGIN
    INSERT INTO dbo.att_daily_attendance (
        employee_id, attendance_date, attendance_period_id, worked_minutes, daily_status
    )
    VALUES (@Emp3, '2026-04-09', @AttPeriodApr, 0, 'ON_LEAVE');
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_daily_attendance WHERE employee_id = @Emp4 AND attendance_date = '2026-04-09')
BEGIN
    INSERT INTO dbo.att_daily_attendance (
        employee_id, attendance_date, attendance_period_id, worked_minutes, anomaly_count, daily_status
    )
    VALUES (@Emp4, '2026-04-09', @AttPeriodApr, 0, 1, 'ABSENT');
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_daily_attendance WHERE employee_id = @Emp5 AND attendance_date = '2026-04-09')
BEGIN
    INSERT INTO dbo.att_attendance_log (employee_id, attendance_date, event_type, event_time, source_type)
    VALUES (@Emp5, '2026-04-09', 'CHECK_IN', '2026-04-09 08:06:00', 'WEB');

    INSERT INTO dbo.att_daily_attendance (
        employee_id, attendance_date, attendance_period_id, actual_check_in_at, worked_minutes,
        missing_check_out, anomaly_count, daily_status
    )
    VALUES (@Emp5, '2026-04-09', @AttPeriodApr, '2026-04-09 08:06:00', 0, 1, 1, 'INCOMPLETE');
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_adjustment_request WHERE request_code = 'ADJ-EMP005-APR09')
BEGIN
    INSERT INTO dbo.att_adjustment_request (
        request_code, employee_id, attendance_date, issue_type, proposed_check_out_at, reason, request_status
    )
    VALUES (
        'ADJ-EMP005-APR09', @Emp5, '2026-04-09', 'MISSING_CHECK_OUT', '2026-04-09 17:15:00',
        N'Quên chấm công khi kết thúc ngày làm việc.', 'SUBMITTED'
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_adjustment_request WHERE request_code = 'ADJ-EMP002-APR08')
BEGIN
    INSERT INTO dbo.att_adjustment_request (
        request_code, employee_id, attendance_date, issue_type, proposed_check_in_at, reason, request_status,
        submitted_at, approved_at, approved_by, manager_note
    )
    VALUES (
        'ADJ-EMP002-APR08', @Emp2, '2026-04-08', 'MISSING_CHECK_IN', '2026-04-08 08:01:00',
        N'Bị lỗi mạng khi check-in trên mobile app.', 'APPROVED',
        DATEADD(DAY, -1, SYSDATETIME()), DATEADD(HOUR, -18, SYSDATETIME()), @ManagerUser, N'Quản lý đã kiểm tra camera ra vào.'
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_adjustment_request WHERE request_code = 'ADJ-EMP004-APR07')
BEGIN
    INSERT INTO dbo.att_adjustment_request (
        request_code, employee_id, attendance_date, issue_type, proposed_check_out_at, reason, request_status,
        submitted_at, rejected_at, rejected_by, rejection_note
    )
    VALUES (
        'ADJ-EMP004-APR07', @Emp4, '2026-04-07', 'MISSING_CHECK_OUT', '2026-04-07 17:10:00',
        N'Quên checkout do họp gấp cuối ngày.', 'REJECTED',
        DATEADD(DAY, -2, SYSDATETIME()), DATEADD(DAY, -1, SYSDATETIME()), @HrUser, N'Không đủ bằng chứng đối chiếu để chốt công.'
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_adjustment_request WHERE request_code = 'ADJ-EMP001-APR06')
BEGIN
    INSERT INTO dbo.att_adjustment_request (
        request_code, employee_id, attendance_date, issue_type, proposed_check_in_at, proposed_check_out_at,
        reason, request_status, submitted_at, approved_at, approved_by, finalized_at, finalized_by, finalize_note
    )
    VALUES (
        'ADJ-EMP001-APR06', @Emp1, '2026-04-06', 'MISSING_BOTH', '2026-04-06 08:00:00', '2026-04-06 17:05:00',
        N'Hệ thống kiosk bảo trì nên không ghi nhận log.', 'FINALIZED',
        DATEADD(DAY, -3, SYSDATETIME()), DATEADD(DAY, -3, SYSDATETIME()), @ManagerUser,
        DATEADD(DAY, -2, SYSDATETIME()), @HrUser, N'Đã tạo synthetic logs và chốt lại bảng công.'
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_overtime_request WHERE request_code = 'OT-EMP002-APR09')
BEGIN
    INSERT INTO dbo.att_overtime_request (
        request_code, employee_id, attendance_date, overtime_start_at, overtime_end_at, requested_minutes, reason, request_status
    )
    VALUES (
        'OT-EMP002-APR09', @Emp2, '2026-04-09', '2026-04-09 18:00:00', '2026-04-09 20:00:00', 120,
        N'Hoàn tất hạng mục release sprint 4.', 'SUBMITTED'
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_overtime_request WHERE request_code = 'OT-EMP003-APR08')
BEGIN
    INSERT INTO dbo.att_overtime_request (
        request_code, employee_id, attendance_date, overtime_start_at, overtime_end_at, requested_minutes,
        reason, request_status, submitted_at, approved_at, approved_by, manager_note
    )
    VALUES (
        'OT-EMP003-APR08', @Emp3, '2026-04-08', '2026-04-08 17:30:00', '2026-04-08 19:00:00', 90,
        N'Hoàn tất báo giá khách hàng chiến lược.', 'APPROVED',
        DATEADD(DAY, -1, SYSDATETIME()), DATEADD(HOUR, -16, SYSDATETIME()), @ManagerUser, N'OT hợp lệ theo kế hoạch team.'
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.att_overtime_request WHERE request_code = 'OT-EMP004-APR07')
BEGIN
    INSERT INTO dbo.att_overtime_request (
        request_code, employee_id, attendance_date, overtime_start_at, overtime_end_at, requested_minutes,
        reason, request_status, submitted_at, rejected_at, rejected_by, rejection_note
    )
    VALUES (
        'OT-EMP004-APR07', @Emp4, '2026-04-07', '2026-04-07 17:15:00', '2026-04-07 19:45:00', 150,
        N'Tự xử lý backlog cá nhân cuối ngày.', 'REJECTED',
        DATEADD(DAY, -2, SYSDATETIME()), DATEADD(DAY, -1, SYSDATETIME()), @ManagerUser, N'OT không nằm trong kế hoạch đã duyệt.'
    );
END
GO

-- 3. Additional payroll items for HR payroll list/detail
DECLARE @PayrollPeriodId BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-05');
DECLARE @HrUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');

DECLARE @Emp2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');
DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @Emp4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @Emp5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');

IF @PayrollPeriodId IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @Emp2
)
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
        @PayrollPeriodId, @Emp2, 22, 22, 120,
        35000000, 35000000, 730000, 0,
        3675000, 11000000, 0,
        21055000, 2110000, 35730000, 29945000, 'PUBLISHED',
        @ManagerUser, SYSDATETIME(), N'Đã xác nhận KPI sprint.',
        @HrUser, SYSDATETIME(), @HrUser, SYSDATETIME()
    );
END

IF @PayrollPeriodId IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @Emp3
)
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
        @PayrollPeriodId, @Emp3, 22, 21,
        40000000, 38181818, 1500000, 0,
        4200000, 11000000, 0,
        24481818, 2965000, 39681818, 32516818, 'PUBLISHED',
        @ManagerUser, DATEADD(DAY, -2, SYSDATETIME()), N'Xác nhận đủ chỉ tiêu kinh doanh.',
        @HrUser, DATEADD(DAY, -1, SYSDATETIME()), @HrUser, SYSDATETIME()
    );
END

IF @PayrollPeriodId IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @Emp4
)
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
        @PayrollPeriodId, @Emp4, 22, 20,
        25000000, 22727273, 730000, 0,
        2625000, 11000000, 0,
        9832273, 483000, 23457273, 20349273, 'PUBLISHED',
        @ManagerUser, DATEADD(DAY, -1, SYSDATETIME()), N'Đã soát xét bảng công và quyết toán.',
        @HrUser, SYSDATETIME(), @HrUser, SYSDATETIME()
    );
END

IF @PayrollPeriodId IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @Emp5
)
BEGIN
    INSERT INTO dbo.pay_payroll_item (
        payroll_period_id, employee_id, scheduled_day_count, present_day_count, unpaid_leave_day_count,
        base_salary_monthly, base_salary_prorated, fixed_earning_total, fixed_deduction_total,
        employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount,
        taxable_income, pit_amount, gross_income, net_pay, item_status,
        manager_confirmed_by, manager_confirmed_at, manager_confirm_note
    )
    VALUES (
        @PayrollPeriodId, @Emp5, 22, 20, 1,
        15000000, 13636364, 730000, 0,
        1575000, 11000000, 0,
        1791364, 89600, 14366364, 12701764, 'MANAGER_CONFIRMED',
        @ManagerUser, SYSDATETIME(), N'Đã xác nhận bảng công tháng 05.'
    );
END

IF @PayrollPeriodId IS NOT NULL
BEGIN
    UPDATE dbo.pay_payroll_period
    SET total_employee_count = agg.total_employee_count,
        manager_confirmed_count = agg.manager_confirmed_count,
        total_gross_amount = agg.total_gross_amount,
        total_net_amount = agg.total_net_amount,
        note = N'Kỳ lương mẫu phục vụ kiểm thử dashboard HR.'
    FROM dbo.pay_payroll_period pp
    CROSS APPLY (
        SELECT
            COUNT(1) AS total_employee_count,
            SUM(CASE WHEN manager_confirmed_at IS NOT NULL THEN 1 ELSE 0 END) AS manager_confirmed_count,
            SUM(gross_income) AS total_gross_amount,
            SUM(net_pay) AS total_net_amount
        FROM dbo.pay_payroll_item
        WHERE payroll_period_id = @PayrollPeriodId
          AND is_deleted = 0
    ) agg
    WHERE pp.payroll_period_id = @PayrollPeriodId;
END
GO

-- 3b. One team-review payroll period so UI has pre-publish states
DECLARE @AttPeriodApr BIGINT = (SELECT attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-04');
DECLARE @FormulaId BIGINT = (SELECT TOP 1 formula_version_id FROM dbo.pay_formula_version WHERE formula_code = 'STD_2026');

IF NOT EXISTS (SELECT 1 FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-04')
   AND @AttPeriodApr IS NOT NULL
   AND @FormulaId IS NOT NULL
BEGIN
    INSERT INTO dbo.pay_payroll_period (
        period_code, period_year, period_month, period_start_date, period_end_date,
        attendance_period_id, formula_version_id, period_status, total_employee_count,
        manager_confirmed_count, total_gross_amount, total_net_amount, note
    )
    VALUES (
        'PR-2026-04', 2026, 4, '2026-04-01', '2026-04-30',
        @AttPeriodApr, @FormulaId, 'TEAM_REVIEW', 3,
        1, 86960030, 72291113, N'Kỳ lương đang ở bước rà soát trước khi HR phê duyệt.'
    );
END
GO

DECLARE @PayrollPeriodApr BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-04');
DECLARE @HrUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');
DECLARE @Emp1 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP001');
DECLARE @Emp2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');
DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');

IF @PayrollPeriodApr IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodApr AND employee_id = @Emp1
)
BEGIN
    INSERT INTO dbo.pay_payroll_item (
        payroll_period_id, employee_id, scheduled_day_count, present_day_count,
        base_salary_monthly, base_salary_prorated, fixed_earning_total, fixed_deduction_total,
        employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount,
        taxable_income, pit_amount, gross_income, net_pay, item_status
    )
    VALUES (
        @PayrollPeriodApr, @Emp1, 22, 22,
        30000000, 30000000, 730000, 0,
        3150000, 11000000, 0,
        16580000, 1240000, 30730000, 25580000, 'DRAFT'
    );
END

IF @PayrollPeriodApr IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodApr AND employee_id = @Emp2
)
BEGIN
    INSERT INTO dbo.pay_payroll_item (
        payroll_period_id, employee_id, scheduled_day_count, present_day_count, approved_ot_minutes,
        base_salary_monthly, base_salary_prorated, fixed_earning_total, fixed_deduction_total,
        employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount,
        taxable_income, pit_amount, gross_income, net_pay, item_status,
        manager_confirmed_by, manager_confirmed_at, manager_confirm_note
    )
    VALUES (
        @PayrollPeriodApr, @Emp2, 22, 21, 90,
        28000000, 26727273, 730000, 0,
        2940000, 11000000, 0,
        13517273, 790000, 27457273, 23727273, 'MANAGER_CONFIRMED',
        @ManagerUser, DATEADD(HOUR, -10, SYSDATETIME()), N'Đã xác nhận OT và ngày công thực tế.'
    );
END

IF @PayrollPeriodApr IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodApr AND employee_id = @Emp3
)
BEGIN
    INSERT INTO dbo.pay_payroll_item (
        payroll_period_id, employee_id, scheduled_day_count, present_day_count, paid_leave_day_count,
        base_salary_monthly, base_salary_prorated, fixed_earning_total, fixed_deduction_total,
        employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount,
        taxable_income, pit_amount, gross_income, net_pay, item_status,
        manager_confirmed_by, manager_confirmed_at, manager_confirm_note,
        hr_approved_by, hr_approved_at
    )
    VALUES (
        @PayrollPeriodApr, @Emp3, 22, 20, 1,
        30000000, 27272727, 1500000, 0,
        3150000, 11000000, 0,
        17622727, 1513840, 28772727, 22983840, 'HR_APPROVED',
        @ManagerUser, DATEADD(DAY, -1, SYSDATETIME()), N'Quản lý đã rà soát doanh số.',
        @HrUser, DATEADD(HOUR, -6, SYSDATETIME())
    );
END

IF @PayrollPeriodApr IS NOT NULL
BEGIN
    UPDATE dbo.pay_payroll_period
    SET total_employee_count = agg.total_employee_count,
        manager_confirmed_count = agg.manager_confirmed_count,
        total_gross_amount = agg.total_gross_amount,
        total_net_amount = agg.total_net_amount
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
GO

DECLARE @PayrollItemAprEmp1 BIGINT = (
    SELECT payroll_item_id FROM dbo.pay_payroll_item
    WHERE payroll_period_id = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-04')
      AND employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP001')
);
DECLARE @PayrollItemAprEmp2 BIGINT = (
    SELECT payroll_item_id FROM dbo.pay_payroll_item
    WHERE payroll_period_id = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-04')
      AND employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002')
);
DECLARE @PayrollItemAprEmp3 BIGINT = (
    SELECT payroll_item_id FROM dbo.pay_payroll_item
    WHERE payroll_period_id = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-04')
      AND employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003')
);
DECLARE @PayrollPeriodId BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-05');
DECLARE @Emp2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');
DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @Emp4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @Emp5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');

IF @PayrollItemAprEmp1 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item_line WHERE payroll_item_id = @PayrollItemAprEmp1 AND component_code = 'BASE_SALARY'
)
BEGIN
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note)
    VALUES
        (@PayrollItemAprEmp1, 'BASE_SALARY', N'Lương cơ bản', 'EARNING', 'SYSTEM', 30000000, 1, 10, N'Kỳ lương nháp tháng 04/2026.'),
        (@PayrollItemAprEmp1, 'ALLOWANCE', N'Phụ cấp ăn trưa', 'EARNING', 'CONFIGURED', 730000, 0, 20, N'Khoản cố định hàng tháng.'),
        (@PayrollItemAprEmp1, 'INSURANCE', N'BHXH, BHYT, BHTN', 'DEDUCTION', 'SYSTEM', 3150000, 0, 30, N'Khấu trừ bảo hiểm bắt buộc.'),
        (@PayrollItemAprEmp1, 'PIT', N'Thuế TNCN', 'DEDUCTION', 'SYSTEM', 1240000, 0, 40, N'Tạm khấu trừ PIT.');
END

IF @PayrollItemAprEmp2 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item_line WHERE payroll_item_id = @PayrollItemAprEmp2 AND component_code = 'BASE_SALARY'
)
BEGIN
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note)
    VALUES
        (@PayrollItemAprEmp2, 'BASE_SALARY', N'Lương cơ bản', 'EARNING', 'SYSTEM', 26727273, 1, 10, N'Lương prorate theo ngày công thực tế.'),
        (@PayrollItemAprEmp2, 'ALLOWANCE', N'Phụ cấp ăn trưa', 'EARNING', 'CONFIGURED', 730000, 0, 20, N'Khoản cố định hàng tháng.'),
        (@PayrollItemAprEmp2, 'OT_PAYMENT', N'Tiền OT đã duyệt', 'EARNING', 'SYSTEM', 0, 1, 30, N'Đã ghi nhận 90 phút OT chờ HR rà soát.'),
        (@PayrollItemAprEmp2, 'INSURANCE', N'BHXH, BHYT, BHTN', 'DEDUCTION', 'SYSTEM', 2940000, 0, 40, N'Khấu trừ bảo hiểm bắt buộc.'),
        (@PayrollItemAprEmp2, 'PIT', N'Thuế TNCN', 'DEDUCTION', 'SYSTEM', 790000, 0, 50, N'Tạm khấu trừ PIT.');
END

IF @PayrollItemAprEmp3 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item_line WHERE payroll_item_id = @PayrollItemAprEmp3 AND component_code = 'BASE_SALARY'
)
BEGIN
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note)
    VALUES
        (@PayrollItemAprEmp3, 'BASE_SALARY', N'Lương cơ bản', 'EARNING', 'SYSTEM', 27272727, 1, 10, N'Lương prorate theo ngày công và phép năm.'),
        (@PayrollItemAprEmp3, 'ALLOWANCE', N'Phụ cấp nhiệm vụ', 'EARNING', 'CONFIGURED', 1500000, 1, 20, N'Khoản phụ cấp theo vai trò.'),
        (@PayrollItemAprEmp3, 'INSURANCE', N'BHXH, BHYT, BHTN', 'DEDUCTION', 'SYSTEM', 3150000, 0, 30, N'Khấu trừ bảo hiểm bắt buộc.'),
        (@PayrollItemAprEmp3, 'PIT', N'Thuế TNCN', 'DEDUCTION', 'SYSTEM', 1513840, 0, 40, N'Tạm khấu trừ PIT sau giảm trừ.');
END
GO

DECLARE @PayrollPeriodId BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-05');
DECLARE @Emp2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');
DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @Emp4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @Emp5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');
DECLARE @PayrollItemEmp2 BIGINT = (
    SELECT payroll_item_id FROM dbo.pay_payroll_item
    WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @Emp2
);
DECLARE @PayrollItemEmp3 BIGINT = (
    SELECT payroll_item_id FROM dbo.pay_payroll_item
    WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @Emp3
);
DECLARE @PayrollItemEmp4 BIGINT = (
    SELECT payroll_item_id FROM dbo.pay_payroll_item
    WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @Emp4
);
DECLARE @PayrollItemEmp5 BIGINT = (
    SELECT payroll_item_id FROM dbo.pay_payroll_item
    WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @Emp5
);

IF @PayrollItemEmp2 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item_line WHERE payroll_item_id = @PayrollItemEmp2 AND component_code = 'BASE_SALARY'
)
BEGIN
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note)
    VALUES
        (@PayrollItemEmp2, 'BASE_SALARY', N'Lương cơ bản', 'EARNING', 'SYSTEM', 35000000, 1, 10, N'Theo cấu trúc lương tháng 05/2026.'),
        (@PayrollItemEmp2, 'ALLOWANCE', N'Phụ cấp ăn trưa', 'EARNING', 'CONFIGURED', 730000, 0, 20, N'Phụ cấp cố định hàng tháng.'),
        (@PayrollItemEmp2, 'OT_PAYMENT', N'Tiền OT đã duyệt', 'EARNING', 'SYSTEM', 0, 1, 30, N'Đã quy đổi từ 120 phút OT được phê duyệt.'),
        (@PayrollItemEmp2, 'INSURANCE', N'BHXH, BHYT, BHTN', 'DEDUCTION', 'SYSTEM', 3675000, 0, 40, N'Khấu trừ bảo hiểm bắt buộc.'),
        (@PayrollItemEmp2, 'PIT', N'Thuế TNCN', 'DEDUCTION', 'SYSTEM', 2110000, 0, 50, N'Tạm khấu trừ PIT theo kỳ.');
END

IF @PayrollItemEmp3 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item_line WHERE payroll_item_id = @PayrollItemEmp3 AND component_code = 'BASE_SALARY'
)
BEGIN
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note)
    VALUES
        (@PayrollItemEmp3, 'BASE_SALARY', N'Lương cơ bản quy đổi', 'EARNING', 'SYSTEM', 38181818, 1, 10, N'Quy đổi theo ngày công hiện diện.'),
        (@PayrollItemEmp3, 'SALES_ALLOWANCE', N'Phụ cấp kinh doanh', 'EARNING', 'CONFIGURED', 1500000, 1, 20, N'Áp dụng theo chính sách sales team.'),
        (@PayrollItemEmp3, 'INSURANCE', N'BHXH, BHYT, BHTN', 'DEDUCTION', 'SYSTEM', 4200000, 0, 40, N'Khấu trừ bảo hiểm bắt buộc.'),
        (@PayrollItemEmp3, 'PIT', N'Thuế TNCN', 'DEDUCTION', 'SYSTEM', 2965000, 0, 50, N'Tạm khấu trừ PIT theo kỳ.');
END

IF @PayrollItemEmp4 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item_line WHERE payroll_item_id = @PayrollItemEmp4 AND component_code = 'BASE_SALARY'
)
BEGIN
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note)
    VALUES
        (@PayrollItemEmp4, 'BASE_SALARY', N'Lương cơ bản quy đổi', 'EARNING', 'SYSTEM', 22727273, 1, 10, N'Khấu trừ theo số ngày vắng mặt trong kỳ.'),
        (@PayrollItemEmp4, 'ALLOWANCE', N'Phụ cấp cố định', 'EARNING', 'CONFIGURED', 730000, 0, 20, N'Phụ cấp duy trì vị trí.'),
        (@PayrollItemEmp4, 'INSURANCE', N'BHXH, BHYT, BHTN', 'DEDUCTION', 'SYSTEM', 2625000, 0, 40, N'Khấu trừ bảo hiểm bắt buộc.'),
        (@PayrollItemEmp4, 'PIT', N'Thuế TNCN', 'DEDUCTION', 'SYSTEM', 483000, 0, 50, N'Tạm khấu trừ PIT theo kỳ.');
END

IF @PayrollItemEmp5 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM dbo.pay_payroll_item_line WHERE payroll_item_id = @PayrollItemEmp5 AND component_code = 'BASE_SALARY'
)
BEGIN
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note)
    VALUES
        (@PayrollItemEmp5, 'BASE_SALARY', N'Lương cơ bản quy đổi', 'EARNING', 'SYSTEM', 13636364, 1, 10, N'Giảm theo 1 ngày nghỉ không lương.'),
        (@PayrollItemEmp5, 'ALLOWANCE', N'Phụ cấp ăn trưa', 'EARNING', 'CONFIGURED', 730000, 0, 20, N'Phụ cấp cố định hàng tháng.'),
        (@PayrollItemEmp5, 'INSURANCE', N'BHXH, BHYT, BHTN', 'DEDUCTION', 'SYSTEM', 1575000, 0, 40, N'Khấu trừ bảo hiểm bắt buộc.'),
        (@PayrollItemEmp5, 'PIT', N'Thuế TNCN', 'DEDUCTION', 'SYSTEM', 89600, 0, 50, N'Tạm khấu trừ PIT theo kỳ.');
END
GO

-- 4. Onboarding workflow data
DECLARE @DeptITHn BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_IT_HN');
DECLARE @DeptSalesHcm BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_SALES_HCM');
DECLARE @DeptFinHcm BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_FIN_HCM');

DECLARE @TitleJrDev BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'JDR_DEV');
DECLARE @TitleSrDev BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'SDR_DEV');
DECLARE @TitleSrAcc BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'SSR_ACC');

DECLARE @HrUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');
DECLARE @LinkedUserHa UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'ha_ln');
DECLARE @Emp6 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP006');
DECLARE @Emp8 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP008');
DECLARE @Emp9 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP009');

IF NOT EXISTS (SELECT 1 FROM dbo.onb_onboarding WHERE onboarding_code = 'ONB-2026-001')
BEGIN
    INSERT INTO dbo.onb_onboarding (
        onboarding_code, full_name, personal_email, personal_phone, gender_code, date_of_birth,
        planned_start_date, employee_code, org_unit_id, job_title_id, manager_employee_id,
        work_location, status, note, created_by
    )
    VALUES (
        'ONB-2026-001', N'Ngô Minh Khang', 'khang.ngo@example.com', '0911002200', 'MALE', '2001-10-12',
        '2026-04-15', 'CAND001', @DeptITHn, @TitleJrDev, @Emp6,
        N'Hanoi Office', 'DRAFT', N'Hồ sơ mới chờ HR rà soát.', @HrUser
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.onb_onboarding WHERE onboarding_code = 'ONB-2026-002')
BEGIN
    INSERT INTO dbo.onb_onboarding (
        onboarding_code, full_name, personal_email, personal_phone, gender_code, date_of_birth,
        planned_start_date, employee_code, org_unit_id, job_title_id, manager_employee_id,
        work_location, status, note, created_by
    )
    VALUES (
        'ONB-2026-002', N'Phạm Gia Hân', 'han.pham@example.com', '0911553344', 'FEMALE', '1999-03-18',
        '2026-04-18', 'CAND002', @DeptITHn, @TitleSrDev, @Emp6,
        N'Hanoi Office', 'IN_PROGRESS', N'Đang hoàn thiện checklist tiếp nhận.', @HrUser
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.onb_onboarding WHERE onboarding_code = 'ONB-2026-003')
BEGIN
    INSERT INTO dbo.onb_onboarding (
        onboarding_code, full_name, personal_email, personal_phone, gender_code, date_of_birth,
        planned_start_date, employee_code, org_unit_id, job_title_id, manager_employee_id,
        work_location, status, note, orientation_confirmed_at, orientation_confirmed_by, orientation_note, created_by
    )
    VALUES (
        'ONB-2026-003', N'Lương Bảo Trân', 'tran.luong@example.com', '0911667788', 'FEMALE', '1997-06-24',
        '2026-04-12', 'CAND003', @DeptSalesHcm, @TitleSrAcc, @Emp8,
        N'HCMC Office', 'READY_FOR_JOIN', N'Đã đủ điều kiện tạo tài khoản và hợp đồng.',
        DATEADD(DAY, -1, SYSDATETIME()), @ManagerUser, N'Manager đã xác nhận orientation.', @HrUser
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.onb_onboarding WHERE onboarding_code = 'ONB-2026-004')
BEGIN
    INSERT INTO dbo.onb_onboarding (
        onboarding_code, full_name, personal_email, personal_phone, gender_code, date_of_birth,
        planned_start_date, employee_code, org_unit_id, job_title_id, manager_employee_id,
        work_location, linked_employee_id, linked_user_id, status, note,
        orientation_confirmed_at, orientation_confirmed_by, orientation_note,
        completed_at, completed_by, completed_note, created_by
    )
    VALUES (
        'ONB-2026-004', N'Lê Ngọc Hà', 'ngochaln030608@gmail.com', '0909888777', 'FEMALE', '1998-04-04',
        '2026-03-20', 'EMP009', @DeptFinHcm, @TitleSrAcc, @Emp8,
        N'HCMC Office', @Emp9, @LinkedUserHa, 'COMPLETED', N'Onboarding đã hoàn tất và bàn giao đầy đủ.',
        DATEADD(DAY, -20, SYSDATETIME()), @ManagerUser, N'Đã hoàn thành orientation tại team.',
        DATEADD(DAY, -15, SYSDATETIME()), @HrUser, N'HR đã xác nhận hoàn tất onboarding.', @HrUser
    );
END
GO

DECLARE @Onb1 BIGINT = (SELECT onboarding_id FROM dbo.onb_onboarding WHERE onboarding_code = 'ONB-2026-001');
DECLARE @Onb2 BIGINT = (SELECT onboarding_id FROM dbo.onb_onboarding WHERE onboarding_code = 'ONB-2026-002');
DECLARE @Onb3 BIGINT = (SELECT onboarding_id FROM dbo.onb_onboarding WHERE onboarding_code = 'ONB-2026-003');
DECLARE @Onb4 BIGINT = (SELECT onboarding_id FROM dbo.onb_onboarding WHERE onboarding_code = 'ONB-2026-004');
DECLARE @HrUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');

IF NOT EXISTS (SELECT 1 FROM dbo.onb_onboarding_checklist WHERE onboarding_id = @Onb2 AND item_code = 'DOC_COLLECTION')
BEGIN
    INSERT INTO dbo.onb_onboarding_checklist (onboarding_id, item_code, item_name, is_required, is_completed, due_date, note, created_by)
    VALUES
        (@Onb2, 'DOC_COLLECTION', N'Thu đủ hồ sơ đầu vào', 1, 1, '2026-04-14', N'Ứng viên đã nộp đủ hồ sơ cá nhân.', @HrUser),
        (@Onb2, 'ACCOUNT_SETUP', N'Tạo email và tài khoản nội bộ', 1, 0, '2026-04-16', N'Chờ IT tạo tài khoản.', @HrUser),
        (@Onb2, 'WORKSTATION', N'Chuẩn bị máy tính làm việc', 1, 0, '2026-04-16', N'Đang chờ kho IT cấp laptop.', @HrUser);
END

IF NOT EXISTS (SELECT 1 FROM dbo.onb_onboarding_checklist WHERE onboarding_id = @Onb3 AND item_code = 'ORIENTATION_DONE')
BEGIN
    INSERT INTO dbo.onb_onboarding_checklist (onboarding_id, item_code, item_name, is_required, is_completed, due_date, completed_at, completed_by, note, created_by)
    VALUES
        (@Onb3, 'ORIENTATION_DONE', N'Hoàn tất orientation', 1, 1, '2026-04-11', DATEADD(DAY, -1, SYSDATETIME()), @ManagerUser, N'Đã giới thiệu team và công việc.', @HrUser),
        (@Onb3, 'WORKPLACE_READY', N'Bàn giao chỗ ngồi và thiết bị', 1, 1, '2026-04-11', DATEADD(DAY, -1, SYSDATETIME()), @HrUser, N'Đã cấp laptop và thẻ nhân viên.', @HrUser);
END

IF NOT EXISTS (SELECT 1 FROM dbo.onb_onboarding_document WHERE onboarding_id = @Onb2 AND document_name = N'Căn cước công dân')
BEGIN
    INSERT INTO dbo.onb_onboarding_document (
        onboarding_id, document_name, document_category, storage_path, mime_type, file_size_bytes, status, note, created_by
    )
    VALUES
        (@Onb2, N'Căn cước công dân', 'IDENTITY', N'/seed/onboarding/cand002/cccd.pdf', 'application/pdf', 248000, 'ACTIVE', N'Bản scan rõ nét.', @HrUser),
        (@Onb2, N'Bằng đại học', 'CERTIFICATE', N'/seed/onboarding/cand002/bachelor.pdf', 'application/pdf', 512000, 'ACTIVE', N'Đã kiểm tra hợp lệ.', @HrUser),
        (@Onb3, N'Hồ sơ nhân sự', 'PROFILE', N'/seed/onboarding/cand003/profile.pdf', 'application/pdf', 322000, 'ACTIVE', N'Hồ sơ onboarding đã hoàn chỉnh.', @HrUser);
END

IF NOT EXISTS (SELECT 1 FROM dbo.onb_onboarding_asset WHERE onboarding_id = @Onb2 AND asset_code = 'LAPTOP-CAND002')
BEGIN
    INSERT INTO dbo.onb_onboarding_asset (
        onboarding_id, asset_code, asset_name, asset_type, assigned_date, status, note, created_by
    )
    VALUES
        (@Onb2, 'LAPTOP-CAND002', N'Laptop Dell Latitude', 'LAPTOP', NULL, 'PLANNED', N'Chuẩn bị cấp trước ngày onboard.', @HrUser),
        (@Onb3, 'BADGE-CAND003', N'Thẻ nhân viên', 'BADGE', '2026-04-11', 'ASSIGNED', N'Đã kích hoạt quyền ra vào văn phòng.', @HrUser);
END

IF NOT EXISTS (SELECT 1 FROM dbo.onb_onboarding_notification_log WHERE onboarding_id = @Onb3 AND recipient_email = 'tran.luong@example.com')
BEGIN
    INSERT INTO dbo.onb_onboarding_notification_log (
        onboarding_id, channel_code, recipient_name, recipient_email, subject_snapshot, body_snapshot,
        delivery_status, sent_at, sent_by, note, created_by
    )
    VALUES
        (@Onb3, 'EMAIL', N'Lương Bảo Trân', 'tran.luong@example.com', N'Welcome to DigitalHRM',
         N'Thư chào mừng và hướng dẫn ngày đầu đi làm.', 'MOCKED', DATEADD(HOUR, -6, SYSDATETIME()), @HrUser,
         N'Seed dữ liệu thông báo onboarding.', @HrUser),
        (@Onb4, 'EMAIL', N'Lê Ngọc Hà', 'ngochaln030608@gmail.com', N'Onboarding completed',
         N'Xác nhận hoàn tất toàn bộ thủ tục tiếp nhận.', 'MOCKED', DATEADD(DAY, -15, SYSDATETIME()), @HrUser,
         N'Thông báo hoàn tất onboarding.', @HrUser);
END
GO

-- 5. Profile change approval workflow data
DECLARE @HrUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @UserMgrC UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'mgr_c');
DECLARE @UserAccD UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'acc_d');
DECLARE @UserDevE UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'dev_e');
DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @Emp4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @Emp5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');

IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee_profile_change_request WHERE employee_id = @Emp3 AND request_status = 'PENDING')
BEGIN
    INSERT INTO dbo.hr_employee_profile_change_request (
        employee_id, requester_user_id, request_type, request_status, payload_json,
        submitted_at, created_at, created_by, updated_at, updated_by, review_note
    )
    VALUES (
        @Emp3, @UserMgrC, 'PROFILE_UPDATE', 'PENDING',
        N'{"mobilePhone":"0909555666","personalEmail":"mgr.c.updated@example.com","emergencyNote":"Liên hệ người thân qua số 0988777666 khi khẩn cấp."}',
        DATEADD(HOUR, -8, SYSDATETIME()), DATEADD(HOUR, -8, SYSDATETIME()), @UserMgrC, NULL, NULL, NULL
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee_profile_change_request WHERE employee_id = @Emp4 AND request_status = 'APPROVED')
BEGIN
    INSERT INTO dbo.hr_employee_profile_change_request (
        employee_id, requester_user_id, request_type, request_status, payload_json,
        submitted_at, reviewed_at, reviewer_user_id, review_note,
        created_at, created_by, updated_at, updated_by
    )
    VALUES (
        @Emp4, @UserAccD, 'PROFILE_UPDATE', 'APPROVED',
        N'{"taxCode":"0319876543","educationLevel":"Cử nhân Kế toán","major":"Tài chính doanh nghiệp"}',
        DATEADD(DAY, -3, SYSDATETIME()), DATEADD(DAY, -2, SYSDATETIME()), @HrUser, N'HR đã đối chiếu chứng từ và cập nhật hồ sơ.',
        DATEADD(DAY, -3, SYSDATETIME()), @UserAccD, DATEADD(DAY, -2, SYSDATETIME()), @HrUser
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee_profile_change_request WHERE employee_id = @Emp5 AND request_status = 'REJECTED')
BEGIN
    INSERT INTO dbo.hr_employee_profile_change_request (
        employee_id, requester_user_id, request_type, request_status, payload_json,
        submitted_at, reviewed_at, reviewer_user_id, review_note,
        created_at, created_by, updated_at, updated_by
    )
    VALUES (
        @Emp5, @UserDevE, 'PROFILE_UPDATE', 'REJECTED',
        N'{"nationality":"Việt Nam","placeOfBirth":"Da Nang","avatarUrl":"https://example.com/avatar/emp005-new.jpg"}',
        DATEADD(DAY, -5, SYSDATETIME()), DATEADD(DAY, -4, SYSDATETIME()), @HrUser, N'Cần bổ sung giấy tờ chứng minh trước khi HR cập nhật.',
        DATEADD(DAY, -5, SYSDATETIME()), @UserDevE, DATEADD(DAY, -4, SYSDATETIME()), @HrUser
    );
END
GO

-- 6. Offboarding workflow data
DECLARE @HrUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');
DECLARE @UserMgrC UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'mgr_c');
DECLARE @UserAccD UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'acc_d');
DECLARE @UserDevE UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'dev_e');
DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
DECLARE @Emp4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @Emp5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');
DECLARE @PayrollPeriodId BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-05');
DECLARE @PayrollItemEmp4 BIGINT = (
    SELECT payroll_item_id FROM dbo.pay_payroll_item
    WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @Emp4
);
DECLARE @PayrollItemEmp5 BIGINT = (
    SELECT payroll_item_id FROM dbo.pay_payroll_item
    WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @Emp5
);

IF NOT EXISTS (SELECT 1 FROM dbo.off_offboarding_case WHERE offboarding_code = 'OFF-2026-001')
BEGIN
    INSERT INTO dbo.off_offboarding_case (
        offboarding_code, employee_id, requested_by_user_id, request_date, requested_last_working_date,
        request_reason, status, created_by
    )
    VALUES (
        'OFF-2026-001', @Emp3, @UserMgrC, '2026-04-05', '2026-04-25',
        N'Mong muốn chuyển hướng sang cơ hội nghề nghiệp mới.', 'REQUESTED', @UserMgrC
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.off_offboarding_case WHERE offboarding_code = 'OFF-2026-002')
BEGIN
    INSERT INTO dbo.off_offboarding_case (
        offboarding_code, employee_id, requested_by_user_id, request_date, requested_last_working_date,
        request_reason, status, manager_reviewed_by, manager_reviewed_at, manager_review_note,
        hr_finalized_by, hr_finalized_at, effective_last_working_date, hr_finalize_note,
        created_by
    )
    VALUES (
        'OFF-2026-002', @Emp4, @UserAccD, '2026-04-01', '2026-04-22',
        N'Kết thúc hợp đồng theo kế hoạch cá nhân.', 'HR_FINALIZED',
        @ManagerUser, DATEADD(DAY, -6, SYSDATETIME()), N'Đã thống nhất kế hoạch bàn giao.',
        @HrUser, DATEADD(DAY, -4, SYSDATETIME()), '2026-04-22', N'HR chốt lịch nghỉ và checklist bàn giao.',
        @HrUser
    );
END

IF NOT EXISTS (SELECT 1 FROM dbo.off_offboarding_case WHERE offboarding_code = 'OFF-2026-003')
BEGIN
    INSERT INTO dbo.off_offboarding_case (
        offboarding_code, employee_id, requested_by_user_id, request_date, requested_last_working_date,
        request_reason, status, manager_reviewed_by, manager_reviewed_at, manager_review_note,
        hr_finalized_by, hr_finalized_at, effective_last_working_date, hr_finalize_note,
        access_revoked_by, access_revoked_at, access_revoke_note,
        settlement_prepared_by, settlement_prepared_at, final_attendance_year, final_attendance_month,
        leave_settlement_units, leave_settlement_amount, final_payroll_period_id, final_payroll_item_id, settlement_note,
        closed_by, closed_at, close_note, created_by
    )
    VALUES (
        'OFF-2026-003', @Emp5, @UserDevE, '2026-03-20', '2026-04-10',
        N'Kết thúc giai đoạn làm việc để tiếp tục học tập.', 'CLOSED',
        @ManagerUser, DATEADD(DAY, -18, SYSDATETIME()), N'Đã phê duyệt đơn nghỉ việc.',
        @HrUser, DATEADD(DAY, -15, SYSDATETIME()), '2026-04-10', N'HR đã chốt hồ sơ thôi việc.',
        @HrUser, DATEADD(DAY, -12, SYSDATETIME()), N'Đã khóa email và tài khoản nội bộ.',
        @HrUser, DATEADD(DAY, -10, SYSDATETIME()), 2026, 4,
        2.00, 1400000, @PayrollPeriodId, @PayrollItemEmp5, N'Đã chuẩn bị quyết toán cuối cùng.',
        @HrUser, DATEADD(DAY, -8, SYSDATETIME()), N'Hoàn tất bàn giao và lưu trữ hồ sơ.',
        @HrUser
    );
END
GO

DECLARE @Off1 BIGINT = (SELECT offboarding_case_id FROM dbo.off_offboarding_case WHERE offboarding_code = 'OFF-2026-001');
DECLARE @Off2 BIGINT = (SELECT offboarding_case_id FROM dbo.off_offboarding_case WHERE offboarding_code = 'OFF-2026-002');
DECLARE @Off3 BIGINT = (SELECT offboarding_case_id FROM dbo.off_offboarding_case WHERE offboarding_code = 'OFF-2026-003');
DECLARE @HrUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');
DECLARE @UserMgrC UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'mgr_c');
DECLARE @UserAccD UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'acc_d');
DECLARE @UserDevE UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'dev_e');

IF NOT EXISTS (SELECT 1 FROM dbo.off_offboarding_checklist_item WHERE offboarding_case_id = @Off2 AND item_name = N'Bàn giao hồ sơ kế toán')
BEGIN
    INSERT INTO dbo.off_offboarding_checklist_item (
        offboarding_case_id, item_type, item_name, owner_role_code, due_date, status, note, created_by
    )
    VALUES
        (@Off2, 'DOCUMENT', N'Bàn giao hồ sơ kế toán', 'HR', '2026-04-18', 'IN_PROGRESS', N'Đang rà soát chứng từ tồn.', @HrUser),
        (@Off2, 'ASSET', N'Hoàn trả laptop và token ngân hàng', 'IT', '2026-04-18', 'OPEN', N'Chờ bộ phận IT xác nhận.', @HrUser),
        (@Off3, 'HANDOVER', N'Bàn giao source code và tài liệu', 'MANAGER', '2026-04-08', 'COMPLETED', N'Đã bàn giao đầy đủ cho team lead.', @HrUser);
END

IF NOT EXISTS (SELECT 1 FROM dbo.off_offboarding_asset_return WHERE offboarding_case_id = @Off2 AND asset_name = N'Laptop Dell Precision')
BEGIN
    INSERT INTO dbo.off_offboarding_asset_return (
        offboarding_case_id, asset_code, asset_name, asset_type, expected_return_date, status, note, created_by
    )
    VALUES
        (@Off2, 'AST-ACC-004', N'Laptop Dell Precision', 'LAPTOP', '2026-04-18', 'PENDING', N'Chờ nhân sự hoàn trả.', @HrUser),
        (@Off3, 'AST-DEV-005', N'Laptop MacBook Pro', 'LAPTOP', '2026-04-10', 'RETURNED', N'Đã nhận và kiểm tra thiết bị.', @HrUser);
END

IF NOT EXISTS (SELECT 1 FROM dbo.off_offboarding_history WHERE offboarding_case_id = @Off1 AND action_code = 'REQUEST')
BEGIN
    INSERT INTO dbo.off_offboarding_history (
        offboarding_case_id, from_status, to_status, action_code, action_note, changed_by, changed_at, created_by
    )
    VALUES
        (@Off1, NULL, 'REQUESTED', 'REQUEST', N'Nhân sự đã gửi đơn nghỉ việc.', @UserMgrC, DATEADD(DAY, -4, SYSDATETIME()), @UserMgrC),
        (@Off2, NULL, 'REQUESTED', 'REQUEST', N'Khởi tạo hồ sơ nghỉ việc.', @UserAccD, DATEADD(DAY, -9, SYSDATETIME()), @UserAccD),
        (@Off2, 'REQUESTED', 'MANAGER_APPROVED', 'MANAGER_APPROVE', N'Quản lý duyệt kế hoạch bàn giao.', @ManagerUser, DATEADD(DAY, -6, SYSDATETIME()), @ManagerUser),
        (@Off2, 'MANAGER_APPROVED', 'HR_FINALIZED', 'HR_FINALIZE', N'HR đã chốt ngày nghỉ việc.', @HrUser, DATEADD(DAY, -4, SYSDATETIME()), @HrUser),
        (@Off3, NULL, 'REQUESTED', 'REQUEST', N'Nhân viên gửi đơn nghỉ việc.', @UserDevE, DATEADD(DAY, -20, SYSDATETIME()), @UserDevE),
        (@Off3, 'REQUESTED', 'MANAGER_APPROVED', 'MANAGER_APPROVE', N'Quản lý đã duyệt.', @ManagerUser, DATEADD(DAY, -18, SYSDATETIME()), @ManagerUser),
        (@Off3, 'MANAGER_APPROVED', 'HR_FINALIZED', 'HR_FINALIZE', N'HR chốt hồ sơ.', @HrUser, DATEADD(DAY, -15, SYSDATETIME()), @HrUser),
        (@Off3, 'HR_FINALIZED', 'ACCESS_REVOKED', 'REVOKE_ACCESS', N'Đã thu hồi quyền truy cập.', @HrUser, DATEADD(DAY, -12, SYSDATETIME()), @HrUser),
        (@Off3, 'ACCESS_REVOKED', 'SETTLEMENT_PREPARED', 'PREPARE_SETTLEMENT', N'Đã chuẩn bị settlement.', @HrUser, DATEADD(DAY, -10, SYSDATETIME()), @HrUser),
        (@Off3, 'SETTLEMENT_PREPARED', 'CLOSED', 'CLOSE_CASE', N'Đóng hồ sơ offboarding.', @HrUser, DATEADD(DAY, -8, SYSDATETIME()), @HrUser);
END
GO

SET NOCOUNT OFF;
GO
