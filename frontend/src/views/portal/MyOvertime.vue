<script setup>
import { ref, reactive, computed } from 'vue'
import { Clock, Calendar, MessageSquare, Plus } from 'lucide-vue-next'
import { submitOvertimeRequest, getMyOvertimeRequests } from '@/api/me/portal'

const showForm = ref(false)
const loading = ref(false)
const form = reactive({
    attendance_date: '',
    overtime_start_at: '',
    overtime_end_at: '',
    requested_minutes: 0,
    reason: '',
    evidence_file_key: null
})

const requests = ref([
    {
        overtime_request_id: 1,
        request_code: 'OT-2026-001',
        attendance_date: '2026-04-15',
        overtime_start_at: '18:00:00',
        overtime_end_at: '21:00:00',
        requested_minutes: 180,
        reason: 'Hoàn thành task deadline - Fix bugs và testing cho HRM System',
        request_status: 'SUBMITTED',
        submitted_at: '2026-04-10T14:30:00Z',
        approved_at: null,
        rejected_at: null,
        manager_note: null,
        rejection_note: null
    },
    {
        overtime_request_id: 2,
        request_code: 'OT-2026-002',
        attendance_date: '2026-04-10',
        overtime_start_at: '17:30:00',
        overtime_end_at: '19:30:00',
        requested_minutes: 120,
        reason: 'Meeting khách hàng - Demo sản phẩm cho Client Project',
        request_status: 'APPROVED',
        submitted_at: '2026-04-08T09:15:00Z',
        approved_at: '2026-04-09T11:20:00Z',
        rejected_at: null,
        manager_note: 'Đã xác nhận tham gia meeting quan trọng',
        rejection_note: null
    }
])

const otReasons = [
    'Hoàn thành deadline',
    'Meeting khách hàng',
    'Support production',
    'Training/Đào tạo',
    'Fix bugs khẩn cấp',
    'Lý do khác',
    'Sự kiện công ty',
    'Khác'
]

const projects = [
    'HRM System',
    'Client Project A',
    'Client Project B',
    'Internal Tools',
    'Marketing Campaign',
    'Khác'
]

const getStatusLabel = (status) => {
    switch (status) {
        case 'SUBMITTED': return 'Đang chờ duyệt'
        case 'APPROVED': return 'Đã duyệt'
        case 'REJECTED': return 'Từ chối'
        default: return status
    }
}

const getStatusColor = (status) => {
    switch (status) {
        case 'SUBMITTED': return 'bg-yellow-100 text-yellow-800'
        case 'APPROVED': return 'bg-green-100 text-green-800'
        case 'REJECTED': return 'bg-red-100 text-red-800'
        default: return 'bg-slate-100 text-slate-800'
    }
}

const calculateMinutes = () => {
    if (form.overtime_start_at && form.overtime_end_at) {
        const start = new Date(`${form.attendance_date}T${form.overtime_start_at}`)
        const end = new Date(`${form.attendance_date}T${form.overtime_end_at}`)
        const diffMs = end - start
        form.requested_minutes = Math.max(0, Math.floor(diffMs / (1000 * 60))) // Convert to minutes
    }
}

const submitRequest = async () => {
    loading.value = true
    try {
        const payload = {
            attendance_date: form.attendance_date,
            overtime_start_at: `${form.attendance_date}T${form.overtime_start_at}:00`,
            overtime_end_at: `${form.attendance_date}T${form.overtime_end_at}:00`,
            requested_minutes: form.requested_minutes,
            reason: form.reason,
            evidence_file_key: form.evidence_file_key
        }

        const response = await submitOvertimeRequest(payload)

        // Add to local list
        requests.value.unshift({
            overtime_request_id: response.overtime_request_id || Date.now(),
            request_code: response.request_code || `OT-${Date.now()}`,
            ...payload,
            request_status: 'SUBMITTED',
            submitted_at: new Date().toISOString(),
            approved_at: null,
            rejected_at: null,
            manager_note: null,
            rejection_note: null
        })

        // Reset form
        Object.keys(form).forEach(key => {
            form[key] = ''
        })
        form.requested_minutes = 0
        showForm.value = false
    } catch (error) {
        console.error('Failed to submit overtime request:', error)
    } finally {
        loading.value = false
    }
}

const formatDate = (dateString) => {
    if (!dateString) return '—'
    return new Date(dateString).toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    })
}

const formatTime = (timeString) => {
    if (!timeString) return '—'
    // Extract time part if it's a full datetime string
    if (timeString.includes('T')) {
        return timeString.split('T')[1].substring(0, 5)
    }
    return timeString
}

const formatMinutes = (minutes) => {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return `${hours}h ${mins}m`
}
</script>

