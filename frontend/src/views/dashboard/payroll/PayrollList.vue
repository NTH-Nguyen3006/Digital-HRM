<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { Banknote, CheckCircle2, Clock3, Download, Landmark, PlayCircle, Search, Send, Wallet } from 'lucide-vue-next'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import InsightCard from '@/components/hrm/InsightCard.vue'
import SurfacePanel from '@/components/hrm/SurfacePanel.vue'
import {
  approvePayrollPeriod,
  exportBankTransferReport,
  exportPitReport,
  generatePayrollDraft,
  getPayrollItemDetail,
  getPayrollItems,
  getPayrollPeriods,
  publishPayrollPeriod,
} from '@/api/admin/payroll'
import { useToast } from '@/composables/useToast'
import { useUiStore } from '@/stores/ui'
import { downloadBlob, unwrapData, unwrapPage } from '@/utils/api'
import { formatCurrency, formatDateTime, formatMonthYear, formatNumber } from '@/utils/format'

const toast = useToast()
const ui = useUiStore()

const loading = ref(false)
const detailLoading = ref(false)
const actionLoading = ref('')
const searchQuery = ref('')
const periods = ref([])
const items = ref([])
const selectedPeriodId = ref(null)
const selectedItem = ref(null)
const detailPanelScrollRef = ref(null)

const selectedPeriod = computed(() =>
  periods.value.find((item) => item.payrollPeriodId === selectedPeriodId.value) || null,
)

const filteredItems = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return items.value

  return items.value.filter((item) =>
    [item.employeeName, item.employeeCode, item.orgUnitName]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(keyword)),
  )
})

const stats = computed(() => [
  {
    title: 'Nhân sự trong kỳ',
    value: selectedPeriod.value?.totalEmployeeCount ?? items.value.length,
    subtitle: 'Đã đưa vào batch lương',
    icon: Banknote,
    tone: 'indigo',
  },
  {
    title: 'Manager xác nhận',
    value: selectedPeriod.value?.managerConfirmedCount ?? items.value.filter((item) => item.managerConfirmedAt).length,
    subtitle: 'Tiến độ xác nhận trước HR',
    icon: CheckCircle2,
    tone: 'emerald',
  },
  {
    title: 'Tổng gross',
    value: formatCurrency(selectedPeriod.value?.totalGrossAmount ?? items.value.reduce((sum, item) => sum + Number(item.grossIncome || 0), 0)),
    subtitle: 'Chi phí lương trước khấu trừ',
    icon: Landmark,
    tone: 'amber',
  },
  {
    title: 'Tổng net',
    value: formatCurrency(selectedPeriod.value?.totalNetAmount ?? items.value.reduce((sum, item) => sum + Number(item.netPay || 0), 0)),
    subtitle: 'Thực chi cho kỳ đang chọn',
    icon: Wallet,
    tone: 'rose',
  },
])

const selectedItemLines = computed(() => selectedItem.value?.lines || [])

const managerConfirmRequiredCount = computed(() =>
  items.value.filter((item) => item.managerConfirmationRequired).length,
)

const managerConfirmedCount = computed(() =>
  items.value.filter((item) => item.itemStatus === 'MANAGER_CONFIRMED' || item.managerConfirmedAt).length,
)

const payrollActionState = computed(() => {
  const period = selectedPeriod.value
  const status = period?.periodStatus || ''
  const hasItems = items.value.length > 0
  const managerPending = items.value.some((item) => item.managerConfirmationRequired && item.itemStatus !== 'MANAGER_CONFIRMED')

  return {
    canGenerate: Boolean(period) && !['APPROVED', 'PUBLISHED'].includes(status),
    canApprove: Boolean(period) && hasItems && ['DRAFT', 'TEAM_REVIEW'].includes(status) && !managerPending,
    canPublish: Boolean(period) && hasItems && status === 'APPROVED',
    canExport: Boolean(period) && ['APPROVED', 'PUBLISHED'].includes(status),
    managerPending,
  }
})

