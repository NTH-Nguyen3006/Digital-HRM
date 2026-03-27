# Sprint 2 API Contract

## Org Unit
- `GET /api/v1/admin/org-units`
- `GET /api/v1/admin/org-units/tree`
- `GET /api/v1/admin/org-units/{orgUnitId}`
- `POST /api/v1/admin/org-units`
- `PUT /api/v1/admin/org-units/{orgUnitId}`
- `PATCH /api/v1/admin/org-units/{orgUnitId}/status`
- `PATCH /api/v1/admin/org-units/{orgUnitId}/manager`

## Job Title
- `GET /api/v1/admin/job-titles`
- `GET /api/v1/admin/job-titles/{jobTitleId}`
- `POST /api/v1/admin/job-titles`
- `PUT /api/v1/admin/job-titles/{jobTitleId}`
- `PATCH /api/v1/admin/job-titles/{jobTitleId}/status`

## Employee
- `GET /api/v1/admin/employees`
- `GET /api/v1/admin/employees/{employeeId}`
- `POST /api/v1/admin/employees`
- `PUT /api/v1/admin/employees/{employeeId}`
- `PATCH /api/v1/admin/employees/{employeeId}/employment-status`
- `PATCH /api/v1/admin/employees/{employeeId}/transfer`

## Employee Profile / Detail
- `GET|PUT /api/v1/admin/employees/{employeeId}/profile`
- `GET|POST /api/v1/admin/employees/{employeeId}/addresses`
- `PUT|DELETE /api/v1/admin/employees/{employeeId}/addresses/{addressId}`
- `GET|POST /api/v1/admin/employees/{employeeId}/emergency-contacts`
- `PUT|DELETE /api/v1/admin/employees/{employeeId}/emergency-contacts/{emergencyContactId}`
- `GET|POST /api/v1/admin/employees/{employeeId}/identifications`
- `PUT|DELETE /api/v1/admin/employees/{employeeId}/identifications/{identificationId}`
- `GET|POST /api/v1/admin/employees/{employeeId}/bank-accounts`
- `PUT|DELETE /api/v1/admin/employees/{employeeId}/bank-accounts/{bankAccountId}`
- `GET|POST /api/v1/admin/employees/{employeeId}/documents`
- `PUT|DELETE /api/v1/admin/employees/{employeeId}/documents/{documentId}`
