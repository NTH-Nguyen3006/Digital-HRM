SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V002__auth_rbac.sql
   Scope:
   - sec_role
   - sec_permission
   - sec_user_role
   - sec_role_permission
   - sec_data_scope_assignment
   ========================================================= */

IF OBJECT_ID(N'dbo.sec_role', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sec_role (
        role_id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_sec_role PRIMARY KEY
            CONSTRAINT DF_sec_role_role_id DEFAULT NEWSEQUENTIALID(),

        role_code VARCHAR(30) NOT NULL,
        role_name NVARCHAR(100) NOT NULL,
        description NVARCHAR(500) NULL,

        status VARCHAR(20) NOT NULL,
        is_system_role BIT NOT NULL
            CONSTRAINT DF_sec_role_is_system_role DEFAULT 1,
        sort_order INT NOT NULL
            CONSTRAINT DF_sec_role_sort_order DEFAULT 0,

        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sec_role_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,

        is_deleted BIT NOT NULL
            CONSTRAINT DF_sec_role_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,

        CONSTRAINT CK_sec_role_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sec_role_role_code'
      AND object_id = OBJECT_ID(N'dbo.sec_role')
)
BEGIN
    CREATE UNIQUE INDEX UX_sec_role_role_code
        ON dbo.sec_role(role_code)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.sec_permission', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sec_permission (
        permission_id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_sec_permission PRIMARY KEY
            CONSTRAINT DF_sec_permission_permission_id DEFAULT NEWSEQUENTIALID(),

        permission_code VARCHAR(100) NOT NULL,
        module_code VARCHAR(50) NOT NULL,
        action_code VARCHAR(30) NOT NULL,
        permission_name NVARCHAR(150) NOT NULL,
        description NVARCHAR(500) NULL,

        status VARCHAR(20) NOT NULL,

        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sec_permission_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,

        is_deleted BIT NOT NULL
            CONSTRAINT DF_sec_permission_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,

        CONSTRAINT CK_sec_permission_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sec_permission_permission_code'
      AND object_id = OBJECT_ID(N'dbo.sec_permission')
)
BEGIN
    CREATE UNIQUE INDEX UX_sec_permission_permission_code
        ON dbo.sec_permission(permission_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_sec_permission_module_action'
      AND object_id = OBJECT_ID(N'dbo.sec_permission')
)
BEGIN
    CREATE INDEX IX_sec_permission_module_action
        ON dbo.sec_permission(module_code, action_code, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.sec_user_role', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sec_user_role (
        user_role_id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_sec_user_role PRIMARY KEY
            CONSTRAINT DF_sec_user_role_user_role_id DEFAULT NEWSEQUENTIALID(),

        user_id UNIQUEIDENTIFIER NOT NULL,
        role_id UNIQUEIDENTIFIER NOT NULL,

        is_primary_role BIT NOT NULL
            CONSTRAINT DF_sec_user_role_is_primary_role DEFAULT 1,
        effective_from DATETIME2(0) NOT NULL
            CONSTRAINT DF_sec_user_role_effective_from DEFAULT SYSDATETIME(),
        effective_to DATETIME2(0) NULL,

        status VARCHAR(20) NOT NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sec_user_role_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,

        CONSTRAINT FK_sec_user_role_user FOREIGN KEY (user_id)
            REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_sec_user_role_role FOREIGN KEY (role_id)
            REFERENCES dbo.sec_role(role_id),
        CONSTRAINT CK_sec_user_role_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_sec_user_role_user'
      AND object_id = OBJECT_ID(N'dbo.sec_user_role')
)
BEGIN
    CREATE INDEX IX_sec_user_role_user
        ON dbo.sec_user_role(user_id, status, effective_from);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_sec_user_role_role'
      AND object_id = OBJECT_ID(N'dbo.sec_user_role')
)
BEGIN
    CREATE INDEX IX_sec_user_role_role
        ON dbo.sec_user_role(role_id, status);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sec_user_role_active_primary'
      AND object_id = OBJECT_ID(N'dbo.sec_user_role')
)
BEGIN
    CREATE UNIQUE INDEX UX_sec_user_role_active_primary
        ON dbo.sec_user_role(user_id)
        WHERE is_primary_role = 1
          AND status = 'ACTIVE'
          AND effective_to IS NULL;
END
GO

IF OBJECT_ID(N'dbo.sec_role_permission', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sec_role_permission (
        role_permission_id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_sec_role_permission PRIMARY KEY
            CONSTRAINT DF_sec_role_permission_role_permission_id DEFAULT NEWSEQUENTIALID(),

        role_id UNIQUEIDENTIFIER NOT NULL,
        permission_id UNIQUEIDENTIFIER NOT NULL,
        is_allowed BIT NOT NULL
            CONSTRAINT DF_sec_role_permission_is_allowed DEFAULT 1,

        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sec_role_permission_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,

        CONSTRAINT FK_sec_role_permission_role FOREIGN KEY (role_id)
            REFERENCES dbo.sec_role(role_id),
        CONSTRAINT FK_sec_role_permission_permission FOREIGN KEY (permission_id)
            REFERENCES dbo.sec_permission(permission_id)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_sec_role_permission_role_permission'
      AND object_id = OBJECT_ID(N'dbo.sec_role_permission')
)
BEGIN
    CREATE UNIQUE INDEX UX_sec_role_permission_role_permission
        ON dbo.sec_role_permission(role_id, permission_id);
END
GO

IF OBJECT_ID(N'dbo.sec_data_scope_assignment', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.sec_data_scope_assignment (
        data_scope_assignment_id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_sec_data_scope_assignment PRIMARY KEY
            CONSTRAINT DF_sec_data_scope_assignment_id DEFAULT NEWSEQUENTIALID(),

        subject_type VARCHAR(20) NOT NULL,
        subject_id UNIQUEIDENTIFIER NOT NULL,

        scope_code VARCHAR(30) NOT NULL,
        target_type VARCHAR(30) NULL,
        target_ref_id VARCHAR(50) NULL,

        is_inclusive BIT NOT NULL
            CONSTRAINT DF_sec_data_scope_assignment_is_inclusive DEFAULT 1,
        priority_order INT NOT NULL
            CONSTRAINT DF_sec_data_scope_assignment_priority_order DEFAULT 0,

        effective_from DATETIME2(0) NOT NULL
            CONSTRAINT DF_sec_data_scope_assignment_effective_from DEFAULT SYSDATETIME(),
        effective_to DATETIME2(0) NULL,

        status VARCHAR(20) NOT NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_sec_data_scope_assignment_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,

        CONSTRAINT CK_sec_data_scope_assignment_subject_type
            CHECK (subject_type IN ('ROLE', 'USER')),
        CONSTRAINT CK_sec_data_scope_assignment_scope_code
            CHECK (scope_code IN ('ALL', 'SELF', 'ORG_UNIT', 'SUBTREE', 'CUSTOM')),
        CONSTRAINT CK_sec_data_scope_assignment_status
            CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_sec_data_scope_assignment_subject'
      AND object_id = OBJECT_ID(N'dbo.sec_data_scope_assignment')
)
BEGIN
    CREATE INDEX IX_sec_data_scope_assignment_subject
        ON dbo.sec_data_scope_assignment(subject_type, subject_id, status, priority_order);
END
GO
