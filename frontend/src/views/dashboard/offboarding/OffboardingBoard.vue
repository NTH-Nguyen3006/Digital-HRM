<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'
import { useAuthStore } from '@/stores/auth'
import {
  UserMinus, Search, Clock, AlertTriangle, CheckCircle2,
  ArrowRight, FileText, MonitorOff, Wallet, ShieldOff,
  Archive, Users, ChevronRight, Building2, CalendarDays,
  BadgeX, ListChecks, ClipboardCheck, Banknote, X,
  CheckCheck, Eye, History, Loader2, Phone, Mail, UserX
} from 'lucide-vue-next'
import {
  getOffboardings, getOffboardingDetail,
  finalizeOffboarding, revokeAccess,
  processSettlement, closeOffboarding,
  createAssetReturn, updateAssetReturn
} from '@/api/admin/offboarding.js'
import {
  getPendingOffboardings,
  getPendingOffboardingDetail,
  reviewOffboarding,
} from '@/api/manager/manager'
import { safeArray, unwrapData, unwrapPage } from '@/utils/api'

/* ------------------ CONFIG ------------------ */

const statusConfig = {
  REQUESTED: { label: 'Chờ quản lý duyệt', badge: 'bg-amber-100 text-amber-700 border-amber-200', dot: 'bg-amber-400' },
  MANAGER_APPROVED: { label: 'Quản lý đã duyệt', badge: 'bg-indigo-100 text-indigo-700 border-indigo-200', dot: 'bg-indigo-400' },
  MANAGER_REJECTED: { label: 'Quản lý từ chối', badge: 'bg-rose-100 text-rose-700 border-rose-200', dot: 'bg-rose-400' },
  HR_FINALIZED: { label: 'HR đã chốt', badge: 'bg-violet-100 text-violet-700 border-violet-200', dot: 'bg-violet-400' },
  ACCESS_REVOKED: { label: 'Thu hồi truy cập', badge: 'bg-orange-100 text-orange-700 border-orange-200', dot: 'bg-orange-400' },
  SETTLEMENT_PREPARED: { label: 'Đã chuẩn bị T.toán', badge: 'bg-sky-100 text-sky-700 border-sky-200', dot: 'bg-sky-400' },
  CLOSED: { label: 'Đã đóng', badge: 'bg-slate-100 text-slate-600 border-slate-200', dot: 'bg-slate-400' },
  CANCELLED: { label: 'Đã hủy', badge: 'bg-rose-100 text-rose-600 border-rose-200', dot: 'bg-rose-300' },
}

/* ------------------ STATE ------------------ */

const records = ref([])
const loading = ref(false)
const searchQuery = ref('')
const activeStatus = ref('ALL')
const selectedRecord = ref(null)
const showPanel = ref(false)
const authStore = useAuthStore()
const isManager = computed(() => authStore.isManager)

const statsData = ref([
  { title: 'Chờ duyệt', value: '0', icon: Clock, color: 'amber', trend: 0 },
  { title: 'Đang xử lý', value: '0', icon: AlertTriangle, color: 'rose', trend: 0 },
  { title: 'Đã đóng tháng này', value: '0', icon: CheckCircle2, color: 'emerald', trend: 0 },
  { title: 'Nghỉ việc năm nay', value: '0', icon: UserMinus, color: 'indigo' },
])

/* ------------------ API CALLS ------------------ */

