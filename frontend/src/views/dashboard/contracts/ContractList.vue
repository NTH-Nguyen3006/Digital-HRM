<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { CalendarRange, ChevronLeft, ChevronRight, FileSignature, Plus, Search, ShieldCheck, TimerReset } from 'lucide-vue-next'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import InsightCard from '@/components/hrm/InsightCard.vue'
import SurfacePanel from '@/components/hrm/SurfacePanel.vue'
import { getContracts, getExpiringContracts } from '@/api/admin/contract'
import { useDebounce } from '@/composables/useDebounce'
import { useToast } from '@/composables/useToast'
import { unwrapPage } from '@/utils/api'
import { formatDate, humanizeStatus } from '@/utils/format'

const router = useRouter()
const toast = useToast()

const loading = ref(false)
const searchKeyword = ref('')
const contracts = ref([])
const expiringContracts = ref([])
const pagination = ref({ totalElements: 0, totalPages: 0 })
const currentPage = ref(0)
const pageSize = ref(9)

const { debouncedValue: debouncedKeyword } = useDebounce(searchKeyword, 400)

const statCards = computed(() => {
  const active = contracts.value.filter((item) => item.contractStatus === 'ACTIVE').length
  const draft = contracts.value.filter((item) => item.contractStatus === 'DRAFT').length

  return [
    {
      title: 'Tổng hợp đồng',
      value: pagination.value.totalElements || contracts.value.length,
      subtitle: 'Đang theo dõi trong hệ thống',
      icon: FileSignature,
      tone: 'indigo',
    },
    {
      title: 'Đang hiệu lực',
      value: active,
      subtitle: 'Sẵn sàng phục vụ vận hành',
      icon: ShieldCheck,
      tone: 'emerald',
    },
    {
      title: 'Bản nháp',
      value: draft,
      subtitle: 'Cần rà soát và phát hành',
      icon: TimerReset,
      tone: 'amber',
    },
    {
      title: 'Sắp hết hạn',
      value: expiringContracts.value.length,
      subtitle: 'Cần gia hạn hoặc tái ký',
      icon: CalendarRange,
      tone: 'rose',
    },
  ]
})

const visiblePages = computed(() => {
  const total = pagination.value.totalPages
  if (total <= 7) return Array.from({ length: total }, (_, index) => index)

  const current = currentPage.value
  if (current <= 3) return [0, 1, 2, 3, 4, '...', total - 1]
  if (current >= total - 4) return [0, '...', total - 5, total - 4, total - 3, total - 2, total - 1]
  return [0, '...', current - 1, current, current + 1, '...', total - 1]
})

async function fetchContracts() {
  loading.value = true
  try {
    const [listResponse, expiringResponse] = await Promise.all([
      getContracts({
        keyword: debouncedKeyword.value?.trim() || undefined,
        page: currentPage.value,
        size: pageSize.value,
      }),
      getExpiringContracts(45),
    ])

    const page = unwrapPage(listResponse)
    contracts.value = page.items
    pagination.value = {
      totalElements: page.totalElements,
      totalPages: page.totalPages,
    }
    expiringContracts.value = Array.isArray(expiringResponse?.data) ? expiringResponse.data : []
  } catch (error) {
    console.error('Failed to fetch contracts:', error)
    contracts.value = []
    expiringContracts.value = []
    pagination.value = { totalElements: 0, totalPages: 0 }
    toast.error('Không thể tải danh sách hợp đồng')
  } finally {
    loading.value = false
  }
}

function changePage(page) {
  if (page < 0 || page >= pagination.value.totalPages || page === currentPage.value) return
  currentPage.value = page
  fetchContracts()
}

function openDetail(contractId) {
  router.push(`/contracts/${contractId}`)
}

onMounted(fetchContracts)
watch(debouncedKeyword, () => {
  if (currentPage.value !== 0) {
    currentPage.value = 0
  }
  fetchContracts()
})
</script>

