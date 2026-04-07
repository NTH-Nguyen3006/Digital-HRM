<script setup>
import { ref, computed } from 'vue'
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'
import {
  UserMinus, Search, Clock, AlertTriangle, CheckCircle2,
  ArrowRight, FileText, MonitorOff, Wallet, ShieldOff,
  Archive, Users, ChevronRight, Building2, CalendarDays,
  BadgeX, ListChecks, ClipboardCheck, Banknote, X,
  CheckCheck, Eye, History
} from 'lucide-vue-next'
import {
  getOffboardings, getOffboardingDetail,
  finalizeOffboarding, revokeAccess,
  processSettlement, closeOffboarding,
  createAssetReturn, updateAssetReturn
} from '@/api/admin/offboarding.js'

// =================== WORKFLOW ===================
// Backend OffboardingStatus flow:
// REQUESTED → (Manager) MANAGER_APPROVED | MANAGER_REJECTED
// MANAGER_APPROVED → (HR) HR_FINALIZED (chốt ngày nghỉ)
// HR_FINALIZED → ACCESS_REVOKED (Thu hồi quyền truy cập) + SETTLEMENT_PREPARED (Chuẩn bị thanh toán) [song song]
// ACCESS_REVOKED | SETTLEMENT_PREPARED → CLOSED (Đóng hồ sơ)
// CLOSED: EmploymentStatus = RESIGNED, hợp đồng = TERMINATED

const statusConfig = {
  REQUESTED:           { label: 'Chờ quản lý duyệt', badge: 'bg-amber-100 text-amber-700 border-amber-200',     dot: 'bg-amber-400' },
  MANAGER_APPROVED:    { label: 'Quản lý đã duyệt',   badge: 'bg-indigo-100 text-indigo-700 border-indigo-200',  dot: 'bg-indigo-400' },
  MANAGER_REJECTED:    { label: 'Quản lý từ chối',    badge: 'bg-rose-100 text-rose-700 border-rose-200',        dot: 'bg-rose-400' },
  HR_FINALIZED:        { label: 'HR đã chốt',          badge: 'bg-violet-100 text-violet-700 border-violet-200', dot: 'bg-violet-400' },
  ACCESS_REVOKED:      { label: 'Thu hồi truy cập',   badge: 'bg-orange-100 text-orange-700 border-orange-200',  dot: 'bg-orange-400' },
  SETTLEMENT_PREPARED: { label: 'Đã chuẩn bị T.toán', badge: 'bg-sky-100 text-sky-700 border-sky-200',          dot: 'bg-sky-400' },
  CLOSED:              { label: 'Đã đóng',             badge: 'bg-slate-100 text-slate-600 border-slate-200',    dot: 'bg-slate-400' },
  CANCELLED:           { label: 'Đã hủy',              badge: 'bg-rose-100 text-rose-600 border-rose-200',       dot: 'bg-rose-300' },
}

// =================== STATE ===================
const searchQuery = ref('')
const activeStatus = ref('ALL')
const selectedRecord = ref(null)
const showPanel = ref(false)

const stats = [
  { title: 'Chờ duyệt', value: '4', icon: Clock, color: 'amber', trend: 1, trendLabel: 'tuần này' },
  { title: 'Đang xử lý', value: '7', icon: AlertTriangle, color: 'rose', trend: -1, trendLabel: 'so với tháng trước' },
  { title: 'Đã đóng tháng này', value: '5', icon: CheckCircle2, color: 'emerald', trend: 2, trendLabel: 'so với tháng trước' },
  { title: 'Nghỉ việc năm nay', value: '23', icon: UserMinus, color: 'indigo' },
]

