export function formatDate(value, fallback = '—') {
  if (!value) return fallback

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return fallback

  return date.toLocaleDateString('vi-VN')
}

export function formatDateTime(value, fallback = '—') {
  if (!value) return fallback

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return fallback

  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function formatMonthYear(year, month, fallback = '—') {
  if (!year || !month) return fallback
  return `T${String(month).padStart(2, '0')}/${year}`
}

export function formatCurrency(value, currency = 'VND', fallback = '—') {
  if (value === null || value === undefined || value === '') return fallback

  const amount = Number(value)
  if (Number.isNaN(amount)) return fallback

  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(amount)
}

export function formatNumber(value, fallback = '0') {
  if (value === null || value === undefined || value === '') return fallback

  const amount = Number(value)
  if (Number.isNaN(amount)) return fallback

  return new Intl.NumberFormat('vi-VN').format(amount)
}

export function formatTime(value, fallback = '—') {
  if (!value) return fallback

  if (typeof value === 'string' && value.length <= 5 && value.includes(':')) {
    return value
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return fallback

  return date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function getInitials(name, fallback = '?') {
  if (!name) return fallback

  const initials = name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('')

  return initials || fallback
}

export function humanizeStatus(status, fallback = 'Không xác định') {
  if (!status) return fallback

  const map = {
    DRAFT: 'Nháp',
    ACTIVE: 'Đang hiệu lực',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Từ chối',
    PENDING: 'Chờ xử lý',
    COMPLETED: 'Hoàn tất',
    REQUESTED: 'Đang chờ',
    MANAGER_APPROVED: 'Quản lý đã duyệt',
    MANAGER_REJECTED: 'Quản lý từ chối',
    HR_FINALIZED: 'HR đã chốt',
    CLOSED: 'Đã đóng',
    EXPIRED: 'Hết hạn',
    TERMINATED: 'Chấm dứt',
    READY_FOR_JOIN: 'Sẵn sàng tiếp nhận',
    IN_PROGRESS: 'Đang xử lý',
    ACCESS_REVOKED: 'Đã thu hồi truy cập',
    SETTLEMENT_PREPARED: 'Đã chuẩn bị quyết toán',
    PUBLISHED: 'Đã phát hành',
  }

  return map[status] || status.replaceAll('_', ' ')
}
