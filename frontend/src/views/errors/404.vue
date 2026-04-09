<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Home, ArrowLeft, Ghost } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push(authStore.isEmployee ? '/' : '/dashboard')
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 flex items-center justify-center p-6 relative overflow-hidden font-sans">
    
    <!-- Background Decor -->
    <div class="absolute -top-24 -left-24 w-96 h-96 bg-indigo-200/40 rounded-full blur-3xl animate-pulse"></div>
    <div class="absolute -bottom-24 -right-24 w-96 h-96 bg-emerald-200/30 rounded-full blur-3xl delay-700 animate-pulse"></div>

    <div class="max-w-xl w-full text-center relative z-10">
      <!-- 404 Illustration -->
      <div class="relative mb-8 flex justify-center">
        <div class="text-[180px] font-black text-slate-100 select-none leading-none tracking-tighter italic">
          404
        </div>
        <div class="absolute inset-0 flex items-center justify-center pt-8">
             <Ghost class="w-24 h-24 text-indigo-600 animate-bounce" />
        </div>
      </div>

      <!-- Content -->
      <h1 class="text-4xl font-black text-slate-900 mb-4 tracking-tight">
        Ôi! Trang này đi vắng rồi
      </h1>
      <p class="text-slate-500 text-lg mb-10 font-medium max-w-md mx-auto leading-relaxed">
        Có vẻ như đường dẫn bạn đang tìm đã được di chuyển hoặc không còn tồn tại trong hệ thống nhân sự của chúng tôi.
      </p>

      <!-- Actions -->
      <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
        <button 
          @click="goBack"
          class="w-full sm:w-auto px-8 py-4 bg-white border-2 border-slate-200 rounded-2xl font-bold text-slate-600 hover:bg-slate-50 hover:border-slate-300 transition-all flex items-center justify-center gap-2 shadow-sm"
        >
          <ArrowLeft class="w-5 h-5" />
          Quay lại
        </button>
        
        <button 
          @click="goHome"
          class="w-full sm:w-auto px-8 py-4 bg-indigo-600 rounded-2xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 flex items-center justify-center gap-2"
        >
          <Home class="w-5 h-5" />
          Về trang chủ
        </button>
      </div>

      <!-- Footer Help -->
      <div class="mt-16 pt-8 border-t border-slate-200/60">
        <p class="text-sm text-slate-400 font-medium italic">
          Nếu đây là lỗi hệ thống, vui lòng liên hệ <span class="text-indigo-500 font-bold">Bộ phận IT</span> để được hỗ trợ.
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes bounce {
  0%, 100% {
    transform: translateY(-5%);
    animation-timing-function: cubic-bezier(0.8, 0, 1, 1);
  }
  50% {
    transform: translateY(0);
    animation-timing-function: cubic-bezier(0, 0, 0.2, 1);
  }
}
</style>
