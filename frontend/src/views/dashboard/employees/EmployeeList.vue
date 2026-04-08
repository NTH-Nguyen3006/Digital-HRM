<script setup>
import { ref, onMounted, watch } from 'vue'
import BaseButton from '@/components/common/BaseButton.vue'
import AvatarBox from '@/components/common/AvatarBox.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { Plus, Search, MoreVertical, Mail, Phone, Filter, ChevronLeft, ChevronRight, Loader2 } from 'lucide-vue-next'
import { getEmployees } from '@/api/admin/employee'

/* ------------------ STATE ------------------ */

const employees = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const totalElements = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = ref(12)

const selectedEmployees = ref([])
const showFilter = ref(false)

/* ------------------ API CALL ------------------ */

const fetchEmployees = async () => {
  loading.value = true
  try {
    const response = await getEmployees({
      keyword: searchKeyword.value,
      page: currentPage.value,
      size: pageSize.value
    })
    employees.value = response.data.content
    totalElements.value = response.data.totalElements
    totalPages.value = response.data.totalPages
  } catch (error) {
    console.error('Failed to fetch employees:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchEmployees)

/* ------------------ WATCHERS & LOGIC ------------------ */

watch(searchKeyword, () => {
  currentPage.value = 0
  fetchEmployees()
})

const changePage = (page) => {
  currentPage.value = page
  fetchEmployees()
}

const toggleSelect = (id) => {
  const index = selectedEmployees.value.indexOf(id)
  if (index === -1) selectedEmployees.value.push(id)
  else selectedEmployees.value.splice(index, 1)
}

const deleteSelected = () => {
  console.log('Deleting:', selectedEmployees.value)
  selectedEmployees.value = []
}

</script>

<template>
  <div class="space-y-8 animate-fade-in">
    <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-6">
      <div>
        <h2 class="text-4xl font-black text-slate-900 tracking-tight">Hồ sơ Nhân sự</h2>
        <p class="text-slate-500 font-medium mt-2 flex items-center gap-2">
          Quản lý và theo dõi thông tin chi tiết của <span
            class="px-2 py-0.5 bg-indigo-50 text-indigo-700 rounded-md font-bold">{{ totalElements }} thành viên</span>
        </p>
      </div>

      <div class="flex items-center gap-3">
        <!-- Search Bar with Glass Effect -->
        <div class="relative group">
          <Search
            class="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
          <input v-model="searchKeyword" type="text" placeholder="Họ tên, mã NV hoặc email..."
            class="w-72 pl-11 pr-4 py-3 bg-white border border-slate-200 rounded-2xl text-sm font-medium focus:ring-4 focus:ring-indigo-500/10 focus:border-indigo-500 transition-all shadow-sm outline-none" />
        </div>

        <button
          class="p-3 bg-white border border-slate-200 rounded-2xl text-slate-600 hover:text-indigo-600 hover:border-indigo-200 hover:bg-indigo-50/30 transition-all shadow-sm">
          <Filter class="w-5 h-5" />
        </button>

        <BaseButton variant="primary" size="lg" shadow class="rounded-2xl! px-6! h-12.5! font-bold">
          <Plus class="w-5 h-5 mr-2" />
          Thêm mới
        </BaseButton>
      </div>
    </div>

    <!-- MAIN CONTENT AREA -->
    <div class="relative min-h-100">

      <!-- Loading Overlay -->
      <div v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center bg-slate-50/50 backdrop-blur-[2px] rounded-3xl">
        <div class="flex flex-col items-center gap-3">
          <Loader2 class="w-10 h-10 text-indigo-600 animate-spin" />
          <span class="text-sm font-bold text-slate-600">Đang tải dữ liệu...</span>
        </div>
      </div>

      <!-- Grid of Employees -->
      <div v-if="employees.length > 0" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-6">
        <div v-for="emp in employees" :key="emp.employeeId"
          class="group bg-white rounded-[28px] border border-slate-200 p-6 transition-all duration-300 hover:border-indigo-200 hover:shadow-[0_20px_40px_rgba(79,70,229,0.08)] relative">
          <!-- Checkbox Overlay on Hover -->
          <div class="absolute top-4 left-4 opacity-0 group-hover:opacity-100 transition-opacity">
            <input type="checkbox" class="w-5 h-5 accent-indigo-600 cursor-pointer rounded-lg border-slate-300"
              :checked="selectedEmployees.includes(emp.employeeId)" @change="toggleSelect(emp.employeeId)" />
          </div>

          <!-- Top Actions -->
          <button class="absolute top-4 right-4 text-slate-300 hover:text-indigo-600 transition-colors">
            <MoreVertical class="w-5 h-5" />
          </button>

          <!-- Card Content -->
          <div class="flex flex-col items-center">
            <AvatarBox :name="emp.fullName" :image="emp.avatarUrl" size="xl" shape="rounded-3xl" />

            <h3
              class="mt-4 font-bold text-slate-900 text-lg tracking-tight group-hover:text-indigo-600 transition-colors">
              {{ emp.fullName }}
            </h3>
            <p class="text-xs font-bold text-slate-400 uppercase tracking-widest">{{ emp.employeeCode }}</p>

            <div class="mt-4 w-full flex flex-wrap justify-center gap-2">
              <span class="px-3 py-1 bg-slate-100 text-slate-600 rounded-full text-[11px] font-bold">
                {{ emp.jobTitleName || 'Chưa định danh' }}
              </span>
              <span class="px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full text-[11px] font-bold">
                {{ emp.orgUnitName }}
              </span>
            </div>
          </div>

          <div class="mt-6 pt-6 border-t border-slate-50 space-y-3">
            <div class="flex items-center gap-3 text-sm font-medium text-slate-600">
              <div class="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
                <Mail class="w-4 h-4" />
              </div>
              <span class="truncate">{{ emp.workEmail || 'N/A' }}</span>
            </div>
            <div class="flex items-center gap-3 text-sm font-medium text-slate-600">
              <div class="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
                <Phone class="w-4 h-4" />
              </div>
              <span>{{ emp.workPhone || 'N/A' }}</span>
            </div>
          </div>

          <div class="mt-6 flex items-center justify-between">
            <StatusBadge :status="emp.employmentStatus" />
            <button class="text-xs font-bold text-indigo-600 hover:text-indigo-700 uppercase tracking-wider">Hồ sơ
              &rarr;</button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <EmptyState v-else-if="!loading" title="Không tìm thấy nhân viên"
        description="Chúng tôi không tìm thấy kết quả nào phù hợp với từ khóa tìm kiếm của bạn."
        actionText="Thêm nhân viên mới" />
    </div>

    <!-- PAGINATION -->
    <div v-if="totalPages > 1" class="flex justify-center items-center gap-3 mt-12 pb-12">
      <BaseButton variant="outline" size="md" :disabled="currentPage === 0" @click="changePage(currentPage - 1)"
        class="rounded-2xl! w-12! p-0!">
        <ChevronLeft class="w-5 h-5" />
      </BaseButton>

      <div class="flex items-center gap-2">
        <template v-for="p in totalPages" :key="p">
          <button v-if="p <= 5 || p > totalPages - 2" @click="changePage(p - 1)"
            class="w-10 h-10 rounded-2xl text-sm font-bold transition-all" :class="currentPage === p - 1
              ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-100'
              : 'bg-white text-slate-500 hover:bg-indigo-50 hover:text-indigo-600 border border-slate-100'">
            {{ p }}
          </button>
          <span v-else-if="p === 6" class="text-slate-400 px-1 font-bold">...</span>
        </template>
      </div>

      <BaseButton variant="outline" size="md" :disabled="currentPage === totalPages - 1"
        @click="changePage(currentPage + 1)" class="rounded-2xl! w-12! p-0!">
        <ChevronRight class="w-5 h-5" />
      </BaseButton>
    </div>
  </div>

  <!-- SELECTION ACTION BAR -->
  <div
    class="fixed bottom-10 left-1/2 -translate-x-1/2 flex items-center gap-6 px-8 py-4 bg-slate-900 shadow-2xl rounded-3xl transition-all duration-500 z-60"
    :class="selectedEmployees.length > 0 ? 'translate-y-0 opacity-100' : 'translate-y-20 opacity-0 pointer-events-none'">
    <div class="flex flex-col">
      <span class="text-white font-bold text-sm">{{ selectedEmployees.length }} nhân sự được chọn</span>
      <span class="text-slate-400 text-[10px] uppercase font-bold tracking-widest">Action Mode</span>
    </div>
    <div class="h-8 w-px bg-slate-700"></div>
    <div class="flex items-center gap-3">
      <BaseButton variant="danger" size="sm" @click="deleteSelected"
        class="px-6 rounded-xl! bg-red-500! hover:bg-red-400! font-bold">
        Xóa hồ sơ
      </BaseButton>
      <BaseButton variant="outline" size="sm"
        class="px-6 rounded-xl! border-slate-700! text-white! hover:bg-slate-800! font-bold">
        Xuất dữ liệu
      </BaseButton>
    </div>
  </div>


</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.5s ease-out forwards;
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