<script setup>
import * as PhosphorIcons from 'lucide-vue-next'
import { computed } from 'vue'

const props = defineProps({
  iconName: { type: String, default: 'FileSearch' },
  title: { type: String, default: 'Không có dữ liệu' },
  description: { type: String, default: 'Hiện tại chưa có báo cáo hoặc dữ liệu nào để hiển thị.' },
  actionText: { type: String, default: '' }
})

const emit = defineEmits(['action'])

const iconComponent = computed(() => {
  return PhosphorIcons[props.iconName] || PhosphorIcons['Box']
})
</script>

<template>
  <div class="py-16 px-4 text-center flex flex-col items-center justify-center bg-white rounded-3xl border border-slate-100">
    <div class="w-20 h-20 mb-4 rounded-3xl bg-slate-50 flex items-center justify-center text-slate-300">
      <component :is="iconComponent" class="w-10 h-10" stroke-width="1.5" />
    </div>
    <h3 class="text-lg font-bold text-slate-800 tracking-tight">{{ title }}</h3>
    <p class="text-slate-500 mt-2 max-w-sm mx-auto text-sm leading-relaxed">{{ description }}</p>
    
    <button 
      v-if="actionText"
      @click="emit('action')"
      class="mt-6 px-6 py-2.5 bg-indigo-50 text-indigo-700 font-semibold rounded-xl hover:bg-indigo-100 hover:text-indigo-800 transition-colors text-sm"
    >
      {{ actionText }}
    </button>
  </div>
</template>
