<script setup>
import { computed, onMounted, ref } from 'vue'
import { CalendarRange, Download, FileText, Landmark, ShieldCheck } from 'lucide-vue-next'
import EmployeeLayout from '@/components/EmployeeLayout.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import InsightCard from '@/components/hrm/InsightCard.vue'
import SurfacePanel from '@/components/hrm/SurfacePanel.vue'
import { downloadMyContract, getMyContracts } from '@/api/me/portal'
import { useToast } from '@/composables/useToast'
import { downloadBlob, safeArray, unwrapData } from '@/utils/api'
import { formatCurrency, formatDate } from '@/utils/format'

const toast = useToast()
const loading = ref(false)
const downloadLoading = ref(null)
const contracts = ref([])

const stats = computed(() => [
  {
    title: 'Tổng hợp đồng',
    value: contracts.value.length,
    subtitle: 'Bao gồm hồ sơ hiện hành và lịch sử',
    icon: FileText,
    tone: 'indigo',
  },
  {
    title: 'Đang hiệu lực',
    value: contracts.value.filter((item) => item.contractStatus === 'ACTIVE').length,
    subtitle: 'Hồ sơ đang có hiệu lực pháp lý',
    icon: ShieldCheck,
    tone: 'emerald',
  },
  {
    title: 'Sắp hết hạn',
    value: contracts.value.filter((item) => item.contractStatus === 'EXPIRED').length,
    subtitle: 'Cần theo dõi gia hạn hoặc tái ký',
    icon: CalendarRange,
    tone: 'amber',
  },
  {
    title: 'Mức lương hiển thị',
    value: contracts.value[0]?.baseSalary ? formatCurrency(contracts.value[0].baseSalary) : '—',
    subtitle: 'Lấy từ hợp đồng mới nhất có dữ liệu',
    icon: Landmark,
    tone: 'rose',
  },
])

async function fetchContracts() {
  loading.value = true
  try {
    const response = await getMyContracts()
    contracts.value = safeArray(unwrapData(response)?.contracts)
  } catch (error) {
    console.error('Failed to fetch my contracts:', error)
    contracts.value = []
    toast.error('Không thể tải hợp đồng cá nhân')
  } finally {
    loading.value = false
  }
}

async function handleDownload(contractId) {
  downloadLoading.value = contractId
  try {
    const blob = await downloadMyContract(contractId)
    downloadBlob(blob, `my-contract-${contractId}.html`)
  } catch (error) {
    console.error('Failed to download contract:', error)
    toast.error('Không thể tải hợp đồng')
  } finally {
    downloadLoading.value = null
  }
}

onMounted(fetchContracts)
</script>

<template>
  <EmployeeLayout>
    <div class="mx-auto max-w-6xl space-y-8 px-6 py-10">
      <div>
        <p class="text-[11px] font-black uppercase tracking-[0.2em] text-indigo-500">Employee Contract Space</p>
        <h1 class="mt-3 text-4xl font-black tracking-tight text-slate-900">Hợp đồng lao động của tôi</h1>
        <p class="mt-2 max-w-3xl text-sm font-medium text-slate-500">
          Xem nhanh các hồ sơ đang hiệu lực, lịch sử hợp đồng và tải bản xuất trực tiếp từ backend.
        </p>
      </div>

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
        <div v-if="loading" class="grid gap-4 md:grid-cols-2">
          <div v-for="item in 4" :key="item" class="h-52 animate-pulse rounded-[28px] bg-slate-100" />
        </div>

        <div v-else-if="contracts.length" class="grid gap-5 md:grid-cols-2">
          <article
            v-for="item in contracts"
            :key="item.laborContractId"
            class="rounded-[28px] border border-slate-200 bg-white p-5 transition-all hover:-translate-y-0.5 hover:border-indigo-200 hover:shadow-lg"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">{{ item.contractNumber }}</p>
                <h3 class="mt-3 text-xl font-black text-slate-900">{{ item.contractTypeName || 'Hợp đồng lao động' }}</h3>
                <p class="mt-1 text-sm font-medium text-slate-500">{{ item.orgUnitName || 'Chưa gán phòng ban' }}</p>
              </div>
              <StatusBadge :status="item.contractStatus || 'ACTIVE'" />
            </div>

            <div class="mt-5 grid gap-4 rounded-[24px] bg-slate-50 p-4 md:grid-cols-2">
              <div>
                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Ngày hiệu lực</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ formatDate(item.effectiveDate) }}</p>
              </div>
              <div>
                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Ngày hết hạn</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ formatDate(item.endDate) }}</p>
              </div>
              <div>
                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Vị trí</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ item.jobTitleName || 'Chưa cập nhật' }}</p>
              </div>
              <div>
                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Lương cơ bản</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ item.baseSalary ? formatCurrency(item.baseSalary) : 'Chưa hiển thị' }}</p>
              </div>
            </div>

            <button
              type="button"
              class="mt-5 inline-flex items-center gap-2 rounded-2xl bg-indigo-600 px-4 py-3 text-sm font-black text-white transition hover:bg-indigo-700 disabled:opacity-60"
              :disabled="downloadLoading === item.laborContractId"
              @click="handleDownload(item.laborContractId)"
            >
              <Download class="h-4 w-4" />
              {{ downloadLoading === item.laborContractId ? 'Đang tải...' : 'Tải bản xuất hợp đồng' }}
            </button>
          </article>
        </div>

        <EmptyState
          v-else
          iconName="FileText"
          title="Chưa có hợp đồng cá nhân"
          description="Hồ sơ hợp đồng sẽ xuất hiện tại đây ngay khi backend có dữ liệu liên kết với tài khoản của bạn."
        />
      </SurfacePanel>
    </div>
  </EmployeeLayout>
</template>
