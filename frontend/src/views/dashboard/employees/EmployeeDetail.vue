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
  Download,
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
  Upload,
  UserCheck,
  Wallet
} from 'lucide-vue-next'
import {
  createDocument,
  getEmployeeDetail,
  getEmployeeProfile,
  listAddresses,
  transferEmployee,
  listEmergencyContacts,
  listIdentifications,
  listBankAccounts,
  listDocuments,
  updateEmployee,
  updateDocument
} from '@/api/admin/employee'
import { getJobTitles } from '@/api/admin/jobTitle'
import { getOrgUnits } from '@/api/admin/orgUnit'
import { uploadStoredFile } from '@/api/storage'
import { useToast } from '@/composables/useToast'
import { unwrapData, unwrapPage } from '@/utils/api'

const authStore = useAuthStore()
const toast = useToast()
const isAdmin = authStore.isAdmin
const isHR = authStore.isHR
const canManage = isHR || isAdmin

const route = useRoute()
const router = useRouter()
const employeeId = route.params.id

const loading = ref(true)
const viewMode = ref('overview')
const activeTab = ref('profile')
const cvUploading = ref(false)
const cvInputRef = ref(null)
const referenceLoading = ref(false)
const editSubmitting = ref(false)
const transferSubmitting = ref(false)
const showEditModal = ref(false)
const showTransferModal = ref(false)

const employee = ref({})
const profile = ref({})
const addresses = ref([])
const emergencyContacts = ref([])
const identifications = ref([])
const bankAccounts = ref([])
const documents = ref([])
const orgUnitOptions = ref([])
const jobTitleOptions = ref([])

const editForm = ref(createEditForm())
const transferForm = ref(createTransferForm())

const statusMap = {
  ACTIVE: { label: 'Chính thức', chip: 'bg-emerald-50 text-emerald-700 border-emerald-200', tone: 'text-emerald-600' },
  PROBATION: { label: 'Thử việc', chip: 'bg-amber-50 text-amber-700 border-amber-200', tone: 'text-amber-600' },
  SUSPENDED: { label: 'Đình chỉ', chip: 'bg-rose-50 text-rose-700 border-rose-200', tone: 'text-rose-600' },
  RESIGNED: { label: 'Đã nghỉ', chip: 'bg-slate-100 text-slate-600 border-slate-200', tone: 'text-slate-500' }
}

function createEditForm() {
  return {
    employeeCode: '',
    orgUnitId: '',
    jobTitleId: '',
    fullName: '',
    genderCode: 'MALE',
    dateOfBirth: '',
    hireDate: '',
    workEmail: '',
    workPhone: '',
    workLocation: '',
    taxCode: '',
    personalEmail: '',
    mobilePhone: '',
    avatarUrl: '',
    note: '',
    managerEmployeeId: '',
  }
}

function createTransferForm() {
  return {
    targetOrgUnitId: '',
    note: '',
  }
}

