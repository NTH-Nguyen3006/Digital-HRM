<script setup>
import { useRouter } from 'vue-router'
import { LogIn, ArrowLeft } from 'lucide-vue-next'
import BaseButton from '@/components/common/BaseButton.vue'
import { useUiStore } from '@/stores/ui'

defineProps({
  variant: {
    type: String,
    default: 'portal' // 'portal' | 'auth'
  }
})

const router = useRouter()
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
const uiStore = useUiStore()

const handleLogout = async () => {
    const confirmed = await uiStore.confirm({
        title: 'Xác nhận đăng xuất',
        message: 'Bạn có chắc muốn đăng xuất khỏi hệ thống không?',
        confirmLabel: 'Đăng xuất',
        danger: true,
    })

    if (!confirmed) return

    await auth.logout()
    router.push('/login')
}
</script>

<template>
  <header :class="[
    'w-full z-50 border-b',
    variant === 'portal' ? 'fixed top-0 left-0 bg-white/70 backdrop-blur-xl border-slate-100' : 'relative bg-white/40 backdrop-blur-xl border-white/60'
  ]">
    <div class="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
      <!-- Logo -->
      <div class="flex items-center space-x-3 cursor-pointer" @click="router.push('/')">
        <div :class="[
          'w-10 h-10 bg-white rounded-xl flex items-center justify-center text-white shadow-lg',
          variant === 'portal' ? 'shadow-indigo-100' : 'shadow-indigo-200'
        ]">

          <img src="@/assets/images/logo.webp" alt="Logo" class="w-6 h-6" />
        </div>
        <span class="text-xl font-bold tracking-tight text-slate-900">Digital <span
            class="text-indigo-600">HRM</span></span>
      </div>

      <!-- Portal Nav & Actions -->
      <template v-if="variant === 'portal'">
        <nav v-if="!auth.isAuthenticated" class="hidden md:flex items-center space-x-10">
          <a href="#features" class="text-sm font-bold text-slate-600 hover:text-indigo-600 transition-colors">Tính năng</a>
          <a href="#services" class="text-sm font-bold text-slate-600 hover:text-indigo-600 transition-colors">Dịch vụ</a>
        </nav>
        <nav v-else class="hidden md:flex items-center space-x-6">
          <router-link to="/portal/attendance" class="text-sm font-bold transition-colors" active-class="text-indigo-600" :class="$route.path === '/portal/attendance' ? '' : 'text-slate-600 hover:text-indigo-600'">Chấm công</router-link>
          <router-link to="/portal/leaves" class="text-sm font-bold transition-colors" active-class="text-indigo-600" :class="$route.path === '/portal/leaves' ? '' : 'text-slate-600 hover:text-indigo-600'">Nghỉ phép</router-link>
          <router-link to="/portal/payslip" class="text-sm font-bold transition-colors" active-class="text-indigo-600" :class="$route.path === '/portal/payslip' ? '' : 'text-slate-600 hover:text-indigo-600'">Phiếu lương</router-link>
          <router-link to="/portal/profile" class="text-sm font-bold transition-colors" active-class="text-indigo-600" :class="$route.path === '/portal/profile' ? '' : 'text-slate-600 hover:text-indigo-600'">Hồ sơ</router-link>
        </nav>
        
        <div class="flex items-center space-x-4">
          <template v-if="!auth.isAuthenticated">
            <BaseButton variant="ghost" class="hidden sm:flex" @click="router.push('/login')">Trợ giúp</BaseButton>
            <BaseButton variant="primary" :icon="LogIn" @click="router.push('/login')">Đăng nhập</BaseButton>
          </template>
          <template v-else>
            <button v-if="auth.isManager || auth.isAdmin || auth.isHR" @click="router.push('/dashboard')" class="text-sm font-bold text-indigo-600 bg-indigo-50 px-4 py-2 rounded-xl hover:bg-indigo-100 transition">
              Vào Dashboard
            </button>
            <button @click="handleLogout" class="text-sm font-bold text-slate-500 hover:text-rose-600 px-4 py-2 hover:bg-rose-50 rounded-xl transition">
              Đăng xuất
            </button>
          </template>
        </div>
      </template>

      <!-- Auth Actions -->
      <template v-else-if="variant === 'auth'">
        <button @click="router.push('/')"
          class="flex items-center space-x-2 px-4 py-2 rounded-full bg-white/60 backdrop-blur-md border border-white/60 text-slate-600 font-bold text-sm hover:bg-white hover:text-indigo-600 hover:shadow-lg hover:shadow-indigo-100 transition-all group">
          <ArrowLeft class="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
          <span>Trang chủ</span>
        </button>
      </template>
    </div>
  </header>
</template>
