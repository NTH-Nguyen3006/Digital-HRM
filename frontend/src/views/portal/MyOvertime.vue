<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Calendar, Clock, Loader2, Plus } from 'lucide-vue-next'
import { getMyOvertimeRequests, submitOvertimeRequest } from '@/api/me/portal'
import { safeArray, unwrapData } from '@/utils/api'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const showForm = ref(false)
const loading = ref(false)
const submitting = ref(false)
const requests = ref([])

const form = reactive({
  attendanceDate: '',
  overtimeStartAt: '',
  overtimeEndAt: '',
  reason: '',
  evidenceFileKey: '',
})

const reasonOptions = [
  'Hoàn thành deadline',
  'Meeting khách hàng',
  'Hỗ trợ production',
  'Fix lỗi khẩn cấp',
  'Bàn giao cuối ngày',
  'Khác',
]

const approvedCount = computed(() => requests.value.filter((item) => item.requestStatus === 'APPROVED').length)
const pendingCount = computed(() => requests.value.filter((item) => item.requestStatus === 'SUBMITTED').length)

function resetForm() {
  form.attendanceDate = ''
  form.overtimeStartAt = ''
  form.overtimeEndAt = ''
  form.reason = ''
  form.evidenceFileKey = ''
}

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('vi-VN')
}

function formatTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}

function formatMinutes(minutes) {
  const total = Number(minutes || 0)
  const hours = Math.floor(total / 60)
  const mins = total % 60
  return `${hours}h ${mins}m`
}

function getStatusLabel(status) {
  const labels = {
    DRAFT: 'Nháp',
    SUBMITTED: 'Chờ duyệt',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Từ chối',
  }
  return labels[status] || status || 'Không xác định'
}

function getStatusColor(status) {
  const styles = {
    DRAFT: 'bg-slate-100 text-slate-700',
    SUBMITTED: 'bg-amber-100 text-amber-800',
    APPROVED: 'bg-emerald-100 text-emerald-800',
    REJECTED: 'bg-rose-100 text-rose-800',
  }
  return styles[status] || 'bg-slate-100 text-slate-700'
}

function toMinutes(start, end) {
  if (!start || !end || !form.attendanceDate) return 0
  const startDate = new Date(`${form.attendanceDate}T${start}:00`)
  const endDate = new Date(`${form.attendanceDate}T${end}:00`)
  return Math.max(0, Math.round((endDate - startDate) / 60000))
}

