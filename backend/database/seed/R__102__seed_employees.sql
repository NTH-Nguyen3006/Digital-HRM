SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__102__seed_employees.sql
   Scope:
   - Seed 5 realistic employees with full profiles
   ========================================================= */

-- Helper to clear existing seed data if needed (optional, but good for clean re-seeds)
-- DELETE FROM dbo.hr_employee_address WHERE employee_id IN (SELECT employee_id FROM dbo.hr_employee WHERE employee_code LIKE 'EMP%');
-- ...

-- 1. Nguyen Van A - HR Manager (Hanoi)
IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP001')
BEGIN
    DECLARE @DeptHrHnId BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_HR_HN');
    DECLARE @TitleHrMgrId BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'MGR_HR');
    
    INSERT INTO dbo.hr_employee (employee_code, org_unit_id, job_title_id, full_name, work_email, work_phone, gender_code, date_of_birth, hire_date, employment_status, work_location)
    VALUES ('EMP001', @DeptHrHnId, @TitleHrMgrId, N'Nguyễn Văn A', 'a.nguyen@digitalhrm.com', '0901234567', 'MALE', '1985-05-20', '2020-01-15', 'ACTIVE', N'Hanoi Office');

    DECLARE @EmpId1 BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.hr_employee_profile (employee_id, first_name, last_name, marital_status, nationality, ethnic_group, religion, education_level, major)
    VALUES (@EmpId1, N'A', N'Nguyễn Văn', 'MARRIED', N'Vietnam', N'Kinh', N'None', N'Master', N'Human Resources Management');

    INSERT INTO dbo.hr_employee_address (employee_id, address_type, address_line, province_name, is_primary)
    VALUES (@EmpId1, 'PERMANENT', N'123 Đường Láng, Đống Đa', N'Hà Nội', 1);

    INSERT INTO dbo.hr_employee_identification (employee_id, document_type, document_number, status, is_primary)
    VALUES (@EmpId1, 'CCCD', '001085001234', 'VALID', 1);

    INSERT INTO dbo.hr_employee_bank_account (employee_id, bank_name, account_number, account_holder_name, status, is_primary)
    VALUES (@EmpId1, 'Vietcombank', '0011001234567', 'NGUYEN VAN A', 'ACTIVE', 1);
END

-- 2. Trần Thị B - Senior Developer (Hanoi)
IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP002')
BEGIN
    DECLARE @DeptItHnId BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_IT_HN');
    DECLARE @TitleSrDevId BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'SDR_DEV');
    
    INSERT INTO dbo.hr_employee (employee_code, org_unit_id, job_title_id, full_name, work_email, work_phone, gender_code, date_of_birth, hire_date, employment_status, work_location)
    VALUES ('EMP002', @DeptItHnId, @TitleSrDevId, N'Trần Thị B', 'b.tran@digitalhrm.com', '0902345678', 'FEMALE', '1990-08-12', '2021-03-01', 'ACTIVE', N'Hanoi Office');

    DECLARE @EmpId2 BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.hr_employee_profile (employee_id, first_name, last_name, marital_status, nationality, ethnic_group, religion, education_level, major)
    VALUES (@EmpId2, N'B', N'Trần Thị', 'SINGLE', N'Vietnam', N'Kinh', N'Buddhism', N'Bachelor', N'Computer Science');

    INSERT INTO dbo.hr_employee_address (employee_id, address_type, address_line, province_name, is_primary)
    VALUES (@EmpId2, 'PERMANENT', N'456 Cầu Giấy', N'Hà Nội', 1);

    INSERT INTO dbo.hr_employee_identification (employee_id, document_type, document_number, status, is_primary)
    VALUES (@EmpId2, 'CCCD', '030090005678', 'VALID', 1);
END

-- 3. Lê Văn C - Sales Manager (HCMC)
IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP003')
BEGIN
    DECLARE @DeptSalesHcmId BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_SALES_HCM');
    DECLARE @TitleSalesMgrId BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'MGR_SALES');
    
    INSERT INTO dbo.hr_employee (employee_code, org_unit_id, job_title_id, full_name, work_email, work_phone, gender_code, date_of_birth, hire_date, employment_status, work_location)
    VALUES ('EMP003', @DeptSalesHcmId, @TitleSalesMgrId, N'Lê Văn C', 'c.le@digitalhrm.com', '0903456789', 'MALE', '1988-11-30', '2019-10-10', 'ACTIVE', N'HCMC Office');

    DECLARE @EmpId3 BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.hr_employee_profile (employee_id, first_name, last_name, marital_status, nationality, ethnic_group, education_level, major)
    VALUES (@EmpId3, N'C', N'Lê Văn', 'MARRIED', N'Vietnam', N'Kinh', N'Bachelor', N'Business Administration');
END

-- 4. Phạm Thị D - Senior Accountant (HCMC)
IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP004')
BEGIN
    DECLARE @DeptFinHcmId BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_FIN_HCM');
    DECLARE @TitleSrAccId BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'SSR_ACC');
    
    INSERT INTO dbo.hr_employee (employee_code, org_unit_id, job_title_id, full_name, work_email, work_phone, gender_code, date_of_birth, hire_date, employment_status, work_location)
    VALUES ('EMP004', @DeptFinHcmId, @TitleSrAccId, N'Phạm Thị D', 'd.pham@digitalhrm.com', '0904567890', 'FEMALE', '1992-02-14', '2022-01-05', 'ACTIVE', N'HCMC Office');

    DECLARE @EmpId4 BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.hr_employee_profile (employee_id, first_name, last_name, marital_status, nationality, ethnic_group, education_level, major)
    VALUES (@EmpId4, N'D', N'Phạm Thị', 'MARRIED', N'Vietnam', N'Kinh', N'Bachelor', N'Accounting');
