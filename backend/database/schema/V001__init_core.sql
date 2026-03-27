SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V001__init_core.sql
   Scope:
   - sec_user_account
   - sec_auth_session
   - sec_password_reset_token
   - sys_audit_log
   ========================================================= */

IF OBJECT_ID(N'dbo.sec_user_account', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sec_user_account (
        user_id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_sec_user_account PRIMARY KEY
            CONSTRAINT DF_sec_user_account_user_id DEFAULT NEWSEQUENTIALID(),

        employee_id BIGINT NULL,
        username VARCHAR(50) NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        password_changed_at DATETIME2(0) NULL,
        must_change_password BIT NOT NULL
            CONSTRAINT DF_sec_user_account_must_change_password DEFAULT 0,

        mfa_enabled BIT NOT NULL
            CONSTRAINT DF_sec_user_account_mfa_enabled DEFAULT 0,
        mfa_secret VARCHAR(128) NULL,

        email VARCHAR(150) NULL,
        phone_number VARCHAR(20) NULL,

        status VARCHAR(30) NOT NULL,
        last_login_at DATETIME2(0) NULL,
        last_login_ip VARCHAR(45) NULL,
        failed_login_count INT NOT NULL
            CONSTRAINT DF_sec_user_account_failed_login_count DEFAULT 0,
        locked_until DATETIME2(0) NULL,

        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sec_user_account_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,

        is_deleted BIT NOT NULL
            CONSTRAINT DF_sec_user_account_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,

        CONSTRAINT CK_sec_user_account_status
            CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED', 'PENDING_ACTIVATION'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sec_user_account_username'
      AND object_id = OBJECT_ID(N'dbo.sec_user_account')
)
BEGIN
    CREATE UNIQUE INDEX UX_sec_user_account_username
        ON dbo.sec_user_account(username)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sec_user_account_email'
      AND object_id = OBJECT_ID(N'dbo.sec_user_account')
)
BEGIN
    CREATE UNIQUE INDEX UX_sec_user_account_email
        ON dbo.sec_user_account(email)
        WHERE email IS NOT NULL AND is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sec_user_account_employee_id'
      AND object_id = OBJECT_ID(N'dbo.sec_user_account')
)
BEGIN
    CREATE UNIQUE INDEX UX_sec_user_account_employee_id
        ON dbo.sec_user_account(employee_id)
        WHERE employee_id IS NOT NULL AND is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_sec_user_account_status'
      AND object_id = OBJECT_ID(N'dbo.sec_user_account')
)
BEGIN
    CREATE INDEX IX_sec_user_account_status
        ON dbo.sec_user_account(status, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.sec_auth_session', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sec_auth_session (
        auth_session_id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_sec_auth_session PRIMARY KEY
            CONSTRAINT DF_sec_auth_session_id DEFAULT NEWSEQUENTIALID(),

        user_id UNIQUEIDENTIFIER NOT NULL,
        refresh_token_hash VARCHAR(255) NOT NULL,

        device_name NVARCHAR(150) NULL,
        device_os NVARCHAR(100) NULL,
        browser_name NVARCHAR(100) NULL,

        ip_address VARCHAR(45) NULL,
        user_agent NVARCHAR(500) NULL,

        login_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sec_auth_session_login_at DEFAULT SYSDATETIME(),
        expires_at DATETIME2(0) NOT NULL,
        revoked_at DATETIME2(0) NULL,
        revoke_reason NVARCHAR(255) NULL,

        status VARCHAR(20) NOT NULL,

        CONSTRAINT FK_sec_auth_session_user
            FOREIGN KEY (user_id) REFERENCES dbo.sec_user_account(user_id),

        CONSTRAINT CK_sec_auth_session_status
            CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sec_auth_session_refresh_token_hash'
      AND object_id = OBJECT_ID(N'dbo.sec_auth_session')
)
BEGIN
    CREATE UNIQUE INDEX UX_sec_auth_session_refresh_token_hash
        ON dbo.sec_auth_session(refresh_token_hash);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_sec_auth_session_user_status'
      AND object_id = OBJECT_ID(N'dbo.sec_auth_session')
)
BEGIN
    CREATE INDEX IX_sec_auth_session_user_status
        ON dbo.sec_auth_session(user_id, status, expires_at);
