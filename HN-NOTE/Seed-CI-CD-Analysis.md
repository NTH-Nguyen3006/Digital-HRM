# Seed CI/CD Analysis

## Mục tiêu

Tạo bộ seed có tính:

- `idempotent`: chạy lặp lại không tạo trùng dữ liệu
- `stable`: code định danh cố định để dễ smoke test
- `feature-oriented`: mỗi nhóm tính năng có đủ dữ liệu để demo hoặc test UI/API
- `CI/CD-friendly`: môi trường mới migrate xong là có dữ liệu dùng ngay

## Nhóm tính năng trong dự án

### Admin

- Quản lý tài khoản
- Quản lý role / permission
- Nhật ký hoạt động
- Cấu hình hệ thống
- Lịch chạy báo cáo / lịch sử chạy báo cáo

### HR

- Cơ cấu tổ chức
- Hồ sơ nhân sự
- Hợp đồng lao động
- Yêu cầu cập nhật hồ sơ
- Onboarding
- Offboarding
- Nghỉ phép
- Chấm công / điều chỉnh công / OT
- Payroll / PIT / bảng lương
- Dashboard tổng quan

### Manager

- Dashboard team
- Danh sách nhân sự theo scope quản lý
- Duyệt nghỉ phép
- Duyệt chấm công / OT
- Xác nhận payroll team
- Theo dõi onboarding / offboarding trong scope

### Employee Portal

- Profile cá nhân
- Check-in / check-out
- Xin nghỉ phép
- Điều chỉnh công
- OT
- Task / inbox
- Resignation
- Payroll self-service

## File seed hiện tại

- [R__100__seed_organization.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__100__seed_organization.sql)
- [R__101__seed_job_titles.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__101__seed_job_titles.sql)
- [R__102__seed_employees.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__102__seed_employees.sql)
- [R__103__seed_user_accounts.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__103__seed_user_accounts.sql)
- [R__104__seed_contracts_and_compensation.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__104__seed_contracts_and_compensation.sql)
- [R__105__seed_leave.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__105__seed_leave.sql)
- [R__106__seed_attendance.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__106__seed_attendance.sql)
- [R__107__seed_payroll.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__107__seed_payroll.sql)
- [R__108__seed_hr_workflows.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__108__seed_hr_workflows.sql)
- [R__109__seed_manager_workspace.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__109__seed_manager_workspace.sql)
- [R__110__seed_ci_demo_dataset.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__110__seed_ci_demo_dataset.sql)

## Vai trò của file mới `R__110`

File [R__110__seed_ci_demo_dataset.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__110__seed_ci_demo_dataset.sql) là lớp seed dày cho CI/CD:

- thêm `12` nhân sự demo mới: `EMP100 -> EMP111`
- thêm user account tương ứng cho toàn bộ nhân sự demo
- thêm hợp đồng và compensation cho toàn bộ nhân sự demo
- thêm leave balance và `12` leave request
- thêm attendance daily/log, `12` adjustment request, `12` OT request
- thêm payroll items cho `PR-2026-04` và `PR-2026-05`
- thêm profile change request và timeline
- thêm `10` onboarding records cùng checklist / document / asset
- thêm `10` offboarding cases cùng checklist / asset / history
- thêm inbox/task cho portal
- thêm audit log và report schedule data cho admin/reporting

## Chiến lược seed khuyến nghị

### 1. Tách seed theo lớp

- `R__100 -> R__107`: foundation data
- `R__108`: workflow data nhẹ
- `R__109`: manager workspace data
- `R__110`: dense demo dataset cho smoke test / demo / staging nhẹ

### 2. Dùng mã cố định

- employee code: `EMP100 -> EMP111`
- onboarding code: `ONB-2026-100 -> ONB-2026-109`
- offboarding code: `OFF-2026-100 -> OFF-2026-109`
- leave / attendance / OT / payroll code có prefix `DEMO`

Điều này giúp:

- dễ viết test dữ liệu
- dễ debug API
- dễ tìm record bằng tay khi chạy staging / UAT

### 3. Ưu tiên `INSERT ... WHERE NOT EXISTS` hoặc `MERGE`

Mục tiêu là:

- không phụ thuộc DB sạch hoàn toàn
- rerun Flyway repeatable vẫn an toàn
- tránh phát sinh record trùng sau nhiều lần deploy

### 4. Không seed quá sâu vào bảng phụ nếu UI không dùng

Seed nên ưu tiên bảng mà UI/API đang đọc trực tiếp:

- employee
- user
- contract
- leave
- attendance
- payroll
- onboarding
- offboarding
- portal inbox
- audit/reporting

### 5. Nên có môi trường seed riêng

Khuyến nghị chia:

- `dev/local`: chạy toàn bộ `R__110`
- `staging/demo`: chạy toàn bộ `R__110`
- `prod`: không chạy `R__110` hoặc tách profile Flyway riêng

Nếu sau này cần an toàn hơn cho production, nên:

- giữ `R__100 -> R__109` là seed hệ thống và workflow tối thiểu
- chuyển `R__110` sang profile demo riêng hoặc thư mục seed riêng cho non-prod

## Gợi ý bước tiếp theo

- rà lại [R__110__seed_ci_demo_dataset.sql](/home/nguyen/My-Project/digital-hrm/backend/database/seed/R__110__seed_ci_demo_dataset.sql) bằng chạy migrate thật trên local DB
- thêm smoke test API cho các code cố định như `EMP100`, `ONB-2026-100`, `OFF-2026-100`
- nếu muốn non-prod/prod tách riêng, chuyển `R__110` thành seed profile `demo`
