# Sprint 3 - Contract Legal Core

## Scope chốt cho lần bàn giao này
Sprint 3 trong backlog tổng có nhiều hạng mục hơn, nhưng ERD người dùng đã chốt hiện tại chỉ bao phủ **contract legal core**. Vì nguyên tắc freeze sprint và tránh thay đổi ERD giữa chừng, lần bàn giao này chỉ triển khai:

- Contract type master (`ct_contract_type`)
- Labor contract core (`ct_labor_contract`)
- Contract appendix (`ct_contract_appendix`)
- Contract attachment metadata (`ct_contract_attachment`)
- Contract status history (`ct_contract_status_history`)
- Permission seed cho contract module

## Workflow thống nhất áp dụng cho contract
- Tạo nháp: `DRAFT`
- Gửi chờ ký: `PENDING_SIGN`
- Duyệt:
  - Approve: vẫn giữ `PENDING_SIGN`, gán người ký công ty và ghi history cùng trạng thái
  - Reject: trả về `DRAFT`
- Chốt hiệu lực: `ACTIVE`
- Vòng đời sau hiệu lực:
  - `ACTIVE -> SUSPENDED`
  - `SUSPENDED -> ACTIVE`
  - `ACTIVE/SUSPENDED -> TERMINATED`
  - `ACTIVE/PENDING_SIGN -> EXPIRED` khi cần xử lý nghiệp vụ

## Out of scope của lần bàn giao này
- Onboarding
- Self-service update profile request của employee
- Engine xuất PDF/Word thật từ template
- File storage engine vật lý, upload binary thật
- Scheduled job auto-expire

## Migration / seed
- `database/schema/V005__contract_core.sql`
- `database/seed/R__sprint3_contract_permissions.sql`

## API root
- `/api/v1/admin/contract-types`
- `/api/v1/admin/contracts`

## Permission summary
- `contract.type.view/create/update/change_status`
- `contract.view/create/update/cancel_draft/submit/review/activate/change_status`
- `contract.history.view`
- `contract.appendix.manage`
- `contract.attachment.manage`
