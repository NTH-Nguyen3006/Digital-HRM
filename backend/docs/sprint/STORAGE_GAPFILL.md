# Storage Gap Fill - Physical File Upload/Download Foundation

## Mục tiêu
Bổ sung phần còn thiếu của Sprint 1-3: upload/download file vật lý thật, thay vì chỉ lưu metadata attachment/document.

## Phạm vi
- Generic storage metadata table: `sys_stored_file`
- Upload file vật lý vào local/internal storage
- Download file vật lý theo `fileKey`
- Metadata endpoint để FE lấy thông tin file
- Validate `storagePath`/`attachmentRef` tồn tại trước khi gắn vào:
  - employee document
  - contract attachment
  - onboarding document
  - leave attachment

## Workflow tích hợp chuẩn cho FE
1. Gọi `POST /api/v1/storage/files` với multipart file.
2. Nhận về `fileKey` và dùng chính giá trị này làm `storagePath` hoặc `attachmentRef` cho API nghiệp vụ hiện có.
3. Khi cần tải file, gọi `GET /api/v1/storage/files/{fileKey}/download`.

## VisibilityScope gợi ý
- `HR_ADMIN`: hồ sơ HR, onboarding docs, contract docs
- `REVIEW_FLOW`: file nghỉ phép cần manager review
- `SELF_ONLY`: file riêng chỉ uploader xem lại
- `INTERNAL`: file vận hành nội bộ, chỉ ADMIN/HR

## Ghi chú
- Bản này dùng internal local storage theo `app.storage.base-dir`.
- `storagePath` trong các bảng nghiệp vụ giờ nên hiểu là **opaque file key**, không phải OS path thật.
- Chưa làm signed URL/object storage/S3/minio ở bản này để giữ monolith ổn định, dễ commit và không phá sprint cũ.
