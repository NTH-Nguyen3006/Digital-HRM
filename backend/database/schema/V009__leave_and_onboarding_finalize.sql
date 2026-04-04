SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V009__leave_and_onboarding_finalize.sql
   Scope:
   - leave type + leave type rule
   - leave balance + transaction ledger
   - leave request + approval/finalize history
   - onboarding orientation confirmation
   - onboarding completion + notification log
   ========================================================= */

IF OBJECT_ID(N'dbo.lea_leave_type', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.lea_leave_type (
        leave_type_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_lea_leave_type PRIMARY KEY,
        leave_type_code VARCHAR(30) NOT NULL,
        leave_type_name NVARCHAR(200) NOT NULL,
        description NVARCHAR(500) NULL,
        sort_order INT NOT NULL
            CONSTRAINT DF_lea_leave_type_sort_order DEFAULT 0,
        is_system BIT NOT NULL
            CONSTRAINT DF_lea_leave_type_is_system DEFAULT 0,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_lea_leave_type_status DEFAULT 'ACTIVE',
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_lea_leave_type_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_lea_leave_type_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT CK_lea_leave_type_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_lea_leave_type_code' AND object_id = OBJECT_ID(N'dbo.lea_leave_type')
)
BEGIN
    CREATE UNIQUE INDEX UX_lea_leave_type_code
        ON dbo.lea_leave_type(leave_type_code)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.lea_leave_type_rule', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.lea_leave_type_rule (
        leave_type_rule_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_lea_leave_type_rule PRIMARY KEY,
        leave_type_id BIGINT NOT NULL,
        version_no INT NOT NULL,
        effective_from DATE NOT NULL,
        effective_to DATE NULL,
        unit_type VARCHAR(10) NOT NULL,
        default_entitlement_units DECIMAL(10,2) NOT NULL
            CONSTRAINT DF_lea_leave_type_rule_default_entitlement DEFAULT 0,
        carry_forward_max_units DECIMAL(10,2) NOT NULL
            CONSTRAINT DF_lea_leave_type_rule_carry_forward_max_units DEFAULT 0,
        is_paid BIT NOT NULL
            CONSTRAINT DF_lea_leave_type_rule_is_paid DEFAULT 1,
        approval_role_code VARCHAR(20) NOT NULL,
        requires_balance_check BIT NOT NULL
            CONSTRAINT DF_lea_leave_type_rule_requires_balance_check DEFAULT 1,
        requires_attachment BIT NOT NULL
            CONSTRAINT DF_lea_leave_type_rule_requires_attachment DEFAULT 0,
        allow_negative_balance BIT NOT NULL
            CONSTRAINT DF_lea_leave_type_rule_allow_negative_balance DEFAULT 0,
        min_notice_days INT NOT NULL
            CONSTRAINT DF_lea_leave_type_rule_min_notice_days DEFAULT 0,
        max_consecutive_units DECIMAL(10,2) NULL,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_lea_leave_type_rule_status DEFAULT 'ACTIVE',
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_lea_leave_type_rule_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_lea_leave_type_rule_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_lea_leave_type_rule_leave_type FOREIGN KEY (leave_type_id) REFERENCES dbo.lea_leave_type(leave_type_id),
        CONSTRAINT CK_lea_leave_type_rule_unit_type CHECK (unit_type IN ('DAY', 'HOUR')),
        CONSTRAINT CK_lea_leave_type_rule_approval_role CHECK (approval_role_code IN ('HR', 'MANAGER')),
        CONSTRAINT CK_lea_leave_type_rule_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
        CONSTRAINT CK_lea_leave_type_rule_effective CHECK (effective_to IS NULL OR effective_to >= effective_from)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_lea_leave_type_rule_version' AND object_id = OBJECT_ID(N'dbo.lea_leave_type_rule')
)
BEGIN
    CREATE UNIQUE INDEX UX_lea_leave_type_rule_version
        ON dbo.lea_leave_type_rule(leave_type_id, version_no)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_lea_leave_type_rule_effective' AND object_id = OBJECT_ID(N'dbo.lea_leave_type_rule')
)
BEGIN
    CREATE INDEX IX_lea_leave_type_rule_effective
        ON dbo.lea_leave_type_rule(leave_type_id, status, effective_from, effective_to, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.lea_leave_balance', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.lea_leave_balance (
        leave_balance_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_lea_leave_balance PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        leave_type_id BIGINT NOT NULL,
        leave_year INT NOT NULL,
        opening_units DECIMAL(10,2) NOT NULL
            CONSTRAINT DF_lea_leave_balance_opening DEFAULT 0,
        accrued_units DECIMAL(10,2) NOT NULL
            CONSTRAINT DF_lea_leave_balance_accrued DEFAULT 0,
        used_units DECIMAL(10,2) NOT NULL
            CONSTRAINT DF_lea_leave_balance_used DEFAULT 0,
        adjusted_units DECIMAL(10,2) NOT NULL
            CONSTRAINT DF_lea_leave_balance_adjusted DEFAULT 0,
        carried_forward_units DECIMAL(10,2) NOT NULL
            CONSTRAINT DF_lea_leave_balance_carry DEFAULT 0,
        settled_units DECIMAL(10,2) NOT NULL
            CONSTRAINT DF_lea_leave_balance_settled DEFAULT 0,
        available_units DECIMAL(10,2) NOT NULL
            CONSTRAINT DF_lea_leave_balance_available DEFAULT 0,
        balance_status VARCHAR(20) NOT NULL
            CONSTRAINT DF_lea_leave_balance_status DEFAULT 'OPEN',
        last_transaction_at DATETIME2(0) NULL,
        closed_at DATETIME2(0) NULL,
        closed_by UNIQUEIDENTIFIER NULL,
        settlement_note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_lea_leave_balance_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_lea_leave_balance_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_lea_leave_balance_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_lea_leave_balance_leave_type FOREIGN KEY (leave_type_id) REFERENCES dbo.lea_leave_type(leave_type_id),
        CONSTRAINT FK_lea_leave_balance_closed_by FOREIGN KEY (closed_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_lea_leave_balance_status CHECK (balance_status IN ('OPEN', 'CLOSED', 'SETTLED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_lea_leave_balance_employee_type_year' AND object_id = OBJECT_ID(N'dbo.lea_leave_balance')
)
BEGIN
    CREATE UNIQUE INDEX UX_lea_leave_balance_employee_type_year
        ON dbo.lea_leave_balance(employee_id, leave_type_id, leave_year)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_lea_leave_balance_year_status' AND object_id = OBJECT_ID(N'dbo.lea_leave_balance')
)
BEGIN
    CREATE INDEX IX_lea_leave_balance_year_status
        ON dbo.lea_leave_balance(leave_year, balance_status, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.lea_leave_request', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.lea_leave_request (
        leave_request_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_lea_leave_request PRIMARY KEY,
        request_code VARCHAR(30) NOT NULL,
        employee_id BIGINT NOT NULL,
        leave_type_id BIGINT NOT NULL,
        leave_type_rule_id BIGINT NOT NULL,
        leave_year INT NOT NULL,
        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        requested_units DECIMAL(10,2) NOT NULL,
        reason NVARCHAR(1000) NOT NULL,
        attachment_ref NVARCHAR(500) NULL,
        approval_role_code VARCHAR(20) NOT NULL,
        request_status VARCHAR(20) NOT NULL
            CONSTRAINT DF_lea_leave_request_status DEFAULT 'SUBMITTED',
        submitted_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_lea_leave_request_submitted_at DEFAULT SYSDATETIME(),
        approved_at DATETIME2(0) NULL,
        approved_by UNIQUEIDENTIFIER NULL,
        rejected_at DATETIME2(0) NULL,
        rejected_by UNIQUEIDENTIFIER NULL,
        finalized_at DATETIME2(0) NULL,
        finalized_by UNIQUEIDENTIFIER NULL,
        approval_note NVARCHAR(1000) NULL,
        rejection_note NVARCHAR(1000) NULL,
        finalize_note NVARCHAR(1000) NULL,
        canceled_at DATETIME2(0) NULL,
        canceled_by UNIQUEIDENTIFIER NULL,
        cancel_note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_lea_leave_request_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_lea_leave_request_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_lea_leave_request_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_lea_leave_request_leave_type FOREIGN KEY (leave_type_id) REFERENCES dbo.lea_leave_type(leave_type_id),
        CONSTRAINT FK_lea_leave_request_leave_type_rule FOREIGN KEY (leave_type_rule_id) REFERENCES dbo.lea_leave_type_rule(leave_type_rule_id),
        CONSTRAINT FK_lea_leave_request_approved_by FOREIGN KEY (approved_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_lea_leave_request_rejected_by FOREIGN KEY (rejected_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_lea_leave_request_finalized_by FOREIGN KEY (finalized_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_lea_leave_request_canceled_by FOREIGN KEY (canceled_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_lea_leave_request_approval_role CHECK (approval_role_code IN ('HR', 'MANAGER')),
        CONSTRAINT CK_lea_leave_request_status CHECK (request_status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'CANCELLED', 'FINALIZED')),
        CONSTRAINT CK_lea_leave_request_date CHECK (end_date >= start_date)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_lea_leave_request_code' AND object_id = OBJECT_ID(N'dbo.lea_leave_request')
)
BEGIN
    CREATE UNIQUE INDEX UX_lea_leave_request_code
        ON dbo.lea_leave_request(request_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_lea_leave_request_employee_status' AND object_id = OBJECT_ID(N'dbo.lea_leave_request')
)
BEGIN
    CREATE INDEX IX_lea_leave_request_employee_status
        ON dbo.lea_leave_request(employee_id, request_status, start_date, end_date, is_deleted);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_lea_leave_request_approval_scope' AND object_id = OBJECT_ID(N'dbo.lea_leave_request')
)
BEGIN
    CREATE INDEX IX_lea_leave_request_approval_scope
        ON dbo.lea_leave_request(approval_role_code, request_status, start_date, end_date, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.lea_leave_request_history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.lea_leave_request_history (
        leave_request_history_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_lea_leave_request_history PRIMARY KEY,
        leave_request_id BIGINT NOT NULL,
        from_status VARCHAR(20) NULL,
        to_status VARCHAR(20) NOT NULL,
        action_code VARCHAR(50) NOT NULL,
        action_note NVARCHAR(1000) NULL,
        changed_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_lea_leave_request_history_changed_at DEFAULT SYSDATETIME(),
        changed_by UNIQUEIDENTIFIER NULL,
        snapshot_json NVARCHAR(MAX) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_lea_leave_request_history_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        CONSTRAINT FK_lea_leave_request_history_request FOREIGN KEY (leave_request_id) REFERENCES dbo.lea_leave_request(leave_request_id),
        CONSTRAINT FK_lea_leave_request_history_changed_by FOREIGN KEY (changed_by) REFERENCES dbo.sec_user_account(user_id)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_lea_leave_request_history_request' AND object_id = OBJECT_ID(N'dbo.lea_leave_request_history')
)
BEGIN
    CREATE INDEX IX_lea_leave_request_history_request
        ON dbo.lea_leave_request_history(leave_request_id, changed_at DESC);
END
GO

IF OBJECT_ID(N'dbo.lea_leave_balance_txn', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.lea_leave_balance_txn (
        leave_balance_txn_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_lea_leave_balance_txn PRIMARY KEY,
        leave_balance_id BIGINT NOT NULL,
        employee_id BIGINT NOT NULL,
        leave_type_id BIGINT NOT NULL,
        leave_request_id BIGINT NULL,
        transaction_type VARCHAR(30) NOT NULL,
        quantity_delta DECIMAL(10,2) NOT NULL,
        balance_before DECIMAL(10,2) NOT NULL,
        balance_after DECIMAL(10,2) NOT NULL,
        transaction_date DATETIME2(0) NOT NULL
            CONSTRAINT DF_lea_leave_balance_txn_date DEFAULT SYSDATETIME(),
        reference_no VARCHAR(50) NULL,
        reason NVARCHAR(1000) NULL,
        attachment_ref NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_lea_leave_balance_txn_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        CONSTRAINT FK_lea_leave_balance_txn_balance FOREIGN KEY (leave_balance_id) REFERENCES dbo.lea_leave_balance(leave_balance_id),
        CONSTRAINT FK_lea_leave_balance_txn_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_lea_leave_balance_txn_leave_type FOREIGN KEY (leave_type_id) REFERENCES dbo.lea_leave_type(leave_type_id),
        CONSTRAINT FK_lea_leave_balance_txn_request FOREIGN KEY (leave_request_id) REFERENCES dbo.lea_leave_request(leave_request_id),
        CONSTRAINT CK_lea_leave_balance_txn_type CHECK (transaction_type IN ('OPENING', 'MANUAL_ADJUSTMENT', 'REQUEST_DEBIT', 'REQUEST_REVERSE', 'PERIOD_CARRY_FORWARD_IN', 'PERIOD_RESET_OUT', 'TERMINATION_SETTLEMENT'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_lea_leave_balance_txn_balance' AND object_id = OBJECT_ID(N'dbo.lea_leave_balance_txn')
)
BEGIN
    CREATE INDEX IX_lea_leave_balance_txn_balance
        ON dbo.lea_leave_balance_txn(leave_balance_id, transaction_date DESC);
END
GO

IF COL_LENGTH('dbo.onb_onboarding', 'orientation_confirmed_at') IS NULL
BEGIN
    ALTER TABLE dbo.onb_onboarding ADD orientation_confirmed_at DATETIME2(0) NULL;
END
GO

IF COL_LENGTH('dbo.onb_onboarding', 'orientation_confirmed_by') IS NULL
BEGIN
    ALTER TABLE dbo.onb_onboarding ADD orientation_confirmed_by UNIQUEIDENTIFIER NULL;
END
GO

IF COL_LENGTH('dbo.onb_onboarding', 'orientation_note') IS NULL
BEGIN
    ALTER TABLE dbo.onb_onboarding ADD orientation_note NVARCHAR(500) NULL;
END
GO

IF COL_LENGTH('dbo.onb_onboarding', 'completed_at') IS NULL
BEGIN
    ALTER TABLE dbo.onb_onboarding ADD completed_at DATETIME2(0) NULL;
END
GO

IF COL_LENGTH('dbo.onb_onboarding', 'completed_by') IS NULL
BEGIN
    ALTER TABLE dbo.onb_onboarding ADD completed_by UNIQUEIDENTIFIER NULL;
END
GO

IF COL_LENGTH('dbo.onb_onboarding', 'completed_note') IS NULL
BEGIN
    ALTER TABLE dbo.onb_onboarding ADD completed_note NVARCHAR(500) NULL;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = N'FK_onb_onboarding_orientation_confirmed_by'
)
BEGIN
    ALTER TABLE dbo.onb_onboarding
        ADD CONSTRAINT FK_onb_onboarding_orientation_confirmed_by
            FOREIGN KEY (orientation_confirmed_by) REFERENCES dbo.sec_user_account(user_id);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = N'FK_onb_onboarding_completed_by'
)
BEGIN
    ALTER TABLE dbo.onb_onboarding
        ADD CONSTRAINT FK_onb_onboarding_completed_by
            FOREIGN KEY (completed_by) REFERENCES dbo.sec_user_account(user_id);
END
GO

IF OBJECT_ID(N'dbo.onb_onboarding_notification_log', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.onb_onboarding_notification_log (
        onboarding_notification_log_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_onb_onboarding_notification_log PRIMARY KEY,
        onboarding_id BIGINT NOT NULL,
        notification_template_id BIGINT NULL,
        channel_code VARCHAR(20) NOT NULL,
        recipient_name NVARCHAR(200) NULL,
        recipient_email VARCHAR(150) NOT NULL,
        subject_snapshot NVARCHAR(255) NULL,
        body_snapshot NVARCHAR(MAX) NOT NULL,
        delivery_status VARCHAR(20) NOT NULL,
        sent_at DATETIME2(0) NULL,
        sent_by UNIQUEIDENTIFIER NULL,
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_onb_onboarding_notification_log_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_onb_onboarding_notification_log_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_onb_onboarding_notification_log_onboarding FOREIGN KEY (onboarding_id) REFERENCES dbo.onb_onboarding(onboarding_id),
        CONSTRAINT FK_onb_onboarding_notification_log_template FOREIGN KEY (notification_template_id) REFERENCES dbo.sys_notification_template(notification_template_id),
        CONSTRAINT FK_onb_onboarding_notification_log_sent_by FOREIGN KEY (sent_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_onb_onboarding_notification_log_channel CHECK (channel_code IN ('EMAIL', 'SYSTEM', 'SMS')),
        CONSTRAINT CK_onb_onboarding_notification_log_delivery CHECK (delivery_status IN ('SENT', 'FAILED', 'MOCKED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_onb_onboarding_notification_log_onboarding' AND object_id = OBJECT_ID(N'dbo.onb_onboarding_notification_log')
)
BEGIN
    CREATE INDEX IX_onb_onboarding_notification_log_onboarding
        ON dbo.onb_onboarding_notification_log(onboarding_id, created_at DESC, is_deleted);
END
GO

SET NOCOUNT OFF;
GO
