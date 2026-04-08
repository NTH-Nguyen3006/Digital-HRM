<script setup>
/**
 * PageHeader — Standardized page title + subtitle + action slot
 *
 * Props:
 *   title (string)       — Main page heading
 *   subtitle (string)    — Optional subtitle / description
 *   icon (Component)     — Optional lucide icon displayed in colored pill
 *   iconColor (string)   — Tailwind bg class for icon container (default: 'bg-indigo-600')
 *   iconShadow (string)  — Tailwind shadow class (default: 'shadow-indigo-100')
 * Slots:
 *   #actions             — Right-side CTA buttons
 *   #badges              — Small info badges below title (optional)
 */
defineProps({
  title:       { type: String, required: true },
  subtitle:    { type: String, default: '' },
  icon:        { type: [Object, Function], default: null },
  iconColor:   { type: String, default: 'bg-indigo-600' },
  iconShadow:  { type: String, default: 'shadow-indigo-100' },
})
</script>

<template>
  <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">

    <!-- Left: Icon + Title -->
    <div>
      <div class="flex items-center gap-4 mb-1">
        <div
          v-if="icon"
          class="w-12 h-12 rounded-[18px] flex items-center justify-center shadow-xl"
          :class="[iconColor, iconShadow]"
        >
          <component :is="icon" class="w-6 h-6 text-white" />
        </div>
        <h2 class="text-4xl font-black text-slate-900 tracking-tight">{{ title }}</h2>
      </div>

      <p v-if="subtitle" class="text-slate-500 font-medium mt-1" :class="icon ? 'ml-1' : ''">
        {{ subtitle }}
      </p>

      <div v-if="$slots.badges" class="mt-3" :class="icon ? 'ml-1' : ''">
        <slot name="badges" />
      </div>
    </div>

    <!-- Right: Action buttons -->
    <div v-if="$slots.actions" class="flex items-center gap-3 shrink-0">
      <slot name="actions" />
    </div>
  </div>
</template>
