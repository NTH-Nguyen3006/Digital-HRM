SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

/* =========================================================
   R__103__seed_user_accounts.sql
   Scope:
   - Seed user accounts for existing employees
   - Assign roles based on job title/dept
   ========================================================= */

-- Standard BCrypt hash for 'P@ssword123'
DECLARE @DefaultPasswordHash VARCHAR(255) = '$2a$10$gaSEymcGFJ74WopEYuob2OLUqXy3IZuTdmv5vDDWVYzVcp7DTic32'; 

-- 1. Upsert User Accounts
MERGE INTO dbo.sec_user_account AS TARGET
USING (
    VALUES 
        ('EMP001', 'nguyen', 'nthn300607@gmail.com'),
        ('EMP002', 'dev_b', 'b.tran@digitalhrm.com'),
        ('EMP003', 'mgr_c', 'c.le@digitalhrm.com'),
        ('EMP004', 'acc_d', 'd.pham@digitalhrm.com'),
        ('EMP005', 'dev_e', 'e.hoang@digitalhrm.com')
) AS SOURCE (employee_code, username, email)
ON TARGET.employee_id = (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = SOURCE.employee_code)
WHEN MATCHED THEN
    UPDATE SET 
        TARGET.username = SOURCE.username,
        TARGET.email = SOURCE.email,
        TARGET.password_hash = @DefaultPasswordHash,
        TARGET.status = 'ACTIVE',
        TARGET.is_deleted = 0,
        TARGET.updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (user_id, employee_id, username, password_hash, status, must_change_password, email, created_at, is_deleted)
    VALUES (
        NEWID(), 
        (SELECT employee_id FROM dbo.hr_employee WHERE employee_code = SOURCE.employee_code),
        SOURCE.username, 
        @DefaultPasswordHash, 
        'ACTIVE', 
        0, 
        SOURCE.email,
        SYSDATETIME(),
        0
    );
GO

-- 2. Ensure Roles are assigned correctly
DECLARE @UserRoles TABLE (username VARCHAR(50), role_code VARCHAR(30), is_primary BIT);
INSERT INTO @UserRoles (username, role_code, is_primary)
VALUES 
    ('nguyen', 'ADMIN', 1), ('nguyen', 'HR', 0),
    ('mgr_c', 'MANAGER', 1),
    ('dev_b', 'EMPLOYEE', 1),
    ('acc_d', 'EMPLOYEE', 1),
    ('dev_e', 'EMPLOYEE', 1);

INSERT INTO dbo.sec_user_role (user_id, role_id, is_primary_role, status, created_at)
SELECT u.user_id, r.role_id, s.is_primary, 'ACTIVE', SYSDATETIME()
FROM @UserRoles s
INNER JOIN dbo.sec_user_account u ON u.username = s.username
INNER JOIN dbo.sec_role r ON r.role_code = s.role_code
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.sec_user_role ur 
    WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id
);
GO
GO

SET NOCOUNT OFF;
GO
