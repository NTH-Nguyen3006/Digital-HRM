<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  Settings, Bell, Shield, Monitor,
  Save, RefreshCw, ChevronRight, Loader2, Edit
} from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import {
  getPlatformSettings,
  updatePlatformSetting,
  getNotificationTemplates,
  createNotificationTemplate,
  updateNotificationTemplate,
} from '@/api/admin/systemConfig'

const toast = useToast()

const activeSection = ref('platform')
const loading = ref(false)
const saving = ref(false)
const templateSaving = ref(false)
const showTemplateEditor = ref(false)
const templateEditorMode = ref('edit')

const platformSettings = ref([])
const editablePlatformSettings = ref([])
const notificationTemplates = ref([])
const templateForm = ref(null)

const templatePresets = [
  {
    templateCode: 'ONBOARDING_WELCOME_EMAIL',
    templateName: 'Email chào mừng nhân sự mới',
    channelCode: 'EMAIL',
    subjectTemplate: 'Digital HRM - Chào mừng ${fullName} gia nhập ${companyName}',
    bodyTemplate: 'Xin chào ${fullName},\n\nChào mừng bạn gia nhập ${companyName}. Ngày làm việc đầu tiên của bạn là ${plannedStartDate} tại ${workLocation}.\n\nQuản lý trực tiếp: ${managerName}\nPhòng ban: ${orgUnitName}\nChức danh: ${jobTitleName}\n\nVui lòng phản hồi email này nếu cần hỗ trợ thêm trước ngày nhận việc.\n\nTrân trọng,\n${companyName}',
    status: 'ACTIVE',
    description: 'Gửi trước ngày onboard cho nhân sự mới.',
  },
  {
    templateCode: 'LEAVE_REQUEST_APPROVED',
    templateName: 'Thông báo duyệt đơn nghỉ phép',
    channelCode: 'EMAIL',
    subjectTemplate: 'Digital HRM - Đơn nghỉ phép ${requestCode} đã được duyệt',
    bodyTemplate: 'Xin chào ${fullName},\n\nĐơn nghỉ phép ${requestCode} từ ${startDate} đến ${endDate} đã được ${approvedByName} phê duyệt.\n\nGhi chú phê duyệt: ${approvalNote}\n\nBạn có thể kiểm tra lại chi tiết trên cổng nhân viên.\n\nTrân trọng,\nDigital HRM',
    status: 'ACTIVE',
    description: 'Thông báo khi đơn nghỉ phép được duyệt.',
  },
  {
    templateCode: 'LEAVE_REQUEST_REJECTED',
    templateName: 'Thông báo từ chối đơn nghỉ phép',
    channelCode: 'EMAIL',
    subjectTemplate: 'Digital HRM - Đơn nghỉ phép ${requestCode} chưa được chấp thuận',
    bodyTemplate: 'Xin chào ${fullName},\n\nĐơn nghỉ phép ${requestCode} của bạn hiện chưa được chấp thuận.\n\nLý do: ${rejectionNote}\nNgười xử lý: ${reviewedByName}\n\nVui lòng cập nhật lại thông tin nếu cần gửi yêu cầu mới.\n\nTrân trọng,\nDigital HRM',
    status: 'ACTIVE',
    description: 'Thông báo khi đơn nghỉ phép bị từ chối.',
  },
  {
    templateCode: 'OT_REQUEST_APPROVED',
    templateName: 'Thông báo duyệt tăng ca',
    channelCode: 'EMAIL',
    subjectTemplate: 'Digital HRM - Yêu cầu tăng ca ${requestCode} đã được duyệt',
    bodyTemplate: 'Xin chào ${fullName},\n\nYêu cầu tăng ca ${requestCode} vào ngày ${attendanceDate} đã được phê duyệt.\n\nThời gian tăng ca: ${overtimeStartAt} - ${overtimeEndAt}\nSố phút: ${requestedMinutes}\n\nTrân trọng,\nDigital HRM',
    status: 'ACTIVE',
    description: 'Thông báo khi yêu cầu OT được phê duyệt.',
  },
  {
    templateCode: 'PAYROLL_PUBLISHED',
    templateName: 'Thông báo phát hành phiếu lương',
    channelCode: 'EMAIL',
    subjectTemplate: 'Digital HRM - Phiếu lương kỳ ${periodCode} đã sẵn sàng',
    bodyTemplate: 'Xin chào ${fullName},\n\nPhiếu lương kỳ ${periodCode} đã được phát hành trên cổng nhân viên.\n\nThu nhập thực nhận: ${netPay}\nPhòng ban: ${orgUnitName}\n\nVui lòng đăng nhập hệ thống để xem chi tiết.\n\nTrân trọng,\nDigital HRM',
    status: 'ACTIVE',
    description: 'Thông báo khi phiếu lương được publish.',
  },
  {
    templateCode: 'OFFBOARDING_CONFIRMATION',
    templateName: 'Xác nhận tiếp nhận offboarding',
    channelCode: 'EMAIL',
    subjectTemplate: 'Digital HRM - Đã tiếp nhận yêu cầu nghỉ việc ${offboardingCode}',
    bodyTemplate: 'Xin chào ${fullName},\n\nHệ thống đã tiếp nhận yêu cầu nghỉ việc ${offboardingCode} của bạn.\n\nNgày đề xuất nghỉ việc cuối cùng: ${requestedLastWorkingDate}\nLý do: ${requestReason}\n\nHR sẽ cập nhật các bước tiếp theo qua email và portal.\n\nTrân trọng,\nDigital HRM',
    status: 'ACTIVE',
    description: 'Thông báo xác nhận đã tạo hồ sơ offboarding.',
  },
]

