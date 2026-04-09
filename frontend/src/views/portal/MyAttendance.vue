<script setup>
import { computed, onMounted, ref } from 'vue'
import { Clock3, Fingerprint, TimerReset, TriangleAlert } from 'lucide-vue-next'
import EmployeeLayout from '@/components/EmployeeLayout.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import InsightCard from '@/components/hrm/InsightCard.vue'
import SurfacePanel from '@/components/hrm/SurfacePanel.vue'
import { getMyAttendance } from '@/api/me/portal'
import { useToast } from '@/composables/useToast'
import { safeArray, unwrapData } from '@/utils/api'
import { formatDate, formatTime } from '@/utils/format'

const toast = useToast()

const loading = ref(false)
const history = ref([])
const adjustmentRequests = ref([])
const overtimeRequests = ref([])

const stats = computed(() => [
  {
    title: 'Log chấm công',
    value: history.value.length,
    subtitle: 'Các lần check-in/check-out gần đây',
    icon: Fingerprint,
    tone: 'indigo',
  },
  {
    title: 'Điều chỉnh công',
    value: adjustmentRequests.value.length,
    subtitle: 'Đơn giải trình đã tạo',
    icon: TimerReset,
    tone: 'amber',
  },
  {
    title: 'Đơn OT',
    value: overtimeRequests.value.length,
    subtitle: 'Theo dõi tăng ca cá nhân',
    icon: Clock3,
    tone: 'emerald',
  },
  {
    title: 'Bất thường',
    value: history.value.filter((item) => item.note || item.anomalyCode).length,
    subtitle: 'Log có ghi chú cần lưu ý',
    icon: TriangleAlert,
    tone: 'rose',
  },
])

async function fetchAttendance() {
  loading.value = true
  try {
    const response = await getMyAttendance()
    const data = unwrapData(response)
    history.value = safeArray(data?.recentLogs)
    adjustmentRequests.value = safeArray(data?.adjustmentRequests)
    overtimeRequests.value = safeArray(data?.overtimeRequests)
  } catch (error) {
    console.error('Failed to fetch my attendance:', error)
    history.value = []
    adjustmentRequests.value = []
    overtimeRequests.value = []
    toast.error('Không thể tải lịch sử chấm công')
  } finally {
    loading.value = false
  }
}

onMounted(fetchAttendance)
</script>

<template>
  <EmployeeLayout>
    <div class="mx-auto max-w-6xl space-y-8 px-6 py-10">
      <div>
        <p class="text-[11px] font-black uppercase tracking-[0.2em] text-indigo-500">Attendance Overview</p>
        <h1 class="mt-3 text-4xl font-black tracking-tight text-slate-900">Lịch sử chấm công của tôi</h1>
        <p class="mt-2 max-w-3xl text-sm font-medium text-slate-500">
          Xem log vào ra, điều chỉnh công và đơn tăng ca trong cùng một luồng thông tin.
        </p>
      </div>

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

      <SurfacePanel>
        <div v-if="loading" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <div v-for="item in 6" :key="item" class="h-44 animate-pulse rounded-[28px] bg-slate-100" />
        </div>

        <div v-else-if="history.length" class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
          <article
            v-for="item in history"
            :key="item.attendanceLogId"
            class="rounded-[28px] border border-slate-200 bg-white p-5 transition-all hover:border-indigo-200 hover:shadow-lg"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">{{ item.eventType || 'ATTENDANCE' }}</p>
                <h3 class="mt-3 text-lg font-black text-slate-900">{{ formatDate(item.attendanceDate || item.eventTime) }}</h3>
                <p class="mt-1 text-sm font-medium text-slate-500">{{ formatTime(item.eventTime) }}</p>
              </div>
              <StatusBadge :status="item.status || (item.eventType === 'CHECK_IN' ? 'ACTIVE' : 'COMPLETED')" />
            </div>

            <div class="mt-5 rounded-[24px] bg-slate-50 p-4">
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Ghi chú</p>
              <p class="mt-2 text-sm font-medium text-slate-700">{{ item.note || item.anomalyCode || 'Không có ghi chú bổ sung' }}</p>
            </div>
          </article>
        </div>

        <EmptyState
          v-else
          iconName="Clock3"
          title="Chưa có log chấm công"
          description="Khi backend có log chấm công gắn với tài khoản của bạn, dữ liệu sẽ hiển thị tại đây."
        />
      </SurfacePanel>
    </div>
  </EmployeeLayout>
</template>
