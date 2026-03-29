<script setup>
import { ref } from 'vue'
import MainLayout from '../../../layouts/MainLayout.vue'
import { UserCheck, Laptop, Mail, Key } from 'lucide-vue-next'

const candidates = ref([
  { id: 1, name: 'Hoàng Văn K', role: 'Frontend Developer', joinDate: '01/04/2026', step: 2 },
  { id: 2, name: 'Đoàn Thị L', role: 'Marketing Specialist', joinDate: '15/04/2026', step: 0 },
])
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <div>
        <h2 class="text-3xl font-black text-slate-900 tracking-tight">Tiếp nhận nhân sự (Onboarding)</h2>
        <p class="text-slate-500 font-medium mt-1">Chào mừng nhân viên mới, tạo tài khoản và phân quyền</p>
      </div>

      <div class="grid lg:grid-cols-2 gap-6">
        <div v-for="can in candidates" :key="can.id" class="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm">
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center space-x-4">
              <div class="w-14 h-14 bg-indigo-50 text-indigo-600 rounded-full flex items-center justify-center font-bold text-xl border-2 border-indigo-100">
                {{ can.name.split(' ').pop().charAt(0) }}
              </div>
              <div>
                <h3 class="font-bold text-xl text-slate-900">{{ can.name }}</h3>
                <p class="text-slate-500 font-medium text-sm">{{ can.role }} • Ngày Join: <span class="text-indigo-600 font-bold">{{ can.joinDate }}</span></p>
              </div>
            </div>
            <button class="px-5 py-2.5 bg-indigo-600 text-white font-bold rounded-xl hover:bg-indigo-700 shadow-md">
              Chuyển trạng thái
            </button>
          </div>

          <!-- Checklist -->
          <div class="space-y-3">
            <h4 class="font-bold text-slate-700 mb-3 text-sm">Checklist chuẩn bị</h4>
            
            <div class="flex items-center justify-between p-3 rounded-xl border" :class="can.step >= 1 ? 'bg-emerald-50 border-emerald-100 text-emerald-800' : 'bg-slate-50 border-slate-100 text-slate-500'">
              <div class="flex items-center space-x-3">
                <Key class="w-5 h-5" />
                <span class="font-bold">1. Tạo tài khoản hệ thống</span>
              </div>
              <UserCheck v-if="can.step >= 1" class="w-5 h-5 text-emerald-500" />
              <button v-else class="text-indigo-600 font-bold text-sm bg-white px-3 py-1.5 rounded border border-indigo-100">Thực hiện</button>
            </div>

            <div class="flex items-center justify-between p-3 rounded-xl border" :class="can.step >= 2 ? 'bg-emerald-50 border-emerald-100 text-emerald-800' : 'bg-slate-50 border-slate-100 text-slate-500'">
              <div class="flex items-center space-x-3">
                <Laptop class="w-5 h-5" />
                <span class="font-bold">2. Cấp phát thiết bị (Laptop)</span>
              </div>
              <UserCheck v-if="can.step >= 2" class="w-5 h-5 text-emerald-500" />
              <button v-else class="text-indigo-600 font-bold text-sm bg-white px-3 py-1.5 rounded border border-indigo-100">Thực hiện</button>
            </div>

            <div class="flex items-center justify-between p-3 rounded-xl border" :class="can.step >= 3 ? 'bg-emerald-50 border-emerald-100 text-emerald-800' : 'bg-slate-50 border-slate-100 text-slate-500'">
              <div class="flex items-center space-x-3">
                <Mail class="w-5 h-5" />
                <span class="font-bold">3. Gửi Email Welcome</span>
              </div>
              <UserCheck v-if="can.step >= 3" class="w-5 h-5 text-emerald-500" />
              <button v-else class="text-indigo-600 font-bold text-sm bg-white px-3 py-1.5 rounded border border-indigo-100">Thực hiện</button>
            </div>

          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>
