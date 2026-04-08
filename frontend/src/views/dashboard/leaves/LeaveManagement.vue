<script setup>
import { ref, computed, onMounted } from 'vue'
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'
import {
  Plus, Check, X, Search, Filter, Download, RefreshCw,
  Calendar, Clock, Users, TrendingDown, ChevronDown,
  FileText, AlertCircle, CheckCircle2, XCircle, Timer,
  SlidersHorizontal, Eye, MoreHorizontal
} from 'lucide-vue-next'
import {
  getLeaveRequests,
  finalizeLeaveRequest,
  getLeaveBalances,
  exportLeaveReports
} from '@/api/admin/leave.js'

// =================== STATE ===================
const activeTab = ref('requests')
const searchQuery = ref('')
const statusFilter = ref('ALL')
const isLoading = ref(false)
const showFilterPanel = ref(false)

// Mock data tổng hợp (will be replaced by API)
const stats = [
  { title: 'Chờ duyệt', value: '12', icon: Timer, color: 'amber', trend: 3, trendLabel: 'so với tuần trước' },
  { title: 'Đã duyệt tháng này', value: '47', icon: CheckCircle2, color: 'emerald', trend: 8, trendLabel: 'so với tháng trước' },
  { title: 'Từ chối', value: '5', icon: XCircle, color: 'rose', trend: -2, trendLabel: 'so với tháng trước' },
  { title: 'Tổng nhân sự nghỉ hôm nay', value: '8', icon: Users, color: 'indigo' },
]

const requests = ref([
  { id: 1, employee: 'Phạm Văn Khoa', avatar: 'PK', dept: 'Engineering', type: 'Nghỉ ốm (SL)', days: 2, from: '25/03/2026', to: '26/03/2026', reason: 'Sốt xuất huyết, có giấy nghỉ bệnh viện', status: 'PENDING', submittedAt: '24/03/2026 08:30' },
  { id: 2, employee: 'Lê Thị Hương', avatar: 'LH', dept: 'Marketing', type: 'Nghỉ phép năm (AL)', days: 3, from: '01/04/2026', to: '03/04/2026', reason: 'Đám cưới anh họ ở Đà Nẵng', status: 'APPROVED', submittedAt: '20/03/2026 14:00' },
  { id: 3, employee: 'Trần Minh Hoàng', avatar: 'TH', dept: 'Sales', type: 'Nghỉ không lương (UP)', days: 5, from: '10/04/2026', to: '14/04/2026', reason: 'Đi du lịch nước ngoài cùng gia đình', status: 'REJECTED', submittedAt: '15/03/2026 09:15' },
  { id: 4, employee: 'Nguyễn Bảo Châu', avatar: 'NC', dept: 'Finance', type: 'Nghỉ thai sản (ML)', days: 180, from: '01/05/2026', to: '27/10/2026', reason: 'Thai sản theo quy định pháp luật', status: 'PENDING', submittedAt: '25/03/2026 11:45' },
  { id: 5, employee: 'Võ Thanh Tuấn', avatar: 'VT', dept: 'HR', type: 'Nghỉ phép năm (AL)', days: 1, from: '07/04/2026', to: '07/04/2026', reason: 'Khám sức khỏe định kỳ', status: 'APPROVED', submittedAt: '05/04/2026 16:00' },
])

const balances = ref([
  { id: 1, employee: 'Phạm Văn Khoa', dept: 'Engineering', total: 12, used: 3, pending: 2, remaining: 7 },
  { id: 2, employee: 'Lê Thị Hương', dept: 'Marketing', total: 12, used: 7, pending: 0, remaining: 5 },
  { id: 3, employee: 'Trần Minh Hoàng', dept: 'Sales', total: 14, used: 10, pending: 1, remaining: 3 },
  { id: 4, employee: 'Nguyễn Bảo Châu', dept: 'Finance', total: 12, used: 2, pending: 0, remaining: 10 },
  { id: 5, employee: 'Võ Thanh Tuấn', dept: 'HR', total: 12, used: 4, pending: 1, remaining: 7 },
])

// =================== COMPUTED ===================
const tabs = [
  { key: 'requests', label: 'Đơn xin nghỉ', icon: FileText },
  { key: 'balances', label: 'Quỹ phép nhân viên', icon: Calendar },
]

