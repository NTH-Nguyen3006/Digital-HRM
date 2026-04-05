<script setup>
import { ref } from 'vue'
import {
  LayoutDashboard, Users, Building2, FileSignature, CalendarOff,
  Clock, Banknote, UserPlus, UserMinus, ShieldCheck, History,
  LogOut, Bell, Search, Menu, ChevronRight, ChevronDown,
  KeyRound, Settings, FilePenLine
} from 'lucide-vue-next'

const isSidebarOpen = ref(true)

const user = ref({
  name: 'Admin User',
  role: 'Hệ thống Quản trị',
  avatar: null
})

const menuGroups = [
  {
    label: 'Tổng hợp',
    items: [{ name: 'Tổng quan', icon: LayoutDashboard, path: '/dashboard' }]
  },
  {
    label: 'Quản lý nhân sự',
    items: [
      { name: 'Cơ cấu tổ chức', icon: Building2, path: '/org-units' },
      { name: 'Hồ sơ nhân sự', icon: Users, path: '/employees' },
      { name: 'Hợp đồng lao động', icon: FileSignature, path: '/contracts' },
      { name: 'Quản lý nghỉ phép', icon: CalendarOff, path: '/leaves' },
      { name: 'Chấm công', icon: Clock, path: '/attendance' },
      { name: 'Tính lương', icon: Banknote, path: '/payroll' },
      { name: 'Thay đổi hồ sơ', icon: FilePenLine, path: '/profile-change-requests' },
    ]
  },
  {
    label: 'Vòng đời nhân sự',
    items: [
      { name: 'Tiếp nhận (Onboarding)', icon: UserPlus, path: '/onboarding' },
      { name: 'Thôi việc (Offboarding)', icon: UserMinus, path: '/offboarding' },
    ]
  },
  {
    label: 'Hệ thống',
    items: [
      { name: 'Tài khoản hệ thống', icon: KeyRound, path: '/users' },
      { name: 'Vai trò & Phân quyền', icon: ShieldCheck, path: '/roles' },
      { name: 'Nhật ký hoạt động', icon: History, path: '/audit-logs' },
      { name: 'Cài đặt hệ thống', icon: Settings, path: '/settings' },
    ]
  },
]
</script>

