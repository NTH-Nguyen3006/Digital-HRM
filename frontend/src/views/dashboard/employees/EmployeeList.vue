<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowRightLeft,
  Check,
  ChevronLeft,
  ChevronRight,
  Download,
  Filter,
  Loader2,
  Mail,
  MoreHorizontal,
  MoreVertical,
  Phone,
  Plus,
  Search,
  X,
} from 'lucide-vue-next'

import AvatarBox from '@/components/common/AvatarBox.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import { getEmployees, transferEmployee } from '@/api/admin/employee'
import { getOrgUnits } from '@/api/admin/orgUnit'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'
import { formatNumber } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const authStore = useAuthStore()
const isAdmin = authStore.isAdmin
const isHR = authStore.isHR
const canManage = isHR || isAdmin

const employees = ref([])
const orgUnitOptions = ref([])
const loading = ref(false)
const referenceLoading = ref(false)
const transferSubmitting = ref(false)

const searchKeyword = ref('')
const selectedOrgUnitId = ref(route.query.orgUnitId ? String(route.query.orgUnitId) : '')
const totalElements = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = ref(12)

const selectedEmployees = ref([])
const pageActionMenuOpen = ref(false)
const openCardMenuId = ref(null)
const transferModalOpen = ref(false)
const transferMode = ref('bulk')
const transferTargetIds = ref([])
const transferForm = ref({
  targetOrgUnitId: '',
  note: '',
})

const orgUnitQueryLabel = computed(() =>
  typeof route.query.orgUnitName === 'string' ? route.query.orgUnitName : '',
)

const activeOrgUnitLabel = computed(() => {
  const found = orgUnitOptions.value.find((item) => String(item.orgUnitId) === selectedOrgUnitId.value)
  return found?.orgUnitName || orgUnitQueryLabel.value || ''
})

const visiblePages = computed(() => {
  const total = totalPages.value
  if (total <= 7) return Array.from({ length: total }, (_, index) => index)

  const current = currentPage.value
  if (current <= 3) return [0, 1, 2, 3, 4, '...', total - 1]
  if (current >= total - 4) return [0, '...', total - 5, total - 4, total - 3, total - 2, total - 1]
  return [0, '...', current - 1, current, current + 1, '...', total - 1]
})

const selectedEmployeeCards = computed(() =>
  employees.value.filter((item) => selectedEmployees.value.includes(item.employeeId)),
)

const transferSummary = computed(() => {
  if (transferMode.value === 'single') {
    const employee = selectedEmployeeCards.value[0] || employees.value.find((item) => item.employeeId === transferTargetIds.value[0])
    return employee?.fullName || 'Nhân sự đã chọn'
  }

  return `${formatNumber(transferTargetIds.value.length)} nhân sự`
})

async function fetchOrgUnits() {
  referenceLoading.value = true
  try {
    const response = await getOrgUnits({ size: 200, status: 'ACTIVE' })
    const data = response?.data || response || {}
    orgUnitOptions.value = Array.isArray(data.items)
      ? data.items
      : Array.isArray(data.content)
        ? data.content
        : []
  } catch (error) {
    console.error('Failed to fetch org units:', error)
    orgUnitOptions.value = []
  } finally {
    referenceLoading.value = false
  }
}

async function fetchEmployees() {
  loading.value = true
  try {
    const response = await getEmployees({
      keyword: searchKeyword.value || undefined,
      orgUnitId: selectedOrgUnitId.value ? Number(selectedOrgUnitId.value) : undefined,
      page: currentPage.value,
      size: pageSize.value,
    })

    const pageData = response?.data || response || {}
    employees.value = Array.isArray(pageData.items)
      ? pageData.items
      : Array.isArray(pageData.content)
        ? pageData.content
        : []
    totalElements.value = Number(pageData.totalElements || 0)
    totalPages.value = Number(pageData.totalPages || 0)

    selectedEmployees.value = selectedEmployees.value.filter((id) =>
      employees.value.some((item) => item.employeeId === id),
    )
  } catch (error) {
    console.error('Failed to fetch employees:', error)
    employees.value = []
    totalElements.value = 0
    totalPages.value = 0
    toast.error('Không thể tải danh sách nhân sự')
  } finally {
    loading.value = false
  }
}

