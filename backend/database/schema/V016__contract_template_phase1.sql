SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.ct_contract_template', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ct_contract_template (
        contract_template_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_ct_contract_template PRIMARY KEY,
        template_code VARCHAR(50) NOT NULL,
        template_name NVARCHAR(200) NOT NULL,
        document_type VARCHAR(30) NOT NULL,
        template_file_key VARCHAR(120) NULL,
        html_template NVARCHAR(MAX) NOT NULL,
        status VARCHAR(20) NOT NULL,
        description NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_ct_contract_template_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_ct_contract_template_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_ct_contract_template_created_by FOREIGN KEY (created_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_ct_contract_template_updated_by FOREIGN KEY (updated_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_ct_contract_template_document_type CHECK (document_type IN ('LABOR_CONTRACT')),
        CONSTRAINT CK_ct_contract_template_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_ct_contract_template_code' AND object_id = OBJECT_ID(N'dbo.ct_contract_template')
)
BEGIN
    CREATE UNIQUE INDEX UX_ct_contract_template_code
        ON dbo.ct_contract_template(template_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_ct_contract_template_status' AND object_id = OBJECT_ID(N'dbo.ct_contract_template')
)
BEGIN
    CREATE INDEX IX_ct_contract_template_status
        ON dbo.ct_contract_template(document_type, status, is_deleted, template_name);
END
GO

SET NOCOUNT OFF;
GO
