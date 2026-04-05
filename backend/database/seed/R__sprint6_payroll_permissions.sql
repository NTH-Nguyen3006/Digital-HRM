SET NOCOUNT ON;

/* =========================================================
   R__sprint6_payroll_permissions.sql
   Scope:
   - payroll permission seed
   - default payroll components
   - default payroll formula 2026
   ========================================================= */

UPDATE p
SET
    p.module_code = s.module_code,
    p.action_code = s.action_code,
    p.permission_name = s.permission_name,
    p.description = s.description,
    p.status = 'ACTIVE',
    p.updated_at = SYSDATETIME()
FROM dbo.sec_permission p
INNER JOIN (
    VALUES
        ('payroll.period.view', 'PAYROLL_PERIOD', 'VIEW', N'Xem danh sách kỳ lương', N'Cho phép xem danh sách kỳ lương và danh sách dòng lương'),
        ('payroll.period.create', 'PAYROLL_PERIOD', 'CREATE', N'Tạo kỳ lương', N'Cho phép tạo mới kỳ lương từ kỳ công đã chốt'),
        ('payroll.period.generate', 'PAYROLL_PERIOD', 'GENERATE', N'Tạo bảng lương nháp', N'Cho phép tạo hoặc tính lại bảng lương nháp'),
        ('payroll.component.view', 'PAYROLL_COMPONENT', 'VIEW', N'Xem thành phần lương', N'Cho phép xem danh mục thành phần lương'),
        ('payroll.component.manage', 'PAYROLL_COMPONENT', 'MANAGE', N'Quản lý thành phần lương', N'Cho phép tạo hoặc cập nhật thành phần lương'),
        ('payroll.formula.manage', 'PAYROLL_FORMULA', 'MANAGE', N'Quản lý công thức tính lương', N'Cho phép cấu hình công thức lương và PIT'),
        ('payroll.compensation.manage', 'PAYROLL_COMPENSATION', 'MANAGE', N'Thiết lập lương cho nhân viên', N'Cho phép thiết lập lương và ngân hàng cho nhân viên'),
        ('payroll.tax_profile.manage', 'PAYROLL_TAX_PROFILE', 'MANAGE', N'Quản lý hồ sơ thuế cá nhân', N'Cho phép thiết lập hồ sơ thuế và người phụ thuộc'),
        ('payroll.draft.adjust', 'PAYROLL_ITEM', 'ADJUST', N'Điều chỉnh bảng lương', N'Cho phép HR điều chỉnh dòng lương trước khi duyệt'),
        ('payroll.team.confirm', 'PAYROLL_ITEM', 'CONFIRM', N'Xác nhận bảng lương team', N'Cho phép manager xem và xác nhận bảng lương team'),
        ('payroll.period.approve', 'PAYROLL_PERIOD', 'APPROVE', N'Phê duyệt bảng lương', N'Cho phép HR phê duyệt bảng lương'),
        ('payroll.payslip.publish', 'PAYROLL_PAYSLIP', 'PUBLISH', N'Phát hành phiếu lương', N'Cho phép phát hành phiếu lương cá nhân'),
        ('payroll.payslip.view_self', 'PAYROLL_PAYSLIP', 'VIEW_SELF', N'Xem phiếu lương cá nhân', N'Cho phép nhân viên xem phiếu lương cá nhân'),
        ('payroll.bank.export', 'PAYROLL_REPORT', 'EXPORT_BANK', N'Xuất file chi lương', N'Cho phép xuất file chi lương chuyển khoản'),
        ('payroll.tax.export', 'PAYROLL_REPORT', 'EXPORT_TAX', N'Xuất báo cáo thuế', N'Cho phép xuất báo cáo PIT theo kỳ lương')
) s(permission_code, module_code, action_code, permission_name, description)
ON p.permission_code = s.permission_code
WHERE p.is_deleted = 0;

INSERT INTO dbo.sec_permission (
    permission_code, module_code, action_code, permission_name, description, status, created_at, is_deleted
)
SELECT
    s.permission_code, s.module_code, s.action_code, s.permission_name, s.description,
    'ACTIVE', SYSDATETIME(), 0
