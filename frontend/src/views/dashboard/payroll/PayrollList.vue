<script setup>
import { ref, computed } from 'vue'
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'
import {
  Calculator, Send, FileText, Download, Plus,
  Banknote, TrendingUp, Users, CheckCircle2,
  ChevronDown, Search, Eye, Settings, RefreshCw,
  ArrowUpRight, ArrowDownRight, BarChart2, Clock,
  Wallet, Shield
} from 'lucide-vue-next'
import {
  getPayrollPeriods,
  getPayrollItems,
  generatePayrollDraft,
  approvePayrollPeriod,
  publishPayrollPeriod,
  exportBankTransferReport,
  exportPitReport,
  adjustPayrollItem
} from '@/api/admin/payroll.js'

// =================== STATE ===================
const activeTab = ref('payslips')
const searchQuery = ref('')
const selectedPeriod = ref('T03/2026')
const isGenerating = ref(false)
const showDetailModal = ref(false)
const selectedItem = ref(null)

const periods = ref(['T04/2026', 'T03/2026', 'T02/2026', 'T01/2026'])

const periodStatus = ref({
  label: 'Nháp',
  class: 'bg-amber-100 text-amber-700 border border-amber-200',
  dot: 'bg-amber-400'
})

const stats = [
  { title: 'Tổng quỹ lương', value: '1.24 tỷ', icon: Banknote, color: 'indigo', trend: 5, trendLabel: 'so với kỳ trước' },
  { title: 'Số phiếu lương', value: '127', icon: Users, color: 'emerald', trend: 2, trendLabel: 'nhân sự' },
  { title: 'Đã thanh toán', value: '98', icon: CheckCircle2, color: 'sky', trend: 12, trendLabel: 'phiếu' },
  { title: 'Tổng khấu trừ thuế', value: '87.4 tr', icon: Shield, color: 'rose', trend: 3, trendLabel: 'TNCN kỳ này' },
]

const payroll = ref([
  { id: 1, employee: 'Nguyễn Văn Anh', avatar: 'NA', dept: 'Engineering', position: 'Senior Dev', baseSalary: 20000000, workDays: 22, allowance: 2000000, insurance: 900000, tax: 1875000, bonus: 1000000, netPay: 20225000, status: 'APPROVED', bankAccount: '...8821' },
  { id: 2, employee: 'Trần Thị Bích', avatar: 'TB', dept: 'Marketing', position: 'Marketing Lead', baseSalary: 15000000, workDays: 21.5, allowance: 1500000, insurance: 675000, tax: 1050000, bonus: 0, netPay: 14482954, status: 'PAID', bankAccount: '...4432' },
  { id: 3, employee: 'Lê Hoàng Cường', avatar: 'LC', dept: 'Sales', position: 'Sales Manager', baseSalary: 18000000, workDays: 22, allowance: 1800000, insurance: 810000, tax: 1500000, bonus: 2000000, netPay: 19490000, status: 'DRAFT', bankAccount: '...7756' },
  { id: 4, employee: 'Phạm Thị Dung', avatar: 'PD', dept: 'Finance', position: 'Accountant', baseSalary: 13000000, workDays: 20, allowance: 1000000, insurance: 585000, tax: 680000, bonus: 0, netPay: 11550000, status: 'PAID', bankAccount: '...3309' },
  { id: 5, employee: 'Hoàng Văn Em', avatar: 'HE', dept: 'HR', position: 'HR Specialist', baseSalary: 12000000, workDays: 22, allowance: 800000, insurance: 540000, tax: 520000, bonus: 500000, netPay: 12240000, status: 'DRAFT', bankAccount: '...6614' },
])

// =================== COMPUTED ===================
const tabs = [
  { key: 'payslips', label: 'Bảng lương chi tiết', icon: FileText },
  { key: 'summary', label: 'Tóm tắt kỳ lương', icon: BarChart2 },
]

