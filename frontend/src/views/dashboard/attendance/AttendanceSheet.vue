<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Calendar, CheckCircle2, ChevronLeft, ChevronRight, Clock3, Download, Search, TriangleAlert, UserX } from 'lucide-vue-next'
import AvatarBox from '@/components/common/AvatarBox.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InsightCard from '@/components/hrm/InsightCard.vue'
import SurfacePanel from '@/components/hrm/SurfacePanel.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { exportAnomaliesReport, getAdjustmentRequests, getDailyAttendance, getOvertimeRequests } from '@/api/admin/attendance'
import { useToast } from '@/composables/useToast'
import { downloadBlob, unwrapPage } from '@/utils/api'
import { formatDate, formatDateTime, formatNumber, formatTime } from '@/utils/format'

const toast = useToast()

const selectedDate = ref(new Date().toISOString().slice(0, 10))
const searchKeyword = ref('')
const loading = ref(false)
const attendanceData = ref([])
const adjustmentRequests = ref([])
const overtimeRequests = ref([])
const attendancePage = ref(0)
const attendancePageSize = ref(9)

const attendanceCards = computed(() =>
  attendanceData.value.map((item) => ({
    ...item,
    displayName: item.employeeFullName || item.employeeName || 'Nhân sự chưa xác định',
    displayCheckIn: formatTime(item.actualCheckInAt) !== '—'
      ? formatTime(item.actualCheckInAt)
      : formatTime(item.plannedStartAt),
    displayCheckOut: formatTime(item.actualCheckOutAt) !== '—'
      ? formatTime(item.actualCheckOutAt)
      : formatTime(item.plannedEndAt),
    displayWorked: formatWorkedHours(item.workedMinutes),
  })),
)

const filteredAttendanceCards = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return attendanceCards.value

  return attendanceCards.value.filter((item) =>
    [item.displayName, item.employeeCode, item.orgUnitName]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(keyword)),
  )
})

const attendanceTotalPages = computed(() =>
  Math.max(1, Math.ceil(filteredAttendanceCards.value.length / attendancePageSize.value)),
)

const paginatedAttendanceCards = computed(() => {
  const start = attendancePage.value * attendancePageSize.value
  return filteredAttendanceCards.value.slice(start, start + attendancePageSize.value)
})

const attendanceVisiblePages = computed(() => {
  const total = attendanceTotalPages.value
  if (total <= 7) return Array.from({ length: total }, (_, index) => index)

  const current = attendancePage.value
  if (current <= 3) return [0, 1, 2, 3, 4, '...', total - 1]
  if (current >= total - 4) return [0, '...', total - 5, total - 4, total - 3, total - 2, total - 1]
  return [0, '...', current - 1, current, current + 1, '...', total - 1]
})

const stats = computed(() => {
  const total = attendanceData.value.length
  const present = attendanceData.value.filter((item) => item.dailyStatus === 'PRESENT').length
  const late = attendanceData.value.filter((item) => (item.anomalyCodes || []).includes('LATE')).length
  const absent = attendanceData.value.filter((item) => item.dailyStatus === 'ABSENT' || item.dailyStatus === 'INCOMPLETE').length

  return [
    {
      title: 'Hiện diện',
      value: present,
      subtitle: `${formatNumber(total)} nhân sự trong tập dữ liệu`,
      icon: CheckCircle2,
      tone: 'emerald',
    },
    {
      title: 'Đi muộn',
      value: late,
      subtitle: 'Cần theo dõi để tối ưu vận hành',
      icon: Clock3,
      tone: 'amber',
    },
    {
      title: 'Thiếu chấm công',
      value: absent,
      subtitle: 'Nhân sự chưa có log check-in/check-out',
      icon: UserX,
      tone: 'rose',
    },
    {
      title: 'Yêu cầu công chờ chốt',
      value: adjustmentRequests.value.filter((item) => ['MANAGER_APPROVED', 'SUBMITTED'].includes(item.requestStatus)).length + overtimeRequests.value.filter((item) => item.requestStatus === 'SUBMITTED').length,
      subtitle: `Ngày ${formatDate(selectedDate.value)}`,
      icon: Calendar,
      tone: 'indigo',
    },
  ]
})

const highlightedIssues = computed(() =>
  attendanceData.value
    .filter((item) => item.dailyStatus !== 'PRESENT' || (item.anomalyCodes || []).length)
    .slice(0, 4),
)

