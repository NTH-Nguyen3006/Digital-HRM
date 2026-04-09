<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Building2, Filter, GitBranch, Loader2, Network, Search, Users } from 'lucide-vue-next'
import { OrgChart as D3OrgChart } from 'd3-org-chart'

import { getOrgUnitTree } from '@/api/admin/orgUnit'
import { getHeadcountDashboard } from '@/api/admin/report'
import { useToast } from '@/composables/useToast'
import { unwrapData } from '@/utils/api'
import { formatNumber } from '@/utils/format'

const router = useRouter()
const toast = useToast()

const tab = ref('tree')
const loading = ref(true)
const searchQuery = ref('')
const chartContainer = ref(null)
const chartInstance = ref(null)
const organizationTree = ref([])
const orgUnitCounts = ref({})

const chartNodeLookup = computed(() =>
  chartNodes.value.reduce((lookup, item) => {
    lookup[item.id] = item
    return lookup
  }, {}),
)

const companyHeadcount = computed(() =>
  organizationTree.value.reduce((sum, node) => sum + countTotalEmployees(node), 0),
)

function countDirectEmployees(node) {
  return Number(orgUnitCounts.value[node.orgUnitCode] || 0)
}

function countTotalEmployees(node) {
  return countDirectEmployees(node) + (node.children || []).reduce((sum, child) => sum + countTotalEmployees(child), 0)
}

function flattenTree(nodes, parentId = 'company-root') {
  return nodes.flatMap((node) => {
    const chartNode = {
      id: `unit-${node.orgUnitId}`,
      parentId,
      orgUnitId: node.orgUnitId,
      orgUnitCode: node.orgUnitCode,
      name: node.orgUnitName,
      orgUnitType: node.orgUnitType,
      managerName: node.managerEmployeeName || 'Chưa gán quản lý',
      directEmployeeCount: countDirectEmployees(node),
      totalEmployeeCount: countTotalEmployees(node),
      childCount: Array.isArray(node.children) ? node.children.length : 0,
      status: node.status,
    }

    return [
      chartNode,
      ...flattenTree(node.children || [], chartNode.id),
    ]
  })
}

const chartNodes = computed(() => [
  {
    id: 'company-root',
    parentId: null,
    name: 'Digital HRM',
    managerName: 'Sơ đồ toàn công ty',
    orgUnitType: 'COMPANY',
    directEmployeeCount: 0,
    totalEmployeeCount: companyHeadcount.value,
    childCount: organizationTree.value.length,
    status: 'ACTIVE',
  },
  ...flattenTree(organizationTree.value),
])

const listItems = computed(() =>
  chartNodes.value
    .filter((item) => item.orgUnitId)
    .sort((a, b) => {
      if (b.totalEmployeeCount !== a.totalEmployeeCount) return b.totalEmployeeCount - a.totalEmployeeCount
      return a.name.localeCompare(b.name, 'vi')
    }),
)

const filteredListItems = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return listItems.value

  return listItems.value.filter((item) =>
    [item.name, item.orgUnitCode, item.managerName, item.orgUnitType]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(keyword)),
  )
})

const treeSummary = computed(() => ({
  totalUnits: listItems.value.length,
  totalEmployees: companyHeadcount.value,
  largestUnitHeadcount: listItems.value.reduce((max, item) => Math.max(max, item.totalEmployeeCount || 0), 0),
  activeBranches: listItems.value.filter((item) => item.status === 'ACTIVE').length,
}))

function getTypeLabel(type) {
  const labels = {
    COMPANY: 'Toàn công ty',
    DIVISION: 'Khối',
    DEPARTMENT: 'Phòng ban',
    TEAM: 'Nhóm',
    BRANCH: 'Chi nhánh',
    UNIT: 'Đơn vị',
  }
  return labels[type] || type || 'Đơn vị'
}

function navigateToEmployees(node) {
  if (!node?.orgUnitId) return

  router.push({
    path: '/employees',
    query: {
      orgUnitId: String(node.orgUnitId),
      orgUnitName: node.name,
      orgUnitCode: node.orgUnitCode,
    },
  })
}

