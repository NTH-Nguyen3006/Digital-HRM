<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Edit3, FileText, Send } from 'lucide-vue-next'
import EmployeeLayout from '@/components/EmployeeLayout.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import InsightCard from '@/components/hrm/InsightCard.vue'
import SurfacePanel from '@/components/hrm/SurfacePanel.vue'
import { getMyProfileChangeRequests, submitProfileChangeRequest } from '@/api/me/portal'
import { useToast } from '@/composables/useToast'
import { unwrapPage } from '@/utils/api'
import { formatDateTime } from '@/utils/format'
import { profileChangeEntries } from '@/utils/profileChange'

const toast = useToast()

const loading = ref(false)
const submitting = ref(false)
const showForm = ref(false)
const requests = ref([])

const form = reactive({
  fieldName: '',
  newValue: '',
  reason: '',
})

const fieldOptions = [
  { value: 'personalEmail', label: 'Email cá nhân', placeholder: 'Nhập email cá nhân mới' },
  { value: 'mobilePhone', label: 'Số điện thoại', placeholder: 'Nhập số điện thoại mới' },
  { value: 'avatarUrl', label: 'Ảnh đại diện', placeholder: 'Dán liên kết ảnh đại diện mới' },
  { value: 'taxCode', label: 'Mã số thuế', placeholder: 'Nhập mã số thuế mới' },
  { value: 'nationality', label: 'Quốc tịch', placeholder: 'Nhập quốc tịch cập nhật' },
  { value: 'placeOfBirth', label: 'Nơi sinh', placeholder: 'Nhập nơi sinh' },
  { value: 'ethnicGroup', label: 'Dân tộc', placeholder: 'Nhập dân tộc' },
  { value: 'religion', label: 'Tôn giáo', placeholder: 'Nhập tôn giáo' },
  { value: 'maritalStatus', label: 'Tình trạng hôn nhân', placeholder: 'SINGLE, MARRIED, DIVORCED hoặc WIDOWED' },
  { value: 'educationLevel', label: 'Học vấn', placeholder: 'Nhập trình độ học vấn' },
  { value: 'major', label: 'Chuyên ngành', placeholder: 'Nhập chuyên ngành' },
  { value: 'emergencyNote', label: 'Ghi chú khẩn cấp', placeholder: 'Nhập ghi chú khẩn cấp' },
]

const selectedField = computed(() => fieldOptions.find((item) => item.value === form.fieldName) || null)

const stats = computed(() => [
  {
    title: 'Tổng yêu cầu',
    value: requests.value.length,
    subtitle: 'Lịch sử thay đổi hồ sơ cá nhân',
    icon: FileText,
    tone: 'indigo',
  },
  {
    title: 'Đang chờ duyệt',
    value: requests.value.filter((item) => (item.requestStatus || item.status) === 'PENDING').length,
    subtitle: 'HR chưa xử lý xong',
    icon: Edit3,
    tone: 'amber',
  },
  {
    title: 'Đã duyệt',
    value: requests.value.filter((item) => (item.requestStatus || item.status) === 'APPROVED').length,
    subtitle: 'Thay đổi đã được áp dụng',
    icon: Send,
    tone: 'emerald',
  },
  {
    title: 'Bị từ chối',
    value: requests.value.filter((item) => (item.requestStatus || item.status) === 'REJECTED').length,
    subtitle: 'Cần rà soát và gửi lại',
    icon: FileText,
    tone: 'rose',
  },
])

