<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  image: { type: String, default: null },
  size: { type: String, default: 'md' }, // sm, md, lg, xl
  shape: { type: String, default: 'rounded-xl' } // rounded-full, rounded-xl, etc.
})

const sizeClasses = {
  sm: 'w-8 h-8 text-xs',
  md: 'w-11 h-11 text-sm',
  lg: 'w-14 h-14 text-base',
  xl: 'w-16 h-16 text-lg'
}

const colors = [
  'bg-emerald-500',
  'bg-indigo-500',
  'bg-rose-500',
  'bg-amber-500',
  'bg-sky-500',
  'bg-violet-500',
  'bg-fuchsia-500',
  'bg-orange-500'
]

const initials = computed(() => {
  if (!props.name) return '?'
  const words = props.name.trim().split(' ')
  if (words.length === 1) return words[0].substring(0, 2).toUpperCase()
  return (words[0][0] + words[words.length - 1][0]).toUpperCase()
})

const colorClass = computed(() => {
  if (!props.name) return colors[0]
  let sum = 0
  for (let i = 0; i < props.name.length; i++) {
    sum += props.name.charCodeAt(i)
  }
  return colors[sum % colors.length]
})
</script>

<template>
  <div 
    class="relative flex items-center justify-center font-bold text-white shadow-sm shrink-0 overflow-hidden"
    :class="[sizeClasses[size], shape, colorClass]"
  >
    <img v-if="image" :src="image" :alt="name" class="w-full h-full object-cover" />
    <span v-else>{{ initials }}</span>
  </div>
</template>
