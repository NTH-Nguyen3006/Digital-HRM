<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import VueApexCharts from 'vue3-apexcharts'
import {
  BarChart3,
  Briefcase,
  Calendar,
  Check,
  ChevronDown,
  ChevronRight,
  Clock,
  Download,
  FileWarning,
  Loader2,
  PieChart,
  TrendingUp,
  UserMinus,
  UserPlus,
  Users,
  X,
} from 'lucide-vue-next'

import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'
import { getExpiringContracts } from '@/api/admin/contract.js'
import { getOrgUnitTree } from '@/api/admin/orgUnit.js'
import { getHeadcountDashboard } from '@/api/admin/report.js'
import { unwrapData } from '@/utils/api'
import { formatNumber } from '@/utils/format'

const loading = ref(true)
const router = useRouter()

const statsData = ref([
  { title: 'Tổng nhân sự', value: '0', icon: Users, color: 'indigo', trend: 0, visible: true },
  { title: 'Nhân viên mới', value: '0', icon: UserPlus, color: 'emerald', trend: 0, visible: true },
  { title: 'Đang thử việc', value: '0', icon: Briefcase, color: 'amber', visible: true },
  { title: 'Nghỉ việc tháng này', value: '0', icon: UserMinus, color: 'rose', visible: true },
])

const alerts = ref([
  { title: 'Hợp đồng sắp hết hạn', count: 0, icon: FileWarning, color: 'text-amber-500', bg: 'bg-amber-50', path: '/contracts' },
  { title: 'Onboarding chờ xử lý', count: 0, icon: Clock, color: 'text-indigo-500', bg: 'bg-indigo-50', path: '/onboarding' },
])

const headcountBreakdown = ref([])
const orgUnitCodeLookup = ref({})
const selectedHeadcountFilters = ref([])
const headcountFilterPanelOpen = ref(false)

const trendSeries = ref([
  { name: 'Nhân viên vào', data: [] },
  { name: 'Nghỉ việc', data: [] },
])

