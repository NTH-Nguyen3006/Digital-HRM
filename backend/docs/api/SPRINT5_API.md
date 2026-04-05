# Sprint 5 API Summary

## Admin / HR
- GET /api/v1/admin/attendance/shifts
- GET /api/v1/admin/attendance/shifts/{shiftId}
- POST /api/v1/admin/attendance/shifts
- PUT /api/v1/admin/attendance/shifts/{shiftId}
- GET /api/v1/admin/attendance/shift-assignments
- POST /api/v1/admin/attendance/shift-assignments

- GET /api/v1/admin/attendance/daily
- GET /api/v1/admin/attendance/daily/detail
- GET /api/v1/admin/attendance/adjustment-requests
- GET /api/v1/admin/attendance/adjustment-requests/{adjustmentRequestId}
- PATCH /api/v1/admin/attendance/adjustment-requests/{adjustmentRequestId}/finalize
- GET /api/v1/admin/attendance/overtime-requests
- GET /api/v1/admin/attendance/periods
- POST /api/v1/admin/attendance/periods/close
- PATCH /api/v1/admin/attendance/periods/{attendancePeriodId}/reopen
- GET /api/v1/admin/attendance/reports/anomalies/export

## Manager
- GET /api/v1/manager/attendance/adjustment-requests/pending
- PATCH /api/v1/manager/attendance/adjustment-requests/{adjustmentRequestId}/review
- GET /api/v1/manager/attendance/overtime-requests/pending
- PATCH /api/v1/manager/attendance/overtime-requests/{overtimeRequestId}/review

## Employee self-service
- POST /api/v1/me/attendance/check-in
- POST /api/v1/me/attendance/check-out
- GET /api/v1/me/attendance/logs
- GET /api/v1/me/attendance/adjustment-requests
- POST /api/v1/me/attendance/adjustment-requests
- PUT /api/v1/me/attendance/adjustment-requests/{adjustmentRequestId}
- PATCH /api/v1/me/attendance/adjustment-requests/{adjustmentRequestId}/cancel
- GET /api/v1/me/attendance/overtime-requests
- POST /api/v1/me/attendance/overtime-requests
