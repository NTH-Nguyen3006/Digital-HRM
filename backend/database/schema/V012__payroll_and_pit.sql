SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   V012__payroll_and_pit.sql
   Scope:
   - salary component master
   - payroll formula version + PIT bracket
   - employee compensation
   - payroll period / payroll item / payslip line
   - personal tax profile / dependents
   ========================================================= */

IF OBJECT_ID(N'dbo.pay_salary_component', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pay_salary_component (
        salary_component_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_pay_salary_component PRIMARY KEY,
        component_code VARCHAR(30) NOT NULL,
        component_name NVARCHAR(200) NOT NULL,
        component_category VARCHAR(20) NOT NULL,
        amount_type VARCHAR(20) NOT NULL,
        taxable BIT NOT NULL CONSTRAINT DF_pay_salary_component_taxable DEFAULT 1,
        insurance_base_included BIT NOT NULL CONSTRAINT DF_pay_salary_component_ins_base DEFAULT 0,
        payslip_visible BIT NOT NULL CONSTRAINT DF_pay_salary_component_visible DEFAULT 1,
        display_order INT NOT NULL CONSTRAINT DF_pay_salary_component_display_order DEFAULT 0,
        status VARCHAR(20) NOT NULL CONSTRAINT DF_pay_salary_component_status DEFAULT 'ACTIVE',
        description NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_pay_salary_component_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_pay_salary_component_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT CK_pay_salary_component_category CHECK (component_category IN ('EARNING','DEDUCTION','INFO')),
        CONSTRAINT CK_pay_salary_component_amount_type CHECK (amount_type IN ('FIXED_AMOUNT','PERCENT_BASE','SYSTEM')),
        CONSTRAINT CK_pay_salary_component_status CHECK (status IN ('ACTIVE','INACTIVE'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_pay_salary_component_code' AND object_id = OBJECT_ID(N'dbo.pay_salary_component'))
BEGIN
    CREATE UNIQUE INDEX UX_pay_salary_component_code
        ON dbo.pay_salary_component(component_code)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.pay_formula_version', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pay_formula_version (
        formula_version_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_pay_formula_version PRIMARY KEY,
        formula_code VARCHAR(30) NOT NULL,
        formula_name NVARCHAR(200) NOT NULL,
        effective_from DATE NOT NULL,
        effective_to DATE NULL,
        personal_deduction_monthly DECIMAL(18,2) NOT NULL,
        dependent_deduction_monthly DECIMAL(18,2) NOT NULL,
        social_insurance_employee_rate DECIMAL(9,4) NOT NULL,
        health_insurance_employee_rate DECIMAL(9,4) NOT NULL,
        unemployment_insurance_employee_rate DECIMAL(9,4) NOT NULL,
        insurance_salary_cap_amount DECIMAL(18,2) NULL,
        unemployment_salary_cap_amount DECIMAL(18,2) NULL,
        standard_work_hours_per_day DECIMAL(9,2) NOT NULL,
        overtime_multiplier_weekday DECIMAL(9,4) NOT NULL,
        status VARCHAR(20) NOT NULL CONSTRAINT DF_pay_formula_version_status DEFAULT 'ACTIVE',
        note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_pay_formula_version_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_pay_formula_version_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT CK_pay_formula_version_effective CHECK (effective_to IS NULL OR effective_to >= effective_from),
        CONSTRAINT CK_pay_formula_version_status CHECK (status IN ('ACTIVE','INACTIVE'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_pay_formula_version_code' AND object_id = OBJECT_ID(N'dbo.pay_formula_version'))
BEGIN
    CREATE UNIQUE INDEX UX_pay_formula_version_code
        ON dbo.pay_formula_version(formula_code)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.pay_formula_tax_bracket', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pay_formula_tax_bracket (
        formula_tax_bracket_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_pay_formula_tax_bracket PRIMARY KEY,
        formula_version_id BIGINT NOT NULL,
        sequence_no INT NOT NULL,
        income_from DECIMAL(18,2) NOT NULL,
        income_to DECIMAL(18,2) NULL,
        tax_rate DECIMAL(9,4) NOT NULL,
        quick_deduction DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_formula_tax_bracket_quick DEFAULT 0,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_pay_formula_tax_bracket_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_pay_formula_tax_bracket_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_pay_formula_tax_bracket_formula FOREIGN KEY (formula_version_id) REFERENCES dbo.pay_formula_version(formula_version_id),
        CONSTRAINT CK_pay_formula_tax_bracket_range CHECK (income_to IS NULL OR income_to >= income_from)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_pay_formula_tax_bracket_formula_seq' AND object_id = OBJECT_ID(N'dbo.pay_formula_tax_bracket'))
BEGIN
    CREATE UNIQUE INDEX UX_pay_formula_tax_bracket_formula_seq
        ON dbo.pay_formula_tax_bracket(formula_version_id, sequence_no)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.pay_employee_compensation', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pay_employee_compensation (
        employee_compensation_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_pay_employee_compensation PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        effective_from DATE NOT NULL,
        effective_to DATE NULL,
        base_salary DECIMAL(18,2) NOT NULL,
        insurance_salary_base DECIMAL(18,2) NULL,
        salary_currency VARCHAR(10) NOT NULL CONSTRAINT DF_pay_employee_compensation_currency DEFAULT 'VND',
        bank_account_name NVARCHAR(200) NULL,
        bank_account_no VARCHAR(50) NULL,
        bank_name NVARCHAR(200) NULL,
        payment_note NVARCHAR(500) NULL,
        status VARCHAR(20) NOT NULL CONSTRAINT DF_pay_employee_compensation_status DEFAULT 'ACTIVE',
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_pay_employee_compensation_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_pay_employee_compensation_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_pay_employee_compensation_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_pay_employee_compensation_effective CHECK (effective_to IS NULL OR effective_to >= effective_from),
        CONSTRAINT CK_pay_employee_compensation_status CHECK (status IN ('ACTIVE','INACTIVE'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_pay_employee_compensation_employee_effective' AND object_id = OBJECT_ID(N'dbo.pay_employee_compensation'))
BEGIN
    CREATE INDEX IX_pay_employee_compensation_employee_effective
        ON dbo.pay_employee_compensation(employee_id, effective_from, effective_to, status, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.pay_employee_compensation_item', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pay_employee_compensation_item (
        employee_compensation_item_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_pay_employee_compensation_item PRIMARY KEY,
        employee_compensation_id BIGINT NOT NULL,
        salary_component_id BIGINT NOT NULL,
        amount_value DECIMAL(18,2) NULL,
        percentage_value DECIMAL(9,4) NULL,
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_pay_employee_compensation_item_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_pay_employee_compensation_item_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_pay_employee_compensation_item_comp FOREIGN KEY (employee_compensation_id) REFERENCES dbo.pay_employee_compensation(employee_compensation_id),
        CONSTRAINT FK_pay_employee_compensation_item_component FOREIGN KEY (salary_component_id) REFERENCES dbo.pay_salary_component(salary_component_id),
        CONSTRAINT CK_pay_employee_compensation_item_value CHECK (amount_value IS NOT NULL OR percentage_value IS NOT NULL)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_pay_employee_compensation_item_unique' AND object_id = OBJECT_ID(N'dbo.pay_employee_compensation_item'))
BEGIN
    CREATE UNIQUE INDEX UX_pay_employee_compensation_item_unique
        ON dbo.pay_employee_compensation_item(employee_compensation_id, salary_component_id)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.pay_personal_tax_profile', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pay_personal_tax_profile (
        personal_tax_profile_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_pay_personal_tax_profile PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        resident_taxpayer BIT NOT NULL CONSTRAINT DF_pay_personal_tax_profile_resident DEFAULT 1,
        family_deduction_enabled BIT NOT NULL CONSTRAINT DF_pay_personal_tax_profile_family DEFAULT 1,
        tax_registration_date DATE NULL,
        tax_finalization_method VARCHAR(30) NOT NULL CONSTRAINT DF_pay_personal_tax_profile_method DEFAULT 'MONTHLY_WITHHOLDING',
        status VARCHAR(20) NOT NULL CONSTRAINT DF_pay_personal_tax_profile_status DEFAULT 'ACTIVE',
        note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_pay_personal_tax_profile_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_pay_personal_tax_profile_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_pay_personal_tax_profile_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_pay_personal_tax_profile_method CHECK (tax_finalization_method IN ('MONTHLY_WITHHOLDING','ANNUAL_AUTHORIZED','SELF_FINALIZATION')),
        CONSTRAINT CK_pay_personal_tax_profile_status CHECK (status IN ('ACTIVE','INACTIVE'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_pay_personal_tax_profile_employee' AND object_id = OBJECT_ID(N'dbo.pay_personal_tax_profile'))
BEGIN
    CREATE UNIQUE INDEX UX_pay_personal_tax_profile_employee
        ON dbo.pay_personal_tax_profile(employee_id)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.pay_tax_dependent', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pay_tax_dependent (
        tax_dependent_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_pay_tax_dependent PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        full_name NVARCHAR(200) NOT NULL,
        relationship_code VARCHAR(20) NOT NULL,
        identification_no VARCHAR(50) NULL,
        date_of_birth DATE NULL,
        deduction_start_month DATE NOT NULL,
        deduction_end_month DATE NULL,
        status VARCHAR(20) NOT NULL CONSTRAINT DF_pay_tax_dependent_status DEFAULT 'ACTIVE',
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_pay_tax_dependent_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_pay_tax_dependent_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_pay_tax_dependent_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT CK_pay_tax_dependent_relationship CHECK (relationship_code IN ('FATHER','MOTHER','SPOUSE','CHILD','SIBLING','OTHER')),
        CONSTRAINT CK_pay_tax_dependent_status CHECK (status IN ('ACTIVE','INACTIVE')),
        CONSTRAINT CK_pay_tax_dependent_month CHECK (deduction_end_month IS NULL OR deduction_end_month >= deduction_start_month)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_pay_tax_dependent_employee' AND object_id = OBJECT_ID(N'dbo.pay_tax_dependent'))
BEGIN
    CREATE INDEX IX_pay_tax_dependent_employee
        ON dbo.pay_tax_dependent(employee_id, status, deduction_start_month, deduction_end_month, is_deleted);
END
GO

IF OBJECT_ID(N'dbo.pay_payroll_period', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pay_payroll_period (
        payroll_period_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_pay_payroll_period PRIMARY KEY,
        period_code VARCHAR(20) NOT NULL,
        period_year INT NOT NULL,
        period_month INT NOT NULL,
        period_start_date DATE NOT NULL,
        period_end_date DATE NOT NULL,
        attendance_period_id BIGINT NOT NULL,
        formula_version_id BIGINT NOT NULL,
        period_status VARCHAR(20) NOT NULL CONSTRAINT DF_pay_payroll_period_status DEFAULT 'DRAFT',
        generated_at DATETIME2(0) NULL,
        generated_by UNIQUEIDENTIFIER NULL,
        approved_at DATETIME2(0) NULL,
        approved_by UNIQUEIDENTIFIER NULL,
        published_at DATETIME2(0) NULL,
        published_by UNIQUEIDENTIFIER NULL,
        note NVARCHAR(1000) NULL,
        total_employee_count INT NOT NULL CONSTRAINT DF_pay_payroll_period_total_employee DEFAULT 0,
        manager_confirmed_count INT NOT NULL CONSTRAINT DF_pay_payroll_period_manager_confirmed DEFAULT 0,
        total_gross_amount DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_period_total_gross DEFAULT 0,
        total_net_amount DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_period_total_net DEFAULT 0,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_pay_payroll_period_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_pay_payroll_period_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_pay_payroll_period_att_period FOREIGN KEY (attendance_period_id) REFERENCES dbo.att_attendance_period(attendance_period_id),
        CONSTRAINT FK_pay_payroll_period_formula FOREIGN KEY (formula_version_id) REFERENCES dbo.pay_formula_version(formula_version_id),
        CONSTRAINT FK_pay_payroll_period_generated_by FOREIGN KEY (generated_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_pay_payroll_period_approved_by FOREIGN KEY (approved_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_pay_payroll_period_published_by FOREIGN KEY (published_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_pay_payroll_period_status CHECK (period_status IN ('DRAFT','TEAM_REVIEW','APPROVED','PUBLISHED')),
        CONSTRAINT CK_pay_payroll_period_month CHECK (period_month BETWEEN 1 AND 12)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_pay_payroll_period_year_month' AND object_id = OBJECT_ID(N'dbo.pay_payroll_period'))
BEGIN
    CREATE UNIQUE INDEX UX_pay_payroll_period_year_month
        ON dbo.pay_payroll_period(period_year, period_month)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.pay_payroll_item', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pay_payroll_item (
        payroll_item_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_pay_payroll_item PRIMARY KEY,
        payroll_period_id BIGINT NOT NULL,
        employee_id BIGINT NOT NULL,
        scheduled_day_count INT NOT NULL CONSTRAINT DF_pay_payroll_item_sched DEFAULT 0,
        present_day_count INT NOT NULL CONSTRAINT DF_pay_payroll_item_present DEFAULT 0,
        paid_leave_day_count INT NOT NULL CONSTRAINT DF_pay_payroll_item_paid_leave DEFAULT 0,
        unpaid_leave_day_count INT NOT NULL CONSTRAINT DF_pay_payroll_item_unpaid_leave DEFAULT 0,
        absent_day_count INT NOT NULL CONSTRAINT DF_pay_payroll_item_absent DEFAULT 0,
        approved_ot_minutes INT NOT NULL CONSTRAINT DF_pay_payroll_item_ot DEFAULT 0,
        base_salary_monthly DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_base_monthly DEFAULT 0,
        base_salary_prorated DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_base_prorated DEFAULT 0,
        fixed_earning_total DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_fixed_earning DEFAULT 0,
        fixed_deduction_total DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_fixed_deduction DEFAULT 0,
        employee_insurance_amount DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_insurance DEFAULT 0,
        personal_deduction_amount DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_personal_ded DEFAULT 0,
        dependent_deduction_amount DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_dep_ded DEFAULT 0,
        taxable_income DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_taxable DEFAULT 0,
        pit_amount DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_pit DEFAULT 0,
        gross_income DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_gross DEFAULT 0,
        net_pay DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_net DEFAULT 0,
        item_status VARCHAR(20) NOT NULL CONSTRAINT DF_pay_payroll_item_status DEFAULT 'DRAFT',
        manager_confirmation_required BIT NOT NULL CONSTRAINT DF_pay_payroll_item_mgr_required DEFAULT 1,
        manager_confirmed_by UNIQUEIDENTIFIER NULL,
        manager_confirmed_at DATETIME2(0) NULL,
        manager_confirm_note NVARCHAR(1000) NULL,
        hr_approved_by UNIQUEIDENTIFIER NULL,
        hr_approved_at DATETIME2(0) NULL,
        published_by UNIQUEIDENTIFIER NULL,
        published_at DATETIME2(0) NULL,
        adjustment_note NVARCHAR(1000) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_pay_payroll_item_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_pay_payroll_item_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_pay_payroll_item_period FOREIGN KEY (payroll_period_id) REFERENCES dbo.pay_payroll_period(payroll_period_id),
        CONSTRAINT FK_pay_payroll_item_employee FOREIGN KEY (employee_id) REFERENCES dbo.hr_employee(employee_id),
        CONSTRAINT FK_pay_payroll_item_manager FOREIGN KEY (manager_confirmed_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_pay_payroll_item_hr FOREIGN KEY (hr_approved_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT FK_pay_payroll_item_published FOREIGN KEY (published_by) REFERENCES dbo.sec_user_account(user_id),
        CONSTRAINT CK_pay_payroll_item_status CHECK (item_status IN ('DRAFT','MANAGER_CONFIRMED','HR_APPROVED','PUBLISHED'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_pay_payroll_item_period_employee' AND object_id = OBJECT_ID(N'dbo.pay_payroll_item'))
BEGIN
    CREATE UNIQUE INDEX UX_pay_payroll_item_period_employee
        ON dbo.pay_payroll_item(payroll_period_id, employee_id)
        WHERE is_deleted = 0;
END
GO

IF OBJECT_ID(N'dbo.pay_payroll_item_line', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pay_payroll_item_line (
        payroll_item_line_id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_pay_payroll_item_line PRIMARY KEY,
        payroll_item_id BIGINT NOT NULL,
        component_code VARCHAR(30) NOT NULL,
        component_name NVARCHAR(200) NOT NULL,
        component_category VARCHAR(20) NOT NULL,
        line_source_type VARCHAR(20) NOT NULL,
        line_amount DECIMAL(18,2) NOT NULL CONSTRAINT DF_pay_payroll_item_line_amount DEFAULT 0,
        taxable BIT NOT NULL CONSTRAINT DF_pay_payroll_item_line_taxable DEFAULT 0,
        display_order INT NOT NULL CONSTRAINT DF_pay_payroll_item_line_display DEFAULT 0,
        line_note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_pay_payroll_item_line_created_at DEFAULT SYSDATETIME(),
        created_by UNIQUEIDENTIFIER NULL,
        updated_at DATETIME2(0) NULL,
        updated_by UNIQUEIDENTIFIER NULL,
        is_deleted BIT NOT NULL CONSTRAINT DF_pay_payroll_item_line_is_deleted DEFAULT 0,
        row_version ROWVERSION NOT NULL,
        CONSTRAINT FK_pay_payroll_item_line_item FOREIGN KEY (payroll_item_id) REFERENCES dbo.pay_payroll_item(payroll_item_id),
        CONSTRAINT CK_pay_payroll_item_line_category CHECK (component_category IN ('EARNING','DEDUCTION','INFO')),
        CONSTRAINT CK_pay_payroll_item_line_source CHECK (line_source_type IN ('SYSTEM','CONFIGURED','MANUAL_ADJUSTMENT'))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_pay_payroll_item_line_item' AND object_id = OBJECT_ID(N'dbo.pay_payroll_item_line'))
BEGIN
    CREATE INDEX IX_pay_payroll_item_line_item
        ON dbo.pay_payroll_item_line(payroll_item_id, display_order, is_deleted);
END
GO

SET NOCOUNT OFF;
