<script setup>
import { ref } from 'vue'
import MainLayout from '../../../layouts/MainLayout.vue'
import { Calculator, Send, FileText } from 'lucide-vue-next'

const payroll = ref([
  { id: 1, employee: 'Nguyễn Văn A', baseSalary: '15,000,000', workDays: 22, allowance: '1,000,000', tax: '500,000', netPay: '15,500,000', status: 'Chưa duyệt' },
  { id: 2, employee: 'Trần Thị B', baseSalary: '12,000,000', workDays: 21.5, allowance: '500,000', tax: '200,000', netPay: '12,027,272', status: 'Đã thanh toán' },
])
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">Tính lương</h2>
          <p class="text-slate-500 font-medium mt-1">Kỳ lương Tháng 03/2026</p>
        </div>
        <div class="flex items-center space-x-3">
          <button class="bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 flex items-center">
            <Calculator class="w-5 h-5 mr-2" /> Chạy bảng lương
          </button>
        </div>
      </div>

      <div class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/50 border-b border-slate-100">
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Nhân viên</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Lương cơ bản</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Ngày công</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Phụ cấp</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Khấu trừ</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Thực nhận</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Trạng thái</th>
                <th class="py-4 px-6"></th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="pay in payroll" :key="pay.id" class="hover:bg-slate-50/50 transition-colors group">
                <td class="py-4 px-6 font-bold text-slate-900">{{ pay.employee }}</td>
                <td class="py-4 px-6 font-semibold text-slate-600">{{ pay.baseSalary }} ₫</td>
                <td class="py-4 px-6 font-semibold text-indigo-600">{{ pay.workDays }}</td>
                <td class="py-4 px-6 font-semibold text-emerald-600">+ {{ pay.allowance }} ₫</td>
                <td class="py-4 px-6 font-semibold text-rose-600">- {{ pay.tax }} ₫</td>
                <td class="py-4 px-6 font-black text-slate-900">{{ pay.netPay }} ₫</td>
                <td class="py-4 px-6">
                  <span class="px-3 py-1.5 rounded-lg text-xs font-bold ring-1 ring-inset" :class="pay.status === 'Đã thanh toán' ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' : 'bg-slate-100 text-slate-700 ring-slate-600/20'">
                    {{ pay.status }}
                  </span>
                </td>
                <td class="py-4 px-6 text-right">
                  <button class="flex items-center text-indigo-600 hover:underline font-bold text-sm">
                    <Send class="w-4 h-4 mr-1.5" /> Gửi phiếu lương
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
