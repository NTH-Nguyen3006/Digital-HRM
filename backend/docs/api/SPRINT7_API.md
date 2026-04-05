# Sprint 7 API

## Offboarding
- `GET /api/v1/admin/offboarding`
- `GET /api/v1/admin/offboarding/{offboardingCaseId}`
- `PATCH /api/v1/admin/offboarding/{offboardingCaseId}/finalize`
- `POST /api/v1/admin/offboarding/{offboardingCaseId}/asset-returns`
- `PUT /api/v1/admin/offboarding/asset-returns/{assetReturnId}`
- `PATCH /api/v1/admin/offboarding/{offboardingCaseId}/settlement`
- `PATCH /api/v1/admin/offboarding/{offboardingCaseId}/close`
- `PATCH /api/v1/admin/offboarding/{offboardingCaseId}/revoke-access`

- `GET /api/v1/manager/offboarding/pending`
- `PATCH /api/v1/manager/offboarding/{offboardingCaseId}/review`
- `POST /api/v1/manager/offboarding/{offboardingCaseId}/checklist-items`
- `PUT /api/v1/manager/offboarding/checklist-items/{checklistItemId}`

- `GET /api/v1/me/offboarding/requests`
- `POST /api/v1/me/offboarding/requests`

## Employee Portal
- `GET /api/v1/me/portal/dashboard`
- `GET /api/v1/me/portal/profile`
- `GET /api/v1/me/portal/leave`
- `POST /api/v1/me/portal/leave-requests`
- `GET /api/v1/me/portal/attendance`
- `POST /api/v1/me/portal/attendance-adjustment-requests`
- `GET /api/v1/me/portal/contracts`
- `GET /api/v1/me/portal/contracts/{laborContractId}/export`
- `GET /api/v1/me/portal/payroll`
- `GET /api/v1/me/portal/payroll/{payrollPeriodId}`
- `GET /api/v1/me/portal/inbox`
- `PATCH /api/v1/me/portal/inbox/{portalInboxItemId}/read`
