<script setup>
import { computed, ref, onBeforeUnmount, onMounted, watch } from 'vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'
import OnboardingCreateModal from '@/components/hrm/OnboardingCreateModal.vue'
import {
  UserPlus, Search, Plus, CheckCircle2, Clock, Users,
  FileText, Laptop, KeyRound, Mail, ClipboardList,
  ChevronRight, AlertCircle, CheckCheck, Eye,
  CalendarDays, Building2, Briefcase, ArrowRight,
  BadgeCheck, UserCheck, Send, FilePlus2, ListChecks,
  Loader2, MoreVertical, PencilLine
} from 'lucide-vue-next'
import {
  getOnboardings, getOnboardingDetail,
  completeOnboarding, notifyOnboarding,
  createOnboarding,
  createUserForOnboarding,
  upsertOnboardingChecklist
} from '@/api/admin/onboarding.js'
import { getOrgUnits } from '@/api/admin/orgUnit'
import { getJobTitles } from '@/api/admin/jobTitle'
import { getEmployees } from '@/api/admin/employee'
import { Phone, FileWarning } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import { useUiStore } from '@/stores/ui'
import { safeArray, unwrapData, unwrapPage } from '@/utils/api'

/* ------------------ CONFIG ------------------ */

const statusConfig = {
  DRAFT: { label: 'Nháp', color: 'slate', dot: 'bg-slate-400', badge: 'bg-slate-100 text-slate-700 border-slate-200', order: 0 },
  IN_PROGRESS: { label: 'Đang chuẩn bị', color: 'amber', dot: 'bg-amber-400', badge: 'bg-amber-100 text-amber-700 border-amber-200', order: 1 },
  READY_FOR_JOIN: { label: 'Sẵn sàng làm', color: 'indigo', dot: 'bg-indigo-400', badge: 'bg-indigo-100 text-indigo-700 border-indigo-200', order: 2 },
  COMPLETED: { label: 'Hoàn tất', color: 'emerald', dot: 'bg-emerald-400', badge: 'bg-emerald-100 text-emerald-700 border-emerald-200', order: 3 },
  CANCELLED: { label: 'Đã hủy', color: 'rose', dot: 'bg-rose-400', badge: 'bg-rose-100 text-rose-700 border-rose-200', order: -1 },
}

const stepDefs = [
  { key: 'checklist', icon: ListChecks, label: 'Checklist chuẩn bị', desc: 'Cấp phát tài sản, tài liệu đầu vào', status: 'IN_PROGRESS' },
  { key: 'orientation', icon: BadgeCheck, label: 'Xác nhận Orientation', desc: 'Manager xác nhận định hướng nhập môn', status: 'IN_PROGRESS' },
  { key: 'account', icon: KeyRound, label: 'Tạo tài khoản hệ thống', desc: 'Cấp username / email công ty', status: 'READY_FOR_JOIN' },
  { key: 'contract', icon: FilePlus2, label: 'Tạo hợp đồng đầu tiên', desc: 'Hợp đồng thử việc / chính thức', status: 'READY_FOR_JOIN' },
  { key: 'notify', icon: Send, label: 'Gửi thông báo chào mừng', desc: 'Email Welcome đến ứng viên & IT Desk', status: 'READY_FOR_JOIN' },
  { key: 'complete', icon: CheckCheck, label: 'Chốt hoàn tất Onboarding', desc: 'Tất cả checklist bắt buộc phải xong', status: 'COMPLETED' },
]


const toast = useToast()
const ui = useUiStore()

const records = ref([])
const loading = ref(false)
const searchQuery = ref('')
const activeStatus = ref('ALL')
const selectedRecord = ref(null)
const showDetailPanel = ref(false)
const showCreateModal = ref(false)
const showChecklistModal = ref(false)
const createLoading = ref(false)
const createReferenceLoading = ref(false)
const checklistSubmitting = ref(false)
const checklistEditingId = ref(null)
const orgUnitOptions = ref([])
const jobTitleOptions = ref([])
const managerOptions = ref([])

const statsData = ref([
  { title: 'Đang chuẩn bị', value: '0', icon: Clock, color: 'amber', trend: 0 },
  { title: 'Sẵn sàng làm việc', value: '0', icon: UserCheck, color: 'indigo', trend: 0 },
  { title: 'Hoàn tất tháng này', value: '0', icon: CheckCircle2, color: 'emerald', trend: 0 },
  { title: 'Tổng tiếp nhận', value: '0', icon: UserPlus, color: 'rose' },
])

const checklistTemplates = [
  {
    itemCode: 'ORIENTATION_DONE',
    itemName: 'Hoàn tất orientation',
    required: true,
    note: 'Đã giới thiệu team và công việc.',
  },
  {
    itemCode: 'WORKPLACE_READY',
    itemName: 'Bàn giao chỗ ngồi và thiết bị',
    required: true,
    note: 'Đã cấp laptop và thẻ nhân viên.',
  },
  {
    itemCode: 'EMAIL_READY',
    itemName: 'Tạo email và quyền hệ thống',
    required: true,
    note: 'IT xác nhận email công ty và quyền truy cập cơ bản.',
  },
  {
    itemCode: 'WELCOME_SENT',
    itemName: 'Gửi thư chào mừng',
    required: false,
    note: 'Gửi email welcome cho nhân sự mới và manager.',
  },
]

const checklistForm = ref({
  itemCode: '',
  itemName: '',
  required: true,
  completed: false,
  dueDate: '',
  note: '',
})