END
GO

IF OBJECT_ID(N'dbo.sec_password_reset_token', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sec_password_reset_token (
        password_reset_token_id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_sec_password_reset_token PRIMARY KEY
            CONSTRAINT DF_sec_password_reset_token_id DEFAULT NEWSEQUENTIALID(),

        user_id UNIQUEIDENTIFIER NOT NULL,
        token_hash VARCHAR(255) NOT NULL,
        channel VARCHAR(20) NOT NULL,

        issued_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sec_password_reset_token_issued_at DEFAULT SYSDATETIME(),
        expires_at DATETIME2(0) NOT NULL,
        used_at DATETIME2(0) NULL,

        status VARCHAR(20) NOT NULL,

        CONSTRAINT FK_sec_password_reset_token_user
            FOREIGN KEY (user_id) REFERENCES dbo.sec_user_account(user_id),

        CONSTRAINT CK_sec_password_reset_token_channel
            CHECK (channel IN ('EMAIL', 'SMS')),

        CONSTRAINT CK_sec_password_reset_token_status
            CHECK (status IN ('PENDING', 'USED', 'EXPIRED', 'REVOKED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sec_password_reset_token_token_hash'
      AND object_id = OBJECT_ID(N'dbo.sec_password_reset_token')
)
BEGIN
    CREATE UNIQUE INDEX UX_sec_password_reset_token_token_hash
        ON dbo.sec_password_reset_token(token_hash);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_sec_password_reset_token_user_status'
      AND object_id = OBJECT_ID(N'dbo.sec_password_reset_token')
)
BEGIN
    CREATE INDEX IX_sec_password_reset_token_user_status
        ON dbo.sec_password_reset_token(user_id, status, expires_at);
END
GO

IF OBJECT_ID(N'dbo.sys_audit_log', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sys_audit_log (
        audit_log_id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_sys_audit_log PRIMARY KEY
            CONSTRAINT DF_sys_audit_log_id DEFAULT NEWSEQUENTIALID(),

        actor_user_id UNIQUEIDENTIFIER NULL,
        actor_username VARCHAR(50) NULL,

        action_code VARCHAR(50) NOT NULL,
        module_code VARCHAR(50) NOT NULL,
        entity_name VARCHAR(100) NOT NULL,
        entity_id VARCHAR(50) NULL,

        request_id VARCHAR(100) NULL,
        old_data_json NVARCHAR(MAX) NULL,
        new_data_json NVARCHAR(MAX) NULL,

        ip_address VARCHAR(45) NULL,
        user_agent NVARCHAR(500) NULL,

        action_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sys_audit_log_action_at DEFAULT SYSDATETIME(),
        result_code VARCHAR(20) NOT NULL,
        message NVARCHAR(1000) NULL,

        CONSTRAINT FK_sys_audit_log_actor_user
            FOREIGN KEY (actor_user_id) REFERENCES dbo.sec_user_account(user_id),

        CONSTRAINT CK_sys_audit_log_result_code
            CHECK (result_code IN ('SUCCESS', 'FAILED', 'DENIED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_sys_audit_log_action_at'
      AND object_id = OBJECT_ID(N'dbo.sys_audit_log')
)
BEGIN
    CREATE INDEX IX_sys_audit_log_action_at
        ON dbo.sys_audit_log(action_at DESC);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_sys_audit_log_module_code'
      AND object_id = OBJECT_ID(N'dbo.sys_audit_log')
)
BEGIN
    CREATE INDEX IX_sys_audit_log_module_code
        ON dbo.sys_audit_log(module_code, action_code, action_at DESC);
END
GO
