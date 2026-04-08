<script setup>
import { ref, reactive } from 'vue'
import { FileText, Calendar, MessageSquare, Upload, X } from 'lucide-vue-next'

const showForm = ref(false)
const loading = ref(false)
const form = reactive({
    effectiveDate: '',
    reason: '',
    detailedReason: '',
    handoverNotes: '',
    attachments: []
})

const requests = ref([
    {
        id: 1,
        effectiveDate: '2026-05-15',
        reason: 'Cá nhân',
        detailedReason: 'Muốn thay đổi môi trường làm việc',
        handoverNotes: 'Đã bàn giao công việc cho đồng nghiệp Nguyễn Văn B',
        status: 'PENDING',
        submittedAt: '2026-04-08T10:30:00Z',
        attachments: ['resignation_letter.pdf']
    }
])

const resignationReasons = [
    'Cá nhân',
    'Sức khỏe',
    'Học tập',
    'Thăng tiến',
    'Di chuyển',
    'Khác'
]

const getStatusLabel = (status) => {
    switch (status) {
        case 'PENDING': return 'Đang chờ duyệt'
        case 'APPROVED': return 'Đã duyệt'
        case 'REJECTED': return 'Từ chối'
        case 'COMPLETED': return 'Hoàn thành'
        default: return status
    }
}

const getStatusColor = (status) => {
    switch (status) {
        case 'PENDING': return 'bg-yellow-100 text-yellow-800'
        case 'APPROVED': return 'bg-green-100 text-green-800'
        case 'REJECTED': return 'bg-red-100 text-red-800'
        case 'COMPLETED': return 'bg-blue-100 text-blue-800'
        default: return 'bg-slate-100 text-slate-800'
    }
}

const submitRequest = async () => {
    loading.value = true
    try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000))

        const newRequest = {
            id: Date.now(),
            ...form,
            status: 'PENDING',
            submittedAt: new Date().toISOString()
        }

        requests.value.unshift(newRequest)

        // Reset form
        Object.keys(form).forEach(key => {
            if (key === 'attachments') {
                form[key] = []
            } else {
                form[key] = ''
            }
        })
        showForm.value = false
    } catch (error) {
        console.error('Failed to submit resignation request:', error)
    } finally {
        loading.value = false
    }
}

const removeAttachment = (index) => {
    form.attachments.splice(index, 1)
}

const formatDate = (dateString) => {
    if (!dateString) return '—'
    return new Date(dateString).toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    })
}

const minDate = () => {
    const today = new Date()
    today.setDate(today.getDate() + 30) // At least 30 days notice
    return today.toISOString().split('T')[0]
}
</script>

