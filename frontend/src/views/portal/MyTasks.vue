<script setup>
import { ref, reactive } from 'vue'
import { CheckSquare, Clock, AlertCircle, CheckCircle, User, Calendar } from 'lucide-vue-next'

const loading = ref(false)
const activeTab = ref('all')
const tasks = ref([
    {
        id: 1,
        title: 'Hoàn thành báo cáo tháng 4',
        description: 'Tổng hợp và trình bày báo cáo hiệu suất công việc tháng 4/2026',
        status: 'IN_PROGRESS',
        priority: 'HIGH',
        assignedBy: 'Trần Văn Minh',
        assignedAt: '2026-04-01T08:00:00Z',
        dueDate: '2026-04-30T17:00:00Z',
        completedAt: null,
        progress: 75,
        comments: [
            {
                id: 1,
                author: 'Trần Văn Minh',
                content: 'Hãy đảm bảo báo cáo có đầy đủ các chỉ số KPI',
                createdAt: '2026-04-01T08:00:00Z'
            }
        ]
    },
    {
        id: 2,
        title: 'Tham gia đào tạo an toàn lao động',
        description: 'Tham gia khóa đào tạo an toàn lao động theo quy định',
        status: 'COMPLETED',
        priority: 'MEDIUM',
        assignedBy: 'Nguyễn Thị Lan',
        assignedAt: '2026-03-15T09:00:00Z',
        dueDate: '2026-04-15T17:00:00Z',
        completedAt: '2026-04-10T16:30:00Z',
        progress: 100,
        comments: []
    },
    {
        id: 3,
        title: 'Cập nhật hồ sơ cá nhân',
        description: 'Cập nhật thông tin cá nhân và bằng cấp mới nhất',
        status: 'PENDING',
        priority: 'LOW',
        assignedBy: 'Lê Văn Hùng',
        assignedAt: '2026-04-05T10:00:00Z',
        dueDate: '2026-04-20T17:00:00Z',
        completedAt: null,
        progress: 0,
        comments: []
    },
    {
        id: 4,
        title: 'Phát triển tính năng mới cho hệ thống',
        description: 'Phát triển module quản lý nhân sự với Vue.js và Spring Boot',
        status: 'IN_PROGRESS',
        priority: 'HIGH',
        assignedBy: 'Phạm Thị Mai',
        assignedAt: '2026-03-20T08:00:00Z',
        dueDate: '2026-05-15T17:00:00Z',
        completedAt: null,
        progress: 45,
        comments: [
            {
                id: 2,
                author: 'Phạm Thị Mai',
                content: 'Đã review code, cần tối ưu hóa performance',
                createdAt: '2026-04-08T14:00:00Z'
            }
        ]
    }
])

const filteredTasks = computed(() => {
    if (activeTab.value === 'all') return tasks.value
    return tasks.value.filter(task => task.status === activeTab.value)
})

const getStatusLabel = (status) => {
    switch (status) {
        case 'PENDING': return 'Chưa bắt đầu'
        case 'IN_PROGRESS': return 'Đang thực hiện'
        case 'COMPLETED': return 'Hoàn thành'
        case 'OVERDUE': return 'Quá hạn'
        default: return status
    }
}

const getStatusColor = (status) => {
    switch (status) {
        case 'PENDING': return 'bg-slate-100 text-slate-800'
        case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800'
        case 'COMPLETED': return 'bg-green-100 text-green-800'
        case 'OVERDUE': return 'bg-red-100 text-red-800'
        default: return 'bg-slate-100 text-slate-800'
    }
}

const getPriorityLabel = (priority) => {
    switch (priority) {
        case 'LOW': return 'Thấp'
        case 'MEDIUM': return 'Trung bình'
        case 'HIGH': return 'Cao'
        default: return priority
    }
}

const getPriorityColor = (priority) => {
    switch (priority) {
        case 'LOW': return 'bg-green-100 text-green-800'
        case 'MEDIUM': return 'bg-yellow-100 text-yellow-800'
        case 'HIGH': return 'bg-red-100 text-red-800'
        default: return 'bg-slate-100 text-slate-800'
    }
}

const formatDate = (dateString) => {
    if (!dateString) return '—'
    return new Date(dateString).toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    })
}

