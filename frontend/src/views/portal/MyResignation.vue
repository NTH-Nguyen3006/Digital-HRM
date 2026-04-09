<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Calendar, FileText, Loader2, MessageSquare } from 'lucide-vue-next'
import { getMyResignationRequests, submitResignationRequest } from '@/api/me/portal'
import { safeArray, unwrapData } from '@/utils/api'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const showForm = ref(false)
const loading = ref(false)
const submitting = ref(false)
const requests = ref([])

const form = reactive({
  requestedLastWorkingDate: '',
  reason: '',
  detailedReason: '',
})

const resignationReasons = [
  'Cá nhân',
  'Sức khỏe',
  'Học tập',
  'Thay đổi định hướng',
  'Di chuyển nơi ở',
  'Khác',
]

function minDate() {
  const today = new Date()
  today.setDate(today.getDate() + 30)
  return today.toISOString().split('T')[0]
}

function resetForm() {
  form.requestedLastWorkingDate = ''
  form.reason = ''
  form.detailedReason = ''
}

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('vi-VN')
}

function formatDateTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN')
}

function getStatusLabel(status) {
  const labels = {
    REQUESTED: 'Đã gửi yêu cầu',
    MANAGER_REVIEWED: 'QL đã xem xét',
    HR_FINALIZED: 'HR đã chốt',
    CLOSED: 'Đã hoàn tất',
    CANCELLED: 'Đã hủy',
  }
  return labels[status] || status || 'Không xác định'
}

function getStatusColor(status) {
  const styles = {
    REQUESTED: 'bg-amber-100 text-amber-800',
    MANAGER_REVIEWED: 'bg-sky-100 text-sky-800',
    HR_FINALIZED: 'bg-indigo-100 text-indigo-800',
    CLOSED: 'bg-emerald-100 text-emerald-800',
    CANCELLED: 'bg-slate-100 text-slate-700',
  }
  return styles[status] || 'bg-slate-100 text-slate-700'
}

