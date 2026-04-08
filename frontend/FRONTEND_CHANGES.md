# 📋 FRONTEND_CHANGES.md
## Digital HRM — Nhật ký thay đổi Frontend

> **Ngày bắt đầu:** 08/04/2026  
> **Người thực hiện:** AI Assistant  
> **Phạm vi:** Toàn bộ `frontend/src/`

---

## 🗂️ Tổng quan cấu trúc thư mục mới

```
frontend/src/
├── composables/              
│   ├── useDebounce.js
│   ├── usePagination.js
│   ├── useToast.js
│   └── useRoleGuard.js
│
├── stores/
│   ├── auth.js               
│   └── ui.js                 
│
├── components/common/
│   ├── AvatarBox.vue         
│   ├── BaseButton.vue        
│   ├── BaseInput.vue         
│   ├── EmptyState.vue        
│   ├── ErrorBoundary.vue     
│   ├── GlassCard.vue         
│   ├── StatCard.vue          
│   ├── StatusBadge.vue       
│   ├── ToastNotification.vue 
│   ├── ConfirmDialog.vue     
│   └── PageHeader.vue        
│
├── layouts/
│   ├── MainLayout.vue        
│   ├── AuthLayout.vue        
│   └── PortalLayout.vue      
│
├── App.vue                   
│
└── views/
    ├── dashboard/
    │   └── manager/          
    └── portal/               
```

---

## 📝 Chi tiết từng thay đổi

---

### 1. `src/App.vue` — Cập nhật

**Lý do:** Mount các global UI components (Toast, ConfirmDialog) một lần duy nhất ở root thay vì import lặp lại ở mỗi view.

**Thay đổi:**
```diff
+ import ToastNotification from '@/components/common/ToastNotification.vue'
+ import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

  <template>
    <ErrorBoundary>
      ...
    </ErrorBoundary>
+   <ToastNotification />
+   <ConfirmDialog />
  </template>
```

---

### 2. `src/stores/ui.js` — Tạo mới

**Mục đích:** Quản lý trạng thái UI toàn cục — Toast notifications và Confirm dialog.

**API:**
```js
const ui = useUiStore()

// Toast
ui.addToast({ type: 'success', message: 'Lưu thành công!' })
ui.addToast({ type: 'error',   message: 'Có lỗi xảy ra!' })
ui.addToast({ type: 'warning', message: 'Cảnh báo!' })

// Confirm dialog (trả về Promise<boolean>)
const confirmed = await ui.confirm({
  title: 'Xóa nhân viên?',
  message: 'Hành động này không thể hoàn tác.',
  confirmLabel: 'Xóa',
  danger: true,
})
if (confirmed) { /* proceed */ }
```

---

### 3. `src/composables/useDebounce.js` — Tạo mới

**Mục đích:** Debounce reactive value, dùng cho các ô tìm kiếm để tránh gọi API liên tục khi user gõ.

**Sử dụng:**
```js
import { useDebounce } from '@/composables/useDebounce'

const searchKeyword = ref('')
const { debouncedValue: debouncedSearch } = useDebounce(searchKeyword, 400)

watch(debouncedSearch, () => fetchData())
```

---

### 4. `src/composables/usePagination.js` — Tạo mới

**Mục đích:** Gom toàn bộ logic phân trang (page, size, totalPages, v.v.) vào một composable tái sử dụng. Tương thích với định dạng `PageResponse` của Spring Boot.

**Sử dụng:**
```js
import { usePagination } from '@/composables/usePagination'

const {
  currentPage, pageSize, totalElements, totalPages,
  isFirstPage, isLastPage, visiblePages,
  setPage, nextPage, prevPage, resetPage, setMeta
} = usePagination({ initialSize: 12 })

// Sau khi gọi API:
setMeta({ totalElements: res.data.totalElements, totalPages: res.data.totalPages })
```

---

### 5. `src/composables/useToast.js` — Tạo mới

**Mục đích:** Shortcut sử dụng toast notification trong bất kỳ component nào. Wrapper của `useUiStore`.