const statusOptions = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'REJECTED', label: 'Từ chối' },
]

const filteredRequests = computed(() => {
  return requests.value.filter(r => {
    const matchSearch = !searchQuery.value ||
      r.employee.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      r.dept.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      r.type.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchStatus = statusFilter.value === 'ALL' || r.status === statusFilter.value
    return matchSearch && matchStatus
  })
})

const statusConfig = {
  PENDING: { label: 'Chờ duyệt', class: 'bg-amber-100 text-amber-700 border border-amber-200', dot: 'bg-amber-400' },
  APPROVED: { label: 'Đã duyệt', class: 'bg-emerald-100 text-emerald-700 border border-emerald-200', dot: 'bg-emerald-400' },
  REJECTED: { label: 'Từ chối', class: 'bg-rose-100 text-rose-700 border border-rose-200', dot: 'bg-rose-400' },
}

const avatarColors = ['bg-indigo-500', 'bg-emerald-500', 'bg-amber-500', 'bg-rose-500', 'bg-violet-500', 'bg-sky-500']

// =================== METHODS ===================
function getAvatarColor(name) {
  const idx = name.charCodeAt(0) % avatarColors.length
  return avatarColors[idx]
}

async function approveRequest(req) {
  try {
    await finalizeLeaveRequest(req.id, { finalStatus: 'APPROVED', hrNote: 'Được duyệt.' })
    req.status = 'APPROVED'
  } catch (e) {
    req.status = 'APPROVED' // fallback for demo
  }
}

async function rejectRequest(req) {
  try {
    await finalizeLeaveRequest(req.id, { finalStatus: 'REJECTED', hrNote: 'Không đủ điều kiện.' })
    req.status = 'REJECTED'
  } catch (e) {
    req.status = 'REJECTED' // fallback for demo
  }
}

async function handleExport() {
  try {
    await exportLeaveReports({ status: statusFilter.value })
  } catch (_) { }
}

function getBalanceWidth(used, total) {
  return Math.min(100, Math.round((used / total) * 100))
}
function getBalanceColor(remaining, total) {
  const pct = remaining / total
  if (pct > 0.5) return 'bg-emerald-400'
  if (pct > 0.25) return 'bg-amber-400'
  return 'bg-rose-400'
}
</script>