const sections = [
  { key: 'platform', label: 'Cấu hình nền tảng', icon: Settings },
  { key: 'security', label: 'Bảo mật & Chính sách', icon: Shield },
  { key: 'notifications', label: 'Thông báo tự động', icon: Bell },
  { key: 'templates', label: 'Mẫu email thông báo', icon: Monitor },
]

const securityKeywords = ['auth', 'password', 'security', 'session', 'login', 'mfa']

const dirtySettingIds = computed(() => {
  return editablePlatformSettings.value
    .filter((edited) => {
      const original = platformSettings.value.find((item) => item.platformSettingId === edited.platformSettingId)
      if (!original || !edited.editable) return false
      return JSON.stringify({
        settingName: edited.settingName,
        settingValue: edited.settingValue,
        valueType: edited.valueType,
        editable: edited.editable,
        status: edited.status,
        description: edited.description,
      }) !== JSON.stringify({
        settingName: original.settingName,
        settingValue: original.settingValue,
        valueType: original.valueType,
        editable: original.editable,
        status: original.status,
        description: original.description,
      })
    })
    .map((item) => item.platformSettingId)
})

const securitySettings = computed(() => {
  return editablePlatformSettings.value.filter((item) =>
    securityKeywords.some((keyword) => item.settingKey?.toLowerCase().includes(keyword)),
  )
})

const notificationStats = computed(() => ({
  total: notificationTemplates.value.length,
  active: notificationTemplates.value.filter((item) => item.status === 'ACTIVE').length,
  email: notificationTemplates.value.filter((item) => item.channelCode === 'EMAIL').length,
}))

function cloneSetting(setting) {
  return {
    ...setting,
    settingValue: setting.settingValue ?? '',
    description: setting.description ?? '',
  }
}

function cloneTemplate(template) {
  return {
    ...template,
    templateCode: template.templateCode ?? '',
    templateName: template.templateName ?? '',
    channelCode: template.channelCode ?? 'EMAIL',
    subjectTemplate: template.subjectTemplate ?? '',
    bodyTemplate: template.bodyTemplate ?? '',
    status: template.status ?? 'ACTIVE',
    description: template.description ?? '',
  }
}

function createEmptyTemplate() {
  return {
    notificationTemplateId: null,
    templateCode: '',
    templateName: '',
    channelCode: 'EMAIL',
    subjectTemplate: '',
    bodyTemplate: '',
    status: 'ACTIVE',
    description: '',
  }
}

function formatValueType(valueType) {
  const labels = {
    STRING: 'Chuỗi',
    BOOLEAN: 'Boolean',
    NUMBER: 'Số',
    JSON: 'JSON',
  }
  return labels[valueType] || valueType
}

