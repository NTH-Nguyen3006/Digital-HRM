# Sprint 1 API Summary

## Auth
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
- `POST /api/v1/auth/change-password`

## User admin
- `GET /api/v1/admin/users`
- `GET /api/v1/admin/users/{userId}`
- `POST /api/v1/admin/users`
- `PUT /api/v1/admin/users/{userId}`
- `PATCH /api/v1/admin/users/{userId}/lock-state`
- `PUT /api/v1/admin/users/{userId}/primary-role`

## Role admin
- `GET /api/v1/admin/roles`
- `GET /api/v1/admin/roles/{roleId}`
- `POST /api/v1/admin/roles`
- `PUT /api/v1/admin/roles/{roleId}`

## Audit
- `GET /api/v1/admin/audit-logs`

## Response format
Tất cả endpoint trả về `ApiResponse<T>` với các field:
- `success`
- `code`
- `message`
- `data`
- `meta`
- `traceId`
- `timestamp`
