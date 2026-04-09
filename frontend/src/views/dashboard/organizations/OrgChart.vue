<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Building2, Filter, GitBranch, Loader2, Minus, Network, Plus, RotateCcw, Search, Users } from 'lucide-vue-next'

import OrgTreeBranch from '@/components/hrm/OrgTreeBranch.vue'
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
const organizationTree = ref([])
const orgUnitCounts = ref({})
const chartZoom = ref(1)
const chartShellRef = ref(null)
const chartDragging = ref(false)
const suppressChartClick = ref(false)

const MIN_CHART_ZOOM = 0.7
const MAX_CHART_ZOOM = 1.7
const CHART_ZOOM_STEP = 0.15

let dragPointerId = null
let dragStartX = 0
let dragStartY = 0
let dragStartScrollLeft = 0
let dragStartScrollTop = 0
let dragMoved = false
let suppressChartClickTimeoutId = null

const chartZoomPercent = computed(() => `${Math.round(chartZoom.value * 100)}%`)
const chartZoomStyle = computed(() => ({
  zoom: String(chartZoom.value),
  '--org-chart-scale': String(chartZoom.value),
}))

function clampChartZoom(value) {
  return Math.min(MAX_CHART_ZOOM, Math.max(MIN_CHART_ZOOM, Number(value)))
}

function zoomInChart() {
  chartZoom.value = clampChartZoom(chartZoom.value + CHART_ZOOM_STEP)
}

function zoomOutChart() {
  chartZoom.value = clampChartZoom(chartZoom.value - CHART_ZOOM_STEP)
}

function resetChartZoom() {
  chartZoom.value = 1
}

function handleChartWheel(event) {
  if (!event.ctrlKey) return

  event.preventDefault()

  const direction = event.deltaY > 0 ? -1 : 1
  chartZoom.value = clampChartZoom(chartZoom.value + direction * CHART_ZOOM_STEP)
}

function clearSuppressChartClickTimer() {
  if (suppressChartClickTimeoutId) {
    window.clearTimeout(suppressChartClickTimeoutId)
    suppressChartClickTimeoutId = null
  }
}

function startChartPan(event) {
  if (event.pointerType === 'touch') return
  if (event.pointerType === 'mouse' && ![0, 1].includes(event.button)) return

  const shell = chartShellRef.value
  if (!shell) return

  chartDragging.value = true
  dragPointerId = event.pointerId
  dragStartX = event.clientX
  dragStartY = event.clientY
  dragStartScrollLeft = shell.scrollLeft
  dragStartScrollTop = shell.scrollTop
  dragMoved = false

  clearSuppressChartClickTimer()
  suppressChartClick.value = false

  shell.setPointerCapture?.(event.pointerId)
  event.preventDefault()
}

function moveChartPan(event) {
  if (!chartDragging.value || dragPointerId !== event.pointerId) return

  const shell = chartShellRef.value
  if (!shell) return

  const deltaX = event.clientX - dragStartX
  const deltaY = event.clientY - dragStartY

  if (Math.abs(deltaX) > 3 || Math.abs(deltaY) > 3) {
    dragMoved = true
  }

  shell.scrollLeft = dragStartScrollLeft - deltaX
  shell.scrollTop = dragStartScrollTop - deltaY
}

function stopChartPan(event) {
  if (!chartDragging.value) return
  if (event?.pointerId !== undefined && dragPointerId !== event.pointerId) return

  const shell = chartShellRef.value
  if (shell && dragPointerId !== null) {
    shell.releasePointerCapture?.(dragPointerId)
  }

  chartDragging.value = false
  dragPointerId = null

  if (dragMoved) {
    suppressChartClick.value = true
    clearSuppressChartClickTimer()
    suppressChartClickTimeoutId = window.setTimeout(() => {
      suppressChartClick.value = false
      suppressChartClickTimeoutId = null
    }, 160)
  }
}

function handleChartClickCapture(event) {
  if (!suppressChartClick.value) return
  event.preventDefault()
  event.stopPropagation()
  suppressChartClick.value = false
  clearSuppressChartClickTimer()
}

function countDirectEmployees(node) {
  return Number(orgUnitCounts.value[node.orgUnitCode] || 0)
}

function countTotalEmployees(node) {
  return countDirectEmployees(node) + (node.children || []).reduce((sum, child) => sum + countTotalEmployees(child), 0)
}

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

