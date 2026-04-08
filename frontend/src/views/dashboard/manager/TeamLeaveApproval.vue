<script setup>
import { ref, onMounted, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useToast } from '@/composables/useToast'
import { useUiStore } from '@/stores/ui'
import { useDebounce } from '@/composables/useDebounce'
import {
  getPendingLeaveRequests,
  reviewLeaveRequest,
  getTeamLeaveCalendar
} from '@/api/manager/manager'
import {
  CalendarOff, Search, Check, X, Loader2,
  Calendar, ChevronLeft, ChevronRight, Clock
} from 'lucide-vue-next'

/* ─── COMPOSABLES ────────────────────────────────────────────── */
const toast = useToast()
const ui    = useUiStore()

/* ─── STATE ─────────────────────────────────────────────────── */
const activeTab = ref('pending')
const requests = ref([])
const calendar = ref([])
const loading = ref(false)
const reviewLoading = ref(null)

const searchKeyword = ref('')
const { debouncedValue: debouncedSearch } = useDebounce(searchKeyword, 400)

const calendarMonth = ref(new Date().toISOString().slice(0, 7)) // YYYY-MM

/* ─── API ────────────────────────────────────────────────────── */
const fetchRequests = async () => {
  loading.value = true
  try {
    const res = await getPendingLeaveRequests({ keyword: debouncedSearch.value || undefined })
    requests.value = res.data?.content ?? res.data ?? []
  } catch (e) {
    toast.error('Không thể tải danh sách đơn phép')
  } finally {
    loading.value = false
  }
}

const fetchCalendar = async () => {
  loading.value = true
  try {
    const res = await getTeamLeaveCalendar({ month: calendarMonth.value })
    calendar.value = res.data ?? []
  } catch (e) {
    toast.error('Không thể tải lịch phép đội nhóm')
  } finally {
    loading.value = false
  }
}

onMounted(fetchRequests)

watch(debouncedSearch, fetchRequests)
watch(activeTab, () => {
  if (activeTab.value === 'pending') fetchRequests()
  else fetchCalendar()
})
watch(calendarMonth, () => {
  if (activeTab.value === 'calendar') fetchCalendar()
})

/* ─── ACTIONS ────────────────────────────────────────────────── */
const handleReview = async (req, status) => {
  const isApprove = status === 'APPROVED'

  if (!isApprove) {
    const confirmed = await ui.confirm({
      title: 'Từ chối đơn nghỉ phép?',
      message: `Bạn sẽ từ chối đơn của ${req.employeeFullName}. Hành động này có thể không hoàn tác.`,
      danger: true,
      confirmLabel: 'Từ chối',
    })
    if (!confirmed) return
  }

  reviewLoading.value = req.leaveRequestId
  try {
    await reviewLeaveRequest(req.leaveRequestId, {
      status,
      note: isApprove ? 'Đã xem xét và phê duyệt.' : 'Không được phê duyệt bởi quản lý.',
    })
    toast.success(isApprove ? `Đã phê duyệt đơn của ${req.employeeFullName}` : `Đã từ chối đơn của ${req.employeeFullName}`)
    requests.value = requests.value.filter(r => r.leaveRequestId !== req.leaveRequestId)
  } catch (e) {
    toast.error('Xử lý đơn thất bại. Vui lòng thử lại.')
  } finally {
    reviewLoading.value = null
  }
}

function fmtDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function prevMonth() {
  const [y, m] = calendarMonth.value.split('-').map(Number)
  const d = new Date(y, m - 2, 1)
  calendarMonth.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}
function nextMonth() {
  const [y, m] = calendarMonth.value.split('-').map(Number)
  const d = new Date(y, m, 1)
  calendarMonth.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}
</script>

