# Sprint 2 - Org Structure + Employee Master

## Scope
- Org Unit master (`hr_org_unit`)
- Job Title master (`hr_job_title`)
- Employee core (`hr_employee`)
- Employee extended profile, address, emergency contact, identification, bank account, document metadata
- Permission seed cho module `ORG_UNIT`, `JOB_TITLE`, `EMPLOYEE`, `EMPLOYEE_PROFILE`, `EMPLOYEE_DOCUMENT`

## Out of scope
- Contract
- Leave
- Attendance
- Payroll
- File storage engine thực tế (Sprint 2 chỉ quản metadata document)
- Import/export batch

## Migration
- `V003__org_employee.sql`
- `V004__employee_profile_detail.sql`
- `R__sprint2_hr_permissions.sql`

## API root
- `/api/v1/admin/org-units`
- `/api/v1/admin/job-titles`
- `/api/v1/admin/employees`

## Permission summary
- `orgunit.view/create/update/change_status/assign_manager`
- `jobtitle.view/create/update/change_status`
- `employee.view/create/update/change_status/transfer`
- `employee.profile.view/update`
- `employee.document.view/manage`