function templateStatusClass(status) {
  return status === 'ACTIVE'
    ? 'bg-emerald-50 text-emerald-700'
    : 'bg-slate-100 text-slate-500'
}

function parseSettingValue(setting) {
  if (setting.valueType === 'BOOLEAN') {
    return String(setting.settingValue).toLowerCase() === 'true'
  }
  return setting.settingValue ?? ''
}

function normalizeSettingValue(setting) {
  if (setting.valueType === 'BOOLEAN') {
    return String(Boolean(setting.settingValue))
  }
  if (setting.valueType === 'NUMBER') {
    return setting.settingValue === '' || setting.settingValue === null ? null : String(setting.settingValue)
  }
  return setting.settingValue ?? ''
}

async function fetchSettings() {
  loading.value = true
  try {
    const [settingsRes, templatesRes] = await Promise.all([
      getPlatformSettings(),
      getNotificationTemplates(),
    ])

    platformSettings.value = settingsRes.data || []
    editablePlatformSettings.value = platformSettings.value.map(cloneSetting)
    notificationTemplates.value = (templatesRes.data || []).map(cloneTemplate)
  } catch (error) {
    toast.error('Không thể tải cấu hình hệ thống')
  } finally {
    loading.value = false
  }
}

async function saveSettings() {
  const changedSettings = editablePlatformSettings.value.filter((item) =>
    dirtySettingIds.value.includes(item.platformSettingId),
  )

  if (!changedSettings.length) {
    await fetchSettings()
    toast.info('Đã làm mới dữ liệu cấu hình')
    return
  }

  saving.value = true
  try {
    await Promise.all(changedSettings.map((item) => updatePlatformSetting(item.platformSettingId, {
      settingName: item.settingName,
      settingValue: normalizeSettingValue(item),
      valueType: item.valueType,
      editable: item.editable,
      status: item.status,
      description: item.description || null,
    })))
    toast.success(`Đã lưu ${changedSettings.length} tham số nền tảng`)
    await fetchSettings()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Lưu cấu hình thất bại')
  } finally {
    saving.value = false
  }
}

function openTemplateEditor(template) {
  templateEditorMode.value = 'edit'
  templateForm.value = cloneTemplate(template)
  showTemplateEditor.value = true
}

function openTemplateCreator(preset = null) {
  templateEditorMode.value = 'create'
  templateForm.value = preset ? cloneTemplate(preset) : createEmptyTemplate()
  showTemplateEditor.value = true
}

async function saveTemplate() {
  if (!templateForm.value) return

  templateSaving.value = true
  try {
    const payload = {
      templateCode: templateForm.value.templateCode,
      templateName: templateForm.value.templateName,
      channelCode: templateForm.value.channelCode,
      subjectTemplate: templateForm.value.subjectTemplate || null,
      bodyTemplate: templateForm.value.bodyTemplate,
      status: templateForm.value.status,
      description: templateForm.value.description || null,
    }

    if (templateEditorMode.value === 'create') {
      await createNotificationTemplate(payload)
      toast.success('Đã tạo mẫu thông báo mới')
    } else {
      await updateNotificationTemplate(templateForm.value.notificationTemplateId, payload)
      toast.success('Đã cập nhật mẫu thông báo')
    }

    showTemplateEditor.value = false
    await fetchSettings()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Lưu mẫu thông báo thất bại')
  } finally {
    templateSaving.value = false
  }
}

