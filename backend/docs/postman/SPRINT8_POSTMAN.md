# Sprint 8 Postman smoke

## Headcount dashboard
`GET /api/v1/admin/reports/dashboard/headcount`

## Team dashboard
`GET /api/v1/manager/reports/dashboard/team`

## Export contract expiry
`GET /api/v1/admin/reports/contracts/expiry/export?fromDate=2026-08-01&toDate=2026-09-30`

## Create scheduled report
`POST /api/v1/admin/reports/schedules`
```json
{
  "scheduleCode": "PAYROLL_SUMMARY_MONTHLY",
  "scheduleName": "Payroll summary monthly",
  "reportCode": "PAYROLL_SUMMARY",
  "frequencyCode": "MONTHLY",
  "dayOfMonth": 1,
  "dayOfWeek": null,
  "runAtHour": 8,
  "runAtMinute": 0,
  "recipientEmails": ["hr@example.com"],
  "parameterJson": "{\"payrollPeriodId\":1}",
  "status": "ACTIVE",
  "description": "Chạy báo cáo lương đầu tháng"
}
```

## Run schedule now
`POST /api/v1/admin/reports/schedules/{reportScheduleConfigId}/run-now`
