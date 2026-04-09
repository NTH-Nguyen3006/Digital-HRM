<script setup>
defineProps({
  title: { type: String, required: true },
  value: { type: [String, Number], required: true },
  subtitle: { type: String, default: '' },
  icon: { type: [Object, Function], default: null },
  tone: { type: String, default: 'indigo' },
})

const toneClassMap = {
  indigo: 'bg-indigo-50 text-indigo-700 border-indigo-100',
  emerald: 'bg-emerald-50 text-emerald-700 border-emerald-100',
  amber: 'bg-amber-50 text-amber-700 border-amber-100',
  rose: 'bg-rose-50 text-rose-700 border-rose-100',
  sky: 'bg-sky-50 text-sky-700 border-sky-100',
  slate: 'bg-slate-100 text-slate-700 border-slate-200',
}
</script>

<template>
  <div class="rounded-[28px] border bg-white p-5 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-lg">
    <div class="flex items-start justify-between gap-4">
      <div>
        <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">{{ title }}</p>
        <p class="mt-3 text-3xl font-black tracking-tight text-slate-900">{{ value }}</p>
        <p v-if="subtitle" class="mt-2 text-sm font-medium text-slate-500">{{ subtitle }}</p>
      </div>

      <div
        v-if="icon"
        class="flex h-12 w-12 items-center justify-center rounded-2xl border"
        :class="toneClassMap[tone] || toneClassMap.indigo"
      >
        <component :is="icon" class="h-5 w-5" />
      </div>
    </div>
  </div>
</template>