<template>
  <div class="space-y-8">
    <PageHeader
      title="Hợp đồng lao động"
      subtitle="Theo dõi vòng đời hợp đồng, nháp gia hạn và các hồ sơ cần xử lý sớm."
      :icon="FileSignature"
    >
      <template #actions>
        <div class="relative">
          <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="Tìm theo mã hợp đồng, nhân sự..."
            class="w-72 rounded-2xl border border-slate-200 bg-white py-3 pl-10 pr-4 text-sm font-medium shadow-sm outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10"
          >
        </div>

        <BaseButton variant="primary" size="md" @click="router.push('/contracts/add')">
          <Plus class="mr-2 h-4 w-4" />
          Tạo hợp đồng
        </BaseButton>
      </template>
    </PageHeader>

    <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
      <InsightCard
        v-for="item in statCards"
        :key="item.title"
        :title="item.title"
        :value="item.value"
        :subtitle="item.subtitle"
        :icon="item.icon"
        :tone="item.tone"
      />
    </div>

    <SurfacePanel>
      <div class="mb-5 flex items-center justify-between gap-4">
        <div>
          <h3 class="text-xl font-black text-slate-900">Danh sách hợp đồng</h3>
          <p class="mt-1 text-sm font-medium text-slate-500">Thiết kế dạng card để đọc nhanh từng hồ sơ thay vì bảng dày đặc.</p>
        </div>
        <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-black uppercase tracking-[0.18em] text-slate-500">
          {{ pagination.totalElements || contracts.length }} hồ sơ
        </span>
      </div>

      <div v-if="loading" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <div v-for="item in 6" :key="item" class="h-48 animate-pulse rounded-[28px] bg-slate-100" />
      </div>

      <div v-else-if="contracts.length" class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <button
          v-for="item in contracts"
          :key="item.laborContractId"
          type="button"
          class="group rounded-[28px] border border-slate-200 bg-white p-5 text-left transition-all hover:-translate-y-1 hover:border-indigo-200 hover:shadow-[0_20px_50px_rgba(99,102,241,0.10)]"
          @click="openDetail(item.laborContractId)"
        >
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">{{ item.contractNumber }}</p>
              <h4 class="mt-3 text-lg font-black text-slate-900 transition-colors group-hover:text-indigo-600">
                {{ item.employeeFullName }}
              </h4>
              <p class="mt-1 text-sm font-medium text-slate-500">{{ item.employeeCode }} · {{ item.orgUnitName || 'Chưa gán đơn vị' }}</p>
            </div>
            <StatusBadge :status="item.contractStatus" :label="humanizeStatus(item.contractStatus)" />
          </div>

          <div class="mt-5 grid gap-4 rounded-[24px] bg-slate-50 p-4 md:grid-cols-2">
            <div>
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Loại hợp đồng</p>
              <p class="mt-2 text-sm font-bold text-slate-800">{{ item.contractTypeName || 'Chưa cập nhật' }}</p>
            </div>
            <div>
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Ngày hiệu lực</p>
              <p class="mt-2 text-sm font-bold text-slate-800">{{ formatDate(item.effectiveDate) }}</p>
            </div>
            <div>
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Ngày hết hạn</p>
              <p class="mt-2 text-sm font-bold text-slate-800">{{ formatDate(item.endDate) }}</p>
            </div>
            <div>
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Mức lương cơ bản</p>
              <p class="mt-2 text-sm font-bold text-slate-800">
                {{ item.baseSalary ? new Intl.NumberFormat('vi-VN').format(item.baseSalary) + ' VND' : 'Chưa cập nhật' }}
              </p>
            </div>
          </div>
        </button>
      </div>

      <EmptyState
        v-else
        iconName="FileSearch"
        title="Không tìm thấy hợp đồng phù hợp"
        description="Thử thay đổi từ khóa hoặc tạo hồ sơ hợp đồng mới cho nhân sự."
        actionText="Tạo hợp đồng"
        @action="router.push('/contracts/add')"
      />

      <div v-if="pagination.totalPages > 1" class="mt-8 flex flex-wrap items-center justify-center gap-3">
        <BaseButton variant="outline" size="md" :disabled="currentPage === 0" class="w-12! rounded-2xl! p-0!"
          @click="changePage(currentPage - 1)">
          <ChevronLeft class="h-5 w-5" />
        </BaseButton>

        <div class="flex flex-wrap items-center justify-center gap-2">
          <template v-for="page in visiblePages" :key="`contract-page-${page}`">
            <button v-if="page !== '...'" type="button" class="h-10 w-10 rounded-2xl text-sm font-bold transition-all"
              :class="currentPage === page
                ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-100'
                : 'border border-slate-100 bg-white text-slate-500 hover:bg-indigo-50 hover:text-indigo-600'"
              @click="changePage(page)">
              {{ page + 1 }}
            </button>
            <span v-else class="px-1 font-bold text-slate-400">...</span>
          </template>
        </div>

        <BaseButton variant="outline" size="md" :disabled="currentPage === pagination.totalPages - 1"
          class="w-12! rounded-2xl! p-0!" @click="changePage(currentPage + 1)">
          <ChevronRight class="h-5 w-5" />
        </BaseButton>
      </div>
    </SurfacePanel>
  </div>
</template>
