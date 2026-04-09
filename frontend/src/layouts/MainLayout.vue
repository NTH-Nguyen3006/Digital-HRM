<script setup>
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { navigationByRole } from '@/config/navigation'
import {
  Bell,
  ChevronDown,
  ChevronRight,
  LogOut,
  Menu,
  Search,
} from 'lucide-vue-next'
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUiStore()

const isSidebarOpen = ref(localStorage.getItem('isSidebarOpen') !== 'false')

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value
  localStorage.setItem('isSidebarOpen', isSidebarOpen.value)
  hideTooltip()
}

const sidebarScrollRef = ref(null)

const handleSidebarScroll = (e) => {
  hideTooltip()
  sessionStorage.setItem('sidebarScrollTop', e.target.scrollTop)
}

onMounted(() => {
  if (sidebarScrollRef.value) {
    const savedScroll = sessionStorage.getItem('sidebarScrollTop')
    if (savedScroll) {
      sidebarScrollRef.value.scrollTop = parseInt(savedScroll, 10)
    }
  }
})

const tooltip = ref({ show: false, text: '', top: 0 })

const showTooltip = (event, text) => {
  if (isSidebarOpen.value) return
  const rect = event.currentTarget.getBoundingClientRect()
  tooltip.value = {
    show: true,
    text: text,
    top: rect.top + (rect.height / 2)
  }
}

const hideTooltip = () => {
  tooltip.value.show = false
}

const handleLogout = async () => {
  hideTooltip()
  const confirmed = await uiStore.confirm({
    title: 'Xác nhận đăng xuất',
    message: 'Bạn có chắc muốn đăng xuất khỏi hệ thống không?',
    confirmLabel: 'Đăng xuất',
    danger: true,
  })

  if (!confirmed) return

  await authStore.logout()
  router.push('/login')
}

// ── Dynamic user info from auth store ─────────────────────────
const userName = computed(() =>
  authStore.user?.fullName || authStore.user?.username || 'Admin User'
)
const userRoleLabel = computed(() => ({
  ADMIN: 'Hệ thống Quản trị',
  HR: 'Nhân sự (HR)',
  MANAGER: 'Quản lý',
  EMPLOYEE: 'Nhân viên',
}[authStore.user?.roleCode] || 'Người dùng'))

const userInitials = computed(() => {
  const n = authStore.user?.fullName || authStore.user?.username || 'AD'
  return n.split(' ').slice(-2).map(w => w[0]?.toUpperCase()).join('')
})

// ── Role-aware menu groups ─────────────────────────────────────
const isManager = computed(() => authStore.isManager)
const isHR = computed(() => authStore.isHR)
const isAdmin = computed(() => authStore.isAdmin)

const menuGroups = computed(() => {
  if (isAdmin.value) return navigationByRole.admin
  if (isHR.value) return navigationByRole.hr
  if (isManager.value) return navigationByRole.manager
  return []
})
</script>

