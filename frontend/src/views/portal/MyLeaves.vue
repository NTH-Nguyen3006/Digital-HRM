<script setup>
import { ref, onMounted } from 'vue'
import { getMyLeaves, submitLeaveRequest } from '@/api/me/portal'
import { useToast } from '@/composables/useToast'
import { Calendar, Plus, Clock, Loader2, Info } from 'lucide-vue-next'

const toast = useToast()

const leaves = ref([])
const balances = ref([])
const loading = ref(false)
const showModal = ref(false)
const submitting = ref(false)

const form = ref({
  leaveTypeId: '',
  fromDate: '',
  toDate: '',
  reason: ''
})

const fetchLeaves = async () => {
  loading.value = true
  try {
    const res = await getMyLeaves()
    // Based on PortalLeaveOverviewResponse.java
    balances.value = res.data?.balances || []
    leaves.value = res.data?.recentRequests || []
  } catch (e) {
    toast.error('Không thể tải lịch sử nghỉ phép')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.fromDate || !form.value.toDate) {
    toast.warning('Vui lòng chọn ngày nghỉ')
    return
  }
  submitting.value = true
  try {
    await submitLeaveRequest(form.value)
    toast.success('Đã gửi đơn xin nghỉ phép')
    showModal.value = false
    await fetchLeaves()
  } catch (e) {
    toast.error('Gửi đơn thất bại')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchLeaves)

const fmtDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : '—'
</script>

<template>

  <div class="space-y-6 max-w-4xl mx-auto pb-10">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-3xl font-black text-slate-900">Nghỉ phép</h2>
        <p class="text-sm font-medium text-slate-500 mt-1">Quản lý quỹ phép và đơn xin nghỉ của bạn</p>
      </div>
      <button @click="showModal = true"
        class="flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white rounded-2xl font-bold hover:bg-indigo-700 transition shadow-xl shadow-indigo-100">
        <Plus class="w-5 h-5" /> Nộp đơn mới
      </button>
    </div>

    <!-- BALANCES -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="b in balances" :key="b.leaveTypeId"
        class="p-6 rounded-4xl bg-white border border-slate-100 shadow-sm relative overflow-hidden group">
        <div
          class="absolute -right-6 -top-6 w-24 h-24 bg-indigo-50 rounded-full blur-2xl group-hover:bg-indigo-100 transition-colors">
        </div>
        <div class="relative z-10">
          <p class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-4">{{ b.leaveTypeName }}</p>
          <div class="flex items-end gap-2">
            <span class="text-4xl font-black text-slate-900">{{ b.availableUnits }}</span>
            <span class="text-sm font-bold text-slate-400 mb-1.5">ngày còn lại</span>
          </div>
          <div class="mt-4 pt-4 border-t border-slate-50 flex items-center justify-between text-[11px] font-bold">
            <span class="text-slate-400">Đã dùng: {{ b.usedUnits }}</span>
            <span class="text-indigo-600">Tổng: {{ b.totalUnits }}</span>
          </div>
        </div>
      </div>
      <div v-if="loading && !balances.length" class="lg:col-span-3 h-32 animate-pulse bg-slate-100 rounded-4xl"></div>
    </div>

    <div>
      <h3 class="text-lg font-black text-slate-900 mb-4 flex items-center gap-2">
        <Clock class="w-5 h-5 text-indigo-500" /> Lịch sử đơn nghỉ
      </h3>

      <div class="bg-white rounded-[24px] border border-slate-100 shadow-sm p-2 relative min-h-75">
        <div v-if="loading" class="absolute inset-0 flex items-center justify-center">
          <Loader2 class="w-8 h-8 text-indigo-500 animate-spin" />
        </div>

        <template v-else>
          <div v-if="leaves.length === 0" class="flex flex-col items-center justify-center py-16 text-center">
            <div class="w-16 h-16 bg-slate-50 rounded-2xl flex items-center justify-center text-slate-300 mb-4">
              <Calendar class="w-8 h-8" />
            </div>
            <p class="font-bold text-slate-700">Chưa có dữ liệu</p>
            <p class="text-sm font-medium text-slate-400">Bạn chưa tạo đơn xin nghỉ phép nào.</p>
          </div>

          <div v-else class="space-y-2">
            <div v-for="leave in leaves" :key="leave.leaveRequestId"
              class="flex items-center justify-between p-4 rounded-2xl hover:bg-slate-50 transition">
              <div class="flex items-center gap-4">
                <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center text-amber-500">
                  <Calendar class="w-5 h-5" />
                </div>
                <div>
                  <p class="font-bold text-slate-700">{{ fmtDate(leave.fromDate) }} - {{ fmtDate(leave.toDate) }}</p>
                  <p class="text-xs font-medium text-slate-400 mt-0.5">{{ leave.reason || 'Tranh thủ cá nhân' }}</p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-xs font-black uppercase tracking-wider mb-1"
                  :class="leave.status === 'APPROVED' ? 'text-emerald-500' : leave.status === 'REJECTED' ? 'text-rose-500' : 'text-amber-500'">
                  {{ leave.status }}
                </p>
                <p class="text-xs font-bold text-slate-500">{{ leave.totalDays }} ngày</p>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>

  <!-- Modal Nộp đơn -->
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="showModal = false" />
        <div class="relative bg-white rounded-[24px] shadow-2xl w-full max-w-md p-6">
          <h3 class="text-lg font-black text-slate-900 mb-5">Đơn xin nghỉ phép</h3>

          <div class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1.5 uppercase tracking-widest">Loại nghỉ</label>
              <select v-model="form.leaveTypeId"
                class="w-full px-4 py-3 bg-slate-50 border border-transparent rounded-2xl outline-none focus:bg-white focus:border-indigo-100 transition-all font-bold text-sm">
                <option value="" disabled>Chọn loại nghỉ...</option>
                <option v-for="b in balances" :key="b.leaveTypeId" :value="b.leaveTypeId">
                  {{ b.leaveTypeName }} (Còn {{ b.availableUnits }} ngày)
                </option>
              </select>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1.5 uppercase tracking-widest">Từ ngày</label>
                <input v-model="form.fromDate" type="date"
                  class="w-full px-4 py-3 bg-slate-50 border border-transparent rounded-2xl outline-none focus:bg-white focus:border-indigo-100 transition-all font-bold text-sm" />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1.5 uppercase tracking-widest">Đến
                  ngày</label>
                <input v-model="form.toDate" type="date"
                  class="w-full px-4 py-3 bg-slate-50 border border-transparent rounded-2xl outline-none focus:bg-white focus:border-indigo-100 transition-all font-bold text-sm" />
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1.5 uppercase tracking-widest">Lý do</label>
              <textarea v-model="form.reason" rows="3"
                class="w-full px-4 py-3 bg-slate-50 border border-transparent rounded-2xl outline-none focus:bg-white focus:border-indigo-100 transition-all font-bold text-sm resize-none"
                placeholder="Lý do xin nghỉ..."></textarea>
            </div>
          </div>

          <div class="mt-6 flex gap-3">
            <button @click="showModal = false"
              class="flex-1 py-2.5 rounded-xl font-bold text-slate-600 bg-slate-100 hover:bg-slate-200 transition">Hủy</button>
            <button @click="handleSubmit" :disabled="submitting"
              class="flex-1 py-2.5 rounded-xl font-bold text-white bg-indigo-600 hover:bg-indigo-700 transition flex items-center justify-center gap-2">
              <Loader2 v-if="submitting" class="w-4 h-4 animate-spin" />
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