onMounted(fetchSettings)
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <h2 class="text-3xl font-black tracking-tight text-slate-900">Cài đặt hệ thống</h2>
        <p class="mt-1 font-medium text-slate-500">Đổ dữ liệu cấu hình nền tảng và mẫu thông báo từ backend</p>
      </div>
      <button
        :disabled="saving || loading"
        @click="saveSettings"
        class="flex items-center gap-2 rounded-xl px-5 py-2.5 font-bold text-white shadow-lg transition-all disabled:opacity-60"
        :class="dirtySettingIds.length
          ? 'bg-indigo-600 shadow-indigo-200 hover:bg-indigo-700'
          : 'bg-slate-500 shadow-slate-200 hover:bg-slate-600'"
      >
        <component :is="saving ? Loader2 : dirtySettingIds.length ? Save : RefreshCw" class="h-5 w-5" :class="saving ? 'animate-spin' : ''" />
        {{ saving ? 'Đang lưu...' : dirtySettingIds.length ? `Lưu ${dirtySettingIds.length} thay đổi` : 'Làm mới dữ liệu' }}
      </button>
    </div>

    <div class="grid gap-6 lg:grid-cols-4">
      <div class="h-fit rounded-3xl border border-slate-100 bg-white p-4 shadow-sm">
        <nav class="space-y-1">
          <button
            v-for="sec in sections"
            :key="sec.key"
            @click="activeSection = sec.key"
            class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-bold transition-all"
            :class="activeSection === sec.key
              ? 'bg-indigo-600 text-white shadow-md shadow-indigo-200'
              : 'text-slate-500 hover:bg-slate-50 hover:text-slate-700'"
          >
            <component :is="sec.icon" class="h-5 w-5 shrink-0" />
            {{ sec.label }}
            <ChevronRight v-if="activeSection === sec.key" class="ml-auto h-4 w-4 opacity-60" />
          </button>
        </nav>
      </div>

      <div class="space-y-6 lg:col-span-3">
        <div v-if="loading" class="flex min-h-[360px] items-center justify-center rounded-3xl border border-slate-100 bg-white shadow-sm">
          <Loader2 class="h-10 w-10 animate-spin text-indigo-600" />
        </div>

        <template v-else>
          <div v-if="activeSection === 'platform'" class="space-y-4">
            <div
              v-for="setting in editablePlatformSettings"
              :key="setting.platformSettingId"
              class="rounded-3xl border border-slate-100 bg-white p-6 shadow-sm"
            >
              <div class="mb-4 flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                <div>
                  <h3 class="text-lg font-black text-slate-900">{{ setting.settingName }}</h3>
                  <p class="mt-1 text-sm font-medium text-slate-400">{{ setting.description || 'Chưa có mô tả' }}</p>
                </div>
                <div class="flex flex-wrap gap-2 text-xs font-bold">
                  <span class="rounded-lg bg-slate-100 px-2.5 py-1 text-slate-600">{{ setting.settingKey }}</span>
                  <span class="rounded-lg bg-indigo-50 px-2.5 py-1 text-indigo-600">{{ formatValueType(setting.valueType) }}</span>
                  <span class="rounded-lg px-2.5 py-1" :class="templateStatusClass(setting.status)">{{ setting.status }}</span>
                </div>
              </div>

              <div class="space-y-4">
                <template v-if="setting.valueType === 'BOOLEAN'">
                  <button
                    @click="setting.settingValue = !parseSettingValue(setting)"
                    class="relative inline-flex h-7 w-12 items-center rounded-full transition-colors"
                    :class="parseSettingValue(setting) ? 'bg-indigo-600' : 'bg-slate-200'"
                  >
                    <span
                      class="inline-block h-5 w-5 transform rounded-full bg-white shadow-md transition-transform"
                      :class="parseSettingValue(setting) ? 'translate-x-6' : 'translate-x-1'"
                    />
                  </button>
                </template>

                <template v-else-if="setting.valueType === 'JSON'">
                  <textarea
                    v-model="setting.settingValue"
                    rows="5"
                    class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm focus:border-indigo-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  />
                </template>

                <template v-else>
                  <input
                    v-model="setting.settingValue"
                    :type="setting.valueType === 'NUMBER' ? 'number' : 'text'"
                    :disabled="!setting.editable"
                    class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm focus:border-indigo-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 disabled:cursor-not-allowed disabled:opacity-60"
                  />
                </template>
              </div>
            </div>

            <div v-if="!editablePlatformSettings.length" class="rounded-3xl border border-dashed border-slate-200 bg-white p-10 text-center text-sm font-medium text-slate-400">
              Backend chưa trả về tham số nền tảng nào.
            </div>
          </div>

          <div v-if="activeSection === 'security'" class="space-y-4">
            <div
              v-for="setting in securitySettings"
              :key="setting.platformSettingId"
              class="rounded-3xl border border-slate-100 bg-white p-6 shadow-sm"
            >
              <div class="mb-2 text-lg font-black text-slate-900">{{ setting.settingName }}</div>
              <div class="mb-4 text-sm font-medium text-slate-400">{{ setting.description || setting.settingKey }}</div>
              <div class="rounded-2xl bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-700">
                {{ normalizeSettingValue(setting) || 'Chưa cấu hình' }}
              </div>
            </div>

            <div v-if="!securitySettings.length" class="rounded-3xl border border-dashed border-amber-200 bg-amber-50 p-8 text-sm font-medium text-amber-700">
              Hiện backend chưa seed riêng nhóm cấu hình bảo mật. Màn hình này đã sẵn sàng nhận dữ liệu ngay khi các `setting_key` liên quan tới `auth/password/security/session` được bổ sung.
            </div>
          </div>

          <div v-if="activeSection === 'notifications'" class="space-y-6">
            <div class="grid gap-4 md:grid-cols-3">
              <div class="rounded-3xl border border-slate-100 bg-white p-5 shadow-sm">
                <div class="text-xs font-bold uppercase tracking-wider text-slate-400">Tổng template</div>
                <div class="mt-1 text-3xl font-black text-slate-900">{{ notificationStats.total }}</div>
              </div>
              <div class="rounded-3xl border border-slate-100 bg-white p-5 shadow-sm">
                <div class="text-xs font-bold uppercase tracking-wider text-slate-400">Đang hoạt động</div>
                <div class="mt-1 text-3xl font-black text-emerald-600">{{ notificationStats.active }}</div>
              </div>
              <div class="rounded-3xl border border-slate-100 bg-white p-5 shadow-sm">
                <div class="text-xs font-bold uppercase tracking-wider text-slate-400">Kênh Email</div>
                <div class="mt-1 text-3xl font-black text-indigo-600">{{ notificationStats.email }}</div>
              </div>
            </div>

            <div class="rounded-3xl border border-slate-100 bg-white shadow-sm">
              <div class="border-b border-slate-100 p-6">
                <h3 class="text-lg font-black text-slate-900">Danh sách luồng thông báo từ backend</h3>
                <p class="mt-1 text-sm font-medium text-slate-400">Thống kê theo template cấu hình hiện có trong hệ thống</p>
              </div>
              <div class="divide-y divide-slate-100">
                <div
                  v-for="template in notificationTemplates"
                  :key="template.notificationTemplateId"
                  class="flex flex-col gap-3 p-5 md:flex-row md:items-center md:justify-between"
                >
                  <div>
                    <div class="font-bold text-slate-900">{{ template.templateName }}</div>
                    <div class="mt-1 text-xs font-medium text-slate-400">
                      <span class="rounded bg-slate-100 px-2 py-0.5 font-mono">{{ template.templateCode }}</span>
                      <span class="ml-2">{{ template.channelCode }}</span>
                    </div>
                  </div>
                  <div class="flex items-center gap-2 text-xs font-bold">
                    <span class="rounded-lg px-2.5 py-1" :class="templateStatusClass(template.status)">{{ template.status }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="activeSection === 'templates'" class="space-y-4">
            <div class="overflow-hidden rounded-3xl border border-slate-100 bg-white shadow-sm">
              <div class="border-b border-slate-100 p-6">
                <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                  <div>
                    <h3 class="text-lg font-black text-slate-900">Mẫu email thông báo</h3>
                    <p class="mt-1 text-sm font-medium text-slate-400">Chỉnh sửa trực tiếp subject/body của template backend hoặc thêm mẫu mail mới cho admin</p>
                  </div>
                  <button
                    @click="openTemplateCreator()"
                    class="inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-bold text-white transition-colors hover:bg-indigo-700"
                  >
                    <Save class="h-4 w-4" /> Thêm template mail
                  </button>
                </div>
              </div>

              <div class="border-b border-slate-100 px-6 py-5">
                <div class="mb-3 text-xs font-black uppercase tracking-widest text-slate-400">Mẫu dựng nhanh</div>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="preset in templatePresets"
                    :key="preset.templateCode"
                    @click="openTemplateCreator(preset)"
                    class="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-left text-xs font-bold text-slate-600 transition hover:border-indigo-200 hover:bg-indigo-50 hover:text-indigo-700"
                  >
                    {{ preset.templateName }}
                  </button>
                </div>
              </div>

              <div class="divide-y divide-slate-100">
                <div
                  v-for="template in notificationTemplates"
                  :key="template.notificationTemplateId"
                  class="flex flex-col gap-4 p-5 hover:bg-slate-50 md:flex-row md:items-center md:justify-between"
                >
                  <div class="min-w-0 flex-1">
                    <div class="font-bold text-slate-900">{{ template.templateName }}</div>
                    <div class="mt-1 text-xs font-medium text-slate-400">
                      <span class="rounded bg-slate-100 px-2 py-0.5 font-mono">{{ template.templateCode }}</span>
                      <span class="ml-2">Subject: {{ template.subjectTemplate || 'Không có subject' }}</span>
                    </div>
                    <p class="mt-2 line-clamp-2 text-sm text-slate-500">{{ template.bodyTemplate }}</p>
                  </div>
                  <button
                    @click="openTemplateEditor(template)"
                    class="flex items-center gap-2 rounded-xl bg-indigo-50 px-4 py-2 text-sm font-bold text-indigo-600 transition-colors hover:bg-indigo-100"
                  >
                    <Edit class="h-4 w-4" /> Chỉnh sửa
                  </button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>

  <Teleport to="body">
    <div v-if="showTemplateEditor && templateForm" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showTemplateEditor = false" />
      <div class="relative z-10 mx-4 flex max-h-[88vh] w-full max-w-2xl flex-col overflow-hidden rounded-3xl bg-white shadow-2xl">
        <div class="flex items-start justify-between border-b border-slate-100 px-6 py-5">
          <div>
            <h3 class="text-2xl font-black text-slate-900">
              {{ templateEditorMode === 'create' ? 'Thêm mẫu thông báo mới' : 'Chỉnh sửa mẫu thông báo' }}
            </h3>
            <p class="text-sm font-medium text-slate-400">
              {{ templateEditorMode === 'create' ? 'Admin có thể tự tạo thêm template mail từ giao diện này' : templateForm.templateCode }}
            </p>
          </div>
          <button @click="showTemplateEditor = false" class="rounded-xl p-2 text-slate-500 hover:bg-slate-100">✕</button>
        </div>

        <div class="flex-1 space-y-4 overflow-y-auto px-6 py-5">
          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="mb-1.5 block text-sm font-bold text-slate-700">Mã template</label>
              <input v-model="templateForm.templateCode" class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-mono uppercase focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-bold text-slate-700">Tên template</label>
              <input v-model="templateForm.templateName" class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
            </div>
            <div class="md:col-span-2">
              <label class="mb-1.5 block text-sm font-bold text-slate-700">Kênh gửi</label>
              <select v-model="templateForm.channelCode" class="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                <option value="EMAIL">EMAIL</option>
                <option value="SYSTEM">SYSTEM</option>
                <option value="SMS">SMS</option>
              </select>
            </div>
          </div>

          <div>
            <label class="mb-1.5 block text-sm font-bold text-slate-700">Subject</label>
            <input v-model="templateForm.subjectTemplate" class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="mb-1.5 block text-sm font-bold text-slate-700">Trạng thái</label>
              <select v-model="templateForm.status" class="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-bold text-slate-700">Mô tả</label>
              <input v-model="templateForm.description" class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
            </div>
          </div>

          <div>
            <label class="mb-1.5 block text-sm font-bold text-slate-700">Body</label>
            <textarea
              v-model="templateForm.bodyTemplate"
              rows="7"
              class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>
        </div>

        <div class="mt-auto flex gap-3 border-t border-slate-100 px-6 py-5">
          <button
            @click="showTemplateEditor = false"
            class="flex-1 rounded-xl border border-slate-200 py-3 font-bold text-slate-600 transition-colors hover:bg-slate-50"
          >
            Hủy
          </button>
          <button
            :disabled="templateSaving"
            @click="saveTemplate"
            class="flex flex-1 items-center justify-center gap-2 rounded-xl bg-indigo-600 py-3 font-bold text-white transition-colors hover:bg-indigo-700 disabled:opacity-60"
          >
            <Loader2 v-if="templateSaving" class="h-4 w-4 animate-spin" />
            <Save v-else class="h-4 w-4" />
            {{ templateEditorMode === 'create' ? 'Tạo template' : 'Lưu template' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