const fetchOffboardings = async () => {
  loading.value = true
  try {
    if (isManager.value) {
      const response = await getPendingOffboardings()
      const items = Array.isArray(unwrapData(response)) ? unwrapData(response) : []
      const keyword = searchQuery.value.trim().toLowerCase()
      records.value = items.filter((item) => {
        const statusMatched = activeStatus.value === 'ALL' ? true : item.status === activeStatus.value
        const keywordMatched = !keyword
          ? true
          : [item.employeeFullName, item.employeeCode, item.offboardingCode, item.orgUnitName]
            .filter(Boolean)
            .some((value) => value.toLowerCase().includes(keyword))
        return statusMatched && keywordMatched
      })
    } else {
      const params = {
        keyword: searchQuery.value,
        status: activeStatus.value !== 'ALL' ? activeStatus.value : undefined
      }
      const response = await getOffboardings(params)
      records.value = unwrapPage(response).items
    }

    // Quick stats calc
    const requested = records.value.filter(r => r.status === 'REQUESTED').length
    const inProcessing = records.value.filter(r => ['MANAGER_APPROVED', 'HR_FINALIZED', 'ACCESS_REVOKED', 'SETTLEMENT_PREPARED'].includes(r.status)).length
    const closed = records.value.filter(r => r.status === 'CLOSED').length

    statsData.value[0].value = requested.toString()
    statsData.value[1].value = inProcessing.toString()
    statsData.value[2].value = closed.toString()
    statsData.value[3].value = records.value.length.toString()
  } catch (error) {
    console.error('Failed to fetch offboardings:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchOffboardings)

watch([searchQuery, activeStatus], () => {
  fetchOffboardings()
})

/* ------------------ ACTIONS ------------------ */

const openPanel = async (rec) => {
  loading.value = true
  try {
    const response = isManager.value
      ? await getPendingOffboardingDetail(rec.offboardingCaseId)
      : await getOffboardingDetail(rec.offboardingCaseId)
    selectedRecord.value = unwrapData(response)
    showPanel.value = true
  } catch (error) {
    console.error('Failed to get detail:', error)
  } finally {
    loading.value = false
  }
}

const handleAction = async (record, actionKey) => {
  try {
    if (actionKey === 'approve') {
      await reviewOffboarding(record.offboardingCaseId, { approved: true, note: 'Manager đã duyệt yêu cầu nghỉ việc.' })
    } else if (actionKey === 'reject') {
      await reviewOffboarding(record.offboardingCaseId, { approved: false, note: 'Manager từ chối yêu cầu nghỉ việc.' })
    } else if (actionKey === 'finalize') {
      await finalizeOffboarding(record.offboardingCaseId, { effectiveLastWorkingDate: record.requestedLastWorkingDate, note: 'Chốt từ UI Admin.' })
    } else if (actionKey === 'revoke') {
      await revokeAccess(record.offboardingCaseId, { note: 'Thu hồi quyền từ UI Admin.' })
    } else if (actionKey === 'settlement') {
      await processSettlement(record.offboardingCaseId, {
        createPayrollDraftIfMissing: true,
        note: 'Chuẩn bị settlement từ dashboard HR.',
      })
    } else if (actionKey === 'close') {
      await closeOffboarding(record.offboardingCaseId, { note: 'Đóng hồ sơ offboarding từ dashboard HR.' })
    }
    await fetchOffboardings()
    if (isManager.value) {
      showPanel.value = false
    } else {
      await openPanel(record)
    }
  } catch (error) {
    console.error(`Action ${actionKey} failed:`, error)
  }
}

function fmt(n) {
  if (!n) return '0 ₫'
  return new Intl.NumberFormat('vi-VN').format(n) + ' ₫'
}

const initials = (name) => {
  if (!name) return '??'
  return name.split(' ').slice(-2).map(w => w[0]).join('')
}

</script>

<template>

  <div class="space-y-8 animate-fade-in">

    <!-- HEADER -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
      <div>
        <div class="flex items-center gap-4 mb-2">
          <div class="w-12 h-12 rounded-[18px] bg-rose-600 flex items-center justify-center shadow-xl shadow-rose-100">
            <UserMinus class="w-6 h-6 text-white" />
          </div>
          <h2 class="text-4xl font-black text-slate-900 tracking-tight">Thôi việc (Offboarding)</h2>
        </div>
        <p class="text-slate-500 font-medium ml-1">Lộ trình bàn giao và giải quyết thủ tục nghỉ việc</p>
      </div>
    </div>

    <!-- WORKFLOW DIAGRAM -->
    <GlassCard :glass="false" class="bg-white border border-slate-100 rounded-4xl p-8">
      <div class="flex flex-col xl:flex-row items-center gap-10">
        <div class="xl:max-w-xs">
          <h3 class="text-lg font-black text-slate-900 mb-2">Quy trình chuyên môn</h3>
          <p class="text-xs text-slate-400 font-bold leading-relaxed">Áp dụng cho nhân viên nghỉ việc tự nguyện hoặc kết
            thúc hợp đồng.</p>
        </div>

        <div class="flex-1 flex flex-wrap justify-center items-center gap-4">
          <template v-for="(step, idx) in [
            { label: 'EM Nộp đơn', sub: 'REQUESTED', icon: FileText, color: 'bg-amber-100 text-amber-600' },
            { label: 'Phê duyệt', sub: 'MANAGER', icon: ClipboardCheck, color: 'bg-indigo-100 text-indigo-600' },
            { label: 'HR Chốt', sub: 'FINALIZED', icon: CalendarDays, color: 'bg-violet-100 text-violet-600' },
            { label: 'Xử lý', sub: 'TK & Settlement', icon: History, color: 'bg-orange-100 text-orange-600' },
            { label: 'Đóng hồ sơ', sub: 'CLOSED', icon: Archive, color: 'bg-slate-100 text-slate-500' },
          ]" :key="idx">
            <div class="flex flex-col items-center gap-2 group cursor-help anim-bounce-subtle">
              <div
                :class="['w-14 h-14 rounded-2xl flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform', step.color]">
                <component :is="step.icon" class="w-6 h-6" />
              </div>
              <div class="text-center">
                <p class="text-[11px] font-black uppercase tracking-wider text-slate-800">{{ step.label }}</p>
                <p class="text-[9px] text-slate-400 font-bold">{{ step.sub }}</p>
              </div>
            </div>
            <ArrowRight v-if="idx < 4" class="w-5 h-5 text-slate-200" />
          </template>
        </div>
      </div>
    </GlassCard>

    <!-- STATS -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <StatCard v-for="s in statsData" :key="s.title" :title="s.title" :value="s.value" :icon="s.icon" :color="s.color"
        :trend="s.trend" class="rounded-4xl!" />
    </div>

    <!-- LIST PANEL -->
    <div class="bg-white border border-slate-100 rounded-4xl overflow-hidden shadow-sm relative min-h-125">

      <!-- Loading Overlay -->
      <div v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center bg-white/60 backdrop-blur-[1px]">
        <Loader2 class="w-10 h-10 text-rose-600 animate-spin" />
      </div>

      <!-- Toolbar -->
      <div class="px-8 py-6 border-b border-slate-50 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
        <div class="flex items-center gap-2 p-1 bg-slate-50 rounded-2xl">
          <button v-for="tab in [
            { id: 'ALL', label: 'Tất cả' },
            { id: 'REQUESTED', label: 'Chờ duyệt' },
            { id: 'HR_FINALIZED', label: 'HR đã chốt' },
            { id: 'CLOSED', label: 'Đã đóng' }
          ]" :key="tab.id" @click="activeStatus = tab.id"
            class="px-5 py-2 rounded-xl text-xs font-black transition-all"
            :class="activeStatus === tab.id ? 'bg-white text-rose-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'">
            {{ tab.label }}
          </button>
        </div>

        <div class="relative group">
          <Search
            class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 group-focus-within:text-rose-500 transition-colors" />
          <input v-model="searchQuery" type="text" placeholder="Tìm tên hoặc mã hồ sơ..."
            class="w-72 pl-11 pr-4 py-3 bg-slate-50 border border-transparent rounded-2xl text-xs font-bold focus:bg-white focus:border-rose-100 focus:ring-4 focus:ring-rose-500/5 transition-all outline-none" />
        </div>
      </div>

      <!-- Content -->
      <div v-if="records.length > 0" class="divide-y divide-slate-50">
        <div v-for="rec in records" :key="rec.offboardingCaseId"
          class="group px-8 py-6 hover:bg-slate-50/50 transition-all cursor-pointer flex flex-col xl:flex-row xl:items-center justify-between gap-6"
          @click="openPanel(rec)">
          <div class="flex items-start gap-5 min-w-75">
            <AvatarBox :name="rec.employeeFullName" size="lg" shape="rounded-[20px]"
              class="shadow-sm border border-slate-100" />
            <div>
              <div class="flex items-center gap-2 mb-1.5 text-xs font-black">
                <span class="text-slate-400 group-hover:text-rose-600 transition-colors uppercase tracking-widest">{{
                  rec.offboardingCode }}</span>
                <div class="w-1 h-1 rounded-full bg-slate-300"></div>
                <span class="text-slate-500">{{ rec.orgUnitName }}</span>
              </div>
              <h4 class="text-lg font-black text-slate-900 mb-2">{{ rec.employeeFullName }}</h4>
              <div class="flex flex-wrap gap-2 text-[10px] font-black uppercase tracking-tighter">
                <span class="px-2 py-1 bg-slate-100 text-slate-600 rounded-lg">
                  Quản lý: {{ rec.managerEmployeeName || 'Chưa gán' }}
                </span>
                <div class="flex items-center gap-1.5 px-2 py-1 bg-rose-50 text-rose-700 rounded-lg">
                  <BadgeX class="w-3 h-3" />
                  Final day: {{ rec.effectiveLastWorkingDate || rec.requestedLastWorkingDate }}
                </div>
              </div>
            </div>
          </div>

          <!-- Side actions -->
          <div class="flex items-center gap-8 justify-between xl:justify-end">
            <div class="flex flex-col items-end gap-1">
              <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest whitespace-nowrap">Trạng thái hồ
                sơ
              </p>
              <StatusBadge :status="rec.status" />
            </div>
            <button
              class="p-2 text-slate-200 hover:text-rose-600 bg-slate-50 hover:bg-rose-50 rounded-xl transition-all">
              <ChevronRight class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      <EmptyState v-else-if="!loading" title="Không tìm thấy hồ sơ Offboarding"
        description="Kỳ lạ thật, không có ai trong danh sách thôi việc hiện tại cả." iconName="UserMinus" />
    </div>

  </div>

  <!-- DETAIL PANEL -->
  <Teleport to="body">
    <Transition name="slide-panel">
      <div v-if="showPanel && selectedRecord" class="fixed inset-0 z-100 flex justify-end">
        <div class="absolute inset-0 bg-slate-900/70 backdrop-blur-md" @click="showPanel = false" />

        <div class="relative bg-white w-full max-w-2xl h-full shadow-2xl flex flex-col animate-slide-left">

          <!-- Header -->
          <div class="p-10 border-b border-slate-100 shrink-0">
            <div class="flex items-start justify-between mb-8">
              <div class="flex items-center gap-5">
                <div
                  class="w-16 h-16 rounded-[24px] bg-rose-50 text-rose-600 flex items-center justify-center font-black text-2xl shadow-inner border border-rose-100">
                  {{ initials(selectedRecord.employeeFullName) }}
                </div>
                <div>
                  <span class="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em] mb-1 block">{{
                    selectedRecord.offboardingCode }}</span>
                  <h3 class="text-3xl font-black text-slate-900 tracking-tight">{{ selectedRecord.employeeFullName }}
                  </h3>
                </div>
              </div>
              <button @click="showPanel = false"
                class="w-10 h-10 rounded-2xl bg-slate-50 text-slate-400 hover:text-rose-500 hover:bg-rose-50 transition-all flex items-center justify-center font-bold">×</button>
            </div>

            <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-wider mb-1">Mã NV</p>
                <p class="text-xs font-bold text-slate-800">{{ selectedRecord.employeeCode }}</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-wider mb-1">Phòng ban</p>
                <p class="text-xs font-bold text-slate-800">{{ selectedRecord.orgUnitName }}</p>
              </div>
              <div class="p-4 bg-rose-50 rounded-2xl">
                <p class="text-[9px] font-black text-rose-400 uppercase tracking-wider mb-1">Ngày làm cuối</p>
                <p class="text-xs font-bold text-rose-600">{{ selectedRecord.effectiveLastWorkingDate || 'Chưa chốt' }}
                </p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-wider mb-1">Manager</p>
                <p class="text-xs font-bold text-slate-800">{{ selectedRecord.managerEmployeeName || 'N/A' }}</p>
              </div>
            </div>
          </div>

          <!-- Body -->
          <div class="flex-1 overflow-y-auto p-10 space-y-10">

            <!-- Reason Section -->
            <div class="p-6 bg-slate-50 rounded-4xl border border-slate-100">
              <h4 class="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em] mb-4 flex items-center gap-2">
                <FileText class="w-4 h-4" /> Lý do & Nguyện vọng
              </h4>
              <p class="text-sm text-slate-700 leading-relaxed italic font-medium">"{{ selectedRecord.requestReason ||
                'Không cung cấp lý do chi tiết' }}"</p>
              <div class="flex items-center gap-4 mt-6 pt-6 border-t border-slate-200/60">
                <div class="flex items-center gap-2 text-xs font-bold text-slate-500">
                  <CalendarDays class="w-3.5 h-3.5" /> Ngày nộp: {{ selectedRecord.requestDate }}
                </div>
                <div class="w-1 h-1 rounded-full bg-slate-300"></div>
                <div class="text-xs font-bold text-rose-600">
                  Nguyện vọng: {{ selectedRecord.requestedLastWorkingDate }}
                </div>
              </div>
            </div>

            <!-- Checklist / Settlement Row -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div class="space-y-4">
                <h4 class="text-[10px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                  <ListChecks class="w-4 h-4 text-rose-600" /> {{ isManager ? 'Checklist bàn giao' : 'Bàn giao tài sản' }}
                </h4>
                <div v-if="isManager && selectedRecord.checklistItems?.length" class="space-y-3">
                  <div v-for="item in selectedRecord.checklistItems" :key="item.offboardingChecklistItemId"
                    class="p-4 bg-white border border-slate-100 rounded-2xl shadow-sm flex items-center justify-between">
                    <div>
                      <p class="text-xs font-black text-slate-800">{{ item.itemName }}</p>
                      <p class="text-[9px] font-bold text-slate-400">{{ item.ownerRoleCode || 'EMPLOYEE' }}</p>
                    </div>
                    <span
                      :class="['px-2 py-0.5 rounded-lg text-[9px] font-black', item.status === 'COMPLETED' ? 'bg-emerald-100 text-emerald-600' : 'bg-amber-100 text-amber-600']">
                      {{ item.status === 'COMPLETED' ? 'ĐÃ XONG' : item.status }}
                    </span>
                  </div>
                </div>
                <div v-else-if="selectedRecord.assetReturns && selectedRecord.assetReturns.length > 0"
                  class="space-y-3">
                  <div v-for="asset in selectedRecord.assetReturns" :key="asset.offboardingAssetReturnId"
                    class="p-4 bg-white border border-slate-100 rounded-2xl shadow-sm flex items-center justify-between">
                    <div>
                      <p class="text-xs font-black text-slate-800">{{ asset.assetName }}</p>
                      <p class="text-[9px] font-bold text-slate-400">{{ asset.assetCode }}</p>
                    </div>
                    <span
                      :class="['px-2 py-0.5 rounded-lg text-[9px] font-black', asset.status === 'RETURNED' ? 'bg-emerald-100 text-emerald-600' : 'bg-amber-100 text-amber-600']">
                      {{ asset.status === 'RETURNED' ? 'ĐÃ TRẢ' : asset.status }}
                    </span>
                  </div>
                </div>
                <div v-else class="text-xs text-slate-400 italic">{{ isManager ? 'Chưa có checklist bàn giao' : 'Không có tài sản cần thu hồi' }}</div>
              </div>

              <div class="space-y-4">
                <h4 class="text-[10px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                  <Wallet class="w-4 h-4 text-emerald-600" /> Quyết toán tài chính
                </h4>
                <div class="p-6 bg-emerald-600 rounded-4xl text-white shadow-xl shadow-emerald-100/50">
                  <p class="text-[10px] font-black text-emerald-200 uppercase tracking-widest mb-4">Dự kiến thanh toán
                    phép</p>
                  <div class="space-y-3">
                    <div class="flex justify-between items-end border-b border-white/10 pb-3">
                      <p class="text-xs font-bold opacity-80">Số ngày dư</p>
                      <p class="text-lg font-black">{{ selectedRecord.leaveSettlementUnits || 0 }} ngày</p>
                    </div>
                    <div class="flex justify-between items-end">
                      <p class="text-xs font-bold opacity-80">Tổng tiền</p>
                      <p class="text-xl font-black">{{ fmt(selectedRecord.leaveSettlementAmount) }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- History Timeline -->
            <div>
              <h4 class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-6 flex items-center gap-2">
                <History class="w-4 h-4 text-indigo-600" /> Lịch sử phê duyệt
              </h4>
              <div class="space-y-6 ml-2 border-l-2 border-slate-100 pl-8">
                <div v-for="(h, idx) in selectedRecord.histories" :key="h.offboardingHistoryId || idx" class="relative">
                  <div
                    class="absolute -left-10.25 top-0 w-5 h-5 rounded-full bg-white border-4 border-indigo-500 shadow-sm">
                  </div>
                  <div class="p-5 bg-slate-50 rounded-[28px] border border-slate-100 shadow-sm">
                    <div class="flex justify-between items-start mb-2">
                      <span class="text-[10px] font-black text-indigo-600 uppercase">{{ h.actionCode }}</span>
                      <span class="text-[9px] font-bold text-slate-400">{{ h.changedAt }}</span>
                    </div>
                    <p class="text-xs text-slate-700 font-bold mb-1">{{ h.changedByUsername }}</p>
                    <p class="text-xs text-slate-500 italic">{{ h.actionNote || 'Không có ghi chú' }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Actions Footer -->
          <div
            class="p-10 border-t border-slate-50 flex items-center gap-4 shrink-0 bg-white shadow-[0_-10px_30px_rgba(0,0,0,0.02)]">
            <template v-if="isManager">
              <BaseButton v-if="selectedRecord.status === 'REQUESTED'" variant="primary" size="lg" shadow
                class="flex-1 rounded-2xl! font-bold h-14! bg-emerald-600 hover:bg-emerald-700"
                @click="handleAction(selectedRecord, 'approve')">
                <CheckCheck class="w-5 h-5 mr-2" /> Duyệt yêu cầu
              </BaseButton>

              <BaseButton v-if="selectedRecord.status === 'REQUESTED'" variant="outline" size="lg"
                class="flex-1 rounded-2xl! font-bold h-14!" @click="handleAction(selectedRecord, 'reject')">
                <X class="w-5 h-5 mr-2 text-rose-600" /> Từ chối
              </BaseButton>

              <div v-if="selectedRecord.status !== 'REQUESTED'"
                class="w-full text-center py-5 bg-slate-100 text-slate-600 rounded-[28px] font-black uppercase text-xs tracking-[0.2em]">
                Manager chỉ xử lý bước phê duyệt ban đầu và theo dõi checklist bàn giao
              </div>
            </template>

            <!-- HR Finalize Action -->
            <BaseButton v-if="!isManager && selectedRecord.status === 'MANAGER_APPROVED'" variant="primary" size="lg"
              shadow class="flex-1 rounded-2xl! font-bold h-14! bg-violet-600 hover:bg-violet-700"
              @click="handleAction(selectedRecord, 'finalize')">
              <ClipboardCheck class="w-5 h-5 mr-2" /> HR Chốt ngày nghỉ
            </BaseButton>

            <!-- IT / Access Actions -->
            <BaseButton v-if="!isManager && selectedRecord.status === 'HR_FINALIZED'" variant="primary" size="lg" shadow
              class="flex-1 rounded-2xl! font-bold h-14! bg-orange-500 hover:bg-orange-600"
              @click="handleAction(selectedRecord, 'revoke')">
              <ShieldOff class="w-5 h-5 mr-2" /> Thu hồi quyền truy cập
            </BaseButton>

            <!-- Settlement -->
            <BaseButton v-if="!isManager && ['HR_FINALIZED', 'ACCESS_REVOKED'].includes(selectedRecord.status)"
              variant="outline" size="lg" class="flex-1 rounded-2xl! font-bold h-14!"
              @click="handleAction(selectedRecord, 'settlement')">
              <Banknote class="w-5 h-5 mr-2 text-emerald-600" /> Quyết toán tài chính
            </BaseButton>

            <!-- Close case -->
            <BaseButton v-if="!isManager && ['ACCESS_REVOKED', 'SETTLEMENT_PREPARED'].includes(selectedRecord.status)"
              variant="primary" size="lg" shadow
              class="flex-1 rounded-2xl! font-bold h-14! bg-slate-800 hover:bg-slate-900"
              @click="handleAction(selectedRecord, 'close')">
              <Archive class="w-5 h-5 mr-2" /> Đóng hồ sơ hoàn tất
            </BaseButton>

            <div v-if="!isManager && selectedRecord.status === 'CLOSED'"
              class="w-full text-center py-5 bg-slate-100 text-slate-600 rounded-[28px] font-black uppercase text-xs tracking-[0.2em]">
              Hồ sơ đã đóng • Lưu trữ kho nhân sự
            </div>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>

</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-slide-left {
  animation: slideLeft 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes slideLeft {
  from {
    transform: translateX(100%);
  }

  to {
    transform: translateX(0);
  }
}

.slide-panel-enter-active,
.slide-panel-leave-active {
  transition: opacity 0.4s ease;
}

.slide-panel-enter-from,
.slide-panel-leave-to {
  opacity: 0;
}

.anim-bounce-subtle:hover {
  transform: translateY(-5px);
}
</style>
