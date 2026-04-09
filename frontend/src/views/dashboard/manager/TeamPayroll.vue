<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import {
  getTeamPayrollItems,
  confirmPayrollItem,
  getTeamPayrollPeriods
} from '@/api/manager/manager'
import {
  Banknote, CheckCircle2, Loader2, Info
} from 'lucide-vue-next'

const toast = useToast()
const router = useRouter()

/* ─── STATE ─────────────────────────────────────────────────── */
const items = ref([])
const loading = ref(false)
const confirmLoading = ref(null)

// Period selector: list periods from dashboard report or fallback
const selectedPeriodId = ref(null)
const periods = ref([])
const periodsLoading = ref(true)

/* ─── API ────────────────────────────────────────────────────── */
const fetchPeriods = async () => {
  periodsLoading.value = true
  try {
    const res = await getTeamPayrollPeriods()
    const reportPeriods = res.data ?? []
    periods.value = Array.isArray(reportPeriods) ? reportPeriods : []
    if (periods.value.length) {
      selectedPeriodId.value = periods.value[0].payrollPeriodId
    }
  } catch {
    periods.value = []
  } finally {
    periodsLoading.value = false
  }
}

const fetchItems = async () => {
  if (!selectedPeriodId.value) return
  loading.value = true
  try {
    const res = await getTeamPayrollItems(selectedPeriodId.value)
    items.value = res.data?.content ?? res.data ?? []
  } catch (e) {
    toast.error('Không thể tải dữ liệu bảng lương')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchPeriods()
  await fetchItems()
})

/* ─── ACTIONS ────────────────────────────────────────────────── */
const handleConfirm = async (item) => {
  confirmLoading.value = item.payrollItemId
  try {
    await confirmPayrollItem(item.payrollItemId, { note: 'Manager xác nhận dữ liệu payroll từ team workspace.' })
    toast.success(`Đã xác nhận lương của ${item.employeeName}`)
    item.managerConfirmedAt = new Date().toISOString()
    item.itemStatus = 'MANAGER_CONFIRMED'
  } catch (e) {
    toast.error('Xác nhận thất bại')
  } finally {
    confirmLoading.value = null
  }
}

