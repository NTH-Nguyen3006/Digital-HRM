<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Clock, Search, RefreshCw, AlertTriangle, ChevronLeft, ChevronRight,
  CheckCircle, XCircle, X, Eye, Calendar, Users, AlertOctagon,
  TrendingUp, Filter
} from 'lucide-vue-next'
import GlassCard from '@/components/common/GlassCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'

import {
  getDailyAttendance,
  getAdjustmentRequests,
  finalizeAdjustmentRequest
} from '@/api/admin/attendance.js'

/* --- TABS --- */
const activeTab = ref('daily') // 'daily' | 'adjustments'

/* --- DATE FILTER --- */
const today = new Date().toISOString().split('T')[0]
const selectedDate = ref(today)

/* --- DAILY ATTENDANCE STATE --- */
const isLoadingDaily = ref(false)
const dailyError = ref('')
const dailyList = ref([])

/* --- ADJUSTMENTS STATE --- */
const isLoadingAdj = ref(false)
const adjError = ref('')
const adjList = ref([])
const adjCurrentPage = ref(1)
const adjTotalPages = ref(0)
const adjTotalElements = ref(0)
const ADJ_PAGE_SIZE = 10
const filterAdjStatus = ref('')

/* --- DETAIL MODAL --- */
const showDetailModal = ref(false)
const detailTarget = ref(null)

/* --- FINALIZE MODAL --- */
const showFinalizeModal = ref(false)
const finalizeTarget = ref(null)
const finalizeForm = ref({ finalStatus: 'APPROVED', note: '' })
const finalizeLoading = ref(false)
const finalizeError = ref('')

/* --- TOAST --- */
const toastMessage = ref('')
const toastType = ref('success')
const toastVisible = ref(false)
const showToast = (msg, type = 'success') => {
  toastMessage.value = msg; toastType.value = type; toastVisible.value = true
  setTimeout(() => { toastVisible.value = false }, 3500)
}

/* --- COMPUTED STATS --- */
const dailyStats = computed(() => ({
  total: dailyList.value.length,
  onTime: dailyList.value.filter(r => r.status === 'ON_TIME' || r.checkInTime).length,
  late: dailyList.value.filter(r => r.status === 'LATE').length,
  absent: dailyList.value.filter(r => !r.checkInTime && r.status !== 'ON_LEAVE').length
}))

/* --- STATUS BADGE --- */
const attendanceBadge = (status) => {
  const map = {
    ON_TIME: { label: 'Đúng giờ', class: 'bg-emerald-100 text-emerald-700 border border-emerald-200' },
    LATE: { label: 'Đi trễ', class: 'bg-amber-100 text-amber-700 border border-amber-200' },
    ABSENT: { label: 'Vắng mặt', class: 'bg-rose-100 text-rose-700 border border-rose-200' },
    ON_LEAVE: { label: 'Nghỉ phép', class: 'bg-indigo-100 text-indigo-700 border border-indigo-200' },
    EARLY_LEAVE: { label: 'Về sớm', class: 'bg-orange-100 text-orange-700 border border-orange-200' }
  }
  return map[status] || { label: status || 'N/A', class: 'bg-slate-100 text-slate-600' }
}

const adjStatusBadge = (status) => {
  const map = {
    PENDING: { label: 'Chờ xử lý', class: 'bg-amber-100 text-amber-700 border border-amber-200' },
    APPROVED: { label: 'Đã duyệt', class: 'bg-emerald-100 text-emerald-700 border border-emerald-200' },
    REJECTED: { label: 'Từ chối', class: 'bg-rose-100 text-rose-700 border border-rose-200' }
  }
  return map[status] || { label: status, class: 'bg-slate-100 text-slate-600' }
}

const formatTime = (timeStr) => {
  if (!timeStr) return '—'
  return timeStr.substring(0, 5)
}

/* --- API CALLS --- */
const fetchDailyAttendance = async () => {
  isLoadingDaily.value = true
  dailyError.value = ''
  try {
    const res = await getDailyAttendance({ date: selectedDate.value })
    dailyList.value = res.content ?? res.data?.content ?? res.data ?? []
  } catch (err) {
    dailyError.value = 'Không thể tải dữ liệu chấm công. Vui lòng thử lại.'
  } finally {
    isLoadingDaily.value = false
  }
}

