<script setup>
import { ref, reactive } from 'vue'
import { FileText, Download, Calendar, Building, User, Clock } from 'lucide-vue-next'

const loading = ref(false)
const contracts = ref([
    {
        id: 1,
        contractNumber: 'HDLD-2026-001',
        contractType: 'Hợp đồng lao động chính thức',
        startDate: '2026-01-01',
        endDate: '2028-12-31',
        position: 'Nhân viên phát triển phần mềm',
        department: 'Công nghệ thông tin',
        salary: 15000000,
        status: 'ACTIVE',
        signedAt: '2026-01-01T08:00:00Z',
        appendices: [
            {
                id: 1,
                title: 'Phụ lục lương tháng 4/2026',
                type: 'Lương',
                effectiveDate: '2026-04-01',
                signedAt: '2026-04-01T08:00:00Z'
            },
            {
                id: 2,
                title: 'Phụ lục thay đổi chức vụ',
                type: 'Chức vụ',
                effectiveDate: '2026-03-15',
                signedAt: '2026-03-15T08:00:00Z'
            }
        ]
    },
    {
        id: 2,
        contractNumber: 'HDLD-2025-045',
        contractType: 'Hợp đồng thử việc',
        startDate: '2025-10-01',
        endDate: '2025-12-31',
        position: 'Thực tập sinh',
        department: 'Công nghệ thông tin',
        salary: 8000000,
        status: 'EXPIRED',
        signedAt: '2025-10-01T08:00:00Z',
        appendices: []
    }
])

const getStatusLabel = (status) => {
    switch (status) {
        case 'ACTIVE': return 'Đang hiệu lực'
        case 'EXPIRED': return 'Đã hết hạn'
        case 'TERMINATED': return 'Đã chấm dứt'
        default: return status
    }
}

const getStatusColor = (status) => {
    switch (status) {
        case 'ACTIVE': return 'bg-green-100 text-green-800'
        case 'EXPIRED': return 'bg-slate-100 text-slate-800'
        case 'TERMINATED': return 'bg-red-100 text-red-800'
        default: return 'bg-slate-100 text-slate-800'
    }
}

const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount)
}

const formatDate = (dateString) => {
    if (!dateString) return '—'
    return new Date(dateString).toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    })
}

const downloadContract = async (contractId, type = 'main') => {
    loading.value = true
    try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000))

        // In real implementation, this would trigger a file download
        console.log(`Downloading ${type} contract ${contractId}`)
    } catch (error) {
        console.error('Failed to download contract:', error)
    } finally {
        loading.value = false
    }
}

const downloadAppendix = async (contractId, appendixId) => {
    loading.value = true
    try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000))

        // In real implementation, this would trigger a file download
        console.log(`Downloading appendix ${appendixId} for contract ${contractId}`)
    } catch (error) {
        console.error('Failed to download appendix:', error)
    } finally {
        loading.value = false
    }
}
</script>