const workflowHint = computed(() => {
  if (!selectedPeriod.value) return 'Chọn một kỳ lương để thao tác.'

  if (actionLoading.value) return 'Hệ thống đang xử lý tác vụ kỳ lương.'

  if (!items.value.length) return 'Kỳ lương chưa có dữ liệu. Hãy chạy nháp để tạo bảng lương.'

  if (selectedPeriod.value.periodStatus === 'APPROVED') {
    return 'Kỳ lương đã được duyệt. HR có thể phát hành phiếu lương hoặc xuất báo cáo.'
  }

  if (selectedPeriod.value.periodStatus === 'PUBLISHED') {
    return 'Kỳ lương đã phát hành. Nhân viên đã có thể xem phiếu lương.'
  }

  if (payrollActionState.value.managerPending) {
    return `Còn ${formatNumber(managerConfirmRequiredCount.value - managerConfirmedCount.value)} phiếu cần manager xác nhận trước khi HR duyệt kỳ.`
  }

  return 'Dữ liệu đã sẵn sàng. HR có thể duyệt kỳ lương để chuyển sang bước phát hành.'
})

const selectedItemSummary = computed(() => {
  if (!selectedItem.value) return []

  return [
    { label: 'Lương tháng', value: formatCurrency(selectedItem.value.baseSalaryMonthly) },
    { label: 'Lương prorate', value: formatCurrency(selectedItem.value.baseSalaryProrated) },
    { label: 'Khấu trừ BH', value: formatCurrency(selectedItem.value.employeeInsuranceAmount) },
    { label: 'Thu nhập tính thuế', value: formatCurrency(selectedItem.value.taxableIncome) },
    { label: 'PIT', value: formatCurrency(selectedItem.value.pitAmount) },
    { label: 'Net pay', value: formatCurrency(selectedItem.value.netPay) },
  ]
})

function formatCompactCurrency(value) {
  const amount = Number(value || 0)
  const absoluteAmount = Math.abs(amount)

  if (absoluteAmount >= 1_000_000_000) {
    return `${new Intl.NumberFormat('vi-VN', {
      minimumFractionDigits: absoluteAmount >= 10_000_000_000 ? 0 : 1,
      maximumFractionDigits: 1,
    }).format(amount / 1_000_000_000)} tỷ`
  }

  if (absoluteAmount >= 1_000_000) {
    return `${new Intl.NumberFormat('vi-VN', {
      minimumFractionDigits: absoluteAmount >= 10_000_000 ? 0 : 1,
      maximumFractionDigits: 1,
    }).format(amount / 1_000_000)} triệu`
  }

  return formatCurrency(amount)
}

function getPeriodProgress(period) {
  if (!period?.totalEmployeeCount) return 0
  return Math.min(100, Math.round(((period.managerConfirmedCount || 0) / period.totalEmployeeCount) * 100))
}

async function fetchPeriods() {
  const response = await getPayrollPeriods()
  periods.value = unwrapPage(response).items
  if (!selectedPeriodId.value && periods.value.length) {
    selectedPeriodId.value = periods.value[0].payrollPeriodId
  }
}

async function fetchItems() {
  if (!selectedPeriodId.value) {
    items.value = []
    selectedItem.value = null
    return
  }

  const response = await getPayrollItems(selectedPeriodId.value, {
    keyword: searchQuery.value || undefined,
    size: 200,
  })
  items.value = unwrapPage(response).items

  if (!items.value.length) {
    selectedItem.value = null
    return
  }

  const nextItemId = selectedItem.value?.payrollItemId && items.value.some((item) => item.payrollItemId === selectedItem.value.payrollItemId)
    ? selectedItem.value.payrollItemId
    : items.value[0].payrollItemId

  await openItem(nextItemId)
}