const statusConfig = {
  DRAFT:    { label: 'Nháp',          class: 'bg-slate-100 text-slate-600 border border-slate-200', dot: 'bg-slate-400' },
  APPROVED: { label: 'Đã duyệt',      class: 'bg-indigo-100 text-indigo-700 border border-indigo-200', dot: 'bg-indigo-400' },
  PAID:     { label: 'Đã thanh toán', class: 'bg-emerald-100 text-emerald-700 border border-emerald-200', dot: 'bg-emerald-400' },
}

const avatarColors = ['bg-indigo-500', 'bg-emerald-500', 'bg-amber-500', 'bg-rose-500', 'bg-violet-500', 'bg-sky-500']
function getAvatarColor(name) {
  return avatarColors[name.charCodeAt(0) % avatarColors.length]
}

const filteredPayroll = computed(() => {
  if (!searchQuery.value) return payroll.value
  const q = searchQuery.value.toLowerCase()
  return payroll.value.filter(p =>
    p.employee.toLowerCase().includes(q) ||
    p.dept.toLowerCase().includes(q) ||
    p.position.toLowerCase().includes(q)
  )
})

function fmt(num) {
  return new Intl.NumberFormat('vi-VN').format(num) + ' ₫'
}

const totalNetPay = computed(() =>
  payroll.value.reduce((s, p) => s + p.netPay, 0)
)
const totalTax = computed(() =>
  payroll.value.reduce((s, p) => s + p.tax, 0)
)
const totalBonus = computed(() =>
  payroll.value.reduce((s, p) => s + p.bonus, 0)
)

// =================== METHODS ===================
function openDetail(item) {
  selectedItem.value = item
  showDetailModal.value = true
}

async function handleGenerate() {
  isGenerating.value = true
  try {
    await generatePayrollDraft('period-03-2026')
  } catch (_) {}
  setTimeout(() => { isGenerating.value = false }, 1500)
}

async function handleApprove() {
  try {
    await approvePayrollPeriod('period-03-2026')
    periodStatus.value = { label: 'Đã duyệt', class: 'bg-indigo-100 text-indigo-700 border border-indigo-200', dot: 'bg-indigo-400' }
  } catch (_) {}
}

async function handlePublish() {
  try {
    await publishPayrollPeriod('period-03-2026')
  } catch (_) {}
}

async function handleBankExport() {
  try {
    await exportBankTransferReport({ period: selectedPeriod.value })
  } catch (_) {}
}

async function handlePitExport() {
  try {
    await exportPitReport({ year: 2026 })
  } catch (_) {}
}
</script>

