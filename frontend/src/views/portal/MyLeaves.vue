<script setup>
import { computed, onMounted, ref } from 'vue'
import { Calendar, CheckCircle2, Clock3, FileText, Loader2, Plus, Send, XCircle } from 'lucide-vue-next'
import { getMyLeaveTypes, getMyLeaves, submitLeaveRequest } from '@/api/me/portal'
import { useToast } from '@/composables/useToast'
import { unwrapData } from '@/utils/api'

const toast = useToast()

const leaves = ref([])
const balances = ref([])
const leaveTypes = ref([])
const loading = ref(false)
const showModal = ref(false)
const submitting = ref(false)

const form = ref({
  leaveTypeId: '',
  startDate: '',
  endDate: '',
  reason: '',
})

const selectedLeaveType = computed(() =>
  leaveTypeOptions.value.find((item) => String(item.leaveTypeId) === String(form.value.leaveTypeId)) || null
)

const leaveTypeOptions = computed(() => {
  const balanceLookup = new Map(
    balances.value.map((item) => [String(item.leaveTypeId), item])
  )

  return leaveTypes.value.map((item) => ({
    ...item,
    availableUnits: balanceLookup.get(String(item.leaveTypeId))?.availableUnits ?? null,
    leaveYear: balanceLookup.get(String(item.leaveTypeId))?.leaveYear ?? null,
  }))
})

const leaveStats = computed(() => {
  const totalAvailable = balances.value.reduce((sum, item) => sum + Number(item.availableUnits || 0), 0)
  const totalUsed = balances.value.reduce((sum, item) => sum + Number(item.usedUnits || 0), 0)
  const pendingRequests = leaves.value.filter((item) => item.requestStatus === 'PENDING').length
  const approvedRequests = leaves.value.filter((item) => item.requestStatus === 'APPROVED').length

  return [
    {
      label: 'Tổng phép còn lại',
      value: formatUnits(totalAvailable),
      hint: `${balances.value.length} loại nghỉ đang khả dụng`,
      icon: Calendar,
      tone: 'bg-indigo-50 text-indigo-600',
    },
    {
      label: 'Phép đã sử dụng',
      value: formatUnits(totalUsed),
      hint: 'Tính theo số dư hiện có',
      icon: Clock3,
      tone: 'bg-amber-50 text-amber-600',
    },
    {
      label: 'Đơn đang chờ',
      value: pendingRequests.toString(),
      hint: 'Đang chờ quản lý hoặc HR xử lý',
      icon: FileText,
      tone: 'bg-sky-50 text-sky-600',
    },
    {
      label: 'Đơn đã duyệt',
      value: approvedRequests.toString(),
      hint: 'Lịch sử đã được chấp thuận',
      icon: CheckCircle2,
      tone: 'bg-emerald-50 text-emerald-600',
    },
  ]
})

const fetchLeaves = async () => {
  loading.value = true
  try {
    const data = unwrapData(await getMyLeaves()) || {}
    balances.value = Array.isArray(data.balances) ? data.balances : []
    leaves.value = Array.isArray(data.recentRequests) ? data.recentRequests : []
  } catch (error) {
    console.error('Failed to fetch leave overview:', error)
    balances.value = []
    leaves.value = []
    toast.error('Không thể tải dữ liệu nghỉ phép')
  } finally {
    loading.value = false
  }
}

const fetchLeaveTypes = async () => {
  try {
    const data = unwrapData(await getMyLeaveTypes())
    leaveTypes.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('Failed to fetch leave types:', error)
    leaveTypes.value = []
  }
}

