<script setup>
import MainLayout from "@/layouts/MainLayout.vue"
import { ref } from 'vue'
import { Lock, FileText, MonitorOff } from 'lucide-vue-next'

const offboardings = ref([
  { id: 1, name: 'Ngô Văn M', role: 'Business Analyst', lastDate: '30/03/2026', step: 1 },
])
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <div>
        <h2 class="text-3xl font-black text-slate-900 tracking-tight">Thôi việc (Offboarding)</h2>
        <p class="text-slate-500 font-medium mt-1">Khóa tài khoản, thu hồi tài sản và bàn giao công việc</p>
      </div>

      <div class="grid lg:grid-cols-2 gap-6">
        <div v-for="off in offboardings" :key="off.id"
          class="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm relative overflow-hidden">
          <div class="absolute top-0 right-0 w-32 h-32 bg-rose-50 rounded-bl-full z-0"></div>

          <div class="flex items-center justify-between mb-6 relative z-10">
            <div class="flex items-center space-x-4">
              <div
                class="w-14 h-14 bg-rose-100 text-rose-600 rounded-full flex items-center justify-center font-bold text-xl border-2 border-rose-200">
                {{ off.name.split(' ').pop().charAt(0) }}
              </div>
              <div>
                <h3 class="font-bold text-xl text-slate-900">{{ off.name }}</h3>
                <p class="text-slate-500 font-medium text-sm">{{ off.role }} • Ngày cuối: <span
                    class="text-rose-600 font-bold">{{ off.lastDate }}</span></p>
              </div>
            </div>
            <button class="px-5 py-2.5 bg-rose-600 text-white font-bold rounded-xl hover:bg-rose-700 shadow-md">
              Chuyển "Đã nghỉ việc"
            </button>
          </div>

          <!-- Checklist -->
          <div class="space-y-3 relative z-10">
            <h4 class="font-bold text-slate-700 mb-3 text-sm">Quy trình xử lý</h4>

            <div class="flex items-center justify-between p-3 rounded-xl border"
              :class="off.step >= 1 ? 'bg-rose-50 border-rose-100 text-rose-800' : 'bg-slate-50 border-slate-100 text-slate-500'">
              <div class="flex items-center space-x-3">
                <FileText class="w-5 h-5" />
                <span class="font-bold">1. Bàn giao công việc</span>
              </div>
              <span v-if="off.step >= 1" class="text-rose-600 font-bold text-sm">Hoàn tất</span>
              <button v-else
                class="text-rose-600 font-bold text-sm bg-white px-3 py-1.5 rounded border border-rose-100">Thực
                hiện</button>
            </div>

            <div class="flex items-center justify-between p-3 rounded-xl border"
              :class="off.step >= 2 ? 'bg-rose-50 border-rose-100 text-rose-800' : 'bg-slate-50 border-slate-100 text-slate-500'">
              <div class="flex items-center space-x-3">
                <MonitorOff class="w-5 h-5" />
                <span class="font-bold">2. Thu hồi thiết bị</span>
              </div>
              <span v-if="off.step >= 2" class="text-rose-600 font-bold text-sm">Hoàn tất</span>
              <button v-else
                class="text-rose-600 font-bold text-sm bg-white px-3 py-1.5 rounded border border-rose-100">Thực
                hiện</button>
            </div>

            <div
              class="flex items-center justify-between p-3 rounded-xl border bg-slate-50 border-slate-100 text-slate-500">
              <div class="flex items-center space-x-3">
                <Lock class="w-5 h-5" />
                <span class="font-bold">3. Khóa tài khoản (Tự động khi nghỉ hưu)</span>
              </div>
              <button class="text-rose-600 font-bold text-sm bg-white px-3 py-1.5 rounded border border-rose-100">Khóa
                ngay</button>
            </div>

          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>
