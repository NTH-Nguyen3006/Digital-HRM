<script setup>
import { computed, onMounted, ref } from 'vue'
import { Calendar, CheckSquare, Clock, Loader2 } from 'lucide-vue-next'
import { getMyTasks, updateTaskStatus } from '@/api/me/portal'
import { safeArray, unwrapData } from '@/utils/api'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const loading = ref(false)
const actionLoading = ref(null)
const activeTab = ref('all')
const tasks = ref([])

const filteredTasks = computed(() => {
  if (activeTab.value === 'all') return tasks.value
  return tasks.value.filter((task) => task.taskStatus === activeTab.value)
})

const stats = computed(() => ({
  open: tasks.value.filter((task) => task.taskStatus === 'OPEN').length,
  done: tasks.value.filter((task) => task.taskStatus === 'DONE').length,
  cancelled: tasks.value.filter((task) => task.taskStatus === 'CANCELLED').length,
  overdue: tasks.value.filter((task) => isOverdue(task)).length,
}))

function getTaskStatusLabel(status) {
  const labels = {
    OPEN: 'Đang mở',
    DONE: 'Hoàn thành',
    CANCELLED: 'Đã hủy',
  }
  return labels[status] || status || 'Không xác định'
}

function getTaskStatusColor(status) {
  const styles = {
    OPEN: 'bg-blue-100 text-blue-800',
    DONE: 'bg-emerald-100 text-emerald-800',
    CANCELLED: 'bg-slate-100 text-slate-700',
  }
  return styles[status] || 'bg-slate-100 text-slate-700'
}

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN')
}

function isOverdue(task) {
  if (!task?.dueAt || task?.taskStatus !== 'OPEN') return false
  return new Date(task.dueAt) < new Date()
}

async function fetchTasks() {
  loading.value = true
  try {
    const response = await getMyTasks()
    tasks.value = safeArray(unwrapData(response))
  } catch (error) {
    console.error('Failed to fetch portal tasks:', error)
    tasks.value = []
    toast.error('Không thể tải danh sách nhiệm vụ')
  } finally {
    loading.value = false
  }
}

async function changeTaskStatus(task, status) {
  actionLoading.value = `${task.portalInboxItemId}-${status}`
  try {
    await updateTaskStatus(task.portalInboxItemId, { status })
    toast.success('Đã cập nhật trạng thái nhiệm vụ')
    await fetchTasks()
  } catch (error) {
    console.error('Failed to update task status:', error)
    toast.error(error.response?.data?.message || 'Cập nhật trạng thái thất bại')
  } finally {
    actionLoading.value = null
  }
}

onMounted(fetchTasks)
</script>

