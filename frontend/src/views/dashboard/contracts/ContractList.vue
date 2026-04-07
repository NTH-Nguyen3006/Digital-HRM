<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  FileSignature, Plus, Search, Eye, AlertTriangle, ChevronLeft, ChevronRight,
  X, RefreshCw, CheckCircle, Clock, Send, FileText, AlertOctagon,
  RotateCcw, Download, Calendar
} from 'lucide-vue-next'
import GlassCard from '@/components/common/GlassCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import {
  getContracts,
  createContract,
  submitContract,
  reviewContract,
  activateContract,
  changeContractLifecycleStatus,
  createRenewalDraft,
  getContractTypeOptions,
  exportContract
} from '@/api/admin/contract.js'
import { getEmployees } from '@/api/admin/employee.js'

/* --- STATE --- */
const isLoading = ref(false)
const errorMessage = ref('')
const list = ref([])
const currentPage = ref(1)
const totalPages = ref(0)
const totalElements = ref(0)
const PAGE_SIZE = 12

/* --- FILTER --- */
const keyword = ref('')
const filterStatus = ref('')

/* --- DROPDOWN DATA --- */
const contractTypes = ref([])
const employees = ref([])

/* --- MODAL: CREATE --- */
const showCreateModal = ref(false)
const createLoading = ref(false)
const createError = ref('')
const createErrors = ref({})
const createForm = ref({
  employeeId: '', contractTypeId: '', startDate: '', endDate: '',
  basicSalary: '', positionAllowance: '', signedDate: '', note: ''
})

/* --- MODAL: DETAIL --- */
const showDetailModal = ref(false)
const detailTarget = ref(null)

/* --- CONFIRM DIALOG --- */
const showConfirm = ref(false)
const confirmConfig = ref({ title: '', message: '', type: 'primary', action: null, loading: false })

/* --- TOAST --- */
const toastMessage = ref('')
const toastType = ref('success')
const toastVisible = ref(false)
const showToast = (msg, type = 'success') => {
  toastMessage.value = msg; toastType.value = type; toastVisible.value = true
  setTimeout(() => { toastVisible.value = false }, 3500)
}

/* --- STATUS BADGE --- */
const statusBadge = (status) => {
  const map = {
    DRAFT: { label: 'Bản nháp', class: 'bg-slate-100 text-slate-600 border-slate-200', icon: FileText },
    REVIEWING: { label: 'Đang xét duyệt', class: 'bg-amber-100 text-amber-700 border-amber-200', icon: Clock },
    APPROVED: { label: 'Đã duyệt', class: 'bg-emerald-100 text-emerald-700 border-emerald-200', icon: CheckCircle },
    ACTIVE: { label: 'Có hiệu lực', class: 'bg-indigo-100 text-indigo-700 border-indigo-200', icon: CheckCircle },
    EXPIRED: { label: 'Hết hạn', class: 'bg-orange-100 text-orange-700 border-orange-200', icon: AlertOctagon },
    TERMINATED: { label: 'Đã chấm dứt', class: 'bg-rose-100 text-rose-700 border-rose-200', icon: X },
    PENDING_RENEWAL: { label: 'Cần gia hạn', class: 'bg-violet-100 text-violet-700 border-violet-200', icon: RotateCcw }
  }
  return map[status] || { label: status, class: 'bg-slate-100 text-slate-600', icon: FileText }
}

/* --- DEBOUNCE --- */
let searchTimer = null
const onSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 1; fetchContracts() }, 500)
}

/* --- API CALLS --- */
const fetchContracts = async () => {
  isLoading.value = true; errorMessage.value = ''
  try {
    const params = { page: currentPage.value - 1, size: PAGE_SIZE }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (filterStatus.value) params.status = filterStatus.value
    const res = await getContracts(params)
    list.value = res.content ?? res.data?.content ?? []
    totalPages.value = res.totalPages ?? res.data?.totalPages ?? 1
    totalElements.value = res.totalElements ?? res.data?.totalElements ?? list.value.length
  } catch (err) {
    errorMessage.value = 'Không thể tải danh sách hợp đồng.'
  } finally {
    isLoading.value = false
  }
}