const records = ref([
  {
    id: 1, code: 'OFF-ABC123XYZ456',
    employeeId: 10, employeeCode: 'EMP-0041', name: 'Ngô Văn Minh',
    dept: 'Operations', manager: 'Trần Thị Thu',
    requestDate: '28/03/2026', requestedLastWorkingDate: '15/04/2026',
    effectiveLastWorkingDate: '15/04/2026',
    requestReason: 'Muốn chuyển sang hướng khác, xin nghỉ việc tự nguyện.',
    status: 'HR_FINALIZED',
    managerReviewNote: 'Đồng ý, nhân viên làm việc tốt.',
    managerReviewedAt: '29/03/2026 09:00',
    hrFinalizeNote: 'Đã chốt ngày 15/04/2026.',
    hrFinalizedAt: '30/03/2026 14:00',
    accessRevokedAt: null, settlementPreparedAt: null, closedAt: null,
    checklistItems: [
      { id: 1, name: 'Bàn giao tài liệu dự án', owner: 'EMPLOYEE', status: 'IN_PROGRESS', dueDate: '10/04/2026' },
      { id: 2, name: 'Bàn giao máy tính MacBook', owner: 'EMPLOYEE', status: 'OPEN', dueDate: '14/04/2026' },
      { id: 3, name: 'Xóa tài khoản GitHub', owner: 'MANAGER', status: 'OPEN', dueDate: '15/04/2026' },
    ],
    assetReturns: [
      { id: 1, name: 'MacBook Pro 14"', code: 'ASSET-0012', status: 'PENDING', expectedReturnDate: '14/04/2026' },
      { id: 2, name: 'Badge ID', code: 'ASSET-0087', status: 'PENDING', expectedReturnDate: '15/04/2026' },
    ],
    leaveSettlementUnits: null, leaveSettlementAmount: null,
    histories: [
      { from: null, to: 'REQUESTED', action: 'REQUEST_SUBMIT', note: 'Tự nguyện xin nghỉ.', changedBy: 'ngo.van.minh', changedAt: '28/03/2026 08:30' },
      { from: 'REQUESTED', to: 'MANAGER_APPROVED', action: 'MANAGER_APPROVE', note: 'Đồng ý.', changedBy: 'tran.thi.thu', changedAt: '29/03/2026 09:00' },
      { from: 'MANAGER_APPROVED', to: 'HR_FINALIZED', action: 'HR_FINALIZE', note: 'Chốt ngày 15/04.', changedBy: 'hr.admin', changedAt: '30/03/2026 14:00' },
    ],
  },
  {
    id: 2, code: 'OFF-DEF456GHI789',
    employeeId: 22, employeeCode: 'EMP-0055', name: 'Lê Thị Phương Linh',
    dept: 'Finance', manager: 'Nguyễn Văn Hùng',
    requestDate: '01/04/2026', requestedLastWorkingDate: '30/04/2026',
    effectiveLastWorkingDate: null,
    requestReason: 'Nghỉ theo nguyện vọng cá nhân, chăm sóc gia đình.',
    status: 'REQUESTED',
    managerReviewNote: null, managerReviewedAt: null,
    hrFinalizeNote: null, hrFinalizedAt: null,
    accessRevokedAt: null, settlementPreparedAt: null, closedAt: null,
    checklistItems: [], assetReturns: [], leaveSettlementUnits: null, leaveSettlementAmount: null,
    histories: [
      { from: null, to: 'REQUESTED', action: 'REQUEST_SUBMIT', note: 'Xin nghỉ vì việc gia đình.', changedBy: 'le.thi.phuong.linh', changedAt: '01/04/2026 10:00' },
    ],
  },
  {
    id: 3, code: 'OFF-XYZ999AAA000',
    employeeId: 5, employeeCode: 'EMP-0012', name: 'Phạm Đức Tài',
    dept: 'Marketing', manager: 'Võ Trà My',
    requestDate: '10/03/2026', requestedLastWorkingDate: '31/03/2026',
    effectiveLastWorkingDate: '31/03/2026',
    requestReason: 'Nhận offer khác tốt hơn.',
    status: 'CLOSED',
    managerReviewNote: 'Tiếc nhưng chấp thuận.', managerReviewedAt: '11/03/2026 10:00',
    hrFinalizeNote: 'OK', hrFinalizedAt: '12/03/2026 16:00',
    accessRevokedAt: '31/03/2026 18:00', settlementPreparedAt: '28/03/2026 09:00',
    closedAt: '01/04/2026 11:00',
    checklistItems: [
      { id: 1, name: 'Bàn giao task Marketing Q2', owner: 'EMPLOYEE', status: 'COMPLETED', dueDate: '30/03/2026' },
    ],
    assetReturns: [
      { id: 1, name: 'Laptop Dell', code: 'ASSET-0031', status: 'RETURNED', expectedReturnDate: '31/03/2026' },
    ],
    leaveSettlementUnits: 3.5, leaveSettlementAmount: 3461539,
    histories: [
      { from: null, to: 'REQUESTED', action: 'REQUEST_SUBMIT', note: 'Nhận offer khác.', changedBy: 'pham.duc.tai', changedAt: '10/03/2026' },
      { from: 'REQUESTED', to: 'MANAGER_APPROVED', action: 'MANAGER_APPROVE', note: 'OK.', changedBy: 'vo.tra.my', changedAt: '11/03/2026' },
      { from: 'MANAGER_APPROVED', to: 'HR_FINALIZED', action: 'HR_FINALIZE', note: 'Chốt.', changedBy: 'hr.admin', changedAt: '12/03/2026' },
      { from: 'HR_FINALIZED', to: 'SETTLEMENT_PREPARED', action: 'PREPARE_SETTLEMENT', note: 'Quyết toán phép.', changedBy: 'hr.admin', changedAt: '28/03/2026' },
      { from: 'SETTLEMENT_PREPARED', to: 'ACCESS_REVOKED', action: 'REVOKE_ACCESS', note: 'Đã khóa TK.', changedBy: 'it.admin', changedAt: '31/03/2026' },
      { from: 'ACCESS_REVOKED', to: 'CLOSED', action: 'CLOSE', note: 'Đóng hồ sơ.', changedBy: 'hr.admin', changedAt: '01/04/2026' },
    ],
  },
])

