SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__110__seed_ci_demo_dataset.sql
   Scope:
   - Seed dense, idempotent demo data for CI/CD smoke tests
   - Cover admin / HR / manager / employee portal features
   - Keep references stable by deterministic codes
   ========================================================= */

-- 1. Stabilize org-unit managers so scope-based features behave consistently
DECLARE @Emp004 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @Emp006 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP006');
DECLARE @Emp007 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP007');
DECLARE @Emp008 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP008');

UPDATE dbo.hr_org_unit
SET manager_employee_id = CASE org_unit_code
    WHEN 'DEPT_HR_HN' THEN @Emp007
    WHEN 'DEPT_IT_HN' THEN @Emp006
    WHEN 'DEPT_SALES_HCM' THEN @Emp008
    WHEN 'DEPT_FIN_HCM' THEN @Emp004
    ELSE manager_employee_id
END,
updated_at = SYSDATETIME()
WHERE org_unit_code IN ('DEPT_HR_HN', 'DEPT_IT_HN', 'DEPT_SALES_HCM', 'DEPT_FIN_HCM')
  AND (
      (org_unit_code = 'DEPT_HR_HN' AND ISNULL(manager_employee_id, 0) <> ISNULL(@Emp007, 0))
      OR (org_unit_code = 'DEPT_IT_HN' AND ISNULL(manager_employee_id, 0) <> ISNULL(@Emp006, 0))
      OR (org_unit_code = 'DEPT_SALES_HCM' AND ISNULL(manager_employee_id, 0) <> ISNULL(@Emp008, 0))
      OR (org_unit_code = 'DEPT_FIN_HCM' AND ISNULL(manager_employee_id, 0) <> ISNULL(@Emp004, 0))
  );
GO

-- 2. Seed dense employee dataset
;WITH DemoEmployees AS (
    SELECT *
    FROM (VALUES
        ('EMP100', 'DEPT_HR_HN',    'MGR_HR',    'EMP007', N'Nguyễn Hồng Minh',  'FEMALE', CAST('1995-01-14' AS DATE), CAST('2024-02-12' AS DATE), N'Hanoi Office', 'minh.nguyen100@digitalhrm.com', '0907000100'),
        ('EMP101', 'DEPT_HR_HN',    'MGR_HR',    'EMP007', N'Trần Quang Phúc',   'MALE',   CAST('1993-03-09' AS DATE), CAST('2023-07-01' AS DATE), N'Hanoi Office', 'phuc.tran101@digitalhrm.com', '0907000101'),
        ('EMP102', 'DEPT_HR_HN',    'MGR_HR',    'EMP007', N'Lê Thuỳ Dương',     'FEMALE', CAST('1997-06-22' AS DATE), CAST('2025-01-06' AS DATE), N'Hanoi Office', 'duong.le102@digitalhrm.com', '0907000102'),
        ('EMP103', 'DEPT_IT_HN',    'SDR_DEV',   'EMP006', N'Phạm Gia Bảo',      'MALE',   CAST('1996-08-05' AS DATE), CAST('2023-09-18' AS DATE), N'Hanoi Office', 'bao.pham103@digitalhrm.com', '0907000103'),
        ('EMP104', 'DEPT_IT_HN',    'JDR_DEV',   'EMP006', N'Đỗ Khánh Vy',       'FEMALE', CAST('1999-11-11' AS DATE), CAST('2025-03-10' AS DATE), N'Hanoi Office', 'vy.do104@digitalhrm.com', '0907000104'),
        ('EMP105', 'DEPT_IT_HN',    'SDR_DEV',   'EMP006', N'Vũ Đức Nam',        'MALE',   CAST('1994-04-02' AS DATE), CAST('2022-12-01' AS DATE), N'Hanoi Office', 'nam.vu105@digitalhrm.com', '0907000105'),
        ('EMP106', 'DEPT_SALES_HCM','EXEC_SALES','EMP008', N'Bùi Hải Yến',       'FEMALE', CAST('1998-05-17' AS DATE), CAST('2024-06-03' AS DATE), N'HCMC Office',  'yen.bui106@digitalhrm.com', '0907000106'),
        ('EMP107', 'DEPT_SALES_HCM','EXEC_SALES','EMP008', N'Hoàng Minh Khang',  'MALE',   CAST('1997-12-21' AS DATE), CAST('2024-08-19' AS DATE), N'HCMC Office',  'khang.hoang107@digitalhrm.com', '0907000107'),
        ('EMP108', 'DEPT_SALES_HCM','EXEC_SALES','EMP008', N'Ngô Gia Linh',      'FEMALE', CAST('2000-02-13' AS DATE), CAST('2025-02-17' AS DATE), N'HCMC Office',  'linh.ngo108@digitalhrm.com', '0907000108'),
        ('EMP109', 'DEPT_FIN_HCM',  'SSR_ACC',   'EMP004', N'Đặng Nhật Anh',     'MALE',   CAST('1995-10-04' AS DATE), CAST('2024-03-11' AS DATE), N'HCMC Office',  'anh.dang109@digitalhrm.com', '0907000109'),
        ('EMP110', 'DEPT_FIN_HCM',  'SSR_ACC',   'EMP004', N'Võ Thanh Tâm',      'FEMALE', CAST('1994-07-28' AS DATE), CAST('2023-11-20' AS DATE), N'HCMC Office',  'tam.vo110@digitalhrm.com', '0907000110'),
        ('EMP111', 'DEPT_FIN_HCM',  'SSR_ACC',   'EMP004', N'Phan Đức Huy',      'MALE',   CAST('1998-09-30' AS DATE), CAST('2025-01-13' AS DATE), N'HCMC Office',  'huy.phan111@digitalhrm.com', '0907000111')
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
        target.mobile_phone = source.mobile_phone,
        target.work_phone = source.mobile_phone,
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
        'ACTIVE', source.work_location, source.work_email, source.mobile_phone, N'Demo dataset for CI/CD seed.'
    );
GO

-- 3. Profiles, contact details, identification, bank accounts
;WITH DemoProfiles AS (
    SELECT *
    FROM (VALUES
        ('EMP100', N'Minh', N'Nguyễn Hồng', 'SINGLE',  N'Human Resources'),
        ('EMP101', N'Phúc', N'Trần Quang',  'MARRIED', N'Law'),
        ('EMP102', N'Dương',N'Lê Thuỳ',     'SINGLE',  N'Business Administration'),
        ('EMP103', N'Bảo',  N'Phạm Gia',    'SINGLE',  N'Computer Science'),
        ('EMP104', N'Vy',   N'Đỗ Khánh',    'SINGLE',  N'Information Technology'),
        ('EMP105', N'Nam',  N'Vũ Đức',      'MARRIED', N'Software Engineering'),
        ('EMP106', N'Yến',  N'Bùi Hải',     'SINGLE',  N'Marketing'),
        ('EMP107', N'Khang',N'Hoàng Minh',  'SINGLE',  N'Business Administration'),
        ('EMP108', N'Linh', N'Ngô Gia',     'SINGLE',  N'International Business'),
        ('EMP109', N'Anh',  N'Đặng Nhật',   'MARRIED', N'Accounting'),
        ('EMP110', N'Tâm',  N'Võ Thanh',    'MARRIED', N'Finance'),
        ('EMP111', N'Huy',  N'Phan Đức',    'SINGLE',  N'Auditing')
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
        ('EMP100', N'Ngõ 81 Trần Cung',      N'Hà Nội'),
        ('EMP101', N'16 Nguyễn Chí Thanh',   N'Hà Nội'),
        ('EMP102', N'41 Lê Đức Thọ',         N'Hà Nội'),
        ('EMP103', N'88 Trần Bình',          N'Hà Nội'),
        ('EMP104', N'124 Hồ Tùng Mậu',       N'Hà Nội'),
        ('EMP105', N'52 Xuân Thuỷ',          N'Hà Nội'),
        ('EMP106', N'201 Nguyễn Hữu Cảnh',   N'Hồ Chí Minh'),
        ('EMP107', N'85 Nguyễn Thị Minh Khai',N'Hồ Chí Minh'),
        ('EMP108', N'142 Phan Xích Long',    N'Hồ Chí Minh'),
        ('EMP109', N'33 Điện Biên Phủ',      N'Hồ Chí Minh'),
        ('EMP110', N'61 Cộng Hoà',           N'Hồ Chí Minh'),
        ('EMP111', N'17 Lê Văn Sỹ',          N'Hồ Chí Minh')
    ) AS s(employee_code, address_line, province_name)
)
INSERT INTO dbo.hr_employee_address (
    employee_id, address_type, address_line, province_name, country_name, is_primary
)
SELECT e.employee_id, 'CURRENT', s.address_line, s.province_name, N'Vietnam', 1
FROM DemoAddress s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.hr_employee_address a
    WHERE a.employee_id = e.employee_id
      AND a.address_type = 'CURRENT'
      AND a.is_deleted = 0
);
GO