**Sử dụng:**
```js
import { useToast } from '@/composables/useToast'

const toast = useToast()
toast.success('Lưu hồ sơ thành công!')
toast.error('Không thể kết nối server.')
toast.warning('Phiên đăng nhập sắp hết hạn.')
toast.info('Dữ liệu đang được làm mới...')
```

---

### 6. `src/composables/useRoleGuard.js` — Tạo mới

**Mục đích:** Kiểm tra role của user hiện tại để render có điều kiện trong template.

**Sử dụng:**
```js
import { useRoleGuard } from '@/composables/useRoleGuard'

const { isHR, isManager, isEmployee, isAdminOrHR, hasRole } = useRoleGuard()
```
```html
<button v-if="isHR">Chỉ HR thấy nút này</button>
<div v-if="hasRole('hr', 'admin')">HR hoặc Admin</div>
```

---

### 7. `src/components/common/ToastNotification.vue` — Tạo mới

**Mục đích:** Component hiển thị danh sách toast ở góc trên phải màn hình. Dùng `Teleport to="body"` để tránh bị overflow parent che khuất. Animation slide-in từ phải.

**Tính năng:**
- 4 loại: `success` (xanh), `error` (đỏ), `warning` (vàng), `info` (tím)
- Tự động đóng sau `duration` ms (default: 3500ms)
- Có nút × đóng thủ công

---

### 8. `src/components/common/ConfirmDialog.vue` — Tạo mới

**Mục đích:** Dialog xác nhận thống nhất, được điều khiển qua `ui.confirm()` (Promise-based). Tránh dùng `window.confirm()` native.

**Tính năng:**
- Mode thường: icon `AlertTriangle`, nút xác nhận màu tím
- Mode nguy hiểm (`danger: true`): icon `Trash2`, nút xác nhận màu đỏ
- Click backdrop để hủy

---

### 9. `src/components/common/PageHeader.vue` — Tạo mới

**Mục đích:** Chuẩn hóa phần header tiêu đề trang. Thay thế việc copy-paste cùng một cấu trúc HTML ở mỗi view.

**Props:**
| Prop | Type | Mặc định | Mô tả |
|------|------|----------|-------|
| `title` | String | required | Tiêu đề trang |
| `subtitle` | String | `''` | Mô tả phụ |
| `icon` | Component | `null` | Icon Lucide |
| `iconColor` | String | `'bg-indigo-600'` | Màu nền icon |
| `iconShadow` | String | `'shadow-indigo-100'` | Shadow của icon |

**Slots:**
- `#actions` — Khu vực nút bên phải
- `#badges` — Badges thông tin bên dưới title

**Sử dụng:**
```html
<PageHeader
  title="Hồ sơ Nhân sự"
  subtitle="Quản lý và theo dõi thông tin nhân sự"
  :icon="Users"
>
  <template #actions>
    <BaseButton variant="primary">Thêm mới</BaseButton>
  </template>
</PageHeader>
```

---

### 10. `src/layouts/MainLayout.vue` — Cập nhật

**Thay đổi:**
1. **Menu động theo role** — HR/Admin thấy menu quản trị đầy đủ; Manager thấy menu phê duyệt team riêng
2. **Tên người dùng real-time** — Lấy từ `authStore.user.fullName` thay vì hardcode `'Admin User'`
3. **Role label tiếng Việt** — Hiển thị `'Nhân sự (HR)'`, `'Quản lý'`, `'Nhân viên'` tương ứng
4. **Initials tự động** — Tính từ `fullName` (2 chữ cái cuối của tên)
5. **Redirect logout** — Đổi từ `router.push('/')` sang `router.push('/login')`

**Menu Manager mới:**
- Dashboard Team
- Hồ sơ nhân sự (xem)
- Duyệt nghỉ phép (route `/manager/leaves`)
- Duyệt chấm công (route `/manager/attendance`)
- Bảng lương Team (route `/manager/payroll`)
- Tiếp nhận / Thôi việc

