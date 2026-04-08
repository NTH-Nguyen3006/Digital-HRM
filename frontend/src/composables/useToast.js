import { useUiStore } from '@/stores/ui'

/**
 * Toast notification composable
 * Wraps the ui store's toast actions for easy use in components
 */
export function useToast() {
  const ui = useUiStore()

  return {
    success: (message, duration) => ui.addToast({ type: 'success', message, duration }),
    error: (message, duration) => ui.addToast({ type: 'error', message, duration }),
    warning: (message, duration) => ui.addToast({ type: 'warning', message, duration }),
    info: (message, duration) => ui.addToast({ type: 'info', message, duration }),
  }
}
