import { ref, computed } from 'vue'

/**
 * Pagination composable
 * @param {Object} options
 * @param {number} options.initialSize - Items per page (default: 12)
 * @returns {Object} - pagination state & helpers
 */
export function usePagination({ initialSize = 12 } = {}) {
  const currentPage = ref(0)       // 0-indexed (matches Spring Pageable)
  const pageSize = ref(initialSize)
  const totalElements = ref(0)
  const totalPages = ref(0)

  const isFirstPage = computed(() => currentPage.value === 0)
  const isLastPage = computed(() => currentPage.value >= totalPages.value - 1)

  function setPage(page) {
    currentPage.value = page
  }

  function nextPage() {
    if (!isLastPage.value) currentPage.value++
  }

  function prevPage() {
    if (!isFirstPage.value) currentPage.value--
  }

  function resetPage() {
    currentPage.value = 0
  }

  /**
   * Update pagination metadata from an API PageResponse
   * @param {{ totalElements: number, totalPages: number }} meta
   */
  function setMeta(meta) {
    totalElements.value = meta.totalElements ?? 0
    totalPages.value = meta.totalPages ?? 0
  }

  /** Visible page numbers for pagination UI (max 5 visible) */
  const visiblePages = computed(() => {
    const total = totalPages.value
    if (total <= 7) return Array.from({ length: total }, (_, i) => i)

    const cur = currentPage.value
    if (cur <= 3) return [0, 1, 2, 3, 4, '...', total - 1]
    if (cur >= total - 4) return [0, '...', total - 5, total - 4, total - 3, total - 2, total - 1]
    return [0, '...', cur - 1, cur, cur + 1, '...', total - 1]
  })

  return {
    currentPage,
    pageSize,
    totalElements,
    totalPages,
    isFirstPage,
    isLastPage,
    visiblePages,
    setPage,
    nextPage,
    prevPage,
    resetPage,
    setMeta,
  }
}
