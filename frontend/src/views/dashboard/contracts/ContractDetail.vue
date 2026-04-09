<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft,
  BriefcaseBusiness,
  CalendarRange,
  CheckCircle2,
  ChevronRight,
  Clock3,
  Download,
  Edit,
  FileSignature,
  FolderKanban,
  Paperclip,
  Plus,
  RotateCcw,
  Send,
  ShieldCheck,
  Trash2,
  UserRound,
  Wallet,
  XCircle,
} from 'lucide-vue-next'
import {
  createAppendix,
  createAttachment,
  createRenewalDraft,
  exportContract,
  getContractDetail,
  getContractTypeDetail,
  getContractHistory,
} from '@/api/admin/contract'
import { useToast } from '@/composables/useToast'
import { useUiStore } from '@/stores/ui'

const router = useRouter()
const route = useRoute()
const toast = useToast()
const ui = useUiStore()

const loading = ref(false)
const exportLoading = ref(false)
const renewalSubmitting = ref(false)
const appendixSubmitting = ref(false)
const attachmentSubmitting = ref(false)
const showAppendixModal = ref(false)
const showAttachmentModal = ref(false)
const appendixOverlayRef = ref(null)
const attachmentOverlayRef = ref(null)
const appendixFirstFieldRef = ref(null)
const attachmentFirstFieldRef = ref(null)

const appendixForm = ref({
  appendixNumber: '',
  appendixName: '',
  effectiveDate: '',
  changedFieldsJson: '',
  note: '',
})

const attachmentForm = ref({
  attachmentType: 'CONTRACT_FILE',
  fileName: '',
  storagePath: '',
  mimeType: 'application/pdf',
  fileSizeBytes: '',
  status: 'ACTIVE',
})
const rawContractDetail = ref(null)

const contract = ref({
  id: null,
  contractCode: 'Đang tải...',
  employee: 'Đang tải...',
  employeeCode: '',
  contractType: 'Đang tải...',
  orgUnit: 'Chưa cập nhật',
  jobTitle: 'Chưa cập nhật',
  baseSalary: '0',
  allowances: 'Chưa cập nhật',
  startDate: '-',
  endDate: '-',
  signDate: '-',
  companyRepresentative: 'Chưa cập nhật',
  companyTitle: 'Đại diện công ty',
  status: 'DRAFT',
  workLocation: 'Chưa cập nhật',
  probationDays: 0,
  notes: 'Chưa có ghi chú cho hợp đồng này.',
})

const statusFlow = [
  { key: 'DRAFT', label: 'Khởi tạo nháp', icon: Edit },
  { key: 'PENDING_SIGN', label: 'Chờ ký duyệt', icon: Clock3 },
  { key: 'ACTIVE', label: 'Đang hiệu lực', icon: CheckCircle2 },
]

const statusConfig = {
  DRAFT: {
    label: 'Nháp',
    chip: 'bg-slate-100 text-slate-700 ring-slate-300',
    accent: 'bg-slate-500',
    surface: 'from-slate-100 to-white',
  },
  PENDING_SIGN: {
    label: 'Chờ ký',
    chip: 'bg-amber-50 text-amber-700 ring-amber-500/20',
    accent: 'bg-amber-500',
    surface: 'from-amber-50 to-white',
  },
  ACTIVE: {
    label: 'Đang hiệu lực',
    chip: 'bg-emerald-50 text-emerald-700 ring-emerald-500/20',
    accent: 'bg-emerald-500',
    surface: 'from-emerald-50 to-white',
  },
  SUSPENDED: {
    label: 'Tạm đình chỉ',
    chip: 'bg-orange-50 text-orange-700 ring-orange-500/20',
    accent: 'bg-orange-500',
    surface: 'from-orange-50 to-white',
  },
  TERMINATED: {
    label: 'Đã chấm dứt',
    chip: 'bg-rose-50 text-rose-700 ring-rose-500/20',
    accent: 'bg-rose-500',
    surface: 'from-rose-50 to-white',
  },
  EXPIRED: {
    label: 'Hết hạn',
    chip: 'bg-slate-100 text-slate-600 ring-slate-300',
    accent: 'bg-slate-400',
    surface: 'from-slate-100 to-white',
  },
}

const history = ref([])
const appendices = ref([])
const attachments = ref([])

const cfg = computed(() => statusConfig[contract.value.status] || statusConfig.DRAFT)
const currentStepIdx = computed(() => statusFlow.findIndex((step) => step.key === contract.value.status))

function parseDate(value) {
  if (!value) return null
  const [day, month, year] = value.split('/')
  const date = new Date(Number(year), Number(month) - 1, Number(day))
  return Number.isNaN(date.getTime()) ? null : date
}

function formatDate(value) {
  if (!value) return '-'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'

  return date.toLocaleDateString('vi-VN')
}

function formatDateToIso(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatDateTime(value) {
  if (!value) return '-'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'

  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatCurrency(value, currency = 'VND') {
  if (value === null || value === undefined || value === '') return '0'

  const amount = Number(value)
  if (Number.isNaN(amount)) return String(value)

  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: currency || 'VND',
    maximumFractionDigits: 0,
  }).format(amount)
}

function formatFileSize(bytes) {
  const size = Number(bytes || 0)
  if (!size) return '0 B'

  const units = ['B', 'KB', 'MB', 'GB']
  let value = size
  let unitIndex = 0

  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024
    unitIndex += 1
  }

  return `${value.toFixed(value >= 10 || unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`
}

