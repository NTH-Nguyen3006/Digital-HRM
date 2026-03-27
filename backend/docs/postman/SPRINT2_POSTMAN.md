# Sprint 2 Postman smoke test

1. Login admin: `POST /api/v1/auth/login`
2. Tạo org unit gốc: `POST /api/v1/admin/org-units`
3. Tạo job title: `POST /api/v1/admin/job-titles`
4. Tạo employee: `POST /api/v1/admin/employees`
5. Upsert profile: `PUT /api/v1/admin/employees/{employeeId}/profile`
6. Tạo address / emergency contact / identification / bank account / document
7. List tree org unit và list employee
8. Thử change status job title / org unit với rule conflict
