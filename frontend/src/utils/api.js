export function unwrapData(response) {
  return response?.data ?? response ?? null
}

export function unwrapPage(response) {
  const data = unwrapData(response)

  if (Array.isArray(data)) {
    return {
      items: data,
      totalElements: data.length,
      totalPages: data.length ? 1 : 0,
      page: 0,
      size: data.length,
    }
  }

  const items =
    data?.items ??
    data?.content ??
    data?.rows ??
    data?.records ??
    []

  return {
    items: Array.isArray(items) ? items : [],
    totalElements: Number(
      data?.totalElements ??
      data?.totalItems ??
      data?.count ??
      (Array.isArray(items) ? items.length : 0),
    ),
    totalPages: Number(
      data?.totalPages ??
      (Array.isArray(items) && items.length ? 1 : 0),
    ),
    page: Number(data?.page ?? data?.pageNumber ?? 0),
    size: Number(data?.size ?? data?.pageSize ?? (Array.isArray(items) ? items.length : 0)),
    raw: data,
  }
}

export function safeArray(value) {
  return Array.isArray(value) ? value : []
}

export function toLookup(items, key = 'value') {
  return safeArray(items).reduce((lookup, item) => {
    const lookupKey = item?.[key]
    if (lookupKey !== undefined && lookupKey !== null) {
      lookup[lookupKey] = item
    }
    return lookup
  }, {})
}

export function downloadBlob(blob, filename) {
  if (!blob) return

  const url = window.URL.createObjectURL(new Blob([blob]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
