# Sprint 5 - Attendance Control

## Scope
- Shift catalog + versioned shift rules
- Shift assignment for employees
- Raw check-in / check-out log
- Daily attendance board
- Attendance adjustment request / manager approval / HR finalization
- Overtime request / manager approval
- Attendance period review / close / reopen
- Anomaly CSV export

## Out of scope
- Payroll calculation
- Attendance device integration protocol
- Generic notification center
- Mobile-specific geo-fencing policy
- Offboarding or payroll settlement

## Notes
- Daily summary is materialized in `att_daily_attendance`.
- Original logs are preserved in `att_attendance_log`.
- Adjustment finalization writes synthetic logs with source `ADJUSTMENT` instead of mutating original logs.
- OT approval threshold is driven by `sys_platform_setting`.