const fetchDropdowns = async () => {
  try {
    const [typesRes, empRes] = await Promise.all([
      getContractTypeOptions(),
      getEmployees({ size: 500, employmentStatus: 'ACTIVE' })
    ])
    contractTypes.value = typesRes.data ?? typesRes ?? []
    employees.value = empRes.content ?? empRes.data?.content ?? []
  } catch (err) { console.error(err) }
}

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page; fetchContracts()
}

const visiblePages = computed(() => {
  const pages = []; const start = Math.max(1, currentPage.value - 2); const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i); return pages
})

/* --- CREATE CONTRACT --- */
const validateCreate = () => {
  const errors = {}
  if (!createForm.value.employeeId) errors.employeeId = 'Chọn nhân viên'
  if (!createForm.value.contractTypeId) errors.contractTypeId = 'Chọn loại hợp đồng'
  if (!createForm.value.startDate) errors.startDate = 'Ngày bắt đầu không được để trống'
  if (!createForm.value.basicSalary || isNaN(createForm.value.basicSalary)) errors.basicSalary = 'Lương cơ bản không hợp lệ'
  createErrors.value = errors
  return Object.keys(errors).length === 0
}

const handleCreate = async () => {
  if (!validateCreate()) return
  createLoading.value = true; createError.value = ''
  try {
    const payload = { ...createForm.value, basicSalary: Number(createForm.value.basicSalary), positionAllowance: Number(createForm.value.positionAllowance || 0) }
    await createContract(payload)
    showToast('Tạo hợp đồng thành công!', 'success')
    showCreateModal.value = false
    createForm.value = { employeeId: '', contractTypeId: '', startDate: '', endDate: '', basicSalary: '', positionAllowance: '', signedDate: '', note: '' }
    currentPage.value = 1; fetchContracts()
  } catch (err) {
    createError.value = err?.response?.data?.message || 'Có lỗi xảy ra'
  } finally {
    createLoading.value = false
  }
}

/* --- ACTIONS --- */
const confirmSendReview = (contract) => {
  confirmConfig.value = {
    title: 'Gửi yêu cầu duyệt', message: `Gửi hợp đồng của "${contract.employeeName}" để xét duyệt?`,
    type: 'primary', loading: false,
    action: async () => {
      confirmConfig.value.loading = true
      try {
        await submitContract(contract.laborContractId)
        showToast('Đã gửi yêu cầu duyệt!', 'success'); showConfirm.value = false; fetchContracts()
      } catch (err) { showToast(err?.response?.data?.message || 'Lỗi', 'error'); confirmConfig.value.loading = false }
    }
  }
  showConfirm.value = true
}

const confirmActivate = (contract) => {
  confirmConfig.value = {
    title: 'Kích hoạt hợp đồng', message: `Kích hoạt hợp đồng của "${contract.employeeName}"? Hợp đồng sẽ có hiệu lực chính thức.`,
    type: 'primary', loading: false,
    action: async () => {
      confirmConfig.value.loading = true
      try {
        await activateContract(contract.laborContractId)
        showToast('Hợp đồng đã được kích hoạt!', 'success'); showConfirm.value = false; fetchContracts()
      } catch (err) { showToast(err?.response?.data?.message || 'Lỗi', 'error'); confirmConfig.value.loading = false }
    }
  }
  showConfirm.value = true
}

const formatCurrency = (val) => val ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val) : '—'

