<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  ArrowLeft,
  Briefcase,
  Building2,
  CheckCircle2,
  ChevronRight,
  CircleDashed,
  CreditCard,
  Edit,
  Eye,
  FileText,
  Files,
  Loader2,
  Mail,
  MapPin,
  Phone,
  ScanFace,
  Send,
  ShieldCheck,
  UserCheck,
  Wallet
} from 'lucide-vue-next'
import {
  getEmployeeDetail,
  getEmployeeProfile,
  listAddresses,
  listEmergencyContacts,
  listIdentifications,
  listBankAccounts,
  listDocuments
} from '@/api/admin/employee'

const authStore = useAuthStore()
const isAdmin = authStore.isAdmin
const isHR = authStore.isHR
const canManage = isHR || isAdmin

const route = useRoute()
const router = useRouter()
const employeeId = route.params.id

const loading = ref(true)
const viewMode = ref('overview')
const activeTab = ref('profile')

const employee = ref({})
const profile = ref({})
const addresses = ref([])
const emergencyContacts = ref([])
const identifications = ref([])
const bankAccounts = ref([])
const documents = ref([])

const statusMap = {
  ACTIVE: { label: 'Chính thức', chip: 'bg-emerald-50 text-emerald-700 border-emerald-200', tone: 'text-emerald-600' },
  PROBATION: { label: 'Thử việc', chip: 'bg-amber-50 text-amber-700 border-amber-200', tone: 'text-amber-600' },
  SUSPENDED: { label: 'Đình chỉ', chip: 'bg-rose-50 text-rose-700 border-rose-200', tone: 'text-rose-600' },
  RESIGNED: { label: 'Đã nghỉ', chip: 'bg-slate-100 text-slate-600 border-slate-200', tone: 'text-slate-500' }
}