<template>
  <div class="h-screen overflow-hidden bg-slate-50 flex font-sans text-slate-900 w-full">

    <div v-if="isSidebarOpen" @click="isSidebarOpen = false"
      class="fixed inset-0 bg-slate-900/40 z-40 lg:hidden backdrop-blur-sm transition-opacity"></div>

    <aside
      class="fixed inset-y-0 left-0 z-50 w-72 bg-white border-r border-slate-100 transition-transform duration-500 lg:relative lg:translate-x-0 flex flex-col"
      :class="!isSidebarOpen ? '-translate-x-full lg:w-20' : ''">

      <div class="p-6 shrink-0 border-b border-transparent">
        <div class="flex items-center space-x-3 mb-2">
          <div
            class="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-white shadow-lg border-3 border-slate-200 shrink-0">
            <img src="@/assets/images/logo.webp" alt="Logo" class="w-full h-full object-cover rounded-xl">
          </div>
          <span v-if="isSidebarOpen" class="text-xl font-bold tracking-tight whitespace-nowrap">
            <span class="text-indigo-600">CONVERGED</span>
          </span>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto overscroll-contain hide-scrollbar px-6 pb-6">
        <nav class="space-y-1 mt-4" :class="[isSidebarOpen ? '' : 'place-items-center']">
          <template v-for="group in menuGroups" :key="group.label">

            <div v-if="isSidebarOpen" class="px-4 pt-4 pb-1">
              <span class="text-xs font-black text-slate-400 uppercase tracking-widest">{{ group.label }}</span>
            </div>
            <div v-else class="border-b border-slate-100 my-2 w-full"></div>

            <router-link v-for="item in group.items" :key="item.name" :to="item.path"
              :title="!isSidebarOpen ? item.name : ''"
              class="flex items-center space-x-3 px-4 py-3 rounded-2xl transition-all duration-300 group hover:translate-x-1"
              :class="[$route.path === item.path ? 'bg-indigo-600 text-white shadow-md shadow-indigo-200' : 'text-slate-500 hover:bg-slate-50 hover:text-indigo-600']">
              <component :is="item.icon" class="w-5 h-5 shrink-0" />
              <span v-if="isSidebarOpen" class="font-semibold text-sm whitespace-nowrap">{{ item.name }}</span>
              <ChevronRight v-if="isSidebarOpen && $route.path === item.path"
                class="w-4 h-4 ml-auto opacity-50 shrink-0" />
            </router-link>
          </template>
        </nav>

        <button
          class="flex items-center space-x-3 px-4 py-3.5 mt-8 rounded-2xl text-slate-500 hover:bg-red-50 hover:text-red-600 transition-all duration-300 group w-full">
          <LogOut class="w-5 h-5 transition-transform group-hover:-translate-x-1" />
          <span v-if="isSidebarOpen" class="font-semibold text-sm">Đăng xuất</span>
        </button>
      </div>
    </aside>
    <main class="flex-1 min-w-0 flex flex-col h-screen">

      <header
        class="h-20 bg-white/80 backdrop-blur-md border-b border-slate-100 px-8 flex items-center justify-between z-30 sticky top-0 shrink-0">
        <div class="flex items-center space-x-4">
          <button @click="isSidebarOpen = !isSidebarOpen" class="p-2 hover:bg-slate-100 rounded-xl transition-colors">
            <Menu class="w-6 h-6 text-slate-500" />
          </button>

          <div
            class="hidden md:flex items-center bg-slate-100/70 rounded-xl px-4 py-2 border border-slate-200 focus-within:bg-white focus-within:border-indigo-400 focus-within:ring-4 focus-within:ring-indigo-500/10 transition-all">
            <Search class="w-4 h-4 text-slate-400 mr-2" />
            <input type="text" placeholder="Tìm kiếm nhanh..."
              class="bg-transparent border-none outline-none text-sm w-56 text-slate-700 font-medium placeholder:text-slate-400" />
          </div>
        </div>

        <div class="flex items-center space-x-6">
          <button class="relative p-2.5 text-slate-500 hover:bg-slate-50 rounded-2xl transition-all group">
            <Bell class="w-5 h-5 group-hover:text-indigo-600" />
            <span class="absolute top-2.5 right-2.5 w-2.5 h-2.5 bg-red-500 border-2 border-white rounded-full"></span>
          </button>

          <div
            class="flex items-center space-x-3 p-1 pl-4 pr-3 bg-slate-50 border border-slate-100 rounded-2xl cursor-pointer hover:bg-slate-100 hover:border-slate-200 transition-all">
            <div class="text-right hidden sm:block">
              <p class="text-sm font-bold text-slate-900 leading-tight">{{ user.name }}</p>
              <p class="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">{{ user.role }}</p>
            </div>
            <div
              class="w-10 h-10 bg-indigo-600 text-white rounded-xl flex items-center justify-center font-bold text-sm shadow-md shadow-indigo-200 border-2 border-white">
              AD</div>
            <ChevronDown class="w-4 h-4 text-slate-400" />
          </div>
        </div>
      </header>
      <div class="flex-1 overflow-y-auto overscroll-contain p-8 bg-slate-50/50">
        <div class="max-w-7xl mx-auto animate-fade-in">
          <slot />
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
@reference "../assets/css/main.css";

.animate-fade-in {
  animation: fadeIn 0.4s ease-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.router-link-active {
  @apply bg-indigo-600 text-white shadow-md shadow-indigo-200;
}

.hide-scrollbar::-webkit-scrollbar {
  display: none;
}

.hide-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>