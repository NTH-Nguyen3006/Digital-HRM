<script setup>
import { onMounted, reactive, ref } from 'vue'
import { AlertTriangle, Calendar, Clock, FileText, Loader2, TimerReset } from 'lucide-vue-next'
import { getMyAttendance, submitAttendanceAdjustment } from '@/api/me/portal'
import { safeArray, unwrapData } from '@/utils/api'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const loading = ref(false)
const submitting = ref(false)
const showForm = ref(false)
const requests = ref([])

const form = reactive({
  issueType: '',
  date: '',
  checkInTime: '',
  checkOutTime: '',
  reason: '',
  evidenceFileKey: '',
})

const issueTypes = [
  { value: 'FORGOT_CHECK_IN', label: 'Quên chấm công vào' },
  { value: 'FORGOT_CHECK_OUT', label: 'Quên chấm công ra' },
  { value: 'WRONG_TIME', label: 'Chấm công sai giờ' },
  { value: 'EARLY_LEAVE', label: 'Về sớm' },
  { value: 'LATE_ARRIVAL', label: 'Đi muộn' },
  { value: 'OTHER', label: 'Lý do khác' },
]

function resetForm() {
  form.issueType = ''
  form.date = ''
  form.checkInTime = ''
  form.checkOutTime = ''
  form.reason = ''
  form.evidenceFileKey = ''
}

function toDateTime(date, time) {
  if (!date || !time) return null
  return `${date}T${time}:00`
}

function getIssueTypeLabel(issueType) {
  return issueTypes.find((item) => item.value === issueType)?.label || issueType || 'Điều chỉnh công'
}

function getStatusLabel(status) {
  const labels = {
    DRAFT: 'Nháp',
    SUBMITTED: 'Chờ duyệt',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Từ chối',
    FINALIZED: 'Đã chốt',
    CANCELED: 'Đã hủy',
  }
  return labels[status] || status || 'Không xác định'
}

function getStatusColor(status) {
  const styles = {
    DRAFT: 'bg-slate-100 text-slate-700',
    SUBMITTED: 'bg-amber-100 text-amber-800',
    APPROVED: 'bg-emerald-100 text-emerald-800',
    REJECTED: 'bg-rose-100 text-rose-800',
    FINALIZED: 'bg-indigo-100 text-indigo-800',
    CANCELED: 'bg-slate-100 text-slate-500',
  }
  return styles[status] || 'bg-slate-100 text-slate-700'
}

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('vi-VN')
}

function formatDateTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN')
}