const checklistStats = computed(() => {
  const items = safeArray(selectedRecord.value?.checklistItems)
  const completed = items.filter((item) => item.completed).length
  const required = items.filter((item) => item.required).length
  const overdue = items.filter((item) => !item.completed && item.dueDate && item.dueDate < new Date().toISOString().slice(0, 10)).length
  return {
    total: items.length,
    completed,
    open: items.length - completed,
    required,
    overdue,
  }
})

let lockedScrollY = 0

/*  API CALL  */
const fetchOnboardings = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchQuery.value,
      status: activeStatus.value !== 'ALL' ? activeStatus.value : undefined
    }
    const response = await getOnboardings(params)
    records.value = unwrapPage(response).items

    // Quick stats calc
    const inProgress = records.value.filter(r => r.status === 'IN_PROGRESS').length
    const ready = records.value.filter(r => r.status === 'READY_FOR_JOIN').length
    const completed = records.value.filter(r => r.status === 'COMPLETED').length

    statsData.value[0].value = inProgress.toString()
    statsData.value[1].value = ready.toString()
    statsData.value[2].value = completed.toString()
    statsData.value[3].value = records.value.length.toString()
  } catch (error) {
    console.error('Failed to fetch onboardings:', error)
  } finally {
    loading.value = false
  }
}

const fetchCreateReferences = async () => {
  createReferenceLoading.value = true
  try {
    const [orgUnitsResponse, jobTitlesResponse, managersResponse] = await Promise.all([
      getOrgUnits({ size: 100 }),
      getJobTitles({ size: 100 }),
      getEmployees({ size: 100 }),
    ])
    orgUnitOptions.value = unwrapPage(orgUnitsResponse).items
    jobTitleOptions.value = unwrapPage(jobTitlesResponse).items
    managerOptions.value = unwrapPage(managersResponse).items
  } catch (error) {
    console.error('Failed to fetch onboarding references:', error)
    toast.error('Không thể tải dữ liệu tham chiếu để tạo onboarding')
  } finally {
    createReferenceLoading.value = false
  }
}

onMounted(fetchOnboardings)

watch([searchQuery, activeStatus], () => {
  fetchOnboardings()
})

function syncBodyScrollLock() {
  if (typeof document === 'undefined' || typeof window === 'undefined') return

  const shouldLock = showCreateModal.value || showDetailPanel.value || showChecklistModal.value
  const { body, documentElement } = document

  if (shouldLock) {
    if (!body.dataset.scrollLocked) {
      lockedScrollY = window.scrollY
      body.dataset.scrollLocked = 'true'
      body.style.position = 'fixed'
      body.style.top = `-${lockedScrollY}px`
      body.style.left = '0'
      body.style.right = '0'
      body.style.width = '100%'
      body.style.overflow = 'hidden'
      documentElement.style.overflow = 'hidden'
    }
    return
  }

  if (body.dataset.scrollLocked) {
    delete body.dataset.scrollLocked
    body.style.position = ''
    body.style.top = ''
    body.style.left = ''
    body.style.right = ''
    body.style.width = ''
    body.style.overflow = ''
    documentElement.style.overflow = ''
    window.scrollTo(0, lockedScrollY)
  }
}

watch([showCreateModal, showDetailPanel, showChecklistModal], syncBodyScrollLock)

onBeforeUnmount(() => {
  if (typeof document !== 'undefined') {
    const { body, documentElement } = document
    delete body.dataset.scrollLocked
    body.style.position = ''
    body.style.top = ''
    body.style.left = ''
    body.style.right = ''
    body.style.width = ''
    body.style.overflow = ''
    documentElement.style.overflow = ''
    if (typeof window !== 'undefined') {
      window.scrollTo(0, lockedScrollY)
    }
  }
})

function getStepState(record, step) {
  const statusOrder = ['DRAFT', 'IN_PROGRESS', 'READY_FOR_JOIN', 'COMPLETED', 'CANCELLED']
  const currentIdx = statusOrder.indexOf(record.status)
  const requiredIdx = statusOrder.indexOf(step.status)

  // Logic simplified based on status mapping
  if (currentIdx >= requiredIdx) return 'done'
  if (currentIdx === requiredIdx - 1) return 'active'
  return 'pending'
}

function resetChecklistForm() {
  checklistEditingId.value = null
  checklistForm.value = {
    itemCode: '',
    itemName: '',
    required: true,
    completed: false,
    dueDate: '',
    note: '',
  }
}

function openChecklistModal(item = null) {
  if (item) {
    checklistEditingId.value = item.onboardingChecklistId
    checklistForm.value = {
      itemCode: item.itemCode || '',
      itemName: item.itemName || '',
      required: Boolean(item.required),
      completed: Boolean(item.completed),
      dueDate: item.dueDate || '',
      note: item.note || '',
    }
  } else {
    resetChecklistForm()
  }
  showChecklistModal.value = true
}

async function refreshSelectedRecord() {
  if (!selectedRecord.value?.onboardingId) return
  const response = await getOnboardingDetail(selectedRecord.value.onboardingId)
  selectedRecord.value = unwrapData(response)
}

