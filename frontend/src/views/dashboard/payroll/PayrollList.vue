<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import {
  DollarSign, Plus, Play, CheckCircle, Send, AlertTriangle,
  ChevronLeft, ChevronRight, X, Eye, RefreshCw,
  Clock, BarChart3, FileText, Zap, Users
} from 'lucide-vue-next'
import GlassCard from '@/components/common/GlassCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import {
  getPayrollPeriods,
  createPayrollPeriod,
  generatePayrollDraft,
  approvePayrollPeriod,
  publishPayrollPeriod,
  getPayrollItems
} from '@/api/admin/payroll.js'

/* --- STATE --- */
const isLoading = ref(false)
const errorMessage = ref('')
const list = ref([])
const currentPage = ref(1)
const totalPages = ref(0)
const totalElements = ref(0)
const PAGE_SIZE = 10

/* --- ITEM MODAL STATE --- */
const showItemsModal = ref(false)
const itemsTarget = ref(null)
const itemsList = ref([])
const itemsLoading = ref(false)

/* --- CREATE PERIOD MODAL --- */
const showCreateModal = ref(false)
const createLoading = ref(false)
const createError = ref('')
const createForm = ref({
  periodName: '',
  startDate: '',
  endDate: '',
  payDate: ''
})
const createErrors = ref({})

/* --- CONFIRM DIALOG STATE --- */
const showConfirmDialog = ref(false)
const confirmConfig = ref({
  title: '',
  message: '',
  type: 'primary', // primary | danger
  action: null,
  loading: false
})

/* --- TOAST --- */
const toastMessage = ref('')
const toastType = ref('success')
const showToastVisible = ref(false)
const showToast = (msg, type = 'success') => {
  toastMessage.value = msg
  toastType.value = type
  showToastVisible.value = true
  setTimeout(() => { showToastVisible.value = false }, 3500)
}

/* --- STATS (computed từ list) --- */
const stats = computed(() => ({
  total: list.value.length,
  draft: list.value.filter(p => p.status === 'DRAFT').length,
  approved: list.value.filter(p => p.status === 'APPROVED').length,
  published: list.value.filter(p => p.status === 'PUBLISHED').length
}))

/* --- STATUS BADGE --- */
const periodStatusBadge = (status) => {
  const map = {
    DRAFT: { label: 'Bản nháp', class: 'bg-slate-100 text-slate-600 border border-slate-200', icon: FileText },
    PROCESSING: { label: 'Đang tính', class: 'bg-indigo-100 text-indigo-700 border border-indigo-200', icon: Zap },
    PENDING_APPROVAL: { label: 'Chờ duyệt', class: 'bg-amber-100 text-amber-700 border border-amber-200', icon: Clock },
    APPROVED: { label: 'Đã duyệt', class: 'bg-emerald-100 text-emerald-700 border border-emerald-200', icon: CheckCircle },
    PUBLISHED: { label: 'Đã xuất bản', class: 'bg-violet-100 text-violet-700 border border-violet-200', icon: Send },
    CLOSED: { label: 'Đã đóng', class: 'bg-slate-100 text-slate-500 border border-slate-200', icon: CheckCircle }
  }
  return map[status] || { label: status, class: 'bg-slate-100 text-slate-600', icon: Clock }
}

/* --- CURRENCY FORMAT --- */
const formatCurrency = (value) => {
  if (!value && value !== 0) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

/* --- API: FETCH PERIODS --- */
const fetchPeriods = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const params = {
      page: currentPage.value - 1, // 0-indexed
      size: PAGE_SIZE
    }
    const res = await getPayrollPeriods(params)
    list.value = res.content ?? res.data?.content ?? res ?? []
    totalPages.value = res.totalPages ?? res.data?.totalPages ?? 1
    totalElements.value = res.totalElements ?? res.data?.totalElements ?? list.value.length
  } catch (err) {
    errorMessage.value = 'Không thể tải danh sách kỳ lương. Vui lòng thử lại.'
    console.error(err)
  } finally {
    isLoading.value = false
  }
}