const daysRemaining = computed(() => {
  const end = parseDate(contract.value.endDate)
  if (!end) return null

  const now = new Date()
  now.setHours(0, 0, 0, 0)
  end.setHours(0, 0, 0, 0)
  return Math.round((end.getTime() - now.getTime()) / 86400000)
})

const overviewStats = computed(() => [
  {
    label: 'Hiệu lực',
    value: `${contract.value.startDate} - ${contract.value.endDate}`,
    hint: daysRemaining.value === null
      ? 'Không giới hạn thời hạn'
      : daysRemaining.value >= 0
        ? `Còn ${daysRemaining.value} ngày đến hạn`
        : `Quá hạn ${Math.abs(daysRemaining.value)} ngày`,
    tone: 'bg-indigo-50 text-indigo-700 border-indigo-100',
    icon: CalendarRange,
  },
  {
    label: 'Công việc',
    value: contract.value.jobTitle,
    hint: contract.value.orgUnit,
    tone: 'bg-amber-50 text-amber-700 border-amber-100',
    icon: BriefcaseBusiness,
  },
  {
    label: 'Thu nhập',
    value: `${contract.value.baseSalary}`,
    hint: `Phụ cấp ${contract.value.allowances}`,
    tone: 'bg-emerald-50 text-emerald-700 border-emerald-100',
    icon: Wallet,
  },
  {
    label: 'Pháp lý',
    value: `${appendices.value.length} phụ lục`,
    hint: `${attachments.value.length} tệp đính kèm hoạt động`,
    tone: 'bg-sky-50 text-sky-700 border-sky-100',
    icon: ShieldCheck,
  },
])

const contractDetails = computed(() => [
  { label: 'Mã hợp đồng', value: contract.value.contractCode },
  { label: 'Loại hợp đồng', value: contract.value.contractType },
  { label: 'Ngày ký', value: contract.value.signDate },
  { label: 'Ngày hiệu lực', value: contract.value.startDate },
  { label: 'Ngày hết hạn', value: contract.value.endDate },
  { label: 'Nơi làm việc', value: contract.value.workLocation },
])

const supportSections = computed(() => [
  {
    key: 'appendices',
    title: 'Phụ lục',
    value: `${appendices.value.length} mục`,
    hint: appendices.value[0]?.title || 'Chưa có phụ lục phát sinh',
    tone: 'bg-indigo-50 border-indigo-100 text-indigo-700',
  },
  {
    key: 'attachments',
    title: 'Tệp đính kèm',
    value: `${attachments.value.length} tệp`,
    hint: attachments.value[0]?.name || 'Chưa có tài liệu kèm theo',
    tone: 'bg-sky-50 border-sky-100 text-sky-700',
  },
  {
    key: 'history',
    title: 'Nhật ký trạng thái',
    value: `${history.value.length} lần cập nhật`,
    hint: history.value[history.value.length - 1]?.note || 'Chưa có lịch sử thay đổi',
    tone: 'bg-amber-50 border-amber-100 text-amber-700',
  },
])

const attachmentTypeOptions = [
  { value: 'CONTRACT_FILE', label: 'File hợp đồng' },
  { value: 'APPENDIX_FILE', label: 'File phụ lục' },
  { value: 'SIGNED_SCAN', label: 'Bản scan đã ký' },
  { value: 'OTHER', label: 'Khác' },
]

function getStatusMeta(status) {
  return statusConfig[status] || statusConfig.DRAFT
}

