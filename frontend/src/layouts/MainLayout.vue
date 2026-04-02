<script setup>
import { ref } from 'vue'
import {
  LayoutDashboard,
  Users,
  Building2,
  FileSignature,
  CalendarOff,
  Clock,
  Banknote,
  UserPlus,
  UserMinus,
  ShieldCheck,
  History,
  LogOut,
  Bell,
  Search,
  Menu,
  ChevronRight,
  KeyRound,
  Settings,
  FilePenLine
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
    items: [
      { name: 'Tổng quan', icon: LayoutDashboard, path: '/dashboard' },
    ]
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
  <div class="min-h-screen bg-slate-50 flex font-sans text-slate-900">
    <!-- Sidebar -->
    <aside
      class="fixed inset-y-0 left-0 z-50 w-72 bg-white border-r border-slate-100 transition-transform duration-500 lg:relative lg:translate-x-0"
      :class="!isSidebarOpen ? '-translate-x-full lg:w-20' : ''">
      <div class="h-full flex flex-col p-6">
        <!-- Logo -->
        <div class="flex items-center space-x-3 mb-12">
          <div
            class="w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center text-white shadow-lg shadow-indigo-200 shrink-0">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <span v-if="isSidebarOpen" class="text-xl font-bold tracking-tight whitespace-nowrap">Digital <span
              class="text-indigo-600">HRM</span></span>
        </div>

        <!-- Navigation -->
        <nav class="flex-1 space-y-1 " :class="[isSidebarOpen ? '' : 'place-items-center']">
          <template v-for="group in menuGroups" :key="group.label">
            <!-- Group Label -->
            <div v-if="isSidebarOpen" class="px-4 pt-4 pb-1">
              <span class="text-xs font-black text-slate-400 uppercase tracking-widest">{{ group.label }}</span>
            </div>
            <div v-else class="border-b border-slate-100 my-2"></div>
            <!-- Group Items -->
            <router-link v-for="item in group.items" :key="item.name" :to="item.path"
              class="flex items-center space-x-3 px-4 py-3 rounded-2xl transition-all duration-300 group hover:translate-x-1"
              :class="[
                $route.path === item.path
                  ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-100'
                  : 'text-slate-500 hover:bg-slate-50 hover:text-indigo-600'
              ]">
              <component :is="item.icon" class="w-5 h-5 shrink-0" />
              <span v-if="isSidebarOpen" class="font-semibold text-sm whitespace-nowrap">{{ item.name }}</span>
              <ChevronRight v-if="isSidebarOpen && $route.path === item.path"
                class="w-4 h-4 ml-auto opacity-50 shrink-0" />
            </router-link>
          </template>
        </nav>

        <!-- Logout -->
        <button
          class="flex items-center space-x-3 px-4 py-3.5 mt-8 rounded-2xl text-slate-500 hover:bg-red-50 hover:text-red-600 transition-all duration-300 group">
          <LogOut class="w-5 h-5 transition-transform group-hover:-translate-x-1" />
          <span v-if="isSidebarOpen" class="font-semibold text-sm">Đăng xuất</span>
        </button>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 min-w-0 flex flex-col h-screen overflow-hidden">
      <!-- Top Navbar -->
      <header
        class="h-20 bg-white/80 backdrop-blur-md border-b border-slate-100 px-8 flex items-center justify-between z-40 sticky top-0 shrink-0">
        <div class="flex items-center space-x-4">
          <button @click="isSidebarOpen = !isSidebarOpen" class="p-2 hover:bg-slate-100 rounded-xl transition-colors">
            <Menu class="w-6 h-6 text-slate-500" />
          </button>

          <div class="hidden md:flex items-center bg-slate-100 rounded-xl px-4 py-2 border border-slate-200">
            <Search class="w-4 h-4 text-slate-400 mr-3" />
            <input type="text" placeholder="Tìm kiếm nhanh..."
              class="bg-transparent border-none outline-none text-sm w-64 text-slate-600 font-medium placeholder:text-slate-400" />
          </div>
        </div>

        <div class="flex items-center space-x-6">
          <button class="relative p-2.5 text-slate-500 hover:bg-slate-50 rounded-2xl transition-all group">
            <Bell class="w-5 h-5 group-hover:text-indigo-600" />
            <span class="absolute top-2.5 right-2.5 w-2.5 h-2.5 bg-red-500 border-2 border-white rounded-full"></span>
          </button>

          <div
            class="flex items-center space-x-3 p-1 pl-4 bg-slate-50 border border-slate-100 rounded-2xl cursor-pointer hover:bg-slate-100 transition-colors">
            <div class="text-right hidden sm:block">
              <p class="text-sm font-bold text-slate-900 leading-tight">{{ user.name }}</p>
              <p class="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">{{ user.role }}</p>
            </div>
            <div
              class="w-10 h-10 bg-linear-to-tr from-indigo-500 to-indigo-600 text-white rounded-xl flex items-center justify-center font-bold text-sm shadow-md shadow-indigo-100 border-2 border-white">
              AD
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <div class="flex-1 overflow-y-auto p-8 bg-slate-50/50">
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
  @apply bg-indigo-600 text-white shadow-lg shadow-indigo-100 scale-105;
}
</style>
