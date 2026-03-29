<script setup>
import { ref } from 'vue'
import MainLayout from '../../layouts/MainLayout.vue'
import StatCard from '../../components/common/StatCard.vue'
import GlassCard from '../../components/common/GlassCard.vue'
import {
  Users,
  Building2,
  Briefcase,
  UserCheck,
  TrendingUp,
  Clock,
  MoreVertical
} from 'lucide-vue-next'

const stats = [
  { title: 'Tổng nhân sự', value: '1,284', trend: 12, trendLabel: 'tháng này', icon: Users, color: 'indigo' },
  { title: 'Phòng ban', value: '24', trend: 0, trendLabel: 'không đổi', icon: Building2, color: 'emerald' },
  { title: 'Vị trí công việc', value: '86', trend: 5, trendLabel: 'tháng này', icon: Briefcase, color: 'sky' },
  { title: 'Đang làm việc', value: '1,240', trend: 2, trendLabel: 'tháng này', icon: UserCheck, color: 'rose' },
]

const recentActivities = ref([
  { id: 1, user: 'Nguyễn Văn A', action: 'Cập nhật hồ sơ', target: 'Nhân viên mới', time: '2 phút trước', status: 'success' },
  { id: 2, user: 'Trần Thị B', action: 'Tạo mới phòng ban', target: 'Phòng Kỹ Thuật', time: '15 phút trước', status: 'info' },
  { id: 3, user: 'Lê Văn C', action: 'Điều chuyển nhân sự', target: 'Lê Văn D', time: '1 giờ trước', status: 'warning' },
  { id: 4, user: 'Phạm Thị E', action: 'Xoá tài liệu', target: 'Hợp đồng lao động', time: '3 giờ trước', status: 'danger' },
])
</script>

