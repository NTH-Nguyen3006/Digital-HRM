<script setup>
import { ref, computed } from 'vue'
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'
import {
  UserPlus, Search, Plus, CheckCircle2, Clock, Users,
  FileText, Laptop, KeyRound, Mail, ClipboardList,
  ChevronRight, AlertCircle, CheckCheck, Eye,
  CalendarDays, Building2, Briefcase, ArrowRight,
  BadgeCheck, UserCheck, Send, FilePlus2, ListChecks
} from 'lucide-vue-next'
import {
  getOnboardings, getOnboardingDetail,
  completeOnboarding, notifyOnboarding,
  createUserForOnboarding, createInitialContract
} from '@/api/admin/onboarding.js'

// =================== WORKFLOW ===================
// Backend OnboardingStatus: DRAFT → IN_PROGRESS → READY_FOR_JOIN → COMPLETED | CANCELLED
//
// Các bước thực tế:
// 1. DRAFT          - Tạo hồ sơ onboarding (HR tạo ~ ứng viên được nhận)
// 2. IN_PROGRESS     - Đang chuẩn bị: cập nhật checklist, tài liệu đầu vào, tài sản cấp phát
//                     + Xác nhận orientation (Manager)
// 3. READY_FOR_JOIN  - Tạo tài khoản user + hợp đồng đầu tiên (HR) → Sẵn sàng đi làm
// 4. COMPLETED       - Hoàn tất onboarding (tất cả checklist bắt buộc xong, hợp đồng ACTIVE)

const statusConfig = {
  DRAFT:          { label: 'Nháp',           color: 'slate',   dot: 'bg-slate-400',   badge: 'bg-slate-100 text-slate-700 border-slate-200',   order: 0 },
  IN_PROGRESS:    { label: 'Đang chuẩn bị',  color: 'amber',   dot: 'bg-amber-400',   badge: 'bg-amber-100 text-amber-700 border-amber-200',    order: 1 },
  READY_FOR_JOIN: { label: 'Sẵn sàng làm',  color: 'indigo',  dot: 'bg-indigo-400',  badge: 'bg-indigo-100 text-indigo-700 border-indigo-200', order: 2 },
  COMPLETED:      { label: 'Hoàn tất',       color: 'emerald', dot: 'bg-emerald-400', badge: 'bg-emerald-100 text-emerald-700 border-emerald-200', order: 3 },
  CANCELLED:      { label: 'Đã hủy',         color: 'rose',    dot: 'bg-rose-400',    badge: 'bg-rose-100 text-rose-700 border-rose-200',       order: -1 },
}

const stepDefs = [
  { key: 'checklist',    icon: ListChecks,   label: 'Checklist chuẩn bị',         desc: 'Cấp phát tài sản, tài liệu đầu vào',  status: 'IN_PROGRESS' },
  { key: 'orientation',  icon: BadgeCheck,   label: 'Xác nhận Orientation',       desc: 'Manager xác nhận định hướng nhập môn', status: 'IN_PROGRESS' },
  { key: 'account',      icon: KeyRound,     label: 'Tạo tài khoản hệ thống',     desc: 'Cấp username / email công ty',          status: 'READY_FOR_JOIN' },
  { key: 'contract',     icon: FilePlus2,    label: 'Tạo hợp đồng đầu tiên',      desc: 'Hợp đồng thử việc / chính thức',        status: 'READY_FOR_JOIN' },
  { key: 'notify',       icon: Send,         label: 'Gửi thông báo chào mừng',   desc: 'Email Welcome đến ứng viên & IT Desk',  status: 'READY_FOR_JOIN' },
  { key: 'complete',     icon: CheckCheck,   label: 'Chốt hoàn tất Onboarding',  desc: 'Tất cả checklist bắt buộc phải xong',   status: 'COMPLETED' },
]

// =================== STATE ===================
const searchQuery = ref('')
const activeStatus = ref('ALL')
const selectedRecord = ref(null)
const showDetailPanel = ref(false)

const stats = [
  { title: 'Đang chuẩn bị', value: '6', icon: Clock, color: 'amber', trend: 2, trendLabel: 'so với tháng trước' },
  { title: 'Sẵn sàng làm việc', value: '3', icon: UserCheck, color: 'indigo', trend: 1, trendLabel: 'tuần này' },
  { title: 'Hoàn tất tháng này', value: '11', icon: CheckCircle2, color: 'emerald', trend: 4, trendLabel: 'so với tháng trước' },
  { title: 'Nhân viên mới tháng 4', value: '8', icon: UserPlus, color: 'rose' },
]

