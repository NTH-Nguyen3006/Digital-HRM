# SPRINT PHASE 4 - Contract OCR Import

## 1. Mục tiêu phase
Triển khai import hợp đồng lao động từ **PDF scan** và **hình ảnh** bằng OCR theo luồng production:

`upload file -> OCR -> extract field -> HR review -> confirm -> create contract draft + attach source file`

Phase này **không thay đổi** workflow review/confirm đã có từ phase 2 và phase 3, chỉ mở rộng thêm nguồn nhập OCR.

---

## 2. Scope đã hoàn thành

### Đã làm
- Hỗ trợ import **PDF scan** bằng OCR
- Hỗ trợ import **ảnh hợp đồng** bằng OCR
- Thêm config hệ thống cho OCR trong `application.yml`
- Thêm service OCR riêng để gọi Tesseract CLI
- Mở rộng `ContractImportSourceType` với:
  - `PDF_OCR`
  - `IMAGE_OCR`
- Mở rộng `ContractImportSessionService` để xử lý OCR
- Thêm endpoint import OCR cho admin/HR
- Điều chỉnh logic confidence để phản ánh chất lượng OCR thực tế
- Vẫn giữ flow:
  - import session
  - HR review
  - confirm mới tạo `ct_labor_contract`

### Chưa làm trong phase này
- OCR bảng biểu phức tạp theo layout nâng cao
- AI semantic mapping nâng cao
- Auto-detect chữ ký/số trang/mục lục
- Batch import nhiều file cùng lúc
- Tự động chuẩn hóa dữ liệu OCR mức nâng cao

---

## 3. Thiết kế kỹ thuật

### 3.1. Source type mở rộng
Enum `ContractImportSourceType` sau phase 4:
- `DOCX`
- `PDF_TEXT`
- `PDF_OCR`
- `IMAGE_OCR`

### 3.2. OCR runtime
OCR hoạt động qua Tesseract CLI, được cấu hình bằng:

```yaml
app:
  ocr:
    enabled: false
    tesseract-binary: tesseract
    language: vie+eng
    page-seg-mode: 6
    pdf-render-dpi: 300
    max-pdf-pages: 10
    process-timeout-seconds: 90
```

### 3.3. Luồng PDF OCR
1. Upload PDF scan
2. Lưu file vào storage
3. Render từng trang PDF ra ảnh bằng PDFBox
4. OCR từng trang bằng Tesseract
5. Gộp text OCR
6. Extract field hợp đồng
7. Trả import session để HR review
8. HR confirm -> tạo contract draft

### 3.4. Luồng image OCR
1. Upload file ảnh
2. Lưu file vào storage
3. OCR ảnh bằng Tesseract
4. Extract field hợp đồng
5. Trả import session để HR review
6. HR confirm -> tạo contract draft

---

## 4. Database migration

### File migration mới
- `database/schema/V019__contract_import_phase4_ocr.sql`

### Nội dung chính
- Update constraint `source_type` của `ct_contract_import_session`
- Cho phép thêm các giá trị:
  - `PDF_OCR`
  - `IMAGE_OCR`

---

## 5. File tạo mới / chỉnh sửa

### Tạo mới
- `database/schema/V019__contract_import_phase4_ocr.sql`
- `src/main/java/com/company/hrm/module/contract/service/ContractOcrService.java`

### Chỉnh sửa
- `src/main/java/com/company/hrm/config/AppProperties.java`
- `src/main/resources/application.yml`
- `src/main/java/com/company/hrm/common/constant/ContractImportSourceType.java`
- `src/main/java/com/company/hrm/module/contract/controller/AdminContractImportController.java`
- `src/main/java/com/company/hrm/module/contract/service/ContractImportSessionService.java`

---

## 6. Endpoint đã bổ sung

### 6.1. Import PDF scan bằng OCR
**POST** `/api/v1/admin/contracts/import-sessions/pdf-ocr`

**multipart/form-data**
- `file`: PDF scan
- `templateId`: optional
- `note`: optional

