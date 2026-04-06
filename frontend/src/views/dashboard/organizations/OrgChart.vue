<script setup>
import { ref, computed } from 'vue'
import { Plus, Search, Filter, MoreVertical, Building2, Users } from 'lucide-vue-next'
import { onMounted } from "vue"
import { OrgChart } from "d3-org-chart"

const tab = ref("tree")

const branches = ref([
  { id: "HCM", name: "Chi nhánh Hồ Chí Minh", parentId: "root" },
  { id: "HN", name: "Chi nhánh Hà Nội", parentId: "root" }
]);

const departments = ref([
  { id: 1, name: 'Khối Phát triển Phần mềm', code: 'DEV', manager: 'Nguyễn Văn A', employees: 45, status: 'Hoạt động', parentId: "HCM" },
  { id: 2, name: 'Khối Kinh doanh & Marketing', code: 'SALES', manager: 'Trần Thị B', employees: 12, status: 'Hoạt động', parentId: "HCM" },
  { id: 3, name: 'Khối Nhân sự - Hành chính', code: 'HR', manager: 'Lê Văn C', employees: 5, status: 'Hoạt động', parentId: "HN" },
  { id: 4, name: 'Khối Tài chính Kế toán', code: 'FIN', manager: 'Phạm Thị D', employees: 4, status: 'Hoạt động', parentId: "HN" },
]);

const employees = ref([
  { id: "emp1", name: "Nguyễn Văn A", parentId: "DEV" },
  { id: "emp2", name: "Nguyễn Văn B", parentId: "DEV" },
  { id: "emp3", name: "Nguyễn Văn C", parentId: "DEV" },

  { id: "emp4", name: "Trần Thị D", parentId: "SALES" },
  { id: "emp5", name: "Trần Văn E", parentId: "SALES" },

  { id: "emp6", name: "Lê Văn F", parentId: "HR" },
  { id: "emp7", name: "Nguyễn Văn G", parentId: "HR" },

  { id: "emp8", name: "Phạm Thị H", parentId: "FIN" }
]);

/* convert departments -> org chart data */
const orgData = computed(() => [
  {
    id: "root",
    name: "Digital HRM",
    manager: "CEO",
    parentId: null
  },

  ...branches.value,

  ...departments.value.map(d => ({
    id: d.code,
    name: d.name,
    manager: d.manager,
    employees: d.employees,
    parentId: d.parentId
  })),

  ...employees.value.map(e => ({
    id: e.id,
    name: e.name,
    manager: "Nhân viên",
    employees: 1,
    parentId: e.parentId
  }))
])