<template>
  <MainLayout>
    <div class="space-y-10">
      <!-- Welcome Header -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-4xl font-black text-slate-900 tracking-tight leading-tight">Chào buổi sáng, Admin!</h2>
          <p class="text-slate-500 font-medium text-lg mt-1">Hôm nay có 4 hoạt động mới cần bạn lưu ý.</p>
        </div>
        <div class="flex items-center space-x-3">
          <button
            class="bg-white border border-slate-200 px-6 py-3 rounded-2xl font-bold text-slate-700 hover:bg-slate-50 transition-all shadow-sm">
            Xuất báo cáo
          </button>
          <button
            class="bg-indigo-600 px-6 py-3 rounded-2xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-100 flex items-center">
            <TrendingUp class="w-5 h-5 mr-2" /> Phân tích HR
          </button>
        </div>
      </div>

      <!-- Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard v-for="stat in stats" :key="stat.title" v-bind="stat" />
      </div>

      <div class="grid lg:grid-cols-3 gap-8">
        <!-- Recent Activities Table -->
        <GlassCard :glass="false" class="lg:col-span-2 overflow-hidden border-none shadow-xl shadow-slate-200/50">
          <div class="flex items-center justify-between mb-8 px-2">
            <h3 class="text-xl font-bold text-slate-900 flex items-center">
              <Clock class="w-6 h-6 mr-3 text-indigo-500" /> Hoạt động gần đây
            </h3>
            <button class="text-indigo-600 font-bold text-sm hover:underline">Xem tất cả</button>
          </div>

          <div class="overflow-x-auto -mx-2">
            <table class="w-full">
              <thead>
                <tr class="text-left border-b border-slate-100">
                  <th class="pb-5 px-4 font-bold text-slate-400 text-xs uppercase tracking-widest">Người thực hiện</th>
                  <th class="pb-5 px-4 font-bold text-slate-400 text-xs uppercase tracking-widest">Hành động</th>
                  <th class="pb-5 px-4 font-bold text-slate-400 text-xs uppercase tracking-widest">Đối tượng</th>
                  <th class="pb-5 px-4 font-bold text-slate-400 text-xs uppercase tracking-widest">Thời gian</th>
                  <th class="pb-5 px-4"></th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-50">
                <tr v-for="activity in recentActivities" :key="activity.id"
                  class="group hover:bg-slate-50/50 transition-colors">
                  <td class="py-5 px-4">
                    <div class="flex items-center space-x-3">
                      <div
                        class="w-9 h-9 rounded-xl bg-slate-100 flex items-center justify-center font-bold text-slate-700 text-xs border border-white">
                        {{ activity.user.split(' ').pop().charAt(0) }}
                      </div>
                      <span class="font-bold text-slate-900 text-sm whitespace-nowrap">{{ activity.user }}</span>
                    </div>
                  </td>
                  <td class="py-5 px-4">
                    <span class="px-3 py-1.5 rounded-xl text-xs font-bold ring-1 ring-inset" :class="[
                      activity.status === 'success' ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/10' : '',
                      activity.status === 'info' ? 'bg-sky-50 text-sky-700 ring-sky-600/10' : '',
                      activity.status === 'warning' ? 'bg-amber-50 text-amber-700 ring-amber-600/10' : '',
                      activity.status === 'danger' ? 'bg-rose-50 text-rose-700 ring-rose-600/10' : ''
                    ]">
                      {{ activity.action }}
                    </span>
                  </td>
                  <td class="py-5 px-4 font-medium text-slate-600 text-sm whitespace-nowrap">{{ activity.target }}</td>
                  <td class="py-5 px-4 text-slate-400 text-sm font-medium">{{ activity.time }}</td>
                  <td class="py-5 px-4 text-right">
                    <button class="p-2 hover:bg-white rounded-lg transition-all opacity-0 group-hover:opacity-100">
                      <MoreVertical class="w-5 h-5 text-slate-400" />
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </GlassCard>

        <!-- Side Widget: Quick Actions -->
        <div class="space-y-6">
          <GlassCard :glass="true"
            class="bg-linear-to-br from-indigo-600 to-indigo-700 text-white border-none shadow-indigo-200">
            <h3 class="text-xl font-bold mb-4">Lối tắt nhanh</h3>
            <p class="text-indigo-100 text-sm mb-8 font-medium leading-relaxed">Truy cập nhanh các thao tác quản trị
              thường xuyên nhất.</p>

            <div class="grid grid-cols-2 gap-3">
              <button
                class="bg-white/10 hover:bg-white/20 p-4 rounded-2xl flex flex-col items-center justify-center transition-all border border-white/10 group">
                <Users class="w-6 h-6 mb-2 group-hover:scale-110 transition-transform" />
                <span class="text-xs font-bold">+ Nhân viên</span>
              </button>
              <button
                class="bg-white/10 hover:bg-white/20 p-4 rounded-2xl flex flex-col items-center justify-center transition-all border border-white/10 group">
                <Clock class="w-6 h-6 mb-2 group-hover:scale-110 transition-transform" />
                <span class="text-xs font-bold">Chấm công</span>
              </button>
              <button
                class="bg-white/10 hover:bg-white/20 p-4 rounded-2xl flex flex-col items-center justify-center transition-all border border-white/10 group">
                <Briefcase class="w-6 h-6 mb-2 group-hover:scale-110 transition-transform" />
                <span class="text-xs font-bold">Viết đơn</span>
              </button>
              <button
                class="bg-white/10 hover:bg-white/20 p-4 rounded-2xl flex flex-col items-center justify-center transition-all border border-white/10 group">
                <FileText class="w-6 h-6 mb-2 group-hover:scale-110 transition-transform" />
                <span class="text-xs font-bold">Báo cáo</span>
              </button>
            </div>
          </GlassCard>

          <GlassCard padding="p-6" class="border-none shadow-xl shadow-slate-200/40">
            <h3 class="text-lg font-bold text-slate-900 mb-6">Trạng thái hệ thống</h3>
            <div class="space-y-5">
              <div v-for="i in 3" :key="i" class="flex items-center justify-between">
                <div class="flex items-center space-x-3">
                  <div class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></div>
                  <span class="text-sm font-bold text-slate-700">Service {{ i }}</span>
                </div>
                <span class="text-xs font-bold text-emerald-600 uppercase tracking-widest">Hoạt động</span>
              </div>
            </div>
          </GlassCard>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<style scoped>
/* Page specific animations if needed */
</style>
