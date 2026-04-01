<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import MainLayout from '../../../layouts/MainLayout.vue'
import {
  ArrowLeft, ChevronRight, Edit, Send, CheckCircle2,
  XCircle, Zap, History, Paperclip, Plus, Download,
  AlertTriangle, Clock, FileSignature, RotateCcw, Trash2
} from 'lucide-vue-next'

const router = useRouter()
const activeTab = ref('detail')

const contract = ref({
  id: 'HD-2026-001',
  contractCode: 'HĐLĐ-2026-001',
  employee: 'Nguyễn Văn An',
  employeeCode: 'NV001',
  contractType: 'Xác định thời hạn (12 tháng)',
  orgUnit: 'Khối Phát triển Phần mềm',
  jobTitle: 'Senior Backend Engineer',
  baseSalary: '25,000,000',
  allowances: '3,000,000',
  startDate: '01/01/2026',
  endDate: '31/12/2026',
  signDate: '20/12/2025',
  companyRepresentative: 'Phạm Văn Giám Đốc',
  companyTitle: 'Tổng Giám đốc',
  status: 'ACTIVE',
  workLocation: 'Văn phòng chính - TP.HCM',
  probationDays: 0,
  notes: 'Hợp đồng gia hạn lần 2 từ hợp đồng gốc 2024.',
})

const statusFlow = [
  { key: 'DRAFT', label: 'Nháp', icon: Edit },
  { key: 'PENDING_SIGN', label: 'Chờ ký', icon: Clock },
  { key: 'ACTIVE', label: 'Hiệu lực', icon: CheckCircle2 },
]

const currentStepIdx = computed(() => statusFlow.findIndex(s => s.key === contract.value.status))