FROM (
    VALUES
        ('payroll.period.view', 'PAYROLL_PERIOD', 'VIEW', N'Xem danh sách kỳ lương', N'Cho phép xem danh sách kỳ lương và danh sách dòng lương'),
        ('payroll.period.create', 'PAYROLL_PERIOD', 'CREATE', N'Tạo kỳ lương', N'Cho phép tạo mới kỳ lương từ kỳ công đã chốt'),
        ('payroll.period.generate', 'PAYROLL_PERIOD', 'GENERATE', N'Tạo bảng lương nháp', N'Cho phép tạo hoặc tính lại bảng lương nháp'),
        ('payroll.component.view', 'PAYROLL_COMPONENT', 'VIEW', N'Xem thành phần lương', N'Cho phép xem danh mục thành phần lương'),
        ('payroll.component.manage', 'PAYROLL_COMPONENT', 'MANAGE', N'Quản lý thành phần lương', N'Cho phép tạo hoặc cập nhật thành phần lương'),
        ('payroll.formula.manage', 'PAYROLL_FORMULA', 'MANAGE', N'Quản lý công thức tính lương', N'Cho phép cấu hình công thức lương và PIT'),
        ('payroll.compensation.manage', 'PAYROLL_COMPENSATION', 'MANAGE', N'Thiết lập lương cho nhân viên', N'Cho phép thiết lập lương và ngân hàng cho nhân viên'),
        ('payroll.tax_profile.manage', 'PAYROLL_TAX_PROFILE', 'MANAGE', N'Quản lý hồ sơ thuế cá nhân', N'Cho phép thiết lập hồ sơ thuế và người phụ thuộc'),
        ('payroll.draft.adjust', 'PAYROLL_ITEM', 'ADJUST', N'Điều chỉnh bảng lương', N'Cho phép HR điều chỉnh dòng lương trước khi duyệt'),
        ('payroll.team.confirm', 'PAYROLL_ITEM', 'CONFIRM', N'Xác nhận bảng lương team', N'Cho phép manager xem và xác nhận bảng lương team'),
        ('payroll.period.approve', 'PAYROLL_PERIOD', 'APPROVE', N'Phê duyệt bảng lương', N'Cho phép HR phê duyệt bảng lương'),
        ('payroll.payslip.publish', 'PAYROLL_PAYSLIP', 'PUBLISH', N'Phát hành phiếu lương', N'Cho phép phát hành phiếu lương cá nhân'),
        ('payroll.payslip.view_self', 'PAYROLL_PAYSLIP', 'VIEW_SELF', N'Xem phiếu lương cá nhân', N'Cho phép nhân viên xem phiếu lương cá nhân'),
        ('payroll.bank.export', 'PAYROLL_REPORT', 'EXPORT_BANK', N'Xuất file chi lương', N'Cho phép xuất file chi lương chuyển khoản'),
        ('payroll.tax.export', 'PAYROLL_REPORT', 'EXPORT_TAX', N'Xuất báo cáo thuế', N'Cho phép xuất báo cáo PIT theo kỳ lương')
) s(permission_code, module_code, action_code, permission_name, description)
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.sec_permission p
    WHERE p.permission_code = s.permission_code
      AND p.is_deleted = 0
);

;WITH role_permission_seed(role_code, permission_code) AS (
    SELECT *
    FROM (VALUES
        ('HR', 'payroll.period.view'),
        ('HR', 'payroll.period.create'),
        ('HR', 'payroll.period.generate'),
        ('HR', 'payroll.component.view'),
        ('HR', 'payroll.component.manage'),
        ('HR', 'payroll.formula.manage'),
        ('HR', 'payroll.compensation.manage'),
        ('HR', 'payroll.tax_profile.manage'),
        ('HR', 'payroll.draft.adjust'),
        ('HR', 'payroll.period.approve'),
        ('HR', 'payroll.payslip.publish'),
        ('HR', 'payroll.bank.export'),
        ('HR', 'payroll.tax.export'),

        ('MANAGER', 'payroll.team.confirm'),

        ('EMPLOYEE', 'payroll.payslip.view_self')
    ) v(role_code, permission_code)
)
UPDATE rp
SET rp.is_allowed = 1
FROM dbo.sec_role_permission rp
INNER JOIN dbo.sec_role r
    ON rp.role_id = r.role_id
INNER JOIN dbo.sec_permission p
    ON rp.permission_id = p.permission_id
