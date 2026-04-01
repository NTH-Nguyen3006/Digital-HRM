<script setup>
import { useRouter } from 'vue-router'
import { LogIn, ArrowLeft } from 'lucide-vue-next'
import BaseButton from '@/components/common/BaseButton.vue'

defineProps({
  variant: {
    type: String,
    default: 'portal' // 'portal' | 'auth'
  }
})

const router = useRouter()
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
          'w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center text-white shadow-lg',
          variant === 'portal' ? 'shadow-indigo-100' : 'shadow-indigo-200'
        ]">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        <span class="text-xl font-bold tracking-tight text-slate-900">Digital <span
            class="text-indigo-600">HRM</span></span>
      </div>

      <!-- Portal Nav & Actions -->
      <template v-if="variant === 'portal'">
        <nav class="hidden md:flex items-center space-x-10">
          <a href="#features" class="text-sm font-bold text-slate-600 hover:text-indigo-600 transition-colors">Tính
            năng</a>
          <a href="#services" class="text-sm font-bold text-slate-600 hover:text-indigo-600 transition-colors">Dịch vụ
          </a>
          <a href="#news" class="text-sm font-bold text-slate-600 hover:text-indigo-600 transition-colors">Tin tức</a>
        </nav>
        <div class="flex items-center space-x-4">
          <BaseButton variant="ghost" class="hidden sm:flex" @click="router.push('/login')">Trợ giúp</BaseButton>
          <BaseButton variant="primary" :icon="LogIn" @click="router.push('/login')">Đăng nhập</BaseButton>
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
