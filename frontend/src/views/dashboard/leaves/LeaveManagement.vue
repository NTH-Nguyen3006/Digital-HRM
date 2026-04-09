<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { CalendarDays, Check, Download, Search, Wallet, X } from 'lucide-vue-next'
import AvatarBox from '@/components/common/AvatarBox.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InsightCard from '@/components/hrm/InsightCard.vue'
import SurfacePanel from '@/components/hrm/SurfacePanel.vue'
import {
  exportLeaveReports,
  finalizeLeaveRequest,
  getLeaveBalances,
  getLeaveRequests,
} from '@/api/admin/leave'
import { useToast } from '@/composables/useToast'
import { downloadBlob, safeArray, unwrapPage } from '@/utils/api'
import { formatDate, formatNumber } from '@/utils/format'

const toast = useToast()

const activeTab = ref('requests')
const searchQuery = ref('')
const statusFilter = ref('ALL')
const loading = ref(false)

const requests = ref([])
const balances = ref([])

const stats = computed(() => {
  const pending = requests.value.filter((item) => ['SUBMITTED', 'APPROVED'].includes(item.requestStatus)).length
  const approved = requests.value.filter((item) => item.requestStatus === 'FINALIZED').length
  const rejected = requests.value.filter((item) => item.requestStatus === 'REJECTED').length
  const balanceCount = balances.value.length

  return [
    {
      title: 'Chờ xử lý',
      value: pending,
      subtitle: 'Đơn phép đang đợi HR duyệt',
      tone: 'amber',
      icon: CalendarDays,
    },
    {
      title: 'Đã duyệt',
      value: approved,
      subtitle: 'Hồ sơ đã hoàn tất trong tập dữ liệu hiện tại',
      tone: 'emerald',
      icon: Check,
    },
    {
      title: 'Từ chối',
      value: rejected,
      subtitle: 'Cần theo dõi lý do và phản hồi',
      tone: 'rose',
      icon: X,
    },
    {
      title: 'Quỹ phép',
      value: balanceCount,
      subtitle: 'Bản ghi quỹ phép đang theo dõi',
      tone: 'indigo',
      icon: Wallet,
    },
  ]
})

async function fetchData() {
  loading.value = true
  try {
    if (activeTab.value === 'requests') {
      const response = await getLeaveRequests({
        keyword: searchQuery.value || undefined,
        status: statusFilter.value !== 'ALL' ? statusFilter.value : undefined,
      })
      requests.value = unwrapPage(response).items
    } else {
      const response = await getLeaveBalances({
        keyword: searchQuery.value || undefined,
      })
      balances.value = unwrapPage(response).items
    }
  } catch (error) {
    console.error('Failed to fetch leave data:', error)
    requests.value = []
    balances.value = []
    toast.error('Không thể tải dữ liệu nghỉ phép')
  } finally {
    loading.value = false
  }
}

async function handleFinalize(item, approved) {
  try {
    await finalizeLeaveRequest(item.leaveRequestId, {
      approved,
      note: approved ? 'HR đã chốt hiệu lực đơn nghỉ từ workspace.' : 'Đơn nghỉ chưa đáp ứng điều kiện để chốt.',
    })
    toast.success(approved ? 'Đã chốt duyệt đơn nghỉ' : 'Đã từ chối đơn nghỉ')
    await fetchData()
  } catch (error) {
    console.error('Finalize leave request failed:', error)
    toast.error('Không thể cập nhật trạng thái đơn nghỉ')
  }
}

async function handleExport() {
  try {
    const blob = await exportLeaveReports({
      keyword: searchQuery.value || undefined,
      status: statusFilter.value !== 'ALL' ? statusFilter.value : undefined,
    })
    downloadBlob(blob, `leave-report-${new Date().toISOString().slice(0, 10)}.csv`)
  } catch (error) {
    console.error('Leave export failed:', error)
    toast.error('Xuất báo cáo thất bại')
  }
}

