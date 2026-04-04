SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V010__storage_foundation.sql
   Scope:
   - sys_stored_file
   - physical file storage metadata foundation
   ========================================================= */

IF OBJECT_ID(N'dbo.sys_stored_file', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sys_stored_file (
        stored_file_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_sys_stored_file PRIMARY KEY,

        file_key VARCHAR(120) NOT NULL,
        module_code VARCHAR(50) NOT NULL,
        business_category VARCHAR(50) NOT NULL,
        visibility_scope VARCHAR(20) NOT NULL
            CONSTRAINT DF_sys_stored_file_visibility_scope DEFAULT 'HR_ADMIN',

        original_file_name NVARCHAR(255) NOT NULL,
        storage_path NVARCHAR(500) NOT NULL,
        mime_type VARCHAR(100) NULL,
        file_size_bytes BIGINT NOT NULL,
        checksum_sha256 VARCHAR(64) NULL,
        note NVARCHAR(500) NULL,

        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sys_stored_file_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_sys_stored_file_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,

        CONSTRAINT FK_sys_stored_file_created_by FOREIGN KEY (created_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_sys_stored_file_updated_by FOREIGN KEY (updated_by)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_sys_stored_file_visibility_scope CHECK (visibility_scope IN ('SELF_ONLY', 'REVIEW_FLOW', 'HR_ADMIN', 'INTERNAL')),
        CONSTRAINT CK_sys_stored_file_file_size CHECK (file_size_bytes >= 0)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sys_stored_file_file_key'
      AND object_id = OBJECT_ID(N'dbo.sys_stored_file')
)
BEGIN
    CREATE UNIQUE INDEX UX_sys_stored_file_file_key
        ON dbo.sys_stored_file(file_key)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_sys_stored_file_module_category'
      AND object_id = OBJECT_ID(N'dbo.sys_stored_file')
)
BEGIN
    CREATE INDEX IX_sys_stored_file_module_category
        ON dbo.sys_stored_file(module_code, business_category, created_at DESC, is_deleted);
END
GO

SET NOCOUNT OFF;
GO