const fetchAllData = async () => {
  loading.value = true
  try {
    const [detailRes, profileRes, addressRes, emergencyRes, idRes, bankRes, docRes] = await Promise.all([
      getEmployeeDetail(employeeId),
      getEmployeeProfile(employeeId),
      listAddresses(employeeId),
      listEmergencyContacts(employeeId),
      listIdentifications(employeeId),
      listBankAccounts(employeeId),
      listDocuments(employeeId)
    ])

    employee.value = detailRes?.data || {}
    profile.value = profileRes?.data || {}
    addresses.value = Array.isArray(addressRes?.data) ? addressRes.data : []
    emergencyContacts.value = Array.isArray(emergencyRes?.data) ? emergencyRes.data : []
    identifications.value = Array.isArray(idRes?.data) ? idRes.data : []
    bankAccounts.value = Array.isArray(bankRes?.data) ? bankRes.data : []
    documents.value = Array.isArray(docRes?.data) ? docRes.data : []
  } catch (error) {
    employee.value = {}
    profile.value = {}
    addresses.value = []
    emergencyContacts.value = []
    identifications.value = []
    bankAccounts.value = []
    documents.value = []
    console.error('Failed to fetch employee data:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchAllData)

const getAvatarInitial = (name) => {
  if (!name) return '?'
  return name.trim().split(' ').filter(Boolean).pop()?.charAt(0)?.toUpperCase() || '?'
}

const formatDate = (dateStr) => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString('vi-VN')
}

const getGenderLabel = (genderCode) => {
  if (genderCode === 'MALE') return 'Nam'
  if (genderCode === 'FEMALE') return 'Nữ'
  if (genderCode === 'OTHER') return 'Khác'
  return 'N/A'
}

const statusMeta = computed(() => statusMap[employee.value.employmentStatus] || statusMap.ACTIVE)

const overviewStats = computed(() => [
  {
    label: 'Mức độ hồ sơ',
    value: `${[employee.value.workEmail, employee.value.mobilePhone, profile.value.nationality, addresses.value[0]?.addressLine, identifications.value[0]?.documentNumber, bankAccounts.value[0]?.accountNumber].filter(Boolean).length}/6`,
    hint: 'Độ đầy đủ thông tin'
  },
  {
    label: 'Liên hệ khẩn',
    value: emergencyContacts.value.length,
    hint: 'Người liên hệ'
  },
  {
    label: 'Giấy tờ',
    value: identifications.value.length,
    hint: 'Hồ sơ định danh'
  },
  {
    label: 'Tài liệu',
    value: documents.value.length,
    hint: 'File đang lưu'
  }
])

const summaryCards = computed(() => [
  {
    title: 'Nhân thân',
    icon: UserCheck,
    items: [
      { label: 'Họ tên', value: employee.value.fullName || 'N/A' },
      { label: 'Giới tính', value: getGenderLabel(employee.value.genderCode) },
      { label: 'Ngày sinh', value: formatDate(employee.value.dateOfBirth) },
      { label: 'Hôn nhân', value: profile.value.maritalStatus === 'MARRIED' ? 'Đã kết hôn' : profile.value.maritalStatus ? 'Độc thân' : 'N/A' }
    ]
  },
  {
    title: 'Công việc',
    icon: Briefcase,
    items: [
      { label: 'Phòng ban', value: employee.value.orgUnitName || 'N/A' },
      { label: 'Chức danh', value: employee.value.jobTitleName || 'N/A' },
      { label: 'Quản lý', value: employee.value.managerEmployeeName || 'N/A' },
      { label: 'Ngày vào làm', value: formatDate(employee.value.hireDate) }
    ]
  },
  {
    title: 'Liên hệ',
    icon: Mail,
    items: [
      { label: 'Email công ty', value: employee.value.workEmail || 'N/A' },
      { label: 'Email cá nhân', value: employee.value.personalEmail || 'N/A' },
      { label: 'SDT cong ty', value: employee.value.workPhone || 'N/A' },
      { label: 'SDT cá nhân', value: employee.value.mobilePhone || 'N/A' }
    ]
  },
  {
    title: 'Nhân sự mở rộng',
    icon: ShieldCheck,
    items: [
      { label: 'Quốc tịch', value: profile.value.nationality || 'Việt Nam' },
      { label: 'Dân tộc', value: profile.value.ethnicGroup || 'Kinh' },
      { label: 'Tôn giáo', value: profile.value.religion || 'Không' },
      { label: 'Mã số thuế', value: employee.value.taxCode || 'N/A' }
    ]
  }
])

const tabs = computed(() => [
  { key: 'profile', label: 'Tổng quan cá nhân', icon: Eye, count: null },
  { key: 'addresses', label: 'Địa chỉ', icon: MapPin, count: addresses.value.length },
  { key: 'emergency', label: 'Liên hệ khẩn', icon: Phone, count: emergencyContacts.value.length },
  { key: 'identification', label: 'Giấy tờ', icon: ScanFace, count: identifications.value.length },
  { key: 'bank', label: 'Tài khoản NH', icon: Wallet, count: bankAccounts.value.length },
  { key: 'documents', label: 'Hồ sơ tài liệu', icon: Files, count: documents.value.length }
])

const currentTab = computed(() => tabs.value.find((tab) => tab.key === activeTab.value) || tabs.value[0])

const compactPanels = computed(() => ({
  profile: {
    title: 'Ho so chinh',
    title: 'Hồ sơ chính',
    description: 'Những thông tin cần truy cập nhanh nhất để làm việc hằng ngày.',
    blocks: [
      {
        title: 'Thông tin cơ bản',
        items: [
          { label: 'Họ tên', value: employee.value.fullName || 'N/A' },
          { label: 'Giới tính', value: getGenderLabel(employee.value.genderCode) },
          { label: 'Ngày sinh', value: formatDate(employee.value.dateOfBirth) },
          { label: 'Nơi sinh', value: profile.value.placeOfBirth || 'N/A' }
        ]
      },
      {
        title: 'Thông tin công việc',
        items: [
          { label: 'Phòng ban', value: employee.value.orgUnitName || 'N/A' },
          { label: 'Chức danh', value: employee.value.jobTitleName || 'N/A' },
          { label: 'Trạng thái', value: statusMeta.value.label },
          { label: 'Địa điểm', value: employee.value.workLocation || 'N/A' }
        ]
      }
    ]
  },
  addresses: {
    title: 'Địa chỉ',
    description: 'Quản lý thường trú, tạm trú và các địa điểm liên quan.',
    cards: addresses.value.map((item) => ({
      title: item.addressType === 'PERMANENT' ? 'Thường trú' : 'Tạm trú',
      badge: item.primary ? 'Chính' : null,
      subtitle: [item.addressLine, item.wardName, item.districtName, item.provinceName].filter(Boolean).join(', ')
    }))
  },
  emergency: {
    title: 'Liên hệ khẩn cấp',
    description: 'Người có thể liên hệ ngay khi xảy ra sự cố.',
    cards: emergencyContacts.value.map((item) => ({
      title: item.contactName || 'N/A',
      badge: item.primary ? 'Chính' : item.relationshipCode,
      subtitle: [item.phoneNumber, item.email].filter(Boolean).join(' • ')
    }))
  },
  identification: {
    title: 'Giấy tờ định danh',
    description: 'CCCD, passport và các giấy tờ pháp lý quan trọng.',
    cards: identifications.value.map((item) => ({
      title: item.documentType || 'N/A',
      badge: item.status === 'ACTIVE' ? 'Còn hiệu lực' : item.status,
      subtitle: `${item.documentNumber || 'N/A'} • ${formatDate(item.expiryDate)}`
    }))
  },
  bank: {
    title: 'Tài khoản ngân hàng',
    description: 'Thông tin tài khoản chi lương và đối soát.',
    cards: bankAccounts.value.map((item) => ({
      title: item.bankName || 'N/A',
      badge: item.primary ? 'Chính' : null,
      subtitle: `${item.accountHolderName || 'N/A'} • ${item.accountNumber || 'N/A'}`
    }))
  },
  documents: {
    title: 'Hồ sơ tài liệu',
    description: 'Tất cả tài liệu đính kèm của nhân sự.',
    cards: documents.value.map((item) => ({
      title: item.documentName || 'N/A',
      badge: item.status || null,
      subtitle: `${item.documentCategory || 'N/A'} • ${((item.fileSizeBytes || 0) / 1024 / 1024).toFixed(2)} MB`
    }))
  }
}))

const activePanel = computed(() => compactPanels.value[activeTab.value] || compactPanels.value.profile)
</script>

<template>
  <div v-if="loading" class="min-h-[60vh] flex flex-col items-center justify-center gap-4">
    <Loader2 class="w-12 h-12 text-indigo-600 animate-spin" />
    <p class="text-slate-500 font-bold">Đang tải hồ sơ nhân sự...</p>
  </div>

  <div v-else class="space-y-6 animate-fade-in">
    <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
      <div class="flex items-center gap-2 text-sm font-medium">
        <button @click="router.push('/employees')" class="text-indigo-600 hover:underline flex items-center gap-1">
          <ArrowLeft class="w-4 h-4" /> Hồ sơ nhân sự
        </button>
        <ChevronRight class="w-4 h-4 text-slate-300" />
        <span class="text-slate-500">{{ employee.employeeCode || 'N/A' }} - {{ employee.fullName || 'Nhan su' }}</span>
      </div>

      <div class="inline-flex rounded-2xl bg-white border border-slate-200 p-1 shadow-sm">
        <button
          type="button"
          @click="viewMode = 'overview'"
          class="px-4 py-2 rounded-xl text-sm font-bold transition-all"
          :class="viewMode === 'overview' ? 'bg-indigo-600 text-white shadow-sm' : 'text-slate-500 hover:text-slate-800'"
        >
          Tổng quan
        </button>
        <button
          type="button"
          @click="viewMode = '360'"
          class="px-4 py-2 rounded-xl text-sm font-bold transition-all"
          :class="viewMode === '360' ? 'bg-slate-900 text-white shadow-sm' : 'text-slate-500 hover:text-slate-800'"
        >
          Hồ sơ 360°
        </button>
      </div>
    </div>

    <section class="relative overflow-hidden rounded-[32px] border border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(255,255,255,0.18),_transparent_24%),linear-gradient(135deg,#0f172a_0%,#312e81_38%,#4f46e5_72%,#1d4ed8_100%)] p-5 text-white shadow-[0_18px_50px_rgba(49,46,129,0.18)] lg:p-6">
      <div class="absolute -right-12 -top-16 h-48 w-48 rounded-full bg-white/10 blur-3xl"></div>
      <div class="absolute -bottom-16 left-20 h-36 w-36 rounded-full bg-cyan-300/10 blur-3xl"></div>

      <div class="relative flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center">
          <div v-if="employee.avatarUrl" class="h-20 w-20 overflow-hidden rounded-[24px] border border-white/20 shadow-xl">
            <img :src="employee.avatarUrl" class="h-full w-full object-cover" />
          </div>
          <div v-else class="flex h-20 w-20 items-center justify-center rounded-[24px] border border-white/20 bg-white/12 text-3xl font-black shadow-xl">
            {{ getAvatarInitial(employee.fullName) }}
          </div>

          <div class="space-y-3">
            <div class="flex flex-wrap items-center gap-3">
              <h1 class="text-2xl font-black tracking-tight lg:text-3xl">{{ employee.fullName || 'Chưa có tên' }}</h1>
              <span class="rounded-full border border-white/15 bg-white/12 px-3 py-1 text-sm font-bold">
                {{ employee.employeeCode || 'N/A' }}
              </span>
              <span class="rounded-full border px-3 py-1 text-xs font-bold" :class="statusMeta.chip">
                {{ statusMeta.label }}
              </span>
            </div>

            <div class="grid gap-2 text-sm text-indigo-100 md:grid-cols-2 xl:grid-cols-4">
              <div class="flex items-center gap-2 rounded-2xl bg-white/8 px-3 py-2">
                <Briefcase class="h-4 w-4" />
                <span>{{ employee.jobTitleName || 'Chưa cập nhật chức danh' }}</span>
              </div>
              <div class="flex items-center gap-2 rounded-2xl bg-white/8 px-3 py-2">
                <Building2 class="h-4 w-4" />
                <span>{{ employee.orgUnitName || 'Chưa cập nhật phòng ban' }}</span>
              </div>
              <div class="flex items-center gap-2 rounded-2xl bg-white/8 px-3 py-2">
                <Mail class="h-4 w-4" />
                <span class="truncate">{{ employee.workEmail || 'Chưa có email công ty' }}</span>
              </div>
              <div class="flex items-center gap-2 rounded-2xl bg-white/8 px-3 py-2">
                <Phone class="h-4 w-4" />
                <span>{{ employee.workPhone || 'Chưa cập nhật SDT' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="flex flex-col gap-3 xl:min-w-[420px]">
          <div class="grid grid-cols-2 gap-2 lg:grid-cols-4">
            <div
              v-for="item in overviewStats"
              :key="item.label"
              class="rounded-2xl border border-white/10 bg-white/10 px-3 py-3 backdrop-blur-sm"
            >
              <div class="text-[10px] font-black uppercase tracking-[0.16em] text-indigo-100/65">{{ item.label }}</div>
              <div class="mt-1 text-xl font-black leading-none">{{ item.value }}</div>
              <div class="mt-1 text-[11px] leading-4 text-indigo-100/65">{{ item.hint }}</div>
            </div>
          </div>

          <div v-if="canManage" class="flex flex-wrap gap-2 pt-1">
            <button class="flex items-center gap-2 rounded-2xl border border-white/15 bg-white/10 px-4 py-2.5 text-sm font-bold transition hover:bg-white/15">
              <Edit class="h-4 w-4" /> Chỉnh sửa
            </button>
            <button class="flex items-center gap-2 rounded-2xl border border-white/15 bg-white/10 px-4 py-2.5 text-sm font-bold transition hover:bg-white/15">
              <Send class="h-4 w-4" /> Điều chuyển
            </button>
          </div>
        </div>
      </div>
    </section>

    <template v-if="viewMode === 'overview'">
      <section class="grid gap-6 xl:grid-cols-[1.35fr_0.95fr]">
        <div class="space-y-6">
          <div class="grid gap-6 md:grid-cols-2">
            <article
              v-for="card in summaryCards"
              :key="card.title"
              class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm"
            >
              <div class="mb-5 flex items-center gap-3">
                <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
                  <component :is="card.icon" class="h-5 w-5" />
                </div>
                <div>
                  <h3 class="text-lg font-black text-slate-900">{{ card.title }}</h3>
                  <p class="text-sm text-slate-400">Thông tin cần xem nhanh</p>
                </div>
              </div>

              <div class="space-y-3">
                <div
                  v-for="row in card.items"
                  :key="row.label"
                  class="flex items-start justify-between gap-4 rounded-2xl bg-slate-50 px-4 py-3"
                >
                  <span class="text-sm font-bold text-slate-400">{{ row.label }}</span>
                  <span class="text-right text-sm font-black text-slate-900">{{ row.value }}</span>
                </div>
              </div>
            </article>
          </div>
        </div>

        <div class="space-y-6">
          <article class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
            <div class="mb-5 flex items-center justify-between">
              <div>
                <h3 class="text-lg font-black text-slate-900">Hồ sơ 360° Preview</h3>
                <p class="text-sm text-slate-400">Chuyển nhanh sang từng nhóm thông tin.</p>
              </div>
              <button
                type="button"
                @click="viewMode = '360'"
                class="rounded-xl bg-slate-900 px-4 py-2 text-sm font-bold text-white transition hover:bg-slate-800"
              >
                Mở 360°
              </button>
            </div>

            <div class="grid gap-3">
              <button
                v-for="tab in tabs"
                :key="tab.key"
                type="button"
                @click="viewMode = '360'; activeTab = tab.key"
                class="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3 text-left transition hover:border-indigo-200 hover:bg-indigo-50/40"
              >
                <div class="flex items-center gap-3">
                  <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
                    <component :is="tab.icon" class="h-4 w-4" />
                  </div>
                  <div>
                    <div class="font-black text-slate-900">{{ tab.label }}</div>
                    <div class="text-xs text-slate-400">
                      {{ tab.count === null ? 'Tổng hợp' : `${tab.count} mục dữ liệu` }}
                    </div>
                  </div>
                </div>
                <ChevronRight class="h-4 w-4 text-slate-300" />
              </button>
            </div>
          </article>

          <article class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
            <div class="mb-4 flex items-center gap-3">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600">
                <CheckCircle2 class="h-5 w-5" />
              </div>
              <div>
                <h3 class="text-lg font-black text-slate-900">Tình trạng hồ sơ</h3>
                <p class="text-sm text-slate-400">Nhận biết nhanh điểm thiếu để bổ sung.</p>
              </div>
            </div>

            <div class="space-y-3">
              <div class="rounded-2xl bg-slate-50 px-4 py-3">
                <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Email công ty</div>
                <div class="mt-1 font-bold text-slate-900">{{ employee.workEmail || 'Cần bổ sung' }}</div>
              </div>
              <div class="rounded-2xl bg-slate-50 px-4 py-3">
                <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Giấy tờ định danh</div>
                <div class="mt-1 font-bold text-slate-900">{{ identifications.length ? 'Đã cập nhật' : 'Chưa có dữ liệu' }}</div>
              </div>
              <div class="rounded-2xl bg-slate-50 px-4 py-3">
                <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Tài khoản ngân hàng</div>
                <div class="mt-1 font-bold text-slate-900">{{ bankAccounts.length ? 'Đã cập nhật' : 'Chưa có dữ liệu' }}</div>
              </div>
              <div class="rounded-2xl bg-slate-50 px-4 py-3">
                <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Địa chỉ chính</div>
                <div class="mt-1 font-bold text-slate-900">{{ addresses.length ? 'Đã cập nhật' : 'Chưa có dữ liệu' }}</div>
              </div>
            </div>
          </article>
        </div>
      </section>
    </template>

    <template v-else>
      <section class="grid gap-6 xl:grid-cols-[280px_minmax(0,1fr)]">
        <aside class="rounded-[28px] border border-slate-200 bg-white p-4 shadow-sm xl:sticky xl:top-24 xl:self-start">
          <div class="mb-4 px-2">
            <p class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Điều hướng 360°</p>
            <h3 class="mt-1 text-lg font-black text-slate-900">Xem hồ sơ theo nhóm</h3>
          </div>

          <div class="space-y-2">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              type="button"
              @click="activeTab = tab.key"
              class="flex w-full items-center justify-between rounded-2xl px-3 py-3 text-left transition-all"
              :class="activeTab === tab.key ? 'bg-slate-900 text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'"
            >
              <div class="flex items-center gap-3">
                <div
                  class="flex h-10 w-10 items-center justify-center rounded-2xl"
                  :class="activeTab === tab.key ? 'bg-white/10 text-white' : 'bg-slate-100 text-slate-500'"
                >
                  <component :is="tab.icon" class="h-4 w-4" />
                </div>
                <div>
                  <div class="font-black">{{ tab.label }}</div>
                  <div class="text-xs" :class="activeTab === tab.key ? 'text-slate-300' : 'text-slate-400'">
                    {{ tab.count === null ? 'Tổng hợp' : `${tab.count} mục` }}
                  </div>
                </div>
              </div>
              <ChevronRight class="h-4 w-4" :class="activeTab === tab.key ? 'text-slate-300' : 'text-slate-300'" />
            </button>
          </div>
        </aside>

        <div class="space-y-6">
          <section class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
            <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
              <div>
                <p class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Hồ sơ 360°</p>
                <h2 class="mt-1 text-2xl font-black text-slate-900">{{ activePanel.title }}</h2>
                <p class="mt-2 text-sm text-slate-500">{{ activePanel.description }}</p>
              </div>
              <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-500">
                Đang xem: <span class="font-black text-slate-900">{{ currentTab.label }}</span>
              </div>
            </div>
          </section>

          <section v-if="activeTab === 'profile'" class="grid gap-6 lg:grid-cols-2">
            <article
              v-for="block in activePanel.blocks"
              :key="block.title"
              class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm"
            >
              <h3 class="mb-5 text-lg font-black text-slate-900">{{ block.title }}</h3>
              <div class="space-y-3">
                <div
                  v-for="item in block.items"
                  :key="item.label"
                  class="flex items-start justify-between gap-4 rounded-2xl bg-slate-50 px-4 py-3"
                >
                  <span class="text-sm font-bold text-slate-400">{{ item.label }}</span>
                  <span class="text-right text-sm font-black text-slate-900">{{ item.value }}</span>
                </div>
              </div>
            </article>
          </section>

          <section v-else-if="activePanel.cards?.length" class="grid gap-4 md:grid-cols-2 2xl:grid-cols-3">
            <article
              v-for="card in activePanel.cards"
              :key="`${activeTab}-${card.title}-${card.subtitle}`"
              class="rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
            >
              <div class="mb-4 flex items-start justify-between gap-3">
                <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
                  <component :is="currentTab.icon" class="h-5 w-5" />
                </div>
                <span
                  v-if="card.badge"
                  class="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] font-black uppercase tracking-wide text-slate-600"
                >
                  {{ card.badge }}
                </span>
              </div>
              <div class="text-lg font-black text-slate-900">{{ card.title }}</div>
              <div class="mt-2 text-sm leading-relaxed text-slate-500">{{ card.subtitle }}</div>
            </article>
          </section>

          <section v-else class="rounded-[28px] border border-dashed border-slate-300 bg-white px-6 py-16 text-center shadow-sm">
            <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-100 text-slate-400">
              <CircleDashed class="h-6 w-6" />
            </div>
            <h3 class="mt-4 text-lg font-black text-slate-900">Chưa có dữ liệu</h3>
            <p class="mt-2 text-sm text-slate-500">Nhóm thông tin này chưa được cập nhật cho nhân sự này.</p>
          </section>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.45s ease-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
