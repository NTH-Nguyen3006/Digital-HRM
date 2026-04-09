SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_ct_contract_attachment_type'
      AND parent_object_id = OBJECT_ID(N'dbo.ct_contract_attachment')
)
BEGIN
    ALTER TABLE dbo.ct_contract_attachment DROP CONSTRAINT CK_ct_contract_attachment_type;
END
GO

ALTER TABLE dbo.ct_contract_attachment
ADD CONSTRAINT CK_ct_contract_attachment_type
CHECK (attachment_type IN ('CONTRACT_FILE', 'APPENDIX_FILE', 'SIGNED_SCAN', 'IMPORT_SOURCE', 'OTHER'));
GO

IF OBJECT_ID(N'dbo.ct_contract_import_session', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ct_contract_import_session (
        contract_import_session_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_ct_contract_import_session PRIMARY KEY,
        contract_template_id BIGINT NULL,
        confirmed_labor_contract_id BIGINT NULL,
        source_file_key VARCHAR(120) NOT NULL,
        source_file_name NVARCHAR(255) NOT NULL,
        source_mime_type VARCHAR(100) NULL,
        source_file_size_bytes BIGINT NULL,
        source_type VARCHAR(20) NOT NULL,
        status VARCHAR(30) NOT NULL,
        extracted_raw_text NVARCHAR(MAX) NULL,
        mapping_json NVARCHAR(MAX) NULL,
        confidence_score DECIMAL(5,2) NULL,
        note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_ct_contract_import_session_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_ct_contract_import_session_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_ct_contract_import_session_template FOREIGN KEY (contract_template_id)
            REFERENCES dbo.ct_contract_template(contract_template_id),
        CONSTRAINT FK_ct_contract_import_session_contract FOREIGN KEY (confirmed_labor_contract_id)
            REFERENCES dbo.ct_labor_contract(labor_contract_id),
        CONSTRAINT FK_ct_contract_import_session_created_by FOREIGN KEY (created_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_ct_contract_import_session_updated_by FOREIGN KEY (updated_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_ct_contract_import_session_source_type CHECK (source_type IN ('DOCX')),
        CONSTRAINT CK_ct_contract_import_session_status CHECK (status IN ('REVIEW_REQUIRED', 'CONFIRMED', 'CANCELLED', 'FAILED')),
        CONSTRAINT CK_ct_contract_import_session_file_size CHECK (source_file_size_bytes IS NULL OR source_file_size_bytes >= 0),
        CONSTRAINT CK_ct_contract_import_session_confidence CHECK (confidence_score IS NULL OR (confidence_score >= 0 AND confidence_score <= 100))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ct_contract_import_session_status' AND object_id = OBJECT_ID(N'dbo.ct_contract_import_session')
)
BEGIN
    CREATE INDEX IX_ct_contract_import_session_status
        ON dbo.ct_contract_import_session(status, created_at DESC, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.ct_contract_import_field', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ct_contract_import_field (
        contract_import_field_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_ct_contract_import_field PRIMARY KEY,
        contract_import_session_id BIGINT NOT NULL,
        field_code VARCHAR(50) NOT NULL,
        field_label NVARCHAR(100) NOT NULL,
        raw_value NVARCHAR(1000) NULL,
        normalized_value NVARCHAR(1000) NULL,
        confidence_score DECIMAL(5,2) NULL,
        source_snippet NVARCHAR(1000) NULL,
        display_order INT NOT NULL,
        is_confirmed_by_hr BIT NOT NULL
            CONSTRAINT DF_ct_contract_import_field_confirmed DEFAULT 0,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_ct_contract_import_field_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_ct_contract_import_field_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_ct_contract_import_field_session FOREIGN KEY (contract_import_session_id)
            REFERENCES dbo.ct_contract_import_session(contract_import_session_id),
        CONSTRAINT FK_ct_contract_import_field_created_by FOREIGN KEY (created_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_ct_contract_import_field_updated_by FOREIGN KEY (updated_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_ct_contract_import_field_confidence CHECK (confidence_score IS NULL OR (confidence_score >= 0 AND confidence_score <= 100))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_ct_contract_import_field_session_code' AND object_id = OBJECT_ID(N'dbo.ct_contract_import_field')
)
BEGIN
    CREATE UNIQUE INDEX UX_ct_contract_import_field_session_code
        ON dbo.ct_contract_import_field(contract_import_session_id, field_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ct_contract_import_field_session' AND object_id = OBJECT_ID(N'dbo.ct_contract_import_field')
)
BEGIN
    CREATE INDEX IX_ct_contract_import_field_session
        ON dbo.ct_contract_import_field(contract_import_session_id, display_order, is_deleted);
END
GO

SET NOCOUNT OFF;
GO
