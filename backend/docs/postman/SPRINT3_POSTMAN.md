# Sprint 3 Postman smoke test - Contract Legal Core

1. Login admin hoặc HR: `POST /api/v1/auth/login`
2. Tạo contract type:
   - `POST /api/v1/admin/contract-types`
3. Lấy option contract type:
   - `GET /api/v1/admin/contract-types/options`
4. Tạo draft hợp đồng:
   - `POST /api/v1/admin/contracts`
5. Cập nhật draft:
   - `PUT /api/v1/admin/contracts/{id}`
6. Gửi chờ ký:
   - `PATCH /api/v1/admin/contracts/{id}/submit`
7. Duyệt:
   - `PATCH /api/v1/admin/contracts/{id}/review`
8. Kích hoạt:
   - `PATCH /api/v1/admin/contracts/{id}/activate`
9. Tạo phụ lục:
   - `POST /api/v1/admin/contracts/{id}/appendices`
10. Kích hoạt phụ lục:
   - `PATCH /api/v1/admin/contracts/{id}/appendices/{appendixId}/activate`
11. Gắn metadata file:
   - `POST /api/v1/admin/contracts/{id}/attachments`
12. Xem history:
   - `GET /api/v1/admin/contracts/{id}/history`
13. Xem cảnh báo sắp hết hạn:
   - `GET /api/v1/admin/contracts/expiring?days=30`