<template>
    <div class="max-w-4xl mx-auto px-6 py-8">
        <div class="mb-8">
            <h1 class="text-3xl font-black text-slate-900 mb-2">Yêu cầu nghỉ việc</h1>
            <p class="text-slate-600">Gửi yêu cầu nghỉ việc để trình lãnh đạo phê duyệt</p>
        </div>

        <!-- Important Notice -->
        <div class="rounded-4xl border border-yellow-200 bg-yellow-50 p-6 mb-8">
            <div class="flex items-start gap-3">
                <MessageSquare class="w-6 h-6 text-yellow-600 mt-0.5" />
                <div>
                    <h3 class="font-semibold text-yellow-800 mb-2">Lưu ý quan trọng</h3>
                    <ul class="text-sm text-yellow-700 space-y-1">
                        <li>• Thời gian báo trước tối thiểu 30 ngày làm việc</li>
                        <li>• Yêu cầu cần được trưởng phòng và HR phê duyệt</li>
                        <li>• Quá trình nghỉ việc sẽ bao gồm bàn giao công việc và thanh toán</li>
                    </ul>
                </div>
            </div>
        </div>

        <!-- Create Request Button -->
        <div class="mb-8">
            <button @click="showForm = !showForm"
                class="inline-flex items-center gap-2 px-6 py-3 bg-red-600 text-white font-semibold rounded-full hover:bg-red-700 transition">
                <FileText class="w-5 h-5" />
                Tạo yêu cầu nghỉ việc
            </button>
        </div>

        <!-- Create Request Form -->
        <div v-if="showForm" class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm mb-8">
            <h2 class="text-xl font-black text-slate-900 mb-6">Thông tin nghỉ việc</h2>

            <form @submit.prevent="submitRequest" class="space-y-6">
                <div class="grid gap-6 md:grid-cols-2">
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Ngày hiệu lực cuối cùng</label>
                        <input v-model="form.effectiveDate" type="date" :min="minDate()" required
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-red-500" />
                        <p class="text-xs text-slate-500 mt-1">Tối thiểu 30 ngày kể từ ngày hiện tại</p>
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Lý do nghỉ việc</label>
                        <select v-model="form.reason" required
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-red-500">
                            <option value="">Chọn lý do</option>
                            <option v-for="reason in resignationReasons" :key="reason" :value="reason">
                                {{ reason }}
                            </option>
                        </select>
                    </div>
                </div>

                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Lý do chi tiết</label>
                    <textarea v-model="form.detailedReason" required rows="4"
                        placeholder="Mô tả chi tiết lý do nghỉ việc..."
                        class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-red-500"></textarea>
                </div>

                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Ghi chú bàn giao công việc</label>
                    <textarea v-model="form.handoverNotes" rows="3"
                        placeholder="Mô tả công việc cần bàn giao, người nhận việc..."
                        class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-red-500"></textarea>
                </div>

                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Tài liệu đính kèm (Đơn xin nghỉ
                        việc)</label>
                    <div class="space-y-2">
                        <input type="file" multiple accept=".pdf,.doc,.docx"
                            @change="(e) => form.attachments = Array.from(e.target.files)"
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-red-500" />
                        <p class="text-xs text-slate-500">Chấp nhận file PDF, DOC, DOCX</p>
                        <div v-if="form.attachments.length > 0" class="flex flex-wrap gap-2">
                            <span v-for="(file, index) in form.attachments" :key="index"
                                class="inline-flex items-center gap-2 px-3 py-1 bg-slate-100 rounded-full text-sm">
                                {{ file.name }}
                                <button @click="removeAttachment(index)" class="text-slate-500 hover:text-red-500">
                                    <X class="w-4 h-4" />
                                </button>
                            </span>
                        </div>
                    </div>
                </div>

                <div class="flex gap-4">
                    <button type="submit" :disabled="loading"
                        class="px-6 py-3 bg-red-600 text-white font-semibold rounded-full hover:bg-red-700 disabled:opacity-50 transition">
                        <span v-if="loading" class="inline-flex items-center gap-2">
                            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                            Đang gửi...
                        </span>
                        <span v-else>Gửi yêu cầu nghỉ việc</span>
                    </button>

                    <button type="button" @click="showForm = false"
                        class="px-6 py-3 bg-slate-200 text-slate-700 font-semibold rounded-full hover:bg-slate-300 transition">
                        Hủy
                    </button>
                </div>
            </form>
        </div>

        <!-- Request History -->
        <div class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm">
            <h2 class="text-xl font-black text-slate-900 mb-6">Lịch sử yêu cầu nghỉ việc</h2>

            <div v-if="requests.length === 0" class="text-center py-8">
                <FileText class="w-16 h-16 text-slate-300 mx-auto mb-4" />
                <h3 class="text-lg font-semibold text-slate-900 mb-2">Chưa có yêu cầu nào</h3>
                <p class="text-slate-600">Tạo yêu cầu nghỉ việc khi cần thiết.</p>
            </div>

            <div v-else class="space-y-4">
                <div v-for="request in requests" :key="request.id"
                    class="rounded-3xl border border-slate-100 bg-slate-50 p-6">
                    <div class="flex items-start justify-between mb-4">
                        <div class="flex-1">
                            <div class="flex items-center gap-3 mb-2">
                                <Calendar class="w-5 h-5 text-slate-400" />
                                <span class="font-semibold text-slate-900">Ngày hiệu lực: {{
                                    formatDate(request.effectiveDate) }}</span>
                                <span class="px-3 py-1 rounded-full text-xs font-semibold"
                                    :class="getStatusColor(request.status)">
                                    {{ getStatusLabel(request.status) }}
                                </span>
                            </div>
                            <p class="text-sm text-slate-600 mb-2"><strong>Lý do:</strong> {{ request.reason }}</p>
                            <p class="text-sm text-slate-600 mb-2"><strong>Chi tiết:</strong> {{ request.detailedReason
                            }}</p>
                            <p v-if="request.handoverNotes" class="text-sm text-slate-600">
                                <strong>Bàn giao:</strong> {{ request.handoverNotes }}
                            </p>
                        </div>
                    </div>

                    <div
                        class="flex items-center justify-between text-sm text-slate-500 pt-4 border-t border-slate-200">
                        <span>Đã gửi: {{ formatDate(request.submittedAt) }}</span>
                    </div>

                    <div v-if="request.attachments.length > 0" class="mt-4 pt-4 border-t border-slate-200">
                        <p class="text-xs text-slate-500 uppercase tracking-wider mb-2">Tài liệu đính kèm</p>
                        <div class="flex flex-wrap gap-2">
                            <span v-for="file in request.attachments" :key="file"
                                class="inline-flex items-center gap-2 px-3 py-1 bg-white rounded-full text-sm border border-slate-200">
                                <Upload class="w-4 h-4" />
                                {{ file }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>