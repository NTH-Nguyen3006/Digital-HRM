<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import StatCard from '@/components/common/StatCard.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { useToast } from '@/composables/useToast'
import {
  getTeamDashboardReport,
  getPendingLeaveRequests,
  getPendingAttendanceAdjustments,
  getPendingOvertimeRequests,
  getPendingOffboardings,
  getTeamMembers
} from '@/api/manager/manager'
import {
  LayoutDashboard, CalendarOff, Clock, AlertTriangle,
  Users, CheckCircle2, ArrowRight, Loader2, UserMinus,
  TrendingUp, Bell
} from 'lucide-vue-next'
import { useRouter } from 'vue-router'

const toast = useToast()
const router = useRouter()

/* ─── STATE ─────────────────────────────────────────────────── */
const loading = ref(true)

const statsData = ref([
  { title: 'Đơn phép chờ duyệt', value: '0', icon: CalendarOff, color: 'amber' },
  { title: 'Điều chỉnh công', value: '0', icon: Clock, color: 'indigo' },
  { title: 'Yêu cầu OT', value: '0', icon: TrendingUp, color: 'violet' },
  { title: 'Đơn thôi việc', value: '0', icon: UserMinus, color: 'rose' },
])

const pendingLeaves = ref([])
const pendingAdjustments = ref([])
const pendingOT = ref([])
const teamMembers = ref([])
const teamReport = ref(null)

/* ─── API ────────────────────────────────────────────────────── */
const fetchAll = async () => {
  loading.value = true
  try {
    const [leaveRes, adjustRes, otRes, offRes, reportRes, membersRes] = await Promise.all([
      getPendingLeaveRequests({ size: 5 }).catch(() => null),
      getPendingAttendanceAdjustments({ size: 5 }).catch(() => null),
      getPendingOvertimeRequests({ size: 5 }).catch(() => null),
      getPendingOffboardings({ size: 5 }).catch(() => null),
      getTeamDashboardReport().catch(() => null),
      getTeamMembers({ size: 8 }).catch(() => null)
    ])

    const leaves = leaveRes?.data || []
    const adjusts = adjustRes?.data || []
    const ots = otRes?.data || []
    const offs = offRes?.data || []

    teamReport.value = reportRes?.data || null
    teamMembers.value = membersRes?.data?.content || membersRes?.data || []

    pendingLeaves.value = Array.isArray(leaves) ? leaves.slice(0, 5) : leaves.content?.slice(0, 5) || []
    pendingAdjustments.value = Array.isArray(adjusts) ? adjusts.slice(0, 5) : adjusts.content?.slice(0, 5) || []
    pendingOT.value = Array.isArray(ots) ? ots.slice(0, 5) : ots.content?.slice(0, 5) || []

    statsData.value[0].value = String(Array.isArray(leaves) ? leaves.length : leaves.totalElements || 0)
    statsData.value[1].value = String(Array.isArray(adjusts) ? adjusts.length : adjusts.totalElements || 0)
    statsData.value[2].value = String(Array.isArray(ots) ? ots.length : ots.totalElements || 0)
    statsData.value[3].value = String(Array.isArray(offs) ? offs.length : offs.totalElements || 0)
  } catch (e) {
    toast.error('Không thể tải dữ liệu dashboard')
  } finally {
    loading.value = false
  }
}

onMounted(fetchAll)

function fmtDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('vi-VN')
}
</script>

