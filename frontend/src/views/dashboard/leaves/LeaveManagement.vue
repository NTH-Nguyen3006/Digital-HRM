<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'
import {
  Plus, Check, X, Search, Filter, Download, RefreshCw,
  Calendar, Clock, Users, TrendingDown, ChevronDown,
  FileText, AlertCircle, CheckCircle2, XCircle, Timer,
  SlidersHorizontal, Eye, MoreHorizontal, Loader2, CalendarDays
} from 'lucide-vue-next'
import {
  getLeaveRequests,
  finalizeLeaveRequest,
  getLeaveBalances,
  exportLeaveReports
} from '@/api/admin/leave.js'

/* ------------------ STATE ------------------ */

const activeTab = ref('requests')
const searchQuery = ref('')
const statusFilter = ref('ALL')
const loading = ref(false)

const requestsList = ref([])
const balancesList = ref([])

const statsData = ref([
  { title: 'Chờ duyệt', value: '0', icon: Timer, color: 'amber', trend: 0 },
  { title: 'Đã duyệt tháng này', value: '0', icon: CheckCircle2, color: 'emerald', trend: 0 },
  { title: 'Từ chối', value: '0', icon: XCircle, color: 'rose', trend: 0 },
  { title: 'Hôm nay vắng mặt', value: '0', icon: Users, color: 'indigo' },
])

