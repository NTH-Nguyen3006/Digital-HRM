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

    // Redirect based on homeRoute from backend or fallback logic
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
  <AuthLayout class="p-0">
    <div class="w-full max-w-[420px] mx-auto animate-fade-in-up" style="animation-delay: 0.1s;">
      <div class="text-center mb-10">
        <div
          class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-indigo-50 text-indigo-600 mb-6 shadow-sm border border-indigo-100/50">
          <Fingerprint class="w-9 h-9" />
        </div>
        <h2 class="text-3xl font-extrabold text-slate-900 tracking-tight mb-3">Đăng nhập</h2>
        <p class="text-slate-500 font-medium text-base">Chào mừng trở lại Digital <span
            class="text-indigo-600 font-bold">HRM</span></p>
      </div>

      <div v-if="errorMessage"
        class="mb-6 p-4 bg-red-50 border border-red-100 rounded-2xl text-red-600 text-sm font-bold flex items-center gap-3 animate-shake">
        <AlertTriangle class="w-5 h-5 shrink-0" />
        {{ errorMessage }}
      </div>

      <form @submit.prevent="handleLogin" class="space-y-6">
        <div class="space-y-5">
          <BaseInput v-model="loginId" label="Tài khoản / Email" type="text"
            placeholder="Mã nhân viên hoặc email công ty" :icon="Mail" required />

          <BaseInput v-model="password" label="Mật khẩu" type="password" placeholder="Nhập mật khẩu của bạn"
            :icon="Lock" required />
        </div>

        <div class="flex items-center justify-between mt-2">
          <label class="flex items-center space-x-2.5 cursor-pointer group">
            <div class="relative flex items-center justify-center">
              <input type="checkbox" v-model="rememberMe"
                class="peer appearance-none w-5 h-5 border-2 border-slate-300 rounded-md bg-white/50 checked:bg-indigo-600 checked:border-indigo-600 focus:ring-4 focus:ring-indigo-500/20 transition-all cursor-pointer" />
              <svg
                class="absolute w-3.5 h-3.5 text-white opacity-0 peer-checked:opacity-100 transition-opacity duration-200 pointer-events-none"
                fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <span class="text-sm font-semibold text-slate-600 group-hover:text-slate-900 transition-colors">Ghi nhớ
              tôi</span>
          </label>
          <a href="#"
            class="text-sm font-bold text-indigo-600 hover:text-indigo-500 transition-colors hover:underline">Quên mật
            khẩu?</a>
        </div>

        <BaseButton type="submit" variant="primary" size="lg" class="w-full mt-8 group relative overflow-hidden"
          :loading="loading" :icon="LogIn" iconRight>
          <span class="font-bold tracking-wide relative z-10">Xác thực & Truy cập</span>
        </BaseButton>

        <p class="text-center text-sm font-medium text-slate-500 mt-10">
          Gặp khó khăn khi đăng nhập?
          <a href="#"
            class="text-indigo-600 font-bold hover:text-indigo-500 transition-colors ml-1 hover:underline">Liên hệ bộ
            phận
            IT / HR</a>
        </p>
      </form>
    </div>
  </AuthLayout>
</template>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