// =================== COMPUTED ===================
const statusTabs = [
  { key: 'ALL', label: 'Tất cả' },
  { key: 'REQUESTED', label: 'Chờ duyệt' },
  { key: 'MANAGER_APPROVED', label: 'Đã duyệt' },
  { key: 'HR_FINALIZED', label: 'HR đã chốt' },
  { key: 'CLOSED', label: 'Đã đóng' },
]

const actionsForStatus = {
  MANAGER_APPROVED: [
    { key: 'finalize', label: 'Chốt ngày nghỉ (HR Finalize)', icon: ClipboardCheck, class: 'bg-violet-600 text-white hover:bg-violet-700' },
  ],
  HR_FINALIZED: [
    { key: 'revoke', label: 'Thu hồi quyền truy cập', icon: ShieldOff, class: 'bg-orange-500 text-white hover:bg-orange-600' },
    { key: 'settlement', label: 'Chuẩn bị thanh toán cuối', icon: Banknote, class: 'bg-sky-500 text-white hover:bg-sky-600' },
  ],
  ACCESS_REVOKED: [
    { key: 'close', label: 'Đóng hồ sơ hoàn tất', icon: Archive, class: 'bg-slate-700 text-white hover:bg-slate-800' },
  ],
  SETTLEMENT_PREPARED: [
    { key: 'close', label: 'Đóng hồ sơ hoàn tất', icon: Archive, class: 'bg-slate-700 text-white hover:bg-slate-800' },
  ],
}

const filtered = computed(() => {
  return records.value.filter(r => {
    const q = searchQuery.value.toLowerCase()
    const matchQ = !q || r.name.toLowerCase().includes(q) || r.code.toLowerCase().includes(q) || r.dept.toLowerCase().includes(q)
    const matchS = activeStatus.value === 'ALL' || r.status === activeStatus.value
    return matchQ && matchS
  })
})

const avatarColors = ['bg-rose-500', 'bg-indigo-500', 'bg-amber-500', 'bg-violet-500', 'bg-sky-500', 'bg-emerald-500']
function avatarColor(name) { return avatarColors[name.charCodeAt(0) % avatarColors.length] }
function initials(name) { return name.split(' ').slice(-2).map(w => w[0]).join('') }

