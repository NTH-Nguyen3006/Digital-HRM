<script setup>
import { ref, computed } from 'vue'
import { Plus, Search, Eye, CheckCircle2, XCircle, Clock, ChevronRight } from 'lucide-vue-next'

const filterStatus = ref('ALL')
const showDetail = ref(false)
const selectedRequest = ref(null)
const activeTab = ref('pending') // pending | approved | rejected

const requests = ref([
  {
    id: 1,
    employeeCode: 'NV002',
    employeeName: 'Trần Thị B',
    requestDate: '01/04/2026 09:15',
    status: 'PENDING',
    changes: [
      { field: 'Số điện thoại', oldValue: '0912345678', newValue: '0901111222' },
      { field: 'Email cá nhân', oldValue: 'ttb.old@gmail.com', newValue: 'tb.new@gmail.com' },
    ],
    reason: 'Thay đổi số điện thoại và email sau khi đổi SIM',
    reviewNote: null,
    reviewedBy: null,
    reviewedAt: null,
  },
  {
    id: 2,
    employeeCode: 'NV003',
    employeeName: 'Lê Văn C',
    requestDate: '30/03/2026 14:20',
    status: 'PENDING',
    changes: [
      { field: 'Địa chỉ thường trú', oldValue: '123 Đường ABC, Q1, TP.HCM', newValue: '456 Đường XYZ, Q3, TP.HCM' },
    ],
    reason: 'Đã chuyển nhà mới',
    reviewNote: null,
    reviewedBy: null,
    reviewedAt: null,
  },
  {
    id: 3,
    employeeCode: 'NV001',
    employeeName: 'Nguyễn Văn An',
    requestDate: '28/03/2026 11:00',
    status: 'APPROVED',
    changes: [
      { field: 'Số CCCD', oldValue: '001093012300', newValue: '001093012345' },
      { field: 'Ngày cấp CCCD', oldValue: '10/05/2018', newValue: '15/08/2020' },
    ],
    reason: 'Cập nhật thông tin CCCD chip mới',
    reviewNote: 'Đã xác minh hồ sơ gốc, phê duyệt.',
    reviewedBy: 'Nguyễn Thị HR',
    reviewedAt: '29/03/2026 09:00',
  },
  {
    id: 4,
    employeeCode: 'NV005',
    employeeName: 'Phạm Thị E',
    requestDate: '27/03/2026 16:30',
    status: 'REJECTED',
    changes: [
      { field: 'Họ và Tên', oldValue: 'Phạm Thị E', newValue: 'Phạm Thị Em' },
    ],
    reason: 'Cải chính lỗi chính tả',
    reviewNote: 'Cần xuất trình giấy khai sinh để xác nhận. Vui lòng cung cấp thêm tài liệu.',
    reviewedBy: 'Nguyễn Thị HR',
    reviewedAt: '28/03/2026 08:45',
  },
])

const filteredRequests = computed(() => {
  const statusMap = { pending: 'PENDING', approved: 'APPROVED', rejected: 'REJECTED' }
  return requests.value.filter(r => r.status === statusMap[activeTab.value])
})

const stats = computed(() => ({
  pending: requests.value.filter(r => r.status === 'PENDING').length,
  approved: requests.value.filter(r => r.status === 'APPROVED').length,
  rejected: requests.value.filter(r => r.status === 'REJECTED').length,
}))

const viewRequest = (req) => {
  selectedRequest.value = req
  showDetail.value = true
}

const approveRequest = (req) => {
  req.status = 'APPROVED'
  req.reviewedBy = 'Nguyễn Thị HR'
  req.reviewedAt = new Date().toLocaleDateString('vi-VN')
  req.reviewNote = 'Đã xác nhận và phê duyệt.'
  showDetail.value = false
}

const rejectRequest = (req) => {
  req.status = 'REJECTED'
  req.reviewedBy = 'Nguyễn Thị HR'
  req.reviewedAt = new Date().toLocaleDateString('vi-VN')
  req.reviewNote = 'Từ chối - cần bổ sung tài liệu xác nhận.'
  showDetail.value = false
}
</script>

