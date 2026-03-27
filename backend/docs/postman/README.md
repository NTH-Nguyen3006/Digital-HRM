# Postman test order - Sprint 1

1. `POST /api/v1/auth/login` bằng admin bootstrap
2. Copy `accessToken` vào Bearer Token
3. `GET /api/v1/admin/roles`
4. `GET /api/v1/admin/users`
5. `POST /api/v1/admin/users`
6. `PUT /api/v1/admin/users/{userId}`
7. `PATCH /api/v1/admin/users/{userId}/lock-state`
8. `PUT /api/v1/admin/users/{userId}/primary-role`
9. `POST /api/v1/auth/forgot-password`
10. `POST /api/v1/auth/reset-password`
11. `POST /api/v1/auth/change-password`
12. `GET /api/v1/admin/audit-logs`
