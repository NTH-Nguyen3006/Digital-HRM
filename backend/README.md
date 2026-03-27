# hrm-backend

Digital HRM backend - Sprint 1 baseline đã hoàn thiện theo structure module-first.

## Project structure
```text
hrm-backend/
├── database/
│   ├── create_database.sql
│   ├── schema/
│   │   ├── V001__init_core.sql
│   │   └── V002__auth_rbac.sql
│   ├── seed/
│   │   └── R__base_roles_admin.sql
│   └── README.md
├── src/main/java/com/company/hrm/
│   ├── config/
│   ├── security/
│   ├── common/
│   │   ├── constant/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── response/
│   │   └── util/
│   ├── module/
│   │   ├── audit/
│   │   ├── auth/
│   │   ├── user/
│   │   ├── role/
│   │   ├── permission/
│   │   ├── orgunit/
│   │   ├── employee/
│   │   ├── contract/
│   │   ├── leave/
│   │   ├── attendance/
│   │   └── payroll/
│   └── HrmApplication.java
├── src/main/resources/
│   ├── application.yml
│   └── application-local.yml
├── docs/
│   ├── api/
│   ├── sprint/
│   └── postman/
├── pom.xml
└── README.md
```

## Sprint 1 scope
- Auth + JWT + refresh session
- Forgot/reset/change password
- User admin management
- Role + permission + data scope
- Audit log foundation + audit log query API

## Local run
1. Chạy `database/create_database.sql` để tạo DB `DigitalHRM`
2. Chạy app với profile local hoặc để Flyway tự migrate
3. Login bằng admin bootstrap mặc định:
   - username: `admin`
   - password: `Admin@123456`

## Lưu ý
- Fixed role: `ADMIN`, `HR`, `MANAGER`, `EMPLOYEE`
- Custom role đang bị khóa mặc định ở Sprint 1 (`app.role-management.allow-custom-role=false`)
- Nếu đã chạy migration cũ trước đó và chỉnh sửa checksum, nên drop DB local rồi chạy lại sạch từ đầu