END

-- 5. Hoàng Văn E - Junior Developer (Hanoi)
IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP005')
BEGIN
    DECLARE @DeptItHnId2 BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_IT_HN');
    DECLARE @TitleJrDevId BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'JDR_DEV');
    
    INSERT INTO dbo.hr_employee (employee_code, org_unit_id, job_title_id, full_name, work_email, work_phone, gender_code, date_of_birth, hire_date, employment_status, work_location)
    VALUES ('EMP005', @DeptItHnId2, @TitleJrDevId, N'Hoàng Văn E', 'e.hoang@digitalhrm.com', '0905678901', 'MALE', '1998-07-25', '2023-06-15', 'ACTIVE', N'Hanoi Office');

    DECLARE @EmpId5 BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.hr_employee_profile (employee_id, first_name, last_name, marital_status, nationality, ethnic_group, education_level, major)
    VALUES (@EmpId5, N'E', N'Hoàng Văn', 'SINGLE', N'Vietnam', N'Kinh', N'Bachelor', N'Information Technology');
END
GO

-- 6. Nguyễn Tấn Lợi - IT Senior (Admin Role)
IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP006')
BEGIN
    DECLARE @DeptItHnId3 BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_IT_HN');
    DECLARE @TitleSrDevId2 BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'SDR_DEV');
    
    INSERT INTO dbo.hr_employee (employee_code, org_unit_id, job_title_id, full_name, work_email, work_phone, gender_code, date_of_birth, hire_date, employment_status, work_location)
    VALUES ('EMP006', @DeptItHnId3, @TitleSrDevId2, N'Nguyễn Tấn Lợi', 'Lointts02286@gmail.com', '0906789012', 'MALE', '1995-01-01', '2024-01-01', 'ACTIVE', N'Hanoi Office');

    DECLARE @EmpId6 BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.hr_employee_profile (employee_id, first_name, last_name, marital_status, nationality, ethnic_group, education_level, major)
    VALUES (@EmpId6, N'Lợi', N'Nguyễn Tấn', 'SINGLE', N'Vietnam', N'Kinh', N'Bachelor', N'Information Technology');
END

-- 7. Nguyễn Đỗ Diễm Quỳnh - HR Manager (HR Role)
IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP007')
BEGIN
    DECLARE @DeptHrHnId2 BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_HR_HN');
    DECLARE @TitleHrMgrId2 BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'MGR_HR');
    
    INSERT INTO dbo.hr_employee (employee_code, org_unit_id, job_title_id, full_name, work_email, gender_code, date_of_birth, hire_date, employment_status, work_location)
    VALUES ('EMP007', @DeptHrHnId2, @TitleHrMgrId2, N'Nguyễn Đỗ Diễm Quỳnh', 'jiemukuin@gmail.com', 'FEMALE', '1996-02-02', '2024-01-01', 'ACTIVE', N'Hanoi Office');

    DECLARE @EmpId7 BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.hr_employee_profile (employee_id, first_name, last_name, marital_status, nationality, ethnic_group, education_level, major)
    VALUES (@EmpId7, N'Quỳnh', N'Nguyễn Đỗ Diễm', 'SINGLE', N'Vietnam', N'Kinh', N'Bachelor', N'Human Resources');
END

-- 8. Nguyễn Đình Bảo Nguyên - Sales Manager (Manager Role)
IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP008')
BEGIN
    DECLARE @DeptSalesHcmId2 BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_SALES_HCM');
    DECLARE @TitleSalesMgrId2 BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'MGR_SALES');
    
    INSERT INTO dbo.hr_employee (employee_code, org_unit_id, job_title_id, full_name, work_email, gender_code, date_of_birth, hire_date, employment_status, work_location)
    VALUES ('EMP008', @DeptSalesHcmId2, @TitleSalesMgrId2, N'Nguyễn Đình Bảo Nguyên', 'nguyen2111vn@gmail.com', 'MALE', '1997-03-03', '2024-01-01', 'ACTIVE', N'HCMC Office');

    DECLARE @EmpId8 BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.hr_employee_profile (employee_id, first_name, last_name, marital_status, nationality, ethnic_group, education_level, major)
    VALUES (@EmpId8, N'Nguyên', N'Nguyễn Đình Bảo', 'SINGLE', N'Vietnam', N'Kinh', N'Bachelor', N'Business Administration');
END

-- 9. Lê Ngọc Hà - Senior Accountant (Employee Role)
IF NOT EXISTS (SELECT 1 FROM dbo.hr_employee WHERE employee_code = 'EMP009')
BEGIN
    DECLARE @DeptFinHcmId2 BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_FIN_HCM');
    DECLARE @TitleSrAccId2 BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'SSR_ACC');
    
    INSERT INTO dbo.hr_employee (employee_code, org_unit_id, job_title_id, full_name, work_email, gender_code, date_of_birth, hire_date, employment_status, work_location)
    VALUES ('EMP009', @DeptFinHcmId2, @TitleSrAccId2, N'Lê Ngọc Hà', 'ngochaln030608@gmail.com', 'FEMALE', '1998-04-04', '2024-01-01', 'ACTIVE', N'HCMC Office');

    DECLARE @EmpId9 BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.hr_employee_profile (employee_id, first_name, last_name, marital_status, nationality, ethnic_group, education_level, major)
    VALUES (@EmpId9, N'Hà', N'Lê Ngọc', 'SINGLE', N'Vietnam', N'Kinh', N'Bachelor', N'Accounting');
END
GO

SET NOCOUNT OFF;
GO