/* --- API: VIEW ITEMS --- */
const openItems = async (period) => {
  itemsTarget.value = period
  itemsList.value = []
  showItemsModal.value = true
  itemsLoading.value = true
  try {
    const res = await getPayrollItems(period.payrollPeriodId, { size: 100 })
    itemsList.value = res.content ?? res.data?.content ?? res ?? []
  } catch (err) {
    console.error(err)
  } finally {
    itemsLoading.value = false
  }
}

/* --- FORM VALIDATION --- */
const validateCreateForm = () => {
  const errors = {}
  if (!createForm.value.periodName.trim()) errors.periodName = 'Tên kỳ lương không được để trống'
  if (!createForm.value.startDate) errors.startDate = 'Ngày bắt đầu không được để trống'
  if (!createForm.value.endDate) errors.endDate = 'Ngày kết thúc không được để trống'
  if (!createForm.value.payDate) errors.payDate = 'Ngày thanh toán không được để trống'
  if (createForm.value.startDate && createForm.value.endDate && createForm.value.endDate < createForm.value.startDate) {
    errors.endDate = 'Ngày kết thúc phải sau ngày bắt đầu'
  }
  createErrors.value = errors
  return Object.keys(errors).length === 0
}

/* --- API: CREATE PERIOD --- */
const handleCreatePeriod = async () => {
  // STEP 1: Validate
  if (!validateCreateForm()) return

  // STEP 3: Pre-flight
  createLoading.value = true
  createError.value = ''

  // STEP 4: Call API
  try {
    await createPayrollPeriod(createForm.value)
    // STEP 5: Success
    showToast('Tạo kỳ lương mới thành công!', 'success')
    showCreateModal.value = false
    createForm.value = { periodName: '', startDate: '', endDate: '', payDate: '' }
    createErrors.value = {}
    currentPage.value = 1
    await fetchPeriods()
  } catch (err) {
    // STEP 6: Error
    createError.value = err?.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại.'
  } finally {
    // STEP 7: Finally
    createLoading.value = false
  }
}

/* --- CONFIRM DIALOG: GENERATE DRAFT --- */
const confirmGenerateDraft = (period) => {
  confirmConfig.value = {
    title: 'Tính toán bảng lương nháp',
    message: `Bạn có chắc muốn chạy tính toán bảng lương nháp cho kỳ "${period.periodName}"?\n\nThao tác này sẽ tính toán lại toàn bộ dữ liệu lương cho kỳ này.`,
    type: 'primary',
    loading: false,
    action: async () => {
      confirmConfig.value.loading = true
      try {
        await generatePayrollDraft(period.payrollPeriodId)
        showToast('Đã kích hoạt tính lương nháp thành công!', 'success')
        showConfirmDialog.value = false
        await fetchPeriods()
      } catch (err) {
        showToast((err?.response?.data?.message || 'Lỗi khi tính lương nháp'), 'error')
        confirmConfig.value.loading = false
      }
    }
  }
  showConfirmDialog.value = true
}

/* --- CONFIRM DIALOG: APPROVE --- */
const confirmApprovePeriod = (period) => {
  confirmConfig.value = {
    title: 'Phê duyệt bảng lương',
    message: `Bạn xác nhận phê duyệt bảng lương kỳ "${period.periodName}"?\n\nSau khi duyệt, dữ liệu sẽ được chốt để chuẩn bị xuất bản.`,
    type: 'primary',
    loading: false,
    action: async () => {
      confirmConfig.value.loading = true
      try {
        await approvePayrollPeriod(period.payrollPeriodId)
        showToast('Phê duyệt bảng lương thành công!', 'success')
        showConfirmDialog.value = false
        await fetchPeriods()
      } catch (err) {
        showToast((err?.response?.data?.message || 'Lỗi khi duyệt bảng lương'), 'error')
        confirmConfig.value.loading = false
      }
    }
  }
  showConfirmDialog.value = true
}