<template>
  
    <div class="space-y-8 animate-fade-in">

      <!-- HEADER -->
      <PageHeader
        title="Duyệt Nghỉ phép"
        subtitle="Phê duyệt các đơn xin nghỉ của thành viên trong đội nhóm"
        :icon="CalendarOff"
        iconColor="bg-amber-500"
        iconShadow="shadow-amber-100"
      />

      <!-- TABS + SEARCH -->
      <div class="bg-white border border-slate-100 rounded-[32px] overflow-hidden shadow-sm">

        <!-- Toolbar -->
        <div class="px-8 pt-6 border-b border-slate-50 flex flex-col lg:flex-row lg:items-end justify-between gap-4">
          <!-- Tabs -->
          <div class="flex gap-2">
            <button
              v-for="tab in [{ key:'pending', label:'Chờ duyệt', icon: Clock }, { key:'calendar', label:'Lịch phép đội', icon: Calendar }]"
              :key="tab.key"
              @click="activeTab = tab.key"
              class="px-6 py-4 text-sm font-black transition-all relative"
              :class="activeTab === tab.key ? 'text-amber-600' : 'text-slate-400 hover:text-slate-600'"
            >
              <div class="flex items-center gap-1.5">
                <component :is="tab.icon" class="w-4 h-4" />
                {{ tab.label }}
              </div>
              <div v-if="activeTab === tab.key"
                class="absolute bottom-0 left-0 right-0 h-1 bg-amber-500 rounded-t-full" />
            </button>
          </div>

          <!-- Search (only for pending) -->
          <div v-if="activeTab === 'pending'" class="flex items-center gap-3 pb-4">
            <div class="relative group">
              <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 group-focus-within:text-amber-500 transition-colors" />
              <input
                v-model="searchKeyword" type="text" placeholder="Tìm nhân viên..."
                class="w-56 pl-10 pr-4 py-2.5 bg-slate-50 border border-transparent rounded-2xl text-xs font-bold focus:bg-white focus:border-amber-200 focus:ring-4 focus:ring-amber-500/5 outline-none transition-all"
              />
            </div>
          </div>

          <!-- Month navigation (for calendar) -->
          <div v-if="activeTab === 'calendar'" class="flex items-center gap-2 pb-4">
            <button @click="prevMonth" class="w-8 h-8 rounded-xl hover:bg-slate-100 flex items-center justify-center transition-colors">
              <ChevronLeft class="w-4 h-4 text-slate-500" />
            </button>
            <span class="px-4 py-1.5 bg-amber-50 text-amber-700 rounded-xl text-xs font-black">
              {{ new Date(calendarMonth + '-01').toLocaleString('vi-VN', { month: 'long', year: 'numeric' }) }}
            </span>
            <button @click="nextMonth" class="w-8 h-8 rounded-xl hover:bg-slate-100 flex items-center justify-center transition-colors">
              <ChevronRight class="w-4 h-4 text-slate-500" />
            </button>
          </div>
        </div>

        <!-- Content -->
        <div class="relative min-h-[400px]">
          <div v-if="loading" class="absolute inset-0 z-10 flex items-center justify-center bg-white/60 backdrop-blur-[1px]">
            <Loader2 class="w-10 h-10 text-amber-500 animate-spin" />
          </div>

          <!-- PENDING REQUESTS LIST -->
          <div v-if="activeTab === 'pending'">
            <div v-if="requests.length > 0" class="divide-y divide-slate-50">
              <div
                v-for="req in requests"
                :key="req.leaveRequestId"
                class="group px-8 py-6 hover:bg-slate-50/60 transition-all flex flex-col xl:flex-row xl:items-center justify-between gap-6"
              >
                <!-- Employee info -->
                <div class="flex items-start gap-4 min-w-[250px]">
                  <AvatarBox :name="req.employeeFullName" :image="req.avatarUrl" size="lg" shape="rounded-[20px]" shadow />
                  <div>
                    <h4 class="text-base font-black text-slate-900 mb-1 group-hover:text-amber-600 transition-colors">
                      {{ req.employeeFullName }}
                    </h4>
                    <div class="flex items-center gap-1.5 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                      <span>{{ req.orgUnitName }}</span>
                      <span class="w-1 h-1 rounded-full bg-slate-300"></span>
                      <span class="text-amber-600">{{ req.leaveTypeName }}</span>
                    </div>
                    <p class="text-xs text-slate-400 mt-1 italic line-clamp-1">"{{ req.reason || 'Không có lý do' }}"</p>
                  </div>
                </div>

                <!-- Leave info -->
                <div class="grid grid-cols-3 gap-6 flex-1">
                  <div>
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Từ ngày</p>
                    <p class="text-sm font-bold text-slate-700">{{ fmtDate(req.fromDate) }}</p>
                  </div>
                  <div>
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Đến ngày</p>
                    <p class="text-sm font-bold text-slate-700">{{ fmtDate(req.toDate) }}</p>
                  </div>
                  <div>
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Số ngày</p>
                    <p class="text-sm font-black text-amber-600">{{ req.totalDays }} ngày</p>
                  </div>
                </div>

                <!-- Actions -->
                <div class="flex items-center gap-3 justify-end min-w-[120px]">
                  <div v-if="reviewLoading === req.leaveRequestId">
                    <Loader2 class="w-6 h-6 text-slate-400 animate-spin" />
                  </div>
                  <template v-else>
                    <button
                      @click="handleReview(req, 'APPROVED')"
                      title="Phê duyệt"
                      class="w-11 h-11 rounded-2xl bg-emerald-50 text-emerald-600 hover:bg-emerald-500 hover:text-white transition-all flex items-center justify-center shadow-sm hover:shadow-lg hover:shadow-emerald-200"
                    >
                      <Check class="w-5 h-5" />
                    </button>
                    <button
                      @click="handleReview(req, 'REJECTED')"
                      title="Từ chối"
                      class="w-11 h-11 rounded-2xl bg-rose-50 text-rose-500 hover:bg-rose-500 hover:text-white transition-all flex items-center justify-center shadow-sm hover:shadow-lg hover:shadow-rose-200"
                    >
                      <X class="w-5 h-5" />
                    </button>
                  </template>
                </div>
              </div>
            </div>
            <EmptyState v-else-if="!loading"
              title="Không có đơn nào chờ duyệt"
              description="Tất cả đơn xin nghỉ của đội nhóm đã được xử lý."
              iconName="CalendarCheck"
            />
          </div>

          <!-- TEAM CALENDAR VIEW -->
          <div v-if="activeTab === 'calendar'" class="p-8">
            <div v-if="calendar.length > 0" class="space-y-3">
              <div
                v-for="emp in calendar" :key="emp.employeeId"
                class="flex items-center gap-4 bg-slate-50 rounded-2xl p-4"
              >
                <AvatarBox :name="emp.employeeFullName" size="sm" shape="rounded-xl" />
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-bold text-slate-900 truncate mb-2">{{ emp.employeeFullName }}</p>
                  <div class="flex flex-wrap gap-1">
                    <span
                      v-for="leave in emp.leaves" :key="leave.leaveRequestId"
                      class="px-2 py-0.5 rounded-lg text-[10px] font-black bg-amber-100 text-amber-700"
                    >
                      {{ fmtDate(leave.fromDate) }} – {{ fmtDate(leave.toDate) }}
                      <span class="ml-1 opacity-60">({{ leave.totalDays }}ng)</span>
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <EmptyState v-else-if="!loading"
              title="Không có lịch phép nào"
              description="Không có nhân viên nào nghỉ phép trong tháng này."
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