async function submitChecklistForm() {
  if (!selectedRecord.value?.onboardingId) return
  if (!checklistForm.value.itemCode || !checklistForm.value.itemName) {
    toast.warning('Vui lòng nhập mã checklist và tên đầu việc')
    return
  }

  const normalizedCode = checklistForm.value.itemCode.trim().toUpperCase()
  const duplicated = safeArray(selectedRecord.value.checklistItems).some(
    (item) => item.itemCode === normalizedCode && item.onboardingChecklistId !== checklistEditingId.value,
  )
  if (duplicated) {
    toast.warning('Mã checklist đã tồn tại trong hồ sơ onboarding này')
    return
  }

  checklistSubmitting.value = true
  try {
    await upsertOnboardingChecklist(
      selectedRecord.value.onboardingId,
      checklistEditingId.value,
      {
        itemCode: normalizedCode,
        itemName: checklistForm.value.itemName,
        required: checklistForm.value.required,
        completed: checklistForm.value.completed,
        dueDate: checklistForm.value.dueDate || null,
        note: checklistForm.value.note || null,
      },
    )
    toast.success(checklistEditingId.value ? 'Đã cập nhật checklist' : 'Đã thêm checklist mới')
    await refreshSelectedRecord()
    resetChecklistForm()
  } catch (error) {
    console.error('Failed to submit checklist:', error)
    toast.error(error.response?.data?.message || 'Không thể lưu checklist')
  } finally {
    checklistSubmitting.value = false
  }
}

function applyChecklistTemplate(template) {
  checklistEditingId.value = null
  checklistForm.value = {
    itemCode: template.itemCode,
    itemName: template.itemName,
    required: template.required,
    completed: false,
    dueDate: selectedRecord.value?.plannedStartDate || '',
    note: template.note,
  }
  showChecklistModal.value = true
}

async function toggleChecklistCompleted(item) {
  if (!selectedRecord.value?.onboardingId) return
  checklistSubmitting.value = true
  try {
    await upsertOnboardingChecklist(
      selectedRecord.value.onboardingId,
      item.onboardingChecklistId,
      {
        itemCode: item.itemCode,
        itemName: item.itemName,
        required: item.required,
        completed: !item.completed,
        dueDate: item.dueDate || null,
        note: item.note || null,
      },
    )
    toast.success(!item.completed ? 'Đã đánh dấu hoàn tất checklist' : 'Đã mở lại checklist')
    await refreshSelectedRecord()
  } catch (error) {
    console.error('Failed to toggle checklist:', error)
    toast.error(error.response?.data?.message || 'Không thể cập nhật checklist')
  } finally {
    checklistSubmitting.value = false
  }
}

async function openDetail(record) {
  loading.value = true
  try {
    const response = await getOnboardingDetail(record.onboardingId)
    selectedRecord.value = unwrapData(response)
    showDetailPanel.value = true
  } catch (error) {
    console.error('Failed to get detail:', error)
  } finally {
    loading.value = false
  }
}

async function handleComplete(record) {
  try {
    await completeOnboarding(record.onboardingId, { note: 'Hoàn tất onboarding từ dashboard HR.' })
    await fetchOnboardings()
    showDetailPanel.value = false
  } catch (error) {
    console.error('Failed to complete onboarding:', error)
  }
}

async function handleNotify(record) {
  try {
    await notifyOnboarding(record.onboardingId, {
      notifyNewHire: true,
      notifyManager: true,
      notifyLinkedUser: true,
      customRecipientEmails: [],
      note: 'Gửi thông báo chào mừng từ dashboard HR.',
    })
    toast.success('Đã gửi thông báo chào mừng')
    await openDetail(record) // Refresh detail
  } catch (error) {
    console.error('Failed to notify:', error)
    toast.error('Gửi thông báo thất bại')
  }
}

async function handleStepProcess(step) {
  const record = selectedRecord.value
  if (!record) return

  try {
    if (step.key === 'account') {
      const confirmed = await ui.confirm({
        title: 'Tạo tài khoản hệ thống',
        message: `Hệ thống sẽ tự động tạo Username và Email công ty cho ${record.fullName}. Bạn có chắc chắn?`,
        confirmLabel: 'Tạo ngay'
      })
      if (!confirmed) return
      const username = record.fullName
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-z0-9]+/g, '.')
        .replace(/^\.+|\.+$/g, '')
        .slice(0, 30) || `onboard.${record.onboardingId}`

      await createUserForOnboarding(record.onboardingId, {
        username,
        initialPassword: 'P@ssword123',
        sendSetupEmail: true,
      })
      toast.success('Đã tạo tài khoản thành công')
    }
    else if (step.key === 'contract') {
      toast.info('Màn HR đã hiển thị đúng dữ liệu onboarding. Flow tạo hợp đồng cần thêm thông tin lương và loại hợp đồng nên sẽ được xử lý ở bước tạo hợp đồng riêng.')
      return
    }
    else if (step.key === 'checklist') {
      openChecklistModal()
      return
    }
    else if (step.key === 'notify') {
      await handleNotify(record)
      return
    }
    else if (step.key === 'complete') {
      await handleComplete(record)
      return
    }

    await openDetail(record) // Refresh
  } catch (error) {
    toast.error('Xử lý thất bại: ' + (error.response?.data?.message || error.message))
  }
}

function initials(name) {
  return name ? name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) : '??'
}

function recordStatus(record) {
  return record?.status || 'DRAFT'
}

async function openCreateModal() {
  showCreateModal.value = true
  if (!orgUnitOptions.value.length || !jobTitleOptions.value.length || !managerOptions.value.length) {
    await fetchCreateReferences()
  }
}

async function submitCreateOnboarding(payload) {
  createLoading.value = true
  try {
    const response = await createOnboarding({
      onboardingCode: payload.onboardingCode,
      fullName: payload.fullName,
      personalEmail: payload.personalEmail,
      personalPhone: payload.personalPhone,
      genderCode: payload.genderCode,
      dateOfBirth: payload.dateOfBirth,
      plannedStartDate: payload.plannedStartDate,
      employeeCode: payload.employeeCode,
      orgUnitId: Number(payload.orgUnitId),
      jobTitleId: Number(payload.jobTitleId),
      managerEmployeeId: payload.managerEmployeeId ? Number(payload.managerEmployeeId) : null,
      workLocation: payload.workLocation,
      note: payload.note,
    })
    const created = unwrapData(response)
    toast.success('Đã tạo hồ sơ onboarding mới')
    showCreateModal.value = false
    await fetchOnboardings()
    if (created?.onboardingId) {
      await openDetail(created)
    }
  } catch (error) {
    console.error('Failed to create onboarding:', error)
    toast.error(error.response?.data?.message || 'Tạo hồ sơ onboarding thất bại')
  } finally {
    createLoading.value = false
  }
}

