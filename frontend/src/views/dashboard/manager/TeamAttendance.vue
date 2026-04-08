<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useToast } from '@/composables/useToast'
import { useUiStore } from '@/stores/ui'
import {
  getPendingAttendanceAdjustments,
  reviewAttendanceAdjustment,
  getPendingOvertimeRequests,
  reviewOvertimeRequest
} from '@/api/manager/manager'
import {
  Clock, TrendingUp, Check, X, Loader2, Calendar
} from 'lucide-vue-next'

/* ─── COMPOSABLES ────────────────────────────────────────────── */
const toast = useToast()
const ui    = useUiStore()

/* ─── STATE ─────────────────────────────────────────────────── */
const activeTab = ref('adjustment')
const adjustments = ref([])
const overtimes = ref([])
const loading = ref(false)
const reviewLoading = ref(null)

/* ─── API ────────────────────────────────────────────────────── */
const fetchAdjustments = async () => {
  loading.value = true
  try {
    const res = await getPendingAttendanceAdjustments()
    adjustments.value = res.data?.content ?? res.data ?? []
  } catch (e) {
    toast.error('Không thể tải yêu cầu điều chỉnh công')
  } finally {
    loading.value = false
  }
}

const fetchOvertimes = async () => {
  loading.value = true
  try {
    const res = await getPendingOvertimeRequests()
    overtimes.value = res.data?.content ?? res.data ?? []
  } catch (e) {
    toast.error('Không thể tải yêu cầu tăng ca')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAdjustments()
  fetchOvertimes()
})

/* ─── ACTIONS ────────────────────────────────────────────────── */
const handleAdjustment = async (req, status) => {
  if (status === 'REJECTED') {
    const ok = await ui.confirm({
      title: 'Từ chối điều chỉnh chấm công?',
      message: `Từ chối yêu cầu của ${req.employeeFullName}.`,
      danger: true,
      confirmLabel: 'Từ chối',
    })
    if (!ok) return
  }
  reviewLoading.value = req.adjustmentRequestId
  try {
    await reviewAttendanceAdjustment(req.adjustmentRequestId, { status })
    toast.success(status === 'APPROVED' ? 'Đã chấp thuận điều chỉnh' : 'Đã từ chối điều chỉnh')
    adjustments.value = adjustments.value.filter(r => r.adjustmentRequestId !== req.adjustmentRequestId)
  } catch (e) {
    toast.error('Xử lý thất bại')
  } finally {
    reviewLoading.value = null
  }
}

const handleOT = async (req, status) => {
  if (status === 'REJECTED') {
    const ok = await ui.confirm({
      title: 'Từ chối tăng ca?',
      message: `Từ chối yêu cầu OT của ${req.employeeFullName}.`,
      danger: true,
      confirmLabel: 'Từ chối',
    })
    if (!ok) return
  }
  reviewLoading.value = req.overtimeRequestId
  try {
    await reviewOvertimeRequest(req.overtimeRequestId, { status })
    toast.success(status === 'APPROVED' ? 'Đã phê duyệt tăng ca' : 'Đã từ chối tăng ca')
    overtimes.value = overtimes.value.filter(r => r.overtimeRequestId !== req.overtimeRequestId)
  } catch (e) {
    toast.error('Xử lý thất bại')
  } finally {
    reviewLoading.value = null
  }
}

function fmtDate(d) {
  return d ? new Date(d).toLocaleDateString('vi-VN') : '—'
}
function fmtTime(t) {
  if (!t) return '—'
  return t.length > 5 ? new Date(t).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : t
}
</script>