function buildNodeMarkup(node) {
  const isRoot = node.id === 'company-root'
  const tone = isRoot
    ? {
        bg: 'linear-gradient(135deg,#0f172a,#312e81 52%,#4f46e5)',
        border: 'rgba(99,102,241,0.45)',
        text: '#ffffff',
        muted: 'rgba(224,231,255,0.82)',
        badgeBg: 'rgba(255,255,255,0.14)',
        badgeColor: '#e0e7ff',
      }
    : node.status === 'ACTIVE'
      ? {
          bg: 'linear-gradient(180deg,#ffffff,#f8fafc)',
          border: 'rgba(148,163,184,0.26)',
          text: '#0f172a',
          muted: '#64748b',
          badgeBg: 'rgba(79,70,229,0.08)',
          badgeColor: '#4338ca',
        }
      : {
          bg: 'linear-gradient(180deg,#ffffff,#f8fafc)',
          border: 'rgba(244,63,94,0.24)',
          text: '#334155',
          muted: '#94a3b8',
          badgeBg: 'rgba(244,63,94,0.08)',
          badgeColor: '#be123c',
        }

  return `
    <div class="org-chart-card" style="background:${tone.bg};border-color:${tone.border};color:${tone.text};">
      <div class="org-chart-card__eyebrow" style="color:${tone.muted};">${getTypeLabel(node.orgUnitType)}</div>
      <div class="org-chart-card__title">${node.name}</div>
      <div class="org-chart-card__meta" style="color:${tone.muted};">${node.managerName}</div>
      <div class="org-chart-card__stats">
        <span class="org-chart-card__badge" style="background:${tone.badgeBg};color:${tone.badgeColor};">
          ${formatNumber(node.totalEmployeeCount || 0)} nhân sự
        </span>
        <span class="org-chart-card__meta" style="color:${tone.muted};">
          ${node.childCount ? `${node.childCount} đơn vị con` : 'Bấm để mở danh sách'}
        </span>
      </div>
    </div>
  `
}

async function renderChart() {
  if (!chartContainer.value || !chartNodes.value.length || tab.value !== 'tree') return

  await nextTick()

  if (!chartInstance.value) {
    chartInstance.value = new D3OrgChart()
  }

  chartContainer.value.innerHTML = ''

  chartInstance.value
    .container(chartContainer.value)
    .data(chartNodes.value)
    .nodeWidth(() => 300)
    .nodeHeight(() => 160)
    .childrenMargin(() => 58)
    .siblingsMargin(() => 28)
    .compact(false)
    .initialZoom(0.8)
    .nodeContent((entry) => buildNodeMarkup(entry.data))
    .onNodeClick((nodeId) => {
      navigateToEmployees(chartNodeLookup.value[nodeId])
    })
    .render()

  chartInstance.value.collapseAll()
  chartInstance.value.setExpanded('company-root', true)
  chartInstance.value.render()
}

async function fetchOrganization() {
  loading.value = true
  try {
    const [treeResponse, dashboardResponse] = await Promise.all([
      getOrgUnitTree(),
      getHeadcountDashboard(),
    ])

    organizationTree.value = unwrapData(treeResponse) || []
    const dashboard = unwrapData(dashboardResponse) || {}
    orgUnitCounts.value = (dashboard.orgUnitHeadcountBreakdown || []).reduce((lookup, item) => {
      lookup[item.key] = Number(item.value || 0)
      return lookup
    }, {})
  } catch (error) {
    console.error('Failed to load organization chart:', error)
    organizationTree.value = []
    orgUnitCounts.value = {}
    toast.error('Không thể tải cơ cấu tổ chức')
  } finally {
    loading.value = false
    renderChart()
  }
}

watch(chartNodes, renderChart)
watch(tab, (value) => {
  if (value === 'tree') renderChart()
})

onMounted(fetchOrganization)