const records = ref([
  {
    id: 1, code: 'ONB-2604-001', name: 'Hoàng Văn Khải', email: 'khai.hv@personal.com',
    phone: '0901 234 567', gender: 'MALE', dept: 'Engineering', deptId: 1,
    jobTitle: 'Frontend Developer', plannedStartDate: '01/04/2026',
    status: 'IN_PROGRESS', manager: 'Lê Quang Nam',
    linkedEmployee: null, linkedUser: null, firstContract: null,
    orientationConfirmedAt: null,
    checklist: [
      { id: 1, name: 'Cấp Laptop', required: true, completed: true },
      { id: 2, name: 'Cấp Badge ID', required: true, completed: false },
      { id: 3, name: 'Đăng ký email công ty', required: true, completed: false },
    ],
    documents: [{ id: 1, name: 'CCCD', status: 'RECEIVED' }],
    assets: [{ id: 1, name: 'Dell XPS 15', status: 'ASSIGNED' }],
  },
  {
    id: 2, code: 'ONB-2604-002', name: 'Đoàn Thị Lan Anh', email: 'lananh.dt@gmail.com',
    phone: '0912 345 678', gender: 'FEMALE', dept: 'Marketing', deptId: 2,
    jobTitle: 'Marketing Specialist', plannedStartDate: '15/04/2026',
    status: 'DRAFT', manager: 'Nguyễn Trà My',
    linkedEmployee: null, linkedUser: null, firstContract: null,
    orientationConfirmedAt: null,
    checklist: [], documents: [], assets: [],
  },
  {
    id: 3, code: 'ONB-2604-003', name: 'Trần Văn Bình', email: 'binh.tv@gmail.com',
    phone: '0978 901 234', gender: 'MALE', dept: 'Finance', deptId: 3,
    jobTitle: 'Senior Accountant', plannedStartDate: '01/04/2026',
    status: 'READY_FOR_JOIN', manager: 'Phạm Thị Hà',
    linkedEmployee: { id: 10, code: 'EMP-0099' }, linkedUser: { id: 'uuid-001' },
    firstContract: { id: 5, status: 'ACTIVE' },
    orientationConfirmedAt: '2026-03-28 09:00',
    checklist: [
      { id: 1, name: 'Cấp Laptop', required: true, completed: true },
      { id: 2, name: 'Badge ID', required: true, completed: true },
    ],
    documents: [
      { id: 1, name: 'CCCD', status: 'RECEIVED' },
      { id: 2, name: 'Bằng cấp', status: 'RECEIVED' },
    ],
    assets: [{ id: 1, name: 'MacBook Pro', status: 'ASSIGNED' }],
  },
  {
    id: 4, code: 'ONB-2603-015', name: 'Nguyễn Minh Phúc', email: 'phuc.nm@gmail.com',
    phone: '0969 112 233', gender: 'MALE', dept: 'Sales', deptId: 4,
    jobTitle: 'Sales Executive', plannedStartDate: '10/03/2026',
    status: 'COMPLETED', manager: 'Võ Thanh Hùng',
    linkedEmployee: { id: 8, code: 'EMP-0088' }, linkedUser: { id: 'uuid-002' },
    firstContract: { id: 3, status: 'ACTIVE' },
    orientationConfirmedAt: '2026-03-08 10:00',
    checklist: [
      { id: 1, name: 'Cấp Laptop', required: true, completed: true },
      { id: 2, name: 'Badge ID', required: true, completed: true },
    ],
    documents: [], assets: [],
  },
])

// =================== COMPUTED ===================
const statusTabs = [
  { key: 'ALL', label: 'Tất cả' },
  { key: 'DRAFT', label: 'Nháp' },
  { key: 'IN_PROGRESS', label: 'Đang chuẩn bị' },
  { key: 'READY_FOR_JOIN', label: 'Sẵn sàng làm' },
  { key: 'COMPLETED', label: 'Hoàn tất' },
]