<template>
  
    <div class="space-y-8">

      <!-- ===== HEADER ===== -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div class="flex items-center gap-3 mb-1">
            <div class="w-10 h-10 rounded-2xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-200">
              <Calculator class="w-5 h-5 text-white" />
            </div>
            <h2 class="text-3xl font-black text-slate-900 tracking-tight">Tính lương</h2>
          </div>
          <div class="ml-[52px] flex items-center gap-3">
            <p class="text-slate-500 font-medium">Kỳ lương:</p>
            <select v-model="selectedPeriod"
              class="text-sm font-bold text-slate-700 border border-slate-200 rounded-xl px-3 py-1.5 bg-white focus:outline-none focus:ring-2 focus:ring-indigo-300 appearance-none cursor-pointer">
              <option v-for="p in periods" :key="p" :value="p">{{ p }}</option>
            </select>
            <!-- Period status badge -->
            <span class="flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold" :class="periodStatus.class">
              <span class="w-1.5 h-1.5 rounded-full" :class="periodStatus.dot"></span>
              {{ periodStatus.label }}
            </span>
          </div>
        </div>

        <!-- Action buttons -->
        <div class="flex flex-wrap items-center gap-2">
          <button @click="handleBankExport"
            class="flex items-center gap-2 px-4 py-2.5 rounded-xl font-semibold text-slate-600 bg-white border border-slate-200 hover:border-slate-300 hover:bg-slate-50 transition-all shadow-sm text-sm">
            <Download class="w-4 h-4" /> UNC Ngân hàng
          </button>
          <button @click="handlePitExport"
            class="flex items-center gap-2 px-4 py-2.5 rounded-xl font-semibold text-slate-600 bg-white border border-slate-200 hover:border-slate-300 hover:bg-slate-50 transition-all shadow-sm text-sm">
            <Download class="w-4 h-4" /> Kê khai Thuế TNCN
          </button>
          <button @click="handleApprove"
            class="flex items-center gap-2 px-4 py-2.5 rounded-xl font-semibold text-indigo-600 bg-indigo-50 border border-indigo-200 hover:bg-indigo-100 transition-all shadow-sm text-sm">
            <CheckCircle2 class="w-4 h-4" /> Duyệt bảng lương
          </button>
          <button @click="handleGenerate" :disabled="isGenerating"
            class="flex items-center gap-2 bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 text-sm disabled:opacity-60 disabled:cursor-not-allowed">
            <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': isGenerating }" />
            {{ isGenerating ? 'Đang tính...' : 'Chạy bảng lương' }}
          </button>
        </div>
      </div>

      <!-- ===== STAT CARDS ===== -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard
          v-for="stat in stats" :key="stat.title"
          :title="stat.title" :value="stat.value"
          :icon="stat.icon" :color="stat.color"
          :trend="stat.trend" :trendLabel="stat.trendLabel"
        />
      </div>

      <!-- ===== MAIN CARD ===== -->
      <GlassCard :glass="false" padding="p-0" class="rounded-3xl border border-slate-100 shadow-sm overflow-hidden bg-white">

        <!-- Tabs + Search -->
        <div class="border-b border-slate-100 px-6 pt-4 pb-0 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div class="flex gap-1">
            <button
              v-for="tab in tabs" :key="tab.key"
              @click="activeTab = tab.key"
              class="flex items-center gap-2 px-4 py-2.5 text-sm font-semibold rounded-t-xl transition-all relative"
              :class="activeTab === tab.key
                ? 'text-indigo-600 bg-indigo-50'
                : 'text-slate-500 hover:text-slate-700 hover:bg-slate-50'"
            >
              <component :is="tab.icon" class="w-4 h-4" />{{ tab.label }}
              <span v-if="activeTab === tab.key"
                class="absolute bottom-0 left-0 right-0 h-0.5 bg-indigo-600 rounded-t" />
            </button>
          </div>

          <div class="pb-3">
            <div class="relative">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input v-model="searchQuery" type="text" placeholder="Tìm nhân viên, phòng ban..."
                class="pl-9 pr-4 py-2 text-sm rounded-xl border border-slate-200 bg-slate-50 focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-indigo-400 w-56 transition-all" />
            </div>
          </div>
        </div>

        <!-- ===== TAB: PAYSLIPS TABLE ===== -->
        <div v-if="activeTab === 'payslips'" class="overflow-x-auto">
          <table class="w-full text-left text-sm">
            <thead>
              <tr class="bg-slate-50/80 border-b border-slate-100">
                <th class="py-3.5 px-5 font-bold text-slate-500 text-xs uppercase tracking-wider">Nhân viên</th>
                <th class="py-3.5 px-4 font-bold text-slate-500 text-xs uppercase tracking-wider">Lương cơ bản</th>
                <th class="py-3.5 px-4 font-bold text-slate-500 text-xs uppercase tracking-wider">Ngày công</th>
                <th class="py-3.5 px-4 font-bold text-xs uppercase tracking-wider text-emerald-600">+ Phụ cấp</th>
                <th class="py-3.5 px-4 font-bold text-xs uppercase tracking-wider text-violet-600">+ Thưởng</th>
                <th class="py-3.5 px-4 font-bold text-xs uppercase tracking-wider text-rose-600">- BHXH</th>
                <th class="py-3.5 px-4 font-bold text-xs uppercase tracking-wider text-rose-600">- Thuế TNCN</th>
                <th class="py-3.5 px-4 font-bold text-slate-500 text-xs uppercase tracking-wider">Thực nhận</th>
                <th class="py-3.5 px-4 font-bold text-slate-500 text-xs uppercase tracking-wider">Trạng thái</th>
                <th class="py-3.5 px-4"></th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-50">
              <tr v-for="pay in filteredPayroll" :key="pay.id"
                class="hover:bg-slate-50/60 transition-colors group cursor-pointer"
                @click="openDetail(pay)">
                <!-- Employee -->
                <td class="py-4 px-5">
                  <div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-xl flex items-center justify-center text-white text-xs font-black shadow-sm shrink-0"
                      :class="getAvatarColor(pay.employee)">
                      {{ pay.avatar }}
                    </div>
                    <div>
                      <div class="font-bold text-slate-900">{{ pay.employee }}</div>
                      <div class="text-xs text-slate-400">{{ pay.position }}</div>
                    </div>
                  </div>
                </td>
                <td class="py-4 px-4">
                  <span class="font-semibold text-slate-700">{{ fmt(pay.baseSalary) }}</span>
                </td>
                <td class="py-4 px-4">
                  <span class="font-bold text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded-lg">{{ pay.workDays }}</span>
                </td>
                <td class="py-4 px-4">
                  <span class="font-semibold text-emerald-600">+{{ fmt(pay.allowance) }}</span>
                </td>
                <td class="py-4 px-4">
                  <span class="font-semibold" :class="pay.bonus > 0 ? 'text-violet-600' : 'text-slate-300'">
                    {{ pay.bonus > 0 ? '+' + fmt(pay.bonus) : '—' }}
                  </span>
                </td>
                <td class="py-4 px-4">
                  <span class="font-semibold text-rose-500">-{{ fmt(pay.insurance) }}</span>
                </td>
                <td class="py-4 px-4">
                  <span class="font-semibold text-rose-500">-{{ fmt(pay.tax) }}</span>
                </td>
                <td class="py-4 px-4">
                  <span class="font-black text-slate-900 text-base">{{ fmt(pay.netPay) }}</span>
                </td>
                <td class="py-4 px-4">
                  <span class="flex items-center gap-1 w-fit px-2.5 py-1 rounded-full text-xs font-bold"
                    :class="statusConfig[pay.status]?.class">
                    <span class="w-1.5 h-1.5 rounded-full" :class="statusConfig[pay.status]?.dot"></span>
                    {{ statusConfig[pay.status]?.label }}
                  </span>
                </td>
                <td class="py-4 px-4 text-right">
                  <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button @click.stop="openDetail(pay)"
                      class="p-1.5 rounded-lg text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 transition-colors">
                      <Eye class="w-4 h-4" />
                    </button>
                    <button @click.stop
                      class="flex items-center gap-1 px-3 py-1.5 text-xs font-bold rounded-xl bg-indigo-50 text-indigo-600 hover:bg-indigo-100 transition-colors">
                      <Send class="w-3.5 h-3.5" /> Gửi phiếu
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
            <!-- Total row -->
            <tfoot>
              <tr class="bg-slate-50/80 border-t-2 border-slate-200">
                <td colspan="3" class="py-4 px-5 font-black text-slate-800 text-sm">Tổng cộng ({{ payroll.length }} nhân viên)</td>
                <td colspan="2" class="py-4 px-4 font-bold text-emerald-600 text-sm">+{{ fmt(totalBonus) }}</td>
                <td colspan="2" class="py-4 px-4 font-bold text-rose-500 text-sm">-{{ fmt(totalTax) }}</td>
                <td class="py-4 px-4 font-black text-indigo-700 text-lg">{{ fmt(totalNetPay) }}</td>
                <td colspan="2"></td>
              </tr>
            </tfoot>
          </table>
        </div>

        <!-- ===== TAB: SUMMARY ===== -->
        <div v-if="activeTab === 'summary'" class="p-6 space-y-6">
          <div class="grid md:grid-cols-2 gap-6">
            <!-- Breakdown card -->
            <div class="bg-slate-50 rounded-2xl p-6 space-y-4">
              <h3 class="font-bold text-slate-800 text-base flex items-center gap-2">
                <Wallet class="w-5 h-5 text-indigo-600" /> Cơ cấu tổng quỹ lương
              </h3>
              <div class="space-y-3">
                <div v-for="row in [
                  { label: 'Lương cơ bản', value: payroll.reduce((s,p)=>s+p.baseSalary,0), color: 'bg-indigo-400' },
                  { label: 'Phụ cấp', value: payroll.reduce((s,p)=>s+p.allowance,0), color: 'bg-emerald-400' },
                  { label: 'Thưởng', value: payroll.reduce((s,p)=>s+p.bonus,0), color: 'bg-violet-400' },
                  { label: '(-) BHXH', value: -payroll.reduce((s,p)=>s+p.insurance,0), color: 'bg-rose-400' },
                  { label: '(-) Thuế TNCN', value: -payroll.reduce((s,p)=>s+p.tax,0), color: 'bg-rose-600' },
                ]" :key="row.label" class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <div class="w-3 h-3 rounded-full" :class="row.color"></div>
                    <span class="text-sm font-medium text-slate-600">{{ row.label }}</span>
                  </div>
                  <span class="font-bold text-slate-800 text-sm" :class="row.value < 0 ? 'text-rose-600' : ''">
                    {{ row.value < 0 ? '-' : '' }}{{ fmt(Math.abs(row.value)) }}
                  </span>
                </div>
                <div class="pt-3 border-t border-slate-200 flex items-center justify-between">
                  <span class="font-black text-slate-900">Tổng thực nhận</span>
                  <span class="font-black text-lg text-indigo-700">{{ fmt(totalNetPay) }}</span>
                </div>
              </div>
            </div>

            <!-- Status breakdown -->
            <div class="bg-slate-50 rounded-2xl p-6 space-y-4">
              <h3 class="font-bold text-slate-800 text-base flex items-center gap-2">
                <BarChart2 class="w-5 h-5 text-indigo-600" /> Phân bổ theo trạng thái
              </h3>
              <div class="space-y-3">
                <div v-for="s in ['DRAFT','APPROVED','PAID']" :key="s">
                  <div class="flex justify-between text-xs font-semibold text-slate-600 mb-1">
                    <span>{{ statusConfig[s].label }}</span>
                    <span>{{ payroll.filter(p=>p.status===s).length }} người</span>
                  </div>
                  <div class="h-2 bg-slate-200 rounded-full overflow-hidden">
                    <div
                      class="h-full rounded-full"
                      :class="s==='DRAFT' ? 'bg-slate-400' : s==='APPROVED' ? 'bg-indigo-500' : 'bg-emerald-500'"
                      :style="{ width: (payroll.filter(p=>p.status===s).length / payroll.length * 100) + '%' }"
                    />
                  </div>
                </div>
              </div>

              <!-- Quick actions -->
              <div class="pt-4 border-t border-slate-200 grid grid-cols-2 gap-2">
                <button @click="handlePublish"
                  class="flex items-center justify-center gap-2 py-2.5 px-4 bg-indigo-600 text-white rounded-xl font-bold text-sm hover:bg-indigo-700 transition-all shadow-md shadow-indigo-100">
                  <Send class="w-4 h-4" /> Phát phiếu lương
                </button>
                <button @click="handleBankExport"
                  class="flex items-center justify-center gap-2 py-2.5 px-4 bg-emerald-600 text-white rounded-xl font-bold text-sm hover:bg-emerald-700 transition-all shadow-md">
                  <Download class="w-4 h-4" /> Xuất UNC
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination footer -->
        <div class="border-t border-slate-100 px-6 py-3 flex items-center justify-between text-sm text-slate-500">
          <span>{{ filteredPayroll.length }} phiếu lương</span>
          <div class="flex items-center gap-1">
            <button class="px-3 py-1 rounded-lg hover:bg-slate-50 font-medium transition-colors">←</button>
            <button class="px-3 py-1 rounded-lg bg-indigo-600 text-white font-bold">1</button>
            <button class="px-3 py-1 rounded-lg hover:bg-slate-50 font-medium transition-colors">→</button>
          </div>
        </div>
      </GlassCard>

    </div>

    <!-- ===== PAYSLIP DETAIL MODAL ===== -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="showDetailModal = false"></div>
          <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-lg overflow-hidden">
            <!-- Modal header -->
            <div class="bg-linear-to-r from-indigo-600 to-indigo-700 p-6 text-white">
              <div class="flex items-center justify-between mb-3">
                <h3 class="font-black text-xl">Chi tiết phiếu lương</h3>
                <button @click="showDetailModal = false" class="w-8 h-8 rounded-xl bg-white/20 hover:bg-white/30 flex items-center justify-center transition-colors text-lg font-bold">×</button>
              </div>
              <div class="flex items-center gap-3" v-if="selectedItem">
                <div class="w-12 h-12 rounded-2xl bg-white/20 flex items-center justify-center font-black text-lg">
                  {{ selectedItem.avatar }}
                </div>
                <div>
                  <div class="font-black text-lg">{{ selectedItem.employee }}</div>
                  <div class="text-indigo-200 text-sm">{{ selectedItem.position }} · {{ selectedItem.dept }}</div>
                </div>
              </div>
            </div>

            <!-- Modal body -->
            <div v-if="selectedItem" class="p-6 space-y-3">
              <div v-for="row in [
                { label: 'Lương cơ bản', value: fmt(selectedItem.baseSalary), type: 'neutral' },
                { label: 'Ngày công thực tế', value: selectedItem.workDays + ' / 22 ngày', type: 'neutral' },
                { label: '+ Phụ cấp', value: fmt(selectedItem.allowance), type: 'positive' },
                { label: '+ Thưởng', value: selectedItem.bonus > 0 ? fmt(selectedItem.bonus) : '—', type: 'positive' },
                { label: '- BHXH nhân viên đóng', value: fmt(selectedItem.insurance), type: 'negative' },
                { label: '- Thuế TNCN', value: fmt(selectedItem.tax), type: 'negative' },
              ]" :key="row.label"
                class="flex items-center justify-between py-2.5 border-b border-slate-100 last:border-0">
                <span class="text-sm font-medium text-slate-600">{{ row.label }}</span>
                <span class="font-bold text-sm"
                  :class="row.type === 'positive' ? 'text-emerald-600' : row.type === 'negative' ? 'text-rose-600' : 'text-slate-800'">
                  {{ row.value }}
                </span>
              </div>
              <div class="flex items-center justify-between pt-3 border-t-2 border-slate-200">
                <span class="font-black text-slate-900">Thực nhận</span>
                <span class="font-black text-2xl text-indigo-700">{{ fmt(selectedItem.netPay) }}</span>
              </div>
              <div class="text-xs text-slate-400 flex items-center gap-1">
                <Banknote class="w-3.5 h-3.5" /> Tài khoản nhận: {{ selectedItem.bankAccount }}
              </div>
            </div>

            <!-- Modal footer -->
            <div class="px-6 pb-6 flex gap-2">
              <button @click="showDetailModal = false"
                class="flex-1 py-2.5 rounded-xl border border-slate-200 font-semibold text-slate-600 hover:bg-slate-50 transition-all text-sm">Đóng</button>
              <button
                class="flex-1 py-2.5 rounded-xl bg-indigo-600 text-white font-bold hover:bg-indigo-700 transition-all text-sm flex items-center justify-center gap-2">
                <Send class="w-4 h-4" /> Gửi phiếu lương
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.25s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .relative,
.modal-leave-to .relative {
  transform: scale(0.95) translateY(20px);
}
</style>