function scrollToSection(sectionId) {
  document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function normalizeContract(detail = {}) {
  return {
    id: detail.laborContractId,
    contractCode: detail.contractNumber || `HĐ-${detail.laborContractId}`,
    employee: detail.employeeFullName || 'Chưa cập nhật',
    employeeCode: detail.employeeCode || '',
    contractType: detail.contractTypeName || 'Chưa cập nhật',
    orgUnit: detail.orgUnitName || 'Chưa cập nhật',
    jobTitle: detail.jobTitleName || 'Chưa cập nhật',
    baseSalary: formatCurrency(detail.baseSalary, detail.salaryCurrency),
    allowances: 'Chưa cập nhật',
    startDate: formatDate(detail.effectiveDate),
    endDate: formatDate(detail.endDate),
    signDate: formatDate(detail.signDate),
    companyRepresentative: detail.signedByCompanyUsername || 'Chưa cập nhật',
    companyTitle: detail.signedByCompanyUsername ? 'Người đại diện ký công ty' : 'Đại diện công ty',
    status: detail.contractStatus || 'DRAFT',
    workLocation: detail.workLocation || 'Chưa cập nhật',
    probationDays: 0,
    notes: detail.note || 'Chưa có ghi chú cho hợp đồng này.',
  }
}

function buildRenewalContractNumber(sourceNumber = '') {
  const stamp = new Date()
  const suffix = `-RN${stamp.getFullYear()}${String(stamp.getMonth() + 1).padStart(2, '0')}${String(stamp.getDate()).padStart(2, '0')}${String(stamp.getHours()).padStart(2, '0')}${String(stamp.getMinutes()).padStart(2, '0')}${String(stamp.getSeconds()).padStart(2, '0')}`
  const maxBaseLength = Math.max(1, 50 - suffix.length)
  return `${String(sourceNumber || 'HD').slice(0, maxBaseLength)}${suffix}`
}

function buildRenewalEndDate(detail, nextEffectiveDate) {
  if (!detail?.endDate || !detail?.effectiveDate) return null

  const sourceEffective = new Date(detail.effectiveDate)
  const sourceEnd = new Date(detail.endDate)
  const nextEffective = new Date(nextEffectiveDate)

  if ([sourceEffective, sourceEnd, nextEffective].some((date) => Number.isNaN(date.getTime()))) {
    return null
  }

  const durationInDays = Math.round((sourceEnd.getTime() - sourceEffective.getTime()) / 86400000)
  if (durationInDays < 0) return null

  nextEffective.setDate(nextEffective.getDate() + durationInDays)
  return formatDateToIso(nextEffective)
}

function parseIsoDate(value) {
  if (!value || typeof value !== 'string') return null
  const [year, month, day] = value.split('-').map(Number)
  if (!year || !month || !day) return null
  return { year, month, day }
}

function getLastDayOfMonth(year, month) {
  return new Date(year, month, 0).getDate()
}

function addMonthsToIsoDate(value, monthsToAdd) {
  const parsed = parseIsoDate(value)
  if (!parsed || !Number.isFinite(monthsToAdd)) return null

  const totalMonths = (parsed.year * 12) + (parsed.month - 1) + monthsToAdd
  const targetYear = Math.floor(totalMonths / 12)
  const targetMonth = (totalMonths % 12) + 1
  const targetDay = Math.min(parsed.day, getLastDayOfMonth(targetYear, targetMonth))

  return formatDateToIso(new Date(targetYear, targetMonth - 1, targetDay))
}

function shiftIsoDateByDays(value, daysToShift) {
  const parsed = parseIsoDate(value)
  if (!parsed || !Number.isFinite(daysToShift)) return null

  const date = new Date(parsed.year, parsed.month - 1, parsed.day)
  date.setDate(date.getDate() + daysToShift)
  return formatDateToIso(date)
}

async function resolveRenewalEndDate(detail, nextEffectiveDate) {
  if (!detail?.contractTypeId || !nextEffectiveDate) {
    return buildRenewalEndDate(detail, nextEffectiveDate)
  }

  try {
    const response = await getContractTypeDetail(detail.contractTypeId)
    const contractType = response?.data || response || {}

    if (contractType.requiresEndDate === false) {
      return null
    }

    if (Number.isInteger(contractType.maxTermMonths) && contractType.maxTermMonths > 0) {
      const maxEndDate = addMonthsToIsoDate(nextEffectiveDate, contractType.maxTermMonths)
      return maxEndDate ? shiftIsoDateByDays(maxEndDate, -1) : buildRenewalEndDate(detail, nextEffectiveDate)
    }
  } catch (error) {
    console.warn('Failed to resolve contract type policy for renewal draft:', error)
  }

  return buildRenewalEndDate(detail, nextEffectiveDate)
}

function normalizeAppendices(items = []) {
  return items.map((item) => ({
    id: item.contractAppendixId,
    appendixCode: item.appendixNumber || `PL-${item.contractAppendixId}`,
    title: item.appendixName || 'Phụ lục hợp đồng',
    effectiveDate: formatDate(item.effectiveDate),
    status: item.status,
    salaryChange: item.changedFieldsJson || item.note || 'Chưa có mô tả thay đổi',
    signedBy: 'Theo hồ sơ hệ thống',
  }))
}

function normalizeAttachments(items = []) {
  return items.map((item) => ({
    id: item.contractAttachmentId,
    name: item.fileName || item.storagePath || 'Tệp đính kèm',
    size: formatFileSize(item.fileSizeBytes),
    uploadedAt: formatDateTime(item.uploadedAt),
    status: item.status,
  }))
}

function normalizeHistory(items = []) {
  return items.map((item) => ({
    id: item.contractStatusHistoryId,
    fromStatus: item.fromStatus,
    toStatus: item.toStatus,
    actor: item.changedByUsername || 'Hệ thống',
    note: item.reason || 'Không có diễn giải',
    timestamp: formatDateTime(item.changedAt),
  }))
}

async function fetchContractDetail() {
  const laborContractId = route.params.id
  if (!laborContractId) return

  loading.value = true
  try {
    const [detailRes, historyRes] = await Promise.all([
      getContractDetail(laborContractId),
      getContractHistory(laborContractId),
    ])

    const detail = detailRes?.data || {}
    rawContractDetail.value = detail
    contract.value = normalizeContract(detail)
    appendices.value = normalizeAppendices(detail?.appendices || [])
    attachments.value = normalizeAttachments(detail?.attachments || [])
    history.value = normalizeHistory(historyRes?.data || [])
  } catch (error) {
    console.error('Failed to load contract detail:', error)
    toast.error('Không thể tải chi tiết hợp đồng')
  } finally {
    loading.value = false
  }
}

async function handleCreateRenewalDraft() {
  if (!contract.value.id || renewalSubmitting.value || !rawContractDetail.value) return

  const confirmed = await ui.confirm({
    title: 'Tạo hợp đồng kế nhiệm',
    message: `Hệ thống sẽ tạo bản nháp kế nhiệm cho ${contract.value.employee} dựa trên hợp đồng hiện tại. Tiếp tục?`,
    confirmLabel: 'Tạo bản nháp',
  })
  if (!confirmed) return

  const source = rawContractDetail.value
  const today = new Date()
  const nextEffectiveDate = source.endDate
    ? (() => {
      const date = new Date(source.endDate)
      date.setDate(date.getDate() + 1)
      return formatDateToIso(date)
    })()
    : formatDateToIso(today)

  renewalSubmitting.value = true
  try {
    const nextEndDate = await resolveRenewalEndDate(source, nextEffectiveDate)
    const response = await createRenewalDraft(contract.value.id, {
      contractTypeId: source.contractTypeId,
      contractNumber: buildRenewalContractNumber(source.contractNumber),
      signDate: formatDateToIso(today),
      effectiveDate: nextEffectiveDate,
      endDate: nextEndDate,
      jobTitleId: source.jobTitleId,
      orgUnitId: source.orgUnitId,
      workLocation: source.workLocation,
      baseSalary: Number(source.baseSalary),
      salaryCurrency: source.salaryCurrency || 'VND',
      workingType: source.workingType,
      note: `Hợp đồng kế nhiệm được tạo từ hợp đồng #${source.contractNumber}.`,
    })

    const newContractId = response?.data?.laborContractId
    toast.success('Đã tạo bản nháp hợp đồng kế nhiệm')

    if (newContractId) {
      router.push(`/contracts/${newContractId}`)
      return
    }

    router.push('/contracts')
  } catch (error) {
    console.error('Failed to create renewal draft:', error)
    toast.error(error.response?.data?.message || 'Tạo hợp đồng kế nhiệm thất bại')
  } finally {
    renewalSubmitting.value = false
  }
}

function resetAppendixForm() {
  appendixForm.value = {
    appendixNumber: '',
    appendixName: '',
    effectiveDate: '',
    changedFieldsJson: '',
    note: '',
  }
}

function resetAttachmentForm() {
  attachmentForm.value = {
    attachmentType: 'CONTRACT_FILE',
    fileName: '',
    storagePath: '',
    mimeType: 'application/pdf',
    fileSizeBytes: '',
    status: 'ACTIVE',
  }
}

function openAppendixModal() {
  resetAppendixForm()
  showAppendixModal.value = true
  nextTick(() => {
    appendixOverlayRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
    appendixFirstFieldRef.value?.focus()
  })
}

function openAttachmentModal() {
  resetAttachmentForm()
  showAttachmentModal.value = true
  nextTick(() => {
    attachmentOverlayRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
    attachmentFirstFieldRef.value?.focus()
  })
}

async function handleCreateAppendix() {
  if (!contract.value.id || appendixSubmitting.value) return

  if (!appendixForm.value.appendixNumber.trim() || !appendixForm.value.appendixName.trim() || !appendixForm.value.effectiveDate) {
    toast.error('Vui lòng nhập đủ số phụ lục, tên phụ lục và ngày hiệu lực')
    return
  }

  appendixSubmitting.value = true
  try {
    await createAppendix(contract.value.id, {
      appendixNumber: appendixForm.value.appendixNumber.trim(),
      appendixName: appendixForm.value.appendixName.trim(),
      effectiveDate: appendixForm.value.effectiveDate,
      changedFieldsJson: appendixForm.value.changedFieldsJson.trim() || null,
      note: appendixForm.value.note.trim() || null,
    })
    toast.success('Tạo phụ lục thành công')
    showAppendixModal.value = false
    await fetchContractDetail()
  } catch (error) {
    console.error('Failed to create appendix:', error)
    toast.error(error.response?.data?.message || 'Tạo phụ lục thất bại')
  } finally {
    appendixSubmitting.value = false
  }
}

async function handleCreateAttachment() {
  if (!contract.value.id || attachmentSubmitting.value) return

  if (!attachmentForm.value.fileName.trim() || !attachmentForm.value.storagePath.trim()) {
    toast.error('Vui lòng nhập tên file và đường dẫn lưu trữ')
    return
  }

  attachmentSubmitting.value = true
  try {
    await createAttachment(contract.value.id, {
      attachmentType: attachmentForm.value.attachmentType,
      fileName: attachmentForm.value.fileName.trim(),
      storagePath: attachmentForm.value.storagePath.trim(),
      mimeType: attachmentForm.value.mimeType.trim() || null,
      fileSizeBytes: attachmentForm.value.fileSizeBytes === '' ? 0 : Number(attachmentForm.value.fileSizeBytes),
      status: attachmentForm.value.status,
    })
    toast.success('Thêm tài liệu thành công')
    showAttachmentModal.value = false
    await fetchContractDetail()
  } catch (error) {
    console.error('Failed to create attachment:', error)
    toast.error(error.response?.data?.message || 'Thêm tài liệu thất bại')
  } finally {
    attachmentSubmitting.value = false
  }
}

async function handleExportPdf() {
  if (!contract.value.id || exportLoading.value) return

  exportLoading.value = true
  try {
    const htmlBlob = await exportContract(contract.value.id)
    const htmlContent = await htmlBlob.text()
    const printWindow = window.open('', '_blank', 'noopener,noreferrer')

    if (!printWindow) {
      toast.error('Trình duyệt đang chặn popup in PDF')
      return
    }

    printWindow.document.open()
    printWindow.document.write(htmlContent)
    printWindow.document.close()

    setTimeout(() => {
      printWindow.focus()
      printWindow.print()
    }, 500)
  } catch (error) {
    console.error('Failed to export contract:', error)
    toast.error('Xuất PDF thất bại')
  } finally {
    exportLoading.value = false
  }
}

onMounted(fetchContractDetail)
watch(() => route.params.id, fetchContractDetail)
watch([showAppendixModal, showAttachmentModal], ([appendixOpen, attachmentOpen]) => {
  document.body.style.overflow = appendixOpen || attachmentOpen ? 'hidden' : ''
})
onBeforeUnmount(() => {
  document.body.style.overflow = ''
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center gap-2 text-sm font-medium">
      <button @click="router.push('/contracts')" class="flex items-center gap-1 text-indigo-600 hover:underline">
        <ArrowLeft class="h-4 w-4" /> Hợp đồng lao động
      </button>
      <ChevronRight class="h-4 w-4 text-slate-300" />
      <span class="text-slate-500">{{ contract.contractCode }}</span>
    </div>

    <div v-if="loading"
      class="flex items-center justify-center gap-3 rounded-4xl border border-slate-200 bg-white px-6 py-16 text-slate-500 shadow-sm">
      <Clock3 class="h-5 w-5 animate-pulse text-indigo-600" />
      <span class="font-medium">Đang tải chi tiết hợp đồng...</span>
    </div>

    <div v-else
      class="overflow-hidden rounded-4xl border border-slate-200 bg-white shadow-[0_20px_60px_rgba(15,23,42,0.06)]">
      <div
        class="relative overflow-hidden bg-[radial-gradient(circle_at_top_right,_rgba(99,102,241,0.16),_transparent_28%),linear-gradient(135deg,#f8fafc_0%,#eef2ff_45%,#ffffff_100%)] px-7 py-7">
        <div class="absolute right-0 top-0 h-56 w-56 rounded-full bg-indigo-200/20 blur-3xl" />
        <div class="relative grid gap-6 xl:grid-cols-[minmax(0,1fr)_280px]">
          <div class="space-y-5">
            <div class="flex flex-wrap items-start gap-4">
              <div
                class="flex h-16 w-16 items-center justify-center rounded-3xl bg-slate-900 text-white shadow-lg shadow-slate-900/10">
                <FileSignature class="h-8 w-8" />
              </div>

              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center gap-3">
                  <h1 class="text-3xl font-black tracking-tight text-slate-900">{{ contract.contractCode }}</h1>
                  <span class="rounded-full px-3 py-1.5 text-xs font-bold ring-1 ring-inset" :class="cfg.chip">
                    <span class="mr-1.5 inline-block h-2 w-2 rounded-full" :class="cfg.accent" />
                    {{ cfg.label }}
                  </span>
                </div>

                <p class="mt-2 text-lg font-semibold text-slate-900">
                  {{ contract.employee }}
                  <span class="font-medium text-slate-400">({{ contract.employeeCode }})</span>
                </p>
                <p class="mt-1 text-sm font-medium text-slate-500">{{ contract.contractType }}</p>
              </div>
            </div>

            <div class="grid gap-3 md:grid-cols-3">
              <div class="rounded-2xl border border-white/80 bg-white/80 px-4 py-3 backdrop-blur-sm">
                <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-slate-400">
                  <FolderKanban class="h-4 w-4" />
                  Đơn vị
                </div>
                <div class="mt-2 text-sm font-bold text-slate-900">{{ contract.orgUnit }}</div>
              </div>
              <div class="rounded-2xl border border-white/80 bg-white/80 px-4 py-3 backdrop-blur-sm">
                <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-slate-400">
                  <BriefcaseBusiness class="h-4 w-4" />
                  Vai trò
                </div>
                <div class="mt-2 text-sm font-bold text-slate-900">{{ contract.jobTitle }}</div>
              </div>
              <div class="rounded-2xl border border-white/80 bg-white/80 px-4 py-3 backdrop-blur-sm">
                <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-slate-400">
                  <CalendarRange class="h-4 w-4" />
                  Thời hạn
                </div>
                <div class="mt-2 text-sm font-bold text-slate-900">{{ contract.startDate }} - {{ contract.endDate }}
                </div>
              </div>
            </div>
          </div>

          <div class="space-y-3 rounded-[28px] border border-white/70 bg-white/75 p-4 backdrop-blur-sm">
            <div class="text-xs font-bold uppercase tracking-[0.2em] text-slate-400">Thao tác nhanh</div>

            <button v-if="contract.status === 'DRAFT'"
              class="flex w-full items-center justify-center gap-2 rounded-2xl bg-amber-400 px-4 py-3 font-bold text-amber-950 transition hover:bg-amber-300">
              <Send class="h-4 w-4" /> Gửi chờ ký
            </button>
            <button v-if="contract.status === 'PENDING_SIGN'"
              class="flex w-full items-center justify-center gap-2 rounded-2xl bg-emerald-500 px-4 py-3 font-bold text-white transition hover:bg-emerald-400">
              <CheckCircle2 class="h-4 w-4" /> Kích hoạt hợp đồng
            </button>
            <button v-if="contract.status === 'PENDING_SIGN'"
              class="flex w-full items-center justify-center gap-2 rounded-2xl bg-rose-500 px-4 py-3 font-bold text-white transition hover:bg-rose-400">
              <XCircle class="h-4 w-4" /> Từ chối
            </button>
            <button v-if="contract.status === 'ACTIVE'"
              class="flex w-full items-center justify-center gap-2 rounded-2xl bg-indigo-600 px-4 py-3 font-bold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-70"
              :disabled="renewalSubmitting" @click="handleCreateRenewalDraft">
              <RotateCcw class="h-4 w-4" />
              {{ renewalSubmitting ? 'Đang tạo...' : 'Tạo hợp đồng kế nhiệm' }}
            </button>
            <button
              class="flex w-full items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 font-bold text-slate-700 transition hover:border-indigo-200 hover:text-indigo-700 disabled:cursor-not-allowed disabled:opacity-70"
              :disabled="exportLoading" @click="handleExportPdf">
              <Download class="h-4 w-4" />
              {{ exportLoading ? 'Đang chuẩn bị...' : 'Xuất PDF' }}
            </button>
          </div>
        </div>
      </div>

      <div class="grid gap-3 border-t border-slate-100 bg-slate-50/70 px-6 py-5 md:grid-cols-2 xl:grid-cols-4">
        <div v-for="item in overviewStats" :key="item.label" class="rounded-2xl border px-4 py-4" :class="item.tone">
          <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] opacity-75">
            <component :is="item.icon" class="h-4 w-4" />
            {{ item.label }}
          </div>
          <div class="mt-3 text-lg font-black">{{ item.value }}</div>
          <div class="mt-1 text-sm font-medium opacity-80">{{ item.hint }}</div>
        </div>
      </div>
    </div>

    <div class="grid gap-4 md:grid-cols-3">
      <button v-for="section in supportSections" :key="section.key"
        class="rounded-[24px] border p-4 text-left transition hover:-translate-y-0.5 hover:shadow-sm"
        :class="section.tone" @click="scrollToSection(section.key)">
        <div class="text-xs font-bold uppercase tracking-[0.18em] opacity-75">{{ section.title }}</div>
        <div class="mt-3 text-lg font-black">{{ section.value }}</div>
        <div class="mt-1 text-sm font-medium opacity-80">{{ section.hint }}</div>
      </button>
    </div>

    <div class="grid gap-6 xl:grid-cols-[minmax(0,1.45fr)_380px]">
      <div class="space-y-6">
        <div class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex items-center justify-between gap-4 border-b border-slate-100 pb-4">
            <div>
              <h3 class="text-xl font-black text-slate-900">Bản tóm tắt hợp đồng</h3>
              <p class="mt-1 text-sm text-slate-500">Thông tin pháp lý và điều khoản cốt lõi của hồ sơ hiện tại</p>
            </div>
            <div class="rounded-2xl bg-slate-100 px-3 py-2 text-right">
              <div class="text-[11px] font-bold uppercase tracking-[0.18em] text-slate-400">Mã nhân sự</div>
              <div class="mt-1 text-sm font-bold text-slate-900">{{ contract.employeeCode }}</div>
            </div>
          </div>

          <div class="mt-5 grid gap-4 md:grid-cols-2">
            <div v-for="item in contractDetails" :key="item.label"
              class="rounded-2xl border border-slate-100 bg-slate-50/70 px-4 py-4">
              <div class="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">{{ item.label }}</div>
              <div class="mt-2 text-base font-bold text-slate-900">{{ item.value }}</div>
            </div>
          </div>
        </div>

        <div class="grid gap-6 md:grid-cols-2">
          <div class="rounded-[28px] border border-emerald-100 bg-linear-to-br from-emerald-50 to-white p-6 shadow-sm">
            <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-emerald-600">
              <Wallet class="h-4 w-4" />
              Lương cơ bản
            </div>
            <div class="mt-4 text-4xl font-black text-emerald-700">{{ contract.baseSalary }}</div>
            <div class="mt-2 text-sm font-medium text-emerald-700/80">VNĐ / tháng</div>
          </div>

          <div class="rounded-[28px] border border-indigo-100 bg-linear-to-br from-indigo-50 to-white p-6 shadow-sm">
            <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-indigo-600">
              <ShieldCheck class="h-4 w-4" />
              Phụ cấp và điều chỉnh
            </div>
            <div class="mt-4 text-4xl font-black text-indigo-700">{{ contract.allowances }}</div>
            <div class="mt-2 text-sm font-medium text-indigo-700/80">VNĐ / tháng</div>
          </div>
        </div>

        <div class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
          <div class="border-b border-slate-100 pb-4">
            <h3 class="text-xl font-black text-slate-900">Ghi chú và bối cảnh</h3>
            <p class="mt-1 text-sm text-slate-500">Phần diễn giải nhanh để HR hoặc quản lý nắm ý chính của hợp đồng</p>
          </div>
          <div class="mt-5 rounded-3xl bg-slate-50 p-5 text-slate-600">
            <p class="text-base leading-7 font-medium">"{{ contract.notes }}"</p>
          </div>
        </div>

        <div id="appendices" class="space-y-4 scroll-mt-6">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-xl font-black text-slate-900">Phụ lục đang quản lý</h3>
              <p class="mt-1 text-sm text-slate-500">Theo dõi những thay đổi phát sinh trong suốt thời gian hiệu lực</p>
            </div>
            <button
              class="flex items-center gap-2 rounded-2xl bg-slate-900 px-4 py-3 text-sm font-bold text-white transition hover:bg-slate-800"
              @click="openAppendixModal">
              <Plus class="h-4 w-4" /> Tạo phụ lục
            </button>
          </div>

          <div class="grid gap-4">
            <div v-for="app in appendices" :key="app.id"
              class="rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm">
              <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                <div class="flex gap-4">
                  <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
                    <FileSignature class="h-6 w-6" />
                  </div>
                  <div>
                    <div class="flex flex-wrap items-center gap-2">
                      <div class="text-lg font-black text-slate-900">{{ app.appendixCode }}</div>
                      <span
                        class="rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700 ring-1 ring-emerald-200">
                        {{ app.status === 'ACTIVE' ? 'Hiệu lực' : 'Đã hủy' }}
                      </span>
                    </div>
                    <p class="mt-2 text-sm font-medium text-slate-600">{{ app.title }}</p>
                    <div class="mt-3 flex flex-wrap gap-3 text-xs font-medium text-slate-400">
                      <span>Hiệu lực {{ app.effectiveDate }}</span>
                      <span>Điều chỉnh {{ app.salaryChange }}</span>
                      <span>Ký bởi {{ app.signedBy }}</span>
                    </div>
                  </div>
                </div>

                <div class="flex gap-2">
                  <button
                    class="rounded-xl border border-slate-200 p-2.5 text-slate-400 transition hover:border-indigo-200 hover:text-indigo-600">
                    <Download class="h-4 w-4" />
                  </button>
                  <button
                    class="rounded-xl border border-slate-200 p-2.5 text-slate-400 transition hover:border-rose-200 hover:text-rose-600">
                    <XCircle class="h-4 w-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div id="attachments" class="space-y-4 scroll-mt-6">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-xl font-black text-slate-900">Tệp pháp lý đi kèm</h3>
              <p class="mt-1 text-sm text-slate-500">Lưu trữ file scan, phụ lục, bản ký và chứng từ đối chiếu</p>
            </div>
            <button
              class="flex items-center gap-2 rounded-2xl bg-slate-900 px-4 py-3 text-sm font-bold text-white transition hover:bg-slate-800"
              @click="openAttachmentModal">
              <Paperclip class="h-4 w-4" /> Đính kèm tài liệu
            </button>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div v-for="att in attachments" :key="att.id"
              class="rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm">
              <div class="flex items-start justify-between gap-4">
                <div class="flex gap-4">
                  <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
                    <Paperclip class="h-5 w-5" />
                  </div>
                  <div>
                    <div class="text-base font-black text-slate-900">{{ att.name }}</div>
                    <div class="mt-1 text-sm font-medium text-slate-500">{{ att.size }}</div>
                    <div class="mt-2 text-xs font-medium text-slate-400">Upload {{ att.uploadedAt }}</div>
                  </div>
                </div>

                <div class="flex gap-2">
                  <button
                    class="rounded-xl border border-slate-200 p-2.5 text-slate-400 transition hover:border-indigo-200 hover:text-indigo-600">
                    <Download class="h-4 w-4" />
                  </button>
                  <button
                    class="rounded-xl border border-slate-200 p-2.5 text-slate-400 transition hover:border-rose-200 hover:text-rose-600">
                    <Trash2 class="h-4 w-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div id="history" class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm scroll-mt-6">
          <div class="border-b border-slate-100 pb-4">
            <h3 class="text-xl font-black text-slate-900">Nhật ký chuyển trạng thái</h3>
            <p class="mt-1 text-sm text-slate-500">Mọi thay đổi đều được ghi nhận theo thời gian để phục vụ tra cứu</p>
          </div>

          <div class="relative mt-6">
            <div class="absolute left-5 top-3 bottom-3 w-px bg-slate-200" />
            <div class="space-y-5">
              <div v-for="item in history" :key="item.id" class="relative pl-14">
                <div class="absolute left-0 top-1 flex h-10 w-10 items-center justify-center rounded-2xl text-white"
                  :class="getStatusMeta(item.toStatus).accent">
                  <CheckCircle2 class="h-5 w-5" />
                </div>

                <div class="rounded-3xl border border-slate-100 bg-slate-50/70 p-5">
                  <div class="flex flex-wrap items-center gap-2">
                    <span v-if="item.fromStatus"
                      class="rounded-full bg-white px-2.5 py-1 text-xs font-bold text-slate-500 ring-1 ring-slate-200">
                      {{ item.fromStatus }}
                    </span>
                    <span v-if="item.fromStatus" class="text-slate-300">→</span>
                    <span class="rounded-full px-2.5 py-1 text-xs font-bold ring-1 ring-inset"
                      :class="getStatusMeta(item.toStatus).chip">
                      {{ getStatusMeta(item.toStatus).label }}
                    </span>
                  </div>

                  <p class="mt-3 text-sm font-medium leading-6 text-slate-600">{{ item.note }}</p>
                  <div class="mt-3 flex flex-wrap gap-3 text-xs font-medium text-slate-400">
                    <span>{{ item.actor }}</span>
                    <span>{{ item.timestamp }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="space-y-6 xl:sticky xl:top-6 xl:self-start">
        <div class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex items-center gap-2 border-b border-slate-100 pb-4">
            <Clock3 class="h-5 w-5 text-indigo-600" />
            <h3 class="text-lg font-black text-slate-900">Tiến trình hợp đồng</h3>
          </div>

          <div class="mt-5 space-y-4">
            <div v-for="(step, index) in statusFlow" :key="step.key" class="relative flex gap-4">
              <div class="relative flex w-11 shrink-0 flex-col items-center">
                <div class="flex h-11 w-11 items-center justify-center rounded-2xl ring-1 ring-inset" :class="index <= currentStepIdx
                  ? 'bg-slate-900 text-white ring-slate-900'
                  : 'bg-slate-50 text-slate-400 ring-slate-200'">
                  <component :is="step.icon" class="h-5 w-5" />
                </div>
                <div v-if="index < statusFlow.length - 1" class="mt-2 h-10 w-0.5 rounded-full"
                  :class="index < currentStepIdx ? 'bg-slate-900' : 'bg-slate-200'" />
              </div>

              <div class="flex-1 pb-5">
                <div class="text-sm font-black text-slate-900">{{ step.label }}</div>
                <div class="mt-1 text-sm text-slate-500">
                  {{ index <= currentStepIdx ? 'Đã hoàn tất trong vòng đời hợp đồng.' : 'Bước tiếp theo của hồ sơ.' }}
                    </div>
                </div>
              </div>
            </div>
          </div>

          <div class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
            <div class="flex items-center gap-2 border-b border-slate-100 pb-4">
              <UserRound class="h-5 w-5 text-indigo-600" />
              <h3 class="text-lg font-black text-slate-900">Chủ thể ký kết</h3>
            </div>

            <div class="mt-5 space-y-4">
              <div class="rounded-2xl bg-slate-50 p-4">
                <div class="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">Người lao động</div>
                <div class="mt-2 text-base font-black text-slate-900">{{ contract.employee }}</div>
                <div class="mt-1 text-sm font-medium text-slate-500">{{ contract.jobTitle }}</div>
              </div>
              <div class="rounded-2xl bg-slate-50 p-4">
                <div class="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">Đại diện doanh nghiệp</div>
                <div class="mt-2 text-base font-black text-slate-900">{{ contract.companyRepresentative }}</div>
                <div class="mt-1 text-sm font-medium text-slate-500">{{ contract.companyTitle }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showAppendixModal" ref="appendixOverlayRef"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 p-4 backdrop-blur-sm"
      @click.self="showAppendixModal = false">
      <div
        class="flex max-h-[min(820px,calc(100vh-2rem))] w-full max-w-2xl flex-col overflow-hidden rounded-[28px] bg-white shadow-2xl">
        <div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 pt-6 pb-4">
          <div>
            <h3 class="text-xl font-black text-slate-900">Tạo phụ lục hợp đồng</h3>
            <p class="mt-1 text-sm text-slate-500">Ghi nhận thay đổi phát sinh cho hợp đồng hiện tại</p>
          </div>
          <button class="rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
            @click="showAppendixModal = false">
            <XCircle class="h-5 w-5" />
          </button>
        </div>

        <form class="flex flex-1 flex-col overflow-hidden" @submit.prevent="handleCreateAppendix">
          <div class="mt-5 flex-1 space-y-4 overflow-y-auto px-6 pb-4">
            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Số phụ lục</label>
                <input ref="appendixFirstFieldRef" v-model="appendixForm.appendixNumber"
                  class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  placeholder="VD: PL-2026-001">
              </div>
              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Ngày hiệu lực</label>
                <input v-model="appendixForm.effectiveDate" type="date"
                  class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
              </div>
            </div>

            <div>
              <label class="mb-1 block text-sm font-semibold text-slate-600">Tên phụ lục</label>
              <input v-model="appendixForm.appendixName"
                class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                placeholder="VD: Phụ lục điều chỉnh lương">
            </div>

            <div>
              <label class="mb-1 block text-sm font-semibold text-slate-600">Nội dung thay đổi</label>
              <textarea v-model="appendixForm.changedFieldsJson"
                class="h-28 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                placeholder="Mô tả ngắn các trường hoặc điều khoản được điều chỉnh" />
            </div>

            <div>
              <label class="mb-1 block text-sm font-semibold text-slate-600">Ghi chú</label>
              <textarea v-model="appendixForm.note"
                class="h-24 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                placeholder="Ghi chú nội bộ cho phụ lục" />
            </div>
          </div>

          <div class="flex justify-end gap-3 border-t border-slate-100 bg-white px-6 py-4">
            <button type="button"
              class="rounded-2xl border border-slate-200 px-4 py-3 font-semibold text-slate-600 hover:bg-slate-50"
              @click="showAppendixModal = false">
              Hủy
            </button>
            <button type="submit" :disabled="appendixSubmitting"
              class="rounded-2xl bg-slate-900 px-5 py-3 font-bold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-70">
              {{ appendixSubmitting ? 'Đang tạo...' : 'Lưu phụ lục' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showAttachmentModal" ref="attachmentOverlayRef"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 p-4 backdrop-blur-sm"
      @click.self="showAttachmentModal = false">
      <div
        class="flex max-h-[min(820px,calc(100vh-2rem))] w-full max-w-2xl flex-col overflow-hidden rounded-[28px] bg-white shadow-2xl">
        <div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 pt-6 pb-4">
          <div>
            <h3 class="text-xl font-black text-slate-900">Thêm tài liệu đính kèm</h3>
            <p class="mt-1 text-sm text-slate-500">Lưu metadata tài liệu pháp lý cho hợp đồng</p>
          </div>
          <button class="rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
            @click="showAttachmentModal = false">
            <XCircle class="h-5 w-5" />
          </button>
        </div>

        <form class="flex flex-1 flex-col overflow-hidden" @submit.prevent="handleCreateAttachment">
          <div class="mt-5 flex-1 space-y-4 overflow-y-auto px-6 pb-4">
            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Loại tài liệu</label>
                <select ref="attachmentFirstFieldRef" v-model="attachmentForm.attachmentType"
                  class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                  <option v-for="item in attachmentTypeOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Tên file</label>
                <input v-model="attachmentForm.fileName"
                  class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  placeholder="VD: Hop_dong_NV001.pdf">
              </div>
            </div>

            <div>
              <label class="mb-1 block text-sm font-semibold text-slate-600">Đường dẫn lưu trữ</label>
              <input v-model="attachmentForm.storagePath"
                class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                placeholder="VD: contracts/2026/hdld-2026-001.pdf">
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">MIME type</label>
                <input v-model="attachmentForm.mimeType"
                  class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  placeholder="application/pdf">
              </div>
              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Dung lượng file (bytes)</label>
                <input v-model="attachmentForm.fileSizeBytes" type="number" min="0"
                  class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  placeholder="0">
              </div>
            </div>
          </div>

          <div class="flex justify-end gap-3 border-t border-slate-100 bg-white px-6 py-4">
            <button type="button"
              class="rounded-2xl border border-slate-200 px-4 py-3 font-semibold text-slate-600 hover:bg-slate-50"
              @click="showAttachmentModal = false">
              Hủy
            </button>
            <button type="submit" :disabled="attachmentSubmitting"
              class="rounded-2xl bg-slate-900 px-5 py-3 font-bold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-70">
              {{ attachmentSubmitting ? 'Đang lưu...' : 'Lưu tài liệu' }}
            </button>
          </div>
        </form>
      </div>
    </div>
</template>