function exportToCSV() {
  if (employees.value.length === 0) return

  const headers = ['Mã NV', 'Họ Tên', 'Email', 'SĐT', 'Phòng ban', 'Chức danh', 'Trạng thái']
  const rows = employees.value.map((emp) => [
    emp.employeeCode,
    emp.fullName,
    emp.workEmail,
    emp.workPhone || '',
    emp.orgUnitName,
    emp.jobTitleName,
    emp.employmentStatus,
  ])

  const csvContent = [
    headers.join(','),
    ...rows.map((row) => row.map((cell) => `"${cell || ''}"`).join(',')),
  ].join('\n')

  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.setAttribute('href', url)
  link.setAttribute('download', `DS_NhanVien_${new Date().toISOString().split('T')[0]}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

function changePage(page) {
  if (page < 0 || page >= totalPages.value) return
  currentPage.value = page
  fetchEmployees()
}

function toggleSelect(id) {
  const index = selectedEmployees.value.indexOf(id)
  if (index === -1) selectedEmployees.value.push(id)
  else selectedEmployees.value.splice(index, 1)
}

function isSelected(id) {
  return selectedEmployees.value.includes(id)
}

function clearSelected() {
  selectedEmployees.value = []
}

function clearOrgUnitFilter() {
  selectedOrgUnitId.value = ''
}

function openEmployeeMenu(employeeId) {
  openCardMenuId.value = openCardMenuId.value === employeeId ? null : employeeId
}

function openTransferModal(mode, employeeIds = []) {
  if (!employeeIds.length) {
    toast.info('Chọn ít nhất một nhân sự để điều chuyển')
    return
  }

  transferMode.value = mode
  transferTargetIds.value = employeeIds
  transferForm.value = {
    targetOrgUnitId: '',
    note: '',
  }
  transferModalOpen.value = true
  openCardMenuId.value = null
  pageActionMenuOpen.value = false
}

async function submitTransfer() {
  if (!transferForm.value.targetOrgUnitId) {
    toast.warning('Vui lòng chọn phòng ban đích')
    return
  }

  transferSubmitting.value = true
  try {
    const payload = {
      targetOrgUnitId: Number(transferForm.value.targetOrgUnitId),
      note: transferForm.value.note?.trim() || 'Điều chuyển từ màn Hồ sơ nhân sự.',
    }

    const results = await Promise.allSettled(
      transferTargetIds.value.map((employeeId) => transferEmployee(employeeId, payload)),
    )

    const successCount = results.filter((item) => item.status === 'fulfilled').length
    const failedCount = results.length - successCount

    if (successCount) {
      toast.success(
        failedCount
          ? `Đã điều chuyển ${successCount} nhân sự, còn ${failedCount} trường hợp chưa thành công.`
          : `Đã điều chuyển ${successCount} nhân sự thành công.`,
      )
    }

    if (failedCount && !successCount) {
      toast.error('Không thể điều chuyển nhân sự đã chọn')
    }

    transferModalOpen.value = false
    selectedEmployees.value = selectedEmployees.value.filter(
      (id) => !transferTargetIds.value.includes(id),
    )
    await fetchEmployees()
  } catch (error) {
    console.error('Transfer employees failed:', error)
    toast.error('Điều chuyển nhân sự thất bại')
  } finally {
    transferSubmitting.value = false
  }
}

watch(searchKeyword, () => {
  currentPage.value = 0
  fetchEmployees()
})

watch(selectedOrgUnitId, (value) => {
  currentPage.value = 0

  const nextQuery = {
    ...route.query,
    orgUnitId: value || undefined,
    orgUnitName: value
      ? orgUnitOptions.value.find((item) => String(item.orgUnitId) === value)?.orgUnitName || route.query.orgUnitName
      : undefined,
  }

  router.replace({ path: route.path, query: nextQuery })
  fetchEmployees()
})

watch(
  () => route.query.orgUnitId,
  (value) => {
    const normalized = value ? String(value) : ''
    if (normalized === selectedOrgUnitId.value) return
    selectedOrgUnitId.value = normalized
  },
)

onMounted(async () => {
  await fetchOrgUnits()
  await fetchEmployees()
})
</script>

<template>
  <div class="space-y-8 animate-fade-in">
    <div class="flex flex-col gap-6 xl:flex-row xl:items-end xl:justify-between">
      <div>
        <h2 class="text-3xl font-black tracking-tight text-slate-900 sm:text-4xl">Hồ sơ Nhân sự</h2>
        <div class="mt-2 flex flex-wrap items-center gap-2 text-sm font-medium text-slate-500">
          <span>
            Quản lý và theo dõi thông tin chi tiết của
            <span class="rounded-md bg-indigo-50 px-2 py-0.5 font-bold text-indigo-700">{{ totalElements }} thành
              viên</span>
          </span>
          <span v-if="activeOrgUnitLabel"
            class="inline-flex flex-wrap items-center gap-2 rounded-full border border-indigo-100 bg-indigo-50 px-3 py-1.5 text-xs text-indigo-700">
            <span class="font-black uppercase tracking-[0.16em] text-indigo-500">Bộ lọc từ cơ cấu tổ chức</span>
            <span class="rounded-full bg-white px-2.5 py-0.5 text-sm font-bold normal-case tracking-normal text-indigo-700 shadow-sm shadow-indigo-100/70">
              {{ activeOrgUnitLabel }}
            </span>
          </span>
        </div>
      </div>

      <div class="flex flex-col gap-3 md:flex-row md:flex-wrap md:items-center md:justify-end">
        <div class="relative group min-w-0 flex-1 md:min-w-[16rem] md:max-w-72">
          <Search
            class="absolute left-3.5 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400 transition-colors group-focus-within:text-indigo-500" />
          <input v-model="searchKeyword" type="text" placeholder="Họ tên, mã NV hoặc email..."
            class="w-full rounded-2xl border border-slate-200 bg-white py-3 pl-11 pr-4 text-sm font-medium shadow-sm outline-none transition-all focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10">
        </div>

        <div class="relative min-w-0 flex-1 md:min-w-[16rem] md:max-w-72">
          <Filter class="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-300" />
          <select v-model="selectedOrgUnitId"
            class="w-full rounded-2xl border border-slate-200 bg-white py-3 pl-12 pr-10 text-sm font-medium shadow-sm outline-none transition-all focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10">
            <option value="">Tất cả phòng ban</option>
            <option v-for="item in orgUnitOptions" :key="item.orgUnitId" :value="String(item.orgUnitId)">
              {{ item.orgUnitName }}
            </option>
          </select>
        </div>

        <div class="relative shrink-0 self-start md:self-auto">
          <button type="button"
            class="flex h-12 items-center justify-center rounded-2xl border border-slate-200 bg-white px-4 text-slate-600 shadow-sm transition-all hover:border-indigo-200 hover:text-indigo-600"
            @click="pageActionMenuOpen = !pageActionMenuOpen">
            <MoreHorizontal class="h-5 w-5" />
          </button>

          <div v-if="pageActionMenuOpen"
            class="absolute right-0 top-[calc(100%+0.5rem)] z-30 w-64 rounded-[24px] border border-slate-200 bg-white p-2 shadow-2xl shadow-slate-900/8">
            <button type="button"
              class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-bold text-slate-700 transition hover:bg-slate-50"
              :disabled="!selectedEmployees.length" @click="openTransferModal('bulk', [...selectedEmployees])">
              <ArrowRightLeft class="h-4 w-4 text-indigo-500" />
              Điều chuyển hàng loạt
            </button>
            <button type="button"
              class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-bold text-slate-700 transition hover:bg-slate-50"
              @click="exportToCSV(); pageActionMenuOpen = false">
              <Download class="h-4 w-4 text-emerald-500" />
              Xuất danh sách hiện tại
            </button>
          </div>
        </div>

        <BaseButton v-if="canManage" variant="primary" size="lg" shadow
          class="h-12.5! w-full shrink-0 whitespace-nowrap rounded-2xl! px-6! font-bold sm:w-auto">
          <Plus class="mr-2 h-5 w-5" />
          Thêm mới
        </BaseButton>
      </div>
    </div>

    <div v-if="activeOrgUnitLabel"
      class="flex flex-wrap items-center justify-between gap-3 rounded-[28px] border border-indigo-100 bg-indigo-50/70 px-5 py-4">
      <div>
        <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">Bộ lọc hiện tại</p>
        <p class="mt-1 text-sm font-bold text-slate-800">Đang xem nhân sự thuộc đơn vị {{ activeOrgUnitLabel }}</p>
      </div>
      <button type="button"
        class="inline-flex items-center gap-2 rounded-2xl border border-indigo-200 bg-white px-4 py-2 text-sm font-bold text-indigo-700 transition hover:bg-indigo-50"
        @click="clearOrgUnitFilter">
        <X class="h-4 w-4" />
        Xóa bộ lọc
      </button>
    </div>

    <div class="relative min-h-[24rem]">
      <div v-if="loading" class="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
        <div v-for="item in pageSize" :key="item" class="h-[300px] animate-pulse rounded-[28px] bg-slate-100" />
      </div>

      <div v-else-if="employees.length" class="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
        <div v-for="emp in employees" :key="emp.employeeId"
          class="group relative rounded-[28px] border border-slate-200 bg-white p-6 transition-all duration-300 hover:border-indigo-200 hover:shadow-[0_20px_40px_rgba(79,70,229,0.08)]">
          <button v-if="canManage" type="button"
            class="absolute left-4 top-4 flex h-6 w-6 items-center justify-center rounded-xl border shadow-sm transition-all duration-200 ease-out"
            :class="isSelected(emp.employeeId)
              ? 'scale-100 border-indigo-600 bg-indigo-600 text-white shadow-indigo-200'
              : 'scale-90 border-slate-200 bg-white/95 text-transparent opacity-0 group-hover:scale-100 group-hover:border-indigo-200 group-hover:text-slate-300 group-hover:opacity-100'"
            :aria-pressed="isSelected(emp.employeeId)" title="Chọn nhân sự" @click.stop="toggleSelect(emp.employeeId)">
            <Check class="h-4 w-4 transition-transform duration-200"
              :class="isSelected(emp.employeeId) ? 'scale-100' : 'scale-75'" />
          </button>

          <div class="absolute right-4 top-4">
            <button type="button"
              class="rounded-xl p-2 text-slate-300 transition-colors hover:bg-slate-100 hover:text-indigo-600"
              @click.stop="openEmployeeMenu(emp.employeeId)">
              <MoreVertical class="h-5 w-5" />
            </button>

            <div v-if="openCardMenuId === emp.employeeId"
              class="absolute right-0 top-[calc(100%+0.5rem)] z-30 w-60 rounded-[24px] border border-slate-200 bg-white p-2 shadow-2xl shadow-slate-900/8">
              <button type="button"
                class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-bold text-slate-700 transition hover:bg-slate-50"
                @click="router.push(`/employees/${emp.employeeId}`); openCardMenuId = null">
                <ArrowRightLeft class="h-4 w-4 rotate-180 text-slate-400" />
                Mở hồ sơ chi tiết
              </button>
              <button v-if="canManage" type="button"
                class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-bold text-slate-700 transition hover:bg-slate-50"
                @click="openTransferModal('single', [emp.employeeId])">
                <ArrowRightLeft class="h-4 w-4 text-indigo-500" />
                Chuyển phòng ban
              </button>
              <button v-if="canManage" type="button"
                class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-bold text-slate-700 transition hover:bg-slate-50"
                @click="toggleSelect(emp.employeeId); openCardMenuId = null">
                <Check class="h-4 w-4 text-emerald-500" />
                {{ isSelected(emp.employeeId) ? 'Bỏ chọn nhân sự' : 'Chọn vào danh sách' }}
              </button>
            </div>
          </div>

          <div class="flex flex-col items-center">
            <AvatarBox :name="emp.fullName" :image="emp.avatarUrl" size="xl" shape="rounded-3xl" />

            <h3
              class="mt-4 text-center text-lg font-bold tracking-tight text-slate-900 transition-colors group-hover:text-indigo-600">
              {{ emp.fullName }}
            </h3>
            <p class="text-xs font-bold uppercase tracking-widest text-slate-400">{{ emp.employeeCode }}</p>

            <div class="mt-4 flex w-full flex-wrap justify-center gap-2">
              <span class="rounded-full bg-slate-100 px-3 py-1 text-[11px] font-bold text-slate-600">
                {{ emp.jobTitleName || 'Chưa định danh' }}
              </span>
              <span class="rounded-full bg-indigo-50 px-3 py-1 text-[11px] font-bold text-indigo-700">
                {{ emp.orgUnitName }}
              </span>
            </div>
          </div>

          <div class="mt-6 space-y-3 border-t border-slate-50 pt-6">
            <div class="flex items-center gap-3 text-sm font-medium text-slate-600">
              <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-50 text-emerald-600">
                <Mail class="h-4 w-4" />
              </div>
              <span class="truncate">{{ emp.workEmail || 'N/A' }}</span>
            </div>
            <div class="flex items-center gap-3 text-sm font-medium text-slate-600">
              <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-50 text-blue-600">
                <Phone class="h-4 w-4" />
              </div>
              <span>{{ emp.workPhone || 'N/A' }}</span>
            </div>
          </div>

          <div class="mt-6 flex items-center justify-between gap-3">
            <StatusBadge :status="emp.employmentStatus" />
            <router-link :to="`/employees/${emp.employeeId}`"
              class="text-xs font-bold uppercase tracking-wider text-indigo-600 hover:text-indigo-700">
              Hồ sơ &rarr;
            </router-link>
          </div>
        </div>
      </div>

      <EmptyState v-else title="Không tìm thấy nhân viên"
        description="Không có nhân sự nào phù hợp với bộ lọc hiện tại hoặc từ khóa tìm kiếm của bạn."
        actionText="Điều chỉnh bộ lọc" />
    </div>

    <div v-if="totalPages > 1" class="flex flex-wrap items-center justify-center gap-3 pb-12">
      <BaseButton variant="outline" size="md" :disabled="currentPage === 0" class="w-12! rounded-2xl! p-0!"
        @click="changePage(currentPage - 1)">
        <ChevronLeft class="h-5 w-5" />
      </BaseButton>

      <div class="flex flex-wrap items-center justify-center gap-2">
        <template v-for="page in visiblePages" :key="`page-${page}`">
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

      <BaseButton variant="outline" size="md" :disabled="currentPage === totalPages - 1" class="w-12! rounded-2xl! p-0!"
        @click="changePage(currentPage + 1)">
        <ChevronRight class="h-5 w-5" />
      </BaseButton>
    </div>

    <div v-if="canManage"
      class="fixed bottom-4 left-1/2 z-[60] flex w-[calc(100vw-2rem)] max-w-3xl -translate-x-1/2 flex-col gap-4 rounded-[28px] bg-slate-900 px-5 py-4 shadow-2xl transition-all duration-500 sm:bottom-8 sm:flex-row sm:items-center sm:justify-between sm:px-6"
      :class="selectedEmployees.length > 0 ? 'translate-y-0 opacity-100' : 'pointer-events-none translate-y-20 opacity-0'">
      <div>
        <span class="text-sm font-bold text-white">{{ selectedEmployees.length }} nhân sự được chọn</span>
        <p class="mt-1 text-[11px] font-bold uppercase tracking-[0.18em] text-slate-400">Bulk action mode</p>
      </div>

      <div class="flex flex-wrap items-center gap-3">
        <BaseButton variant="outline" size="sm"
          class="rounded-xl! border-slate-700! px-5 text-white! hover:bg-slate-800!"
          @click="openTransferModal('bulk', [...selectedEmployees])">
          <ArrowRightLeft class="mr-2 h-4 w-4" />
          Điều chuyển
        </BaseButton>
        <BaseButton variant="outline" size="sm"
          class="rounded-xl! border-slate-700! px-5 text-white! hover:bg-slate-800!" @click="exportToCSV">
          <Download class="mr-2 h-4 w-4" />
          Xuất dữ liệu
        </BaseButton>
        <BaseButton variant="ghost" size="sm" class="rounded-xl! px-5 text-slate-200! hover:bg-slate-800!"
          @click="clearSelected">
          Bỏ chọn
        </BaseButton>
      </div>
    </div>

    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="transferModalOpen" class="fixed inset-0 z-[9998] flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-slate-900/55 backdrop-blur-sm" @click="transferModalOpen = false" />

          <div class="relative w-full max-w-lg overflow-hidden rounded-4xl bg-white shadow-2xl">
            <div class="border-b border-slate-100 px-6 py-5">
              <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">
                {{ transferMode === 'single' ? 'Điều chuyển nhân sự' : 'Điều chuyển hàng loạt' }}
              </p>
              <h3 class="mt-2 text-2xl font-black text-slate-900">{{ transferSummary }}</h3>
              <p class="mt-2 text-sm font-medium text-slate-500">
                Chuyển nhân sự sang đơn vị mới và ghi chú lại lý do để tiện theo dõi về sau.
              </p>
            </div>

            <div class="space-y-5 px-6 py-6">
              <div class="space-y-2">
                <label class="text-sm font-black text-slate-800">Phòng ban đích</label>
                <select v-model="transferForm.targetOrgUnitId"
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  <option value="">Chọn phòng ban</option>
                  <option v-for="item in orgUnitOptions" :key="item.orgUnitId" :value="String(item.orgUnitId)">
                    {{ item.orgUnitName }}
                  </option>
                </select>
                <p v-if="referenceLoading" class="text-xs font-medium text-slate-400">Đang tải danh mục phòng ban...</p>
              </div>

              <div class="space-y-2">
                <label class="text-sm font-black text-slate-800">Ghi chú</label>
                <textarea v-model="transferForm.note" rows="4"
                  placeholder="Ví dụ: Cơ cấu lại team quý 2, bổ sung nguồn lực cho khối sản phẩm..."
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10" />
              </div>
            </div>

            <div class="flex flex-col gap-3 border-t border-slate-100 px-6 py-5 sm:flex-row sm:justify-end">
              <BaseButton variant="outline" @click="transferModalOpen = false">
                Hủy
              </BaseButton>
              <BaseButton variant="primary" :loading="transferSubmitting" @click="submitTransfer">
                <ArrowRightLeft class="mr-2 h-4 w-4" />
                Xác nhận điều chuyển
              </BaseButton>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.5s ease-out forwards;
}

.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.25s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}
</style>
