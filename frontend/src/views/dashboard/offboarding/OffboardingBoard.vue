<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  UserMinus, Search, Eye, AlertTriangle, RefreshCw, ChevronLeft, ChevronRight,
  X, ShieldOff, Package, CreditCard, CheckCircle, Clock, AlertOctagon, Zap
} from 'lucide-vue-next'
import GlassCard from '@/components/common/GlassCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import {
  getOffboardings,
  finalizeOffboarding,
  revokeAccess,
  closeOffboarding,
  processSettlement
} from '@/api/admin/offboarding.js'

/* --- STATE --- */
const isLoading = ref(false)
const errorMessage = ref('')
const list = ref([])
const currentPage = ref(1)
const totalPages = ref(0)
const totalElements = ref(0)
const PAGE_SIZE = 10

/* --- FILTER --- */
const filterStatus = ref('')

/* --- MODAL: DETAIL --- */
const showDetailModal = ref(false)
const detailTarget = ref(null)

/* --- MODAL: FINALIZE --- */
const showFinalizeModal = ref(false)
const finalizeTarget = ref(null)
const finalizeForm = ref({ note: '' })
const finalizeLoading = ref(false)
const finalizeError = ref('')

/* --- ACTION LOADING --- */
const actionLoading = ref({})

/* --- CONFIRM --- */
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
    INITIATED: { label: 'Khởi tạo', class: 'bg-amber-100 text-amber-700 border-amber-200', icon: Clock },
    IN_PROGRESS: { label: 'Đang xử lý', class: 'bg-blue-100 text-blue-700 border-blue-200', icon: Zap },
    PENDING_SETTLEMENT: { label: 'Chờ thanh toán', class: 'bg-orange-100 text-orange-700 border-orange-200', icon: CreditCard },
    COMPLETED: { label: 'Hoàn tất', class: 'bg-emerald-100 text-emerald-700 border-emerald-200', icon: CheckCircle },
    CLOSED: { label: 'Đã đóng', class: 'bg-slate-100 text-slate-500 border-slate-200', icon: CheckCircle }
  }
  return map[status] || { label: status, class: 'bg-slate-100 text-slate-600', icon: Clock }
}

/* --- STATS --- */
const stats = computed(() => ({
  total: list.value.length,
  inProgress: list.value.filter(o => o.status === 'IN_PROGRESS').length,
  pending: list.value.filter(o => o.status === 'INITIATED').length,
  completed: list.value.filter(o => o.status === 'COMPLETED' || o.status === 'CLOSED').length
}))

/* --- API --- */
const fetchOffboardings = async () => {
  isLoading.value = true; errorMessage.value = ''
  try {
    const params = { page: currentPage.value - 1, size: PAGE_SIZE }
    if (filterStatus.value) params.status = filterStatus.value
    const res = await getOffboardings(params)
    list.value = res.content ?? res.data?.content ?? []
    totalPages.value = res.totalPages ?? res.data?.totalPages ?? 1
    totalElements.value = res.totalElements ?? res.data?.totalElements ?? list.value.length
  } catch (err) {
    errorMessage.value = 'Không thể tải danh sách offboarding.'
  } finally {
    isLoading.value = false
  }
}

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page; fetchOffboardings()
}
const visiblePages = computed(() => {
  const pages = []; const start = Math.max(1, currentPage.value - 2); const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i); return pages
})

/* --- ACTIONS --- */
const handleRevokeAccess = (ob) => {
  confirmConfig.value = {
    title: 'Thu hồi quyền truy cập',
    message: `Thu hồi tài khoản hệ thống của "${ob.employeeName}"? Nhân viên sẽ không thể đăng nhập.`,
    type: 'danger', loading: false,
    action: async () => {
      confirmConfig.value.loading = true
      try {
        await revokeAccess(ob.offboardingCaseId, {})
        showToast('Đã thu hồi quyền truy cập!', 'success'); showConfirm.value = false; fetchOffboardings()
      } catch (err) { showToast(err?.response?.data?.message || 'Lỗi', 'error'); confirmConfig.value.loading = false }
    }
  }
  showConfirm.value = true
}

const handleSettlement = (ob) => {
  confirmConfig.value = {
    title: 'Đối soát thanh toán',
    message: `Thực hiện đối soát lương dư và công nợ cho "${ob.employeeName}"?`,
    type: 'primary', loading: false,
    action: async () => {
      confirmConfig.value.loading = true
      try {
        await processSettlement(ob.offboardingCaseId, { clearanceStatus: true })
        showToast('Đã hoàn tất đối soát!', 'success'); showConfirm.value = false; fetchOffboardings()
      } catch (err) { showToast(err?.response?.data?.message || 'Lỗi', 'error'); confirmConfig.value.loading = false }
    }
  }
  showConfirm.value = true
}