const fetchAdjustments = async () => {
  isLoadingAdj.value = true
  adjError.value = ''
  try {
    const params = { page: adjCurrentPage.value - 1, size: ADJ_PAGE_SIZE }
    if (filterAdjStatus.value) params.status = filterAdjStatus.value
    const res = await getAdjustmentRequests(params)
    adjList.value = res.content ?? res.data?.content ?? []
    adjTotalPages.value = res.totalPages ?? res.data?.totalPages ?? 1
    adjTotalElements.value = res.totalElements ?? res.data?.totalElements ?? adjList.value.length
  } catch (err) {
    adjError.value = 'Không thể tải danh sách điều chỉnh giờ công.'
  } finally {
    isLoadingAdj.value = false
  }
}

const goToAdjPage = (page) => {
  if (page < 1 || page > adjTotalPages.value) return
  adjCurrentPage.value = page
  fetchAdjustments()
}

const adjVisiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, adjCurrentPage.value - 2)
  const end = Math.min(adjTotalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

/* --- FINALIZE --- */
const openFinalizeModal = (req) => {
  finalizeTarget.value = req
  finalizeForm.value = { finalStatus: 'APPROVED', note: '' }
  finalizeError.value = ''
  showFinalizeModal.value = true
}

const handleFinalize = async () => {
  if (!finalizeForm.value.note.trim() && finalizeForm.value.finalStatus === 'REJECTED') {
    finalizeError.value = 'Vui lòng nhập lý do từ chối'
    return
  }
  finalizeLoading.value = true
  finalizeError.value = ''
  try {
    await finalizeAdjustmentRequest(finalizeTarget.value.adjustmentRequestId, finalizeForm.value)
    showToast('Xử lý yêu cầu điều chỉnh thành công!', 'success')
    showFinalizeModal.value = false
    fetchAdjustments()
  } catch (err) {
    finalizeError.value = err?.response?.data?.message || 'Có lỗi xảy ra'
  } finally {
    finalizeLoading.value = false
  }
}

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'daily') fetchDailyAttendance()
  else fetchAdjustments()
}

