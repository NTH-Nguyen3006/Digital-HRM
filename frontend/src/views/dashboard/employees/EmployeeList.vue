<script setup>
import { ref, computed } from 'vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { Plus, Search, MoreVertical, Mail, Phone, Filter } from 'lucide-vue-next'

/* ------------------ DATA ------------------ */

const employees = ref([
  { id: 'NV001', name: 'Nguyễn Văn A', department: 'DEV', phone: '0901234561', email: 'nva@company.com', status: 'Chính thức' },
  { id: 'NV002', name: 'Trần Thị B', department: 'HR', phone: '0901234562', email: 'ttb@company.com', status: 'Thử việc' },
  { id: 'NV003', name: 'Lê Văn C', department: 'SALES', phone: '0901234563', email: 'lvc@company.com', status: 'Chính thức' },
  { id: 'NV004', name: 'Phạm Thị D', department: 'DEV', phone: '0901234564', email: 'ptd@company.com', status: 'Chính thức' },
  { id: 'NV005', name: 'Hoàng Văn E', department: 'DEV', phone: '0901234565', email: 'hve@company.com', status: 'Thử việc' },
  { id: 'NV006', name: 'Đỗ Thị F', department: 'HR', phone: '0901234566', email: 'dtf@company.com', status: 'Chính thức' },
  { id: 'NV007', name: 'Ngô Văn G', department: 'SALES', phone: '0901234567', email: 'nvg@company.com', status: 'Chính thức' },
  { id: 'NV008', name: 'Bùi Thị H', department: 'DEV', phone: '0901234568', email: 'bth@company.com', status: 'Chính thức' },
  { id: 'NV009', name: 'Lý Văn I', department: 'HR', phone: '0901234569', email: 'lvi@company.com', status: 'Thử việc' },
  { id: 'NV010', name: 'Trịnh Thị K', department: 'SALES', phone: '0901234570', email: 'ttk@company.com', status: 'Chính thức' }
])

/* ------------------ FILTER ------------------ */

const search = ref('')
const showFilter = ref(false)

const roles = ['DEV', 'HR', 'SALES']
const statuses = ['Chính thức', 'Thử việc']

const roleFilter = ref([])
const statusFilter = ref([])

/* ------------------ FILTER LOGIC ------------------ */

const filteredEmployees = computed(() =>
  employees.value.filter(emp => {

    const matchSearch =
      emp.name.toLowerCase().includes(search.value.toLowerCase()) ||
      emp.id.toLowerCase().includes(search.value.toLowerCase())

    const matchRole =
      !roleFilter.value.length ||
      roleFilter.value.includes(emp.department)

    const matchStatus =
      !statusFilter.value.length ||
      statusFilter.value.includes(emp.status)

    return matchSearch && matchRole && matchStatus
  })
)

/* ------------------ PAGINATION ------------------ */

const page = ref(1)
const perPage = 8

const totalPages = computed(() =>
  Math.ceil(filteredEmployees.value.length / perPage)
)

const paginatedEmployees = computed(() => {
  const start = (page.value - 1) * perPage
  return filteredEmployees.value.slice(start, start + perPage)
})

/* ------------------ SELECTION ------------------ */

const selectedEmployees = ref([])

const toggleSelect = id => {
  selectedEmployees.value.includes(id)
    ? selectedEmployees.value = selectedEmployees.value.filter(i => i !== id)
    : selectedEmployees.value.push(id)
}

const deleteSelected = () => {
  employees.value = employees.value.filter(
    emp => !selectedEmployees.value.includes(emp.id)
  )
  selectedEmployees.value = []
}

/* ------------------ FILTER TOGGLE ------------------ */

const toggleFilter = (list, value) => {
  const index = list.indexOf(value)
  index === -1 ? list.push(value) : list.splice(index, 1)
}
</script>

