<script setup>
import { useRouter } from 'vue-router'
import AppHeader from '@/components/partials/AppHeader.vue'
import AppFooter from '@/components/partials/AppFooter.vue'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-slate-50 flex flex-col relative overflow-hidden font-sans">
    <div class="absolute top-0 left-0 w-full h-full pointer-events-none z-0">
      <div
        class="absolute -top-40 -left-20 w-[500px] h-[500px] bg-indigo-200/50 rounded-full blur-[120px] animate-pulse">
      </div>
      <div
        class="absolute top-1/2 -right-20 w-[600px] h-[600px] bg-emerald-100/60 rounded-full blur-[150px] animate-float">
      </div>
      <div
        class="absolute -bottom-40 left-1/3 w-[400px] h-[400px] bg-sky-200/40 rounded-full blur-[100px] animate-pulse"
        style="animation-delay: -2s"></div>
    </div>

    <AppHeader variant="portal" />

    <main class="flex-1 flex items-center justify-center pt-24 p-4 md:p-8 relative z-10 w-full">
      <div
        class="w-full max-w-[1200px] min-h-[600px] flex flex-col lg:flex-row bg-white/40 backdrop-blur-2xl rounded-[3rem] border border-white/60 shadow-2xl overflow-hidden">

        <div
          class="hidden lg:flex flex-1 flex-col justify-between p-16 relative bg-gradient-to-br from-indigo-50/50 to-white/50 border-r border-white/40">
          <div class="absolute inset-0 opacity-10"
            style="background-image: radial-gradient(#4f46e5 1px, transparent 1px); background-size: 32px 32px;"></div>

          <div class="relative z-10 animate-fade-in-up">
            <div class="flex items-center space-x-3 mb-12">
              <div
                class="w-12 h-12 bg-indigo-600 rounded-2xl flex items-center justify-center shadow-lg shadow-indigo-200">
                <svg class="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
            </div>

            <h1 class="text-4xl font-bold text-slate-800 leading-[1.2] tracking-tight mb-4">
              Không gian làm việc <br />
              <span class="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-sky-500">hợp nhất.</span>
            </h1>
            <p class="text-slate-500 text-base font-medium leading-relaxed max-w-md">
              Đăng nhập để kết nối với đội ngũ, quản lý quy trình hành chính và nâng cao năng suất tổ chức từ một cổng
              duy
              nhất.
            </p>
          </div>

          <!-- Custom Graphic / Testimonial visual -->
          <!-- <div class="relative z-10 mt-12 animate-scale-in" style="animation-delay: 0.2s">
            <div class="p-6 rounded-3xl bg-white/60 backdrop-blur-md border border-white shadow-xl max-w-sm relative">
              <div
                class="absolute -top-4 -right-4 w-12 h-12 bg-emerald-50 text-emerald-500 rounded-2xl flex items-center justify-center shadow-sm border border-emerald-100 rotate-12">
                <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <div class="flex items-center space-x-4 mb-4">
                <div
                  class="w-12 h-12 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-bold">
                  HR</div>
                <div>
                  <p class="text-sm font-bold text-slate-900">Bảo mật cấp độ doanh nghiệp</p>
                  <p class="text-xs font-medium text-slate-500">Xác thực 2 lớp (2FA) được kích hoạt</p>
                </div>
              </div>
              <div class="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden">
                <div class="w-full h-full bg-gradient-to-r from-indigo-500 to-emerald-400"></div>
              </div>
            </div>
          </div> -->


        </div>

        <!-- Right side: Form container -->
        <div class="flex-1 flex flex-col items-center justify-center p-8 lg:p-24 relative bg-white/20">
          <slot />
        </div>
      </div>
    </main>

    <AppFooter variant="portal" />
  </div>
</template>

<style scoped>
@reference "../assets/css/main.css";

.animate-fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}


.animate-scale-in {
  animation: scaleIn 1s cubic-bezier(0.16, 1, 0.3, 1) forwards;
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

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