<template>
    <div class="max-w-4xl mx-auto px-6 py-8">
        <div class="mb-8">
            <h1 class="text-3xl font-black text-slate-900 mb-2">Yêu cầu làm thêm giờ</h1>
            <p class="text-slate-600">Đăng ký làm thêm giờ để được phê duyệt và tính lương</p>
        </div>

        <!-- Create Request Button -->
        <div class="mb-8">
            <button @click="showForm = !showForm"
                class="inline-flex items-center gap-2 px-6 py-3 bg-blue-600 text-white font-semibold rounded-full hover:bg-blue-700 transition">
                <Plus class="w-5 h-5" />
                Tạo yêu cầu OT
            </button>
        </div>

        <!-- Create Request Form -->
        <div v-if="showForm" class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm mb-8">
            <h2 class="text-xl font-black text-slate-900 mb-6">Thông tin làm thêm giờ</h2>

            <form @submit.prevent="submitRequest" class="space-y-6">
                <div class="grid gap-6 md:grid-cols-2">
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Ngày làm thêm</label>
                        <input v-model="form.attendance_date" type="date" :min="new Date().toISOString().split('T')[0]"
                            required
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Lý do làm thêm</label>
                        <select v-model="form.reason" required
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500">
                            <option value="">Chọn lý do</option>
                            <option v-for="reason in otReasons" :key="reason" :value="reason">
                                {{ reason }}
                            </option>
                        </select>
                    </div>
                </div>

                <div class="grid gap-6 md:grid-cols-3">
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Giờ bắt đầu</label>
                        <input v-model="form.overtime_start_at" type="time" required @change="calculateMinutes"
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Giờ kết thúc</label>
                        <input v-model="form.overtime_end_at" type="time" required @change="calculateMinutes"
                            class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">Tổng thời gian</label>
                        <input :value="formatMinutes(form.requested_minutes)" type="text" readonly
                            class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-slate-900" />
                    </div>
                </div>

                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Bằng chứng đính kèm (tùy
                        chọn)</label>
                    <input type="file" accept=".pdf,.jpg,.jpeg,.png"
                        @change="(e) => form.evidence_file_key = e.target.files[0]?.name || null"
                        class="w-full px-4 py-3 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    <p class="text-xs text-slate-500 mt-1">Chấp nhận file PDF, JPG, PNG</p>
                </div>

                <div class="flex gap-4">
                    <button type="submit" :disabled="loading"
                        class="px-6 py-3 bg-blue-600 text-white font-semibold rounded-full hover:bg-blue-700 disabled:opacity-50 transition">
                        <span v-if="loading" class="inline-flex items-center gap-2">
                            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                            Đang gửi...
                        </span>
                        <span v-else>Gửi yêu cầu OT</span>
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
            <h2 class="text-xl font-black text-slate-900 mb-6">Lịch sử yêu cầu OT</h2>

            <div v-if="requests.length === 0" class="text-center py-8">
                <Clock class="w-16 h-16 text-slate-300 mx-auto mb-4" />
                <h3 class="text-lg font-semibold text-slate-900 mb-2">Chưa có yêu cầu nào</h3>
                <p class="text-slate-600">Tạo yêu cầu làm thêm giờ khi cần thiết.</p>
            </div>

            <div v-else class="space-y-4">
                <div v-for="request in requests" :key="request.overtime_request_id"
                    class="rounded-3xl border border-slate-100 bg-slate-50 p-6">
                    <div class="flex items-start justify-between mb-4">
                        <div class="flex-1">
                            <div class="flex items-center gap-3 mb-2">
                                <Calendar class="w-5 h-5 text-slate-400" />
                                <span class="font-semibold text-slate-900">
                                    {{ formatDate(request.attendance_date) }} - {{ request.request_code }}
                                </span>
                                <span class="px-3 py-1 rounded-full text-xs font-semibold"
                                    :class="getStatusColor(request.request_status)">
                                    {{ getStatusLabel(request.request_status) }}
                                </span>
                            </div>
                            <p class="text-sm text-slate-600 mb-1">
                                <strong>Thời gian:</strong> {{ formatTime(request.overtime_start_at) }} - {{
                                    formatTime(request.overtime_end_at) }}
                                ({{ formatMinutes(request.requested_minutes) }})
                            </p>
                            <p class="text-sm text-slate-600"><strong>Lý do:</strong> {{ request.reason }}</p>
                        </div>
                    </div>

                    <div
                        class="flex items-center justify-between text-sm text-slate-500 pt-4 border-t border-slate-200">
                        <span>Đã gửi: {{ formatDate(request.submitted_at) }}</span>
                        <span v-if="request.approved_at">
                            Duyệt: {{ formatDate(request.approved_at) }}
                        </span>
                    </div>

                    <div v-if="request.manager_note" class="mt-4 pt-4 border-t border-slate-200">
                        <p class="text-xs text-slate-500 uppercase tracking-wider mb-2">Ghi chú quản lý</p>
                        <p class="text-sm text-slate-700">{{ request.manager_note }}</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>