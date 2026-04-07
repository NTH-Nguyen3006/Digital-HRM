# 🧠 DIGITAL HRM - MASTER ARCHITECTURE & UI GUIDELINES (v2.0)

Tài liệu này là BỘ LUẬT THÉP (Steel Laws) dành cho quá trình phát triển Frontend (Vue 3) của hệ thống Digital HRM. Mọi AI hoặc Engineer khi tham gia dự án **BẮT BUỘC PHẢI ĐỌC, HIỂU VÀ TUÂN THỦ 100%**. Mọi sự vi phạm, tự ý chế class, ghi đè kiến trúc đều sẽ làm hỏng tính đồng nhất của hệ thống.

---

## 1. ARCHITECTURE, TECH STACK & NAMING CONVENTION

**Công nghệ lõi:** Vue 3 + `<script setup>` + Composition API + Tailwind CSS v4.

**Quy tắc Đặt tên & Thư mục (FILE & NAMING CONVENTION):**
- **Views (Màn hình):** Đặt trong `views/<module>/`. Hậu tố bắt buộc là `*View.vue` hoặc tên danh từ rõ ràng (Ví dụ: `EmployeeListView.vue`, `LeaveRequestView.vue`).
- **Components (Thành phần con):** Đặt trong `components/<module>/`. Viết hoa PascalCase (Ví dụ: `EmployeeCard.vue`, `PayrollTable.vue`).
- **API:** Phân tách theo role `api/<role>/<domain>.js` (Ví dụ: `api/manager/leave.js`).
- **Store:** `stores/<domain>.js` (Ví dụ: `auth.js`).
🚨 **Luật cấm:** KHÔNG bao giờ đặt tên chung chung thiếu context Domain (VD: `test.vue`, `index.vue`, `data.js`).

**Kiến trúc Persistent Layout:**
Cơ chế Layout được điều khiển **TẬP TRUNG** tại `App.vue`. Việc của View chỉ là định nghĩa nội dung cho vùng `<router-view>`.
🚨 **Luật cấm:** CẤM TUYỆT ĐỐI việc import `<MainLayout.vue>` vào trong các file views (Tránh chớp trang, mất state cuộn).

---

## 2. ROUTER & NAVIGATION RULES

- **Named Routes:** Tuyệt đối KHÔNG navigate bằng hardcode path string (VD: `router.push('/dashboard')`). BẮT BUỘC dùng Named Routes (VD: `router.push({ name: 'Dashboard' })`).
- **Role-based Access & Auth Guard:** Logic bảo vệ route dựa vào `meta.roles`. 
- **Redirect Flow chuẩn:**
  - Chưa login $\rightarrow$ Ép về `/login`.
  - Login thành công $\rightarrow$ Chuyển về Dashboard tương ứng của Role.
  - Cố tình vào route không đủ quyền $\rightarrow$ Redirect về trang `403 Unauthorized` hoặc Dashboard.

---

## 3. DESIGN SYSTEM & UI RULES

Hệ thống tuân thủ **Glassmorphism** (`.glass`, `.glass-dark`) và màu sắc từ `@theme` (Primary: Indigo, Success: Emerald, Danger: Rose).
CẤM DÙNG thư viện icon ngoài. Bắt buộc dùng `lucide-vue-next`.

### 3.1 BASE COMPONENTS (CRITICAL)
- `BaseButton`: Nút bấm duy nhất được dùng. Nhận `:variant`, `:size`, `:loading`, `:disabled`.
- `BaseInput`: Ô nhập liệu duy nhất. Nhận `v-model`, `:error` (Báo chữ đỏ/viền đỏ), `:required`.
- `GlassCard` / `StatCard`: Dùng để bọc form và hiển thị số liệu Dashboard.
🚨 **Luật cấm:** KHÔNG dùng `<button>`, `<input>`, `<select>` thuần HTML. KHÔNG tự chế class CSS rác.

### 3.2 TABLE / DATAGRID STANDARD (CẤU TRÚC BẢNG CHUẨN)
Mọi danh sách dạng bảng phải tuân thủ layout sau:
- **Wrapper:** Bắt buộc bọc bảng trong thẻ div có class `overflow-x-auto` để chống vỡ layout trên Mobile.
- **Header (`thead`):** `bg-slate-50 uppercase tracking-wider text-slate-500 text-xs font-bold`.
- **Body (`tbody`):** Row phải có hiệu ứng `hover:bg-slate-50/50 transition-colors`.
- **Action Column:** Cột cuối cùng luôn là thao tác (Edit/Delete/View) dùng icon Lucide hoặc BaseButton `size="sm"`.
- **States (3 Trạng thái bảng):**
  1. `Loading`: Render Skeleton Row.
  2. `Empty`: Render 1 Row gộp cột (colspan) text "Không có dữ liệu".
  3. `Success`: Render v-for rows.
- **Pagination:** Phải có control phân trang ở dưới bảng. Lên API gọi `page - 1`.

---

## 4. FORM DATA FLOW & ERROR HANDLING UX STANDARD

Đây là luồng "Sinh - Lão - Bệnh - Tử" bắt buộc cho MỌI Form (Submit Flow) trong hệ thống:

