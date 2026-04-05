# Sprint 5 Postman Smoke Flow

## 1. HR creates shift
POST /api/v1/admin/attendance/shifts

## 2. HR assigns shift
POST /api/v1/admin/attendance/shift-assignments

## 3. Employee check-in / check-out
POST /api/v1/me/attendance/check-in
POST /api/v1/me/attendance/check-out

## 4. HR views daily board
GET /api/v1/admin/attendance/daily?fromDate=2026-04-01&toDate=2026-04-01

## 5. Employee creates attendance adjustment
POST /api/v1/me/attendance/adjustment-requests

## 6. Manager reviews adjustment
PATCH /api/v1/manager/attendance/adjustment-requests/{id}/review

## 7. HR finalizes adjustment
PATCH /api/v1/admin/attendance/adjustment-requests/{id}/finalize

## 8. Employee creates overtime request
POST /api/v1/me/attendance/overtime-requests

## 9. Manager reviews overtime
PATCH /api/v1/manager/attendance/overtime-requests/{id}/review

## 10. HR closes attendance period
POST /api/v1/admin/attendance/periods/close

## 11. HR exports anomalies
GET /api/v1/admin/attendance/reports/anomalies/export?fromDate=2026-04-01&toDate=2026-04-30