INNER JOIN role_permission_seed s
    ON s.role_code = r.role_code
   AND s.permission_code = p.permission_code;

;WITH role_permission_seed(role_code, permission_code) AS (
    SELECT *
    FROM (VALUES
        ('HR', 'payroll.period.view'),
        ('HR', 'payroll.period.create'),
        ('HR', 'payroll.period.generate'),
        ('HR', 'payroll.component.view'),
        ('HR', 'payroll.component.manage'),
        ('HR', 'payroll.formula.manage'),
        ('HR', 'payroll.compensation.manage'),
        ('HR', 'payroll.tax_profile.manage'),
        ('HR', 'payroll.draft.adjust'),
        ('HR', 'payroll.period.approve'),
        ('HR', 'payroll.payslip.publish'),
        ('HR', 'payroll.bank.export'),
        ('HR', 'payroll.tax.export'),

        ('MANAGER', 'payroll.team.confirm'),

        ('EMPLOYEE', 'payroll.payslip.view_self')
    ) v(role_code, permission_code)
)
INSERT INTO dbo.sec_role_permission (
    role_id, permission_id, is_allowed, created_at
)
SELECT
    r.role_id, p.permission_id, 1, SYSDATETIME()
FROM role_permission_seed s
INNER JOIN dbo.sec_role r
    ON r.role_code = s.role_code
   AND r.is_deleted = 0
INNER JOIN dbo.sec_permission p
    ON p.permission_code = s.permission_code
   AND p.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.sec_role_permission rp
    WHERE rp.role_id = r.role_id
      AND rp.permission_id = p.permission_id
);

MERGE dbo.pay_salary_component AS target
USING (
    SELECT 'ALLOWANCE_POSITION' AS component_code, N'Phụ cấp chức vụ' AS component_name, 'EARNING' AS component_category, 'FIXED_AMOUNT' AS amount_type, 1 AS taxable, 0 AS insurance_base_included, 1 AS payslip_visible, 100 AS display_order, 'ACTIVE' AS status, N'Phụ cấp cố định theo tháng.' AS description
    UNION ALL
    SELECT 'ALLOWANCE_RESPONSIBILITY', N'Phụ cấp trách nhiệm', 'EARNING', 'FIXED_AMOUNT', 1, 0, 1, 110, 'ACTIVE', N'Phụ cấp trách nhiệm cố định theo tháng.'
    UNION ALL
    SELECT 'BONUS_PERFORMANCE', N'Thưởng hiệu suất', 'EARNING', 'FIXED_AMOUNT', 1, 0, 1, 120, 'ACTIVE', N'Thưởng hiệu suất theo kỳ.'
    UNION ALL
    SELECT 'DEDUCTION_ADVANCE', N'Khấu trừ tạm ứng', 'DEDUCTION', 'FIXED_AMOUNT', 0, 0, 1, 800, 'ACTIVE', N'Khấu trừ hoàn ứng/tạm ứng.'
    UNION ALL
    SELECT 'DEDUCTION_OTHER', N'Khấu trừ khác', 'DEDUCTION', 'FIXED_AMOUNT', 0, 0, 1, 810, 'ACTIVE', N'Khấu trừ khác do HR nhập.'
) AS source
ON target.component_code = source.component_code AND target.is_deleted = 0
WHEN MATCHED THEN
    UPDATE SET
        target.component_name = source.component_name,
        target.component_category = source.component_category,
        target.amount_type = source.amount_type,
        target.taxable = source.taxable,
        target.insurance_base_included = source.insurance_base_included,
        target.payslip_visible = source.payslip_visible,
        target.display_order = source.display_order,
        target.status = source.status,
        target.description = source.description,
        target.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (
        component_code, component_name, component_category, amount_type, taxable, insurance_base_included,
        payslip_visible, display_order, status, description, created_at, is_deleted
    )
    VALUES (
        source.component_code, source.component_name, source.component_category, source.amount_type, source.taxable,
        source.insurance_base_included, source.payslip_visible, source.display_order, source.status,
        source.description, SYSDATETIME(), 0
    );

DECLARE @FormulaVersionId BIGINT;

