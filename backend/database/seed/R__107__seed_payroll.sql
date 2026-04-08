SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__107__seed_payroll.sql
   Scope:
   - Seed payroll periods
   - Seed sample payroll items and lines (payslips)
   ========================================================= */

-- 1. Payroll Period (e.g., April 2026)
DECLARE @AttPeriodId BIGINT = (SELECT TOP 1 attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-05');
DECLARE @FormulaId BIGINT = (SELECT TOP 1 formula_version_id FROM dbo.pay_formula_version WHERE formula_code = 'STD_2026');

IF NOT EXISTS (SELECT 1 FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-05')
   AND @AttPeriodId IS NOT NULL 
   AND @FormulaId IS NOT NULL
BEGIN
    INSERT INTO dbo.pay_payroll_period (period_code, period_year, period_month, period_start_date, period_end_date, attendance_period_id, formula_version_id, period_status, total_employee_count, total_gross_amount, total_net_amount)
    VALUES ('PR-2026-05', 2026, 5, '2026-05-01', '2026-05-31', @AttPeriodId, @FormulaId, 'PUBLISHED', 5, 160000000, 140000000);
END
GO

-- 2. Payroll Items (Payslips for 2 employees as an example)
DECLARE @PayrollPeriodId BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-05');
DECLARE @CompEmp1 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP001');

IF NOT EXISTS (SELECT 1 FROM dbo.pay_payroll_item WHERE payroll_period_id = @PayrollPeriodId AND employee_id = @CompEmp1)
   AND @PayrollPeriodId IS NOT NULL
BEGIN
    -- Payslip for EMP001
    INSERT INTO dbo.pay_payroll_item (
        payroll_period_id, employee_id, scheduled_day_count, present_day_count, 
        base_salary_monthly, base_salary_prorated, fixed_earning_total, fixed_deduction_total,
        employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount,
        taxable_income, pit_amount, gross_income, net_pay, item_status
    )
    VALUES (
        @PayrollPeriodId, @CompEmp1, 22, 22,
        45000000, 45000000, 730000, 0,
        4725000, 11000000, 0,
        30005000, 3950000, 45730000, 37055000, 'PUBLISHED'
    );
    
    DECLARE @PayItemId1 BIGINT = SCOPE_IDENTITY();
    
    -- Earning: Base
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable)
    VALUES (@PayItemId1, 'BASE_SALARY', N'Lương cơ bản', 'EARNING', 'SYSTEM', 45000000, 1);
    
    -- Earning: Allowance
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable)
    VALUES (@PayItemId1, 'ALLOWANCE', N'Trợ cấp ăn trưa', 'EARNING', 'CONFIGURED', 730000, 0);

    -- Deduction: Insurance
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable)
    VALUES (@PayItemId1, 'BHXH_BHYT_BHTN', N'BHXH, BHYT, BHTN (10.5%)', 'DEDUCTION', 'SYSTEM', 4725000, 0);

    -- Deduction: PIT
    INSERT INTO dbo.pay_payroll_item_line (payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable)
    VALUES (@PayItemId1, 'PIT', N'Thuế TNCN', 'DEDUCTION', 'SYSTEM', 3950000, 0);
END
GO
