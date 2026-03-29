<script setup>
defineProps({
  variant: {
    type: String,
    default: 'portal' // 'portal' | 'auth'
  }
})
</script>

<template>
  <footer v-if="variant === 'portal'" class="bg-slate-50 border-t border-slate-200 py-20 px-6">
    <div class="max-w-7xl mx-auto grid md:grid-cols-4 gap-12">
      <div class="space-y-6">
        <div class="flex items-center space-x-3">
          <div class="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center text-white">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <span class="text-lg font-bold text-slate-900">Digital HRM</span>
        </div>
        <p class="text-slate-500 text-sm font-medium leading-relaxed">
          Hệ thống quản trị nguồn nhân lực hiện đại, tập trung vào trải nghiệm số và tối ưu năng suất lao động.
        </p>
      </div>

      <div>
        <h4 class="text-slate-900 font-bold mb-6">Sản phẩm</h4>
        <ul class="space-y-4 text-sm font-medium text-slate-500">
          <li class="hover:text-indigo-600 cursor-pointer transition-colors">Tuyển dụng</li>
          <li class="hover:text-indigo-600 cursor-pointer transition-colors">Đào tạo & Phát triển</li>
          <li class="hover:text-indigo-600 cursor-pointer transition-colors">Tính lương (Payroll)</li>
          <li class="hover:text-indigo-600 cursor-pointer transition-colors">Đánh giá KPI</li>
        </ul>
      </div>

      <div>
        <h4 class="text-slate-900 font-bold mb-6">Công ty</h4>
        <ul class="space-y-4 text-sm font-medium text-slate-500">
          <li class="hover:text-indigo-600 cursor-pointer transition-colors">Về chúng tôi</li>
          <li class="hover:text-indigo-600 cursor-pointer transition-colors">Tuyển dụng</li>
          <li class="hover:text-indigo-600 cursor-pointer transition-colors">Chính sách bảo mật</li>
          <li class="hover:text-indigo-600 cursor-pointer transition-colors">Điều khoản dịch vụ</li>
        </ul>
      </div>

      <div>
        <h4 class="text-slate-900 font-bold mb-6">Liên hệ</h4>
        <p class="text-sm font-medium text-slate-500 mb-4">Email: support@company.com</p>
        <p class="text-sm font-medium text-slate-500">Hotline: 1900 123 456</p>
        <div class="mt-8 flex space-x-4">
          <div
            class="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center text-indigo-600 hover:bg-indigo-600 hover:text-white transition-all cursor-pointer">
            f
          </div>
          <div
            class="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center text-indigo-600 hover:bg-indigo-600 hover:text-white transition-all cursor-pointer">
            in
          </div>
        </div>
      </div>
    </div>

    <div
      class="max-w-7xl mx-auto mt-20 pt-8 border-t border-slate-200 text-center text-slate-400 text-xs font-bold tracking-widest uppercase">
      &copy; 2026 DIGITAL HRM SYSTEM. CỔNG THÔNG TIN.
    </div>
  </footer>

  <footer v-else-if="variant === 'auth'"
    class="relative z-10 bg-white/40 backdrop-blur-md border-t border-white/60 py-6 text-center mt-auto">
    <div class="max-w-7xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-4">
      <div class="flex items-center space-x-2">
        <svg class="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
        <span class="font-bold text-slate-800">Digital HRM</span>
      </div>
      <div class="text-xs font-bold text-slate-500 tracking-widest uppercase">
        &copy; 2026 DIGITAL HRM SYSTEM. BẢO MẬT & ĐÁNG TIN CẬY.
      </div>
      <div class="flex space-x-6">
        <a href="#" class="text-sm font-semibold text-slate-500 hover:text-indigo-600 transition-colors">Hỗ trợ</a>
        <a href="#" class="text-sm font-semibold text-slate-500 hover:text-indigo-600 transition-colors">Bảo mật</a>
      </div>
    </div>
  </footer>
</template>