async function fetchAll() {
  loading.value = true
  try {
    await fetchPeriods()
    await fetchItems()
  } catch (error) {
    console.error('Failed to fetch payroll data:', error)
    periods.value = []
    items.value = []
    selectedItem.value = null
    toast.error('Không thể tải dữ liệu tính lương')
  } finally {
    loading.value = false
  }
}

async function openItem(payrollItemId) {
  if (!payrollItemId) return

  if (detailPanelScrollRef.value) {
    detailPanelScrollRef.value.scrollTo({ top: 0, behavior: 'smooth' })
  }

  detailLoading.value = true
  try {
    const response = await getPayrollItemDetail(payrollItemId)
    selectedItem.value = unwrapData(response)
    await nextTick()
    if (detailPanelScrollRef.value) {
      detailPanelScrollRef.value.scrollTo({ top: 0, behavior: 'smooth' })
    }
  } catch (error) {
    console.error('Failed to fetch payroll item detail:', error)
    toast.error('Không thể tải chi tiết phiếu lương')
  } finally {
    detailLoading.value = false
  }
}

async function runPeriodAction(action, handler, payload, successMessage) {
  if (!selectedPeriodId.value) return

  actionLoading.value = action
  try {
    await handler(selectedPeriodId.value, payload)
    toast.success(successMessage)
    await fetchAll()
  } catch (error) {
    console.error(`Payroll action ${action} failed:`, error)
    toast.error(error.response?.data?.message || 'Không thể xử lý tác vụ kỳ lương')
  } finally {
    actionLoading.value = ''
  }
}

function getPeriodStatusLabel(status) {
  const labels = {
    DRAFT: 'Nháp',
    TEAM_REVIEW: 'Chờ team xác nhận',
    APPROVED: 'Đã duyệt',
    PUBLISHED: 'Đã phát hành',
  }
  return labels[status] || status || 'Không xác định'
}

function getItemStatusLabel(status) {
  const labels = {
    DRAFT: 'Nháp',
    MANAGER_CONFIRMED: 'QL đã xác nhận',
    HR_APPROVED: 'HR đã duyệt',
    PUBLISHED: 'Đã phát hành',
  }
  return labels[status] || status || 'Không xác định'
}

function getActionTitle(action) {
  if (!selectedPeriod.value) return 'Chọn một kỳ lương trước'

  if (action === 'generate') {
    if (payrollActionState.value.canGenerate) return 'Tính lại bảng lương nháp cho kỳ đang chọn'
    return 'Không thể chạy nháp khi kỳ lương đã duyệt hoặc đã phát hành'
  }

  if (action === 'approve') {
    if (!items.value.length) return 'Kỳ lương chưa có dữ liệu để duyệt'
    if (payrollActionState.value.managerPending) return 'Cần manager xác nhận hết các phiếu bắt buộc trước khi duyệt'
    if (payrollActionState.value.canApprove) return 'Chốt duyệt toàn bộ phiếu lương trong kỳ'
    return 'Chỉ có thể duyệt khi kỳ đang ở trạng thái nháp hoặc chờ team xác nhận'
  }

  if (action === 'publish') {
    if (payrollActionState.value.canPublish) return 'Phát hành phiếu lương để nhân viên tra cứu'
    return 'Chỉ có thể phát hành sau khi kỳ lương đã được duyệt'
  }

  return ''
}

async function handleGenerateDraft() {
  if (!selectedPeriod.value || !payrollActionState.value.canGenerate) return

  const confirmed = await ui.confirm({
    title: 'Chạy bảng lương nháp',
    message: `Hệ thống sẽ tính lại bảng lương nháp cho kỳ ${formatMonthYear(selectedPeriod.value.periodYear, selectedPeriod.value.periodMonth, selectedPeriod.value.periodCode)}. Các dòng lương nháp hiện tại sẽ được thay thế.`,
    confirmLabel: 'Chạy nháp',
  })
  if (!confirmed) return

  await runPeriodAction(
    'generate',
    generatePayrollDraft,
    { regenerate: true, note: 'Tạo lại bảng lương nháp từ HR workspace.' },
    'Đã tạo lại bảng lương nháp',
  )
}

