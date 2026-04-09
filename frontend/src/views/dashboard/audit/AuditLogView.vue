<script setup>
import { ref, onMounted, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { usePagination } from '@/composables/usePagination'
import { useDebounce } from '@/composables/useDebounce'
import { getAuditLogs } from '@/api/admin/auditLog'
import {
  History, Search, Filter, ChevronLeft, ChevronRight,
  CheckCircle2, XCircle, User, Loader2
} from 'lucide-vue-next'

/* ─── STATE ─────────────────────────────────────────────────── */
const logs = ref([])
const loading = ref(false)

const searchKeyword = ref('')
const { debouncedValue: debouncedSearch } = useDebounce(searchKeyword, 400)

const filterModule = ref('')
const filterAction = ref('')
const filterResult = ref('')
const filterFrom   = ref('')
const filterTo     = ref('')

const {
  currentPage, totalElements, totalPages, visiblePages,
  setPage, resetPage, setMeta, isFirstPage, isLastPage
} = usePagination({ initialSize: 20 })

/* ─── CONFIG ─────────────────────────────────────────────────── */
const MODULE_OPTIONS = [
  { value: '', label: 'Tất cả module' },
  { value: 'USER', label: 'Tài khoản' },
  { value: 'EMPLOYEE', label: 'Nhân sự' },
  { value: 'ROLE', label: 'Phân quyền' },
  { value: 'CONTRACT', label: 'Hợp đồng' },
  { value: 'LEAVE', label: 'Nghỉ phép' },
  { value: 'ATTENDANCE', label: 'Chấm công' },
  { value: 'PAYROLL', label: 'Lương' },
  { value: 'ONBOARDING', label: 'Tiếp nhận' },
  { value: 'OFFBOARDING', label: 'Thôi việc' },
]

const ACTION_OPTIONS = [
  { value: '', label: 'Tất cả hành động' },
  { value: 'CREATE', label: 'Tạo mới' },
  { value: 'UPDATE', label: 'Cập nhật' },
  { value: 'DELETE', label: 'Xóa' },
  { value: 'LOGIN', label: 'Đăng nhập' },
  { value: 'LOGOUT', label: 'Đăng xuất' },
  { value: 'APPROVE', label: 'Phê duyệt' },
  { value: 'REJECT', label: 'Từ chối' },
]

const moduleColorMap = {
  USER: 'bg-indigo-100 text-indigo-700',
  EMPLOYEE: 'bg-emerald-100 text-emerald-700',
  ROLE: 'bg-violet-100 text-violet-700',
  CONTRACT: 'bg-sky-100 text-sky-700',
  LEAVE: 'bg-amber-100 text-amber-700',
  ATTENDANCE: 'bg-cyan-100 text-cyan-700',
  PAYROLL: 'bg-green-100 text-green-700',
  ONBOARDING: 'bg-blue-100 text-blue-700',
  OFFBOARDING: 'bg-rose-100 text-rose-700',
}

function getModuleColor(module) {
  return moduleColorMap[module] || 'bg-slate-100 text-slate-600'
}

function formatDateTime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

/* ─── API ────────────────────────────────────────────────────── */
const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await getAuditLogs({
      actorUsername: debouncedSearch.value || undefined,
      moduleCode: filterModule.value || undefined,
      actionCode: filterAction.value || undefined,
      resultCode: filterResult.value || undefined,
      from: filterFrom.value ? filterFrom.value + 'T00:00:00' : undefined,
      to: filterTo.value ? filterTo.value + 'T23:59:59' : undefined,
      page: currentPage.value,
      size: 20,
    })
    logs.value = res.data?.items || []
    setMeta({
      totalElements: res.data?.totalElements ?? logs.value.length,
      totalPages: res.data?.totalPages ?? 1,
    })
  } catch (e) {
    console.error('fetchLogs failed:', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchLogs)

watch([debouncedSearch, filterModule, filterAction, filterResult, filterFrom, filterTo], () => {
  resetPage()
  fetchLogs()
})

watch(currentPage, fetchLogs)
</script>

<template>
  
    <div class="space-y-8 animate-fade-in">

      <!-- HEADER -->
      <PageHeader
        title="Nhật ký Hoạt động"
        subtitle="Theo dõi toàn bộ thao tác và sự kiện xảy ra trong hệ thống"
        :icon="History"
        iconColor="bg-slate-800"
        iconShadow="shadow-slate-200"
      />

      <!-- FILTERS -->
      <div class="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">

          <!-- Search -->
          <div class="xl:col-span-2 relative group">
            <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="Tìm theo username..."
              class="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-transparent rounded-2xl text-sm font-medium focus:bg-white focus:border-indigo-200 focus:ring-4 focus:ring-indigo-500/5 outline-none transition-all"
            />
          </div>

          <!-- Module filter -->
          <select
            v-model="filterModule"
            class="py-2.5 px-4 bg-slate-50 border border-transparent rounded-2xl text-sm font-medium text-slate-700 focus:bg-white focus:border-indigo-200 outline-none transition-all cursor-pointer"
          >
            <option v-for="opt in MODULE_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>

          <!-- Action filter -->
          <select
            v-model="filterAction"
            class="py-2.5 px-4 bg-slate-50 border border-transparent rounded-2xl text-sm font-medium text-slate-700 focus:bg-white focus:border-indigo-200 outline-none transition-all cursor-pointer"
          >
            <option v-for="opt in ACTION_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>

          <!-- Result filter -->
          <div class="flex items-center gap-2 p-1 bg-slate-50 rounded-2xl">
            <button
              v-for="r in [{ v:'', l:'Tất cả' },{ v:'SUCCESS', l:'Thành công' },{ v:'FAILURE', l:'Thất bại' }]"
              :key="r.v"
              @click="filterResult = r.v"
              class="flex-1 py-2 rounded-xl text-xs font-black transition-all"
              :class="filterResult === r.v ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-400 hover:text-slate-600'"
            >
              {{ r.l }}
            </button>
          </div>
        </div>

        <!-- Date range -->
        <div class="mt-4 flex flex-wrap items-center gap-4">
          <div class="flex items-center gap-2">
            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Từ</span>
            <input v-model="filterFrom" type="date"
              class="py-2 px-3 bg-slate-50 border border-transparent rounded-xl text-sm font-medium focus:bg-white focus:border-indigo-200 outline-none transition-all cursor-pointer" />
          </div>
          <div class="flex items-center gap-2">
            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Đến</span>
            <input v-model="filterTo" type="date"
              class="py-2 px-3 bg-slate-50 border border-transparent rounded-xl text-sm font-medium focus:bg-white focus:border-indigo-200 outline-none transition-all cursor-pointer" />
          </div>
          <span v-if="totalElements" class="text-xs font-bold text-slate-400 ml-auto">
            {{ totalElements }} bản ghi
          </span>
        </div>
      </div>

      <!-- LOG LIST -->
      <div class="bg-white border border-slate-100 rounded-3xl overflow-hidden shadow-sm relative min-h-[400px]">

        <!-- Loading -->
        <div v-if="loading" class="absolute inset-0 z-20 flex items-center justify-center bg-white/60 backdrop-blur-[1px]">
          <Loader2 class="w-10 h-10 text-slate-700 animate-spin" />
        </div>

        <!-- Items -->
        <div v-if="logs.length > 0" class="divide-y divide-slate-50">
          <div
            v-for="log in logs"
            :key="log.auditLogId"
            class="group px-8 py-5 hover:bg-slate-50/60 transition-all flex flex-col xl:flex-row xl:items-center gap-4"
          >
            <!-- Status icon -->
            <div class="shrink-0">
              <div
                class="w-10 h-10 rounded-2xl flex items-center justify-center shadow-sm"
                :class="log.resultCode === 'SUCCESS' ? 'bg-emerald-50' : 'bg-rose-50'"
              >
                <CheckCircle2 v-if="log.resultCode === 'SUCCESS'" class="w-5 h-5 text-emerald-500" />
                <XCircle v-else class="w-5 h-5 text-rose-500" />
              </div>
            </div>

            <!-- Main info -->
            <div class="flex-1 min-w-0">
              <div class="flex flex-wrap items-center gap-2 mb-1">
                <span class="text-[10px] font-black px-2 py-1 rounded-lg uppercase tracking-wider" :class="getModuleColor(log.moduleCode)">
                  {{ log.moduleCode }}
                </span>
                <span class="text-xs font-black text-slate-700 bg-slate-100 px-2 py-1 rounded-lg">
                  {{ log.actionCode }}
                </span>
                <span v-if="log.entityName" class="text-xs text-slate-400 font-medium truncate">
                  → {{ log.entityName }} #{{ log.entityId }}
                </span>
              </div>
              <p class="text-sm font-medium text-slate-600 truncate">{{ log.message || 'Không có mô tả' }}</p>
            </div>

            <!-- Actor + Meta -->
            <div class="flex flex-wrap items-center gap-6 text-xs font-bold text-slate-500 shrink-0">
              <div class="flex items-center gap-2">
                <div class="w-7 h-7 rounded-xl bg-indigo-100 text-indigo-600 flex items-center justify-center font-black text-[10px]">
                  {{ (log.actorUsername || 'SYS')[0].toUpperCase() }}
                </div>
                <span>{{ log.actorUsername || 'System' }}</span>
              </div>
              <div class="text-slate-300">{{ log.ipAddress || '—' }}</div>
              <div class="text-slate-400 whitespace-nowrap">{{ formatDateTime(log.actionAt) }}</div>
            </div>
          </div>
        </div>

        <EmptyState
          v-else-if="!loading"
          title="Không có nhật ký nào"
          description="Không tìm thấy bản ghi nào phù hợp với bộ lọc hiện tại."
        />

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="border-t border-slate-50 px-8 py-4 flex items-center justify-between">
          <span class="text-xs font-bold text-slate-400">Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
          <div class="flex items-center gap-2">
            <button
              :disabled="isFirstPage"
              @click="setPage(currentPage - 1)"
              class="w-8 h-8 rounded-xl flex items-center justify-center transition-all disabled:opacity-30"
              :class="isFirstPage ? 'text-slate-300' : 'hover:bg-slate-100 text-slate-600'"
            >
              <ChevronLeft class="w-4 h-4" />
            </button>
            <template v-for="p in visiblePages" :key="p">
              <span v-if="p === '...'" class="text-slate-300 px-1 font-bold">...</span>
              <button
                v-else
                @click="setPage(p)"
                class="w-8 h-8 rounded-xl text-xs font-bold transition-all"
                :class="currentPage === p ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-100' : 'hover:bg-slate-100 text-slate-600'"
              >
                {{ p + 1 }}
              </button>
            </template>
            <button
              :disabled="isLastPage"
              @click="setPage(currentPage + 1)"
              class="w-8 h-8 rounded-xl flex items-center justify-center transition-all disabled:opacity-30"
              :class="isLastPage ? 'text-slate-300' : 'hover:bg-slate-100 text-slate-600'"
            >
              <ChevronRight class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

    </div>
  
</template>

<style scoped>
.animate-fade-in { animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
</style>