const statusConfig = {
  DRAFT: { label: 'Nháp', cls: 'bg-slate-100 text-slate-600 ring-slate-300', dot: 'bg-slate-400' },
  PENDING_SIGN: { label: 'Chờ ký', cls: 'bg-amber-50 text-amber-700 ring-amber-600/20', dot: 'bg-amber-400' },
  ACTIVE: { label: 'Đang hiệu lực', cls: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20', dot: 'bg-emerald-500 animate-pulse' },
  SUSPENDED: { label: 'Tạm đình chỉ', cls: 'bg-orange-50 text-orange-700 ring-orange-600/20', dot: 'bg-orange-400' },
  TERMINATED: { label: 'Chấm dứt', cls: 'bg-rose-50 text-rose-700 ring-rose-600/20', dot: 'bg-rose-500' },
  EXPIRED: { label: 'Hết hạn', cls: 'bg-slate-100 text-slate-500 ring-slate-200', dot: 'bg-slate-400' },
}

const history = ref([
  { id: 1, fromStatus: null, toStatus: 'DRAFT', actor: 'Nguyễn Thị HR', note: 'Tạo nháp hợp đồng', timestamp: '20/12/2025 09:00' },
  { id: 2, fromStatus: 'DRAFT', toStatus: 'PENDING_SIGN', actor: 'Nguyễn Thị HR', note: 'Gửi chờ ký duyệt', timestamp: '20/12/2025 10:30' },
  { id: 3, fromStatus: 'PENDING_SIGN', toStatus: 'ACTIVE', actor: 'Phạm Văn Giám Đốc', note: 'Ký kết và kích hoạt hợp đồng', timestamp: '20/12/2025 14:00' },
])

const appendices = ref([
  {
    id: 1, appendixCode: 'PLH-001', title: 'Phụ lục điều chỉnh lương Q1/2026',
    effectiveDate: '01/01/2026', status: 'ACTIVE', salaryChange: '+2,000,000',
    signedBy: 'Phạm Văn Giám Đốc'
  },
])

const attachments = ref([
  { id: 1, name: 'Hợp đồng scan bản gốc.pdf', size: '2.5 MB', uploadedAt: '20/12/2025', status: 'ACTIVE' },
  { id: 2, name: 'Phụ lục lương Q1.pdf', size: '1.2 MB', uploadedAt: '01/01/2026', status: 'ACTIVE' },
])

const tabs = [
  { key: 'detail', label: 'Chi tiết HĐ' },
  { key: 'appendix', label: 'Phụ lục' },
  { key: 'attachments', label: 'Đính kèm' },
  { key: 'history', label: 'Lịch sử trạng thái' },
]

const cfg = computed(() => statusConfig[contract.value.status] || statusConfig.DRAFT)
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <!-- Breadcrumb -->
      <div class="flex items-center gap-2 text-sm font-medium">
        <button @click="router.push('/contracts')" class="text-indigo-600 hover:underline flex items-center gap-1">
          <ArrowLeft class="w-4 h-4" /> Hợp đồng lao động
        </button>
        <ChevronRight class="w-4 h-4 text-slate-300" />
        <span class="text-slate-500">{{ contract.contractCode }}</span>
      </div>

      <!-- Contract Header -->
      <div class="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
        <div class="bg-linear-to-r from-slate-900 to-slate-800 p-8 text-white relative overflow-hidden">
          <div class="absolute -top-16 -right-16 w-56 h-56 bg-white/5 rounded-full blur-3xl"></div>
          <div class="relative flex flex-col md:flex-row md:items-start gap-6">
            <div class="w-16 h-16 bg-white/10 rounded-2xl flex items-center justify-center">
              <FileSignature class="w-8 h-8 text-white" />
            </div>
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <h1 class="text-2xl font-black">{{ contract.contractCode }}</h1>
                <span class="px-3 py-1.5 text-xs font-bold rounded-lg ring-1 ring-inset" :class="cfg.cls">
                  <span class="inline-block w-2 h-2 rounded-full mr-1" :class="cfg.dot"></span>
                  {{ cfg.label }}
                </span>
              </div>
              <div class="text-slate-300 font-medium">
                <span class="font-bold text-white">{{ contract.employee }}</span> ({{ contract.employeeCode }})
                · {{ contract.contractType }}
              </div>
              <div class="flex flex-wrap gap-x-6 gap-y-1 mt-3 text-sm text-slate-400 font-medium">
                <span>📁 {{ contract.orgUnit }}</span>
                <span>💼 {{ contract.jobTitle }}</span>
                <span>📅 {{ contract.startDate }} → {{ contract.endDate }}</span>
              </div>
            </div>
            <!-- Action Buttons -->
            <div class="flex flex-wrap gap-2">
              <button v-if="contract.status === 'DRAFT'"
                class="flex items-center gap-2 bg-amber-400 hover:bg-amber-300 text-amber-900 px-4 py-2.5 rounded-xl font-bold text-sm transition-all">
                <Send class="w-4 h-4" /> Gửi chờ ký
              </button>
              <button v-if="contract.status === 'PENDING_SIGN'"
                class="flex items-center gap-2 bg-emerald-500 hover:bg-emerald-400 text-white px-4 py-2.5 rounded-xl font-bold text-sm transition-all">
                <CheckCircle2 class="w-4 h-4" /> Kích hoạt
              </button>
              <button v-if="contract.status === 'PENDING_SIGN'"
                class="flex items-center gap-2 bg-rose-500 hover:bg-rose-400 text-white px-4 py-2.5 rounded-xl font-bold text-sm transition-all">
                <XCircle class="w-4 h-4" /> Từ chối
              </button>
              <button v-if="contract.status === 'ACTIVE'"
                class="flex items-center gap-2 bg-indigo-500 hover:bg-indigo-400 text-white px-4 py-2.5 rounded-xl font-bold text-sm transition-all">
                <RotateCcw class="w-4 h-4" /> Gia hạn
              </button>
              <button class="flex items-center gap-2 bg-white/10 hover:bg-white/20 text-white px-4 py-2.5 rounded-xl font-bold text-sm transition-all border border-white/20">
                <Download class="w-4 h-4" /> Xuất HTML
              </button>
            </div>
          </div>
        </div>

        <!-- Status Progress Bar -->
        <div class="px-8 py-5 border-b border-slate-100 bg-slate-50/50">
          <div class="flex items-center gap-2">
            <template v-for="(step, i) in statusFlow" :key="step.key">
              <div class="flex items-center gap-2">
                <div class="flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm font-bold"
                  :class="i <= currentStepIdx
                    ? 'bg-indigo-600 text-white'
                    : 'bg-white border border-slate-200 text-slate-400'">
                  <component :is="step.icon" class="w-4 h-4" />
                  {{ step.label }}
                </div>
              </div>
              <div v-if="i < statusFlow.length - 1"
                class="flex-1 h-0.5 rounded-full"
                :class="i < currentStepIdx ? 'bg-indigo-600' : 'bg-slate-200'"></div>
            </template>
          </div>
        </div>
      </div>

      <!-- Tab Navigation -->
      <div class="flex overflow-x-auto bg-white rounded-2xl border border-slate-100 shadow-sm p-1.5 gap-1">
        <button v-for="tab in tabs" :key="tab.key"
          @click="activeTab = tab.key"
          class="px-4 py-2.5 rounded-xl font-bold text-sm whitespace-nowrap transition-all"
          :class="activeTab === tab.key ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-500 hover:bg-slate-50 hover:text-slate-700'">
          {{ tab.label }}
        </button>
      </div>

      <!-- Tab: Detail -->
      <div v-if="activeTab === 'detail'" class="grid md:grid-cols-2 gap-6">
        <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6">
          <h3 class="font-black text-slate-900 text-lg mb-5 pb-3 border-b border-slate-100">Thông tin hợp đồng</h3>
          <div class="space-y-3">
            <div v-for="(val, key) in {
              'Mã hợp đồng': contract.contractCode,
              'Loại hợp đồng': contract.contractType,
              'Ngày hiệu lực': contract.startDate,
              'Ngày hết hạn': contract.endDate,
              'Ngày ký': contract.signDate,
              'Địa điểm làm việc': contract.workLocation,
            }" :key="key" class="flex items-center justify-between py-2 border-b border-slate-50">
              <span class="text-sm text-slate-400 font-bold">{{ key }}</span>
              <span class="text-sm font-bold text-slate-900">{{ val }}</span>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6">
          <h3 class="font-black text-slate-900 text-lg mb-5 pb-3 border-b border-slate-100">Điều khoản lương thưởng</h3>
          <div class="space-y-3">
            <div class="p-4 bg-emerald-50 rounded-2xl border border-emerald-100">
              <div class="text-xs text-emerald-600 font-bold uppercase tracking-wider mb-1">Lương cơ bản (VNĐ)</div>
              <div class="text-3xl font-black text-emerald-700">{{ contract.baseSalary }}</div>
            </div>
            <div class="p-4 bg-indigo-50 rounded-2xl border border-indigo-100">
              <div class="text-xs text-indigo-600 font-bold uppercase tracking-wider mb-1">Phụ cấp (VNĐ)</div>
              <div class="text-3xl font-black text-indigo-700">{{ contract.allowances }}</div>
            </div>
          </div>
          <div class="mt-4 p-4 bg-slate-50 rounded-2xl">
            <p class="text-sm text-slate-500 font-medium italic">"{{ contract.notes }}"</p>
          </div>
        </div>

        <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6 md:col-span-2">
          <h3 class="font-black text-slate-900 text-lg mb-5 pb-3 border-b border-slate-100">Đại diện ký kết</h3>
          <div class="grid grid-cols-2 gap-8">
            <div class="text-center p-6 bg-slate-50 rounded-2xl">
              <div class="text-sm font-bold text-slate-400 mb-1">ĐẠI DIỆN NGƯỜI LAO ĐỘNG</div>
              <div class="text-xl font-black text-slate-900 mt-4">{{ contract.employee }}</div>
              <div class="text-slate-500 font-medium text-sm mt-1">{{ contract.jobTitle }}</div>
            </div>
            <div class="text-center p-6 bg-slate-50 rounded-2xl">
              <div class="text-sm font-bold text-slate-400 mb-1">ĐẠI DIỆN NGƯỜI SỬ DỤNG LAO ĐỘNG</div>
              <div class="text-xl font-black text-slate-900 mt-4">{{ contract.companyRepresentative }}</div>
              <div class="text-slate-500 font-medium text-sm mt-1">{{ contract.companyTitle }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab: Appendix -->
      <div v-if="activeTab === 'appendix'" class="space-y-4">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-black text-slate-900">Phụ lục hợp đồng</h3>
          <button class="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2.5 rounded-xl font-bold text-sm hover:bg-indigo-700 shadow-md shadow-indigo-200">
            <Plus class="w-4 h-4" /> Tạo phụ lục
          </button>
        </div>
        <div v-for="app in appendices" :key="app.id"
          class="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <div class="flex items-start justify-between">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center">
                <FileSignature class="w-6 h-6 text-indigo-600" />
              </div>
              <div>
                <div class="flex items-center gap-2 mb-1">
                  <h4 class="font-black text-slate-900">{{ app.appendixCode }}</h4>
                  <span class="px-2 py-0.5 text-xs font-bold rounded-md bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200">
                    {{ app.status === 'ACTIVE' ? 'Hiệu lực' : 'Đã hủy' }}
                  </span>
                </div>
                <p class="text-slate-600 font-medium text-sm">{{ app.title }}</p>
                <div class="flex gap-4 mt-2 text-xs text-slate-400 font-medium">
                  <span>🗓 Hiệu lực: {{ app.effectiveDate }}</span>
                  <span>💰 Điều chỉnh lương: <span class="font-bold text-emerald-600">{{ app.salaryChange }}</span></span>
                  <span>✍️ Ký bởi: {{ app.signedBy }}</span>
                </div>
              </div>
            </div>
            <div class="flex gap-2">
              <button class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"><Download class="w-4 h-4" /></button>
              <button class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"><XCircle class="w-4 h-4" /></button>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab: Attachments -->
      <div v-if="activeTab === 'attachments'" class="space-y-4">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-black text-slate-900">Tài liệu đính kèm</h3>
          <button class="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2.5 rounded-xl font-bold text-sm hover:bg-indigo-700 shadow-md shadow-indigo-200">
            <Paperclip class="w-4 h-4" /> Đính kèm tài liệu
          </button>
        </div>
        <div class="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
          <table class="w-full">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-100">
                <th class="py-3 px-5 text-left font-bold text-slate-500 text-xs uppercase tracking-wider">Tên tài liệu</th>
                <th class="py-3 px-5 text-left font-bold text-slate-500 text-xs uppercase tracking-wider">Kích thước</th>
                <th class="py-3 px-5 text-left font-bold text-slate-500 text-xs uppercase tracking-wider">Ngày upload</th>
                <th class="py-3 px-5"></th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="att in attachments" :key="att.id" class="hover:bg-slate-50/50 transition-colors">
                <td class="py-4 px-5">
                  <div class="flex items-center gap-3">
                    <div class="w-9 h-9 bg-indigo-50 rounded-xl flex items-center justify-center">
                      <Paperclip class="w-4 h-4 text-indigo-600" />
                    </div>
                    <span class="font-bold text-slate-900 text-sm">{{ att.name }}</span>
                  </div>
                </td>
                <td class="py-4 px-5 text-sm text-slate-500 font-medium">{{ att.size }}</td>
                <td class="py-4 px-5 text-sm text-slate-500 font-medium">{{ att.uploadedAt }}</td>
                <td class="py-4 px-5">
                  <div class="flex items-center justify-end gap-2">
                    <button class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"><Download class="w-4 h-4" /></button>
                    <button class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"><Trash2 class="w-4 h-4" /></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Tab: History -->
      <div v-if="activeTab === 'history'" class="space-y-4">
        <h3 class="text-lg font-black text-slate-900">Lịch sử thay đổi trạng thái</h3>
        <div class="relative">
          <!-- Timeline line -->
          <div class="absolute left-6 top-4 bottom-4 w-0.5 bg-slate-200"></div>
          <div class="space-y-4">
            <div v-for="h in history" :key="h.id" class="relative flex items-start gap-4 pl-14">
              <!-- Timeline dot -->
              <div class="absolute left-4 w-5 h-5 rounded-full border-2 border-white shadow-md flex items-center justify-center"
                :class="h.toStatus === 'ACTIVE' ? 'bg-emerald-500' : h.toStatus === 'PENDING_SIGN' ? 'bg-amber-400' : 'bg-indigo-500'">
              </div>
              <!-- Content -->
              <div class="flex-1 bg-white rounded-2xl border border-slate-100 shadow-sm p-4">
                <div class="flex items-center gap-3 mb-2">
                  <span v-if="h.fromStatus" class="text-xs font-bold px-2 py-0.5 bg-slate-100 text-slate-500 rounded">{{ h.fromStatus }}</span>
                  <span v-if="h.fromStatus" class="text-slate-400">→</span>
                  <span class="text-xs font-bold px-2 py-0.5 rounded"
                    :class="h.toStatus === 'ACTIVE' ? 'bg-emerald-100 text-emerald-700' : h.toStatus === 'PENDING_SIGN' ? 'bg-amber-100 text-amber-700' : 'bg-indigo-100 text-indigo-700'">
                    {{ h.toStatus }}
                  </span>
                </div>
                <p class="text-slate-600 text-sm font-medium">"{{ h.note }}"</p>
                <div class="flex items-center gap-3 mt-2 text-xs text-slate-400 font-medium">
                  <span>👤 {{ h.actor }}</span>
                  <span>🕐 {{ h.timestamp }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>
