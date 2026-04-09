<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMyPayrollHistory, getMyPayslip } from '@/api/me/portal'
import { useToast } from '@/composables/useToast'
import { unwrapData } from '@/utils/api'
import { Banknote, Download, Loader2 } from 'lucide-vue-next'

const toast = useToast()

const history = ref([])
const loading = ref(false)
const detailLoading = ref(false)
const selectedSlip = ref(null)
const selectedPeriodId = ref(null)

const selectedPeriod = computed(() =>
  history.value.find((item) => item.payrollPeriodId === selectedPeriodId.value) || null,
)

const deductionTotal = computed(() => {
  if (!selectedSlip.value) return 0
  return Number(selectedSlip.value.employeeInsuranceAmount || 0)
    + Number(selectedSlip.value.pitAmount || 0)
    + Number(selectedSlip.value.fixedDeductionTotal || 0)
})

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(Number(value || 0))
}

function formatPeriodLabel(item) {
  if (!item) return 'Kỳ lương'
  const month = String(item.periodMonth || '').padStart(2, '0')
  if (item.periodYear && item.periodMonth) {
    return `Tháng ${month}/${item.periodYear}`
  }
  return item.periodCode || 'Kỳ lương'
}

async function fetchSlip(periodId) {
  if (!periodId) return

  selectedPeriodId.value = periodId
  detailLoading.value = true
  try {
    const response = await getMyPayslip(periodId)
    selectedSlip.value = unwrapData(response)
  } catch (error) {
    console.error('Failed to fetch payslip detail:', error)
    selectedSlip.value = null
    toast.error(error?.response?.data?.message || 'Không thể xem chi tiết phiếu lương')
  } finally {
    detailLoading.value = false
  }
}

async function fetchHistory() {
  loading.value = true
  try {
    const response = await getMyPayrollHistory()
    const data = unwrapData(response)
    const items = Array.isArray(data?.payslips) ? data.payslips : Array.isArray(data) ? data : []
    history.value = items

    if (items.length) {
      await fetchSlip(items[0].payrollPeriodId)
    } else {
      selectedPeriodId.value = null
      selectedSlip.value = null
    }
  } catch (error) {
    console.error('Failed to fetch payroll history:', error)
    history.value = []
    selectedSlip.value = null
    toast.error('Không thể tải lịch sử lương')
  } finally {
    loading.value = false
  }
}

onMounted(fetchHistory)
</script>