/* ------------------ API CALLS ------------------ */

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchQuery.value,
      status: statusFilter.value !== 'ALL' ? statusFilter.value : undefined
    }
    
    if (activeTab.value === 'requests') {
      const response = await getLeaveRequests(params)
      requestsList.value = response.data || []
      
      // Update statistics based on requests
      const pendingCount = requestsList.value.filter(r => r.status === 'PENDING').length
      const approvedCount = requestsList.value.filter(r => r.status === 'APPROVED').length
      const rejectedCount = requestsList.value.filter(r => r.status === 'REJECTED').length
      
      statsData.value[0].value = pendingCount.toString()
      statsData.value[1].value = approvedCount.toString()
      statsData.value[2].value = rejectedCount.toString()
      // Note: 'Hôm nay vắng mặt' would ideally come from attendance or a specific query, 
      // here we just use the total approved for demo consistency if needed.
      statsData.value[3].value = requestsList.value.filter(r => r.status === 'APPROVED').length.toString()
      
    } else {
      const response = await getLeaveBalances({ keyword: searchQuery.value })
      balancesList.value = response.data || []
    }
  } catch (error) {
    console.error('Failed to fetch leave data:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

watch([activeTab, searchQuery, statusFilter], () => {
  fetchData()
})

/* ------------------ ACTIONS ------------------ */

const handleFinalize = async (req, status) => {
  try {
    await finalizeLeaveRequest(req.leaveRequestId, { 
      finalStatus: status, 
      hrNote: status === 'APPROVED' ? 'Đã phê duyệt hồ sơ.' : 'Không được phê duyệt.' 
    })
    await fetchData()
  } catch (error) {
    console.error(`Failed to ${status} request:`, error)
  }
}

const handleExport = async () => {
  try {
    const data = await exportLeaveReports({ status: statusFilter.value })
    const url = window.URL.createObjectURL(new Blob([data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `bao-cao-nghi-phep-${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    console.error('Export failed:', error)
  }
}

/* ------------------ UI HELPERS ------------------ */

const tabs = [
  { key: 'requests', label: 'Yêu cầu nghỉ phép', icon: FileText },
  { key: 'balances', label: 'Quỹ phép nhân viên', icon: CalendarDays },
]

function getBalanceWidth(used, total) {
  if (!total || total === 0) return 0
  return Math.min(100, Math.round((used / total) * 100))
}

function getBalanceColor(remaining, total) {
  if (!total) return 'bg-slate-200'
  const pct = remaining / total
  if (pct > 0.5) return 'bg-emerald-500'
  if (pct > 0.2) return 'bg-amber-500'
  return 'bg-rose-500'
}

</script>

<template>
  
    <div class="space-y-8 animate-fade-in">

      <!-- HEADER -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <div class="flex items-center gap-4 mb-2">
            <div class="w-12 h-12 rounded-[18px] bg-indigo-600 flex items-center justify-center shadow-xl shadow-indigo-100">
              <Calendar class="w-6 h-6 text-white" />
            </div>
            <h2 class="text-4xl font-black text-slate-900 tracking-tight">Quản lý nghỉ phép</h2>
          </div>
          <p class="text-slate-500 font-medium ml-1">Theo dõi và phê duyệt nguyện vọng nghỉ phép của nhân sự</p>
        </div>

        <div class="flex items-center gap-3">
          <BaseButton variant="outline" @click="handleExport" class="rounded-2xl! font-bold">
            <Download class="w-4 h-4 mr-2" /> Xuất báo cáo
          </BaseButton>
          <BaseButton variant="primary" shadow class="rounded-2xl! font-bold">
            <Plus class="w-4 h-4 mr-2" /> Tạo đơn nghỉ
          </BaseButton>
        </div>
      </div>

      <!-- STATS -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard v-for="s in statsData" :key="s.title" 
          :title="s.title" :value="s.value" :icon="s.icon" :color="s.color" :trend="s.trend" 
          class="rounded-[32px]!"
        />
      </div>

      <!-- MAIN CONTENT -->
      <div class="bg-white border border-slate-100 rounded-[32px] overflow-hidden shadow-sm relative min-h-[500px]">
        
        <!-- Loading Overlay -->
        <div v-if="loading" class="absolute inset-0 z-20 flex items-center justify-center bg-white/60 backdrop-blur-[1px]">
          <Loader2 class="w-10 h-10 text-indigo-600 animate-spin" />
        </div>

        <!-- Toolbar & Tabs -->
        <div class="px-8 pt-8 border-b border-slate-50 flex flex-col lg:flex-row lg:items-end justify-between gap-6">
          <div class="flex gap-2">
             <button 
              v-for="tab in tabs" 
              :key="tab.key"
              @click="activeTab = tab.key"
              class="px-6 py-4 text-sm font-black transition-all relative"
              :class="activeTab === tab.key ? 'text-indigo-600' : 'text-slate-400 hover:text-slate-600'"
            >
              <div class="flex items-center gap-2">
                <component :is="tab.icon" class="w-4 h-4" />
                {{ tab.label }}
              </div>
              <div v-if="activeTab === tab.key" class="absolute bottom-0 left-0 right-0 h-1 bg-indigo-600 rounded-t-full shadow-[0_-4px_10px_rgba(79,70,229,0.3)]"></div>
            </button>
          </div>

          <div class="flex items-center gap-4 pb-4">
             <div v-if="activeTab === 'requests'" class="flex items-center gap-2 p-1 bg-slate-50 rounded-2xl">
                <button 
                  v-for="st in ['ALL', 'PENDING', 'APPROVED', 'REJECTED']"
                  :key="st"
                  @click="statusFilter = st"
                  class="px-4 py-2 rounded-xl text-[10px] font-black uppercase tracking-wider transition-all"
                  :class="statusFilter === st ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-400 hover:text-slate-600'"
                >
                  {{ st === 'ALL' ? 'Tất cả' : st }}
                </button>
             </div>

             <div class="relative group">
                <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                <input 
                  v-model="searchQuery" 
                  type="text" 
                  placeholder="Tìm nhân viên..."
                  class="w-64 pl-11 pr-4 py-3 bg-slate-50 border border-transparent rounded-2xl text-xs font-bold focus:bg-white focus:border-indigo-100 focus:ring-4 focus:ring-indigo-500/5 transition-all outline-none" 
                />
              </div>
          </div>
        </div>

        <!-- Content Area -->
        <div class="min-h-[400px]">
          
          <!-- TAB: YÊU CẦU NGHỈ PHÉP -->
          <div v-if="activeTab === 'requests'" class="divide-y divide-slate-50">
            <template v-if="requestsList.length > 0">
              <div 
                v-for="req in requestsList" 
                :key="req.leaveRequestId"
                class="group px-8 py-6 hover:bg-slate-50/50 transition-all cursor-pointer flex flex-col xl:flex-row xl:items-center justify-between gap-6"
              >
                <div class="flex items-start gap-5 min-w-[350px]">
                  <AvatarBox :name="req.employeeFullName" :image="req.avatarUrl" size="lg" shape="rounded-[20px]" shadow />
                  <div>
                    <h4 class="text-lg font-black text-slate-900 mb-1">{{ req.employeeFullName }}</h4>
                    <div class="flex items-center gap-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                      <span>{{ req.orgUnitName }}</span>
                      <div class="w-1 h-1 rounded-full bg-slate-300"></div>
                      <span class="text-indigo-600">{{ req.leaveTypeName }}</span>
                    </div>
                    <p class="text-xs text-slate-500 font-medium mt-2 line-clamp-1 italic">"{{ req.reason || 'Không có lý do chi tiết' }}"</p>
                  </div>
                </div>

                <div class="flex-1 grid grid-cols-2 lg:grid-cols-4 gap-6 items-center">
                  <div class="text-center xl:text-left">
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Thời gian</p>
                    <p class="text-xs font-bold text-slate-700">{{ req.fromDate }} → {{ req.toDate }}</p>
                  </div>
                  <div class="text-center xl:text-left">
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Số lượng</p>
                    <p class="text-xs font-black text-indigo-600">{{ req.totalDays }} ngày</p>
                  </div>
                  <div class="text-center xl:text-left">
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Ngày gửi</p>
                    <p class="text-xs font-medium text-slate-500">{{ req.createdAt }}</p>
                  </div>
                  <div class="flex justify-center xl:justify-end">
                    <StatusBadge :status="req.status" />
                  </div>
                </div>

                <div class="flex items-center gap-2 justify-end min-w-[180px]">
                   <template v-if="req.status === 'PENDING'">
                      <button @click="handleFinalize(req, 'APPROVED')" 
                        class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 hover:bg-emerald-600 hover:text-white transition-all flex items-center justify-center shadow-sm">
                        <Check class="w-5 h-5" />
                      </button>
                      <button @click="handleFinalize(req, 'REJECTED')" 
                        class="w-10 h-10 rounded-xl bg-rose-50 text-rose-600 hover:bg-rose-600 hover:text-white transition-all flex items-center justify-center shadow-sm">
                        <X class="w-5 h-5" />
                      </button>
                   </template>
                   <button class="p-2 text-slate-200 hover:text-indigo-600 bg-slate-50 hover:bg-indigo-50 rounded-xl transition-all">
                      <ChevronRight class="w-5 h-5" />
                   </button>
                </div>
              </div>
            </template>
            <EmptyState v-else-if="!loading" title="Không có đơn xin nghỉ" description="Hiện tại không có đơn xin nghỉ nào cần bạn xử lý." iconName="CalendarX" />
          </div>

          <!-- TAB: QUỸ PHÉP NHÂN VIÊN -->
          <div v-if="activeTab === 'balances'" class="divide-y divide-slate-50">
            <template v-if="balancesList.length > 0">
               <div 
                v-for="bal in balancesList" 
                :key="bal.leaveBalanceId"
                class="group px-8 py-6 hover:bg-slate-50/50 transition-all flex flex-col lg:flex-row lg:items-center gap-8"
              >
                <div class="flex items-center gap-4 w-64 shrink-0">
                   <AvatarBox :name="bal.employeeFullName" size="md" shape="rounded-xl" />
                   <div>
                      <h4 class="text-sm font-bold text-slate-900 line-clamp-1">{{ bal.employeeFullName }}</h4>
                      <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest">{{ bal.employeeCode }}</p>
                   </div>
                </div>

                <div class="flex-1">
                   <div class="flex justify-between items-end mb-2">
                      <p class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Tiến độ sử dụng phép năm</p>
                      <p class="text-xs font-black text-slate-800">{{ bal.usedDays }} / {{ bal.totalEntitledDays }} <span class="text-slate-300 ml-1">ngày</span></p>
                   </div>
                   <div class="h-2.5 bg-slate-100 rounded-full overflow-hidden shadow-inner">
                      <div 
                        class="h-full rounded-full transition-all duration-1000"
                        :class="getBalanceColor(bal.remainingDays, bal.totalEntitledDays)"
                        :style="{ width: getBalanceWidth(bal.usedDays, bal.totalEntitledDays) + '%' }"
                      ></div>
                   </div>
                </div>

                <div class="flex items-center gap-4 min-w-[200px] justify-between lg:justify-end">
                   <div class="text-center px-4 py-2 bg-amber-50 rounded-2xl border border-amber-100/50">
                      <p class="text-[9px] font-black text-amber-500 uppercase tracking-tighter mb-0.5">Đang chờ</p>
                      <p class="text-sm font-black text-amber-700">{{ bal.pendingDays }}</p>
                   </div>
                   <div class="text-center px-4 py-2 bg-emerald-50 rounded-2xl border border-emerald-100/50">
                      <p class="text-[9px] font-black text-emerald-500 uppercase tracking-tighter mb-0.5">Còn lại</p>
                      <p class="text-sm font-black text-emerald-700">{{ bal.remainingDays }}</p>
                   </div>
                   <button class="p-2 text-slate-100 group-hover:text-indigo-600 transition-colors">
                      <MoreHorizontal class="w-5 h-5" />
                   </button>
                </div>
               </div>
            </template>
            <EmptyState v-else-if="!loading" title="Không có dữ liệu quỹ phép" description="Chưa có dữ liệu quỹ phép năm nào được ghi nhận cho hệ thống." iconName="Calendar" />
          </div>

        </div>

        <!-- Footer Pagination -->
        <div class="border-t border-slate-50 px-8 py-5 flex items-center justify-between text-xs font-bold text-slate-400">
           <span>Hiển thị {{ activeTab === 'requests' ? requestsList.length : balancesList.length }} bản ghi</span>
           <div class="flex gap-2">
              <button class="w-8 h-8 rounded-lg hover:bg-slate-100 flex items-center justify-center transition-colors">←</button>
              <button class="w-8 h-8 rounded-lg bg-indigo-600 text-white flex items-center justify-center shadow-lg shadow-indigo-100">1</button>
              <button class="w-8 h-8 rounded-lg hover:bg-slate-100 flex items-center justify-center transition-colors">2</button>
              <button class="w-8 h-8 rounded-lg hover:bg-slate-100 flex items-center justify-center transition-colors">→</button>
           </div>
        </div>
      </div>
    </div>
  
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
