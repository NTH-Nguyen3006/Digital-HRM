<script setup>
import { ref } from 'vue'
import { Calendar, Clock, CheckCircle } from 'lucide-vue-next'

const history = ref([
  { date: '29/03/2026', checkIn: '08:00', checkOut: '17:30', status: 'Đúng giờ' },
  { date: '28/03/2026', checkIn: '08:10', checkOut: '17:35', status: 'Đi muộn' },
  { date: '27/03/2026', checkIn: '07:55', checkOut: '17:30', status: 'Đúng giờ' },
])
</script>

<template>

  <div class="max-w-5xl mx-auto pt-32 pb-20 px-6">
    <div class="mb-8">
      <h1 class="text-3xl font-black text-slate-900">Lịch sử chấm công</h1>
      <p class="text-slate-500 font-medium mt-2">Xem lại thời gian làm việc cá nhân của bạn</p>
    </div>

    <div class="grid md:grid-cols-4 gap-6 mb-8">
      <div class="bg-indigo-600 rounded-3xl p-6 text-white shadow-xl shadow-indigo-200">
        <div class="text-indigo-200 text-sm font-bold mb-1">Ngày công tháng này</div>
        <div class="text-4xl font-black">21.5</div>
      </div>
      <div class="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm flex flex-col justify-center">
        <div class="text-slate-400 text-xs font-bold uppercase tracking-widest mb-1">Đi muộn</div>
        <div class="text-2xl font-black text-amber-500">2 lần</div>
      </div>
      <div class="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm flex flex-col justify-center">
        <div class="text-slate-400 text-xs font-bold uppercase tracking-widest mb-1">Quên chấm công</div>
        <div class="text-2xl font-black text-rose-500">0 lần</div>
      </div>
    </div>

    <div class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
      <table class="w-full text-left">
        <thead>
          <tr class="bg-slate-50/50 border-b border-slate-100">
            <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Ngày</th>
            <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Giờ vào</th>
            <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Giờ ra</th>
            <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Trạng thái</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="att in history" :key="att.date" class="hover:bg-slate-50/50 transition-colors">
            <td class="py-4 px-6 font-bold text-slate-900 flex items-center">
              <Calendar class="w-4 h-4 mr-2 text-slate-400" /> {{ att.date }}
            </td>
            <td class="py-4 px-6 font-semibold text-emerald-600">{{ att.checkIn }}</td>
            <td class="py-4 px-6 font-semibold text-rose-600">{{ att.checkOut }}</td>
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

</template>