function getBalancePercent(item) {
  const total = Number(item.openingUnits || 0) + Number(item.accruedUnits || 0) + Number(item.adjustedUnits || 0) + Number(item.carriedForwardUnits || 0)
  const used = Number(item.usedUnits || 0) + Number(item.settledUnits || 0)
  if (!total) return 0
  return Math.min(100, Math.round((used / total) * 100))
}

onMounted(fetchData)
watch([activeTab, statusFilter], fetchData)
watch(searchQuery, fetchData)
</script>

<template>
  <div class="space-y-8">
    <PageHeader
      title="Quản lý nghỉ phép"
      subtitle="HR theo dõi đơn nghỉ, quỹ phép và các trường hợp cần xử lý nhanh."
      :icon="CalendarDays"
    >
      <template #actions>
        <BaseButton variant="outline" @click="handleExport">
          <Download class="mr-2 h-4 w-4" />
          Xuất báo cáo
        </BaseButton>
      </template>
    </PageHeader>

    <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
      <InsightCard
        v-for="item in stats"
        :key="item.title"
        :title="item.title"
        :value="item.value"
        :subtitle="item.subtitle"
        :icon="item.icon"
        :tone="item.tone"
      />
    </div>

    <SurfacePanel>
      <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
        <div class="flex flex-wrap items-center gap-2 rounded-2xl bg-slate-100 p-1">
          <button
            type="button"
            class="rounded-2xl px-4 py-2 text-sm font-black transition-all"
            :class="activeTab === 'requests' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
            @click="activeTab = 'requests'"
          >
            Đơn nghỉ cần duyệt
          </button>
          <button
            type="button"
            class="rounded-2xl px-4 py-2 text-sm font-black transition-all"
            :class="activeTab === 'balances' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
            @click="activeTab = 'balances'"
          >
            Quỹ phép nhân sự
          </button>
        </div>

        <div class="flex flex-col gap-3 md:flex-row">
          <div v-if="activeTab === 'requests'" class="flex flex-wrap gap-2">
            <button
              v-for="status in ['ALL', 'SUBMITTED', 'APPROVED', 'FINALIZED', 'REJECTED']"
              :key="status"
              type="button"
              class="rounded-full px-3 py-2 text-xs font-black uppercase tracking-[0.18em] transition-all"
              :class="statusFilter === status ? 'bg-indigo-50 text-indigo-700' : 'bg-slate-100 text-slate-500 hover:text-slate-800'"
              @click="statusFilter = status"
            >
              {{
                status === 'ALL'
                  ? 'Tất cả'
                  : status === 'SUBMITTED'
                    ? 'Chờ duyệt'
                    : status === 'APPROVED'
                      ? 'QL đã duyệt'
                      : status === 'FINALIZED'
                        ? 'Đã chốt'
                        : 'Từ chối'
              }}
            </button>
          </div>

          <div class="relative">
            <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm nhân sự hoặc loại nghỉ..."
              class="w-full rounded-2xl border border-slate-200 bg-white py-3 pl-10 pr-4 text-sm font-medium outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10 md:w-72"
            >
          </div>
        </div>
      </div>

      <div v-if="loading" class="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <div v-for="item in 6" :key="item" class="h-44 animate-pulse rounded-[28px] bg-slate-100" />
      </div>

      <div v-else-if="activeTab === 'requests'" class="mt-6 space-y-4">
        <div
          v-for="item in requests"
          :key="item.leaveRequestId"
          class="rounded-[28px] border border-slate-200 bg-white p-5 transition-all hover:border-indigo-200 hover:shadow-lg"
        >
          <div class="flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">
            <div class="flex items-start gap-4">
              <AvatarBox :name="item.employeeName" size="lg" shape="rounded-[20px]" />
              <div>
                <div class="flex flex-wrap items-center gap-2">
                  <h4 class="text-lg font-black text-slate-900">{{ item.employeeName }}</h4>
                  <StatusBadge :status="item.requestStatus" />
                </div>
                <p class="mt-1 text-sm font-medium text-slate-500">
                  {{ item.employeeCode || '—' }} · {{ item.leaveTypeName || 'Chưa có loại phép' }}
                </p>
                <p class="mt-3 max-w-2xl text-sm text-slate-600">Mã đơn: {{ item.requestCode || '—' }}</p>
              </div>
            </div>

            <div class="grid gap-4 md:grid-cols-3 xl:min-w-[420px]">
              <div class="rounded-2xl bg-slate-50 p-4">
                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Khoảng nghỉ</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ formatDate(item.startDate) }} - {{ formatDate(item.endDate) }}</p>
              </div>
              <div class="rounded-2xl bg-slate-50 p-4">
                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Số ngày</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ formatNumber(item.requestedUnits || 0) }}</p>
              </div>
              <div class="rounded-2xl bg-slate-50 p-4">
                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Ngày gửi</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ formatDate(item.submittedDate) }}</p>
              </div>
            </div>
          </div>

          <div v-if="['SUBMITTED', 'APPROVED'].includes(item.requestStatus)" class="mt-5 flex flex-wrap justify-end gap-3">
            <BaseButton variant="outline" @click="handleFinalize(item, false)">
              <X class="mr-2 h-4 w-4" />
              Từ chối
            </BaseButton>
            <BaseButton variant="primary" @click="handleFinalize(item, true)">
              <Check class="mr-2 h-4 w-4" />
              Duyệt đơn
            </BaseButton>
          </div>
        </div>

        <EmptyState
          v-if="!requests.length"
          iconName="CalendarCheck"
          title="Không có đơn nghỉ phù hợp"
          description="Không còn hồ sơ nào cần HR xử lý theo bộ lọc hiện tại."
        />
      </div>

      <div v-else class="mt-6 grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <div
          v-for="item in balances"
          :key="item.leaveBalanceId || `${item.employeeId}-${item.leaveTypeId}-${item.leaveYear}`"
          class="rounded-[28px] border border-slate-200 bg-white p-5 transition-all hover:border-indigo-200 hover:shadow-lg"
        >
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">{{ item.leaveTypeName }}</p>
              <h4 class="mt-3 text-lg font-black text-slate-900">{{ item.employeeName || `Nhân sự #${item.employeeId}` }}</h4>
              <p class="mt-1 text-sm font-medium text-slate-500">{{ item.employeeCode || 'Quỹ phép cá nhân' }}</p>
            </div>
            <StatusBadge :status="item.balanceStatus || 'OPEN'" />
          </div>

          <div class="mt-5 rounded-[24px] bg-slate-50 p-4">
            <div class="mb-3 flex items-center justify-between text-sm font-bold text-slate-700">
              <span>Đã dùng {{ formatNumber(item.usedUnits || 0) }}</span>
              <span>Còn {{ formatNumber(item.availableUnits || 0) }}</span>
            </div>
            <div class="h-3 overflow-hidden rounded-full bg-slate-200">
              <div class="h-full rounded-full bg-indigo-500" :style="{ width: `${getBalancePercent(item)}%` }" />
            </div>
            <div class="mt-3 flex items-center justify-between text-xs font-bold uppercase tracking-[0.16em] text-slate-400">
              <span>Phát sinh {{ formatNumber((item.openingUnits || 0) + (item.accruedUnits || 0)) }}</span>
              <span>Năm {{ item.leaveYear || '—' }}</span>
            </div>
          </div>
        </div>

        <div v-if="!balances.length" class="md:col-span-2 xl:col-span-3">
          <EmptyState
            iconName="Wallet"
            title="Chưa có quỹ phép để hiển thị"
            description="Không tìm thấy bản ghi quỹ phép nào theo điều kiện lọc hiện tại."
          />
        </div>
      </div>
    </SurfacePanel>
  </div>
</template>