onMounted(() => {
  if (localStorage.getItem('isAuthenticated') === 'true') {
    fetchDailyAttendance()
  }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/20 to-slate-100 p-6">

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
          <div class="w-10 h-10 rounded-2xl bg-gradient-to-br from-blue-500 to-cyan-600 flex items-center justify-center shadow-lg shadow-blue-200">
            <Clock class="w-5 h-5 text-white" />
          </div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Dữ liệu Chấm công</h1>
        </div>
        <p class="text-slate-500 text-sm ml-1">Theo dõi giờ vào/ra và xử lý yêu cầu điều chỉnh giờ công</p>
      </div>
    </div>

    <!-- TABS -->
    <div class="flex gap-1 p-1.5 bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 w-fit mb-6 shadow-sm">
      <button v-for="tab in [
        { key: 'daily', label: 'Chấm công hàng ngày', icon: Calendar },
        { key: 'adjustments', label: 'Yêu cầu điều chỉnh', icon: AlertOctagon }
      ]" :key="tab.key" @click="switchTab(tab.key)"
        class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold transition-all duration-200"
        :class="activeTab === tab.key
          ? 'bg-blue-600 text-white shadow-md shadow-blue-200'
          : 'text-slate-500 hover:text-blue-600 hover:bg-blue-50'">
        <component :is="tab.icon" class="w-4 h-4" />
        {{ tab.label }}
      </button>
    </div>

    <!-- ========== TAB: DAILY ATTENDANCE ========== -->
    <div v-if="activeTab === 'daily'">
      <!-- Stats -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-5">
        <GlassCard padding="p-4">
          <p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Tổng nhân viên</p>
          <p class="text-3xl font-bold text-slate-800">{{ dailyStats.total }}</p>
        </GlassCard>
        <GlassCard padding="p-4">
          <p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Đúng giờ</p>
          <p class="text-3xl font-bold text-emerald-600">{{ dailyStats.onTime }}</p>
        </GlassCard>
        <GlassCard padding="p-4">
          <p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Đi trễ</p>
          <p class="text-3xl font-bold text-amber-600">{{ dailyStats.late }}</p>
        </GlassCard>
        <GlassCard padding="p-4">
          <p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Vắng mặt</p>
          <p class="text-3xl font-bold text-rose-600">{{ dailyStats.absent }}</p>
        </GlassCard>
      </div>

      <!-- Filter Bar -->
      <GlassCard padding="p-4" class="mb-5">
        <div class="flex flex-wrap gap-3 items-center">
          <BaseInput v-model="selectedDate" type="date" @change="fetchDailyAttendance" label="Ngày" />
          <BaseButton variant="outline" size="sm" :icon="RefreshCw" @click="fetchDailyAttendance" :loading="isLoadingDaily">Tải lại</BaseButton>
        </div>
      </GlassCard>

      <div v-if="dailyError" class="mb-4 p-4 bg-rose-50 border border-rose-200 rounded-2xl flex items-center gap-3 text-rose-700 text-sm">
        <AlertTriangle class="w-5 h-5 flex-shrink-0" />{{ dailyError }}
      </div>

      <GlassCard padding="p-0" class="overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full min-w-[800px]">
            <thead class="bg-slate-50/80 border-b border-slate-200/60">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Nhân viên</th>
                <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Check-in</th>
                <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Check-out</th>
                <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Số giờ</th>
                <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Ca làm</th>
                <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              <template v-if="isLoadingDaily">
                <tr v-for="i in 8" :key="i" class="border-b border-slate-100">
                  <td class="px-4 py-3"><div class="flex items-center gap-2"><div class="w-8 h-8 rounded-xl bg-slate-200 animate-pulse"></div><div class="h-3 w-28 bg-slate-200 rounded animate-pulse"></div></div></td>
                  <td class="px-4 py-3 text-center"><div class="h-3 w-14 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
                  <td class="px-4 py-3 text-center"><div class="h-3 w-14 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
                  <td class="px-4 py-3 text-center"><div class="h-3 w-10 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
                  <td class="px-4 py-3"><div class="h-3 w-20 bg-slate-200 rounded animate-pulse"></div></td>
                  <td class="px-4 py-3"><div class="h-6 w-20 bg-slate-200 rounded-full animate-pulse"></div></td>
                </tr>
              </template>
              <tr v-else-if="dailyList.length === 0">
                <td colspan="6" class="text-center py-16">
                  <div class="flex flex-col items-center gap-3">
                    <div class="w-14 h-14 rounded-3xl bg-blue-50 flex items-center justify-center"><Clock class="w-7 h-7 text-blue-400" /></div>
                    <p class="font-semibold text-slate-500">Không có dữ liệu chấm công cho ngày này</p>
                  </div>
                </td>
              </tr>
              <tr v-else v-for="rec in dailyList" :key="rec.attendanceId ?? rec.employeeId"
                  class="border-b border-slate-100 hover:bg-blue-50/30 transition-colors group">
                <td class="px-4 py-3">
                  <div class="flex items-center gap-2">
                    <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-blue-400 to-cyan-500 flex items-center justify-center text-white font-bold text-sm shadow-sm">{{ (rec.fullName || '?').charAt(0) }}</div>
                    <div>
                      <p class="font-semibold text-slate-800 text-sm">{{ rec.fullName }}</p>
                      <p class="text-xs text-slate-400">{{ rec.employeeCode }}</p>
                    </div>
                  </div>
                </td>
                <td class="px-4 py-3 text-center">
                  <span class="text-sm font-semibold" :class="rec.checkInTime ? 'text-emerald-600' : 'text-slate-300'">{{ formatTime(rec.checkInTime) }}</span>
                </td>
                <td class="px-4 py-3 text-center">
                  <span class="text-sm font-semibold" :class="rec.checkOutTime ? 'text-slate-700' : 'text-slate-300'">{{ formatTime(rec.checkOutTime) }}</span>
                </td>
                <td class="px-4 py-3 text-center">
                  <span class="text-sm font-bold text-slate-700">{{ rec.totalHours ? rec.totalHours + 'h' : '—' }}</span>
                </td>
                <td class="px-4 py-3 text-sm text-slate-600">{{ rec.shiftName || '—' }}</td>
                <td class="px-4 py-3">
                  <span class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold" :class="attendanceBadge(rec.status).class">
                    {{ attendanceBadge(rec.status).label }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </GlassCard>
    </div>

    <!-- ========== TAB: ADJUSTMENT REQUESTS ========== -->
    <div v-if="activeTab === 'adjustments'">
      <GlassCard padding="p-4" class="mb-5">
        <div class="flex items-center gap-3">
          <select v-model="filterAdjStatus" @change="adjCurrentPage = 1; fetchAdjustments()"
            class="h-10 px-3 text-sm text-slate-700 rounded-xl bg-white/80 border border-slate-200 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 outline-none transition-all">
            <option value="">Tất cả trạng thái</option>
            <option value="PENDING">Chờ xử lý</option>
            <option value="APPROVED">Đã duyệt</option>
            <option value="REJECTED">Từ chối</option>
          </select>
          <BaseButton variant="outline" size="sm" :icon="RefreshCw" @click="fetchAdjustments" :loading="isLoadingAdj">Tải lại</BaseButton>
        </div>
      </GlassCard>

      <div v-if="adjError" class="mb-4 p-4 bg-rose-50 border border-rose-200 rounded-2xl flex items-center gap-3 text-rose-700 text-sm">
        <AlertTriangle class="w-5 h-5 flex-shrink-0" />{{ adjError }}
      </div>

      <GlassCard padding="p-0" class="overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full min-w-[900px]">
            <thead class="bg-slate-50/80 border-b border-slate-200/60">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Nhân viên</th>
                <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Ngày yêu cầu</th>
                <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Giờ vào (điều chỉnh)</th>
                <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Giờ ra (điều chỉnh)</th>
                <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Lý do</th>
                <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Trạng thái</th>
                <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <template v-if="isLoadingAdj">
                <tr v-for="i in 5" :key="i" class="border-b border-slate-100">
                  <td class="px-4 py-3"><div class="h-3 w-28 bg-slate-200 rounded animate-pulse"></div></td>
                  <td class="px-4 py-3"><div class="h-3 w-20 bg-slate-200 rounded animate-pulse"></div></td>
                  <td class="px-4 py-3 text-center"><div class="h-3 w-14 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
                  <td class="px-4 py-3 text-center"><div class="h-3 w-14 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
                  <td class="px-4 py-3"><div class="h-3 w-40 bg-slate-200 rounded animate-pulse"></div></td>
                  <td class="px-4 py-3"><div class="h-6 w-20 bg-slate-200 rounded-full animate-pulse"></div></td>
                  <td class="px-4 py-3"><div class="h-3 w-20 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
                </tr>
              </template>
              <tr v-else-if="adjList.length === 0">
                <td colspan="7" class="text-center py-14">
                  <div class="flex flex-col items-center gap-3">
                    <div class="w-14 h-14 rounded-3xl bg-amber-50 flex items-center justify-center"><AlertOctagon class="w-7 h-7 text-amber-400" /></div>
                    <p class="font-semibold text-slate-500">Không có yêu cầu điều chỉnh nào</p>
                  </div>
                </td>
              </tr>
              <tr v-else v-for="req in adjList" :key="req.adjustmentRequestId"
                  class="border-b border-slate-100 hover:bg-blue-50/20 transition-colors group">
                <td class="px-4 py-3">
                  <p class="font-semibold text-slate-800 text-sm">{{ req.employeeName || req.fullName }}</p>
                  <p class="text-xs text-slate-400">{{ req.employeeCode }}</p>
                </td>
                <td class="px-4 py-3 text-sm text-slate-600">{{ req.requestDate ? new Date(req.requestDate).toLocaleDateString('vi-VN') : '—' }}</td>
                <td class="px-4 py-3 text-center text-sm font-semibold text-blue-600">{{ formatTime(req.requestedCheckIn) }}</td>
                <td class="px-4 py-3 text-center text-sm font-semibold text-blue-600">{{ formatTime(req.requestedCheckOut) }}</td>
                <td class="px-4 py-3 text-sm text-slate-600 max-w-[200px] truncate" :title="req.reason">{{ req.reason || '—' }}</td>
                <td class="px-4 py-3">
                  <span class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold" :class="adjStatusBadge(req.status).class">
                    {{ adjStatusBadge(req.status).label }}
                  </span>
                </td>
                <td class="px-4 py-3">
                  <div class="flex items-center justify-center gap-1 opacity-60 group-hover:opacity-100 transition-opacity">
                    <button v-if="req.status === 'PENDING'" @click="openFinalizeModal(req)"
                      class="px-3 py-1.5 rounded-lg bg-blue-100 text-blue-700 text-xs font-semibold hover:bg-blue-200 transition-all">
                      Xử lý
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- PAGINATION -->
        <div v-if="!isLoadingAdj && adjList.length > 0" class="flex items-center justify-between px-6 py-4 border-t border-slate-200/60 bg-slate-50/40">
          <p class="text-sm text-slate-500">Trang <span class="font-semibold">{{ adjCurrentPage }}</span> / {{ adjTotalPages }}</p>
          <div class="flex items-center gap-1.5">
            <button @click="goToAdjPage(adjCurrentPage - 1)" :disabled="adjCurrentPage <= 1"
              class="w-9 h-9 rounded-xl flex items-center justify-center hover:bg-blue-100 disabled:opacity-30 transition-all border border-slate-200">
              <ChevronLeft class="w-4 h-4 text-slate-500" />
            </button>
            <button v-for="p in adjVisiblePages" :key="p" @click="goToAdjPage(p)"
              class="w-9 h-9 rounded-xl text-sm font-semibold transition-all border"
              :class="adjCurrentPage === p ? 'bg-blue-600 text-white border-blue-600' : 'text-slate-600 hover:bg-blue-50 border-slate-200'">
              {{ p }}
            </button>
            <button @click="goToAdjPage(adjCurrentPage + 1)" :disabled="adjCurrentPage >= adjTotalPages"
              class="w-9 h-9 rounded-xl flex items-center justify-center hover:bg-blue-100 disabled:opacity-30 transition-all border border-slate-200">
              <ChevronRight class="w-4 h-4 text-slate-500" />
            </button>
          </div>
        </div>
      </GlassCard>
    </div>

    <!-- MODAL: FINALIZE ADJUSTMENT -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showFinalizeModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showFinalizeModal = false">
        <div class="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="px-6 py-4 bg-gradient-to-r from-blue-600 to-cyan-600">
            <h3 class="text-white font-semibold">Xử lý yêu cầu điều chỉnh giờ công</h3>
            <p class="text-blue-100 text-sm mt-0.5">{{ finalizeTarget?.employeeName }}</p>
          </div>
          <div class="p-6 space-y-4">
            <div v-if="finalizeError" class="p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-sm flex items-center gap-2">
              <AlertTriangle class="w-4 h-4 flex-shrink-0" />{{ finalizeError }}
            </div>
            <div>
              <label class="block text-sm font-semibold text-slate-700 mb-1.5">Quyết định <span class="text-red-500">*</span></label>
              <div class="flex gap-3">
                <label class="flex items-center gap-2 cursor-pointer p-3 flex-1 rounded-xl border transition-all" :class="finalizeForm.finalStatus === 'APPROVED' ? 'border-emerald-500 bg-emerald-50' : 'border-slate-200 hover:border-slate-300'">
                  <input type="radio" v-model="finalizeForm.finalStatus" value="APPROVED" class="accent-emerald-600" />
                  <span class="text-sm font-semibold text-emerald-700">Chấp thuận</span>
                </label>
                <label class="flex items-center gap-2 cursor-pointer p-3 flex-1 rounded-xl border transition-all" :class="finalizeForm.finalStatus === 'REJECTED' ? 'border-rose-500 bg-rose-50' : 'border-slate-200 hover:border-slate-300'">
                  <input type="radio" v-model="finalizeForm.finalStatus" value="REJECTED" class="accent-rose-600" />
                  <span class="text-sm font-semibold text-rose-700">Từ chối</span>
                </label>
              </div>
            </div>
            <div>
              <label class="block text-sm font-semibold text-slate-700 mb-1.5">
                Ghi chú {{ finalizeForm.finalStatus === 'REJECTED' ? '(bắt buộc)' : '(tuỳ chọn)' }}
                <span v-if="finalizeForm.finalStatus === 'REJECTED'" class="text-red-500">*</span>
              </label>
              <textarea v-model="finalizeForm.note" rows="3" class="w-full px-4 py-3 text-sm text-slate-700 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 outline-none resize-none" placeholder="Nhập ghi chú..."></textarea>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showFinalizeModal = false" :disabled="finalizeLoading">Hủy</BaseButton>
            <BaseButton variant="primary" :loading="finalizeLoading" @click="handleFinalize">Xác nhận</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>