const filtered = computed(() => {
  return records.value.filter(r => {
    const q = searchQuery.value.toLowerCase()
    const matchQ = !q || r.name.toLowerCase().includes(q) || r.code.toLowerCase().includes(q) || r.dept.toLowerCase().includes(q)
    const matchS = activeStatus.value === 'ALL' || r.status === activeStatus.value
    return matchQ && matchS
  })
})

const avatarColors = ['bg-indigo-500', 'bg-emerald-500', 'bg-amber-500', 'bg-violet-500', 'bg-sky-500', 'bg-rose-500']
function avatarColor(name) { return avatarColors[name.charCodeAt(0) % avatarColors.length] }
function initials(name) { return name.split(' ').slice(-2).map(w => w[0]).join('') }

function getStepState(record, step) {
  const statusOrder = ['DRAFT', 'IN_PROGRESS', 'READY_FOR_JOIN', 'COMPLETED', 'CANCELLED']
  const currentIdx = statusOrder.indexOf(record.status)
  const requiredIdx = statusOrder.indexOf(step.status)
  // Special logic per step key
  if (step.key === 'checklist') return record.checklist.length > 0 ? 'done' : (currentIdx >= 1 ? 'active' : 'pending')
  if (step.key === 'orientation') return record.orientationConfirmedAt ? 'done' : (currentIdx >= 1 ? 'active' : 'pending')
  if (step.key === 'account') return record.linkedUser ? 'done' : (currentIdx >= 2 ? 'active' : 'pending')
  if (step.key === 'contract') return record.firstContract ? 'done' : (currentIdx >= 2 ? 'active' : 'pending')
  if (step.key === 'notify') return currentIdx >= 2 ? 'active' : 'pending'
  if (step.key === 'complete') return record.status === 'COMPLETED' ? 'done' : (currentIdx >= 2 ? 'active' : 'pending')
  return 'pending'
}

function openDetail(record) {
  selectedRecord.value = record
  showDetailPanel.value = true
}

async function handleComplete(record) {
  try {
    await completeOnboarding(record.id)
    record.status = 'COMPLETED'
  } catch (_) {
    record.status = 'COMPLETED' // demo
  }
}

async function handleNotify(record) {
  try {
    await notifyOnboarding(record.id, { notifyNewHire: true, notifyManager: true })
  } catch (_) {}
}
</script>

