<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import EmployeeLayout from '@/components/EmployeeLayout.vue'
import {
  Phone,
  Mail,
  MapPin,
  ShieldCheck,
  FileText,
  Edit2,
  Plus,
  UploadCloud,
  Download,
  Trash2,
  Building2,
  Briefcase,
  ChevronDown,
  Loader2,
  X
} from 'lucide-vue-next'
import BaseButton from '@/components/common/BaseButton.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { getPortalProfile, submitProfileChangeRequest } from '@/api/me/portal'
import { useToast } from '@/composables/useToast'

/* --- STATE --- */
const router = useRouter()
const toast = useToast()
const activeTab = ref('general')
const loading = ref(true)
const submitting = ref(false)
const activeEditor = ref('')

const tabs = [
  { key: 'general', label: 'Tổng quan' },
  { key: 'job', label: 'Công việc' },
  { key: 'payroll', label: 'Lương & Thưởng' },
  { key: 'documents', label: 'Tài liệu' },
  { key: 'setting', label: 'Cài đặt' }
]

const portalProfile = ref(null)
const personalForm = reactive({
  personalEmail: '',
  mobilePhone: '',
  maritalStatus: '',
  nationality: '',
  personalTaxId: '',
  healthInsurance: '',
  socialInsurance: '',
  reason: '',
})
const addressForm = reactive({
  primaryAddress: '',
  country: '',
  stateProvince: '',
  city: '',
  postCode: '',
  reason: '',
})

const PERSONAL_FIELDS = [
  { key: 'personalEmail', label: 'Email cá nhân', source: 'employee', type: 'email' },
  { key: 'mobilePhone', label: 'Số điện thoại', source: 'employee', type: 'text' },
  { key: 'maritalStatus', label: 'Tình trạng hôn nhân', source: 'profile', type: 'text' },
  { key: 'nationality', label: 'Quốc tịch', source: 'profile', type: 'text' },
  { key: 'personalTaxId', label: 'Mã số thuế', source: 'profile', type: 'text' },
  { key: 'healthInsurance', label: 'Bảo hiểm y tế', source: 'profile', type: 'text' },
  { key: 'socialInsurance', label: 'Bảo hiểm xã hội', source: 'profile', type: 'text' },
]

const ADDRESS_FIELDS = [
  { key: 'primaryAddress', label: 'Địa chỉ chi tiết', type: 'text' },
  { key: 'country', label: 'Quốc gia', type: 'text' },
  { key: 'stateProvince', label: 'Tỉnh / Thành phố', type: 'text' },
  { key: 'city', label: 'Quận / Huyện', type: 'text' },
  { key: 'postCode', label: 'Mã bưu chính', type: 'text' },
]

