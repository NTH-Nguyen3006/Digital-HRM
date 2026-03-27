SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V004__employee_profile_detail.sql
   Scope:
   - hr_employee_profile
   - hr_employee_address
   - hr_employee_emergency_contact
   - hr_employee_identification
   - hr_employee_bank_account
   - hr_employee_document
   ========================================================= */

IF OBJECT_ID(N'dbo.hr_employee_profile', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_employee_profile (
        employee_profile_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_employee_profile PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        first_name NVARCHAR(100) NULL,
        middle_name NVARCHAR(100) NULL,
        last_name NVARCHAR(100) NULL,
        marital_status VARCHAR(20) NULL,
        nationality NVARCHAR(100) NULL,
        place_of_birth NVARCHAR(200) NULL,
        ethnic_group NVARCHAR(100) NULL,
        religion NVARCHAR(100) NULL,
        education_level NVARCHAR(200) NULL,
        major NVARCHAR(200) NULL,
        emergency_note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_profile_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_hr_employee_profile_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_hr_employee_profile_employee
            FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_hr_employee_profile_marital_status CHECK (marital_status IS NULL OR marital_status IN ('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_hr_employee_profile_employee' AND object_id = OBJECT_ID(N'dbo.hr_employee_profile')
)
BEGIN
    CREATE UNIQUE INDEX UX_hr_employee_profile_employee
        ON dbo.hr_employee_profile(employee_id)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.hr_employee_address', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_employee_address (
        employee_address_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_employee_address PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        address_type VARCHAR(20) NOT NULL,
        country_name NVARCHAR(100) NULL,
        province_name NVARCHAR(100) NULL,
        district_name NVARCHAR(100) NULL,
        ward_name NVARCHAR(100) NULL,
        address_line NVARCHAR(300) NOT NULL,
        postal_code VARCHAR(20) NULL,
        is_primary BIT NOT NULL
            CONSTRAINT DF_hr_employee_address_is_primary DEFAULT 0,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_address_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_hr_employee_address_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_hr_employee_address_employee
            FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_hr_employee_address_type CHECK (address_type IN ('PERMANENT', 'CURRENT', 'TEMPORARY'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_employee_address_employee' AND object_id = OBJECT_ID(N'dbo.hr_employee_address')
)
BEGIN
    CREATE INDEX IX_hr_employee_address_employee
        ON dbo.hr_employee_address(employee_id, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.hr_employee_emergency_contact', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_employee_emergency_contact (
        emergency_contact_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_employee_emergency_contact PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        contact_name NVARCHAR(200) NOT NULL,
        relationship_code VARCHAR(30) NOT NULL,
        phone_number VARCHAR(20) NOT NULL,
        email VARCHAR(150) NULL,
        address_line NVARCHAR(300) NULL,
        is_primary BIT NOT NULL
            CONSTRAINT DF_hr_employee_emergency_contact_is_primary DEFAULT 1,
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_emergency_contact_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_hr_employee_emergency_contact_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_hr_employee_emergency_contact_employee
            FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_hr_employee_emergency_contact_relationship CHECK (relationship_code IN ('FATHER', 'MOTHER', 'SPOUSE', 'SIBLING', 'OTHER'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_employee_emergency_contact_employee' AND object_id = OBJECT_ID(N'dbo.hr_employee_emergency_contact')
)
BEGIN
    CREATE INDEX IX_hr_employee_emergency_contact_employee
        ON dbo.hr_employee_emergency_contact(employee_id, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.hr_employee_identification', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_employee_identification (
        employee_identification_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_employee_identification PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        document_type VARCHAR(30) NOT NULL,
        document_number VARCHAR(50) NOT NULL,
        issue_date DATE NULL,
        issue_place NVARCHAR(200) NULL,
        expiry_date DATE NULL,
        country_of_issue NVARCHAR(100) NULL,
        is_primary BIT NOT NULL
            CONSTRAINT DF_hr_employee_identification_is_primary DEFAULT 1,
        status VARCHAR(20) NOT NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_identification_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_hr_employee_identification_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_hr_employee_identification_employee
            FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_hr_employee_identification_document_type CHECK (document_type IN ('CCCD', 'CMND', 'PASSPORT', 'TAX', 'OTHER')),
        CONSTRAINT CK_hr_employee_identification_status CHECK (status IN ('VALID', 'EXPIRED', 'REVOKED')),
        CONSTRAINT CK_hr_employee_identification_expiry CHECK (expiry_date IS NULL OR issue_date IS NULL OR expiry_date >= issue_date)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_hr_employee_identification_doc_number' AND object_id = OBJECT_ID(N'dbo.hr_employee_identification')
)
BEGIN
    CREATE UNIQUE INDEX UX_hr_employee_identification_doc_number
        ON dbo.hr_employee_identification(document_type, document_number)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_employee_identification_employee' AND object_id = OBJECT_ID(N'dbo.hr_employee_identification')
)
BEGIN
    CREATE INDEX IX_hr_employee_identification_employee
        ON dbo.hr_employee_identification(employee_id, status, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.hr_employee_bank_account', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_employee_bank_account (
        employee_bank_account_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_employee_bank_account PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        bank_name NVARCHAR(200) NOT NULL,
        bank_code VARCHAR(30) NULL,
        account_number VARCHAR(50) NOT NULL,
        account_holder_name NVARCHAR(200) NOT NULL,
        branch_name NVARCHAR(200) NULL,
        is_primary BIT NOT NULL
            CONSTRAINT DF_hr_employee_bank_account_is_primary DEFAULT 1,
        status VARCHAR(20) NOT NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_bank_account_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_hr_employee_bank_account_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_hr_employee_bank_account_employee
            FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_hr_employee_bank_account_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_employee_bank_account_employee' AND object_id = OBJECT_ID(N'dbo.hr_employee_bank_account')
)
BEGIN
    CREATE INDEX IX_hr_employee_bank_account_employee
        ON dbo.hr_employee_bank_account(employee_id, status, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.hr_employee_document', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_employee_document (
        employee_document_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_employee_document PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        document_category VARCHAR(30) NOT NULL,
        document_name NVARCHAR(255) NOT NULL,
        storage_path NVARCHAR(500) NOT NULL,
        mime_type VARCHAR(100) NULL,
        file_size_bytes BIGINT NULL,
        uploaded_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_document_uploaded_at DEFAULT SYSDATETIME(),
        uploaded_by UNIQUEIDENTIFIER NULL,
        status VARCHAR(20) NOT NULL,
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_document_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_hr_employee_document_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_hr_employee_document_employee
            FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_hr_employee_document_category CHECK (document_category IN ('PROFILE', 'IDENTITY', 'CERTIFICATE', 'PHOTO', 'BANK', 'OTHER')),
        CONSTRAINT CK_hr_employee_document_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_employee_document_employee' AND object_id = OBJECT_ID(N'dbo.hr_employee_document')
)
BEGIN
    CREATE INDEX IX_hr_employee_document_employee
        ON dbo.hr_employee_document(employee_id, document_category, status, is_deleted);
END
GO