const resetForm = () => {
  form.value = {
    leaveTypeId: '',
    startDate: '',
    endDate: '',
    reason: '',
  }
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const handleSubmit = async () => {
  if (!form.value.leaveTypeId) {
    toast.warning('Vui lòng chọn loại nghỉ')
    return
  }

  if (!form.value.startDate || !form.value.endDate) {
    toast.warning('Vui lòng chọn khoảng thời gian nghỉ')
    return
  }

  if (form.value.startDate > form.value.endDate) {
    toast.warning('Ngày bắt đầu không thể lớn hơn ngày kết thúc')
    return
  }

  if (!form.value.reason.trim()) {
    toast.warning('Vui lòng nhập lý do xin nghỉ')
    return
  }

  submitting.value = true
  try {
    await submitLeaveRequest({
      leaveTypeId: Number(form.value.leaveTypeId),
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      reason: form.value.reason.trim(),
      submit: true,
    })
    toast.success('Đã gửi đơn xin nghỉ phép')
    closeModal()
    await fetchLeaves()
  } catch (error) {
    console.error('Failed to submit leave request:', error)
    toast.error(error?.response?.data?.message || 'Gửi đơn thất bại')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchLeaves(), fetchLeaveTypes()])
})

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('vi-VN')
}

function formatDateTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatUnits(value) {
  const num = Number(value || 0)
  return Number.isInteger(num) ? `${num}` : num.toFixed(1)
}

function statusLabel(status) {
  const map = {
    PENDING: 'Đang chờ duyệt',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Từ chối',
    CANCELED: 'Đã hủy',
    FINALIZED: 'Đã chốt',
    DRAFT: 'Nháp',
  }
  return map[status] || status || 'Chưa xác định'
}

function statusClass(status) {
  const map = {
    PENDING: 'bg-amber-50 text-amber-700 border-amber-200',
    APPROVED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    REJECTED: 'bg-rose-50 text-rose-700 border-rose-200',
    CANCELED: 'bg-slate-100 text-slate-700 border-slate-200',
    FINALIZED: 'bg-indigo-50 text-indigo-700 border-indigo-200',
    DRAFT: 'bg-sky-50 text-sky-700 border-sky-200',
  }
  return map[status] || 'bg-slate-100 text-slate-700 border-slate-200'
}
</script>

