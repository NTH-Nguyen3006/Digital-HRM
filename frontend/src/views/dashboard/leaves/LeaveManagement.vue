<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import {
  CheckCircle, XCircle, Clock, Search, Eye, AlertTriangle,
  ChevronLeft, ChevronRight, X, CalendarDays, User, MessageSquare,
  Filter, RefreshCw, ClipboardList
} from 'lucide-vue-next'
import GlassCard from '@/components/common/GlassCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import {
  getPendingLeaveRequests,
  reviewLeaveRequest
} from '@/api/manager/manager.js'

/* --- STATE --- */
const isLoading = ref(false)
const errorMessage = ref('')
const list = ref([])
const currentPage = ref(1)
const totalPages = ref(0)
const totalElements = ref(0)
const PAGE_SIZE = 10

/* --- FILTER STATE --- */
const keyword = ref('')
const filterDateFrom = ref('')
const filterDateTo = ref('')

/* --- MODAL STATE: APPROVE --- */
const showApproveModal = ref(false)
const approveTarget = ref(null)
const approveNote = ref('')
const approveLoading = ref(false)

/* --- MODAL STATE: REJECT (BẮT BUỘC nhập lý do theo STEEL LAW) --- */
const showRejectModal = ref(false)
const rejectTarget = ref(null)
const rejectNote = ref('')
const rejectNoteError = ref('')
const rejectLoading = ref(false)

/* --- MODAL STATE: DETAIL --- */
const showDetailModal = ref(false)
const detailTarget = ref(null)

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

/* --- STATUS BADGE --- */
const statusBadge = computed(() => (status) => {
  const map = {
    PENDING: { label: 'Chờ duyệt', class: 'bg-amber-100 text-amber-700 border border-amber-200', icon: Clock },
    APPROVED: { label: 'Đã duyệt', class: 'bg-emerald-100 text-emerald-700 border border-emerald-200', icon: CheckCircle },
    REJECTED: { label: 'Từ chối', class: 'bg-rose-100 text-rose-700 border border-rose-200', icon: XCircle },
    FINALIZED: { label: 'Đã chốt HR', class: 'bg-indigo-100 text-indigo-700 border border-indigo-200', icon: CheckCircle }
  }
  return map[status] || { label: status, class: 'bg-slate-100 text-slate-600', icon: Clock }
})

/* --- LEAVE TYPE LABEL --- */
const leaveTypeLabel = (type) => {
  const map = {
    ANNUAL: 'Nghỉ phép năm',
    SICK: 'Nghỉ bệnh',
    MATERNITY: 'Nghỉ thai sản',
    PATERNITY: 'Nghỉ phép cha',
    UNPAID: 'Nghỉ không lương',
    COMPENSATORY: 'Nghỉ bù'
  }
  return map[type] || type
}

/* --- DEBOUNCE SEARCH --- */
let searchTimer = null
const onSearchInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    fetchLeaveRequests()
  }, 500)
}

const onFilterChange = () => {
  currentPage.value = 1
  fetchLeaveRequests()
}

/* --- API CALL --- */
const fetchLeaveRequests = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const params = {
      page: currentPage.value - 1, // 0-indexed
      size: PAGE_SIZE
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (filterDateFrom.value) params.fromDate = filterDateFrom.value
    if (filterDateTo.value) params.toDate = filterDateTo.value

    const res = await getPendingLeaveRequests(params)
    list.value = res.content ?? res.data?.content ?? res ?? []
    totalPages.value = res.totalPages ?? res.data?.totalPages ?? 1
    totalElements.value = res.totalElements ?? res.data?.totalElements ?? list.value.length
  } catch (err) {
    errorMessage.value = 'Không thể tải danh sách đơn phép. Vui lòng thử lại.'
    console.error(err)
  } finally {
    isLoading.value = false
  }
}

/* --- APPROVE FLOW --- */
const openApproveModal = (req) => {
  approveTarget.value = req
  approveNote.value = ''
  showApproveModal.value = true
}

const handleApprove = async () => {
  approveLoading.value = true
  try {
    await reviewLeaveRequest(approveTarget.value.leaveRequestId, {
      status: 'APPROVED',
      note: approveNote.value.trim() || null
    })
    showToast('Đã duyệt đơn nghỉ phép thành công!', 'success')
    showApproveModal.value = false
    await fetchLeaveRequests()
  } catch (err) {
    showToast('Lỗi khi duyệt đơn: ' + (err?.response?.data?.message || 'Thử lại sau'), 'error')
  } finally {
    approveLoading.value = false
  }
}

