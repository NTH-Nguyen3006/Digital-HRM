<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import {
  Search, Plus, Edit2, UserX, UserCheck, Filter, X, ChevronLeft,
  ChevronRight, Users, Briefcase, Building2, AlertTriangle, Eye,
  ArrowLeftRight, RefreshCw, Download
} from 'lucide-vue-next'
import GlassCard from '@/components/common/GlassCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import {
  getEmployees,
  createEmployee,
  updateEmployee,
  changeEmploymentStatus
} from '@/api/admin/employee.js'
import { getOrgUnits } from '@/api/admin/orgUnit.js'
import { getJobTitles } from '@/api/admin/jobTitle.js'

/* --- STATE --- */
const router = useRouter()
const isLoading = ref(false)
const errorMessage = ref('')
const list = ref([])
const currentPage = ref(1)
const totalPages = ref(0)
const totalElements = ref(0)
const PAGE_SIZE = 15

/* --- FILTER STATE --- */
const keyword = ref('')
const filterStatus = ref('')
const filterOrgUnit = ref('')
const filterJobTitle = ref('')
const showFilterPanel = ref(false)

/* --- DROPDOWN DATA --- */
const orgUnits = ref([])
const jobTitles = ref([])

/* --- MODAL STATE --- */
const showModal = ref(false)
const modalMode = ref('create') // 'create' | 'edit'
const editingEmployee = ref(null)
const showStatusModal = ref(false)
const statusTarget = ref(null)
const showConfirmDelete = ref(false)
const confirmTarget = ref(null)

/* --- FORM STATE --- */
const formErrors = ref({})
const formLoading = ref(false)
const formGeneralError = ref('')
const form = ref({
  employeeCode: '',
  fullName: '',
  workEmail: '',
  workPhone: '',
  genderCode: '',
  dateOfBirth: '',
  hireDate: '',
  employmentStatus: 'ACTIVE',
  orgUnitId: '',
  jobTitleId: '',
  workLocation: '',
  taxCode: '',
  personalEmail: '',
  mobilePhone: '',
  note: ''
})
const statusForm = ref({
  employmentStatus: '',
  note: ''
})
const statusFormError = ref('')

/* --- COMPUTED --- */
const statusBadge = computed(() => (status) => {
  const map = {
    ACTIVE: { label: 'Đang làm việc', class: 'bg-emerald-100 text-emerald-700 border border-emerald-200' },
    RESIGNED: { label: 'Đã nghỉ việc', class: 'bg-rose-100 text-rose-700 border border-rose-200' },
    ON_LEAVE: { label: 'Đang nghỉ phép', class: 'bg-amber-100 text-amber-700 border border-amber-200' },
    PROBATION: { label: 'Thử việc', class: 'bg-indigo-100 text-indigo-700 border border-indigo-200' },
    SUSPENDED: { label: 'Tạm đình chỉ', class: 'bg-slate-100 text-slate-600 border border-slate-200' }
  }
  return map[status] || { label: status, class: 'bg-slate-100 text-slate-600' }
})

const genderLabel = computed(() => (code) => {
  const map = { MALE: 'Nam', FEMALE: 'Nữ', OTHER: 'Khác' }
  return map[code] || code
})

/* --- DEBOUNCE SEARCH --- */
let searchTimer = null
const onSearchInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    fetchEmployees()
  }, 500)
}

const onFilterChange = () => {
  currentPage.value = 1
  fetchEmployees()
}

/* --- API CALLS --- */
const fetchEmployees = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const params = {
      page: currentPage.value - 1, // 0-indexed cho backend
      size: PAGE_SIZE
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (filterStatus.value) params.employmentStatus = filterStatus.value
    if (filterOrgUnit.value) params.orgUnitId = filterOrgUnit.value
    if (filterJobTitle.value) params.jobTitleId = filterJobTitle.value

    const res = await getEmployees(params)
    list.value = res.content ?? res.data?.content ?? []
    totalPages.value = res.totalPages ?? res.data?.totalPages ?? 0
    totalElements.value = res.totalElements ?? res.data?.totalElements ?? 0
  } catch (err) {
    errorMessage.value = 'Không thể tải danh sách nhân sự. Vui lòng thử lại.'
    console.error(err)
  } finally {
    isLoading.value = false
  }
}

