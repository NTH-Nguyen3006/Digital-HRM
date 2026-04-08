<script setup>
import GlassCard from "@/components/common/GlassCard.vue"
import StatCard from "@/components/common/StatCard.vue"
import {
  BarChart3,
  Briefcase,
  Calendar,
  Clock,
  Download,
  FileWarning,
  Loader2,
  PieChart,
  TrendingUp,
  UserMinus,
  UserPlus,
  Users
} from "lucide-vue-next"
import { useAuthStore } from '@/stores/auth'
import { onMounted, ref, computed } from 'vue'
import VueApexCharts from "vue3-apexcharts"

const authStore = useAuthStore()
const isHR = authStore.isHR
const isAdmin = authStore.isAdmin

import { getExpiringContracts } from '@/api/admin/contract.js'
import { getEmployees } from '@/api/admin/employee.js'
import { getLeaveRequests } from '@/api/admin/leave.js'
import { getHeadcountDashboard } from '@/api/admin/report.js'

const loading = ref(true)
const statsData = ref([
  { title: "Tổng nhân sự", value: "0", icon: Users, color: "indigo", trend: 0, visible: true },
  { title: "Nhân viên mới", value: "0", icon: UserPlus, color: "emerald", trend: 0, visible: true },
  { title: "Đang thử việc", value: "0", icon: Briefcase, color: "amber", visible: true },
  { title: "Nghỉ việc tháng này", value: "0", icon: UserMinus, color: "rose", visible: true }
])

const alerts = ref([
  { title: "Hợp đồng sắp hết hạn", count: 0, icon: FileWarning, color: 'text-amber-500', bg: 'bg-amber-50' },
  { title: "Đơn phép chờ duyệt", count: 0, icon: Clock, color: 'text-indigo-500', bg: 'bg-indigo-50' }
])

const headcountSeries = ref([{ name: "Nhân sự", data: [] }])
const headcountOptions = ref({
  chart: { type: "bar", toolbar: { show: false }, fontFamily: 'Inter, sans-serif' },
  colors: ["#6366f1"],
  plotOptions: { bar: { borderRadius: 8, columnWidth: "50%" } },
  dataLabels: { enabled: false },
  xaxis: { categories: [], axisBorder: { show: false } },
  grid: { borderColor: "#f1f5f9", strokeDashArray: 4 }
})

const trendSeries = ref([
  { name: "Nhân viên vào", data: [] },
  { name: "Nghỉ việc", data: [] }
])
const trendOptions = ref({
  chart: { type: "line", toolbar: { show: false }, fontFamily: 'Inter, sans-serif' },
  stroke: { curve: "smooth", width: 4 },
  colors: ["#10b981", "#ef4444"],
  xaxis: { categories: ["T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"] },
  legend: { position: "top", horizontalAlign: 'right', fontWeight: 700 },
  grid: { borderColor: "#f1f5f9", strokeDashArray: 4 }
})

const contractSeries = ref([])
const contractOptions = ref({
  chart: { type: "donut" },
  labels: ["Xác định thời hạn", "Không xác định", "Thử việc", "Khác"],
  colors: ["#6366f1", "#22c55e", "#f59e0b", "#ef4444"],
  legend: { position: "bottom", fontWeight: 600 },
  stroke: { width: 0 },
  plotOptions: { pie: { donut: { size: '75%' } } }
})

/* ------------------ DATA FETCHING ------------------ */

