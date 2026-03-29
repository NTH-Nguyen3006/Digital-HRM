# Digital HRM - Design System & Style Guide

Tài liệu này quy chuẩn các thông số thiết kế (màu sắc, hiệu ứng, bo góc) được sử dụng trong dự án Digital HRM để đảm bảo tính đồng nhất về UI/UX.

---

## Bảng Màu (Color Palette)

Dự án sử dụng hệ màu dựa trên **Tailwind CSS v4**.

### 1. Màu Chính (Primary Colors)
Sử dụng cho các nút hành động chính, trạng thái active, và nhận diện thương hiệu.
- **Primary**: `indigo-600` (Thường dùng: `bg-primary`, `text-primary`)
- **Primary Hover**: `indigo-700` (Thường dùng: `hover:bg-primary-hover`)
- **Accent**: `emerald-400` (Sử dụng cho các điểm nhấn hoặc trạng thái đặc biệt)

### 2. Màu Trung Tính (Neutral Colors)
Sử dụng cho nền, chữ và viền.
- **Background (Light mode)**: `slate-50` (`bg-slate-50`)
- **Text**: `slate-900` (`text-slate-900`)
- **Dark Background/Deep Dark**: `slate-950` (`bg-slate-950` hoặc biến `--color-slate-950`)

---

## Hiệu Ứng Glassmorphism (Kính mờ)

Đây là phong cách chủ đạo của dự án. Hãy sử dụng các class sau thay vì viết CSS thủ công:

| Class | Mô tả | Cách dùng |
| :--- | :--- | :--- |
| `.glass` | Nền trắng mờ, có viền sáng và bóng đổ. | Thẻ card, container sáng. |
| `.glass-dark` | Nền tối mờ, độ tương phản cao. | Sidebar, Modal tối. |
| `.input-glass` | Hiệu ứng kính cho các ô nhập liệu. | `<input>`, `<select>`, `<textarea>`. |

---

## Bo Góc & Khoảng Cách (Radius & Spacing)

Để giao diện trông hiện đại và mềm mại, dự án ưu tiên bo góc lớn:
- **Lớn (XL)**: `rounded-xl` (1rem - 16px)
- **Rất Lớn (2XL)**: `rounded-2xl` (1.5rem - 24px)

---

## Hoạt Ảnh (Animations)

- **Floating**: Sử dụng class `animate-float` cho các phần tử cần hiệu ứng bay bồng bềnh (như icon minh họa, vật thể trang trí).

---

## 🛠 Cách Sử Dụng Trong Code

Vì các biến này đã được định nghĩa trong `@theme` của Tailwind, bạn có thể gọi trực tiếp:

```html
<!-- Ví dụ: Một chiếc nút theo chuẩn dự án -->
<button class="bg-primary hover:bg-primary-hover text-white rounded-xl px-6 py-2 transition-all">
  Xác nhận
</button>

<!-- Ví dụ: Một chiếc Card hiệu ứng kính -->
<div class="glass p-6 rounded-2xl">
  <h2 class="text-slate-900 font-bold">Tiêu đề Card</h2>
  <p class="text-slate-600">Nội dung mô tả...</p>
</div>
