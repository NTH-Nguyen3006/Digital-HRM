<script setup>
import { ref, onMounted, watch } from 'vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'
import {
  UserPlus, Search, Plus, CheckCircle2, Clock, Users,
  FileText, Laptop, KeyRound, Mail, ClipboardList,
  ChevronRight, AlertCircle, CheckCheck, Eye,
  CalendarDays, Building2, Briefcase, ArrowRight,
  BadgeCheck, UserCheck, Send, FilePlus2, ListChecks,
  Loader2, MoreVertical
} from 'lucide-vue-next'
import {
  getOnboardings, getOnboardingDetail,
  completeOnboarding, notifyOnboarding,
  createUserForOnboarding, createInitialContract,
  upsertOnboardingChecklist
} from '@/api/admin/onboarding.js'
import { Phone, Mail, FileWarning } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import { useUiStore } from '@/stores/ui'

/* ------------------ CONFIG ------------------ */

const statusConfig = {
  DRAFT: { label: 'Nháp', color: 'slate', dot: 'bg-slate-400', badge: 'bg-slate-100 text-slate-700 border-slate-200', order: 0 },
  IN_PROGRESS: { label: 'Đang chuẩn bị', color: 'amber', dot: 'bg-amber-400', badge: 'bg-amber-100 text-amber-700 border-amber-200', order: 1 },
  READY_FOR_JOIN: { label: 'Sẵn sàng làm', color: 'indigo', dot: 'bg-indigo-400', badge: 'bg-indigo-100 text-indigo-700 border-indigo-200', order: 2 },
  COMPLETED: { label: 'Hoàn tất', color: 'emerald', dot: 'bg-emerald-400', badge: 'bg-emerald-100 text-emerald-700 border-emerald-200', order: 3 },
  CANCELLED: { label: 'Đã hủy', color: 'rose', dot: 'bg-rose-400', badge: 'bg-rose-100 text-rose-700 border-rose-200', order: -1 },
}

const stepDefs = [
  { key: 'checklist', icon: ListChecks, label: 'Checklist chuẩn bị', desc: 'Cấp phát tài sản, tài liệu đầu vào', status: 'IN_PROGRESS' },
  { key: 'orientation', icon: BadgeCheck, label: 'Xác nhận Orientation', desc: 'Manager xác nhận định hướng nhập môn', status: 'IN_PROGRESS' },
  { key: 'account', icon: KeyRound, label: 'Tạo tài khoản hệ thống', desc: 'Cấp username / email công ty', status: 'READY_FOR_JOIN' },
  { key: 'contract', icon: FilePlus2, label: 'Tạo hợp đồng đầu tiên', desc: 'Hợp đồng thử việc / chính thức', status: 'READY_FOR_JOIN' },
  { key: 'notify', icon: Send, label: 'Gửi thông báo chào mừng', desc: 'Email Welcome đến ứng viên & IT Desk', status: 'READY_FOR_JOIN' },
  { key: 'complete', icon: CheckCheck, label: 'Chốt hoàn tất Onboarding', desc: 'Tất cả checklist bắt buộc phải xong', status: 'COMPLETED' },
]


const toast = useToast()
const ui = useUiStore()

const records = ref([])
const loading = ref(false)
const searchQuery = ref('')
const activeStatus = ref('ALL')
const selectedRecord = ref(null)
const showDetailPanel = ref(false)

const statsData = ref([
  { title: 'Đang chuẩn bị', value: '0', icon: Clock, color: 'amber', trend: 0 },
  { title: 'Sẵn sàng làm việc', value: '0', icon: UserCheck, color: 'indigo', trend: 0 },
  { title: 'Hoàn tất tháng này', value: '0', icon: CheckCircle2, color: 'emerald', trend: 0 },
  { title: 'Tổng tiếp nhận', value: '0', icon: UserPlus, color: 'rose' },
])