/* --- REJECT FLOW (BẮT BUỘC nhập lý do — STEEL LAW) --- */
const openRejectModal = (req) => {
  rejectTarget.value = req
  rejectNote.value = ''
  rejectNoteError.value = ''
  showRejectModal.value = true
}

const handleReject = async () => {
  // Validate: BẮT BUỘC nhập lý do từ chối
  if (!rejectNote.value.trim()) {
    rejectNoteError.value = 'Vui lòng nhập lý do từ chối đơn phép'
    return
  }

  rejectLoading.value = true
  rejectNoteError.value = ''
  try {
    await reviewLeaveRequest(rejectTarget.value.leaveRequestId, {
      status: 'REJECTED',
      note: rejectNote.value.trim()
    })
    showToast('Đã từ chối đơn nghỉ phép.', 'success')
    showRejectModal.value = false
    await fetchLeaveRequests()
  } catch (err) {
    rejectNoteError.value = err?.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại.'
  } finally {
    rejectLoading.value = false
  }
}

/* --- DETAIL --- */
const openDetail = (req) => {
  detailTarget.value = req
  showDetailModal.value = true
}

/* --- PAGINATION --- */
const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchLeaveRequests()
}

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

/* --- CLEAR FILTER --- */
const clearFilters = () => {
  keyword.value = ''
  filterDateFrom.value = ''
  filterDateTo.value = ''
  currentPage.value = 1
  fetchLeaveRequests()
}

/* --- COMPUTE DAYS --- */
const calcDays = (from, to) => {
  if (!from || !to) return '—'
  const d1 = new Date(from)
  const d2 = new Date(to)
  const diff = Math.ceil((d2 - d1) / (1000 * 60 * 60 * 24)) + 1
  return diff + ' ngày'
}