MERGE dbo.pay_formula_version AS target
USING (
    SELECT
        'PAYROLL_STD_2026' AS formula_code,
        N'Cấu hình payroll chuẩn 2026' AS formula_name,
        CAST('2026-01-01' AS DATE) AS effective_from,
        NULL AS effective_to,
        CAST(15500000 AS DECIMAL(18,2)) AS personal_deduction_monthly,
        CAST(6200000 AS DECIMAL(18,2)) AS dependent_deduction_monthly,
        CAST(8.0000 AS DECIMAL(9,4)) AS social_insurance_employee_rate,
        CAST(1.5000 AS DECIMAL(9,4)) AS health_insurance_employee_rate,
        CAST(1.0000 AS DECIMAL(9,4)) AS unemployment_insurance_employee_rate,
        NULL AS insurance_salary_cap_amount,
        NULL AS unemployment_salary_cap_amount,
        CAST(8.00 AS DECIMAL(9,2)) AS standard_work_hours_per_day,
        CAST(1.5000 AS DECIMAL(9,4)) AS overtime_multiplier_weekday,
        'ACTIVE' AS status,
        N'Mặc định theo Luật Thuế TNCN 109/2025/QH15 áp dụng cho kỳ tính thuế năm 2026.' AS note
) AS source
ON target.formula_code = source.formula_code AND target.is_deleted = 0
WHEN MATCHED THEN
    UPDATE SET
        target.formula_name = source.formula_name,
        target.effective_from = source.effective_from,
        target.effective_to = source.effective_to,
        target.personal_deduction_monthly = source.personal_deduction_monthly,
        target.dependent_deduction_monthly = source.dependent_deduction_monthly,
        target.social_insurance_employee_rate = source.social_insurance_employee_rate,
        target.health_insurance_employee_rate = source.health_insurance_employee_rate,
        target.unemployment_insurance_employee_rate = source.unemployment_insurance_employee_rate,
        target.insurance_salary_cap_amount = source.insurance_salary_cap_amount,
        target.unemployment_salary_cap_amount = source.unemployment_salary_cap_amount,
        target.standard_work_hours_per_day = source.standard_work_hours_per_day,
        target.overtime_multiplier_weekday = source.overtime_multiplier_weekday,
        target.status = source.status,
        target.note = source.note,
        target.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (
        formula_code, formula_name, effective_from, effective_to, personal_deduction_monthly, dependent_deduction_monthly,
        social_insurance_employee_rate, health_insurance_employee_rate, unemployment_insurance_employee_rate,
        insurance_salary_cap_amount, unemployment_salary_cap_amount, standard_work_hours_per_day, overtime_multiplier_weekday,
        status, note, created_at, is_deleted
    )
    VALUES (
        source.formula_code, source.formula_name, source.effective_from, source.effective_to,
        source.personal_deduction_monthly, source.dependent_deduction_monthly,
        source.social_insurance_employee_rate, source.health_insurance_employee_rate, source.unemployment_insurance_employee_rate,
        source.insurance_salary_cap_amount, source.unemployment_salary_cap_amount, source.standard_work_hours_per_day,
        source.overtime_multiplier_weekday, source.status, source.note, SYSDATETIME(), 0
    );

SELECT @FormulaVersionId = formula_version_id
FROM dbo.pay_formula_version
WHERE formula_code = 'PAYROLL_STD_2026'
  AND is_deleted = 0;

UPDATE dbo.pay_formula_tax_bracket
SET is_deleted = 1,
    updated_at = SYSDATETIME()
WHERE formula_version_id = @FormulaVersionId
  AND is_deleted = 0;

INSERT INTO dbo.pay_formula_tax_bracket (
    formula_version_id, sequence_no, income_from, income_to, tax_rate, quick_deduction, created_at, is_deleted
)
SELECT @FormulaVersionId, 1, 0, 10000000, 5, 0, SYSDATETIME(), 0
UNION ALL
SELECT @FormulaVersionId, 2, 10000000.01, 30000000, 10, 500000, SYSDATETIME(), 0
UNION ALL
SELECT @FormulaVersionId, 3, 30000000.01, 60000000, 20, 3500000, SYSDATETIME(), 0
UNION ALL
SELECT @FormulaVersionId, 4, 60000000.01, 100000000, 30, 9500000, SYSDATETIME(), 0
UNION ALL
SELECT @FormulaVersionId, 5, 100000000.01, NULL, 35, 14500000, SYSDATETIME(), 0;

SET NOCOUNT OFF;
