<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  UserPlus, Search, Eye, AlertTriangle, RefreshCw, ChevronLeft, ChevronRight,
  CheckCircle, X, Mail, KeyRound, FileText, Zap, Clock, Users
} from 'lucide-vue-next'
import GlassCard from '@/components/common/GlassCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import {
  getOnboardings,
  createOnboarding,
  completeOnboarding,
  notifyOnboarding,
  createUserForOnboarding,
  createInitialContract
} from '@/api/admin/onboarding.js'

/* --- STATE --- */
const isLoading = ref(false)
const errorMessage = ref('')
const list = ref([])
const currentPage = ref(1)
const totalPages = ref(0)
const totalElements = ref(0)
const PAGE_SIZE = 10

/* --- FILTER --- */
const keyword = ref('')
const filterStatus = ref('')

/* --- MODAL: CREATE --- */
const showCreateModal = ref(false)
const createLoading = ref(false)
const createError = ref('')
const createErrors = ref({})
const createForm = ref({
  fullName: '', email: '', phone: '', position: '',
  orgUnitId: '', startDate: '', note: ''
})

/* --- MODAL: DETAIL --- */
const showDetailModal = ref(false)
const detailTarget = ref(null)

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
    PENDING: { label: 'Chờ xử lý', class: 'bg-amber-100 text-amber-700 border-amber-200', icon: Clock },
    IN_PROGRESS: { label: 'Đang tiến hành', class: 'bg-blue-100 text-blue-700 border-blue-200', icon: Zap },
    COMPLETED: { label: 'Hoàn tất', class: 'bg-emerald-100 text-emerald-700 border-emerald-200', icon: CheckCircle },
    CANCELLED: { label: 'Đã hủy', class: 'bg-rose-100 text-rose-700 border-rose-200', icon: X }
  }
  return map[status] || { label: status, class: 'bg-slate-100 text-slate-600', icon: Clock }
}

/* --- STATS --- */
const stats = computed(() => ({
  total: list.value.length,
  inProgress: list.value.filter(o => o.status === 'IN_PROGRESS').length,
  pending: list.value.filter(o => o.status === 'PENDING').length,
  completed: list.value.filter(o => o.status === 'COMPLETED').length
}))

/* --- DEBOUNCE --- */
let searchTimer = null
const onSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 1; fetchOnboardings() }, 500)
}

/* --- API --- */
const fetchOnboardings = async () => {
  isLoading.value = true; errorMessage.value = ''
  try {
    const params = { page: currentPage.value - 1, size: PAGE_SIZE }
    if (filterStatus.value) params.status = filterStatus.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const res = await getOnboardings(params)
    list.value = res.content ?? res.data?.content ?? []
    totalPages.value = res.totalPages ?? res.data?.totalPages ?? 1
    totalElements.value = res.totalElements ?? res.data?.totalElements ?? list.value.length
  } catch (err) {
    errorMessage.value = 'Không thể tải danh sách onboarding.'
  } finally {
    isLoading.value = false
  }
}

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page; fetchOnboardings()
}
const visiblePages = computed(() => {
  const pages = []; const start = Math.max(1, currentPage.value - 2); const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i); return pages
})

/* --- VALIDATE CREATE --- */
const validateCreate = () => {
  const errors = {}
  if (!createForm.value.fullName.trim()) errors.fullName = 'Họ tên không được để trống'
  if (!createForm.value.email.trim()) errors.email = 'Email không được để trống'
  if (!createForm.value.startDate) errors.startDate = 'Ngày bắt đầu không được để trống'
  createErrors.value = errors
  return Object.keys(errors).length === 0
}

const handleCreate = async () => {
  if (!validateCreate()) return
  createLoading.value = true; createError.value = ''
  try {
    await createOnboarding(createForm.value)
    showToast('Đã tạo hồ sơ tiếp nhận thành công!', 'success')
    showCreateModal.value = false
    createForm.value = { fullName: '', email: '', phone: '', position: '', orgUnitId: '', startDate: '', note: '' }
    createErrors.value = {}; currentPage.value = 1; fetchOnboardings()
  } catch (err) {
    createError.value = err?.response?.data?.message || 'Có lỗi xảy ra'
  } finally {
    createLoading.value = false
  }
}

