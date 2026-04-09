SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__111__seed_feature_rich_dataset.sql
   Scope:
   - Expand realistic demo data for major HRM features
   - Keep all inserts idempotent and deterministic for CI/CD
   - Focus on denser org, employee, workflow, portal and reporting data
   ========================================================= */

-- 1. Expand organization tree with team-level nodes
;WITH ParentOrg AS (
    SELECT org_unit_code, org_unit_id, hierarchy_level, path_code
    FROM dbo.hr_org_unit
    WHERE org_unit_code IN ('DEPT_HR_HN', 'DEPT_IT_HN', 'DEPT_SALES_HCM', 'DEPT_FIN_HCM')
),
OrgSeed AS (
    SELECT *
    FROM (VALUES
        ('TEAM_TA_HN',       N'Talent Acquisition Team (HN)',     'TEAM', 'DEPT_HR_HN',     1),
        ('TEAM_CNB_HN',      N'C&B Team (HN)',                    'TEAM', 'DEPT_HR_HN',     2),
        ('TEAM_PLATFORM_HN', N'Platform Engineering Team (HN)',   'TEAM', 'DEPT_IT_HN',     1),
        ('TEAM_QA_HN',       N'Quality Assurance Team (HN)',      'TEAM', 'DEPT_IT_HN',     2),
        ('TEAM_SMB_HCM',     N'SMB Sales Team (HCM)',             'TEAM', 'DEPT_SALES_HCM', 1),
        ('TEAM_ENT_HCM',     N'Enterprise Sales Team (HCM)',      'TEAM', 'DEPT_SALES_HCM', 2),
        ('TEAM_AP_HCM',      N'Accounts Payable Team (HCM)',      'TEAM', 'DEPT_FIN_HCM',   1),
        ('TEAM_PAYROLL_HCM', N'Payroll Operations Team (HCM)',    'TEAM', 'DEPT_FIN_HCM',   2)
    ) AS s(org_unit_code, org_unit_name, org_unit_type, parent_org_unit_code, sort_order)
),
Resolved AS (
    SELECT
        s.org_unit_code,
        s.org_unit_name,
        s.org_unit_type,
        p.org_unit_id AS parent_org_unit_id,
        p.hierarchy_level + 1 AS hierarchy_level,
        p.path_code + s.org_unit_code + '/' AS path_code,
        s.sort_order
    FROM OrgSeed s
    INNER JOIN ParentOrg p ON p.org_unit_code = s.parent_org_unit_code
)
MERGE dbo.hr_org_unit AS target
USING Resolved AS source
ON target.org_unit_code = source.org_unit_code
WHEN MATCHED THEN
    UPDATE SET
        target.parent_org_unit_id = source.parent_org_unit_id,
        target.org_unit_name = source.org_unit_name,
        target.org_unit_type = source.org_unit_type,
        target.status = 'ACTIVE',
        target.effective_from = '2024-01-01',
        target.hierarchy_level = source.hierarchy_level,
        target.path_code = source.path_code,
        target.sort_order = source.sort_order,
        target.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (
        parent_org_unit_id, org_unit_code, org_unit_name, org_unit_type,
        status, effective_from, hierarchy_level, path_code, sort_order
    )
    VALUES (
        source.parent_org_unit_id, source.org_unit_code, source.org_unit_name, source.org_unit_type,
        'ACTIVE', '2024-01-01', source.hierarchy_level, source.path_code, source.sort_order
    );
GO

-- 2. Expand job title catalog for richer org / employee scenarios
MERGE dbo.hr_job_title AS target
USING (
    VALUES
        ('HRBP',        N'HR Business Partner',        'L3', N'Đối tác nhân sự cho khối kinh doanh và vận hành.', 'ACTIVE', 20),
        ('TA_SPEC',     N'Talent Acquisition Specialist','L4',N'Chuyên viên tuyển dụng.',                         'ACTIVE', 21),
        ('CNB_SPEC',    N'Compensation & Benefits Specialist','L4',N'Chuyên viên C&B.',                           'ACTIVE', 22),
        ('DEVOPS_ENG',  N'DevOps Engineer',            'L4', N'Kỹ sư vận hành nền tảng và CI/CD.',               'ACTIVE', 23),
        ('QA_ENG',      N'QA Engineer',                'L4', N'Kỹ sư kiểm thử chất lượng.',                      'ACTIVE', 24),
        ('TECH_LEAD',   N'Technical Lead',             'L3', N'Trưởng nhóm kỹ thuật.',                           'ACTIVE', 25),
        ('SALES_OPS',   N'Sales Operations Specialist','L4', N'Chuyên viên vận hành kinh doanh.',                'ACTIVE', 26),
        ('ACC_PAY',     N'Payroll Accountant',         'L4', N'Kế toán tiền lương.',                              'ACTIVE', 27),
        ('FIN_ANALYST', N'Financial Analyst',          'L4', N'Chuyên viên phân tích tài chính.',                'ACTIVE', 28),
        ('OFFICE_ADMIN',N'Office Administrator',       'L5', N'Nhân viên hành chính văn phòng.',                 'ACTIVE', 29)
) AS source (job_title_code, job_title_name, job_level_code, description, status, sort_order)
ON target.job_title_code = source.job_title_code
WHEN MATCHED THEN
    UPDATE SET
        target.job_title_name = source.job_title_name,
        target.job_level_code = source.job_level_code,
        target.description = source.description,
        target.status = source.status,
        target.sort_order = source.sort_order,
        target.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (job_title_code, job_title_name, job_level_code, description, status, sort_order, created_at, is_deleted)
    VALUES (source.job_title_code, source.job_title_name, source.job_level_code, source.description, source.status, source.sort_order, SYSDATETIME(), 0);
GO

-- 3. Seed 12 additional employees with stable manager scopes
;WITH DemoEmployees AS (
    SELECT *
    FROM (VALUES
        ('EMP112', 'TEAM_TA_HN',       'TA_SPEC',     'EMP007', N'Nguyễn Khánh Linh',  'FEMALE', CAST('1998-01-19' AS DATE), CAST('2024-09-02' AS DATE), N'Hanoi Office', 'linh.nguyen112@digitalhrm.com', '0907110112'),
        ('EMP113', 'TEAM_CNB_HN',      'CNB_SPEC',    'EMP007', N'Trần Quốc Bảo',      'MALE',   CAST('1996-04-08' AS DATE), CAST('2023-10-16' AS DATE), N'Hanoi Office', 'bao.tran113@digitalhrm.com', '0907110113'),
        ('EMP114', 'TEAM_PLATFORM_HN', 'DEVOPS_ENG',  'EMP006', N'Lê Gia Huy',         'MALE',   CAST('1997-07-23' AS DATE), CAST('2024-04-15' AS DATE), N'Hanoi Office', 'huy.le114@digitalhrm.com', '0907110114'),
        ('EMP115', 'TEAM_PLATFORM_HN', 'TECH_LEAD',   'EMP006', N'Phạm Thu Hà',        'FEMALE', CAST('1993-11-02' AS DATE), CAST('2022-08-01' AS DATE), N'Hanoi Office', 'ha.pham115@digitalhrm.com', '0907110115'),
        ('EMP116', 'TEAM_QA_HN',       'QA_ENG',      'EMP006', N'Đỗ Minh Tú',         'MALE',   CAST('1999-02-14' AS DATE), CAST('2025-01-13' AS DATE), N'Hanoi Office', 'tu.do116@digitalhrm.com', '0907110116'),
        ('EMP117', 'TEAM_QA_HN',       'QA_ENG',      'EMP006', N'Võ Ngọc Diễm',       'FEMALE', CAST('1998-09-28' AS DATE), CAST('2024-12-02' AS DATE), N'Hanoi Office', 'diem.vo117@digitalhrm.com', '0907110117'),
        ('EMP118', 'TEAM_SMB_HCM',     'SALES_OPS',   'EMP008', N'Bùi Hoàng Nam',      'MALE',   CAST('1997-03-18' AS DATE), CAST('2024-07-08' AS DATE), N'HCMC Office',  'nam.bui118@digitalhrm.com', '0907110118'),
        ('EMP119', 'TEAM_SMB_HCM',     'EXEC_SALES',  'EMP008', N'Ngô Phương Thảo',    'FEMALE', CAST('1999-08-05' AS DATE), CAST('2025-02-10' AS DATE), N'HCMC Office',  'thao.ngo119@digitalhrm.com', '0907110119'),
        ('EMP120', 'TEAM_ENT_HCM',     'EXEC_SALES',  'EMP008', N'Hoàng Đức Mạnh',     'MALE',   CAST('1995-12-12' AS DATE), CAST('2023-06-05' AS DATE), N'HCMC Office',  'manh.hoang120@digitalhrm.com', '0907110120'),
        ('EMP121', 'TEAM_AP_HCM',      'FIN_ANALYST', 'EMP004', N'Phan Thanh Trúc',    'FEMALE', CAST('1996-06-26' AS DATE), CAST('2024-05-20' AS DATE), N'HCMC Office',  'truc.phan121@digitalhrm.com', '0907110121'),
        ('EMP122', 'TEAM_PAYROLL_HCM', 'ACC_PAY',     'EMP004', N'Đặng Nhật Quang',    'MALE',   CAST('1994-10-30' AS DATE), CAST('2023-09-11' AS DATE), N'HCMC Office',  'quang.dang122@digitalhrm.com', '0907110122'),
        ('EMP123', 'TEAM_CNB_HN',      'HRBP',        'EMP007', N'Vũ Thanh Mai',       'FEMALE', CAST('1995-05-16' AS DATE), CAST('2024-01-08' AS DATE), N'Hanoi Office', 'mai.vu123@digitalhrm.com', '0907110123')
    ) AS s(employee_code, org_unit_code, job_title_code, manager_code, full_name, gender_code, date_of_birth, hire_date, work_location, work_email, mobile_phone)
),
Resolved AS (
    SELECT
        s.employee_code,
        o.org_unit_id,
        j.job_title_id,
        m.employee_id AS manager_employee_id,
        s.full_name,
        s.work_email,
        s.mobile_phone,
        s.gender_code,
        s.date_of_birth,
        s.hire_date,
        s.work_location
    FROM DemoEmployees s
    INNER JOIN dbo.hr_org_unit o ON o.org_unit_code = s.org_unit_code
    INNER JOIN dbo.hr_job_title j ON j.job_title_code = s.job_title_code
    LEFT JOIN dbo.hr_employee m ON m.employee_code = s.manager_code
)
MERGE dbo.hr_employee AS target
USING Resolved AS source
ON target.employee_code = source.employee_code
WHEN MATCHED THEN
    UPDATE SET
        target.org_unit_id = source.org_unit_id,
        target.job_title_id = source.job_title_id,
        target.manager_employee_id = source.manager_employee_id,
        target.full_name = source.full_name,
        target.work_email = source.work_email,
        target.work_phone = source.mobile_phone,
        target.mobile_phone = source.mobile_phone,
        target.gender_code = source.gender_code,
        target.date_of_birth = source.date_of_birth,
        target.hire_date = source.hire_date,
        target.employment_status = 'ACTIVE',
        target.work_location = source.work_location,
        target.personal_email = source.work_email,
        target.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (
        employee_code, org_unit_id, job_title_id, manager_employee_id,
        full_name, work_email, work_phone, gender_code, date_of_birth, hire_date,
        employment_status, work_location, personal_email, mobile_phone, note
    )
    VALUES (
        source.employee_code, source.org_unit_id, source.job_title_id, source.manager_employee_id,
        source.full_name, source.work_email, source.mobile_phone, source.gender_code, source.date_of_birth, source.hire_date,
        'ACTIVE', source.work_location, source.work_email, source.mobile_phone, N'Feature-rich seed dataset for CI/CD.'
    );
GO

