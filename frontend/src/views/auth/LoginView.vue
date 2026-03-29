<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Mail, Lock, LogIn } from 'lucide-vue-next'
import AuthLayout from '@/layouts/AuthLayout.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const router = useRouter()
const email = ref('')
const password = ref('')
const loading = ref(false)
const rememberMe = ref(false)

const handleLogin = async () => {
  if (!email.value || !password.value) return

  loading.value = true

  // Simulate API call
  setTimeout(() => {
    loading.value = false
    router.push('/dashboard')
  }, 1000)
}
</script>

<template>
  <AuthLayout class="p-16">
    <div class="w-full max-w-[360px] mx-auto animate-fade-in-up" style="animation-delay: 0.1s;">
      <div class="text-center mb-10">
        <h2 class="text-3xl font-extrabold text-slate-800 tracking-tight mb-3">Đăng nhập</h2>
        <p class="text-slate-500 font-medium">Chào mừng trở lại Digital HRM</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-6">
        <div class="space-y-5">
          <BaseInput v-model="email" label="Email công ty" type="email" placeholder="ten.ho@company.com" :icon="Mail"
            required />

          <BaseInput v-model="password" label="Mật khẩu" type="password" placeholder="••••••••" :icon="Lock" required />
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

        <BaseButton type="submit" variant="primary" size="lg" class="w-full mt-8 group" :loading="loading" :icon="LogIn"
          iconRight>
          <span class="font-bold tracking-wide">Vào Không Gian Làm Việc</span>
        </BaseButton>

        <p class="text-center text-sm font-medium text-slate-500 mt-8">
          Chưa có tài khoản?
          <a href="#" class="text-indigo-600 font-bold hover:text-indigo-500 transition-colors ml-1">Liên hệ bộ phận
            HR</a>
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
