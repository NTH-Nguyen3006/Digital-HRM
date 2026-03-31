SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V006__system_config_foundation.sql
   Scope:
   - system notification template
   - platform settings
   - role menu configuration
   ========================================================= */

IF OBJECT_ID(N'dbo.sys_notification_template', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sys_notification_template (
        notification_template_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_sys_notification_template PRIMARY KEY,
        template_code VARCHAR(50) NOT NULL,
        template_name NVARCHAR(200) NOT NULL,
        channel_code VARCHAR(20) NOT NULL,
        subject_template NVARCHAR(255) NULL,
        body_template NVARCHAR(MAX) NOT NULL,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_sys_notification_template_status DEFAULT 'ACTIVE',
        description NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sys_notification_template_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_sys_notification_template_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT CK_sys_notification_template_channel CHECK (channel_code IN ('EMAIL', 'SYSTEM', 'SMS')),
        CONSTRAINT CK_sys_notification_template_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sys_notification_template_code' AND object_id = OBJECT_ID(N'dbo.sys_notification_template')
)
BEGIN
    CREATE UNIQUE INDEX UX_sys_notification_template_code
        ON dbo.sys_notification_template(template_code)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.sys_platform_setting', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sys_platform_setting (
        platform_setting_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_sys_platform_setting PRIMARY KEY,
        setting_key VARCHAR(100) NOT NULL,
        setting_name NVARCHAR(200) NOT NULL,
        setting_value NVARCHAR(MAX) NULL,
        value_type VARCHAR(20) NOT NULL,
        is_editable BIT NOT NULL
            CONSTRAINT DF_sys_platform_setting_is_editable DEFAULT 1,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_sys_platform_setting_status DEFAULT 'ACTIVE',
        description NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sys_platform_setting_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_sys_platform_setting_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT CK_sys_platform_setting_value_type CHECK (value_type IN ('STRING', 'BOOLEAN', 'NUMBER', 'JSON')),
        CONSTRAINT CK_sys_platform_setting_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sys_platform_setting_key' AND object_id = OBJECT_ID(N'dbo.sys_platform_setting')
)
BEGIN
    CREATE UNIQUE INDEX UX_sys_platform_setting_key
        ON dbo.sys_platform_setting(setting_key)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.sys_role_menu_config', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sys_role_menu_config (
        role_menu_config_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_sys_role_menu_config PRIMARY KEY,
        role_id UNIQUEIDENTIFIER NOT NULL,
        menu_key VARCHAR(100) NOT NULL,
        menu_name NVARCHAR(200) NOT NULL,
        route_path VARCHAR(255) NOT NULL,
        icon_name VARCHAR(100) NULL,
        parent_menu_key VARCHAR(100) NULL,
        sort_order INT NOT NULL
            CONSTRAINT DF_sys_role_menu_config_sort_order DEFAULT 0,
        is_visible BIT NOT NULL
            CONSTRAINT DF_sys_role_menu_config_is_visible DEFAULT 1,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_sys_role_menu_config_status DEFAULT 'ACTIVE',
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sys_role_menu_config_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_sys_role_menu_config_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_sys_role_menu_config_role FOREIGN KEY (role_id) REFERENCES dbo.sec_role(role_id),
        CONSTRAINT CK_sys_role_menu_config_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sys_role_menu_config_role_menu' AND object_id = OBJECT_ID(N'dbo.sys_role_menu_config')
)
BEGIN
    CREATE UNIQUE INDEX UX_sys_role_menu_config_role_menu
        ON dbo.sys_role_menu_config(role_id, menu_key)
        WHERE is_deleted = 0;
END
GO

SET NOCOUNT OFF;
GO
