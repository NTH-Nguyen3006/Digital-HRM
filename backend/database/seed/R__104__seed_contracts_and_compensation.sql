SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__104__seed_contracts_and_compensation.sql
   Scope:
   - Seed contract types and labor contracts for EMP001-EMP005
   - Seed salary components, formula, and compensation
   ========================================================= */

-- 1. Contract Types
IF NOT EXISTS (SELECT 1 FROM dbo.ct_contract_type WHERE contract_type_code = '12M')
BEGIN
    INSERT INTO dbo.ct_contract_type (contract_type_code, contract_type_name, max_term_months, requires_end_date, status, created_at, is_deleted)
    VALUES ('12M', N'Hợp đồng 12 tháng', 12, 1, 'ACTIVE', SYSDATETIME(), 0);
END

IF NOT EXISTS (SELECT 1 FROM dbo.ct_contract_type WHERE contract_type_code = 'INDEF')
BEGIN
    INSERT INTO dbo.ct_contract_type (contract_type_code, contract_type_name, max_term_months, requires_end_date, status, created_at, is_deleted)
    VALUES ('INDEF', N'Hợp đồng không xác định thời hạn', NULL, 0, 'ACTIVE', SYSDATETIME(), 0);
END
GO

-- 2. Labor Contracts for EMP001-EMP005
DECLARE @Type12M BIGINT = (SELECT contract_type_id FROM dbo.ct_contract_type WHERE contract_type_code = '12M');
DECLARE @TypeINDEF BIGINT = (SELECT contract_type_id FROM dbo.ct_contract_type WHERE contract_type_code = 'INDEF');

-- EMP001
IF NOT EXISTS (SELECT 1 FROM dbo.ct_labor_contract WHERE contract_number = 'HDLD-EMP001-01')
   AND EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP001')
BEGIN
    DECLARE @Emp1 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP001');
    DECLARE @Job1 BIGINT = (SELECT job_title_id FROM dbo.hr_employee WHERE employee_id = @Emp1);
    DECLARE @Org1 BIGINT = (SELECT org_unit_id FROM dbo.hr_employee WHERE employee_id = @Emp1);
    
    INSERT INTO dbo.ct_labor_contract (employee_id, contract_type_id, contract_number, sign_date, effective_date, end_date, job_title_id, org_unit_id, base_salary, salary_currency, working_type, contract_status)
    VALUES (@Emp1, @TypeINDEF, 'HDLD-EMP001-01', '2020-01-10', '2020-01-15', NULL, @Job1, @Org1, 45000000, 'VND', 'FULL_TIME', 'ACTIVE');
END

-- EMP002
IF NOT EXISTS (SELECT 1 FROM dbo.ct_labor_contract WHERE contract_number = 'HDLD-EMP002-01')
   AND EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP002')
BEGIN
    DECLARE @Emp2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');
    DECLARE @Job2 BIGINT = (SELECT job_title_id FROM dbo.hr_employee WHERE employee_id = @Emp2);
    DECLARE @Org2 BIGINT = (SELECT org_unit_id FROM dbo.hr_employee WHERE employee_id = @Emp2);
    
    INSERT INTO dbo.ct_labor_contract (employee_id, contract_type_id, contract_number, sign_date, effective_date, end_date, job_title_id, org_unit_id, base_salary, salary_currency, working_type, contract_status)
    VALUES (@Emp2, @TypeINDEF, 'HDLD-EMP002-01', '2021-02-25', '2021-03-01', NULL, @Job2, @Org2, 35000000, 'VND', 'FULL_TIME', 'ACTIVE');
END

-- EMP003
IF NOT EXISTS (SELECT 1 FROM dbo.ct_labor_contract WHERE contract_number = 'HDLD-EMP003-01')
   AND EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP003')