const toNullableNumber = (value) => {
  if (value === '' || value === null || value === undefined) return null
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

const getApiErrorMessage = (error, fallback) => error?.response?.data?.message || fallback

const syncEditForm = () => {
  editForm.value = {
    employeeCode: employee.value.employeeCode || '',
    orgUnitId: employee.value.orgUnitId ? String(employee.value.orgUnitId) : '',
    jobTitleId: employee.value.jobTitleId ? String(employee.value.jobTitleId) : '',
    fullName: employee.value.fullName || '',
    genderCode: employee.value.genderCode || 'MALE',
    dateOfBirth: employee.value.dateOfBirth || '',
    hireDate: employee.value.hireDate || '',
    workEmail: employee.value.workEmail || '',
    workPhone: employee.value.workPhone || '',
    workLocation: employee.value.workLocation || '',
    taxCode: employee.value.taxCode || '',
    personalEmail: employee.value.personalEmail || '',
    mobilePhone: employee.value.mobilePhone || '',
    avatarUrl: employee.value.avatarUrl || '',
    note: employee.value.note || '',
    managerEmployeeId: employee.value.managerEmployeeId ? String(employee.value.managerEmployeeId) : '',
  }
}

const syncTransferForm = () => {
  transferForm.value = {
    targetOrgUnitId: employee.value.orgUnitId ? String(employee.value.orgUnitId) : '',
    note: '',
  }
}

const fetchReferenceData = async () => {
  referenceLoading.value = true
  try {
    const [orgUnitsRes, jobTitlesRes] = await Promise.all([
      getOrgUnits({ page: 0, size: 200, status: 'ACTIVE' }),
      getJobTitles({ page: 0, size: 200, status: 'ACTIVE' })
    ])

    orgUnitOptions.value = unwrapPage(orgUnitsRes).items
    jobTitleOptions.value = unwrapPage(jobTitlesRes).items
  } catch (error) {
    console.error('Failed to fetch employee reference data:', error)
  } finally {
    referenceLoading.value = false
  }
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
    syncEditForm()
    syncTransferForm()
  } catch (error) {
    employee.value = {}
    profile.value = {}
    addresses.value = []
    emergencyContacts.value = []
    identifications.value = []
    bankAccounts.value = []
    documents.value = []
    syncEditForm()
    syncTransferForm()
    console.error('Failed to fetch employee data:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAllData()
  fetchReferenceData()
})

const documentDownloadUrl = (document) => {
  if (!document?.storagePath) return '#'
  return `/api/v1/storage/files/${document.storagePath}/download`
}

const isCvDocument = (document) => {
  const keyword = `${document?.documentName || ''} ${document?.note || ''}`.toLowerCase()
  return keyword.includes('cv') || keyword.includes('resume') || document?.documentCategory === 'PROFILE'
}

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
const cvDocument = computed(() => documents.value.find((item) => isCvDocument(item)) || null)
const otherDocuments = computed(() => documents.value.filter((item) => item.employeeDocumentId !== cvDocument.value?.employeeDocumentId))
const profileChecklist = computed(() => [
  employee.value.workEmail,
  employee.value.mobilePhone,
  profile.value.nationality,
  addresses.value[0]?.addressLine,
  identifications.value[0]?.documentNumber,
  bankAccounts.value[0]?.accountNumber
])
const profileFilledCount = computed(() => profileChecklist.value.filter(Boolean).length)
const profileChecklistTotal = computed(() => profileChecklist.value.length || 1)
const profileCompletionPercent = computed(() => Math.round((profileFilledCount.value / profileChecklistTotal.value) * 100))

const overviewStats = computed(() => [
  {
    label: 'Mức độ hồ sơ',
    value: `${profileFilledCount.value}/${profileChecklistTotal.value}`,
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

const cvSummary = computed(() => {
  if (!cvDocument.value) {
    return {
      title: 'Chưa có CV bản mềm',
      subtitle: 'Tải CV để HR và quản lý tra cứu nhanh ngay trong hồ sơ nhân viên.',
      meta: 'Ưu tiên PDF hoặc DOCX',
    }
  }

  const sizeInMb = ((cvDocument.value.fileSizeBytes || 0) / 1024 / 1024).toFixed(2)
  return {
    title: cvDocument.value.documentName || 'CV nhân viên',
    subtitle: cvDocument.value.note || 'CV mềm đang lưu trong hồ sơ nhân viên.',
    meta: `${cvDocument.value.mimeType || 'Tài liệu'} • ${sizeInMb} MB • ${formatDate(cvDocument.value.uploadedAt)}`,
  }
})

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
      subtitle: `${item.documentCategory || 'N/A'} • ${((item.fileSizeBytes || 0) / 1024 / 1024).toFixed(2)} MB`,
      downloadUrl: documentDownloadUrl(item),
      note: item.note,
    }))
  }
}))

const activePanel = computed(() => compactPanels.value[activeTab.value] || compactPanels.value.profile)

function triggerCvUpload() {
  if (!canManage) return
  cvInputRef.value?.click()
}

async function handleCvSelected(event) {
  const [file] = Array.from(event.target.files || [])
  event.target.value = ''

  if (!file) return

  cvUploading.value = true
  try {
    const uploaded = unwrapData(await uploadStoredFile({
      file,
      moduleCode: 'EMPLOYEE',
      businessCategory: 'EMPLOYEE_CV',
      visibilityScope: 'INTERNAL',
      note: `CV cho nhân sự ${employee.value.employeeCode || employee.value.fullName || employeeId}`,
    }))

    const payload = {
      documentCategory: 'PROFILE',
      documentName: file.name,
      storagePath: uploaded.fileKey,
      mimeType: file.type || uploaded.mimeType || 'application/octet-stream',
      fileSizeBytes: file.size,
      status: 'ACTIVE',
      note: 'CV nhân viên',
    }

    if (cvDocument.value?.employeeDocumentId) {
      await updateDocument(employeeId, cvDocument.value.employeeDocumentId, payload)
      toast.success('Đã cập nhật CV bản mềm')
    } else {
      await createDocument(employeeId, payload)
      toast.success('Đã thêm CV bản mềm')
    }

    const docRes = await listDocuments(employeeId)
    documents.value = Array.isArray(docRes?.data) ? docRes.data : []
    activeTab.value = 'documents'
    viewMode.value = '360'
  } catch (error) {
    console.error('Failed to upload CV:', error)
    toast.error('Không thể tải lên CV bản mềm')
  } finally {
    cvUploading.value = false
  }
}