;WITH DemoIdentification AS (
    SELECT *
    FROM (VALUES
        ('EMP100', '079095001100'), ('EMP101', '079093001101'), ('EMP102', '079097001102'),
        ('EMP103', '079096001103'), ('EMP104', '079099001104'), ('EMP105', '079094001105'),
        ('EMP106', '079098001106'), ('EMP107', '079097001107'), ('EMP108', '079000001108'),
        ('EMP109', '079095001109'), ('EMP110', '079094001110'), ('EMP111', '079098001111')
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
        ('EMP100', N'Vietcombank', '970100100100'),
        ('EMP101', N'BIDV',        '970100100101'),
        ('EMP102', N'Techcombank', '970100100102'),
        ('EMP103', N'ACB',         '970100100103'),
        ('EMP104', N'MB Bank',     '970100100104'),
        ('EMP105', N'VietinBank',  '970100100105'),
        ('EMP106', N'Techcombank', '970100100106'),
        ('EMP107', N'ACB',         '970100100107'),
        ('EMP108', N'MB Bank',     '970100100108'),
        ('EMP109', N'Vietcombank', '970100100109'),
        ('EMP110', N'BIDV',        '970100100110'),
        ('EMP111', N'Techcombank', '970100100111')
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

-- 4. User accounts and primary employee role
DECLARE @DemoPasswordHash VARCHAR(255) = '$2a$10$gaSEymcGFJ74WopEYuob2OLUqXy3IZuTdmv5vDDWVYzVcp7DTic32';

;WITH DemoUsers AS (
    SELECT *
    FROM (VALUES
        ('EMP100', 'demo_emp100', 'minh.nguyen100@digitalhrm.com'),
        ('EMP101', 'demo_emp101', 'phuc.tran101@digitalhrm.com'),
        ('EMP102', 'demo_emp102', 'duong.le102@digitalhrm.com'),
        ('EMP103', 'demo_emp103', 'bao.pham103@digitalhrm.com'),
        ('EMP104', 'demo_emp104', 'vy.do104@digitalhrm.com'),
        ('EMP105', 'demo_emp105', 'nam.vu105@digitalhrm.com'),
        ('EMP106', 'demo_emp106', 'yen.bui106@digitalhrm.com'),
        ('EMP107', 'demo_emp107', 'khang.hoang107@digitalhrm.com'),
        ('EMP108', 'demo_emp108', 'linh.ngo108@digitalhrm.com'),
        ('EMP109', 'demo_emp109', 'anh.dang109@digitalhrm.com'),
        ('EMP110', 'demo_emp110', 'tam.vo110@digitalhrm.com'),
        ('EMP111', 'demo_emp111', 'huy.phan111@digitalhrm.com')
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
        target.password_hash = @DemoPasswordHash,
        target.status = 'ACTIVE',
        target.must_change_password = 0,
        target.is_deleted = 0,
        target.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (user_id, employee_id, username, password_hash, status, must_change_password, email, created_at, is_deleted)
    VALUES (NEWID(), source.employee_id, source.username, @DemoPasswordHash, 'ACTIVE', 0, source.email, SYSDATETIME(), 0);
GO

DECLARE @EmployeeRoleId UNIQUEIDENTIFIER = (SELECT role_id FROM dbo.sec_role WHERE role_code = 'EMPLOYEE');

INSERT INTO dbo.sec_user_role (user_id, role_id, is_primary_role, status, created_at)
SELECT u.user_id, @EmployeeRoleId, 1, 'ACTIVE', SYSDATETIME()
FROM dbo.sec_user_account u
WHERE u.username LIKE 'demo_emp1%'
  AND @EmployeeRoleId IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.sec_user_role ur
      WHERE ur.user_id = u.user_id
        AND ur.role_id = @EmployeeRoleId
  );
GO

-- 5. Contract and compensation dataset
DECLARE @TypeINDEF110 BIGINT = (SELECT contract_type_id FROM dbo.ct_contract_type WHERE contract_type_code = 'INDEF');
DECLARE @Type12M110 BIGINT = (SELECT contract_type_id FROM dbo.ct_contract_type WHERE contract_type_code = '12M');
DECLARE @AllowanceId110 BIGINT = (SELECT salary_component_id FROM dbo.pay_salary_component WHERE component_code = 'ALLOWANCE');

;WITH DemoContract AS (
    SELECT *
    FROM (VALUES
        ('EMP100', 'HDLD-EMP100-01', 'INDEF', CAST('2024-02-10' AS DATE), CAST('2024-02-12' AS DATE), CAST(NULL AS DATE), 24000000),
        ('EMP101', 'HDLD-EMP101-01', 'INDEF', CAST('2023-06-28' AS DATE), CAST('2023-07-01' AS DATE), CAST(NULL AS DATE), 26000000),
        ('EMP102', 'HDLD-EMP102-01', '12M',   CAST('2025-01-02' AS DATE), CAST('2025-01-06' AS DATE), CAST('2026-12-31' AS DATE), 22000000),
        ('EMP103', 'HDLD-EMP103-01', 'INDEF', CAST('2023-09-15' AS DATE), CAST('2023-09-18' AS DATE), CAST(NULL AS DATE), 32000000),
        ('EMP104', 'HDLD-EMP104-01', '12M',   CAST('2025-03-05' AS DATE), CAST('2025-03-10' AS DATE), CAST('2026-08-31' AS DATE), 18000000),
        ('EMP105', 'HDLD-EMP105-01', 'INDEF', CAST('2022-11-28' AS DATE), CAST('2022-12-01' AS DATE), CAST(NULL AS DATE), 34000000),
        ('EMP106', 'HDLD-EMP106-01', '12M',   CAST('2024-05-30' AS DATE), CAST('2024-06-03' AS DATE), CAST('2026-05-30' AS DATE), 19000000),
        ('EMP107', 'HDLD-EMP107-01', 'INDEF', CAST('2024-08-15' AS DATE), CAST('2024-08-19' AS DATE), CAST(NULL AS DATE), 21000000),
        ('EMP108', 'HDLD-EMP108-01', '12M',   CAST('2025-02-12' AS DATE), CAST('2025-02-17' AS DATE), CAST('2026-11-30' AS DATE), 17500000),
        ('EMP109', 'HDLD-EMP109-01', 'INDEF', CAST('2024-03-08' AS DATE), CAST('2024-03-11' AS DATE), CAST(NULL AS DATE), 23000000),
        ('EMP110', 'HDLD-EMP110-01', 'INDEF', CAST('2023-11-16' AS DATE), CAST('2023-11-20' AS DATE), CAST(NULL AS DATE), 24500000),
        ('EMP111', 'HDLD-EMP111-01', '12M',   CAST('2025-01-10' AS DATE), CAST('2025-01-13' AS DATE), CAST('2026-10-31' AS DATE), 22500000)
    ) AS s(employee_code, contract_number, contract_type_code, sign_date, effective_date, end_date, base_salary)
)
INSERT INTO dbo.ct_labor_contract (
    employee_id, contract_type_id, contract_number, sign_date, effective_date, end_date,
    job_title_id, org_unit_id, base_salary, salary_currency, working_type, contract_status
)
SELECT
    e.employee_id,
    CASE WHEN s.contract_type_code = '12M' THEN @Type12M110 ELSE @TypeINDEF110 END,
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
        ('EMP100', 24000000), ('EMP101', 26000000), ('EMP102', 22000000), ('EMP103', 32000000),
        ('EMP104', 18000000), ('EMP105', 34000000), ('EMP106', 19000000), ('EMP107', 21000000),
        ('EMP108', 17500000), ('EMP109', 23000000), ('EMP110', 24500000), ('EMP111', 22500000)
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
    'BC' + RIGHT(e.employee_code, 3) + '001',
    N'Vietcombank'
FROM DemoComp s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.pay_employee_compensation c WHERE c.employee_id = e.employee_id AND c.status = 'ACTIVE' AND c.is_deleted = 0
);
GO

DECLARE @AllowanceId110 BIGINT = (SELECT salary_component_id FROM dbo.pay_salary_component WHERE component_code = 'ALLOWANCE');

INSERT INTO dbo.pay_employee_compensation_item (employee_compensation_id, salary_component_id, amount_value)
SELECT c.employee_compensation_id, @AllowanceId110, 730000
FROM dbo.pay_employee_compensation c
INNER JOIN dbo.hr_employee e ON e.employee_id = c.employee_id
WHERE e.employee_code IN ('EMP100','EMP101','EMP102','EMP103','EMP104','EMP105','EMP106','EMP107','EMP108','EMP109','EMP110','EMP111')
  AND @AllowanceId110 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.pay_employee_compensation_item ci
      WHERE ci.employee_compensation_id = c.employee_compensation_id
        AND ci.salary_component_id = @AllowanceId110
  );
GO

-- 6. Leave balances and requests
DECLARE @ALTypeId110 BIGINT = (SELECT leave_type_id FROM dbo.lea_leave_type WHERE leave_type_code = 'AL');

;WITH DemoLeaveBalance AS (
    SELECT *
    FROM (VALUES
        ('EMP100', 10.0, 2.0), ('EMP101', 12.0, 0.0), ('EMP102', 11.0, 1.0), ('EMP103', 9.0, 3.0),
        ('EMP104', 12.0, 0.0), ('EMP105', 8.0, 4.0), ('EMP106', 11.0, 1.0), ('EMP107', 10.0, 2.0),
        ('EMP108', 12.0, 0.0), ('EMP109', 9.5, 2.5), ('EMP110', 11.0, 1.0), ('EMP111', 10.0, 2.0)
    ) AS s(employee_code, available_units, used_units)
)
INSERT INTO dbo.lea_leave_balance (
    employee_id, leave_type_id, leave_year, opening_units, accrued_units, used_units, available_units, balance_status
)
SELECT e.employee_id, @ALTypeId110, 2026, 0.0, 12.0, s.used_units, s.available_units, 'OPEN'
FROM DemoLeaveBalance s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE @ALTypeId110 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM dbo.lea_leave_balance b
      WHERE b.employee_id = e.employee_id
        AND b.leave_type_id = @ALTypeId110
        AND b.leave_year = 2026
  );
GO

DECLARE @ALTypeId110 BIGINT = (SELECT leave_type_id FROM dbo.lea_leave_type WHERE leave_type_code = 'AL');
DECLARE @ALRuleId110 BIGINT = (SELECT leave_type_rule_id FROM dbo.lea_leave_type_rule WHERE leave_type_id = @ALTypeId110 AND version_no = 1);
DECLARE @HrUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');