/* --- QUICK ACTIONS --- */
const handleComplete = (ob) => {
  confirmConfig.value = {
    title: 'Hoàn tất Onboarding',
    message: `Xác nhận hoàn tất toàn bộ thủ tục tiếp nhận cho "${ob.fullName}"?`,
    type: 'primary', loading: false,
    action: async () => {
      confirmConfig.value.loading = true
      try {
        await completeOnboarding(ob.onboardingId)
        showToast('Đã hoàn tất onboarding!', 'success'); showConfirm.value = false; fetchOnboardings()
      } catch (err) { showToast(err?.response?.data?.message || 'Lỗi', 'error'); confirmConfig.value.loading = false }
    }
  }
  showConfirm.value = true
}

const handleSendNotify = async (ob) => {
  actionLoading.value[ob.onboardingId + '_notify'] = true
  try {
    await notifyOnboarding(ob.onboardingId, {})
    showToast('Đã gửi email thông báo!', 'success')
  } catch (err) { showToast(err?.response?.data?.message || 'Lỗi', 'error') }
  finally { actionLoading.value[ob.onboardingId + '_notify'] = false }
}

const handleCreateUser = async (ob) => {
  actionLoading.value[ob.onboardingId + '_user'] = true
  try {
    await createUserForOnboarding(ob.onboardingId, {})
    showToast('Đã cấp tài khoản hệ thống!', 'success')
  } catch (err) { showToast(err?.response?.data?.message || 'Lỗi', 'error') }
  finally { actionLoading.value[ob.onboardingId + '_user'] = false }
}