async function openEditModal() {
  if (!canManage) return
  if (!orgUnitOptions.value.length || !jobTitleOptions.value.length) {
    await fetchReferenceData()
  }
  syncEditForm()
  showEditModal.value = true
}

async function openTransferModal() {
  if (!canManage) return
  if (!orgUnitOptions.value.length) {
    await fetchReferenceData()
  }
  syncTransferForm()
  showTransferModal.value = true
}

async function submitEdit() {
  if (!editForm.value.employeeCode || !editForm.value.fullName || !editForm.value.orgUnitId || !editForm.value.jobTitleId || !editForm.value.dateOfBirth || !editForm.value.hireDate) {
    toast.error('Vui lòng điền đầy đủ các trường bắt buộc')
    return
  }

  editSubmitting.value = true
  try {
    await updateEmployee(employeeId, {
      employeeCode: editForm.value.employeeCode.trim(),
      orgUnitId: Number(editForm.value.orgUnitId),
      jobTitleId: Number(editForm.value.jobTitleId),
      managerEmployeeId: toNullableNumber(editForm.value.managerEmployeeId),
      fullName: editForm.value.fullName.trim(),
      workEmail: editForm.value.workEmail?.trim() || null,
      workPhone: editForm.value.workPhone?.trim() || null,
      genderCode: editForm.value.genderCode || 'MALE',
      dateOfBirth: editForm.value.dateOfBirth,
      hireDate: editForm.value.hireDate,
      workLocation: editForm.value.workLocation?.trim() || null,
      taxCode: editForm.value.taxCode?.trim() || null,
      personalEmail: editForm.value.personalEmail?.trim() || null,
      mobilePhone: editForm.value.mobilePhone?.trim() || null,
      avatarUrl: editForm.value.avatarUrl?.trim() || null,
      note: editForm.value.note?.trim() || null,
    })

    showEditModal.value = false
    await fetchAllData()
    toast.success('Đã cập nhật hồ sơ nhân sự')
  } catch (error) {
    console.error('Failed to update employee:', error)
    toast.error(getApiErrorMessage(error, 'Không thể cập nhật hồ sơ nhân sự'))
  } finally {
    editSubmitting.value = false
  }
}

async function submitTransfer() {
  if (!transferForm.value.targetOrgUnitId) {
    toast.error('Vui lòng chọn phòng ban đích')
    return
  }

  transferSubmitting.value = true
  try {
    await transferEmployee(employeeId, {
      targetOrgUnitId: Number(transferForm.value.targetOrgUnitId),
      note: transferForm.value.note?.trim() || null,
    })

    showTransferModal.value = false
    await fetchAllData()
    toast.success('Đã điều chuyển nhân sự thành công')
  } catch (error) {
    console.error('Failed to transfer employee:', error)
    toast.error(getApiErrorMessage(error, 'Không thể điều chuyển nhân sự'))
  } finally {
    transferSubmitting.value = false
  }
}
</script>