<template>

  <div class="space-y-8 animate-fade-in">

    <!-- HEADER -->
    <PageHeader title="Dashboard Quản lý" subtitle="Tổng hợp các yêu cầu từ đội nhóm cần xử lý" :icon="LayoutDashboard"
      iconColor="bg-violet-600" iconShadow="shadow-violet-100" />

    <!-- Loading overlay -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <Loader2 class="w-12 h-12 text-violet-500 animate-spin" />
    </div>

    <template v-else>
      <!-- KPI STATS -->
      <!-- TEAM OVERVIEW -->
      <div class="grid lg:grid-cols-12 gap-8">
        <div class="lg:col-span-8 grid grid-cols-1 sm:grid-cols-2 gap-6">
          <StatCard v-for="stat in statsData" :key="stat.title" :title="stat.title" :value="stat.value"
            :icon="stat.icon" :color="stat.color" class="rounded-4xl!" />
        </div>

        <div class="lg:col-span-4">
          <div
            class="h-full p-8 rounded-4xl bg-slate-900 border border-slate-800 text-white relative overflow-hidden group">
            <div class="absolute -right-8 -top-8 w-32 h-32 bg-indigo-500/20 rounded-full blur-3xl"></div>
            <div class="relative z-10 space-y-6">
              <div class="flex items-center justify-between">
                <div class="w-12 h-12 rounded-2xl bg-white/10 flex items-center justify-center">
                  <Users class="w-6 h-6 text-indigo-400" />
                </div>
                <div class="text-right">
                  <p class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Quy mô đội</p>
                  <p class="text-3xl font-black">{{ teamReport?.teamHeadcount || '--' }} <small
                      class="text-sm font-bold opacity-50">NS</small></p>
                </div>
              </div>

              <div class="grid grid-cols-3 gap-2">
                <div class="p-3 rounded-2xl bg-white/5 border border-white/5 text-center">
                  <p class="text-[9px] font-black text-slate-500 uppercase mb-1">Có mặt</p>
                  <p class="text-lg font-black text-emerald-400">{{ teamReport?.presentTodayCount ?? '--' }}</p>
                </div>
                <div class="p-3 rounded-2xl bg-white/5 border border-white/5 text-center">
                  <p class="text-[9px] font-black text-slate-500 uppercase mb-1">Vắng</p>
                  <p class="text-lg font-black text-rose-400">{{ teamReport?.absentTodayCount ?? '--' }}</p>
                </div>
                <div class="p-3 rounded-2xl bg-white/5 border border-white/5 text-center">
                  <p class="text-[9px] font-black text-slate-500 uppercase mb-1">Phép</p>
                  <p class="text-lg font-black text-amber-400">{{ teamReport?.onLeaveTodayCount ?? '--' }}</p>
                </div>
              </div>

              <div class="pt-2">
                <div class="flex items-center justify-between text-[11px] font-bold text-slate-400 mb-2">
                  <span>Tỷ lệ đi làm (%)</span>
                  <span class="text-white">{{ Math.round((teamReport?.presentTodayCount / (teamReport?.teamHeadcount ||
                    1)) * 100) || 0 }}%</span>
                </div>
                <div class="h-1.5 w-full bg-white/10 rounded-full overflow-hidden">
                  <div class="h-full bg-emerald-500 rounded-full transition-all duration-1000"
                    :style="{ width: `${(teamReport?.presentTodayCount / (teamReport?.teamHeadcount || 1)) * 100}%` }">
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- DASHBOARD DETAILS -->
      <div class="grid lg:grid-cols-12 gap-8">
        <!-- LEFT: Pending Actions -->
        <div class="lg:col-span-8 space-y-8">

          <!-- PENDING LEAVES -->
          <GlassCard :glass="false" class="bg-white border border-slate-100 rounded-4xl overflow-hidden shadow-sm">
            <div class="px-8 py-6 border-b border-slate-50 flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-2xl bg-amber-100 flex items-center justify-center">
                  <CalendarOff class="w-5 h-5 text-amber-600" />
                </div>
                <div>
                  <h3 class="font-black text-slate-900 text-sm">Đơn nghỉ phép chờ duyệt</h3>
                  <p class="text-xs text-slate-400 font-medium">{{ statsData[0].value }} đơn đang chờ</p>
                </div>
              </div>
              <button @click="router.push('/manager/leaves')"
                class="text-xs font-black text-amber-600 hover:text-amber-700 flex items-center gap-1 transition-colors">
                Xem tất cả
                <ArrowRight class="w-3.5 h-3.5" />
              </button>
            </div>

            <div v-if="pendingLeaves.length > 0" class="divide-y divide-slate-50">
              <div v-for="req in pendingLeaves" :key="req.leaveRequestId"
                class="px-8 py-4 hover:bg-slate-50/60 transition-all flex items-center gap-4 cursor-pointer"
                @click="router.push('/manager/leaves')">
                <AvatarBox :name="req.employeeFullName" size="md" shape="rounded-xl" />
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-bold text-slate-900 truncate">{{ req.employeeFullName }}</p>
                  <p class="text-xs text-slate-400 font-medium">{{ req.leaveTypeName }} · {{ req.totalDays }} ngày</p>
                </div>
                <div class="text-right shrink-0">
                  <p class="text-xs font-bold text-amber-600">{{ fmtDate(req.fromDate) }}</p>
                  <p class="text-[10px] text-slate-400">→ {{ fmtDate(req.toDate) }}</p>
                </div>
              </div>
            </div>
            <div v-else class="px-8 py-10 text-center text-sm text-slate-400 font-medium">
              🎉 Không có đơn nào cần xử lý
            </div>
          </GlassCard>

          <!-- PENDING OVERTIME -->
          <GlassCard :glass="false" class="bg-white border border-slate-100 rounded-4xl overflow-hidden shadow-sm">
            <div class="px-8 py-6 border-b border-slate-50 flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-2xl bg-violet-100 flex items-center justify-center">
                  <TrendingUp class="w-5 h-5 text-violet-600" />
                </div>
                <div>
                  <h3 class="font-black text-slate-900 text-sm">Yêu cầu tăng ca (OT)</h3>
                  <p class="text-xs text-slate-400 font-medium">{{ statsData[2].value }} yêu cầu chờ</p>
                </div>
              </div>
              <button @click="router.push('/manager/attendance')"
                class="text-xs font-black text-violet-600 hover:text-violet-700 flex items-center gap-1 transition-colors">
                Xem tất cả
                <ArrowRight class="w-3.5 h-3.5" />
              </button>
            </div>

            <div v-if="pendingOT.length > 0" class="divide-y divide-slate-50">
              <div v-for="req in pendingOT" :key="req.overtimeRequestId"
                class="px-8 py-4 hover:bg-slate-50/60 transition-all flex items-center gap-4 cursor-pointer"
                @click="router.push('/manager/attendance')">
                <AvatarBox :name="req.employeeFullName" size="md" shape="rounded-xl" />
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-bold text-slate-900 truncate">{{ req.employeeFullName }}</p>
                  <p class="text-xs text-slate-400 font-medium">{{ req.reason || 'Tăng ca' }}</p>
                </div>
                <div class="text-right shrink-0">
                  <p class="text-xs font-bold text-violet-600">{{ fmtDate(req.date) }}</p>
                  <p class="text-[10px] text-slate-400">{{ req.startTime }} – {{ req.endTime }}</p>
                </div>
              </div>
            </div>
            <div v-else class="px-8 py-10 text-center text-sm text-slate-400 font-medium">
              Không có yêu cầu OT nào
            </div>
          </GlassCard>

          <!-- PENDING ATTENDANCE ADJUSTMENT -->
          <GlassCard :glass="false" class="bg-white border border-slate-100 rounded-4xl overflow-hidden shadow-sm">
            <div class="px-8 py-6 border-b border-slate-50 flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-2xl bg-indigo-100 flex items-center justify-center">
                  <Clock class="w-5 h-5 text-indigo-600" />
                </div>
                <div>
                  <h3 class="font-black text-slate-900 text-sm">Điều chỉnh chấm công</h3>
                  <p class="text-xs text-slate-400 font-medium">{{ statsData[1].value }} đơn quên quẹt thẻ</p>
                </div>
              </div>
              <button @click="router.push('/manager/attendance')"
                class="text-xs font-black text-indigo-600 hover:text-indigo-700 flex items-center gap-1 transition-colors">
                Xem tất cả
                <ArrowRight class="w-3.5 h-3.5" />
              </button>
            </div>

            <div v-if="pendingAdjustments.length > 0" class="divide-y divide-slate-50">
              <div v-for="req in pendingAdjustments" :key="req.adjustmentRequestId"
                class="px-8 py-4 hover:bg-slate-50/60 transition-all flex items-center gap-4 cursor-pointer"
                @click="router.push('/manager/attendance')">
                <AvatarBox :name="req.employeeFullName" size="md" shape="rounded-xl" />
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-bold text-slate-900 truncate">{{ req.employeeFullName }}</p>
                  <p class="text-xs text-slate-400 font-medium">{{ req.reason || 'Quên quẹt thẻ' }}</p>
                </div>
                <p class="text-xs font-bold text-indigo-600 shrink-0">{{ fmtDate(req.attendanceDate) }}</p>
              </div>
            </div>
            <div v-else class="px-8 py-10 text-center text-sm text-slate-400 font-medium">
              Không có điều chỉnh nào
            </div>
          </GlassCard>

        </div>

        <!-- RIGHT: Members & Tools -->
        <div class="lg:col-span-4 space-y-8">
          <!-- TEAM MEMBERS -->
          <GlassCard :glass="false" class="bg-white border border-slate-100 rounded-4xl overflow-hidden shadow-sm">
            <div class="px-8 py-6 border-b border-slate-50 flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-2xl bg-slate-100 flex items-center justify-center">
                  <Users class="w-5 h-5 text-slate-600" />
                </div>
                <div>
                  <h3 class="font-black text-slate-900 text-sm">Nhân viên dưới quyền</h3>
                  <p class="text-xs text-slate-400 font-medium">Trực thuộc đội nhóm</p>
                </div>
              </div>
              <button @click="router.push('/employees')"
                class="text-xs font-black text-slate-600 hover:text-indigo-600 flex items-center gap-1 transition-colors">
                Tất cả
                <ArrowRight class="w-3.5 h-3.5" />
              </button>
            </div>

            <div v-if="teamMembers.length > 0" class="divide-y divide-slate-50 px-4 py-2">
              <div v-for="member in teamMembers" :key="member.employeeId"
                class="p-4 hover:bg-slate-50 rounded-2xl transition-all flex items-center gap-4 cursor-pointer"
                @click="router.push(`/employees/${member.employeeId}`)">
                <AvatarBox :name="member.fullName" size="sm" shape="rounded-xl" />
                <div class="flex-1 min-w-0">
                  <p class="text-xs font-black text-slate-900 truncate">{{ member.fullName }}</p>
                  <p class="text-[10px] text-slate-400 font-bold uppercase tracking-wider">{{ member.jobTitleName }}</p>
                </div>
                <div class="shrink-0 w-2 h-2 rounded-full bg-emerald-400"></div>
              </div>
            </div>
            <div v-else class="px-8 py-10 text-center text-sm text-slate-400 font-medium italic">
              (Chưa có danh sách nhân viên)
            </div>
          </GlassCard>

          <!-- QUICK ACTIONS -->
          <GlassCard :glass="false" class="bg-white border border-slate-100 rounded-4xl p-8 shadow-sm">
            <h3 class="font-black text-slate-900 text-sm mb-6 flex items-center gap-2">
              <Bell class="w-4 h-4 text-violet-600" /> Truy cập nhanh
            </h3>
            <div class="space-y-3">
              <button v-for="action in [
                { label: 'Phê duyệt nghỉ phép', sub: 'Xem tất cả đơn xin nghỉ', icon: CalendarOff, color: 'bg-amber-50 text-amber-600 hover:bg-amber-100', path: '/manager/leaves' },
                { label: 'Duyệt chấm công & OT', sub: 'Điều chỉnh giờ công và tăng ca', icon: Clock, color: 'bg-indigo-50 text-indigo-600 hover:bg-indigo-100', path: '/manager/attendance' },
                { label: 'Bảng lương đội nhóm', sub: 'Xác nhận phiếu lương tháng', icon: CheckCircle2, color: 'bg-emerald-50 text-emerald-600 hover:bg-emerald-100', path: '/manager/payroll' },
              ]" :key="action.label" @click="router.push(action.path)"
                class="w-full flex items-center gap-4 p-4 rounded-2xl transition-all group" :class="action.color">
                <component :is="action.icon" class="w-5 h-5 shrink-0" />
                <div class="text-left flex-1">
                  <p class="text-sm font-black">{{ action.label }}</p>
                  <p class="text-[10px] font-medium opacity-70">{{ action.sub }}</p>
                </div>
                <ArrowRight class="w-4 h-4 opacity-0 group-hover:opacity-100 transition-opacity" />
              </button>
            </div>
          </GlassCard>
        </div>

      </div>
    </template>
  </div>

</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
