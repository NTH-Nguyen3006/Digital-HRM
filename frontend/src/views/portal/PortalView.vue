<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import EmployeeLayout from '@/components/EmployeeLayout.vue'
import BaseButton from '../../components/common/BaseButton.vue'
import GlassCard from '../../components/common/GlassCard.vue'
import { useToast } from '@/composables/useToast'
import {
  ArrowRight,
  Users,
  Clock,
  ShieldCheck,
  FileText,
  HelpCircle,
  TrendingUp,
  MessageCircle,
  Zap,
  Layout,
  Star,
  Globe,
  LogIn,
  LogOut,
  MapPin,
  ListTodo,
  Loader2
} from 'lucide-vue-next'
import { getPortalDashboard, getCheckInStatus, checkIn, checkOut } from '@/api/me/portal'

const router = useRouter()
const toast = useToast()
const dashboard = ref(null)
const loading = ref(false)
const attLoading = ref(false)
const checkInStatus = ref({
  canCheckIn: true,
  canCheckOut: false,
  lastEventTime: null,
  lastEventType: null
})

const features = [
  {
    title: 'Đồng bộ Đa Chi nhánh',
    desc: 'Quản lý thông suốt nhân sự, chấm công, và chính sách trên toàn bộ mạng lưới công ty.',
    icon: Globe,
    color: 'bg-indigo-50 text-indigo-600'
  },
  {
    title: 'Dữ liệu Tập trung',
    desc: 'Hồ sơ nhân viên, hợp đồng và đánh giá năng lực được số hóa bảo mật và khoa học.',
    icon: ShieldCheck,
    color: 'bg-emerald-50 text-emerald-600'
  },
  {
    title: 'Tự phục vụ (ESS)',
    desc: 'Nhân viên chủ động thực hiện xin nghỉ, xem bảng lương và đề xuất nội bộ nhanh chóng.',
    icon: Users,
    color: 'bg-sky-50 text-sky-600'
  },
  {
    title: 'Báo cáo Thời gian thực',
    desc: 'Dashboard tổng quan về tình hình nhân sự của mọi chi nhánh mọi lúc.',
    icon: TrendingUp,
    color: 'bg-rose-50 text-rose-600'
  }
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
  { id: 'hn', name: 'Trụ sở Hà Nội', address: 'Tòa nhà Landmark, Nam Từ Liêm', employees: 245, active: true },
  { id: 'hcm', name: 'Chi nhánh TP.HCM', address: 'Quận 1, Thành phố Hồ Chí Minh', employees: 180, active: true },
  { id: 'dn', name: 'Chi nhánh Đà Nẵng', address: 'Hải Châu, Thành phố Đà Nẵng', employees: 85, active: true },
]

const stats = computed(() => {
  const data = dashboard.value
  if (!data) return []
  return [
    {
      label: 'Nghỉ phép còn lại',
      value: data.totalAvailableLeaveUnits ?? 0,
      icon: Clock,
      color: 'bg-emerald-50 text-emerald-600'
    },
    {
      label: 'Đơn nghỉ đang chờ',
      value: data.pendingLeaveRequestCount ?? 0,
      icon: ShieldCheck,
      color: 'bg-indigo-50 text-indigo-600'
    },
    {
      label: 'Điều chỉnh chấm công',
      value: data.pendingAttendanceAdjustmentCount ?? 0,
      icon: Zap,
      color: 'bg-amber-50 text-amber-600'
    },
    {
      label: 'Phiếu lương đã phát hành',
      value: data.publishedPayslipCount ?? 0,
      icon: FileText,
      color: 'bg-sky-50 text-sky-600'
    }
  ]
})

const fmtValue = (value) => {
  if (value === null || value === undefined) return '0'
  return typeof value === 'number' ? value.toLocaleString('vi-VN') : value.toString()
}

const fetchDashboard = async () => {
  try {
    dashboard.value = await getPortalDashboard()
  } catch (error) {
    toast.error('Không thể tải dữ liệu portal. Vui lòng thử lại.')
  }
}

const fetchCheckInStatus = async () => {
  try {
    const res = await getCheckInStatus()
    checkInStatus.value = res.data
  } catch (error) {
    console.error('Không thể tải trạng thái chấm công')
  }
}

const handleCheckIn = async () => {
  attLoading.value = true
  try {
    await checkIn({ note: 'Check-in từ Portal' })
    toast.success('Check-in thành công!')
    await fetchCheckInStatus()
  } catch (error) {
    toast.error('Check-in thất bại')
  } finally {
    attLoading.value = false
  }
}

const handleCheckOut = async () => {
  attLoading.value = true
  try {
    await checkOut({ note: 'Check-out từ Portal' })
    toast.success('Check-out thành công!')
    await fetchCheckInStatus()
  } catch (error) {
    toast.error('Check-out thất bại')
  } finally {
    attLoading.value = false
  }
}