const fetchDropdowns = async () => {
  try {
    const [orgRes, jobRes] = await Promise.all([
      getOrgUnits({ size: 200 }),
      getJobTitles({ size: 200 })
    ])
    orgUnits.value = orgRes.content ?? orgRes.data?.content ?? []
    jobTitles.value = jobRes.content ?? jobRes.data?.content ?? []
  } catch (err) {
    console.error('fetchDropdowns failed:', err)
  }
}

/* --- PAGINATION --- */
const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchEmployees()
}

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

/* --- MODAL ACTIONS --- */
const openCreateModal = () => {
  modalMode.value = 'create'
  editingEmployee.value = null
  resetForm()
  formGeneralError.value = ''
  showModal.value = true
}

const openEditModal = (emp) => {
  modalMode.value = 'edit'
  editingEmployee.value = emp
  form.value = {
    employeeCode: emp.employeeCode || '',
    fullName: emp.fullName || '',
    workEmail: emp.workEmail || '',
    workPhone: emp.workPhone || '',
    genderCode: emp.genderCode || '',
    dateOfBirth: emp.dateOfBirth || '',
    hireDate: emp.hireDate || '',
    employmentStatus: emp.employmentStatus || 'ACTIVE',
    orgUnitId: emp.orgUnitId || '',
    jobTitleId: emp.jobTitleId || '',
    workLocation: emp.workLocation || '',
    taxCode: emp.taxCode || '',
    personalEmail: emp.personalEmail || '',
    mobilePhone: emp.mobilePhone || '',
    note: emp.note || ''
  }
  formErrors.value = {}
  formGeneralError.value = ''
  showModal.value = true
}

const openStatusModal = (emp) => {
  statusTarget.value = emp
  statusForm.value = { employmentStatus: emp.employmentStatus, note: '' }
  statusFormError.value = ''
  showStatusModal.value = true
}

const closeModal = () => {
  showModal.value = false
  formGeneralError.value = ''
  formErrors.value = {}
}

const resetForm = () => {
  form.value = {
    employeeCode: '', fullName: '', workEmail: '', workPhone: '',
    genderCode: '', dateOfBirth: '', hireDate: '', employmentStatus: 'ACTIVE',
    orgUnitId: '', jobTitleId: '', workLocation: '', taxCode: '',
    personalEmail: '', mobilePhone: '', note: ''
  }
  formErrors.value = {}
}

/* --- FORM VALIDATION --- */
const validateForm = () => {
  const errors = {}
  if (!form.value.employeeCode.trim()) errors.employeeCode = 'Mã nhân viên không được để trống'
  if (!form.value.fullName.trim()) errors.fullName = 'Họ tên không được để trống'
  if (!form.value.genderCode) errors.genderCode = 'Vui lòng chọn giới tính'
  if (!form.value.dateOfBirth) errors.dateOfBirth = 'Ngày sinh không được để trống'
  if (!form.value.hireDate) errors.hireDate = 'Ngày vào làm không được để trống'
  if (!form.value.orgUnitId) errors.orgUnitId = 'Vui lòng chọn đơn vị'
  if (!form.value.jobTitleId) errors.jobTitleId = 'Vui lòng chọn chức danh'
  if (form.value.workEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.workEmail)) {
    errors.workEmail = 'Email không đúng định dạng'
  }
  formErrors.value = errors
  return Object.keys(errors).length === 0
}

/* --- SUBMIT FORM --- */
const handleSubmit = async () => {
  // STEP 1: Validate Frontend
  if (!validateForm()) return

  // STEP 3: Pre-flight
  formLoading.value = true
  formGeneralError.value = ''

  // STEP 4: Call API
  try {
    const payload = { ...form.value }
    if (!payload.orgUnitId) delete payload.orgUnitId
    else payload.orgUnitId = Number(payload.orgUnitId)
    if (!payload.jobTitleId) delete payload.jobTitleId
    else payload.jobTitleId = Number(payload.jobTitleId)

    if (modalMode.value === 'create') {
      await createEmployee(payload)
      showToast('Thêm mới nhân sự thành công!', 'success')
    } else {
      const { employmentStatus, ...updatePayload } = payload
      await updateEmployee(editingEmployee.value.employeeId, updatePayload)
      showToast('Cập nhật thông tin nhân sự thành công!', 'success')
    }

    // STEP 5: Success
    closeModal()
    currentPage.value = 1
    await fetchEmployees()
  } catch (err) {
    // STEP 6: Error
    const msg = err?.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại.'
    formGeneralError.value = msg
  } finally {
    // STEP 7: Finally
    formLoading.value = false
  }
}

