<script setup>
import { FileSignature, MoreVertical, Plus, Search } from 'lucide-vue-next';
import { ref } from 'vue';
import MainLayout from '../../../layouts/MainLayout.vue';

const contracts = ref([
  { id: 'HD001', employee: 'Nguyễn Văn A', type: 'Xác định thời hạn (12 tháng)', startDate: '01/01/2026', endDate: '31/12/2026', status: 'Đang hiệu lực' },
  { id: 'HD002', employee: 'Trần Thị B', type: 'Thử việc (2 tháng)', startDate: '15/03/2026', endDate: '15/05/2026', status: 'Đang hiệu lực' },
  { id: 'HD003', employee: 'Lê Văn C', type: 'Không xác định thời hạn', startDate: '01/01/2020', endDate: '-', status: 'Đang hiệu lực' },
  { id: 'HD004', employee: 'Phạm Thị D', type: 'Thời vụ (3 tháng)', startDate: '01/10/2025', endDate: '31/12/2025', status: 'Đã hết hạn' },
]);
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">Hợp đồng lao động</h2>
          <p class="text-slate-500 font-medium mt-1">Quản lý hiệu lực các loại hợp đồng</p>
        </div>
        <div class="flex items-center space-x-3">
          <div class="relative">
            <Search class="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input type="text" placeholder="Tìm hợp đồng..."
              class="pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 w-64 shadow-sm" />
          </div>
          <button
            class="bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 flex items-center">
            <Plus class="w-5 h-5 mr-2" /> Tạo hợp đồng
          </button>
        </div>
      </div>

      <div class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/50 border-b border-slate-100">
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Mã HĐ</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Nhân viên</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Loại hợp đồng</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Ngày hiệu lực</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Ngày hết hạn</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Trạng thái</th>
                <th class="py-4 px-6"></th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="contract in contracts" :key="contract.id" class="hover:bg-slate-50/50 transition-colors group">
                <td class="py-4 px-6 font-bold text-indigo-600">{{ contract.id }}</td>
                <td class="py-4 px-6 font-bold text-slate-900">{{ contract.employee }}</td>
                <td class="py-4 px-6">
                  <div class="flex items-center space-x-2 text-slate-700">
                    <FileSignature class="w-4 h-4 text-slate-400" />
                    <span class="font-medium">{{ contract.type }}</span>
                  </div>
                </td>
                <td class="py-4 px-6 font-semibold text-slate-600">{{ contract.startDate }}</td>
                <td class="py-4 px-6 font-semibold text-slate-600">{{ contract.endDate }}</td>
                <td class="py-4 px-6">
                  <span class="px-3 py-1.5 rounded-lg text-xs font-bold ring-1 ring-inset"
                    :class="contract.status === 'Đang hiệu lực' ? 'bg-sky-50 text-sky-700 ring-sky-600/20' : 'bg-rose-50 text-rose-700 ring-rose-600/20'">
                    {{ contract.status }}
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