<template>
  <div class="h-screen overflow-hidden bg-slate-50 flex font-sans text-slate-900 w-full relative">

    <div v-if="isSidebarOpen" @click="toggleSidebar"
      class="fixed inset-0 bg-slate-900/40 z-40 lg:hidden backdrop-blur-sm transition-opacity"></div>

    <aside
      class="fixed inset-y-0 left-0 z-50 bg-white border-r border-slate-100 transition-all duration-300 ease-in-out flex flex-col lg:relative"
      :class="isSidebarOpen ? 'w-72 translate-x-0' : 'w-20 -translate-x-full lg:translate-x-0'">

      <div class="h-20 shrink-0 border-b border-transparent flex items-center"
        :class="isSidebarOpen ? 'px-6 justify-start' : 'justify-center'">
        <div class="flex items-center" :class="isSidebarOpen ? 'space-x-3' : ''">
          <div
            class="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-white shadow-lg border-2 border-slate-200 shrink-0">
            <img src="@/assets/images/logo.webp" alt="Logo" class="w-full h-full object-cover rounded-xl">
          </div>
          <span v-if="isSidebarOpen" class="text-xl font-bold tracking-tight whitespace-nowrap overflow-hidden">
            <span class="text-indigo-600">CONVERGED</span>
          </span>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto overscroll-contain hide-scrollbar pb-6" ref="sidebarScrollRef"
        @scroll="handleSidebarScroll" :class="isSidebarOpen ? 'px-6' : 'px-3'">
        <nav class="space-y-1 mt-4">
          <template v-for="group in menuGroups" :key="group.label">

            <div v-if="isSidebarOpen" class="px-4 pt-4 pb-1">
              <span class="text-xs font-black text-slate-400 uppercase tracking-widest whitespace-nowrap">{{ group.label
                }}</span>
            </div>
            <div v-else class="border-b border-slate-200 my-4 mx-2"></div>

            <router-link v-for="item in group.items" :key="item.name" :to="item.path" @click="handleMenuClick"
              @mouseenter="showTooltip($event, item.name)" @mouseleave="hideTooltip"
              class="relative flex items-center py-3 rounded-2xl transition-all duration-300 group" :class="[
                $route.path === item.path ? 'bg-indigo-600 text-white shadow-md shadow-indigo-200' : 'text-slate-500 hover:bg-slate-50 hover:text-indigo-600',
                isSidebarOpen ? 'px-4 space-x-3 hover:translate-x-1' : 'justify-center'
              ]">

              <component :is="item.icon" class="w-5 h-5 shrink-0" />

              <span v-if="isSidebarOpen" class="font-semibold text-sm whitespace-nowrap overflow-hidden">{{ item.name
                }}</span>

              <ChevronRight v-if="isSidebarOpen && $route.path === item.path"
                class="w-4 h-4 ml-auto opacity-50 shrink-0" />
            </router-link>
          </template>
        </nav>

        <button @click="handleLogout" @mouseenter="showTooltip($event, 'Đăng xuất')" @mouseleave="hideTooltip"
          class="relative flex items-center py-3.5 mt-8 rounded-2xl text-slate-500 hover:bg-red-50 hover:text-red-600 transition-all duration-300 group w-full cursor-pointer"
          :class="isSidebarOpen ? 'px-4 space-x-3' : 'justify-center'">

          <LogOut class="w-5 h-5 shrink-0"
            :class="isSidebarOpen ? 'group-hover:-translate-x-1 transition-transform' : ''" />
          <span v-if="isSidebarOpen" class="font-semibold text-sm whitespace-nowrap overflow-hidden">Đăng xuất</span>
        </button>
      </div>
    </aside>

    <main class="flex-1 min-w-0 flex flex-col h-screen">
      <header
        class="h-20 bg-white/80 backdrop-blur-md border-b border-slate-100 px-4 sm:px-6 lg:px-8 flex items-center justify-between z-30 sticky top-0 shrink-0">
        <div class="flex items-center space-x-4">
          <button @click="toggleSidebar" class="p-2 hover:bg-slate-100 rounded-xl transition-colors">
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
              <p class="text-sm font-bold text-slate-900 leading-tight">{{ userName }}</p>
              <p class="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">{{ userRoleLabel }}</p>
            </div>
            <div
              class="w-10 h-10 bg-indigo-600 text-white rounded-xl flex items-center justify-center font-bold text-sm shadow-md shadow-indigo-200 border-2 border-white">
              {{ userInitials }}</div>
            <ChevronDown class="w-4 h-4 text-slate-400" />
          </div>
        </div>
      </header>

      <div class="flex-1 overflow-y-auto overscroll-contain p-4 sm:p-6 lg:p-8 bg-slate-50/50">
        <div class="max-w-7xl mx-auto animate-fade-in">
          <slot />
        </div>
      </div>
    </main>

    <transition name="slide-fade">
      <div v-if="tooltip.show"
        class="fixed left-21 px-3 py-2 bg-slate-800 text-white text-xs font-bold rounded-lg whitespace-nowrap z-100 shadow-lg pointer-events-none transform -translate-y-1/2 flex items-center"
        :style="{ top: tooltip.top + 'px' }">
        <div class="absolute -left-1 top-1/2 -translate-y-1/2 w-2 h-2 bg-slate-800 rotate-45 rounded-sm"></div>
        {{ tooltip.text }}
      </div>
    </transition>

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

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translate(-10px, -50%);
}

.slide-fade-enter-to,
.slide-fade-leave-from {
  opacity: 1;
  transform: translate(0, -50%);
}
</style>