/* --- CHANGE STATUS --- */
const handleChangeStatus = async () => {
  if (!statusForm.value.employmentStatus) {
    statusFormError.value = 'Vui lòng chọn trạng thái'
    return
  }
  if (!statusForm.value.note.trim()) {
    statusFormError.value = 'Vui lòng nhập lý do thay đổi trạng thái'
    return
  }

  formLoading.value = true
  statusFormError.value = ''
  try {
    await changeEmploymentStatus(statusTarget.value.employeeId, statusForm.value)
    showToast('Cập nhật trạng thái thành công!', 'success')
    showStatusModal.value = false
    await fetchEmployees()
  } catch (err) {
    statusFormError.value = err?.response?.data?.message || 'Có lỗi xảy ra'
  } finally {
    formLoading.value = false
  }
}

/* --- CLEAR FILTER --- */
const clearAllFilters = () => {
  keyword.value = ''
  filterStatus.value = ''
  filterOrgUnit.value = ''
  filterJobTitle.value = ''
  currentPage.value = 1
  fetchEmployees()
}

/* --- TOAST NOTIFICATION --- */
const toastMessage = ref('')
const toastType = ref('success')
const showToastVisible = ref(false)
const showToast = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToastVisible.value = true
  setTimeout(() => { showToastVisible.value = false }, 3500)
}