const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await getPortalProfile()
    portalProfile.value = res.data
  } catch (error) {
    console.error('Failed to fetch portal profile:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchProfile)

const documents = ref([
  { id: 1, name: 'CV_NhanVien.pdf', category: 'Document', uploadedAt: '25/04/2023', size: '2.4 MB' },
])

/* --- COMPUTED --- */
const empData = computed(() => portalProfile.value?.employee || {})
const proData = computed(() => portalProfile.value?.profile || {})
const compData = computed(() => portalProfile.value?.compensation || {})
const avatarLetter = computed(() => empData.value.fullName?.split(' ').pop()?.[0] || 'C')
const profileStatusLabel = computed(() => empData.value.employmentStatus === 'ACTIVE' ? 'Đang làm việc' : 'Đã nghỉ')
const isEditorOpen = computed(() => activeEditor.value === 'personal' || activeEditor.value === 'address')
const editorTitle = computed(() => activeEditor.value === 'address' ? 'Cập nhật địa chỉ liên hệ' : 'Cập nhật thông tin cá nhân')
const editorFields = computed(() => activeEditor.value === 'address' ? ADDRESS_FIELDS : PERSONAL_FIELDS)
const activeForm = computed(() => activeEditor.value === 'address' ? addressForm : personalForm)

/* --- METHODS --- */
const formatDate = (value) => {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value || 0)
}

const syncFormsFromProfile = () => {
  for (const field of PERSONAL_FIELDS) {
    personalForm[field.key] = field.source === 'employee'
      ? (empData.value?.[field.key] ?? '')
      : (proData.value?.[field.key] ?? '')
  }
  personalForm.reason = ''

  for (const field of ADDRESS_FIELDS) {
    addressForm[field.key] = proData.value?.[field.key] ?? ''
  }
  addressForm.reason = ''
}

const openEditor = (type) => {
  syncFormsFromProfile()
  activeEditor.value = type
}

const closeEditor = () => {
  activeEditor.value = ''
}

const buildPayload = (fields, form, source) => {
  return fields.reduce((payload, field) => {
    const currentValue = source(field.key)
    const nextValue = (form[field.key] ?? '').toString().trim()
    const normalizedCurrent = (currentValue ?? '').toString().trim()

    if (nextValue !== normalizedCurrent) {
      payload[field.key] = nextValue
    }

    return payload
  }, {})
}

const submitEditor = async () => {
  const reason = activeForm.value.reason?.trim()
  if (!reason) {
    toast.warning('Vui lòng nhập lý do cập nhật để HR dễ xử lý')
    return
  }

  const payload = activeEditor.value === 'address'
    ? buildPayload(ADDRESS_FIELDS, addressForm, (key) => proData.value?.[key])
    : buildPayload(
      PERSONAL_FIELDS,
      personalForm,
      (key) => (PERSONAL_FIELDS.find((item) => item.key === key)?.source === 'employee'
        ? empData.value?.[key]
        : proData.value?.[key])
    )

  if (!Object.keys(payload).length) {
    toast.warning('Bạn chưa thay đổi trường nào để gửi cập nhật')
    return
  }

  submitting.value = true
  try {
    await submitProfileChangeRequest({ payload, reason })
    toast.success('Đã gửi yêu cầu cập nhật hồ sơ cho HR')
    closeEditor()
    await fetchProfile()
  } catch (error) {
    console.error('Failed to submit profile change request:', error)
    toast.error('Không thể gửi yêu cầu cập nhật hồ sơ')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <EmployeeLayout>
    <div class="max-w-350 mx-auto px-4 py-8 md:px-8 md:py-12">
      <div class="grid gap-8 grid-cols-1 xl:grid-cols-[360px_1fr]">

        <aside class="space-y-6">

          <div class="rounded-4xl border border-slate-200 bg-white shadow-sm overflow-hidden sticky top-8">

            <div class="bg-linear-to-br from-slate-900 via-indigo-900 to-indigo-800 px-8 py-10 text-white">
              <div class="flex items-start gap-5">
                <div
                  class="flex h-24 w-24 shrink-0 items-center justify-center rounded-3xl bg-white/10 text-4xl font-black text-white shadow-inner border border-white/20 backdrop-blur-sm">
                  {{ avatarLetter }}
                </div>
                <div class="flex-1">
                  <p class="text-[10px] font-bold uppercase tracking-[0.35em] text-indigo-200">Hồ sơ Nhân sự</p>
                  <h1 class="mt-3 text-2xl font-black leading-tight text-white">{{ empData.fullName }}</h1>
                  <p class="mt-1.5 text-sm font-medium text-indigo-100">{{ empData.jobTitleName }}</p>
                  <span
                    class="mt-4 inline-flex items-center gap-1.5 rounded-full bg-emerald-500/20 border border-emerald-500/30 px-3 py-1.5 text-xs font-bold uppercase tracking-wider text-emerald-300">
                    <ShieldCheck class="w-3.5 h-3.5" /> {{ profileStatusLabel }}
                  </span>
                </div>
              </div>
            </div>

            <div class="p-6 space-y-5">
              <div
                class="rounded-2xl border border-slate-100 bg-slate-50 p-5 hover:border-indigo-100 hover:bg-indigo-50/30 transition-colors">
                <p class="text-[11px] font-bold uppercase tracking-[0.25em] text-slate-400 mb-4">Thông tin liên hệ</p>
                <div class="space-y-4 text-sm text-slate-700">
                  <div class="flex items-center gap-3">
                    <div
                      class="w-8 h-8 rounded-full bg-white border border-slate-200 flex items-center justify-center shadow-sm shrink-0">
                      <Mail class="w-4 h-4 text-slate-500" />
                    </div>
                    <div class="min-w-0">
                      <p class="text-slate-400 text-[10px] font-bold uppercase tracking-wider">Email công việc</p>
                      <p class="font-semibold text-slate-900 truncate">{{ empData.workEmail }}</p>
                    </div>
                  </div>
                  <div class="flex items-center gap-3">
                    <div
                      class="w-8 h-8 rounded-full bg-white border border-slate-200 flex items-center justify-center shadow-sm shrink-0">
                      <Phone class="w-4 h-4 text-slate-500" />
                    </div>
                    <div>
                      <p class="text-slate-400 text-[10px] font-bold uppercase tracking-wider">Điện thoại</p>
                      <p class="font-semibold text-slate-900">{{ empData.workPhone }}</p>
                    </div>
                  </div>
                  <div class="flex items-center gap-3">
                    <div
                      class="w-8 h-8 rounded-full bg-white border border-slate-200 flex items-center justify-center shadow-sm shrink-0">
                      <MapPin class="w-4 h-4 text-slate-500" />
                    </div>
                    <div>
                      <p class="text-slate-400 text-[10px] font-bold uppercase tracking-wider">Nơi làm việc</p>
                      <p class="font-semibold text-slate-900">{{ empData.workLocation }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div
                class="rounded-2xl border border-slate-100 bg-slate-50 p-5 hover:border-indigo-100 hover:bg-indigo-50/30 transition-colors">
                <p class="text-[11px] font-bold uppercase tracking-[0.25em] text-slate-400 mb-4">Trực thuộc Tổ chức</p>
                <div class="space-y-4 text-sm text-slate-700">
                  <div>
                    <p class="text-slate-400 text-[10px] font-bold uppercase tracking-wider flex items-center gap-1.5">
                      <Briefcase class="w-3 h-3" /> Phòng ban
                    </p>
                    <p class="font-semibold text-slate-900 mt-0.5">{{ empData.orgUnitName }}</p>
                  </div>
                  <div>
                    <p class="text-slate-400 text-[10px] font-bold uppercase tracking-wider flex items-center gap-1.5">
                      <Building2 class="w-3 h-3" /> Quản lý trực tiếp
                    </p>
                    <div class="flex items-center gap-2 mt-1.5">
                      <img :src="empData.managerAvatar" class="w-6 h-6 rounded-full border border-slate-200" />
                      <p class="font-semibold text-slate-900">{{ empData.managerEmployeeName }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3 text-sm text-slate-700">
                <div class="rounded-2xl bg-white p-4 border border-slate-200 shadow-sm text-center">
                  <p class="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1">Mã NV</p>
                  <p class="font-bold text-indigo-600">{{ empData.employeeCode }}</p>
                </div>
                <div class="rounded-2xl bg-white p-4 border border-slate-200 shadow-sm text-center">
                  <p class="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1">Cấp bậc</p>
                  <p class="font-bold text-indigo-600">{{ empData.jobLevelCode }}</p>
                </div>
              </div>

            </div>
          </div>
        </aside>

        <main class="min-w-0 flex flex-col">

          <div class="flex gap-8 border-b border-slate-200 overflow-x-auto hide-scrollbar mb-8">
            <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
              class="pb-4 text-sm font-bold whitespace-nowrap transition-all relative outline-none focus:outline-none group"
              :class="activeTab === tab.key ? 'text-indigo-600' : 'text-slate-500 hover:text-slate-900'">
              {{ tab.label }}
              <div class="absolute bottom-0 left-0 right-0 h-0.75 rounded-t-full transition-all duration-300"
                :class="activeTab === tab.key ? 'bg-indigo-600 opacity-100' : 'bg-transparent opacity-0 group-hover:bg-slate-200 group-hover:opacity-100'">
              </div>
            </button>
          </div>

          <section v-if="activeTab === 'general'"
            class="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-300">
            <GlassCard padding="p-8" class="rounded-4xl">
              <div class="flex items-center justify-between gap-4 mb-8">
                <div>
                  <p class="text-[10px] font-bold uppercase tracking-[0.3em] text-slate-400">Lý lịch trích ngang</p>
                  <h2 class="mt-2 text-xl font-black text-slate-900">Thông tin Cá nhân</h2>
                </div>
                <div class="flex items-center gap-3">
                  <BaseButton variant="ghost" size="sm" @click="router.push('/portal/profile-change')">Lịch sử
                  </BaseButton>
                  <BaseButton variant="outline" size="sm" :icon="Edit2" @click="openEditor('personal')">Cập nhật
                  </BaseButton>
                </div>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-y-6 gap-x-8">
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Họ và tên</p>
                  <p class="text-sm font-bold text-slate-900">{{ empData.fullName }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Giới tính</p>
                  <p class="text-sm font-bold text-slate-900">{{ empData.genderCode }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Ngày sinh</p>
                  <p class="text-sm font-bold text-slate-900">{{ formatDate(empData.dateOfBirth) }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Tình trạng hôn nhân</p>
                  <p class="text-sm font-bold text-slate-900">{{ proData.maritalStatus }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Email cá nhân</p>
                  <p class="text-sm font-bold text-slate-900">{{ empData.personalEmail }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Quốc tịch</p>
                  <p class="text-sm font-bold text-slate-900">{{ proData.nationality }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Bảo hiểm Y tế</p>
                  <p class="text-sm font-bold text-slate-900">{{ proData.healthInsurance }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Bảo hiểm Xã hội</p>
                  <p class="text-sm font-bold text-slate-900">{{ proData.socialInsurance }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Mã số thuế (TNCN)</p>
                  <p class="text-sm font-bold text-slate-900">{{ proData.personalTaxId }}</p>
                </div>
              </div>
            </GlassCard>

            <GlassCard padding="p-8" class="rounded-4xl">
              <div class="flex items-center justify-between gap-4 mb-8">
                <div>
                  <p class="text-[10px] font-bold uppercase tracking-[0.3em] text-slate-400">Chỗ ở hiện tại</p>
                  <h2 class="mt-2 text-xl font-black text-slate-900">Địa chỉ Liên hệ</h2>
                </div>
                <BaseButton variant="outline" size="sm" :icon="Edit2" @click="openEditor('address')">Sửa địa chỉ
                </BaseButton>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-y-6 gap-x-8">
                <div class="space-y-1 md:col-span-2 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Địa chỉ chi tiết</p>
                  <p class="text-sm font-bold text-slate-900">{{ proData.primaryAddress }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Quốc gia</p>
                  <p class="text-sm font-bold text-slate-900">{{ proData.country }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Tỉnh / Thành phố</p>
                  <p class="text-sm font-bold text-slate-900">{{ proData.stateProvince }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Quận / Huyện</p>
                  <p class="text-sm font-bold text-slate-900">{{ proData.city }}</p>
                </div>
                <div class="space-y-1 border-b border-slate-100 pb-3">
                  <p class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Mã bưu chính</p>
                  <p class="text-sm font-bold text-slate-900">{{ proData.postCode }}</p>
                </div>
              </div>
            </GlassCard>
          </section>

          <section v-if="activeTab === 'job'" class="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-300">
            <GlassCard padding="p-8" class="rounded-4xl">
              <div class="flex items-center justify-between mb-6">
                <h2 class="text-xl font-black text-slate-900">Lịch sử Công tác</h2>
                <BaseButton variant="secondary" size="sm" :icon="Plus">Thêm sự kiện</BaseButton>
              </div>

              <div class="overflow-x-auto rounded-2xl border border-slate-200">
                <table class="w-full text-left border-collapse whitespace-nowrap">
                  <thead
                    class="bg-slate-50 border-b border-slate-200 uppercase tracking-wider text-slate-500 text-[11px] font-bold">
                    <tr>
                      <th class="px-6 py-4">Ngày hiệu lực</th>
                      <th class="px-6 py-4">Chức danh</th>
                      <th class="px-6 py-4">Phòng ban</th>
                      <th class="px-6 py-4">Hình thức</th>
                      <th class="px-6 py-4 text-right">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody class="text-sm text-slate-700">
                    <tr class="hover:bg-slate-50/50 transition-colors">
                      <td class="px-6 py-4 font-semibold text-slate-900">{{ formatDate(empData.hireDate) }}</td>
                      <td class="px-6 py-4 text-indigo-600 font-semibold">{{ empData.jobTitleName }}</td>
                      <td class="px-6 py-4">{{ empData.orgUnitName }}</td>
                      <td class="px-6 py-4"><span class="px-2 py-1 bg-slate-100 rounded-md text-xs font-medium">Toàn
                          thời
                          gian</span></td>
                      <td class="px-6 py-4 text-right">
                        <BaseButton variant="ghost" size="sm" :icon="Edit2" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </GlassCard>

            <GlassCard padding="p-8" class="rounded-4xl">
              <div class="flex items-center justify-between mb-6">
                <h2 class="text-xl font-black text-slate-900">Lịch sử Hợp đồng</h2>
                <BaseButton variant="secondary" size="sm" :icon="Plus">Tạo Hợp đồng</BaseButton>
              </div>

              <div class="overflow-x-auto rounded-2xl border border-slate-200">
                <table class="w-full text-left border-collapse whitespace-nowrap">
                  <thead
                    class="bg-slate-50 border-b border-slate-200 uppercase tracking-wider text-slate-500 text-[11px] font-bold">
                    <tr>
                      <th class="px-6 py-4">Mã Hợp đồng</th>
                      <th class="px-6 py-4">Loại Hợp đồng</th>
                      <th class="px-6 py-4">Ngày bắt đầu</th>
                      <th class="px-6 py-4">Ngày kết thúc</th>
                      <th class="px-6 py-4 text-right">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody class="text-sm text-slate-700">
                    <tr class="hover:bg-slate-50/50 transition-colors">
                      <td class="px-6 py-4 font-semibold text-slate-900">HDLD-2023-0501</td>
                      <td class="px-6 py-4">Hợp đồng KXD thời hạn</td>
                      <td class="px-6 py-4">{{ formatDate(empData.hireDate) }}</td>
                      <td class="px-6 py-4 text-slate-400">—</td>
                      <td class="px-6 py-4 text-right">
                        <BaseButton variant="ghost" size="sm" :icon="Download" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </GlassCard>
          </section>

          <section v-if="activeTab === 'payroll'"
            class="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-300">
            <GlassCard padding="p-8" class="rounded-4xl bg-indigo-50/30 border-indigo-100">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-xs font-bold uppercase tracking-widest text-indigo-500 mb-2">Gói thu nhập đãi ngộ</p>
                  <p class="text-4xl font-black text-slate-900">{{ formatCurrency(compData.total) }} <span
                      class="text-base font-medium text-slate-500">/ tháng</span></p>
                </div>
              </div>
            </GlassCard>

            <GlassCard padding="p-8" class="rounded-4xl space-y-6">
              <div class="flex items-center justify-between pb-6 border-b border-slate-100">
                <div>
                  <p class="text-sm font-semibold text-slate-900">Mức lương Cơ bản (Gross)</p>
                  <p class="text-xs text-slate-500 mt-1">Dùng để đóng BHXH</p>
                </div>
                <span class="text-xl font-bold text-slate-900">{{ formatCurrency(compData.salary) }}</span>
              </div>

              <div class="flex items-center justify-between pb-6 border-b border-slate-100 group cursor-pointer">
                <div>
                  <p class="text-sm font-semibold text-slate-900">Phụ cấp Cố định (Recurring)</p>
                  <p class="text-xs text-slate-500 mt-1">Trợ cấp ăn trưa, xăng xe</p>
                </div>
                <div class="flex items-center gap-4">
                  <span class="text-xl font-bold text-slate-900">{{ formatCurrency(compData.recurring) }}</span>
                  <BaseButton variant="ghost" size="sm" :icon="ChevronDown" />
                </div>
              </div>

              <div class="flex items-center justify-between group cursor-pointer">
                <div>
                  <p class="text-sm font-semibold text-slate-900">Các khoản Khấu trừ mặc định</p>
                </div>
                <div class="flex items-center gap-4">
                  <span class="text-xl font-bold text-slate-900">{{ formatCurrency(compData.offset) }}</span>
                  <BaseButton variant="ghost" size="sm" :icon="ChevronDown" />
                </div>
              </div>
            </GlassCard>
          </section>

          <section v-if="activeTab === 'documents'"
            class="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-300">
            <GlassCard padding="p-8" class="rounded-4xl">
              <div class="flex items-center justify-between mb-8">
                <h2 class="text-xl font-black text-slate-900">Hồ sơ cá nhân & Chứng từ</h2>
              </div>

              <div
                class="border-2 border-dashed border-indigo-200 rounded-3xl bg-indigo-50/30 p-10 text-center hover:bg-indigo-50 hover:border-indigo-400 transition-colors cursor-pointer mb-8">
                <div
                  class="w-16 h-16 bg-white rounded-2xl shadow-sm flex items-center justify-center mx-auto mb-4 text-indigo-500">
                  <UploadCloud class="w-8 h-8" />
                </div>
                <p class="text-slate-900 font-bold text-lg mb-1">Tải tệp đính kèm lên</p>
                <p class="text-slate-500 text-sm mb-6">Hỗ trợ PDF, DOCX, JPG (Tối đa 10MB)</p>
                <BaseButton variant="secondary" class="shadow-sm">Chọn File</BaseButton>
              </div>

              <div class="space-y-3">
                <div v-for="doc in documents" :key="doc.id"
                  class="flex items-center justify-between p-4 rounded-2xl border border-slate-200 hover:shadow-md transition-all bg-white">
                  <div class="flex items-center gap-4">
                    <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0"
                      :class="doc.category === 'Payslip' ? 'bg-emerald-50 text-emerald-600' : 'bg-indigo-50 text-indigo-600'">
                      <FileText class="w-6 h-6" />
                    </div>
                    <div>
                      <p class="text-sm font-bold text-slate-900">{{ doc.name }}</p>
                      <p class="text-xs font-medium text-slate-500 mt-1 uppercase tracking-wider">{{ doc.size }} • Cập
                        nhật {{ doc.uploadedAt }}</p>
                    </div>
                  </div>
                  <div class="flex items-center gap-2">
                    <BaseButton variant="ghost" size="sm" :icon="Download" class="text-indigo-600" />
                    <BaseButton variant="ghost" size="sm" :icon="Trash2" class="text-rose-500" />
                  </div>
                </div>
              </div>
            </GlassCard>
          </section>

          <section v-if="activeTab === 'setting'"
            class="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-300">
            <GlassCard padding="p-8" class="rounded-4xl">
              <h2 class="text-xl font-black text-slate-900 mb-8">Tùy chọn Cổng thông tin (Portal)</h2>

              <div
                class="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-slate-100">
                <div>
                  <p class="text-sm font-bold text-slate-900">Múi giờ làm việc</p>
                  <p class="text-xs text-slate-500 mt-1">Hệ thống chấm công sẽ dựa theo múi giờ này</p>
                </div>
                <BaseButton variant="secondary" size="sm" class="shrink-0 font-medium">GMT +07:00 (Hồ Chí Minh)
                </BaseButton>
              </div>

              <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 pt-6">
                <div>
                  <p class="text-sm font-bold text-slate-900">Bảo mật ngày sinh</p>
                  <p class="text-xs text-slate-500 mt-1">Ai có thể thấy sinh nhật của bạn trên Calendar?</p>
                </div>
                <div class="relative w-full md:w-56 shrink-0">
                  <select
                    class="w-full appearance-none rounded-xl border border-slate-200 py-2.5 pl-4 pr-10 text-sm font-semibold text-slate-900 bg-slate-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 cursor-pointer">
                    <option>Toàn Công ty (Mặc định)</option>
                    <option>Chỉ Quản lý trực tiếp</option>
                    <option>Chỉ mình tôi (Ẩn)</option>
                  </select>
                  <ChevronDown
                    class="w-4 h-4 text-slate-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>
              </div>
            </GlassCard>
          </section>

        </main>
      </div>
    </div>

    <div v-if="isEditorOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-slate-950/50 backdrop-blur-sm" @click="closeEditor" />
      <div
        class="relative z-10 w-full max-w-3xl overflow-hidden rounded-4xl border border-slate-200 bg-white shadow-[0_25px_90px_rgba(15,23,42,0.18)]">
        <div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5 sm:px-8">
          <div>
            <p class="text-[11px] font-black uppercase tracking-[0.2em] text-indigo-500">Profile Update Request</p>
            <h3 class="mt-2 text-2xl font-black text-slate-900">{{ editorTitle }}</h3>
            <p class="mt-2 text-sm font-medium text-slate-500">
              Thay đổi sẽ được gửi cho HR duyệt trước khi cập nhật vào hồ sơ chính thức.
            </p>
          </div>
          <button class="rounded-2xl p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
            @click="closeEditor">
            <X class="h-5 w-5" />
          </button>
        </div>

        <div class="max-h-[70vh] overflow-y-auto px-6 py-6 sm:px-8">
          <div class="grid gap-5 md:grid-cols-2">
            <label v-for="field in editorFields" :key="field.key" class="block"
              :class="field.key === 'primaryAddress' ? 'md:col-span-2' : ''">
              <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">{{ field.label }}</span>
              <input v-model="activeForm[field.key]" :type="field.type || 'text'"
                class="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10"
                :placeholder="`Nhập ${field.label.toLowerCase()}`">
            </label>

            <label class="block md:col-span-2">
              <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Lý do cập nhật</span>
              <textarea v-model="activeForm.reason" rows="4"
                class="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10"
                placeholder="Ví dụ: cần cập nhật thông tin liên hệ mới để HR đồng bộ hồ sơ" />
            </label>
          </div>
        </div>

        <div class="flex flex-wrap justify-end gap-3 border-t border-slate-100 px-6 py-5 sm:px-8">
          <BaseButton variant="outline" @click="closeEditor">Hủy</BaseButton>
          <BaseButton variant="primary" :loading="submitting" @click="submitEditor">
            <Loader2 v-if="submitting" class="mr-2 h-4 w-4 animate-spin" />
            Gửi yêu cầu cập nhật
          </BaseButton>
        </div>
      </div>
    </div>
  </EmployeeLayout>
</template>

<style scoped>
/* Ẩn scrollbar ngang cho thanh Tabs trên Mobile/Tablet */
.hide-scrollbar::-webkit-scrollbar {
  display: none;
}

.hide-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