async function fetchRequests() {
  loading.value = true
  try {
    const response = await getMyOvertimeRequests()
    requests.value = safeArray(unwrapData(response))
  } catch (error) {
    console.error('Failed to fetch overtime requests:', error)
    requests.value = []
    toast.error('Không thể tải danh sách yêu cầu OT')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.attendanceDate || !form.overtimeStartAt || !form.overtimeEndAt || !form.reason) {
    toast.warning('Vui lòng nhập đủ ngày, thời gian và lý do OT')
    return
  }

  const requestedMinutes = toMinutes(form.overtimeStartAt, form.overtimeEndAt)
  if (!requestedMinutes) {
    toast.warning('Khoảng thời gian OT chưa hợp lệ')
    return
  }

  submitting.value = true
  try {
    await submitOvertimeRequest({
      attendanceDate: form.attendanceDate,
      overtimeStartAt: `${form.attendanceDate}T${form.overtimeStartAt}:00`,
      overtimeEndAt: `${form.attendanceDate}T${form.overtimeEndAt}:00`,
      reason: form.reason,
      evidenceFileKey: form.evidenceFileKey || null,
    })
    toast.success('Đã gửi yêu cầu OT')
    resetForm()
    showForm.value = false
    await fetchRequests()
  } catch (error) {
    console.error('Failed to submit overtime request:', error)
    toast.error(error.response?.data?.message || 'Gửi yêu cầu OT thất bại')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchRequests)
</script>

<template>
  <div class="max-w-4xl mx-auto px-6 py-8 space-y-8">
    <div>
      <h1 class="text-3xl font-black text-slate-900 mb-2">Yêu cầu làm thêm giờ</h1>
      <p class="text-slate-600">Theo dõi toàn bộ yêu cầu OT thực tế và gửi đăng ký tăng ca từ dữ liệu backend.</p>
    </div>

    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-3xl bg-white border border-slate-200 p-6">
        <p class="text-sm font-semibold text-slate-500">Tổng yêu cầu</p>
        <p class="mt-2 text-3xl font-black text-slate-900">{{ requests.length }}</p>
      </div>
      <div class="rounded-3xl bg-amber-50 border border-amber-100 p-6">
        <p class="text-sm font-semibold text-amber-700">Chờ duyệt</p>
        <p class="mt-2 text-3xl font-black text-amber-900">{{ pendingCount }}</p>
      </div>
      <div class="rounded-3xl bg-emerald-50 border border-emerald-100 p-6">
        <p class="text-sm font-semibold text-emerald-700">Đã duyệt</p>
        <p class="mt-2 text-3xl font-black text-emerald-900">{{ approvedCount }}</p>
      </div>
    </div>

    <div>
      <button
        @click="showForm = !showForm"
        class="inline-flex items-center gap-2 px-6 py-3 bg-blue-600 text-white font-semibold rounded-full hover:bg-blue-700 transition"
      >
        <Plus class="w-5 h-5" />
        {{ showForm ? 'Hủy tạo yêu cầu' : 'Tạo yêu cầu OT' }}
      </button>
    </div>

    <div v-if="showForm" class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm">
      <h2 class="text-xl font-black text-slate-900 mb-6">Thông tin làm thêm giờ</h2>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        <div class="grid gap-6 md:grid-cols-2">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Ngày làm thêm *</label>
            <input
              v-model="form.attendanceDate"
              type="date"
              class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
          </div>

          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Lý do *</label>
            <select
              v-model="form.reason"
              class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Chọn lý do</option>
              <option v-for="reason in reasonOptions" :key="reason" :value="reason">{{ reason }}</option>
            </select>
          </div>
        </div>

        <div class="grid gap-6 md:grid-cols-3">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Giờ bắt đầu *</label>
            <input
              v-model="form.overtimeStartAt"
              type="time"
              class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
          </div>

          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Giờ kết thúc *</label>
            <input
              v-model="form.overtimeEndAt"
              type="time"
              class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
          </div>

          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Thời lượng</label>
            <input
              :value="formatMinutes(toMinutes(form.overtimeStartAt, form.overtimeEndAt))"
              type="text"
              readonly
              class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-slate-900"
            >
          </div>
        </div>

        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Mã file minh chứng</label>
          <input
            v-model="form.evidenceFileKey"
            type="text"
            placeholder="Ví dụ: ot-proof-2026-04-09.pdf"
            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
        </div>

        <div class="flex gap-4">
          <button
            type="submit"
            :disabled="submitting"
            class="px-6 py-3 bg-blue-600 text-white font-semibold rounded-full hover:bg-blue-700 disabled:opacity-50 transition inline-flex items-center gap-2"
          >
            <Loader2 v-if="submitting" class="w-4 h-4 animate-spin" />
            <span>{{ submitting ? 'Đang gửi...' : 'Gửi yêu cầu OT' }}</span>
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

    <div class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm">
      <h2 class="text-xl font-black text-slate-900 mb-6">Lịch sử yêu cầu OT</h2>

      <div v-if="loading" class="space-y-4">
        <div v-for="item in 3" :key="item" class="h-32 animate-pulse rounded-3xl bg-slate-100" />
      </div>

      <div v-else-if="requests.length === 0" class="text-center py-8">
        <Clock class="w-16 h-16 text-slate-300 mx-auto mb-4" />
        <h3 class="text-lg font-semibold text-slate-900 mb-2">Chưa có yêu cầu nào</h3>
        <p class="text-slate-600">Khi bạn gửi yêu cầu OT, lịch sử sẽ hiển thị tại đây.</p>
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="request in requests"
          :key="request.overtimeRequestId"
          class="rounded-3xl border border-slate-100 bg-slate-50 p-6"
        >
          <div class="flex items-start justify-between gap-4 mb-4">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2 flex-wrap">
                <Calendar class="w-5 h-5 text-slate-400" />
                <span class="font-semibold text-slate-900">
                  {{ formatDate(request.attendanceDate) }} - {{ request.requestCode }}
                </span>
                <span class="px-3 py-1 rounded-full text-xs font-semibold" :class="getStatusColor(request.requestStatus)">
                  {{ getStatusLabel(request.requestStatus) }}
                </span>
              </div>
              <p class="text-sm text-slate-600 mb-1">
                <strong>Thời gian:</strong>
                {{ formatTime(request.overtimeStartAt) }} - {{ formatTime(request.overtimeEndAt) }}
                ({{ formatMinutes(request.requestedMinutes) }})
              </p>
              <p class="text-sm text-slate-600"><strong>Lý do:</strong> {{ request.reason }}</p>
            </div>
          </div>

          <div class="flex items-center justify-between text-sm text-slate-500 pt-4 border-t border-slate-200 flex-wrap gap-2">
            <span>Đã gửi: {{ formatDate(request.submittedAt) }}</span>
            <span v-if="request.approvedAt">Duyệt: {{ formatDate(request.approvedAt) }}</span>
            <span v-else-if="request.rejectedAt">Từ chối: {{ formatDate(request.rejectedAt) }}</span>
          </div>

          <div v-if="request.managerNote || request.rejectionNote || request.evidenceFileKey" class="mt-4 pt-4 border-t border-slate-200 space-y-2">
            <p v-if="request.managerNote" class="text-sm text-slate-700">
              <strong>Ghi chú quản lý:</strong> {{ request.managerNote }}
            </p>
            <p v-if="request.rejectionNote" class="text-sm text-rose-700">
              <strong>Lý do từ chối:</strong> {{ request.rejectionNote }}
            </p>
            <p v-if="request.evidenceFileKey" class="text-sm text-slate-600">
              <strong>Minh chứng:</strong> {{ request.evidenceFileKey }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