const isOverdue = (task) => {
    if (task.status === 'COMPLETED') return false
    return new Date(task.dueDate) < new Date()
}

const updateTaskStatus = async (taskId, newStatus) => {
    loading.value = true
    try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 500))

        const task = tasks.value.find(t => t.id === taskId)
        if (task) {
            task.status = newStatus
            if (newStatus === 'COMPLETED') {
                task.completedAt = new Date().toISOString()
                task.progress = 100
            }
        }
    } catch (error) {
        console.error('Failed to update task status:', error)
    } finally {
        loading.value = false
    }
}

const updateProgress = async (taskId, progress) => {
    loading.value = true
    try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 500))

        const task = tasks.value.find(t => t.id === taskId)
        if (task) {
            task.progress = progress
            if (progress === 100) {
                task.status = 'COMPLETED'
                task.completedAt = new Date().toISOString()
            }
        }
    } catch (error) {
        console.error('Failed to update task progress:', error)
    } finally {
        loading.value = false
    }
}
</script>

<template>
    <div class="max-w-4xl mx-auto px-6 py-8">
        <div class="mb-8">
            <h1 class="text-3xl font-black text-slate-900 mb-2">Nhiệm vụ của tôi</h1>
            <p class="text-slate-600">Theo dõi và cập nhật tiến độ các nhiệm vụ được giao</p>
        </div>

        <!-- Tabs -->
        <div class="mb-8">
            <div class="flex gap-2 bg-slate-100 p-1 rounded-2xl w-fit">
                <button v-for="tab in [
                    { key: 'all', label: 'Tất cả' },
                    { key: 'PENDING', label: 'Chưa bắt đầu' },
                    { key: 'IN_PROGRESS', label: 'Đang thực hiện' },
                    { key: 'COMPLETED', label: 'Hoàn thành' }
                ]" :key="tab.key" @click="activeTab = tab.key" :class="[
                    'px-4 py-2 rounded-xl font-semibold transition',
                    activeTab === tab.key
                        ? 'bg-white text-slate-900 shadow-sm'
                        : 'text-slate-600 hover:text-slate-900'
                ]">
                    {{ tab.label }}
                </button>
            </div>
        </div>

        <!-- Task Statistics -->
        <div class="grid gap-4 md:grid-cols-4 mb-8">
            <div class="rounded-3xl bg-slate-50 p-6 text-center">
                <div class="text-2xl font-black text-slate-900">{{tasks.filter(t => t.status === 'PENDING').length}}
                </div>
                <div class="text-sm text-slate-600">Chưa bắt đầu</div>
            </div>
            <div class="rounded-3xl bg-blue-50 p-6 text-center">
                <div class="text-2xl font-black text-blue-900">{{tasks.filter(t => t.status === 'IN_PROGRESS').length
                }}</div>
                <div class="text-sm text-blue-600">Đang thực hiện</div>
            </div>
            <div class="rounded-3xl bg-green-50 p-6 text-center">
                <div class="text-2xl font-black text-green-900">{{tasks.filter(t => t.status === 'COMPLETED').length}}
                </div>
                <div class="text-sm text-green-600">Hoàn thành</div>
            </div>
            <div class="rounded-3xl bg-red-50 p-6 text-center">
                <div class="text-2xl font-black text-red-900">{{tasks.filter(t => isOverdue(t)).length}}</div>
                <div class="text-sm text-red-600">Quá hạn</div>
            </div>
        </div>

        <!-- Tasks List -->
        <div class="space-y-6">
            <div v-for="task in filteredTasks" :key="task.id"
                class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm"
                :class="{ 'border-red-200 bg-red-50': isOverdue(task) }">
                <!-- Task Header -->
                <div class="flex items-start justify-between mb-6">
                    <div class="flex-1">
                        <div class="flex items-center gap-3 mb-2">
                            <CheckSquare class="w-6 h-6 text-slate-400" />
                            <h3 class="text-xl font-black text-slate-900">{{ task.title }}</h3>
                            <span class="px-3 py-1 rounded-full text-xs font-semibold"
                                :class="getStatusColor(task.status)">
                                {{ getStatusLabel(task.status) }}
                            </span>
                            <span v-if="isOverdue(task)"
                                class="px-3 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800">
                                Quá hạn
                            </span>
                        </div>
                        <p class="text-slate-600 mb-3">{{ task.description }}</p>

                        <div class="flex items-center gap-4 text-sm text-slate-500">
                            <div class="flex items-center gap-1">
                                <User class="w-4 h-4" />
                                <span>Giao bởi: {{ task.assignedBy }}</span>
                            </div>
                            <div class="flex items-center gap-1">
                                <Calendar class="w-4 h-4" />
                                <span>Hạn: {{ formatDate(task.dueDate) }}</span>
                            </div>
                        </div>
                    </div>

                    <div class="flex flex-col items-end gap-2">
                        <span class="px-3 py-1 rounded-full text-xs font-semibold"
                            :class="getPriorityColor(task.priority)">
                            {{ getPriorityLabel(task.priority) }}
                        </span>
                    </div>
                </div>

                <!-- Progress -->
                <div class="mb-6">
                    <div class="flex items-center justify-between mb-2">
                        <span class="text-sm font-semibold text-slate-700">Tiến độ</span>
                        <span class="text-sm text-slate-600">{{ task.progress }}%</span>
                    </div>
                    <div class="w-full bg-slate-200 rounded-full h-2">
                        <div class="bg-teal-600 h-2 rounded-full transition-all duration-300"
                            :style="{ width: `${task.progress}%` }"></div>
                    </div>
                </div>

                <!-- Actions -->
                <div class="flex items-center justify-between">
                    <div class="flex gap-2">
                        <button v-if="task.status === 'PENDING'" @click="updateTaskStatus(task.id, 'IN_PROGRESS')"
                            :disabled="loading"
                            class="px-4 py-2 bg-blue-600 text-white font-semibold rounded-full hover:bg-blue-700 disabled:opacity-50 transition">
                            Bắt đầu
                        </button>

                        <button v-if="task.status === 'IN_PROGRESS'" @click="updateTaskStatus(task.id, 'COMPLETED')"
                            :disabled="loading"
                            class="px-4 py-2 bg-green-600 text-white font-semibold rounded-full hover:bg-green-700 disabled:opacity-50 transition">
                            Hoàn thành
                        </button>

                        <button v-if="task.status !== 'COMPLETED'"
                            @click="updateProgress(task.id, Math.min(task.progress + 25, 100))" :disabled="loading"
                            class="px-4 py-2 bg-slate-200 text-slate-700 font-semibold rounded-full hover:bg-slate-300 disabled:opacity-50 transition">
                            Cập nhật tiến độ
                        </button>
                    </div>

                    <div class="text-sm text-slate-500">
                        <span v-if="task.completedAt">Hoàn thành: {{ formatDate(task.completedAt) }}</span>
                        <span v-else-if="task.status === 'IN_PROGRESS'">Đang thực hiện</span>
                        <span v-else>Chưa bắt đầu</span>
                    </div>
                </div>

                <!-- Comments -->
                <div v-if="task.comments.length > 0" class="mt-6 pt-6 border-t border-slate-200">
                    <h4 class="text-sm font-bold text-slate-900 uppercase tracking-wider mb-3">Bình luận</h4>
                    <div class="space-y-3">
                        <div v-for="comment in task.comments" :key="comment.id" class="bg-slate-50 rounded-2xl p-4">
                            <div class="flex items-center gap-2 mb-2">
                                <User class="w-4 h-4 text-slate-400" />
                                <span class="font-semibold text-slate-900">{{ comment.author }}</span>
                                <span class="text-sm text-slate-500">{{ formatDate(comment.createdAt) }}</span>
                            </div>
                            <p class="text-slate-700">{{ comment.content }}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Empty State -->
        <div v-if="filteredTasks.length === 0"
            class="rounded-4xl border border-slate-200 bg-white p-16 shadow-sm text-center">
            <CheckSquare class="w-20 h-20 text-slate-300 mx-auto mb-6" />
            <h3 class="text-xl font-bold text-slate-900 mb-2">Không có nhiệm vụ nào</h3>
            <p class="text-slate-600">Bạn chưa được giao nhiệm vụ nào trong danh mục này.</p>
        </div>
    </div>
</template>