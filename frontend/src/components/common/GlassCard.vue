<script setup>
defineProps({
  padding: {
    type: String,
    default: 'p-6'
  },
  dark: Boolean,
  hover: Boolean,
  glass: {
    type: Boolean,
    default: true
  }
})
</script>

<template>
  <div
    class="rounded-3xl transition-all duration-300"
    :class="[
      padding,
      glass ? (dark ? 'bg-slate-900/40 backdrop-blur-xl border border-white/10 shadow-2xl text-white' : 'bg-white/70 backdrop-blur-md border border-white/20 shadow-xl') : 'bg-white border border-slate-100 shadow-sm',
      hover ? 'hover:shadow-2xl hover:-translate-y-1' : ''
    ]"
  >
    <slot />
  </div>
</template>