-- 4. Employee profile details
;WITH DemoProfiles AS (
    SELECT *
    FROM (VALUES
        ('EMP112', N'Linh',  N'Nguyễn Khánh', 'SINGLE',  N'Human Resource Management'),
        ('EMP113', N'Bảo',   N'Trần Quốc',    'MARRIED', N'Accounting'),
        ('EMP114', N'Huy',   N'Lê Gia',       'SINGLE',  N'Information Systems'),
        ('EMP115', N'Hà',    N'Phạm Thu',     'MARRIED', N'Software Engineering'),
        ('EMP116', N'Tú',    N'Đỗ Minh',      'SINGLE',  N'Computer Science'),
        ('EMP117', N'Diễm',  N'Võ Ngọc',      'SINGLE',  N'Information Technology'),
        ('EMP118', N'Nam',   N'Bùi Hoàng',    'MARRIED', N'Business Administration'),
        ('EMP119', N'Thảo',  N'Ngô Phương',   'SINGLE',  N'International Business'),
        ('EMP120', N'Mạnh',  N'Hoàng Đức',    'MARRIED', N'Marketing'),
        ('EMP121', N'Trúc',  N'Phan Thanh',   'SINGLE',  N'Finance'),
        ('EMP122', N'Quang', N'Đặng Nhật',    'MARRIED', N'Accounting'),
        ('EMP123', N'Mai',   N'Vũ Thanh',     'MARRIED', N'Human Resources')
    ) AS s(employee_code, first_name, last_name, marital_status, major)
)
INSERT INTO dbo.hr_employee_profile (
    employee_id, first_name, last_name, marital_status, nationality, ethnic_group, education_level, major
)
SELECT e.employee_id, s.first_name, s.last_name, s.marital_status, N'Vietnam', N'Kinh', N'Bachelor', s.major
FROM DemoProfiles s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.hr_employee_profile p WHERE p.employee_id = e.employee_id AND p.is_deleted = 0
);
GO

;WITH DemoAddress AS (
    SELECT *
    FROM (VALUES
        ('EMP112', N'18 Trần Quốc Hoàn',        N'Hà Nội'),
        ('EMP113', N'105 Hoàng Quốc Việt',      N'Hà Nội'),
        ('EMP114', N'42 Duy Tân',               N'Hà Nội'),
        ('EMP115', N'68 Tôn Thất Thuyết',       N'Hà Nội'),
        ('EMP116', N'11 Phạm Văn Đồng',         N'Hà Nội'),
        ('EMP117', N'27 Xuân La',               N'Hà Nội'),
        ('EMP118', N'212 Điện Biên Phủ',        N'Hồ Chí Minh'),
        ('EMP119', N'74 Nguyễn Văn Trỗi',       N'Hồ Chí Minh'),
        ('EMP120', N'152 Võ Văn Kiệt',          N'Hồ Chí Minh'),
        ('EMP121', N'40 Nguyễn Thị Thập',       N'Hồ Chí Minh'),
        ('EMP122', N'93 Phổ Quang',             N'Hồ Chí Minh'),
        ('EMP123', N'56 Phạm Hùng',             N'Hà Nội')
    ) AS s(employee_code, address_line, province_name)
)
INSERT INTO dbo.hr_employee_address (
    employee_id, address_type, address_line, province_name, country_name, is_primary
)
SELECT e.employee_id, 'CURRENT', s.address_line, s.province_name, N'Vietnam', 1
FROM DemoAddress s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.hr_employee_address a
    WHERE a.employee_id = e.employee_id
      AND a.address_type = 'CURRENT'
      AND a.is_deleted = 0
);
GO

;WITH DemoIdentification AS (
    SELECT *
    FROM (VALUES
        ('EMP112', '079098011112'), ('EMP113', '079096011113'), ('EMP114', '079097011114'),
        ('EMP115', '079093011115'), ('EMP116', '079099011116'), ('EMP117', '079098011117'),
        ('EMP118', '079097011118'), ('EMP119', '079099011119'), ('EMP120', '079095011120'),
        ('EMP121', '079096011121'), ('EMP122', '079094011122'), ('EMP123', '079095011123')
    ) AS s(employee_code, document_number)
)
INSERT INTO dbo.hr_employee_identification (
    employee_id, document_type, document_number, status, is_primary
)
SELECT e.employee_id, 'CCCD', s.document_number, 'VALID', 1
FROM DemoIdentification s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.hr_employee_identification i
    WHERE i.employee_id = e.employee_id
      AND i.document_type = 'CCCD'
      AND i.is_deleted = 0
);
GO

;WITH DemoBank AS (
    SELECT *
    FROM (VALUES
        ('EMP112', N'Techcombank', '970111011112'),
        ('EMP113', N'Vietcombank', '970111011113'),
        ('EMP114', N'MB Bank',     '970111011114'),
        ('EMP115', N'ACB',         '970111011115'),
        ('EMP116', N'Techcombank', '970111011116'),
        ('EMP117', N'BIDV',        '970111011117'),
        ('EMP118', N'MB Bank',     '970111011118'),
        ('EMP119', N'Techcombank', '970111011119'),
        ('EMP120', N'ACB',         '970111011120'),
        ('EMP121', N'VietinBank',  '970111011121'),
        ('EMP122', N'BIDV',        '970111011122'),
        ('EMP123', N'Vietcombank', '970111011123')
    ) AS s(employee_code, bank_name, account_number)
)
INSERT INTO dbo.hr_employee_bank_account (
    employee_id, bank_name, account_number, account_holder_name, status, is_primary
)
SELECT e.employee_id, s.bank_name, s.account_number, UPPER(e.full_name), 'ACTIVE', 1
FROM DemoBank s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.hr_employee_bank_account b WHERE b.employee_id = e.employee_id AND b.is_deleted = 0
);
GO

-- 5. User accounts and employee roles
DECLARE @DemoPasswordHash111 VARCHAR(255) = '$2a$10$gaSEymcGFJ74WopEYuob2OLUqXy3IZuTdmv5vDDWVYzVcp7DTic32';

;WITH DemoUsers AS (
    SELECT *
    FROM (VALUES
        ('EMP112', 'demo_emp112', 'linh.nguyen112@digitalhrm.com'),
        ('EMP113', 'demo_emp113', 'bao.tran113@digitalhrm.com'),
        ('EMP114', 'demo_emp114', 'huy.le114@digitalhrm.com'),
        ('EMP115', 'demo_emp115', 'ha.pham115@digitalhrm.com'),
        ('EMP116', 'demo_emp116', 'tu.do116@digitalhrm.com'),
        ('EMP117', 'demo_emp117', 'diem.vo117@digitalhrm.com'),
        ('EMP118', 'demo_emp118', 'nam.bui118@digitalhrm.com'),
        ('EMP119', 'demo_emp119', 'thao.ngo119@digitalhrm.com'),
        ('EMP120', 'demo_emp120', 'manh.hoang120@digitalhrm.com'),
        ('EMP121', 'demo_emp121', 'truc.phan121@digitalhrm.com'),
        ('EMP122', 'demo_emp122', 'quang.dang122@digitalhrm.com'),
        ('EMP123', 'demo_emp123', 'mai.vu123@digitalhrm.com')
    ) AS s(employee_code, username, email)
),
Resolved AS (
    SELECT e.employee_id, s.username, s.email
    FROM DemoUsers s
    INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
)
MERGE dbo.sec_user_account AS target
USING Resolved AS source
ON target.employee_id = source.employee_id
WHEN MATCHED THEN
    UPDATE SET
        target.username = source.username,
        target.email = source.email,
        target.password_hash = @DemoPasswordHash111,
        target.status = 'ACTIVE',
        target.must_change_password = 0,
        target.is_deleted = 0,
        target.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (user_id, employee_id, username, password_hash, status, must_change_password, email, created_at, is_deleted)
    VALUES (NEWID(), source.employee_id, source.username, @DemoPasswordHash111, 'ACTIVE', 0, source.email, SYSDATETIME(), 0);
GO

DECLARE @EmployeeRoleId111 UNIQUEIDENTIFIER = (SELECT role_id FROM dbo.sec_role WHERE role_code = 'EMPLOYEE');

INSERT INTO dbo.sec_user_role (user_id, role_id, is_primary_role, status, created_at)
SELECT u.user_id, @EmployeeRoleId111, 1, 'ACTIVE', SYSDATETIME()
FROM dbo.sec_user_account u
WHERE u.username IN (
    'demo_emp112','demo_emp113','demo_emp114','demo_emp115','demo_emp116','demo_emp117',
    'demo_emp118','demo_emp119','demo_emp120','demo_emp121','demo_emp122','demo_emp123'
)
  AND @EmployeeRoleId111 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM dbo.sec_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = @EmployeeRoleId111
  );
GO

-- 6. Contracts, compensation and tax profiles
DECLARE @TypeINDEF111 BIGINT = (SELECT contract_type_id FROM dbo.ct_contract_type WHERE contract_type_code = 'INDEF');
DECLARE @Type12M111 BIGINT = (SELECT contract_type_id FROM dbo.ct_contract_type WHERE contract_type_code = '12M');
DECLARE @AllowanceId111 BIGINT = (SELECT salary_component_id FROM dbo.pay_salary_component WHERE component_code = 'ALLOWANCE');

;WITH DemoContract AS (
    SELECT *
    FROM (VALUES
        ('EMP112', 'HDLD-EMP112-01', '12M',   CAST('2024-08-28' AS DATE), CAST('2024-09-02' AS DATE), CAST('2026-08-31' AS DATE), 18500000),
        ('EMP113', 'HDLD-EMP113-01', 'INDEF', CAST('2023-10-10' AS DATE), CAST('2023-10-16' AS DATE), CAST(NULL AS DATE),         21000000),
        ('EMP114', 'HDLD-EMP114-01', 'INDEF', CAST('2024-04-10' AS DATE), CAST('2024-04-15' AS DATE), CAST(NULL AS DATE),         30000000),
        ('EMP115', 'HDLD-EMP115-01', 'INDEF', CAST('2022-07-25' AS DATE), CAST('2022-08-01' AS DATE), CAST(NULL AS DATE),         38000000),
        ('EMP116', 'HDLD-EMP116-01', '12M',   CAST('2025-01-09' AS DATE), CAST('2025-01-13' AS DATE), CAST('2026-12-31' AS DATE), 17500000),
        ('EMP117', 'HDLD-EMP117-01', '12M',   CAST('2024-11-27' AS DATE), CAST('2024-12-02' AS DATE), CAST('2026-11-30' AS DATE), 17800000),
        ('EMP118', 'HDLD-EMP118-01', 'INDEF', CAST('2024-07-01' AS DATE), CAST('2024-07-08' AS DATE), CAST(NULL AS DATE),         20500000),
        ('EMP119', 'HDLD-EMP119-01', '12M',   CAST('2025-02-05' AS DATE), CAST('2025-02-10' AS DATE), CAST('2026-12-31' AS DATE), 18000000),
        ('EMP120', 'HDLD-EMP120-01', 'INDEF', CAST('2023-05-29' AS DATE), CAST('2023-06-05' AS DATE), CAST(NULL AS DATE),         23500000),
        ('EMP121', 'HDLD-EMP121-01', 'INDEF', CAST('2024-05-15' AS DATE), CAST('2024-05-20' AS DATE), CAST(NULL AS DATE),         22500000),
        ('EMP122', 'HDLD-EMP122-01', 'INDEF', CAST('2023-09-05' AS DATE), CAST('2023-09-11' AS DATE), CAST(NULL AS DATE),         24000000),
        ('EMP123', 'HDLD-EMP123-01', 'INDEF', CAST('2024-01-03' AS DATE), CAST('2024-01-08' AS DATE), CAST(NULL AS DATE),         24500000)
    ) AS s(employee_code, contract_number, contract_type_code, sign_date, effective_date, end_date, base_salary)
)
INSERT INTO dbo.ct_labor_contract (
    employee_id, contract_type_id, contract_number, sign_date, effective_date, end_date,
    job_title_id, org_unit_id, base_salary, salary_currency, working_type, contract_status
)
SELECT
    e.employee_id,
    CASE WHEN s.contract_type_code = '12M' THEN @Type12M111 ELSE @TypeINDEF111 END,
    s.contract_number,
    s.sign_date,
    s.effective_date,
    s.end_date,
    e.job_title_id,
    e.org_unit_id,
    s.base_salary,
    'VND',
    'FULL_TIME',
    'ACTIVE'