**Quy trình 7 bước (BẤT DI BẤT DỊCH):**
1. `Validate Frontend:` Kiểm tra rỗng, sai format TRƯỚC KHI call API.
2. `Map Error:` Nếu fail $\rightarrow$ gán lỗi vào biến map với prop `BaseInput :error` (Báo đỏ ngay tại ô nhập). Không call API.
3. `Pre-flight:` Nếu pass $\rightarrow$ Set `isLoading.value = true` (Lập tức disable nút BaseButton).
4. `Call API:` Bọc trong `try/catch`. 
5. `Success:` Hiện Toast xanh $\rightarrow$ Phải thực hiện: `Reset Form` HOẶC `Đóng Modal + Reload List Data` HOẶC `Redirect`.
6. `Catch Error:` Bắt lỗi Axios $\rightarrow$ Gán vào `errorMessage` tổng của Form hoặc Toast báo lỗi. KHÔNG ĐƯỢC NUỐT LỖI (Swallow error).
7. `Finally:` Luôn luôn set `isLoading.value = false` để mở khóa UI.

**Phân cấp UX báo lỗi (Error Handling UX):**
1. Lỗi Field (Nhập thiếu, sai format) $\rightarrow$ Hiện dưới `BaseInput` (`:error`).
2. Lỗi Business API (VD: "Hợp đồng đã tồn tại") $\rightarrow$ Hiện message cảnh báo màu `rose-600` ngay trên đầu Form.
3. Lỗi Global (500 Server, Mất mạng) $\rightarrow$ Bắn Toast/Snackbar góc màn hình.

---

## 5. ACTOR-BASED UI (HRM CORE)

Hệ thống có 4 Roles, yêu cầu UI/API mapping khắt khe:
- **Employee:** Xem thông tin cá nhân (`portal.js`), Submit yêu cầu (Chỉ gửi đi).
- **Manager:** Duyệt/Từ chối (`manager.js`). ❗ **Luật thép:** Nút REJECT bắt buộc mở Modal yêu cầu nhập lý do.
- **HR / Admin:** Data Grid lớn (`employee.js`, `payroll.js`), cấu hình hệ thống, chốt dữ liệu đa luồng.
🚨 **Luật cấm:** CẤM hardcode quyền. CẤM gọi chéo API của Actor này cho Actor khác.

---

## 6. SEARCH & FILTER STANDARD

- Mọi search/filter phải:
  - debounce >= 500ms
  - reset `currentPage = 1`
  - call lại API

- Query format:
  - search → `keyword`
  - filter → params rõ ràng (status, date...)

- Clear filter:
  - reset toàn bộ params
  - reload list

🚨 RULE:
- KHÔNG giữ page cũ khi search/filter thay đổi

---

## 7. API RESPONSE MAPPING RULE

Chuẩn hóa:

Backend (Spring Pageable):
- content
- totalPages
- totalElements

Frontend phải map:

```js
list.value = response.content
totalPages.value = response.totalPages
totalItems.value = response.totalElements
```

---

## 8. AI SOP — CODING PROTOCOL (QUY TRÌNH ÉP BUỘC)

Mỗi Task AI code phải rà soát qua 4 bước:

1. **Context Check:** Đọc file nghiệp vụ (Excel/Sheet). Xác định Role $\rightarrow$ Chọn đúng file API.
2. **UI Mapping:** Tái sử dụng `BaseButton`, `BaseInput`, `GlassCard`. Xác định 3 state (Loading/Empty/Success).
3. **Implementation:** - Viết code Vue 3 đúng chuẩn Persistent Layout.
   - Mapping Response $\rightarrow$ UI chính xác.
   - Thêm Confirm Dialog (Xác nhận) TRƯỚC các hành động DELETE/REJECT.
4. **Self-QA (Bắt Buộc):** - Role đúng chưa? API gọi đúng chưa?
   - Validate Frontend TRƯỚC submit làm chưa? 
   - Sau khi mutation (Tạo/Sửa/Xóa) đã gọi lại hàm fetch để Reload List chưa?
   - Nút đã `:disabled` khi loading chưa? 
   - Pagination trừ 1 chưa?

---

## 🔒 VÙNG CHỐT: THE 6 STEEL LAWS (LUẬT)

1. **Architecture Laws:** Code ở `<script setup>`. **CẤM** import Layout ở Page Level. Named Routes ONLY.
2. **UI Laws:** Dùng BaseComponent thuần túy. Bảng phải có `overflow-x-auto` và Skeleton loading.
3. **Form & Data Laws:** Validate trước $\rightarrow$ Set Loading $\rightarrow$ Call API $\rightarrow$ Reload/Reset $\rightarrow$ Tắt Loading. Không bao giờ im lặng khi có lỗi.
4. **API Laws:** Async always `try/catch`. 500ms Debouncing Search-field. Pagination `-1` xuống API.
5. **Mutation Laws:** Các hành động mang tính phá hủy (Xóa/Hủy/Từ chối) **BẮT BUỘC** phải có bước Confirm.
6. **Role Laws:** Hành vi ai người nấy làm. Import lộn hàm API của Role khác = Phá hủy hệ thống.
