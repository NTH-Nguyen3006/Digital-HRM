<script setup>
import { ref } from 'vue'
import MainLayout from '../../../layouts/MainLayout.vue'
import { Plus, Search, Filter, MoreVertical, Mail, Phone } from 'lucide-vue-next'

const employees = ref([
  { id: 'NV001', name: 'Nguyễn Văn A', cccd: '001099012345', dob: '15/05/1995', phone: '0901234567', email: 'nva@company.com', department: 'DEV', status: 'Chính thức' },
  { id: 'NV002', name: 'Trần Thị B', cccd: '001098054321', dob: '22/08/1998', phone: '0912345678', email: 'ttb@company.com', department: 'HR', status: 'Thử việc' },
  { id: 'NV003', name: 'Lê Văn C', cccd: '030097011223', dob: '10/12/1997', phone: '0923456789', email: 'lvc@company.com', department: 'SALES', status: 'Chính thức' },
])
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">Hồ sơ nhân sự</h2>
          <p class="text-slate-500 font-medium mt-1">Quản lý danh sách, thông tin cơ bản nhân viên</p>
        </div>
        <div class="flex items-center space-x-3">
          <div class="relative">
            <Search class="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input type="text" placeholder="Tìm bằng Tên, Mã, CCCD..."
              class="pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 w-72 shadow-sm" />
          </div>
          <button class="bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 flex items-center">
            <Plus class="w-5 h-5 mr-2" /> Thêm nhân viên
          </button>
        </div>
      </div>

      <div class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/50 border-b border-slate-100">
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Mã NV</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Họ và Tên</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">CCCD & Ngày sinh</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Liên hệ</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Phòng ban</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Trạng thái</th>
                <th class="py-4 px-6"></th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="emp in employees" :key="emp.id" class="hover:bg-slate-50/50 transition-colors group">
                <td class="py-4 px-6 font-semibold text-slate-600">{{ emp.id }}</td>
                <td class="py-4 px-6">
                  <div class="flex items-center space-x-3">
                    <div class="w-10 h-10 rounded-full bg-linear-to-tr from-indigo-100 to-indigo-50 text-indigo-700 flex items-center justify-center font-bold shadow-sm outline outline-1 outline-indigo-200">
                      {{ emp.name.split(' ').pop().charAt(0) }}
                    </div>
                    <span class="font-bold text-slate-900">{{ emp.name }}</span>
                  </div>
                </td>
                <td class="py-4 px-6">
                  <div class="text-sm font-medium text-slate-900">{{ emp.cccd }}</div>
                  <div class="text-xs font-semibold text-slate-500 mt-1">{{ emp.dob }}</div>
                </td>
                <td class="py-4 px-6">
                  <div class="flex items-center space-x-1.5 text-sm text-slate-700 mb-1">
                    <Phone class="w-3.5 h-3.5 text-slate-400" /> <span>{{ emp.phone }}</span>
                  </div>
                  <div class="flex items-center space-x-1.5 text-xs text-slate-500">
                    <Mail class="w-3.5 h-3.5 text-slate-400" /> <span>{{ emp.email }}</span>
                  </div>
                </td>
                <td class="py-4 px-6 font-semibold text-slate-700">{{ emp.department }}</td>
                <td class="py-4 px-6">
                  <span class="px-3 py-1.5 rounded-lg text-xs font-bold ring-1 ring-inset" :class="emp.status === 'Chính thức' ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' : 'bg-amber-50 text-amber-700 ring-amber-600/20'">
                    {{ emp.status }}
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
