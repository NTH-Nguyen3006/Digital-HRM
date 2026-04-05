# Sprint 6 - Payroll & PIT

## Scope
- Salary component master
- Payroll formula versioning and PIT tax brackets
- Employee compensation package and bank payout data
- Payroll period creation from closed attendance period
- Draft payroll generation from attendance + leave + contract data
- Manual payroll item adjustment before approval
- Manager team confirmation on payroll items
- HR approval and payslip publication
- Personal tax profile and dependent management
- Bank transfer CSV export and PIT CSV export

## Out of scope
- Actual bank disbursement integration
- Annual tax finalization workflow beyond monthly withholding foundation
- Employer-side insurance contribution ledger
- Accounting journal posting
- Payroll rollback after publication

## Notes
- Sprint 6 depends on a CLOSED attendance period from Sprint 5.
- Payroll calculation uses active contract + effective compensation setup.
- Paid leave day count is derived from finalized leave requests marked as paid.
- Storage file upload foundation from prior gapfill can be reused later for tax attachments, but Sprint 6 does not require physical tax document workflow.