function openPanel(rec) {
  selectedRecord.value = rec
  showPanel.value = true
}

function fmt(n) { return new Intl.NumberFormat('vi-VN').format(n) + ' ₫' }

const checklistStatusConfig = {
  OPEN:        { label: 'Chưa làm', class: 'bg-slate-100 text-slate-500' },
  IN_PROGRESS: { label: 'Đang làm', class: 'bg-amber-100 text-amber-700' },
  COMPLETED:   { label: 'Hoàn tất', class: 'bg-emerald-100 text-emerald-700' },
}
const assetStatusConfig = {
  PENDING:  { label: 'Chờ trả', class: 'bg-amber-100 text-amber-700' },
  RETURNED: { label: 'Đã trả',  class: 'bg-emerald-100 text-emerald-700' },
  WAIVED:   { label: 'Miễn thu', class: 'bg-slate-100 text-slate-500' },
}

async function handleAction(record, actionKey) {
  try {
    if (actionKey === 'finalize') {
      await finalizeOffboarding(record.id, { effectiveLastWorkingDate: record.requestedLastWorkingDate, note: 'Chốt từ giao diện.' })
      record.status = 'HR_FINALIZED'
    } else if (actionKey === 'revoke') {
      await revokeAccess(record.id, { note: 'Thu hồi từ giao diện.' })
      record.status = 'ACCESS_REVOKED'
      record.accessRevokedAt = new Date().toLocaleString('vi-VN')
    } else if (actionKey === 'settlement') {
      await processSettlement(record.id, { createPayrollDraftIfMissing: true, note: 'Chuẩn bị từ giao diện.' })
      record.status = 'SETTLEMENT_PREPARED'
      record.settlementPreparedAt = new Date().toLocaleString('vi-VN')
    } else if (actionKey === 'close') {
      await closeOffboarding(record.id)
      record.status = 'CLOSED'
      record.closedAt = new Date().toLocaleString('vi-VN')
    }
  } catch (_) {
    // demo fallback already applied above
  }
}
</script>