async function fetchRequests() {
  loading.value = true
  try {
    const response = await getMyProfileChangeRequests()
    requests.value = unwrapPage(response).items
  } catch (error) {
    console.error('Failed to fetch profile change requests:', error)
    requests.value = []
    toast.error('Không thể tải lịch sử cập nhật hồ sơ')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.fieldName || !form.newValue || !form.reason) {
    toast.warning('Vui lòng nhập đủ trường bắt buộc')
    return
  }

  submitting.value = true
  try {
    await submitProfileChangeRequest({
      payload: {
        [form.fieldName]: form.newValue,
      },
      reason: form.reason,
    })

    form.fieldName = ''
    form.newValue = ''
    form.reason = ''
    showForm.value = false

    toast.success('Đã gửi yêu cầu cập nhật hồ sơ')
    await fetchRequests()
  } catch (error) {
    console.error('Failed to submit profile change request:', error)
    toast.error('Không thể gửi yêu cầu cập nhật')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchRequests)
</script>

<template>
  <EmployeeLayout>
    <div class="mx-auto max-w-5xl space-y-8 px-6 py-10">
      <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-[11px] font-black uppercase tracking-[0.2em] text-indigo-500">Profile Change Workflow</p>
          <h1 class="mt-3 text-4xl font-black tracking-tight text-slate-900">Yêu cầu cập nhật hồ sơ</h1>
          <p class="mt-2 max-w-3xl text-sm font-medium text-slate-500">
            Gửi thay đổi trực tiếp lên backend và theo dõi trạng thái duyệt của từng yêu cầu.
          </p>
        </div>

        <BaseButton variant="primary" @click="showForm = !showForm">
          <Edit3 class="mr-2 h-4 w-4" />
          {{ showForm ? 'Đóng biểu mẫu' : 'Tạo yêu cầu mới' }}
        </BaseButton>
      </div>

      <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
        <InsightCard
          v-for="item in stats"
          :key="item.title"
          :title="item.title"
          :value="item.value"
          :subtitle="item.subtitle"
          :icon="item.icon"
          :tone="item.tone"
        />
      </div>

      <SurfacePanel v-if="showForm">
        <div class="grid gap-4 md:grid-cols-2">
          <label class="block">
            <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Trường thông tin</span>
            <select v-model="form.fieldName" class="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
              <option value="">Chọn trường cần cập nhật</option>
              <option v-for="option in fieldOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
          <label class="block">
            <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Giá trị mới</span>
            <input v-model="form.newValue" type="text" class="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10" :placeholder="selectedField?.placeholder || 'Thông tin muốn cập nhật'" >
          </label>
          <label class="block md:col-span-2">
            <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Lý do thay đổi</span>
            <textarea v-model="form.reason" rows="4" class="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10" placeholder="Mô tả ngắn gọn lý do HR cần biết" />
          </label>
        </div>

        <div class="mt-5 flex flex-wrap justify-end gap-3">
          <BaseButton variant="outline" @click="showForm = false">Hủy</BaseButton>
          <BaseButton variant="primary" :loading="submitting" @click="handleSubmit">
            <Send class="mr-2 h-4 w-4" />
            Gửi yêu cầu
          </BaseButton>
        </div>
      </SurfacePanel>

      <SurfacePanel>
        <div class="mb-5">
          <h2 class="text-xl font-black text-slate-900">Lịch sử yêu cầu</h2>
          <p class="mt-1 text-sm font-medium text-slate-500">Tất cả yêu cầu được hiển thị theo luồng xét duyệt hiện có.</p>
        </div>

        <div v-if="loading" class="grid gap-4">
          <div v-for="item in 4" :key="item" class="h-32 animate-pulse rounded-[28px] bg-slate-100" />
        </div>

        <div v-else-if="requests.length" class="space-y-4">
          <article
            v-for="item in requests"
            :key="item.requestId || item.profileChangeRequestId || item.id"
            class="rounded-[28px] border border-slate-200 bg-white p-5 transition-all hover:border-indigo-200 hover:shadow-lg"
          >
            <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
              <div>
                <div class="flex flex-wrap items-center gap-2">
                  <h3 class="text-lg font-black text-slate-900">{{ item.fieldName || item.requestTitle || 'Yêu cầu cập nhật hồ sơ' }}</h3>
                  <StatusBadge :status="item.requestStatus || item.status || 'PENDING'" />
                </div>
                <p class="mt-2 text-sm font-medium text-slate-600">{{ item.reason || item.requestReason || 'Không có mô tả bổ sung.' }}</p>
              </div>
              <div class="text-sm font-medium text-slate-500">
                Gửi lúc {{ formatDateTime(item.createdAt || item.submittedAt) }}
              </div>
            </div>

            <div class="mt-4 grid gap-4 rounded-[24px] bg-slate-50 p-4 md:grid-cols-2">
              <div
                v-for="entry in profileChangeEntries(item.payloadJson)"
                :key="`${item.requestId || item.profileChangeRequestId || item.id}-${entry.field}`"
              >
                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">{{ entry.label }}</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ entry.value || 'Chưa cung cấp' }}</p>
              </div>
              <p
                v-if="!profileChangeEntries(item.payloadJson).length"
                class="md:col-span-2 text-sm font-medium text-slate-500"
              >
                Chưa đọc được dữ liệu thay đổi từ yêu cầu này.
              </p>
            </div>
          </article>
        </div>

        <EmptyState
          v-else
          iconName="FileText"
          title="Chưa có yêu cầu cập nhật hồ sơ"
          description="Khi bạn tạo yêu cầu mới, trạng thái và lịch sử xử lý sẽ hiển thị ở đây."
        />
      </SurfacePanel>
    </div>
  </EmployeeLayout>
</template>
