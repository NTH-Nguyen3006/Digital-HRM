# CONTRACT PHASE 1

## 1. Mục tiêu phase
Xây nền cho module hợp đồng lao động theo hướng production:
- quản lý template hợp đồng
- generate DOCX/PDF từ dữ liệu hợp đồng trong hệ thống
- lưu file sinh ra vào storage
- gắn file sinh ra vào hợp đồng dưới dạng attachment

Phase 1 **không xử lý import file từ bên ngoài** và **không làm OCR**.

---

## 2. Scope đã làm

### Đã làm
- Thêm module `contract template`
- Cho phép HR/Admin quản lý template hợp đồng
- Hỗ trợ generate file hợp đồng:
  - DOCX
  - PDF
- File generate được lưu qua storage hiện có
- File generate được gắn vào `ct_contract_attachment`
- Mở rộng response attachment để FE dễ hiển thị file hơn

### Chưa làm
- Import DOCX/PDF/image
- Extract dữ liệu từ file
- Review trước khi lưu contract
- OCR scan/image

---

## 3. Thiết kế database

### Migration
- `V016__contract_template_phase1.sql`

### Bảng mới
#### `ct_contract_template`
Dùng để quản lý mẫu hợp đồng.

Các trường chính:
- `contract_template_id`
- `template_code`
- `template_name`
- `document_type`
- `template_file_key`
- `html_template`
- `status`
- `description`
- audit fields
- soft delete
- row version

### Seed permission
- `R__contract_template_phase1_permissions.sql`

Permission mới:
- `contract.template.view`
- `contract.template.manage`
- `contract.generate`

---

## 4. Kiến trúc backend

### Module mới / mở rộng
- `module/contract/entity/CtContractTemplate`
- `module/contract/repository/CtContractTemplateRepository`
- `module/contract/service/ContractTemplateService`
- `module/contract/service/ContractDocumentGenerationService`
- `module/contract/controller/AdminContractTemplateController`

### Kỹ thuật generate file
- DOCX: Apache POI XWPF
- PDF: OpenHTMLToPDF

### Storage
Tận dụng module storage hiện có:
- file template upload qua storage
- file output generate cũng lưu qua storage
- contract attachment giữ reference tới file đã lưu

---

## 5. File tạo mới
- `database/schema/V016__contract_template_phase1.sql`
- `database/seed/R__contract_template_phase1_permissions.sql`
- `src/main/java/com/company/hrm/common/constant/ContractTemplateDocumentType.java`
- `src/main/java/com/company/hrm/module/contract/entity/CtContractTemplate.java`
- `src/main/java/com/company/hrm/module/contract/repository/CtContractTemplateRepository.java`
- `src/main/java/com/company/hrm/module/contract/dto/ContractTemplateUpsertRequest.java`
- `src/main/java/com/company/hrm/module/contract/dto/ContractTemplateListItemResponse.java`
- `src/main/java/com/company/hrm/module/contract/dto/ContractTemplateDetailResponse.java`
- `src/main/java/com/company/hrm/module/contract/dto/ContractTemplateStatusUpdateRequest.java`
- `src/main/java/com/company/hrm/module/contract/dto/GenerateContractDocumentRequest.java`
- `src/main/java/com/company/hrm/module/contract/controller/AdminContractTemplateController.java`
- `src/main/java/com/company/hrm/module/contract/service/ContractTemplateService.java`
- `src/main/java/com/company/hrm/module/contract/service/ContractDocumentGenerationService.java`

---

## 6. File đã sửa
- `pom.xml`
- `src/main/java/com/company/hrm/module/contract/controller/AdminLaborContractController.java`
- `src/main/java/com/company/hrm/module/contract/service/LaborContractService.java`
- `src/main/java/com/company/hrm/module/contract/dto/ContractAttachmentResponse.java`

---

## 7. Endpoint

### Contract template
- `GET /api/v1/admin/contract-templates`
- `GET /api/v1/admin/contract-templates/{templateId}`
- `POST /api/v1/admin/contract-templates`
- `PUT /api/v1/admin/contract-templates/{templateId}`
- `PATCH /api/v1/admin/contract-templates/{templateId}/status`

### Generate file hợp đồng
- `POST /api/v1/admin/contracts/{laborContractId}/generate-docx`
- `POST /api/v1/admin/contracts/{laborContractId}/generate-pdf`

---

## 8. Request/response mẫu

### Tạo template
```json
{
  "templateCode": "LABOR_CONTRACT_STD_V1",
  "templateName": "Mẫu HĐLĐ chuẩn V1",
  "documentType": "LABOR_CONTRACT",
  "templateFileKey": "file_key_docx_da_upload",
  "htmlTemplate": "<h1>HỢP ĐỒNG LAO ĐỘNG</h1><p>${employee_full_name}</p>",
  "status": "ACTIVE",
  "description": "Mẫu hợp đồng lao động chuẩn"
}
```

### Generate DOCX/PDF
```json
{
  "templateId": 1
}
```

---

## 9. Placeholder đang hỗ trợ
- `${contract_id}`
- `${contract_number}`
- `${employee_id}`
- `${employee_code}`
- `${employee_full_name}`
- `${contract_type_code}`
- `${contract_type_name}`
- `${sign_date}`
- `${effective_date}`
- `${end_date}`
- `${job_title_code}`
- `${job_title_name}`
- `${org_unit_code}`
- `${org_unit_name}`
- `${work_location}`
- `${base_salary}`
- `${salary_currency}`
- `${working_type}`
- `${contract_status}`
- `${signed_by_company_username}`
- `${note}`
- `${today}`

---

## 10. Cách test
1. Upload file DOCX template vào storage
2. Tạo `contract template`
3. Gọi generate DOCX
4. Gọi generate PDF
5. Kiểm tra file sinh ra đã lưu vào storage
6. Kiểm tra `ct_contract_attachment` có record tương ứng

---

## 11. Commit message đề xuất
```bash
feat(contract): add phase1 contract templates and DOCX/PDF generation
```

---

## 12. Điều kiện freeze phase
Freeze khi đạt đủ:
- upload template qua storage thành công
- tạo/sửa template thành công
- generate DOCX thành công
- generate PDF thành công
- attachment response trả ra đủ file metadata cho FE

---

## 13. Bước tiếp theo
Phase 2:
- import DOCX
- extract field
- HR review
- confirm mới tạo contract draft
