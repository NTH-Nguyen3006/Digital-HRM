<script setup>
import { ref, onErrorCaptured } from 'vue'

const error = ref(null)

onErrorCaptured((err, instance, info) => {
  console.error('Captured error:', err)
  console.error('Component instance:', instance)
  console.error('Error info:', info)
  error.value = err
  // Trả về false để ngăn lỗi tiếp tục lan lên trên
  return false
})

const resetError = () => {
  error.value = null
  window.location.reload()
}
</script>

<template>
  <div v-if="error" class="min-h-screen flex items-center justify-center bg-slate-50 p-6">
    <div class="max-w-md w-full bg-white rounded-3xl shadow-xl p-10 text-center border border-slate-100">
      <div class="w-20 h-20 bg-rose-100 text-rose-600 rounded-2xl flex items-center justify-center mx-auto mb-6">
        <svg class="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <h2 class="text-2xl font-bold text-slate-800 mb-2">Đã có lỗi xảy ra</h2>
      <p class="text-slate-500 mb-8 font-medium">Hệ thống gặp sự cố không mong muốn. Vui lòng thử tải lại trang.</p>
      
      <div v-if="error.message" class="text-xs bg-slate-50 p-4 rounded-xl text-left mb-8 overflow-auto max-h-32 font-mono text-slate-400">
        {{ error.message }}
      </div>

      <button @click="resetError" class="w-full bg-indigo-600 text-white font-bold py-4 rounded-2xl hover:bg-indigo-700 transition shadow-lg shadow-indigo-200">
        Tải lại trang ngay
      </button>
    </div>
  </div>
  <slot v-else />
</template>
