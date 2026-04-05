SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V013__offboarding_and_employee_portal.sql
   Scope:
   - offboarding workflow
   - handover checklist
   - asset return tracking
   - offboarding history
   - employee portal inbox/task center
   ========================================================= */

IF OBJECT_ID(N'dbo.off_offboarding_case', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.off_offboarding_case (
        offboarding_case_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_off_offboarding_case PRIMARY KEY,
        offboarding_code VARCHAR(30) NOT NULL,
        employee_id BIGINT NOT NULL,
        requested_by_user_id UNIQUEIDENTIFIER NULL,
        request_date DATE NOT NULL,
        requested_last_working_date DATE NULL,
        request_reason NVARCHAR(1000) NOT NULL,
        status VARCHAR(30) NOT NULL CONSTRAINT DF_off_offboarding_case_status DEFAULT 'REQUESTED',
        manager_reviewed_by UNIQUEIDENTIFIER NULL,
        manager_reviewed_at DATETIME2(0) NULL,
        manager_review_note NVARCHAR(1000) NULL,
        hr_finalized_by UNIQUEIDENTIFIER NULL,
        hr_finalized_at DATETIME2(0) NULL,
        effective_last_working_date DATE NULL,
        hr_finalize_note NVARCHAR(1000) NULL,
        access_revoked_by UNIQUEIDENTIFIER NULL,
        access_revoked_at DATETIME2(0) NULL,
        access_revoke_note NVARCHAR(1000) NULL,
        settlement_prepared_by UNIQUEIDENTIFIER NULL,
        settlement_prepared_at DATETIME2(0) NULL,
        final_attendance_year INT NULL,
        final_attendance_month INT NULL,
        leave_settlement_units DECIMAL(18,2) NULL,
        leave_settlement_amount DECIMAL(18,2) NULL,
        final_payroll_period_id BIGINT NULL,
        final_payroll_item_id BIGINT NULL,
        settlement_note NVARCHAR(1000) NULL,
        closed_by UNIQUEIDENTIFIER NULL,
        closed_at DATETIME2(0) NULL,
        close_note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_off_offboarding_case_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_off_offboarding_case_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_off_offboarding_case_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_off_offboarding_case_requested_by FOREIGN KEY (requested_by_user_id) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_off_offboarding_case_manager_reviewed_by FOREIGN KEY (manager_reviewed_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_off_offboarding_case_hr_finalized_by FOREIGN KEY (hr_finalized_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_off_offboarding_case_access_revoked_by FOREIGN KEY (access_revoked_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_off_offboarding_case_settlement_prepared_by FOREIGN KEY (settlement_prepared_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_off_offboarding_case_closed_by FOREIGN KEY (closed_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_off_offboarding_case_payroll_period FOREIGN KEY (final_payroll_period_id) REFERENCES dbo.pay_payroll_period(payroll_period_id),
        CONSTRAINT FK_off_offboarding_case_payroll_item FOREIGN KEY (final_payroll_item_id) REFERENCES dbo.pay_payroll_item(payroll_item_id),
        CONSTRAINT CK_off_offboarding_case_status CHECK (status IN ('REQUESTED','MANAGER_APPROVED','MANAGER_REJECTED','HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED','CANCELLED')),
        CONSTRAINT CK_off_offboarding_case_attendance_month CHECK (
            final_attendance_month IS NULL OR (final_attendance_month BETWEEN 1 AND 12)
        )
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_off_offboarding_case_code' AND object_id = OBJECT_ID(N'dbo.off_offboarding_case'))
BEGIN
    CREATE UNIQUE INDEX UX_off_offboarding_case_code
        ON dbo.off_offboarding_case(offboarding_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_off_offboarding_case_employee_status' AND object_id = OBJECT_ID(N'dbo.off_offboarding_case'))
BEGIN
    CREATE INDEX IX_off_offboarding_case_employee_status
        ON dbo.off_offboarding_case(employee_id, status, request_date, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.off_offboarding_checklist_item', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.off_offboarding_checklist_item (
        offboarding_checklist_item_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_off_offboarding_checklist_item PRIMARY KEY,
        offboarding_case_id BIGINT NOT NULL,
        item_type VARCHAR(30) NOT NULL,
        item_name NVARCHAR(255) NOT NULL,
        owner_role_code VARCHAR(30) NULL,
        due_date DATE NULL,
        status VARCHAR(30) NOT NULL CONSTRAINT DF_off_offboarding_checklist_status DEFAULT 'OPEN',
        completed_by UNIQUEIDENTIFIER NULL,
        completed_at DATETIME2(0) NULL,
        note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_off_offboarding_checklist_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_off_offboarding_checklist_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_off_offboarding_checklist_case FOREIGN KEY (offboarding_case_id) REFERENCES dbo.off_offboarding_case(offboarding_case_id),
        CONSTRAINT FK_off_offboarding_checklist_completed_by FOREIGN KEY (completed_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_off_offboarding_checklist_item_type CHECK (item_type IN ('HANDOVER','KNOWLEDGE_TRANSFER','ACCESS_HANDOVER','ASSET','DOCUMENT','OTHER')),
        CONSTRAINT CK_off_offboarding_checklist_status CHECK (status IN ('OPEN','IN_PROGRESS','COMPLETED','CANCELLED'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_off_offboarding_checklist_case' AND object_id = OBJECT_ID(N'dbo.off_offboarding_checklist_item'))
BEGIN
    CREATE INDEX IX_off_offboarding_checklist_case
        ON dbo.off_offboarding_checklist_item(offboarding_case_id, status, due_date, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.off_offboarding_asset_return', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.off_offboarding_asset_return (
        offboarding_asset_return_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_off_offboarding_asset_return PRIMARY KEY,
        offboarding_case_id BIGINT NOT NULL,
        asset_code VARCHAR(50) NULL,
        asset_name NVARCHAR(255) NOT NULL,
        asset_type VARCHAR(50) NULL,
        expected_return_date DATE NULL,
        returned_date DATE NULL,
        status VARCHAR(30) NOT NULL CONSTRAINT DF_off_offboarding_asset_status DEFAULT 'PENDING',
        updated_by_user_id UNIQUEIDENTIFIER NULL,
        updated_at_action DATETIME2(0) NULL,
        note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_off_offboarding_asset_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_off_offboarding_asset_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_off_offboarding_asset_case FOREIGN KEY (offboarding_case_id) REFERENCES dbo.off_offboarding_case(offboarding_case_id),
        CONSTRAINT FK_off_offboarding_asset_updated_by_user FOREIGN KEY (updated_by_user_id) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_off_offboarding_asset_status CHECK (status IN ('PENDING','RETURNED','WAIVED','LOST'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_off_offboarding_asset_case' AND object_id = OBJECT_ID(N'dbo.off_offboarding_asset_return'))
BEGIN
    CREATE INDEX IX_off_offboarding_asset_case
        ON dbo.off_offboarding_asset_return(offboarding_case_id, status, expected_return_date, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.off_offboarding_history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.off_offboarding_history (
        offboarding_history_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_off_offboarding_history PRIMARY KEY,
        offboarding_case_id BIGINT NOT NULL,
        from_status VARCHAR(30) NULL,
        to_status VARCHAR(30) NOT NULL,
        action_code VARCHAR(50) NOT NULL,
        action_note NVARCHAR(1000) NULL,
        changed_by UNIQUEIDENTIFIER NULL,
        changed_at DATETIME2(0) NOT NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_off_offboarding_history_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        CONSTRAINT FK_off_offboarding_history_case FOREIGN KEY (offboarding_case_id) REFERENCES dbo.off_offboarding_case(offboarding_case_id),
        CONSTRAINT FK_off_offboarding_history_changed_by FOREIGN KEY (changed_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_off_offboarding_history_from_status CHECK (from_status IS NULL OR from_status IN ('REQUESTED','MANAGER_APPROVED','MANAGER_REJECTED','HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED','CANCELLED')),
        CONSTRAINT CK_off_offboarding_history_to_status CHECK (to_status IN ('REQUESTED','MANAGER_APPROVED','MANAGER_REJECTED','HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED','CANCELLED'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_off_offboarding_history_case' AND object_id = OBJECT_ID(N'dbo.off_offboarding_history'))
BEGIN
    CREATE INDEX IX_off_offboarding_history_case
        ON dbo.off_offboarding_history(offboarding_case_id, changed_at DESC);
END
GO

IF OBJECT_ID(N'dbo.por_portal_inbox_item', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.por_portal_inbox_item (
        portal_inbox_item_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_por_portal_inbox_item PRIMARY KEY,
        user_id UNIQUEIDENTIFIER NOT NULL,
        employee_id BIGINT NULL,
        item_type VARCHAR(20) NOT NULL,
        category_code VARCHAR(50) NULL,
        title NVARCHAR(255) NOT NULL,
        message NVARCHAR(2000) NOT NULL,
        related_module VARCHAR(50) NULL,
        related_entity_id VARCHAR(100) NULL,
        due_at DATETIME2(0) NULL,
        read_at DATETIME2(0) NULL,
        task_status VARCHAR(20) NOT NULL CONSTRAINT DF_por_portal_inbox_task_status DEFAULT 'OPEN',
        completed_at DATETIME2(0) NULL,
        status VARCHAR(20) NOT NULL CONSTRAINT DF_por_portal_inbox_status DEFAULT 'ACTIVE',
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_por_portal_inbox_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_por_portal_inbox_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_por_portal_inbox_user FOREIGN KEY (user_id) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_por_portal_inbox_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_por_portal_inbox_item_type CHECK (item_type IN ('NOTIFICATION','TASK')),
        CONSTRAINT CK_por_portal_inbox_task_status CHECK (task_status IN ('OPEN','DONE','CANCELLED')),
        CONSTRAINT CK_por_portal_inbox_status CHECK (status IN ('ACTIVE','INACTIVE'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_por_portal_inbox_user' AND object_id = OBJECT_ID(N'dbo.por_portal_inbox_item'))
BEGIN
    CREATE INDEX IX_por_portal_inbox_user
        ON dbo.por_portal_inbox_item(user_id, status, read_at, due_at, is_deleted);
END
GO

SET NOCOUNT OFF;
GO
