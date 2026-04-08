<script setup>
import { ref, onMounted } from 'vue'
import { getMyPayrollHistory, getMyPayslip } from '@/api/me/portal'
import { useToast } from '@/composables/useToast'
import { Banknote, Loader2, ChevronRight, Download } from 'lucide-vue-next'

const toast = useToast()
const history = ref([])
const loading = ref(false)
const selectedSlip = ref(null)

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await getMyPayrollHistory()
    history.value = res.data?.content || res.data || []
  } catch (e) {
    toast.error('Không thể tải lịch sử lương')
  } finally {
    loading.value = false
  }
}

const fetchSlip = async (periodId) => {
  try {
    const res = await getMyPayslip(periodId)
    selectedSlip.value = res.data
  } catch (e) {
    toast.error('Không thể xem chi tiết')
  }
}

onMounted(fetchHistory)

const fmt = (n) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n || 0)
</script>

<template>

  <div class="max-w-4xl mx-auto pb-10 flex gap-6">

    <!-- MASTER VIEW: History List -->
    <div class="w-1/3 bg-white rounded-[24px] border border-slate-100 p-4 shadow-sm min-h-125">
      <h3 class="font-black text-slate-800 mb-4 px-2">Kỳ lương</h3>

      <div v-if="loading" class="flex justify-center p-10">
        <Loader2 class="w-6 h-6 text-indigo-500 animate-spin" />
      </div>

      <div v-else class="space-y-2">
        <div v-for="item in history" :key="item.payrollPeriodId" @click="fetchSlip(item.payrollPeriodId)"
          class="p-3 rounded-xl cursor-pointer transition-colors border"
          :class="selectedSlip?.payrollPeriodId === item.payrollPeriodId ? 'bg-indigo-50 border-indigo-100 shadow-sm' : 'border-transparent hover:bg-slate-50'">
          <p class="font-bold text-slate-700">{{ item.periodName || 'Tháng ' + item.month }}</p>
          <p class="text-xs font-medium text-slate-400 mt-1">Đã thanh toán: {{ fmt(item.netSalary) }}</p>
        </div>
      </div>
    </div>

    <!-- DETAIL VIEW: Payslip breakdown -->
    <div class="flex-1 bg-white rounded-[24px] border border-slate-100 p-8 shadow-sm">
      <div v-if="!selectedSlip"
        class="h-full flex items-center justify-center text-slate-400 font-medium text-sm flex-col gap-3">
        <Banknote class="w-12 h-12 opacity-20" />
        Chọn một kỳ lương để xem chi tiết
      </div>

      <div v-else class="animate-fade-in space-y-6">
        <div class="flex justify-between items-start">
          <div>
            <h2 class="text-2xl font-black text-slate-800">Phiếu lương</h2>
            <p class="text-slate-500 font-medium">{{ selectedSlip.periodName }}</p>
          </div>
          <button
            class="w-10 h-10 bg-slate-50 text-slate-400 rounded-xl hover:bg-indigo-50 hover:text-indigo-600 transition flex items-center justify-center">
            <Download class="w-5 h-5" />
          </button>
        </div>

        <div class="bg-slate-50 rounded-2xl p-6">
          <p class="text-xs font-black text-slate-400 uppercase tracking-widest mb-1">Thực lĩnh</p>
          <p class="text-4xl font-black text-indigo-600">{{ fmt(selectedSlip.netSalary) }}</p>
        </div>

        <div class="space-y-2">
          <div class="flex justify-between p-3 bg-white rounded-xl">
            <span class="font-medium text-slate-600">Lương cơ bản</span>
            <span class="font-bold text-slate-800">{{ fmt(selectedSlip.basicSalary) }}</span>
          </div>
          <div class="flex justify-between p-3 bg-emerald-50/50 rounded-xl text-emerald-800">
            <span class="font-medium">Tổng phụ cấp (+{{ selectedSlip.workDays }} ngày)</span>
            <span class="font-bold">+{{ fmt(selectedSlip.totalAllowance) }}</span>
          </div>
          <div class="flex justify-between p-3 bg-rose-50/50 rounded-xl text-rose-800">
            <span class="font-medium">Bảo hiểm & Thuế TNCN</span>
            <span class="font-bold">-{{ fmt(selectedSlip.totalDeduction) }}</span>
          </div>
        </div>
      </div>
    </div>

  </div>

</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(5px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