function formatTimeRange(request) {
  const checkIn = request.proposedCheckInAt ? new Date(request.proposedCheckInAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : '—'
  const checkOut = request.proposedCheckOutAt ? new Date(request.proposedCheckOutAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : '—'
  return `${checkIn} - ${checkOut}`
}

async function fetchRequests() {
  loading.value = true
  try {
    const response = await getMyAttendance()
    const data = unwrapData(response)
    requests.value = safeArray(data?.adjustmentRequests)
  } catch (error) {
    console.error('Failed to fetch attendance adjustments:', error)
    requests.value = []
    toast.error('Không thể tải yêu cầu điều chỉnh chấm công')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.issueType || !form.date || !form.reason.trim()) {
    toast.warning('Vui lòng nhập đủ loại vấn đề, ngày và lý do')
    return
  }

  submitting.value = true
  try {
    await submitAttendanceAdjustment({
      attendanceDate: form.date,
      issueType: form.issueType,
      proposedCheckInAt: toDateTime(form.date, form.checkInTime),
      proposedCheckOutAt: toDateTime(form.date, form.checkOutTime),
      reason: form.reason.trim(),
      evidenceFileKey: form.evidenceFileKey || null,
      submit: true,
    })
    toast.success('Đã gửi yêu cầu điều chỉnh chấm công')
    resetForm()
    showForm.value = false
    await fetchRequests()
  } catch (error) {
    console.error('Failed to submit attendance adjustment:', error)
    toast.error(error.response?.data?.message || 'Gửi yêu cầu thất bại')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchRequests)
</script>

<template>
  <div class="mx-auto max-w-4xl space-y-8 px-6 py-8">
    <div>
      <h1 class="text-3xl font-black text-slate-900 mb-2">Điều chỉnh chấm công</h1>
      <p class="text-slate-600">Gửi giải trình công và theo dõi trạng thái phê duyệt từ dữ liệu thực tế của hệ thống.</p>
    </div>

    <div class="rounded-4xl border border-amber-200 bg-amber-50 p-6">
      <div class="flex items-start gap-3">
        <AlertTriangle class="w-6 h-6 text-amber-600 mt-0.5" />
        <div class="text-sm text-amber-700 space-y-1">
          <p class="font-semibold text-amber-800">Lưu ý quan trọng</p>
          <p>Gửi yêu cầu càng sớm càng tốt để HR và quản lý có thể xử lý đúng kỳ công.</p>
          <p>Bạn có thể đính kèm tên file minh chứng nếu hệ thống lưu trữ nội bộ đã cấp mã file trước đó.</p>
        </div>
      </div>
    </div>

    <div>
      <button
        @click="showForm = !showForm"
        class="inline-flex items-center gap-2 px-6 py-3 bg-teal-600 text-white font-semibold rounded-full hover:bg-teal-700 transition"
      >
        <TimerReset class="w-5 h-5" />
        {{ showForm ? 'Hủy tạo yêu cầu' : 'Tạo yêu cầu mới' }}
      </button>
    </div>

    <div v-if="showForm" class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm">
      <h2 class="text-xl font-black text-slate-900 mb-6">Tạo yêu cầu điều chỉnh</h2>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        <div class="grid gap-6 md:grid-cols-2">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Loại vấn đề *</label>
            <select
              v-model="form.issueType"
              class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
            >
              <option value="">Chọn loại vấn đề</option>
              <option v-for="type in issueTypes" :key="type.value" :value="type.value">
                {{ type.label }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Ngày cần điều chỉnh *</label>
            <input
              v-model="form.date"
              type="date"
              class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
            >
          </div>
        </div>

        <div class="grid gap-6 md:grid-cols-2">
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Giờ vào đề xuất</label>
            <input
              v-model="form.checkInTime"
              type="time"
              class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
            >
          </div>

          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">Giờ ra đề xuất</label>
            <input
              v-model="form.checkOutTime"
              type="time"
              class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
            >
          </div>
        </div>

        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Lý do *</label>
          <textarea
            v-model="form.reason"
            rows="4"
            class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
            placeholder="Mô tả rõ lý do cần điều chỉnh công..."
          />
        </div>

        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Mã file minh chứng</label>
          <input
            v-model="form.evidenceFileKey"
            type="text"
            placeholder="Ví dụ: attendance-proof-2026-04-09.pdf"
            class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
          >
        </div>

        <div class="flex gap-4">
          <button
            type="submit"
            :disabled="submitting"
            class="px-6 py-3 bg-teal-600 text-white font-semibold rounded-full hover:bg-teal-700 disabled:opacity-50 transition inline-flex items-center gap-2"
          >
            <Loader2 v-if="submitting" class="w-4 h-4 animate-spin" />
            <span>{{ submitting ? 'Đang gửi...' : 'Gửi yêu cầu' }}</span>
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
      <div v-for="item in 3" :key="item" class="h-40 animate-pulse rounded-4xl bg-slate-100" />
    </div>

    <div v-else-if="requests.length" class="space-y-6">
      <div
        v-for="request in requests"
        :key="request.adjustmentRequestId"
        class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm"
      >
        <div class="flex items-start justify-between gap-4 mb-6">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2 flex-wrap">
              <Clock class="w-6 h-6 text-slate-400" />
              <h3 class="text-xl font-black text-slate-900">
                {{ getIssueTypeLabel(request.issueType) }}
              </h3>
              <span class="px-3 py-1 rounded-full text-xs font-semibold" :class="getStatusColor(request.requestStatus)">
                {{ getStatusLabel(request.requestStatus) }}
              </span>
            </div>
            <p class="text-slate-600">{{ request.reason }}</p>
          </div>
          <div class="text-right text-sm text-slate-500">
            <p class="font-bold text-slate-700">{{ request.requestCode }}</p>
            <p>{{ formatDate(request.attendanceDate) }}</p>
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-xs font-bold uppercase tracking-wider text-slate-400">Ngày công</p>
            <p class="mt-2 font-semibold text-slate-900">{{ formatDate(request.attendanceDate) }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-xs font-bold uppercase tracking-wider text-slate-400">Khung giờ đề xuất</p>
            <p class="mt-2 font-semibold text-slate-900">{{ formatTimeRange(request) }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-xs font-bold uppercase tracking-wider text-slate-400">Đã gửi</p>
            <p class="mt-2 font-semibold text-slate-900">{{ formatDateTime(request.submittedAt) }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-xs font-bold uppercase tracking-wider text-slate-400">Phản hồi gần nhất</p>
            <p class="mt-2 font-semibold text-slate-900">
              {{ formatDateTime(request.finalizedAt || request.approvedAt || request.rejectedAt || request.canceledAt) }}
            </p>
          </div>
        </div>

        <div v-if="request.managerNote || request.rejectionNote || request.finalizeNote || request.cancelNote || request.evidenceFileKey" class="mt-5 space-y-3">
          <div v-if="request.managerNote" class="rounded-2xl bg-emerald-50 p-4 text-sm text-emerald-800">
            <span class="font-bold">Ghi chú quản lý:</span> {{ request.managerNote }}
          </div>
          <div v-if="request.rejectionNote" class="rounded-2xl bg-rose-50 p-4 text-sm text-rose-800">
            <span class="font-bold">Lý do từ chối:</span> {{ request.rejectionNote }}
          </div>
          <div v-if="request.finalizeNote" class="rounded-2xl bg-indigo-50 p-4 text-sm text-indigo-800">
            <span class="font-bold">Ghi chú chốt công:</span> {{ request.finalizeNote }}
          </div>
          <div v-if="request.cancelNote" class="rounded-2xl bg-slate-100 p-4 text-sm text-slate-700">
            <span class="font-bold">Lý do hủy:</span> {{ request.cancelNote }}
          </div>
          <div v-if="request.evidenceFileKey" class="rounded-2xl bg-slate-50 p-4 text-sm text-slate-700">
            <FileText class="w-4 h-4 inline-block mr-2" />
            {{ request.evidenceFileKey }}
          </div>
        </div>
      </div>
    </div>

    <div v-else class="rounded-4xl border border-slate-200 bg-white p-16 shadow-sm text-center">
      <TimerReset class="w-20 h-20 text-slate-300 mx-auto mb-6" />
      <h3 class="text-xl font-bold text-slate-900 mb-2">Chưa có yêu cầu nào</h3>
      <p class="text-slate-600">Khi bạn gửi điều chỉnh công, dữ liệu sẽ hiển thị tại đây từ backend.</p>
    </div>
  </div>
</template>