async function fetchRequests() {
  loading.value = true
  try {
    const response = await getMyResignationRequests()
    requests.value = safeArray(unwrapData(response))
  } catch (error) {
    console.error('Failed to fetch resignation requests:', error)
    requests.value = []
    toast.error('Không thể tải yêu cầu nghỉ việc')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.requestedLastWorkingDate || !form.reason || !form.detailedReason.trim()) {
    toast.warning('Vui lòng nhập đủ ngày nghỉ và lý do')
    return
  }

  submitting.value = true
  try {
    await submitResignationRequest({
      requestedLastWorkingDate: form.requestedLastWorkingDate,
      requestReason: `${form.reason}: ${form.detailedReason.trim()}`,
    })
    toast.success('Đã gửi yêu cầu nghỉ việc')
    resetForm()
    showForm.value = false
    await fetchRequests()
  } catch (error) {
    console.error('Failed to submit resignation request:', error)
    toast.error(error.response?.data?.message || 'Gửi yêu cầu nghỉ việc thất bại')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchRequests)
</script>

<template>
  <div class="max-w-4xl mx-auto px-6 py-8 space-y-8">
    <div>
      <h1 class="text-3xl font-black text-slate-900 mb-2">Yêu cầu nghỉ việc</h1>
      <p class="text-slate-600">Gửi đơn nghỉ việc và theo dõi tiến trình offboarding thực tế từ backend.</p>
    </div>

    <div class="rounded-4xl border border-yellow-200 bg-yellow-50 p-6">
      <div class="flex items-start gap-3">
        <MessageSquare class="w-6 h-6 text-yellow-600 mt-0.5" />
        <div>
          <h3 class="font-semibold text-yellow-800 mb-2">Lưu ý quan trọng</h3>
          <ul class="text-sm text-yellow-700 space-y-1">
            <li>Thời gian báo trước tối thiểu 30 ngày.</li>
            <li>Yêu cầu sẽ đi qua các bước xem xét của quản lý và HR.</li>
            <li>Thông tin settlement và ngày nghỉ chính thức sẽ được cập nhật theo tiến trình xử lý.</li>
          </ul>
        </div>
      </div>
    </div>

    <div>
      <button
        @click="showForm = !showForm"
        class="inline-flex items-center gap-2 px-6 py-3 bg-red-600 text-white font-semibold rounded-full hover:bg-red-700 transition"
      >
        <FileText class="w-5 h-5" />
        {{ showForm ? 'Hủy tạo yêu cầu' : 'Tạo yêu cầu nghỉ việc' }}
      </button>
    </div>

    <div v-if="showForm" class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm">
      <h2 class="text-xl font-black text-slate-900 mb-6">Thông tin nghỉ việc</h2>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        <div class="grid gap-6 md:grid-cols-2">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Ngày làm việc cuối dự kiến *</label>
            <input
              v-model="form.requestedLastWorkingDate"
              type="date"
              :min="minDate()"
              class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-red-500"
            >
          </div>

          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Nhóm lý do *</label>
            <select
              v-model="form.reason"
              class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-red-500"
            >
              <option value="">Chọn lý do</option>
              <option v-for="reason in resignationReasons" :key="reason" :value="reason">{{ reason }}</option>
            </select>
          </div>
        </div>

        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Lý do chi tiết *</label>
          <textarea
            v-model="form.detailedReason"
            rows="4"
            placeholder="Mô tả rõ lý do và bối cảnh nghỉ việc..."
            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-red-500"
          />
        </div>

        <div class="flex gap-4">
          <button
            type="submit"
            :disabled="submitting"
            class="px-6 py-3 bg-red-600 text-white font-semibold rounded-full hover:bg-red-700 disabled:opacity-50 transition inline-flex items-center gap-2"
          >
            <Loader2 v-if="submitting" class="w-4 h-4 animate-spin" />
            <span>{{ submitting ? 'Đang gửi...' : 'Gửi yêu cầu nghỉ việc' }}</span>
          </button>

          <button
            type="button"
            @click="showForm = false"
            class="px-6 py-3 bg-slate-200 text-slate-700 font-semibold rounded-full hover:bg-slate-300 transition"
          >
            Hủy
          </button>
        </div>
      </form>
    </div>

    <div v-if="loading" class="space-y-4">
      <div v-for="item in 3" :key="item" class="h-36 animate-pulse rounded-4xl bg-slate-100" />
    </div>

    <div v-else-if="requests.length" class="space-y-4">
      <div
        v-for="request in requests"
        :key="request.offboardingCaseId"
        class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm"
      >
        <div class="flex items-start justify-between gap-4 flex-wrap">
          <div class="space-y-3">
            <div class="flex items-center gap-3 flex-wrap">
              <Calendar class="w-5 h-5 text-slate-400" />
              <span class="font-semibold text-slate-900">{{ request.offboardingCode }}</span>
              <span class="px-3 py-1 rounded-full text-xs font-semibold" :class="getStatusColor(request.status)">
                {{ getStatusLabel(request.status) }}
              </span>
            </div>
            <p class="text-sm text-slate-600">Ngày gửi yêu cầu: {{ formatDate(request.requestDate) }}</p>
            <p class="text-sm text-slate-600">Ngày nghỉ dự kiến: {{ formatDate(request.requestedLastWorkingDate) }}</p>
            <p class="text-sm text-slate-600">Ngày nghỉ hiệu lực: {{ formatDate(request.effectiveLastWorkingDate) }}</p>
            <p class="text-sm text-slate-600">Quản lý trực tiếp: {{ request.managerEmployeeName || '—' }}</p>
          </div>

          <div class="grid gap-3 md:grid-cols-2 w-full md:w-auto md:min-w-[320px]">
            <div class="rounded-2xl bg-slate-50 p-4">
              <p class="text-xs font-bold uppercase tracking-wider text-slate-400">HR Finalized</p>
              <p class="mt-2 font-semibold text-slate-900">{{ formatDateTime(request.hrFinalizedAt) }}</p>
            </div>
            <div class="rounded-2xl bg-slate-50 p-4">
              <p class="text-xs font-bold uppercase tracking-wider text-slate-400">Closed At</p>
              <p class="mt-2 font-semibold text-slate-900">{{ formatDateTime(request.closedAt) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="rounded-4xl border border-slate-200 bg-white p-16 shadow-sm text-center">
      <FileText class="w-20 h-20 text-slate-300 mx-auto mb-6" />
      <h3 class="text-xl font-bold text-slate-900 mb-2">Chưa có yêu cầu nào</h3>
      <p class="text-slate-600">Lịch sử nghỉ việc sẽ xuất hiện tại đây khi backend có dữ liệu tương ứng.</p>
    </div>
  </div>
</template>
