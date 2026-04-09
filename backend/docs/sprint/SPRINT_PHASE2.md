# CONTRACT PHASE 2

## 1. Mục tiêu phase
Xây flow import hợp đồng từ file DOCX theo hướng:

**import -> extract -> review -> confirm -> create contract draft**

Mục tiêu là để HR upload file `.docx`, hệ thống đọc dữ liệu sơ bộ, đổ ra form review, rồi chỉ khi HR xác nhận thì mới lưu vào bảng contract chính.

---

## 2. Scope đã làm

### Đã làm
- Thêm `contract import session`
- Thêm `contract import field`
- Hỗ trợ import file DOCX trực tiếp
- Tự lưu file nguồn vào storage
- Trích text từ DOCX
- Tự map các field hợp đồng phổ biến
- Trả dữ liệu gợi ý cho UI review
- HR confirm mới tạo `ct_labor_contract` ở trạng thái `DRAFT`
- Gắn file DOCX nguồn vào attachment với type `IMPORT_SOURCE`

### Chưa làm
- Import PDF text
- Import scan/image OCR
- Confidence theo OCR

---

## 3. Thiết kế database

### Migration
- `V017__contract_import_phase2.sql`

### Bảng mới
#### `ct_contract_import_session`
Các trường chính:
- `contract_import_session_id`
- `status`
- `source_file_key`
- `source_file_name`
- `source_mime_type`
- `source_type`
- `extracted_raw_text`
- `template_id`
- `employee_id`
- `confidence_score`
- `note`
- audit fields

#### `ct_contract_import_field`
Các trường chính:
- `contract_import_field_id`
- `contract_import_session_id`
- `field_code`
- `raw_value`
- `normalized_value`
- `confidence_score`
- `source_snippet`
- `display_order`

### Seed permission
- `R__contract_import_phase2_permissions.sql`

Permission mới:
- `contract.import.view`
- `contract.import.manage`

---

## 4. Kiến trúc backend

### Enum mới
- `ContractImportFieldCode`
- `ContractImportSessionStatus`
- `ContractImportSourceType`

### Service mới
- `ContractImportSessionService`

### Controller mới
- `AdminContractImportController`

### Điểm quan trọng
- Session import chỉ là lớp staging
- Không ghi thẳng vào `ct_labor_contract`
- Confirm mới tạo contract thật

---

## 5. File tạo mới
- `database/schema/V017__contract_import_phase2.sql`
- `database/seed/R__contract_import_phase2_permissions.sql`
- `src/main/java/com/company/hrm/common/constant/ContractImportFieldCode.java`
- `src/main/java/com/company/hrm/common/constant/ContractImportSessionStatus.java`
- `src/main/java/com/company/hrm/common/constant/ContractImportSourceType.java`
- `src/main/java/com/company/hrm/module/contract/entity/CtContractImportSession.java`
- `src/main/java/com/company/hrm/module/contract/entity/CtContractImportField.java`
- `src/main/java/com/company/hrm/module/contract/repository/CtContractImportSessionRepository.java`
- `src/main/java/com/company/hrm/module/contract/repository/CtContractImportFieldRepository.java`
- `src/main/java/com/company/hrm/module/contract/dto/ContractImportFieldResponse.java`
- `src/main/java/com/company/hrm/module/contract/dto/ContractImportSuggestedContractDataResponse.java`
- `src/main/java/com/company/hrm/module/contract/dto/ContractImportSessionListItemResponse.java`
- `src/main/java/com/company/hrm/module/contract/dto/ContractImportSessionDetailResponse.java`
- `src/main/java/com/company/hrm/module/contract/controller/AdminContractImportController.java`
- `src/main/java/com/company/hrm/module/contract/service/ContractImportSessionService.java`

---

## 6. File đã sửa
- `src/main/java/com/company/hrm/common/constant/ContractAttachmentType.java`
- `src/main/java/com/company/hrm/module/employee/repository/HrEmployeeRepository.java`
- `src/main/java/com/company/hrm/module/contract/repository/CtContractTypeRepository.java`
- `src/main/java/com/company/hrm/module/jobtitle/repository/HrJobTitleRepository.java`
- `src/main/java/com/company/hrm/module/orgunit/repository/HrOrgUnitRepository.java`

---

## 7. Endpoint

### Danh sách session
- `GET /api/v1/admin/contracts/import-sessions`

### Chi tiết session
- `GET /api/v1/admin/contracts/import-sessions/{contractImportSessionId}`

### Import DOCX
- `POST /api/v1/admin/contracts/import-sessions/docx`

### Confirm session
- `POST /api/v1/admin/contracts/import-sessions/{contractImportSessionId}/confirm`

### Cancel session
- `PATCH /api/v1/admin/contracts/import-sessions/{contractImportSessionId}/cancel`

---

## 8. Field đang auto-extract
- CONTRACT_NUMBER
- EMPLOYEE_CODE
- EMPLOYEE_FULL_NAME
- CONTRACT_TYPE
- SIGN_DATE
- EFFECTIVE_DATE
- END_DATE
- JOB_TITLE
- ORG_UNIT
- WORK_LOCATION
- BASE_SALARY
- SALARY_CURRENCY
- WORKING_TYPE
- NOTE

---

## 9. Logic gợi ý dữ liệu cho UI
Backend cố gắng resolve thêm:
- `employeeId`
- `contractTypeId`
- `jobTitleId`
- `orgUnitId`
- `workLocation`
- `workingType`
- `baseSalary`
- các ngày ở định dạng chuẩn

UI dùng `suggestedContractData` để auto-fill form review.

---

## 10. Cách test
1. Upload DOCX vào endpoint import session
2. Kiểm tra session được tạo
3. Mở detail session xem raw text + extracted fields
4. Kiểm tra suggested data đổ ra đúng cơ bản
5. Confirm session để tạo contract draft
6. Kiểm tra file nguồn đã gắn vào attachment `IMPORT_SOURCE`
7. Test cancel session

---

## 11. Commit message đề xuất
```bash
feat(contract): add phase2 docx import session with extract-review-confirm flow
```

---

## 12. Điều kiện freeze phase
Freeze khi đạt đủ:
- import DOCX tạo session thành công
- field extract ra được dữ liệu cơ bản
- HR review được dữ liệu gợi ý
- confirm tạo contract draft thành công
- file nguồn được lưu và gắn attachment đúng loại
- cancel session hoạt động đúng

---

## 13. Bước tiếp theo
Phase 3:
- import PDF text-based
- tái sử dụng cùng flow review/confirm
