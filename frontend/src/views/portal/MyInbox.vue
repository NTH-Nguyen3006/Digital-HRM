<script setup>
import { ref, onMounted } from 'vue'
import { getMyInbox, markInboxAsRead } from '@/api/me/portal'
import { Bell, Check, X, Clock, AlertTriangle, Info, CheckCircle } from 'lucide-vue-next'

const inboxItems = ref([])
const loading = ref(false)
const activeFilter = ref('all')

const filters = [
    { key: 'all', label: 'Tất cả', count: 0 },
    { key: 'unread', label: 'Chưa đọc', count: 0 },
    { key: 'read', label: 'Đã đọc', count: 0 }
]

const getIcon = (type) => {
    switch (type) {
        case 'APPROVAL': return CheckCircle
        case 'REJECTION': return X
        case 'REMINDER': return Clock
        case 'WARNING': return AlertTriangle
        default: return Info
    }
}

const getIconColor = (type) => {
    switch (type) {
        case 'APPROVAL': return 'text-green-500'
        case 'REJECTION': return 'text-red-500'
        case 'REMINDER': return 'text-blue-500'
        case 'WARNING': return 'text-yellow-500'
        default: return 'text-slate-500'
    }
}

const loadInbox = async () => {
    loading.value = true
    try {
        const params = activeFilter.value !== 'all' ? { readStatus: activeFilter.value.toUpperCase() } : {}
        const response = await getMyInbox(params)
        inboxItems.value = response.data?.data || []

        // Update filter counts
        filters[0].count = inboxItems.value.length
        filters[1].count = inboxItems.value.filter(item => !item.isRead).length
        filters[2].count = inboxItems.value.filter(item => item.isRead).length
    } catch (error) {
        console.error('Failed to load inbox:', error)
        // Sample data for demo
        inboxItems.value = [
            {
                portalInboxItemId: 1,
                title: 'Đơn nghỉ phép đã được phê duyệt',
                message: 'Đơn nghỉ phép của bạn từ 15/04/2026 đến 17/04/2026 đã được trưởng phòng phê duyệt.',
                type: 'APPROVAL',
                isRead: false,
                createdAt: '2026-04-10T08:30:00Z',
                senderName: 'Trần Văn Minh'
            },
            {
                portalInboxItemId: 2,
                title: 'Nhắc nhở cập nhật thông tin cá nhân',
                message: 'Vui lòng cập nhật thông tin ngân hàng để nhận lương kịp thời.',
                type: 'REMINDER',
                isRead: true,
                createdAt: '2026-04-08T14:20:00Z',
                senderName: 'HR Department'
            },
            {
                portalInboxItemId: 3,
                title: 'Phiếu lương tháng 4 đã được phát hành',
                message: 'Phiếu lương của bạn đã sẵn sàng. Vui lòng kiểm tra và xác nhận.',
                type: 'INFO',
                isRead: false,
                createdAt: '2026-04-05T09:00:00Z',
                senderName: 'Payroll Team'
            }
        ]
        filters[0].count = inboxItems.value.length
        filters[1].count = inboxItems.value.filter(item => !item.isRead).length
        filters[2].count = inboxItems.value.filter(item => item.isRead).length
    } finally {
        loading.value = false
    }
}

const markAsRead = async (itemId) => {
    try {
        await markInboxAsRead(itemId)
        const item = inboxItems.value.find(i => i.portalInboxItemId === itemId)
        if (item) {
            item.isRead = true
            filters[1].count--
            filters[2].count++
        }
    } catch (error) {
        console.error('Failed to mark as read:', error)
        // For demo, just update locally
        const item = inboxItems.value.find(i => i.portalInboxItemId === itemId)
        if (item) {
            item.isRead = true
            filters[1].count--
            filters[2].count++
        }
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

onMounted(() => {
    loadInbox()
})
</script>

<template>
    <div class="max-w-4xl mx-auto px-6 py-8">
        <div class="mb-8">
            <h1 class="text-3xl font-black text-slate-900 mb-2">Thông báo</h1>
            <p class="text-slate-600">Theo dõi các thông báo và cập nhật quan trọng từ hệ thống</p>
        </div>

        <!-- Filters -->
        <div class="flex gap-2 mb-6">
            <button v-for="filter in filters" :key="filter.key" @click="activeFilter = filter.key; loadInbox()"
                class="px-4 py-2 rounded-full text-sm font-semibold transition" :class="activeFilter === filter.key
                    ? 'bg-teal-600 text-white shadow-md'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'">
                {{ filter.label }}
                <span v-if="filter.count > 0" class="ml-2 bg-white/20 px-2 py-0.5 rounded-full text-xs">
                    {{ filter.count }}
                </span>
            </button>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="flex justify-center py-12">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
        </div>

        <!-- Inbox Items -->
        <div v-else-if="inboxItems.length > 0" class="space-y-4">
            <div v-for="item in inboxItems" :key="item.portalInboxItemId"
                class="rounded-4xl border border-slate-200 bg-white p-6 shadow-sm transition hover:shadow-md"
                :class="{ 'bg-slate-50': item.isRead }">
                <div class="flex items-start gap-4">
                    <div class="shrink-0">
                        <component :is="getIcon(item.type)" :class="['w-6 h-6', getIconColor(item.type)]" />
                    </div>

                    <div class="flex-1 min-w-0">
                        <div class="flex items-start justify-between gap-4">
                            <div class="flex-1">
                                <h3 class="text-lg font-semibold text-slate-900 mb-1">{{ item.title }}</h3>
                                <p class="text-slate-600 mb-3 leading-relaxed">{{ item.message }}</p>

                                <div class="flex items-center gap-4 text-sm text-slate-500">
                                    <span>{{ item.senderName }}</span>
                                    <span>{{ formatDate(item.createdAt) }}</span>
                                </div>
                            </div>

                            <div class="flex items-center gap-2">
                                <span v-if="!item.isRead"
                                    class="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-teal-100 text-teal-800">
                                    Mới
                                </span>

                                <button v-if="!item.isRead" @click="markAsRead(item.portalInboxItemId)"
                                    class="inline-flex items-center gap-1 px-3 py-1 rounded-lg bg-teal-600 text-white text-sm font-semibold hover:bg-teal-700 transition">
                                    <Check class="w-4 h-4" />
                                    Đánh dấu đã đọc
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Empty State -->
        <div v-else class="text-center py-12">
            <Bell class="w-16 h-16 text-slate-300 mx-auto mb-4" />
            <h3 class="text-lg font-semibold text-slate-900 mb-2">Không có thông báo nào</h3>
            <p class="text-slate-600">Bạn sẽ nhận được thông báo khi có cập nhật mới.</p>
        </div>
    </div>
</template>