<script setup>
defineProps({
  modelValue: [String, Number],
  label: String,
  type: {
    type: String,
    default: 'text'
  },
  placeholder: String,
  error: String,
  disabled: Boolean,
  required: Boolean,
  icon: Object
})

defineEmits(['update:modelValue'])
</script>

<template>
  <div class="w-full space-y-1.5">
    <label v-if="label" class="block text-sm font-semibold text-slate-700 ml-1">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    
    <div class="relative group">
      <div v-if="icon" class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400 group-focus-within:text-indigo-500 transition-colors">
        <component :is="icon" class="w-5 h-5" />
      </div>
      
      <input
        :type="type"
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        :placeholder="placeholder"
        :disabled="disabled"
        class="w-full h-11 px-4 text-slate-700 rounded-xl bg-white/50 backdrop-blur-sm border border-slate-200 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 transition-all outline-none"
        :class="[
          icon ? 'pl-11' : '',
          error ? 'border-red-400 focus:border-red-500 focus:ring-red-500/10' : ''
        ]"
      />
    </div>
    
    <p v-if="error" class="text-xs font-medium text-red-500 ml-1 mt-1 transition-all">
      {{ error }}
    </p>
  </div>
</template>
