SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V007__employee_profile_workflow.sql
   Scope:
   - employee profile lock state
   - employee profile change request workflow
   - profile timeline event
   ========================================================= */

IF COL_LENGTH('dbo.hr_employee_profile', 'profile_status') IS NULL
BEGIN
    ALTER TABLE dbo.hr_employee_profile
    ADD profile_status VARCHAR(20) NOT NULL
        CONSTRAINT DF_hr_employee_profile_status DEFAULT 'ACTIVE';
END
GO

IF COL_LENGTH('dbo.hr_employee_profile', 'locked_reason') IS NULL
BEGIN
    ALTER TABLE dbo.hr_employee_profile
    ADD locked_reason NVARCHAR(500) NULL;
END
GO

IF COL_LENGTH('dbo.hr_employee_profile', 'locked_at') IS NULL
BEGIN
    ALTER TABLE dbo.hr_employee_profile
    ADD locked_at DATETIME2(0) NULL;
END
GO

IF COL_LENGTH('dbo.hr_employee_profile', 'locked_by') IS NULL
BEGIN
    ALTER TABLE dbo.hr_employee_profile
    ADD locked_by UNIQUEIDENTIFIER NULL;
END
GO

IF COL_LENGTH('dbo.hr_employee_profile', 'restored_at') IS NULL
BEGIN
    ALTER TABLE dbo.hr_employee_profile
    ADD restored_at DATETIME2(0) NULL;
END
GO

IF COL_LENGTH('dbo.hr_employee_profile', 'restored_by') IS NULL
BEGIN
    ALTER TABLE dbo.hr_employee_profile
    ADD restored_by UNIQUEIDENTIFIER NULL;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints WHERE name = N'CK_hr_employee_profile_status'
)
BEGIN
    ALTER TABLE dbo.hr_employee_profile
    ADD CONSTRAINT CK_hr_employee_profile_status CHECK (profile_status IN ('ACTIVE', 'LOCKED'));
END
GO

IF OBJECT_ID(N'dbo.hr_employee_profile_change_request', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_employee_profile_change_request (
        profile_change_request_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_employee_profile_change_request PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        requester_user_id UNIQUEIDENTIFIER NOT NULL,
        request_type VARCHAR(30) NOT NULL,
        request_status VARCHAR(20) NOT NULL,
        payload_json NVARCHAR(MAX) NOT NULL,
        submitted_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_profile_change_request_submitted_at DEFAULT SYSDATETIME(),
        reviewed_at DATETIME2(0) NULL,
        reviewer_user_id UNIQUEIDENTIFIER NULL,
        review_note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_profile_change_request_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_hr_employee_profile_change_request_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_hr_employee_profile_change_request_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_hr_employee_profile_change_request_requester FOREIGN KEY (requester_user_id) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_hr_employee_profile_change_request_reviewer FOREIGN KEY (reviewer_user_id) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_hr_employee_profile_change_request_type CHECK (request_type IN ('PROFILE_UPDATE')),
        CONSTRAINT CK_hr_employee_profile_change_request_status CHECK (request_status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_employee_profile_change_request_employee' AND object_id = OBJECT_ID(N'dbo.hr_employee_profile_change_request')
)
BEGIN
    CREATE INDEX IX_hr_employee_profile_change_request_employee
        ON dbo.hr_employee_profile_change_request(employee_id, request_status, is_deleted, submitted_at DESC);
END
GO

IF OBJECT_ID(N'dbo.hr_employee_profile_timeline', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hr_employee_profile_timeline (
        profile_timeline_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_hr_employee_profile_timeline PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        event_type VARCHAR(50) NOT NULL,
        summary NVARCHAR(255) NOT NULL,
        detail_json NVARCHAR(MAX) NULL,
        actor_user_id UNIQUEIDENTIFIER NULL,
        event_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_hr_employee_profile_timeline_event_at DEFAULT SYSDATETIME(),
        CONSTRAINT FK_hr_employee_profile_timeline_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_hr_employee_profile_timeline_actor FOREIGN KEY (actor_user_id) REFERENCES dbo.sec_user_account(user_id)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_hr_employee_profile_timeline_employee' AND object_id = OBJECT_ID(N'dbo.hr_employee_profile_timeline')
)
BEGIN
    CREATE INDEX IX_hr_employee_profile_timeline_employee
        ON dbo.hr_employee_profile_timeline(employee_id, event_at DESC);
END
GO

SET NOCOUNT OFF;
GO
