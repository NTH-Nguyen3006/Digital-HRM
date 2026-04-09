# Feature And Seed Analysis

## Mục tiêu

Tài liệu này tổng hợp nhanh các nhóm tính năng chính của dự án `digital-hrm` và seed tương ứng để:

- dễ đọc tổng quan dự án
- biết module nào đang được seed
- biết file seed nào nên ưu tiên khi cần demo, smoke test hoặc CI/CD

## Nhóm tính năng toàn dự án

### 1. Auth / RBAC / Audit

- Đăng nhập, refresh token, reset mật khẩu
- Quản trị user, role, permission, data scope
- Audit log cho thao tác quản trị và workflow

DB / seed liên quan:

- `backend/database/schema/V001__init_core.sql`
- `backend/database/schema/V002__auth_rbac.sql`
- `backend/database/seed/R__base_roles_admin.sql`
- `backend/database/seed/R__110__seed_ci_demo_dataset.sql`
- `backend/database/seed/R__111__seed_feature_rich_dataset.sql`

### 2. Tổ chức và nhân sự

- Org chart
- Job title
- Employee list / employee detail
- Hồ sơ nhân sự, địa chỉ, giấy tờ, tài khoản ngân hàng

DB / seed liên quan:

- `backend/database/schema/V003__org_employee.sql`
- `backend/database/schema/V004__employee_profile_detail.sql`
- `backend/database/seed/R__100__seed_organization.sql`
- `backend/database/seed/R__101__seed_job_titles.sql`
- `backend/database/seed/R__102__seed_employees.sql`
- `backend/database/seed/R__110__seed_ci_demo_dataset.sql`
- `backend/database/seed/R__111__seed_feature_rich_dataset.sql`

### 3. Contract / Compensation

- Contract type
- Labor contract
- Cấu hình lương và compensation item
- Tình huống hợp đồng sắp hết hạn

DB / seed liên quan:

- `backend/database/schema/V005__contract_core.sql`
- `backend/database/schema/V012__payroll_and_pit.sql`
- `backend/database/seed/R__104__seed_contracts_and_compensation.sql`
- `backend/database/seed/R__108__seed_hr_workflows.sql`
- `backend/database/seed/R__110__seed_ci_demo_dataset.sql`
- `backend/database/seed/R__111__seed_feature_rich_dataset.sql`

### 4. Profile Change Workflow

- Nhân viên gửi yêu cầu cập nhật hồ sơ
- HR duyệt / từ chối
- Timeline thay đổi hồ sơ

DB / seed liên quan:

- `backend/database/schema/V007__employee_profile_workflow.sql`
- `backend/database/seed/R__108__seed_hr_workflows.sql`
- `backend/database/seed/R__110__seed_ci_demo_dataset.sql`
- `backend/database/seed/R__111__seed_feature_rich_dataset.sql`

### 5. Onboarding

- Tạo hồ sơ onboarding
- Checklist nhận việc
- Tài liệu onboarding
- Cấp phát tài sản

DB / seed liên quan:

- `backend/database/schema/V008__onboarding_core.sql`
- `backend/database/schema/V009__leave_and_onboarding_finalize.sql`
- `backend/database/seed/R__108__seed_hr_workflows.sql`
- `backend/database/seed/R__110__seed_ci_demo_dataset.sql`
- `backend/database/seed/R__111__seed_feature_rich_dataset.sql`

### 6. Leave

- Leave type / rule
- Leave balance
- Leave request theo luồng submit / approve / reject / finalize

DB / seed liên quan:

- `backend/database/schema/V009__leave_and_onboarding_finalize.sql`
- `backend/database/seed/R__105__seed_leave.sql`
- `backend/database/seed/R__109__seed_manager_workspace.sql`
- `backend/database/seed/R__110__seed_ci_demo_dataset.sql`
- `backend/database/seed/R__111__seed_feature_rich_dataset.sql`

### 7. Attendance / Adjustment / Overtime

- Shift, shift assignment
- Attendance log, daily attendance
- Attendance adjustment request
- Overtime request
- Manager review team attendance

DB / seed liên quan:

- `backend/database/schema/V011__attendance_control.sql`
- `backend/database/seed/R__106__seed_attendance.sql`
- `backend/database/seed/R__108__seed_hr_workflows.sql`
- `backend/database/seed/R__109__seed_manager_workspace.sql`
- `backend/database/seed/R__110__seed_ci_demo_dataset.sql`
- `backend/database/seed/R__111__seed_feature_rich_dataset.sql`

### 8. Payroll / PIT

- Salary component
- Payroll period
- Payroll item / payslip line
- PIT profile / dependent
- Portal payslip, manager payroll, HR payroll

DB / seed liên quan:

- `backend/database/schema/V012__payroll_and_pit.sql`
- `backend/database/seed/R__107__seed_payroll.sql`
- `backend/database/seed/R__108__seed_hr_workflows.sql`
- `backend/database/seed/R__109__seed_manager_workspace.sql`
- `backend/database/seed/R__110__seed_ci_demo_dataset.sql`
- `backend/database/seed/R__111__seed_feature_rich_dataset.sql`

### 9. Offboarding / Portal Employee

- Offboarding request
- Checklist bàn giao
- Asset return
- Portal inbox / task / resignation

DB / seed liên quan:

- `backend/database/schema/V013__offboarding_and_employee_portal.sql`
- `backend/database/seed/R__108__seed_hr_workflows.sql`
- `backend/database/seed/R__110__seed_ci_demo_dataset.sql`
- `backend/database/seed/R__111__seed_feature_rich_dataset.sql`

### 10. Reporting / Governance

- Lịch chạy báo cáo
- Lịch sử chạy báo cáo
- Dữ liệu phục vụ dashboard và giám sát vận hành

DB / seed liên quan:

- `backend/database/schema/V014__reporting_and_governance.sql`
- `backend/database/seed/R__110__seed_ci_demo_dataset.sql`
- `backend/database/seed/R__111__seed_feature_rich_dataset.sql`

## Seed strategy hiện tại

### Seed nền

- `R__100` đến `R__107` dựng khung dữ liệu cơ bản
- phù hợp cho dev local hoặc chạy từng sprint

### Seed workflow / workspace

- `R__108` tập trung cho HR workspace
- `R__109` tập trung cho manager workspace

### Seed dense dataset cho CI/CD

- `R__110` tạo lớp dữ liệu dày cho nhiều màn hình
- `R__111` mở rộng thêm dữ liệu thực tế hơn cho org, employee, payroll, onboarding, offboarding, portal, audit và reporting

## Vì sao `R__111` hữu ích cho CI/CD

- dùng mã cố định như `EMP112`-`EMP123`, `ONB-2026-200`-`209`, `OFF-2026-200`-`209`
- không dùng random, nên snapshot test và smoke test ổn định hơn
- toàn bộ insert theo `MERGE` hoặc `NOT EXISTS`, nên Flyway chạy lặp lại an toàn
- dữ liệu trải đều nhiều trạng thái: `DRAFT`, `SUBMITTED`, `APPROVED`, `REJECTED`, `FINALIZED`, `PUBLISHED`, `CLOSED`
- giúp QA và demo có đủ case cho admin, HR, manager và employee portal

## Khuyến nghị sử dụng

- Demo nhẹ: dùng đến `R__108`
- Demo manager / approval flow: thêm `R__109`
- Demo đầy đủ hoặc chạy CI/CD smoke test: chạy toàn bộ tới `R__111`
