# Sprint 8 API

## Admin / HR
- `GET /api/v1/admin/reports/dashboard/headcount`
- `GET /api/v1/admin/reports/org-movement/export`
- `GET /api/v1/admin/reports/contracts/expiry/export`
- `GET /api/v1/admin/reports/leave-balances/export`
- `GET /api/v1/admin/reports/attendance/anomaly-ot/export`
- `GET /api/v1/admin/reports/payroll/summary/export`
- `GET /api/v1/admin/reports/payroll/pit/export`
- `GET /api/v1/admin/reports/onboarding-offboarding/export`
- `GET /api/v1/admin/reports/audit/export`
- `GET /api/v1/admin/reports/system-health`
- `GET /api/v1/admin/reports/schedules`
- `POST /api/v1/admin/reports/schedules`
- `PUT /api/v1/admin/reports/schedules/{reportScheduleConfigId}`
- `GET /api/v1/admin/reports/schedules/{reportScheduleConfigId}/runs`
- `POST /api/v1/admin/reports/schedules/{reportScheduleConfigId}/run-now`

## Manager
- `GET /api/v1/manager/reports/dashboard/team`