<template>
  <div class="max-w-4xl mx-auto px-6 py-8">
    <div class="mb-8">
      <h1 class="text-3xl font-black text-slate-900 mb-2">Nhiệm vụ của tôi</h1>
      <p class="text-slate-600">Theo dõi các task được gửi về portal và cập nhật trạng thái trực tiếp từ backend.</p>
    </div>

    <div class="mb-8">
      <div class="flex gap-2 bg-slate-100 p-1 rounded-2xl w-fit">
        <button
          v-for="tab in [
            { key: 'all', label: 'Tất cả' },
            { key: 'OPEN', label: 'Đang mở' },
            { key: 'DONE', label: 'Hoàn thành' },
            { key: 'CANCELLED', label: 'Đã hủy' },
          ]"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'px-4 py-2 rounded-xl font-semibold transition',
            activeTab === tab.key ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900',
          ]"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="grid gap-4 md:grid-cols-4 mb-8">
      <div class="rounded-3xl bg-slate-50 p-6 text-center">
        <div class="text-2xl font-black text-slate-900">{{ stats.open }}</div>
        <div class="text-sm text-slate-600">Đang mở</div>
      </div>
      <div class="rounded-3xl bg-emerald-50 p-6 text-center">
        <div class="text-2xl font-black text-emerald-900">{{ stats.done }}</div>
        <div class="text-sm text-emerald-600">Hoàn thành</div>
      </div>
      <div class="rounded-3xl bg-slate-100 p-6 text-center">
        <div class="text-2xl font-black text-slate-900">{{ stats.cancelled }}</div>
        <div class="text-sm text-slate-600">Đã hủy</div>
      </div>
      <div class="rounded-3xl bg-rose-50 p-6 text-center">
        <div class="text-2xl font-black text-rose-900">{{ stats.overdue }}</div>
        <div class="text-sm text-rose-600">Quá hạn</div>
      </div>
    </div>

    <div v-if="loading" class="space-y-6">
      <div v-for="item in 3" :key="item" class="h-40 animate-pulse rounded-4xl bg-slate-100" />
    </div>

    <div v-else-if="filteredTasks.length" class="space-y-6">
      <div
        v-for="task in filteredTasks"
        :key="task.portalInboxItemId"
        class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm"
        :class="{ 'border-rose-200 bg-rose-50': isOverdue(task) }"
      >
        <div class="flex items-start justify-between gap-4 mb-6">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2 flex-wrap">
              <CheckSquare class="w-6 h-6 text-slate-400" />
              <h3 class="text-xl font-black text-slate-900">{{ task.title }}</h3>
              <span class="px-3 py-1 rounded-full text-xs font-semibold" :class="getTaskStatusColor(task.taskStatus)">
                {{ getTaskStatusLabel(task.taskStatus) }}
              </span>
              <span
                v-if="isOverdue(task)"
                class="px-3 py-1 rounded-full text-xs font-semibold bg-rose-100 text-rose-800"
              >
                Quá hạn
              </span>
            </div>
            <p class="text-slate-600">{{ task.message }}</p>
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4 mb-6">
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-xs font-bold uppercase tracking-wider text-slate-400">Module</p>
            <p class="mt-2 font-semibold text-slate-900">{{ task.relatedModule || 'Portal Task' }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-xs font-bold uppercase tracking-wider text-slate-400">Ngày tạo</p>
            <p class="mt-2 font-semibold text-slate-900">{{ formatDate(task.createdAt) }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-xs font-bold uppercase tracking-wider text-slate-400">Hạn xử lý</p>
            <p class="mt-2 font-semibold text-slate-900">{{ formatDate(task.dueAt) }}</p>
          </div>
          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="text-xs font-bold uppercase tracking-wider text-slate-400">Hoàn thành</p>
            <p class="mt-2 font-semibold text-slate-900">{{ formatDate(task.completedAt) }}</p>
          </div>
        </div>

        <div class="flex items-center justify-between flex-wrap gap-3">
          <div class="text-sm text-slate-500 flex items-center gap-2">
            <Calendar class="w-4 h-4" />
            <span v-if="task.relatedEntityId">Mã liên kết: {{ task.relatedEntityId }}</span>
            <span v-else>Task nội bộ trên portal</span>
          </div>

          <div class="flex gap-2">
            <button
              v-if="task.taskStatus === 'OPEN'"
              @click="changeTaskStatus(task, 'DONE')"
              :disabled="actionLoading === `${task.portalInboxItemId}-DONE`"
              class="px-4 py-2 bg-emerald-600 text-white font-semibold rounded-full hover:bg-emerald-700 disabled:opacity-50 transition inline-flex items-center gap-2"
            >
              <Loader2 v-if="actionLoading === `${task.portalInboxItemId}-DONE`" class="w-4 h-4 animate-spin" />
              Hoàn thành
            </button>

            <button
              v-if="task.taskStatus === 'OPEN'"
              @click="changeTaskStatus(task, 'CANCELLED')"
              :disabled="actionLoading === `${task.portalInboxItemId}-CANCELLED`"
              class="px-4 py-2 bg-slate-200 text-slate-700 font-semibold rounded-full hover:bg-slate-300 disabled:opacity-50 transition inline-flex items-center gap-2"
            >
              <Loader2 v-if="actionLoading === `${task.portalInboxItemId}-CANCELLED`" class="w-4 h-4 animate-spin" />
              Hủy task
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="rounded-4xl border border-slate-200 bg-white p-16 shadow-sm text-center">
      <CheckSquare class="w-20 h-20 text-slate-300 mx-auto mb-6" />
      <h3 class="text-xl font-bold text-slate-900 mb-2">Không có nhiệm vụ nào</h3>
      <p class="text-slate-600">Khi backend gán task cho tài khoản của bạn, dữ liệu sẽ hiển thị ở đây.</p>
    </div>
  </div>
</template>