### 6.2. Import ảnh bằng OCR
**POST** `/api/v1/admin/contracts/import-sessions/image-ocr`

**multipart/form-data**
- `file`: image
- `templateId`: optional
- `note`: optional

### 6.3. Confirm import session
Giữ nguyên API cũ:

**POST** `/api/v1/admin/contracts/import-sessions/{contractImportSessionId}/confirm`

---

## 7. Logic nghiệp vụ chính

### 7.1. Điều kiện chạy OCR
- Chỉ chạy khi `app.ocr.enabled=true`
- Nếu OCR đang tắt -> trả lỗi rõ ràng

### 7.2. Giới hạn PDF OCR
- Có giới hạn số trang qua `app.ocr.max-pdf-pages`
- Mặc định: `10 trang`

### 7.3. File ảnh hỗ trợ
- PNG
- JPG
- JPEG
- BMP

### 7.4. Confidence score
Confidence không còn để đẹp cho vui.

Phase 4 dùng confidence thực tế của OCR để scale độ tin cậy field:
- OCR rõ -> confidence cao hơn
- OCR mờ/lệch -> confidence thấp hơn

---

## 8. Kết quả đầu ra sau phase 4

Sau khi import OCR thành công, backend trả về:
- import session
- raw text từ OCR
- danh sách field extract được
- `suggestedContractData`
- `confidenceScore`

HR sẽ review dữ liệu này trước khi confirm.

Sau khi confirm:
- tạo `ct_labor_contract` ở trạng thái `DRAFT`
- gắn file nguồn vào `ct_contract_attachment` với loại `IMPORT_SOURCE`

---

## 9. Cách test

### Case 1. OCR disabled
- Set `app.ocr.enabled=false`
- Gọi API `/pdf-ocr` hoặc `/image-ocr`
- Kỳ vọng: trả lỗi rõ ràng OCR chưa được bật

### Case 2. PDF scan OCR success
- Bật OCR
- Upload PDF scan rõ nét
- Kỳ vọng:
  - tạo import session thành công
  - source type = `PDF_OCR`
  - có `fields`
  - có `suggestedContractData`

### Case 3. Image OCR success
- Upload ảnh hợp đồng rõ nét
- Kỳ vọng:
  - tạo import session thành công
  - source type = `IMAGE_OCR`
  - có dữ liệu review

### Case 4. Confirm OCR session
- Gọi API confirm
- Kỳ vọng:
  - tạo contract draft
  - session = `CONFIRMED`
  - có attachment loại `IMPORT_SOURCE`

### Case 5. Confidence hợp lý
- Test 1 file scan rõ và 1 file scan mờ
- Kỳ vọng:
  - file rõ có confidence cao hơn file mờ

---

## 10. Phụ thuộc hạ tầng

Phase 4 cần server có cài **Tesseract OCR**.

Ví dụ Ubuntu:

```bash
sudo apt-get install tesseract-ocr
sudo apt-get install tesseract-ocr-vie
sudo apt-get install tesseract-ocr-eng
```

Nếu chạy Docker hoặc server production, cần đảm bảo:
- binary `tesseract` có trong PATH
- đủ language pack `vie` và `eng`

---

## 11. Commit message đề xuất

```bash
feat(contract): add phase4 OCR import for scanned pdf and image contracts
```

---

## 12. Điều kiện freeze phase 4
Phase 4 được freeze khi:
- OCR disabled trả lỗi đúng
- PDF OCR import thành công
- image OCR import thành công
- confirm session OCR tạo contract draft đúng
- confidence phản ánh chất lượng file hợp lý
- attachment file nguồn được lưu đúng

---

## 13. Sprint/phase tiếp theo nên làm gì
Gợi ý phase tiếp theo:
- highlight field confidence thấp trên UI review
- bắt HR xác nhận tay các field nghi ngờ
- hỗ trợ OCR nâng cao cho file chất lượng thấp
- hỗ trợ batch import
- thêm audit chi tiết cho thao tác review/override field

