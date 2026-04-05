# Sprint 6 API Summary

## Admin / HR
- GET /api/v1/admin/payroll/components
- GET /api/v1/admin/payroll/components/{salaryComponentId}
- POST /api/v1/admin/payroll/components
- PUT /api/v1/admin/payroll/components/{salaryComponentId}
- GET /api/v1/admin/payroll/formulas
- GET /api/v1/admin/payroll/formulas/{formulaVersionId}
- POST /api/v1/admin/payroll/formulas
- PUT /api/v1/admin/payroll/formulas/{formulaVersionId}
- GET /api/v1/admin/payroll/compensations
- GET /api/v1/admin/payroll/compensations/{employeeId}
- PUT /api/v1/admin/payroll/compensations/{employeeId}
- GET /api/v1/admin/payroll/tax-profiles/{employeeId}
- PUT /api/v1/admin/payroll/tax-profiles/{employeeId}
- POST /api/v1/admin/payroll/tax-profiles/{employeeId}/dependents
- PUT /api/v1/admin/payroll/dependents/{taxDependentId}
- PATCH /api/v1/admin/payroll/dependents/{taxDependentId}/deactivate
- GET /api/v1/admin/payroll/periods
- POST /api/v1/admin/payroll/periods
- POST /api/v1/admin/payroll/periods/{payrollPeriodId}/generate-draft
- GET /api/v1/admin/payroll/periods/{payrollPeriodId}/items
- GET /api/v1/admin/payroll/items/{payrollItemId}
- PATCH /api/v1/admin/payroll/items/{payrollItemId}/adjust
- PATCH /api/v1/admin/payroll/periods/{payrollPeriodId}/approve
- PATCH /api/v1/admin/payroll/periods/{payrollPeriodId}/publish
- GET /api/v1/admin/payroll/reports/bank-transfer/export
- GET /api/v1/admin/payroll/reports/pit/export

## Manager
- GET /api/v1/manager/payroll/periods/{payrollPeriodId}/items
- PATCH /api/v1/manager/payroll/items/{payrollItemId}/confirm

## Employee self-service
- GET /api/v1/me/payroll/payslips
- GET /api/v1/me/payroll/payslips/{payrollPeriodId}
