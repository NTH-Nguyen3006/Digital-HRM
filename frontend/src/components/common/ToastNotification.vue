<script setup>
import { computed } from 'vue'
import { useUiStore } from '@/stores/ui'
import { CheckCircle2, XCircle, AlertTriangle, Info, X } from 'lucide-vue-next'

const ui = useUiStore()

const iconMap = {
  success: CheckCircle2,
  error: XCircle,
  warning: AlertTriangle,
  info: Info,
}

const colorMap = {
  success: 'bg-emerald-50 border-emerald-200 text-emerald-800',
  error:   'bg-rose-50 border-rose-200 text-rose-800',
  warning: 'bg-amber-50 border-amber-200 text-amber-800',
  info:    'bg-indigo-50 border-indigo-200 text-indigo-800',
}

const iconColorMap = {
  success: 'text-emerald-500',
  error:   'text-rose-500',
  warning: 'text-amber-500',
  info:    'text-indigo-500',
}
</script>

<template>
  <Teleport to="body">
    <div class="fixed top-6 right-6 z-[9999] flex flex-col gap-3 pointer-events-none">
      <TransitionGroup name="toast" tag="div" class="flex flex-col gap-3">
        <div
          v-for="toast in ui.toasts"
          :key="toast.id"
          class="pointer-events-auto flex items-start gap-3 px-5 py-4 rounded-2xl border shadow-xl shadow-black/5 min-w-[320px] max-w-[400px] backdrop-blur-sm"
          :class="colorMap[toast.type]"
        >
          <component
            :is="iconMap[toast.type]"
            class="w-5 h-5 shrink-0 mt-0.5"
            :class="iconColorMap[toast.type]"
          />
          <p class="text-sm font-semibold flex-1 leading-relaxed">{{ toast.message }}</p>
          <button
            @click="ui.removeToast(toast.id)"
            class="shrink-0 opacity-40 hover:opacity-100 transition-opacity"
          >
            <X class="w-4 h-4" />
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active {
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}
.toast-leave-active {
  transition: all 0.25s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(30px) scale(0.95);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(30px) scale(0.9);
}
</style>
