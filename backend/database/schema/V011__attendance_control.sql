SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V011__attendance_control.sql
   Scope:
   - shift master + version
   - shift assignment
   - raw attendance log
   - attendance adjustment workflow
   - overtime request
   - attendance period
   - daily attendance summary
   ========================================================= */

IF OBJECT_ID(N'dbo.att_shift', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_shift (
        shift_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_shift PRIMARY KEY,
        shift_code VARCHAR(30) NOT NULL,
        shift_name NVARCHAR(200) NOT NULL,
        description NVARCHAR(500) NULL,
        sort_order INT NOT NULL
            CONSTRAINT DF_att_shift_sort_order DEFAULT 0,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_att_shift_status DEFAULT 'ACTIVE',
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_shift_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_att_shift_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT CK_att_shift_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_att_shift_code' AND object_id = OBJECT_ID(N'dbo.att_shift')
)
BEGIN
    CREATE UNIQUE INDEX UX_att_shift_code
        ON dbo.att_shift(shift_code)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.att_shift_version', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_shift_version (
        shift_version_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_shift_version PRIMARY KEY,
        shift_id BIGINT NOT NULL,
        version_no INT NOT NULL,
        effective_from DATE NOT NULL,
        effective_to DATE NULL,
        start_time TIME(0) NOT NULL,
        end_time TIME(0) NOT NULL,
        crosses_midnight BIT NOT NULL
            CONSTRAINT DF_att_shift_version_crosses_midnight DEFAULT 0,
        break_minutes INT NOT NULL
            CONSTRAINT DF_att_shift_version_break_minutes DEFAULT 0,
        late_grace_minutes INT NOT NULL
            CONSTRAINT DF_att_shift_version_late_grace_minutes DEFAULT 0,
        early_leave_grace_minutes INT NOT NULL
            CONSTRAINT DF_att_shift_version_early_leave_grace_minutes DEFAULT 0,
        ot_allowed BIT NOT NULL
            CONSTRAINT DF_att_shift_version_ot_allowed DEFAULT 0,
        night_shift BIT NOT NULL
            CONSTRAINT DF_att_shift_version_night_shift DEFAULT 0,
        min_work_minutes_for_present INT NULL,
        status VARCHAR(20) NOT NULL
            CONSTRAINT DF_att_shift_version_status DEFAULT 'ACTIVE',
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_shift_version_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_att_shift_version_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_att_shift_version_shift FOREIGN KEY (shift_id) REFERENCES dbo.att_shift(shift_id),
        CONSTRAINT CK_att_shift_version_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
        CONSTRAINT CK_att_shift_version_effective CHECK (effective_to IS NULL OR effective_to >= effective_from),
        CONSTRAINT CK_att_shift_version_break_minutes CHECK (break_minutes >= 0),
        CONSTRAINT CK_att_shift_version_late_grace_minutes CHECK (late_grace_minutes >= 0),
        CONSTRAINT CK_att_shift_version_early_leave_grace_minutes CHECK (early_leave_grace_minutes >= 0),
        CONSTRAINT CK_att_shift_version_min_work CHECK (min_work_minutes_for_present IS NULL OR min_work_minutes_for_present >= 0)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_att_shift_version_shift_version' AND object_id = OBJECT_ID(N'dbo.att_shift_version')
)
BEGIN
    CREATE UNIQUE INDEX UX_att_shift_version_shift_version
        ON dbo.att_shift_version(shift_id, version_no)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'IX_att_shift_version_effective' AND object_id = OBJECT_ID(N'dbo.att_shift_version')
)
BEGIN
    CREATE INDEX IX_att_shift_version_effective
        ON dbo.att_shift_version(shift_id, status, effective_from, effective_to, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.att_shift_assignment', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_shift_assignment (
        shift_assignment_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_shift_assignment PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        shift_id BIGINT NOT NULL,
        effective_from DATE NOT NULL,
        effective_to DATE NULL,
        assignment_note NVARCHAR(500) NULL,
        assignment_batch_ref VARCHAR(50) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_shift_assignment_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_att_shift_assignment_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_att_shift_assignment_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_att_shift_assignment_shift FOREIGN KEY (shift_id) REFERENCES dbo.att_shift(shift_id),
        CONSTRAINT CK_att_shift_assignment_effective CHECK (effective_to IS NULL OR effective_to >= effective_from)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'IX_att_shift_assignment_employee_effective' AND object_id = OBJECT_ID(N'dbo.att_shift_assignment')
)
BEGIN
    CREATE INDEX IX_att_shift_assignment_employee_effective
        ON dbo.att_shift_assignment(employee_id, effective_from, effective_to, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.att_adjustment_request', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_adjustment_request (
        adjustment_request_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_adjustment_request PRIMARY KEY,
        request_code VARCHAR(30) NOT NULL,
        employee_id BIGINT NOT NULL,
        attendance_date DATE NOT NULL,
        issue_type VARCHAR(30) NOT NULL,
        proposed_check_in_at DATETIME2(0) NULL,
        proposed_check_out_at DATETIME2(0) NULL,
        reason NVARCHAR(1000) NOT NULL,
        evidence_file_key VARCHAR(120) NULL,
        request_status VARCHAR(20) NOT NULL
            CONSTRAINT DF_att_adjustment_request_status DEFAULT 'SUBMITTED',
        submitted_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_adjustment_request_submitted_at DEFAULT SYSDATETIME(),
        approved_by UNIQUEIDENTIFIER NULL,
        approved_at DATETIME2(0) NULL,
        rejected_by UNIQUEIDENTIFIER NULL,
        rejected_at DATETIME2(0) NULL,
        finalized_by UNIQUEIDENTIFIER NULL,
        finalized_at DATETIME2(0) NULL,
        canceled_by UNIQUEIDENTIFIER NULL,
        canceled_at DATETIME2(0) NULL,
        manager_note NVARCHAR(1000) NULL,
        rejection_note NVARCHAR(1000) NULL,
        finalize_note NVARCHAR(1000) NULL,
        cancel_note NVARCHAR(1000) NULL,
        copied_from_adjustment_request_id BIGINT NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_adjustment_request_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_att_adjustment_request_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_att_adjustment_request_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_att_adjustment_request_approved_by FOREIGN KEY (approved_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_att_adjustment_request_rejected_by FOREIGN KEY (rejected_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_att_adjustment_request_finalized_by FOREIGN KEY (finalized_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_att_adjustment_request_canceled_by FOREIGN KEY (canceled_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_att_adjustment_request_copy_from FOREIGN KEY (copied_from_adjustment_request_id) REFERENCES dbo.att_adjustment_request(adjustment_request_id),
        CONSTRAINT CK_att_adjustment_request_issue_type CHECK (issue_type IN ('MISSING_CHECK_IN', 'MISSING_CHECK_OUT', 'MISSING_BOTH', 'WRONG_TIME', 'OTHER')),
        CONSTRAINT CK_att_adjustment_request_status CHECK (request_status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'CANCELLED', 'FINALIZED')),
        CONSTRAINT CK_att_adjustment_request_proposed_time CHECK (proposed_check_in_at IS NOT NULL OR proposed_check_out_at IS NOT NULL)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_att_adjustment_request_code' AND object_id = OBJECT_ID(N'dbo.att_adjustment_request')
)
BEGIN
    CREATE UNIQUE INDEX UX_att_adjustment_request_code
        ON dbo.att_adjustment_request(request_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'IX_att_adjustment_request_employee_status' AND object_id = OBJECT_ID(N'dbo.att_adjustment_request')
)
BEGIN
    CREATE INDEX IX_att_adjustment_request_employee_status
        ON dbo.att_adjustment_request(employee_id, attendance_date, request_status, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.att_adjustment_history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_adjustment_history (
        adjustment_history_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_adjustment_history PRIMARY KEY,
        adjustment_request_id BIGINT NOT NULL,
        from_status VARCHAR(20) NULL,
        to_status VARCHAR(20) NOT NULL,
        action_code VARCHAR(50) NOT NULL,
        action_note NVARCHAR(1000) NULL,
        changed_at DATETIME2(0) NOT NULL,
        changed_by UNIQUEIDENTIFIER NULL,
        snapshot_json NVARCHAR(MAX) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_adjustment_history_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        CONSTRAINT FK_att_adjustment_history_request FOREIGN KEY (adjustment_request_id) REFERENCES dbo.att_adjustment_request(adjustment_request_id),
        CONSTRAINT FK_att_adjustment_history_changed_by FOREIGN KEY (changed_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_att_adjustment_history_to_status CHECK (to_status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'CANCELLED', 'FINALIZED')),
        CONSTRAINT CK_att_adjustment_history_from_status CHECK (from_status IS NULL OR from_status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'CANCELLED', 'FINALIZED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'IX_att_adjustment_history_request' AND object_id = OBJECT_ID(N'dbo.att_adjustment_history')
)
BEGIN
    CREATE INDEX IX_att_adjustment_history_request
        ON dbo.att_adjustment_history(adjustment_request_id, changed_at);
END
GO

IF OBJECT_ID(N'dbo.att_attendance_period', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_attendance_period (
        attendance_period_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_attendance_period PRIMARY KEY,
        period_code VARCHAR(20) NOT NULL,
        period_year INT NOT NULL,
        period_month INT NOT NULL,
        period_start_date DATE NOT NULL,
        period_end_date DATE NOT NULL,
        period_status VARCHAR(20) NOT NULL
            CONSTRAINT DF_att_attendance_period_status DEFAULT 'DRAFT',
        note NVARCHAR(500) NULL,
        closed_by UNIQUEIDENTIFIER NULL,
        closed_at DATETIME2(0) NULL,
        reopened_by UNIQUEIDENTIFIER NULL,
        reopened_at DATETIME2(0) NULL,
        reopen_reason NVARCHAR(1000) NULL,
        reopened_flag BIT NOT NULL
            CONSTRAINT DF_att_attendance_period_reopened_flag DEFAULT 0,
        total_employee_count INT NOT NULL
            CONSTRAINT DF_att_attendance_period_total_employee_count DEFAULT 0,
        total_anomaly_day_count INT NOT NULL
            CONSTRAINT DF_att_attendance_period_total_anomaly_day_count DEFAULT 0,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_attendance_period_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_att_attendance_period_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_att_attendance_period_closed_by FOREIGN KEY (closed_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_att_attendance_period_reopened_by FOREIGN KEY (reopened_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_att_attendance_period_month CHECK (period_month BETWEEN 1 AND 12),
        CONSTRAINT CK_att_attendance_period_status CHECK (period_status IN ('DRAFT', 'REVIEW', 'CLOSED'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_att_attendance_period_year_month' AND object_id = OBJECT_ID(N'dbo.att_attendance_period')
)
BEGIN
    CREATE UNIQUE INDEX UX_att_attendance_period_year_month
        ON dbo.att_attendance_period(period_year, period_month)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.att_overtime_request', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_overtime_request (
        overtime_request_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_overtime_request PRIMARY KEY,
        request_code VARCHAR(30) NOT NULL,
        employee_id BIGINT NOT NULL,
        attendance_date DATE NOT NULL,
        overtime_start_at DATETIME2(0) NOT NULL,
        overtime_end_at DATETIME2(0) NOT NULL,
        requested_minutes INT NOT NULL,
        reason NVARCHAR(1000) NOT NULL,
        evidence_file_key VARCHAR(120) NULL,
        request_status VARCHAR(20) NOT NULL
            CONSTRAINT DF_att_overtime_request_status DEFAULT 'SUBMITTED',
        submitted_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_overtime_request_submitted_at DEFAULT SYSDATETIME(),
        approved_by UNIQUEIDENTIFIER NULL,
        approved_at DATETIME2(0) NULL,
        rejected_by UNIQUEIDENTIFIER NULL,
        rejected_at DATETIME2(0) NULL,
        manager_note NVARCHAR(1000) NULL,
        rejection_note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_overtime_request_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_att_overtime_request_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_att_overtime_request_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_att_overtime_request_approved_by FOREIGN KEY (approved_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_att_overtime_request_rejected_by FOREIGN KEY (rejected_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_att_overtime_request_status CHECK (request_status IN ('SUBMITTED', 'APPROVED', 'REJECTED')),
        CONSTRAINT CK_att_overtime_request_minutes CHECK (requested_minutes > 0),
        CONSTRAINT CK_att_overtime_request_time CHECK (overtime_end_at > overtime_start_at)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_att_overtime_request_code' AND object_id = OBJECT_ID(N'dbo.att_overtime_request')
)
BEGIN
    CREATE UNIQUE INDEX UX_att_overtime_request_code
        ON dbo.att_overtime_request(request_code)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'IX_att_overtime_request_employee_status' AND object_id = OBJECT_ID(N'dbo.att_overtime_request')
)
BEGIN
    CREATE INDEX IX_att_overtime_request_employee_status
        ON dbo.att_overtime_request(employee_id, attendance_date, request_status, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.att_attendance_log', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_attendance_log (
        attendance_log_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_attendance_log PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        attendance_date DATE NOT NULL,
        event_type VARCHAR(20) NOT NULL,
        event_time DATETIME2(0) NOT NULL,
        source_type VARCHAR(20) NOT NULL,
        shift_assignment_id BIGINT NULL,
        adjustment_request_id BIGINT NULL,
        latitude DECIMAL(10,7) NULL,
        longitude DECIMAL(10,7) NULL,
        device_ref VARCHAR(100) NULL,
        note NVARCHAR(500) NULL,
        raw_payload_json NVARCHAR(MAX) NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_attendance_log_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_att_attendance_log_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_att_attendance_log_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_att_attendance_log_shift_assignment FOREIGN KEY (shift_assignment_id) REFERENCES dbo.att_shift_assignment(shift_assignment_id),
        CONSTRAINT FK_att_attendance_log_adjustment_request FOREIGN KEY (adjustment_request_id) REFERENCES dbo.att_adjustment_request(adjustment_request_id),
        CONSTRAINT CK_att_attendance_log_event_type CHECK (event_type IN ('CHECK_IN', 'CHECK_OUT')),
        CONSTRAINT CK_att_attendance_log_source_type CHECK (source_type IN ('WEB', 'MOBILE_APP', 'IMPORT', 'DEVICE', 'ADJUSTMENT'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'IX_att_attendance_log_employee_date' AND object_id = OBJECT_ID(N'dbo.att_attendance_log')
)
BEGIN
    CREATE INDEX IX_att_attendance_log_employee_date
        ON dbo.att_attendance_log(employee_id, attendance_date, event_time, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.att_daily_attendance', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.att_daily_attendance (
        daily_attendance_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_att_daily_attendance PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        attendance_date DATE NOT NULL,
        shift_assignment_id BIGINT NULL,
        shift_version_id BIGINT NULL,
        attendance_period_id BIGINT NULL,
        finalized_adjustment_request_id BIGINT NULL,
        planned_start_at DATETIME2(0) NULL,
        planned_end_at DATETIME2(0) NULL,
        actual_check_in_at DATETIME2(0) NULL,
        actual_check_out_at DATETIME2(0) NULL,
        worked_minutes INT NOT NULL
            CONSTRAINT DF_att_daily_attendance_worked_minutes DEFAULT 0,
        late_minutes INT NOT NULL
            CONSTRAINT DF_att_daily_attendance_late_minutes DEFAULT 0,
        early_leave_minutes INT NOT NULL
            CONSTRAINT DF_att_daily_attendance_early_leave_minutes DEFAULT 0,
        approved_ot_minutes INT NOT NULL
            CONSTRAINT DF_att_daily_attendance_approved_ot_minutes DEFAULT 0,
        missing_check_in BIT NOT NULL
            CONSTRAINT DF_att_daily_attendance_missing_check_in DEFAULT 0,
        missing_check_out BIT NOT NULL
            CONSTRAINT DF_att_daily_attendance_missing_check_out DEFAULT 0,
        anomaly_count INT NOT NULL
            CONSTRAINT DF_att_daily_attendance_anomaly_count DEFAULT 0,
        anomaly_codes VARCHAR(500) NULL,
        daily_status VARCHAR(20) NOT NULL
            CONSTRAINT DF_att_daily_attendance_daily_status DEFAULT 'ABSENT',
        on_leave BIT NOT NULL
            CONSTRAINT DF_att_daily_attendance_on_leave DEFAULT 0,
        calculated_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_daily_attendance_calculated_at DEFAULT SYSDATETIME(),
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT DF_att_daily_attendance_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL
            CONSTRAINT DF_att_daily_attendance_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_att_daily_attendance_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_att_daily_attendance_shift_assignment FOREIGN KEY (shift_assignment_id) REFERENCES dbo.att_shift_assignment(shift_assignment_id),
        CONSTRAINT FK_att_daily_attendance_shift_version FOREIGN KEY (shift_version_id) REFERENCES dbo.att_shift_version(shift_version_id),
        CONSTRAINT FK_att_daily_attendance_period FOREIGN KEY (attendance_period_id) REFERENCES dbo.att_attendance_period(attendance_period_id),
        CONSTRAINT FK_att_daily_attendance_finalized_adjustment FOREIGN KEY (finalized_adjustment_request_id) REFERENCES dbo.att_adjustment_request(adjustment_request_id),
        CONSTRAINT CK_att_daily_attendance_daily_status CHECK (daily_status IN ('NO_SHIFT', 'ABSENT', 'PRESENT', 'INCOMPLETE', 'ON_LEAVE')),
        CONSTRAINT CK_att_daily_attendance_non_negative CHECK (worked_minutes >= 0 AND late_minutes >= 0 AND early_leave_minutes >= 0 AND approved_ot_minutes >= 0 AND anomaly_count >= 0)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_att_daily_attendance_employee_date' AND object_id = OBJECT_ID(N'dbo.att_daily_attendance')
)
BEGIN
    CREATE UNIQUE INDEX UX_att_daily_attendance_employee_date
        ON dbo.att_daily_attendance(employee_id, attendance_date)
        WHERE is_deleted = 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'IX_att_daily_attendance_date_status' AND object_id = OBJECT_ID(N'dbo.att_daily_attendance')
)
BEGIN
    CREATE INDEX IX_att_daily_attendance_date_status
        ON dbo.att_daily_attendance(attendance_date, daily_status, anomaly_count, is_deleted);
END
GO

SET NOCOUNT OFF;
GO