<template>
  <div class="mx-auto max-w-6xl space-y-8 px-4 pb-10 md:px-6">
    <section class="rounded-[32px] border border-slate-200/80 bg-[linear-gradient(135deg,#ffffff_0%,#f8fbff_100%)] p-6 shadow-[0_18px_60px_rgba(15,23,42,0.06)] md:p-8">
      <div class="flex flex-col gap-6 xl:flex-row xl:items-start xl:justify-between">
        <div class="max-w-3xl">
          <p class="text-[11px] font-black uppercase tracking-[0.2em] text-indigo-500">Leave Center</p>
          <h1 class="mt-3 text-4xl font-black tracking-tight text-slate-900 md:text-5xl">Quản lý nghỉ phép cá nhân</h1>
          <p class="mt-3 text-sm font-medium leading-relaxed text-slate-500 md:text-base">
            Theo dõi số dư phép, chọn đúng loại nghỉ và gửi đơn trực tiếp tới luồng xét duyệt từ một màn hình duy nhất.
          </p>
        </div>

        <button
          @click="showModal = true"
          class="inline-flex items-center justify-center gap-2 rounded-2xl bg-indigo-600 px-6 py-3 font-bold text-white transition hover:bg-indigo-700 shadow-xl shadow-indigo-100"
        >
          <Plus class="h-5 w-5" />
          Nộp đơn mới
        </button>
      </div>

      <div class="mt-8 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <div
          v-for="item in leaveStats"
          :key="item.label"
          class="rounded-[26px] border border-slate-200/80 bg-white p-5 shadow-[0_12px_40px_rgba(15,23,42,0.05)]"
        >
          <div class="flex items-center justify-between">
            <div class="flex h-11 w-11 items-center justify-center rounded-2xl" :class="item.tone">
              <component :is="item.icon" class="h-5 w-5" />
            </div>
          </div>
          <p class="mt-4 text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">{{ item.label }}</p>
          <p class="mt-3 text-3xl font-black text-slate-900">{{ item.value }}</p>
          <p class="mt-2 text-sm font-medium text-slate-500">{{ item.hint }}</p>
        </div>
      </div>
    </section>

    <section class="grid gap-6 xl:grid-cols-[minmax(0,1.15fr)_minmax(320px,0.85fr)]">
      <div class="rounded-[30px] border border-slate-200 bg-white p-6 shadow-[0_16px_50px_rgba(15,23,42,0.05)] md:p-8">
        <div class="mb-6 flex items-center gap-3">
          <Calendar class="h-5 w-5 text-indigo-500" />
          <div>
            <h2 class="text-2xl font-black text-slate-900">Số dư theo loại nghỉ</h2>
            <p class="mt-1 text-sm font-medium text-slate-500">Danh sách loại nghỉ đang áp dụng cho tài khoản của bạn.</p>
          </div>
        </div>

        <div v-if="loading && !balances.length" class="grid gap-4 sm:grid-cols-2">
          <div v-for="item in 4" :key="item" class="h-40 animate-pulse rounded-[28px] bg-slate-100" />
        </div>

        <div v-else-if="balances.length" class="grid gap-4 sm:grid-cols-2">
          <article
            v-for="balance in balances"
            :key="balance.leaveBalanceId || balance.leaveTypeId"
            class="rounded-[28px] border border-slate-200/80 bg-[linear-gradient(180deg,#ffffff_0%,#f8fafc_100%)] p-5"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">{{ balance.leaveTypeName }}</p>
                <p class="mt-2 text-sm font-semibold text-slate-500">{{ balance.leaveTypeCode || 'Không có mã loại nghỉ' }}</p>
              </div>
              <span class="rounded-full bg-slate-100 px-3 py-1 text-[11px] font-black uppercase tracking-wider text-slate-600">
                {{ balance.leaveYear || 'Năm nay' }}
              </span>
            </div>

            <div class="mt-5 flex items-end gap-2">
              <p class="text-4xl font-black text-slate-900">{{ formatUnits(balance.availableUnits) }}</p>
              <p class="pb-1 text-sm font-bold text-slate-400">ngày còn lại</p>
            </div>

            <div class="mt-5 grid gap-3 text-sm font-medium text-slate-500 sm:grid-cols-2">
              <div class="rounded-2xl bg-slate-50 p-3">
                <p class="text-[10px] font-black uppercase tracking-wider text-slate-400">Đã dùng</p>
                <p class="mt-2 text-lg font-black text-slate-900">{{ formatUnits(balance.usedUnits) }}</p>
              </div>
              <div class="rounded-2xl bg-slate-50 p-3">
                <p class="text-[10px] font-black uppercase tracking-wider text-slate-400">Tổng cộng</p>
                <p class="mt-2 text-lg font-black text-slate-900">{{ formatUnits(balance.totalUnits || balance.openingUnits + balance.accruedUnits) }}</p>
              </div>
            </div>
          </article>
        </div>

        <div v-else class="rounded-[28px] border border-dashed border-slate-200 bg-slate-50 px-6 py-10 text-center">
          <p class="text-lg font-black text-slate-900">Chưa có quỹ phép khả dụng</p>
          <p class="mt-2 text-sm font-medium text-slate-500">Cần HR cấp số dư phép hoặc seed dữ liệu loại nghỉ cho tài khoản này.</p>
        </div>
      </div>

      <div class="rounded-[30px] border border-slate-200 bg-slate-950 p-6 text-white shadow-[0_20px_60px_rgba(15,23,42,0.16)] md:p-8">
        <div class="flex items-center gap-3 text-indigo-300">
          <Send class="h-5 w-5" />
          <p class="text-xs font-black uppercase tracking-[0.18em]">Tạo đơn nhanh</p>
        </div>

        <div class="mt-6 space-y-5">
          <div class="rounded-[24px] border border-white/10 bg-white/5 p-4">
            <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Loại nghỉ đang chọn</p>
            <p class="mt-2 text-lg font-black text-white">{{ selectedLeaveType?.leaveTypeName || 'Chưa chọn loại nghỉ' }}</p>
            <p class="mt-1 text-sm font-medium text-slate-300">
              {{
                selectedLeaveType
                  ? selectedLeaveType.availableUnits === null
                    ? 'Chưa có số dư phép được ghi nhận cho loại nghỉ này.'
                    : `Còn ${formatUnits(selectedLeaveType.availableUnits)} ngày`
                  : 'Mở biểu mẫu để chọn loại nghỉ phù hợp.'
              }}
            </p>
          </div>

          <div class="rounded-[24px] border border-white/10 bg-white/5 p-4">
            <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Lưu ý</p>
            <ul class="mt-3 space-y-2 text-sm font-medium text-slate-300">
              <li>Chỉ các loại nghỉ có trong quỹ phép mới được chọn.</li>
              <li>Ngày bắt đầu và kết thúc sẽ được backend kiểm tra theo rule của loại nghỉ.</li>
              <li>Lý do xin nghỉ là bắt buộc trước khi gửi.</li>
            </ul>
          </div>

          <button
            @click="showModal = true"
            class="w-full rounded-2xl bg-[linear-gradient(135deg,#4f46e5_0%,#4338ca_100%)] px-5 py-4 font-black text-white transition hover:brightness-110"
          >
            Mở biểu mẫu xin nghỉ
          </button>
        </div>
      </div>
    </section>

    <section class="rounded-[30px] border border-slate-200 bg-white p-6 shadow-[0_16px_50px_rgba(15,23,42,0.05)] md:p-8">
      <div class="mb-6 flex items-center gap-3">
        <Clock3 class="h-5 w-5 text-indigo-500" />
        <div>
          <h2 class="text-2xl font-black text-slate-900">Lịch sử đơn nghỉ</h2>
          <p class="mt-1 text-sm font-medium text-slate-500">Theo dõi loại nghỉ, mã đơn, trạng thái và thời điểm gửi đơn.</p>
        </div>
      </div>

      <div v-if="loading" class="grid gap-4">
        <div v-for="item in 4" :key="item" class="h-28 animate-pulse rounded-[26px] bg-slate-100" />
      </div>

      <div v-else-if="leaves.length" class="space-y-4">
        <article
          v-for="leave in leaves"
          :key="leave.leaveRequestId"
          class="rounded-[28px] border border-slate-200 bg-[linear-gradient(180deg,#ffffff_0%,#f8fafc_100%)] p-5 transition hover:border-indigo-200 hover:shadow-lg"
        >
          <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-3">
                <h3 class="text-lg font-black text-slate-900">{{ leave.leaveTypeName || 'Đơn xin nghỉ phép' }}</h3>
                <span class="rounded-full border px-3 py-1 text-[11px] font-black uppercase tracking-wider" :class="statusClass(leave.requestStatus)">
                  {{ statusLabel(leave.requestStatus) }}
                </span>
              </div>

              <div class="mt-3 grid gap-3 text-sm font-medium text-slate-500 md:grid-cols-2 xl:grid-cols-4">
                <p><span class="font-black text-slate-900">Mã đơn:</span> {{ leave.requestCode || '—' }}</p>
                <p><span class="font-black text-slate-900">Từ ngày:</span> {{ formatDate(leave.startDate) }}</p>
                <p><span class="font-black text-slate-900">Đến ngày:</span> {{ formatDate(leave.endDate) }}</p>
                <p><span class="font-black text-slate-900">Số ngày:</span> {{ formatUnits(leave.requestedUnits) }}</p>
              </div>

              <p class="mt-4 text-sm font-medium leading-relaxed text-slate-600">{{ leave.reason || 'Không có lý do đi kèm.' }}</p>
            </div>

            <div class="rounded-[22px] bg-slate-50 px-4 py-3 text-sm font-medium text-slate-500 xl:min-w-[220px]">
              <p><span class="font-black text-slate-900">Gửi lúc:</span> {{ formatDateTime(leave.submittedAt) }}</p>
              <p class="mt-2"><span class="font-black text-slate-900">Cấp duyệt:</span> {{ leave.approvalRoleCode || 'Theo workflow hệ thống' }}</p>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="rounded-[28px] border border-dashed border-slate-200 bg-slate-50 px-6 py-12 text-center">
        <p class="text-lg font-black text-slate-900">Chưa có đơn nghỉ phép nào</p>
        <p class="mt-2 text-sm font-medium text-slate-500">Khi bạn tạo đơn mới, toàn bộ lịch sử xử lý sẽ hiển thị ở khu vực này.</p>
      </div>
    </section>
  </div>

  <Teleport to="body">
    <Transition name="modal">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-950/50 backdrop-blur-sm" @click="closeModal" />
        <div class="relative z-10 w-full max-w-xl rounded-[28px] bg-white p-6 shadow-2xl md:p-7">
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">Leave Request</p>
              <h3 class="mt-2 text-2xl font-black text-slate-900">Đơn xin nghỉ phép</h3>
            </div>
            <button class="rounded-2xl p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700" @click="closeModal">
              <XCircle class="h-5 w-5" />
            </button>
          </div>

          <div class="mt-6 space-y-4">
            <div>
              <label class="mb-1.5 block text-xs font-black uppercase tracking-widest text-slate-500">Loại nghỉ</label>
              <select
                v-model="form.leaveTypeId"
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-bold text-slate-900 outline-none transition focus:border-indigo-200 focus:bg-white"
              >
                <option value="" disabled>Chọn loại nghỉ...</option>
                <option v-for="item in leaveTypeOptions" :key="item.leaveTypeId" :value="String(item.leaveTypeId)">
                  {{
                    item.availableUnits === null
                      ? `${item.leaveTypeName}`
                      : `${item.leaveTypeName} (Còn ${formatUnits(item.availableUnits)} ngày)`
                  }}
                </option>
              </select>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="mb-1.5 block text-xs font-black uppercase tracking-widest text-slate-500">Từ ngày</label>
                <input
                  v-model="form.startDate"
                  type="date"
                  class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-bold text-slate-900 outline-none transition focus:border-indigo-200 focus:bg-white"
                />
              </div>
              <div>
                <label class="mb-1.5 block text-xs font-black uppercase tracking-widest text-slate-500">Đến ngày</label>
                <input
                  v-model="form.endDate"
                  type="date"
                  class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-bold text-slate-900 outline-none transition focus:border-indigo-200 focus:bg-white"
                />
              </div>
            </div>

            <div v-if="selectedLeaveType" class="rounded-[22px] border border-indigo-100 bg-indigo-50/70 p-4 text-sm font-medium text-slate-600">
              Bạn đang chọn <span class="font-black text-slate-900">{{ selectedLeaveType.leaveTypeName }}</span>.
              <template v-if="selectedLeaveType.availableUnits !== null">
                Hệ thống hiện ghi nhận còn <span class="font-black text-indigo-600">{{ formatUnits(selectedLeaveType.availableUnits) }} ngày</span> khả dụng.
              </template>
              <template v-else>
                Hệ thống chưa có số dư phép cho loại nghỉ này, nhưng bạn vẫn có thể gửi nếu rule backend cho phép.
              </template>
            </div>

            <div>
              <label class="mb-1.5 block text-xs font-black uppercase tracking-widest text-slate-500">Lý do</label>
              <textarea
                v-model="form.reason"
                rows="4"
                class="w-full resize-none rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-900 outline-none transition focus:border-indigo-200 focus:bg-white"
                placeholder="Mô tả ngắn gọn lý do xin nghỉ..."
              />
            </div>
          </div>

          <div class="mt-6 flex gap-3">
            <button
              @click="closeModal"
              class="flex-1 rounded-xl bg-slate-100 py-3 font-bold text-slate-600 transition hover:bg-slate-200"
            >
              Hủy
            </button>
            <button
              @click="handleSubmit"
              :disabled="submitting"
              class="flex flex-1 items-center justify-center gap-2 rounded-xl bg-indigo-600 py-3 font-bold text-white transition hover:bg-indigo-700 disabled:opacity-60"
            >
              <Loader2 v-if="submitting" class="h-4 w-4 animate-spin" />
              Gửi đơn
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
  transform: scale(0.96);
}
</style>
