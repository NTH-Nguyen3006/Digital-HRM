SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__100__seed_organization.sql
   Scope:
   - Seed core organization structure (Company, Branches, Departments)
   ========================================================= */

-- 1. Create Company (Root)
IF NOT EXISTS (SELECT 1 FROM dbo.hr_org_unit WHERE org_unit_code = 'CORP')
BEGIN
    INSERT INTO dbo.hr_org_unit (org_unit_code, org_unit_name, org_unit_type, status, effective_from, hierarchy_level, path_code, sort_order)
    VALUES ('CORP', N'DigitalHRM Corporation', 'COMPANY', 'ACTIVE', '2020-01-01', 1, '/CORP/', 1);
END
ELSE
BEGIN
    UPDATE dbo.hr_org_unit
    SET org_unit_name = N'DigitalHRM Corporation',
        status = 'ACTIVE'
    WHERE org_unit_code = 'CORP';
END
GO

-- 2. Create Branches (Hanoi, HCMC)
DECLARE @CorpId BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'CORP');

-- Hanoi Branch
IF NOT EXISTS (SELECT 1 FROM dbo.hr_org_unit WHERE org_unit_code = 'BR_HN')
BEGIN
    INSERT INTO dbo.hr_org_unit (parent_org_unit_id, org_unit_code, org_unit_name, org_unit_type, status, effective_from, hierarchy_level, path_code, sort_order)
    VALUES (@CorpId, 'BR_HN', N'Hanoi Branch', 'BRANCH', 'ACTIVE', '2020-01-01', 2, '/CORP/BR_HN/', 1);
END

-- HCMC Branch
IF NOT EXISTS (SELECT 1 FROM dbo.hr_org_unit WHERE org_unit_code = 'BR_HCM')
BEGIN
    INSERT INTO dbo.hr_org_unit (parent_org_unit_id, org_unit_code, org_unit_name, org_unit_type, status, effective_from, hierarchy_level, path_code, sort_order)
    VALUES (@CorpId, 'BR_HCM', N'HCMC Branch', 'BRANCH', 'ACTIVE', '2020-01-01', 2, '/CORP/BR_HCM/', 2);
END
GO

-- 3. Create Departments for Hanoi Branch
DECLARE @HnId BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'BR_HN');

-- HR Department HN
IF NOT EXISTS (SELECT 1 FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_HR_HN')
BEGIN
    INSERT INTO dbo.hr_org_unit (parent_org_unit_id, org_unit_code, org_unit_name, org_unit_type, status, effective_from, hierarchy_level, path_code, sort_order)
    VALUES (@HnId, 'DEPT_HR_HN', N'Human Resources Dept (HN)', 'DEPARTMENT', 'ACTIVE', '2020-01-01', 3, '/CORP/BR_HN/DEPT_HR_HN/', 1);
END

-- IT Department HN
IF NOT EXISTS (SELECT 1 FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_IT_HN')
BEGIN
    INSERT INTO dbo.hr_org_unit (parent_org_unit_id, org_unit_code, org_unit_name, org_unit_type, status, effective_from, hierarchy_level, path_code, sort_order)
    VALUES (@HnId, 'DEPT_IT_HN', N'Information Technology Dept (HN)', 'DEPARTMENT', 'ACTIVE', '2020-01-01', 3, '/CORP/BR_HN/DEPT_IT_HN/', 2);
END
GO

-- 4. Create Departments for HCMC Branch
DECLARE @HcmId BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'BR_HCM');

-- Sales Department HCM
IF NOT EXISTS (SELECT 1 FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_SALES_HCM')
BEGIN
    INSERT INTO dbo.hr_org_unit (parent_org_unit_id, org_unit_code, org_unit_name, org_unit_type, status, effective_from, hierarchy_level, path_code, sort_order)
    VALUES (@HcmId, 'DEPT_SALES_HCM', N'Sales Dept (HCM)', 'DEPARTMENT', 'ACTIVE', '2020-01-01', 3, '/CORP/BR_HCM/DEPT_SALES_HCM/', 1);
END

-- Finance Department HCM
IF NOT EXISTS (SELECT 1 FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_FIN_HCM')
BEGIN
    INSERT INTO dbo.hr_org_unit (parent_org_unit_id, org_unit_code, org_unit_name, org_unit_type, status, effective_from, hierarchy_level, path_code, sort_order)
    VALUES (@HcmId, 'DEPT_FIN_HCM', N'Finance & Accounting Dept (HCM)', 'DEPARTMENT', 'ACTIVE', '2020-01-01', 3, '/CORP/BR_HCM/DEPT_FIN_HCM/', 2);
END
GO

SET NOCOUNT OFF;
GO
