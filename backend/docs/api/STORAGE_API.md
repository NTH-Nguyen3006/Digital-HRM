# Storage API

## Upload file
`POST /api/v1/storage/files`

Content-Type: `multipart/form-data`

Form-data:
- `file`: binary file
- `moduleCode`: ví dụ `EMPLOYEE_DOCUMENT`, `CONTRACT_ATTACHMENT`, `ONBOARDING_DOCUMENT`, `LEAVE_ATTACHMENT`
- `businessCategory`: ví dụ `PROFILE`, `LABOR_CONTRACT`, `CHECKLIST`, `MEDICAL_CERTIFICATE`
- `visibilityScope`: `SELF_ONLY` | `REVIEW_FLOW` | `HR_ADMIN` | `INTERNAL`
- `note`: optional

## Get metadata
`GET /api/v1/storage/files/{fileKey}`

## Download file
`GET /api/v1/storage/files/{fileKey}/download`