<template>
  
    <div class="space-y-6">
      <!-- Header -->
      <div>
        <h2 class="text-3xl font-black text-slate-900 tracking-tight">Yêu cầu thay đổi hồ sơ</h2>
        <p class="text-slate-500 font-medium mt-1">Duyệt các yêu cầu chỉnh sửa thông tin cá nhân từ nhân viên</p>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-3 gap-4">
        <div
          class="bg-amber-50 border border-amber-100 rounded-2xl p-5 flex items-center gap-4 cursor-pointer hover:shadow-md transition-shadow"
          @click="activeTab = 'pending'">
          <div class="w-12 h-12 bg-amber-400 rounded-2xl flex items-center justify-center text-white">
            <Clock class="w-6 h-6" />
          </div>
          <div>
            <div class="text-3xl font-black text-amber-700">{{ stats.pending }}</div>
            <div class="text-amber-600 font-bold text-sm">Chờ duyệt</div>
          </div>
        </div>
        <div
          class="bg-emerald-50 border border-emerald-100 rounded-2xl p-5 flex items-center gap-4 cursor-pointer hover:shadow-md transition-shadow"
          @click="activeTab = 'approved'">
          <div class="w-12 h-12 bg-emerald-500 rounded-2xl flex items-center justify-center text-white">
            <CheckCircle2 class="w-6 h-6" />
          </div>
          <div>
            <div class="text-3xl font-black text-emerald-700">{{ stats.approved }}</div>
            <div class="text-emerald-600 font-bold text-sm">Đã duyệt</div>
          </div>
        </div>
        <div
          class="bg-rose-50 border border-rose-100 rounded-2xl p-5 flex items-center gap-4 cursor-pointer hover:shadow-md transition-shadow"
          @click="activeTab = 'rejected'">
          <div class="w-12 h-12 bg-rose-500 rounded-2xl flex items-center justify-center text-white">
            <XCircle class="w-6 h-6" />
          </div>
          <div>
            <div class="text-3xl font-black text-rose-700">{{ stats.rejected }}</div>
            <div class="text-rose-600 font-bold text-sm">Từ chối</div>
          </div>
        </div>
      </div>

      <!-- Tab Navigation -->
      <div class="flex gap-1 bg-white border border-slate-100 rounded-2xl p-1.5 shadow-sm w-fit">
        <button v-for="t in [{ k: 'pending', l: '⏳ Chờ duyệt' }, { k: 'approved', l: '✅ Đã duyệt' }, { k: 'rejected', l: '❌ Từ chối' }]"
          :key="t.k" @click="activeTab = t.k" class="px-5 py-2.5 rounded-xl font-bold text-sm transition-all"
          :class="activeTab === t.k ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-500 hover:bg-slate-50'">
          {{ t.l }}
        </button>
      </div>

      <!-- Request List -->
      <div class="space-y-4">
        <div v-if="filteredRequests.length === 0"
          class="bg-white rounded-3xl border border-slate-100 shadow-sm p-12 text-center">
          <div class="text-6xl mb-4">📋</div>
          <div class="text-lg font-bold text-slate-600">Không có yêu cầu nào</div>
          <p class="text-slate-400 font-medium mt-2">Tất cả yêu cầu đã được xử lý hoặc chưa có yêu cầu mới.</p>
        </div>

        <div v-for="req in filteredRequests" :key="req.id"
          class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6 hover:shadow-md transition-shadow cursor-pointer"
          @click="viewRequest(req)">
          <div class="flex items-start justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-2xl text-white flex items-center justify-center font-bold text-lg shadow-md"
                :class="req.status === 'PENDING' ? 'bg-amber-400' : req.status === 'APPROVED' ? 'bg-emerald-500' : 'bg-rose-500'">
                {{ req.employeeName.split(' ').pop().charAt(0) }}
              </div>
              <div>
                <div class="font-black text-slate-900">{{ req.employeeName }}</div>
                <div class="text-sm text-slate-400 font-medium">{{ req.employeeCode }} · Gửi lúc {{ req.requestDate }}
                </div>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <span class="px-3 py-1.5 rounded-xl text-xs font-bold ring-1 ring-inset" :class="req.status === 'PENDING' ? 'bg-amber-50 text-amber-700 ring-amber-600/20'
                : req.status === 'APPROVED' ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20'
                  : 'bg-rose-50 text-rose-700 ring-rose-600/20'">
                {{ req.status === 'PENDING' ? '⏳ Chờ duyệt' : req.status === 'APPROVED' ? '✅ Đã duyệt' : '❌ Từ chối' }}
              </span>
              <ChevronRight class="w-5 h-5 text-slate-300" />
            </div>
          </div>

          <!-- Changes preview -->
          <div class="bg-slate-50 rounded-2xl p-4 mb-4">
            <p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">Các trường thay đổi ({{
              req.changes.length }})</p>
            <div class="space-y-2">
              <div v-for="change in req.changes" :key="change.field" class="flex flex-wrap items-center gap-2 text-sm">
                <span class="font-bold text-slate-700 bg-white px-2 py-0.5 rounded-lg border border-slate-200">{{
                  change.field }}</span>
                <span class="text-slate-400 font-mono text-xs">{{ change.oldValue }}</span>
                <span class="text-slate-400">→</span>
                <span class="text-indigo-700 font-bold font-mono text-xs bg-indigo-50 px-2 py-0.5 rounded-lg">{{
                  change.newValue }}</span>
              </div>
            </div>
          </div>

          <div class="flex items-center justify-between">
            <p class="text-sm text-slate-500 font-medium italic flex-1 truncate">"{{ req.reason }}"</p>
            <div v-if="req.status === 'PENDING'" class="flex gap-2 ml-4" @click.stop>
              <button @click="approveRequest(req)"
                class="flex items-center gap-1.5 px-4 py-2 bg-emerald-500 text-white rounded-xl font-bold text-sm hover:bg-emerald-600 transition-colors shadow-sm">
                <CheckCircle2 class="w-4 h-4" /> Duyệt
              </button>
              <button @click="rejectRequest(req)"
                class="flex items-center gap-1.5 px-4 py-2 bg-rose-500 text-white rounded-xl font-bold text-sm hover:bg-rose-600 transition-colors shadow-sm">
                <XCircle class="w-4 h-4" /> Từ chối
              </button>
            </div>
          </div>

          <!-- Review info for decided requests -->
          <div v-if="req.reviewedBy" class="mt-3 pt-3 border-t border-slate-100">
            <p class="text-xs text-slate-400 font-medium">
              {{ req.status === 'APPROVED' ? '✅ Đã duyệt' : '❌ Từ chối' }} bởi
              <span class="font-bold text-slate-600">{{ req.reviewedBy }}</span>
              lúc {{ req.reviewedAt }}
              <span class="text-slate-400"> · "{{ req.reviewNote }}"</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  
</template>