<template>
  
    <div class="space-y-8 animate-fade-in">

      <!-- HEADER -->
      <PageHeader
        title="Duyệt Chấm công & Tăng ca"
        subtitle="Phê duyệt điều chỉnh giờ công và yêu cầu OT của đội nhóm"
        :icon="Clock"
        iconColor="bg-indigo-600"
        iconShadow="shadow-indigo-100"
      />

      <!-- CONTENT CARD -->
      <div class="bg-white border border-slate-100 rounded-[32px] overflow-hidden shadow-sm">

        <!-- Tabs -->
        <div class="px-8 border-b border-slate-50 flex gap-2">
          <button
            v-for="tab in [
              { key:'adjustment', label:'Điều chỉnh chấm công', icon: Clock, count: adjustments.length },
              { key:'overtime', label:'Yêu cầu tăng ca (OT)', icon: TrendingUp, count: overtimes.length },
            ]"
            :key="tab.key"
            @click="activeTab = tab.key"
            class="px-6 py-5 text-sm font-black transition-all relative flex items-center gap-2"
            :class="activeTab === tab.key ? 'text-indigo-600' : 'text-slate-400 hover:text-slate-600'"
          >
            <component :is="tab.icon" class="w-4 h-4" />
            {{ tab.label }}
            <span v-if="tab.count > 0"
              class="w-5 h-5 rounded-full text-[10px] font-black flex items-center justify-center"
              :class="activeTab === tab.key ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-500'">
              {{ tab.count }}
            </span>
            <div v-if="activeTab === tab.key"
              class="absolute bottom-0 left-0 right-0 h-1 bg-indigo-500 rounded-t-full" />
          </button>
        </div>

        <!-- Content -->
        <div class="relative min-h-[400px]">
          <div v-if="loading" class="absolute inset-0 z-10 flex items-center justify-center bg-white/60">
            <Loader2 class="w-10 h-10 text-indigo-600 animate-spin" />
          </div>

          <!-- ADJUSTMENT REQUESTS -->
          <div v-if="activeTab === 'adjustment'">
            <div v-if="adjustments.length > 0" class="divide-y divide-slate-50">
              <div
                v-for="req in adjustments" :key="req.adjustmentRequestId"
                class="group px-8 py-6 hover:bg-slate-50/60 transition-all flex flex-col xl:flex-row xl:items-center justify-between gap-6"
              >
                <div class="flex items-start gap-4 min-w-[250px]">
                  <AvatarBox :name="req.employeeFullName" size="lg" shape="rounded-[20px]" shadow />
                  <div>
                    <h4 class="font-black text-slate-900 mb-1">{{ req.employeeFullName }}</h4>
                    <div class="flex items-center gap-1.5 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                      Ngày <span class="text-indigo-600 font-black">{{ fmtDate(req.attendanceDate) }}</span>
                    </div>
                  </div>
                </div>

                <div class="flex-1 grid grid-cols-3 gap-6">
                  <div>
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Giờ đúng</p>
                    <p class="text-sm font-bold text-slate-700">{{ fmtTime(req.adjustedCheckIn) }}</p>
                  </div>
                  <div>
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Giờ ra</p>
                    <p class="text-sm font-bold text-slate-700">{{ fmtTime(req.adjustedCheckOut) }}</p>
                  </div>
                  <div>
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Lý do</p>
                    <p class="text-xs font-medium text-slate-600 italic line-clamp-2">{{ req.reason || '—' }}</p>
                  </div>
                </div>

                <div class="flex items-center gap-2 justify-end">
                  <div v-if="reviewLoading === req.adjustmentRequestId">
                    <Loader2 class="w-6 h-6 text-slate-400 animate-spin" />
                  </div>
                  <template v-else>
                    <button @click="handleAdjustment(req, 'APPROVED')"
                      class="w-11 h-11 rounded-2xl bg-emerald-50 text-emerald-600 hover:bg-emerald-500 hover:text-white transition-all flex items-center justify-center shadow-sm hover:shadow-emerald-200">
                      <Check class="w-5 h-5" />
                    </button>
                    <button @click="handleAdjustment(req, 'REJECTED')"
                      class="w-11 h-11 rounded-2xl bg-rose-50 text-rose-500 hover:bg-rose-500 hover:text-white transition-all flex items-center justify-center shadow-sm hover:shadow-rose-200">
                      <X class="w-5 h-5" />
                    </button>
                  </template>
                </div>
              </div>
            </div>
            <EmptyState v-else-if="!loading"
              title="Không có yêu cầu điều chỉnh"
              description="Không có đơn điều chỉnh chấm công nào đang chờ xử lý."
              iconName="Clock"
            />
          </div>

          <!-- OVERTIME REQUESTS -->
          <div v-if="activeTab === 'overtime'">
            <div v-if="overtimes.length > 0" class="divide-y divide-slate-50">
              <div
                v-for="req in overtimes" :key="req.overtimeRequestId"
                class="group px-8 py-6 hover:bg-slate-50/60 transition-all flex flex-col xl:flex-row xl:items-center justify-between gap-6"
              >
                <div class="flex items-start gap-4 min-w-[250px]">
                  <AvatarBox :name="req.employeeFullName" size="lg" shape="rounded-[20px]" shadow />
                  <div>
                    <h4 class="font-black text-slate-900 mb-1">{{ req.employeeFullName }}</h4>
                    <div class="flex items-center gap-1 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                      <Calendar class="w-3 h-3" /> {{ fmtDate(req.date) }}
                    </div>
                  </div>
                </div>

                <div class="flex-1 grid grid-cols-3 gap-6">
                  <div>
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Bắt đầu</p>
                    <p class="text-sm font-bold text-violet-700">{{ req.startTime || '—' }}</p>
                  </div>
                  <div>
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Kết thúc</p>
                    <p class="text-sm font-bold text-violet-700">{{ req.endTime || '—' }}</p>
                  </div>
                  <div>
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Lý do OT</p>
                    <p class="text-xs font-medium text-slate-600 italic line-clamp-2">{{ req.reason || '—' }}</p>
                  </div>
                </div>

                <div class="flex items-center gap-2 justify-end">
                  <div v-if="reviewLoading === req.overtimeRequestId">
                    <Loader2 class="w-6 h-6 text-slate-400 animate-spin" />
                  </div>
                  <template v-else>
                    <button @click="handleOT(req, 'APPROVED')"
                      class="w-11 h-11 rounded-2xl bg-emerald-50 text-emerald-600 hover:bg-emerald-500 hover:text-white transition-all flex items-center justify-center shadow-sm hover:shadow-emerald-200">
                      <Check class="w-5 h-5" />
                    </button>
                    <button @click="handleOT(req, 'REJECTED')"
                      class="w-11 h-11 rounded-2xl bg-rose-50 text-rose-500 hover:bg-rose-500 hover:text-white transition-all flex items-center justify-center shadow-sm hover:shadow-rose-200">
                      <X class="w-5 h-5" />
                    </button>
                  </template>
                </div>
              </div>
            </div>
            <EmptyState v-else-if="!loading"
              title="Không có yêu cầu OT"
              description="Không có yêu cầu tăng ca nào đang chờ phê duyệt."
              iconName="TrendingUp"
            />
          </div>
        </div>
      </div>

    </div>
  
</template>

<style scoped>
.animate-fade-in { animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
</style>