async function handleApprovePeriod() {
  if (!selectedPeriod.value || !payrollActionState.value.canApprove) return

  const confirmed = await ui.confirm({
    title: 'Duyệt kỳ lương',
    message: 'HR sẽ chốt duyệt toàn bộ phiếu lương trong kỳ này. Sau bước này, dữ liệu được xem là đã chốt nội bộ và sẵn sàng để phát hành.',
    confirmLabel: 'Duyệt kỳ',
  })
  if (!confirmed) return

  await runPeriodAction(
    'approve',
    approvePayrollPeriod,
    { note: 'Phê duyệt kỳ lương từ HR workspace.' },
    'Đã duyệt kỳ lương',
  )
}

async function handlePublishPeriod() {
  if (!selectedPeriod.value || !payrollActionState.value.canPublish) return

  const confirmed = await ui.confirm({
    title: 'Phát hành phiếu lương',
    message: 'Phiếu lương sẽ được phát hành chính thức cho nhân viên. Sau bước này, nhân viên có thể xem dữ liệu lương của kỳ này trên cổng cá nhân.',
    confirmLabel: 'Phát hành',
  })
  if (!confirmed) return

  await runPeriodAction(
    'publish',
    publishPayrollPeriod,
    { note: 'Phát hành phiếu lương từ HR workspace.' },
    'Đã phát hành kỳ lương',
  )
}

async function handleExportBank() {
  if (!selectedPeriodId.value || !payrollActionState.value.canExport) return

  try {
    const blob = await exportBankTransferReport({ payrollPeriodId: selectedPeriodId.value })
    downloadBlob(blob, `bank-transfer-${selectedPeriodId.value}.csv`)
  } catch (error) {
    console.error('Bank transfer export failed:', error)
    toast.error('Xuất file chuyển khoản thất bại')
  }
}

async function handleExportPit() {
  if (!selectedPeriodId.value || !payrollActionState.value.canExport) return

  try {
    const blob = await exportPitReport({ payrollPeriodId: selectedPeriodId.value })
    downloadBlob(blob, `pit-report-${selectedPeriodId.value}.csv`)
  } catch (error) {
    console.error('PIT export failed:', error)
    toast.error('Xuất báo cáo thuế thất bại')
  }
}

onMounted(fetchAll)
watch(selectedPeriodId, fetchItems)
watch(searchQuery, fetchItems)
</script>

