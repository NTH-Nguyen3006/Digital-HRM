<script setup>
import { ref } from 'vue'
import { Download, Search, CheckCircle, Clock } from 'lucide-vue-next'

const attendance = ref([
  { id: 1, employee: 'Nguyễn Văn A', date: '29/03/2026', checkIn: '08:00', checkOut: '17:30', status: 'Đúng giờ', workHours: '8h', overtime: '0h' },
  { id: 2, employee: 'Trần Thị B', date: '29/03/2026', checkIn: '08:15', checkOut: '17:30', status: 'Đi muộn', workHours: '7h45m', overtime: '0h' },
  { id: 3, employee: 'Lê Văn C', date: '29/03/2026', checkIn: '07:55', checkOut: '19:00', status: 'Đúng giờ', workHours: '8h', overtime: '1.5h' },
])
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">Chấm công</h2>
          <p class="text-slate-500 font-medium mt-1">Ghi nhận giờ vào/ra, tính số ngày công</p>
        </div>
        <div class="flex items-center space-x-3">
          <div class="relative">
            <Search class="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input type="text" placeholder="Tìm theo nhân viên..."
              class="pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 w-64 shadow-sm" />
          </div>
          <button
            class="bg-white border border-slate-200 px-5 py-2.5 rounded-xl font-bold text-slate-700 hover:bg-slate-50 transition-all shadow-sm flex items-center">
            <Download class="w-5 h-5 mr-2" /> Xuất bảng công
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div class="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4">
          <div class="w-12 h-12 bg-indigo-50 rounded-2xl flex items-center justify-center text-indigo-600">
            <CheckCircle class="w-6 h-6" />
          </div>
          <div>
            <div class="text-slate-500 text-sm font-bold mb-1">Đã Check-in hôm nay</div>
            <div class="text-2xl font-black text-slate-900">145 / 150</div>
          </div>
        </div>
        <div class="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4">
          <div class="w-12 h-12 bg-amber-50 rounded-2xl flex items-center justify-center text-amber-600">
            <Clock class="w-6 h-6" />
          </div>
          <div>
            <div class="text-slate-500 text-sm font-bold mb-1">Đi muộn</div>
            <div class="text-2xl font-black text-slate-900">12</div>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/50 border-b border-slate-100">
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Nhân viên</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Ngày</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">In / Out</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Thời gian làm</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Trạng thái</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="att in attendance" :key="att.id" class="hover:bg-slate-50/50 transition-colors group">
                <td class="py-4 px-6 font-bold text-slate-900">{{ att.employee }}</td>
                <td class="py-4 px-6 font-semibold text-slate-600">{{ att.date }}</td>
                <td class="py-4 px-6">
                  <div class="flex items-center space-x-2 font-medium text-sm">
                    <span class="text-emerald-600">{{ att.checkIn }}</span>
                    <span class="text-slate-400">-</span>
                    <span class="text-rose-600">{{ att.checkOut }}</span>
                  </div>
                </td>
                <td class="py-4 px-6">
                  <div class="text-sm font-bold text-slate-700">{{ att.workHours }}</div>
                  <div class="text-xs font-semibold text-indigo-500 mt-0.5" v-if="att.overtime !== '0h'">+OT: {{
                    att.overtime }}</div>
                </td>
                <td class="py-4 px-6">
                  <span class="px-3 py-1.5 rounded-lg text-xs font-bold ring-1 ring-inset"
                    :class="att.status === 'Đúng giờ' ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' : 'bg-amber-50 text-amber-700 ring-amber-600/20'">
                    {{ att.status }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </MainLayout>
</template>
