# HRM Frontend Implementation Notes

## Muc tieu

Tai thiet ke frontend theo huong:

- Giữ tối đa giao diện hiện có, chỉ tối ưu chỗ cần thiết.
- Kết nối frontend với API backend hiện có, khong sua backend.
- Ưu tiên giao diện card/panel hien dai, giam phu thuoc vao bang.
- Tach logic dung chung ra helper/component de de mo rong.
- Tap trung vao 3 role: `HR`, `Employee`, `Manager`.

## Phan tich role va router

### Employee

Route portal hien co:

- `/`
- `/portal/profile`
- `/portal/attendance`
- `/portal/leaves`
- `/portal/payslip`
- `/portal/attendance-adjustment`
- `/portal/overtime`
- `/portal/contract`
- `/portal/resignation`
- `/portal/profile-change`
- `/portal/inbox`
- `/portal/tasks`

Nhan xet:

- Employee dang dung `PortalLayout`.
- Mot so man hinh da co UI tot nhung parse sai `ApiResponse`.
- Mot so man hinh dang mock du lieu (`MyContract`, `MyProfileChange`, `MyAttendance`).

### HR

Route nghiep vu chinh:

- `/employees`
- `/employees/:id`
- `/contracts`
- `/contracts/:id`
- `/contracts/add`
- `/profile-change-requests`
- `/onboarding`
- `/offboarding`
- `/leaves`
- `/attendance`
- `/payroll`

Nhan xet:

- Nhieu man hinh da co shell UI nhung data mapping chua dong nhat.
- `LeaveManagement`, `AttendanceSheet`, `PayrollList` con gia dinh response hoac dung mock.
- Can co helper chung cho `ApiResponse/PageResponse`.

### Manager

Route nghiep vu chinh:

- `/manager/dashboard`
- `/manager/leaves`
- `/manager/attendance`
- `/manager/payroll`
- dung chung mot so route voi HR nhu `/employees`, `/onboarding`, `/offboarding`

Nhan xet:

- Dashboard va cac trang phe duyet da co UI kha tot.
- Co sai lech endpoint manager leave so voi backend.
- Can giu role flow manager on dinh trong khi admin khong bi anh huong.

## Cac thay doi da thuc hien

### 1. Chuan hoa tang dung chung

Them cac file moi:

- `frontend/src/utils/api.js`
- `frontend/src/utils/format.js`
- `frontend/src/config/navigation.js`
- `frontend/src/components/hrm/InsightCard.vue`
- `frontend/src/components/hrm/SurfacePanel.vue`

Noi dung:

- `api.js`: helper `unwrapData`, `unwrapPage`, `safeArray`, `downloadBlob`.
- `format.js`: format ngay, gio, tien, thang-nam, status humanize.
- `navigation.js`: tach menu theo role ra khoi `MainLayout`.
- `InsightCard`, `SurfacePanel`: panel/card tai su dung cho HRM pages.

### 2. To chuc lai menu theo role

Cap nhat:

- `frontend/src/layouts/MainLayout.vue`

Ket qua:

- Menu `admin`, `hr`, `manager` duoc quan ly tap trung.
- De them role/module moi ma khong phai sua layout qua nhieu.

### 3. Sua endpoint frontend bi lech backend

Cap nhat:

- `frontend/src/api/admin/leave.js`
- `frontend/src/api/manager/manager.js`

Da sua cac duong dan khong khop backend:

- Admin leave:
  - `/leave-balances`
  - `/leave-requests`
  - `/leave-periods/close`
  - `/leave-settlements`
  - `/leave-reports/export`
- Manager leave:
  - `/leave-requests/pending`
  - `/leave-requests/{id}/review`
  - `/leave-calendar`

### 4. HR pages da duoc ket noi va thiet ke lai

Cap nhat:

- `frontend/src/views/dashboard/contracts/ContractList.vue`
- `frontend/src/views/dashboard/leaves/LeaveManagement.vue`
- `frontend/src/views/dashboard/attendance/AttendanceSheet.vue`
- `frontend/src/views/dashboard/payroll/PayrollList.vue`

Huong thay doi:

- Chuyen tu table-heavy sang card/panel-heavy.
- Dung `unwrapPage()` de doc `ApiResponse<PageResponse<...>>`.
- Bo sung KPI summary, highlight block, va bo loc tim kiem de doc nhanh.
- Su dung file export backend thay vi mock.

### 5. Employee pages da noi backend that

Cap nhat:

- `frontend/src/views/portal/PortalView.vue`
- `frontend/src/views/portal/MyAttendance.vue`
- `frontend/src/views/portal/MyContract.vue`
- `frontend/src/views/portal/MyProfileChange.vue`

Huong thay doi:

- `PortalView`: sua parse sai `ApiResponse`.
- `MyAttendance`: doc dung `recentLogs`, `adjustmentRequests`, `overtimeRequests`.
- `MyContract`: bo mock, goi `getMyContracts()` va `downloadMyContract()`.
- `MyProfileChange`: bo mock, goi `getMyProfileChangeRequests()` va `submitProfileChangeRequest()`.

## Nguyen tac UI/UX da ap dung

- Han che bang cho cac man hinh nghiep vu chinh.
- Dung card, summary rail, section panel, status badge.
- Tap trung vao scan nhanh:
  - thong tin chinh o dau card
  - metric blocks o giua
  - action ro rang o cuoi
- Giu tong mau va visual language hien co de khong lam "lech he".

## Tac dong den cau truc thu muc

Da bo sung cau truc hop ly hon:

- `frontend/src/components/hrm/`
- `frontend/src/config/`
- `frontend/src/utils/`

Muc dich:

- Tach role config, data helper, format helper.
- Giam viec viet logic trung lap trong tung page.
- Tao nen tang de tiep tuc refactor cac man con lai nhu onboarding/offboarding/manager pages neu can.

## Kiem thu

Da chay:

- `cd frontend && npm run build`

Trang thai:

- Build thanh cong.

## Ghi chu tiep theo de mo rong

Nhung man hinh nen tiep tuc refactor trong buoc sau neu muon lam sau hon nua:

- `frontend/src/views/dashboard/onboarding/OnboardingBoard.vue`
- `frontend/src/views/dashboard/offboarding/OffboardingBoard.vue`
- `frontend/src/views/dashboard/manager/ManagerDashboard.vue`
- `frontend/src/views/dashboard/manager/TeamLeaveApproval.vue`
- `frontend/src/views/dashboard/manager/TeamAttendance.vue`
- `frontend/src/views/dashboard/manager/TeamPayroll.vue`

Ly do:

- Cac man nay da co UI tuong doi tot, nhung van co the chuan hoa them theo helper moi va mo rong workflow chi tiet hon.