const fmtCurrency = (n) => {
  if (n == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}

const confirmedCount = () => items.value.filter(i => i.managerConfirmedAt || i.itemStatus === 'MANAGER_CONFIRMED').length
</script>

<template>
  
    <div class="space-y-8 animate-fade-in">

      <!-- HEADER -->
      <PageHeader
        title="Bảng lương Đội nhóm"
        subtitle="Xem xét và xác nhận phiếu lương cho từng thành viên"
        :icon="Banknote"
        iconColor="bg-emerald-600"
        iconShadow="shadow-emerald-100"
      />

      <!-- PERIOD LOADING -->
      <div v-if="periodsLoading" class="flex items-center justify-center py-8">
        <Loader2 class="w-8 h-8 text-emerald-600 animate-spin" />
      </div>

      <template v-else>
        <!-- NO PERIODS -->
        <div v-if="periods.length === 0" class="bg-emerald-50 border border-emerald-100 rounded-3xl p-8 flex items-center gap-4">
          <div class="w-12 h-12 rounded-2xl bg-emerald-100 flex items-center justify-center shrink-0">
            <Info class="w-6 h-6 text-emerald-600" />
          </div>
          <div>
            <p class="font-black text-emerald-900">Chưa có kỳ lương nào</p>
            <p class="text-sm text-emerald-700 font-medium mt-1">HR chưa mở kỳ tính lương. Khi có kỳ lương, danh sách sẽ hiện tại đây.</p>
          </div>
        </div>

        <!-- PERIOD SELECTOR + TABLE -->
        <template v-else>
          <!-- Period Tabs -->
          <div class="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
            <button
              v-for="p in periods" :key="p.payrollPeriodId"
              @click="selectedPeriodId = p.payrollPeriodId; fetchItems()"
              class="shrink-0 px-5 py-3 rounded-2xl text-sm font-black transition-all"
              :class="selectedPeriodId === p.payrollPeriodId
                ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-200'
                : 'bg-white border border-slate-100 text-slate-600 hover:border-emerald-200'"
            >
              {{ p.periodCode || `${p.periodMonth}/${p.periodYear}` }}
            </button>
          </div>

          <!-- Summary banner -->
          <div v-if="items.length" class="flex items-center gap-3 px-6 py-4 bg-emerald-50 border border-emerald-100 rounded-3xl">
            <CheckCircle2 class="w-5 h-5 text-emerald-600 shrink-0" />
            <span class="text-sm font-bold text-emerald-800">
              Đã xác nhận <strong>{{ confirmedCount() }}</strong> /
              <strong>{{ items.length }}</strong> phiếu lương
            </span>
          </div>

          <!-- Payroll cards -->
          <div class="bg-white border border-slate-100 rounded-[32px] overflow-hidden shadow-sm relative">
            <div v-if="loading" class="absolute inset-0 z-10 flex items-center justify-center bg-white/60">
              <Loader2 class="w-10 h-10 text-emerald-600 animate-spin" />
            </div>

            <div v-if="items.length > 0" class="divide-y divide-slate-50">
              <div
                v-for="item in items" :key="item.payrollItemId"
                class="group px-8 py-6 hover:bg-slate-50/60 transition-all flex flex-col xl:flex-row xl:items-center justify-between gap-6"
                :class="item.confirmed ? 'opacity-60' : ''"
              >
                <!-- Employee info -->
                <div class="flex items-start gap-4 min-w-[260px]">
                  <AvatarBox :name="item.employeeName" size="lg" shape="rounded-[20px]" shadow />
                  <div>
                    <h4 class="font-black text-slate-900 text-base mb-1">{{ item.employeeName }}</h4>
                    <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">{{ item.employeeCode }}</p>
                    <p class="text-xs font-medium text-slate-400 mt-1">{{ item.orgUnitName }}</p>
                  </div>
                </div>

                <!-- Salary breakdown -->
                <div class="flex-1 grid grid-cols-2 lg:grid-cols-4 gap-6">
                  <div class="bg-slate-50 rounded-2xl p-4 text-center">
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Lương cơ bản</p>
                    <p class="text-sm font-black text-slate-800">{{ fmtCurrency(item.baseSalaryMonthly) }}</p>
                  </div>
                  <div class="bg-indigo-50 rounded-2xl p-4 text-center">
                    <p class="text-[9px] font-black text-indigo-400 uppercase tracking-widest mb-1">Phụ cấp</p>
                    <p class="text-sm font-black text-indigo-700">{{ fmtCurrency(item.fixedEarningTotal) }}</p>
                  </div>
                  <div class="bg-rose-50 rounded-2xl p-4 text-center">
                    <p class="text-[9px] font-black text-rose-400 uppercase tracking-widest mb-1">Khấu trừ</p>
                    <p class="text-sm font-black text-rose-700">{{ fmtCurrency(item.fixedDeductionTotal) }}</p>
                  </div>
                  <div class="bg-emerald-50 rounded-2xl p-4 text-center border-2 border-emerald-100">
                    <p class="text-[9px] font-black text-emerald-500 uppercase tracking-widest mb-1">Thực lĩnh</p>
                    <p class="text-sm font-black text-emerald-700">{{ fmtCurrency(item.netPay) }}</p>
                  </div>
                </div>

                <!-- Action -->
                <div class="shrink-0 flex items-center gap-3">
                  <button
                    type="button"
                    class="rounded-xl px-3 py-2 text-xs font-black text-slate-500 transition hover:bg-slate-100 hover:text-indigo-600"
                    @click="router.push(`/employees/${item.employeeId}`)"
                  >
                    Hồ sơ
                  </button>
                  <div v-if="item.managerConfirmedAt || item.itemStatus === 'MANAGER_CONFIRMED'" class="flex items-center gap-2 px-4 py-2.5 bg-emerald-50 rounded-2xl">
                    <CheckCircle2 class="w-4 h-4 text-emerald-600" />
                    <span class="text-xs font-black text-emerald-700">Đã xác nhận</span>
                  </div>
                  <button
                    v-else
                    @click="handleConfirm(item)"
                    :disabled="confirmLoading === item.payrollItemId"
                    class="flex items-center gap-2 px-5 py-3 bg-emerald-600 text-white rounded-2xl text-sm font-black hover:bg-emerald-700 transition-all shadow-lg shadow-emerald-200 disabled:opacity-50"
                  >
                    <Loader2 v-if="confirmLoading === item.payrollItemId" class="w-4 h-4 animate-spin" />
                    <CheckCircle2 v-else class="w-4 h-4" />
                    Xác nhận
                  </button>
                </div>
              </div>
            </div>

            <EmptyState v-else-if="!loading"
              title="Chưa có dữ liệu lương"
              description="Kỳ lương này chưa được HR tính toán hoặc phân công cho đội nhóm của bạn."
            />
          </div>
        </template>
      </template>

    </div>
  
</template>

<style scoped>
.animate-fade-in { animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
</style>
