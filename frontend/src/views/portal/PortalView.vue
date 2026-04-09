<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmployeeLayout from '@/components/EmployeeLayout.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { useToast } from '@/composables/useToast'
import {
  ArrowRight,
  Clock,
  FileText,
  Globe,
  HelpCircle,
  ListTodo,
  Loader2,
  LogIn,
  LogOut,
  MapPin,
  MessageCircle,
  ShieldCheck,
  Star,
  TrendingUp,
  Users,
  Zap,
} from 'lucide-vue-next'
import { checkIn, checkOut, getCheckInStatus, getPortalDashboard } from '@/api/me/portal'
import { unwrapData } from '@/utils/api'

const router = useRouter()
const toast = useToast()

const dashboard = ref(null)
const loading = ref(false)
const attLoading = ref(false)
const checkInStatus = ref({
  canCheckIn: true,
  canCheckOut: false,
  lastEventTime: null,
  lastEventType: null,
})

const features = [
  {
    title: 'Đồng bộ Đa Chi nhánh',
    desc: 'Quản lý thông suốt nhân sự, chấm công và chính sách trên toàn bộ mạng lưới công ty.',
    icon: Globe,
    color: 'bg-indigo-50 text-indigo-600',
  },
  {
    title: 'Dữ liệu Tập trung',
    desc: 'Hồ sơ, hợp đồng và thông tin cá nhân được lưu trữ tập trung và bảo mật.',
    icon: ShieldCheck,
    color: 'bg-emerald-50 text-emerald-600',
  },
  {
    title: 'Tự phục vụ Nhanh',
    desc: 'Nhân viên chủ động xử lý nghỉ phép, bảng lương, chấm công và tác vụ thường ngày.',
    icon: Users,
    color: 'bg-sky-50 text-sky-600',
  },
  {
    title: 'Cập nhật Theo thời gian thực',
    desc: 'Các chỉ số quan trọng được làm mới từ backend để bạn luôn nhìn thấy bức tranh hiện tại.',
    icon: TrendingUp,
    color: 'bg-rose-50 text-rose-600',
  },
]

const services = [
  { id: 1, name: 'Đăng ký nghỉ phép', icon: Clock, path: '/portal/leaves' },
  { id: 2, name: 'Tra cứu bảng lương', icon: FileText, path: '/portal/payslip' },
  { id: 3, name: 'Hồ sơ cá nhân', icon: Users, path: '/portal/profile' },
  { id: 4, name: 'Lịch sử chấm công', icon: Zap, path: '/portal/attendance' },
  { id: 5, name: 'Sổ tay nhân sự', icon: HelpCircle, path: '#' },
  { id: 6, name: 'Kênh hỗ trợ HR', icon: MessageCircle, path: '#' },
]

const branches = [
  { id: 'hn', name: 'Trụ sở Hà Nội', address: 'Tòa nhà Landmark, Nam Từ Liêm', employees: 245 },
  { id: 'hcm', name: 'Chi nhánh TP.HCM', address: 'Quận 1, Thành phố Hồ Chí Minh', employees: 180 },
  { id: 'dn', name: 'Chi nhánh Đà Nẵng', address: 'Hải Châu, Thành phố Đà Nẵng', employees: 85 },
]

const stats = computed(() => {
  const data = dashboard.value
  if (!data) return []

  return [
    {
      label: 'Nghỉ phép còn lại',
      value: data.totalAvailableLeaveUnits ?? 0,
      icon: Clock,
      color: 'bg-emerald-50 text-emerald-600',
    },
    {
      label: 'Đơn nghỉ đang chờ',
      value: data.pendingLeaveRequestCount ?? 0,
      icon: ShieldCheck,
      color: 'bg-indigo-50 text-indigo-600',
    },
    {
      label: 'Điều chỉnh công',
      value: data.pendingAttendanceAdjustmentCount ?? 0,
      icon: Zap,
      color: 'bg-amber-50 text-amber-600',
    },
    {
      label: 'Phiếu lương đã phát hành',
      value: data.publishedPayslipCount ?? 0,
      icon: FileText,
      color: 'bg-sky-50 text-sky-600',
    },
  ]
})

