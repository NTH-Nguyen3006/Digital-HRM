<script setup>
import { ref } from 'vue'
import { Plus, Check, X, Search } from 'lucide-vue-next'

const requests = ref([
  { id: 1, employee: 'Phạm Văn X', type: 'Nghỉ ốm', days: 2, from: '25/03/2026', to: '26/03/2026', reason: 'Sốt xuất huyết', status: 'Chờ duyệt' },
  { id: 2, employee: 'Lê Thị Y', type: 'Nghỉ phép năm (AL)', days: 1, from: '28/03/2026', to: '28/03/2026', reason: 'Việc gia đình', status: 'Đã duyệt' },
  { id: 3, employee: 'Trần Văn Z', type: 'Nghỉ không lương (UP)', days: 5, from: '10/04/2026', to: '14/04/2026', reason: 'Đi du lịch nước ngoài', status: 'Từ chối' },
])
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">Quản lý nghỉ phép</h2>
          <p class="text-slate-500 font-medium mt-1">Duyệt đơn và trừ tổng phép năm</p>
        </div>
        <div class="flex items-center space-x-3">
          <button
            class="bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 flex items-center">
            <Plus class="w-5 h-5 mr-2" /> Gửi đơn xin nghỉ
          </button>
        </div>
      </div>

      <div class="grid lg:grid-cols-3 gap-6">
        <!-- Dashboard / Request list -->
        <div class="lg:col-span-2 space-y-4">
          <div v-for="req in requests" :key="req.id"
            class="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col md:flex-row md:items-center justify-between gap-6 hover:shadow-md transition-shadow">
            <div class="flex-1 space-y-3">
              <div class="flex items-center justify-between md:justify-start md:space-x-4">
                <h3 class="font-bold text-lg text-slate-900">{{ req.employee }}</h3>
                <span class="px-2.5 py-1 rounded-md text-xs font-bold" :class="[
                  req.status === 'Chờ duyệt' ? 'bg-amber-100 text-amber-700' :
                    req.status === 'Đã duyệt' ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'
                ]">
                  {{ req.status }}
                </span>
              </div>
              <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 text-sm font-medium">
                <div>
                  <div class="text-slate-400 text-xs mb-1">Loại phép</div>
                  <div class="text-slate-700">{{ req.type }}</div>
                </div>
                <div>
                  <div class="text-slate-400 text-xs mb-1">Thời gian</div>
                  <div class="text-slate-700">{{ req.days }} ngày ({{ req.from }} - {{ req.to }})</div>
                </div>
                <div class="col-span-2">
                  <div class="text-slate-400 text-xs mb-1">Lý do</div>
                  <div class="text-slate-700 italic">"{{ req.reason }}"</div>
                </div>
              </div>
            </div>

            <div class="flex items-center gap-2" v-if="req.status === 'Chờ duyệt'">
              <button
                class="flex-1 md:flex-none flex justify-center items-center px-4 py-2 bg-emerald-50 text-emerald-600 hover:bg-emerald-500 hover:text-white rounded-xl font-bold transition-all shadow-sm">
                <Check class="w-4 h-4 mr-1.5" /> Duyệt
              </button>
              <button
                class="flex-1 md:flex-none flex justify-center items-center px-4 py-2 bg-rose-50 text-rose-600 hover:bg-rose-500 hover:text-white rounded-xl font-bold transition-all shadow-sm">
                <X class="w-4 h-4 mr-1.5" /> Từ chối
              </button>
            </div>
          </div>
        </div>

        <!-- Sidebar standard -->
        <div class="bg-slate-800 text-white rounded-3xl p-6 shadow-xl relative overflow-hidden h-fit">
          <div class="absolute -top-24 -right-24 w-48 h-48 bg-white/5 rounded-full blur-3xl"></div>
          <h3 class="text-xl font-bold mb-6">Trạng thái phép cá nhân</h3>
          <div class="space-y-4">
            <div>
              <div class="flex justify-between text-sm mb-1 font-medium text-slate-300">
                <span>Tổng phép trong năm (AL)</span>
                <span class="font-bold text-white">12 ngày</span>
              </div>
            </div>
            <div>
              <div class="flex justify-between text-sm mb-1 font-medium text-slate-300">
                <span>Đã sử dụng</span>
                <span class="font-bold text-white">3 ngày</span>
              </div>
            </div>
            <hr class="border-slate-700">
            <div>
              <div class="flex justify-between font-bold text-lg">
                <span>Còn lại</span>
                <span class="text-emerald-400">9 ngày</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>
