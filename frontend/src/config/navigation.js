import {
  Banknote,
  CalendarOff,
  Clock,
  FilePenLine,
  FileSignature,
  History,
  KeyRound,
  LayoutDashboard,
  Network,
  Settings,
  ShieldCheck,
  UserMinus,
  UserPlus,
  Users,
} from 'lucide-vue-next'

export const navigationByRole = {
  admin: [
    {
      label: 'TỔNG HỢP',
      items: [
        { name: 'Tổng quan', icon: LayoutDashboard, path: '/dashboard' },
      ],
    },
    {
      label: 'HỆ THỐNG',
      items: [
        { name: 'Tài khoản', icon: KeyRound, path: '/users' },
        { name: 'Phân quyền', icon: ShieldCheck, path: '/roles' },
        { name: 'Nhật ký hoạt động', icon: History, path: '/audit-logs' },
        { name: 'Cài đặt chung', icon: Settings, path: '/settings' },
      ],
    },
  ],
  hr: [
    {
      label: 'TỔNG HỢP',
      items: [
        { name: 'Tổng quan', icon: LayoutDashboard, path: '/dashboard' },
      ],
    },
    {
      label: 'TỔ CHỨC',
      items: [
        { name: 'Cơ cấu tổ chức', icon: Network, path: '/org-units' },
      ],
    },
    {
      label: 'NHÂN SỰ',
      items: [
        { name: 'Hồ sơ nhân sự', icon: Users, path: '/employees' },
        { name: 'Hợp đồng lao động', icon: FileSignature, path: '/contracts' },
        { name: 'Phê duyệt hồ sơ', icon: FilePenLine, path: '/profile-change-requests' },
      ],
    },
    {
      label: 'VÒNG ĐỜI NHÂN SỰ',
      items: [
        { name: 'Tiếp nhận (Onboarding)', icon: UserPlus, path: '/onboarding' },
        { name: 'Thôi việc (Offboarding)', icon: UserMinus, path: '/offboarding' },
      ],
    },
    {
      label: 'CÔNG LƯƠNG',
      items: [
        { name: 'Quản lý nghỉ phép', icon: CalendarOff, path: '/leaves' },
        { name: 'Dữ liệu chấm công', icon: Clock, path: '/attendance' },
        { name: 'Bảng tính lương', icon: Banknote, path: '/payroll' },
      ],
    },
  ],
  manager: [
    {
      label: 'TỔNG HỢP',
      items: [
        { name: 'Dashboard Team', icon: LayoutDashboard, path: '/manager/dashboard' },
      ],
    },
    {
      label: 'QUẢN LÝ NHÂN SỰ',
      items: [
        { name: 'Hồ sơ nhân sự', icon: Users, path: '/employees' },
        { name: 'Tiếp nhận', icon: UserPlus, path: '/onboarding' },
        { name: 'Thôi việc', icon: UserMinus, path: '/offboarding' },
      ],
    },
    {
      label: 'PHÊ DUYỆT',
      items: [
        { name: 'Duyệt nghỉ phép', icon: CalendarOff, path: '/manager/leaves' },
        { name: 'Duyệt chấm công', icon: Clock, path: '/manager/attendance' },
        { name: 'Bảng lương Team', icon: Banknote, path: '/manager/payroll' },
      ],
    },
  ],
}
