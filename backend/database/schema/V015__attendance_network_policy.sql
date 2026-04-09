SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V015__attendance_network_policy.sql
   Scope:
   - attendance network policy
   - whitelist ip/cidr for self attendance
   - enrich raw attendance log with network validation data
   ========================================================= */

IF OBJECT_ID(N'dbo.att_network_policy', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_network_policy (
        network_policy_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_network_policy PRIMARY KEY,
        policy_code VARCHAR(30) NOT NULL,
        policy_name NVARCHAR(200) NOT NULL,
        scope_type VARCHAR(20) NOT NULL,
        org_unit_id BIGINT NULL,
        allow_check_in BIT NOT NULL
            CONSTRAINT DF_att_network_policy_allow_check_in DEFAULT 1,
        allow_check_out BIT NOT NULL
            CONSTRAINT DF_att_network_policy_allow_check_out DEFAULT 1,
        effective_from DATE NULL,
        effective_to DATE NULL,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_att_network_policy_status DEFAULT 'ACTIVE',
        description NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_network_policy_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_att_network_policy_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_att_network_policy_org_unit FOREIGN KEY (org_unit_id) REFERENCES dbo.hr_org_unit(org_unit_id),
        CONSTRAINT CK_att_network_policy_scope_type CHECK (scope_type IN ('GLOBAL', 'ORG_UNIT')),
        CONSTRAINT CK_att_network_policy_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
        CONSTRAINT CK_att_network_policy_effective CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from),
        CONSTRAINT CK_att_network_policy_allow_action CHECK (allow_check_in = 1 OR allow_check_out = 1)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_att_network_policy_code' AND object_id = OBJECT_ID(N'dbo.att_network_policy')
)
BEGIN
    CREATE UNIQUE INDEX UX_att_network_policy_code
        ON dbo.att_network_policy(policy_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'IX_att_network_policy_scope_status' AND object_id = OBJECT_ID(N'dbo.att_network_policy')
)
BEGIN
    CREATE INDEX IX_att_network_policy_scope_status
        ON dbo.att_network_policy(scope_type, org_unit_id, status, effective_from, effective_to, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.att_network_policy_ip', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_network_policy_ip (
        network_policy_ip_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_network_policy_ip PRIMARY KEY,
        network_policy_id BIGINT NOT NULL,
        cidr_or_ip VARCHAR(64) NOT NULL,
        description NVARCHAR(250) NULL,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_att_network_policy_ip_status DEFAULT 'ACTIVE',
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_network_policy_ip_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_att_network_policy_ip_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_att_network_policy_ip_policy FOREIGN KEY (network_policy_id) REFERENCES dbo.att_network_policy(network_policy_id),
        CONSTRAINT CK_att_network_policy_ip_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'IX_att_network_policy_ip_policy' AND object_id = OBJECT_ID(N'dbo.att_network_policy_ip')
)
BEGIN
    CREATE INDEX IX_att_network_policy_ip_policy
        ON dbo.att_network_policy_ip(network_policy_id, status, is_deleted);
END
GO

IF COL_LENGTH('dbo.att_attendance_log', 'client_ip') IS NULL
BEGIN
    ALTER TABLE dbo.att_attendance_log ADD client_ip VARCHAR(64) NULL;
END
GO

IF COL_LENGTH('dbo.att_attendance_log', 'forwarded_for_raw') IS NULL
BEGIN
    ALTER TABLE dbo.att_attendance_log ADD forwarded_for_raw VARCHAR(500) NULL;
END
GO

IF COL_LENGTH('dbo.att_attendance_log', 'network_policy_id') IS NULL
BEGIN
    ALTER TABLE dbo.att_attendance_log ADD network_policy_id BIGINT NULL;
END
GO

IF COL_LENGTH('dbo.att_attendance_log', 'network_validation_status') IS NULL
BEGIN
    ALTER TABLE dbo.att_attendance_log ADD network_validation_status VARCHAR(20) NULL;
END
GO

IF COL_LENGTH('dbo.att_attendance_log', 'network_validation_message') IS NULL
BEGIN
    ALTER TABLE dbo.att_attendance_log ADD network_validation_message NVARCHAR(500) NULL;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_att_attendance_log_network_policy'
)
BEGIN
    ALTER TABLE dbo.att_attendance_log WITH CHECK
        ADD CONSTRAINT FK_att_attendance_log_network_policy
        FOREIGN KEY (network_policy_id) REFERENCES dbo.att_network_policy(network_policy_id);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints WHERE name = N'CK_att_attendance_log_network_validation_status'
)
BEGIN
    ALTER TABLE dbo.att_attendance_log WITH CHECK
        ADD CONSTRAINT CK_att_attendance_log_network_validation_status
        CHECK (network_validation_status IS NULL OR network_validation_status IN ('PASSED', 'FAILED', 'BYPASSED'));
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'IX_att_attendance_log_network_policy' AND object_id = OBJECT_ID(N'dbo.att_attendance_log')
)
BEGIN
    CREATE INDEX IX_att_attendance_log_network_policy
        ON dbo.att_attendance_log(network_policy_id, attendance_date, is_deleted);
END
GO

SET NOCOUNT OFF;
GO
