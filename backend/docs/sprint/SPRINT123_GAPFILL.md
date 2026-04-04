# Sprint 1-2-3 Gap Fill

Bản cập nhật này bổ sung các phần còn thiếu để source tiến gần trạng thái production-ready theo backlog đã chốt cho Sprint 1, 2, 3.

## Sprint 1 bổ sung
- PATCH status cho role

## Sprint 2 bổ sung
- Permission matrix
- Role menu config
- Notification templates
- Platform settings
- Org unit / job title import-export CSV

## Sprint 3 bổ sung
- Employee profile change request workflow
- Profile lock/restore
- Profile timeline
- Employee import-export CSV
- Onboarding core
- Contract export HTML

## Lưu ý
- Đã bổ sung storage foundation riêng để upload/download file vật lý thật qua `sys_stored_file` + `/api/v1/storage/files`.
- Contract export hiện dùng HTML template ổn định để FE/ops có thể chuyển tiếp sang PDF nếu cần.
- Chưa verify build Maven trong môi trường hiện tại do container không có Maven/dependency cache.
