# Sprint 6 Postman Smoke Flow

## 1. HR creates / updates salary components
POST /api/v1/admin/payroll/components
PUT /api/v1/admin/payroll/components/{salaryComponentId}

## 2. HR creates formula version
POST /api/v1/admin/payroll/formulas

## 3. HR sets employee compensation
PUT /api/v1/admin/payroll/compensations/{employeeId}

## 4. HR sets tax profile and dependents
PUT /api/v1/admin/payroll/tax-profiles/{employeeId}
POST /api/v1/admin/payroll/tax-profiles/{employeeId}/dependents

## 5. HR creates payroll period from closed attendance period
POST /api/v1/admin/payroll/periods

## 6. HR generates payroll draft
POST /api/v1/admin/payroll/periods/{payrollPeriodId}/generate-draft

## 7. Manager reviews team payroll
GET /api/v1/manager/payroll/periods/{payrollPeriodId}/items
PATCH /api/v1/manager/payroll/items/{payrollItemId}/confirm

## 8. HR adjusts payroll item if needed
PATCH /api/v1/admin/payroll/items/{payrollItemId}/adjust

## 9. HR approves payroll period
PATCH /api/v1/admin/payroll/periods/{payrollPeriodId}/approve

## 10. HR publishes payslips
PATCH /api/v1/admin/payroll/periods/{payrollPeriodId}/publish

## 11. Employee views personal payslip
GET /api/v1/me/payroll/payslips
GET /api/v1/me/payroll/payslips/{payrollPeriodId}

## 12. HR exports bank transfer and PIT reports
GET /api/v1/admin/payroll/reports/bank-transfer/export?payrollPeriodId={payrollPeriodId}
GET /api/v1/admin/payroll/reports/pit/export?payrollPeriodId={payrollPeriodId}
