SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__101__seed_job_titles.sql
   Scope:
   - Seed common job titles across different levels
   ========================================================= */

MERGE INTO dbo.hr_job_title AS TARGET
USING (
    VALUES 
        ('DIR_HR', N'HR Director', 'L1', N'Giám đốc nhân sự', 'ACTIVE', 1),
        ('MGR_HR', N'HR Manager', 'L2', N'Trưởng phòng nhân sự', 'ACTIVE', 2),
        ('MGR_IT', N'IT Manager', 'L2', N'Trưởng phòng IT', 'ACTIVE', 3),
        ('MGR_SALES', N'Sales Manager', 'L2', N'Trưởng phòng kinh doanh', 'ACTIVE', 4),
        ('SDR_DEV', N'Senior Software Developer', 'L3', N'Lập trình viên cao cấp', 'ACTIVE', 5),
        ('JDR_DEV', N'Junior Software Developer', 'L4', N'Lập trình viên', 'ACTIVE', 6),
        ('SSR_ACC', N'Senior Accountant', 'L3', N'Kế toán tổng hợp', 'ACTIVE', 7),
        ('EXEC_SALES', N'Sales Executive', 'L4', N'Chuyên viên kinh doanh', 'ACTIVE', 8)
) AS SOURCE (job_title_code, job_title_name, job_level_code, description, status, sort_order)
ON TARGET.job_title_code = SOURCE.job_title_code
WHEN MATCHED THEN
    UPDATE SET 
        TARGET.job_title_name = SOURCE.job_title_name,
        TARGET.job_level_code = SOURCE.job_level_code,
        TARGET.description = SOURCE.description,
        TARGET.status = SOURCE.status,
        TARGET.sort_order = SOURCE.sort_order,
        TARGET.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (job_title_code, job_title_name, job_level_code, description, status, sort_order, created_at, is_deleted)
    VALUES (SOURCE.job_title_code, SOURCE.job_title_name, SOURCE.job_level_code, SOURCE.description, SOURCE.status, SOURCE.sort_order, SYSDATETIME(), 0);
GO

SET NOCOUNT OFF;
GO
