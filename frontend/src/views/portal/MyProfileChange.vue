<script setup>
import { ref, reactive } from 'vue'
import { Edit, Upload, X, Check } from 'lucide-vue-next'

const showForm = ref(false)
const loading = ref(false)
const form = reactive({
    changeType: '',
    fieldName: '',
    currentValue: '',
    newValue: '',
    reason: '',
    attachments: []
})

const changeTypes = [
    { value: 'personal_info', label: 'Thông tin cá nhân' },
    { value: 'contact_info', label: 'Thông tin liên hệ' },
    { value: 'bank_info', label: 'Thông tin ngân hàng' },
    { value: 'emergency_contact', label: 'Liên hệ khẩn cấp' },
    { value: 'address', label: 'Địa chỉ' },
    { value: 'other', label: 'Khác' }
]

const requests = ref([
    {
        id: 1,
        changeType: 'contact_info',
        fieldName: 'Số điện thoại',
        currentValue: '0912 345 678',
        newValue: '0987 654 321',
        reason: 'Đổi số điện thoại mới',
        status: 'PENDING',
        submittedAt: '2026-04-08T10:30:00Z',
        attachments: []
    },
    {
        id: 2,
        changeType: 'bank_info',
        fieldName: 'Số tài khoản ngân hàng',
        currentValue: '1234567890',
        newValue: '0987654321',
        reason: 'Đổi tài khoản ngân hàng',
        status: 'APPROVED',
        submittedAt: '2026-04-05T14:20:00Z',
        reviewedAt: '2026-04-06T09:15:00Z',
        reviewerName: 'Nguyễn Thị Lan',
        attachments: ['bank_statement.pdf']
    }
])

const getStatusLabel = (status) => {
    switch (status) {
        case 'PENDING': return 'Đang chờ duyệt'
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
        console.error('Failed to submit request:', error)
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
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    })
}
</script>

<template>
    <div class="max-w-4xl mx-auto px-6 py-8">
        <div class="mb-8">
            <h1 class="text-3xl font-black text-slate-900 mb-2">Yêu cầu cập nhật hồ sơ</h1>
            <p class="text-slate-600">Gửi yêu cầu thay đổi thông tin cá nhân để HR duyệt</p>
        </div>

        <!-- Create Request Button -->
        <div class="mb-8">
            <button @click="showForm = !showForm"
                class="inline-flex items-center gap-2 px-6 py-3 bg-teal-600 text-white font-semibold rounded-full hover:bg-teal-700 transition">
                <Edit class="w-5 h-5" />
                Tạo yêu cầu mới
            </button>
        </div>

        <!-- Create Request Form -->
        <div v-if="showForm" class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm mb-8">
            <h2 class="text-xl font-black text-slate-900 mb-6">Tạo yêu cầu cập nhật</h2>

            <form @submit.prevent="submitRequest" class="space-y-6">
                <div class="grid gap-6 md:grid-cols-2">
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Loại thay đổi</label>
                        <select v-model="form.changeType" required
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-teal-500">
                            <option value="">Chọn loại thay đổi</option>
                            <option v-for="type in changeTypes" :key="type.value" :value="type.value">
                                {{ type.label }}
                            </option>
                        </select>
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Trường thông tin</label>
                        <input v-model="form.fieldName" type="text" required
                            placeholder="Ví dụ: Số điện thoại, Địa chỉ..."
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-teal-500" />
                    </div>
                </div>

                <div class="grid gap-6 md:grid-cols-2">
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Giá trị hiện tại</label>
                        <input v-model="form.currentValue" type="text" required placeholder="Giá trị hiện tại"
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-teal-500" />
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Giá trị mới</label>
                        <input v-model="form.newValue" type="text" required placeholder="Giá trị muốn thay đổi"
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-teal-500" />
                    </div>
                </div>

                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Lý do thay đổi</label>
                    <textarea v-model="form.reason" required rows="4" placeholder="Giải thích lý do cần thay đổi..."
                        class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-teal-500"></textarea>
                </div>

                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Tài liệu đính kèm (tùy chọn)</label>
                    <div class="space-y-2">
                        <input type="file" multiple @change="(e) => form.attachments = Array.from(e.target.files)"
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-teal-500" />
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
                        class="px-6 py-3 bg-teal-600 text-white font-semibold rounded-full hover:bg-teal-700 disabled:opacity-50 transition">
                        <span v-if="loading" class="inline-flex items-center gap-2">
                            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                            Đang gửi...
                        </span>
                        <span v-else>Gửi yêu cầu</span>
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
            <h2 class="text-xl font-black text-slate-900 mb-6">Lịch sử yêu cầu</h2>

            <div v-if="requests.length === 0" class="text-center py-8">
                <Edit class="w-16 h-16 text-slate-300 mx-auto mb-4" />
                <h3 class="text-lg font-semibold text-slate-900 mb-2">Chưa có yêu cầu nào</h3>
                <p class="text-slate-600">Tạo yêu cầu đầu tiên để cập nhật thông tin cá nhân.</p>
            </div>

            <div v-else class="space-y-4">
                <div v-for="request in requests" :key="request.id"
                    class="rounded-3xl border border-slate-100 bg-slate-50 p-6">
                    <div class="flex items-start justify-between mb-4">
                        <div>
                            <h3 class="font-semibold text-slate-900 mb-1">{{ request.fieldName }}</h3>
                            <p class="text-sm text-slate-600">{{ request.reason }}</p>
                        </div>
                        <span class="px-3 py-1 rounded-full text-xs font-semibold"
                            :class="getStatusColor(request.status)">
                            {{ getStatusLabel(request.status) }}
                        </span>
                    </div>

                    <div class="grid gap-4 md:grid-cols-2 mb-4">
                        <div>
                            <p class="text-xs text-slate-500 uppercase tracking-wider mb-1">Giá trị hiện tại</p>
                            <p class="text-sm font-semibold text-slate-900">{{ request.currentValue }}</p>
                        </div>
                        <div>
                            <p class="text-xs text-slate-500 uppercase tracking-wider mb-1">Giá trị mới</p>
                            <p class="text-sm font-semibold text-slate-900">{{ request.newValue }}</p>
                        </div>
                    </div>

                    <div class="flex items-center justify-between text-sm text-slate-500">
                        <span>Đã gửi: {{ formatDate(request.submittedAt) }}</span>
                        <span v-if="request.reviewerName">
                            Duyệt bởi: {{ request.reviewerName }} ({{ formatDate(request.reviewedAt) }})
                        </span>
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