const fetchDashboard = async () => {
  loading.value = true
  try {
    const [hcRes, empRes, expRes, leaveRes] = await Promise.all([
      getHeadcountDashboard(),
      getEmployees({ size: 1 }),
      getExpiringContracts(30),
      getLeaveRequests({ status: 'PENDING' })
    ])

    // 1. Stats
    statsData.value[0].value = empRes.metadata?.totalElements?.toString() || "0"
    statsData.value[1].value = (hcRes.data?.newJoinsCurrentMonth || 0).toString()
    statsData.value[2].value = (hcRes.data?.probationCount || 0).toString()
    statsData.value[3].value = (hcRes.data?.resignedCurrentMonth || 0).toString()

    // 2. Alerts
    alerts.value[0].count = expRes.data?.length || 0
    alerts.value[1].count = leaveRes.data?.length || 0

    // 3. Dept Chart
    if (hcRes.data?.deptDistribution) {
      headcountOptions.value.xaxis.categories = hcRes.data.deptDistribution.map(d => d.name)
      headcountSeries.value[0].data = hcRes.data.deptDistribution.map(d => d.count)
    }

    // 4. Trend Chart
    if (hcRes.data?.monthlyTrends) {
      trendSeries.value[0].data = hcRes.data.monthlyTrends.map(m => m.joins)
      trendSeries.value[1].data = hcRes.data.monthlyTrends.map(m => m.leaves)
    }

    // 5. Contract Donut
    if (hcRes.data?.contractDistribution) {
      contractSeries.value = [
        hcRes.data.contractDistribution.FIXED_TERM || 0,
        hcRes.data.contractDistribution.INDEFINITE || 0,
        hcRes.data.contractDistribution.PROBATION || 0,
        hcRes.data.contractDistribution.OTHER || 0
      ]
    }

  } catch (error) {
    console.error("Failed to load dashboard data:", error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchDashboard)

</script>

<template>

  <div class="space-y-10 animate-fade-in relative">

    <!-- Loading Overlay -->
    <div v-if="loading" class="fixed inset-0 z-100 bg-white/40 backdrop-blur-[2px] flex items-center justify-center">
      <div class="flex flex-col items-center gap-4">
        <Loader2 class="w-12 h-12 text-indigo-600 animate-spin" />
        <p class="text-sm font-black text-slate-400 uppercase tracking-[0.3em]">Đang tổng hợp dữ liệu...</p>
      </div>
    </div>

    <!-- HEADER -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
      <div>
        <h2 class="text-4xl font-black text-slate-900 tracking-tight flex items-center gap-3">
          <BarChart3 class="w-8 h-8 text-indigo-600" /> Dashboard
        </h2>
        <p class="text-slate-500 font-medium ml-1">Chào buổi sáng! Đây là tình hình nhân sự hiện tại của hệ thống.</p>
      </div>
      <div class="flex items-center gap-3">
        <button
          class="flex items-center gap-2 px-5 py-3 rounded-2xl font-bold bg-white text-slate-600 border border-slate-100 hover:bg-slate-50 transition-all shadow-sm">
          <Download class="w-4 h-4" /> Xuất báo cáo
        </button>
      </div>
    </div>

    <!-- KPI STATS -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <StatCard v-for="stat in statsData" :key="stat.title" :title="stat.title" :value="stat.value" :icon="stat.icon"
        :color="stat.color" :trend="stat.trend"
        class="rounded-[32px]! hover:scale-[1.02] transition-transform duration-300" />
    </div>

    <!-- MAIN INSIGHTS -->
    <div class="grid lg:grid-cols-3 gap-8">

      <!-- SYSTEM ALERTS -->
      <GlassCard class="bg-white border border-slate-100 rounded-[32px] p-8">
        <div class="flex items-center justify-between mb-8">
          <h3 class="text-xl font-black text-slate-900">Thông báo cần xử lý</h3>
          <span class="px-3 py-1 bg-rose-50 text-rose-600 text-[10px] font-black uppercase rounded-lg">Realtime</span>
        </div>

        <div class="space-y-4">
          <div v-for="alert in alerts" :key="alert.title"
            class="group p-5 rounded-3xl bg-slate-50/50 hover:bg-white border border-transparent hover:border-slate-100 hover:shadow-xl hover:shadow-slate-100/50 transition-all cursor-pointer flex items-center justify-between">
            <div class="flex items-center gap-4">
              <div :class="['w-12 h-12 rounded-2xl flex items-center justify-center shadow-sm', alert.bg, alert.color]">
                <component :is="alert.icon" class="w-6 h-6" />
              </div>
              <div>
                <p class="text-sm font-black text-slate-800">{{ alert.title }}</p>
                <p class="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Cần ưu tiên</p>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-lg font-black text-slate-900">{{ alert.count }}</span>
              <ChevronRight class="w-4 h-4 text-slate-300 group-hover:text-indigo-600 transition-colors" />
            </div>
          </div>
        </div>

        <div
          class="mt-8 p-6 bg-indigo-600 rounded-[28px] text-white shadow-xl shadow-indigo-100 overflow-hidden relative">
          <div class="relative z-10">
            <p class="text-[10px] font-black uppercase tracking-widest opacity-60 mb-2">Lời nhắc hôm nay</p>
            <p class="text-sm font-bold leading-relaxed">Đừng quên kiểm tra các hồ sơ Onboarding mới cho phòng
              Engineering.</p>
          </div>
          <Calendar class="absolute -right-4 -bottom-4 w-24 h-24 opacity-10 rotate-12" />
        </div>
      </GlassCard>

      <!-- CONTRACT DONUT -->
      <GlassCard class="lg:col-span-2 bg-white border border-slate-100 rounded-[32px] p-8">
        <div class="flex items-center justify-between mb-8">
          <div>
            <h3 class="text-xl font-black text-slate-900">Cấu trúc hợp đồng</h3>
            <p class="text-xs text-slate-400 font-bold uppercase tracking-widest mt-1">Phân bổ loại hình nhân sự</p>
          </div>
          <PieChart class="w-6 h-6 text-slate-200" />
        </div>

        <div class="flex flex-col md:flex-row items-center justify-around gap-10">
          <div class="w-full max-w-[320px]">
            <VueApexCharts type="donut" height="340" :options="contractOptions" :series="contractSeries" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div v-for="(lab, idx) in contractOptions.labels" :key="lab"
              class="p-4 bg-slate-50 rounded-2xl border border-slate-100/50">
              <div class="flex items-center gap-2 mb-1">
                <div class="w-2 h-2 rounded-full" :style="{ backgroundColor: contractOptions.colors[idx] }"></div>
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-tighter">{{ lab }}</p>
              </div>
              <p class="text-lg font-black text-slate-700">{{ contractSeries[idx] || 0 }}</p>
            </div>
          </div>
        </div>
      </GlassCard>
    </div>

    <!-- SECONDARY CHARTS -->
    <div class="grid lg:grid-cols-2 gap-8">

      <!-- DEPT HEADCOUNT -->
      <GlassCard class="bg-white border border-slate-100 rounded-[32px] p-8">
        <div class="flex items-center justify-between mb-8">
          <h3 class="text-xl font-black text-slate-900 flex items-center gap-2">
            <Users class="w-5 h-5 text-indigo-500" /> Headcount theo phòng ban
          </h3>
        </div>
        <VueApexCharts type="bar" height="320" :options="headcountOptions" :series="headcountSeries" />
      </GlassCard>

      <!-- JOIN VS LEAVE TREND -->
      <GlassCard class="bg-white border border-slate-100 rounded-[32px] p-8">
        <div class="flex items-center justify-between mb-8">
          <h3 class="text-xl font-black text-slate-900 flex items-center gap-2">
            <TrendingUp class="w-5 h-5 text-emerald-500" /> Biến động nhân sự
          </h3>
        </div>
        <VueApexCharts type="line" height="320" :options="trendOptions" :series="trendSeries" />
      </GlassCard>

    </div>

  </div>

</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