const handleClose = (ob) => {
  confirmConfig.value = {
    title: 'Đóng hồ sơ nghỉ việc',
    message: `Đóng vĩnh viễn hồ sơ nghỉ việc của "${ob.employeeName}"? Thao tác này không thể hoàn tác.`,
    type: 'danger', loading: false,
    action: async () => {
      confirmConfig.value.loading = true
      try {
        await closeOffboarding(ob.offboardingCaseId)
        showToast('Đã đóng hồ sơ nghỉ việc!', 'success'); showConfirm.value = false; fetchOffboardings()
      } catch (err) { showToast(err?.response?.data?.message || 'Lỗi', 'error'); confirmConfig.value.loading = false }
    }
  }
  showConfirm.value = true
}

const openFinalizeModal = (ob) => {
  finalizeTarget.value = ob
  finalizeForm.value = { note: '' }
  finalizeError.value = ''
  showFinalizeModal.value = true
}

const handleFinalize = async () => {
  if (!finalizeForm.value.note.trim()) { finalizeError.value = 'Vui lòng nhập ghi chú khi chốt hồ sơ'; return }
  finalizeLoading.value = true; finalizeError.value = ''
  try {
    await finalizeOffboarding(finalizeTarget.value.offboardingCaseId, finalizeForm.value)
    showToast('Đã chốt hồ sơ nghỉ việc!', 'success')
    showFinalizeModal.value = false; fetchOffboardings()
  } catch (err) {
    finalizeError.value = err?.response?.data?.message || 'Có lỗi xảy ra'
  } finally {
    finalizeLoading.value = false
  }
}