const trendOptions = ref({
  chart: { type: 'line', toolbar: { show: false }, fontFamily: 'Inter, sans-serif' },
  stroke: { curve: 'smooth', width: 4 },
  colors: ['#10b981', '#ef4444'],
  xaxis: { categories: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6'] },
  legend: { position: 'top', horizontalAlign: 'right', fontWeight: 700 },
  grid: { borderColor: '#f1f5f9', strokeDashArray: 4 },
})

const contractSeries = ref([])
const contractOptions = ref({
  chart: { type: 'donut' },
  labels: [],
  colors: ['#6366f1', '#22c55e', '#f59e0b', '#ef4444'],
  legend: { position: 'bottom', fontWeight: 600 },
  stroke: { width: 0 },
  plotOptions: {
    pie: {
      donut: {
        size: '75%',
        labels: {
          show: true,
          name: {
            show: true,
            fontSize: '14px',
            fontWeight: 700,
            color: '#64748b',
          },
          value: {
            show: true,
            fontSize: '24px',
            fontWeight: 800,
            color: '#0f172a',
            formatter: (value) => formatNumber(value),
          },
          total: {
            show: true,
            label: 'Tổng số',
            fontSize: '13px',
            fontWeight: 700,
            color: '#94a3b8',
            formatter: ({ globals }) =>
              formatNumber(globals.seriesTotals.reduce((total, current) => total + current, 0)),
          },
        },
      },
    },
  },
  tooltip: {
    y: {
      formatter: (value) => `${formatNumber(value)} nhân sự`,
    },
  },
})

const statusColorMap = {
  ACTIVE: '#6366f1',
  PROBATION: '#f59e0b',
  SUSPENDED: '#ef4444',
  RESIGNED: '#22c55e',
  TERMINATED: '#0f172a',
}

const employmentStatusMeta = [
  { key: 'ACTIVE', label: 'Chính thức' },
  { key: 'PROBATION', label: 'Thử việc' },
  { key: 'SUSPENDED', label: 'Tạm đình chỉ' },
  { key: 'RESIGNED', label: 'Đã nghỉ việc' },
  { key: 'TERMINATED', label: 'Chấm dứt' },
]

const contractChartKey = computed(() =>
  JSON.stringify({
    labels: contractOptions.value.labels,
    series: contractSeries.value,
  }),
)

const availableHeadcountFilters = computed(() =>
  headcountBreakdown.value.map((item) => ({
    ...item,
    orgUnitId: orgUnitCodeLookup.value[item.key] || null,
  })),
)

const filteredHeadcountBreakdown = computed(() => {
  if (!selectedHeadcountFilters.value.length) return headcountBreakdown.value

  return headcountBreakdown.value.filter((item) =>
    selectedHeadcountFilters.value.includes(item.key),
  )
})

const selectedHeadcountTotal = computed(() =>
  filteredHeadcountBreakdown.value.reduce((sum, item) => sum + Number(item.value || 0), 0),
)

const selectedHeadcountItems = computed(() =>
  availableHeadcountFilters.value.filter((item) => selectedHeadcountFilters.value.includes(item.key)),
)

const visibleSelectedHeadcountItems = computed(() => selectedHeadcountItems.value.slice(0, 2))

const hiddenSelectedHeadcountCount = computed(() =>
  Math.max(0, selectedHeadcountItems.value.length - visibleSelectedHeadcountItems.value.length),
)

const headcountFilterLabel = computed(() => {
  if (!selectedHeadcountItems.value.length) return 'Chọn phòng ban'
  if (selectedHeadcountItems.value.length === 1) return selectedHeadcountItems.value[0].label
  return `${selectedHeadcountItems.value.length} phòng ban`
})

const headcountSeries = computed(() => [
  {
    name: 'Nhân sự',
    data: filteredHeadcountBreakdown.value.map((item) => Number(item.value || 0)),
  },
])

const headcountChartKey = computed(() =>
  JSON.stringify(filteredHeadcountBreakdown.value.map((item) => `${item.key}:${item.value}`)),
)

const headcountChartMinWidth = computed(() =>
  Math.max(560, filteredHeadcountBreakdown.value.length * 92),
)

const headcountOptions = computed(() => ({
  chart: {
    type: 'bar',
    toolbar: { show: false },
    fontFamily: 'Inter, sans-serif',
    events: {
      dataPointSelection: (_event, _chartContext, config) => {
        const item = filteredHeadcountBreakdown.value[config.dataPointIndex]
        if (!item) return
        openHeadcountOrgUnit(item)
      },
    },
  },
  colors: ['#6366f1'],
  plotOptions: {
    bar: {
      borderRadius: 10,
      columnWidth: filteredHeadcountBreakdown.value.length > 4 ? '58%' : '42%',
      distributed: false,
    },
  },
  states: {
    hover: {
      filter: {
        type: 'lighten',
        value: 0.08,
      },
    },
  },
  dataLabels: { enabled: false },
  xaxis: {
    categories: filteredHeadcountBreakdown.value.map((item) => item.label),
    labels: {
      rotate: -18,
      style: {
        fontWeight: 700,
        colors: '#64748b',
      },
      trim: true,
      maxHeight: 90,
    },
    axisBorder: { show: false },
  },
  yaxis: {
    labels: {
      style: {
        colors: '#94a3b8',
        fontWeight: 600,
      },
      formatter: (value) => formatNumber(value),
    },
  },
  noData: {
    text: 'Không có dữ liệu phù hợp',
    align: 'center',
    verticalAlign: 'middle',
    style: {
      color: '#94a3b8',
      fontSize: '13px',
      fontWeight: 700,
    },
  },
  tooltip: {
    y: {
      formatter: (value) => `${formatNumber(value)} nhân sự`,
    },
  },
  grid: { borderColor: '#f1f5f9', strokeDashArray: 4 },
}))

function flattenOrgTree(nodes = []) {
  return nodes.flatMap((node) => [node, ...flattenOrgTree(node.children || [])])
}

function getCurrentMonthMovement(monthlyMovement = []) {
  return monthlyMovement[monthlyMovement.length - 1] || null
}

function navigateToAlert(path) {
  if (!path) return
  router.push(path)
}

function isHeadcountFilterActive(code) {
  return selectedHeadcountFilters.value.includes(code)
}

function toggleHeadcountFilter(code) {
  if (!code) return

  if (isHeadcountFilterActive(code)) {
    selectedHeadcountFilters.value = selectedHeadcountFilters.value.filter((item) => item !== code)
    return
  }

  selectedHeadcountFilters.value = [...selectedHeadcountFilters.value, code]
}

function resetHeadcountFilter() {
  selectedHeadcountFilters.value = []
}

function openHeadcountOrgUnit(item) {
  const orgUnitId = item?.orgUnitId || orgUnitCodeLookup.value[item?.key]
  if (!orgUnitId) return

  router.push({
    path: '/employees',
    query: {
      orgUnitId: String(orgUnitId),
      orgUnitName: item.label,
      orgUnitCode: item.key,
    },
  })
}

async function fetchDashboard() {
  loading.value = true
  try {
    const [hcRes, expRes, orgTreeRes] = await Promise.all([
      getHeadcountDashboard(),
      getExpiringContracts(30),
      getOrgUnitTree(),
    ])

    const dashboard = unwrapData(hcRes) || {}
    const expiringContracts = unwrapData(expRes) || []

    const orgTree = flattenOrgTree(unwrapData(orgTreeRes) || [])
    orgUnitCodeLookup.value = orgTree.reduce((lookup, item) => {
      lookup[item.orgUnitCode] = item.orgUnitId
      return lookup
    }, {})

    const currentMonthMovement = getCurrentMonthMovement(dashboard.monthlyMovement || [])

    statsData.value[0].value = String(dashboard.totalEmployeeCount || 0)
    statsData.value[1].value = String(currentMonthMovement?.joinerCount || 0)
    statsData.value[2].value = String(dashboard.probationEmployeeCount || 0)
    statsData.value[3].value = String(currentMonthMovement?.leaverCount || 0)

    alerts.value[0].count = Array.isArray(expiringContracts) ? expiringContracts.length : 0
    alerts.value[1].count = dashboard.pendingOnboardingCount || 0

    headcountBreakdown.value = (dashboard.orgUnitHeadcountBreakdown || []).map((item) => ({
      ...item,
      value: Number(item.value || 0),
    }))

    trendOptions.value.xaxis.categories = (dashboard.monthlyMovement || []).map((item) => item.periodLabel)
    trendSeries.value[0].data = (dashboard.monthlyMovement || []).map((item) => item.joinerCount || 0)
    trendSeries.value[1].data = (dashboard.monthlyMovement || []).map((item) => item.leaverCount || 0)

    const employmentStatusLookup = Object.fromEntries(
      (dashboard.employmentStatusBreakdown || []).map((item) => [
        item.key,
        {
          label: item.label,
          value: item.value || 0,
        },
      ]),
    )

    const normalizedEmploymentStatusBreakdown = employmentStatusMeta.map((status) => ({
      key: status.key,
      label: status.label,
      value: employmentStatusLookup[status.key]?.value || 0,
    }))

    contractSeries.value = normalizedEmploymentStatusBreakdown.map((item) => item.value)
    contractOptions.value = {
      ...contractOptions.value,
      labels: normalizedEmploymentStatusBreakdown.map((item) => item.label),
      colors: normalizedEmploymentStatusBreakdown.map((item) => statusColorMap[item.key] || '#94a3b8'),
    }
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchDashboard)
</script>

<template>
  <div class="relative space-y-10 animate-fade-in">
    <div v-if="loading" class="fixed inset-0 z-[100] flex items-center justify-center bg-white/45 backdrop-blur-[2px]">
      <div class="flex flex-col items-center gap-4">
        <Loader2 class="h-12 w-12 animate-spin text-indigo-600" />
        <p class="text-sm font-black uppercase tracking-[0.3em] text-slate-400">Đang tổng hợp dữ liệu...</p>
      </div>
    </div>

    <div class="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
      <div>
        <h2 class="flex items-center gap-3 text-3xl font-black tracking-tight text-slate-900 sm:text-4xl">
          <BarChart3 class="h-8 w-8 text-indigo-600" /> Dashboard
        </h2>
        <p class="ml-1 mt-2 text-sm font-medium text-slate-500 sm:text-base">
          Chào buổi sáng! Đây là tình hình nhân sự hiện tại của hệ thống.
        </p>
      </div>

      <div class="flex items-center gap-3">
        <button
          class="flex items-center gap-2 rounded-2xl border border-slate-100 bg-white px-5 py-3 font-bold text-slate-600 shadow-sm transition-all hover:bg-slate-50"
        >
          <Download class="h-4 w-4" /> Xuất báo cáo
        </button>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
      <StatCard
        v-for="stat in statsData"
        :key="stat.title"
        :title="stat.title"
        :value="stat.value"
        :icon="stat.icon"
        :color="stat.color"
        :trend="stat.trend"
        class="rounded-[32px]! transition-transform duration-300 hover:scale-[1.02]"
      />
    </div>

    <div class="grid gap-8 lg:grid-cols-3">
      <GlassCard class="rounded-[32px] border border-slate-100 bg-white p-8">
        <div class="mb-8 flex items-center justify-between">
          <h3 class="text-xl font-black text-slate-900">Thông báo cần xử lý</h3>
          <span class="rounded-lg bg-rose-50 px-3 py-1 text-[10px] font-black uppercase text-rose-600">Realtime</span>
        </div>

        <div class="space-y-4">
          <div
            v-for="alert in alerts"
            :key="alert.title"
            class="group flex cursor-pointer items-center justify-between rounded-3xl border border-transparent bg-slate-50/50 p-5 transition-all hover:border-slate-100 hover:bg-white hover:shadow-xl hover:shadow-slate-100/50"
            role="button"
            tabindex="0"
            @click="navigateToAlert(alert.path)"
            @keydown.enter="navigateToAlert(alert.path)"
            @keydown.space.prevent="navigateToAlert(alert.path)"
          >
            <div class="flex items-center gap-4">
              <div :class="['flex h-12 w-12 items-center justify-center rounded-2xl shadow-sm', alert.bg, alert.color]">
                <component :is="alert.icon" class="h-6 w-6" />
              </div>
              <div>
                <p class="text-sm font-black text-slate-800">{{ alert.title }}</p>
                <p class="text-[10px] font-bold uppercase tracking-wider text-slate-400">Cần ưu tiên</p>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-lg font-black text-slate-900">{{ alert.count }}</span>
              <ChevronRight class="h-4 w-4 text-slate-300 transition-colors group-hover:text-indigo-600" />
            </div>
          </div>
        </div>

        <div class="relative mt-8 overflow-hidden rounded-[28px] bg-indigo-600 p-6 text-white shadow-xl shadow-indigo-100">
          <div class="relative z-10">
            <p class="mb-2 text-[10px] font-black uppercase tracking-widest opacity-60">Lời nhắc hôm nay</p>
            <p class="text-sm font-bold leading-relaxed">
              Kiểm tra onboarding chờ xử lý và các hợp đồng sắp hết hạn để xử lý kịp thời.
            </p>
          </div>
          <Calendar class="absolute -bottom-4 -right-4 h-24 w-24 rotate-12 opacity-10" />
        </div>
      </GlassCard>

      <GlassCard class="rounded-[32px] border border-slate-100 bg-white p-5 sm:p-8 lg:col-span-2">
        <div class="mb-6 flex items-center justify-between sm:mb-8">
          <div>
            <h3 class="text-xl font-black text-slate-900">Cơ cấu nhân sự</h3>
            <p class="mt-1 text-xs font-bold uppercase tracking-widest text-slate-400">Phân bổ theo trạng thái lao động</p>
          </div>
          <PieChart class="h-6 w-6 text-slate-200" />
        </div>

        <div class="flex flex-col items-center gap-6 md:flex-row md:items-center md:justify-around md:gap-10">
          <div class="w-full max-w-[320px]">
            <VueApexCharts :key="contractChartKey" type="donut" height="340" :options="contractOptions" :series="contractSeries" />
          </div>
          <div class="grid w-full grid-cols-2 gap-3 md:w-auto md:gap-4">
            <div
              v-for="(label, idx) in contractOptions.labels"
              :key="label"
              class="rounded-2xl border border-slate-100/50 bg-slate-50 p-3 sm:p-4"
            >
              <div class="mb-1 flex items-center gap-2">
                <div class="h-2 w-2 rounded-full" :style="{ backgroundColor: contractOptions.colors[idx] }"></div>
                <p class="text-[8px] font-black uppercase tracking-tight text-slate-400 sm:text-[9px]">{{ label }}</p>
              </div>
              <p class="text-base font-black text-slate-700 sm:text-lg">{{ formatNumber(contractSeries[idx] || 0) }}</p>
            </div>
          </div>
        </div>
      </GlassCard>
    </div>

    <div class="grid gap-8 lg:grid-cols-2">
      <GlassCard class="overflow-hidden rounded-[32px] border border-slate-100 bg-white p-6 sm:p-8">
        <div class="mb-6 flex flex-col gap-4">
          <div class="flex flex-col gap-3 xl:flex-row xl:items-start xl:justify-between">
            <div>
              <h3 class="flex items-center gap-2 text-xl font-black text-slate-900">
                <Users class="h-5 w-5 text-indigo-500" /> Headcount theo phòng ban
              </h3>
              <p class="mt-1 text-sm font-medium text-slate-500">
                Lọc nhanh theo từng đơn vị. Bấm vào cột để mở danh sách nhân sự đã lọc.
              </p>
            </div>

            <div class="rounded-2xl bg-slate-50 px-4 py-3">
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">
                {{ selectedHeadcountFilters.length ? 'Đang xem' : 'Toàn bộ' }}
              </p>
              <p class="mt-1 text-lg font-black text-slate-900">
                {{ formatNumber(selectedHeadcountTotal) }} nhân sự
              </p>
            </div>
          </div>

          <div class="space-y-3">
            <div class="flex flex-wrap items-center gap-2">
              <button
                type="button"
                class="rounded-full px-3 py-2 text-xs font-black uppercase tracking-[0.18em] transition-all"
                :class="!selectedHeadcountFilters.length ? 'bg-slate-900 text-white shadow-sm' : 'bg-slate-100 text-slate-500 hover:text-slate-800'"
                @click="resetHeadcountFilter"
              >
                Tất cả
              </button>

              <button
                type="button"
                class="inline-flex max-w-full items-center gap-2 rounded-full border px-3 py-2 text-xs font-black uppercase tracking-[0.12em] transition-all"
                :class="headcountFilterPanelOpen || selectedHeadcountFilters.length
                  ? 'border-indigo-200 bg-indigo-50 text-indigo-700'
                  : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300'"
                @click="headcountFilterPanelOpen = !headcountFilterPanelOpen"
              >
                <span class="truncate">{{ headcountFilterLabel }}</span>
                <span
                  v-if="selectedHeadcountFilters.length"
                  class="rounded-full bg-white/90 px-2 py-0.5 text-[10px] font-black text-indigo-700"
                >
                  {{ selectedHeadcountFilters.length }}
                </span>
                <ChevronDown class="h-3.5 w-3.5 transition-transform" :class="headcountFilterPanelOpen ? 'rotate-180' : ''" />
              </button>

              <button
                v-for="item in visibleSelectedHeadcountItems"
                :key="item.key"
                type="button"
                class="inline-flex max-w-full items-center gap-2 rounded-full bg-slate-100 px-3 py-2 text-xs font-black uppercase tracking-[0.08em] text-slate-600 transition-all hover:bg-slate-200"
                @click="toggleHeadcountFilter(item.key)"
              >
                <span class="truncate">{{ item.label }} · {{ formatNumber(item.value) }}</span>
                <X class="h-3.5 w-3.5" />
              </button>

              <span
                v-if="hiddenSelectedHeadcountCount"
                class="inline-flex items-center rounded-full bg-slate-100 px-3 py-2 text-xs font-black uppercase tracking-[0.08em] text-slate-500"
              >
                +{{ hiddenSelectedHeadcountCount }}
              </span>

              <button
                v-if="selectedHeadcountFilters.length"
                type="button"
                class="text-xs font-bold text-slate-400 transition hover:text-slate-700"
                @click="resetHeadcountFilter"
              >
                Xóa chọn
              </button>
            </div>

            <div
              v-if="headcountFilterPanelOpen"
              class="grid max-h-60 gap-2 overflow-y-auto rounded-[24px] border border-slate-200 bg-slate-50 p-3 sm:grid-cols-2 xl:grid-cols-3"
            >
              <button
                v-for="item in availableHeadcountFilters"
                :key="item.key"
                type="button"
                class="flex items-center justify-between gap-3 rounded-2xl border px-3 py-3 text-left text-xs font-bold uppercase tracking-[0.08em] transition-all"
                :class="isHeadcountFilterActive(item.key)
                  ? 'border-indigo-200 bg-white text-indigo-700 shadow-sm'
                  : 'border-transparent bg-white/70 text-slate-600 hover:border-slate-200'"
                @click="toggleHeadcountFilter(item.key)"
              >
                <span class="min-w-0 truncate">{{ item.label }} · {{ formatNumber(item.value) }}</span>
                <Check v-if="isHeadcountFilterActive(item.key)" class="h-4 w-4 shrink-0" />
              </button>
            </div>
          </div>
        </div>

        <div class="w-full overflow-hidden pb-2">
          <div class="headcount-scroll-area overflow-x-auto overflow-y-hidden">
            <div class="min-w-full" :style="{ minWidth: `${headcountChartMinWidth}px` }">
              <VueApexCharts
                :key="headcountChartKey"
                type="bar"
                height="340"
                width="100%"
                :options="headcountOptions"
                :series="headcountSeries"
              />
            </div>
          </div>
        </div>

        <p class="text-xs font-medium text-slate-400 md:hidden">
          Vuốt ngang để xem đầy đủ biểu đồ theo phòng ban.
        </p>
      </GlassCard>

      <GlassCard class="rounded-[32px] border border-slate-100 bg-white p-6 sm:p-8">
        <div class="mb-8 flex items-center justify-between">
          <h3 class="flex items-center gap-2 text-xl font-black text-slate-900">
            <TrendingUp class="h-5 w-5 text-emerald-500" /> Biến động nhân sự
          </h3>
        </div>
        <VueApexCharts type="line" height="340" :options="trendOptions" :series="trendSeries" />
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

.headcount-scroll-area {
  overscroll-behavior-x: contain;
  -webkit-overflow-scrolling: touch;
  touch-action: pan-x;
}
</style>