<template>
  <MainLayout>
    <div class="space-y-8">

      <!-- ===== HEADER ===== -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div class="flex items-center gap-3 mb-1">
            <div class="w-10 h-10 rounded-2xl bg-rose-600 flex items-center justify-center shadow-lg shadow-rose-200">
              <UserMinus class="w-5 h-5 text-white" />
            </div>
            <h2 class="text-3xl font-black text-slate-900 tracking-tight">Thôi việc (Offboarding)</h2>
          </div>
          <p class="text-slate-500 font-medium ml-[52px]">Quản lý quy trình nghỉ việc từ khi nhân viên nộp đơn đến khi đóng hồ sơ</p>
        </div>
      </div>

      <!-- ===== WORKFLOW DIAGRAM ===== -->
      <GlassCard :glass="false" class="bg-white border border-slate-100 rounded-3xl">
        <h3 class="text-sm font-bold text-slate-500 uppercase tracking-wider mb-4">Quy trình nghiệp vụ</h3>
        <div class="flex flex-wrap items-start gap-x-2 gap-y-4 overflow-x-auto pb-2">
          <template v-for="(step, idx) in [
            { label: 'EM nộp đơn',       sublabel: 'REQUESTED',           color: 'amber',  icon: FileText },
            { label: 'Quản lý duyệt',    sublabel: 'MANAGER_APPROVED',   color: 'indigo', icon: ClipboardCheck },
            { label: 'HR chốt ngày',     sublabel: 'HR_FINALIZED',       color: 'violet', icon: CalendarDays },
            { label: 'Thu hồi TK',       sublabel: 'ACCESS_REVOKED',     color: 'orange', icon: ShieldOff },
            { label: 'Thanh toán cuối',  sublabel: 'SETTLEMENT_PREPARED', color: 'sky',   icon: Banknote },
            { label: 'Đóng hồ sơ',       sublabel: 'CLOSED',             color: 'slate',  icon: Archive },
          ]" :key="step.sublabel">
            <div class="flex flex-col items-center min-w-[100px]">
              <div class="w-11 h-11 rounded-2xl flex items-center justify-center mb-1.5 shadow-sm"
                :class="step.color === 'amber' ? 'bg-amber-100 text-amber-600' : step.color === 'indigo' ? 'bg-indigo-100 text-indigo-600' : step.color === 'violet' ? 'bg-violet-100 text-violet-600' : step.color === 'orange' ? 'bg-orange-100 text-orange-600' : step.color === 'sky' ? 'bg-sky-100 text-sky-600' : 'bg-slate-100 text-slate-500'">
                <component :is="step.icon" class="w-5 h-5" />
              </div>
              <div class="text-xs font-bold text-slate-700 text-center">{{ step.label }}</div>
              <div class="text-[10px] text-slate-400">{{ step.sublabel }}</div>
            </div>
            <div v-if="idx < 5" class="flex items-center mt-2.5">
              <ArrowRight v-if="idx !== 2" class="w-4 h-4 text-slate-300" />
              <!-- Step 3 branches into 2 parallel paths -->
              <div v-if="idx === 2" class="flex flex-col gap-1 mx-1">
                <ArrowRight class="w-4 h-4 text-orange-300" />
                <ArrowRight class="w-4 h-4 text-sky-300" />
              </div>
            </div>
          </template>
        </div>
        <p class="text-xs text-slate-400 mt-3 border-t border-slate-100 pt-3">
          ⚡ Bước 4 và 5 (Thu hồi TK + Thanh toán) có thể thực hiện song song sau khi HR_FINALIZED.
          Đóng hồ sơ yêu cầu: đã thu hồi TK + tất cả checklist/tài sản hoàn tất + hợp đồng bị TERMINATED.
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
          <div class="flex gap-1 overflow-x-auto">
            <button v-for="tab in statusTabs" :key="tab.key"
              @click="activeStatus = tab.key"
              class="px-3 py-1.5 rounded-xl text-sm font-semibold transition-all whitespace-nowrap"
              :class="activeStatus === tab.key ? 'bg-rose-600 text-white shadow' : 'text-slate-500 hover:bg-slate-100'"
            >{{ tab.label }}</button>
          </div>
          <div class="relative ml-auto">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input v-model="searchQuery" type="text" placeholder="Tìm tên, mã, phòng ban..."
              class="pl-9 pr-4 py-2 text-sm rounded-xl border border-slate-200 bg-slate-50 focus:outline-none focus:ring-2 focus:ring-rose-300 w-52" />
          </div>
        </div>

        <!-- Records -->
        <div class="divide-y divide-slate-50">
          <div v-for="rec in filtered" :key="rec.id"
            class="group p-5 hover:bg-slate-50/60 transition-colors cursor-pointer"
            @click="openPanel(rec)">
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
                    <div class="flex items-center gap-1 text-slate-500"><Building2 class="w-3.5 h-3.5" /> {{ rec.dept }}</div>
                    <div class="flex items-center gap-1 text-slate-500"><Users class="w-3.5 h-3.5" /> {{ rec.manager }}</div>
                    <div class="flex items-center gap-1 text-slate-500">
                      <CalendarDays class="w-3.5 h-3.5" />
                      Ngày nộp: <strong class="text-slate-700 ml-1">{{ rec.requestDate }}</strong>
                    </div>
                    <div class="flex items-center gap-1 text-slate-500">
                      <BadgeX class="w-3.5 h-3.5" />
                      Ngày cuối: <strong class="text-rose-600 ml-1">{{ rec.effectiveLastWorkingDate || rec.requestedLastWorkingDate }}</strong>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Action quick badges -->
              <div class="flex items-center gap-2 flex-shrink-0">
                <div v-if="rec.checklistItems.length > 0" class="text-xs text-slate-400 flex items-center gap-1">
                  <ListChecks class="w-3.5 h-3.5" />
                  {{ rec.checklistItems.filter(c=>c.status==='COMPLETED').length }}/{{ rec.checklistItems.length }}
                </div>
                <div v-if="rec.assetReturns.length > 0" class="text-xs text-slate-400 flex items-center gap-1">
                  <MonitorOff class="w-3.5 h-3.5" />
                  {{ rec.assetReturns.filter(a=>a.status==='RETURNED').length }}/{{ rec.assetReturns.length }}
                </div>
                <div v-if="rec.leaveSettlementUnits" class="text-xs font-bold text-sky-600 bg-sky-50 px-2 py-0.5 rounded-lg">
                  Phép: {{ rec.leaveSettlementUnits }}d
                </div>
                <button class="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-colors opacity-0 group-hover:opacity-100">
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          <div v-if="filtered.length === 0" class="py-16 text-center">
            <UserMinus class="w-12 h-12 text-slate-300 mx-auto mb-3" />
            <p class="text-slate-500 font-medium">Không có hồ sơ nào phù hợp</p>
          </div>
        </div>

        <div class="border-t border-slate-100 px-6 py-3 flex items-center justify-between text-sm text-slate-500">
          <span>{{ filtered.length }} hồ sơ</span>
          <div class="flex gap-1">
            <button class="px-3 py-1 rounded-lg hover:bg-slate-50 font-medium">←</button>
            <button class="px-3 py-1 rounded-lg bg-rose-600 text-white font-bold">1</button>
            <button class="px-3 py-1 rounded-lg hover:bg-slate-50 font-medium">→</button>
          </div>
        </div>
      </GlassCard>
    </div>

    <!-- ===== DETAIL SIDE PANEL ===== -->
    <Teleport to="body">
      <Transition name="slide">
        <div v-if="showPanel && selectedRecord" class="fixed inset-0 z-50 flex justify-end">
          <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showPanel = false" />
          <div class="relative bg-white w-full max-w-xl h-full overflow-y-auto shadow-2xl flex flex-col">

            <!-- Header -->
            <div class="bg-gradient-to-r from-rose-600 to-rose-700 p-6 text-white flex-shrink-0">
              <div class="flex items-center justify-between mb-4">
                <span class="text-sm font-semibold text-rose-200">{{ selectedRecord.code }}</span>
                <button @click="showPanel = false" class="w-8 h-8 rounded-xl bg-white/20 hover:bg-white/30 flex items-center justify-center text-lg font-bold">×</button>
              </div>
              <div class="flex items-center gap-4">
                <div class="w-14 h-14 rounded-2xl bg-white/20 flex items-center justify-center text-xl font-black">
                  {{ initials(selectedRecord.name) }}
                </div>
                <div>
                  <h3 class="text-xl font-black">{{ selectedRecord.name }}</h3>
                  <p class="text-rose-200 text-sm">{{ selectedRecord.dept }} · {{ selectedRecord.employeeCode }}</p>
                  <div class="flex items-center gap-2 mt-1">
                    <span class="flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-white/20 text-white border border-white/30">
                      {{ statusConfig[selectedRecord.status]?.label }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Body -->
            <div class="flex-1 p-6 space-y-6 overflow-y-auto">

              <!-- Reason -->
              <div class="bg-rose-50 rounded-2xl p-4 border border-rose-100">
                <div class="text-xs font-bold text-rose-600 uppercase mb-1">Lý do nghỉ việc</div>
                <p class="text-sm text-slate-700 italic">{{ selectedRecord.requestReason }}</p>
                <div class="mt-2 grid grid-cols-2 gap-2 text-xs text-slate-500">
                  <div>Ngày nộp: <strong>{{ selectedRecord.requestDate }}</strong></div>
                  <div>Ngày cuối mong muốn: <strong class="text-rose-600">{{ selectedRecord.requestedLastWorkingDate }}</strong></div>
                </div>
              </div>

              <!-- Timeline -->
              <div>
                <h4 class="font-bold text-slate-700 mb-3 flex items-center gap-2 text-sm">
                  <History class="w-4 h-4 text-rose-600" /> Lịch sử xử lý
                </h4>
                <div class="space-y-2">
                  <div v-for="(h, idx) in selectedRecord.histories" :key="idx"
                    class="flex gap-3">
                    <div class="flex flex-col items-center">
                      <div class="w-7 h-7 rounded-full bg-rose-100 flex items-center justify-center flex-shrink-0">
                        <div class="w-2.5 h-2.5 rounded-full bg-rose-500"></div>
                      </div>
                      <div v-if="idx < selectedRecord.histories.length - 1" class="w-0.5 h-full bg-slate-200 mt-1"></div>
                    </div>
                    <div class="pb-4">
                      <div class="text-xs font-bold text-slate-700">{{ h.action.replace('_', ' ') }}</div>
                      <div class="text-xs text-slate-400">{{ h.changedAt }} · {{ h.changedBy }}</div>
                      <div v-if="h.note" class="text-xs text-slate-500 italic mt-0.5">{{ h.note }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Checklist -->
              <div v-if="selectedRecord.checklistItems.length > 0">
                <h4 class="font-bold text-slate-700 mb-2 flex items-center gap-2 text-sm">
                  <ListChecks class="w-4 h-4 text-rose-600" /> Checklist bàn giao
                </h4>
                <div class="space-y-1.5">
                  <div v-for="item in selectedRecord.checklistItems" :key="item.id"
                    class="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-100">
                    <div>
                      <div class="text-sm font-medium text-slate-700">{{ item.name }}</div>
                      <div class="text-xs text-slate-400">Hạn: {{ item.dueDate }} · {{ item.owner }}</div>
                    </div>
                    <span class="text-xs font-bold px-2 py-0.5 rounded-lg" :class="checklistStatusConfig[item.status]?.class">
                      {{ checklistStatusConfig[item.status]?.label }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- Asset Returns -->
              <div v-if="selectedRecord.assetReturns.length > 0">
                <h4 class="font-bold text-slate-700 mb-2 flex items-center gap-2 text-sm">
                  <MonitorOff class="w-4 h-4 text-rose-600" /> Thu hồi tài sản
                </h4>
                <div class="space-y-1.5">
                  <div v-for="asset in selectedRecord.assetReturns" :key="asset.id"
                    class="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-100">
                    <div>
                      <div class="text-sm font-medium text-slate-700">{{ asset.name }}</div>
                      <div class="text-xs text-slate-400">{{ asset.code }} · Hạn: {{ asset.expectedReturnDate }}</div>
                    </div>
                    <span class="text-xs font-bold px-2 py-0.5 rounded-lg" :class="assetStatusConfig[asset.status]?.class">
                      {{ assetStatusConfig[asset.status]?.label }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- Settlement info -->
              <div v-if="selectedRecord.leaveSettlementUnits" class="bg-sky-50 rounded-2xl p-4 border border-sky-100">
                <h4 class="font-bold text-sky-700 mb-2 text-sm flex items-center gap-2">
                  <Wallet class="w-4 h-4" /> Thanh toán ngày phép dư
                </h4>
                <div class="grid grid-cols-2 gap-2 text-sm">
                  <div class="text-slate-600">Số ngày phép còn lại:</div>
                  <div class="font-bold text-slate-800">{{ selectedRecord.leaveSettlementUnits }} ngày</div>
                  <div class="text-slate-600">Số tiền quy đổi:</div>
                  <div class="font-bold text-sky-700">{{ fmt(selectedRecord.leaveSettlementAmount) }}</div>
                </div>
              </div>

              <!-- Actions -->
              <div v-if="actionsForStatus[selectedRecord.status]" class="space-y-2 pt-2 border-t border-slate-100">
                <button
                  v-for="action in actionsForStatus[selectedRecord.status]"
                  :key="action.key"
                  @click.stop="handleAction(selectedRecord, action.key)"
                  class="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl font-bold transition text-sm"
                  :class="action.class">
                  <component :is="action.icon" class="w-4 h-4" />
                  {{ action.label }}
                </button>
              </div>
              <div v-if="selectedRecord.status === 'CLOSED'" class="flex items-center justify-center gap-2 py-3 bg-slate-50 rounded-xl text-sm text-slate-500 font-semibold border border-slate-200">
                <Archive class="w-4 h-4" /> Hồ sơ đã đóng hoàn tất
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
</style>