onMounted(() => {
  if (localStorage.getItem('isAuthenticated') === 'true') fetchOffboardings()
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-rose-50/20 to-slate-100 p-6">

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
          <div class="w-10 h-10 rounded-2xl bg-gradient-to-br from-rose-500 to-red-600 flex items-center justify-center shadow-lg shadow-rose-200">
            <UserMinus class="w-5 h-5 text-white" />
          </div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Thôi việc (Offboarding)</h1>
        </div>
        <p class="text-slate-500 text-sm ml-1">Quản lý quy trình bàn giao, thu hồi tài sản và chốt thanh toán</p>
      </div>
      <BaseButton variant="outline" size="sm" :icon="RefreshCw" @click="fetchOffboardings" :loading="isLoading">Làm mới</BaseButton>
    </div>

    <!-- STATS -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <GlassCard padding="p-4"><p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Tổng hồ sơ</p><p class="text-3xl font-bold text-slate-800">{{ stats.total }}</p></GlassCard>
      <GlassCard padding="p-4"><p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Mới khởi tạo</p><p class="text-3xl font-bold text-amber-600">{{ stats.pending }}</p></GlassCard>
      <GlassCard padding="p-4"><p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Đang xử lý</p><p class="text-3xl font-bold text-blue-600">{{ stats.inProgress }}</p></GlassCard>
      <GlassCard padding="p-4"><p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Hoàn tất</p><p class="text-3xl font-bold text-emerald-600">{{ stats.completed }}</p></GlassCard>
    </div>

    <!-- FILTER -->
    <GlassCard padding="p-4" class="mb-5">
      <div class="flex items-center gap-3">
        <select v-model="filterStatus" @change="currentPage = 1; fetchOffboardings()"
          class="h-10 px-3 text-sm text-slate-700 rounded-xl bg-white/80 border border-slate-200 focus:border-rose-500 focus:ring-4 focus:ring-rose-500/10 outline-none transition-all">
          <option value="">Tất cả trạng thái</option>
          <option value="INITIATED">Khởi tạo</option>
          <option value="IN_PROGRESS">Đang xử lý</option>
          <option value="PENDING_SETTLEMENT">Chờ thanh toán</option>
          <option value="COMPLETED">Hoàn tất</option>
          <option value="CLOSED">Đã đóng</option>
        </select>
      </div>
    </GlassCard>

    <div v-if="errorMessage" class="mb-4 p-4 bg-rose-50 border border-rose-200 rounded-2xl flex items-center gap-3 text-rose-700 text-sm"><AlertTriangle class="w-5 h-5" />{{ errorMessage }}</div>

    <!-- TABLE -->
    <GlassCard padding="p-0" class="overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[950px]">
          <thead class="bg-slate-50/80 border-b border-slate-200/60">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Nhân viên</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Ngày nghỉ việc</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Lý do</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Checklist</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Trạng thái</th>
              <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <template v-if="isLoading">
              <tr v-for="i in 5" :key="i" class="border-b border-slate-100">
                <td class="px-4 py-3"><div class="flex items-center gap-2"><div class="w-8 h-8 rounded-xl bg-slate-200 animate-pulse"></div><div class="space-y-1.5"><div class="h-3 w-24 bg-slate-200 rounded animate-pulse"></div><div class="h-2.5 w-16 bg-slate-100 rounded animate-pulse"></div></div></div></td>
                <td class="px-4 py-3"><div class="h-3 w-20 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-3 w-32 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-2 w-28 bg-slate-200 rounded-full animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-6 w-24 bg-slate-200 rounded-full animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-3 w-32 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
              </tr>
            </template>
            <tr v-else-if="list.length === 0">
              <td colspan="6" class="text-center py-16">
                <div class="flex flex-col items-center gap-3">
                  <div class="w-14 h-14 rounded-3xl bg-rose-50 flex items-center justify-center"><UserMinus class="w-7 h-7 text-rose-400" /></div>
                  <p class="font-semibold text-slate-500">Không có hồ sơ thôi việc nào</p>
                </div>
              </td>
            </tr>
            <tr v-else v-for="ob in list" :key="ob.offboardingCaseId" class="border-b border-slate-100 hover:bg-rose-50/20 transition-colors group">
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-rose-400 to-red-500 flex items-center justify-center text-white font-bold text-sm shadow-sm">{{ (ob.employeeName || '?').charAt(0) }}</div>
                  <div>
                    <p class="font-semibold text-slate-800 text-sm">{{ ob.employeeName }}</p>
                    <p class="text-xs text-slate-400">{{ ob.employeeCode }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ ob.terminationDate ? new Date(ob.terminationDate).toLocaleDateString('vi-VN') : '—' }}</td>
              <td class="px-4 py-3 text-sm text-slate-600 max-w-[180px] truncate" :title="ob.reason">{{ ob.reason || '—' }}</td>
              <td class="px-4 py-3">
                <div class="space-y-1">
                  <div class="flex items-center gap-1.5">
                    <div class="w-2.5 h-2.5 rounded-full" :class="ob.accessRevoked ? 'bg-emerald-500' : 'bg-slate-200'"></div>
                    <span class="text-xs text-slate-500">Thu hồi access</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <div class="w-2.5 h-2.5 rounded-full" :class="ob.settlementDone ? 'bg-emerald-500' : 'bg-slate-200'"></div>
                    <span class="text-xs text-slate-500">Đối soát lương</span>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5">
                  <component :is="statusBadge(ob.status).icon" class="w-3.5 h-3.5" />
                  <span class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold border" :class="statusBadge(ob.status).class">{{ statusBadge(ob.status).label }}</span>
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center justify-center gap-1 opacity-60 group-hover:opacity-100 transition-opacity">
                  <button @click="detailTarget = ob; showDetailModal = true" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-indigo-100 hover:text-indigo-600 transition-all" title="Chi tiết"><Eye class="w-4 h-4" /></button>
                  <button v-if="!ob.accessRevoked && ob.status !== 'CLOSED'" @click="handleRevokeAccess(ob)" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-rose-100 hover:text-rose-600 transition-all" title="Thu hồi access"><ShieldOff class="w-4 h-4" /></button>
                  <button v-if="!ob.settlementDone && ob.status !== 'CLOSED'" @click="handleSettlement(ob)" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-amber-100 hover:text-amber-600 transition-all" title="Đối soát lương"><CreditCard class="w-4 h-4" /></button>
                  <button v-if="ob.status === 'IN_PROGRESS'" @click="openFinalizeModal(ob)" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-emerald-100 hover:text-emerald-600 transition-all" title="Chốt hồ sơ"><CheckCircle class="w-4 h-4" /></button>
                  <button v-if="ob.status === 'COMPLETED'" @click="handleClose(ob)" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-slate-100 hover:text-slate-700 transition-all" title="Đóng hồ sơ"><X class="w-4 h-4" /></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="!isLoading && list.length > 0" class="flex items-center justify-between px-6 py-4 border-t border-slate-200/60 bg-slate-50/40">
        <p class="text-sm text-slate-500">Trang <span class="font-semibold">{{ currentPage }}</span> / {{ totalPages }}</p>
        <div class="flex items-center gap-1.5">
          <button @click="goToPage(currentPage - 1)" :disabled="currentPage <= 1" class="w-9 h-9 rounded-xl flex items-center justify-center hover:bg-rose-100 disabled:opacity-30 transition-all border border-slate-200"><ChevronLeft class="w-4 h-4 text-slate-500" /></button>
          <button v-for="p in visiblePages" :key="p" @click="goToPage(p)" class="w-9 h-9 rounded-xl text-sm font-semibold transition-all border" :class="currentPage === p ? 'bg-rose-600 text-white border-rose-600' : 'text-slate-600 hover:bg-rose-50 border-slate-200'">{{ p }}</button>
          <button @click="goToPage(currentPage + 1)" :disabled="currentPage >= totalPages" class="w-9 h-9 rounded-xl flex items-center justify-center hover:bg-rose-100 disabled:opacity-30 transition-all border border-slate-200"><ChevronRight class="w-4 h-4 text-slate-500" /></button>
        </div>
      </div>
    </GlassCard>

    <!-- MODAL: CHI TIẾT -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showDetailModal = false">
        <div class="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 bg-gradient-to-r from-rose-600 to-red-600">
            <h3 class="text-white font-semibold">Chi tiết hồ sơ thôi việc</h3>
            <button @click="showDetailModal = false" class="w-8 h-8 rounded-xl bg-white/10 hover:bg-white/20 flex items-center justify-center text-white"><X class="w-4 h-4" /></button>
          </div>
          <div v-if="detailTarget" class="p-6 space-y-3">
            <div class="p-4 bg-rose-50 rounded-2xl border border-rose-100">
              <p class="font-bold text-slate-800 text-lg">{{ detailTarget.employeeName }}</p>
              <p class="text-slate-500 text-sm">{{ detailTarget.employeeCode }}</p>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div class="p-3 bg-slate-50 rounded-xl border border-slate-100"><p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Ngày nghỉ</p><p class="font-semibold text-slate-700 text-sm">{{ detailTarget.terminationDate ? new Date(detailTarget.terminationDate).toLocaleDateString('vi-VN') : '—' }}</p></div>
              <div class="p-3 bg-slate-50 rounded-xl border border-slate-100"><p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Trạng thái</p><span class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold border" :class="statusBadge(detailTarget.status).class">{{ statusBadge(detailTarget.status).label }}</span></div>
            </div>
            <div class="p-3 bg-slate-50 rounded-xl border border-slate-100"><p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Lý do nghỉ việc</p><p class="text-slate-700 text-sm">{{ detailTarget.reason || '—' }}</p></div>
            <div class="p-3 bg-slate-50 rounded-xl border border-slate-100 space-y-2">
              <p class="text-xs text-slate-400 font-bold uppercase">Checklist bàn giao</p>
              <div class="flex items-center gap-2"><div class="w-5 h-5 rounded-full flex items-center justify-center" :class="detailTarget.accessRevoked ? 'bg-emerald-100' : 'bg-slate-100'"><CheckCircle class="w-3 h-3" :class="detailTarget.accessRevoked ? 'text-emerald-600' : 'text-slate-300'" /></div><span class="text-sm text-slate-700">Thu hồi quyền truy cập</span></div>
              <div class="flex items-center gap-2"><div class="w-5 h-5 rounded-full flex items-center justify-center" :class="detailTarget.settlementDone ? 'bg-emerald-100' : 'bg-slate-100'"><CheckCircle class="w-3 h-3" :class="detailTarget.settlementDone ? 'text-emerald-600' : 'text-slate-300'" /></div><span class="text-sm text-slate-700">Đối soát lương & công nợ</span></div>
            </div>
          </div>
          <div class="flex justify-end px-6 py-4 border-t border-slate-100"><BaseButton variant="outline" @click="showDetailModal = false">Đóng</BaseButton></div>
        </div>
      </div>
    </Transition>

    <!-- MODAL: CHỐT HỒ SƠ -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showFinalizeModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showFinalizeModal = false">
        <div class="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="px-6 py-4 bg-gradient-to-r from-emerald-500 to-teal-600">
            <h3 class="text-white font-semibold">Chốt hồ sơ nghỉ việc</h3>
            <p class="text-emerald-100 text-sm mt-0.5">{{ finalizeTarget?.employeeName }}</p>
          </div>
          <div class="p-6 space-y-4">
            <div v-if="finalizeError" class="p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-sm flex items-center gap-2"><AlertTriangle class="w-4 h-4 flex-shrink-0" />{{ finalizeError }}</div>
            <div class="space-y-1.5">
              <label class="block text-sm font-semibold text-slate-700 ml-1">Ghi chú chốt hồ sơ <span class="text-red-500">*</span></label>
              <textarea v-model="finalizeForm.note" rows="4" placeholder="Nhập ghi chú xác nhận hoàn tất thủ tục..." class="w-full px-4 py-3 text-sm text-slate-700 rounded-xl border border-slate-200 focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 outline-none resize-none"></textarea>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showFinalizeModal = false" :disabled="finalizeLoading">Hủy</BaseButton>
            <BaseButton variant="primary" :loading="finalizeLoading" @click="handleFinalize">Xác nhận chốt</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

    <!-- CONFIRM DIALOG -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showConfirm" class="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
        <div class="w-full max-w-sm bg-white rounded-3xl shadow-2xl overflow-hidden">
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
