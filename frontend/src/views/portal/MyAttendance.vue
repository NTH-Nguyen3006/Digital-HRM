<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '@/components/EmployeeLayout.vue'
import { Calendar, Clock, CheckCircle, Loader2 } from 'lucide-vue-next'
import { getMyAttendance } from '@/api/me/portal'
import { useToast } from '@/composables/useToast'

const toast = useToast()
const history = ref([])
const loading = ref(false)
const stats = ref({
  presentDays: 0,
  lateCount: 0,
  missingCount: 0
})

const fetchAttendance = async () => {
  loading.value = true
  try {
    const data = await getMyAttendance()
    history.value = data.recentLogs || []
    // Basic heuristics for stats from recent logs
    stats.value.lateCount = history.value.filter(log => log.note?.toLowerCase().includes('muộn')).length
  } catch (error) {
    toast.error('Không thể tải lịch sử chấm công')
  } finally {
    loading.value = false
  }
}

const fmtDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : '—'
const fmtTime = (t) => t ? new Date(t).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : '—'

onMounted(fetchAttendance)
</script>

<template>
  <EmployeeLayout>
    <div class="max-w-5xl mx-auto pt-8 pb-20 px-6">
      <div class="mb-8">
        <h1 class="text-3xl font-black text-slate-900">Lịch sử chấm công</h1>
        <p class="text-slate-500 font-medium mt-2">Xem lại thời gian làm việc cá nhân của bạn</p>
      </div>

      <div v-if="loading" class="flex flex-col items-center justify-center py-20 bg-white rounded-3xl border border-slate-100 shadow-sm">
        <Loader2 class="w-12 h-12 text-indigo-500 animate-spin mb-4" />
        <p class="text-slate-500 font-medium">Đang tải lịch sử chấm công...</p>
      </div>

      <template v-else>
        <div class="grid md:grid-cols-4 gap-6 mb-8">
          <div class="bg-indigo-600 rounded-3xl p-6 text-white shadow-xl shadow-indigo-200">
            <div class="text-indigo-200 text-sm font-bold mb-1">Số lần quẹt thẻ</div>
            <div class="text-4xl font-black">{{ history.length }}</div>
          </div>
          <div class="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm flex flex-col justify-center">
            <div class="text-slate-400 text-xs font-bold uppercase tracking-widest mb-1">Ghi chú bất thường</div>
            <div class="text-2xl font-black text-amber-500">{{ stats.lateCount }} lần</div>
          </div>
          <div class="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm flex flex-col justify-center">
            <div class="text-slate-400 text-xs font-bold uppercase tracking-widest mb-1">Thiếu dữ liệu</div>
            <div class="text-2xl font-black text-rose-500">{{ stats.missingCount }} lần</div>
          </div>
        </div>

        <div class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
          <table class="w-full text-left">
            <thead>
              <tr class="bg-slate-50/50 border-b border-slate-100">
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Ngày công</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Loại</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Thời gian</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Ghi chú</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="att in history" :key="att.attendanceLogId" class="hover:bg-slate-50/50 transition-colors">
                <td class="py-4 px-6 font-bold text-slate-900 flex items-center">
                  <Calendar class="w-4 h-4 mr-2 text-slate-400" /> {{ fmtDate(att.attendanceDate) }}
                </td>
                <td class="py-4 px-6">
                   <span class="px-2 py-1 rounded-md text-[10px] font-black uppercase tracking-wider"
                    :class="att.eventType === 'CHECK_IN' ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'">
                    {{ att.eventType }}
                  </span>
                </td>
                <td class="py-4 px-6 font-semibold text-slate-700">{{ fmtTime(att.eventTime) }}</td>
                <td class="py-4 px-6">
                  <span class="text-sm text-slate-500">{{ att.note || '—' }}</span>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-if="history.length === 0" class="py-20 text-center text-slate-400 font-medium">
            Rất tiếc, không tìm thấy dữ liệu chấm công nào.
          </div>
        </div>
      </template>
    </div>
  </EmployeeLayout>
</template>
