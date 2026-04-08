<script setup>
import { ref, reactive } from 'vue'
import { Clock, Calendar, AlertTriangle, CheckCircle, FileText } from 'lucide-vue-next'

const loading = ref(false)
const showForm = ref(false)
const form = reactive({
    issueType: '',
    date: '',
    checkInTime: '',
    checkOutTime: '',
    reason: '',
    evidence: null
})

const requests = ref([
    {
        id: 1,
        issueType: 'FORGOT_CHECK_IN',
        date: '2026-04-15',
        checkInTime: '09:00',
        checkOutTime: '18:00',
        reason: 'Quên chấm công vào sáng nay do khẩn cấp gia đình',
        status: 'PENDING',
        submittedAt: '2026-04-15T10:30:00Z',
        approvedAt: null,
        approvedBy: null,
        comments: []
    },
    {
        id: 2,
        issueType: 'EARLY_LEAVE',
        date: '2026-04-10',
        checkInTime: '08:30',
        checkOutTime: '16:45',
        reason: 'Phải về sớm để đưa con đi khám bệnh',
        status: 'APPROVED',
        submittedAt: '2026-04-10T17:00:00Z',
        approvedAt: '2026-04-11T09:00:00Z',
        approvedBy: 'Nguyễn Thị Lan',
        comments: [
            {
                id: 1,
                author: 'Nguyễn Thị Lan',
                content: 'Đã phê duyệt. Vui lòng bổ sung giấy tờ liên quan.',
                createdAt: '2026-04-11T09:00:00Z'
            }
        ]
    }
])

const issueTypes = [
    { value: 'FORGOT_CHECK_IN', label: 'Quên chấm công vào' },
    { value: 'FORGOT_CHECK_OUT', label: 'Quên chấm công ra' },
    { value: 'WRONG_TIME', label: 'Chấm công sai giờ' },
    { value: 'EARLY_LEAVE', label: 'Về sớm' },
    { value: 'LATE_ARRIVAL', label: 'Đến muộn' },
    { value: 'OTHER', label: 'Lý do khác' }
]

const getStatusLabel = (status) => {
    switch (status) {
        case 'PENDING': return 'Chờ duyệt'
        case 'APPROVED': return 'Đã duyệt'
        case 'REJECTED': return 'Từ chối'
        default: return status
    }
}

const getStatusColor = (status) => {
    switch (status) {
        case 'PENDING': return 'bg-yellow-100 text-yellow-800'
        case 'APPROVED': return 'bg-green-100 text-green-800'
        case 'REJECTED': return 'bg-red-100 text-red-800'
        default: return 'bg-slate-100 text-slate-800'
    }
}

const getIssueTypeLabel = (issueType) => {
    const type = issueTypes.find(t => t.value === issueType)
    return type ? type.label : issueType
}

const formatDate = (dateString) => {
    if (!dateString) return '—'
    return new Date(dateString).toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    })
}

const formatDateTime = (dateString) => {
    if (!dateString) return '—'
    return new Date(dateString).toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    })
}

const validateForm = () => {
    if (!form.issueType) {
        alert('Vui lòng chọn loại vấn đề')
        return false
    }
    if (!form.date) {
        alert('Vui lòng chọn ngày')
        return false
    }
    if (!form.reason.trim()) {
        alert('Vui lòng nhập lý do')
        return false
    }
    return true
}

const submitRequest = async () => {
    if (!validateForm()) return

    loading.value = true
    try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000))

        const newRequest = {
            id: Date.now(),
            ...form,
            status: 'PENDING',
            submittedAt: new Date().toISOString(),
            approvedAt: null,
            approvedBy: null,
            comments: []
        }

        requests.value.unshift(newRequest)

        // Reset form
        Object.keys(form).forEach(key => {
            form[key] = key === 'evidence' ? null : ''
        })
        showForm.value = false

        alert('Yêu cầu điều chỉnh chấm công đã được gửi thành công!')
    } catch (error) {
        console.error('Failed to submit request:', error)
        alert('Có lỗi xảy ra khi gửi yêu cầu. Vui lòng thử lại.')
    } finally {
        loading.value = false
    }
}

const handleFileUpload = (event) => {
    const file = event.target.files[0]
    if (file) {
        form.evidence = file
    }
}
</script>