<template>
  <div v-if="loading" class="min-h-[60vh] flex flex-col items-center justify-center gap-4">
    <Loader2 class="w-12 h-12 text-indigo-600 animate-spin" />
    <p class="text-slate-500 font-bold">Đang tải hồ sơ nhân sự...</p>
  </div>

  <div v-else class="space-y-6 animate-fade-in">
    <input ref="cvInputRef" type="file" accept=".pdf,.doc,.docx,.ppt,.pptx,.txt" class="hidden"
      @change="handleCvSelected">

    <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
      <div class="flex items-center gap-2 text-sm font-medium">
        <button @click="router.push('/employees')" class="text-indigo-600 hover:underline flex items-center gap-1">
          <ArrowLeft class="w-4 h-4" /> Hồ sơ nhân sự
        </button>
        <ChevronRight class="w-4 h-4 text-slate-300" />
        <span class="text-slate-500">{{ employee.employeeCode || 'N/A' }} - {{ employee.fullName || 'Nhan su' }}</span>
      </div>

      <div class="inline-flex rounded-2xl bg-white border border-slate-200 p-1 shadow-sm">
        <button type="button" @click="viewMode = 'overview'"
          class="px-4 py-2 rounded-xl text-sm font-bold transition-all"
          :class="viewMode === 'overview' ? 'bg-indigo-600 text-white shadow-sm' : 'text-slate-500 hover:text-slate-800'">
          Tổng quan
        </button>
        <button type="button" @click="viewMode = '360'" class="px-4 py-2 rounded-xl text-sm font-bold transition-all"
          :class="viewMode === '360' ? 'bg-slate-900 text-white shadow-sm' : 'text-slate-500 hover:text-slate-800'">
          Hồ sơ 360°
        </button>
      </div>
    </div>

    <section
      class="relative overflow-hidden rounded-4xl border border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(255,255,255,0.18),_transparent_24%),linear-gradient(135deg,#0f172a_0%,#312e81_38%,#4f46e5_72%,#1d4ed8_100%)] p-5 text-white shadow-[0_18px_50px_rgba(49,46,129,0.18)] lg:p-6">
      <div class="absolute -right-12 -top-16 h-48 w-48 rounded-full bg-white/10 blur-3xl"></div>
      <div class="absolute -bottom-16 left-20 h-36 w-36 rounded-full bg-cyan-300/10 blur-3xl"></div>

      <div class="relative flex flex-col gap-6 xl:flex-row xl:items-start xl:justify-between">
        <div class="min-w-0 flex flex-col gap-4 lg:flex-row lg:items-start">
          <div v-if="employee.avatarUrl"
            class="h-20 w-20 overflow-hidden rounded-[24px] border border-white/20 shadow-xl">
            <img :src="employee.avatarUrl" class="h-full w-full object-cover" />
          </div>
          <div v-else
            class="flex h-20 w-20 items-center justify-center rounded-[24px] border border-white/20 bg-white/12 text-3xl font-black shadow-xl">
            {{ getAvatarInitial(employee.fullName) }}
          </div>

          <div class="min-w-0 space-y-4">
            <div class="flex flex-wrap items-center gap-3">
              <h1 class="max-w-full truncate text-2xl font-black tracking-tight lg:max-w-[24rem] lg:text-3xl">{{
                employee.fullName || 'Chưa có tên' }}</h1>
              <span class="rounded-full border border-white/15 bg-white/12 px-3 py-1 text-sm font-bold">
                {{ employee.employeeCode || 'N/A' }}
              </span>
              <span class="rounded-full border px-3 py-1 text-xs font-bold" :class="statusMeta.chip">
                {{ statusMeta.label }}
              </span>
            </div>

            <div class="grid gap-2 text-sm text-indigo-100 md:grid-cols-2 xl:grid-cols-4">
              <div class="min-w-0 flex items-center gap-2 rounded-2xl border border-white/10 bg-white/8 px-3 py-2">
                <Briefcase class="h-4 w-4 shrink-0" />
                <span class="truncate">{{ employee.jobTitleName || 'Chưa cập nhật chức danh' }}</span>
              </div>
              <div class="min-w-0 flex items-center gap-2 rounded-2xl border border-white/10 bg-white/8 px-3 py-2">
                <Building2 class="h-4 w-4 shrink-0" />
                <span class="truncate">{{ employee.orgUnitName || 'Chưa cập nhật phòng ban' }}</span>
              </div>
              <div class="min-w-0 flex items-center gap-2 rounded-2xl border border-white/10 bg-white/8 px-3 py-2">
                <Mail class="h-4 w-4 shrink-0" />
                <span class="truncate">{{ employee.workEmail || 'Chưa có email công ty' }}</span>
              </div>
              <div class="min-w-0 flex items-center gap-2 rounded-2xl border border-white/10 bg-white/8 px-3 py-2">
                <Phone class="h-4 w-4 shrink-0" />
                <span class="truncate">{{ employee.workPhone || 'Chưa cập nhật SDT' }}</span>
              </div>
            </div>

            <div class="rounded-2xl border border-white/10 bg-black/10 px-4 py-3 backdrop-blur-sm">
              <div class="flex items-center justify-between gap-3">
                <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-100/80">Mức độ hoàn thiện hồ sơ
                </p>
                <p class="text-sm font-black text-white">{{ profileCompletionPercent }}%</p>
              </div>
              <div class="mt-2 h-2 overflow-hidden rounded-full bg-white/20">
                <div
                  class="h-full rounded-full bg-gradient-to-r from-emerald-300 via-sky-300 to-indigo-200 transition-all duration-300"
                  :style="{ width: `${profileCompletionPercent}%` }"></div>
              </div>
              <p class="mt-2 text-xs text-indigo-100/80">
                Đã có {{ profileFilledCount }}/{{ profileChecklistTotal }} thông tin quan trọng.
              </p>
            </div>
          </div>
        </div>

        <div class="min-w-0 flex flex-col gap-3 xl:min-w-[460px]">
          <div class="grid grid-cols-2 gap-2 lg:grid-cols-4">
            <div v-for="item in overviewStats" :key="item.label"
              class="min-w-0 rounded-2xl border border-white/10 bg-white/10 px-3 py-3 backdrop-blur-sm">
              <div class="truncate text-[10px] font-black uppercase tracking-[0.12em] text-indigo-100/65">{{ item.label
                }}</div>
              <div class="mt-1 text-xl font-black leading-none">{{ item.value }}</div>
              <div class="truncate mt-1 text-[11px] leading-4 text-indigo-100/65">{{ item.hint }}</div>
            </div>
          </div>

          <div v-if="canManage" class="grid grid-cols-1 gap-2 pt-1 sm:grid-cols-3">
            <button type="button"
              class="flex items-center justify-center gap-2 rounded-2xl border border-white/15 bg-white/10 px-3 py-2.5 text-sm font-bold whitespace-nowrap transition hover:bg-white/15"
              :disabled="editSubmitting" @click="openEditModal">
              <Edit class="h-4 w-4" /> Chỉnh sửa
            </button>
            <button type="button"
              class="flex items-center justify-center gap-2 rounded-2xl border border-white/15 bg-white/10 px-3 py-2.5 text-sm font-bold whitespace-nowrap transition hover:bg-white/15"
              :disabled="transferSubmitting" @click="openTransferModal">
              <Send class="h-4 w-4" /> Điều chuyển
            </button>
            <button type="button"
              class="flex items-center justify-center gap-2 rounded-2xl border border-indigo-200/50 bg-white text-slate-800 px-3 py-2.5 text-sm font-bold whitespace-nowrap transition hover:bg-indigo-50"
              :disabled="cvUploading" @click="triggerCvUpload">
              <Upload class="h-4 w-4 text-indigo-600" />
              <span>{{ cvDocument ? 'Cập nhật CV' : 'Tải CV' }}</span>
            </button>
          </div>
        </div>
      </div>
    </section>

    <template v-if="viewMode === 'overview'">
      <section class="grid gap-6 xl:grid-cols-[1.35fr_0.95fr]">
        <div class="space-y-6">
          <div class="grid gap-6 md:grid-cols-2">
            <article v-for="card in summaryCards" :key="card.title"
              class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
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
                <div v-for="row in card.items" :key="row.label"
                  class="flex items-start justify-between gap-4 rounded-2xl bg-slate-50 px-4 py-3">
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
              <button type="button" @click="viewMode = '360'"
                class="rounded-xl bg-slate-900 px-4 py-2 text-sm font-bold text-white transition hover:bg-slate-800">
                Mở 360°
              </button>
            </div>

            <div class="grid gap-3">
              <button v-for="tab in tabs" :key="tab.key" type="button" @click="viewMode = '360'; activeTab = tab.key"
                class="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3 text-left transition hover:border-indigo-200 hover:bg-indigo-50/40">
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
            <div
              class="mb-5 rounded-[26px] border border-indigo-100 bg-[linear-gradient(135deg,#eef2ff_0%,#ffffff_65%)] p-5">
              <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                <div class="min-w-0">
                  <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">CV bản mềm</p>
                  <h3 class="mt-2 text-xl font-black text-slate-900">{{ cvSummary.title }}</h3>
                  <p class="mt-2 text-sm leading-relaxed text-slate-500">{{ cvSummary.subtitle }}</p>
                  <p class="mt-3 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{{ cvSummary.meta }}</p>
                </div>

                <div class="flex flex-wrap gap-2">
                  <a v-if="cvDocument" :href="documentDownloadUrl(cvDocument)"
                    class="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-bold text-slate-700 transition hover:border-indigo-200 hover:text-indigo-600">
                    <Download class="h-4 w-4" />
                    Tải CV
                  </a>
                  <button v-if="canManage" type="button"
                    class="inline-flex items-center gap-2 rounded-2xl bg-slate-900 px-4 py-2.5 text-sm font-bold text-white transition hover:bg-slate-800"
                    :disabled="cvUploading" @click="triggerCvUpload">
                    <Upload class="h-4 w-4" />
                    {{ cvUploading ? 'Đang tải lên...' : (cvDocument ? 'Thay CV' : 'Thêm CV') }}
                  </button>
                </div>
              </div>
            </div>

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
                <div class="mt-1 font-bold text-slate-900">{{ identifications.length ? 'Đã cập nhật' : 'Chưa có dữ liệu'
                  }}</div>
              </div>
              <div class="rounded-2xl bg-slate-50 px-4 py-3">
                <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Tài khoản ngân hàng</div>
                <div class="mt-1 font-bold text-slate-900">{{ bankAccounts.length ? 'Đã cập nhật' : 'Chưa có dữ liệu' }}
                </div>
              </div>
              <div class="rounded-2xl bg-slate-50 px-4 py-3">
                <div class="text-xs font-black uppercase tracking-[0.18em] text-slate-400">Địa chỉ chính</div>
                <div class="mt-1 font-bold text-slate-900">{{ addresses.length ? 'Đã cập nhật' : 'Chưa có dữ liệu' }}
                </div>
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
            <button v-for="tab in tabs" :key="tab.key" type="button" @click="activeTab = tab.key"
              class="flex w-full items-center justify-between rounded-2xl px-3 py-3 text-left transition-all"
              :class="activeTab === tab.key ? 'bg-slate-900 text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'">
              <div class="flex items-center gap-3">
                <div class="flex h-10 w-10 items-center justify-center rounded-2xl"
                  :class="activeTab === tab.key ? 'bg-white/10 text-white' : 'bg-slate-100 text-slate-500'">
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
            <article v-for="block in activePanel.blocks" :key="block.title"
              class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
              <h3 class="mb-5 text-lg font-black text-slate-900">{{ block.title }}</h3>
              <div class="space-y-3">
                <div v-for="item in block.items" :key="item.label"
                  class="flex items-start justify-between gap-4 rounded-2xl bg-slate-50 px-4 py-3">
                  <span class="text-sm font-bold text-slate-400">{{ item.label }}</span>
                  <span class="text-right text-sm font-black text-slate-900">{{ item.value }}</span>
                </div>
              </div>
            </article>
          </section>

          <section v-else-if="activeTab === 'documents'" class="space-y-6">
            <article class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
              <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div>
                  <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">CV nhân viên</p>
                  <h3 class="mt-2 text-2xl font-black text-slate-900">
                    {{ cvDocument ? cvDocument.documentName : 'Chưa có CV bản mềm' }}
                  </h3>
                  <p class="mt-2 text-sm font-medium text-slate-500">
                    {{ cvDocument ? (cvDocument.note || 'CV mềm đang được lưu trong hồ sơ tài liệu của nhân sự.') : 'Tải CV để hỗ trợ tuyển dụng nội bộ, đánh giá năng lực và tra cứu nhanh.' }}
                  </p>
                  <p v-if="cvDocument" class="mt-3 text-xs font-bold uppercase tracking-[0.18em] text-slate-400">
                    {{ cvDocument.mimeType || 'Tài liệu' }} • {{ ((cvDocument.fileSizeBytes || 0) / 1024 /
                    1024).toFixed(2) }} MB • {{ formatDate(cvDocument.uploadedAt) }}
                  </p>
                </div>

                <div class="flex flex-wrap gap-2">
                  <a v-if="cvDocument" :href="documentDownloadUrl(cvDocument)"
                    class="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-bold text-slate-700 transition hover:border-indigo-200 hover:text-indigo-600">
                    <Download class="h-4 w-4" />
                    Tải CV
                  </a>
                  <button v-if="canManage" type="button"
                    class="inline-flex items-center gap-2 rounded-2xl bg-indigo-600 px-4 py-2.5 text-sm font-bold text-white transition hover:bg-indigo-700"
                    :disabled="cvUploading" @click="triggerCvUpload">
                    <Upload class="h-4 w-4" />
                    {{ cvUploading ? 'Đang tải lên...' : (cvDocument ? 'Cập nhật CV' : 'Thêm CV') }}
                  </button>
                </div>
              </div>
            </article>

            <section v-if="otherDocuments.length" class="grid gap-4 md:grid-cols-2 2xl:grid-cols-3">
              <article v-for="item in otherDocuments" :key="item.employeeDocumentId"
                class="rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
                <div class="mb-4 flex items-start justify-between gap-3">
                  <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
                    <FileText class="h-5 w-5" />
                  </div>
                  <span v-if="item.status"
                    class="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] font-black uppercase tracking-wide text-slate-600">
                    {{ item.status }}
                  </span>
                </div>
                <div class="text-lg font-black text-slate-900">{{ item.documentName }}</div>
                <div class="mt-2 text-sm leading-relaxed text-slate-500">
                  {{ item.note || `${item.documentCategory || 'Tài liệu'} • ${((item.fileSizeBytes || 0) / 1024 /
                    1024).toFixed(2)} MB` }}
                </div>
                <div class="mt-4">
                  <a :href="documentDownloadUrl(item)"
                    class="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm font-bold text-slate-700 transition hover:border-indigo-200 hover:text-indigo-600">
                    <Download class="h-4 w-4" />
                    Tải tài liệu
                  </a>
                </div>
              </article>
            </section>

            <section v-else-if="!cvDocument"
              class="rounded-[28px] border border-dashed border-slate-300 bg-white px-6 py-16 text-center shadow-sm">
              <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-100 text-slate-400">
                <Files class="h-6 w-6" />
              </div>
              <h3 class="mt-4 text-lg font-black text-slate-900">Chưa có tài liệu nào</h3>
              <p class="mt-2 text-sm text-slate-500">Hiện hồ sơ này chưa có CV hoặc tài liệu bổ sung nào để hiển thị.
              </p>
            </section>
          </section>

          <section v-else-if="activePanel.cards?.length" class="grid gap-4 md:grid-cols-2 2xl:grid-cols-3">
            <article v-for="card in activePanel.cards" :key="`${activeTab}-${card.title}-${card.subtitle}`"
              class="rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
              <div class="mb-4 flex items-start justify-between gap-3">
                <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
                  <component :is="currentTab.icon" class="h-5 w-5" />
                </div>
                <span v-if="card.badge"
                  class="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] font-black uppercase tracking-wide text-slate-600">
                  {{ card.badge }}
                </span>
              </div>
              <div class="text-lg font-black text-slate-900">{{ card.title }}</div>
              <div class="mt-2 text-sm leading-relaxed text-slate-500">{{ card.subtitle }}</div>
            </article>
          </section>

          <section v-else
            class="rounded-[28px] border border-dashed border-slate-300 bg-white px-6 py-16 text-center shadow-sm">
            <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-100 text-slate-400">
              <CircleDashed class="h-6 w-6" />
            </div>
            <h3 class="mt-4 text-lg font-black text-slate-900">Chưa có dữ liệu</h3>
            <p class="mt-2 text-sm text-slate-500">Nhóm thông tin này chưa được cập nhật cho nhân sự này.</p>
          </section>
        </div>
      </section>
    </template>

    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="showEditModal" class="fixed inset-0 z-[9998]">
          <div class="fixed inset-0 bg-slate-950/60 backdrop-blur-sm" @click="showEditModal = false" />

          <div class="relative z-10 flex min-h-full items-center justify-center overflow-y-auto p-4 md:p-6">
            <div
              class="relative mx-auto my-4 flex max-h-[calc(100dvh-2rem)] w-full max-w-4xl flex-col overflow-hidden rounded-4xl bg-white shadow-2xl md:my-6">
              <div class="shrink-0 border-b border-slate-100 px-6 py-5">
                <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">Cập nhật hồ sơ</p>
                <h3 class="mt-2 text-2xl font-black text-slate-900">Chỉnh sửa thông tin nhân sự</h3>
                <p class="mt-2 text-sm font-medium text-slate-500">
                  Cập nhật nhanh các thông tin nền tảng để hồ sơ nhân viên luôn đồng bộ với dữ liệu vận hành.
                </p>
              </div>

              <div class="min-h-0 flex-1 overflow-y-auto px-6 py-6">
                <div class="grid gap-4 md:grid-cols-2">
                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Họ và tên</span>
                    <input v-model="editForm.fullName" type="text"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Mã nhân viên</span>
                    <input v-model="editForm.employeeCode" type="text"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Phòng ban</span>
                    <select v-model="editForm.orgUnitId"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                      <option value="">Chọn phòng ban</option>
                      <option v-for="item in orgUnitOptions" :key="item.orgUnitId" :value="String(item.orgUnitId)">
                        {{ item.orgUnitName }}
                      </option>
                    </select>
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Chức danh</span>
                    <select v-model="editForm.jobTitleId"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                      <option value="">Chọn chức danh</option>
                      <option v-for="item in jobTitleOptions" :key="item.jobTitleId" :value="String(item.jobTitleId)">
                        {{ item.jobTitleName }}
                      </option>
                    </select>
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Giới tính</span>
                    <select v-model="editForm.genderCode"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                      <option value="MALE">Nam</option>
                      <option value="FEMALE">Nữ</option>
                      <option value="OTHER">Khác</option>
                    </select>
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Ngày sinh</span>
                    <input v-model="editForm.dateOfBirth" type="date"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Ngày vào làm</span>
                    <input v-model="editForm.hireDate" type="date"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Email công ty</span>
                    <input v-model="editForm.workEmail" type="email"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">SĐT công ty</span>
                    <input v-model="editForm.workPhone" type="text"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Email cá nhân</span>
                    <input v-model="editForm.personalEmail" type="email"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">SĐT cá nhân</span>
                    <input v-model="editForm.mobilePhone" type="text"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Địa điểm làm việc</span>
                    <input v-model="editForm.workLocation" type="text"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2">
                    <span class="text-sm font-black text-slate-800">Mã số thuế</span>
                    <input v-model="editForm.taxCode" type="text"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2 md:col-span-2">
                    <span class="text-sm font-black text-slate-800">Avatar URL</span>
                    <input v-model="editForm.avatarUrl" type="text" placeholder="https://..."
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                  </label>

                  <label class="space-y-2 md:col-span-2">
                    <span class="text-sm font-black text-slate-800">Ghi chú</span>
                    <textarea v-model="editForm.note" rows="4"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10" />
                  </label>

                  <p v-if="referenceLoading" class="mt-4 text-xs font-medium text-slate-400">
                    Đang tải danh mục phòng ban và chức danh...
                  </p>
                </div>
              </div>

              <div class="shrink-0 border-t border-slate-100 bg-white px-6 py-5">
                <div class="flex flex-col gap-3 sm:flex-row sm:justify-end">
                  <button type="button"
                    class="rounded-2xl border border-slate-200 px-5 py-3 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
                    @click="showEditModal = false">
                    Hủy
                  </button>
                  <button type="button"
                    class="inline-flex items-center justify-center gap-2 rounded-2xl bg-slate-900 px-5 py-3 text-sm font-bold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                    :disabled="editSubmitting" @click="submitEdit">
                    <Loader2 v-if="editSubmitting" class="h-4 w-4 animate-spin" />
                    <Edit v-else class="h-4 w-4" />
                    {{ editSubmitting ? 'Đang lưu...' : 'Lưu thay đổi' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="showTransferModal" class="fixed inset-0 z-[9998]">
          <div class="fixed inset-0 bg-slate-950/60 backdrop-blur-sm" @click="showTransferModal = false" />

          <div class="relative z-10 flex min-h-full items-center justify-center overflow-y-auto p-4 md:p-6">
            <div
              class="relative mx-auto my-4 flex max-h-[calc(100dvh-2rem)] w-full max-w-xl flex-col overflow-hidden rounded-4xl bg-white shadow-2xl md:my-6">
              <div class="shrink-0 border-b border-slate-100 px-6 py-5">
                <p class="text-[11px] font-black uppercase tracking-[0.18em] text-indigo-500">Điều chuyển nhân sự</p>
                <h3 class="mt-2 text-2xl font-black text-slate-900">Chuyển nhân sự sang đơn vị mới</h3>
                <p class="mt-2 text-sm font-medium text-slate-500">
                  Thao tác này sẽ cập nhật đơn vị làm việc hiện tại của nhân viên và ghi chú lại lý do điều chuyển.
                </p>
              </div>

              <div class="min-h-0 flex-1 space-y-5 overflow-y-auto px-6 py-6">
                <label class="space-y-2">
                  <span class="text-sm font-black text-slate-800">Phòng ban đích</span>
                  <select v-model="transferForm.targetOrgUnitId"
                    class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10">
                    <option value="">Chọn phòng ban</option>
                    <option v-for="item in orgUnitOptions" :key="item.orgUnitId" :value="String(item.orgUnitId)">
                      {{ item.orgUnitName }}
                    </option>
                  </select>
                </label>

                <div class="rounded-2xl border border-indigo-100 bg-indigo-50/70 px-4 py-3 text-sm text-slate-600">
                  Đơn vị hiện tại:
                  <span class="font-black text-slate-900">{{ employee.orgUnitName || 'Chưa cập nhật' }}</span>
                </div>

                <label class="space-y-2">
                  <span class="text-sm font-black text-slate-800">Ghi chú điều chuyển</span>
                  <textarea v-model="transferForm.note" rows="4"
                    placeholder="Ví dụ: Bổ sung nguồn lực cho team sản phẩm, sắp xếp lại cơ cấu vận hành..."
                    class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-medium outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10" />
                </label>

                <p v-if="referenceLoading" class="text-xs font-medium text-slate-400">Đang tải danh mục phòng ban...</p>
              </div>

              <div class="shrink-0 border-t border-slate-100 bg-white px-6 py-5">
                <div class="flex flex-col gap-3 sm:flex-row sm:justify-end">
                  <button type="button"
                    class="rounded-2xl border border-slate-200 px-5 py-3 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
                    @click="showTransferModal = false">
                    Hủy
                  </button>
                  <button type="button"
                    class="inline-flex items-center justify-center gap-2 rounded-2xl bg-indigo-600 px-5 py-3 text-sm font-bold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
                    :disabled="transferSubmitting" @click="submitTransfer">
                    <Loader2 v-if="transferSubmitting" class="h-4 w-4 animate-spin" />
                    <Send v-else class="h-4 w-4" />
                    {{ transferSubmitting ? 'Đang điều chuyển...' : 'Xác nhận điều chuyển' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.45s ease-out forwards;
}

.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.25s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
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
