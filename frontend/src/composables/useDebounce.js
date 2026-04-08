import { ref, watch } from 'vue'

/**
 * Debounce a reactive value
 * @param {import('vue').Ref} value - Reactive value to debounce
 * @param {number} delay - Delay in ms (default: 400)
 * @returns {{ debouncedValue: import('vue').Ref }}
 */
export function useDebounce(value, delay = 400) {
  const debouncedValue = ref(value.value)
  let timeout = null

  watch(value, (newVal) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      debouncedValue.value = newVal
    }, delay)
  })

  return { debouncedValue }
}
