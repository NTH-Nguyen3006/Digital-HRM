<script setup>
import GlassCard from './GlassCard.vue'

defineProps({
  title: String,
  value: [String, Number],
  trend: Number, // positive or negative
  trendLabel: String,
  icon: Object,
  color: {
    type: String,
    default: 'indigo'
  }
})
</script>

<template>
  <GlassCard padding="p-5" hover class="relative overflow-hidden group">
    <div class="flex items-center justify-between">
      <div class="space-y-1">
        <p class="text-sm font-medium text-slate-500 uppercase tracking-wider">{{ title }}</p>
        <h3 class="text-3xl font-bold text-slate-900 group-hover:text-indigo-600 transition-colors">{{ value }}</h3>
      </div>
      
      <div 
        class="w-12 h-12 rounded-2xl flex items-center justify-center transition-transform duration-500 group-hover:scale-110 group-hover:rotate-6"
        :class="[
          color === 'indigo' ? 'bg-indigo-50 text-indigo-600' : '',
          color === 'emerald' ? 'bg-emerald-50 text-emerald-600' : '',
          color === 'sky' ? 'bg-sky-50 text-sky-600' : '',
          color === 'rose' ? 'bg-rose-50 text-rose-600' : ''
        ]"
      >
        <component :is="icon" class="w-6 h-6" />
      </div>
    </div>
    
    <div class="mt-4 flex items-center text-sm">
      <div 
        v-if="trend"
        class="flex items-center font-bold mr-2"
        :class="trend > 0 ? 'text-emerald-500' : 'text-rose-500'"
      >
        <span v-if="trend > 0">↑</span>
        <span v-else>↓</span>
        {{ Math.abs(trend) }}%
      </div>
      <span class="text-slate-400 font-medium">{{ trendLabel }}</span>
    </div>

    <!-- Decorative background element -->
    <div class="absolute -right-4 -bottom-4 w-24 h-24 bg-indigo-50/50 rounded-full blur-3xl group-hover:bg-indigo-100 group-hover:scale-125 transition-all duration-700"></div>
  </GlassCard>
</template>