/* --- LIFECYCLE --- */
onMounted(() => {
  // Chỉ gọi API khi đã đăng nhập (tránh 401 → redirect)
  if (localStorage.getItem('isAuthenticated') === 'true') {
    fetchEmployees()
    fetchDropdowns()
  }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-indigo-50/30 to-slate-100 p-6">

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
          <div class="w-10 h-10 rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-600 flex items-center justify-center shadow-lg shadow-indigo-200">
            <Users class="w-5 h-5 text-white" />
          </div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Danh sách Nhân sự</h1>
        </div>
        <p class="text-slate-500 text-sm ml-1">
          Quản lý toàn bộ hồ sơ nhân viên trong hệ thống
          <span v-if="totalElements > 0" class="font-semibold text-indigo-600">({{ totalElements }} nhân sự)</span>
        </p>
      </div>
      <div class="flex items-center gap-3">
        <BaseButton variant="outline" size="sm" :icon="RefreshCw" @click="fetchEmployees" :loading="isLoading">
          Làm mới
        </BaseButton>
        <BaseButton variant="primary" size="md" :icon="Plus" @click="openCreateModal">
          Thêm nhân sự
        </BaseButton>
      </div>
    </div>

    <!-- SEARCH & FILTER BAR -->
    <GlassCard padding="p-4" class="mb-5">
      <div class="flex flex-col lg:flex-row gap-3">
        <!-- Search -->
        <div class="flex-1">
          <BaseInput
            v-model="keyword"
            @input="onSearchInput"
            type="text"
            placeholder="Tìm theo mã, tên, email nhân viên..."
            :icon="Search"
          />
        </div>

        <!-- Filter selects -->
        <div class="flex flex-wrap gap-2 items-center">
          <select
            v-model="filterStatus"
            @change="onFilterChange"
            class="h-10 px-3 text-sm text-slate-700 rounded-xl bg-white/80 border border-slate-200 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 outline-none transition-all min-w-[160px]">
            <option value="">Tất cả trạng thái</option>
            <option value="ACTIVE">Đang làm việc</option>
            <option value="PROBATION">Thử việc</option>
            <option value="ON_LEAVE">Đang nghỉ phép</option>
            <option value="RESIGNED">Đã nghỉ việc</option>
            <option value="SUSPENDED">Tạm đình chỉ</option>
          </select>

          <select
            v-model="filterOrgUnit"
            @change="onFilterChange"
            class="h-10 px-3 text-sm text-slate-700 rounded-xl bg-white/80 border border-slate-200 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 outline-none transition-all min-w-[160px]">
            <option value="">Tất cả đơn vị</option>
            <option v-for="ou in orgUnits" :key="ou.orgUnitId" :value="ou.orgUnitId">
              {{ ou.orgUnitName }}
            </option>
          </select>

          <select
            v-model="filterJobTitle"
            @change="onFilterChange"
            class="h-10 px-3 text-sm text-slate-700 rounded-xl bg-white/80 border border-slate-200 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 outline-none transition-all min-w-[160px]">
            <option value="">Tất cả chức danh</option>
            <option v-for="jt in jobTitles" :key="jt.jobTitleId" :value="jt.jobTitleId">
              {{ jt.jobTitleName }}
            </option>
          </select>

          <BaseButton
            v-if="keyword || filterStatus || filterOrgUnit || filterJobTitle"
            variant="ghost" size="sm" :icon="X"
            @click="clearAllFilters"
            class="text-rose-500 hover:bg-rose-50">
            Xóa bộ lọc
          </BaseButton>
        </div>
      </div>
    </GlassCard>

    <!-- ERROR MESSAGE -->
    <div v-if="errorMessage" class="mb-4 p-4 bg-rose-50 border border-rose-200 rounded-2xl flex items-center gap-3 text-rose-700 text-sm">
      <AlertTriangle class="w-5 h-5 flex-shrink-0" />
      {{ errorMessage }}
    </div>

    <!-- DATA TABLE -->
    <GlassCard padding="p-0" class="overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[1000px]">
          <thead class="bg-slate-50/80 border-b border-slate-200/60">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Nhân viên</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Email công ty</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Đơn vị</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Chức danh</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Ngày vào làm</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Trạng thái</th>
              <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <!-- LOADING SKELETON -->
            <template v-if="isLoading">
              <tr v-for="i in 8" :key="'sk-' + i" class="border-b border-slate-100/60">
                <td class="px-4 py-3">
                  <div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-xl bg-slate-200 animate-pulse"></div>
                    <div class="space-y-1.5">
                      <div class="h-3 w-28 bg-slate-200 rounded animate-pulse"></div>
                      <div class="h-2.5 w-20 bg-slate-100 rounded animate-pulse"></div>
                    </div>
                  </div>
                </td>
                <td class="px-4 py-3"><div class="h-3 w-36 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-3 w-28 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-3 w-24 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-3 w-20 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-6 w-24 bg-slate-200 rounded-full animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-3 w-20 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
              </tr>
            </template>

            <!-- EMPTY STATE -->
            <tr v-else-if="list.length === 0">
              <td colspan="7" class="text-center py-20">
                <div class="flex flex-col items-center gap-4">
                  <div class="w-16 h-16 rounded-3xl bg-slate-100 flex items-center justify-center">
                    <Users class="w-8 h-8 text-slate-400" />
                  </div>
                  <div>
                    <p class="font-semibold text-slate-600 text-base">Không tìm thấy nhân sự</p>
                    <p class="text-sm text-slate-400 mt-1">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
                  </div>
                  <BaseButton variant="outline" size="sm" @click="clearAllFilters">Xóa bộ lọc</BaseButton>
                </div>
              </td>
            </tr>

            <!-- DATA ROWS -->
            <tr v-else v-for="emp in list" :key="emp.employeeId"
                class="border-b border-slate-100/60 hover:bg-indigo-50/30 transition-colors duration-150 group">
              <td class="px-4 py-3">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-400 to-violet-500 flex items-center justify-center text-white font-bold text-sm shadow-sm flex-shrink-0">
                    {{ (emp.fullName || '?').charAt(0).toUpperCase() }}
                  </div>
                  <div>
                    <p class="font-semibold text-slate-800 text-sm leading-tight">{{ emp.fullName }}</p>
                    <p class="text-xs text-slate-400 font-mono">{{ emp.employeeCode }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ emp.workEmail || '—' }}</td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5 text-sm text-slate-600">
                  <Building2 class="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                  {{ emp.orgUnitName || '—' }}
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5 text-sm text-slate-600">
                  <Briefcase class="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                  {{ emp.jobTitleName || '—' }}
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-slate-500">
                {{ emp.hireDate ? new Date(emp.hireDate).toLocaleDateString('vi-VN') : '—' }}
              </td>
              <td class="px-4 py-3">
                <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold"
                      :class="statusBadge(emp.employmentStatus).class">
                  {{ statusBadge(emp.employmentStatus).label }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center justify-center gap-1.5 opacity-60 group-hover:opacity-100 transition-opacity">
                  <!-- View -->
                  <button
                    @click="router.push({ name: 'employee-detail', params: { id: emp.employeeId } })"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-indigo-100 hover:text-indigo-600 transition-all"
                    title="Xem chi tiết">
                    <Eye class="w-4 h-4" />
                  </button>
                  <!-- Edit -->
                  <button
                    @click="openEditModal(emp)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-amber-100 hover:text-amber-600 transition-all"
                    title="Chỉnh sửa">
                    <Edit2 class="w-4 h-4" />
                  </button>
                  <!-- Change Status -->
                  <button
                    @click="openStatusModal(emp)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-violet-100 hover:text-violet-600 transition-all"
                    title="Thay đổi trạng thái">
                    <ArrowLeftRight class="w-4 h-4" />
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
          Trang <span class="font-semibold text-slate-700">{{ currentPage }}</span> /
          {{ totalPages }} —
          <span class="font-semibold text-indigo-600">{{ totalElements }}</span> nhân sự
        </p>
        <div class="flex items-center gap-1.5">
          <button
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage <= 1"
            class="w-9 h-9 rounded-xl flex items-center justify-center text-slate-500 hover:bg-indigo-100 hover:text-indigo-600 disabled:opacity-30 disabled:cursor-not-allowed transition-all border border-slate-200">
            <ChevronLeft class="w-4 h-4" />
          </button>
          <button
            v-for="page in visiblePages" :key="page"
            @click="goToPage(page)"
            class="w-9 h-9 rounded-xl text-sm font-semibold transition-all border"
            :class="currentPage === page
              ? 'bg-indigo-600 text-white border-indigo-600 shadow-lg shadow-indigo-200'
              : 'text-slate-600 hover:bg-indigo-50 border-slate-200'">
            {{ page }}
          </button>
          <button
            @click="goToPage(currentPage + 1)"
            :disabled="currentPage >= totalPages"
            class="w-9 h-9 rounded-xl flex items-center justify-center text-slate-500 hover:bg-indigo-100 hover:text-indigo-600 disabled:opacity-30 disabled:cursor-not-allowed transition-all border border-slate-200">
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>
    </GlassCard>

    <!-- ===================== MODAL: TẠO / CHỈNH SỬA NHÂN SỰ ===================== -->
    <Transition enter-active-class="transition-all duration-300 ease-out"
                enter-from-class="opacity-0" enter-to-class="opacity-100"
                leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="closeModal">
        <Transition enter-active-class="transition-all duration-300 ease-out"
                    enter-from-class="opacity-0 scale-95 translate-y-4"
                    enter-to-class="opacity-100 scale-100 translate-y-0">
          <div v-if="showModal" class="w-full max-w-3xl bg-white rounded-3xl shadow-2xl overflow-hidden max-h-[90vh] flex flex-col">
            <!-- Modal Header -->
            <div class="flex items-center justify-between px-6 py-4 bg-gradient-to-r from-indigo-600 to-violet-600">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-xl bg-white/20 flex items-center justify-center">
                  <component :is="modalMode === 'create' ? Plus : Edit2" class="w-4 h-4 text-white" />
                </div>
                <h3 class="text-white font-semibold text-lg">
                  {{ modalMode === 'create' ? 'Thêm nhân sự mới' : 'Chỉnh sửa nhân sự' }}
                </h3>
              </div>
              <button @click="closeModal" class="w-8 h-8 rounded-xl bg-white/10 hover:bg-white/20 flex items-center justify-center text-white transition-all">
                <X class="w-4 h-4" />
              </button>
            </div>

            <!-- FORM ERROR GENERAL -->
            <div v-if="formGeneralError" class="mx-6 mt-4 p-3 bg-rose-50 border border-rose-200 rounded-xl flex items-center gap-2 text-rose-700 text-sm">
              <AlertTriangle class="w-4 h-4 flex-shrink-0" />
              {{ formGeneralError }}
            </div>

            <!-- Modal Body -->
            <div class="flex-1 overflow-y-auto p-6">
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <BaseInput v-model="form.employeeCode" label="Mã nhân viên" placeholder="VD: EMP001" :error="formErrors.employeeCode" :required="true" :disabled="modalMode === 'edit'" />
                <BaseInput v-model="form.fullName" label="Họ và tên" placeholder="Nguyễn Văn A" :error="formErrors.fullName" :required="true" />
                <BaseInput v-model="form.workEmail" label="Email công ty" type="email" placeholder="email@company.com" :error="formErrors.workEmail" />
                <BaseInput v-model="form.workPhone" label="Điện thoại công ty" placeholder="024 xxxx xxxx" />

                <!-- Giới tính -->
                <div class="w-full space-y-1.5">
                  <label class="block text-sm font-semibold text-slate-700 ml-1">Giới tính <span class="text-red-500">*</span></label>
                  <select v-model="form.genderCode"
                    class="w-full h-11 px-4 text-slate-700 rounded-xl bg-white/50 backdrop-blur-sm border focus:outline-none focus:ring-4 transition-all"
                    :class="formErrors.genderCode ? 'border-red-400 focus:border-red-500 focus:ring-red-500/10' : 'border-slate-200 focus:border-indigo-500 focus:ring-indigo-500/10'">
                    <option value="">-- Chọn giới tính --</option>
                    <option value="MALE">Nam</option>
                    <option value="FEMALE">Nữ</option>
                    <option value="OTHER">Khác</option>
                  </select>
                  <p v-if="formErrors.genderCode" class="text-xs font-medium text-red-500 ml-1">{{ formErrors.genderCode }}</p>
                </div>

                <BaseInput v-model="form.dateOfBirth" label="Ngày sinh" type="date" :error="formErrors.dateOfBirth" :required="true" />

                <!-- Đơn vị -->
                <div class="w-full space-y-1.5">
                  <label class="block text-sm font-semibold text-slate-700 ml-1">Đơn vị / Phòng ban <span class="text-red-500">*</span></label>
                  <select v-model="form.orgUnitId"
                    class="w-full h-11 px-4 text-slate-700 rounded-xl bg-white/50 backdrop-blur-sm border focus:outline-none focus:ring-4 transition-all"
                    :class="formErrors.orgUnitId ? 'border-red-400 focus:border-red-500 focus:ring-red-500/10' : 'border-slate-200 focus:border-indigo-500 focus:ring-indigo-500/10'">
                    <option value="">-- Chọn đơn vị --</option>
                    <option v-for="ou in orgUnits" :key="ou.orgUnitId" :value="ou.orgUnitId">{{ ou.orgUnitName }}</option>
                  </select>
                  <p v-if="formErrors.orgUnitId" class="text-xs font-medium text-red-500 ml-1">{{ formErrors.orgUnitId }}</p>
                </div>

                <!-- Chức danh -->
                <div class="w-full space-y-1.5">
                  <label class="block text-sm font-semibold text-slate-700 ml-1">Chức danh <span class="text-red-500">*</span></label>
                  <select v-model="form.jobTitleId"
                    class="w-full h-11 px-4 text-slate-700 rounded-xl bg-white/50 backdrop-blur-sm border focus:outline-none focus:ring-4 transition-all"
                    :class="formErrors.jobTitleId ? 'border-red-400 focus:border-red-500 focus:ring-red-500/10' : 'border-slate-200 focus:border-indigo-500 focus:ring-indigo-500/10'">
                    <option value="">-- Chọn chức danh --</option>
                    <option v-for="jt in jobTitles" :key="jt.jobTitleId" :value="jt.jobTitleId">{{ jt.jobTitleName }}</option>
                  </select>
                  <p v-if="formErrors.jobTitleId" class="text-xs font-medium text-red-500 ml-1">{{ formErrors.jobTitleId }}</p>
                </div>

                <BaseInput v-model="form.hireDate" label="Ngày vào làm" type="date" :error="formErrors.hireDate" :required="true" />
                <BaseInput v-model="form.workLocation" label="Địa điểm làm việc" placeholder="VD: Hà Nội" />
                <BaseInput v-model="form.taxCode" label="Mã số thuế" placeholder="000000000" />
                <BaseInput v-model="form.personalEmail" label="Email cá nhân" type="email" placeholder="personal@gmail.com" />
                <BaseInput v-model="form.mobilePhone" label="Số điện thoại cá nhân" placeholder="09x xxxx xxxx" />
              </div>

              <!-- Ghi chú -->
              <div class="mt-4 w-full space-y-1.5">
                <label class="block text-sm font-semibold text-slate-700 ml-1">Ghi chú</label>
                <textarea v-model="form.note" rows="3" placeholder="Nhập ghi chú nếu có..."
                  class="w-full px-4 py-3 text-slate-700 rounded-xl bg-white/50 backdrop-blur-sm border border-slate-200 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 outline-none transition-all text-sm resize-none"></textarea>
              </div>
            </div>

            <!-- Modal Footer -->
            <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
              <BaseButton variant="outline" @click="closeModal" :disabled="formLoading">Hủy bỏ</BaseButton>
              <BaseButton variant="primary" :loading="formLoading" @click="handleSubmit">
                {{ modalMode === 'create' ? 'Thêm nhân sự' : 'Lưu thay đổi' }}
              </BaseButton>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>

    <!-- ===================== MODAL: THAY ĐỔI TRẠNG THÁI ===================== -->
    <Transition enter-active-class="transition-all duration-300 ease-out"
                enter-from-class="opacity-0" enter-to-class="opacity-100"
                leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showStatusModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showStatusModal = false">
        <div class="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="px-6 py-4 bg-gradient-to-r from-violet-600 to-purple-600">
            <div class="flex items-center gap-3">
              <ArrowLeftRight class="w-5 h-5 text-white" />
              <h3 class="text-white font-semibold">Thay đổi trạng thái nhân sự</h3>
            </div>
            <p class="text-violet-200 text-sm mt-1">{{ statusTarget?.fullName }} ({{ statusTarget?.employeeCode }})</p>
          </div>
          <div class="p-6 space-y-4">
            <div v-if="statusFormError" class="p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-sm flex items-center gap-2">
              <AlertTriangle class="w-4 h-4 flex-shrink-0" />
              {{ statusFormError }}
            </div>

            <div class="w-full space-y-1.5">
              <label class="block text-sm font-semibold text-slate-700 ml-1">Trạng thái mới <span class="text-red-500">*</span></label>
              <select v-model="statusForm.employmentStatus"
                class="w-full h-11 px-4 text-slate-700 rounded-xl bg-white/50 border border-slate-200 focus:border-violet-500 focus:ring-4 focus:ring-violet-500/10 outline-none transition-all">
                <option value="ACTIVE">Đang làm việc</option>
                <option value="PROBATION">Thử việc</option>
                <option value="ON_LEAVE">Đang nghỉ phép</option>
                <option value="RESIGNED">Đã nghỉ việc</option>
                <option value="SUSPENDED">Tạm đình chỉ</option>
              </select>
            </div>

            <div class="w-full space-y-1.5">
              <label class="block text-sm font-semibold text-slate-700 ml-1">Lý do thay đổi <span class="text-red-500">*</span></label>
              <textarea v-model="statusForm.note" rows="3" placeholder="Nhập lý do thay đổi trạng thái..."
                class="w-full px-4 py-3 text-slate-700 rounded-xl bg-white/50 border border-slate-200 focus:border-violet-500 focus:ring-4 focus:ring-violet-500/10 outline-none resize-none text-sm"></textarea>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showStatusModal = false" :disabled="formLoading">Hủy</BaseButton>
            <BaseButton variant="primary" :loading="formLoading" @click="handleChangeStatus">Xác nhận</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>