<template>
  <div class="mx-auto flex max-w-6xl flex-col gap-6 px-4 pb-10 md:px-6 xl:flex-row">
    <div class="xl:w-[340px] xl:flex-none">
      <div class="min-h-[560px] rounded-[28px] border border-slate-200 bg-white p-4 shadow-sm">
        <div class="mb-4 px-2">
          <h3 class="text-2xl font-black text-slate-900">Kỳ lương</h3>
          <p class="mt-1 text-sm font-medium text-slate-500">Dữ liệu đã có sẵn từ kỳ lương phát hành trong database.</p>
        </div>

        <div v-if="loading" class="flex justify-center p-10">
          <Loader2 class="h-6 w-6 animate-spin text-indigo-500" />
        </div>

        <div v-else-if="history.length" class="space-y-3">
          <button
            v-for="item in history"
            :key="item.payrollPeriodId"
            type="button"
            class="w-full rounded-[22px] border p-4 text-left transition-all"
            :class="selectedPeriodId === item.payrollPeriodId
              ? 'border-indigo-200 bg-indigo-50 shadow-sm'
              : 'border-slate-200 hover:border-indigo-200 hover:bg-slate-50'"
            @click="fetchSlip(item.payrollPeriodId)"
          >
            <p class="text-xl font-black text-slate-800">{{ formatPeriodLabel(item) }}</p>
            <p class="mt-2 text-sm font-medium text-slate-500">Mã kỳ: {{ item.periodCode || '—' }}</p>
            <p class="mt-3 text-sm font-bold text-indigo-600">Đã thanh toán: {{ formatCurrency(item.netPay) }}</p>
          </button>
        </div>

        <div
          v-else
          class="flex min-h-[420px] flex-col items-center justify-center rounded-[24px] border border-dashed border-slate-200 bg-slate-50 px-6 text-center"
        >
          <Banknote class="h-10 w-10 text-slate-300" />
          <p class="mt-4 text-lg font-black text-slate-900">Chưa có phiếu lương phát hành</p>
          <p class="mt-2 text-sm font-medium text-slate-500">Khi kỳ lương được publish, phiếu lương sẽ xuất hiện tại đây.</p>
        </div>
      </div>
    </div>

    <div class="min-w-0 flex-1">
      <div class="min-h-[560px] rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm md:p-8">
        <div
          v-if="detailLoading"
          class="flex min-h-[460px] items-center justify-center"
        >
          <Loader2 class="h-7 w-7 animate-spin text-indigo-500" />
        </div>

        <div
          v-else-if="!selectedSlip"
          class="flex min-h-[460px] flex-col items-center justify-center gap-3 text-center text-slate-400"
        >
          <Banknote class="h-12 w-12 opacity-20" />
          <p class="text-base font-medium">Chọn một kỳ lương để xem chi tiết</p>
        </div>

        <div v-else class="space-y-6 animate-fade-in">
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">Phiếu lương cá nhân</p>
              <h2 class="mt-2 text-3xl font-black text-slate-900">{{ formatPeriodLabel(selectedPeriod) }}</h2>
              <p class="mt-2 text-sm font-medium text-slate-500">
                {{ selectedSlip.employeeCode }} · {{ selectedSlip.employeeName }} · {{ selectedSlip.orgUnitName || 'Chưa có đơn vị' }}
              </p>
            </div>

            <button
              type="button"
              class="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-slate-50 text-slate-400 transition hover:bg-indigo-50 hover:text-indigo-600"
              title="Sắp hỗ trợ xuất phiếu lương"
            >
              <Download class="h-5 w-5" />
            </button>
          </div>

          <div class="rounded-[24px] bg-[linear-gradient(135deg,#eef2ff_0%,#ffffff_100%)] p-6 ring-1 ring-indigo-100">
            <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Thực lĩnh</p>
            <p class="mt-3 text-4xl font-black text-indigo-600">{{ formatCurrency(selectedSlip.netPay) }}</p>
            <div class="mt-4 grid gap-3 text-sm font-semibold text-slate-500 md:grid-cols-3">
              <p>Gross: <span class="font-black text-slate-900">{{ formatCurrency(selectedSlip.grossIncome) }}</span></p>
              <p>Ngày hiện diện: <span class="font-black text-slate-900">{{ selectedSlip.presentDayCount }}/{{ selectedSlip.scheduledDayCount }}</span></p>
              <p>OT: <span class="font-black text-slate-900">{{ selectedSlip.approvedOtMinutes || 0 }} phút</span></p>
            </div>
          </div>

          <div class="grid gap-3 md:grid-cols-3">
            <div class="rounded-[22px] bg-slate-50 p-5">
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Lương tháng</p>
              <p class="mt-3 text-2xl font-black text-slate-900">{{ formatCurrency(selectedSlip.baseSalaryMonthly) }}</p>
            </div>
            <div class="rounded-[22px] bg-emerald-50/70 p-5">
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-emerald-600">Lương prorate + phụ cấp</p>
              <p class="mt-3 text-2xl font-black text-emerald-700">{{ formatCurrency(Number(selectedSlip.baseSalaryProrated || 0) + Number(selectedSlip.fixedEarningTotal || 0)) }}</p>
            </div>
            <div class="rounded-[22px] bg-rose-50/70 p-5">
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-rose-600">Khấu trừ</p>
              <p class="mt-3 text-2xl font-black text-rose-700">{{ formatCurrency(deductionTotal) }}</p>
            </div>
          </div>

          <div class="grid gap-3 md:grid-cols-2">
            <div class="rounded-[22px] border border-slate-200 p-5">
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Tóm tắt công</p>
              <div class="mt-4 space-y-3 text-sm font-medium text-slate-600">
                <div class="flex items-center justify-between">
                  <span>Ngày làm việc</span>
                  <span class="font-black text-slate-900">{{ selectedSlip.presentDayCount }}/{{ selectedSlip.scheduledDayCount }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span>Nghỉ có lương</span>
                  <span class="font-black text-slate-900">{{ selectedSlip.paidLeaveDayCount || 0 }} ngày</span>
                </div>
                <div class="flex items-center justify-between">
                  <span>Nghỉ không lương</span>
                  <span class="font-black text-slate-900">{{ selectedSlip.unpaidLeaveDayCount || 0 }} ngày</span>
                </div>
                <div class="flex items-center justify-between">
                  <span>Vắng mặt</span>
                  <span class="font-black text-slate-900">{{ selectedSlip.absentDayCount || 0 }} ngày</span>
                </div>
              </div>
            </div>

            <div class="rounded-[22px] border border-slate-200 p-5">
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Khấu trừ chi tiết</p>
              <div class="mt-4 space-y-3 text-sm font-medium text-slate-600">
                <div class="flex items-center justify-between">
                  <span>Bảo hiểm</span>
                  <span class="font-black text-slate-900">{{ formatCurrency(selectedSlip.employeeInsuranceAmount) }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span>Thuế TNCN</span>
                  <span class="font-black text-slate-900">{{ formatCurrency(selectedSlip.pitAmount) }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span>Khấu trừ cố định</span>
                  <span class="font-black text-slate-900">{{ formatCurrency(selectedSlip.fixedDeductionTotal) }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span>Thu nhập tính thuế</span>
                  <span class="font-black text-slate-900">{{ formatCurrency(selectedSlip.taxableIncome) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="overflow-hidden rounded-[24px] border border-slate-200">
            <div class="border-b border-slate-200 bg-slate-50 px-5 py-4">
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Breakdown thành phần lương</p>
            </div>
            <div v-if="selectedSlip.lines?.length" class="divide-y divide-slate-100">
              <div
                v-for="line in selectedSlip.lines"
                :key="line.payrollItemLineId"
                class="flex flex-col gap-2 px-5 py-4 md:flex-row md:items-center md:justify-between"
              >
                <div class="min-w-0">
                  <p class="font-black text-slate-900">{{ line.componentName }}</p>
                  <p class="mt-1 text-xs font-bold uppercase tracking-[0.18em] text-slate-400">
                    {{ line.componentCode }} · {{ line.componentCategory }}
                  </p>
                </div>
                <p class="text-sm font-black text-slate-900">{{ formatCurrency(line.lineAmount) }}</p>
              </div>
            </div>
            <div v-else class="px-5 py-6 text-sm font-medium text-slate-500">
              Phiếu lương này chưa có line chi tiết.
            </div>
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