FROM DemoContract s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.ct_labor_contract c WHERE c.contract_number = s.contract_number
);
GO

;WITH DemoComp AS (
    SELECT *
    FROM (VALUES
        ('EMP112', 18500000), ('EMP113', 21000000), ('EMP114', 30000000), ('EMP115', 38000000),
        ('EMP116', 17500000), ('EMP117', 17800000), ('EMP118', 20500000), ('EMP119', 18000000),
        ('EMP120', 23500000), ('EMP121', 22500000), ('EMP122', 24000000), ('EMP123', 24500000)
    ) AS s(employee_code, base_salary)
)
INSERT INTO dbo.pay_employee_compensation (
    employee_id, effective_from, base_salary, insurance_salary_base, bank_account_name, bank_account_no, bank_name
)
SELECT
    e.employee_id,
    CAST('2026-01-01' AS DATE),
    s.base_salary,
    s.base_salary,
    UPPER(e.full_name),
    'BC' + RIGHT(e.employee_code, 3) + '111',
    N'Techcombank'
FROM DemoComp s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.pay_employee_compensation c
    WHERE c.employee_id = e.employee_id AND c.status = 'ACTIVE' AND c.is_deleted = 0
);
GO

DECLARE @AllowanceId111 BIGINT = (SELECT salary_component_id FROM dbo.pay_salary_component WHERE component_code = 'ALLOWANCE');

INSERT INTO dbo.pay_employee_compensation_item (employee_compensation_id, salary_component_id, amount_value)
SELECT c.employee_compensation_id, @AllowanceId111, 900000
FROM dbo.pay_employee_compensation c
INNER JOIN dbo.hr_employee e ON e.employee_id = c.employee_id
WHERE e.employee_code IN ('EMP112','EMP113','EMP114','EMP115','EMP116','EMP117','EMP118','EMP119','EMP120','EMP121','EMP122','EMP123')
  AND @AllowanceId111 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.pay_employee_compensation_item ci
      WHERE ci.employee_compensation_id = c.employee_compensation_id
        AND ci.salary_component_id = @AllowanceId111
  );
GO

;WITH DemoTaxProfile AS (
    SELECT *
    FROM (VALUES
        ('EMP112', 'MONTHLY_WITHHOLDING'), ('EMP113', 'ANNUAL_AUTHORIZED'), ('EMP114', 'MONTHLY_WITHHOLDING'),
        ('EMP115', 'ANNUAL_AUTHORIZED'),   ('EMP116', 'MONTHLY_WITHHOLDING'), ('EMP117', 'SELF_FINALIZATION'),
        ('EMP118', 'MONTHLY_WITHHOLDING'), ('EMP119', 'MONTHLY_WITHHOLDING'), ('EMP120', 'ANNUAL_AUTHORIZED'),
        ('EMP121', 'MONTHLY_WITHHOLDING'), ('EMP122', 'ANNUAL_AUTHORIZED'),   ('EMP123', 'ANNUAL_AUTHORIZED')
    ) AS s(employee_code, tax_finalization_method)
)
INSERT INTO dbo.pay_personal_tax_profile (
    employee_id, resident_taxpayer, family_deduction_enabled, tax_registration_date, tax_finalization_method, status, note
)
SELECT e.employee_id, 1, 1, CAST('2026-01-01' AS DATE), s.tax_finalization_method, 'ACTIVE', N'Hồ sơ PIT seed cho CI/CD.'
FROM DemoTaxProfile s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.pay_personal_tax_profile p WHERE p.employee_id = e.employee_id AND p.is_deleted = 0
);
GO

;WITH DemoDependents AS (
    SELECT *
    FROM (VALUES
        ('EMP113', N'Trần Minh Châu',  'CHILD',  'DEP11301', CAST('2019-05-08' AS DATE), CAST('2026-01-01' AS DATE)),
        ('EMP115', N'Nguyễn Thị Lan',  'MOTHER', 'DEP11501', CAST('1967-02-21' AS DATE), CAST('2026-01-01' AS DATE)),
        ('EMP118', N'Bùi Phương Nhi',  'CHILD',  'DEP11801', CAST('2021-09-14' AS DATE), CAST('2026-01-01' AS DATE)),
        ('EMP120', N'Hoàng Kim Anh',   'SPOUSE', 'DEP12001', CAST('1996-08-19' AS DATE), CAST('2026-01-01' AS DATE)),
        ('EMP122', N'Đặng Đức Tài',    'FATHER', 'DEP12201', CAST('1963-12-03' AS DATE), CAST('2026-01-01' AS DATE)),
        ('EMP123', N'Vũ Minh Trang',   'CHILD',  'DEP12301', CAST('2020-11-25' AS DATE), CAST('2026-01-01' AS DATE))
    ) AS s(employee_code, full_name, relationship_code, identification_no, date_of_birth, deduction_start_month)
)
INSERT INTO dbo.pay_tax_dependent (
    employee_id, full_name, relationship_code, identification_no, date_of_birth, deduction_start_month, status, note
)
SELECT e.employee_id, s.full_name, s.relationship_code, s.identification_no, s.date_of_birth, s.deduction_start_month, 'ACTIVE', N'Người phụ thuộc demo.'
FROM DemoDependents s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.pay_tax_dependent d WHERE d.employee_id = e.employee_id AND d.identification_no = s.identification_no
);
GO

-- 7. Leave balances and requests
DECLARE @ALTypeId111 BIGINT = (SELECT leave_type_id FROM dbo.lea_leave_type WHERE leave_type_code = 'AL');
DECLARE @ALRuleId111 BIGINT = (SELECT leave_type_rule_id FROM dbo.lea_leave_type_rule WHERE leave_type_id = @ALTypeId111 AND version_no = 1);
DECLARE @HrUser111 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

;WITH DemoLeaveBalance AS (
    SELECT *
    FROM (VALUES
        ('EMP112', 10.5, 1.5), ('EMP113', 9.0, 3.0),  ('EMP114', 11.0, 1.0), ('EMP115', 8.0, 4.0),
        ('EMP116', 12.0, 0.0), ('EMP117', 10.0, 2.0), ('EMP118', 9.5, 2.5),  ('EMP119', 11.0, 1.0),
        ('EMP120', 8.5, 3.5),  ('EMP121', 10.0, 2.0), ('EMP122', 9.0, 3.0),  ('EMP123', 11.5, 0.5)
    ) AS s(employee_code, available_units, used_units)
)
INSERT INTO dbo.lea_leave_balance (
    employee_id, leave_type_id, leave_year, opening_units, accrued_units, used_units, available_units, balance_status
)
SELECT e.employee_id, @ALTypeId111, 2026, 0.0, 12.0, s.used_units, s.available_units, 'OPEN'
FROM DemoLeaveBalance s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE @ALTypeId111 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.lea_leave_balance b
      WHERE b.employee_id = e.employee_id
        AND b.leave_type_id = @ALTypeId111
        AND b.leave_year = 2026
  );
GO

DECLARE @ALTypeId111 BIGINT = (SELECT leave_type_id FROM dbo.lea_leave_type WHERE leave_type_code = 'AL');
DECLARE @ALRuleId111 BIGINT = (SELECT leave_type_rule_id FROM dbo.lea_leave_type_rule WHERE leave_type_id = @ALTypeId111 AND version_no = 1);
DECLARE @HrUser111 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

