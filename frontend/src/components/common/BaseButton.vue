<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary' // primary, secondary, outline, ghost, danger
  },
  size: {
    type: String,
    default: 'md' // sm, md, lg
  },
  loading: Boolean,
  disabled: Boolean,
  icon: Object,
  iconRight: Boolean
})
</script>

<template>
  <button
    :disabled="disabled || loading"
    class="relative inline-flex items-center justify-center font-medium transition-all duration-300 rounded-xl focus:outline-none focus:ring-4 disabled:opacity-50 disabled:cursor-not-allowed group overflow-hidden"
    :class="[
      variant === 'primary' ? 'bg-indigo-600 text-white hover:bg-indigo-700 shadow-lg shadow-indigo-200 focus:ring-indigo-500/20' : '',
      variant === 'secondary' ? 'bg-slate-900 text-white hover:bg-slate-800 focus:ring-slate-500/20' : '',
      variant === 'outline' ? 'border-2 border-slate-200 text-slate-700 hover:border-indigo-600 hover:text-indigo-600 focus:ring-indigo-500/10' : '',
      variant === 'ghost' ? 'text-slate-600 hover:bg-slate-100 focus:ring-slate-500/10' : '',
      variant === 'danger' ? 'bg-red-500 text-white hover:bg-red-600 shadow-lg shadow-red-200 focus:ring-red-500/20' : '',
      size === 'sm' ? 'px-3 py-1.5 text-sm' : '',
      size === 'md' ? 'px-5 py-2.5 text-base' : '',
      size === 'lg' ? 'px-8 py-3.5 text-lg' : ''
    ]"
  >
    <!-- Background Shine Effect -->
    <div class="absolute inset-0 w-full h-full bg-white/20 -translate-x-full group-hover:translate-x-full transition-transform duration-700 pointer-events-none"></div>

    <!-- Loading Spinner -->
    <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-current" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>

    <component :is="icon" v-if="icon && !iconRight" class="w-5 h-5 mr-2" />
    <slot />
    <component :is="icon" v-if="icon && iconRight" class="w-5 h-5 ml-2" />
  </button>
</template>