function createTreeNode(node, parentId = 'company-root') {
  return {
    id: `unit-${node.orgUnitId}`,
    parentId,
    orgUnitId: node.orgUnitId,
    orgUnitCode: node.orgUnitCode,
    name: node.orgUnitName,
    orgUnitType: node.orgUnitType,
    typeLabel: getTypeLabel(node.orgUnitType),
    managerName: node.managerEmployeeName || 'Chưa gán quản lý',
    totalEmployeeCount: countTotalEmployees(node),
    childCount: Array.isArray(node.children) ? node.children.length : 0,
    status: node.status,
    children: (node.children || []).map((child) => createTreeNode(child, `unit-${node.orgUnitId}`)),
  }
}

function flattenTree(nodes, parentId = 'company-root') {
  return nodes.flatMap((node) => {
    const item = {
      id: `unit-${node.orgUnitId}`,
      parentId,
      orgUnitId: node.orgUnitId,
      orgUnitCode: node.orgUnitCode,
      name: node.orgUnitName,
      orgUnitType: node.orgUnitType,
      managerName: node.managerEmployeeName || 'Chưa gán quản lý',
      totalEmployeeCount: countTotalEmployees(node),
      childCount: Array.isArray(node.children) ? node.children.length : 0,
      status: node.status,
    }

    return [item, ...flattenTree(node.children || [], item.id)]
  })
}

const chartNodes = computed(() => [
  {
    id: 'company-root',
    parentId: null,
    name: 'Digital HRM',
    managerName: 'Sơ đồ toàn công ty',
    orgUnitType: 'COMPANY',
    totalEmployeeCount: companyHeadcount.value,
    childCount: organizationTree.value.length,
    status: 'ACTIVE',
  },
  ...flattenTree(organizationTree.value),
])

const companyHeadcount = computed(() =>
  organizationTree.value.reduce((sum, node) => sum + countTotalEmployees(node), 0),
)

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

const summaryCards = computed(() => [
  {
    key: 'units',
    label: 'Đơn vị',
    value: formatNumber(treeSummary.value.totalUnits),
    hint: 'Tổng số đơn vị trong cây tổ chức',
    icon: Network,
    iconClass: 'bg-indigo-50 text-indigo-600',
    accentClass: 'from-indigo-500/12 via-indigo-500/5 to-transparent',
  },
  {
    key: 'active',
    label: 'Đang hoạt động',
    value: formatNumber(treeSummary.value.activeBranches),
    hint: 'Số đơn vị đang ở trạng thái active',
    icon: GitBranch,
    iconClass: 'bg-emerald-50 text-emerald-600',
    accentClass: 'from-emerald-500/12 via-emerald-500/5 to-transparent',
  },
  {
    key: 'headcount',
    label: 'Headcount lớn nhất',
    value: formatNumber(treeSummary.value.largestUnitHeadcount),
    hint: 'Quy mô nhân sự lớn nhất theo đơn vị',
    icon: Users,
    iconClass: 'bg-amber-50 text-amber-600',
    accentClass: 'from-amber-500/12 via-amber-500/5 to-transparent',
  },
])

function matchesKeyword(node, keyword) {
  return [node.orgUnitName, node.orgUnitCode, node.managerEmployeeName, node.orgUnitType]
    .filter(Boolean)
    .some((value) => value.toLowerCase().includes(keyword))
}

function filterTree(nodes, keyword) {
  return nodes.reduce((result, node) => {
    const filteredChildren = filterTree(node.children || [], keyword)
    if (matchesKeyword(node, keyword) || filteredChildren.length) {
      result.push({
        ...node,
        children: filteredChildren,
      })
    }
    return result
  }, [])
}

const visibleOrganizationTree = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return organizationTree.value
  return filterTree(organizationTree.value, keyword)
})

const treeRoot = computed(() => ({
  id: 'company-root',
  name: 'Digital HRM',
  managerName: 'Sơ đồ toàn công ty',
  typeLabel: 'Toàn công ty',
  totalEmployeeCount: companyHeadcount.value,
  childCount: visibleOrganizationTree.value.length,
  status: 'ACTIVE',
  isRoot: true,
  children: visibleOrganizationTree.value.map((node) => createTreeNode(node)),
}))

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
  }
}

onMounted(fetchOrganization)