<template>
  <MainLayout>
    <div class="space-y-6">

      <!-- HEADER -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">

        <!-- Title -->
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">
            Hồ sơ nhân sự
          </h2>
          <p class="text-slate-500 font-medium mt-1">
            Quản lý danh sách, thông tin cơ bản nhân viên
          </p>
        </div>

        <!-- Toolbar -->
        <div class="flex items-center gap-3">

          <!-- Search -->
          <div class="relative">
            <Search class="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input v-model="search" type="text" placeholder="Tìm bằng Tên hoặc Mã..." class="pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm
              focus:outline-none focus:ring-2 focus:ring-indigo-500/20
              focus:border-indigo-500 w-64 shadow-sm" />
          </div>

          <!-- Filter -->
          <div class="relative">

            <button @click="showFilter = !showFilter" class="flex items-center gap-2 px-4 py-2.5 bg-white border border-slate-200
              rounded-xl text-sm text-slate-600 shadow-sm
              hover:border-indigo-400 hover:text-indigo-600 transition">
              <Filter class="w-4 h-4" />
            </button>

            <!-- Filter Panel -->
            <div v-if="showFilter" class="absolute right-0 mt-2 w-64 bg-white border border-slate-200
              shadow-xl rounded-xl p-4 z-50">

              <div class="grid grid-cols-2 gap-6">

                <!-- Role -->
                <div>
                  <div class="font-semibold text-sm mb-2 text-slate-700">
                    Chức vụ
                  </div>

                  <label v-for="role in roles" :key="role" class="flex items-center gap-2 text-sm mb-1 cursor-pointer">
                    <input type="checkbox" class="accent-indigo-600" :checked="roleFilter.includes(role)"
                      @click="toggleFilter(roleFilter, role)" />
                    {{ role }}
                  </label>
                </div>

                <!-- Status -->
                <div>
                  <div class="font-semibold text-sm mb-2 text-slate-700">
                    Trạng thái
                  </div>

                  <label v-for="status in statuses" :key="status"
                    class="flex items-center gap-2 text-sm mb-1 cursor-pointer">
                    <input type="checkbox" class="accent-indigo-600" :checked="statusFilter.includes(status)"
                      @click="toggleFilter(statusFilter, status)" />
                    {{ status }}
                  </label>
                </div>

              </div>
            </div>

          </div>

          <!-- Add Button -->
          <button class="bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white
            hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 flex items-center">
            <Plus class="w-5 h-5 mr-2" />
            Thêm nhân viên
          </button>

        </div>
      </div>

      <!-- EMPLOYEE GRID -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">

        <div v-for="emp in paginatedEmployees" :key="emp.id"
          class="bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition p-5 relative">

          <!-- Checkbox -->
          <input type="checkbox" class="absolute top-4 left-4 accent-indigo-500"
            :checked="selectedEmployees.includes(emp.id)" @click="toggleSelect(emp.id)" />

          <!-- Menu -->
          <button class="absolute top-4 right-4 text-slate-400 hover:text-slate-600">
            <MoreVertical class="w-4 h-4" />
          </button>

          <!-- Avatar -->
          <div class="flex flex-col items-center mt-2">
            <div
              class="w-16 h-16 rounded-full bg-indigo-100 flex items-center justify-center font-bold text-indigo-700 text-lg">
              {{ emp.name.split(' ').pop().charAt(0) }}
            </div>

            <div class="mt-3 font-semibold text-slate-900 text-center">
              {{ emp.name }}
            </div>

            <div class="text-xs text-slate-500">
              {{ emp.id }}
            </div>

            <span class="text-xs px-2 py-1 rounded bg-indigo-50 text-indigo-600 font-medium mt-2">
              {{ emp.department }}
            </span>
          </div>

          <!-- Contact -->
          <div class="mt-4 space-y-2 text-sm">
            <div class="flex items-center text-slate-700">
              <Phone class="w-4 h-4 mr-2 text-slate-400" />
              {{ emp.phone }}
            </div>

            <div class="flex items-center text-slate-600 text-xs">
              <Mail class="w-4 h-4 mr-2 text-slate-400" />
              {{ emp.email }}
            </div>
          </div>

          <!-- Status -->
          <div class="mt-4 flex justify-center">
            <span class="px-3 py-1 rounded-lg text-xs font-bold ring-1 ring-inset" :class="emp.status === 'Chính thức'
              ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20'
              : 'bg-amber-50 text-amber-700 ring-amber-600/20'">
              {{ emp.status }}
            </span>
          </div>

        </div>

      </div>

      <!-- PAGINATION -->
      <div class="flex justify-center items-center gap-2 mt-10">

        <!-- Prev -->
        <BaseButton variant="outline" size="sm" :disabled="page === 1" @click="page--">
          Prev
        </BaseButton>

        <!-- Page numbers -->
        <template v-for="p in totalPages" :key="p">

          <BaseButton size="sm" :variant="p === page ? 'primary' : 'outline'" @click="page = p">
            {{ p }}
          </BaseButton>

        </template>

        <!-- Next -->
        <BaseButton variant="outline" size="sm" :disabled="page === totalPages" @click="page++">
          Next
        </BaseButton>

      </div>
    </div>

    <!-- FLOAT ACTION BAR -->
    <div v-if="selectedEmployees.length" class="fixed bottom-6 right-6 bg-white shadow-2xl border border-slate-200
    rounded-2xl px-5 py-3 flex items-center gap-4 z-50">

      <span class="text-sm font-semibold text-slate-700">
        {{ selectedEmployees.length }} nhân viên được chọn
      </span>

      <div class="flex items-center gap-2">
        <BaseButton variant="danger" size="sm" @click="deleteSelected">
          Xóa
        </BaseButton>

        <BaseButton variant="primary" size="sm">
          Chuyển phòng ban
        </BaseButton>
      </div>
    </div>

  </MainLayout>
</template>