</script>

<template>

  <div class="space-y-8 animate-fade-in">

    <!-- HEADER -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
      <div>
        <div class="flex items-center gap-4 mb-2">
          <div
            class="w-12 h-12 rounded-[18px] bg-indigo-600 flex items-center justify-center shadow-xl shadow-indigo-100">
            <UserPlus class="w-6 h-6 text-white" />
          </div>
          <h2 class="text-4xl font-black text-slate-900 tracking-tight">Tiếp nhận Nhân sự</h2>
        </div>
        <p class="text-slate-500 font-medium ml-1">Chuẩn bị lộ trình nhập môn cho thành viên mới</p>
      </div>

      <BaseButton variant="primary" size="lg" shadow class="rounded-2xl! px-6! h-12.5! font-bold"
        @click="openCreateModal">
        <Plus class="w-5 h-5 mr-2" /> Tạo hồ sơ Onboarding
      </BaseButton>
    </div>

    <!-- WORKFLOW DIAGRAM -->
    <div class="grid grid-cols-1 xl:grid-cols-12 gap-6 items-start">
      <GlassCard :glass="false" class="xl:col-span-12 bg-white border border-slate-100 rounded-4xl p-6">
        <div class="flex flex-col lg:flex-row items-center gap-8 justify-between">
          <div class="lg:max-w-xs">
            <h3 class="text-lg font-black text-slate-900 mb-1">Quy trình chuyên môn</h3>
            <p class="text-xs text-slate-400 font-medium">Theo tiêu chuẩn ISO-HRM của tập đoàn</p>
          </div>

          <div class="flex-1 flex flex-wrap justify-center lg:justify-end items-center gap-4 py-2">
            <template v-for="(step, idx) in [
              { label: 'Khởi tạo', sub: 'Hồ sơ nháp', icon: FilePlus2, color: 'bg-slate-100 text-slate-500' },
              { label: 'Chuẩn bị', sub: 'Checklist & Asset', icon: Laptop, color: 'bg-amber-100 text-amber-600' },
              { label: 'Sẵn sàng', sub: 'TK & Hợp đồng', icon: KeyRound, color: 'bg-indigo-100 text-indigo-600' },
              { label: 'Hoàn tất', sub: 'Nhân viên chính thức', icon: BadgeCheck, color: 'bg-emerald-100 text-emerald-600' },
            ]" :key="idx">
              <div class="flex flex-col items-center gap-2 group cursor-help">
                <div
                  :class="['w-14 h-14 rounded-2xl flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform', step.color]">
                  <component :is="step.icon" class="w-6 h-6" />
                </div>
                <div class="text-center">
                  <p class="text-[11px] font-black uppercase tracking-wider text-slate-800">{{ step.label }}</p>
                  <p class="text-[9px] text-slate-400 font-bold whitespace-nowrap">{{ step.sub }}</p>
                </div>
              </div>
              <ArrowRight v-if="idx < 3" class="w-5 h-5 text-slate-200" />
            </template>
          </div>
        </div>
      </GlassCard>
    </div>

    <!-- STATS -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <StatCard v-for="s in statsData" :key="s.title" :title="s.title" :value="s.value" :icon="s.icon" :color="s.color"
        :trend="s.trend" class="rounded-4xl!" />
    </div>

    <!-- LIST PANEL -->
    <div class="bg-white border border-slate-100 rounded-4xl overflow-hidden shadow-sm relative min-h-125">

      <!-- Loading Overlay -->
      <div v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center bg-white/60 backdrop-blur-[1px]">
        <Loader2 class="w-10 h-10 text-indigo-600 animate-spin" />
      </div>

      <!-- Toolbar -->
      <div class="px-8 py-6 border-b border-slate-50 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
        <div class="flex items-center gap-2 p-1 bg-slate-50 rounded-2xl">
          <button v-for="tab in [
            { id: 'ALL', label: 'Tất cả' },
            { id: 'IN_PROGRESS', label: 'Đang chuẩn bị' },
            { id: 'READY_FOR_JOIN', label: 'Sẵn sàng' },
            { id: 'COMPLETED', label: 'Hoàn tất' }
          ]" :key="tab.id" @click="activeStatus = tab.id"
            class="px-5 py-2 rounded-xl text-xs font-black transition-all"
            :class="activeStatus === tab.id ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'">
            {{ tab.label }}
          </button>
        </div>

        <div class="relative group">
          <Search
            class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
          <input v-model="searchQuery" type="text" placeholder="Tìm tên hoặc mã hồ sơ..."
            class="w-72 pl-11 pr-4 py-3 bg-slate-50 border border-transparent rounded-2xl text-xs font-bold focus:bg-white focus:border-indigo-100 focus:ring-4 focus:ring-indigo-500/5 transition-all outline-none" />
        </div>
      </div>

      <!-- Content -->
      <div v-if="records.length > 0" class="divide-y divide-slate-50">
        <div v-for="rec in records" :key="rec.onboardingId"
          class="group px-8 py-6 hover:bg-slate-50/50 transition-all cursor-pointer flex flex-col xl:flex-row xl:items-center justify-between gap-6"
          @click="openDetail(rec)">
          <div class="flex items-start gap-5 min-w-75">
            <AvatarBox :name="rec.fullName" :image="rec.avatarUrl" size="lg" shape="rounded-[20px]" class="shadow-sm" />
            <div>
              <div class="flex items-center gap-2 mb-1.5 text-xs font-black">
                <span class="text-slate-400 group-hover:text-indigo-600 transition-colors uppercase tracking-widest">{{
                  rec.onboardingCode }}</span>
                <div class="w-1 h-1 rounded-full bg-slate-300"></div>
                <span class="text-slate-500">{{ rec.jobTitleName }}</span>
              </div>
              <h4 class="text-lg font-black text-slate-900 mb-2">{{ rec.fullName }}</h4>
              <div class="flex flex-wrap gap-2">
                <span
                  class="px-2 py-1 bg-slate-100 text-slate-600 rounded-lg text-[10px] font-black uppercase tracking-tighter">
                  {{ rec.orgUnitName }}
                </span>
                <div
                  class="flex items-center gap-1.5 px-2 py-1 bg-indigo-50 text-indigo-700 rounded-lg text-[10px] font-black">
                  <CalendarDays class="w-3 h-3" />
                  Bắt đầu: {{ rec.plannedStartDate }}
                </div>
              </div>
            </div>
          </div>

          <!-- Steps Mini Visual -->
          <div class="flex-1 flex justify-center xl:justify-start items-center gap-1.5">
            <div v-for="step in stepDefs" :key="step.key"
              class="w-8 h-8 rounded-xl flex items-center justify-center transition-all bg-slate-50 text-slate-300"
              :class="{
                'bg-emerald-50 text-emerald-600 border border-emerald-100 shadow-sm shadow-emerald-100/50': getStepState(rec, step) === 'done',
                'bg-indigo-50 text-indigo-600 ring-2 ring-indigo-200 scale-110 z-10 shadow-lg shadow-indigo-100': getStepState(rec, step) === 'active'
              }">
              <component :is="step.icon" class="w-4 h-4" />
            </div>
          </div>

          <!-- Side actions -->
          <div class="flex items-center gap-6 justify-between xl:justify-end">
            <StatusBadge :status="recordStatus(rec)" />
            <button
              class="p-2 text-slate-300 hover:text-indigo-600 bg-slate-50 hover:bg-indigo-50 rounded-xl transition-all">
              <chevron-right class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      <EmptyState v-else-if="!loading" title="Không tìm thấy hồ sơ Onboarding"
        description="Có vẻ như không có nhân sự mới nào trong danh sách hiện tại của bạn." iconName="UserPlus" />
    </div>

  </div>

  <OnboardingCreateModal :open="showCreateModal" :submitting="createLoading" :reference-loading="createReferenceLoading"
    :org-unit-options="orgUnitOptions" :job-title-options="jobTitleOptions" :manager-options="managerOptions"
    @close="showCreateModal = false" @submit="submitCreateOnboarding" />

  <Teleport to="body">
    <Transition name="slide-panel">
      <div v-if="showChecklistModal && selectedRecord"
        class="fixed inset-0 z-100 overflow-y-auto bg-slate-950/45 p-4 sm:p-6" @click.self="showChecklistModal = false">
        <div
          class="relative z-10 mx-auto my-6 w-full max-w-4xl overflow-hidden rounded-4xl border border-slate-200 bg-white shadow-[0_18px_40px_rgba(15,23,42,0.14)]">
          <div class="grid gap-0 lg:grid-cols-[1.1fr_0.9fr]">
            <div class="border-r border-slate-200 p-8">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <p class="text-[11px] font-black uppercase tracking-[0.22em] text-indigo-500">Checklist Processing</p>
                  <h3 class="mt-3 text-3xl font-black tracking-tight text-slate-900">Xử lý checklist onboarding</h3>
                  <p class="mt-2 text-sm font-medium text-slate-500">
                    Thêm đầu việc chuẩn bị, chỉnh sửa item hiện có và chốt hoàn thành ngay trong luồng tiếp nhận.
                  </p>
                </div>
                <button type="button"
                  class="flex h-10 w-10 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 transition-all hover:border-rose-200 hover:bg-rose-50 hover:text-rose-500"
                  @click="showChecklistModal = false">
                  ×
                </button>
              </div>

              <div class="mt-8 rounded-[28px] border border-slate-200 bg-slate-50 p-5">
                <h4 class="text-lg font-black text-slate-900">
                  {{ checklistEditingId ? 'Cập nhật đầu việc' : 'Thêm đầu việc mới' }}
                </h4>
                <div class="mt-4 flex flex-wrap gap-2">
                  <button v-for="template in checklistTemplates" :key="`modal-${template.itemCode}`" type="button"
                    class="rounded-full border border-slate-200 bg-white px-3 py-1.5 text-xs font-black text-slate-700 transition-all hover:border-indigo-200 hover:text-indigo-700"
                    @click="applyChecklistTemplate(template)">
                    {{ template.itemName }}
                  </button>
                </div>
                <div class="mt-5 grid gap-4 md:grid-cols-2">
                  <label class="block">
                    <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Mã checklist</span>
                    <input v-model="checklistForm.itemCode" type="text"
                      class="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10"
                      placeholder="Ví dụ: LAPTOP_READY" />
                  </label>

                  <label class="block">
                    <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Hạn xử lý</span>
                    <input v-model="checklistForm.dueDate" type="date"
                      class="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10" />
                  </label>

                  <label class="block md:col-span-2">
                    <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Tên đầu việc</span>
                    <input v-model="checklistForm.itemName" type="text"
                      class="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10"
                      placeholder="Ví dụ: Chuẩn bị laptop và email công ty" />
                  </label>

                  <label class="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-3">
                    <input v-model="checklistForm.required" type="checkbox"
                      class="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500" />
                    <span class="text-sm font-bold text-slate-700">Đầu việc bắt buộc</span>
                  </label>

                  <label class="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-3">
                    <input v-model="checklistForm.completed" type="checkbox"
                      class="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500" />
                    <span class="text-sm font-bold text-slate-700">Đánh dấu đã hoàn tất</span>
                  </label>

                  <label class="block md:col-span-2">
                    <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Ghi chú</span>
                    <textarea v-model="checklistForm.note" rows="4"
                      class="mt-2 w-full rounded-[24px] border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10"
                      placeholder="Ghi chú cho HR/IT/manager nếu cần" />
                  </label>
                </div>

                <div class="mt-5 flex flex-wrap gap-3">
                  <BaseButton variant="outline" @click="resetChecklistForm">Làm mới</BaseButton>
                  <BaseButton variant="primary" :loading="checklistSubmitting" @click="submitChecklistForm">
                    <CheckCheck class="mr-2 h-4 w-4" />
                    {{ checklistEditingId ? 'Lưu cập nhật' : 'Thêm checklist' }}
                  </BaseButton>
                </div>
              </div>
            </div>

            <div class="bg-slate-950 p-8 text-white">
              <p class="text-[11px] font-black uppercase tracking-[0.22em] text-indigo-200">Current Checklist</p>
              <h4 class="mt-3 text-2xl font-black">{{ selectedRecord.fullName }}</h4>
              <p class="mt-2 text-sm font-medium text-slate-300">
                Theo dõi các đầu việc chuẩn bị trước ngày bắt đầu và cập nhật tiến độ ngay trong hồ sơ onboarding.
              </p>

              <div class="mt-5 grid grid-cols-2 gap-3">
                <div class="rounded-[22px] border border-white/10 bg-white/5 p-4">
                  <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Đã xong</p>
                  <p class="mt-2 text-2xl font-black text-white">{{ checklistStats.completed }}</p>
                </div>
                <div class="rounded-[22px] border border-white/10 bg-white/5 p-4">
                  <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Đang mở</p>
                  <p class="mt-2 text-2xl font-black text-white">{{ checklistStats.open }}</p>
                </div>
              </div>

              <div class="mt-6 space-y-4">
                <div v-for="item in safeArray(selectedRecord.checklistItems)" :key="item.onboardingChecklistId"
                  class="rounded-[24px] border border-white/10 bg-white/5 p-4">
                  <div class="flex items-start justify-between gap-3">
                    <div>
                      <p class="text-sm font-black text-white">{{ item.itemName }}</p>
                      <p class="mt-1 text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">{{ item.itemCode
                        }}</p>
                    </div>
                    <span class="rounded-full px-2.5 py-1 text-[10px] font-black uppercase tracking-[0.14em]"
                      :class="item.completed ? 'bg-emerald-500/15 text-emerald-200' : 'bg-amber-500/15 text-amber-200'">
                      {{ item.completed ? 'Done' : 'Open' }}
                    </span>
                  </div>
                  <p v-if="item.note" class="mt-3 text-sm font-medium text-slate-300">{{ item.note }}</p>
                  <p v-if="item.completedAt || item.completedByUsername"
                    class="mt-2 text-xs font-bold text-emerald-300">
                    {{ item.completedByUsername || 'Hệ thống' }} xác nhận hoàn tất
                  </p>
                  <div class="mt-3 flex flex-wrap gap-2">
                    <span v-if="item.required"
                      class="rounded-full bg-rose-500/15 px-2.5 py-1 text-[10px] font-black uppercase tracking-[0.14em] text-rose-200">
                      Required
                    </span>
                    <span v-if="item.dueDate"
                      class="rounded-full bg-white/8 px-2.5 py-1 text-[10px] font-black uppercase tracking-[0.14em] text-slate-300">
                      Due {{ item.dueDate }}
                    </span>
                  </div>
                </div>

                <div v-if="!safeArray(selectedRecord.checklistItems).length"
                  class="rounded-[24px] border border-dashed border-white/15 bg-white/[0.03] p-5 text-sm font-medium text-slate-300">
                  Chưa có checklist nào. Hãy tạo item đầu tiên để mở quy trình chuẩn bị onboarding.
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- DETAIL PANEL -->
  <Teleport to="body">
    <Transition name="slide-panel">
      <div v-if="showDetailPanel && selectedRecord" class="fixed inset-0 z-100 flex justify-end">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-md" @click="showDetailPanel = false" />

        <div class="relative bg-white w-full max-w-2xl h-full shadow-2xl flex flex-col animate-slide-left">

          <!-- Header -->
          <div class="p-8 border-b border-slate-100 shrink-0">
            <div class="flex items-start justify-between mb-8">
              <div class="flex items-center gap-4">
                <div
                  class="w-14 h-14 rounded-3xl bg-indigo-50 text-indigo-600 flex items-center justify-center font-black text-xl shadow-inner">
                  {{ initials(selectedRecord.fullName) }}
                </div>
                <div>
                  <span class="text-xs font-black text-slate-400 uppercase tracking-widest">{{
                    selectedRecord.onboardingCode }}</span>
                  <h3 class="text-2xl font-black text-slate-900">{{ selectedRecord.fullName }}</h3>
                </div>
              </div>
              <button @click="showDetailPanel = false"
                class="w-10 h-10 rounded-2xl bg-slate-50 text-slate-400 hover:text-rose-500 hover:bg-rose-50 transition-all flex items-center justify-center font-bold">×</button>
            </div>

            <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Chức danh</p>
                <p class="text-xs font-bold text-slate-800">{{ selectedRecord.jobTitleName }}</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Phòng ban</p>
                <p class="text-xs font-bold text-slate-800">{{ selectedRecord.orgUnitName }}</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Dự kiến tham gia</p>
                <p class="text-xs font-bold text-indigo-600">{{ selectedRecord.plannedStartDate }}</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Manager</p>
                <p class="text-xs font-bold text-slate-800">{{ selectedRecord.managerFullName || 'Chưa gán' }}</p>
              </div>
            </div>
          </div>

          <!-- Tabs Body -->
          <div class="flex-1 overflow-y-auto p-8 space-y-8">

            <!-- Checklist -->
            <div>
              <div class="mb-4 flex items-start justify-between gap-4">
                <div>
                  <h4 class="text-sm font-black text-slate-900 uppercase tracking-widest flex items-center gap-2">
                    <ListChecks class="w-4 h-4 text-indigo-600" /> Checklist tiến độ
                  </h4>
                  <p class="mt-2 text-sm font-medium text-slate-500">
                    {{ checklistStats.completed }}/{{ checklistStats.total || 0 }} đầu việc đã hoàn tất, còn {{
                    checklistStats.open }} đầu việc đang mở.
                  </p>
                </div>
                <BaseButton variant="outline" size="sm" @click="openChecklistModal()">
                  <Plus class="mr-2 h-4 w-4" />
                  Thêm checklist
                </BaseButton>
              </div>

              <div class="mb-4 flex flex-wrap gap-2">
                <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-black text-slate-700">
                  Tổng {{ checklistStats.total }}
                </span>
                <span class="rounded-full bg-emerald-50 px-3 py-1 text-xs font-black text-emerald-700">
                  Đã xong {{ checklistStats.completed }}
                </span>
                <span class="rounded-full bg-amber-50 px-3 py-1 text-xs font-black text-amber-700">
                  Đang mở {{ checklistStats.open }}
                </span>
                <span class="rounded-full bg-rose-50 px-3 py-1 text-xs font-black text-rose-700">
                  Bắt buộc {{ checklistStats.required }}
                </span>
                <span v-if="checklistStats.overdue"
                  class="rounded-full bg-rose-100 px-3 py-1 text-xs font-black text-rose-700">
                  Quá hạn {{ checklistStats.overdue }}
                </span>
              </div>

              <div class="mb-4 flex flex-wrap gap-2">
                <button v-for="template in checklistTemplates" :key="template.itemCode" type="button"
                  class="rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1.5 text-xs font-black text-indigo-700 transition-all hover:border-indigo-300 hover:bg-indigo-100"
                  @click="applyChecklistTemplate(template)">
                  + {{ template.itemName }}
                </button>
              </div>
              <div v-if="safeArray(selectedRecord.checklistItems).length"
                class="mb-4 space-y-3 rounded-[28px] border border-slate-200 bg-slate-50 p-4">
                <div v-for="item in safeArray(selectedRecord.checklistItems)" :key="item.onboardingChecklistId"
                  class="flex flex-col gap-3 rounded-[24px] bg-white p-4 shadow-sm shadow-slate-200/60 md:flex-row md:items-center md:justify-between">
                  <div>
                    <div class="flex flex-wrap items-center gap-2">
                      <p class="text-sm font-black text-slate-900">{{ item.itemName }}</p>
                      <span class="rounded-full px-2.5 py-1 text-[10px] font-black uppercase tracking-[0.14em]"
                        :class="item.completed ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'">
                        {{ item.completed ? 'Đã xong' : 'Đang mở' }}
                      </span>
                      <span v-if="item.required"
                        class="rounded-full bg-rose-50 px-2.5 py-1 text-[10px] font-black uppercase tracking-[0.14em] text-rose-700">
                        Bắt buộc
                      </span>
                    </div>
                    <p class="mt-1 text-xs font-bold uppercase tracking-[0.14em] text-slate-400">{{ item.itemCode }}</p>
                    <p v-if="item.note" class="mt-2 text-sm font-medium text-slate-500">{{ item.note }}</p>
                    <p v-if="item.dueDate" class="mt-2 text-xs font-bold"
                      :class="!item.completed && item.dueDate < new Date().toISOString().slice(0, 10) ? 'text-rose-500' : 'text-slate-400'">
                      Hạn xử lý: {{ item.dueDate }}
                    </p>
                    <p v-if="item.completedAt || item.completedByUsername"
                      class="mt-1 text-xs font-bold text-emerald-600">
                      Hoàn tất {{ item.completedAt ? `lúc ${item.completedAt}` : '' }}{{ item.completedByUsername ? `
                      bởi
                      ${item.completedByUsername}` : '' }}
                    </p>
                  </div>

                  <div class="flex flex-wrap gap-2">
                    <BaseButton variant="outline" size="sm" @click="openChecklistModal(item)">
                      <PencilLine class="mr-2 h-4 w-4" />
                      Sửa
                    </BaseButton>
                    <BaseButton variant="primary" size="sm" :loading="checklistSubmitting"
                      @click="toggleChecklistCompleted(item)">
                      <CheckCheck class="mr-2 h-4 w-4" />
                      {{ item.completed ? 'Mở lại' : 'Hoàn tất' }}
                    </BaseButton>
                  </div>
                </div>
              </div>
              <div class="space-y-3">
                <div v-for="step in stepDefs" :key="step.key"
                  class="p-4 rounded-3xl border transition-all flex items-center justify-between" :class="{
                    'bg-emerald-50 border-emerald-100': getStepState(selectedRecord, step) === 'done',
                    'bg-white border-slate-100 shadow-sm': getStepState(selectedRecord, step) !== 'done',
                    'ring-2 ring-indigo-500/20 border-indigo-100!': getStepState(selectedRecord, step) === 'active'
                  }">
                  <div class="flex items-center gap-4">
                    <div
                      :class="['w-10 h-10 rounded-2xl flex items-center justify-center font-bold',
                        getStepState(selectedRecord, step) === 'done' ? 'bg-emerald-100 text-emerald-600' :
                          getStepState(selectedRecord, step) === 'active' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-slate-100 text-slate-400']">
                      <CheckCheck v-if="getStepState(selectedRecord, step) === 'done'" class="w-5 h-5" />
                      <component :is="step.icon" v-else class="w-5 h-5" />
                    </div>
                    <div>
                      <p class="text-sm font-black text-slate-900">{{ step.label }}</p>
                      <p class="text-xs text-slate-400 font-medium">{{ step.desc }}</p>
                    </div>
                  </div>

                  <button v-if="getStepState(selectedRecord, step) === 'active'" @click="handleStepProcess(step)"
                    class="px-4 py-2 bg-indigo-50 text-indigo-600 rounded-xl text-xs font-black shadow-sm hover:bg-indigo-600 hover:text-white transition-all">
                    Xử lý →
                  </button>
                  <div v-else-if="getStepState(selectedRecord, step) === 'done'"
                    class="text-emerald-500 font-black text-[10px] uppercase">Đã xong</div>
                </div>
              </div>
            </div>

            <!-- Documents & Assets -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div class="p-6 bg-slate-50 rounded-4xl border border-slate-100">
                <h4
                  class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-4 flex items-center justify-between">
                  Hồ sơ đầu vào
                  <span class="text-indigo-600 cursor-pointer hover:underline">+ Thêm</span>
                </h4>
                <div v-if="selectedRecord.documents?.length" class="space-y-3">
                  <div v-for="doc in safeArray(selectedRecord.documents)" :key="doc.onboardingDocumentId"
                    class="flex items-center justify-between text-xs font-bold bg-white p-2.5 rounded-xl border border-slate-100 shadow-sm">
                    <span class="truncate">{{ doc.documentName }}</span>
                    <div class="flex items-center gap-2">
                      <span class="text-emerald-500 text-[10px]">{{ doc.status }}</span>
                      <Eye class="w-4 h-4 text-slate-400 cursor-pointer hover:text-indigo-600" />
                    </div>
                  </div>
                </div>
                <div v-else class="py-4 text-center text-xs text-slate-400 italic">Chưa có hồ sơ nào</div>
              </div>

              <div
                class="p-6 bg-indigo-600 rounded-4xl text-white shadow-xl shadow-indigo-100 overflow-hidden relative">
                <div class="relative z-10">
                  <h4
                    class="text-[10px] font-black text-indigo-200 uppercase tracking-widest mb-4 flex items-center justify-between">
                    Trang thiết bị
                    <span class="text-white cursor-pointer hover:underline">+ Cấp phát</span>
                  </h4>
                  <div v-if="selectedRecord.assets?.length" class="space-y-2">
                    <div v-for="asset in safeArray(selectedRecord.assets)" :key="asset.onboardingAssetId"
                      class="flex items-center justify-between text-xs font-bold bg-white/10 p-2.5 rounded-xl">
                      <span class="truncate">{{ asset.assetName }}</span>
                      <span class="text-[10px] opacity-70">{{ asset.status }}</span>
                    </div>
                  </div>
                  <div v-else class="py-4 text-center text-xs text-indigo-200 italic">Chưa có thiết bị nào</div>
                </div>
                <Laptop class="absolute -right-4 -bottom-4 w-24 h-24 opacity-10 rotate-12" />
              </div>
            </div>

          </div>

          <!-- Sticky Actions -->
          <div
            class="p-8 border-t border-slate-50 flex items-center gap-4 shrink-0 bg-white shadow-[0_-10px_30px_rgba(0,0,0,0.02)]">
            <BaseButton v-if="recordStatus(selectedRecord) === 'READY_FOR_JOIN'" variant="primary" size="lg" shadow
              class="flex-1 rounded-2xl! font-bold" @click="handleComplete(selectedRecord)">
              <CheckCheck class="w-5 h-5 mr-2" /> Chốt hoàn tất Onboarding
            </BaseButton>
            <BaseButton v-if="recordStatus(selectedRecord) === 'READY_FOR_JOIN'" variant="outline" size="lg"
              class="flex-1 rounded-2xl! font-bold" @click="handleNotify(selectedRecord)">
              <Send class="w-5 h-5 mr-2" /> Gửi mail chào mừng
            </BaseButton>

            <div v-if="recordStatus(selectedRecord) === 'COMPLETED'"
              class="w-full text-center py-4 bg-emerald-50 text-emerald-700 rounded-2xl font-black uppercase text-xs tracking-widest">
              Quy trình đã hoàn thành 100%
            </div>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>

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

.animate-slide-left {
  animation: slideLeft 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes slideLeft {
  from {
    transform: translateX(100%);
  }

  to {
    transform: translateX(0);
  }
}

.slide-panel-enter-active,
.slide-panel-leave-active {
  transition: opacity 0.4s ease;
}

.slide-panel-enter-from,
.slide-panel-leave-to {
  opacity: 0;
}
</style>