---

## 🚧 Đang thực hiện (In Progress)

### Phase 2: HR Missing Views (✅ Hoàn thành)
- [x] `views/dashboard/profile/ProfileChangeRequests.vue`
- [x] `views/dashboard/audit/AuditLogView.vue`
- [x] `views/dashboard/roles/RoleManagement.vue`
- [ ] Kết nối API thực vào `PayrollList.vue` (Chuyển sang backlog)

### Phase 3: Manager Views (✅ Hoàn thành)
- [x] `views/dashboard/manager/ManagerDashboard.vue`
- [x] `views/dashboard/manager/TeamLeaveApproval.vue`
- [x] `views/dashboard/manager/TeamAttendance.vue`
- [x] `views/dashboard/manager/TeamPayroll.vue`
- [x] Cập nhật `router/dashboard.js` — thêm routes manager

### Phase 4: Employee Portal (Đang thực hiện)
- [ ] Nâng cấp `views/portal/PortalView.vue` (Đã cập nhật routes, chuẩn bị refactor thiết kế nếu cần)
- [ ] Nâng cấp `views/portal/MyProfile.vue`
- [ ] Nâng cấp `views/portal/MyAttendance.vue`
- [x] Tạo `views/portal/MyLeaves.vue` (Xong hiển thị & submit đơn phép)
- [x] Tạo `views/portal/MyPayslip.vue` (Xong hiển thị logic)

---

## ✅ Nguyên tắc thiết kế áp dụng

| Nguyên tắc | Mô tả |
|-----------|-------|
| **No Table Layout** | Dùng Cards/Rows thay bảng (trừ Payroll vì cần alignment số) |
| **Slide Panels** | Chi tiết hiển thị dạng panel từ phải (pattern từ Onboarding/Offboarding) |
| **Glassmorphism** | Modal + Overlay dùng `backdrop-blur` |
| **API First** | Mọi view kết nối API thực, không dùng mock data |
| **Composables** | Logic dùng > 2 lần phải tách thành composable |
| **Color System** | Indigo=primary, Emerald=success, Amber=warning, Rose=danger |
| **Animation** | Fade-in khi mount, Slide-in cho panel, Scale-in cho modal |

---

## 🔌 API Modules mapping (không thay đổi backend)

| Module | Path | Dùng cho |
|--------|------|---------|
| `api/auth.js` | `/api/v1/auth/*` | Login, Logout, Refresh, Change Password |
| `api/admin/employee.js` | `/api/v1/admin/employees/*` | CRUD nhân sự, Profile, Bank, Documents |
| `api/admin/attendance.js` | `/api/v1/admin/attendance/*` | Shifts, Daily, Adjustment, OT, Periods |
| `api/admin/leave.js` | `/api/v1/admin/leave/*` | Leave types, Balances, Requests, Reports |
| `api/admin/payroll.js` | `/api/v1/admin/payroll/*` | Components, Formulas, Compensations, Periods |
| `api/admin/contract.js` | `/api/v1/admin/contracts/*` | CRUD hợp đồng |
| `api/admin/onboarding.js` | `/api/v1/admin/onboarding/*` | Quy trình tiếp nhận |
| `api/admin/offboarding.js` | `/api/v1/admin/offboarding/*` | Quy trình thôi việc |
| `api/admin/orgUnit.js` | `/api/v1/admin/org-units/*` | Cơ cấu tổ chức |
| `api/admin/report.js` | `/api/v1/admin/reports/*` | Báo cáo tổng hợp |
| `api/admin/role.js` | `/api/v1/admin/roles/*` | Phân quyền |
| `api/admin/auditLog.js` | `/api/v1/admin/audit-logs/*` | Nhật ký hệ thống |
| `api/manager/manager.js` | `/api/v1/manager/*` | Leave/OT/Payroll phê duyệt, Team reports |
| `api/me/portal.js` | `/api/v1/me/portal/*` | Dashboard, Profile, My Leave, My Payslip |