const fetchAll = async () => {
  loading.value = true
  await Promise.all([
    fetchDashboard(),
    fetchCheckInStatus()
  ])
  loading.value = false
}

onMounted(fetchAll)

const fmtDateTime = (d) => {
  if (!d) return '—'
  return new Date(d).toLocaleString('vi-VN', { 
    hour: '2-digit', 
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit'
  })
}
</script>

<template>
  <EmployeeLayout>
    <div class="space-y-24 mb-40">
      <section class="max-w-7xl mx-auto px-6 pt-20 lg:pt-32">
        <div class="grid lg:grid-cols-12 gap-12 items-start">
          <div class="lg:col-span-8 space-y-8">
            <div
              class="inline-flex items-center gap-3 px-4 py-2 rounded-full bg-indigo-50 text-indigo-700 text-sm font-semibold border border-indigo-100/80">
              <Star class="w-4 h-4" /> Cổng thông tin Nhân viên
            </div>

            <div class="space-y-4">
              <h1 class="text-5xl lg:text-6xl font-black text-slate-900 leading-tight">
                Chào mừng trở lại, <span class="text-indigo-600">{{ dashboard?.fullName ?? 'Nhân viên' }}</span>
              </h1>
              <p class="text-lg text-slate-500 max-w-2xl">
                Truy cập nhanh hồ sơ, nghỉ phép, chấm công và bảng lương cá nhân. Mọi thông tin luôn được cập nhật tự động
                để bạn tập trung vào công việc.
              </p>
            </div>

            <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div v-for="item in stats" :key="item.label"
                class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                  <div class="w-11 h-11 rounded-2xl flex items-center justify-center" :class="item.color">
                    <component :is="item.icon" class="w-5 h-5" />
                  </div>
                  <ArrowRight class="w-4 h-4 text-slate-300" />
                </div>
                <p class="text-xs uppercase tracking-[0.2em] text-slate-400 font-semibold mb-2">{{ item.label }}</p>
                <p class="text-2xl font-black text-slate-900">{{ fmtValue(item.value) }}</p>
              </div>
            </div>
          </div>

          <!-- Attendance Widget -->
          <div class="lg:col-span-4 h-full">
            <GlassCard class="p-8 h-full bg-slate-900 border-slate-800" dark>
              <div class="h-full flex flex-col justify-between space-y-8">
                <div>
                   <div class="flex items-center justify-between mb-6">
                    <div class="flex items-center gap-3 text-indigo-400">
                      <Clock class="w-5 h-5" />
                      <p class="font-bold uppercase tracking-widest text-xs">Chấm công hành chính</p>
                    </div>
                    <div v-if="checkInStatus.lastEventType" 
                         class="px-2 py-0.5 rounded text-[10px] font-black uppercase tracking-wider"
                         :class="checkInStatus.lastEventType === 'CHECK_IN' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-500/20 text-slate-400'">
                      {{ checkInStatus.lastEventType }}
                    </div>
                  </div>

                  <div class="space-y-2">
                    <p class="text-white text-3xl font-black">{{ fmtDateTime(new Date()) }}</p>
                    <p class="text-slate-400 text-sm font-medium flex items-center gap-1.5">
                      <MapPin class="w-3.5 h-3.5" /> Văn phòng hiện tại
                    </p>
                  </div>

                  <div v-if="checkInStatus.lastEventTime" class="mt-6 p-4 rounded-2xl bg-white/5 border border-white/5">
                    <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Ghi nhận cuối</p>
                    <p class="text-white font-bold">{{ fmtDateTime(checkInStatus.lastEventTime) }}</p>
                  </div>
                </div>

                <div class="space-y-3">
                  <button v-if="checkInStatus.canCheckIn" 
                    @click="handleCheckIn" :disabled="attLoading"
                    class="w-full flex items-center justify-center gap-3 px-6 py-4 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded-2xl font-black transition-all shadow-xl shadow-indigo-900/40">
                    <Loader2 v-if="attLoading" class="w-5 h-5 animate-spin" />
                    <LogIn v-else class="w-5 h-5" />
                    Bắt đầu làm việc
                  </button>
                  <button v-if="checkInStatus.canCheckOut" 
                    @click="handleCheckOut" :disabled="attLoading"
                    class="w-full flex items-center justify-center gap-3 px-6 py-4 bg-white/10 hover:bg-white/20 disabled:opacity-50 text-white rounded-2xl font-black transition-all border border-white/10">
                    <Loader2 v-if="attLoading" class="w-5 h-5 animate-spin" />
                    <LogOut v-else class="w-5 h-5" />
                    Kết thúc ca làm
                  </button>
                </div>
              </div>
            </GlassCard>
          </div>
        </div>
      </section>

      <section class="max-w-7xl mx-auto px-6">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <GlassCard class="lg:col-span-2 p-8" dark>
            <div class="space-y-8">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-xs uppercase tracking-[0.2em] text-indigo-300 font-bold mb-1">Thông tin định danh</p>
                  <h2 class="text-3xl font-black text-white">Hồ sơ cá nhân</h2>
                </div>
                <div class="text-right">
                  <p class="text-[10px] uppercase tracking-widest text-slate-400 font-bold">Mã nhân viên</p>
                  <p class="text-lg font-black text-indigo-400">{{ dashboard?.employeeCode ?? '—' }}</p>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="p-6 rounded-3xl bg-white/10 border border-white/10">
                  <p class="text-[10px] uppercase tracking-widest text-slate-400 font-bold mb-3">Phòng ban</p>
                  <p class="text-xl font-bold text-white">{{ dashboard?.orgUnitName ?? '—' }}</p>
                </div>
                <div class="p-6 rounded-3xl bg-white/10 border border-white/10">
                  <p class="text-[10px] uppercase tracking-widest text-slate-400 font-bold mb-3">Chức danh</p>
                  <p class="text-xl font-bold text-white">{{ dashboard?.jobTitleName ?? '—' }}</p>
                </div>
                <div class="p-6 rounded-3xl bg-white/10 border border-white/10">
                  <p class="text-[10px] uppercase tracking-widest text-slate-400 font-bold mb-3">Trạng thái làm việc</p>
                  <p class="text-xl font-bold text-white">{{ dashboard?.employmentStatus ?? '—' }}</p>
                </div>
                <div class="p-6 rounded-3xl bg-white/10 border border-white/10">
                  <p class="text-[10px] uppercase tracking-widest text-slate-400 font-bold mb-3">Thông báo mới</p>
                  <p class="text-xl font-bold text-white">{{ fmtValue(dashboard?.unreadInboxCount) }} tin nhắn</p>
                </div>
              </div>
            </div>
          </GlassCard>

          <GlassCard class="p-8" dark>
            <div class="h-full flex flex-col justify-between">
              <div class="space-y-6">
                <div class="flex items-center gap-3 text-indigo-400">
                  <Users class="w-5 h-5" />
                  <p class="font-bold uppercase tracking-widest text-xs">Trạng thái tài khoản</p>
                </div>

                <div class="space-y-4">
                  <div class="p-5 rounded-3xl bg-white/10 border border-white/10">
                    <p class="text-slate-400 text-sm mb-1">Công việc cần làm</p>
                    <p class="text-4xl font-black text-white">{{ fmtValue(dashboard?.openPortalTaskCount) }}</p>
                  </div>
                  <div class="p-5 rounded-3xl bg-white/10 border border-white/10">
                    <p class="text-slate-400 text-sm mb-1">Ngày tham gia</p>
                    <p class="text-xl font-bold text-white">{{ dashboard?.joinedDate ?? '—' }}</p>
                  </div>
                </div>
              </div>

              <div class="mt-8 pt-8 border-t border-white/10">
                <BaseButton @click="router.push('/portal/profile')" variant="secondary" size="sm"
                  class="w-full text-white/60 hover:text-white border-white/10 hover:bg-white/5">
                  Hồ sơ chi tiết
                </BaseButton>
              </div>
            </div>
          </GlassCard>
        </div>
      </section>

      <section class="max-w-7xl mx-auto px-6">
        <div class="text-center space-y-4 mb-20">
          <h2 class="text-4xl md:text-5xl font-black text-slate-900 tracking-tight">Hệ sinh thái Digital HRM</h2>
          <p class="text-xl text-slate-500 font-medium max-w-2xl mx-auto leading-relaxed">
            Chúng tôi mang đến giải pháp HRM tập trung vào hiệu suất và sự hài lòng của từng cá nhân trong tổ chức.
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div v-for="f in features" :key="f.title"
            class="group p-8 rounded-[2.5rem] bg-white border border-slate-200 hover:border-indigo-200 hover:shadow-2xl hover:shadow-indigo-50 transition-all duration-500">
            <div
              :class="[f.color, 'w-14 h-14 rounded-2xl flex items-center justify-center mb-10 transition-transform group-hover:scale-110 group-hover:rotate-6 shadow-inner']">
              <component :is="f.icon" class="w-7 h-7" />
            </div>
            <h3 class="text-xl font-bold text-slate-900 mb-4">{{ f.title }}</h3>
            <p class="text-slate-500 font-medium leading-relaxed">{{ f.desc }}</p>
          </div>
        </div>
      </section>

      <!-- Portal Services -->
      <section id="services" class="bg-slate-950 py-40 px-6 relative overflow-hidden">
        <div class="absolute top-0 left-0 w-full h-full">
          <div class="absolute -top-24 -left-24 w-96 h-96 bg-indigo-600/10 rounded-full blur-[120px]"></div>
          <div class="absolute bottom-0 right-0 w-96 h-96 bg-emerald-500/10 rounded-full blur-[120px]"></div>
        </div>

        <div class="max-w-7xl mx-auto relative z-10">
          <div class="flex flex-col md:flex-row items-end justify-between gap-12 mb-24">
            <div class="space-y-6">
              <div class="text-emerald-400 font-bold uppercase tracking-widest text-sm">Self-Service Center</div>
              <h2 class="text-5xl font-black text-white tracking-tight">Dịch vụ cho nhân viên.</h2>
            </div>
            <p class="text-slate-400 font-medium text-lg max-w-md">Tiết kiệm thời gian với hệ thống tự phục vụ thông
              minh, minh bạch và tức thì.</p>
          </div>

          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
            <div v-for="s in services" :key="s.id" @click="router.push(s.path)"
              class="p-8 rounded-4xl bg-white/5 border border-white/10 hover:bg-white/10 hover:border-white/20 transition-all duration-300 cursor-pointer group flex flex-col items-center text-center">
              <div
                class="w-16 h-16 rounded-2xl bg-indigo-600/20 text-indigo-400 flex items-center justify-center mb-6 group-hover:bg-indigo-600 group-hover:text-white transition-all scale-100 group-hover:scale-110">
                <component :is="s.icon" class="w-8 h-8" />
              </div>
              <span class="text-white font-bold text-sm leading-tight">{{ s.name }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Branch Network Section -->
      <section id="network" class="max-w-7xl mx-auto px-6">
        <div class="flex flex-col md:flex-row items-center justify-between mb-24 gap-8 text-center md:text-left">
          <div class="space-y-4">
            <h2 class="text-5xl font-black text-slate-900 tracking-tight">Mạng lưới Chi nhánh.</h2>
            <p class="text-slate-500 font-medium max-w-xl">Hệ thống xử lý hàng nghìn dữ liệu cán bộ nhân viên đồng bộ
              theo thời gian thực tại các chi nhánh văn phòng trên toàn quốc.</p>
          </div>
          <div class="px-6 py-4 rounded-3xl bg-indigo-50 border border-indigo-100 flex items-center space-x-4">
            <div class="w-12 h-12 rounded-full bg-indigo-500 flex items-center justify-center animate-pulse">
              <Globe class="w-6 h-6 text-white" />
            </div>
            <div>
              <p class="text-sm font-bold text-indigo-900/60 leading-tight">Tổng nhân sự</p>
              <p class="text-2xl font-black text-indigo-700">510+</p>
            </div>
          </div>
        </div>

        <div class="grid lg:grid-cols-3 gap-8">
          <div v-for="branch in branches" :key="branch.id"
            class="group relative p-8 rounded-[2.5rem] bg-slate-50 border border-slate-200 hover:bg-white hover:border-indigo-200 hover:shadow-2xl hover:shadow-indigo-100/50 transition-all duration-500 overflow-hidden cursor-default">

            <div
              class="absolute top-0 right-0 p-8 sm:p-12 opacity-5 group-hover:opacity-10 transition-opacity transform group-hover:scale-110 duration-700 pointer-events-none">
              <Globe class="w-32 h-32" />
            </div>

            <div class="flex items-center space-x-4 mb-8">
              <div
                class="w-14 h-14 rounded-2xl bg-white shadow-sm flex items-center justify-center border border-slate-100">
                <div class="w-3 h-3 rounded-full bg-emerald-500 animate-pulse"></div>
              </div>
              <div>
                <div class="text-slate-400 font-bold text-xs uppercase tracking-widest mb-1">Office</div>
                <h3 class="text-xl font-black text-slate-900">{{ branch.name }}</h3>
              </div>
            </div>

            <div class="space-y-4 mb-8">
              <div class="flex items-center text-slate-600">
                <Layout class="w-5 h-5 mr-3 text-slate-400" />
                <span class="font-medium text-sm">{{ branch.address }}</span>
              </div>
              <div class="flex items-center text-slate-600">
                <Users class="w-5 h-5 mr-3 text-slate-400" />
                <span class="font-medium text-sm"><strong class="text-slate-900">{{ branch.employees }}</strong> nhân
                  sự</span>
              </div>
            </div>

            <div class="flex items-center text-indigo-600 font-bold text-sm">
              Trạng thái: Hoạt động ổn định
              <ShieldCheck class="w-4 h-4 ml-2 text-emerald-500" />
            </div>
          </div>
        </div>
      </section>
    </div>
  </EmployeeLayout>
</template>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 1s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.animate-scale-in {
  animation: scaleIn 1.2s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
