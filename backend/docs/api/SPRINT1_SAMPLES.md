# Sprint 1 - Sample Requests & Responses

## 1. Login
### Request
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "loginId": "admin",
  "password": "Admin@123456",
  "deviceName": "Chrome on Windows",
  "deviceOs": "Windows 11",
  "browserName": "Chrome"
}
```

## 2. Create user
### Request
```http
POST /api/v1/admin/users
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "employeeId": 10001,
  "username": "hr.user",
  "email": "hr.user@company.local",
  "phoneNumber": "0901234567",
  "status": "ACTIVE",
  "roleCode": "HR",
  "sendSetupEmail": true
}
```

## 3. Update user
### Request
```http
PUT /api/v1/admin/users/{{userId}}
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "employeeId": 10001,
  "username": "hr.user",
  "email": "hr.user@company.local",
  "phoneNumber": "0909999999",
  "status": "ACTIVE",
  "mustChangePassword": true
}
```

## 4. Lock user
### Request
```http
PATCH /api/v1/admin/users/{{userId}}/lock-state
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "locked": true,
  "reason": "Khóa tài khoản để kiểm tra nội bộ",
  "lockedUntil": "2026-03-31T23:59:59"
}
```

## 5. Assign primary role
### Request
```http
PUT /api/v1/admin/users/{{userId}}/primary-role
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "roleCode": "MANAGER",
  "reason": "Điều chỉnh role chính theo cơ cấu tổ chức"
}
```

## 6. Update role
### Request
```http
PUT /api/v1/admin/roles/{{roleId}}
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "roleName": "Human Resources",
  "description": "Nhân sự cập nhật sau Sprint 1",
  "sortOrder": 2,
  "status": "ACTIVE",
  "permissionCodes": [
    "auth.login",
    "auth.logout",
    "auth.forgot_password",
    "auth.change_password"
  ],
  "dataScopes": [
    {
      "scopeCode": "ORG_UNIT",
      "targetType": "ORG_UNIT",
      "targetRefId": null,
      "inclusive": true,
      "priorityOrder": 1,
      "status": "ACTIVE"
    }
  ]
}
```

## 7. Audit logs
### Request
```http
GET /api/v1/admin/audit-logs?moduleCode=AUTH&page=0&size=20
Authorization: Bearer {{accessToken}}
```

## Response envelope
Tất cả endpoint trả về cùng một envelope:
```json
{
  "success": true,
  "code": "...",
  "message": "...",
  "data": {},
  "meta": null,
  "traceId": "...",
  "timestamp": "2026-03-27T12:34:56+07:00"
}
```
