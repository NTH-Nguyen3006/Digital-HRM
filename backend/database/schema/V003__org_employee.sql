SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V003__org_employee.sql
   Scope:
   - hr_org_unit
   - hr_job_title
   - hr_employee
   - sec_user_account.employee_id FK -> hr_employee.employee_id
   ========================================================= */

IF OBJECT_ID(N'dbo.hr_org_unit', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_org_unit (
        org_unit_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_org_unit PRIMARY KEY,
        parent_org_unit_id BIGINT NULL,
        org_unit_code VARCHAR(30) NOT NULL,
        org_unit_name NVARCHAR(200) NOT NULL,
        org_unit_type VARCHAR(30) NOT NULL,
        manager_employee_id BIGINT NULL,
        hierarchy_level INT NOT NULL
            CONSTRAINT DF_hr_org_unit_hierarchy_level DEFAULT 1,
        path_code VARCHAR(500) NULL,
        sort_order INT NOT NULL
            CONSTRAINT DF_hr_org_unit_sort_order DEFAULT 0,
        status VARCHAR(20) NOT NULL,
        effective_from DATE NOT NULL,
        effective_to DATE NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_org_unit_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_hr_org_unit_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT CK_hr_org_unit_type CHECK (org_unit_type IN ('COMPANY', 'BRANCH', 'DIVISION', 'DEPARTMENT', 'TEAM')),
        CONSTRAINT CK_hr_org_unit_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
        CONSTRAINT CK_hr_org_unit_effective CHECK (effective_to IS NULL OR effective_to >= effective_from)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.key_constraints
    WHERE name = N'FK_hr_org_unit_parent' AND parent_object_id = OBJECT_ID(N'dbo.hr_org_unit')
)
BEGIN
    ALTER TABLE dbo.hr_org_unit
        ADD CONSTRAINT FK_hr_org_unit_parent
            FOREIGN KEY (parent_org_unit_id) REFERENCES dbo.hr_org_unit(org_unit_id);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_hr_org_unit_code' AND object_id = OBJECT_ID(N'dbo.hr_org_unit')
)
BEGIN
    CREATE UNIQUE INDEX UX_hr_org_unit_code
        ON dbo.hr_org_unit(org_unit_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_org_unit_parent' AND object_id = OBJECT_ID(N'dbo.hr_org_unit')
)
BEGIN
    CREATE INDEX IX_hr_org_unit_parent
        ON dbo.hr_org_unit(parent_org_unit_id, sort_order, is_deleted);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_org_unit_path_code' AND object_id = OBJECT_ID(N'dbo.hr_org_unit')
)
BEGIN
    CREATE INDEX IX_hr_org_unit_path_code
        ON dbo.hr_org_unit(path_code);
END
GO

IF OBJECT_ID(N'dbo.hr_job_title', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_job_title (
        job_title_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_job_title PRIMARY KEY,
        job_title_code VARCHAR(30) NOT NULL,
        job_title_name NVARCHAR(200) NOT NULL,
        job_level_code VARCHAR(30) NULL,
        description NVARCHAR(1000) NULL,
        status VARCHAR(20) NOT NULL,
        sort_order INT NOT NULL
            CONSTRAINT DF_hr_job_title_sort_order DEFAULT 0,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_job_title_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_hr_job_title_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT CK_hr_job_title_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_hr_job_title_code' AND object_id = OBJECT_ID(N'dbo.hr_job_title')
)
BEGIN
    CREATE UNIQUE INDEX UX_hr_job_title_code
        ON dbo.hr_job_title(job_title_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_job_title_status' AND object_id = OBJECT_ID(N'dbo.hr_job_title')
)
BEGIN
    CREATE INDEX IX_hr_job_title_status
        ON dbo.hr_job_title(status, sort_order, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.hr_employee', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_employee (
        employee_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_employee PRIMARY KEY,
        employee_code VARCHAR(30) NOT NULL,
        org_unit_id BIGINT NOT NULL,
        job_title_id BIGINT NOT NULL,
        manager_employee_id BIGINT NULL,
        full_name NVARCHAR(200) NOT NULL,
        work_email VARCHAR(150) NULL,
        work_phone VARCHAR(20) NULL,
        gender_code VARCHAR(10) NOT NULL,
        date_of_birth DATE NOT NULL,
        hire_date DATE NOT NULL,
        employment_status VARCHAR(30) NOT NULL,
        work_location NVARCHAR(200) NULL,
        tax_code VARCHAR(30) NULL,
        personal_email VARCHAR(150) NULL,
        mobile_phone VARCHAR(20) NULL,
        avatar_url NVARCHAR(500) NULL,
        note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_hr_employee_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_hr_employee_org_unit
            FOREIGN KEY (org_unit_id) REFERENCES dbo.hr_org_unit(org_unit_id),
        CONSTRAINT FK_hr_employee_job_title
            FOREIGN KEY (job_title_id) REFERENCES dbo.hr_job_title(job_title_id),
        CONSTRAINT FK_hr_employee_manager
            FOREIGN KEY (manager_employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_hr_employee_gender CHECK (gender_code IN ('MALE', 'FEMALE', 'OTHER')),
        CONSTRAINT CK_hr_employee_status CHECK (employment_status IN ('PROBATION', 'ACTIVE', 'SUSPENDED', 'RESIGNED', 'TERMINATED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_hr_employee_code' AND object_id = OBJECT_ID(N'dbo.hr_employee')
)
BEGIN
    CREATE UNIQUE INDEX UX_hr_employee_code
        ON dbo.hr_employee(employee_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_hr_employee_work_email' AND object_id = OBJECT_ID(N'dbo.hr_employee')
)
BEGIN
    CREATE UNIQUE INDEX UX_hr_employee_work_email
        ON dbo.hr_employee(work_email)
        WHERE work_email IS NOT NULL AND is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_employee_org_unit' AND object_id = OBJECT_ID(N'dbo.hr_employee')
)
BEGIN
    CREATE INDEX IX_hr_employee_org_unit
        ON dbo.hr_employee(org_unit_id, employment_status, is_deleted);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_employee_manager' AND object_id = OBJECT_ID(N'dbo.hr_employee')
)
BEGIN
    CREATE INDEX IX_hr_employee_manager
        ON dbo.hr_employee(manager_employee_id, is_deleted);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.key_constraints
    WHERE name = N'FK_hr_org_unit_manager_employee' AND parent_object_id = OBJECT_ID(N'dbo.hr_org_unit')
)
BEGIN
    ALTER TABLE dbo.hr_org_unit
        ADD CONSTRAINT FK_hr_org_unit_manager_employee
            FOREIGN KEY (manager_employee_id) REFERENCES dbo.hr_employee(employee_id);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.key_constraints
    WHERE name = N'FK_sec_user_account_employee' AND parent_object_id = OBJECT_ID(N'dbo.sec_user_account')
)
BEGIN
    ALTER TABLE dbo.sec_user_account
        ADD CONSTRAINT FK_sec_user_account_employee
            FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id);
END
GO
