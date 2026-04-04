# Sprint 4 API Summary

## Leave Type
- GET /api/v1/admin/leave-types
- GET /api/v1/admin/leave-types/{leaveTypeId}
- POST /api/v1/admin/leave-types
- PUT /api/v1/admin/leave-types/{leaveTypeId}
- PATCH /api/v1/admin/leave-types/{leaveTypeId}/deactivate

## Leave Balance / HR Workflow
- GET /api/v1/admin/leave/balances
- GET /api/v1/admin/leave/balances/{leaveBalanceId}
- PATCH /api/v1/admin/leave/balances/adjust
- GET /api/v1/admin/leave/requests
- GET /api/v1/admin/leave/requests/{leaveRequestId}
- PATCH /api/v1/admin/leave/requests/{leaveRequestId}/finalize
- POST /api/v1/admin/leave/period-close
- POST /api/v1/admin/leave/settlement
- GET /api/v1/admin/leave/reports/export

## Employee Self-Service
- GET /api/v1/me/leave/balances
- GET /api/v1/me/leave/requests
- GET /api/v1/me/leave/requests/{leaveRequestId}
- POST /api/v1/me/leave/requests
- PUT /api/v1/me/leave/requests/{leaveRequestId}
- PATCH /api/v1/me/leave/requests/{leaveRequestId}/cancel

## Manager
- GET /api/v1/manager/leave/requests/pending
- PATCH /api/v1/manager/leave/requests/{leaveRequestId}/review
- GET /api/v1/manager/leave/calendar
- PATCH /api/v1/manager/onboarding/{onboardingId}/orientation-confirm

## Onboarding HR
- PATCH /api/v1/admin/onboarding/{onboardingId}/complete
- POST /api/v1/admin/onboarding/{onboardingId}/notify