<template>
  <MainLayout>
    <div class="space-y-8">

      <!-- ===== HEADER ===== -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div class="flex items-center gap-3 mb-1">
            <div class="w-10 h-10 rounded-2xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-200">
              <UserPlus class="w-5 h-5 text-white" />
            </div>
            <h2 class="text-3xl font-black text-slate-900 tracking-tight">Tiếp nhận nhân sự</h2>
          </div>
          <p class="text-slate-500 font-medium ml-[52px]">Quản lý quy trình Onboarding từ khi nhận hồ sơ đến khi hoàn tất nhập môn</p>
        </div>
        <button class="flex items-center gap-2 bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 text-sm flex-shrink-0">
          <Plus class="w-4 h-4" /> Tạo hồ sơ Onboarding
        </button>
      </div>

      <!-- ===== WORKFLOW DIAGRAM ===== -->
      <GlassCard :glass="false" class="bg-white border border-slate-100 rounded-3xl">
        <h3 class="text-sm font-bold text-slate-500 uppercase tracking-wider mb-4">Quy trình nghiệp vụ</h3>
        <div class="flex items-start gap-2 overflow-x-auto pb-2">
          <template v-for="(step, idx) in [
            { label: 'Tạo hồ sơ', sublabel: 'DRAFT', icon: FilePlus2, color: 'slate' },
            { label: 'Chuẩn bị & Orientation', sublabel: 'IN_PROGRESS', icon: ListChecks, color: 'amber' },
            { label: 'Tạo TK & Hợp đồng', sublabel: 'READY_FOR_JOIN', icon: KeyRound, color: 'indigo' },
            { label: 'Hoàn tất', sublabel: 'COMPLETED', icon: CheckCheck, color: 'emerald' },
          ]" :key="step.sublabel">
            <div class="flex flex-col items-center min-w-[130px]">
              <div class="w-12 h-12 rounded-2xl flex items-center justify-center mb-2 shadow-sm"
                :class="step.color === 'slate' ? 'bg-slate-100 text-slate-500' : step.color === 'amber' ? 'bg-amber-100 text-amber-600' : step.color === 'indigo' ? 'bg-indigo-100 text-indigo-600' : 'bg-emerald-100 text-emerald-600'">
                <component :is="step.icon" class="w-5 h-5" />
              </div>
              <div class="text-xs font-bold text-slate-700 text-center leading-tight">{{ step.label }}</div>
              <div class="text-[10px] text-slate-400 mt-0.5">{{ step.sublabel }}</div>
            </div>
            <ArrowRight v-if="idx < 3" class="w-5 h-5 text-slate-300 mt-3 flex-shrink-0" />
          </template>
        </div>
        <p class="text-xs text-slate-400 mt-3 border-t border-slate-100 pt-3">
          ⚡ Điều kiện chốt: Orientation đã xác nhận + toàn bộ checklist bắt buộc hoàn tất + hợp đồng đầu tiên ở trạng thái ACTIVE
        </p>
      </GlassCard>

      <!-- ===== STAT CARDS ===== -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard v-for="s in stats" :key="s.title" :title="s.title" :value="s.value" :icon="s.icon" :color="s.color" :trend="s.trend" :trendLabel="s.trendLabel" />
      </div>

      <!-- ===== LIST PANEL ===== -->
      <GlassCard :glass="false" padding="p-0" class="bg-white border border-slate-100 rounded-3xl overflow-hidden">

        <!-- Toolbar -->
        <div class="px-6 pt-5 pb-4 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center gap-4">
          <!-- Status tabs -->
          <div class="flex gap-1 overflow-x-auto">
            <button v-for="tab in statusTabs" :key="tab.key"
              @click="activeStatus = tab.key"
              class="px-3 py-1.5 rounded-xl text-sm font-semibold transition-all whitespace-nowrap"
              :class="activeStatus === tab.key ? 'bg-indigo-600 text-white shadow' : 'text-slate-500 hover:bg-slate-100'"
            >{{ tab.label }}</button>
          </div>
          <!-- Search -->
          <div class="relative ml-auto">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input v-model="searchQuery" type="text" placeholder="Tìm tên, mã, phòng ban..."
              class="pl-9 pr-4 py-2 text-sm rounded-xl border border-slate-200 bg-slate-50 focus:outline-none focus:ring-2 focus:ring-indigo-300 w-52" />
          </div>
        </div>

        <!-- Records -->
        <div class="divide-y divide-slate-50">
          <div v-for="rec in filtered" :key="rec.id"
            class="group p-5 hover:bg-slate-50/60 transition-colors cursor-pointer"
            @click="openDetail(rec)">
            <div class="flex flex-col md:flex-row md:items-center gap-4">

              <!-- Avatar + Info -->
              <div class="flex items-start gap-4 flex-1 min-w-0">
                <div class="w-11 h-11 rounded-2xl flex items-center justify-center text-white text-sm font-black shadow flex-shrink-0"
                  :class="avatarColor(rec.name)">
                  {{ initials(rec.name) }}
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex flex-wrap items-center gap-2 mb-1.5">
                    <span class="font-bold text-slate-900">{{ rec.name }}</span>
                    <span class="text-xs text-slate-400 bg-slate-100 px-2 py-0.5 rounded-lg">{{ rec.code }}</span>
                    <span class="flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold border"
                      :class="statusConfig[rec.status]?.badge">
                      <span class="w-1.5 h-1.5 rounded-full" :class="statusConfig[rec.status]?.dot" />
                      {{ statusConfig[rec.status]?.label }}
                    </span>
                  </div>
                  <div class="grid grid-cols-2 sm:grid-cols-4 gap-x-4 gap-y-1 text-sm">
                    <div class="flex items-center gap-1 text-slate-500">
                      <Building2 class="w-3.5 h-3.5" /> <span>{{ rec.dept }}</span>
                    </div>
                    <div class="flex items-center gap-1 text-slate-500">
                      <Briefcase class="w-3.5 h-3.5" /> <span class="truncate">{{ rec.jobTitle }}</span>
                    </div>
                    <div class="flex items-center gap-1 text-slate-500">
                      <CalendarDays class="w-3.5 h-3.5" />
                      <span>Dự kiến: <strong class="text-indigo-600">{{ rec.plannedStartDate }}</strong></span>
                    </div>
                    <div class="flex items-center gap-1 text-slate-500">
                      <Users class="w-3.5 h-3.5" /> <span>{{ rec.manager }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Mini progress steps -->
              <div class="flex items-center gap-1.5 flex-shrink-0">
                <div v-for="step in stepDefs" :key="step.key"
                  class="w-6 h-6 rounded-lg flex items-center justify-center"
                  :class="{
                    'bg-emerald-100 text-emerald-600': getStepState(rec, step) === 'done',
                    'bg-indigo-100 text-indigo-600 ring-2 ring-indigo-300': getStepState(rec, step) === 'active',
                    'bg-slate-100 text-slate-300': getStepState(rec, step) === 'pending',
                  }"
                  :title="step.label">
                  <component :is="step.icon" class="w-3.5 h-3.5" />
                </div>
                <button class="ml-2 p-1.5 rounded-lg text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 transition-colors opacity-0 group-hover:opacity-100">
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          <div v-if="filtered.length === 0" class="py-16 text-center">
            <UserPlus class="w-12 h-12 text-slate-300 mx-auto mb-3" />
            <p class="text-slate-500 font-medium">Không có hồ sơ nào phù hợp</p>
          </div>
        </div>

        <!-- Footer -->
        <div class="border-t border-slate-100 px-6 py-3 flex items-center justify-between text-sm text-slate-500">
          <span>{{ filtered.length }} hồ sơ</span>
          <div class="flex gap-1">
            <button class="px-3 py-1 rounded-lg hover:bg-slate-50 font-medium">←</button>
            <button class="px-3 py-1 rounded-lg bg-indigo-600 text-white font-bold">1</button>
            <button class="px-3 py-1 rounded-lg hover:bg-slate-50 font-medium">→</button>
          </div>
        </div>
      </GlassCard>
    </div>

    <!-- ===== DETAIL SIDE PANEL ===== -->
    <Teleport to="body">
      <Transition name="slide">
        <div v-if="showDetailPanel && selectedRecord" class="fixed inset-0 z-50 flex justify-end">
          <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showDetailPanel = false" />
          <div class="relative bg-white w-full max-w-xl h-full overflow-y-auto shadow-2xl flex flex-col">

            <!-- Panel header -->
            <div class="bg-gradient-to-r from-indigo-600 to-indigo-700 p-6 text-white flex-shrink-0">
              <div class="flex items-center justify-between mb-4">
                <span class="text-sm font-semibold text-indigo-200">{{ selectedRecord.code }}</span>
                <button @click="showDetailPanel = false" class="w-8 h-8 rounded-xl bg-white/20 hover:bg-white/30 flex items-center justify-center text-lg font-bold transition-colors">×</button>
              </div>
              <div class="flex items-center gap-4">
                <div class="w-14 h-14 rounded-2xl bg-white/20 flex items-center justify-center text-xl font-black">
                  {{ initials(selectedRecord.name) }}
                </div>
                <div>
                  <h3 class="text-xl font-black">{{ selectedRecord.name }}</h3>
                  <p class="text-indigo-200 text-sm">{{ selectedRecord.jobTitle }} · {{ selectedRecord.dept }}</p>
                  <p class="text-indigo-200 text-sm mt-0.5">📅 Ngày làm: <strong class="text-white">{{ selectedRecord.plannedStartDate }}</strong></p>
                </div>
              </div>
            </div>

            <!-- Panel body -->
            <div class="flex-1 p-6 space-y-6 overflow-y-auto">

              <!-- Status badge -->
              <div class="flex items-center gap-3">
                <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-bold border"
                  :class="statusConfig[selectedRecord.status]?.badge">
                  <span class="w-2 h-2 rounded-full" :class="statusConfig[selectedRecord.status]?.dot" />
                  {{ statusConfig[selectedRecord.status]?.label }}
                </span>
                <span class="text-slate-400 text-sm">Quản lý: <strong class="text-slate-700">{{ selectedRecord.manager }}</strong></span>
              </div>

              <!-- Step actions -->
              <div>
                <h4 class="font-bold text-slate-700 mb-3 flex items-center gap-2">
                  <ListChecks class="w-4 h-4 text-indigo-600" /> Các bước hành động
                </h4>
                <div class="space-y-2">
                  <div v-for="step in stepDefs" :key="step.key"
                    class="flex items-center justify-between p-3 rounded-xl border transition-all"
                    :class="{
                      'bg-emerald-50 border-emerald-200': getStepState(selectedRecord, step) === 'done',
                      'bg-indigo-50 border-indigo-200': getStepState(selectedRecord, step) === 'active',
                      'bg-slate-50 border-slate-200 opacity-60': getStepState(selectedRecord, step) === 'pending',
                    }">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded-xl flex items-center justify-center"
                        :class="{
                          'bg-emerald-100 text-emerald-600': getStepState(selectedRecord, step) === 'done',
                          'bg-indigo-100 text-indigo-600': getStepState(selectedRecord, step) === 'active',
                          'bg-slate-100 text-slate-400': getStepState(selectedRecord, step) === 'pending',
                        }">
                        <CheckCircle2 v-if="getStepState(selectedRecord, step) === 'done'" class="w-4 h-4" />
                        <component :is="step.icon" v-else class="w-4 h-4" />
                      </div>
                      <div>
                        <div class="text-sm font-bold" :class="getStepState(selectedRecord, step) === 'done' ? 'text-emerald-700' : 'text-slate-800'">
                          {{ step.label }}
                        </div>
                        <div class="text-xs text-slate-400">{{ step.desc }}</div>
                      </div>
                    </div>
                    <div>
                      <span v-if="getStepState(selectedRecord, step) === 'done'"
                        class="text-xs font-bold text-emerald-600 bg-emerald-100 px-2 py-1 rounded-lg">Xong</span>
                      <button v-else-if="getStepState(selectedRecord, step) === 'active'"
                        class="text-xs font-bold text-indigo-600 bg-indigo-100 hover:bg-indigo-200 px-3 py-1.5 rounded-xl transition-colors"
                        @click.stop>Thực hiện →</button>
                      <span v-else class="text-xs text-slate-400">Chờ...</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Checklist summary -->
              <div v-if="selectedRecord.checklist.length > 0">
                <h4 class="font-bold text-slate-700 mb-2 flex items-center gap-2">
                  <ClipboardList class="w-4 h-4 text-indigo-600" /> Checklist ({{ selectedRecord.checklist.filter(c=>c.completed).length }}/{{ selectedRecord.checklist.length }})
                </h4>
                <div class="space-y-1.5">
                  <div v-for="item in selectedRecord.checklist" :key="item.id"
                    class="flex items-center gap-2 text-sm">
                    <div class="w-4 h-4 rounded flex-shrink-0" :class="item.completed ? 'bg-emerald-500 text-white flex items-center justify-center' : 'border-2 border-slate-300'">
                      <span v-if="item.completed" class="text-[10px]">✓</span>
                    </div>
                    <span :class="item.completed ? 'text-slate-500 line-through' : 'text-slate-800'">{{ item.name }}</span>
                    <span v-if="item.required && !item.completed" class="text-[10px] text-rose-500 font-bold">BẮT BUỘC</span>
                  </div>
                </div>
              </div>

              <!-- Action buttons by status -->
              <div class="space-y-2 pt-2 border-t border-slate-100">
                <button v-if="selectedRecord.status === 'READY_FOR_JOIN'"
                  @click="handleNotify(selectedRecord)"
                  class="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-violet-600 text-white font-bold hover:bg-violet-700 transition text-sm">
                  <Send class="w-4 h-4" /> Gửi email chào mừng
                </button>
                <button v-if="selectedRecord.status === 'READY_FOR_JOIN'"
                  @click="handleComplete(selectedRecord)"
                  class="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-emerald-600 text-white font-bold hover:bg-emerald-700 transition text-sm">
                  <CheckCheck class="w-4 h-4" /> Chốt hoàn tất Onboarding
                </button>
                <button v-if="selectedRecord.status === 'COMPLETED'"
                  class="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-slate-100 text-slate-500 font-semibold text-sm cursor-default">
                  <CheckCircle2 class="w-4 h-4 text-emerald-500" /> Đã hoàn tất quy trình
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </MainLayout>
</template>

<style scoped>
.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; }
.slide-enter-from .relative, .slide-leave-to .relative { transform: translateX(100%); }
</style>
