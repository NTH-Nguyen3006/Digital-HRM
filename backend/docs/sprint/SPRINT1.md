# Sprint 1

## Mục tiêu
Hoàn thiện nền tảng security + auth + user admin + role/permission + audit đủ để FE bám API và backend có thể commit repo ngay.

## Module bàn giao
- auth
- user
- role
- permission foundation
- audit foundation

## Endpoint chính
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
- `POST /api/v1/auth/change-password`
- `GET /api/v1/admin/users`
- `GET /api/v1/admin/users/{userId}`
- `POST /api/v1/admin/users`
- `PUT /api/v1/admin/users/{userId}`
- `PATCH /api/v1/admin/users/{userId}/lock-state`
- `PUT /api/v1/admin/users/{userId}/primary-role`
- `GET /api/v1/admin/roles`
- `GET /api/v1/admin/roles/{roleId}`
- `POST /api/v1/admin/roles`
- `PUT /api/v1/admin/roles/{roleId}`
- `GET /api/v1/admin/audit-logs`

## Freeze checklist
- Flyway migrate sạch từ DB rỗng
- Bootstrap admin chạy thành công
- Login / refresh / logout / forgot-reset / change password chạy ổn
- User CRUD admin + assign role + lock/unlock chạy ổn
- Role detail/update chạy ổn
- Audit log query API trả dữ liệu đúng format
- Không còn default in-memory user của Spring Security
