# Postman quick test

## 1. Upload file nghỉ phép
- Method: `POST`
- URL: `{{baseUrl}}/api/v1/storage/files`
- Authorization: Bearer Token
- Body -> form-data:
  - `file` = chọn file PDF/JPG thật
  - `moduleCode` = `LEAVE_ATTACHMENT`
  - `businessCategory` = `MEDICAL_CERTIFICATE`
  - `visibilityScope` = `REVIEW_FLOW`

Kết quả: nhận `data.fileKey`

## 2. Dùng fileKey cho leave request
`POST {{baseUrl}}/api/v1/me/leave-requests`

```json
{
  "leaveTypeId": 1,
  "startDate": "2026-04-20",
  "endDate": "2026-04-20",
  "requestedUnits": 1,
  "reason": "Nghỉ khám bệnh",
  "attachmentRef": "{{fileKey}}",
  "submit": true
}
```

## 3. Download lại file
`GET {{baseUrl}}/api/v1/storage/files/{{fileKey}}/download`
