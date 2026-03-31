# Sprint 1-2-3 Gap Fill API

## Role
- PATCH /api/v1/admin/roles/{roleId}/status

## System config
- GET /api/v1/admin/permissions/matrix
- GET, PUT /api/v1/admin/roles/{roleId}/menu-configs
- GET, POST, PUT /api/v1/admin/notification-templates
- GET, PUT /api/v1/admin/platform-settings

## Import/export
- GET /api/v1/admin/org-units/export
- POST /api/v1/admin/org-units/import
- GET /api/v1/admin/job-titles/export
- POST /api/v1/admin/job-titles/import
- GET /api/v1/admin/employees/export
- POST /api/v1/admin/employees/import

## Employee profile workflow
- POST /api/v1/me/profile-change-requests
- GET /api/v1/me/profile-change-requests
- GET /api/v1/admin/profile-change-requests
- PATCH /api/v1/admin/profile-change-requests/{requestId}/review
- PATCH /api/v1/admin/employees/{employeeId}/profile-lock
- PATCH /api/v1/admin/employees/{employeeId}/profile-restore
- GET /api/v1/admin/employees/{employeeId}/profile-timeline
- GET /api/v1/admin/employees/managed

## Onboarding
- GET /api/v1/admin/onboarding
- GET /api/v1/admin/onboarding/{onboardingId}
- POST /api/v1/admin/onboarding
- PUT /api/v1/admin/onboarding/{onboardingId}
- POST/PUT checklist, documents, assets
- POST /api/v1/admin/onboarding/{onboardingId}/create-user
- POST /api/v1/admin/onboarding/{onboardingId}/initial-contract

## Contract export
- GET /api/v1/admin/contracts/{laborContractId}/export
- GET /api/v1/admin/contracts/{laborContractId}/appendices/{appendixId}/export