<template>
    <div class="max-w-4xl mx-auto px-6 py-8">
        <div class="mb-8">
            <h1 class="text-3xl font-black text-slate-900 mb-2">Hợp đồng lao động</h1>
            <p class="text-slate-600">Xem và tải xuống các hợp đồng lao động của bạn</p>
        </div>

        <!-- Important Notice -->
        <div class="rounded-4xl border border-blue-200 bg-blue-50 p-6 mb-8">
            <div class="flex items-start gap-3">
                <FileText class="w-6 h-6 text-blue-600 mt-0.5" />
                <div>
                    <h3 class="font-semibold text-blue-800 mb-2">Thông tin hợp đồng</h3>
                    <ul class="text-sm text-blue-700 space-y-1">
                        <li>• Tất cả hợp đồng lao động được lưu trữ an toàn và bảo mật</li>
                        <li>• Bạn có thể tải xuống hợp đồng và phụ lục bất cứ lúc nào</li>
                        <li>• Liên hệ phòng nhân sự nếu cần thay đổi thông tin hợp đồng</li>
                    </ul>
                </div>
            </div>
        </div>

        <!-- Contracts List -->
        <div class="space-y-6">
            <div v-for="contract in contracts" :key="contract.id"
                class="rounded-4xl border border-slate-200 bg-white p-8 shadow-sm">
                <!-- Contract Header -->
                <div class="flex items-start justify-between mb-6">
                    <div class="flex-1">
                        <div class="flex items-center gap-3 mb-2">
                            <FileText class="w-6 h-6 text-slate-400" />
                            <h3 class="text-xl font-black text-slate-900">{{ contract.contractNumber }}</h3>
                            <span class="px-3 py-1 rounded-full text-xs font-semibold"
                                :class="getStatusColor(contract.status)">
                                {{ getStatusLabel(contract.status) }}
                            </span>
                        </div>
                        <p class="text-slate-600">{{ contract.contractType }}</p>
                    </div>

                    <button @click="downloadContract(contract.id)" :disabled="loading"
                        class="inline-flex items-center gap-2 px-4 py-2 bg-teal-600 text-white font-semibold rounded-full hover:bg-teal-700 disabled:opacity-50 transition">
                        <Download class="w-4 h-4" />
                        Tải hợp đồng
                    </button>
                </div>

                <!-- Contract Details -->
                <div class="grid gap-6 md:grid-cols-2 mb-6">
                    <div class="space-y-4">
                        <div class="flex items-center gap-3">
                            <Calendar class="w-5 h-5 text-slate-400" />
                            <div>
                                <p class="text-sm text-slate-500">Thời hạn hợp đồng</p>
                                <p class="font-semibold text-slate-900">
                                    {{ formatDate(contract.startDate) }} - {{ formatDate(contract.endDate) }}
                                </p>
                            </div>
                        </div>

                        <div class="flex items-center gap-3">
                            <User class="w-5 h-5 text-slate-400" />
                            <div>
                                <p class="text-sm text-slate-500">Chức vụ</p>
                                <p class="font-semibold text-slate-900">{{ contract.position }}</p>
                            </div>
                        </div>

                        <div class="flex items-center gap-3">
                            <Building class="w-5 h-5 text-slate-400" />
                            <div>
                                <p class="text-sm text-slate-500">Phòng ban</p>
                                <p class="font-semibold text-slate-900">{{ contract.department }}</p>
                            </div>
                        </div>
                    </div>

                    <div class="space-y-4">
                        <div class="flex items-center gap-3">
                            <div class="w-5 h-5 bg-green-100 rounded-full flex items-center justify-center">
                                <span class="text-green-600 font-bold text-sm">₫</span>
                            </div>
                            <div>
                                <p class="text-sm text-slate-500">Mức lương</p>
                                <p class="font-semibold text-slate-900">{{ formatCurrency(contract.salary) }}/tháng</p>
                            </div>
                        </div>

                        <div class="flex items-center gap-3">
                            <Clock class="w-5 h-5 text-slate-400" />
                            <div>
                                <p class="text-sm text-slate-500">Ngày ký</p>
                                <p class="font-semibold text-slate-900">{{ formatDate(contract.signedAt) }}</p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Appendices -->
                <div v-if="contract.appendices.length > 0" class="border-t border-slate-200 pt-6">
                    <h4 class="text-lg font-bold text-slate-900 mb-4">Phụ lục hợp đồng</h4>

                    <div class="space-y-3">
                        <div v-for="appendix in contract.appendices" :key="appendix.id"
                            class="flex items-center justify-between p-4 bg-slate-50 rounded-2xl">
                            <div class="flex-1">
                                <h5 class="font-semibold text-slate-900">{{ appendix.title }}</h5>
                                <p class="text-sm text-slate-600">{{ appendix.type }} • Hiệu lực từ {{
                                    formatDate(appendix.effectiveDate) }}</p>
                            </div>

                            <button @click="downloadAppendix(contract.id, appendix.id)" :disabled="loading"
                                class="inline-flex items-center gap-2 px-3 py-2 bg-slate-200 text-slate-700 font-semibold rounded-full hover:bg-slate-300 disabled:opacity-50 transition">
                                <Download class="w-4 h-4" />
                                Tải xuống
                            </button>
                        </div>
                    </div>
                </div>

                <div v-else class="border-t border-slate-200 pt-6">
                    <p class="text-slate-500 text-center py-4">Không có phụ lục nào cho hợp đồng này</p>
                </div>
            </div>
        </div>

        <!-- Empty State -->
        <div v-if="contracts.length === 0"
            class="rounded-4xl border border-slate-200 bg-white p-16 shadow-sm text-center">
            <FileText class="w-20 h-20 text-slate-300 mx-auto mb-6" />
            <h3 class="text-xl font-bold text-slate-900 mb-2">Chưa có hợp đồng nào</h3>
            <p class="text-slate-600">Thông tin hợp đồng sẽ hiển thị sau khi bạn hoàn tất thủ tục tuyển dụng.</p>
        </div>
    </div>
</template>