<template>
    <div class="max-w-4xl mx-auto px-6 py-8">
        <div class="mb-8">
            <h1 class="text-3xl font-black text-slate-900 mb-2">Điều chỉnh chấm công</h1>
            <p class="text-slate-600">Yêu cầu điều chỉnh thông tin chấm công khi có sai sót</p>
        </div>

        <!-- Important Notice -->
        <div class="rounded-4xl border border-amber-200 bg-amber-50 p-6 mb-8">
            <div class="flex items-start gap-3">
                <AlertTriangle class="w-6 h-6 text-amber-600 mt-0.5" />
                <div>
                    <h3 class="font-semibold text-amber-800 mb-2">Lưu ý quan trọng</h3>
                    <ul class="text-sm text-amber-700 space-y-1">
                        <li>• Yêu cầu điều chỉnh phải được gửi trong vòng 7 ngày kể từ ngày xảy ra sự việc</li>
                        <li>• Cần cung cấp bằng chứng hợp lý (hình ảnh, tài liệu, email, v.v.)</li>
                        <li>• Phòng nhân sự sẽ xem xét và phê duyệt trong vòng 2-3 ngày làm việc</li>
                        <li>• Việc lạm dụng tính năng này có thể dẫn đến kỷ luật</li>
                    </ul>
                </div>
            </div>
        </div>

        <!-- New Request Button -->
        <div class="mb-8">
            <button @click="showForm = !showForm"
                class="inline-flex items-center gap-2 px-6 py-3 bg-teal-600 text-white font-semibold rounded-full hover:bg-teal-700 transition">
                <Clock class="w-5 h-5" />
                {{ showForm ? 'Hủy' : 'Tạo yêu cầu mới' }}
            </button>
        </div>

        <!-- New Request Form -->
        <div v-if="showForm" class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm mb-8">
            <h2 class="text-xl font-bold text-slate-900 mb-6">Tạo yêu cầu điều chỉnh chấm công</h2>

            <form @submit.prevent="submitRequest" class="space-y-6">
                <div class="grid gap-6 md:grid-cols-2">
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Loại vấn đề *</label>
                        <select v-model="form.issueType"
                            class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500">
                            <option value="">Chọn loại vấn đề</option>
                            <option v-for="type in issueTypes" :key="type.value" :value="type.value">
                                {{ type.label }}
                            </option>
                        </select>
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Ngày xảy ra *</label>
                        <input v-model="form.date" type="date"
                            class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500">
                    </div>
                </div>

                <div class="grid gap-6 md:grid-cols-2">
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Giờ vào</label>
                        <input v-model="form.checkInTime" type="time"
                            class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500">
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Giờ ra</label>
                        <input v-model="form.checkOutTime" type="time"
                            class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500">
                    </div>
                </div>

                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Lý do chi tiết *</label>
                    <textarea v-model="form.reason" rows="4"
                        placeholder="Mô tả chi tiết lý do cần điều chỉnh chấm công..."
                        class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500"></textarea>
                </div>

                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Bằng chứng (không bắt buộc)</label>
                    <input @change="handleFileUpload" type="file" accept="image/*,.pdf,.doc,.docx"
                        class="w-full px-4 py-3 border border-slate-300 rounded-2xl focus:ring-2 focus:ring-teal-500 focus:border-teal-500">
                    <p class="text-xs text-slate-500 mt-1">Chấp nhận: hình ảnh, PDF, Word. Tối đa 10MB</p>
                </div>

                <div class="flex gap-4">
                    <button type="submit" :disabled="loading"
                        class="px-6 py-3 bg-teal-600 text-white font-semibold rounded-full hover:bg-teal-700 disabled:opacity-50 transition">
                        {{ loading ? 'Đang gửi...' : 'Gửi yêu cầu' }}
                    </button>
                    <button type="button" @click="showForm = false"
                        class="px-6 py-3 bg-slate-200 text-slate-700 font-semibold rounded-full hover:bg-slate-300 transition">
                        Hủy
                    </button>
                </div>
            </form>
        </div>

        <!-- Requests List -->
        <div class="space-y-6">
            <div v-for="request in requests" :key="request.id"
                class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm">
                <!-- Request Header -->
                <div class="flex items-start justify-between mb-6">
                    <div class="flex-1">
                        <div class="flex items-center gap-3 mb-2">
                            <Clock class="w-6 h-6 text-slate-400" />
                            <h3 class="text-xl font-black text-slate-900">
                                {{ getIssueTypeLabel(request.issueType) }}
                            </h3>
                            <span class="px-3 py-1 rounded-full text-xs font-semibold"
                                :class="getStatusColor(request.status)">
                                {{ getStatusLabel(request.status) }}
                            </span>
                        </div>
                        <p class="text-slate-600 mb-3">{{ request.reason }}</p>

                        <div class="flex items-center gap-4 text-sm text-slate-500">
                            <div class="flex items-center gap-1">
                                <Calendar class="w-4 h-4" />
                                <span>{{ formatDate(request.date) }}</span>
                            </div>
                            <div class="flex items-center gap-1">
                                <Clock class="w-4 h-4" />
                                <span>{{ request.checkInTime }} - {{ request.checkOutTime }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Request Details -->
                <div class="grid gap-4 md:grid-cols-2 mb-6">
                    <div>
                        <p class="text-sm text-slate-500">Ngày gửi</p>
                        <p class="font-semibold text-slate-900">{{ formatDateTime(request.submittedAt) }}</p>
                    </div>
                    <div v-if="request.approvedAt">
                        <p class="text-sm text-slate-500">Ngày duyệt</p>
                        <p class="font-semibold text-slate-900">{{ formatDateTime(request.approvedAt) }}</p>
                    </div>
                    <div v-if="request.approvedBy">
                        <p class="text-sm text-slate-500">Người duyệt</p>
                        <p class="font-semibold text-slate-900">{{ request.approvedBy }}</p>
                    </div>
                </div>

                <!-- Comments -->
                <div v-if="request.comments.length > 0" class="border-t border-slate-200 pt-6">
                    <h4 class="text-sm font-bold text-slate-900 uppercase tracking-wider mb-3">Bình luận</h4>
                    <div class="space-y-3">
                        <div v-for="comment in request.comments" :key="comment.id" class="bg-slate-50 rounded-2xl p-4">
                            <div class="flex items-center gap-2 mb-2">
                                <FileText class="w-4 h-4 text-slate-400" />
                                <span class="font-semibold text-slate-900">{{ comment.author }}</span>
                                <span class="text-sm text-slate-500">{{ formatDateTime(comment.createdAt) }}</span>
                            </div>
                            <p class="text-slate-700">{{ comment.content }}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Empty State -->
        <div v-if="requests.length === 0"
            class="rounded-4xl border border-slate-200 bg-white p-16 shadow-sm text-center">
            <Clock class="w-20 h-20 text-slate-300 mx-auto mb-6" />
            <h3 class="text-xl font-bold text-slate-900 mb-2">Chưa có yêu cầu nào</h3>
            <p class="text-slate-600">Bạn chưa gửi yêu cầu điều chỉnh chấm công nào.</p>
        </div>
    </div>
</template>
