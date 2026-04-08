<script setup>
import { useUiStore } from '@/stores/ui'
import { AlertTriangle, Trash2 } from 'lucide-vue-next'

const ui = useUiStore()
</script>

<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div
        v-if="ui.confirmDialog.visible"
        class="fixed inset-0 z-[9998] flex items-center justify-center p-4"
      >
        <!-- Backdrop -->
        <div
          class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm"
          @click="ui.resolveConfirm(false)"
        />

        <!-- Dialog -->
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-md overflow-hidden animate-scale-in">

          <!-- Icon Header -->
          <div
            class="p-8 pb-0 flex flex-col items-center text-center"
          >
            <div
              class="w-16 h-16 rounded-2xl flex items-center justify-center mb-5"
              :class="ui.confirmDialog.danger ? 'bg-rose-100' : 'bg-indigo-100'"
            >
              <component
                :is="ui.confirmDialog.danger ? Trash2 : AlertTriangle"
                class="w-7 h-7"
                :class="ui.confirmDialog.danger ? 'text-rose-600' : 'text-indigo-600'"
              />
            </div>
            <h3 class="text-xl font-black text-slate-900 mb-2">{{ ui.confirmDialog.title }}</h3>
            <p class="text-sm font-medium text-slate-500 leading-relaxed">
              {{ ui.confirmDialog.message }}
            </p>
          </div>

          <!-- Actions -->
          <div class="p-6 flex gap-3 mt-4">
            <button
              @click="ui.resolveConfirm(false)"
              class="flex-1 py-3 rounded-2xl border border-slate-200 font-bold text-sm text-slate-600 hover:bg-slate-50 transition-all"
            >
              {{ ui.confirmDialog.cancelLabel }}
            </button>
            <button
              @click="ui.resolveConfirm(true)"
              class="flex-1 py-3 rounded-2xl font-bold text-sm text-white transition-all shadow-lg"
              :class="ui.confirmDialog.danger
                ? 'bg-rose-600 hover:bg-rose-700 shadow-rose-200'
                : 'bg-indigo-600 hover:bg-indigo-700 shadow-indigo-200'"
            >
              {{ ui.confirmDialog.confirmLabel }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.dialog-enter-active, .dialog-leave-active {
  transition: opacity 0.25s ease;
}
.dialog-enter-from, .dialog-leave-to {
  opacity: 0;
}
.animate-scale-in {
  animation: scaleIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.92) translateY(10px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}
</style>