onMounted(() => {
  if (localStorage.getItem('isAuthenticated') === 'true') fetchOnboardings()
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-green-50/20 to-slate-100 p-6">

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
          <div class="w-10 h-10 rounded-2xl bg-gradient-to-br from-green-500 to-emerald-600 flex items-center justify-center shadow-lg shadow-green-200">
            <UserPlus class="w-5 h-5 text-white" />
          </div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Tiếp nhận Nhân viên mới</h1>
        </div>
        <p class="text-slate-500 text-sm ml-1">Quản lý toàn bộ quy trình onboarding từ cấp tài khoản đến ký hợp đồng</p>
      </div>
      <div class="flex items-center gap-3">
        <BaseButton variant="outline" size="sm" :icon="RefreshCw" @click="fetchOnboardings" :loading="isLoading">Làm mới</BaseButton>
        <BaseButton variant="primary" size="md" :icon="UserPlus" @click="showCreateModal = true">Tạo hồ sơ tiếp nhận</BaseButton>
      </div>
    </div>

    <!-- STATS -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <GlassCard padding="p-4"><p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Tổng hồ sơ</p><p class="text-3xl font-bold text-slate-800">{{ stats.total }}</p></GlassCard>
      <GlassCard padding="p-4"><p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Đang tiến hành</p><p class="text-3xl font-bold text-blue-600">{{ stats.inProgress }}</p></GlassCard>
      <GlassCard padding="p-4"><p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Chờ xử lý</p><p class="text-3xl font-bold text-amber-600">{{ stats.pending }}</p></GlassCard>
      <GlassCard padding="p-4"><p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Hoàn tất</p><p class="text-3xl font-bold text-emerald-600">{{ stats.completed }}</p></GlassCard>
    </div>

    <!-- FILTER -->
    <GlassCard padding="p-4" class="mb-5">
      <div class="flex flex-wrap gap-3">
        <div class="flex-1 min-w-[240px]">
          <BaseInput
            v-model="keyword"
            @input="onSearch"
            type="text"
            placeholder="Tìm theo tên, email ứng viên..."
            :icon="Search"
          />
        </div>
        <select v-model="filterStatus" @change="currentPage = 1; fetchOnboardings()"
          class="h-10 px-3 text-sm text-slate-700 rounded-xl bg-white/80 border border-slate-200 focus:border-green-500 focus:ring-4 focus:ring-green-500/10 outline-none transition-all">
          <option value="">Tất cả trạng thái</option>
          <option value="PENDING">Chờ xử lý</option>
          <option value="IN_PROGRESS">Đang tiến hành</option>
          <option value="COMPLETED">Hoàn tất</option>
          <option value="CANCELLED">Đã hủy</option>
        </select>
      </div>
    </GlassCard>

    <div v-if="errorMessage" class="mb-4 p-4 bg-rose-50 border border-rose-200 rounded-2xl flex items-center gap-3 text-rose-700 text-sm"><AlertTriangle class="w-5 h-5" />{{ errorMessage }}</div>

    <!-- TABLE -->
    <GlassCard padding="p-0" class="overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[900px]">
          <thead class="bg-slate-50/80 border-b border-slate-200/60">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Ứng viên</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Vị trí</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Ngày vào làm</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Tiến độ</th>
              <th class="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Trạng thái</th>
              <th class="px-4 py-3 text-center text-xs font-bold uppercase tracking-wider text-slate-500">Thao tác nhanh</th>
            </tr>
          </thead>
          <tbody>
            <template v-if="isLoading">
              <tr v-for="i in 5" :key="i" class="border-b border-slate-100">
                <td class="px-4 py-3"><div class="flex items-center gap-2"><div class="w-8 h-8 rounded-xl bg-slate-200 animate-pulse"></div><div class="space-y-1.5"><div class="h-3 w-24 bg-slate-200 rounded animate-pulse"></div><div class="h-2.5 w-28 bg-slate-100 rounded animate-pulse"></div></div></div></td>
                <td class="px-4 py-3"><div class="h-3 w-24 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-3 w-20 bg-slate-200 rounded animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-2 w-32 bg-slate-200 rounded-full animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-6 w-24 bg-slate-200 rounded-full animate-pulse"></div></td>
                <td class="px-4 py-3"><div class="h-3 w-28 bg-slate-200 rounded animate-pulse mx-auto"></div></td>
              </tr>
            </template>
            <tr v-else-if="list.length === 0">
              <td colspan="6" class="text-center py-16">
                <div class="flex flex-col items-center gap-3">
                  <div class="w-14 h-14 rounded-3xl bg-green-50 flex items-center justify-center"><UserPlus class="w-7 h-7 text-green-400" /></div>
                  <p class="font-semibold text-slate-500">Chưa có hồ sơ tiếp nhận nào</p>
                  <BaseButton variant="primary" size="sm" :icon="UserPlus" @click="showCreateModal = true">Tạo hồ sơ đầu tiên</BaseButton>
                </div>
              </td>
            </tr>
            <tr v-else v-for="ob in list" :key="ob.onboardingId" class="border-b border-slate-100 hover:bg-green-50/20 transition-colors group">
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-green-400 to-emerald-500 flex items-center justify-center text-white font-bold text-sm shadow-sm">{{ (ob.fullName || '?').charAt(0) }}</div>
                  <div>
                    <p class="font-semibold text-slate-800 text-sm">{{ ob.fullName }}</p>
                    <p class="text-xs text-slate-400">{{ ob.email }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ ob.position || ob.jobTitleName || '—' }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ ob.startDate ? new Date(ob.startDate).toLocaleDateString('vi-VN') : '—' }}</td>
              <td class="px-4 py-3">
                <div class="w-full bg-slate-100 rounded-full h-2">
                  <div class="h-2 rounded-full bg-gradient-to-r from-green-400 to-emerald-500 transition-all duration-500"
                       :style="{ width: (ob.progressPercent || (ob.status === 'COMPLETED' ? 100 : ob.status === 'IN_PROGRESS' ? 50 : ob.status === 'PENDING' ? 10 : 0)) + '%' }"></div>
                </div>
                <p class="text-xs text-slate-400 mt-0.5">{{ ob.progressPercent || (ob.status === 'COMPLETED' ? 100 : ob.status === 'IN_PROGRESS' ? 50 : 10) }}%</p>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5">
                  <component :is="statusBadge(ob.status).icon" class="w-3.5 h-3.5" />
                  <span class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold border" :class="statusBadge(ob.status).class">{{ statusBadge(ob.status).label }}</span>
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center justify-center gap-1 opacity-60 group-hover:opacity-100 transition-opacity flex-wrap">
                  <button @click="detailTarget = ob; showDetailModal = true" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-indigo-100 hover:text-indigo-600 transition-all" title="Chi tiết"><Eye class="w-4 h-4" /></button>
                  <button v-if="ob.status !== 'COMPLETED'" @click="handleSendNotify(ob)" :disabled="actionLoading[ob.onboardingId + '_notify']"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-blue-100 hover:text-blue-600 transition-all disabled:opacity-50" title="Gửi email thông báo"><Mail class="w-4 h-4" /></button>
                  <button v-if="ob.status === 'IN_PROGRESS'" @click="handleCreateUser(ob)" :disabled="actionLoading[ob.onboardingId + '_user']"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-amber-100 hover:text-amber-600 transition-all disabled:opacity-50" title="Cấp tài khoản"><KeyRound class="w-4 h-4" /></button>
                  <button v-if="ob.status === 'IN_PROGRESS'" @click="handleComplete(ob)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-emerald-100 hover:text-emerald-600 transition-all" title="Hoàn tất onboarding"><CheckCircle class="w-4 h-4" /></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="!isLoading && list.length > 0" class="flex items-center justify-between px-6 py-4 border-t border-slate-200/60 bg-slate-50/40">
        <p class="text-sm text-slate-500">Trang <span class="font-semibold">{{ currentPage }}</span> / {{ totalPages }}</p>
        <div class="flex items-center gap-1.5">
          <button @click="goToPage(currentPage - 1)" :disabled="currentPage <= 1" class="w-9 h-9 rounded-xl flex items-center justify-center hover:bg-green-100 disabled:opacity-30 transition-all border border-slate-200"><ChevronLeft class="w-4 h-4 text-slate-500" /></button>
          <button v-for="p in visiblePages" :key="p" @click="goToPage(p)" class="w-9 h-9 rounded-xl text-sm font-semibold transition-all border" :class="currentPage === p ? 'bg-green-600 text-white border-green-600' : 'text-slate-600 hover:bg-green-50 border-slate-200'">{{ p }}</button>
          <button @click="goToPage(currentPage + 1)" :disabled="currentPage >= totalPages" class="w-9 h-9 rounded-xl flex items-center justify-center hover:bg-green-100 disabled:opacity-30 transition-all border border-slate-200"><ChevronRight class="w-4 h-4 text-slate-500" /></button>
        </div>
      </div>
    </GlassCard>

    <!-- MODAL: TẠO HỒ SƠ TIẾP NHẬN -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showCreateModal = false">
        <div class="w-full max-w-lg bg-white rounded-3xl shadow-2xl overflow-hidden max-h-[90vh] flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 bg-gradient-to-r from-green-500 to-emerald-600">
            <div class="flex items-center gap-3"><UserPlus class="w-5 h-5 text-white" /><h3 class="text-white font-semibold text-lg">Tạo hồ sơ tiếp nhận</h3></div>
            <button @click="showCreateModal = false" class="w-8 h-8 rounded-xl bg-white/10 hover:bg-white/20 flex items-center justify-center text-white"><X class="w-4 h-4" /></button>
          </div>
          <div v-if="createError" class="mx-6 mt-4 p-3 bg-rose-50 border border-rose-200 rounded-xl flex items-center gap-2 text-rose-700 text-sm"><AlertTriangle class="w-4 h-4 flex-shrink-0" />{{ createError }}</div>
          <div class="flex-1 overflow-y-auto p-6 space-y-4">
            <BaseInput v-model="createForm.fullName" label="Họ và tên ứng viên" placeholder="Nguyễn Văn A" :error="createErrors.fullName" :required="true" />
            <BaseInput v-model="createForm.email" label="Email" type="email" placeholder="email@example.com" :error="createErrors.email" :required="true" />
            <BaseInput v-model="createForm.phone" label="Số điện thoại" placeholder="09x xxxx xxxx" />
            <BaseInput v-model="createForm.position" label="Vị trí / Chức danh" placeholder="VD: Software Engineer" />
            <BaseInput v-model="createForm.startDate" label="Ngày bắt đầu làm việc" type="date" :error="createErrors.startDate" :required="true" />
            <div class="space-y-1.5">
              <label class="block text-sm font-semibold text-slate-700 ml-1">Ghi chú</label>
              <textarea v-model="createForm.note" rows="2" class="w-full px-4 py-3 text-sm text-slate-700 rounded-xl bg-white/50 border border-slate-200 focus:border-green-500 focus:ring-4 focus:ring-green-500/10 outline-none resize-none"></textarea>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showCreateModal = false" :disabled="createLoading">Hủy</BaseButton>
            <BaseButton variant="primary" :loading="createLoading" @click="handleCreate">Tạo hồ sơ</BaseButton>
          </div>
        </div>
      </div>
    </Transition>

    <!-- MODAL: CHI TIẾT -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm" @click.self="showDetailModal = false">
        <div class="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 bg-gradient-to-r from-green-600 to-emerald-600">
            <h3 class="text-white font-semibold">Chi tiết hồ sơ tiếp nhận</h3>
            <button @click="showDetailModal = false" class="w-8 h-8 rounded-xl bg-white/10 hover:bg-white/20 flex items-center justify-center text-white"><X class="w-4 h-4" /></button>
          </div>
          <div v-if="detailTarget" class="p-6 space-y-3">
            <div class="p-4 bg-green-50 rounded-2xl border border-green-100">
              <p class="font-bold text-slate-800 text-lg">{{ detailTarget.fullName }}</p>
              <p class="text-slate-500 text-sm">{{ detailTarget.email }}</p>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div class="p-3 bg-slate-50 rounded-xl border border-slate-100"><p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Vị trí</p><p class="font-semibold text-slate-700 text-sm">{{ detailTarget.position || '—' }}</p></div>
              <div class="p-3 bg-slate-50 rounded-xl border border-slate-100"><p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Trạng thái</p><span class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold border" :class="statusBadge(detailTarget.status).class">{{ statusBadge(detailTarget.status).label }}</span></div>
              <div class="p-3 bg-slate-50 rounded-xl border border-slate-100"><p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Ngày vào làm</p><p class="font-semibold text-slate-700 text-sm">{{ detailTarget.startDate ? new Date(detailTarget.startDate).toLocaleDateString('vi-VN') : '—' }}</p></div>
              <div class="p-3 bg-slate-50 rounded-xl border border-slate-100"><p class="text-xs text-slate-400 font-bold uppercase mb-0.5">SĐT</p><p class="font-semibold text-slate-700 text-sm">{{ detailTarget.phone || '—' }}</p></div>
            </div>
            <div v-if="detailTarget.note" class="p-3 bg-slate-50 rounded-xl border border-slate-100"><p class="text-xs text-slate-400 font-bold uppercase mb-0.5">Ghi chú</p><p class="text-slate-700 text-sm">{{ detailTarget.note }}</p></div>
          </div>
          <div class="flex justify-end px-6 py-4 border-t border-slate-100"><BaseButton variant="outline" @click="showDetailModal = false">Đóng</BaseButton></div>
        </div>
      </div>
    </Transition>

    <!-- CONFIRM -->
    <Transition enter-active-class="transition-all duration-300 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition-all duration-200" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="showConfirm" class="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
        <div class="w-full max-w-sm bg-white rounded-3xl shadow-2xl overflow-hidden">
          <div class="p-6">
            <h3 class="font-bold text-slate-800 text-lg mb-2">{{ confirmConfig.title }}</h3>
            <p class="text-slate-500 text-sm">{{ confirmConfig.message }}</p>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
            <BaseButton variant="outline" @click="showConfirm = false" :disabled="confirmConfig.loading">Hủy</BaseButton>
            <BaseButton variant="primary" :loading="confirmConfig.loading" @click="confirmConfig.action && confirmConfig.action()">Xác nhận</BaseButton>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>
