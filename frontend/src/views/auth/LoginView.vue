<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Mail, Lock, LogIn, AlertTriangle, Fingerprint } from 'lucide-vue-next'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loginId = ref('')
const password = ref('')
const loading = ref(false)
const rememberMe = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  if (!loginId.value || !password.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    const user = await authStore.login(loginId.value, password.value)

    if (user.homeRoute) {
      router.push(user.homeRoute)
    } else if (user.roleCode === 'EMPLOYEE') {
      router.push('/')
    } else {
      router.push('/dashboard')
    }
  } catch (error) {
    console.error('Login Error:', error)
    errorMessage.value = error.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen w-full flex items-center justify-center bg-[#f8fafc] relative overflow-hidden font-sans">
    <!-- Dynamic Background Orbs -->
    <div class="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-indigo-200/40 blur-[120px] rounded-full animate-pulse"></div>
    <div class="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-blue-200/40 blur-[120px] rounded-full animate-pulse" style="animation-delay: 2s;"></div>

    <div class="w-full max-w-[440px] px-6 relative z-10 animate-fade-in-up">
      <!-- Glass Card Container -->
      <div class="bg-white/70 backdrop-blur-xl border border-white/40 shadow-[0_20px_50px_rgba(0,0,0,0.05)] rounded-[32px] overflow-hidden p-8 md:p-10">
        
        <!-- Header -->
        <div class="text-center mb-10">
          <div class="inline-flex items-center justify-center w-20 h-20 rounded-[24px] bg-linear-to-tr from-indigo-600 to-blue-500 text-white mb-6 shadow-xl shadow-indigo-200/50 transform hover:scale-105 transition-transform duration-300">
            <Fingerprint class="w-10 h-10" />
          </div>
          <h2 class="text-4xl font-black text-slate-900 tracking-tight mb-2">Đăng nhập</h2>
          <p class="text-slate-500 font-medium">Chào mừng trở lại <span class="text-indigo-600 font-bold">HRM System</span></p>
        </div>

        <!-- Error Alert -->
        <Transition name="fade-shake">
          <div v-if="errorMessage" class="mb-8 p-4 bg-rose-50 border border-rose-100 rounded-2xl text-rose-600 text-sm font-bold flex items-center gap-3">
            <AlertTriangle class="w-5 h-5 shrink-0" />
            {{ errorMessage }}
          </div>
        </Transition>

        <!-- Form -->
        <form @submit.prevent="handleLogin" class="space-y-6">
          <div class="space-y-4">
            <BaseInput 
              v-model="loginId" 
              label="Tài khoản hệ thống" 
              type="text"
              placeholder="Mã nhân viên / Email" 
              :icon="Mail" 
              required 
              class="transition-all duration-300"
            />

            <BaseInput 
              v-model="password" 
              label="Mật khẩu" 
              type="password" 
              placeholder="••••••••"
              :icon="Lock" 
              required 
              class="transition-all duration-300"
            />
          </div>

          <div class="flex items-center justify-between mt-2">
            <label class="flex items-center space-x-2.5 cursor-pointer group">
              <div class="relative flex items-center justify-center">
                <input type="checkbox" v-model="rememberMe"
                  class="peer appearance-none w-5 h-5 border-2 border-slate-200 rounded-md bg-white checked:bg-indigo-600 checked:border-indigo-600 transition-all cursor-pointer" />
                <Check class="absolute w-3.5 h-3.5 text-white opacity-0 peer-checked:opacity-100 transition-opacity duration-200 pointer-events-none" />
              </div>
              <span class="text-sm font-semibold text-slate-500 group-hover:text-slate-900 transition-colors">Ghi nhớ đăng nhập</span>
            </label>
            <a href="#" class="text-sm font-bold text-indigo-600 hover:text-indigo-400 transition-colors">Quên mật khẩu?</a>
          </div>

          <BaseButton 
            type="submit" 
            variant="primary" 
            size="lg" 
            class="w-full mt-6 h-14! rounded-2xl! shadow-lg shadow-indigo-100 hover:shadow-indigo-200 transition-all active:scale-[0.98]"
            :loading="loading" 
            :icon="LogIn" 
            iconRight
          >
            Xác thực hệ thống
          </BaseButton>

          <div class="text-center mt-10">
            <p class="text-sm font-medium text-slate-400">
              Bạn gặp sự cố kỹ thuật? 
              <a href="#" class="text-indigo-600 font-bold hover:underline ml-1">Hỗ trợ ngay</a>
            </p>
          </div>
        </form>
      </div>
      
      <!-- Footer Info -->
      <div class="mt-8 text-center text-xs text-slate-400 font-medium">
        &copy; 2026 Digital HRM Platform. All Rights Reserved.
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-shake-enter-active {
  animation: shake 0.4s cubic-bezier(.36,.07,.19,.97) both;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}
</style>