BEGIN
    DECLARE @Emp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
    DECLARE @Job3 BIGINT = (SELECT job_title_id FROM dbo.hr_employee WHERE employee_id = @Emp3);
    DECLARE @Org3 BIGINT = (SELECT org_unit_id FROM dbo.hr_employee WHERE employee_id = @Emp3);
    
    INSERT INTO dbo.ct_labor_contract (employee_id, contract_type_id, contract_number, sign_date, effective_date, end_date, job_title_id, org_unit_id, base_salary, salary_currency, working_type, contract_status)
    VALUES (@Emp3, @TypeINDEF, 'HDLD-EMP003-01', '2019-10-05', '2019-10-10', NULL, @Job3, @Org3, 40000000, 'VND', 'FULL_TIME', 'ACTIVE');
END

-- EMP004
IF NOT EXISTS (SELECT 1 FROM dbo.ct_labor_contract WHERE contract_number = 'HDLD-EMP004-01')
   AND EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP004')
BEGIN
    DECLARE @Emp4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
    DECLARE @Job4 BIGINT = (SELECT job_title_id FROM dbo.hr_employee WHERE employee_id = @Emp4);
    DECLARE @Org4 BIGINT = (SELECT org_unit_id FROM dbo.hr_employee WHERE employee_id = @Emp4);
    
    INSERT INTO dbo.ct_labor_contract (employee_id, contract_type_id, contract_number, sign_date, effective_date, end_date, job_title_id, org_unit_id, base_salary, salary_currency, working_type, contract_status)
    VALUES (@Emp4, @TypeINDEF, 'HDLD-EMP004-01', '2022-01-01', '2022-01-05', NULL, @Job4, @Org4, 25000000, 'VND', 'FULL_TIME', 'ACTIVE');
END

-- EMP005
IF NOT EXISTS (SELECT 1 FROM dbo.ct_labor_contract WHERE contract_number = 'HDLD-EMP005-01')
   AND EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP005')
BEGIN
    DECLARE @Emp5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');
    DECLARE @Job5 BIGINT = (SELECT job_title_id FROM dbo.hr_employee WHERE employee_id = @Emp5);
    DECLARE @Org5 BIGINT = (SELECT org_unit_id FROM dbo.hr_employee WHERE employee_id = @Emp5);
    
    INSERT INTO dbo.ct_labor_contract (employee_id, contract_type_id, contract_number, sign_date, effective_date, end_date, job_title_id, org_unit_id, base_salary, salary_currency, working_type, contract_status)
    VALUES (@Emp5, @Type12M, 'HDLD-EMP005-01', '2023-06-10', '2023-06-15', '2026-06-14', @Job5, @Org5, 15000000, 'VND', 'FULL_TIME', 'ACTIVE');
END
GO

-- 3. Salary Components
IF NOT EXISTS (SELECT 1 FROM dbo.pay_salary_component WHERE component_code = 'BASE_SALARY')
BEGIN
    INSERT INTO dbo.pay_salary_component (component_code, component_name, component_category, amount_type, taxable, insurance_base_included, display_order)
    VALUES ('BASE_SALARY', N'Lương cơ bản', 'EARNING', 'SYSTEM', 1, 1, 10);
END

IF NOT EXISTS (SELECT 1 FROM dbo.pay_salary_component WHERE component_code = 'ALLOWANCE')
BEGIN
    INSERT INTO dbo.pay_salary_component (component_code, component_name, component_category, amount_type, taxable, insurance_base_included, display_order)
    VALUES ('ALLOWANCE', N'Trợ cấp ăn trưa', 'EARNING', 'FIXED_AMOUNT', 0, 0, 20);
END
GO

-- 4. Formula Version
IF NOT EXISTS (SELECT 1 FROM dbo.pay_formula_version WHERE formula_code = 'STD_2026')
BEGIN
    INSERT INTO dbo.pay_formula_version (formula_code, formula_name, effective_from, personal_deduction_monthly, dependent_deduction_monthly, social_insurance_employee_rate, health_insurance_employee_rate, unemployment_insurance_employee_rate, standard_work_hours_per_day, overtime_multiplier_weekday)
    VALUES ('STD_2026', N'Công thức chuẩn 2026', '2026-01-01', 11000000, 4400000, 0.08, 0.015, 0.01, 8.0, 1.5);
END
GO

-- 5. Employee Compensation
DECLARE @CompEmp1 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP001');
DECLARE @AllowanceId BIGINT = (SELECT salary_component_id FROM dbo.pay_salary_component WHERE component_code = 'ALLOWANCE');

