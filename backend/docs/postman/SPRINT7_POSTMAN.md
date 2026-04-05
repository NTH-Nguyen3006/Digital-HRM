# Sprint 7 Postman smoke test

1. Employee login
2. POST `/api/v1/me/offboarding/requests`
3. Manager login, GET `/api/v1/manager/offboarding/pending`
4. Manager PATCH `/api/v1/manager/offboarding/{id}/review`
5. HR PATCH `/api/v1/admin/offboarding/{id}/finalize`
6. Manager POST checklist item
7. HR POST asset return
8. Admin PATCH revoke access
9. HR PATCH settlement
10. HR PATCH close

Portal:
1. Employee GET `/api/v1/me/portal/dashboard`
2. Employee GET `/api/v1/me/portal/profile`
3. Employee GET `/api/v1/me/portal/inbox`
4. Employee PATCH `/api/v1/me/portal/inbox/{id}/read`
