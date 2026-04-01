<script setup>
import { ref, computed } from 'vue'
import MainLayout from '../../../layouts/MainLayout.vue'
import {
  Settings, Bell, Shield, Monitor, Palette,
  ToggleLeft, ToggleRight, Save, RefreshCw, ChevronRight
} from 'lucide-vue-next'

const activeSection = ref('platform')
const saved = ref(false)

const platform = ref({
  companyName: 'Digital HRM Corp',
  systemTimezone: 'Asia/Ho_Chi_Minh',
  defaultLanguage: 'vi',
  dateFormat: 'DD/MM/YYYY',
  maxLoginAttempts: 5,
  sessionTimeoutMinutes: 480,
  passwordMinLength: 8,
  requireUppercase: true,
  requireNumber: true,
  requireSpecialChar: true,
  passwordExpiryDays: 90,
  twoFactorEnabled: false,
  maintenanceMode: false,
  maintenanceMessage: '',
})

const notifications = ref([
  { key: 'contractExpiry', label: 'Cảnh báo hợp đồng sắp hết hạn', description: 'Gửi email khi HĐ còn 30/15/7 ngày', enabled: true },
  { key: 'newEmployee', label: 'Thông báo nhân viên mới', description: 'HR nhận thông báo khi có nhân viên mới onboard', enabled: true },
  { key: 'leaveRequest', label: 'Yêu cầu nghỉ phép', description: 'Manager nhận email khi có đơn nghỉ phép mới', enabled: true },
  { key: 'profileChange', label: 'Yêu cầu thay đổi hồ sơ', description: 'HR nhận thông báo đề xuất cập nhật hồ sơ', enabled: false },
  { key: 'payrollReady', label: 'Bảng lương đã xử lý', description: 'Nhân viên nhận phiếu lương qua email', enabled: false },
  { key: 'auditAlert', label: 'Cảnh báo bảo mật audit', description: 'Admin nhận alert khi phát hiện hành vi bất thường', enabled: true },
])

const notificationTemplates = ref([
  { id: 1, code: 'CONTRACT_EXPIRY_WARNING', name: 'Cảnh báo HĐ hết hạn', subject: '[HRM] Hợp đồng sắp hết hạn', lastModified: '01/03/2026' },
  { id: 2, code: 'WELCOME_NEW_EMPLOYEE', name: 'Chào mừng nhân viên mới', subject: '[HRM] Chào mừng bạn đến với công ty!', lastModified: '15/02/2026' },
  { id: 3, code: 'PASSWORD_RESET', name: 'Đặt lại mật khẩu', subject: '[HRM] Yêu cầu đặt lại mật khẩu', lastModified: '01/01/2026' },
  { id: 4, code: 'PROFILE_CHANGE_APPROVED', name: 'Hồ sơ được duyệt', subject: '[HRM] Yêu cầu cập nhật hồ sơ đã được phê duyệt', lastModified: '20/02/2026' },
])

const sections = [
  { key: 'platform', label: 'Cấu hình nền tảng', icon: Settings },
  { key: 'security', label: 'Bảo mật & Chính sách', icon: Shield },
  { key: 'notifications', label: 'Thông báo tự động', icon: Bell },
  { key: 'templates', label: 'Mẫu email thông báo', icon: Monitor },
]