function formatWorkedHours(minutes) {
  if (minutes === null || minutes === undefined || minutes === '') return '—'

  const totalMinutes = Number(minutes)
  if (Number.isNaN(totalMinutes) || totalMinutes <= 0) return '—'

  const hours = Math.floor(totalMinutes / 60)
  const remainMinutes = totalMinutes % 60

  if (!hours) return `${remainMinutes}p`
  if (!remainMinutes) return `${hours}h`
  return `${hours}h ${remainMinutes}p`
}

async function fetchAttendance() {
  loading.value = true
  try {
    const [attendanceResponse, adjustmentResponse, overtimeResponse] = await Promise.all([
      getDailyAttendance({
        fromDate: selectedDate.value,
        toDate: selectedDate.value,
        size: 200,
      }),
      getAdjustmentRequests({
        keyword: searchKeyword.value || undefined,
        fromDate: selectedDate.value,
        toDate: selectedDate.value,
        size: 20,
      }),
      getOvertimeRequests({
        keyword: searchKeyword.value || undefined,
        fromDate: selectedDate.value,
        toDate: selectedDate.value,
        size: 20,
      }),
    ])
    attendanceData.value = unwrapPage(attendanceResponse).items
    adjustmentRequests.value = unwrapPage(adjustmentResponse).items
    overtimeRequests.value = unwrapPage(overtimeResponse).items
  } catch (error) {
    console.error('Failed to fetch attendance:', error)
    attendanceData.value = []
    adjustmentRequests.value = []
    overtimeRequests.value = []
    toast.error('Không thể tải dữ liệu chấm công')
  } finally {
    loading.value = false
  }
}

async function handleExport() {
  try {
    const blob = await exportAnomaliesReport({
      fromDate: selectedDate.value,
      toDate: selectedDate.value,
    })
    downloadBlob(blob, `attendance-anomalies-${selectedDate.value}.csv`)
  } catch (error) {
    console.error('Attendance export failed:', error)
    toast.error('Xuất báo cáo thất bại')
  }
}

function changeAttendancePage(page) {
  if (page < 0 || page >= attendanceTotalPages.value) return
  attendancePage.value = page
}

onMounted(fetchAttendance)
watch(selectedDate, () => {
  attendancePage.value = 0
  fetchAttendance()
})

watch(searchKeyword, () => {
  attendancePage.value = 0
  fetchAttendance()
})
</script>