onBeforeUnmount(() => {
  clearSuppressChartClickTimer()
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
      <div>
        <h2 class="text-3xl font-black tracking-tight text-slate-900 sm:text-4xl">Cơ cấu tổ chức</h2>
        <p class="mt-2 max-w-3xl text-sm font-medium text-slate-500 sm:text-base">
          Sơ đồ và danh mục đang lấy dữ liệu thật từ hệ thống. Bấm vào một chi nhánh hoặc phòng ban để mở hồ sơ nhân sự
          đã lọc sẵn theo đơn vị.
        </p>
      </div>

      <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        <article v-for="card in summaryCards" :key="card.key"
          class="group relative overflow-hidden rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
          <div class="absolute inset-x-0 top-0 h-24 bg-linear-to-br opacity-90" :class="card.accentClass"></div>
          <div class="relative flex items-start justify-between gap-4">
            <div class="min-w-0">
              <p class="text-xs font-black uppercase tracking-[0.16em] text-slate-400">{{ card.label }}</p>
              <p class="mt-2 text-3xl font-black leading-none text-slate-900">{{ card.value }}</p>
              <p class="mt-3 max-w-[18rem] text-sm font-medium leading-5 text-slate-500">{{ card.hint }}</p>
            </div>

            <div class="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl shadow-sm ring-1 ring-black/5"
              :class="card.iconClass">
              <component :is="card.icon" class="h-5 w-5" />
            </div>
          </div>
        </article>
      </div>
    </div>

    <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
      <div class="flex flex-wrap items-center gap-2 rounded-2xl bg-slate-100 p-1">
        <button type="button" class="rounded-2xl px-4 py-2 text-sm font-black transition-all"
          :class="tab === 'tree' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
          @click="tab = 'tree'">
          Sơ đồ tổ chức
        </button>
        <button type="button" class="rounded-2xl px-4 py-2 text-sm font-black transition-all"
          :class="tab === 'list' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
          @click="tab = 'list'">
          Danh mục đơn vị
        </button>
      </div>

      <div class="flex flex-col gap-3 sm:flex-row">
        <div class="relative">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input v-model="searchQuery" type="text" placeholder="Tìm tên đơn vị, mã hoặc quản lý..."
            class="w-full rounded-2xl border border-slate-200 bg-white py-3 pl-10 pr-4 text-sm font-medium outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10 sm:w-80">
        </div>

        <div
          class="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-500">
          <Filter class="h-4 w-4 text-slate-400" />
          {{ formatNumber(filteredListItems.length) }} đơn vị
        </div>
      </div>
    </div>

    <section class="rounded-4xl border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="grid gap-4 p-5 sm:p-6 xl:grid-cols-[minmax(0,1.5fr)_360px]">
        <div class="h-130 animate-pulse rounded-[28px] bg-slate-100" />
        <div class="grid gap-4">
          <div v-for="item in 5" :key="item" class="h-24 animate-pulse rounded-[24px] bg-slate-100" />
        </div>
      </div>

      <div v-else-if="tab === 'tree'" class="grid gap-5 p-4 sm:p-6 xl:grid-cols-[minmax(0,1.55fr)_360px]">
        <div
          class="overflow-hidden rounded-[28px] border border-slate-200 bg-[linear-gradient(180deg,#eef2ff_0%,#ffffff_34%)]">
          <div class="flex items-center justify-between border-b border-slate-200 px-5 py-4">
            <div>
              <h3 class="text-lg font-black text-slate-900">Sơ đồ trực quan</h3>
              <p class="mt-1 text-sm font-medium text-slate-500">Bấm trực tiếp vào node để mở danh sách nhân sự theo đơn
                vị.</p>
            </div>

            <div class="flex flex-wrap items-center justify-end gap-2">
              <div class="inline-flex items-center gap-1 rounded-full border border-slate-200 bg-white p-1 shadow-sm">
                <button type="button"
                  class="flex h-9 w-9 items-center justify-center rounded-full text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 disabled:cursor-not-allowed disabled:opacity-40"
                  :disabled="chartZoom <= MIN_CHART_ZOOM" @click="zoomOutChart">
                  <Minus class="h-4 w-4" />
                </button>
                <button type="button"
                  class="min-w-18 rounded-full px-3 py-2 text-xs font-black uppercase tracking-[0.16em] text-slate-600 transition hover:bg-slate-100 hover:text-slate-900"
                  @click="resetChartZoom">
                  {{ chartZoomPercent }}
                </button>
                <button type="button"
                  class="flex h-9 w-9 items-center justify-center rounded-full text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 disabled:cursor-not-allowed disabled:opacity-40"
                  :disabled="chartZoom >= MAX_CHART_ZOOM" @click="zoomInChart">
                  <Plus class="h-4 w-4" />
                </button>
                <button type="button"
                  class="flex h-9 w-9 items-center justify-center rounded-full text-slate-500 transition hover:bg-slate-100 hover:text-slate-900"
                  @click="resetChartZoom">
                  <RotateCcw class="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>

          <div ref="chartShellRef" class="org-chart-shell" :class="{ 'org-chart-shell--dragging': chartDragging }"
            @wheel="handleChartWheel" @pointerdown="startChartPan" @pointermove="moveChartPan" @pointerup="stopChartPan"
            @pointercancel="stopChartPan" @lostpointercapture="stopChartPan" @click.capture="handleChartClickCapture">
            <div v-if="treeRoot.children.length" class="org-tree-stage-wrap">
              <div class="org-tree-stage" :style="chartZoomStyle">
                <OrgTreeBranch :node="treeRoot" @select="navigateToEmployees" />
              </div>
            </div>
            <div v-else class="flex min-h-130 flex-col items-center justify-center gap-4 px-6 text-center">
              <div class="flex h-16 w-16 items-center justify-center rounded-[24px] bg-slate-100 text-slate-400">
                <Loader2 class="h-7 w-7" />
              </div>
              <div>
                <h3 class="text-lg font-black text-slate-900">Không có nhánh cây phù hợp</h3>
                <p class="mt-2 text-sm font-medium text-slate-500">Thử đổi từ khóa tìm kiếm để xem lại cơ cấu tổ chức.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="space-y-4">
          <article class="rounded-[28px] border border-slate-200 bg-white p-5">
            <div class="mb-4 flex items-center justify-between gap-3">
              <div>
                <h3 class="text-lg font-black text-slate-900">Đơn vị nổi bật</h3>
                <p class="mt-1 text-sm font-medium text-slate-500">Top headcount hiện tại.</p>
              </div>
              <span
                class="rounded-full bg-slate-100 px-3 py-1 text-xs font-black uppercase tracking-[0.18em] text-slate-500">
                {{ Math.min(filteredListItems.length, 5) }} đơn vị
              </span>
            </div>

            <div class="space-y-3">
              <button v-for="item in filteredListItems.slice(0, 5)" :key="item.id" type="button"
                class="flex w-full items-start justify-between gap-4 rounded-[24px] border border-slate-200 bg-slate-50 px-4 py-4 text-left transition-all hover:border-indigo-200 hover:bg-indigo-50/40"
                @click="navigateToEmployees(item)">
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
        <div
          class="hidden border-b border-slate-200 bg-slate-50/90 px-6 py-4 md:grid md:grid-cols-[minmax(0,2fr)_130px_150px_130px_140px] md:gap-4">
          <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Đơn vị</div>
          <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Loại</div>
          <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Quản lý</div>
          <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Nhân sự</div>
          <div class="text-right text-xs font-black uppercase tracking-[0.18em] text-slate-400">Thao tác</div>
        </div>

        <div v-if="filteredListItems.length" class="divide-y divide-slate-100">
          <div v-for="item in filteredListItems" :key="item.id"
            class="grid gap-4 px-4 py-4 transition-colors hover:bg-slate-50 md:grid-cols-[minmax(0,2fr)_130px_150px_130px_140px] md:px-6">
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
              <button type="button"
                class="rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm font-bold text-slate-700 transition hover:border-indigo-200 hover:text-indigo-600"
                @click="navigateToEmployees(item)">
                Xem nhân sự
              </button>
            </div>
          </div>
        </div>

        <div v-else class="flex min-h-70 flex-col items-center justify-center gap-4 px-6 text-center">
          <div class="flex h-16 w-16 items-center justify-center rounded-[24px] bg-slate-100 text-slate-400">
            <Loader2 class="h-7 w-7" />
          </div>
          <div>
            <h3 class="text-lg font-black text-slate-900">Không tìm thấy đơn vị phù hợp</h3>
            <p class="mt-2 text-sm font-medium text-slate-500">Thử đổi từ khóa tìm kiếm để xem lại danh mục cơ cấu tổ
              chức.</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.org-chart-shell {
  overflow-x: auto;
  overflow-y: auto;
  padding: 1.5rem;
  height: clamp(360px, 68vh, 760px);
  max-height: calc(100dvh - 220px);
  cursor: grab;
  overscroll-behavior: contain;
  user-select: none;
}

.org-chart-shell--dragging {
  cursor: grabbing;
}

.org-tree-stage-wrap {
  display: inline-flex;
  min-width: max-content;
  justify-content: center;
  padding: 0.5rem 1rem 2rem;
}

.org-tree-stage {
  display: inline-flex;
  min-width: max-content;
  justify-content: center;
  transform: scale(var(--org-chart-scale, 1));
  transform-origin: top center;
}

@supports (zoom: 1) {
  .org-tree-stage {
    transform: none;
  }
}

@media (max-width: 1279px) {
  .org-chart-shell {
    max-height: min(70vh, 720px);
  }
}

@media (max-width: 767px) {
  .org-chart-shell {
    height: clamp(320px, 62vh, 560px);
    max-height: calc(100dvh - 190px);
    padding: 1rem;
  }

  .org-tree-stage-wrap {
    padding: 0.25rem 0.5rem 1.5rem;
  }
}
</style>