/* --- LIFECYCLE --- */
const router = useRouter()
onMounted(() => {
  // Chỉ gọi API khi đã đăng nhập (tránh 401 → redirect)
  if (localStorage.getItem('isAuthenticated') === 'true') {
    fetchLeaveRequests()
  }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-amber-50/20 to-slate-100 p-6">

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
          <div class="w-10 h-10 rounded-2xl bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center shadow-lg shadow-amber-200">
            <ClipboardList class="w-5 h-5 text-white" />
          </div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Duyệt Đơn Nghỉ Phép</h1>
        </div>
        <p class="text-slate-500 text-sm ml-1">
          Xem xét và phê duyệt các đơn xin nghỉ phép từ thành viên trong nhóm
          <span v-if="totalElements > 0" class="font-semibold text-amber-600">({{ totalElements }} đơn)</span>
        </p>
      </div>
      <BaseButton variant="outline" size="sm" :icon="RefreshCw" @click="fetchLeaveRequests" :loading="isLoading">
        Làm mới
      </BaseButton>
    </div>

    <!-- FILTER BAR -->
    <GlassCard padding="p-4" class="mb-5">
      <div class="flex flex-col lg:flex-row gap-3">
        <div class="flex-1">
          <BaseInput
            v-model="keyword"
            @input="onSearchInput"
            type="text"
            placeholder="Tìm theo tên nhân viên..."
            :icon="Search"
          />
        </div>
        <div class="flex flex-wrap gap-2 items-center">
          <div class="flex items-center gap-2">
            <span class="text-slate-500 font-medium text-sm whitespace-nowrap">Từ ngày:</span>
            <BaseInput v-model="filterDateFrom" type="date" @change="onFilterChange" />
          </div>
          <div class="flex items-center gap-2">
            <span class="text-slate-500 font-medium text-sm whitespace-nowrap">Đến ngày:</span>
            <BaseInput v-model="filterDateTo" type="date" @change="onFilterChange" />
          </div>
          <BaseButton
            v-if="keyword || filterDateFrom || filterDateTo"
            variant="ghost" size="sm" :icon="X" @click="clearFilters"
            class="text-rose-500 hover:bg-rose-50">
            Xóa lọc
          </BaseButton>
        </div>
      </div>
    </GlassCard>

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
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Nhân viên</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Loại phép</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Thời gian nghỉ</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Số ngày</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Lý do</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Trạng thái</th>
              <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <!-- LOADING SKELETON -->
            <template v-if="isLoading">
              <tr v-for="i in 6" :key="'sk-' + i" class="border-b border-slate-100">
                <td class="px-4 py-4">
                  <div class="flex items-center gap-2">
                    <div class="w-8 h-8 rounded-xl bg-slate-200 animate-pulse"></div>
                    <div class="space-y-1.5">
                      <div class="h-3 w-24 bg-slate-200 rounded animate-pulse"></div>
                      <div class="h-2.5 w-16 bg-slate-100 rounded animate-pulse"></div>
                    </div>
                  </div>
                </td>
                <td class="px-4 py-4"><div class="h-3 w-28 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-4"><div class="h-3 w-32 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-4"><div class="h-3 w-12 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-4"><div class="h-3 w-40 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-4"><div class="h-6 w-20 bg-slate-200 rounded-full animate-pulse"></div></td>
                <td class="px-4 py-4"><div class="h-3 w-32 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
              </tr>
            </template>

            <!-- EMPTY STATE -->
            <tr v-else-if="list.length === 0">
              <td colspan="7" class="text-center py-20">
                <div class="flex flex-col items-center gap-4">
                  <div class="w-16 h-16 rounded-3xl bg-amber-50 flex items-center justify-center">
                    <ClipboardList class="w-8 h-8 text-amber-400" />
                  </div>
                  <div>
                    <p class="font-semibold text-slate-600 text-base">Không có đơn phép nào</p>
                    <p class="text-sm text-slate-400 mt-1">Chưa có đơn xin nghỉ phép nào chờ duyệt từ nhóm của bạn</p>
                  </div>
                </div>
              </td>
            </tr>

            <!-- DATA ROWS -->
            <tr v-else v-for="req in list" :key="req.leaveRequestId"
                class="border-b border-slate-100/60 hover:bg-amber-50/30 transition-colors duration-150 group">
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-white font-bold text-sm shadow-sm flex-shrink-0">
                    {{ (req.employeeName || req.fullName || '?').charAt(0).toUpperCase() }}
                  </div>
                  <div>
                    <p class="font-semibold text-slate-800 text-sm leading-tight">{{ req.employeeName || req.fullName || '—' }}</p>
                    <p class="text-xs text-slate-400">{{ req.employeeCode || '' }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3">
                <span class="text-sm font-medium text-slate-700">{{ leaveTypeLabel(req.leaveTypeName || req.leaveType) }}</span>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5 text-sm text-slate-600">
                  <CalendarDays class="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                  <span>{{ req.startDate ? new Date(req.startDate).toLocaleDateString('vi-VN') : '—' }}</span>
                  <span class="text-slate-300">→</span>
                  <span>{{ req.endDate ? new Date(req.endDate).toLocaleDateString('vi-VN') : '—' }}</span>
                </div>
              </td>
              <td class="px-4 py-3">
                <span class="inline-flex items-center justify-center w-16 h-7 rounded-xl bg-amber-100 text-amber-700 text-xs font-bold border border-amber-200">
                  {{ req.totalDays ? req.totalDays + ' ngày' : calcDays(req.startDate, req.endDate) }}
                </span>
              </td>
              <td class="px-4 py-3">
                <p class="text-sm text-slate-600 max-w-[200px] truncate" :title="req.reason">{{ req.reason || '—' }}</p>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5">
                  <component :is="statusBadge(req.status).icon" class="w-3.5 h-3.5" />
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold"
                        :class="statusBadge(req.status).class">
                    {{ statusBadge(req.status).label }}
                  </span>
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center justify-center gap-1.5 opacity-60 group-hover:opacity-100 transition-opacity">
                  <!-- View Detail -->
                  <button @click="openDetail(req)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-indigo-100 hover:text-indigo-600 transition-all"
                    title="Xem chi tiết">
                    <Eye class="w-4 h-4" />
                  </button>

                  <!-- Approve (chỉ hiện khi PENDING) -->
                  <button v-if="req.status === 'PENDING'"
                    @click="openApproveModal(req)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-emerald-100 hover:text-emerald-600 transition-all"
                    title="Duyệt đơn">
                    <CheckCircle class="w-4 h-4" />
                  </button>

                  <!-- Reject (STEEL LAW: BẮT BUỘC mở modal nhập lý do) -->
                  <button v-if="req.status === 'PENDING'"
                    @click="openRejectModal(req)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-rose-100 hover:text-rose-600 transition-all"
                    title="Từ chối đơn">
                    <XCircle class="w-4 h-4" />
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
          <span class="font-semibold text-amber-600">{{ totalElements }}</span> đơn
        </p>
        <div class="flex items-center gap-1.5">
          <button @click="goToPage(currentPage - 1)" :disabled="currentPage <= 1"
            class="w-9 h-9 rounded-xl flex items-center justify-center text-slate-500 hover:bg-amber-100 hover:text-amber-600 disabled:opacity-30 disabled:cursor-not-allowed transition-all border border-slate-200">
            <ChevronLeft class="w-4 h-4" />
          </button>
          <button v-for="page in visiblePages" :key="page" @click="goToPage(page)"
            class="w-9 h-9 rounded-xl text-sm font-semibold transition-all border"
            :class="currentPage === page ? 'bg-amber-500 text-white border-amber-500 shadow-lg shadow-amber-200' : 'text-slate-600 hover:bg-amber-50 border-slate-200'">
            {{ page }}
          </button>
          <button @click="goToPage(currentPage + 1)" :disabled="currentPage >= totalPages"
            class="w-9 h-9 rounded-xl flex items-center justify-center text-slate-500 hover:bg-amber-100 hover:text-amber-600 disabled:opacity-30 disabled:cursor-not-allowed transition-all border border-slate-200">
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>
    </GlassCard>

    <!-- ===================== MODAL: DUYỆT ĐƠN ===================== -->
    <Transition enter-active-class="transition-all duration-300 ease-out"
                enter-from-class="opacity-0" enter-to-class="opacity-100"
                leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showApproveModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showApproveModal = false">
        <div class="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="px-6 py-4 bg-gradient-to-r from-emerald-500 to-teal-600">
            <div class="flex items-center gap-3">
              <CheckCircle class="w-5 h-5 text-white" />
              <h3 class="text-white font-semibold">Xác nhận duyệt đơn nghỉ phép</h3>
            </div>
            <p class="text-emerald-100 text-sm mt-1">{{ approveTarget?.employeeName || approveTarget?.fullName }}</p>
          </div>
          <div class="p-6 space-y-4">
            <div class="p-4 bg-emerald-50 rounded-2xl border border-emerald-200 space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-slate-500">Loại phép:</span>
                <span class="font-semibold text-slate-700">{{ leaveTypeLabel(approveTarget?.leaveTypeName || approveTarget?.leaveType) }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-slate-500">Thời gian:</span>
                <span class="font-semibold text-slate-700">
                  {{ approveTarget?.startDate ? new Date(approveTarget.startDate).toLocaleDateString('vi-VN') : '—' }}
                  → {{ approveTarget?.endDate ? new Date(approveTarget.endDate).toLocaleDateString('vi-VN') : '—' }}
                </span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-slate-500">Lý do:</span>
                <span class="font-semibold text-slate-700 text-right max-w-[200px]">{{ approveTarget?.reason || '—' }}</span>
              </div>
            </div>

            <div class="w-full space-y-1.5">
              <label class="block text-sm font-semibold text-slate-700 ml-1">Ghi chú của Quản lý (tuỳ chọn)</label>
              <textarea v-model="approveNote" rows="3" placeholder="Nhập ghi chú nếu có..."
                class="w-full px-4 py-3 text-slate-700 rounded-xl bg-white/50 border border-slate-200 focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 outline-none resize-none text-sm"></textarea>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showApproveModal = false" :disabled="approveLoading">Hủy</BaseButton>
            <BaseButton variant="primary" :loading="approveLoading" @click="handleApprove">Xác nhận duyệt</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ===================== MODAL: TỪ CHỐI (BẮT BUỘC LÝ DO — STEEL LAW) ===================== -->
    <Transition enter-active-class="transition-all duration-300 ease-out"
                enter-from-class="opacity-0" enter-to-class="opacity-100"
                leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showRejectModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showRejectModal = false">
        <div class="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="px-6 py-4 bg-gradient-to-r from-rose-500 to-red-600">
            <div class="flex items-center gap-3">
              <XCircle class="w-5 h-5 text-white" />
              <h3 class="text-white font-semibold">Từ chối đơn nghỉ phép</h3>
            </div>
            <p class="text-rose-100 text-sm mt-1">{{ rejectTarget?.employeeName || rejectTarget?.fullName }}</p>
          </div>
          <div class="p-6 space-y-4">
            <div v-if="rejectNoteError" class="p-3 bg-rose-50 border border-rose-200 rounded-xl flex items-center gap-2 text-rose-700 text-sm">
              <AlertTriangle class="w-4 h-4 flex-shrink-0" />
              {{ rejectNoteError }}
            </div>

            <div class="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-1.5">
              <div class="flex justify-between text-sm">
                <span class="text-slate-500">Nhân viên:</span>
                <span class="font-semibold text-slate-700">{{ rejectTarget?.employeeName || rejectTarget?.fullName }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-slate-500">Loại phép:</span>
                <span class="font-semibold text-slate-700">{{ leaveTypeLabel(rejectTarget?.leaveTypeName || rejectTarget?.leaveType) }}</span>
              </div>
            </div>

            <div class="w-full space-y-1.5">
              <label class="block text-sm font-semibold text-slate-700 ml-1">
                Lý do từ chối <span class="text-red-500">*</span>
              </label>
              <textarea v-model="rejectNote" rows="4"
                placeholder="Nhập lý do từ chối đơn nghỉ phép (bắt buộc)..."
                class="w-full px-4 py-3 text-slate-700 rounded-xl bg-white/50 border focus:outline-none focus:ring-4 outline-none resize-none text-sm transition-all"
                :class="rejectNoteError ? 'border-red-400 focus:border-red-500 focus:ring-red-500/10' : 'border-slate-200 focus:border-rose-500 focus:ring-rose-500/10'"></textarea>
              <p class="text-xs text-slate-400 ml-1">Lý do sẽ được gửi thông báo đến nhân viên</p>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showRejectModal = false" :disabled="rejectLoading">Hủy</BaseButton>
            <BaseButton variant="danger" :loading="rejectLoading" @click="handleReject">Xác nhận từ chối</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ===================== MODAL: CHI TIẾT ĐƠN PHÉP ===================== -->
    <Transition enter-active-class="transition-all duration-300 ease-out"
                enter-from-class="opacity-0" enter-to-class="opacity-100"
                leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showDetailModal = false">
        <div class="w-full max-w-lg bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="px-6 py-4 bg-gradient-to-r from-indigo-600 to-violet-600 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <Eye class="w-5 h-5 text-white" />
              <h3 class="text-white font-semibold">Chi tiết đơn nghỉ phép</h3>
            </div>
            <button @click="showDetailModal = false" class="w-8 h-8 rounded-xl bg-white/10 hover:bg-white/20 flex items-center justify-center text-white transition-all">
              <X class="w-4 h-4" />
            </button>
          </div>
          <div class="p-6 space-y-3" v-if="detailTarget">
            <div class="grid grid-cols-2 gap-3">
              <div class="col-span-2 p-4 bg-indigo-50 rounded-2xl border border-indigo-100">
                <p class="text-xs text-indigo-500 font-semibold uppercase tracking-wide mb-0.5">Nhân viên</p>
                <p class="font-bold text-slate-800">{{ detailTarget.employeeName || detailTarget.fullName }}</p>
                <p class="text-slate-500 text-sm">{{ detailTarget.employeeCode }}</p>
              </div>
              <div class="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <p class="text-xs text-slate-400 font-semibold uppercase tracking-wide mb-0.5">Loại phép</p>
                <p class="font-semibold text-slate-700 text-sm">{{ leaveTypeLabel(detailTarget.leaveTypeName || detailTarget.leaveType) }}</p>
              </div>
              <div class="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <p class="text-xs text-slate-400 font-semibold uppercase tracking-wide mb-0.5">Trạng thái</p>
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold" :class="statusBadge(detailTarget.status).class">
                  {{ statusBadge(detailTarget.status).label }}
                </span>
              </div>
              <div class="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <p class="text-xs text-slate-400 font-semibold uppercase tracking-wide mb-0.5">Ngày bắt đầu</p>
                <p class="font-semibold text-slate-700 text-sm">{{ detailTarget.startDate ? new Date(detailTarget.startDate).toLocaleDateString('vi-VN') : '—' }}</p>
              </div>
              <div class="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <p class="text-xs text-slate-400 font-semibold uppercase tracking-wide mb-0.5">Ngày kết thúc</p>
                <p class="font-semibold text-slate-700 text-sm">{{ detailTarget.endDate ? new Date(detailTarget.endDate).toLocaleDateString('vi-VN') : '—' }}</p>
              </div>
              <div class="col-span-2 p-3 bg-slate-50 rounded-xl border border-slate-100">
                <p class="text-xs text-slate-400 font-semibold uppercase tracking-wide mb-0.5">Lý do nghỉ phép</p>
                <p class="text-slate-700 text-sm">{{ detailTarget.reason || '—' }}</p>
              </div>
              <div v-if="detailTarget.managerNote" class="col-span-2 p-3 bg-amber-50 rounded-xl border border-amber-100">
                <p class="text-xs text-amber-500 font-semibold uppercase tracking-wide mb-0.5">Ghi chú Quản lý</p>
                <p class="text-slate-700 text-sm">{{ detailTarget.managerNote }}</p>
              </div>
            </div>
          </div>
          <div class="flex justify-end px-6 py-4 border-t border-slate-100">
            <BaseButton variant="outline" @click="showDetailModal = false">Đóng</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>