onBeforeUnmount(() => {
  if (chartContainer.value) {
    chartContainer.value.innerHTML = ''
  }
  chartInstance.value = null
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
      <div>
        <h2 class="text-3xl font-black tracking-tight text-slate-900 sm:text-4xl">Cơ cấu tổ chức</h2>
        <p class="mt-2 max-w-3xl text-sm font-medium text-slate-500 sm:text-base">
          Sơ đồ và danh mục đang lấy dữ liệu thật từ hệ thống. Bấm vào một chi nhánh hoặc phòng ban để mở hồ sơ nhân sự đã lọc sẵn theo đơn vị.
        </p>
      </div>

      <div class="grid gap-3 sm:grid-cols-3">
        <div class="rounded-[28px] border border-slate-200 bg-white px-5 py-4 shadow-sm">
          <div class="flex items-center gap-3">
            <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
              <Network class="h-5 w-5" />
            </div>
            <div>
              <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Đơn vị</p>
              <p class="mt-1 text-xl font-black text-slate-900">{{ formatNumber(treeSummary.totalUnits) }}</p>
            </div>
          </div>
        </div>

        <div class="rounded-[28px] border border-slate-200 bg-white px-5 py-4 shadow-sm">
          <div class="flex items-center gap-3">
            <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600">
              <GitBranch class="h-5 w-5" />
            </div>
            <div>
              <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Đang hoạt động</p>
              <p class="mt-1 text-xl font-black text-slate-900">{{ formatNumber(treeSummary.activeBranches) }}</p>
            </div>
          </div>
        </div>

        <div class="rounded-[28px] border border-slate-200 bg-white px-5 py-4 shadow-sm">
          <div class="flex items-center gap-3">
            <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-amber-50 text-amber-600">
              <Users class="h-5 w-5" />
            </div>
            <div>
              <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Headcount lớn nhất</p>
              <p class="mt-1 text-xl font-black text-slate-900">{{ formatNumber(treeSummary.largestUnitHeadcount) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
      <div class="flex flex-wrap items-center gap-2 rounded-2xl bg-slate-100 p-1">
        <button
          type="button"
          class="rounded-2xl px-4 py-2 text-sm font-black transition-all"
          :class="tab === 'tree' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
          @click="tab = 'tree'"
        >
          Sơ đồ tổ chức
        </button>
        <button
          type="button"
          class="rounded-2xl px-4 py-2 text-sm font-black transition-all"
          :class="tab === 'list' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
          @click="tab = 'list'"
        >
          Danh mục đơn vị
        </button>
      </div>

      <div class="flex flex-col gap-3 sm:flex-row">
        <div class="relative">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm tên đơn vị, mã hoặc quản lý..."
            class="w-full rounded-2xl border border-slate-200 bg-white py-3 pl-10 pr-4 text-sm font-medium outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10 sm:w-80"
          >
        </div>

        <div class="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-500">
          <Filter class="h-4 w-4 text-slate-400" />
          {{ formatNumber(filteredListItems.length) }} đơn vị
        </div>
      </div>
    </div>

    <section class="rounded-[32px] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="grid gap-4 p-5 sm:p-6 xl:grid-cols-[minmax(0,1.5fr)_360px]">
        <div class="h-[520px] animate-pulse rounded-[28px] bg-slate-100" />
        <div class="grid gap-4">
          <div v-for="item in 5" :key="item" class="h-24 animate-pulse rounded-[24px] bg-slate-100" />
        </div>
      </div>

      <div v-else-if="tab === 'tree'" class="grid gap-5 p-4 sm:p-6 xl:grid-cols-[minmax(0,1.55fr)_360px]">
        <div class="overflow-hidden rounded-[28px] border border-slate-200 bg-[linear-gradient(180deg,#eef2ff_0%,#ffffff_34%)]">
          <div class="flex items-center justify-between border-b border-slate-200 px-5 py-4">
            <div>
              <h3 class="text-lg font-black text-slate-900">Sơ đồ trực quan</h3>
              <p class="mt-1 text-sm font-medium text-slate-500">Bấm trực tiếp vào node để mở danh sách nhân sự theo đơn vị.</p>
            </div>
            <div class="inline-flex items-center gap-2 rounded-full bg-white px-3 py-2 text-xs font-black uppercase tracking-[0.18em] text-indigo-600 shadow-sm">
              <span class="flex h-2 w-2 rounded-full bg-indigo-500"></span>
              Live data
            </div>
          </div>

          <div class="org-chart-shell">
            <div ref="chartContainer" class="min-h-[520px] min-w-[980px]"></div>
          </div>
        </div>

        <div class="space-y-4">
          <article class="rounded-[28px] border border-slate-200 bg-slate-50 p-5">
            <h3 class="text-lg font-black text-slate-900">Cách dùng nhanh</h3>
            <div class="mt-4 space-y-3 text-sm text-slate-600">
              <div class="rounded-2xl bg-white px-4 py-3">
                Bấm vào <span class="font-black text-slate-900">chi nhánh, khối hoặc phòng ban</span> để sang hồ sơ nhân sự với bộ lọc tương ứng.
              </div>
              <div class="rounded-2xl bg-white px-4 py-3">
                Tab <span class="font-black text-slate-900">Danh mục đơn vị</span> giúp rà nhanh quản lý, headcount và tình trạng hoạt động.
              </div>
              <div class="rounded-2xl bg-white px-4 py-3">
                Mobile vẫn giữ được khả năng xem danh sách và kéo ngang khu vực sơ đồ khi cần.
              </div>
            </div>
          </article>

          <article class="rounded-[28px] border border-slate-200 bg-white p-5">
            <div class="mb-4 flex items-center justify-between gap-3">
              <div>
                <h3 class="text-lg font-black text-slate-900">Đơn vị nổi bật</h3>
                <p class="mt-1 text-sm font-medium text-slate-500">Top headcount hiện tại.</p>
              </div>
              <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-black uppercase tracking-[0.18em] text-slate-500">
                {{ Math.min(filteredListItems.length, 5) }} đơn vị
              </span>
            </div>

            <div class="space-y-3">
              <button
                v-for="item in filteredListItems.slice(0, 5)"
                :key="item.id"
                type="button"
                class="flex w-full items-start justify-between gap-4 rounded-[24px] border border-slate-200 bg-slate-50 px-4 py-4 text-left transition-all hover:border-indigo-200 hover:bg-indigo-50/40"
                @click="navigateToEmployees(item)"
              >
                <div class="min-w-0">
                  <p class="text-base font-black text-slate-900">{{ item.name }}</p>
                  <p class="mt-1 text-sm font-medium text-slate-500">{{ item.orgUnitCode }} · {{ item.managerName }}</p>
                </div>
                <span class="rounded-full bg-white px-3 py-1 text-xs font-black text-indigo-700 shadow-sm">
                  {{ formatNumber(item.totalEmployeeCount) }}
                </span>
              </button>
            </div>
          </article>
        </div>
      </div>

      <div v-else class="overflow-hidden">
        <div class="hidden border-b border-slate-200 bg-slate-50/90 px-6 py-4 md:grid md:grid-cols-[minmax(0,2fr)_130px_150px_130px_140px] md:gap-4">
          <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Đơn vị</div>
          <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Loại</div>
          <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Quản lý</div>
          <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Nhân sự</div>
          <div class="text-right text-xs font-black uppercase tracking-[0.18em] text-slate-400">Thao tác</div>
        </div>

        <div v-if="filteredListItems.length" class="divide-y divide-slate-100">
          <div
            v-for="item in filteredListItems"
            :key="item.id"
            class="grid gap-4 px-4 py-4 transition-colors hover:bg-slate-50 md:grid-cols-[minmax(0,2fr)_130px_150px_130px_140px] md:px-6"
          >
            <div class="min-w-0">
              <div class="flex items-start gap-3">
                <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
                  <Building2 class="h-5 w-5" />
                </div>
                <div class="min-w-0">
                  <p class="truncate text-base font-black text-slate-900">{{ item.name }}</p>
                  <p class="mt-1 text-sm font-medium text-slate-500">{{ item.orgUnitCode }}</p>
                </div>
              </div>
            </div>

            <div class="flex items-center text-sm font-bold text-slate-600">
              {{ getTypeLabel(item.orgUnitType) }}
            </div>

            <div class="flex items-center text-sm font-medium text-slate-600">
              {{ item.managerName }}
            </div>

            <div class="flex items-center">
              <span class="rounded-full bg-indigo-50 px-3 py-1 text-sm font-black text-indigo-700">
                {{ formatNumber(item.totalEmployeeCount) }}
              </span>
            </div>

            <div class="flex items-center justify-start md:justify-end">
              <button
                type="button"
                class="rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm font-bold text-slate-700 transition hover:border-indigo-200 hover:text-indigo-600"
                @click="navigateToEmployees(item)"
              >
                Xem nhân sự
              </button>
            </div>
          </div>
        </div>

        <div v-else class="flex min-h-[280px] flex-col items-center justify-center gap-4 px-6 text-center">
          <div class="flex h-16 w-16 items-center justify-center rounded-[24px] bg-slate-100 text-slate-400">
            <Loader2 class="h-7 w-7" />
          </div>
          <div>
            <h3 class="text-lg font-black text-slate-900">Không tìm thấy đơn vị phù hợp</h3>
            <p class="mt-2 text-sm font-medium text-slate-500">Thử đổi từ khóa tìm kiếm để xem lại danh mục cơ cấu tổ chức.</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.org-chart-shell {
  overflow-x: auto;
  overflow-y: hidden;
  padding: 1.5rem;
}

:deep(.org-chart-card) {
  display: flex;
  min-height: 146px;
  width: 100%;
  flex-direction: column;
  justify-content: space-between;
  border: 1px solid;
  border-radius: 24px;
  padding: 16px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

:deep(.org-chart-card:hover) {
  transform: translateY(-3px);
  box-shadow: 0 24px 56px rgba(79, 70, 229, 0.16);
}

:deep(.org-chart-card__eyebrow) {
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

:deep(.org-chart-card__title) {
  margin-top: 0.55rem;
  font-size: 18px;
  font-weight: 900;
  line-height: 1.2;
}

:deep(.org-chart-card__meta) {
  font-size: 12px;
  font-weight: 700;
}

:deep(.org-chart-card__stats) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-top: 1rem;
}

:deep(.org-chart-card__badge) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 0.45rem 0.85rem;
  font-size: 12px;
  font-weight: 900;
}
</style>
