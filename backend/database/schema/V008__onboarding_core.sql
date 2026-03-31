SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V008__onboarding_core.sql
   Scope:
   - onboarding master
   - onboarding checklist
   - onboarding document
   - onboarding asset allocation
   ========================================================= */

IF OBJECT_ID(N'dbo.onb_onboarding', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.onb_onboarding (
        onboarding_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_onb_onboarding PRIMARY KEY,
        onboarding_code VARCHAR(30) NOT NULL,
        full_name NVARCHAR(200) NOT NULL,
        personal_email VARCHAR(150) NULL,
        personal_phone VARCHAR(20) NULL,
        gender_code VARCHAR(10) NOT NULL,
        date_of_birth DATE NOT NULL,
        planned_start_date DATE NOT NULL,
        employee_code VARCHAR(30) NULL,
        org_unit_id BIGINT NOT NULL,
        job_title_id BIGINT NOT NULL,
        manager_employee_id BIGINT NULL,
        work_location NVARCHAR(200) NULL,
        linked_employee_id BIGINT NULL,
        linked_user_id UNIQUEIDENTIFIER NULL,
        first_contract_id BIGINT NULL,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_onb_onboarding_status DEFAULT 'DRAFT',
        note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_onb_onboarding_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_onb_onboarding_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_onb_onboarding_org_unit FOREIGN KEY (org_unit_id) REFERENCES dbo.hr_org_unit(org_unit_id),
        CONSTRAINT FK_onb_onboarding_job_title FOREIGN KEY (job_title_id) REFERENCES dbo.hr_job_title(job_title_id),
        CONSTRAINT FK_onb_onboarding_manager FOREIGN KEY (manager_employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_onb_onboarding_employee FOREIGN KEY (linked_employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_onb_onboarding_user FOREIGN KEY (linked_user_id) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_onb_onboarding_contract FOREIGN KEY (first_contract_id) REFERENCES dbo.ct_labor_contract(labor_contract_id),
        CONSTRAINT CK_onb_onboarding_gender CHECK (gender_code IN ('MALE', 'FEMALE', 'OTHER')),
        CONSTRAINT CK_onb_onboarding_status CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'READY_FOR_JOIN', 'COMPLETED', 'CANCELLED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_onb_onboarding_code' AND object_id = OBJECT_ID(N'dbo.onb_onboarding')
)
BEGIN
    CREATE UNIQUE INDEX UX_onb_onboarding_code
        ON dbo.onb_onboarding(onboarding_code)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.onb_onboarding_checklist', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.onb_onboarding_checklist (
        onboarding_checklist_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_onb_onboarding_checklist PRIMARY KEY,
        onboarding_id BIGINT NOT NULL,
        item_code VARCHAR(50) NOT NULL,
        item_name NVARCHAR(255) NOT NULL,
        is_required BIT NOT NULL
            CONSTRAINT DF_onb_onboarding_checklist_is_required DEFAULT 1,
        is_completed BIT NOT NULL
            CONSTRAINT DF_onb_onboarding_checklist_is_completed DEFAULT 0,
        due_date DATE NULL,
        completed_at DATETIME2(0) NULL,
        completed_by UNIQUEIDENTIFIER NULL,
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_onb_onboarding_checklist_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_onb_onboarding_checklist_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_onb_onboarding_checklist_onboarding FOREIGN KEY (onboarding_id) REFERENCES dbo.onb_onboarding(onboarding_id),
        CONSTRAINT FK_onb_onboarding_checklist_completed_by FOREIGN KEY (completed_by) REFERENCES dbo.sec_user_account(user_id)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_onb_onboarding_checklist_onboarding' AND object_id = OBJECT_ID(N'dbo.onb_onboarding_checklist')
)
BEGIN
    CREATE INDEX IX_onb_onboarding_checklist_onboarding
        ON dbo.onb_onboarding_checklist(onboarding_id, is_deleted, is_completed, due_date);
END
GO

IF OBJECT_ID(N'dbo.onb_onboarding_document', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.onb_onboarding_document (
        onboarding_document_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_onb_onboarding_document PRIMARY KEY,
        onboarding_id BIGINT NOT NULL,
        document_name NVARCHAR(255) NOT NULL,
        document_category VARCHAR(30) NOT NULL,
        storage_path NVARCHAR(500) NOT NULL,
        mime_type VARCHAR(100) NULL,
        file_size_bytes BIGINT NULL,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_onb_onboarding_document_status DEFAULT 'ACTIVE',
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_onb_onboarding_document_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_onb_onboarding_document_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_onb_onboarding_document_onboarding FOREIGN KEY (onboarding_id) REFERENCES dbo.onb_onboarding(onboarding_id),
        CONSTRAINT CK_onb_onboarding_document_category CHECK (document_category IN ('PROFILE', 'IDENTITY', 'CERTIFICATE', 'OTHER')),
        CONSTRAINT CK_onb_onboarding_document_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_onb_onboarding_document_onboarding' AND object_id = OBJECT_ID(N'dbo.onb_onboarding_document')
)
BEGIN
    CREATE INDEX IX_onb_onboarding_document_onboarding
        ON dbo.onb_onboarding_document(onboarding_id, is_deleted, status);
END
GO

IF OBJECT_ID(N'dbo.onb_onboarding_asset', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.onb_onboarding_asset (
        onboarding_asset_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_onb_onboarding_asset PRIMARY KEY,
        onboarding_id BIGINT NOT NULL,
        asset_code VARCHAR(50) NOT NULL,
        asset_name NVARCHAR(255) NOT NULL,
        asset_type VARCHAR(50) NOT NULL,
        assigned_date DATE NULL,
        returned_date DATE NULL,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_onb_onboarding_asset_status DEFAULT 'PLANNED',
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_onb_onboarding_asset_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_onb_onboarding_asset_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_onb_onboarding_asset_onboarding FOREIGN KEY (onboarding_id) REFERENCES dbo.onb_onboarding(onboarding_id),
        CONSTRAINT CK_onb_onboarding_asset_status CHECK (status IN ('PLANNED', 'ASSIGNED', 'RETURNED', 'CANCELLED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_onb_onboarding_asset_onboarding' AND object_id = OBJECT_ID(N'dbo.onb_onboarding_asset')
)
BEGIN
    CREATE INDEX IX_onb_onboarding_asset_onboarding
        ON dbo.onb_onboarding_asset(onboarding_id, is_deleted, status);
END
GO

SET NOCOUNT OFF;
GO