onMounted(() => {
  const chart = new OrgChart()

  chart
    .container("#org-chart")
    .data(orgData.value)
    .nodeWidth(() => 280)
    .nodeHeight(() => 130)
    .childrenMargin(() => 70)
    .siblingsMargin(() => 40)
    .compact(false)
    .initialZoom(0.9)
    .nodeContent((d) => {
      const data = d.data
      const isCompany = data.id === "root"
      const isBranch = ["HCM", "HN"].includes(data.id)
      const isEmployee = data.manager === "Nhân viên"

      // Chọn class màu dựa trên vai trò
      const cardClass = isCompany
        ? "bg-indigo-100 border-indigo-500"
        : isBranch
          ? "bg-green-100 border-green-500"
          : isEmployee
            ? "bg-slate-50 border-slate-200"
            : "bg-cyan-100 border-cyan-500"

      return `
      <div class="${cardClass} ${isEmployee ? "w-[220px]" : "w-[260px]"} rounded-[18px] border shadow-sm p-4 text-center cursor-pointer font-sans transition-all hover:shadow-md">
          
          <!-- Avatar/Icon Circle -->
          <div class="w-10 h-10 rounded-full bg-white text-indigo-600 flex items-center justify-center font-bold mx-auto mb-2 shadow-sm border border-slate-100">
            ${data.name.charAt(0)}
          </div>

          <!-- Name -->
          <div class="font-bold text-slate-900 ${isEmployee ? "text-[13px]" : "text-[14px]"} leading-tight">
            ${data.name}
          </div>

          <!-- Role/Manager -->
          ${data.manager
          ? `<div class="text-[12px] text-slate-500 mt-0.5">${data.manager}</div>`
          : ""
        }

          <!-- Stats -->
          ${data.employees
          ? `<div class="mt-2 text-[12px] text-indigo-600 font-bold bg-indigo-100/50 rounded-full py-0.5 px-2 inline-block">
                ${data.employees} nhân sự
             </div>`
          : ""
        }

      </div>
      `
    })
    .onNodeClick((nodeId) => {
      chart.setExpanded(nodeId)
      chart.render()
    })
    .render()

  // Trạng thái ban đầu: Thu gọn hết và chỉ mở node gốc
  chart.collapseAll()
  chart.setExpanded("root", true)
  chart.render()
})
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">Cơ cấu tổ chức</h2>
          <p class="text-slate-500 font-medium mt-1">Quản lý danh sách phòng ban và chi nhánh công ty</p>
        </div>
        <div class="flex items-center space-x-3">
          <div class="relative">
            <Search class="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input type="text" placeholder="Tìm kiếm phòng ban..."
              class="pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 w-64 shadow-sm" />
          </div>
          <button
            class="p-2.5 bg-white border border-slate-200 rounded-xl text-slate-600 hover:bg-slate-50 hover:text-indigo-600 transition-colors shadow-sm">
            <Filter class="w-5 h-5" />
          </button>
          <button
            class="bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 flex items-center">
            <Plus class="w-5 h-5 mr-2" /> Thêm phòng ban
          </button>
        </div>
      </div>

      <!-- TAB -->
      <div class="flex gap-2">
        <button @click="tab = 'tree'" :class="[
          'px-4 py-2 rounded-xl font-semibold',
          tab === 'tree'
            ? 'bg-indigo-600 text-white'
            : 'bg-white border'
        ]">
          Cơ cấu tổ chức
        </button>

        <button @click="tab = 'list'" :class="[
          'px-4 py-2 rounded-xl font-semibold',
          tab === 'list'
            ? 'bg-indigo-600 text-white'
            : 'bg-white border'
        ]">
          Danh mục
        </button>
      </div>

      <!-- ORG CHART -->
      <div v-show="tab === 'tree'" class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-visible">
        <div class="p-8 overflow-x-auto flex justify-center">
          <div id="org-chart" class="w-full"></div>
        </div>
      </div>

      <!-- Content -->
      <div v-show="tab === 'list'" class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/50 border-b border-slate-100">
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Tên phòng ban</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Mã / Code</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Người quản lý</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Nhân sự</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Trạng thái</th>
                <th class="py-4 px-6 text-right"></th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="dept in departments" :key="dept.id" class="hover:bg-slate-50/50 transition-colors group">
                <td class="py-4 px-6">
                  <div class="flex items-center space-x-3">
                    <div
                      class="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center font-bold shadow-sm border border-indigo-100/50">
                      <Building2 class="w-5 h-5" />
                    </div>
                    <span class="font-bold text-slate-900">{{ dept.name }}</span>
                  </div>
                </td>
                <td class="py-4 px-6 font-semibold text-slate-600">{{ dept.code }}</td>
                <td class="py-4 px-6">
                  <div class="flex items-center space-x-2">
                    <div
                      class="w-8 h-8 rounded-full bg-slate-200 flex items-center justify-center text-xs font-bold text-slate-600">
                      {{ dept.manager.split(' ').pop().charAt(0) }}
                    </div>
                    <span class="font-medium text-slate-700">{{ dept.manager }}</span>
                  </div>
                </td>
                <td class="py-4 px-6">
                  <div class="flex items-center space-x-1.5 text-slate-600">
                    <Users class="w-4 h-4 text-slate-400" />
                    <span class="font-bold">{{ dept.employees }}</span>
                  </div>
                </td>
                <td class="py-4 px-6">
                  <span
                    class="px-3 py-1.5 rounded-lg text-xs font-bold bg-emerald-50 text-emerald-700 ring-1 ring-inset ring-emerald-600/20">
                    {{ dept.status }}
                  </span>
                </td>
                <td class="py-4 px-6 text-right">
                  <button
                    class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-slate-100 rounded-lg transition-colors">
                    <MoreVertical class="w-5 h-5" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </MainLayout>
</template>