/* --- CONFIRM DIALOG: PUBLISH (DESTRUCTIVE) --- */
const confirmPublishPeriod = (period) => {
  confirmConfig.value = {
    title: 'Xuất bản phiếu lương',
    message: `Bạn có chắc chắn muốn xuất bản phiếu lương kỳ "${period.periodName}" cho toàn bộ nhân viên?\n\nThao tác này sẽ gửi thông báo đến tất cả nhân sự và không thể hoàn tác.`,
    type: 'danger',
    loading: false,
    action: async () => {
      confirmConfig.value.loading = true
      try {
        await publishPayrollPeriod(period.payrollPeriodId)
        showToast('Xuất bản phiếu lương thành công! Nhân viên đã được thông báo.', 'success')
        showConfirmDialog.value = false
        await fetchPeriods()
      } catch (err) {
        showToast((err?.response?.data?.message || 'Lỗi khi xuất bản'), 'error')
        confirmConfig.value.loading = false
      }
    }
  }
  showConfirmDialog.value = true
}

/* --- PAGINATION --- */
const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchPeriods()
}

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

/* --- CLOSE CREATE MODAL --- */
const closeCreateModal = () => {
  showCreateModal.value = false
  createError.value = ''
  createErrors.value = {}
}

/* --- LIFECYCLE --- */
const router = useRouter()
onMounted(() => {
  // Chỉ gọi API khi đã đăng nhập (tránh 401 → redirect)
  if (localStorage.getItem('isAuthenticated') === 'true') {
    fetchPeriods()
  }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-emerald-50/20 to-slate-100 p-6">

    <!-- Toast Notification -->
    <Transition enter-active-class="transition-all duration-300 ease-out"
                enter-from-class="opacity-0 translate-y-2 scale-95"
                enter-to-class="opacity-100 translate-y-0 scale-100"
                leave-active-class="transition-all duration-200 ease-in"
                leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showToastVisible"
           class="fixed top-6 right-6 z-[9999] flex items-center gap-3 px-5 py-3 rounded-2xl shadow-2xl font-medium text-sm backdrop-blur-md border"
           :class="toastType === 'success'
             ? 'bg-emerald-500/90 text-white border-emerald-400'
             : 'bg-rose-500/90 text-white border-rose-400'">
        {{ toastMessage }}
      </div>
    </Transition>

    <!-- PAGE HEADER -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <div class="flex items-center gap-3 mb-1">
          <div class="w-10 h-10 rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center shadow-lg shadow-emerald-200">
            <DollarSign class="w-5 h-5 text-white" />
          </div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Quản lý Kỳ Lương</h1>
        </div>
        <p class="text-slate-500 text-sm ml-1">
          Tạo, tính toán, duyệt và xuất bản bảng lương theo từng kỳ trả lương
        </p>
      </div>
      <div class="flex items-center gap-3">
        <BaseButton variant="outline" size="sm" :icon="RefreshCw" @click="fetchPeriods" :loading="isLoading">
          Làm mới
        </BaseButton>
        <BaseButton variant="primary" size="md" :icon="Plus" @click="showCreateModal = true">
          Tạo kỳ lương mới
        </BaseButton>
      </div>
    </div>

    <!-- STAT CARDS -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <GlassCard padding="p-5" :hover="true">
        <div class="flex items-center justify-between mb-3">
          <p class="text-sm font-semibold text-slate-500">Tổng kỳ lương</p>
          <div class="w-9 h-9 rounded-xl bg-indigo-100 flex items-center justify-center">
            <BarChart3 class="w-4 h-4 text-indigo-600" />
          </div>
        </div>
        <p class="text-3xl font-bold text-slate-800">{{ stats.total }}</p>
        <p class="text-xs text-slate-400 mt-1">{{ totalElements }} kỳ tổng</p>
      </GlassCard>
      <GlassCard padding="p-5" :hover="true">
        <div class="flex items-center justify-between mb-3">
          <p class="text-sm font-semibold text-slate-500">Bản nháp</p>
          <div class="w-9 h-9 rounded-xl bg-slate-100 flex items-center justify-center">
            <FileText class="w-4 h-4 text-slate-500" />
          </div>
        </div>
        <p class="text-3xl font-bold text-slate-700">{{ stats.draft }}</p>
        <p class="text-xs text-slate-400 mt-1">Chưa tính lương</p>
      </GlassCard>
      <GlassCard padding="p-5" :hover="true">
        <div class="flex items-center justify-between mb-3">
          <p class="text-sm font-semibold text-slate-500">Đã duyệt</p>
          <div class="w-9 h-9 rounded-xl bg-emerald-100 flex items-center justify-center">
            <CheckCircle class="w-4 h-4 text-emerald-600" />
          </div>
        </div>
        <p class="text-3xl font-bold text-emerald-600">{{ stats.approved }}</p>
        <p class="text-xs text-slate-400 mt-1">Chờ xuất bản</p>
      </GlassCard>
      <GlassCard padding="p-5" :hover="true">
        <div class="flex items-center justify-between mb-3">
          <p class="text-sm font-semibold text-slate-500">Đã xuất bản</p>
          <div class="w-9 h-9 rounded-xl bg-violet-100 flex items-center justify-center">
            <Send class="w-4 h-4 text-violet-600" />
          </div>
        </div>
        <p class="text-3xl font-bold text-violet-600">{{ stats.published }}</p>
        <p class="text-xs text-slate-400 mt-1">Nhân viên đã nhận</p>
      </GlassCard>
    </div>

    <!-- ERROR -->
    <div v-if="errorMessage" class="mb-4 p-4 bg-rose-50 border border-rose-200 rounded-2xl flex items-center gap-3 text-rose-700 text-sm">
      <AlertTriangle class="w-5 h-5 flex-shrink-0" />
      {{ errorMessage }}
    </div>

    <!-- DATA TABLE -->
    <GlassCard padding="p-0" class="overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[900px]">
          <thead class="bg-slate-50/80 border-b border-slate-200/60">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Kỳ lương</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Thời gian kỳ</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Ngày trả lương</th>
              <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Số nhân viên</th>
              <th class="px-4 py-3 text-right text-xs font-bold uppercase tracking-wider text-slate-500">Tổng quỹ lương</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Trạng thái</th>
              <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <!-- LOADING SKELETON -->
            <template v-if="isLoading">
              <tr v-for="i in 5" :key="'sk-' + i" class="border-b border-slate-100">
                <td class="px-4 py-4"><div class="h-3 w-32 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-4"><div class="h-3 w-40 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-4"><div class="h-3 w-24 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-4 text-center"><div class="h-3 w-10 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
                <td class="px-4 py-4 text-right"><div class="h-3 w-32 bg-slate-200 rounded animate-pulse ml-auto"></div></td>
                <td class="px-4 py-4"><div class="h-6 w-24 bg-slate-200 rounded-full animate-pulse"></div></td>
                <td class="px-4 py-4"><div class="h-3 w-40 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
              </tr>
            </template>

            <!-- EMPTY STATE -->
            <tr v-else-if="list.length === 0">
              <td colspan="7" class="text-center py-20">
                <div class="flex flex-col items-center gap-4">
                  <div class="w-16 h-16 rounded-3xl bg-emerald-50 flex items-center justify-center">
                    <DollarSign class="w-8 h-8 text-emerald-400" />
                  </div>
                  <div>
                    <p class="font-semibold text-slate-600 text-base">Chưa có kỳ lương nào</p>
                    <p class="text-sm text-slate-400 mt-1">Tạo kỳ lương đầu tiên để bắt đầu quy trình tính lương</p>
                  </div>
                  <BaseButton variant="primary" size="sm" :icon="Plus" @click="showCreateModal = true">
                    Tạo kỳ lương mới
                  </BaseButton>
                </div>
              </td>
            </tr>

            <!-- DATA ROWS -->
            <tr v-else v-for="period in list" :key="period.payrollPeriodId"
                class="border-b border-slate-100/60 hover:bg-emerald-50/30 transition-colors duration-150 group">
              <td class="px-4 py-3">
                <div>
                  <p class="font-semibold text-slate-800 text-sm">{{ period.periodName }}</p>
                  <p class="text-xs text-slate-400 font-mono">ID: {{ period.payrollPeriodId }}</p>
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-slate-600">
                {{ period.startDate ? new Date(period.startDate).toLocaleDateString('vi-VN') : '—' }}
                <span class="text-slate-300 mx-1">→</span>
                {{ period.endDate ? new Date(period.endDate).toLocaleDateString('vi-VN') : '—' }}
              </td>
              <td class="px-4 py-3 text-sm font-medium text-slate-700">
                {{ period.payDate ? new Date(period.payDate).toLocaleDateString('vi-VN') : '—' }}
              </td>
              <td class="px-4 py-3 text-center">
                <div class="flex items-center justify-center gap-1 text-sm text-slate-600">
                  <Users class="w-3.5 h-3.5 text-slate-400" />
                  <span class="font-semibold">{{ period.totalEmployees ?? '—' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 text-right">
                <p class="font-bold text-slate-800 text-sm">{{ formatCurrency(period.totalGrossSalary) }}</p>
                <p class="text-xs text-slate-400">Net: {{ formatCurrency(period.totalNetSalary) }}</p>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5">
                  <component :is="periodStatusBadge(period.status).icon" class="w-3.5 h-3.5"
                    :class="{
                      'text-slate-500': period.status === 'DRAFT' || period.status === 'CLOSED',
                      'text-indigo-600': period.status === 'PROCESSING',
                      'text-amber-600': period.status === 'PENDING_APPROVAL',
                      'text-emerald-600': period.status === 'APPROVED',
                      'text-violet-600': period.status === 'PUBLISHED'
                    }" />
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold"
                        :class="periodStatusBadge(period.status).class">
                    {{ periodStatusBadge(period.status).label }}
                  </span>
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center justify-center gap-1 opacity-60 group-hover:opacity-100 transition-opacity flex-wrap">
                  <!-- View payroll items -->
                  <button @click="openItems(period)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-indigo-100 hover:text-indigo-600 transition-all"
                    title="Xem phiếu lương">
                    <Eye class="w-4 h-4" />
                  </button>

                  <!-- Generate Draft (DRAFT → PROCESSING) -->
                  <button v-if="period.status === 'DRAFT'"
                    @click="confirmGenerateDraft(period)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-indigo-100 hover:text-indigo-600 transition-all"
                    title="Tính lương nháp">
                    <Play class="w-4 h-4" />
                  </button>

                  <!-- Approve (PENDING_APPROVAL → APPROVED) -->
                  <button v-if="period.status === 'PENDING_APPROVAL'"
                    @click="confirmApprovePeriod(period)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-emerald-100 hover:text-emerald-600 transition-all"
                    title="Duyệt bảng lương">
                    <CheckCircle class="w-4 h-4" />
                  </button>

                  <!-- Publish (APPROVED → PUBLISHED) — DESTRUCTIVE -->
                  <button v-if="period.status === 'APPROVED'"
                    @click="confirmPublishPeriod(period)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-violet-100 hover:text-violet-600 transition-all"
                    title="Xuất bản phiếu lương">
                    <Send class="w-4 h-4" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- PAGINATION -->
      <div v-if="!isLoading && list.length > 0"
           class="flex flex-col sm:flex-row items-center justify-between gap-3 px-6 py-4 border-t border-slate-200/60 bg-slate-50/40">
        <p class="text-sm text-slate-500">
          Trang <span class="font-semibold text-slate-700">{{ currentPage }}</span> / {{ totalPages }} —
          <span class="font-semibold text-emerald-600">{{ totalElements }}</span> kỳ lương
        </p>
        <div class="flex items-center gap-1.5">
          <button @click="goToPage(currentPage - 1)" :disabled="currentPage <= 1"
            class="w-9 h-9 rounded-xl flex items-center justify-center text-slate-500 hover:bg-emerald-100 hover:text-emerald-600 disabled:opacity-30 disabled:cursor-not-allowed transition-all border border-slate-200">
            <ChevronLeft class="w-4 h-4" />
          </button>
          <button v-for="page in visiblePages" :key="page" @click="goToPage(page)"
            class="w-9 h-9 rounded-xl text-sm font-semibold transition-all border"
            :class="currentPage === page
              ? 'bg-emerald-500 text-white border-emerald-500 shadow-lg shadow-emerald-200'
              : 'text-slate-600 hover:bg-emerald-50 border-slate-200'">
            {{ page }}
          </button>
          <button @click="goToPage(currentPage + 1)" :disabled="currentPage >= totalPages"
            class="w-9 h-9 rounded-xl flex items-center justify-center text-slate-500 hover:bg-emerald-100 hover:text-emerald-600 disabled:opacity-30 disabled:cursor-not-allowed transition-all border border-slate-200">
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>
    </GlassCard>

    <!-- ===================== MODAL: TẠO KỲ LƯƠNG ===================== -->
    <Transition enter-active-class="transition-all duration-300 ease-out"
                enter-from-class="opacity-0" enter-to-class="opacity-100"
                leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="closeCreateModal">
        <div class="w-full max-w-lg bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 bg-gradient-to-r from-emerald-500 to-teal-600">
            <div class="flex items-center gap-3">
              <DollarSign class="w-5 h-5 text-white" />
              <h3 class="text-white font-semibold">Tạo kỳ lương mới</h3>
            </div>
            <button @click="closeCreateModal" class="w-8 h-8 rounded-xl bg-white/10 hover:bg-white/20 flex items-center justify-center text-white transition-all">
              <X class="w-4 h-4" />
            </button>
          </div>

          <div v-if="createError" class="mx-6 mt-4 p-3 bg-rose-50 border border-rose-200 rounded-xl flex items-center gap-2 text-rose-700 text-sm">
            <AlertTriangle class="w-4 h-4 flex-shrink-0" />
            {{ createError }}
          </div>

          <div class="p-6 space-y-4">
            <BaseInput v-model="createForm.periodName" label="Tên kỳ lương" placeholder="VD: Tháng 4/2026" :error="createErrors.periodName" :required="true" />

            <div class="grid grid-cols-2 gap-3">
              <BaseInput v-model="createForm.startDate" label="Ngày bắt đầu kỳ" type="date" :error="createErrors.startDate" :required="true" />
              <BaseInput v-model="createForm.endDate" label="Ngày kết thúc kỳ" type="date" :error="createErrors.endDate" :required="true" />
            </div>

            <BaseInput v-model="createForm.payDate" label="Ngày trả lương" type="date" :error="createErrors.payDate" :required="true" />

            <div class="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-emerald-700 text-xs">
              Sau khi tạo kỳ lương, bạn cần chạy "Tính lương nháp" để hệ thống tự động tính toán dữ liệu lương cho tất cả nhân viên.
            </div>
          </div>

          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="closeCreateModal" :disabled="createLoading">Hủy bỏ</BaseButton>
            <BaseButton variant="primary" :loading="createLoading" @click="handleCreatePeriod">Tạo kỳ lương</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ===================== MODAL: XEM PHIẾU LƯƠNG ===================== -->
    <Transition enter-active-class="transition-all duration-300 ease-out"
                enter-from-class="opacity-0" enter-to-class="opacity-100"
                leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showItemsModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showItemsModal = false">
        <div class="w-full max-w-4xl bg-white rounded-3xl shadow-2xl overflow-hidden max-h-[85vh] flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 bg-gradient-to-r from-indigo-600 to-violet-600">
            <div>
              <div class="flex items-center gap-3">
                <Eye class="w-5 h-5 text-white" />
                <h3 class="text-white font-semibold">Phiếu lương nhân viên</h3>
              </div>
              <p class="text-indigo-200 text-sm mt-0.5">{{ itemsTarget?.periodName }}</p>
            </div>
            <button @click="showItemsModal = false" class="w-8 h-8 rounded-xl bg-white/10 hover:bg-white/20 flex items-center justify-center text-white transition-all">
              <X class="w-4 h-4" />
            </button>
          </div>
          <div class="flex-1 overflow-auto">
            <div class="overflow-x-auto">
              <table class="w-full min-w-[700px]">
                <thead class="bg-slate-50 border-b border-slate-200 sticky top-0">
                  <tr>
                    <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Nhân viên</th>
                    <th class="px-4 py-3 text-right text-xs font-bold uppercase tracking-wider text-slate-500">Lương cơ bản</th>
                    <th class="px-4 py-3 text-right text-xs font-bold uppercase tracking-wider text-slate-500">Phụ cấp</th>
                    <th class="px-4 py-3 text-right text-xs font-bold uppercase tracking-wider text-slate-500">Khấu trừ</th>
                    <th class="px-4 py-3 text-right text-xs font-bold uppercase tracking-wider text-slate-500">Thực nhận</th>
                    <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">TT Duyệt</th>
                  </tr>
                </thead>
                <tbody>
                  <template v-if="itemsLoading">
                    <tr v-for="i in 5" :key="i" class="border-b border-slate-100">
                      <td class="px-4 py-3"><div class="h-3 w-28 bg-slate-200 rounded animate-pulse"></div></td>
                      <td class="px-4 py-3 text-right"><div class="h-3 w-24 bg-slate-200 rounded animate-pulse ml-auto"></div></td>
                      <td class="px-4 py-3 text-right"><div class="h-3 w-20 bg-slate-200 rounded animate-pulse ml-auto"></div></td>
                      <td class="px-4 py-3 text-right"><div class="h-3 w-20 bg-slate-200 rounded animate-pulse ml-auto"></div></td>
                      <td class="px-4 py-3 text-right"><div class="h-3 w-24 bg-slate-200 rounded animate-pulse ml-auto"></div></td>
                      <td class="px-4 py-3"><div class="h-5 w-16 bg-slate-200 rounded-full animate-pulse"></div></td>
                    </tr>
                  </template>
                  <tr v-else-if="itemsList.length === 0">
                    <td colspan="6" class="text-center py-12 text-slate-400">Chưa có dữ liệu phiếu lương</td>
                  </tr>
                  <tr v-else v-for="item in itemsList" :key="item.payrollItemId"
                      class="border-b border-slate-100 hover:bg-slate-50/50 transition-colors">
                    <td class="px-4 py-3">
                      <p class="font-semibold text-slate-800 text-sm">{{ item.employeeName || item.fullName }}</p>
                      <p class="text-xs text-slate-400">{{ item.employeeCode }}</p>
                    </td>
                    <td class="px-4 py-3 text-right text-sm font-medium text-slate-700">{{ formatCurrency(item.basicSalary) }}</td>
                    <td class="px-4 py-3 text-right text-sm text-emerald-600 font-medium">+{{ formatCurrency(item.totalAllowance) }}</td>
                    <td class="px-4 py-3 text-right text-sm text-rose-600 font-medium">-{{ formatCurrency(item.totalDeduction) }}</td>
                    <td class="px-4 py-3 text-right">
                      <span class="font-bold text-slate-900">{{ formatCurrency(item.netSalary) }}</span>
                    </td>
                    <td class="px-4 py-3">
                      <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold"
                            :class="item.status === 'CONFIRMED' ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'">
                        {{ item.status === 'CONFIRMED' ? 'Đã xác nhận' : 'Chờ xác nhận' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="flex justify-end px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showItemsModal = false">Đóng</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ===================== CONFIRM DIALOG (dùng chung) ===================== -->
    <Transition enter-active-class="transition-all duration-300 ease-out"
                enter-from-class="opacity-0" enter-to-class="opacity-100"
                leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showConfirmDialog" class="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
        <div class="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="px-6 py-5">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 rounded-2xl flex items-center justify-center flex-shrink-0"
                   :class="confirmConfig.type === 'danger' ? 'bg-rose-100' : 'bg-indigo-100'">
                <AlertTriangle class="w-6 h-6" :class="confirmConfig.type === 'danger' ? 'text-rose-600' : 'text-indigo-600'" />
              </div>
              <div>
                <h3 class="font-bold text-slate-800 text-lg leading-tight">{{ confirmConfig.title }}</h3>
                <p class="text-slate-500 text-sm mt-2 whitespace-pre-line leading-relaxed">{{ confirmConfig.message }}</p>
              </div>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showConfirmDialog = false" :disabled="confirmConfig.loading">Hủy bỏ</BaseButton>
            <BaseButton
              :variant="confirmConfig.type === 'danger' ? 'danger' : 'primary'"
              :loading="confirmConfig.loading"
              @click="confirmConfig.action && confirmConfig.action()">
              Xác nhận
            </BaseButton>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>
