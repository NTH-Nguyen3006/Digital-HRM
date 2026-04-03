<script setup>
import { ref, computed } from 'vue'
import MainLayout from '../../../layouts/MainLayout.vue'
import { Plus, Search, Filter, MoreVertical, Building2, Users } from 'lucide-vue-next'
import { onMounted } from "vue"
import { OrgChart } from "d3-org-chart"

const departments = ref([
  { id: 1, name: 'Khối Phát triển Phần mềm', code: 'DEV', manager: 'Nguyễn Văn A', employees: 45, status: 'Hoạt động' },
  { id: 2, name: 'Khối Kinh doanh & Marketing', code: 'SALES', manager: 'Trần Thị B', employees: 12, status: 'Hoạt động' },
  { id: 3, name: 'Khối Nhân sự - Hành chính', code: 'HR', manager: 'Lê Văn C', employees: 5, status: 'Hoạt động' },
  { id: 4, name: 'Khối Tài chính Kế toán', code: 'FIN', manager: 'Phạm Thị D', employees: 4, status: 'Hoạt động' },
])
/* convert departments -> org chart data */
const orgData = computed(() => [
  {
    id: "root",
    name: "Digital HRM",
    manager: "CEO",
    parentId: null
  },
  ...departments.value.map(d => ({
    id: d.code,
    name: d.name,
    manager: d.manager,
    employees: d.employees,
    parentId: "root"
  }))
])

onMounted(() => {
  const chart = new OrgChart()
    .container("#org-chart")
    .data(orgData.value)
    .nodeWidth(() => 260)
    .nodeHeight(() => 120)
    .childrenMargin(() => 50)
    .nodeContent((d) => {
      const data = d.data

      return `
      <div style="
          width:260px;
          background:white;
          border-radius:16px;
          border:1px solid #e2e8f0;
          box-shadow:0 4px 10px rgba(0,0,0,0.05);
          padding:16px;
          font-family:Inter;
          text-align:center;
      ">
          
          <div style="
              width:40px;
              height:40px;
              border-radius:50%;
              background:#eef2ff;
              color:#4f46e5;
              display:flex;
              align-items:center;
              justify-content:center;
              font-weight:700;
              margin:auto;
              margin-bottom:8px;
          ">
            ${data.name.charAt(0)}
          </div>

          <div style="font-weight:700;color:#0f172a">
            ${data.name}
          </div>

          <div style="font-size:12px;color:#64748b">
            ${data.manager ?? ""}
          </div>

          <div style="
              margin-top:6px;
              font-size:12px;
              color:#4f46e5;
              font-weight:600;
          ">
            ${data.employees ?? 0} nhân sự
          </div>

      </div>
      `
    })
    .render()
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
          <button class="p-2.5 bg-white border border-slate-200 rounded-xl text-slate-600 hover:bg-slate-50 hover:text-indigo-600 transition-colors shadow-sm">
            <Filter class="w-5 h-5" />
          </button>
          <button class="bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 flex items-center">
            <Plus class="w-5 h-5 mr-2" /> Thêm phòng ban
          </button>
        </div>
      </div>

      <!-- ORG CHART -->
      <div class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-visible">
        <div class="p-8 overflow-x-auto flex justify-center min-h-[350px]">
          <div id="org-chart" class="w-full"></div>
        </div>
      </div>
      
      <!-- Content -->
      <div class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
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
                    <div class="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center font-bold shadow-sm border border-indigo-100/50">
                      <Building2 class="w-5 h-5" />
                    </div>
                    <span class="font-bold text-slate-900">{{ dept.name }}</span>
                  </div>
                </td>
                <td class="py-4 px-6 font-semibold text-slate-600">{{ dept.code }}</td>
                <td class="py-4 px-6">
                  <div class="flex items-center space-x-2">
                    <div class="w-8 h-8 rounded-full bg-slate-200 flex items-center justify-center text-xs font-bold text-slate-600">
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
                  <span class="px-3 py-1.5 rounded-lg text-xs font-bold bg-emerald-50 text-emerald-700 ring-1 ring-inset ring-emerald-600/20">
                    {{ dept.status }}
                  </span>
                </td>
                <td class="py-4 px-6 text-right">
                  <button class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-slate-100 rounded-lg transition-colors">
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
