SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V014__reporting_and_governance.sql
   Scope:
   - reporting & dashboard
   - report schedule config
   - report schedule run log
   ========================================================= */

IF OBJECT_ID(N'dbo.rep_report_schedule_config', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.rep_report_schedule_config (
        report_schedule_config_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_rep_report_schedule_config PRIMARY KEY,
        schedule_code VARCHAR(50) NOT NULL,
        schedule_name NVARCHAR(200) NOT NULL,
        report_code VARCHAR(50) NOT NULL,
        frequency_code VARCHAR(20) NOT NULL,
        day_of_week INT NULL,
        day_of_month INT NULL,
        run_at_hour INT NOT NULL,
        run_at_minute INT NOT NULL,
        recipient_emails_csv NVARCHAR(1000) NOT NULL,
        parameter_json NVARCHAR(MAX) NULL,
        status VARCHAR(20) NOT NULL CONSTRAINT DF_rep_report_schedule_config_status DEFAULT 'ACTIVE',
        next_run_at DATETIME2(0) NULL,
        last_run_at DATETIME2(0) NULL,
        last_run_status VARCHAR(20) NULL,
        last_run_message NVARCHAR(1000) NULL,
        description NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_rep_report_schedule_config_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_rep_report_schedule_config_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT CK_rep_report_schedule_config_report_code CHECK (
            report_code IN ('ORG_MOVEMENT','CONTRACT_EXPIRY','LEAVE_BALANCE','ATTENDANCE_ANOMALY_OT','PAYROLL_SUMMARY','PIT','ONBOARDING_OFFBOARDING','AUDIT_LOG')
        ),
        CONSTRAINT CK_rep_report_schedule_config_frequency CHECK (
            frequency_code IN ('DAILY','WEEKLY','MONTHLY')
        ),
        CONSTRAINT CK_rep_report_schedule_config_day_of_week CHECK (
            day_of_week IS NULL OR (day_of_week BETWEEN 1 AND 7)
        ),
        CONSTRAINT CK_rep_report_schedule_config_day_of_month CHECK (
            day_of_month IS NULL OR (day_of_month BETWEEN 1 AND 31)
        ),
        CONSTRAINT CK_rep_report_schedule_config_run_at_hour CHECK (
            run_at_hour BETWEEN 0 AND 23
        ),
        CONSTRAINT CK_rep_report_schedule_config_run_at_minute CHECK (
            run_at_minute BETWEEN 0 AND 59
        ),
        CONSTRAINT CK_rep_report_schedule_config_status CHECK (
            status IN ('ACTIVE','INACTIVE')
        ),
        CONSTRAINT CK_rep_report_schedule_config_last_run_status CHECK (
            last_run_status IS NULL OR last_run_status IN ('RUNNING','SUCCESS','FAILED')
        )
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_rep_report_schedule_config_schedule_code' AND object_id = OBJECT_ID(N'dbo.rep_report_schedule_config'))
BEGIN
    CREATE UNIQUE INDEX UX_rep_report_schedule_config_schedule_code
        ON dbo.rep_report_schedule_config(schedule_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_rep_report_schedule_config_status_next_run' AND object_id = OBJECT_ID(N'dbo.rep_report_schedule_config'))
BEGIN
    CREATE INDEX IX_rep_report_schedule_config_status_next_run
        ON dbo.rep_report_schedule_config(status, next_run_at, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.rep_report_schedule_run', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.rep_report_schedule_run (
        report_schedule_run_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_rep_report_schedule_run PRIMARY KEY,
        report_schedule_config_id BIGINT NOT NULL,
        trigger_type VARCHAR(20) NOT NULL,
        triggered_by_user_id UNIQUEIDENTIFIER NULL,
        started_at DATETIME2(0) NOT NULL,
        finished_at DATETIME2(0) NULL,
        run_status VARCHAR(20) NOT NULL,
        output_file_key VARCHAR(120) NULL,
        output_file_name VARCHAR(255) NULL,
        output_row_count INT NULL,
        run_message NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_rep_report_schedule_run_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_rep_report_schedule_run_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_rep_report_schedule_run_config FOREIGN KEY (report_schedule_config_id) REFERENCES dbo.rep_report_schedule_config(report_schedule_config_id),
        CONSTRAINT FK_rep_report_schedule_run_triggered_by FOREIGN KEY (triggered_by_user_id) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_rep_report_schedule_run_trigger_type CHECK (
            trigger_type IN ('MANUAL','SCHEDULED')
        ),
        CONSTRAINT CK_rep_report_schedule_run_status CHECK (
            run_status IN ('RUNNING','SUCCESS','FAILED')
        )
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_rep_report_schedule_run_config_started' AND object_id = OBJECT_ID(N'dbo.rep_report_schedule_run'))
BEGIN
    CREATE INDEX IX_rep_report_schedule_run_config_started
        ON dbo.rep_report_schedule_run(report_schedule_config_id, started_at DESC, is_deleted);
END
GO

SET NOCOUNT OFF;
GO