/*  API CALL  */
const fetchOnboardings = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchQuery.value,
      status: activeStatus.value !== 'ALL' ? activeStatus.value : undefined
    }
    const response = await getOnboardings(params)
    records.value = response.data || []

    // Quick stats calc
    const inProgress = records.value.filter(r => r.onboardingStatus === 'IN_PROGRESS').length
    const ready = records.value.filter(r => r.onboardingStatus === 'READY_FOR_JOIN').length
    const completed = records.value.filter(r => r.onboardingStatus === 'COMPLETED').length

    statsData.value[0].value = inProgress.toString()
    statsData.value[1].value = ready.toString()
    statsData.value[2].value = completed.toString()
    statsData.value[3].value = records.value.length.toString()
  } catch (error) {
    console.error('Failed to fetch onboardings:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchOnboardings)

watch([searchQuery, activeStatus], () => {
  fetchOnboardings()
})

function getStepState(record, step) {
  const statusOrder = ['DRAFT', 'IN_PROGRESS', 'READY_FOR_JOIN', 'COMPLETED', 'CANCELLED']
  const currentIdx = statusOrder.indexOf(record.onboardingStatus)
  const requiredIdx = statusOrder.indexOf(step.status)

  // Logic simplified based on status mapping
  if (currentIdx >= requiredIdx) return 'done'
  if (currentIdx === requiredIdx - 1) return 'active'
  return 'pending'
}

async function openDetail(record) {
  loading.value = true
  try {
    const response = await getOnboardingDetail(record.onboardingId)
    selectedRecord.value = response.data
    showDetailPanel.value = true
  } catch (error) {
    console.error('Failed to get detail:', error)
  } finally {
    loading.value = false
  }
}

async function handleComplete(record) {
  try {
    await completeOnboarding(record.onboardingId)
    await fetchOnboardings()
    showDetailPanel.value = false
  } catch (error) {
    console.error('Failed to complete onboarding:', error)
  }
}

async function handleNotify(record) {
  try {
    await notifyOnboarding(record.onboardingId, { notifyNewHire: true, notifyManager: true })
    toast.success('Đã gửi thông báo chào mừng')
    await openDetail(record) // Refresh detail
  } catch (error) {
    console.error('Failed to notify:', error)
    toast.error('Gửi thông báo thất bại')
  }
}

async function handleStepProcess(step) {
  const record = selectedRecord.value
  if (!record) return

  try {
    if (step.key === 'account') {
      const confirmed = await ui.confirm({
        title: 'Tạo tài khoản hệ thống',
        message: `Hệ thống sẽ tự động tạo Username và Email công ty cho ${record.fullName}. Bạn có chắc chắn?`,
        confirmLabel: 'Tạo ngay'
      })
      if (!confirmed) return
      await createUserForOnboarding(record.onboardingId, {})
      toast.success('Đã tạo tài khoản thành công')
    }
    else if (step.key === 'contract') {
      const confirmed = await ui.confirm({
        title: 'Tạo hợp đồng đầu tiên',
        message: 'Hệ thống sẽ tạo bản nháp hợp đồng dựa trên thông tin ứng viên. Tiếp tục?',
        confirmLabel: 'Tạo bản nháp'
      })
      if (!confirmed) return
      await createInitialContract(record.onboardingId, { contractType: 'PROBATION' })
      toast.success('Đã tạo bản nháp hợp đồng')
    }
    else if (step.key === 'checklist') {
      // For now, just mark it as "In Progress" or "Ready" via a simple update
      toast.info('Tính năng cập nhật Checklist chi tiết đang được phát triển')
      return
    }
    else if (step.key === 'notify') {
      await handleNotify(record)
      return
    }
    else if (step.key === 'complete') {
      await handleComplete(record)
      return
    }

    await openDetail(record) // Refresh
  } catch (error) {
    toast.error('Xử lý thất bại: ' + (error.response?.data?.message || error.message))
  }
}

function initials(name) {
  return name ? name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) : '??'
}

</script>

<template>

  <div class="space-y-8 animate-fade-in">

    <!-- HEADER -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
      <div>
        <div class="flex items-center gap-4 mb-2">
          <div
            class="w-12 h-12 rounded-[18px] bg-indigo-600 flex items-center justify-center shadow-xl shadow-indigo-100">
            <UserPlus class="w-6 h-6 text-white" />
          </div>
          <h2 class="text-4xl font-black text-slate-900 tracking-tight">Tiếp nhận Nhân sự</h2>
        </div>
        <p class="text-slate-500 font-medium ml-1">Chuẩn bị lộ trình nhập môn cho thành viên mới</p>
      </div>

      <BaseButton variant="primary" size="lg" shadow class="rounded-2xl! px-6! h-12.5! font-bold">
        <Plus class="w-5 h-5 mr-2" /> Tạo hồ sơ Onboarding
      </BaseButton>
    </div>

    <!-- WORKFLOW DIAGRAM -->
    <div class="grid grid-cols-1 xl:grid-cols-12 gap-6 items-start">
      <GlassCard :glass="false" class="xl:col-span-12 bg-white border border-slate-100 rounded-4xl p-6">
        <div class="flex flex-col lg:flex-row items-center gap-8 justify-between">
          <div class="lg:max-w-xs">
            <h3 class="text-lg font-black text-slate-900 mb-1">Quy trình chuyên môn</h3>
            <p class="text-xs text-slate-400 font-medium">Theo tiêu chuẩn ISO-HRM của tập đoàn</p>
          </div>

          <div class="flex-1 flex flex-wrap justify-center lg:justify-end items-center gap-4 py-2">
            <template v-for="(step, idx) in [
              { label: 'Khởi tạo', sub: 'Hồ sơ nháp', icon: FilePlus2, color: 'bg-slate-100 text-slate-500' },
              { label: 'Chuẩn bị', sub: 'Checklist & Asset', icon: Laptop, color: 'bg-amber-100 text-amber-600' },
              { label: 'Sẵn sàng', sub: 'TK & Hợp đồng', icon: KeyRound, color: 'bg-indigo-100 text-indigo-600' },
              { label: 'Hoàn tất', sub: 'Nhân viên chính thức', icon: BadgeCheck, color: 'bg-emerald-100 text-emerald-600' },
            ]" :key="idx">
              <div class="flex flex-col items-center gap-2 group cursor-help">
                <div
                  :class="['w-14 h-14 rounded-2xl flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform', step.color]">
                  <component :is="step.icon" class="w-6 h-6" />
                </div>
                <div class="text-center">
                  <p class="text-[11px] font-black uppercase tracking-wider text-slate-800">{{ step.label }}</p>
                  <p class="text-[9px] text-slate-400 font-bold whitespace-nowrap">{{ step.sub }}</p>
                </div>
              </div>
              <ArrowRight v-if="idx < 3" class="w-5 h-5 text-slate-200" />
            </template>
          </div>
        </div>
      </GlassCard>
    </div>

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
        <Loader2 class="w-10 h-10 text-indigo-600 animate-spin" />
      </div>

      <!-- Toolbar -->
      <div class="px-8 py-6 border-b border-slate-50 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
        <div class="flex items-center gap-2 p-1 bg-slate-50 rounded-2xl">
          <button v-for="tab in [
            { id: 'ALL', label: 'Tất cả' },
            { id: 'IN_PROGRESS', label: 'Đang chuẩn bị' },
            { id: 'READY_FOR_JOIN', label: 'Sẵn sàng' },
            { id: 'COMPLETED', label: 'Hoàn tất' }
          ]" :key="tab.id" @click="activeStatus = tab.id"
            class="px-5 py-2 rounded-xl text-xs font-black transition-all"
            :class="activeStatus === tab.id ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'">
            {{ tab.label }}
          </button>
        </div>

        <div class="relative group">
          <Search
            class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
          <input v-model="searchQuery" type="text" placeholder="Tìm tên hoặc mã hồ sơ..."
            class="w-72 pl-11 pr-4 py-3 bg-slate-50 border border-transparent rounded-2xl text-xs font-bold focus:bg-white focus:border-indigo-100 focus:ring-4 focus:ring-indigo-500/5 transition-all outline-none" />
        </div>
      </div>

      <!-- Content -->
      <div v-if="records.length > 0" class="divide-y divide-slate-50">
        <div v-for="rec in records" :key="rec.onboardingId"
          class="group px-8 py-6 hover:bg-slate-50/50 transition-all cursor-pointer flex flex-col xl:flex-row xl:items-center justify-between gap-6"
          @click="openDetail(rec)">
          <div class="flex items-start gap-5 min-w-75">
            <AvatarBox :name="rec.fullName" :image="rec.avatarUrl" size="lg" shape="rounded-[20px]" class="shadow-sm" />
            <div>
              <div class="flex items-center gap-2 mb-1.5 text-xs font-black">
                <span class="text-slate-400 group-hover:text-indigo-600 transition-colors uppercase tracking-widest">{{
                  rec.onboardingCode }}</span>
                <div class="w-1 h-1 rounded-full bg-slate-300"></div>
                <span class="text-slate-500">{{ rec.jobTitleName }}</span>
              </div>
              <h4 class="text-lg font-black text-slate-900 mb-2">{{ rec.fullName }}</h4>
              <div class="flex flex-wrap gap-2">
                <span
                  class="px-2 py-1 bg-slate-100 text-slate-600 rounded-lg text-[10px] font-black uppercase tracking-tighter">
                  {{ rec.orgUnitName }}
                </span>
                <div
                  class="flex items-center gap-1.5 px-2 py-1 bg-indigo-50 text-indigo-700 rounded-lg text-[10px] font-black">
                  <CalendarDays class="w-3 h-3" />
                  Bắt đầu: {{ rec.plannedStartDate }}
                </div>
              </div>
            </div>
          </div>

          <!-- Steps Mini Visual -->
          <div class="flex-1 flex justify-center xl:justify-start items-center gap-1.5">
            <div v-for="step in stepDefs" :key="step.key"
              class="w-8 h-8 rounded-xl flex items-center justify-center transition-all bg-slate-50 text-slate-300"
              :class="{
                'bg-emerald-50 text-emerald-600 border border-emerald-100 shadow-sm shadow-emerald-100/50': getStepState(rec, step) === 'done',
                'bg-indigo-50 text-indigo-600 ring-2 ring-indigo-200 scale-110 z-10 shadow-lg shadow-indigo-100': getStepState(rec, step) === 'active'
              }">
              <component :is="step.icon" class="w-4 h-4" />
            </div>
          </div>

          <!-- Side actions -->
          <div class="flex items-center gap-6 justify-between xl:justify-end">
            <StatusBadge :status="rec.onboardingStatus" />
            <button
              class="p-2 text-slate-300 hover:text-indigo-600 bg-slate-50 hover:bg-indigo-50 rounded-xl transition-all">
              <chevron-right class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      <EmptyState v-else-if="!loading" title="Không tìm thấy hồ sơ Onboarding"
        description="Có vẻ như không có nhân sự mới nào trong danh sách hiện tại của bạn." iconName="UserPlus" />
    </div>

  </div>

  <!-- DETAIL PANEL -->
  <Teleport to="body">
    <Transition name="slide-panel">
      <div v-if="showDetailPanel && selectedRecord" class="fixed inset-0 z-100 flex justify-end">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-md" @click="showDetailPanel = false" />

        <div class="relative bg-white w-full max-w-2xl h-full shadow-2xl flex flex-col animate-slide-left">

          <!-- Header -->
          <div class="p-8 border-b border-slate-100 shrink-0">
            <div class="flex items-start justify-between mb-8">
              <div class="flex items-center gap-4">
                <div
                  class="w-14 h-14 rounded-3xl bg-indigo-50 text-indigo-600 flex items-center justify-center font-black text-xl shadow-inner">
                  {{ initials(selectedRecord.fullName) }}
                </div>
                <div>
                  <span class="text-xs font-black text-slate-400 uppercase tracking-widest">{{
                    selectedRecord.onboardingCode }}</span>
                  <h3 class="text-2xl font-black text-slate-900">{{ selectedRecord.fullName }}</h3>
                </div>
              </div>
              <button @click="showDetailPanel = false"
                class="w-10 h-10 rounded-2xl bg-slate-50 text-slate-400 hover:text-rose-500 hover:bg-rose-50 transition-all flex items-center justify-center font-bold">×</button>
            </div>

            <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Chức danh</p>
                <p class="text-xs font-bold text-slate-800">{{ selectedRecord.jobTitleName }}</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Phòng ban</p>
                <p class="text-xs font-bold text-slate-800">{{ selectedRecord.orgUnitName }}</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Dự kiến tham gia</p>
                <p class="text-xs font-bold text-indigo-600">{{ selectedRecord.plannedStartDate }}</p>
              </div>
              <div class="p-4 bg-slate-50 rounded-2xl">
                <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Manager</p>
                <p class="text-xs font-bold text-slate-800">{{ selectedRecord.managerEmployeeName || 'Chưa gán' }}</p>
              </div>
            </div>
          </div>

          <!-- Tabs Body -->
          <div class="flex-1 overflow-y-auto p-8 space-y-8">

            <!-- Checklist -->
            <div>
              <h4 class="text-sm font-black text-slate-900 uppercase tracking-widest mb-4 flex items-center gap-2">
                <ListChecks class="w-4 h-4 text-indigo-600" /> Checklist tiến độ
              </h4>
              <div class="space-y-3">
                <div v-for="step in stepDefs" :key="step.key"
                  class="p-4 rounded-3xl border transition-all flex items-center justify-between" :class="{
                    'bg-emerald-50 border-emerald-100': getStepState(selectedRecord, step) === 'done',
                    'bg-white border-slate-100 shadow-sm': getStepState(selectedRecord, step) !== 'done',
                    'ring-2 ring-indigo-500/20 border-indigo-100!': getStepState(selectedRecord, step) === 'active'
                  }">
                  <div class="flex items-center gap-4">
                    <div
                      :class="['w-10 h-10 rounded-2xl flex items-center justify-center font-bold',
                        getStepState(selectedRecord, step) === 'done' ? 'bg-emerald-100 text-emerald-600' :
                          getStepState(selectedRecord, step) === 'active' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-slate-100 text-slate-400']">
                      <CheckCheck v-if="getStepState(selectedRecord, step) === 'done'" class="w-5 h-5" />
                      <component :is="step.icon" v-else class="w-5 h-5" />
                    </div>
                    <div>
                      <p class="text-sm font-black text-slate-900">{{ step.label }}</p>
                      <p class="text-xs text-slate-400 font-medium">{{ step.desc }}</p>
                    </div>
                  </div>

                  <button v-if="getStepState(selectedRecord, step) === 'active'" @click="handleStepProcess(step)"
                    class="px-4 py-2 bg-indigo-50 text-indigo-600 rounded-xl text-xs font-black shadow-sm hover:bg-indigo-600 hover:text-white transition-all">
                    Xử lý →
                  </button>
                  <div v-else-if="getStepState(selectedRecord, step) === 'done'"
                    class="text-emerald-500 font-black text-[10px] uppercase">Đã xong</div>
                </div>
              </div>
            </div>

            <!-- Documents & Assets -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div class="p-6 bg-slate-50 rounded-4xl border border-slate-100">
                <h4
                  class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-4 flex items-center justify-between">
                  Hồ sơ đầu vào
                  <span class="text-indigo-600 cursor-pointer hover:underline">+ Thêm</span>
                </h4>
                <div v-if="selectedRecord.documents?.length" class="space-y-3">
                  <div v-for="doc in selectedRecord.documents" :key="doc.id"
                    class="flex items-center justify-between text-xs font-bold bg-white p-2.5 rounded-xl border border-slate-100 shadow-sm">
                    <span class="truncate">{{ doc.documentName }}</span>
                    <div class="flex items-center gap-2">
                      <span v-if="doc.received" class="text-emerald-500 text-[10px]">Đã nhận</span>
                      <Eye class="w-4 h-4 text-slate-400 cursor-pointer hover:text-indigo-600" />
                    </div>
                  </div>
                </div>
                <div v-else class="py-4 text-center text-xs text-slate-400 italic">Chưa có hồ sơ nào</div>
              </div>

              <div
                class="p-6 bg-indigo-600 rounded-4xl text-white shadow-xl shadow-indigo-100 overflow-hidden relative">
                <div class="relative z-10">
                  <h4
                    class="text-[10px] font-black text-indigo-200 uppercase tracking-widest mb-4 flex items-center justify-between">
                    Trang thiết bị
                    <span class="text-white cursor-pointer hover:underline">+ Cấp phát</span>
                  </h4>
                  <div v-if="selectedRecord.assets?.length" class="space-y-2">
                    <div v-for="asset in selectedRecord.assets" :key="asset.id"
                      class="flex items-center justify-between text-xs font-bold bg-white/10 p-2.5 rounded-xl">
                      <span class="truncate">{{ asset.assetName }}</span>
                      <span class="text-[10px] opacity-70">{{ asset.specifications }}</span>
                    </div>
                  </div>
                  <div v-else class="py-4 text-center text-xs text-indigo-200 italic">Chưa có thiết bị nào</div>
                </div>
                <Laptop class="absolute -right-4 -bottom-4 w-24 h-24 opacity-10 rotate-12" />
              </div>
            </div>

          </div>

          <!-- Sticky Actions -->
          <div
            class="p-8 border-t border-slate-50 flex items-center gap-4 shrink-0 bg-white shadow-[0_-10px_30px_rgba(0,0,0,0.02)]">
            <BaseButton v-if="selectedRecord.onboardingStatus === 'READY_FOR_JOIN'" variant="primary" size="lg" shadow
              class="flex-1 rounded-2xl! font-bold" @click="handleComplete(selectedRecord)">
              <CheckCheck class="w-5 h-5 mr-2" /> Chốt hoàn tất Onboarding
            </BaseButton>
            <BaseButton v-if="selectedRecord.onboardingStatus === 'READY_FOR_JOIN'" variant="outline" size="lg"
              class="flex-1 rounded-2xl! font-bold" @click="handleNotify(selectedRecord)">
              <Send class="w-5 h-5 mr-2" /> Gửi mail chào mừng
            </BaseButton>

            <div v-if="selectedRecord.onboardingStatus === 'COMPLETED'"
              class="w-full text-center py-4 bg-emerald-50 text-emerald-700 rounded-2xl font-black uppercase text-xs tracking-widest">
              Quy trình đã hoàn thành 100%
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
</style>
