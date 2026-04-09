# Database layout

- `create_database.sql`: tạo database mới
- `schema/V001__init_core.sql`: Sprint 1 core auth/session/audit
- `schema/V002__auth_rbac.sql`: Sprint 1 RBAC/data scope
- `schema/V003__org_employee.sql`: Sprint 2 org unit/job title/employee core
- `schema/V004__employee_profile_detail.sql`: Sprint 2 employee detail tables
- `seed/R__base_roles_admin.sql`: seed role, permission, role-permission, default admin bootstrap note
- `seed/R__sprint2_hr_permissions.sql`: seed permission cho Sprint 2

- `schema/V005__contract_core.sql`: Sprint 3 contract legal core
- `seed/R__sprint3_contract_permissions.sql`: seed permission cho Sprint 3 contract
- `seed/R__104__seed_contracts_and_compensation.sql`: seed hợp đồng và cấu trúc đãi ngộ nền
- `seed/R__105__seed_leave.sql`: seed loại nghỉ phép, quỹ phép và đơn nghỉ mẫu
- `seed/R__106__seed_attendance.sql`: seed dữ liệu attendance và kỳ công nền
- `seed/R__107__seed_payroll.sql`: seed kỳ lương, payroll item và payroll line nền
- `seed/R__108__seed_hr_workflows.sql`: seed dữ liệu mẫu cho HR workspace gồm attendance anomaly, adjustment/OT request, payroll workspace, profile change approval, onboarding, offboarding và hợp đồng sắp hết hạn
- `seed/R__109__seed_manager_workspace.sql`: seed dữ liệu manager workspace cho phạm vi nhân sự trực thuộc
- `seed/R__110__seed_ci_demo_dataset.sql`: seed dữ liệu demo dày cho smoke test và CI/CD
- `seed/R__111__seed_feature_rich_dataset.sql`: seed mở rộng theo feature với dữ liệu thực tế hơn cho org/employee/workflow/reporting