<template>
  <div class="space-y-8">
    <PageHeader title="Bảng tính lương"
      subtitle="Kỳ lương, từng phiếu lương và trạng thái vận hành được gom về một workspace HR duy nhất."
      :icon="Banknote">
      <template #actions>
        <BaseButton variant="outline" :disabled="!payrollActionState.canExport"
          :title="payrollActionState.canExport ? 'Xuất file chuyển khoản ngân hàng' : 'Chỉ có thể xuất sau khi kỳ lương đã được duyệt'"
          @click="handleExportBank">
          <Download class="mr-2 h-4 w-4" />
          UNC ngân hàng
        </BaseButton>
        <BaseButton variant="outline" :disabled="!payrollActionState.canExport"
          :title="payrollActionState.canExport ? 'Xuất báo cáo PIT' : 'Chỉ có thể xuất sau khi kỳ lương đã được duyệt'"
          @click="handleExportPit">
          <Download class="mr-2 h-4 w-4" />
          Báo cáo PIT
        </BaseButton>
        <BaseButton variant="outline" :disabled="!payrollActionState.canGenerate" :title="getActionTitle('generate')"
          :loading="actionLoading === 'generate'" @click="handleGenerateDraft">
          <PlayCircle class="mr-2 h-4 w-4" />
          Chạy nháp
        </BaseButton>
        <BaseButton variant="outline" :disabled="!payrollActionState.canApprove" :title="getActionTitle('approve')"
          :loading="actionLoading === 'approve'" @click="handleApprovePeriod">
          <CheckCircle2 class="mr-2 h-4 w-4" />
          Duyệt kỳ
        </BaseButton>
        <BaseButton variant="primary" :disabled="!payrollActionState.canPublish" :title="getActionTitle('publish')"
          :loading="actionLoading === 'publish'" @click="handlePublishPeriod">
          <Send class="mr-2 h-4 w-4" />
          Phát hành
        </BaseButton>
      </template>
    </PageHeader>

    <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
      <InsightCard v-for="item in stats" :key="item.title" :title="item.title" :value="item.value"
        :subtitle="item.subtitle" :icon="item.icon" :tone="item.tone" />
    </div>

    <SurfacePanel>
      <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
        <div class="flex flex-wrap gap-2">
          <button v-for="period in periods" :key="period.payrollPeriodId" type="button"
            class="min-w-47.5 rounded-[24px] border px-4 py-4 text-left transition-all"
            :class="selectedPeriodId === period.payrollPeriodId ? 'border-indigo-200 bg-indigo-50 text-indigo-700 shadow-sm' : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300'"
            @click="selectedPeriodId = period.payrollPeriodId">
            <div class="flex items-start justify-between gap-3">
              <div>
                <p class="text-sm font-black">{{ formatMonthYear(period.periodYear, period.periodMonth,
                  period.periodCode)
                  }}</p>
                <p class="mt-1 text-xs font-bold tracking-[0.08em] text-slate-400">{{
                  getPeriodStatusLabel(period.periodStatus) }}</p>
              </div>
              <Clock3 class="h-4 w-4 text-slate-400" />
            </div>
            <div class="mt-4 h-2 overflow-hidden rounded-full bg-white/80">
              <div class="h-full rounded-full bg-indigo-500" :style="{ width: `${getPeriodProgress(period)}%` }" />
            </div>
            <p class="mt-2 text-xs font-medium text-slate-500">{{ getPeriodProgress(period) }}% manager confirm</p>
          </button>
        </div>

        <div class="relative">
          <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input v-model="searchQuery" type="text" placeholder="Tìm nhân sự trong kỳ..."
            class="w-full rounded-2xl border border-slate-200 bg-white py-3 pl-10 pr-4 text-sm font-medium outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10 md:w-72">
        </div>
      </div>

      <div v-if="selectedPeriod" class="mt-6 rounded-[28px] bg-slate-50 p-5">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <p class="text-[11px] font-black uppercase tracking-[0.2em] text-slate-400">Kỳ đang chọn</p>
            <h3 class="mt-2 text-2xl font-black text-slate-900">
              {{ formatMonthYear(selectedPeriod.periodYear, selectedPeriod.periodMonth, selectedPeriod.periodCode) }}
            </h3>
            <p class="mt-1 text-sm font-medium text-slate-500">
              Công thức {{ selectedPeriod.formulaCode || 'chưa gán' }} · attendance {{
                selectedPeriod.attendancePeriodCode
                || 'chưa liên kết' }}
            </p>
            <p class="mt-3 text-sm font-medium text-slate-600">
              {{ workflowHint }}
            </p>
          </div>
          <div class="flex flex-col items-start gap-2 lg:items-end">
            <StatusBadge :status="selectedPeriod.periodStatus || 'DRAFT'"
              :label="getPeriodStatusLabel(selectedPeriod.periodStatus)" />
            <p class="text-xs font-medium text-slate-500">
              Generated {{ formatDateTime(selectedPeriod.generatedAt) }} · Approved {{
                formatDateTime(selectedPeriod.approvedAt) }} · Published {{ formatDateTime(selectedPeriod.publishedAt) }}
            </p>
          </div>
        </div>
      </div>

      <div v-if="loading" class="mt-6 grid gap-6 xl:items-start xl:grid-cols-[minmax(0,1.05fr)_minmax(380px,520px)]">
        <div class="h-120 animate-pulse rounded-[28px] bg-slate-100" />
        <div class="h-120 animate-pulse rounded-[28px] bg-slate-100" />
      </div>

      <div v-else-if="filteredItems.length"
        class="mt-6 grid gap-6 xl:items-start xl:grid-cols-[minmax(0,1.02fr)_minmax(380px,520px)]">
        <SurfacePanel class="min-h-0">
          <div
            class="flex flex-col gap-4 border-b border-slate-200/80 pb-5 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Danh sách phiếu lương</p>
              <h3 class="mt-2 text-2xl font-black text-slate-900">{{ formatNumber(filteredItems.length) }} nhân sự trong
                kỳ
              </h3>
              <p class="mt-2 text-sm font-medium text-slate-500">Chọn một nhân sự để xem breakdown chi tiết ở panel bên
                phải.</p>
            </div>
            <div
              class="inline-flex items-center gap-2 rounded-2xl bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-500">
              <Wallet class="h-4 w-4 text-slate-400" />
              Scan nhanh theo net pay và trạng thái
            </div>
          </div>

          <div class="mt-5 space-y-4">
            <article v-for="item in filteredItems" :key="item.payrollItemId"
              class="cursor-pointer overflow-hidden rounded-[28px] border transition-all"
              :class="selectedItem?.payrollItemId === item.payrollItemId ? 'border-indigo-200 bg-indigo-50/60 shadow-lg' : 'border-slate-200 bg-white hover:border-indigo-200 hover:shadow-lg'"
              @click="openItem(item.payrollItemId)">
              <div class="flex flex-col gap-4 p-5">
                <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                  <div class="flex min-w-0 items-start gap-4">
                    <AvatarBox :name="item.employeeName" size="lg" shape="rounded-[20px]" />
                    <div class="min-w-0 flex-1">
                      <h4 class="text-lg font-black leading-tight text-slate-900">{{ item.employeeName }}</h4>
                      <div class="mt-1 space-y-1 text-sm font-medium leading-6 text-slate-500">
                        <p>{{ item.employeeCode }}</p>
                        <p>{{ item.orgUnitName || '—' }}</p>
                      </div>
                    </div>
                  </div>

                  <div class="flex shrink-0 flex-wrap items-center gap-2 xl:max-w-55 xl:justify-end">
                    <StatusBadge :status="item.itemStatus || 'DRAFT'" :label="getItemStatusLabel(item.itemStatus)" />
                    <span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-bold"
                      :class="item.managerConfirmedAt ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'">
                      {{ item.managerConfirmedAt ? 'Manager đã xác nhận' : 'Chờ manager xác nhận' }}
                    </span>
                  </div>
                </div>

                <div class="grid gap-3 sm:grid-cols-[minmax(0,1.15fr)_repeat(2,minmax(0,0.85fr))]">
                  <div class="rounded-[24px] p-4 text-white"
                    :class="selectedItem?.payrollItemId === item.payrollItemId ? 'bg-slate-900' : 'bg-linear-to-br from-slate-900 via-slate-800 to-slate-700'">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-white/60">Net pay</p>
                    <p class="mt-2 text-2xl font-black leading-none">{{ formatCompactCurrency(item.netPay) }}</p>
                    <p class="mt-3 text-sm font-medium text-white/70">Gross {{ formatCompactCurrency(item.grossIncome)
                      }}
                    </p>
                  </div>

                  <div class="rounded-2xl bg-slate-50 p-4 ring-1 ring-slate-100">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Ngày hiện diện</p>
                    <p class="mt-2 text-lg font-black text-slate-900">{{ item.presentDayCount ?? 0 }}/{{
                      item.scheduledDayCount ?? 0 }}</p>
                    <p class="mt-1 text-sm font-semibold text-slate-500">Đủ công trong kỳ</p>
                  </div>

                  <div class="rounded-2xl bg-slate-50 p-4 ring-1 ring-slate-100">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">OT / Vắng</p>
                    <p class="mt-2 text-lg font-black text-slate-900">{{ item.approvedOtMinutes || 0 }} phút</p>
                    <p class="mt-1 text-sm font-semibold text-slate-500">{{ item.absentDayCount || 0 }} ngày vắng</p>
                  </div>
                </div>
              </div>
            </article>
          </div>
        </SurfacePanel>

        <SurfacePanel class="h-fit self-start pt-4 sm:pt-5 xl:sticky xl:top-0">
          <div ref="detailPanelScrollRef" class="xl:max-h-[calc(100vh-6.5rem)] xl:overflow-y-auto xl:pr-1 xl:-mt-1">
            <div v-if="detailLoading" class="h-80 animate-pulse rounded-[28px] bg-slate-100" />

            <div v-else-if="selectedItem" class="space-y-5">
              <div
                class="overflow-hidden rounded-[28px] border border-slate-200 bg-[linear-gradient(180deg,#f8fafc_0%,#ffffff_100%)]">
                <div class="border-b border-slate-200 px-4 py-4 sm:px-5 sm:py-5">
                  <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                    <div class="flex min-w-0 items-start gap-3 sm:gap-4">
                      <AvatarBox :name="selectedItem.employeeName" size="xl" shape="rounded-[24px]" />
                      <div class="min-w-0 flex-1">
                        <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Phiếu lương</p>
                        <h3 class="mt-2 text-xl font-black leading-tight text-slate-900 sm:text-2xl">
                          {{ selectedItem.employeeName }}
                        </h3>
                        <div class="mt-1 space-y-1 text-sm font-medium leading-6 text-slate-500">
                          <p>{{ selectedItem.employeeCode }}</p>
                          <p>{{ selectedItem.orgUnitName || '—' }}</p>
                        </div>

                        <div class="mt-3 flex flex-wrap gap-2">
                          <span
                            class="inline-flex items-center rounded-full bg-slate-100 px-3 py-1 text-xs font-bold text-slate-600">
                            Kỳ {{ formatMonthYear(selectedPeriod?.periodYear, selectedPeriod?.periodMonth,
                              selectedPeriod?.periodCode) }}
                          </span>
                          <span
                            class="inline-flex items-center rounded-full bg-indigo-50 px-3 py-1 text-xs font-bold text-indigo-700">
                            {{ selectedItem.presentDayCount ?? 0 }}/{{ selectedItem.scheduledDayCount ?? 0 }} ngày hiện
                            diện
                          </span>
                        </div>
                      </div>
                    </div>

                    <div class="flex shrink-0 flex-wrap items-center gap-2 xl:max-w-55 xl:justify-end">
                      <StatusBadge :status="selectedItem.itemStatus || 'DRAFT'"
                        :label="getItemStatusLabel(selectedItem.itemStatus)" />
                      <span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-bold"
                        :class="selectedItem.managerConfirmedAt ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'">
                        {{ selectedItem.managerConfirmedAt ? 'Manager đã xác nhận' : 'Chờ manager xác nhận' }}
                      </span>
                    </div>
                  </div>
                </div>

                <div class="grid gap-3 px-4 py-4 sm:grid-cols-2 sm:px-5">
                  <div class="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-100">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Gross</p>
                    <p class="mt-2 text-lg font-black leading-snug text-slate-900 break-all sm:break-normal">{{
                      formatCurrency(selectedItem.grossIncome) }}</p>
                  </div>
                  <div class="rounded-2xl bg-emerald-50/70 p-4 shadow-sm ring-1 ring-emerald-100">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-emerald-600">Net pay</p>
                    <p class="mt-2 text-lg font-black leading-snug text-emerald-700 break-all sm:break-normal">{{
                      formatCurrency(selectedItem.netPay) }}</p>
                  </div>
                  <div class="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-100">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">PIT</p>
                    <p class="mt-2 text-lg font-black leading-snug text-slate-900 break-all sm:break-normal">{{
                      formatCurrency(selectedItem.pitAmount) }}</p>
                  </div>
                  <div class="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-100">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">OT / Vắng</p>
                    <p class="mt-2 text-lg font-black leading-snug text-slate-900">{{ selectedItem.approvedOtMinutes ||
                      0 }}
                      phút</p>
                    <p class="mt-1 text-sm font-semibold text-slate-500">{{ selectedItem.absentDayCount || 0 }} ngày
                      vắng
                    </p>
                  </div>
                </div>
              </div>

              <div class="grid gap-3 md:grid-cols-2">
                <div v-for="row in selectedItemSummary" :key="row.label" class="rounded-2xl bg-slate-50 p-4">
                  <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">{{ row.label }}</p>
                  <p class="mt-2 text-sm font-bold text-slate-800">{{ row.value }}</p>
                </div>
              </div>

              <div class="overflow-hidden rounded-[24px] border border-slate-200">
                <div class="flex items-center justify-between gap-3 border-b border-slate-200 bg-white px-4 py-4">
                  <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Breakdown thành phần</p>
                  <StatusBadge :status="selectedItem.itemStatus || 'DRAFT'"
                    :label="getItemStatusLabel(selectedItem.itemStatus)" />
                </div>

                <div v-if="selectedItemLines.length" class="max-h-105 space-y-3 overflow-y-auto bg-slate-50/80 p-4">
                  <div v-for="line in selectedItemLines" :key="line.payrollItemLineId"
                    class="rounded-2xl bg-white p-4 shadow-sm">
                    <div class="flex items-center justify-between gap-3">
                      <div>
                        <p class="text-sm font-black text-slate-900">{{ line.componentName }}</p>
                        <p class="mt-1 text-xs font-bold uppercase tracking-[0.18em] text-slate-400">
                          {{ line.componentCode }} · {{ line.componentCategory }} · {{ line.lineSourceType }}
                        </p>
                      </div>
                      <p class="text-sm font-black text-slate-900">{{ formatCurrency(line.lineAmount) }}</p>
                    </div>
                    <p v-if="line.lineNote" class="mt-2 text-sm font-medium text-slate-500">{{ line.lineNote }}</p>
                  </div>
                </div>
                <p v-else class="p-4 text-sm font-medium text-slate-500">Phiếu lương này chưa có line chi tiết.</p>
              </div>

              <div class="rounded-[24px] bg-slate-50 p-4 text-sm font-medium text-slate-600">
                <p>Manager confirmed: {{ formatDateTime(selectedItem.managerConfirmedAt) }}</p>
                <p class="mt-2">HR approved: {{ formatDateTime(selectedItem.hrApprovedAt) }}</p>
                <p class="mt-2">Published: {{ formatDateTime(selectedItem.publishedAt) }}</p>
                <p v-if="selectedItem.adjustmentNote" class="mt-2">Adjustment note: {{ selectedItem.adjustmentNote }}
                </p>
              </div>
            </div>

            <EmptyState v-else iconName="Wallet" title="Chọn một phiếu lương"
              description="Bấm vào card nhân sự ở bên trái để xem breakdown lương chi tiết." />
          </div>
        </SurfacePanel>
      </div>

      <EmptyState v-else iconName="Banknote" title="Chưa có dữ liệu bảng lương"
        description="Kỳ lương này chưa được tạo hoặc chưa có nhân sự phù hợp với bộ lọc hiện tại." />
    </SurfacePanel>
  </div>
</template>