const spotlightCards = computed(() => [
  {
    eyebrow: 'Inbox',
    title: `${fmtValue(dashboard.value?.unreadInboxCount)} thông báo mới`,
    description: 'Các cập nhật từ HR, nhắc việc và thông báo nội bộ đang chờ bạn xem.',
    icon: MessageCircle,
    border: 'border-rose-100',
    accent: 'from-rose-500/15 to-orange-500/10',
    actionLabel: 'Mở inbox',
    path: '/portal/inbox',
  },
  {
    eyebrow: 'Task',
    title: `${fmtValue(dashboard.value?.openPortalTaskCount)} việc đang mở`,
    description: 'Những đầu việc cần xử lý trong portal được gom vào một cụm thao tác nhanh.',
    icon: ListTodo,
    border: 'border-sky-100',
    accent: 'from-sky-500/15 to-indigo-500/10',
    actionLabel: 'Xem nhiệm vụ',
    path: '/portal/tasks',
  },
  {
    eyebrow: 'Payroll',
    title: `${fmtValue(dashboard.value?.publishedPayslipCount)} phiếu lương`,
    description: 'Theo dõi các kỳ lương đã phát hành và tra cứu chi tiết nhanh hơn.',
    icon: FileText,
    border: 'border-emerald-100',
    accent: 'from-emerald-500/15 to-cyan-500/10',
    actionLabel: 'Mở bảng lương',
    path: '/portal/payslip',
  },
])

const profileCards = computed(() => [
  { label: 'Phòng ban', value: dashboard.value?.orgUnitName ?? '—' },
  { label: 'Chức danh', value: dashboard.value?.jobTitleName ?? '—' },
  { label: 'Trạng thái làm việc', value: dashboard.value?.employmentStatus ?? '—' },
  { label: 'Thông báo mới', value: `${fmtValue(dashboard.value?.unreadInboxCount)} tin nhắn` },
])

const accountPanels = computed(() => [
  {
    label: 'Công việc cần làm',
    value: fmtValue(dashboard.value?.openPortalTaskCount),
    hint: 'Task đang mở',
  },
  {
    label: 'Ngày tham gia',
    value: dashboard.value?.joinedDate ?? '—',
    hint: 'Thông tin hồ sơ',
  },
])

const fmtValue = (value) => {
  if (value === null || value === undefined) return '0'
  return typeof value === 'number' ? value.toLocaleString('vi-VN') : value.toString()
}

const fmtDateTime = (value) => {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
  })
}

const fetchDashboard = async () => {
  try {
    dashboard.value = unwrapData(await getPortalDashboard())
  } catch (error) {
    console.error('Failed to fetch portal dashboard:', error)
    toast.error('Không thể tải dữ liệu portal. Vui lòng thử lại.')
  }
}

const fetchCheckInStatus = async () => {
  try {
    checkInStatus.value = unwrapData(await getCheckInStatus()) || {
      canCheckIn: true,
      canCheckOut: false,
      lastEventTime: null,
      lastEventType: null,
    }
  } catch (error) {
    console.error('Không thể tải trạng thái chấm công', error)
  }
}

const handleCheckIn = async () => {
  attLoading.value = true
  try {
    const today = new Date().toISOString().split('T')[0]
    await checkIn({
      sourceType: 'WEB',
      attendanceDate: today,
      note: 'Check-in từ Portal',
    })
    toast.success('Check-in thành công!')
    await fetchCheckInStatus()
  } catch (error) {
    console.error('Check-in failed:', error)
    toast.error(error.response?.data?.message || 'Check-in thất bại')
  } finally {
    attLoading.value = false
  }
}

const handleCheckOut = async () => {
  attLoading.value = true
  try {
    const today = new Date().toISOString().split('T')[0]
    await checkOut({
      sourceType: 'WEB',
      attendanceDate: today,
      note: 'Check-out từ Portal',
    })
    toast.success('Check-out thành công!')
    await fetchCheckInStatus()
  } catch (error) {
    console.error('Check-out failed:', error)
    toast.error(error.response?.data?.message || 'Check-out thất bại')
  } finally {
    attLoading.value = false
  }
}

const fetchAll = async () => {
  loading.value = true
  await Promise.all([fetchDashboard(), fetchCheckInStatus()])
  loading.value = false
}

onMounted(fetchAll)
</script>