onMounted(() => {
  if (localStorage.getItem('isAuthenticated') === 'true') {
    fetchContracts(); fetchDropdowns()
  }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-violet-50/20 to-slate-100 p-6">

    <!-- Toast -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0 translate-y-2 scale-95" enter-to-class="opacity-100 translate-y-0 scale-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="toastVisible" class="fixed top-6 right-6 z-[9999] px-5 py-3 rounded-2xl shadow-2xl font-medium text-sm backdrop-blur-md border"
           :class="toastType === 'success' ? 'bg-emerald-500/90 text-white border-emerald-400' : 'bg-rose-500/90 text-white border-rose-400'">
        {{ toastMessage }}
      </div>
    </Transition>

    <!-- HEADER -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <div class="flex items-center gap-3 mb-1">
          <div class="w-10 h-10 rounded-2xl bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center shadow-lg shadow-violet-200">
            <FileSignature class="w-5 h-5 text-white" />
          </div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Hợp đồng Lao động</h1>
        </div>
        <p class="text-slate-500 text-sm ml-1">Quản lý vòng đời hợp đồng từ soạn thảo đến kích hoạt
          <span v-if="totalElements > 0" class="font-semibold text-violet-600">({{ totalElements }} hợp đồng)</span>
        </p>
      </div>
      <div class="flex items-center gap-3">
        <BaseButton variant="outline" size="sm" :icon="RefreshCw" @click="fetchContracts" :loading="isLoading">Làm mới</BaseButton>
        <BaseButton variant="primary" size="md" :icon="Plus" @click="showCreateModal = true">Tạo hợp đồng</BaseButton>
      </div>
    </div>

    <!-- FILTER -->
    <GlassCard padding="p-4" class="mb-5">
      <div class="flex flex-wrap gap-3">
        <div class="flex-1 min-w-[240px]">
          <BaseInput
            v-model="keyword"
            @input="onSearch"
            type="text"
            placeholder="Tìm theo nhân viên, số hợp đồng..."
            :icon="Search"
          />
        </div>
        <select v-model="filterStatus" @change="currentPage = 1; fetchContracts()"
          class="h-10 px-3 text-sm text-slate-700 rounded-xl bg-white/80 border border-slate-200 focus:border-violet-500 focus:ring-4 focus:ring-violet-500/10 outline-none transition-all">
          <option value="">Tất cả trạng thái</option>
          <option value="DRAFT">Bản nháp</option>
          <option value="REVIEWING">Đang xét duyệt</option>
          <option value="APPROVED">Đã duyệt</option>
          <option value="ACTIVE">Có hiệu lực</option>
          <option value="EXPIRED">Hết hạn</option>
          <option value="TERMINATED">Chấm dứt</option>
        </select>
        <BaseButton v-if="keyword || filterStatus" variant="ghost" size="sm" :icon="X" @click="keyword = ''; filterStatus = ''; currentPage = 1; fetchContracts()" class="text-rose-500 hover:bg-rose-50">Xóa lọc</BaseButton>
      </div>
    </GlassCard>

    <div v-if="errorMessage" class="mb-4 p-4 bg-rose-50 border border-rose-200 rounded-2xl flex items-center gap-3 text-rose-700 text-sm">
      <AlertTriangle class="w-5 h-5" />{{ errorMessage }}
    </div>

    <!-- TABLE -->
    <GlassCard padding="p-0" class="overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[1000px]">
          <thead class="bg-slate-50/80 border-b border-slate-200/60">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Nhân viên</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Loại hợp đồng</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Thời hạn</th>
              <th class="px-4 py-3 text-right text-xs font-bold uppercase tracking-wider text-slate-500">Lương cơ bản</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Trạng thái</th>
              <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <template v-if="isLoading">
              <tr v-for="i in 8" :key="i" class="border-b border-slate-100">
                <td class="px-4 py-3"><div class="flex items-center gap-2"><div class="w-8 h-8 rounded-xl bg-slate-200 animate-pulse"></div><div class="space-y-1.5"><div class="h-3 w-24 bg-slate-200 rounded animate-pulse"></div><div class="h-2.5 w-16 bg-slate-100 rounded animate-pulse"></div></div></div></td>
                <td class="px-4 py-3"><div class="h-3 w-28 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-3 w-36 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-3 text-right"><div class="h-3 w-24 bg-slate-200 rounded animate-pulse ml-auto"></div></td>
                <td class="px-4 py-3"><div class="h-6 w-24 bg-slate-200 rounded-full animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-3 w-30 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
              </tr>
            </template>
            <tr v-else-if="list.length === 0">
              <td colspan="6" class="text-center py-16">
                <div class="flex flex-col items-center gap-3">
                  <div class="w-14 h-14 rounded-3xl bg-violet-50 flex items-center justify-center"><FileSignature class="w-7 h-7 text-violet-400" /></div>
                  <p class="font-semibold text-slate-500">Chưa có hợp đồng nào</p>
                  <BaseButton variant="primary" size="sm" :icon="Plus" @click="showCreateModal = true">Tạo hợp đồng đầu tiên</BaseButton>
                </div>
              </td>
            </tr>
            <tr v-else v-for="contract in list" :key="contract.laborContractId"
                class="border-b border-slate-100 hover:bg-violet-50/20 transition-colors group">
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-violet-400 to-purple-500 flex items-center justify-center text-white font-bold text-sm shadow-sm">
                    {{ (contract.employeeName || '?').charAt(0) }}
                  </div>
                  <div>
                    <p class="font-semibold text-slate-800 text-sm">{{ contract.employeeName }}</p>
                    <p class="text-xs text-slate-400 font-mono">{{ contract.contractCode || contract.laborContractId }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ contract.contractTypeName || '—' }}</td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1 text-sm text-slate-600">
                  <Calendar class="w-3.5 h-3.5 text-slate-400" />
                  <span>{{ contract.startDate ? new Date(contract.startDate).toLocaleDateString('vi-VN') : '—' }}</span>
                  <span v-if="contract.endDate" class="text-slate-300">→</span>
                  <span v-if="contract.endDate">{{ new Date(contract.endDate).toLocaleDateString('vi-VN') }}</span>
                  <span v-else class="text-indigo-500 font-medium text-xs">Không xác định</span>
                </div>
              </td>
              <td class="px-4 py-3 text-right font-semibold text-slate-800 text-sm">{{ formatCurrency(contract.basicSalary) }}</td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5">
                  <component :is="statusBadge(contract.status).icon" class="w-3.5 h-3.5 text-current" />
                  <span class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold border" :class="statusBadge(contract.status).class">
                    {{ statusBadge(contract.status).label }}
                  </span>
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center justify-center gap-1 opacity-60 group-hover:opacity-100 transition-opacity">
                  <button @click="detailTarget = contract; showDetailModal = true"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-indigo-100 hover:text-indigo-600 transition-all" title="Xem chi tiết">
                    <Eye class="w-4 h-4" />
                  </button>
                  <button v-if="contract.status === 'DRAFT'" @click="confirmSendReview(contract)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-amber-100 hover:text-amber-600 transition-all" title="Gửi duyệt">
                    <Send class="w-4 h-4" />
                  </button>
                  <button v-if="contract.status === 'APPROVED'" @click="confirmActivate(contract)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-emerald-100 hover:text-emerald-600 transition-all" title="Kích hoạt">
                    <CheckCircle class="w-4 h-4" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="!isLoading && list.length > 0" class="flex items-center justify-between px-6 py-4 border-t border-slate-200/60 bg-slate-50/40">
        <p class="text-sm text-slate-500">Trang <span class="font-semibold">{{ currentPage }}</span> / {{ totalPages }} — <span class="font-semibold text-violet-600">{{ totalElements }}</span> hợp đồng</p>
        <div class="flex items-center gap-1.5">
          <button @click="goToPage(currentPage - 1)" :disabled="currentPage <= 1" class="w-9 h-9 rounded-xl flex items-center justify-center hover:bg-violet-100 hover:text-violet-600 disabled:opacity-30 transition-all border border-slate-200"><ChevronLeft class="w-4 h-4 text-slate-500" /></button>
          <button v-for="page in visiblePages" :key="page" @click="goToPage(page)" class="w-9 h-9 rounded-xl text-sm font-semibold transition-all border" :class="currentPage === page ? 'bg-violet-600 text-white border-violet-600 shadow-lg shadow-violet-200' : 'text-slate-600 hover:bg-violet-50 border-slate-200'">{{ page }}</button>
          <button @click="goToPage(currentPage + 1)" :disabled="currentPage >= totalPages" class="w-9 h-9 rounded-xl flex items-center justify-center hover:bg-violet-100 hover:text-violet-600 disabled:opacity-30 transition-all border border-slate-200"><ChevronRight class="w-4 h-4 text-slate-500" /></button>
        </div>
      </div>
    </GlassCard>

    <!-- MODAL: TẠO HỢP ĐỒNG -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showCreateModal = false">
        <div class="w-full max-w-2xl bg-white rounded-3xl shadow-2xl overflow-hidden max-h-[90vh] flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 bg-gradient-to-r from-violet-600 to-purple-600">
            <div class="flex items-center gap-3"><FileSignature class="w-5 h-5 text-white" /><h3 class="text-white font-semibold text-lg">Tạo hợp đồng lao động</h3></div>
            <button @click="showCreateModal = false" class="w-8 h-8 rounded-xl bg-white/10 hover:bg-white/20 flex items-center justify-center text-white transition-all"><X class="w-4 h-4" /></button>
          </div>
          <div v-if="createError" class="mx-6 mt-4 p-3 bg-rose-50 border border-rose-200 rounded-xl flex items-center gap-2 text-rose-700 text-sm"><AlertTriangle class="w-4 h-4" />{{ createError }}</div>
          <div class="flex-1 overflow-y-auto p-6">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <!-- Nhân viên -->
              <div class="sm:col-span-2 space-y-1.5">
                <label class="block text-sm font-semibold text-slate-700 ml-1">Nhân viên <span class="text-red-500">*</span></label>
                <select v-model="createForm.employeeId" class="w-full h-11 px-4 text-slate-700 rounded-xl bg-white/50 border focus:outline-none focus:ring-4 transition-all" :class="createErrors.employeeId ? 'border-red-400 focus:ring-red-500/10' : 'border-slate-200 focus:border-violet-500 focus:ring-violet-500/10'">
                  <option value="">-- Chọn nhân viên --</option>
                  <option v-for="emp in employees" :key="emp.employeeId" :value="emp.employeeId">{{ emp.fullName }} ({{ emp.employeeCode }})</option>
                </select>
                <p v-if="createErrors.employeeId" class="text-xs text-red-500 ml-1">{{ createErrors.employeeId }}</p>
              </div>
              <!-- Loại HĐ -->
              <div class="space-y-1.5">
                <label class="block text-sm font-semibold text-slate-700 ml-1">Loại hợp đồng <span class="text-red-500">*</span></label>
                <select v-model="createForm.contractTypeId" class="w-full h-11 px-4 text-slate-700 rounded-xl bg-white/50 border focus:outline-none focus:ring-4 transition-all" :class="createErrors.contractTypeId ? 'border-red-400 focus:ring-red-500/10' : 'border-slate-200 focus:border-violet-500 focus:ring-violet-500/10'">
                  <option value="">-- Chọn loại hợp đồng --</option>
                  <option v-for="ct in contractTypes" :key="ct.contractTypeId" :value="ct.contractTypeId">{{ ct.contractTypeName }}</option>
                </select>
                <p v-if="createErrors.contractTypeId" class="text-xs text-red-500 ml-1">{{ createErrors.contractTypeId }}</p>
              </div>
              <BaseInput v-model="createForm.basicSalary" label="Lương cơ bản (VNĐ)" type="number" placeholder="0" :error="createErrors.basicSalary" :required="true" />
              <BaseInput v-model="createForm.startDate" label="Ngày bắt đầu" type="date" :error="createErrors.startDate" :required="true" />
              <BaseInput v-model="createForm.endDate" label="Ngày kết thúc (để trống nếu không xác định)" type="date" />
              <BaseInput v-model="createForm.positionAllowance" label="Phụ cấp chức vụ" type="number" placeholder="0" />
              <BaseInput v-model="createForm.signedDate" label="Ngày ký kết" type="date" />
              <div class="sm:col-span-2 space-y-1.5">
                <label class="block text-sm font-semibold text-slate-700 ml-1">Ghi chú</label>
                <textarea v-model="createForm.note" rows="2" class="w-full px-4 py-3 text-sm text-slate-700 rounded-xl bg-white/50 border border-slate-200 focus:border-violet-500 focus:ring-4 focus:ring-violet-500/10 outline-none resize-none"></textarea>
              </div>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showCreateModal = false" :disabled="createLoading">Hủy</BaseButton>
            <BaseButton variant="primary" :loading="createLoading" @click="handleCreate">Tạo hợp đồng</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

    <!-- MODAL: CHI TIẾT -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showDetailModal = false">
        <div class="w-full max-w-lg bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 bg-gradient-to-r from-indigo-600 to-violet-600">
            <h3 class="text-white font-semibold">Chi tiết hợp đồng</h3>
            <button @click="showDetailModal = false" class="w-8 h-8 rounded-xl bg-white/10 hover:bg-white/20 flex items-center justify-center text-white"><X class="w-4 h-4" /></button>
          </div>
          <div v-if="detailTarget" class="p-6 grid grid-cols-2 gap-3">
            <div class="col-span-2 p-4 bg-indigo-50 rounded-2xl border border-indigo-100">
              <p class="text-xs text-indigo-500 font-bold uppercase mb-0.5">Nhân viên</p>
              <p class="font-bold text-slate-800">{{ detailTarget.employeeName }}</p>
            </div>
            <div class="p-3 bg-slate-50 rounded-xl border border-slate-100">
              <p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Loại hợp đồng</p>
              <p class="font-semibold text-slate-700 text-sm">{{ detailTarget.contractTypeName }}</p>
            </div>
            <div class="p-3 bg-slate-50 rounded-xl border border-slate-100">
              <p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Trạng thái</p>
              <span class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold border" :class="statusBadge(detailTarget.status).class">{{ statusBadge(detailTarget.status).label }}</span>
            </div>
            <div class="p-3 bg-slate-50 rounded-xl border border-slate-100">
              <p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Ngày bắt đầu</p>
              <p class="font-semibold text-slate-700 text-sm">{{ detailTarget.startDate ? new Date(detailTarget.startDate).toLocaleDateString('vi-VN') : '—' }}</p>
            </div>
            <div class="p-3 bg-slate-50 rounded-xl border border-slate-100">
              <p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Ngày kết thúc</p>
              <p class="font-semibold text-slate-700 text-sm">{{ detailTarget.endDate ? new Date(detailTarget.endDate).toLocaleDateString('vi-VN') : 'Không xác định' }}</p>
            </div>
            <div class="col-span-2 p-3 bg-emerald-50 rounded-xl border border-emerald-100">
              <p class="text-xs text-emerald-500 font-bold uppercase mb-0.5">Lương cơ bản</p>
              <p class="font-bold text-emerald-800 text-lg">{{ formatCurrency(detailTarget.basicSalary) }}</p>
            </div>
            <div v-if="detailTarget.note" class="col-span-2 p-3 bg-slate-50 rounded-xl border border-slate-100">
              <p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Ghi chú</p>
              <p class="text-slate-700 text-sm">{{ detailTarget.note }}</p>
            </div>
          </div>
          <div class="flex justify-end px-6 py-4 border-t border-slate-100">
            <BaseButton variant="outline" @click="showDetailModal = false">Đóng</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

    <!-- CONFIRM DIALOG -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showConfirm" class="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
        <div class="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="p-6">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 rounded-2xl flex items-center justify-center flex-shrink-0" :class="confirmConfig.type === 'danger' ? 'bg-rose-100' : 'bg-indigo-100'">
                <AlertTriangle class="w-6 h-6" :class="confirmConfig.type === 'danger' ? 'text-rose-600' : 'text-indigo-600'" />
              </div>
              <div><h3 class="font-bold text-slate-800 text-lg">{{ confirmConfig.title }}</h3><p class="text-slate-500 text-sm mt-1">{{ confirmConfig.message }}</p></div>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showConfirm = false" :disabled="confirmConfig.loading">Hủy</BaseButton>
            <BaseButton :variant="confirmConfig.type === 'danger' ? 'danger' : 'primary'" :loading="confirmConfig.loading" @click="confirmConfig.action && confirmConfig.action()">Xác nhận</BaseButton>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>
