<script setup>
import { ref } from 'vue'
import MainLayout from '../../../layouts/MainLayout.vue'
import { Plus, Search, Filter, MoreVertical, Building2, Users } from 'lucide-vue-next'

const departments = ref([
  { id: 1, name: 'Khối Phát triển Phần mềm', code: 'DEV', manager: 'Nguyễn Văn A', employees: 45, status: 'Hoạt động' },
  { id: 2, name: 'Khối Kinh doanh & Marketing', code: 'SALES', manager: 'Trần Thị B', employees: 12, status: 'Hoạt động' },
  { id: 3, name: 'Khối Nhân sự - Hành chính', code: 'HR', manager: 'Lê Văn C', employees: 5, status: 'Hoạt động' },
  { id: 4, name: 'Khối Tài chính Kế toán', code: 'FIN', manager: 'Phạm Thị D', employees: 4, status: 'Hoạt động' },
])
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
