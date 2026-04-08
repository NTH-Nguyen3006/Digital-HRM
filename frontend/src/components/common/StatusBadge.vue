<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, required: true },
  label: { type: String, default: '' },
  showDot: { type: Boolean, default: true }
})

// Các map màu dựa trên chuẩn chung
const statusStyles = {
  // Trạng thái chung / Đang chờ
  PENDING: { bg: 'bg-amber-100', text: 'text-amber-700', border: 'border-amber-200', dot: 'bg-amber-400', defaultLabel: 'Chờ xử lý' },
  IN_PROGRESS: { bg: 'bg-blue-100', text: 'text-blue-700', border: 'border-blue-200', dot: 'bg-blue-400', defaultLabel: 'Đang xử lý' },
  REQUESTED: { bg: 'bg-amber-100', text: 'text-amber-700', border: 'border-amber-200', dot: 'bg-amber-400', defaultLabel: 'Chờ duyệt' },
  
  // Trạng thái thành công / Hoàn tất
  ACTIVE: { bg: 'bg-emerald-100', text: 'text-emerald-700', border: 'border-emerald-200', dot: 'bg-emerald-500', defaultLabel: 'Hoạt động' },
  APPROVED: { bg: 'bg-emerald-100', text: 'text-emerald-700', border: 'border-emerald-200', dot: 'bg-emerald-500', defaultLabel: 'Đã duyệt' },
  COMPLETED: { bg: 'bg-emerald-100', text: 'text-emerald-700', border: 'border-emerald-200', dot: 'bg-emerald-500', defaultLabel: 'Hoàn tất' },
  READY_FOR_JOIN: { bg: 'bg-indigo-100', text: 'text-indigo-700', border: 'border-indigo-200', dot: 'bg-indigo-500', defaultLabel: 'Sẵn sàng' },
  MANAGER_APPROVED: { bg: 'bg-indigo-100', text: 'text-indigo-700', border: 'border-indigo-200', dot: 'bg-indigo-500', defaultLabel: 'QL đã duyệt' },
  HR_FINALIZED: { bg: 'bg-violet-100', text: 'text-violet-700', border: 'border-violet-200', dot: 'bg-violet-500', defaultLabel: 'HR đã chốt' },

  // Trạng thái cảnh báo / Lỗi / Hủy
  REJECTED: { bg: 'bg-rose-100', text: 'text-rose-700', border: 'border-rose-200', dot: 'bg-rose-500', defaultLabel: 'Từ chối' },
  MANAGER_REJECTED: { bg: 'bg-rose-100', text: 'text-rose-700', border: 'border-rose-200', dot: 'bg-rose-500', defaultLabel: 'QL từ chối' },
  CANCELLED: { bg: 'bg-rose-50', text: 'text-rose-600', border: 'border-rose-200', dot: 'bg-rose-300', defaultLabel: 'Đã hủy' },
  EXPIRING_SOON: { bg: 'bg-orange-100', text: 'text-orange-700', border: 'border-orange-200', dot: 'bg-orange-500', defaultLabel: 'Sắp hết hạn' },
  EXPIRED: { bg: 'bg-red-100', text: 'text-red-700', border: 'border-red-200', dot: 'bg-red-500', defaultLabel: 'Đã hết hạn' },
  TERMINATED: { bg: 'bg-slate-100', text: 'text-slate-600', border: 'border-slate-200', dot: 'bg-slate-400', defaultLabel: 'Chấm dứt' },

  // Các trạng thái xám / Draft
  DRAFT: { bg: 'bg-slate-100', text: 'text-slate-700', border: 'border-slate-200', dot: 'bg-slate-400', defaultLabel: 'Nháp' },
  CLOSED: { bg: 'bg-slate-100', text: 'text-slate-600', border: 'border-slate-200', dot: 'bg-slate-400', defaultLabel: 'Đã đóng' },
  OPEN: { bg: 'bg-slate-50', text: 'text-slate-500', border: 'border-slate-200', dot: 'bg-slate-300', defaultLabel: 'Mở' },
}

const currentStyle = computed(() => {
  const s = props.status?.toUpperCase() || ''
  return statusStyles[s] || { bg: 'bg-slate-100', text: 'text-slate-600', border: 'border-slate-200', dot: 'bg-slate-400', defaultLabel: s }
})

const displayLabel = computed(() => props.label || currentStyle.value.defaultLabel)
</script>

<template>
  <span 
    class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold border"
    :class="[currentStyle.bg, currentStyle.text, currentStyle.border]"
  >
    <span v-if="showDot" class="w-1.5 h-1.5 rounded-full" :class="currentStyle.dot"></span>
    {{ displayLabel }}
  </span>
</template>