const saveSettings = () => {
  saved.value = true
  setTimeout(() => saved.value = false, 2000)
}
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">Cài đặt hệ thống</h2>
          <p class="text-slate-500 font-medium mt-1">Cấu hình nền tảng, bảo mật và hành vi thông báo</p>
        </div>
        <button @click="saveSettings"
          class="flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold transition-all shadow-lg"
          :class="saved ? 'bg-emerald-500 text-white shadow-emerald-200' : 'bg-indigo-600 text-white hover:bg-indigo-700 shadow-indigo-200'">
          <component :is="saved ? RefreshCw : Save" class="w-5 h-5" />
          {{ saved ? 'Đã lưu!' : 'Lưu cài đặt' }}
        </button>
      </div>

      <div class="grid lg:grid-cols-4 gap-6">
        <!-- Sidebar Navigation -->
        <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-4 h-fit">
          <nav class="space-y-1">
            <button v-for="sec in sections" :key="sec.key"
              @click="activeSection = sec.key"
              class="w-full flex items-center gap-3 px-4 py-3 rounded-2xl transition-all text-sm font-bold text-left"
              :class="activeSection === sec.key
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-200'
                : 'text-slate-500 hover:bg-slate-50 hover:text-slate-700'">
              <component :is="sec.icon" class="w-5 h-5 shrink-0" />
              {{ sec.label }}
              <ChevronRight v-if="activeSection === sec.key" class="w-4 h-4 ml-auto opacity-60" />
            </button>
          </nav>
        </div>

        <!-- Content -->
        <div class="lg:col-span-3 space-y-6">

          <!-- Platform Config -->
          <div v-if="activeSection === 'platform'" class="space-y-6">
            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6">
              <h3 class="font-black text-slate-900 text-lg mb-6 pb-3 border-b border-slate-100">Thông tin công ty</h3>
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-bold text-slate-700 mb-1.5">Tên công ty / Hệ thống</label>
                  <input v-model="platform.companyName" class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-bold text-slate-700 mb-1.5">Múi giờ</label>
                    <select v-model="platform.systemTimezone" class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                      <option value="Asia/Ho_Chi_Minh">Asia/Ho_Chi_Minh (UTC+7)</option>
                      <option value="UTC">UTC</option>
                    </select>
                  </div>
                  <div>
                    <label class="block text-sm font-bold text-slate-700 mb-1.5">Định dạng ngày</label>
                    <select v-model="platform.dateFormat" class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                      <option value="DD/MM/YYYY">DD/MM/YYYY</option>
                      <option value="MM/DD/YYYY">MM/DD/YYYY</option>
                      <option value="YYYY-MM-DD">YYYY-MM-DD</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-bold text-slate-700 mb-1.5">Ngôn ngữ mặc định</label>
                  <select v-model="platform.defaultLanguage" class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                    <option value="vi">Tiếng Việt</option>
                    <option value="en">English</option>
                  </select>
                </div>
              </div>
            </div>

            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6">
              <h3 class="font-black text-slate-900 text-lg mb-6 pb-3 border-b border-slate-100">Chế độ bảo trì</h3>
              <div class="flex items-center justify-between mb-4">
                <div>
                  <div class="font-bold text-slate-900">Kích hoạt chế độ bảo trì</div>
                  <div class="text-sm text-slate-400 font-medium">Tạm thời chặn người dùng truy cập hệ thống</div>
                </div>
                <button @click="platform.maintenanceMode = !platform.maintenanceMode"
                  class="relative inline-flex h-7 w-12 items-center rounded-full transition-colors"
                  :class="platform.maintenanceMode ? 'bg-rose-500' : 'bg-slate-200'">
                  <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow-md transition-transform"
                    :class="platform.maintenanceMode ? 'translate-x-6' : 'translate-x-1'"></span>
                </button>
              </div>
              <div v-if="platform.maintenanceMode">
                <label class="block text-sm font-bold text-slate-700 mb-1.5">Thông báo bảo trì</label>
                <textarea v-model="platform.maintenanceMessage" rows="3" placeholder="Hệ thống đang bảo trì, vui lòng quay lại sau..."
                  class="w-full px-4 py-2.5 border border-rose-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-rose-500/20 resize-none bg-rose-50"></textarea>
              </div>
            </div>
          </div>

          <!-- Security Config -->
          <div v-if="activeSection === 'security'" class="space-y-6">
            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6">
              <h3 class="font-black text-slate-900 text-lg mb-6 pb-3 border-b border-slate-100">Chính sách mật khẩu</h3>
              <div class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-bold text-slate-700 mb-1.5">Số ký tự tối thiểu</label>
                    <input v-model="platform.passwordMinLength" type="number" min="6" max="32"
                      class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-bold text-slate-700 mb-1.5">Hết hạn sau (ngày)</label>
                    <input v-model="platform.passwordExpiryDays" type="number"
                      class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
                  </div>
                </div>
                <div class="space-y-3">
                  <div v-for="rule in [
                    {key:'requireUppercase', label:'Yêu cầu chữ hoa (A-Z)'},
                    {key:'requireNumber', label:'Yêu cầu chữ số (0-9)'},
                    {key:'requireSpecialChar', label:'Yêu cầu ký tự đặc biệt (!@#$...)'},
                  ]" :key="rule.key" class="flex items-center justify-between">
                    <span class="text-sm font-bold text-slate-700">{{ rule.label }}</span>
                    <button @click="platform[rule.key] = !platform[rule.key]"
                      class="relative inline-flex h-7 w-12 items-center rounded-full transition-colors"
                      :class="platform[rule.key] ? 'bg-indigo-600' : 'bg-slate-200'">
                      <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow-md transition-transform"
                        :class="platform[rule.key] ? 'translate-x-6' : 'translate-x-1'"></span>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6">
              <h3 class="font-black text-slate-900 text-lg mb-6 pb-3 border-b border-slate-100">Phiên đăng nhập</h3>
              <div class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-bold text-slate-700 mb-1.5">Số lần sai mật khẩu tối đa</label>
                    <input v-model="platform.maxLoginAttempts" type="number" min="3" max="10"
                      class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-bold text-slate-700 mb-1.5">Timeout phiên (phút)</label>
                    <input v-model="platform.sessionTimeoutMinutes" type="number"
                      class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
                  </div>
                </div>
                <div class="flex items-center justify-between p-4 bg-slate-50 rounded-2xl">
                  <div>
                    <div class="font-bold text-slate-900">Xác thực 2 bước (2FA)</div>
                    <div class="text-sm text-slate-400 font-medium">Bắt buộc đối với tài khoản admin</div>
                  </div>
                  <button @click="platform.twoFactorEnabled = !platform.twoFactorEnabled"
                    class="relative inline-flex h-7 w-12 items-center rounded-full transition-colors"
                    :class="platform.twoFactorEnabled ? 'bg-indigo-600' : 'bg-slate-200'">
                    <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow-md transition-transform"
                      :class="platform.twoFactorEnabled ? 'translate-x-6' : 'translate-x-1'"></span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Notifications -->
          <div v-if="activeSection === 'notifications'" class="space-y-4">
            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6">
              <h3 class="font-black text-slate-900 text-lg mb-6 pb-3 border-b border-slate-100">Luồng thông báo tự động</h3>
              <div class="space-y-4">
                <div v-for="notif in notifications" :key="notif.key"
                  class="flex items-center justify-between p-4 rounded-2xl border transition-colors"
                  :class="notif.enabled ? 'bg-indigo-50 border-indigo-100' : 'bg-slate-50 border-slate-100'">
                  <div>
                    <div class="font-bold text-slate-900 text-sm">{{ notif.label }}</div>
                    <div class="text-xs text-slate-400 font-medium mt-0.5">{{ notif.description }}</div>
                  </div>
                  <button @click="notif.enabled = !notif.enabled"
                    class="relative inline-flex h-7 w-12 items-center rounded-full transition-colors ml-6 shrink-0"
                    :class="notif.enabled ? 'bg-indigo-600' : 'bg-slate-200'">
                    <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow-md transition-transform"
                      :class="notif.enabled ? 'translate-x-6' : 'translate-x-1'"></span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Notification Templates -->
          <div v-if="activeSection === 'templates'" class="space-y-4">
            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
              <div class="p-6 border-b border-slate-100">
                <h3 class="font-black text-slate-900 text-lg">Mẫu email thông báo</h3>
                <p class="text-slate-400 font-medium text-sm mt-1">Tùy chỉnh nội dung email gửi tự động từ hệ thống</p>
              </div>
              <div class="divide-y divide-slate-100">
                <div v-for="tpl in notificationTemplates" :key="tpl.id"
                  class="p-5 flex items-center justify-between hover:bg-slate-50 transition-colors cursor-pointer">
                  <div class="flex items-center gap-4">
                    <div class="w-10 h-10 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600">
                      <Bell class="w-5 h-5" />
                    </div>
                    <div>
                      <div class="font-bold text-slate-900 text-sm">{{ tpl.name }}</div>
                      <div class="text-xs text-slate-400 font-medium mt-0.5">
                        <span class="font-mono bg-slate-100 px-2 py-0.5 rounded">{{ tpl.code }}</span>
                        <span class="ml-2">Subject: {{ tpl.subject }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="flex items-center gap-3">
                    <span class="text-xs text-slate-400">Cập nhật: {{ tpl.lastModified }}</span>
                    <button class="px-3 py-1.5 bg-indigo-50 text-indigo-600 rounded-lg text-xs font-bold hover:bg-indigo-100 transition-colors">
                      Chỉnh sửa
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  </MainLayout>
</template>