<template>
  <div class="space-y-8">
    <PageHeader
      title="Nhật ký chấm công"
      subtitle="Theo dõi hiện diện hằng ngày với góc nhìn nhanh, gọn và ưu tiên các trường hợp bất thường."
      :icon="Clock3"
    >
      <template #actions>
        <div class="relative">
          <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="Tìm nhân sự..."
            class="w-64 rounded-2xl border border-slate-200 bg-white py-3 pl-10 pr-4 text-sm font-medium outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10"
          >
        </div>
        <div class="relative">
          <Calendar class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            v-model="selectedDate"
            type="date"
            class="rounded-2xl border border-slate-200 bg-white py-3 pl-10 pr-4 text-sm font-medium outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10"
          >
        </div>
        <BaseButton variant="outline" @click="handleExport">
          <Download class="mr-2 h-4 w-4" />
          Xuất bất thường
        </BaseButton>
      </template>
    </PageHeader>

    <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
      <InsightCard
        v-for="item in stats"
        :key="item.title"
        :title="item.title"
        :value="item.value"
        :subtitle="item.subtitle"
        :icon="item.icon"
        :tone="item.tone"
      />
    </div>

    <div v-if="highlightedIssues.length" class="grid gap-4 lg:grid-cols-4">
      <button
        v-for="item in highlightedIssues"
        :key="`${item.employeeId}-${item.attendanceDate}`"
        type="button"
        class="rounded-[28px] border border-amber-100 bg-amber-50/70 p-5 text-left transition-all hover:-translate-y-0.5 hover:border-amber-200 hover:shadow-lg"
      >
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-amber-100 text-amber-600">
            <TriangleAlert class="h-5 w-5" />
          </div>
          <div>
            <p class="text-sm font-black text-slate-900">{{ item.employeeName }}</p>
            <p class="text-xs font-bold uppercase tracking-[0.18em] text-amber-600">{{ item.dailyStatus || 'Thiếu log' }}</p>
          </div>
        </div>
        <p class="mt-4 text-sm font-medium text-slate-600">{{ item.orgUnitName || 'Chưa gán đơn vị' }}</p>
      </button>
    </div>

    <div class="grid gap-6 xl:grid-cols-2">
      <SurfacePanel>
        <div class="flex items-center justify-between gap-4">
          <div>
            <h3 class="text-xl font-black text-slate-900">Điều chỉnh công</h3>
            <p class="mt-1 text-sm font-medium text-slate-500">Các yêu cầu quên check-in/check-out hoặc cần hiệu chỉnh log trong ngày.</p>
          </div>
          <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-black uppercase tracking-[0.18em] text-slate-500">
            {{ adjustmentRequests.length }} yêu cầu
          </span>
        </div>

        <div v-if="adjustmentRequests.length" class="mt-5 space-y-3">
          <article
            v-for="item in adjustmentRequests.slice(0, 4)"
            :key="item.adjustmentRequestId"
            class="rounded-[24px] border border-slate-200 bg-slate-50 p-4"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-sm font-black text-slate-900">{{ item.employeeName }}</p>
                <p class="mt-1 text-xs font-bold uppercase tracking-[0.18em] text-slate-400">{{ item.requestCode }} · {{ item.issueType }}</p>
              </div>
              <StatusBadge :status="item.requestStatus || 'SUBMITTED'" />
            </div>
            <p class="mt-3 text-sm font-medium text-slate-600">{{ item.reason || 'Không có lý do bổ sung.' }}</p>
            <div class="mt-3 flex flex-wrap gap-3 text-xs font-bold text-slate-500">
              <span>Đề xuất vào: {{ formatTime(item.proposedCheckInAt) }}</span>
              <span>Đề xuất ra: {{ formatTime(item.proposedCheckOutAt) }}</span>
              <span>Gửi lúc: {{ formatDateTime(item.submittedAt) }}</span>
            </div>
          </article>
        </div>

        <EmptyState
          v-else
          iconName="Clock3"
          title="Không có yêu cầu điều chỉnh công"
          description="Ngày đang chọn chưa phát sinh yêu cầu điều chỉnh giờ công nào."
        />
      </SurfacePanel>

      <SurfacePanel>
        <div class="flex items-center justify-between gap-4">
          <div>
            <h3 class="text-xl font-black text-slate-900">Tăng ca trong ngày</h3>
            <p class="mt-1 text-sm font-medium text-slate-500">Hàng đợi OT giúp HR đối chiếu nhanh trước khi chốt kỳ công và lương.</p>
          </div>
          <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-black uppercase tracking-[0.18em] text-slate-500">
            {{ overtimeRequests.length }} yêu cầu
          </span>
        </div>

        <div v-if="overtimeRequests.length" class="mt-5 space-y-3">
          <article
            v-for="item in overtimeRequests.slice(0, 4)"
            :key="item.overtimeRequestId"
            class="rounded-[24px] border border-slate-200 bg-slate-50 p-4"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-sm font-black text-slate-900">{{ item.employeeName }}</p>
                <p class="mt-1 text-xs font-bold uppercase tracking-[0.18em] text-slate-400">{{ item.requestCode }} · {{ item.orgUnitName || '—' }}</p>
              </div>
              <StatusBadge :status="item.requestStatus || 'SUBMITTED'" />
            </div>
            <p class="mt-3 text-sm font-medium text-slate-600">{{ item.reason || 'Không có mô tả tăng ca.' }}</p>
            <div class="mt-3 flex flex-wrap gap-3 text-xs font-bold text-slate-500">
              <span>Bắt đầu: {{ formatTime(item.overtimeStartAt) }}</span>
              <span>Kết thúc: {{ formatTime(item.overtimeEndAt) }}</span>
              <span>{{ formatNumber(item.requestedMinutes || 0) }} phút</span>
            </div>
          </article>
        </div>

        <EmptyState
          v-else
          iconName="Calendar"
          title="Không có yêu cầu tăng ca"
          description="Ngày đang chọn chưa phát sinh đề nghị OT nào."
        />
      </SurfacePanel>
    </div>

    <SurfacePanel>
      <div class="mb-5 flex items-center justify-between gap-4">
        <div>
          <h3 class="text-xl font-black text-slate-900">Từng nhân sự trong ngày</h3>
          <p class="mt-1 text-sm font-medium text-slate-500">Danh sách dạng card giúp xem nhanh giờ vào, giờ ra và trạng thái hiện diện.</p>
        </div>
        <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-black uppercase tracking-[0.18em] text-slate-500">
          {{ filteredAttendanceCards.length }} bản ghi
        </span>
      </div>

      <div v-if="loading" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <div v-for="item in 6" :key="item" class="h-40 animate-pulse rounded-[28px] bg-slate-100" />
      </div>

      <div v-else-if="filteredAttendanceCards.length" class="space-y-6">
        <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <article
          v-for="item in paginatedAttendanceCards"
          :key="`${item.employeeId}-${item.attendanceDate}`"
          class="rounded-[28px] border border-slate-200 bg-white p-5 transition-all hover:border-indigo-200 hover:shadow-lg"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-center gap-4">
              <AvatarBox :name="item.displayName" size="lg" shape="rounded-[20px]" />
              <div>
                <h4 class="text-lg font-black text-slate-900">{{ item.displayName }}</h4>
                <p class="mt-1 text-sm font-medium text-slate-500">{{ item.employeeCode }} · {{ item.orgUnitName || '—' }}</p>
              </div>
            </div>
            <StatusBadge :status="item.dailyStatus || 'ABSENT'" />
          </div>

          <div class="mt-5 grid gap-4 rounded-[24px] bg-slate-50 p-4 md:grid-cols-3">
            <div>
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Check in</p>
              <p class="mt-2 text-sm font-bold text-slate-800">{{ item.displayCheckIn }}</p>
            </div>
            <div>
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Check out</p>
              <p class="mt-2 text-sm font-bold text-slate-800">{{ item.displayCheckOut }}</p>
            </div>
            <div>
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Tổng giờ</p>
              <p class="mt-2 text-sm font-bold text-slate-800">{{ item.displayWorked }}</p>
            </div>
          </div>

          <div class="mt-4 flex flex-wrap gap-2">
            <span
              v-if="item.plannedStartAt || item.plannedEndAt"
              class="rounded-full bg-indigo-50 px-3 py-1 text-xs font-black text-indigo-700"
            >
              Ca dự kiến {{ formatTime(item.plannedStartAt) }} - {{ formatTime(item.plannedEndAt) }}
            </span>
            <span
              v-for="code in item.anomalyCodes || []"
              :key="`${item.employeeId}-${code}`"
              class="rounded-full bg-amber-50 px-3 py-1 text-xs font-black text-amber-700"
            >
              {{ code }}
            </span>
          </div>
        </article>
        </div>

        <div v-if="attendanceTotalPages > 1" class="flex flex-wrap items-center justify-center gap-3">
          <BaseButton
            variant="outline"
            size="md"
            :disabled="attendancePage === 0"
            class="w-12! rounded-2xl! p-0!"
            @click="changeAttendancePage(attendancePage - 1)"
          >
            <ChevronLeft class="h-5 w-5" />
          </BaseButton>

          <div class="flex flex-wrap items-center justify-center gap-2">
            <template v-for="page in attendanceVisiblePages" :key="`attendance-page-${page}`">
              <button
                v-if="page !== '...'"
                type="button"
                class="h-10 w-10 rounded-2xl text-sm font-bold transition-all"
                :class="attendancePage === page
                  ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-100'
                  : 'border border-slate-100 bg-white text-slate-500 hover:bg-indigo-50 hover:text-indigo-600'"
                @click="changeAttendancePage(page)"
              >
                {{ page + 1 }}
              </button>
              <span v-else class="px-1 font-bold text-slate-400">...</span>
            </template>
          </div>

          <BaseButton
            variant="outline"
            size="md"
            :disabled="attendancePage === attendanceTotalPages - 1"
            class="w-12! rounded-2xl! p-0!"
            @click="changeAttendancePage(attendancePage + 1)"
          >
            <ChevronRight class="h-5 w-5" />
          </BaseButton>
        </div>
      </div>

      <EmptyState
        v-else
        iconName="Clock3"
        title="Không có dữ liệu chấm công"
        :description="`Ngày ${formatDate(selectedDate)} hiện chưa có bản ghi phù hợp với bộ lọc.`"
      />
    </SurfacePanel>
  </div>
</template>
