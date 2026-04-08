<script setup>
import { ref, onMounted, watch } from 'vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { Download, Search, CheckCircle, Clock, Calendar, Filter, Loader2, ChevronLeft, ChevronRight, UserMinus } from 'lucide-vue-next'
import { getDailyAttendance } from '@/api/admin/attendance'

/* ------------------ STATE ------------------ */

const attendanceData = ref([])
const loading = ref(false)
const selectedDate = ref(new Date().toISOString().substr(0, 10))
const searchKeyword = ref('')

const stats = ref({
  present: 0,
  late: 0,
  absent: 0,
  total: 0
})

/* ------------------ API CALL ------------------ */

const fetchAttendance = async () => {
  loading.value = true
  try {
    const response = await getDailyAttendance({ 
      date: selectedDate.value,
      keyword: searchKeyword.value
    })
    // Giả định response.data chứa danh sách và một số thống kê
    // Cấu trúc thực tế có thể khác, tôi sẽ mapping dựa trên phán đoán logic dự án
    attendanceData.value = response.data || []
    
    // Mocking stats update based on data if API doesn't provide it
    stats.value.total = attendanceData.value.length
    stats.value.present = attendanceData.value.filter(a => a.checkInTime).length
    stats.value.late = attendanceData.value.filter(a => a.status === 'LATE' || a.status === 'ĐI MUỘN').length
    stats.value.absent = stats.value.total - stats.value.present
  } catch (error) {
    console.error('Failed to fetch attendance:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchAttendance)

/* ------------------ WATCHERS ------------------ */

watch([selectedDate, searchKeyword], () => {
  fetchAttendance()
})

/* ------------------ HELPERS ------------------ */

const formatTime = (time) => {
  if (!time) return '--:--'
  // Xử lý nếu time là ISO string hoặc HH:mm
  return time.length > 5 ? new Date(time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : time
}

</script>

<template>
  
    <div class="space-y-8 animate-fade-in">
      
      <!-- HEADER & ACTIONS -->
      <div class="flex flex-col xl:flex-row xl:items-end justify-between gap-6">
        <div>
          <h2 class="text-4xl font-black text-slate-900 tracking-tight">Nhật ký Chấm công</h2>
          <p class="text-slate-500 font-medium mt-2 flex items-center gap-2">
            Theo dõi sự hiện diện của nhân sự ngày 
            <span class="px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full font-bold border border-indigo-100">{{ selectedDate }}</span>
          </p>
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <!-- Date Selector -->
          <div class="relative group">
            <Calendar class="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
            <input 
              v-model="selectedDate" 
              type="date" 
              class="pl-11 pr-4 py-3 bg-white border border-slate-200 rounded-2xl text-sm font-bold focus:ring-4 focus:ring-indigo-500/10 focus:border-indigo-500 transition-all shadow-sm outline-none cursor-pointer"
            />
          </div>

          <!-- Search -->
          <div class="relative group">
            <Search class="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
            <input 
              v-model="searchKeyword" 
              type="text" 
              placeholder="Tìm tên nhân viên..." 
              class="w-64 pl-11 pr-4 py-3 bg-white border border-slate-200 rounded-2xl text-sm font-medium focus:ring-4 focus:ring-indigo-500/10 focus:border-indigo-500 transition-all shadow-sm outline-none"
            />
          </div>

          <BaseButton variant="outline" class="rounded-2xl! h-[50px]! px-5! font-bold">
            <Download class="w-5 h-5 mr-2" />
            Xuất báo cáo
          </BaseButton>
        </div>
      </div>

      <!-- STATS OVERVIEW -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div class="bg-white p-6 rounded-[32px] border border-slate-100 shadow-sm flex items-center gap-5 group">
          <div class="w-14 h-14 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-110">
            <CheckCircle class="w-7 h-7" />
          </div>
          <div>
            <div class="text-slate-400 text-xs font-bold uppercase tracking-widest mb-1">Hiện diện</div>
            <div class="text-3xl font-black text-slate-900">{{ stats.present }}<span class="text-slate-300 text-lg font-bold ml-1">/{{ stats.total }}</span></div>
          </div>
        </div>

        <div class="bg-white p-6 rounded-[32px] border border-slate-100 shadow-sm flex items-center gap-5 group">
          <div class="w-14 h-14 bg-amber-50 text-amber-600 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-110">
            <Clock class="w-7 h-7" />
          </div>
          <div>
            <div class="text-slate-400 text-xs font-bold uppercase tracking-widest mb-1">Đi muộn</div>
            <div class="text-3xl font-black text-slate-900">{{ stats.late }}</div>
          </div>
        </div>

        <div class="bg-white p-6 rounded-[32px] border border-slate-100 shadow-sm flex items-center gap-5 group">
          <div class="w-14 h-14 bg-rose-50 text-rose-600 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-110">
            <UserMinus class="w-7 h-7" />
          </div>
          <div>
            <div class="text-slate-400 text-xs font-bold uppercase tracking-widest mb-1">Vắng mặt</div>
            <div class="text-3xl font-black text-slate-900">{{ stats.absent }}</div>
          </div>
        </div>

        <div class="bg-indigo-600 p-6 rounded-[32px] shadow-xl shadow-indigo-100 flex flex-col justify-center">
          <div class="text-indigo-100 text-xs font-bold uppercase tracking-widest mb-1">Tỷ lệ chuyên cần</div>
          <div class="text-3xl font-black text-white">{{ stats.total ? Math.round((stats.present / stats.total) * 100) : 0 }}%</div>
        </div>
      </div>

      <!-- ATTENDANCE LIST (ROWS) -->
      <div class="relative min-h-[400px]">
        <!-- Loading -->
        <div v-if="loading" class="absolute inset-0 z-20 flex items-center justify-center bg-slate-50/50 backdrop-blur-[2px] rounded-[32px]">
          <Loader2 class="w-10 h-10 text-indigo-600 animate-spin" />
        </div>

        <div v-if="attendanceData.length > 0" class="space-y-4">
          <!-- Row Item -->
          <div 
            v-for="att in attendanceData" 
            :key="att.employeeId"
            class="group bg-white rounded-3xl border border-slate-100 p-4 md:px-8 md:py-5 flex flex-col md:flex-row md:items-center justify-between gap-6 transition-all hover:border-indigo-200 hover:shadow-xl hover:shadow-indigo-500/5 hover:-translate-y-1"
          >
            <!-- Employee Info -->
            <div class="flex items-center gap-4 min-w-[280px]">
              <AvatarBox :name="att.employeeFullName" :image="att.avatarUrl" size="lg" shape="rounded-2xl" />
              <div>
                <h4 class="font-bold text-slate-900 text-lg group-hover:text-indigo-600 transition-colors">{{ att.employeeFullName }}</h4>
                <p class="text-xs font-bold text-slate-400 uppercase tracking-widest">{{ att.employeeCode }}</p>
              </div>
            </div>

            <!-- Timeline -->
            <div class="flex-1 flex items-center justify-center md:justify-start gap-8">
              <div class="text-center md:text-left">
                <div class="text-[10px] font-black text-slate-300 uppercase tracking-wider mb-1">Check In</div>
                <div class="text-lg font-black" :class="att.checkInTime ? 'text-indigo-600' : 'text-slate-300'">
                  {{ formatTime(att.checkInTime) }}
                </div>
              </div>

              <div class="hidden md:block w-16 h-0.5 bg-slate-100 relative">
                <div v-if="att.checkInTime && att.checkOutTime" class="absolute inset-0 bg-indigo-200 animate-pulse"></div>
              </div>

              <div class="text-center md:text-left">
                <div class="text-[10px] font-black text-slate-300 uppercase tracking-wider mb-1">Check Out</div>
                <div class="text-lg font-black" :class="att.checkOutTime ? 'text-indigo-600' : 'text-slate-300'">
                  {{ formatTime(att.checkOutTime) }}
                </div>
              </div>
            </div>

            <!-- Stats -->
            <div class="flex items-center gap-10">
              <div class="text-center md:text-right hidden sm:block">
                <div class="text-[10px] font-black text-slate-300 uppercase tracking-wider mb-1">Thời gian làm</div>
                <div class="font-bold text-slate-700">{{ att.totalWorkHours || '0.0' }}h</div>
              </div>
              
              <div class="min-w-[120px] flex justify-end">
                <StatusBadge :status="att.status || (att.checkInTime ? 'ACTIVE' : 'OFFLINE')" />
              </div>
            </div>

            <!-- Actions -->
            <button class="p-2 text-slate-300 hover:text-indigo-600 hover:bg-indigo-50 rounded-xl transition-all">
              <MoreVertical class="w-5 h-5" />
            </button>
          </div>
        </div>

        <!-- Empty -->
        <EmptyState 
          v-else-if="!loading"
          title="Không có bản ghi chấm công"
          :description="`Không có dữ liệu chấm công nào được ghi nhận cho ngày ${selectedDate}.`"
          iconName="Clock"
        />
      </div>

    </div>
  
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
