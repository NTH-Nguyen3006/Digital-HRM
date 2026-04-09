<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Mail, Lock, LogIn, AlertTriangle, Fingerprint, Check, Eye, EyeOff, LifeBuoy, Phone, X } from 'lucide-vue-next'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { forgotPassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()
const loginId = ref('')
const password = ref('')
const loading = ref(false)
const rememberMe = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const showForgotModal = ref(false)
const showSupportModal = ref(false)
const forgotEmail = ref('')
const forgotLoading = ref(false)

const supportEmail = 'support@company.com'
const supportHotline = '1900 123 456'

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

const openForgotModal = () => {
  forgotEmail.value = loginId.value.includes('@') ? loginId.value : ''
  showForgotModal.value = true
}

const handleForgotPassword = async () => {
  if (!forgotEmail.value.trim()) {
    toast.warning('Vui lòng nhập email để nhận hướng dẫn đặt lại mật khẩu')
    return
  }

  forgotLoading.value = true
  try {
    await forgotPassword(forgotEmail.value.trim())
    toast.success('Nếu email hợp lệ, hệ thống sẽ gửi hướng dẫn đặt lại mật khẩu')
    showForgotModal.value = false
    forgotEmail.value = ''
  } catch (error) {
    console.error('Forgot password error:', error)
    toast.error(error.response?.data?.message || 'Không thể gửi yêu cầu quên mật khẩu')
  } finally {
    forgotLoading.value = false
  }
}

const copySupport = async (value, label) => {
  try {
    await navigator.clipboard.writeText(value)
    toast.success(`Đã sao chép ${label}`)
  } catch (error) {
    console.error('Copy support info failed:', error)
    toast.warning(`Không thể sao chép ${label}`)
  }
}
</script>

<template>
  <div
    class="min-h-screen w-full flex items-center justify-center bg-[#f8fafc] relative overflow-hidden font-sans px-4 py-8 sm:px-6">
    <!-- Dynamic Background Orbs -->
    <div
      class="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-indigo-200/40 blur-[120px] rounded-full animate-pulse">
    </div>
    <div
      class="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-blue-200/40 blur-[120px] rounded-full animate-pulse"
      style="animation-delay: 2s;"></div>

    <div class="relative z-10 w-full max-w-130 animate-fade-in-up sm:max-w-140">
      <div
        class="bg-white/70 backdrop-blur-xl border border-white/40 shadow-[0_20px_50px_rgba(0,0,0,0.05)] rounded-4xl overflow-hidden p-7 sm:p-8 md:p-10 lg:p-11">

        <div class="text-center mb-9 sm:mb-10">
          <div
            class="inline-flex items-center justify-center w-20 h-20 rounded-[24px] bg-linear-to-tr from-indigo-600 to-blue-500 text-white mb-6 shadow-xl shadow-indigo-200/50 transform hover:scale-105 transition-transform duration-300 sm:w-22 sm:h-22">
            <Fingerprint class="w-10 h-10" />
          </div>
          <h2 class="text-4xl font-black text-slate-900 tracking-tight mb-2 sm:text-[2.8rem]">Đăng nhập</h2>
          <p class="mx-auto max-w-md text-slate-500 font-medium sm:text-lg">Chào mừng trở lại <span
              class="text-indigo-600 font-bold">HRM System</span></p>
        </div>

        <!-- Error Alert -->
        <Transition name="fade-shake">
          <div v-if="errorMessage"
            class="mb-8 p-4 bg-rose-50 border border-rose-100 rounded-2xl text-rose-600 text-sm font-bold flex items-center gap-3">
            <AlertTriangle class="w-5 h-5 shrink-0" />
            {{ errorMessage }}
          </div>
        </Transition>

        <!-- Form -->
        <form @submit.prevent="handleLogin" class="space-y-6 sm:space-y-7">
          <div class="space-y-4 sm:space-y-5">
            <BaseInput v-model="loginId" label="Tài khoản hệ thống" type="text" placeholder="Mã nhân viên / Email"
              :icon="Mail" required class="transition-all duration-300" />

            <div class="w-full space-y-1.5">
              <label class="block text-sm font-semibold text-slate-700 ml-1">
                Mật khẩu
                <span class="text-red-500">*</span>
              </label>

              <div class="relative group">
                <div
                  class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400 group-focus-within:text-indigo-500 transition-colors">
                  <Lock class="w-5 h-5" />
                </div>

                <input v-model="password" :type="showPassword ? 'text' : 'password'" placeholder="••••••••" required
                  class="w-full h-12 pl-11 pr-12 text-slate-700 rounded-xl bg-white/50 backdrop-blur-sm border border-slate-200 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 transition-all outline-none sm:h-13" />

                <button type="button"
                  class="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-400 hover:text-indigo-600 transition-colors"
                  :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiển thị mật khẩu'"
                  @click="showPassword = !showPassword">
                  <EyeOff v-if="showPassword" class="w-5 h-5" />
                  <Eye v-else class="w-5 h-5" />
                </button>
              </div>
            </div>
          </div>

          <div class="mt-2 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <label class="flex items-center space-x-2.5 cursor-pointer group">
              <div class="relative flex items-center justify-center">
                <input type="checkbox" v-model="rememberMe"
                  class="peer appearance-none w-5 h-5 border-2 border-slate-200 rounded-md bg-white checked:bg-indigo-600 checked:border-indigo-600 transition-all cursor-pointer" />
                <Check
                  class="absolute w-3.5 h-3.5 text-white opacity-0 peer-checked:opacity-100 transition-opacity duration-200 pointer-events-none" />
              </div>
              <span class="text-sm font-semibold text-slate-500 group-hover:text-slate-900 transition-colors">Ghi nhớ
                đăng nhập</span>
            </label>
            <button type="button"
              class="text-sm font-bold text-indigo-600 hover:text-indigo-400 transition-colors sm:text-right"
              @click="openForgotModal">
              Quên mật khẩu?
            </button>
          </div>

          <BaseButton type="submit" variant="primary" size="lg"
            class="w-full mt-6 h-14! rounded-2xl! shadow-lg shadow-indigo-100 hover:shadow-indigo-200 transition-all active:scale-[0.98] sm:h-15!"
            :loading="loading" :icon="LogIn" iconRight>
            Xác thực hệ thống
          </BaseButton>

          <div class="text-center mt-10">
            <p class="text-sm font-medium text-slate-400">
              Bạn gặp sự cố kỹ thuật?
              <button type="button" class="ml-1 text-indigo-600 font-bold hover:underline"
                @click="showSupportModal = true">
                Hỗ trợ ngay
              </button>
            </p>
          </div>
        </form>
      </div>

      <!-- Footer Info -->
      <div class="mt-8 text-center text-xs text-slate-400 font-medium">
        &copy; 2026 Digital HRM Platform. All Rights Reserved.
      </div>
    </div>

    <Teleport to="body">
      <Transition name="fade-shake">
        <div v-if="showForgotModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-slate-950/50 backdrop-blur-sm" @click="showForgotModal = false" />
          <div
            class="relative z-10 w-full max-w-md rounded-[28px] border border-white/50 bg-white p-6 shadow-[0_25px_80px_rgba(15,23,42,0.18)]">
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">Password Recovery</p>
                <h3 class="mt-2 text-2xl font-black text-slate-900">Quên mật khẩu</h3>
                <p class="mt-2 text-sm font-medium leading-relaxed text-slate-500">
                  Nhập email công việc hoặc email khôi phục để nhận hướng dẫn đặt lại mật khẩu.
                </p>
              </div>
              <button type="button"
                class="rounded-2xl p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
                @click="showForgotModal = false">
                <X class="h-5 w-5" />
              </button>
            </div>

            <div class="mt-6">
              <BaseInput v-model="forgotEmail" label="Email nhận hỗ trợ" type="email" placeholder="you@company.com"
                :icon="Mail" required />
            </div>

            <div class="mt-6 flex gap-3">
              <BaseButton type="button" variant="outline" class="flex-1" @click="showForgotModal = false">
                Hủy
              </BaseButton>
              <BaseButton type="button" variant="primary" class="flex-1" :loading="forgotLoading"
                @click="handleForgotPassword">
                Gửi yêu cầu
              </BaseButton>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="fade-shake">
        <div v-if="showSupportModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-slate-950/55 backdrop-blur-sm" @click="showSupportModal = false" />
          <div
            class="relative z-10 w-full max-w-lg rounded-[30px] border border-white/50 bg-white p-6 shadow-[0_25px_80px_rgba(15,23,42,0.18)] md:p-7">
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">Support Center</p>
                <h3 class="mt-2 text-2xl font-black text-slate-900">Hỗ trợ đăng nhập</h3>
                <p class="mt-2 text-sm font-medium leading-relaxed text-slate-500">
                  Nếu bạn không vào được hệ thống, hãy thử khôi phục mật khẩu hoặc liên hệ bộ phận hỗ trợ theo thông tin
                  dưới đây.
                </p>
              </div>
              <button type="button"
                class="rounded-2xl p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
                @click="showSupportModal = false">
                <X class="h-5 w-5" />
              </button>
            </div>

            <div class="mt-6 grid gap-4 md:grid-cols-2">
              <button type="button"
                class="rounded-[24px] border border-slate-200 bg-slate-50 p-5 text-left transition hover:border-indigo-200 hover:bg-indigo-50/40"
                @click="copySupport(supportEmail, 'email hỗ trợ')">
                <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-indigo-100 text-indigo-600">
                  <Mail class="h-5 w-5" />
                </div>
                <p class="mt-4 text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Email</p>
                <p class="mt-2 text-base font-black text-slate-900">{{ supportEmail }}</p>
                <p class="mt-2 text-sm font-medium text-slate-500">Bấm để sao chép email hỗ trợ.</p>
              </button>

              <button type="button"
                class="rounded-[24px] border border-slate-200 bg-slate-50 p-5 text-left transition hover:border-indigo-200 hover:bg-indigo-50/40"
                @click="copySupport(supportHotline, 'số hotline')">
                <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-100 text-emerald-600">
                  <Phone class="h-5 w-5" />
                </div>
                <p class="mt-4 text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Hotline</p>
                <p class="mt-2 text-base font-black text-slate-900">{{ supportHotline }}</p>
                <p class="mt-2 text-sm font-medium text-slate-500">Bấm để sao chép số điện thoại hỗ trợ.</p>
              </button>
            </div>

            <div class="mt-5 rounded-[24px] border border-indigo-100 bg-indigo-50/60 p-5">
              <div class="flex items-center gap-3">
                <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-white text-indigo-600 shadow-sm">
                  <LifeBuoy class="h-5 w-5" />
                </div>
                <div>
                  <p class="text-sm font-black text-slate-900">Gợi ý xử lý nhanh</p>
                  <p class="text-sm font-medium text-slate-500">Thử theo thứ tự này để xử lý nhanh hơn.</p>
                </div>
              </div>
              <ul class="mt-4 space-y-2 text-sm font-medium text-slate-600">
                <li>1. Kiểm tra lại mã nhân viên hoặc email đăng nhập.</li>
                <li>2. Dùng chức năng quên mật khẩu nếu bạn còn truy cập email.</li>
                <li>3. Nếu tài khoản bị khóa hoặc không còn email cũ, liên hệ IT/HR để xác minh.</li>
              </ul>
            </div>

            <div class="mt-6 flex gap-3">
              <BaseButton type="button" variant="outline" class="flex-1" @click="showSupportModal = false">
                Đóng
              </BaseButton>
              <BaseButton type="button" variant="primary" class="flex-1"
                @click="showSupportModal = false; openForgotModal()">
                Khôi phục mật khẩu
              </BaseButton>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-shake-enter-active {
  animation: shake 0.4s cubic-bezier(.36, .07, .19, .97) both;
}

@keyframes shake {

  10%,
  90% {
    transform: translate3d(-1px, 0, 0);
  }

  20%,
  80% {
    transform: translate3d(2px, 0, 0);
  }

  30%,
  50%,
  70% {
    transform: translate3d(-4px, 0, 0);
  }

  40%,
  60% {
    transform: translate3d(4px, 0, 0);
  }
}
</style>
