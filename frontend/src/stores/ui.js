import { defineStore } from 'pinia'
import { ref } from 'vue'

let toastIdCounter = 0

export const useUiStore = defineStore('ui', () => {
  // ─── Toast ───────────────────────────────────────────────────
  const toasts = ref([])
  const routeLoading = ref(false)

  /**
   * @param {{ type: 'success'|'error'|'warning'|'info', message: string, duration?: number }} toast
   */
  function addToast({ type = 'info', message, duration = 3500 }) {
    const id = ++toastIdCounter
    toasts.value.push({ id, type, message })
    setTimeout(() => removeToast(id), duration)
  }

  function removeToast(id) {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx !== -1) toasts.value.splice(idx, 1)
  }

  // ─── Confirm Dialog ──────────────────────────────────────────
  const confirmDialog = ref({
    visible: false,
    title: '',
    message: '',
    confirmLabel: 'Xác nhận',
    cancelLabel: 'Hủy',
    danger: false,
    resolve: null,
  })

  /**
   * Show a confirm dialog. Returns a Promise<boolean>.
   * @param {{ title: string, message: string, confirmLabel?: string, danger?: boolean }} opts
   */
  function confirm(opts) {
    return new Promise((resolve) => {
      confirmDialog.value = {
        visible: true,
        title: opts.title || 'Xác nhận',
        message: opts.message || '',
        confirmLabel: opts.confirmLabel || 'Xác nhận',
        cancelLabel: opts.cancelLabel || 'Hủy',
        danger: opts.danger ?? false,
        resolve,
      }
    })
  }

  function resolveConfirm(result) {
    confirmDialog.value.resolve?.(result)
    confirmDialog.value.visible = false
  }

  function setRouteLoading(value) {
    routeLoading.value = Boolean(value)
  }

  return {
    // toast
    toasts,
    addToast,
    removeToast,
    routeLoading,
    setRouteLoading,
    // confirm
    confirmDialog,
    confirm,
    resolveConfirm,
  }
})
