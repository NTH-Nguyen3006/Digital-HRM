# Sprint 4 Postman Checklist

1. Login bằng tài khoản EMPLOYEE có employee linkage.
2. Tạo đơn nghỉ phép bằng `/api/v1/me/leave/requests`.
3. Login MANAGER cùng cây org, duyệt bằng `/api/v1/manager/leave/requests/{id}/review`.
4. Login HR, finalize bằng `/api/v1/admin/leave/requests/{id}/finalize`.
5. Kiểm tra số dư thay đổi ở `/api/v1/admin/leave/balances/{id}` hoặc `/api/v1/me/leave/balances`.
6. Chạy year close bằng `/api/v1/admin/leave/period-close`.
7. Chạy settlement khi cần.
8. Manager xác nhận orientation.
9. HR complete onboarding.
10. HR gửi notify onboarding và kiểm tra log trong detail onboarding.