<template>
  <EmployeeLayout>
    <div class="relative mb-28 overflow-hidden">
      <div
        class="absolute inset-x-0 top-0 h-[720px] bg-[radial-gradient(circle_at_top_left,_rgba(99,102,241,0.16),_transparent_34%),radial-gradient(circle_at_top_right,_rgba(16,185,129,0.12),_transparent_28%),linear-gradient(180deg,_#f8fbff_0%,_#ffffff_72%)]">
      </div>
      <div class="absolute -left-30 top-24 h-72 w-72 rounded-full bg-indigo-200/20 blur-3xl"></div>
      <div class="absolute -right-20 top-40 h-64 w-64 rounded-full bg-cyan-200/20 blur-3xl"></div>

      <section class="relative max-w-360 mx-auto px-8 pt-20 lg:px-10 lg:pt-28 xl:px-12">
        <div class="space-y-10 xl:space-y-12">
          <div class="space-y-6">
            <div
              class="inline-flex items-center gap-3 rounded-full border border-indigo-100 bg-white/80 px-4 py-2 text-sm font-semibold text-indigo-700 shadow-sm backdrop-blur">
              <Star class="w-4 h-4" /> Cổng thông tin Nhân viên
            </div>

            <div class="grid gap-6 xl:grid-cols-[minmax(0,1.1fr)_minmax(320px,0.9fr)] xl:items-end">
              <div class="space-y-4">
                <h1
                  class="max-w-4xl bg-[linear-gradient(135deg,#4338ca_0%,#6366f1_48%,#0f172a_100%)] bg-clip-text text-4xl font-black leading-tight tracking-tight text-transparent lg:text-5xl 2xl:text-6xl">
                  {{ dashboard?.fullName ?? 'Nhân viên' }}
                </h1>
                <p class="max-w-3xl text-lg font-medium leading-relaxed text-slate-500">
                  Một nơi duy nhất để theo dõi ngày làm việc, tác vụ cần xử lý, hồ sơ và bảng lương cá nhân của bạn.
                </p>
              </div>

              <div
                class="rounded-[30px] border border-indigo-100/80 bg-white/85 p-6 shadow-[0_16px_50px_rgba(79,70,229,0.08)] backdrop-blur-sm">
                <p class="text-[11px] font-black uppercase tracking-[0.2em] text-indigo-400">Tổng quan nhanh</p>
                <p class="mt-3 text-base font-semibold leading-relaxed text-slate-600">
                  Từ đây bạn có thể xem thông báo mới, nhiệm vụ cần xử lý và trạng thái chấm công trong cùng một màn
                  hình.
                </p>
              </div>
            </div>
          </div>

          <div class="grid gap-8 xl:grid-cols-[minmax(0,1.7fr)_390px] 2xl:grid-cols-[minmax(0,1.8fr)_410px]">
            <div class="space-y-8">
              <div
                class="rounded-[34px] border border-slate-200/80 bg-white/70 p-5 shadow-[0_18px_50px_rgba(15,23,42,0.06)] backdrop-blur-sm sm:p-6">
                <div class="mb-5 flex items-center justify-between gap-4">
                  <div>
                    <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Chỉ số cá nhân</p>
                    <h2 class="mt-2 text-2xl font-black text-slate-900">Những gì cần theo dõi hôm nay</h2>
                  </div>
                </div>

                <div class="grid gap-4 sm:grid-cols-2 2xl:grid-cols-4">
                  <div v-for="item in stats" :key="item.label"
                    class="group rounded-[28px] border border-slate-200/80 bg-white/90 p-5 2xl:p-6 shadow-[0_12px_40px_rgba(15,23,42,0.06)] backdrop-blur-sm transition-all hover:-translate-y-1 hover:border-indigo-200 hover:shadow-[0_22px_60px_rgba(79,70,229,0.12)]">
                    <div class="mb-4 flex items-center justify-between">
                      <div class="flex h-12 w-12 items-center justify-center rounded-2xl ring-1 ring-inset ring-black/5"
                        :class="item.color">
                        <component :is="item.icon" class="w-5 h-5" />
                      </div>
                      <ArrowRight class="w-4 h-4 text-slate-300 transition-transform group-hover:translate-x-1" />
                    </div>
                    <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">{{ item.label }}</p>
                    <p class="mt-3 text-3xl font-black text-slate-900">{{ fmtValue(item.value) }}</p>
                  </div>
                </div>
              </div>

              <div
                class="rounded-[34px] border border-slate-200/80 bg-white/70 p-5 shadow-[0_18px_50px_rgba(15,23,42,0.06)] backdrop-blur-sm sm:p-6">
                <div class="mb-5 flex items-center justify-between gap-4">
                  <div>
                    <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Trung tâm thao tác</p>
                    <h2 class="mt-2 text-2xl font-black text-slate-900">Inbox, task và payroll</h2>
                  </div>
                </div>

                <div class="grid gap-5 xl:grid-cols-3">
                  <div v-for="card in spotlightCards" :key="card.title"
                    class="rounded-[30px] border bg-[linear-gradient(180deg,#ffffff_0%,#f8fafc_100%)] p-6 shadow-[0_18px_50px_rgba(15,23,42,0.08)]"
                    :class="`${card.border} bg-linear-to-br ${card.accent}`">
                    <div class="flex items-start justify-between gap-4">
                      <div>
                        <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">{{ card.eyebrow }}
                        </p>
                        <h3 class="mt-2 text-xl font-black text-slate-900">{{ card.title }}</h3>
                      </div>
                      <div
                        class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-900 text-white shadow-lg shadow-slate-900/10">
                        <component :is="card.icon" class="w-5 h-5" />
                      </div>
                    </div>
                    <p class="mt-3 text-sm font-medium leading-relaxed text-slate-500">{{ card.description }}</p>
                    <button
                      class="mt-5 inline-flex items-center gap-2 rounded-full bg-slate-900 px-4 py-2 text-sm font-bold text-white transition hover:bg-indigo-600"
                      @click="router.push(card.path)">
                      {{ card.actionLabel }}
                      <ArrowRight class="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div class="xl:sticky xl:top-24">
              <GlassCard
                class="h-full min-h-155 rounded-[34px] border border-slate-200/80 bg-[linear-gradient(180deg,#111827_0%,#1f2a44_100%)] p-8 text-white shadow-[0_25px_80px_rgba(15,23,42,0.2)] 2xl:p-9"
                dark>
                <div class="flex h-full flex-col justify-between space-y-8">
                  <div>
                    <div class="mb-6 flex items-center justify-between">
                      <div class="flex items-center gap-3 text-indigo-300">
                        <Clock class="w-5 h-5" />
                        <p class="text-xs font-bold uppercase tracking-widest">Chấm công hành chính</p>
                      </div>
                      <div v-if="checkInStatus.lastEventType"
                        class="rounded-full px-3 py-1 text-[10px] font-black uppercase tracking-wider"
                        :class="checkInStatus.lastEventType === 'CHECK_IN' ? 'bg-emerald-500/20 text-emerald-300' : 'bg-white/10 text-slate-200'">
                        {{ checkInStatus.lastEventType === 'CHECK_IN' ? 'Đang làm việc' : 'Đã check-out' }}
                      </div>
                    </div>

                    <div class="space-y-2">
                      <p class="text-4xl font-black tracking-tight text-white">{{ fmtDateTime(new Date()) }}</p>
                      <p class="flex items-center gap-1.5 text-sm font-medium text-slate-300">
                        <MapPin class="w-3.5 h-3.5" /> Văn phòng hiện tại
                      </p>
                    </div>

                    <div class="mt-6 rounded-[24px] border border-white/10 bg-white/5 p-4">
                      <p class="mb-1 text-[10px] font-bold uppercase tracking-widest text-slate-400">Ghi nhận cuối</p>
                      <p class="font-bold text-white">{{ fmtDateTime(checkInStatus.lastEventTime) }}</p>
                    </div>
                  </div>

                  <div class="space-y-3">
                    <button v-if="checkInStatus.canCheckIn" @click="handleCheckIn" :disabled="attLoading"
                      class="w-full flex items-center justify-center gap-3 rounded-2xl bg-[linear-gradient(135deg,#4f46e5_0%,#4338ca_100%)] px-6 py-4 font-black text-white shadow-[0_18px_45px_rgba(79,70,229,0.35)] transition hover:-translate-y-px hover:brightness-110 disabled:opacity-50">
                      <Loader2 v-if="attLoading" class="w-5 h-5 animate-spin" />
                      <LogIn v-else class="w-5 h-5" />
                      Bắt đầu làm việc
                    </button>

                    <button v-if="checkInStatus.canCheckOut" @click="handleCheckOut" :disabled="attLoading"
                      class="w-full flex items-center justify-center gap-3 rounded-2xl border border-white/10 bg-white/10 px-6 py-4 font-black text-white transition hover:bg-white/20 disabled:opacity-50">
                      <Loader2 v-if="attLoading" class="w-5 h-5 animate-spin" />
                      <LogOut v-else class="w-5 h-5" />
                      Kết thúc ca làm
                    </button>
                  </div>
                </div>
              </GlassCard>
            </div>
          </div>
        </div>
      </section>

      <section class="relative max-w-7xl mx-auto px-6 mt-10">
        <div class="grid gap-8 lg:grid-cols-[minmax(0,1.5fr)_340px]">
          <div
            class="rounded-[34px] border border-slate-200/80 bg-[linear-gradient(135deg,#0f172a_0%,#1e293b_48%,#312e81_100%)] p-8 shadow-[0_25px_80px_rgba(15,23,42,0.18)]">
            <div class="space-y-8">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <p class="mb-1 text-xs font-bold uppercase tracking-[0.2em] text-indigo-200">Thông tin định danh</p>
                  <h2 class="text-3xl font-black text-white">Hồ sơ cá nhân</h2>
                </div>
                <div class="text-right">
                  <p class="text-[10px] font-bold uppercase tracking-widest text-indigo-200/70">Mã nhân viên</p>
                  <p class="text-lg font-black text-indigo-300">{{ dashboard?.employeeCode ?? '—' }}</p>
                </div>
              </div>

              <div class="grid gap-5 md:grid-cols-2">
                <div v-for="card in profileCards" :key="card.label"
                  class="rounded-[26px] border border-white/10 bg-white/8 p-5 backdrop-blur-sm">
                  <p class="text-[10px] font-black uppercase tracking-[0.18em] text-indigo-100/70">{{ card.label }}</p>
                  <p class="mt-3 text-xl font-bold text-white">{{ card.value }}</p>
                </div>
              </div>
            </div>
          </div>

          <div
            class="rounded-[34px] border border-slate-200/80 bg-[linear-gradient(180deg,#f8fafc_0%,#ffffff_100%)] p-8 shadow-[0_18px_60px_rgba(15,23,42,0.08)]">
            <div class="flex h-full flex-col justify-between">
              <div class="space-y-6">
                <div class="flex items-center gap-3 text-indigo-600">
                  <Users class="w-5 h-5" />
                  <p class="text-xs font-bold uppercase tracking-widest">Trạng thái tài khoản</p>
                </div>

                <div class="space-y-4">
                  <div v-for="panel in accountPanels" :key="panel.label"
                    class="rounded-[24px] border border-slate-200 bg-slate-50 p-5">
                    <p class="text-sm font-semibold text-slate-500">{{ panel.label }}</p>
                    <p class="mt-2 text-3xl font-black text-slate-900">{{ panel.value }}</p>
                    <p class="mt-1 text-xs font-bold uppercase tracking-wider text-slate-400">{{ panel.hint }}</p>
                  </div>
                </div>
              </div>

              <div class="mt-8 border-t border-slate-200 pt-6">
                <BaseButton @click="router.push('/portal/profile')" variant="secondary" size="sm"
                  class="w-full rounded-full bg-slate-900 text-white hover:bg-indigo-600">
                  Hồ sơ chi tiết
                </BaseButton>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="relative max-w-7xl mx-auto px-6 mt-20">
        <div class="mb-12 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div class="space-y-3">
            <p class="text-xs font-black uppercase tracking-[0.2em] text-indigo-500">Self-Service Center</p>
            <h2 class="text-4xl font-black tracking-tight text-slate-900">Dịch vụ bạn dùng thường xuyên</h2>
          </div>
          <p class="max-w-xl text-sm font-medium leading-relaxed text-slate-500">
            Truy cập nhanh các tác vụ hàng ngày thay vì phải tìm sâu trong menu. Mỗi thao tác đều dẫn thẳng tới màn hình
            xử lý thực tế.
          </p>
        </div>

        <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
          <button v-for="s in services" :key="s.id" @click="s.path !== '#' && router.push(s.path)"
            class="group rounded-[30px] border border-slate-200 bg-white p-6 text-left shadow-[0_18px_50px_rgba(15,23,42,0.05)] transition-all hover:-translate-y-1 hover:border-indigo-200 hover:shadow-[0_25px_60px_rgba(99,102,241,0.12)]"
            :class="s.path === '#' ? 'cursor-default opacity-70' : 'cursor-pointer'">
            <div class="flex items-start justify-between gap-4">
              <div
                class="flex h-14 w-14 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600 transition-all group-hover:bg-indigo-600 group-hover:text-white">
                <component :is="s.icon" class="w-6 h-6" />
              </div>
              <ArrowRight class="w-4 h-4 text-slate-300 transition-transform group-hover:translate-x-1" />
            </div>
            <h3 class="mt-6 text-xl font-black text-slate-900">{{ s.name }}</h3>
            <p class="mt-2 text-sm font-medium text-slate-500">
              {{
                s.id === 1 ? 'Tạo đơn nghỉ, theo dõi phê duyệt và kiểm tra số dư phép.' :
                  s.id === 2 ? 'Xem phiếu lương đã phát hành và lịch sử các kỳ lương.' :
                    s.id === 3 ? 'Cập nhật thông tin cá nhân và tra cứu hồ sơ hiện tại.' :
                      s.id === 4 ? 'Theo dõi log vào ra, điều chỉnh công và overtime.' :
                        s.id === 5 ? 'Không gian dành cho sổ tay nội bộ và hướng dẫn nhân sự.' :
                          'Kênh liên hệ HR và các phản hồi cần hỗ trợ.'
              }}
            </p>
          </button>
        </div>
      </section>

      <section class="relative max-w-7xl mx-auto px-6 mt-24">
        <div class="grid gap-8 lg:grid-cols-[minmax(0,1.15fr)_minmax(0,0.85fr)]">
          <div
            class="rounded-[36px] border border-slate-200 bg-[linear-gradient(135deg,#ffffff_0%,#f8fbff_100%)] p-8 shadow-[0_20px_60px_rgba(15,23,42,0.06)]">
            <div class="mb-10 space-y-3">
              <p class="text-xs font-black uppercase tracking-[0.2em] text-indigo-500">Digital HRM</p>
              <h2 class="text-4xl font-black tracking-tight text-slate-900">Hệ sinh thái hỗ trợ trải nghiệm nhân viên
              </h2>
              <p class="max-w-2xl text-sm font-medium leading-relaxed text-slate-500">
                Hệ thống được thiết kế để giảm thao tác lặp lại, tăng tính minh bạch và giúp bạn xử lý công việc hành
                chính nhanh hơn.
              </p>
            </div>

            <div class="grid gap-6 md:grid-cols-2">
              <div v-for="f in features" :key="f.title"
                class="group rounded-[28px] border border-slate-200 bg-white p-6 transition-all hover:border-indigo-200 hover:shadow-xl hover:shadow-indigo-50">
                <div :class="[f.color, 'mb-6 flex h-12 w-12 items-center justify-center rounded-2xl']">
                  <component :is="f.icon" class="w-6 h-6" />
                </div>
                <h3 class="text-lg font-black text-slate-900">{{ f.title }}</h3>
                <p class="mt-3 text-sm font-medium leading-relaxed text-slate-500">{{ f.desc }}</p>
              </div>
            </div>
          </div>

          <div class="rounded-[36px] border border-slate-200 bg-slate-950 p-8 shadow-[0_25px_80px_rgba(15,23,42,0.18)]">
            <div class="mb-10 flex items-center justify-between">
              <div>
                <p class="text-xs font-black uppercase tracking-[0.2em] text-emerald-400">Mạng lưới vận hành</p>
                <h2 class="mt-3 text-3xl font-black text-white">Chi nhánh kết nối</h2>
              </div>
              <div class="flex h-14 w-14 items-center justify-center rounded-2xl bg-white/5 text-emerald-400">
                <Globe class="w-7 h-7" />
              </div>
            </div>

            <div class="space-y-4">
              <div v-for="branch in branches" :key="branch.id"
                class="rounded-[24px] border border-white/10 bg-white/5 p-5 transition-all hover:bg-white/8">
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="text-lg font-black text-white">{{ branch.name }}</p>
                    <p class="mt-2 text-sm font-medium text-slate-400">{{ branch.address }}</p>
                  </div>
                  <div
                    class="rounded-full bg-emerald-500/15 px-3 py-1 text-[11px] font-black uppercase tracking-wider text-emerald-300">
                    Online
                  </div>
                </div>
                <div class="mt-4 flex items-center justify-between text-sm font-medium text-slate-300">
                  <span>{{ branch.employees }} nhân sự</span>
                  <span>Hoạt động ổn định</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </EmployeeLayout>
</template>
