# CONTRACT PHASE 3

## 1. Mục tiêu phase
Mở rộng flow import của Phase 2 để hỗ trợ:

**PDF text-based -> extract -> review -> confirm**

Chỉ hỗ trợ PDF có lớp text thật. PDF scan chưa làm trong phase này.

---

## 2. Scope đã làm

### Đã làm
- Bổ sung import PDF text-based
- Dùng PDFBox để strip text
- Tái sử dụng flow review/confirm của phase 2
- Tái sử dụng session import + field extract
- Chặn rõ PDF scan rỗng text bằng lỗi riêng

### Chưa làm
- PDF scan OCR
- Image OCR
- Confidence dựa trên OCR

---

## 3. Thiết kế database

### Migration
- `V018__contract_import_phase3_pdf_text.sql`

### Nội dung migration
- mở rộng constraint `source_type` của `ct_contract_import_session`
- cho phép:
  - `DOCX`
  - `PDF_TEXT`

Không tạo bảng mới trong phase này.

---

## 4. Kiến trúc backend

### Dependency mới
- `org.apache.pdfbox:pdfbox:2.0.35`

### Mở rộng service
`ContractImportSessionService` được bổ sung:
- `createPdfImportSession(...)`
- `extractFromPdfText(...)`
- refactor logic parse chung sang `extractFromRawText(...)`

### Controller
`AdminContractImportController` có thêm endpoint import PDF.

---

## 5. File tạo mới
- `database/schema/V018__contract_import_phase3_pdf_text.sql`

---

## 6. File đã sửa
- `pom.xml`
- `src/main/java/com/company/hrm/common/constant/ContractImportSourceType.java`
- `src/main/java/com/company/hrm/module/contract/controller/AdminContractImportController.java`
- `src/main/java/com/company/hrm/module/contract/service/ContractImportSessionService.java`

---

## 7. Endpoint mới

### Import PDF text-based
- `POST /api/v1/admin/contracts/import-sessions/pdf`

`multipart/form-data`
- `file`
- `templateId` optional
- `note` optional

---

## 8. Logic nghiệp vụ

### Nếu PDF có text
- strip text bằng PDFBox
- parse field như phase 2
- tạo import session
- trả dữ liệu review
- confirm -> tạo contract draft

### Nếu PDF không có text
- fail request
- trả code lỗi riêng để phân biệt với OCR phase sau

### Lỗi nghiệp vụ chính
- `CONTRACT_IMPORT_PDF_TEXT_EMPTY`
- message: PDF hiện không có lớp text trích xuất được, phase 3 chỉ hỗ trợ PDF text-based

---

## 9. Cách test
1. Import PDF text-based hợp lệ
2. Kiểm tra session được tạo với `sourceType = PDF_TEXT`
3. Kiểm tra extracted fields và suggested data
4. Confirm session để tạo contract draft
5. Import PDF scan không có text và kiểm tra lỗi `CONTRACT_IMPORT_PDF_TEXT_EMPTY`

---

## 10. Commit message đề xuất
```bash
feat(contract): add phase3 text-based pdf import for contract review flow
```

---

## 11. Điều kiện freeze phase
Freeze khi đạt đủ:
- import PDF text-based thành công
- extracted fields và suggested data ra đúng cơ bản
- confirm session PDF tạo contract draft thành công
- PDF scan bị chặn đúng message OCR chưa hỗ trợ

---

## 12. Bước tiếp theo
Phase 4:
- PDF scan OCR
- image OCR
- confidence score phản ánh chất lượng OCR