IF NOT EXISTS (SELECT 1 FROM dbo.pay_employee_compensation WHERE employee_id = @CompEmp1 AND status = 'ACTIVE')
BEGIN
    INSERT INTO dbo.pay_employee_compensation (employee_id, effective_from, base_salary, insurance_salary_base, bank_account_name, bank_account_no, bank_name)
    VALUES (@CompEmp1, '2026-01-01', 45000000, 45000000, 'NGUYEN VAN A', '0011001234567', 'Vietcombank');
    
    DECLARE @CompId1 BIGINT = SCOPE_IDENTITY();
    INSERT INTO dbo.pay_employee_compensation_item (employee_compensation_id, salary_component_id, amount_value)
    VALUES (@CompId1, @AllowanceId, 730000);
END

DECLARE @CompEmp2 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP002');
IF NOT EXISTS (SELECT 1 FROM dbo.pay_employee_compensation WHERE employee_id = @CompEmp2 AND status = 'ACTIVE')
BEGIN
    INSERT INTO dbo.pay_employee_compensation (employee_id, effective_from, base_salary, insurance_salary_base, bank_account_name, bank_account_no, bank_name)
    VALUES (@CompEmp2, '2026-01-01', 35000000, 35000000, 'TRAN THI B', '19034567891234', 'Techcombank');
    
    DECLARE @CompId2 BIGINT = SCOPE_IDENTITY();
    INSERT INTO dbo.pay_employee_compensation_item (employee_compensation_id, salary_component_id, amount_value)
    VALUES (@CompId2, @AllowanceId, 730000);
END

DECLARE @CompEmp3 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP003');
IF NOT EXISTS (SELECT 1 FROM dbo.pay_employee_compensation WHERE employee_id = @CompEmp3 AND status = 'ACTIVE')
BEGIN
    INSERT INTO dbo.pay_employee_compensation (employee_id, effective_from, base_salary, insurance_salary_base, bank_account_name, bank_account_no, bank_name)
    VALUES (@CompEmp3, '2026-01-01', 40000000, 40000000, 'LE VAN C', '19012345674321', 'Techcombank');
    
    DECLARE @CompId3 BIGINT = SCOPE_IDENTITY();
    INSERT INTO dbo.pay_employee_compensation_item (employee_compensation_id, salary_component_id, amount_value)
    VALUES (@CompId3, @AllowanceId, 730000);
END

DECLARE @CompEmp4 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
IF NOT EXISTS (SELECT 1 FROM dbo.pay_employee_compensation WHERE employee_id = @CompEmp4 AND status = 'ACTIVE')
BEGIN
    INSERT INTO dbo.pay_employee_compensation (employee_id, effective_from, base_salary, insurance_salary_base, bank_account_name, bank_account_no, bank_name)
    VALUES (@CompEmp4, '2026-01-01', 25000000, 25000000, 'PHAM THI D', '211123234543', 'MB Bank');
    
    DECLARE @CompId4 BIGINT = SCOPE_IDENTITY();
    INSERT INTO dbo.pay_employee_compensation_item (employee_compensation_id, salary_component_id, amount_value)
    VALUES (@CompId4, @AllowanceId, 730000);
END

DECLARE @CompEmp5 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP005');
IF NOT EXISTS (SELECT 1 FROM dbo.pay_employee_compensation WHERE employee_id = @CompEmp5 AND status = 'ACTIVE')
BEGIN
    INSERT INTO dbo.pay_employee_compensation (employee_id, effective_from, base_salary, insurance_salary_base, bank_account_name, bank_account_no, bank_name)
    VALUES (@CompEmp5, '2026-01-01', 15000000, 15000000, 'HOANG VAN E', '0451000123456', 'Vietcombank');
    
    DECLARE @CompId5 BIGINT = SCOPE_IDENTITY();
    INSERT INTO dbo.pay_employee_compensation_item (employee_compensation_id, salary_component_id, amount_value)
    VALUES (@CompId5, @AllowanceId, 730000);
END
GO