<template>
  <MainLayout>
    <div class="space-y-8">

      <!-- ===== HEADER ===== -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div class="flex items-center gap-3 mb-1">
            <div
              class="w-10 h-10 rounded-2xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-200">
              <Calendar class="w-5 h-5 text-white" />
            </div>
            <h2 class="text-3xl font-black text-slate-900 tracking-tight">Quản lý nghỉ phép</h2>
          </div>
          <p class="text-slate-500 font-medium ml-13 pl-[52px]">Duyệt đơn xin nghỉ và theo dõi quỹ phép năm toàn công ty
          </p>
        </div>

        <div class="flex items-center gap-3">
          <button @click="handleExport"
            class="flex items-center gap-2 px-4 py-2.5 rounded-xl font-semibold text-slate-600 bg-white border border-slate-200 hover:border-slate-300 hover:bg-slate-50 transition-all shadow-sm text-sm">
            <Download class="w-4 h-4" />
            Xuất báo cáo
          </button>
          <button
            class="flex items-center gap-2 bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 text-sm">
            <Plus class="w-4 h-4" />
            Tạo đơn nghỉ
          </button>
        </div>
      </div>

      <!-- ===== STAT CARDS ===== -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard v-for="stat in stats" :key="stat.title" :title="stat.title" :value="stat.value" :icon="stat.icon"
          :color="stat.color" :trend="stat.trend" :trendLabel="stat.trendLabel" />
      </div>

      <!-- ===== MAIN CONTENT ===== -->
      <GlassCard :glass="false" padding="p-0"
        class="rounded-3xl border border-slate-100 shadow-sm overflow-hidden bg-white">

        <!-- Tabs + Toolbar -->
        <div
          class="border-b border-slate-100 px-6 pt-4 pb-0 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <!-- Tabs -->
          <div class="flex gap-1">
            <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
              class="flex items-center gap-2 px-4 py-2.5 text-sm font-semibold rounded-t-xl transition-all relative"
              :class="activeTab === tab.key
                ? 'text-indigo-600 bg-indigo-50'
                : 'text-slate-500 hover:text-slate-700 hover:bg-slate-50'">
              <component :is="tab.icon" class="w-4 h-4" />
              {{ tab.label }}
              <span v-if="activeTab === tab.key"
                class="absolute bottom-0 left-0 right-0 h-0.5 bg-indigo-600 rounded-t" />
            </button>
          </div>

          <!-- Search & Filter -->
          <div class="flex items-center gap-2 pb-3">
            <div class="relative">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input v-model="searchQuery" type="text" placeholder="Tìm nhân viên, phòng ban..."
                class="pl-9 pr-4 py-2 text-sm rounded-xl border border-slate-200 bg-slate-50 focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-indigo-400 w-56 transition-all" />
            </div>
            <select v-model="statusFilter"
              class="py-2 px-3 text-sm rounded-xl border border-slate-200 bg-slate-50 focus:outline-none focus:ring-2 focus:ring-indigo-300 text-slate-700 font-medium appearance-none cursor-pointer">
              <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>

        <!-- ===== TAB: LEAVE REQUESTS ===== -->
        <div v-if="activeTab === 'requests'" class="divide-y divide-slate-50">
          <TransitionGroup name="list">
            <div v-for="req in filteredRequests" :key="req.id" class="group p-5 hover:bg-slate-50/70 transition-colors">
              <div class="flex flex-col md:flex-row md:items-center gap-4">

                <!-- Avatar + Info -->
                <div class="flex items-start gap-4 flex-1 min-w-0">
                  <div
                    class="flex-shrink-0 w-11 h-11 rounded-2xl flex items-center justify-center text-white text-sm font-black shadow-md"
                    :class="getAvatarColor(req.employee)">
                    {{ req.avatar }}
                  </div>

                  <div class="flex-1 min-w-0">
                    <div class="flex flex-wrap items-center gap-2 mb-2">
                      <h3 class="font-bold text-slate-900 text-base">{{ req.employee }}</h3>
                      <span class="text-xs text-slate-400 font-medium bg-slate-100 px-2 py-0.5 rounded-lg">{{ req.dept
                        }}</span>
                      <span class="flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold"
                        :class="statusConfig[req.status]?.class">
                        <span class="w-1.5 h-1.5 rounded-full" :class="statusConfig[req.status]?.dot"></span>
                        {{ statusConfig[req.status]?.label }}
                      </span>
                    </div>

                    <!-- Detail grid -->
                    <div class="grid grid-cols-2 sm:grid-cols-4 gap-x-6 gap-y-1.5 text-sm">
                      <div>
                        <div class="text-xs text-slate-400 font-medium mb-0.5">Loại phép</div>
                        <div class="font-semibold text-slate-700">{{ req.type }}</div>
                      </div>
                      <div>
                        <div class="text-xs text-slate-400 font-medium mb-0.5">Số ngày</div>
                        <div class="font-bold text-indigo-600">{{ req.days }} ngày</div>
                      </div>
                      <div>
                        <div class="text-xs text-slate-400 font-medium mb-0.5">Khoảng thời gian</div>
                        <div class="font-semibold text-slate-700">{{ req.from }} → {{ req.to }}</div>
                      </div>
                      <div>
                        <div class="text-xs text-slate-400 font-medium mb-0.5">Ngày gửi</div>
                        <div class="font-medium text-slate-500">{{ req.submittedAt }}</div>
                      </div>
                    </div>

                    <!-- Reason -->
                    <div class="mt-2 flex items-start gap-1.5">
                      <AlertCircle class="w-3.5 h-3.5 text-slate-400 mt-0.5 flex-shrink-0" />
                      <p class="text-xs text-slate-500 italic">{{ req.reason }}</p>
                    </div>
                  </div>
                </div>

                <!-- Actions -->
                <div class="flex items-center gap-2 md:flex-shrink-0">
                  <template v-if="req.status === 'PENDING'">
                    <button @click="approveRequest(req)"
                      class="flex items-center gap-1.5 px-4 py-2 bg-emerald-50 text-emerald-700 hover:bg-emerald-500 hover:text-white rounded-xl font-bold text-sm transition-all border border-emerald-200 hover:border-emerald-500 shadow-sm">
                      <Check class="w-4 h-4" /> Duyệt
                    </button>
                    <button @click="rejectRequest(req)"
                      class="flex items-center gap-1.5 px-4 py-2 bg-rose-50 text-rose-700 hover:bg-rose-500 hover:text-white rounded-xl font-bold text-sm transition-all border border-rose-200 hover:border-rose-500 shadow-sm">
                      <X class="w-4 h-4" /> Từ chối
                    </button>
                  </template>
                  <template v-else>
                    <button
                      class="flex items-center gap-1.5 px-3 py-2 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-xl text-sm font-semibold transition-all">
                      <Eye class="w-4 h-4" /> Chi tiết
                    </button>
                  </template>
                </div>
              </div>
            </div>
          </TransitionGroup>

          <!-- Empty state -->
          <div v-if="filteredRequests.length === 0" class="py-16 text-center">
            <FileText class="w-12 h-12 text-slate-300 mx-auto mb-3" />
            <p class="text-slate-500 font-medium">Không có đơn nào phù hợp</p>
            <p class="text-slate-400 text-sm mt-1">Thử thay đổi bộ lọc hoặc tìm kiếm</p>
          </div>
        </div>

        <!-- ===== TAB: LEAVE BALANCES ===== -->
        <div v-if="activeTab === 'balances'" class="divide-y divide-slate-50">
          <div v-for="bal in balances" :key="bal.id" class="p-5 hover:bg-slate-50/70 transition-colors group">
            <div class="flex flex-col sm:flex-row sm:items-center gap-4">
              <!-- Avatar + Info -->
              <div class="flex items-center gap-3 w-56 flex-shrink-0">
                <div class="w-10 h-10 rounded-xl flex items-center justify-center text-white text-xs font-black shadow"
                  :class="getAvatarColor(bal.employee)">
                  {{bal.employee.split(' ').map(w => w[0]).slice(-2).join('')}}
                </div>
                <div>
                  <div class="font-bold text-slate-900 text-sm">{{ bal.employee }}</div>
                  <div class="text-xs text-slate-400">{{ bal.dept }}</div>
                </div>
              </div>

              <!-- Progress bar -->
              <div class="flex-1">
                <div class="flex justify-between text-xs font-semibold text-slate-500 mb-1.5">
                  <span>Đã dùng: <span class="text-slate-700">{{ bal.used }} ngày</span></span>
                  <span>Tổng: <span class="text-slate-700">{{ bal.total }} ngày</span></span>
                </div>
                <div class="h-2.5 bg-slate-100 rounded-full overflow-hidden">
                  <div class="h-full rounded-full transition-all duration-700"
                    :class="getBalanceColor(bal.remaining, bal.total)"
                    :style="{ width: getBalanceWidth(bal.used, bal.total) + '%' }" />
                </div>
              </div>

              <!-- Stats chips -->
              <div class="flex items-center gap-2 flex-shrink-0">
                <div class="text-center px-3 py-1.5 bg-amber-50 rounded-xl border border-amber-100">
                  <div class="text-xs text-amber-600 font-medium">Chờ duyệt</div>
                  <div class="text-lg font-black text-amber-700">{{ bal.pending }}</div>
                </div>
                <div class="text-center px-3 py-1.5 bg-emerald-50 rounded-xl border border-emerald-100">
                  <div class="text-xs text-emerald-600 font-medium">Còn lại</div>
                  <div class="text-lg font-black text-emerald-700">{{ bal.remaining }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination footer -->
        <div class="border-t border-slate-100 px-6 py-3 flex items-center justify-between text-sm text-slate-500">
          <span>Hiển thị {{ filteredRequests.length }} kết quả</span>
          <div class="flex items-center gap-1">
            <button class="px-3 py-1 rounded-lg hover:bg-slate-50 font-medium transition-colors">←</button>
            <button class="px-3 py-1 rounded-lg bg-indigo-600 text-white font-bold">1</button>
            <button class="px-3 py-1 rounded-lg hover:bg-slate-50 font-medium transition-colors">2</button>
            <button class="px-3 py-1 rounded-lg hover:bg-slate-50 font-medium transition-colors">→</button>
          </div>
        </div>
      </GlassCard>

    </div>
  </MainLayout>
</template>

<style scoped>
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
