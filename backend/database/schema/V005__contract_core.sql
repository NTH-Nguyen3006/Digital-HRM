SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V005__contract_core.sql
   Scope:
   - ct_contract_type
   - ct_labor_contract
   - ct_contract_appendix
   - ct_contract_status_history
   - ct_contract_attachment
   ========================================================= */

IF OBJECT_ID(N'dbo.ct_contract_type', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ct_contract_type (
        contract_type_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_ct_contract_type PRIMARY KEY,
        contract_type_code VARCHAR(30) NOT NULL,
        contract_type_name NVARCHAR(200) NOT NULL,
        max_term_months INT NULL,
        requires_end_date BIT NOT NULL
            CONSTRAINT DF_ct_contract_type_requires_end_date DEFAULT 1,
        status VARCHAR(20) NOT NULL,
        description NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_ct_contract_type_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_ct_contract_type_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_ct_contract_type_created_by FOREIGN KEY (created_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_ct_contract_type_updated_by FOREIGN KEY (updated_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_ct_contract_type_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
        CONSTRAINT CK_ct_contract_type_max_term CHECK (max_term_months IS NULL OR max_term_months > 0)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_ct_contract_type_code' AND object_id = OBJECT_ID(N'dbo.ct_contract_type')
)
BEGIN
    CREATE UNIQUE INDEX UX_ct_contract_type_code
        ON dbo.ct_contract_type(contract_type_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ct_contract_type_status' AND object_id = OBJECT_ID(N'dbo.ct_contract_type')
)
BEGIN
    CREATE INDEX IX_ct_contract_type_status
        ON dbo.ct_contract_type(status, is_deleted, contract_type_name);
END
GO

IF OBJECT_ID(N'dbo.ct_labor_contract', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ct_labor_contract (
        labor_contract_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_ct_labor_contract PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        contract_type_id BIGINT NOT NULL,
        contract_number VARCHAR(50) NOT NULL,
        sign_date DATE NOT NULL,
        effective_date DATE NOT NULL,
        end_date DATE NULL,
        job_title_id BIGINT NOT NULL,
        org_unit_id BIGINT NOT NULL,
        work_location NVARCHAR(200) NULL,
        base_salary DECIMAL(18,2) NOT NULL,
        salary_currency VARCHAR(10) NOT NULL
            CONSTRAINT DF_ct_labor_contract_salary_currency DEFAULT 'VND',
        working_type VARCHAR(20) NOT NULL,
        contract_status VARCHAR(20) NOT NULL,
        signed_by_company_user_id UNIQUEIDENTIFIER NULL,
        note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_ct_labor_contract_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_ct_labor_contract_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_ct_labor_contract_employee FOREIGN KEY (employee_id)
            REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_ct_labor_contract_contract_type FOREIGN KEY (contract_type_id)
            REFERENCES dbo.ct_contract_type(contract_type_id),
        CONSTRAINT FK_ct_labor_contract_job_title FOREIGN KEY (job_title_id)
            REFERENCES dbo.hr_job_title(job_title_id),
        CONSTRAINT FK_ct_labor_contract_org_unit FOREIGN KEY (org_unit_id)
            REFERENCES dbo.hr_org_unit(org_unit_id),
        CONSTRAINT FK_ct_labor_contract_signed_by_company_user FOREIGN KEY (signed_by_company_user_id)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_ct_labor_contract_created_by FOREIGN KEY (created_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_ct_labor_contract_updated_by FOREIGN KEY (updated_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_ct_labor_contract_working_type CHECK (working_type IN ('FULL_TIME', 'PART_TIME')),
        CONSTRAINT CK_ct_labor_contract_status CHECK (contract_status IN ('DRAFT', 'PENDING_SIGN', 'ACTIVE', 'EXPIRED', 'TERMINATED', 'SUSPENDED')),
        CONSTRAINT CK_ct_labor_contract_date_range CHECK (end_date IS NULL OR end_date >= effective_date),
        CONSTRAINT CK_ct_labor_contract_sign_date CHECK (sign_date <= effective_date),
        CONSTRAINT CK_ct_labor_contract_base_salary CHECK (base_salary > 0)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_ct_labor_contract_number' AND object_id = OBJECT_ID(N'dbo.ct_labor_contract')
)
BEGIN
    CREATE UNIQUE INDEX UX_ct_labor_contract_number
        ON dbo.ct_labor_contract(contract_number)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ct_labor_contract_employee_status' AND object_id = OBJECT_ID(N'dbo.ct_labor_contract')
)
BEGIN
    CREATE INDEX IX_ct_labor_contract_employee_status
        ON dbo.ct_labor_contract(employee_id, contract_status, is_deleted, effective_date DESC);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ct_labor_contract_type_status' AND object_id = OBJECT_ID(N'dbo.ct_labor_contract')
)
BEGIN
    CREATE INDEX IX_ct_labor_contract_type_status
        ON dbo.ct_labor_contract(contract_type_id, contract_status, is_deleted);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ct_labor_contract_expiry' AND object_id = OBJECT_ID(N'dbo.ct_labor_contract')
)
BEGIN
    CREATE INDEX IX_ct_labor_contract_expiry
        ON dbo.ct_labor_contract(contract_status, end_date, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.ct_contract_appendix', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ct_contract_appendix (
        contract_appendix_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_ct_contract_appendix PRIMARY KEY,
        labor_contract_id BIGINT NOT NULL,
        appendix_number VARCHAR(50) NOT NULL,
        appendix_name NVARCHAR(255) NOT NULL,
        effective_date DATE NOT NULL,
        changed_fields_json NVARCHAR(MAX) NULL,
        status VARCHAR(20) NOT NULL,
        note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_ct_contract_appendix_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_ct_contract_appendix_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_ct_contract_appendix_contract FOREIGN KEY (labor_contract_id)
            REFERENCES dbo.ct_labor_contract(labor_contract_id),
        CONSTRAINT FK_ct_contract_appendix_created_by FOREIGN KEY (created_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_ct_contract_appendix_updated_by FOREIGN KEY (updated_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_ct_contract_appendix_status CHECK (status IN ('DRAFT', 'ACTIVE', 'CANCELLED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_ct_contract_appendix_number' AND object_id = OBJECT_ID(N'dbo.ct_contract_appendix')
)
BEGIN
    CREATE UNIQUE INDEX UX_ct_contract_appendix_number
        ON dbo.ct_contract_appendix(labor_contract_id, appendix_number)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ct_contract_appendix_contract' AND object_id = OBJECT_ID(N'dbo.ct_contract_appendix')
)
BEGIN
    CREATE INDEX IX_ct_contract_appendix_contract
        ON dbo.ct_contract_appendix(labor_contract_id, status, effective_date, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.ct_contract_status_history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ct_contract_status_history (
        contract_status_history_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_ct_contract_status_history PRIMARY KEY,
        labor_contract_id BIGINT NOT NULL,
        from_status VARCHAR(20) NULL,
        to_status VARCHAR(20) NOT NULL,
        changed_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_ct_contract_status_history_changed_at DEFAULT SYSDATETIME(),
        changed_by UNIQUEIDENTIFIER NULL,
        reason NVARCHAR(1000) NULL,
        CONSTRAINT FK_ct_contract_status_history_contract FOREIGN KEY (labor_contract_id)
            REFERENCES dbo.ct_labor_contract(labor_contract_id),
        CONSTRAINT FK_ct_contract_status_history_changed_by FOREIGN KEY (changed_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_ct_contract_status_history_from_status CHECK (from_status IS NULL OR from_status IN ('DRAFT', 'PENDING_SIGN', 'ACTIVE', 'EXPIRED', 'TERMINATED', 'SUSPENDED')),
        CONSTRAINT CK_ct_contract_status_history_to_status CHECK (to_status IN ('DRAFT', 'PENDING_SIGN', 'ACTIVE', 'EXPIRED', 'TERMINATED', 'SUSPENDED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ct_contract_status_history_contract' AND object_id = OBJECT_ID(N'dbo.ct_contract_status_history')
)
BEGIN
    CREATE INDEX IX_ct_contract_status_history_contract
        ON dbo.ct_contract_status_history(labor_contract_id, changed_at DESC);
END
GO

IF OBJECT_ID(N'dbo.ct_contract_attachment', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ct_contract_attachment (
        contract_attachment_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_ct_contract_attachment PRIMARY KEY,
        labor_contract_id BIGINT NOT NULL,
        attachment_type VARCHAR(30) NOT NULL,
        file_name NVARCHAR(255) NOT NULL,
        storage_path NVARCHAR(500) NOT NULL,
        mime_type VARCHAR(100) NULL,
        file_size_bytes BIGINT NULL,
        uploaded_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_ct_contract_attachment_uploaded_at DEFAULT SYSDATETIME(),
        uploaded_by UNIQUEIDENTIFIER NULL,
        status VARCHAR(20) NOT NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_ct_contract_attachment_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_ct_contract_attachment_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_ct_contract_attachment_contract FOREIGN KEY (labor_contract_id)
            REFERENCES dbo.ct_labor_contract(labor_contract_id),
        CONSTRAINT FK_ct_contract_attachment_uploaded_by FOREIGN KEY (uploaded_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_ct_contract_attachment_created_by FOREIGN KEY (created_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_ct_contract_attachment_updated_by FOREIGN KEY (updated_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_ct_contract_attachment_type CHECK (attachment_type IN ('CONTRACT_FILE', 'APPENDIX_FILE', 'SIGNED_SCAN', 'OTHER')),
        CONSTRAINT CK_ct_contract_attachment_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
        CONSTRAINT CK_ct_contract_attachment_file_size CHECK (file_size_bytes IS NULL OR file_size_bytes >= 0)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ct_contract_attachment_contract' AND object_id = OBJECT_ID(N'dbo.ct_contract_attachment')
)
BEGIN
    CREATE INDEX IX_ct_contract_attachment_contract
        ON dbo.ct_contract_attachment(labor_contract_id, status, uploaded_at DESC, is_deleted);
END
GO

SET NOCOUNT OFF;
GO