;WITH DemoLeaveRequest AS (
    SELECT *
    FROM (VALUES
        ('REQ-2026-DEMO-100', 'EMP100', CAST('2026-04-15' AS DATE), CAST('2026-04-15' AS DATE), 1.0, 'SUBMITTED', N'Nghỉ khám sức khỏe định kỳ.'),
        ('REQ-2026-DEMO-101', 'EMP101', CAST('2026-04-18' AS DATE), CAST('2026-04-18' AS DATE), 1.0, 'APPROVED',  N'Nghỉ xử lý việc cá nhân.'),
        ('REQ-2026-DEMO-102', 'EMP102', CAST('2026-04-22' AS DATE), CAST('2026-04-22' AS DATE), 1.0, 'REJECTED',  N'Nghỉ đi công việc gia đình.'),
        ('REQ-2026-DEMO-103', 'EMP103', CAST('2026-04-24' AS DATE), CAST('2026-04-25' AS DATE), 2.0, 'FINALIZED', N'Nghỉ phép năm sau sprint release.'),
        ('REQ-2026-DEMO-104', 'EMP104', CAST('2026-05-05' AS DATE), CAST('2026-05-05' AS DATE), 1.0, 'SUBMITTED', N'Nghỉ thi chứng chỉ chuyên môn.'),
        ('REQ-2026-DEMO-105', 'EMP105', CAST('2026-05-08' AS DATE), CAST('2026-05-09' AS DATE), 2.0, 'APPROVED',  N'Nghỉ du lịch cùng gia đình.'),
        ('REQ-2026-DEMO-106', 'EMP106', CAST('2026-04-16' AS DATE), CAST('2026-04-16' AS DATE), 1.0, 'SUBMITTED', N'Nghỉ gặp khách hàng ngoài tỉnh.'),
        ('REQ-2026-DEMO-107', 'EMP107', CAST('2026-04-19' AS DATE), CAST('2026-04-19' AS DATE), 1.0, 'APPROVED',  N'Nghỉ việc riêng đã báo trước.'),
        ('REQ-2026-DEMO-108', 'EMP108', CAST('2026-04-29' AS DATE), CAST('2026-04-29' AS DATE), 1.0, 'REJECTED',  N'Nghỉ gấp do trùng lịch team.'),
        ('REQ-2026-DEMO-109', 'EMP109', CAST('2026-05-06' AS DATE), CAST('2026-05-06' AS DATE), 1.0, 'FINALIZED', N'Nghỉ làm thủ tục cá nhân.'),
        ('REQ-2026-DEMO-110', 'EMP110', CAST('2026-05-12' AS DATE), CAST('2026-05-12' AS DATE), 1.0, 'APPROVED',  N'Nghỉ chăm sóc gia đình.'),
        ('REQ-2026-DEMO-111', 'EMP111', CAST('2026-05-14' AS DATE), CAST('2026-05-15' AS DATE), 2.0, 'SUBMITTED', N'Nghỉ phép theo kế hoạch.')
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
    @ALTypeId110,
    @ALRuleId110,
    2026,
    s.start_date,
    s.end_date,
    s.requested_units,
    s.reason,
    'MANAGER',
    s.request_status,
    DATEADD(DAY, -2, SYSDATETIME()),
    CASE WHEN s.request_status = 'APPROVED' THEN DATEADD(DAY, -1, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'APPROVED' THEN @ManagerUser110 END,
    CASE WHEN s.request_status = 'REJECTED' THEN DATEADD(DAY, -1, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'REJECTED' THEN @ManagerUser110 END,
    CASE WHEN s.request_status = 'FINALIZED' THEN DATEADD(HOUR, -12, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'FINALIZED' THEN @HrUser110 END,
    CASE WHEN s.request_status = 'APPROVED' THEN N'Quản lý đã duyệt đơn nghỉ.' END,
    CASE WHEN s.request_status = 'REJECTED' THEN N'Trùng lịch nhân sự tối thiểu của bộ phận.' END,
    CASE WHEN s.request_status = 'FINALIZED' THEN N'HR đã chốt cân đối phép năm.' END
FROM DemoLeaveRequest s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE @ALTypeId110 IS NOT NULL
  AND @ALRuleId110 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM dbo.lea_leave_request r WHERE r.request_code = s.request_code
  );
GO

-- 7. Attendance periods, assignments, daily data, adjustments, OT
DECLARE @OfficeShift110 BIGINT = (SELECT shift_id FROM dbo.att_shift WHERE shift_code = 'OFFICE');
DECLARE @AttPeriodApr110 BIGINT = (SELECT attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-04');
DECLARE @AttPeriodMay110 BIGINT = (SELECT attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-05');

INSERT INTO dbo.att_shift_assignment (employee_id, shift_id, effective_from)
SELECT e.employee_id, @OfficeShift110, '2026-01-01'
FROM dbo.hr_employee e
WHERE e.employee_code IN ('EMP100','EMP101','EMP102','EMP103','EMP104','EMP105','EMP106','EMP107','EMP108','EMP109','EMP110','EMP111')
  AND @OfficeShift110 IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.att_shift_assignment a
      WHERE a.employee_id = e.employee_id
        AND a.shift_id = @OfficeShift110
        AND a.effective_from = '2026-01-01'
  );
GO

DECLARE @AttPeriodApr110 BIGINT = (SELECT attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-04');
DECLARE @AttPeriodMay110 BIGINT = (SELECT attendance_period_id FROM dbo.att_attendance_period WHERE period_code = '2026-05');

;WITH DemoDaily AS (
    SELECT *
    FROM (VALUES
        ('EMP100', CAST('2026-04-08' AS DATE), 'PRESENT',   CAST('2026-04-08 08:01:00' AS DATETIME2), CAST('2026-04-08 17:15:00' AS DATETIME2), 494, 0, 0, 0),
        ('EMP101', CAST('2026-04-08' AS DATE), 'PRESENT',   CAST('2026-04-08 08:14:00' AS DATETIME2), CAST('2026-04-08 17:28:00' AS DATETIME2), 494, 14, 1, 0),
        ('EMP102', CAST('2026-04-08' AS DATE), 'ABSENT',    CAST(NULL AS DATETIME2),                    CAST(NULL AS DATETIME2),                    0,   0, 1, 0),
        ('EMP103', CAST('2026-04-08' AS DATE), 'PRESENT',   CAST('2026-04-08 08:04:00' AS DATETIME2), CAST('2026-04-08 17:42:00' AS DATETIME2), 518, 0, 0, 0),
        ('EMP104', CAST('2026-04-08' AS DATE), 'INCOMPLETE',CAST('2026-04-08 08:09:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    0,   0, 1, 1),
        ('EMP105', CAST('2026-04-08' AS DATE), 'PRESENT',   CAST('2026-04-08 07:57:00' AS DATETIME2), CAST('2026-04-08 18:05:00' AS DATETIME2), 548, 0, 0, 0),
        ('EMP106', CAST('2026-04-09' AS DATE), 'PRESENT',   CAST('2026-04-09 08:03:00' AS DATETIME2), CAST('2026-04-09 17:18:00' AS DATETIME2), 495, 0, 0, 0),
        ('EMP107', CAST('2026-04-09' AS DATE), 'PRESENT',   CAST('2026-04-09 08:18:00' AS DATETIME2), CAST('2026-04-09 17:39:00' AS DATETIME2), 501, 18, 1, 0),
        ('EMP108', CAST('2026-04-09' AS DATE), 'ON_LEAVE',  CAST(NULL AS DATETIME2),                    CAST(NULL AS DATETIME2),                    0,   0, 0, 1),
        ('EMP109', CAST('2026-04-09' AS DATE), 'ABSENT',    CAST(NULL AS DATETIME2),                    CAST(NULL AS DATETIME2),                    0,   0, 1, 0),
        ('EMP110', CAST('2026-04-09' AS DATE), 'PRESENT',   CAST('2026-04-09 08:06:00' AS DATETIME2), CAST('2026-04-09 17:25:00' AS DATETIME2), 499, 0, 0, 0),
        ('EMP111', CAST('2026-04-09' AS DATE), 'PRESENT',   CAST('2026-04-09 07:59:00' AS DATETIME2), CAST('2026-04-09 18:10:00' AS DATETIME2), 551, 0, 0, 0)
    ) AS s(employee_code, attendance_date, daily_status, check_in_at, check_out_at, worked_minutes, late_minutes, anomaly_count, on_leave)
)
INSERT INTO dbo.att_daily_attendance (
    employee_id, attendance_date, attendance_period_id, actual_check_in_at, actual_check_out_at,
    worked_minutes, late_minutes, anomaly_count, missing_check_out, on_leave, daily_status
)
SELECT
    e.employee_id,
    s.attendance_date,
    CASE WHEN MONTH(s.attendance_date) = 4 THEN @AttPeriodApr110 ELSE @AttPeriodMay110 END,
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
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.att_daily_attendance d
    WHERE d.employee_id = e.employee_id
      AND d.attendance_date = s.attendance_date
);
GO

;WITH DemoLogs AS (
    SELECT *
    FROM (VALUES
        ('EMP100', CAST('2026-04-08' AS DATE), 'CHECK_IN',  CAST('2026-04-08 08:01:00' AS DATETIME2), 'WEB'),
        ('EMP100', CAST('2026-04-08' AS DATE), 'CHECK_OUT', CAST('2026-04-08 17:15:00' AS DATETIME2), 'WEB'),
        ('EMP101', CAST('2026-04-08' AS DATE), 'CHECK_IN',  CAST('2026-04-08 08:14:00' AS DATETIME2), 'MOBILE_APP'),
        ('EMP101', CAST('2026-04-08' AS DATE), 'CHECK_OUT', CAST('2026-04-08 17:28:00' AS DATETIME2), 'MOBILE_APP'),
        ('EMP103', CAST('2026-04-08' AS DATE), 'CHECK_IN',  CAST('2026-04-08 08:04:00' AS DATETIME2), 'WEB'),
        ('EMP103', CAST('2026-04-08' AS DATE), 'CHECK_OUT', CAST('2026-04-08 17:42:00' AS DATETIME2), 'WEB'),
        ('EMP104', CAST('2026-04-08' AS DATE), 'CHECK_IN',  CAST('2026-04-08 08:09:00' AS DATETIME2), 'WEB'),
        ('EMP105', CAST('2026-04-08' AS DATE), 'CHECK_IN',  CAST('2026-04-08 07:57:00' AS DATETIME2), 'WEB'),
        ('EMP105', CAST('2026-04-08' AS DATE), 'CHECK_OUT', CAST('2026-04-08 18:05:00' AS DATETIME2), 'WEB'),
        ('EMP106', CAST('2026-04-09' AS DATE), 'CHECK_IN',  CAST('2026-04-09 08:03:00' AS DATETIME2), 'WEB'),
        ('EMP106', CAST('2026-04-09' AS DATE), 'CHECK_OUT', CAST('2026-04-09 17:18:00' AS DATETIME2), 'WEB'),
        ('EMP107', CAST('2026-04-09' AS DATE), 'CHECK_IN',  CAST('2026-04-09 08:18:00' AS DATETIME2), 'MOBILE_APP'),
        ('EMP107', CAST('2026-04-09' AS DATE), 'CHECK_OUT', CAST('2026-04-09 17:39:00' AS DATETIME2), 'MOBILE_APP'),
        ('EMP110', CAST('2026-04-09' AS DATE), 'CHECK_IN',  CAST('2026-04-09 08:06:00' AS DATETIME2), 'WEB'),
        ('EMP110', CAST('2026-04-09' AS DATE), 'CHECK_OUT', CAST('2026-04-09 17:25:00' AS DATETIME2), 'WEB'),
        ('EMP111', CAST('2026-04-09' AS DATE), 'CHECK_IN',  CAST('2026-04-09 07:59:00' AS DATETIME2), 'WEB'),
        ('EMP111', CAST('2026-04-09' AS DATE), 'CHECK_OUT', CAST('2026-04-09 18:10:00' AS DATETIME2), 'WEB')
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

DECLARE @ManagerUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');

;WITH DemoAdjustments AS (
    SELECT *
    FROM (VALUES
        ('ADJ-DEMO-100', 'EMP100', CAST('2026-04-08' AS DATE), 'WRONG_TIME',        CAST('2026-04-08 08:00:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'SUBMITTED', N'Xin điều chỉnh giờ check-in do lỗi kiosk.'),
        ('ADJ-DEMO-101', 'EMP101', CAST('2026-04-08' AS DATE), 'MISSING_CHECK_OUT', CAST(NULL AS DATETIME2),                    CAST('2026-04-08 17:35:00' AS DATETIME2), 'APPROVED',  N'Quên checkout do họp cuối ngày.'),
        ('ADJ-DEMO-102', 'EMP102', CAST('2026-04-08' AS DATE), 'MISSING_BOTH',      CAST('2026-04-08 08:05:00' AS DATETIME2), CAST('2026-04-08 17:00:00' AS DATETIME2), 'REJECTED',  N'Trình bày bổ sung log khi vào công ty.'),
        ('ADJ-DEMO-103', 'EMP103', CAST('2026-04-08' AS DATE), 'WRONG_TIME',        CAST('2026-04-08 08:02:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'SUBMITTED', N'App mobile ghi sai thời gian.'),
        ('ADJ-DEMO-104', 'EMP104', CAST('2026-04-08' AS DATE), 'MISSING_CHECK_OUT', CAST(NULL AS DATETIME2),                    CAST('2026-04-08 17:12:00' AS DATETIME2), 'SUBMITTED', N'Quên thao tác checkout.'),
        ('ADJ-DEMO-105', 'EMP105', CAST('2026-04-08' AS DATE), 'MISSING_CHECK_IN',  CAST('2026-04-08 07:58:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'APPROVED',  N'Đã check-in nhưng thiết bị không lưu.'),
        ('ADJ-DEMO-106', 'EMP106', CAST('2026-04-09' AS DATE), 'WRONG_TIME',        CAST('2026-04-09 08:00:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'SUBMITTED', N'Lệch 3 phút với máy chấm công.'),
        ('ADJ-DEMO-107', 'EMP107', CAST('2026-04-09' AS DATE), 'MISSING_CHECK_OUT', CAST(NULL AS DATETIME2),                    CAST('2026-04-09 17:42:00' AS DATETIME2), 'APPROVED',  N'Checkout sau khi hỗ trợ khách hàng.'),
        ('ADJ-DEMO-108', 'EMP108', CAST('2026-04-09' AS DATE), 'OTHER',             CAST('2026-04-09 08:00:00' AS DATETIME2), CAST('2026-04-09 17:00:00' AS DATETIME2), 'REJECTED',  N'Ngày này đang nghỉ phép nhưng cần bổ sung ghi chú.'),
        ('ADJ-DEMO-109', 'EMP109', CAST('2026-04-09' AS DATE), 'MISSING_BOTH',      CAST('2026-04-09 08:07:00' AS DATETIME2), CAST('2026-04-09 16:58:00' AS DATETIME2), 'SUBMITTED', N'Quên thao tác cả đầu và cuối ca.'),
        ('ADJ-DEMO-110', 'EMP110', CAST('2026-04-09' AS DATE), 'WRONG_TIME',        CAST('2026-04-09 08:05:00' AS DATETIME2), CAST(NULL AS DATETIME2),                    'APPROVED',  N'Kiosk ghi muộn hơn thực tế.'),
        ('ADJ-DEMO-111', 'EMP111', CAST('2026-04-09' AS DATE), 'MISSING_CHECK_OUT', CAST(NULL AS DATETIME2),                    CAST('2026-04-09 18:11:00' AS DATETIME2), 'SUBMITTED', N'Quên checkout sau khi OT.')
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
    CASE WHEN s.request_status = 'APPROVED' THEN @ManagerUser110 END,
    CASE WHEN s.request_status = 'REJECTED' THEN DATEADD(HOUR, -12, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'REJECTED' THEN @ManagerUser110 END,
    CASE WHEN s.request_status = 'APPROVED' THEN N'Quản lý đã xác thực dữ liệu đối chiếu.' END,
    CASE WHEN s.request_status = 'REJECTED' THEN N'Không đủ căn cứ xác nhận.' END
FROM DemoAdjustments s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.att_adjustment_request r WHERE r.request_code = s.request_code
);
GO

DECLARE @ManagerUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');

;WITH DemoOT AS (
    SELECT *
    FROM (VALUES
        ('OT-DEMO-100', 'EMP100', CAST('2026-04-15' AS DATE), CAST('2026-04-15 17:30:00' AS DATETIME2), CAST('2026-04-15 19:00:00' AS DATETIME2),  90, 'SUBMITTED', N'Hoàn tất báo cáo nhân sự quý.'),
        ('OT-DEMO-101', 'EMP101', CAST('2026-04-16' AS DATE), CAST('2026-04-16 17:30:00' AS DATETIME2), CAST('2026-04-16 19:30:00' AS DATETIME2), 120, 'APPROVED',  N'Chuẩn bị workshop nội bộ.'),
        ('OT-DEMO-102', 'EMP102', CAST('2026-04-17' AS DATE), CAST('2026-04-17 17:45:00' AS DATETIME2), CAST('2026-04-17 19:00:00' AS DATETIME2),  75, 'REJECTED',  N'OT chưa nằm trong kế hoạch.'),
        ('OT-DEMO-103', 'EMP103', CAST('2026-04-18' AS DATE), CAST('2026-04-18 18:00:00' AS DATETIME2), CAST('2026-04-18 21:00:00' AS DATETIME2), 180, 'SUBMITTED', N'Fix issue production.'),
        ('OT-DEMO-104', 'EMP104', CAST('2026-04-19' AS DATE), CAST('2026-04-19 17:30:00' AS DATETIME2), CAST('2026-04-19 19:00:00' AS DATETIME2),  90, 'APPROVED',  N'Hoàn tất tài liệu bàn giao.'),
        ('OT-DEMO-105', 'EMP105', CAST('2026-04-20' AS DATE), CAST('2026-04-20 18:15:00' AS DATETIME2), CAST('2026-04-20 20:45:00' AS DATETIME2), 150, 'SUBMITTED', N'Support release team.'),
        ('OT-DEMO-106', 'EMP106', CAST('2026-04-21' AS DATE), CAST('2026-04-21 17:30:00' AS DATETIME2), CAST('2026-04-21 20:00:00' AS DATETIME2), 150, 'SUBMITTED', N'Chuẩn bị proposal khách hàng.'),
        ('OT-DEMO-107', 'EMP107', CAST('2026-04-22' AS DATE), CAST('2026-04-22 17:45:00' AS DATETIME2), CAST('2026-04-22 20:15:00' AS DATETIME2), 150, 'APPROVED',  N'Chốt báo giá khu vực miền Nam.'),
        ('OT-DEMO-108', 'EMP108', CAST('2026-04-23' AS DATE), CAST('2026-04-23 17:30:00' AS DATETIME2), CAST('2026-04-23 18:45:00' AS DATETIME2),  75, 'REJECTED',  N'Không cần thiết tăng ca trong ngày phép.'),
        ('OT-DEMO-109', 'EMP109', CAST('2026-04-24' AS DATE), CAST('2026-04-24 17:30:00' AS DATETIME2), CAST('2026-04-24 19:30:00' AS DATETIME2), 120, 'SUBMITTED', N'Đối chiếu công nợ cuối tuần.'),
        ('OT-DEMO-110', 'EMP110', CAST('2026-04-25' AS DATE), CAST('2026-04-25 17:30:00' AS DATETIME2), CAST('2026-04-25 18:45:00' AS DATETIME2),  75, 'APPROVED',  N'Khóa số liệu kế toán.'),
        ('OT-DEMO-111', 'EMP111', CAST('2026-04-26' AS DATE), CAST('2026-04-26 17:30:00' AS DATETIME2), CAST('2026-04-26 19:00:00' AS DATETIME2),  90, 'SUBMITTED', N'Hoàn thiện chứng từ thanh toán.')
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
    CASE WHEN s.request_status = 'APPROVED' THEN DATEADD(HOUR, -10, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'APPROVED' THEN @ManagerUser110 END,
    CASE WHEN s.request_status = 'REJECTED' THEN DATEADD(HOUR, -10, SYSDATETIME()) END,
    CASE WHEN s.request_status = 'REJECTED' THEN @ManagerUser110 END,
    CASE WHEN s.request_status = 'APPROVED' THEN N'OT nằm trong kế hoạch của bộ phận.' END,
    CASE WHEN s.request_status = 'REJECTED' THEN N'Yêu cầu chưa đủ căn cứ kinh doanh.' END
FROM DemoOT s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.att_overtime_request r WHERE r.request_code = s.request_code
);
GO

-- 8. Payroll items and payroll lines for dense demo dataset
DECLARE @PayrollPeriodApr110 BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-04');
DECLARE @PayrollPeriodMay110 BIGINT = (SELECT payroll_period_id FROM dbo.pay_payroll_period WHERE period_code = 'PR-2026-05');
DECLARE @HrUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');

;WITH DemoPayroll AS (
    SELECT *
    FROM (VALUES
        ('PR-2026-04', 'EMP100', 22, 21, 1, 0, 0, 24000000, 22909091, 730000, 1800000, 11000000, 0, 10839091, 676955, 23639091, 19162136, 'MANAGER_CONFIRMED'),
        ('PR-2026-04', 'EMP101', 22, 22, 0, 0, 0, 26000000, 26000000, 730000, 1950000, 11000000, 0, 13780000, 1102400, 26730000, 23677600, 'DRAFT'),
        ('PR-2026-04', 'EMP102', 22, 20, 1, 1, 0, 22000000, 20000000, 730000, 1650000, 11000000, 0, 8080000, 573000, 20730000, 18507000, 'DRAFT'),
        ('PR-2026-04', 'EMP103', 22, 22, 0, 0, 180, 32000000, 32000000, 730000, 2400000, 11000000, 0, 19330000, 1933000, 32730000, 27997000, 'MANAGER_CONFIRMED'),
        ('PR-2026-04', 'EMP104', 22, 21, 0, 1, 90, 18000000, 17181818, 730000, 1350000, 11000000, 0, 5568818, 317788, 17911818, 16244030, 'DRAFT'),
        ('PR-2026-04', 'EMP105', 22, 22, 0, 0, 150, 34000000, 34000000, 730000, 2550000, 11000000, 0, 21180000, 2329800, 34730000, 29850200, 'MANAGER_CONFIRMED'),
        ('PR-2026-05', 'EMP106', 22, 21, 1, 0, 150, 19000000, 18136364, 730000, 1425000, 11000000, 0, 6441364, 402585, 18866364, 17038779, 'PUBLISHED'),
        ('PR-2026-05', 'EMP107', 22, 22, 0, 0, 150, 21000000, 21000000, 730000, 1575000, 11000000, 0, 9155000, 732400, 21730000, 19422500, 'PUBLISHED'),
        ('PR-2026-05', 'EMP108', 22, 20, 1, 1, 0, 17500000, 15909091, 730000, 1312500, 11000000, 0, 4326591, 173064, 16639091, 15153527, 'MANAGER_CONFIRMED'),
        ('PR-2026-05', 'EMP109', 22, 21, 1, 0, 120, 23000000, 21954545, 730000, 1725000, 11000000, 0, 9959545, 796764, 22684545, 20162781, 'PUBLISHED'),
        ('PR-2026-05', 'EMP110', 22, 22, 0, 0, 75, 24500000, 24500000, 730000, 1837500, 11000000, 0, 12392500, 1115325, 25230000, 22277175, 'PUBLISHED'),
        ('PR-2026-05', 'EMP111', 22, 21, 1, 0, 90, 22500000, 21477273, 730000, 1687500, 11000000, 0, 9519773, 761582, 22207273, 19758191, 'MANAGER_CONFIRMED')
    ) AS s(period_code, employee_code, scheduled_day_count, present_day_count, paid_leave_day_count, unpaid_leave_day_count, approved_ot_minutes, base_salary_monthly, base_salary_prorated, fixed_earning_total, employee_insurance_amount, personal_deduction_amount, dependent_deduction_amount, taxable_income, pit_amount, gross_income, net_pay, item_status)
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
    CASE WHEN s.period_code = 'PR-2026-04' THEN @PayrollPeriodApr110 ELSE @PayrollPeriodMay110 END,
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
    CASE WHEN s.item_status IN ('MANAGER_CONFIRMED','PUBLISHED') THEN @ManagerUser110 END,
    CASE WHEN s.item_status IN ('MANAGER_CONFIRMED','PUBLISHED') THEN DATEADD(HOUR, -8, SYSDATETIME()) END,
    CASE WHEN s.item_status IN ('MANAGER_CONFIRMED','PUBLISHED') THEN N'Demo confirm by manager for CI/CD.' END,
    CASE WHEN s.item_status = 'PUBLISHED' THEN @HrUser110 END,
    CASE WHEN s.item_status = 'PUBLISHED' THEN DATEADD(HOUR, -4, SYSDATETIME()) END,
    CASE WHEN s.item_status = 'PUBLISHED' THEN @HrUser110 END,
    CASE WHEN s.item_status = 'PUBLISHED' THEN DATEADD(HOUR, -2, SYSDATETIME()) END
FROM DemoPayroll s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.pay_payroll_item i
    WHERE i.payroll_period_id = CASE WHEN s.period_code = 'PR-2026-04' THEN @PayrollPeriodApr110 ELSE @PayrollPeriodMay110 END
      AND i.employee_id = e.employee_id
);
GO

INSERT INTO dbo.pay_payroll_item_line (
    payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note
)
SELECT i.payroll_item_id, 'BASE_SALARY', N'Lương cơ bản', 'EARNING', 'SYSTEM', i.base_salary_prorated, 1, 10, N'Demo CI/CD line.'
FROM dbo.pay_payroll_item i
INNER JOIN dbo.hr_employee e ON e.employee_id = i.employee_id
WHERE e.employee_code IN ('EMP100','EMP101','EMP102','EMP103','EMP104','EMP105','EMP106','EMP107','EMP108','EMP109','EMP110','EMP111')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.pay_payroll_item_line l WHERE l.payroll_item_id = i.payroll_item_id AND l.component_code = 'BASE_SALARY'
  );

INSERT INTO dbo.pay_payroll_item_line (
    payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note
)
SELECT i.payroll_item_id, 'ALLOWANCE', N'Phụ cấp cố định', 'EARNING', 'CONFIGURED', i.fixed_earning_total, 0, 20, N'Khoản phụ cấp mẫu.'
FROM dbo.pay_payroll_item i
INNER JOIN dbo.hr_employee e ON e.employee_id = i.employee_id
WHERE e.employee_code IN ('EMP100','EMP101','EMP102','EMP103','EMP104','EMP105','EMP106','EMP107','EMP108','EMP109','EMP110','EMP111')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.pay_payroll_item_line l WHERE l.payroll_item_id = i.payroll_item_id AND l.component_code = 'ALLOWANCE'
  );

INSERT INTO dbo.pay_payroll_item_line (
    payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note
)
SELECT i.payroll_item_id, 'INSURANCE', N'BHXH, BHYT, BHTN', 'DEDUCTION', 'SYSTEM', i.employee_insurance_amount, 0, 30, N'Khấu trừ bảo hiểm mẫu.'
FROM dbo.pay_payroll_item i
INNER JOIN dbo.hr_employee e ON e.employee_id = i.employee_id
WHERE e.employee_code IN ('EMP100','EMP101','EMP102','EMP103','EMP104','EMP105','EMP106','EMP107','EMP108','EMP109','EMP110','EMP111')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.pay_payroll_item_line l WHERE l.payroll_item_id = i.payroll_item_id AND l.component_code = 'INSURANCE'
  );

INSERT INTO dbo.pay_payroll_item_line (
    payroll_item_id, component_code, component_name, component_category, line_source_type, line_amount, taxable, display_order, line_note
)
SELECT i.payroll_item_id, 'PIT', N'Thuế TNCN', 'DEDUCTION', 'SYSTEM', i.pit_amount, 0, 40, N'Khấu trừ PIT mẫu.'
FROM dbo.pay_payroll_item i
INNER JOIN dbo.hr_employee e ON e.employee_id = i.employee_id
WHERE e.employee_code IN ('EMP100','EMP101','EMP102','EMP103','EMP104','EMP105','EMP106','EMP107','EMP108','EMP109','EMP110','EMP111')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.pay_payroll_item_line l WHERE l.payroll_item_id = i.payroll_item_id AND l.component_code = 'PIT'
  );
GO

-- 9. Profile change requests and profile timeline
DECLARE @AdminUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');
DECLARE @HrUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

;WITH DemoProfileChange AS (
    SELECT *
    FROM (VALUES
        ('EMP100', 'PENDING',   N'{"mobilePhone":"0908000100","personalEmail":"minh.personal100@gmail.com"}'),
        ('EMP101', 'APPROVED',  N'{"addressLine":"92 Nguyễn Chí Thanh","provinceName":"Hà Nội"}'),
        ('EMP102', 'REJECTED',  N'{"maritalStatus":"MARRIED"}'),
        ('EMP103', 'PENDING',   N'{"mobilePhone":"0908000103"}'),
        ('EMP104', 'APPROVED',  N'{"personalEmail":"vy.private104@gmail.com"}'),
        ('EMP105', 'REJECTED',  N'{"addressLine":"101 Cầu Giấy","provinceName":"Hà Nội"}'),
        ('EMP106', 'PENDING',   N'{"mobilePhone":"0908000106"}'),
        ('EMP107', 'APPROVED',  N'{"personalEmail":"khang.private107@gmail.com"}'),
        ('EMP108', 'PENDING',   N'{"addressLine":"77 Phan Xích Long","provinceName":"Hồ Chí Minh"}'),
        ('EMP109', 'REJECTED',  N'{"mobilePhone":"0908000109"}'),
        ('EMP110', 'APPROVED',  N'{"personalEmail":"tam.private110@gmail.com"}'),
        ('EMP111', 'PENDING',   N'{"addressLine":"88 Lê Văn Sỹ","provinceName":"Hồ Chí Minh"}')
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
    DATEADD(DAY, -2, SYSDATETIME()),
    CASE WHEN s.request_status IN ('APPROVED','REJECTED') THEN DATEADD(DAY, -1, SYSDATETIME()) END,
    CASE WHEN s.request_status IN ('APPROVED','REJECTED') THEN @HrUser110 END,
    CASE
        WHEN s.request_status = 'APPROVED' THEN N'HR đã duyệt yêu cầu cập nhật.'
        WHEN s.request_status = 'REJECTED' THEN N'Hồ sơ cần bổ sung minh chứng.'
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
      AND r.is_deleted = 0
);
GO

DECLARE @AdminUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');

INSERT INTO dbo.hr_employee_profile_timeline (
    employee_id, event_type, summary, detail_json, actor_user_id, event_at
)
SELECT e.employee_id, 'PROFILE_UPDATED', N'Cập nhật hồ sơ demo', N'{"source":"R__110__seed_ci_demo_dataset"}', @AdminUser110, DATEADD(DAY, -1, SYSDATETIME())
FROM dbo.hr_employee e
WHERE e.employee_code IN ('EMP100','EMP101','EMP102','EMP103','EMP104','EMP105','EMP106','EMP107','EMP108','EMP109','EMP110','EMP111')
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.hr_employee_profile_timeline t
      WHERE t.employee_id = e.employee_id
        AND t.event_type = 'PROFILE_UPDATED'
        AND t.summary = N'Cập nhật hồ sơ demo'
  );
GO

-- 10. Onboarding records with checklist/documents/assets
DECLARE @Emp004 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP004');
DECLARE @Emp006 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP006');
DECLARE @Emp007 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP007');
DECLARE @Emp008 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP008');
DECLARE @Emp109 BIGINT = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP109');
DECLARE @HrUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');
DECLARE @DeptHr110 BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_HR_HN');
DECLARE @DeptIt110 BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_IT_HN');
DECLARE @DeptSales110 BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_SALES_HCM');
DECLARE @DeptFin110 BIGINT = (SELECT org_unit_id FROM dbo.hr_org_unit WHERE org_unit_code = 'DEPT_FIN_HCM');
DECLARE @TitleMgrHr110 BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'MGR_HR');
DECLARE @TitleJrDev110 BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'JDR_DEV');
DECLARE @TitleSrDev110 BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'SDR_DEV');
DECLARE @TitleExecSales110 BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'EXEC_SALES');
DECLARE @TitleAcc110 BIGINT = (SELECT job_title_id FROM dbo.hr_job_title WHERE job_title_code = 'SSR_ACC');
DECLARE @UserEmp109Onb UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = @Emp109);

;WITH DemoOnboarding AS (
    SELECT *
    FROM (VALUES
        ('ONB-2026-100', N'Nguyễn Gia Hân', 'giahan100@example.com', '0919000100', 'FEMALE', CAST('2000-01-10' AS DATE), CAST('2026-04-15' AS DATE), 'CAND100', @DeptHr110,   @TitleMgrHr110,  @Emp007, NULL,      NULL,        'DRAFT',         N'Hồ sơ mới chờ HR rà soát.'),
        ('ONB-2026-101', N'Trần Minh Đức', 'minhduc101@example.com', '0919000101', 'MALE',   CAST('1999-02-11' AS DATE), CAST('2026-04-16' AS DATE), 'CAND101', @DeptIt110,   @TitleJrDev110,  @Emp006, NULL,      NULL,        'IN_PROGRESS',   N'Đang chuẩn bị checklist nhập môn.'),
        ('ONB-2026-102', N'Lê Phương Anh', 'phuonganh102@example.com','0919000102','FEMALE', CAST('1998-03-12' AS DATE), CAST('2026-04-17' AS DATE), 'CAND102', @DeptIt110,   @TitleSrDev110,  @Emp006, NULL,      NULL,        'READY_FOR_JOIN',N'Đã sẵn sàng cho ngày đầu tiên.'),
        ('ONB-2026-103', N'Đỗ Hải Nam',    'hainam103@example.com',  '0919000103', 'MALE',   CAST('1997-04-13' AS DATE), CAST('2026-04-18' AS DATE), 'CAND103', @DeptSales110,@TitleExecSales110,@Emp008, NULL,      NULL,        'DRAFT',         N'Chờ manager xác nhận lịch orientation.'),
        ('ONB-2026-104', N'Vũ Thảo My',    'thaomy104@example.com',  '0919000104', 'FEMALE', CAST('1999-05-14' AS DATE), CAST('2026-04-19' AS DATE), 'CAND104', @DeptSales110,@TitleExecSales110,@Emp008, NULL,      NULL,        'IN_PROGRESS',   N'Đã gửi email welcome.'),
        ('ONB-2026-105', N'Phan Khôi Nguyên','khoinguyen105@example.com','0919000105','MALE',CAST('1998-06-15' AS DATE), CAST('2026-04-20' AS DATE), 'CAND105', @DeptFin110,  @TitleAcc110,    @Emp004, NULL,      NULL,        'READY_FOR_JOIN',N'Chờ tạo tài khoản hệ thống.'),
        ('ONB-2026-106', N'Bùi Kim Oanh',  'kimoanh106@example.com', '0919000106', 'FEMALE', CAST('1997-07-16' AS DATE), CAST('2026-04-21' AS DATE), 'CAND106', @DeptHr110,   @TitleMgrHr110,  @Emp007, NULL,      NULL,        'DRAFT',         N'Hồ sơ ứng viên đã xác nhận thông tin.'),
        ('ONB-2026-107', N'Ngô Đức Tài',   'ductai107@example.com',  '0919000107', 'MALE',   CAST('1996-08-17' AS DATE), CAST('2026-04-22' AS DATE), 'CAND107', @DeptIt110,   @TitleJrDev110,  @Emp006, NULL,      NULL,        'IN_PROGRESS',   N'Đang chờ cấp thiết bị.'),
        ('ONB-2026-108', N'Hoàng Thu Trang','thutrang108@example.com','0919000108','FEMALE', CAST('1999-09-18' AS DATE), CAST('2026-04-23' AS DATE), 'CAND108', @DeptFin110,  @TitleAcc110,    @Emp004, @Emp109,   @UserEmp109Onb, 'COMPLETED', N'Đã onboard hoàn tất và liên kết nhân sự.'),
        ('ONB-2026-109', N'Tạ Nhật Long',  'nhatlong109@example.com','0919000109', 'MALE',   CAST('1998-10-19' AS DATE), CAST('2026-04-24' AS DATE), 'CAND109', @DeptSales110,@TitleExecSales110,@Emp008, NULL,      NULL,        'READY_FOR_JOIN',N'Hoàn tất hồ sơ chờ ngày nhận việc.')
    ) AS s(onboarding_code, full_name, personal_email, personal_phone, gender_code, date_of_birth, planned_start_date, employee_code, org_unit_id, job_title_id, manager_employee_id, linked_employee_id, linked_user_id, status, note)
)
INSERT INTO dbo.onb_onboarding (
    onboarding_code, full_name, personal_email, personal_phone, gender_code, date_of_birth,
    planned_start_date, employee_code, org_unit_id, job_title_id, manager_employee_id,
    work_location, linked_employee_id, linked_user_id, status, note,
    orientation_confirmed_at, orientation_confirmed_by, orientation_note,
    completed_at, completed_by, completed_note, created_by
)
SELECT
    s.onboarding_code, s.full_name, s.personal_email, s.personal_phone, s.gender_code, s.date_of_birth,
    s.planned_start_date, s.employee_code, s.org_unit_id, s.job_title_id, s.manager_employee_id,
    CASE WHEN s.org_unit_id IN (@DeptHr110, @DeptIt110) THEN N'Hanoi Office' ELSE N'HCMC Office' END,
    s.linked_employee_id, s.linked_user_id, s.status, s.note,
    CASE WHEN s.status IN ('READY_FOR_JOIN','COMPLETED') THEN DATEADD(DAY, -1, SYSDATETIME()) END,
    CASE WHEN s.status IN ('READY_FOR_JOIN','COMPLETED') THEN @ManagerUser110 END,
    CASE WHEN s.status IN ('READY_FOR_JOIN','COMPLETED') THEN N'Orientation đã được manager xác nhận.' END,
    CASE WHEN s.status = 'COMPLETED' THEN DATEADD(HOUR, -6, SYSDATETIME()) END,
    CASE WHEN s.status = 'COMPLETED' THEN @HrUser110 END,
    CASE WHEN s.status = 'COMPLETED' THEN N'Hoàn tất toàn bộ onboarding checklist.' END,
    @HrUser110
FROM DemoOnboarding s
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.onb_onboarding o WHERE o.onboarding_code = s.onboarding_code
);
GO

DECLARE @HrUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

INSERT INTO dbo.onb_onboarding_checklist (
    onboarding_id, item_code, item_name, is_required, is_completed, due_date, completed_at, completed_by, note, created_by
)
SELECT o.onboarding_id, 'WELCOME', N'Gửi welcome email', 1,
       CASE WHEN o.status IN ('IN_PROGRESS','READY_FOR_JOIN','COMPLETED') THEN 1 ELSE 0 END,
       o.planned_start_date,
       CASE WHEN o.status IN ('IN_PROGRESS','READY_FOR_JOIN','COMPLETED') THEN DATEADD(DAY, -2, CAST(o.planned_start_date AS DATETIME2)) END,
       CASE WHEN o.status IN ('IN_PROGRESS','READY_FOR_JOIN','COMPLETED') THEN @HrUser110 END,
       N'Checklist demo cho onboarding.',
       @HrUser110
FROM dbo.onb_onboarding o
WHERE o.onboarding_code LIKE 'ONB-2026-10%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.onb_onboarding_checklist c WHERE c.onboarding_id = o.onboarding_id AND c.item_code = 'WELCOME'
  );

INSERT INTO dbo.onb_onboarding_checklist (
    onboarding_id, item_code, item_name, is_required, is_completed, due_date, completed_at, completed_by, note, created_by
)
SELECT o.onboarding_id, 'ASSET', N'Chuẩn bị thiết bị làm việc', 1,
       CASE WHEN o.status IN ('READY_FOR_JOIN','COMPLETED') THEN 1 ELSE 0 END,
       o.planned_start_date,
       CASE WHEN o.status IN ('READY_FOR_JOIN','COMPLETED') THEN DATEADD(DAY, -1, CAST(o.planned_start_date AS DATETIME2)) END,
       CASE WHEN o.status IN ('READY_FOR_JOIN','COMPLETED') THEN @HrUser110 END,
       N'Checklist demo cho onboarding.',
       @HrUser110
FROM dbo.onb_onboarding o
WHERE o.onboarding_code LIKE 'ONB-2026-10%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.onb_onboarding_checklist c WHERE c.onboarding_id = o.onboarding_id AND c.item_code = 'ASSET'
  );
GO

DECLARE @HrUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

INSERT INTO dbo.onb_onboarding_document (
    onboarding_id, document_name, document_category, storage_path, mime_type, file_size_bytes, status, note, created_by
)
SELECT o.onboarding_id, N'CV ứng viên', 'PROFILE', N'/seed/onboarding/' + o.onboarding_code + N'/cv.pdf', 'application/pdf', 204800, 'ACTIVE', N'Tài liệu demo.', @HrUser110
FROM dbo.onb_onboarding o
WHERE o.onboarding_code LIKE 'ONB-2026-10%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.onb_onboarding_document d WHERE d.onboarding_id = o.onboarding_id AND d.document_name = N'CV ứng viên'
  );
GO

DECLARE @HrUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

INSERT INTO dbo.onb_onboarding_asset (
    onboarding_id, asset_code, asset_name, asset_type, assigned_date, status, note, created_by
)
SELECT o.onboarding_id, 'AST-' + RIGHT(o.onboarding_code, 3), N'Laptop onboarding', 'LAPTOP',
       CASE WHEN o.status IN ('READY_FOR_JOIN','COMPLETED') THEN o.planned_start_date END,
       CASE WHEN o.status IN ('READY_FOR_JOIN','COMPLETED') THEN 'ASSIGNED' ELSE 'PLANNED' END,
       N'Tài sản demo cho onboarding.',
       @HrUser110
FROM dbo.onb_onboarding o
WHERE o.onboarding_code LIKE 'ONB-2026-10%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.onb_onboarding_asset a WHERE a.onboarding_id = o.onboarding_id AND a.asset_code = 'AST-' + RIGHT(o.onboarding_code, 3)
  );
GO

-- 11. Offboarding cases with checklist, assets, history
DECLARE @HrUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');
DECLARE @ManagerUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen_ndb');
DECLARE @UserEmp100 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP100'));
DECLARE @UserEmp101 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP101'));
DECLARE @UserEmp102 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP102'));
DECLARE @UserEmp103 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP103'));
DECLARE @UserEmp104 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP104'));
DECLARE @UserEmp105 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP105'));
DECLARE @UserEmp106 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP106'));
DECLARE @UserEmp107 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP107'));
DECLARE @UserEmp108 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP108'));
DECLARE @UserEmp109 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP109'));
DECLARE @UserEmp110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP110'));
DECLARE @UserEmp111 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = 'EMP111'));

;WITH DemoOffboarding AS (
    SELECT *
    FROM (VALUES
        ('OFF-2026-100', 'EMP100', @UserEmp100, CAST('2026-04-02' AS DATE), CAST('2026-04-25' AS DATE), 'REQUESTED',          N'Đề nghị nghỉ việc theo kế hoạch cá nhân.'),
        ('OFF-2026-101', 'EMP101', @UserEmp101, CAST('2026-04-01' AS DATE), CAST('2026-04-22' AS DATE), 'MANAGER_APPROVED',   N'Đã thống nhất bàn giao với team.'),
        ('OFF-2026-102', 'EMP102', @UserEmp102, CAST('2026-03-29' AS DATE), CAST('2026-04-18' AS DATE), 'MANAGER_REJECTED',   N'Tạm hoãn do thiếu nhân sự thay thế.'),
        ('OFF-2026-103', 'EMP103', @UserEmp103, CAST('2026-03-25' AS DATE), CAST('2026-04-15' AS DATE), 'HR_FINALIZED',      N'HR đã chốt ngày nghỉ việc.'),
        ('OFF-2026-104', 'EMP104', @UserEmp104, CAST('2026-03-20' AS DATE), CAST('2026-04-12' AS DATE), 'ACCESS_REVOKED',    N'Đã thu hồi quyền truy cập hệ thống.'),
        ('OFF-2026-105', 'EMP105', @UserEmp105, CAST('2026-03-18' AS DATE), CAST('2026-04-10' AS DATE), 'SETTLEMENT_PREPARED',N'Đang chuẩn bị settlement cuối cùng.'),
        ('OFF-2026-106', 'EMP106', @UserEmp106, CAST('2026-03-15' AS DATE), CAST('2026-04-09' AS DATE), 'CLOSED',             N'Đã hoàn tất quy trình thôi việc.'),
        ('OFF-2026-107', 'EMP107', @UserEmp107, CAST('2026-04-03' AS DATE), CAST('2026-04-28' AS DATE), 'REQUESTED',          N'Nghỉ việc để theo đuổi cơ hội mới.'),
        ('OFF-2026-108', 'EMP108', @UserEmp108, CAST('2026-04-04' AS DATE), CAST('2026-04-30' AS DATE), 'MANAGER_APPROVED',   N'Đã chốt người nhận bàn giao.'),
        ('OFF-2026-109', 'EMP109', @UserEmp109, CAST('2026-04-05' AS DATE), CAST('2026-05-03' AS DATE), 'HR_FINALIZED',      N'Offboarding đã chuyển qua HR finalize.')
    ) AS s(offboarding_code, employee_code, requested_by_user_id, request_date, requested_last_working_date, status, request_reason)
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
    s.requested_by_user_id,
    s.request_date,
    s.requested_last_working_date,
    s.request_reason,
    s.status,
    CASE WHEN s.status IN ('MANAGER_APPROVED','HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN @ManagerUser110 END,
    CASE WHEN s.status IN ('MANAGER_APPROVED','HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN DATEADD(DAY, -6, SYSDATETIME()) END,
    CASE WHEN s.status IN ('MANAGER_APPROVED','HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN N'Quản lý đã rà soát kế hoạch bàn giao.' END,
    CASE WHEN s.status IN ('HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN @HrUser110 END,
    CASE WHEN s.status IN ('HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN DATEADD(DAY, -4, SYSDATETIME()) END,
    CASE WHEN s.status IN ('HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN s.requested_last_working_date END,
    CASE WHEN s.status IN ('HR_FINALIZED','ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN N'HR đã chốt lịch nghỉ việc.' END,
    CASE WHEN s.status IN ('ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN @HrUser110 END,
    CASE WHEN s.status IN ('ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN DATEADD(DAY, -3, SYSDATETIME()) END,
    CASE WHEN s.status IN ('ACCESS_REVOKED','SETTLEMENT_PREPARED','CLOSED') THEN N'Đã khóa email và quyền truy cập.' END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN @HrUser110 END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN DATEADD(DAY, -2, SYSDATETIME()) END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN 2026 END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN 4 END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN 1.5 END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN 1500000 END,
    CASE WHEN s.status IN ('SETTLEMENT_PREPARED','CLOSED') THEN N'Đã chuẩn bị settlement demo.' END,
    CASE WHEN s.status = 'CLOSED' THEN @HrUser110 END,
    CASE WHEN s.status = 'CLOSED' THEN DATEADD(DAY, -1, SYSDATETIME()) END,
    CASE WHEN s.status = 'CLOSED' THEN N'Hoàn tất đóng hồ sơ offboarding demo.' END,
    s.requested_by_user_id
FROM DemoOffboarding s
INNER JOIN dbo.hr_employee e ON e.employee_code = s.employee_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.off_offboarding_case c WHERE c.offboarding_code = s.offboarding_code
);
GO

DECLARE @HrUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

INSERT INTO dbo.off_offboarding_checklist_item (
    offboarding_case_id, item_type, item_name, owner_role_code, due_date, status, note, created_by
)
SELECT c.offboarding_case_id, 'HANDOVER', N'Bàn giao công việc chính', 'MANAGER',
       c.requested_last_working_date,
       CASE WHEN c.status IN ('CLOSED','SETTLEMENT_PREPARED') THEN 'COMPLETED' WHEN c.status IN ('HR_FINALIZED','ACCESS_REVOKED') THEN 'IN_PROGRESS' ELSE 'OPEN' END,
       N'Checklist offboarding demo.',
       @HrUser110
FROM dbo.off_offboarding_case c
WHERE c.offboarding_code LIKE 'OFF-2026-10%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.off_offboarding_checklist_item i WHERE i.offboarding_case_id = c.offboarding_case_id AND i.item_name = N'Bàn giao công việc chính'
  );
GO

DECLARE @HrUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'quynh_ndd');

INSERT INTO dbo.off_offboarding_asset_return (
    offboarding_case_id, asset_code, asset_name, asset_type, expected_return_date, status, note, created_by
)
SELECT c.offboarding_case_id, 'AST-RET-' + RIGHT(c.offboarding_code, 3), N'Laptop làm việc', 'LAPTOP',
       c.requested_last_working_date,
       CASE WHEN c.status = 'CLOSED' THEN 'RETURNED' WHEN c.status IN ('ACCESS_REVOKED','SETTLEMENT_PREPARED') THEN 'PENDING' ELSE 'PENDING' END,
       N'Tài sản offboarding demo.',
       @HrUser110
FROM dbo.off_offboarding_case c
WHERE c.offboarding_code LIKE 'OFF-2026-10%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.off_offboarding_asset_return a WHERE a.offboarding_case_id = c.offboarding_case_id AND a.asset_code = 'AST-RET-' + RIGHT(c.offboarding_code, 3)
  );
GO

INSERT INTO dbo.off_offboarding_history (
    offboarding_case_id, from_status, to_status, action_code, action_note, changed_by, changed_at, created_by
)
SELECT c.offboarding_case_id, NULL, 'REQUESTED', 'REQUEST', N'Khởi tạo hồ sơ offboarding demo.', c.requested_by_user_id, DATEADD(DAY, -8, SYSDATETIME()), c.requested_by_user_id
FROM dbo.off_offboarding_case c
WHERE c.offboarding_code LIKE 'OFF-2026-10%'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.off_offboarding_history h WHERE h.offboarding_case_id = c.offboarding_case_id AND h.action_code = 'REQUEST'
  );
GO

-- 12. Portal inbox and tasks
DECLARE @AdminUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');

INSERT INTO dbo.por_portal_inbox_item (
    user_id, employee_id, item_type, category_code, title, message, related_module, related_entity_id,
    due_at, read_at, task_status, completed_at, status, created_by
)
SELECT u.user_id, e.employee_id, 'NOTIFICATION', 'PORTAL', N'Thông báo nhân sự',
       N'Hệ thống đã nạp dữ liệu demo phục vụ kiểm thử portal và CI/CD.',
       'PORTAL', e.employee_code, DATEADD(DAY, 3, SYSDATETIME()),
       NULL, 'OPEN', NULL, 'ACTIVE', @AdminUser110
FROM dbo.hr_employee e
INNER JOIN dbo.sec_user_account u ON u.employee_id = e.employee_id
WHERE e.employee_code IN ('EMP100','EMP101','EMP102','EMP103','EMP104','EMP105','EMP106','EMP107','EMP108','EMP109','EMP110','EMP111')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.por_portal_inbox_item i
      WHERE i.user_id = u.user_id
        AND i.title = N'Thông báo nhân sự'
        AND i.related_entity_id = e.employee_code
  );

INSERT INTO dbo.por_portal_inbox_item (
    user_id, employee_id, item_type, category_code, title, message, related_module, related_entity_id,
    due_at, read_at, task_status, completed_at, status, created_by
)
SELECT u.user_id, e.employee_id, 'TASK', 'SELF_SERVICE', N'Hoàn tất cập nhật hồ sơ cá nhân',
       N'Vui lòng kiểm tra email, số điện thoại và địa chỉ liên hệ trong portal.',
       'EMPLOYEE_PROFILE', e.employee_code, DATEADD(DAY, 5, SYSDATETIME()),
       CASE WHEN RIGHT(e.employee_code, 1) IN ('0','2','4','6','8') THEN DATEADD(HOUR, -6, SYSDATETIME()) END,
       CASE WHEN RIGHT(e.employee_code, 1) IN ('1','3','5') THEN 'DONE' ELSE 'OPEN' END,
       CASE WHEN RIGHT(e.employee_code, 1) IN ('1','3','5') THEN DATEADD(HOUR, -3, SYSDATETIME()) END,
       'ACTIVE', @AdminUser110
FROM dbo.hr_employee e
INNER JOIN dbo.sec_user_account u ON u.employee_id = e.employee_id
WHERE e.employee_code IN ('EMP100','EMP101','EMP102','EMP103','EMP104','EMP105','EMP106','EMP107','EMP108','EMP109','EMP110','EMP111')
  AND NOT EXISTS (
      SELECT 1 FROM dbo.por_portal_inbox_item i
      WHERE i.user_id = u.user_id
        AND i.title = N'Hoàn tất cập nhật hồ sơ cá nhân'
        AND i.related_entity_id = e.employee_code
  );
GO

-- 13. Audit logs for admin reporting and activity feed
DECLARE @AdminUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');

;WITH DemoAudit AS (
    SELECT *
    FROM (VALUES
        ('quynh_ndd', 'CREATE',          'EMPLOYEE',      'hr_employee',                       'EMP100', 'SUCCESS', N'Tạo hồ sơ nhân viên demo EMP100'),
        ('quynh_ndd', 'UPDATE',          'EMPLOYEE',      'hr_employee',                       'EMP101', 'SUCCESS', N'Cập nhật hồ sơ nhân viên demo EMP101'),
        ('nguyen',    'ASSIGN_MANAGER',  'ORG_UNIT',      'hr_org_unit',                       'DEPT_SALES_HCM', 'SUCCESS', N'Gán quản lý cho phòng Sales HCM'),
        ('quynh_ndd', 'CREATE',          'CONTRACT',      'ct_labor_contract',                 'HDLD-EMP100-01', 'SUCCESS', N'Tạo hợp đồng demo'),
        ('nguyen_ndb','APPROVE',         'LEAVE_REQUEST', 'lea_leave_request',                 'REQ-2026-DEMO-101', 'SUCCESS', N'Duyệt đơn nghỉ phép'),
        ('nguyen_ndb','REJECT',          'LEAVE_REQUEST', 'lea_leave_request',                 'REQ-2026-DEMO-102', 'SUCCESS', N'Từ chối đơn nghỉ phép'),
        ('nguyen_ndb','REVIEW',          'ATTENDANCE',    'att_adjustment_request',            'ADJ-DEMO-101', 'SUCCESS', N'Xử lý điều chỉnh công'),
        ('nguyen_ndb','REVIEW',          'ATTENDANCE',    'att_overtime_request',              'OT-DEMO-107', 'SUCCESS', N'Duyệt OT cho sales'),
        ('quynh_ndd', 'GENERATE',        'PAYROLL',       'pay_payroll_period',                'PR-2026-04', 'SUCCESS', N'Tạo bảng lương tháng 04'),
        ('quynh_ndd', 'APPROVE',         'PAYROLL',       'pay_payroll_period',                'PR-2026-05', 'SUCCESS', N'Duyệt kỳ lương tháng 05'),
        ('quynh_ndd', 'CREATE',          'ONBOARDING',    'onb_onboarding',                    'ONB-2026-100', 'SUCCESS', N'Tạo onboarding mới'),
        ('nguyen_ndb','CONFIRM',         'ONBOARDING',    'onb_onboarding',                    'ONB-2026-102', 'SUCCESS', N'Xác nhận orientation'),
        ('nguyen_ndb','APPROVE',         'OFFBOARDING',   'off_offboarding_case',              'OFF-2026-108', 'SUCCESS', N'Duyệt offboarding'),
        ('quynh_ndd', 'FINALIZE',        'OFFBOARDING',   'off_offboarding_case',              'OFF-2026-109', 'SUCCESS', N'HR finalize offboarding'),
        ('nguyen',    'LOGIN',           'AUTH',          'sec_user_account',                  'nguyen', 'SUCCESS', N'Đăng nhập quản trị viên'),
        ('nguyen',    'CREATE',          'USER',          'sec_user_account',                  'demo_emp100', 'SUCCESS', N'Tạo tài khoản demo_emp100'),
        ('nguyen',    'ASSIGN_ROLE',     'ROLE',          'sec_user_role',                     'demo_emp100', 'SUCCESS', N'Gán role EMPLOYEE'),
        ('ha_ln',     'SUBMIT_REQUEST',  'EMPLOYEE_PROFILE_REQUEST', 'hr_employee_profile_change_request', 'EMP009', 'SUCCESS', N'Nhân viên gửi yêu cầu cập nhật hồ sơ'),
        ('quynh_ndd', 'REVIEW_REQUEST',  'EMPLOYEE_PROFILE_REQUEST', 'hr_employee_profile_change_request', 'EMP104', 'SUCCESS', N'HR duyệt yêu cầu cập nhật hồ sơ'),
        ('nguyen',    'EXPORT',          'REPORT',        'rep_report_schedule_run',           'AUDIT_LOG', 'SUCCESS', N'Xuất báo cáo audit log'),
        ('nguyen',    'UPDATE',          'SYSTEM_CONFIG', 'sys_system_config',                 'attendance.ot.max_minutes_per_day', 'SUCCESS', N'Cập nhật cấu hình OT'),
        ('nguyen_ndb','VIEW_TEAM',       'REPORT_DASHBOARD','rep_team_dashboard',             'EMP008', 'SUCCESS', N'Manager xem team dashboard'),
        ('demo_emp106','CHECK_IN',       'PORTAL',        'att_attendance_log',                'EMP106', 'SUCCESS', N'Nhân viên portal check-in'),
        ('demo_emp107','REQUEST',        'OFFBOARDING',   'off_offboarding_case',              'OFF-2026-107', 'SUCCESS', N'Nhân viên gửi đơn nghỉ việc')
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
    'seed-' + LOWER(REPLACE(s.module_code, '_', '-')),
    NULL,
    N'{"seed":"R__110__seed_ci_demo_dataset"}',
    '127.0.0.1',
    N'Flyway Seed',
    DATEADD(MINUTE, -5, SYSDATETIME()),
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

-- 14. Report schedules and run history
DECLARE @AdminUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');
DECLARE @Now110 DATETIME2(0) = SYSDATETIME();

;WITH DemoSchedule AS (
    SELECT *
    FROM (VALUES
        ('SCH-ORG-MONTHLY',   N'Báo cáo biến động tổ chức',         'ORG_MOVEMENT',            'MONTHLY', NULL, 1,  8,  0, N'hr@digitalhrm.com,admin@digitalhrm.com'),
        ('SCH-CONTRACT-WEEK', N'Báo cáo hợp đồng sắp hết hạn',      'CONTRACT_EXPIRY',         'WEEKLY', 1,    NULL, 8, 30, N'hr@digitalhrm.com'),
        ('SCH-LEAVE-WEEK',    N'Báo cáo số dư phép',                'LEAVE_BALANCE',           'WEEKLY', 2,    NULL, 9,  0, N'hr@digitalhrm.com,manager@digitalhrm.com'),
        ('SCH-ATT-DAILY',     N'Báo cáo bất thường công & OT',      'ATTENDANCE_ANOMALY_OT',   'DAILY',  NULL, NULL, 7, 30, N'hr@digitalhrm.com'),
        ('SCH-PAY-MONTHLY',   N'Báo cáo tổng hợp bảng lương',       'PAYROLL_SUMMARY',         'MONTHLY', NULL, 3, 10, 0, N'finance@digitalhrm.com,hr@digitalhrm.com'),
        ('SCH-PIT-MONTHLY',   N'Báo cáo PIT',                       'PIT',                     'MONTHLY', NULL, 5, 10, 30, N'finance@digitalhrm.com'),
        ('SCH-LIFE-WEEKLY',   N'Onboarding và Offboarding',         'ONBOARDING_OFFBOARDING',  'WEEKLY', 5,    NULL, 11, 0, N'hr@digitalhrm.com,manager@digitalhrm.com'),
        ('SCH-AUDIT-DAILY',   N'Nhật ký hệ thống',                  'AUDIT_LOG',               'DAILY',  NULL, NULL, 23, 0, N'admin@digitalhrm.com')
    ) AS s(schedule_code, schedule_name, report_code, frequency_code, day_of_week, day_of_month, run_at_hour, run_at_minute, recipient_emails_csv)
)
INSERT INTO dbo.rep_report_schedule_config (
    schedule_code, schedule_name, report_code, frequency_code, day_of_week, day_of_month,
    run_at_hour, run_at_minute, recipient_emails_csv, parameter_json, status,
    next_run_at, last_run_at, last_run_status, last_run_message, description, created_by
)
SELECT
    s.schedule_code, s.schedule_name, s.report_code, s.frequency_code, s.day_of_week, s.day_of_month,
    s.run_at_hour, s.run_at_minute, s.recipient_emails_csv, N'{"seed":"R__110__seed_ci_demo_dataset"}',
    'ACTIVE',
    DATEADD(DAY, 1, @Now110),
    DATEADD(DAY, -1, @Now110),
    'SUCCESS',
    N'Lần chạy gần nhất thành công.',
    N'Lịch báo cáo demo cho CI/CD và dashboard.',
    @AdminUser110
FROM DemoSchedule s
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.rep_report_schedule_config c WHERE c.schedule_code = s.schedule_code
);
GO

DECLARE @AdminUser110 UNIQUEIDENTIFIER = (SELECT user_id FROM dbo.sec_user_account WHERE username = 'nguyen');

INSERT INTO dbo.rep_report_schedule_run (
    report_schedule_config_id, trigger_type, triggered_by_user_id, started_at, finished_at,
    run_status, output_file_key, output_file_name, output_row_count, run_message, created_by
)
SELECT
    c.report_schedule_config_id,
    CASE WHEN RIGHT(c.schedule_code, 1) IN ('Y','K') THEN 'MANUAL' ELSE 'SCHEDULED' END,
    @AdminUser110,
    DATEADD(DAY, -1, SYSDATETIME()),
    DATEADD(DAY, -1, DATEADD(MINUTE, 3, SYSDATETIME())),
    'SUCCESS',
    'reports/' + c.schedule_code + '.csv',
    c.schedule_code + '.csv',
    120,
    N'Bản ghi run demo thành công.',
    @AdminUser110
FROM dbo.rep_report_schedule_config c
WHERE c.schedule_code IN (
    'SCH-ORG-MONTHLY','SCH-CONTRACT-WEEK','SCH-LEAVE-WEEK','SCH-ATT-DAILY',
    'SCH-PAY-MONTHLY','SCH-PIT-MONTHLY','SCH-LIFE-WEEKLY','SCH-AUDIT-DAILY'
)
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.rep_report_schedule_run r
      WHERE r.report_schedule_config_id = c.report_schedule_config_id
        AND r.output_file_name = c.schedule_code + '.csv'
  );
GO

SET NOCOUNT OFF;
GO