;WITH DemoLeaveRequest AS (
    SELECT *
    FROM (VALUES
        ('REQ-2026-PLUS-112', 'EMP112', CAST('2026-05-18' AS DATE), CAST('2026-05-18' AS DATE), 1.0, 'SUBMITTED', N'Nghỉ tham dự workshop tuyển dụng.'),
        ('REQ-2026-PLUS-113', 'EMP113', CAST('2026-05-20' AS DATE), CAST('2026-05-20' AS DATE), 1.0, 'APPROVED',  N'Nghỉ giải quyết việc gia đình.'),
        ('REQ-2026-PLUS-114', 'EMP114', CAST('2026-05-22' AS DATE), CAST('2026-05-23' AS DATE), 2.0, 'FINALIZED', N'Nghỉ sau đợt release hạ tầng.'),
        ('REQ-2026-PLUS-115', 'EMP115', CAST('2026-05-26' AS DATE), CAST('2026-05-26' AS DATE), 1.0, 'APPROVED',  N'Nghỉ khám sức khỏe định kỳ.'),
        ('REQ-2026-PLUS-116', 'EMP116', CAST('2026-05-27' AS DATE), CAST('2026-05-27' AS DATE), 1.0, 'REJECTED',  N'Trùng thời gian UAT.'),
        ('REQ-2026-PLUS-117', 'EMP117', CAST('2026-05-28' AS DATE), CAST('2026-05-28' AS DATE), 1.0, 'SUBMITTED', N'Nghỉ việc cá nhân.'),
        ('REQ-2026-PLUS-118', 'EMP118', CAST('2026-05-19' AS DATE), CAST('2026-05-19' AS DATE), 1.0, 'APPROVED',  N'Nghỉ đi gặp đối tác ở tỉnh.'),
        ('REQ-2026-PLUS-119', 'EMP119', CAST('2026-05-21' AS DATE), CAST('2026-05-22' AS DATE), 2.0, 'SUBMITTED', N'Nghỉ phép đã lên kế hoạch.'),
        ('REQ-2026-PLUS-120', 'EMP120', CAST('2026-05-29' AS DATE), CAST('2026-05-29' AS DATE), 1.0, 'FINALIZED', N'Nghỉ xử lý công việc cá nhân.'),
        ('REQ-2026-PLUS-121', 'EMP121', CAST('2026-05-30' AS DATE), CAST('2026-05-30' AS DATE), 1.0, 'APPROVED',  N'Nghỉ theo lịch gia đình.'),
        ('REQ-2026-PLUS-122', 'EMP122', CAST('2026-06-02' AS DATE), CAST('2026-06-03' AS DATE), 2.0, 'REJECTED',  N'Trùng kỳ chốt lương.'),
        ('REQ-2026-PLUS-123', 'EMP123', CAST('2026-06-04' AS DATE), CAST('2026-06-04' AS DATE), 1.0, 'SUBMITTED', N'Nghỉ tham dự lớp đào tạo HRBP.')
    ) AS s(request_code, employee_code, start_date, end_date, requested_units, request_status, reason)
)
INSERT INTO dbo.lea_leave_request (
    request_code, employee_id, leave_type_id, leave_type_rule_id, leave_year,
    start_date, end_date, requested_units, reason, approval_role_code, request_status,
    submitted_at, approved_at, approved_by, rejected_at, rejected_by, finalized_at, finalized_by,
    approval_note, rejection_note, finalize_note
)
SELECT
    s.request_code,
    e.employee_id,
    @ALTypeId111,
    @ALRuleId111,
    2026,
    s.start_date,
    s.end_date,
    s.requested_units,
    s.reason,
    'MANAGER',
    s.request_status,
    DATEADD(DAY, -2, SYSDATETIME()),
    CASE WHEN s.request_status = 'APPROVED' THEN DATEADD(DAY, -1, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'APPROVED' THEN mu.user_id END,
    CASE WHEN s.request_status = 'REJECTED' THEN DATEADD(DAY, -1, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'REJECTED' THEN mu.user_id END,
    CASE WHEN s.request_status = 'FINALIZED' THEN DATEADD(HOUR, -10, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'FINALIZED' THEN @HrUser111 END,
    CASE WHEN s.request_status = 'APPROVED' THEN N'Quản lý đã duyệt kế hoạch nghỉ.' END,
    CASE WHEN s.request_status = 'REJECTED' THEN N'Bộ phận cần duy trì tối thiểu nhân sự vận hành.' END,
    CASE WHEN s.request_status = 'FINALIZED' THEN N'HR đã hoàn tất bút toán phép năm.' END
FROM DemoLeaveRequest s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
LEFT JOIN dbo.sec_user_account mu ON mu.employee_id = e.manager_employee_id
WHERE @ALTypeId111 IS NOT NULL
  AND @ALRuleId111 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM dbo.lea_leave_request r WHERE r.request_code = s.request_code
  );
GO

-- 8. Attendance, adjustments and overtime
DECLARE @OfficeShift111 BIGINT = (SELECT shift_id FROM dbo.att_shift WHERE shift_code = 'OFFICE');
DECLARE @AttPeriodMay111 BIGINT = (SELECT attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-05');

INSERT INTO dbo.att_shift_assignment (employee_id, shift_id, effective_from)
SELECT e.employee_id, @OfficeShift111, '2026-01-01'
FROM dbo.hr_employee e
WHERE e.employee_code IN ('EMP112','EMP113','EMP114','EMP115','EMP116','EMP117','EMP118','EMP119','EMP120','EMP121','EMP122','EMP123')
  AND @OfficeShift111 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.att_shift_assignment a
      WHERE a.employee_id = e.employee_id
        AND a.shift_id = @OfficeShift111
        AND a.effective_from = '2026-01-01'
  );
GO

DECLARE @AttPeriodMay111 BIGINT = (SELECT attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-05');

;WITH DemoDaily AS (
    SELECT *
    FROM (VALUES
        ('EMP112', CAST('2026-05-12' AS DATE), 'PRESENT',    CAST('2026-05-12 08:02:00' AS DATETIME2), CAST('2026-05-12 17:20:00' AS DATETIME2), 498,  0, 0, 0),
        ('EMP113', CAST('2026-05-12' AS DATE), 'PRESENT',    CAST('2026-05-12 08:11:00' AS DATETIME2), CAST('2026-05-12 17:26:00' AS DATETIME2), 495, 11, 1, 0),
        ('EMP114', CAST('2026-05-12' AS DATE), 'PRESENT',    CAST('2026-05-12 07:56:00' AS DATETIME2), CAST('2026-05-12 18:08:00' AS DATETIME2), 552,  0, 0, 0),
        ('EMP115', CAST('2026-05-12' AS DATE), 'PRESENT',    CAST('2026-05-12 08:06:00' AS DATETIME2), CAST('2026-05-12 17:48:00' AS DATETIME2), 522,  0, 0, 0),
        ('EMP116', CAST('2026-05-13' AS DATE), 'INCOMPLETE', CAST('2026-05-13 08:07:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    0,    0, 1, 0),
        ('EMP117', CAST('2026-05-13' AS DATE), 'PRESENT',    CAST('2026-05-13 08:00:00' AS DATETIME2), CAST('2026-05-13 17:22:00' AS DATETIME2), 502,  0, 0, 0),
        ('EMP118', CAST('2026-05-13' AS DATE), 'PRESENT',    CAST('2026-05-13 08:15:00' AS DATETIME2), CAST('2026-05-13 17:35:00' AS DATETIME2), 500, 15, 1, 0),
        ('EMP119', CAST('2026-05-13' AS DATE), 'ON_LEAVE',   CAST(NULL AS DATETIME2),                    CAST(NULL AS DATETIME2),                    0,    0, 0, 1),
        ('EMP120', CAST('2026-05-14' AS DATE), 'PRESENT',    CAST('2026-05-14 08:03:00' AS DATETIME2), CAST('2026-05-14 18:02:00' AS DATETIME2), 539,  0, 0, 0),
        ('EMP121', CAST('2026-05-14' AS DATE), 'ABSENT',     CAST(NULL AS DATETIME2),                    CAST(NULL AS DATETIME2),                    0,    0, 1, 0),
        ('EMP122', CAST('2026-05-14' AS DATE), 'PRESENT',    CAST('2026-05-14 08:04:00' AS DATETIME2), CAST('2026-05-14 17:41:00' AS DATETIME2), 517,  0, 0, 0),
        ('EMP123', CAST('2026-05-14' AS DATE), 'PRESENT',    CAST('2026-05-14 07:58:00' AS DATETIME2), CAST('2026-05-14 17:18:00' AS DATETIME2), 500,  0, 0, 0)
    ) AS s(employee_code, attendance_date, daily_status, check_in_at, check_out_at, worked_minutes, late_minutes, anomaly_count, on_leave)
)
INSERT INTO dbo.att_daily_attendance (
    employee_id, attendance_date, attendance_period_id, actual_check_in_at, actual_check_out_at,
    worked_minutes, late_minutes, anomaly_count, missing_check_out, on_leave, daily_status
)
SELECT
    e.employee_id,
    s.attendance_date,
    @AttPeriodMay111,
    s.check_in_at,
    s.check_out_at,
    s.worked_minutes,
    s.late_minutes,
    s.anomaly_count,
    CASE WHEN s.daily_status = 'INCOMPLETE' THEN 1 ELSE 0 END,
    s.on_leave,
    s.daily_status
FROM DemoDaily s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE @AttPeriodMay111 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.att_daily_attendance d
      WHERE d.employee_id = e.employee_id
        AND d.attendance_date = s.attendance_date
  );
GO

;WITH DemoLogs AS (
    SELECT *
    FROM (VALUES
        ('EMP112', CAST('2026-05-12' AS DATE), 'CHECK_IN',  CAST('2026-05-12 08:02:00' AS DATETIME2), 'WEB'),
        ('EMP112', CAST('2026-05-12' AS DATE), 'CHECK_OUT', CAST('2026-05-12 17:20:00' AS DATETIME2), 'WEB'),
        ('EMP113', CAST('2026-05-12' AS DATE), 'CHECK_IN',  CAST('2026-05-12 08:11:00' AS DATETIME2), 'MOBILE_APP'),
        ('EMP113', CAST('2026-05-12' AS DATE), 'CHECK_OUT', CAST('2026-05-12 17:26:00' AS DATETIME2), 'MOBILE_APP'),
        ('EMP114', CAST('2026-05-12' AS DATE), 'CHECK_IN',  CAST('2026-05-12 07:56:00' AS DATETIME2), 'WEB'),
        ('EMP114', CAST('2026-05-12' AS DATE), 'CHECK_OUT', CAST('2026-05-12 18:08:00' AS DATETIME2), 'WEB'),
        ('EMP115', CAST('2026-05-12' AS DATE), 'CHECK_IN',  CAST('2026-05-12 08:06:00' AS DATETIME2), 'WEB'),
        ('EMP115', CAST('2026-05-12' AS DATE), 'CHECK_OUT', CAST('2026-05-12 17:48:00' AS DATETIME2), 'WEB'),
        ('EMP116', CAST('2026-05-13' AS DATE), 'CHECK_IN',  CAST('2026-05-13 08:07:00' AS DATETIME2), 'WEB'),
        ('EMP117', CAST('2026-05-13' AS DATE), 'CHECK_IN',  CAST('2026-05-13 08:00:00' AS DATETIME2), 'WEB'),
        ('EMP117', CAST('2026-05-13' AS DATE), 'CHECK_OUT', CAST('2026-05-13 17:22:00' AS DATETIME2), 'WEB'),
        ('EMP118', CAST('2026-05-13' AS DATE), 'CHECK_IN',  CAST('2026-05-13 08:15:00' AS DATETIME2), 'MOBILE_APP'),
        ('EMP118', CAST('2026-05-13' AS DATE), 'CHECK_OUT', CAST('2026-05-13 17:35:00' AS DATETIME2), 'MOBILE_APP'),
        ('EMP120', CAST('2026-05-14' AS DATE), 'CHECK_IN',  CAST('2026-05-14 08:03:00' AS DATETIME2), 'WEB'),
        ('EMP120', CAST('2026-05-14' AS DATE), 'CHECK_OUT', CAST('2026-05-14 18:02:00' AS DATETIME2), 'WEB'),
        ('EMP122', CAST('2026-05-14' AS DATE), 'CHECK_IN',  CAST('2026-05-14 08:04:00' AS DATETIME2), 'WEB'),
        ('EMP122', CAST('2026-05-14' AS DATE), 'CHECK_OUT', CAST('2026-05-14 17:41:00' AS DATETIME2), 'WEB'),
        ('EMP123', CAST('2026-05-14' AS DATE), 'CHECK_IN',  CAST('2026-05-14 07:58:00' AS DATETIME2), 'WEB'),
        ('EMP123', CAST('2026-05-14' AS DATE), 'CHECK_OUT', CAST('2026-05-14 17:18:00' AS DATETIME2), 'WEB')
    ) AS s(employee_code, attendance_date, event_type, event_time, source_type)
)
INSERT INTO dbo.att_attendance_log (employee_id, attendance_date, event_type, event_time, source_type)
SELECT e.employee_id, s.attendance_date, s.event_type, s.event_time, s.source_type
FROM DemoLogs s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.att_attendance_log l
    WHERE l.employee_id = e.employee_id
      AND l.attendance_date = s.attendance_date
      AND l.event_type = s.event_type
      AND l.event_time = s.event_time
);
GO

;WITH DemoAdjustments AS (
    SELECT *
    FROM (VALUES
        ('ADJ-PLUS-112', 'EMP112', CAST('2026-05-12' AS DATE), 'WRONG_TIME',        CAST('2026-05-12 08:00:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'SUBMITTED', N'Kiosk ghi chậm 2 phút.'),
        ('ADJ-PLUS-113', 'EMP113', CAST('2026-05-12' AS DATE), 'MISSING_CHECK_OUT', CAST(NULL AS DATETIME2),                    CAST('2026-05-12 17:29:00' AS DATETIME2), 'APPROVED',  N'Quên thao tác checkout sau họp.'),
        ('ADJ-PLUS-114', 'EMP114', CAST('2026-05-12' AS DATE), 'MISSING_CHECK_IN',  CAST('2026-05-12 07:55:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'APPROVED',  N'Máy chấm công không ghi nhận check-in.'),
        ('ADJ-PLUS-115', 'EMP115', CAST('2026-05-12' AS DATE), 'WRONG_TIME',        CAST('2026-05-12 08:05:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'SUBMITTED', N'Cần đối soát với log VPN.'),
        ('ADJ-PLUS-116', 'EMP116', CAST('2026-05-13' AS DATE), 'MISSING_CHECK_OUT', CAST(NULL AS DATETIME2),                    CAST('2026-05-13 17:05:00' AS DATETIME2), 'SUBMITTED', N'Quên checkout trong ngày test UAT.'),
        ('ADJ-PLUS-117', 'EMP117', CAST('2026-05-13' AS DATE), 'OTHER',             CAST('2026-05-13 08:00:00' AS DATETIME2), CAST('2026-05-13 17:20:00' AS DATETIME2), 'REJECTED',  N'Lý do cần bổ sung minh chứng rõ hơn.'),
        ('ADJ-PLUS-118', 'EMP118', CAST('2026-05-13' AS DATE), 'WRONG_TIME',        CAST('2026-05-13 08:07:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'APPROVED',  N'Dữ liệu mobile app bị lệch múi giờ.'),
        ('ADJ-PLUS-119', 'EMP119', CAST('2026-05-13' AS DATE), 'OTHER',             CAST('2026-05-13 08:00:00' AS DATETIME2), CAST('2026-05-13 17:00:00' AS DATETIME2), 'REJECTED',  N'Ngày nghỉ phép không cần điều chỉnh công.'),
        ('ADJ-PLUS-120', 'EMP120', CAST('2026-05-14' AS DATE), 'MISSING_CHECK_OUT', CAST(NULL AS DATETIME2),                    CAST('2026-05-14 18:03:00' AS DATETIME2), 'APPROVED',  N'Checkout sau khi chốt hợp đồng khách hàng.'),
        ('ADJ-PLUS-121', 'EMP121', CAST('2026-05-14' AS DATE), 'MISSING_BOTH',      CAST('2026-05-14 08:04:00' AS DATETIME2), CAST('2026-05-14 17:01:00' AS DATETIME2), 'SUBMITTED', N'Cần bổ sung do đi làm việc ngân hàng.'),
        ('ADJ-PLUS-122', 'EMP122', CAST('2026-05-14' AS DATE), 'WRONG_TIME',        CAST('2026-05-14 08:00:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'APPROVED',  N'Máy ghi muộn 4 phút.'),
        ('ADJ-PLUS-123', 'EMP123', CAST('2026-05-14' AS DATE), 'MISSING_CHECK_IN',  CAST('2026-05-14 07:57:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'SUBMITTED', N'Đã vào văn phòng trước giờ nhưng kiosk lỗi.')
    ) AS s(request_code, employee_code, attendance_date, issue_type, proposed_check_in_at, proposed_check_out_at, request_status, reason)
)
INSERT INTO dbo.att_adjustment_request (
    request_code, employee_id, attendance_date, issue_type, proposed_check_in_at, proposed_check_out_at,
    reason, request_status, submitted_at, approved_at, approved_by, rejected_at, rejected_by, manager_note, rejection_note
)
SELECT
    s.request_code,
    e.employee_id,
    s.attendance_date,
    s.issue_type,
    s.proposed_check_in_at,
    s.proposed_check_out_at,
    s.reason,
    s.request_status,
    DATEADD(DAY, -1, SYSDATETIME()),
    CASE WHEN s.request_status = 'APPROVED' THEN DATEADD(HOUR, -12, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'APPROVED' THEN mu.user_id END,
    CASE WHEN s.request_status = 'REJECTED' THEN DATEADD(HOUR, -12, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'REJECTED' THEN mu.user_id END,
    CASE WHEN s.request_status = 'APPROVED' THEN N'Đã xác minh với log chấm công.' END,
    CASE WHEN s.request_status = 'REJECTED' THEN N'Chưa đủ thông tin xác thực.' END
FROM DemoAdjustments s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
LEFT JOIN dbo.sec_user_account mu ON mu.employee_id = e.manager_employee_id
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.att_adjustment_request r WHERE r.request_code = s.request_code
);
GO

;WITH DemoOT AS (
    SELECT *
    FROM (VALUES
        ('OT-PLUS-112', 'EMP112', CAST('2026-05-15' AS DATE), CAST('2026-05-15 17:30:00' AS DATETIME2), CAST('2026-05-15 19:00:00' AS DATETIME2),  90, 'SUBMITTED', N'Chuẩn bị offer package cho ứng viên.'),
        ('OT-PLUS-113', 'EMP113', CAST('2026-05-15' AS DATE), CAST('2026-05-15 17:45:00' AS DATETIME2), CAST('2026-05-15 19:15:00' AS DATETIME2),  90, 'APPROVED',  N'Đối soát bảng chấm công.'),
        ('OT-PLUS-114', 'EMP114', CAST('2026-05-16' AS DATE), CAST('2026-05-16 18:00:00' AS DATETIME2), CAST('2026-05-16 20:30:00' AS DATETIME2), 150, 'APPROVED',  N'Xử lý cảnh báo pipeline production.'),
        ('OT-PLUS-115', 'EMP115', CAST('2026-05-16' AS DATE), CAST('2026-05-16 18:00:00' AS DATETIME2), CAST('2026-05-16 21:00:00' AS DATETIME2), 180, 'SUBMITTED', N'Chuẩn bị migration hạ tầng.'),
        ('OT-PLUS-116', 'EMP116', CAST('2026-05-17' AS DATE), CAST('2026-05-17 17:30:00' AS DATETIME2), CAST('2026-05-17 19:00:00' AS DATETIME2),  90, 'SUBMITTED', N'Hoàn tất regression test.'),
        ('OT-PLUS-117', 'EMP117', CAST('2026-05-17' AS DATE), CAST('2026-05-17 17:45:00' AS DATETIME2), CAST('2026-05-17 19:15:00' AS DATETIME2),  90, 'REJECTED',  N'OT chưa có kế hoạch phê duyệt trước.'),
        ('OT-PLUS-118', 'EMP118', CAST('2026-05-18' AS DATE), CAST('2026-05-18 17:30:00' AS DATETIME2), CAST('2026-05-18 19:30:00' AS DATETIME2), 120, 'APPROVED',  N'Chuẩn bị dữ liệu forecast doanh số.'),
        ('OT-PLUS-119', 'EMP119', CAST('2026-05-18' AS DATE), CAST('2026-05-18 17:30:00' AS DATETIME2), CAST('2026-05-18 18:45:00' AS DATETIME2),  75, 'SUBMITTED', N'Follow-up khách hàng sau roadshow.'),
        ('OT-PLUS-120', 'EMP120', CAST('2026-05-19' AS DATE), CAST('2026-05-19 18:00:00' AS DATETIME2), CAST('2026-05-19 20:00:00' AS DATETIME2), 120, 'APPROVED',  N'Chốt proposal khách hàng enterprise.'),
        ('OT-PLUS-121', 'EMP121', CAST('2026-05-19' AS DATE), CAST('2026-05-19 17:45:00' AS DATETIME2), CAST('2026-05-19 19:15:00' AS DATETIME2),  90, 'SUBMITTED', N'Đối chiếu số liệu dòng tiền.'),
        ('OT-PLUS-122', 'EMP122', CAST('2026-05-20' AS DATE), CAST('2026-05-20 18:00:00' AS DATETIME2), CAST('2026-05-20 20:30:00' AS DATETIME2), 150, 'APPROVED',  N'Khóa bảng lương cuối tháng.'),
        ('OT-PLUS-123', 'EMP123', CAST('2026-05-20' AS DATE), CAST('2026-05-20 17:30:00' AS DATETIME2), CAST('2026-05-20 19:00:00' AS DATETIME2),  90, 'SUBMITTED', N'Chuẩn bị báo cáo nhân sự cho ban điều hành.')
    ) AS s(request_code, employee_code, attendance_date, overtime_start_at, overtime_end_at, requested_minutes, request_status, reason)
)
INSERT INTO dbo.att_overtime_request (
    request_code, employee_id, attendance_date, overtime_start_at, overtime_end_at, requested_minutes,
    reason, request_status, submitted_at, approved_at, approved_by, rejected_at, rejected_by, manager_note, rejection_note
)
SELECT
    s.request_code,
    e.employee_id,
    s.attendance_date,
    s.overtime_start_at,
    s.overtime_end_at,
    s.requested_minutes,
    s.reason,
    s.request_status,
    DATEADD(DAY, -1, SYSDATETIME()),
    CASE WHEN s.request_status = 'APPROVED' THEN DATEADD(HOUR, -8, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'APPROVED' THEN mu.user_id END,
    CASE WHEN s.request_status = 'REJECTED' THEN DATEADD(HOUR, -8, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'REJECTED' THEN mu.user_id END,
    CASE WHEN s.request_status = 'APPROVED' THEN N'OT phù hợp kế hoạch bộ phận.' END,
    CASE WHEN s.request_status = 'REJECTED' THEN N'Yêu cầu chưa có phê duyệt trước.' END
FROM DemoOT s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
LEFT JOIN dbo.sec_user_account mu ON mu.employee_id = e.manager_employee_id
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.att_overtime_request r WHERE r.request_code = s.request_code
);
GO

-- 9. Payroll dataset for May 2026
DECLARE @PayrollPeriodMay111 BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-05');
DECLARE @HrUser111B UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

;WITH DemoPayroll AS (
    SELECT *
    FROM (VALUES
        ('EMP112', 22, 21, 1, 0,  90, 18500000, 17659091, 900000, 1387500, 11000000, 0, 6171591, 394726, 18559091, 16776865, 'PUBLISHED'),
        ('EMP113', 22, 22, 0, 0,  90, 21000000, 21000000, 900000, 1575000, 11000000, 0, 9400000, 752000, 21900000, 19648000, 'MANAGER_CONFIRMED'),
        ('EMP114', 22, 22, 0, 0, 150, 30000000, 30000000, 900000, 2250000, 11000000, 0, 17650000, 1582500, 30900000, 27067500, 'PUBLISHED'),
        ('EMP115', 22, 22, 0, 0, 180, 38000000, 38000000, 900000, 2850000, 11000000, 0, 25950000, 3209000, 38900000, 32841000, 'PUBLISHED'),
        ('EMP116', 22, 21, 0, 1,  90, 17500000, 16704545, 900000, 1312500, 11000000, 0, 5282045, 286641, 17604545, 16005404, 'DRAFT'),
        ('EMP117', 22, 22, 0, 0,   0, 17800000, 17800000, 900000, 1335000, 11000000, 0, 6365000, 407600, 18700000, 16957400, 'DRAFT'),
        ('EMP118', 22, 22, 0, 0, 120, 20500000, 20500000, 900000, 1537500, 11000000, 0, 8862500, 709000, 21400000, 19153500, 'PUBLISHED'),
        ('EMP119', 22, 20, 2, 0,  75, 18000000, 18000000, 900000, 1350000, 11000000, 0, 6550000, 419000, 18900000, 17131000, 'MANAGER_CONFIRMED'),
        ('EMP120', 22, 22, 0, 0, 120, 23500000, 23500000, 900000, 1762500, 11000000, 0, 11637500, 931000, 24400000, 21706500, 'PUBLISHED'),
        ('EMP121', 22, 20, 0, 2,  90, 22500000, 20454545, 900000, 1687500, 11000000, 0, 8657045, 692564, 21354545, 18974481, 'DRAFT'),
        ('EMP122', 22, 22, 0, 0, 150, 24000000, 24000000, 900000, 1800000, 11000000, 0, 12100000, 968000, 24900000, 22132000, 'PUBLISHED'),
        ('EMP123', 22, 22, 0, 0,  90, 24500000, 24500000, 900000, 1837500, 11000000, 0, 12562500, 1005000, 25400000, 22557500, 'MANAGER_CONFIRMED')
    ) AS s(employee_code, scheduled_day_count, present_day_count, paid_leave_day_count, unpaid_leave_day_count, approved_ot_minutes, base_salary_monthly, base_salary_prorated, fixed_earning_total, employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount, taxable_income, pit_amount, gross_income, net_pay, item_status)
)
INSERT INTO dbo.pay_payroll_item (
    payroll_period_id, employee_id, scheduled_day_count, present_day_count, paid_leave_day_count, unpaid_leave_day_count,
    approved_ot_minutes, base_salary_monthly, base_salary_prorated, fixed_earning_total, fixed_deduction_total,
    employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount,
    taxable_income, pit_amount, gross_income, net_pay, item_status,
    manager_confirmed_by, manager_confirmed_at, manager_confirm_note,
    hr_approved_by, hr_approved_at, published_by, published_at
)
SELECT
    @PayrollPeriodMay111,
    e.employee_id,
    s.scheduled_day_count,
    s.present_day_count,
    s.paid_leave_day_count,
    s.unpaid_leave_day_count,
    s.approved_ot_minutes,
    s.base_salary_monthly,
    s.base_salary_prorated,
    s.fixed_earning_total,
    0,
    s.employee_insurance_amount,
    s.personal_deduction_amount,
    s.dependent_deduction_amount,
    s.taxable_income,
    s.pit_amount,
    s.gross_income,
    s.net_pay,
    s.item_status,
    CASE WHEN s.item_status IN ('MANAGER_CONFIRMED','PUBLISHED') THEN mu.user_id END,
    CASE WHEN s.item_status IN ('MANAGER_CONFIRMED','PUBLISHED') THEN DATEADD(HOUR, -6, SYSDATETIME()) END,
    CASE WHEN s.item_status IN ('MANAGER_CONFIRMED','PUBLISHED') THEN N'Xác nhận dữ liệu payroll theo team.' END,
    CASE WHEN s.item_status = 'PUBLISHED' THEN @HrUser111B END,
    CASE WHEN s.item_status = 'PUBLISHED' THEN DATEADD(HOUR, -3, SYSDATETIME()) END,
    CASE WHEN s.item_status = 'PUBLISHED' THEN @HrUser111B END,
    CASE WHEN s.item_status = 'PUBLISHED' THEN DATEADD(HOUR, -2, SYSDATETIME()) END
FROM DemoPayroll s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
LEFT JOIN dbo.sec_user_account mu ON mu.employee_id = e.manager_employee_id
WHERE @PayrollPeriodMay111 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.pay_payroll_item i
      WHERE i.payroll_period_id = @PayrollPeriodMay111
        AND i.employee_id = e.employee_id
  );
GO

INSERT INTO dbo.pay_payroll_item_line (
    payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note
)
SELECT i.payroll_item_id, 'BASE_SALARY', N'Lương cơ bản', 'EARNING', 'SYSTEM', i.base_salary_prorated, 1, 10, N'Feature-rich seed payroll line.'
FROM dbo.pay_payroll_item i
INNER JOIN dbo.hr_employee e ON e.employee_id = i.employee_id
WHERE e.employee_code IN ('EMP112','EMP113','EMP114','EMP115','EMP116','EMP117','EMP118','EMP119','EMP120','EMP121','EMP122','EMP123')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.pay_payroll_item_line l WHERE l.payroll_item_id = i.payroll_item_id AND l.component_code = 'BASE_SALARY'
  );

INSERT INTO dbo.pay_payroll_item_line (
    payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note
)
SELECT i.payroll_item_id, 'ALLOWANCE', N'Phụ cấp cố định', 'EARNING', 'CONFIGURED', i.fixed_earning_total, 0, 20, N'Phụ cấp cố định của seed CI/CD.'
FROM dbo.pay_payroll_item i
INNER JOIN dbo.hr_employee e ON e.employee_id = i.employee_id
WHERE e.employee_code IN ('EMP112','EMP113','EMP114','EMP115','EMP116','EMP117','EMP118','EMP119','EMP120','EMP121','EMP122','EMP123')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.pay_payroll_item_line l WHERE l.payroll_item_id = i.payroll_item_id AND l.component_code = 'ALLOWANCE'
  );

INSERT INTO dbo.pay_payroll_item_line (
    payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note
)
SELECT i.payroll_item_id, 'INSURANCE', N'BHXH, BHYT, BHTN', 'DEDUCTION', 'SYSTEM', i.employee_insurance_amount, 0, 30, N'Khấu trừ bảo hiểm.'
FROM dbo.pay_payroll_item i
INNER JOIN dbo.hr_employee e ON e.employee_id = i.employee_id
WHERE e.employee_code IN ('EMP112','EMP113','EMP114','EMP115','EMP116','EMP117','EMP118','EMP119','EMP120','EMP121','EMP122','EMP123')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.pay_payroll_item_line l WHERE l.payroll_item_id = i.payroll_item_id AND l.component_code = 'INSURANCE'
  );

INSERT INTO dbo.pay_payroll_item_line (
    payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note
)
SELECT i.payroll_item_id, 'PIT', N'Thuế TNCN', 'DEDUCTION', 'SYSTEM', i.pit_amount, 0, 40, N'Khấu trừ PIT.'
FROM dbo.pay_payroll_item i
INNER JOIN dbo.hr_employee e ON e.employee_id = i.employee_id
WHERE e.employee_code IN ('EMP112','EMP113','EMP114','EMP115','EMP116','EMP117','EMP118','EMP119','EMP120','EMP121','EMP122','EMP123')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.pay_payroll_item_line l WHERE l.payroll_item_id = i.payroll_item_id AND l.component_code = 'PIT'
  );
GO

-- 10. Profile change requests and timeline
DECLARE @AdminUser111 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');
DECLARE @HrUser111C UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

;WITH DemoProfileChange AS (
    SELECT *
    FROM (VALUES
        ('EMP112', 'PENDING',   N'{"mobilePhone":"0908220112","personalEmail":"linh.private112@gmail.com"}'),
        ('EMP113', 'APPROVED',  N'{"addressLine":"118 Hoàng Quốc Việt","provinceName":"Hà Nội"}'),
        ('EMP114', 'APPROVED',  N'{"personalEmail":"huy.private114@gmail.com"}'),
        ('EMP115', 'REJECTED',  N'{"mobilePhone":"0908220115"}'),
        ('EMP116', 'PENDING',   N'{"addressLine":"14 Phạm Văn Đồng","provinceName":"Hà Nội"}'),
        ('EMP117', 'PENDING',   N'{"maritalStatus":"MARRIED"}'),
        ('EMP118', 'APPROVED',  N'{"mobilePhone":"0908220118"}'),
        ('EMP119', 'REJECTED',  N'{"personalEmail":"thao.private119@gmail.com"}'),
        ('EMP120', 'APPROVED',  N'{"addressLine":"166 Võ Văn Kiệt","provinceName":"Hồ Chí Minh"}'),
        ('EMP121', 'PENDING',   N'{"personalEmail":"truc.private121@gmail.com"}'),
        ('EMP122', 'APPROVED',  N'{"mobilePhone":"0908220122"}'),
        ('EMP123', 'PENDING',   N'{"addressLine":"63 Phạm Hùng","provinceName":"Hà Nội"}')
    ) AS s(employee_code, request_status, payload_json)
)
INSERT INTO dbo.hr_employee_profile_change_request (
    employee_id, requester_user_id, request_type, request_status, payload_json,
    submitted_at, reviewed_at, reviewer_user_id, review_note, created_by
)
SELECT
    e.employee_id,
    u.user_id,
    'PROFILE_UPDATE',
    s.request_status,
    s.payload_json,
    DATEADD(DAY, -3, SYSDATETIME()),
    CASE WHEN s.request_status IN ('APPROVED','REJECTED') THEN DATEADD(DAY, -2, SYSDATETIME()) END,
    CASE WHEN s.request_status IN ('APPROVED','REJECTED') THEN @HrUser111C END,
    CASE
        WHEN s.request_status = 'APPROVED' THEN N'HR đã duyệt cập nhật hồ sơ.'
        WHEN s.request_status = 'REJECTED' THEN N'Yêu cầu cần bổ sung tài liệu xác minh.'
    END,
    u.user_id
FROM DemoProfileChange s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
INNER JOIN dbo.sec_user_account u ON u.employee_id = e.employee_id
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.hr_employee_profile_change_request r
    WHERE r.employee_id = e.employee_id
      AND r.request_status = s.request_status
      AND r.payload_json = s.payload_json
      AND r.is_deleted = 0
);
GO

DECLARE @AdminUser111 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');

INSERT INTO dbo.hr_employee_profile_timeline (
    employee_id, event_type, summary, detail_json, actor_user_id, event_at
)
SELECT e.employee_id, 'PROFILE_UPDATED', N'Cập nhật hồ sơ feature-rich', N'{"source":"R__111__seed_feature_rich_dataset"}', @AdminUser111, DATEADD(DAY, -1, SYSDATETIME())
FROM dbo.hr_employee e
WHERE e.employee_code IN ('EMP112','EMP113','EMP114','EMP115','EMP116','EMP117','EMP118','EMP119','EMP120','EMP121','EMP122','EMP123')
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.hr_employee_profile_timeline t
      WHERE t.employee_id = e.employee_id
        AND t.event_type = 'PROFILE_UPDATED'
        AND t.summary = N'Cập nhật hồ sơ feature-rich'
  );
GO

-- 11. Onboarding workflow expansion
DECLARE @HrUser111D UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

;WITH DemoOnboarding AS (
    SELECT *
    FROM (VALUES
        ('ONB-2026-200', N'Nguyễn Hoài Phương', 'hoaiphuong200@example.com', '0919200200', 'FEMALE', CAST('2000-01-20' AS DATE), CAST('2026-06-01' AS DATE), 'CAND200', 'TEAM_TA_HN',       'TA_SPEC',    'EMP007', 'DRAFT',          N'Ứng viên tuyển dụng nội bộ.'),
        ('ONB-2026-201', N'Trần Hữu Tâm',      'huutam201@example.com',     '0919200201', 'MALE',   CAST('1998-02-21' AS DATE), CAST('2026-06-02' AS DATE), 'CAND201', 'TEAM_CNB_HN',      'HRBP',       'EMP007', 'IN_PROGRESS',    N'Đang hoàn thiện tài liệu nhận việc.'),
        ('ONB-2026-202', N'Lê Gia Bảo',        'giabao202@example.com',     '0919200202', 'MALE',   CAST('1999-03-22' AS DATE), CAST('2026-06-03' AS DATE), 'CAND202', 'TEAM_PLATFORM_HN', 'DEVOPS_ENG', 'EMP006', 'READY_FOR_JOIN', N'Đã chốt lịch orientation.'),
        ('ONB-2026-203', N'Phạm Khánh Vy',     'khanhvy203@example.com',    '0919200203', 'FEMALE', CAST('2001-04-23' AS DATE), CAST('2026-06-04' AS DATE), 'CAND203', 'TEAM_QA_HN',       'QA_ENG',     'EMP006', 'IN_PROGRESS',    N'Đang chờ cấp tài khoản test.'),
        ('ONB-2026-204', N'Đỗ Nhật Minh',      'nhatminh204@example.com',   '0919200204', 'MALE',   CAST('1998-05-24' AS DATE), CAST('2026-06-05' AS DATE), 'CAND204', 'TEAM_SMB_HCM',     'EXEC_SALES', 'EMP008', 'DRAFT',          N'Chờ xác nhận lịch đào tạo sản phẩm.'),
        ('ONB-2026-205', N'Võ Thùy Dung',      'thuydung205@example.com',   '0919200205', 'FEMALE', CAST('1999-06-25' AS DATE), CAST('2026-06-06' AS DATE), 'CAND205', 'TEAM_ENT_HCM',     'SALES_OPS',  'EMP008', 'READY_FOR_JOIN', N'Đã hoàn tất hồ sơ pháp lý.'),
        ('ONB-2026-206', N'Bùi Anh Tuấn',      'anhtuan206@example.com',    '0919200206', 'MALE',   CAST('1997-07-26' AS DATE), CAST('2026-06-08' AS DATE), 'CAND206', 'TEAM_AP_HCM',      'FIN_ANALYST','EMP004', 'IN_PROGRESS',    N'Đang chờ account ERP.'),
        ('ONB-2026-207', N'Ngô Mai Chi',       'maichi207@example.com',     '0919200207', 'FEMALE', CAST('2000-08-27' AS DATE), CAST('2026-06-09' AS DATE), 'CAND207', 'TEAM_PAYROLL_HCM', 'ACC_PAY',    'EMP004', 'READY_FOR_JOIN', N'Chờ cấp thiết bị và account.'),
        ('ONB-2026-208', N'Hoàng Quốc Đạt',    'quocdat208@example.com',    '0919200208', 'MALE',   CAST('1998-09-28' AS DATE), CAST('2026-06-10' AS DATE), 'CAND208', 'TEAM_PLATFORM_HN', 'TECH_LEAD',  'EMP006', 'DRAFT',          N'Ứng viên lateral hire.'),
        ('ONB-2026-209', N'Tạ Phương Linh',    'phuonglinh209@example.com', '0919200209', 'FEMALE', CAST('2001-10-29' AS DATE), CAST('2026-06-11' AS DATE), 'CAND209', 'TEAM_CNB_HN',      'CNB_SPEC',   'EMP007', 'IN_PROGRESS',    N'Đã gửi bộ hồ sơ nhận việc.')
    ) AS s(onboarding_code, full_name, personal_email, personal_phone, gender_code, date_of_birth, planned_start_date, employee_code, org_unit_code, job_title_code, manager_code, status, note)
)
INSERT INTO dbo.onb_onboarding (
    onboarding_code, full_name, personal_email, personal_phone, gender_code, date_of_birth,
    planned_start_date, employee_code, org_unit_id, job_title_id, manager_employee_id,
    work_location, status, note, orientation_confirmed_at, orientation_confirmed_by, orientation_note, created_by
)
SELECT
    s.onboarding_code,
    s.full_name,
    s.personal_email,
    s.personal_phone,
    s.gender_code,
    s.date_of_birth,
    s.planned_start_date,
    s.employee_code,
    o.org_unit_id,
    j.job_title_id,
    m.employee_id,
    CASE WHEN s.org_unit_code LIKE 'TEAM_%_HN' THEN N'Hanoi Office' ELSE N'HCMC Office' END,
    s.status,
    s.note,
    CASE WHEN s.status = 'READY_FOR_JOIN' THEN DATEADD(DAY, -1, SYSDATETIME()) END,
    CASE WHEN s.status = 'READY_FOR_JOIN' THEN mu.user_id END,
    CASE WHEN s.status = 'READY_FOR_JOIN' THEN N'Manager đã xác nhận lịch orientation.' END,
    @HrUser111D
FROM DemoOnboarding s
INNER JOIN dbo.hr_org_unit o ON o.org_unit_code = s.org_unit_code
INNER JOIN dbo.hr_job_title j ON j.job_title_code = s.job_title_code
INNER JOIN dbo.hr_employee m ON m.employee_code = s.manager_code
LEFT JOIN dbo.sec_user_account mu ON mu.employee_id = m.employee_id
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.onb_onboarding x WHERE x.onboarding_code = s.onboarding_code
);
GO

DECLARE @HrUser111E UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

INSERT INTO dbo.onb_onboarding_checklist (
    onboarding_id, item_code, item_name, is_required, is_completed, due_date, completed_at, completed_by, note, created_by
)
SELECT o.onboarding_id, 'WELCOME', N'Gửi thư chào mừng', 1,
       CASE WHEN o.status IN ('IN_PROGRESS','READY_FOR_JOIN','COMPLETED') THEN 1 ELSE 0 END,
       o.planned_start_date,
       CASE WHEN o.status IN ('IN_PROGRESS','READY_FOR_JOIN','COMPLETED') THEN DATEADD(DAY, -3, CAST(o.planned_start_date AS DATETIME2)) END,
       CASE WHEN o.status IN ('IN_PROGRESS','READY_FOR_JOIN','COMPLETED') THEN @HrUser111E END,
       N'Checklist onboarding feature-rich.',
       @HrUser111E
FROM dbo.onb_onboarding o
WHERE o.onboarding_code LIKE 'ONB-2026-20%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.onb_onboarding_checklist c WHERE c.onboarding_id = o.onboarding_id AND c.item_code = 'WELCOME'
  );

INSERT INTO dbo.onb_onboarding_checklist (
    onboarding_id, item_code, item_name, is_required, is_completed, due_date, completed_at, completed_by, note, created_by
)
SELECT o.onboarding_id, 'ACCOUNT', N'Cấp tài khoản hệ thống', 1,
       CASE WHEN o.status = 'READY_FOR_JOIN' THEN 1 ELSE 0 END,
       o.planned_start_date,
       CASE WHEN o.status = 'READY_FOR_JOIN' THEN DATEADD(DAY, -1, CAST(o.planned_start_date AS DATETIME2)) END,
       CASE WHEN o.status = 'READY_FOR_JOIN' THEN @HrUser111E END,
       N'Checklist onboarding feature-rich.',
       @HrUser111E
FROM dbo.onb_onboarding o
WHERE o.onboarding_code LIKE 'ONB-2026-20%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.onb_onboarding_checklist c WHERE c.onboarding_id = o.onboarding_id AND c.item_code = 'ACCOUNT'
  );
GO

DECLARE @HrUser111E UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

INSERT INTO dbo.onb_onboarding_document (
    onboarding_id, document_name, document_category, storage_path, mime_type, file_size_bytes, status, note, created_by
)
SELECT o.onboarding_id, N'Hồ sơ ứng viên', 'PROFILE', N'/seed/onboarding/' + o.onboarding_code + N'/profile.pdf', 'application/pdf', 245760, 'ACTIVE', N'Tài liệu onboarding feature-rich.', @HrUser111E
FROM dbo.onb_onboarding o
WHERE o.onboarding_code LIKE 'ONB-2026-20%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.onb_onboarding_document d WHERE d.onboarding_id = o.onboarding_id AND d.document_name = N'Hồ sơ ứng viên'
  );

INSERT INTO dbo.onb_onboarding_asset (
    onboarding_id, asset_code, asset_name, asset_type, assigned_date, status, note, created_by
)
SELECT o.onboarding_id, 'ASTX-' + RIGHT(o.onboarding_code, 3), N'Laptop tiêu chuẩn', 'LAPTOP',
       CASE WHEN o.status = 'READY_FOR_JOIN' THEN o.planned_start_date END,
       CASE WHEN o.status = 'READY_FOR_JOIN' THEN 'ASSIGNED' ELSE 'PLANNED' END,
       N'Tài sản onboarding feature-rich.',
       @HrUser111E
FROM dbo.onb_onboarding o
WHERE o.onboarding_code LIKE 'ONB-2026-20%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.onb_onboarding_asset a WHERE a.onboarding_id = o.onboarding_id AND a.asset_code = 'ASTX-' + RIGHT(o.onboarding_code, 3)
  );
GO

-- 12. Offboarding workflow expansion
DECLARE @HrUser111F UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

;WITH DemoOffboarding AS (
    SELECT *
    FROM (VALUES
        ('OFF-2026-200', 'EMP112', CAST('2026-05-03' AS DATE), CAST('2026-05-30' AS DATE), 'REQUESTED',           N'Nghỉ việc để chuyển địa điểm sinh sống.'),
        ('OFF-2026-201', 'EMP113', CAST('2026-05-02' AS DATE), CAST('2026-05-29' AS DATE), 'MANAGER_APPROVED',    N'Đã thống nhất kế hoạch bàn giao C&B.'),
        ('OFF-2026-202', 'EMP114', CAST('2026-05-01' AS DATE), CAST('2026-05-28' AS DATE), 'HR_FINALIZED',       N'HR đã chốt lịch nghỉ việc DevOps.'),
        ('OFF-2026-203', 'EMP115', CAST('2026-04-29' AS DATE), CAST('2026-05-27' AS DATE), 'ACCESS_REVOKED',     N'Đã thu hồi quyền production.'),
        ('OFF-2026-204', 'EMP116', CAST('2026-05-04' AS DATE), CAST('2026-05-31' AS DATE), 'REQUESTED',           N'Thử việc không tiếp tục.'),
        ('OFF-2026-205', 'EMP117', CAST('2026-05-05' AS DATE), CAST('2026-06-02' AS DATE), 'MANAGER_REJECTED',    N'Chưa có người thay thế phù hợp.'),
        ('OFF-2026-206', 'EMP118', CAST('2026-05-06' AS DATE), CAST('2026-06-03' AS DATE), 'SETTLEMENT_PREPARED', N'Đã chuẩn bị settlement sales ops.'),
        ('OFF-2026-207', 'EMP119', CAST('2026-05-07' AS DATE), CAST('2026-06-04' AS DATE), 'HR_FINALIZED',       N'HR đã xác nhận ngày cuối làm việc.'),
        ('OFF-2026-208', 'EMP120', CAST('2026-05-08' AS DATE), CAST('2026-06-05' AS DATE), 'CLOSED',              N'Đã hoàn tất offboarding enterprise sales.'),
        ('OFF-2026-209', 'EMP121', CAST('2026-05-09' AS DATE), CAST('2026-06-06' AS DATE), 'MANAGER_APPROVED',    N'Đã chốt kế hoạch bàn giao phân tích tài chính.')
    ) AS s(offboarding_code, employee_code, request_date, requested_last_working_date, status, request_reason)
)
INSERT INTO dbo.off_offboarding_case (
    offboarding_code, employee_id, requested_by_user_id, request_date, requested_last_working_date, request_reason, status,
    manager_reviewed_by, manager_reviewed_at, manager_review_note,
    hr_finalized_by, hr_finalized_at, effective_last_working_date, hr_finalize_note,
    access_revoked_by, access_revoked_at, access_revoke_note,
    settlement_prepared_by, settlement_prepared_at, final_attendance_year, final_attendance_month,
    leave_settlement_units, leave_settlement_amount, settlement_note,
    closed_by, closed_at, close_note, created_by
)
SELECT
    s.offboarding_code,
    e.employee_id,
    u.user_id,
    s.request_date,
    s.requested_last_working_date,
    s.request_reason,
    s.status,
    CASE WHEN s.status IN ('MANAGER_APPROVED','HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN mu.user_id END,
    CASE WHEN s.status IN ('MANAGER_APPROVED','HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN DATEADD(DAY, -5, SYSDATETIME()) END,
    CASE WHEN s.status IN ('MANAGER_APPROVED','HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN N'Quản lý đã xác nhận phương án bàn giao.' END,
    CASE WHEN s.status IN ('HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN @HrUser111F END,
    CASE WHEN s.status IN ('HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN DATEADD(DAY, -4, SYSDATETIME()) END,
    CASE WHEN s.status IN ('HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN s.requested_last_working_date END,
    CASE WHEN s.status IN ('HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN N'HR đã chốt hồ sơ nghỉ việc.' END,
    CASE WHEN s.status IN ('ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN @HrUser111F END,
    CASE WHEN s.status IN ('ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN DATEADD(DAY, -3, SYSDATETIME()) END,
    CASE WHEN s.status IN ('ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN N'Đã khóa quyền truy cập liên quan.' END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN @HrUser111F END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN DATEADD(DAY, -2, SYSDATETIME()) END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN 2026 END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN 5 END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN 1.0 END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN 1200000 END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN N'Đã chuẩn bị settlement feature-rich.' END,
    CASE WHEN s.status = 'CLOSED' THEN @HrUser111F END,
    CASE WHEN s.status = 'CLOSED' THEN DATEADD(DAY, -1, SYSDATETIME()) END,
    CASE WHEN s.status = 'CLOSED' THEN N'Hoàn tất đóng hồ sơ offboarding.' END,
    u.user_id
FROM DemoOffboarding s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
LEFT JOIN dbo.sec_user_account u ON u.employee_id = e.employee_id
LEFT JOIN dbo.sec_user_account mu ON mu.employee_id = e.manager_employee_id
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.off_offboarding_case c WHERE c.offboarding_code = s.offboarding_code
);
GO

DECLARE @HrUser111F UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

INSERT INTO dbo.off_offboarding_checklist_item (
    offboarding_case_id, item_type, item_name, owner_role_code, due_date, status, note, created_by
)
SELECT c.offboarding_case_id, 'HANDOVER', N'Bàn giao công việc và tài liệu', 'MANAGER',
       c.requested_last_working_date,
       CASE WHEN c.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN 'COMPLETED' WHEN c.status IN ('HR_FINALIZED','ACCESS_REVOKED') THEN 'IN_PROGRESS' ELSE 'OPEN' END,
       N'Checklist offboarding feature-rich.',
       @HrUser111F
FROM dbo.off_offboarding_case c
WHERE c.offboarding_code LIKE 'OFF-2026-20%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.off_offboarding_checklist_item i WHERE i.offboarding_case_id = c.offboarding_case_id AND i.item_name = N'Bàn giao công việc và tài liệu'
  );

INSERT INTO dbo.off_offboarding_asset_return (
    offboarding_case_id, asset_code, asset_name, asset_type, expected_return_date, status, note, created_by
)
SELECT c.offboarding_case_id, 'RETX-' + RIGHT(c.offboarding_code, 3), N'Laptop công ty', 'LAPTOP',
       c.requested_last_working_date,
       CASE WHEN c.status = 'CLOSED' THEN 'RETURNED' ELSE 'PENDING' END,
       N'Tài sản offboarding feature-rich.',
       @HrUser111F
FROM dbo.off_offboarding_case c
WHERE c.offboarding_code LIKE 'OFF-2026-20%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.off_offboarding_asset_return a WHERE a.offboarding_case_id = c.offboarding_case_id AND a.asset_code = 'RETX-' + RIGHT(c.offboarding_code, 3)
  );

INSERT INTO dbo.off_offboarding_history (
    offboarding_case_id, from_status, to_status, action_code, action_note, changed_by, changed_at, created_by
)
SELECT c.offboarding_case_id, NULL, 'REQUESTED', 'REQUEST', N'Khởi tạo hồ sơ offboarding feature-rich.', c.requested_by_user_id, DATEADD(DAY, -7, SYSDATETIME()), c.requested_by_user_id
FROM dbo.off_offboarding_case c
WHERE c.offboarding_code LIKE 'OFF-2026-20%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.off_offboarding_history h WHERE h.offboarding_case_id = c.offboarding_case_id AND h.action_code = 'REQUEST'
  );
GO

-- 13. Portal inbox, audit log and reporting schedules
DECLARE @AdminUser111B UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');

INSERT INTO dbo.por_portal_inbox_item (
    user_id, employee_id, item_type, category_code, title, message, related_module, related_entity_id,
    due_at, read_at, task_status, completed_at, status, created_by
)
SELECT u.user_id, e.employee_id, 'NOTIFICATION', 'PORTAL', N'Cập nhật dashboard theo team',
       N'Dữ liệu feature-rich đã được nạp để kiểm thử portal và các dashboard theo vai trò.',
       'PORTAL', e.employee_code, DATEADD(DAY, 4, SYSDATETIME()),
       NULL, 'OPEN', NULL, 'ACTIVE', @AdminUser111B
FROM dbo.hr_employee e
INNER JOIN dbo.sec_user_account u ON u.employee_id = e.employee_id
WHERE e.employee_code IN ('EMP112','EMP113','EMP114','EMP115','EMP116','EMP117','EMP118','EMP119','EMP120','EMP121','EMP122','EMP123')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.por_portal_inbox_item i
      WHERE i.user_id = u.user_id
        AND i.title = N'Cập nhật dashboard theo team'
        AND i.related_entity_id = e.employee_code
  );

INSERT INTO dbo.por_portal_inbox_item (
    user_id, employee_id, item_type, category_code, title, message, related_module, related_entity_id,
    due_at, read_at, task_status, completed_at, status, created_by
)
SELECT u.user_id, e.employee_id, 'TASK', 'SELF_SERVICE', N'Rà soát thông tin ngân hàng',
       N'Vui lòng xác nhận lại số tài khoản và ngân hàng nhận lương trước kỳ payroll tiếp theo.',
       'PAYROLL', e.employee_code, DATEADD(DAY, 6, SYSDATETIME()),
       CASE WHEN RIGHT(e.employee_code, 1) IN ('2','4','6','8') THEN DATEADD(HOUR, -5, SYSDATETIME()) END,
       CASE WHEN RIGHT(e.employee_code, 1) IN ('3','5','7','9') THEN 'DONE' ELSE 'OPEN' END,
       CASE WHEN RIGHT(e.employee_code, 1) IN ('3','5','7','9') THEN DATEADD(HOUR, -2, SYSDATETIME()) END,
       'ACTIVE', @AdminUser111B
FROM dbo.hr_employee e
INNER JOIN dbo.sec_user_account u ON u.employee_id = e.employee_id
WHERE e.employee_code IN ('EMP112','EMP113','EMP114','EMP115','EMP116','EMP117','EMP118','EMP119','EMP120','EMP121','EMP122','EMP123')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.por_portal_inbox_item i
      WHERE i.user_id = u.user_id
        AND i.title = N'Rà soát thông tin ngân hàng'
        AND i.related_entity_id = e.employee_code
  );
GO

;WITH DemoAudit AS (
    SELECT *
    FROM (VALUES
        ('quynh_ndd',  'CREATE',         'ORG_UNIT',      'hr_org_unit',                       'TEAM_TA_HN',         'SUCCESS', N'Tạo team Talent Acquisition'),
        ('quynh_ndd',  'CREATE',         'ORG_UNIT',      'hr_org_unit',                       'TEAM_PLATFORM_HN',   'SUCCESS', N'Tạo team Platform'),
        ('quynh_ndd',  'CREATE',         'JOB_TITLE',     'hr_job_title',                      'DEVOPS_ENG',         'SUCCESS', N'Tạo chức danh DevOps Engineer'),
        ('nguyen',     'CREATE',         'USER',          'sec_user_account',                  'demo_emp112',        'SUCCESS', N'Tạo tài khoản demo_emp112'),
        ('nguyen',     'ASSIGN_ROLE',    'ROLE',          'sec_user_role',                     'demo_emp112',        'SUCCESS', N'Gán role EMPLOYEE cho demo_emp112'),
        ('quynh_ndd',  'CREATE',         'EMPLOYEE',      'hr_employee',                       'EMP112',             'SUCCESS', N'Tạo nhân sự EMP112'),
        ('quynh_ndd',  'CREATE',         'EMPLOYEE',      'hr_employee',                       'EMP123',             'SUCCESS', N'Tạo nhân sự EMP123'),
        ('quynh_ndd',  'CREATE',         'CONTRACT',      'ct_labor_contract',                 'HDLD-EMP115-01',     'SUCCESS', N'Tạo hợp đồng cho EMP115'),
        ('quynh_ndd',  'CREATE',         'PAYROLL',       'pay_employee_compensation',         'EMP122',             'SUCCESS', N'Tạo cấu hình lương cho EMP122'),
        ('demo_emp113','REQUEST',        'LEAVE_REQUEST', 'lea_leave_request',                 'REQ-2026-PLUS-113',  'SUCCESS', N'Nhân viên gửi đơn nghỉ phép'),
        ('demo_emp116','REQUEST',        'ATTENDANCE',    'att_adjustment_request',            'ADJ-PLUS-116',       'SUCCESS', N'Nhân viên gửi yêu cầu điều chỉnh công'),
        ('demo_emp118','REQUEST',        'ATTENDANCE',    'att_overtime_request',              'OT-PLUS-118',        'SUCCESS', N'Nhân viên gửi yêu cầu OT'),
        ('quynh_ndd',  'GENERATE',       'PAYROLL',       'pay_payroll_item',                  'EMP114-2026-05',     'SUCCESS', N'Tạo phiếu lương tháng 05 cho EMP114'),
        ('quynh_ndd',  'REVIEW_REQUEST', 'EMPLOYEE_PROFILE_REQUEST','hr_employee_profile_change_request','EMP118','SUCCESS', N'HR duyệt yêu cầu cập nhật hồ sơ EMP118'),
        ('quynh_ndd',  'CREATE',         'ONBOARDING',    'onb_onboarding',                    'ONB-2026-202',       'SUCCESS', N'Tạo onboarding ứng viên DevOps'),
        ('nguyen_ndb', 'CONFIRM',        'ONBOARDING',    'onb_onboarding',                    'ONB-2026-205',       'SUCCESS', N'Manager xác nhận orientation sales ops'),
        ('demo_emp120','REQUEST',        'OFFBOARDING',   'off_offboarding_case',              'OFF-2026-208',       'SUCCESS', N'Nhân viên gửi đơn nghỉ việc'),
        ('quynh_ndd',  'FINALIZE',       'OFFBOARDING',   'off_offboarding_case',              'OFF-2026-202',       'SUCCESS', N'HR finalize hồ sơ offboarding'),
        ('nguyen',     'EXPORT',         'REPORT',        'rep_report_schedule_run',           'SCH-TEAM-ATT-DAILY', 'SUCCESS', N'Xuất báo cáo attendance team'),
        ('nguyen',     'UPDATE',         'SYSTEM_CONFIG', 'sys_system_config',                 'portal.task.reminder.days', 'SUCCESS', N'Cập nhật cấu hình reminder portal')
    ) AS s(actor_username, action_code, module_code, entity_name, entity_id, result_code, message)
)
INSERT INTO dbo.sys_audit_log (
    actor_user_id, actor_username, action_code, module_code, entity_name, entity_id,
    request_id, old_data_json, new_data_json, ip_address, user_agent, action_at, result_code, message
)
SELECT
    u.user_id,
    s.actor_username,
    s.action_code,
    s.module_code,
    s.entity_name,
    s.entity_id,
    'seed-' + LOWER(REPLACE(s.module_code, '_', '-')) + '-111',
    NULL,
    N'{"seed":"R__111__seed_feature_rich_dataset"}',
    '127.0.0.1',
    N'Flyway Seed',
    DATEADD(MINUTE, -3, SYSDATETIME()),
    s.result_code,
    s.message
FROM DemoAudit s
LEFT JOIN dbo.sec_user_account u ON u.username = s.actor_username
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.sys_audit_log a
    WHERE a.actor_username = s.actor_username
      AND a.action_code = s.action_code
      AND a.module_code = s.module_code
      AND a.entity_id = s.entity_id
      AND a.message = s.message
);
GO

DECLARE @AdminUser111B UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');
DECLARE @Now111 DATETIME2(0) = SYSDATETIME();

;WITH DemoSchedule AS (
    SELECT *
    FROM (VALUES
        ('SCH-TEAM-ATT-DAILY',  N'Báo cáo công và OT theo team',        'ATTENDANCE_ANOMALY_OT',  'DAILY',   NULL, NULL,  7, 45, N'hr@digitalhrm.com,manager@digitalhrm.com'),
        ('SCH-TEAM-LEAVE-WEEK', N'Báo cáo nghỉ phép theo line manager', 'LEAVE_BALANCE',          'WEEKLY', 2,    NULL, 8, 15, N'hr@digitalhrm.com,manager@digitalhrm.com'),
        ('SCH-OPS-PAY-MONTH',   N'Báo cáo payroll vận hành',            'PAYROLL_SUMMARY',        'MONTHLY', NULL, 2,    9, 30, N'finance@digitalhrm.com,hr@digitalhrm.com'),
        ('SCH-LIFECYCLE-WEEK',  N'Báo cáo lifecycle nhân sự',           'ONBOARDING_OFFBOARDING', 'WEEKLY', 5,    NULL, 10, 0, N'hr@digitalhrm.com,admin@digitalhrm.com')
    ) AS s(schedule_code, schedule_name, report_code, frequency_code, day_of_week, day_of_month, run_at_hour, run_at_minute, recipient_emails_csv)
)
INSERT INTO dbo.rep_report_schedule_config (
    schedule_code, schedule_name, report_code, frequency_code, day_of_week, day_of_month,
    run_at_hour, run_at_minute, recipient_emails_csv, parameter_json, status,
    next_run_at, last_run_at, last_run_status, last_run_message, description, created_by
)
SELECT
    s.schedule_code, s.schedule_name, s.report_code, s.frequency_code, s.day_of_week, s.day_of_month,
    s.run_at_hour, s.run_at_minute, s.recipient_emails_csv,
    N'{"seed":"R__111__seed_feature_rich_dataset","scope":"team"}',
    'ACTIVE',
    DATEADD(DAY, 1, @Now111),
    DATEADD(DAY, -1, @Now111),
    'SUCCESS',
    N'Run gần nhất thành công.',
    N'Lịch báo cáo mở rộng cho feature-rich dataset.',
    @AdminUser111B
FROM DemoSchedule s
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.rep_report_schedule_config c WHERE c.schedule_code = s.schedule_code
);
GO

DECLARE @AdminUser111B UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');

INSERT INTO dbo.rep_report_schedule_run (
    report_schedule_config_id, trigger_type, triggered_by_user_id, started_at, finished_at,
    run_status, output_file_key, output_file_name, output_row_count, run_message, created_by
)
SELECT
    c.report_schedule_config_id,
    'SCHEDULED',
    @AdminUser111B,
    DATEADD(DAY, -1, SYSDATETIME()),
    DATEADD(DAY, -1, DATEADD(MINUTE, 4, SYSDATETIME())),
    'SUCCESS',
    'reports/' + c.schedule_code + '.csv',
    c.schedule_code + '.csv',
    180,
    N'Run feature-rich thành công.',
    @AdminUser111B
FROM dbo.rep_report_schedule_config c
WHERE c.schedule_code IN ('SCH-TEAM-ATT-DAILY','SCH-TEAM-LEAVE-WEEK','SCH-OPS-PAY-MONTH','SCH-LIFECYCLE-WEEK')
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.rep_report_schedule_run r
      WHERE r.report_schedule_config_id = c.report_schedule_config_id
        AND r.output_file_name = c.schedule_code + '.csv'
  );
GO

SET NOCOUNT OFF;
GO
