<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Edit, Upload, Download, Plus, Trash2, ChevronRight,
  Phone, Mail, MapPin, CreditCard, Briefcase, UserCheck,
  AlertTriangle, FileText, Lock, Unlock, RefreshCw, Send
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const activeTab = ref('profile')
const showEditModal = ref(false)
const showTransferModal = ref(false)
const showAddressModal = ref(false)

const employee = ref({
  employeeCode: 'NV001',
  fullName: 'Nguyễn Văn An',
  gender: 'MALE',
  dob: '15/05/1993',
  email: 'nva@company.local',
  phone: '0901234567',
  orgUnit: 'Khối Phát triển Phần mềm',
  jobTitle: 'Senior Backend Engineer',
  directManager: 'Trần Văn Manager',
  employmentStatus: 'OFFICIAL',
  startDate: '01/03/2022',
  probationEnd: '01/05/2022',
  nationality: 'Việt Nam',
  ethnicity: 'Kinh',
  maritalStatus: 'MARRIED',
  educationLevel: 'Đại học',
  avatarInitial: 'A'
})

const profile = ref({
  religion: 'Không',
  placeOfBirth: 'Hà Nội',
  permanentAddress: '123 Đường ABC, Quận 1, TP.HCM',
  taxCode: '8901234567',
  socialInsuranceCode: 'VN1234567',
  healthInsuranceCode: 'HN123456789',
  bloodType: 'A+',
})

const addresses = ref([
  { id: 1, type: 'PERMANENT', fullAddress: '123 Đường ABC, Quận 1, TP.HCM', isPrimary: true },
  { id: 2, type: 'TEMPORARY', fullAddress: '45 Đường XYZ, Quận Bình Thạnh, TP.HCM', isPrimary: false },
])

const emergencyContacts = ref([
  { id: 1, fullName: 'Nguyễn Thị Bình', relationship: 'VỢ', phone: '0987654321', email: 'ntb@gmail.com' },
])

const identifications = ref([
  { id: 1, docType: 'CCCD', docNumber: '001093012345', issueDate: '15/08/2020', expiryDate: '15/08/2035', issuePlace: 'Cục CS QLHC về TTXH', status: 'ACTIVE' },
])

const bankAccounts = ref([
  { id: 1, bankName: 'Ngân hàng Vietcombank', accountNumber: '1234567890123', accountName: 'NGUYEN VAN AN', branch: 'Chi nhánh TP.HCM', isPrimary: true },
])

const documents = ref([
  { id: 1, name: 'HĐLĐ 001-2022', category: 'CONTRACT', status: 'ACTIVE', uploadedAt: '01/03/2022', fileSize: '1.2 MB' },
  { id: 2, name: 'QĐ tiếp nhận', category: 'DECISION', status: 'ACTIVE', uploadedAt: '01/03/2022', fileSize: '0.8 MB' },
  { id: 3, name: 'CV + Bằng cấp', category: 'EDUCATION', status: 'ACTIVE', uploadedAt: '28/02/2022', fileSize: '5.4 MB' },
])

const tabs = [
  { key: 'profile', label: 'Hồ sơ chính', icon: UserCheck },
  { key: 'addresses', label: 'Địa chỉ', icon: MapPin },
  { key: 'emergency', label: 'Liên hệ khẩn', icon: Phone },
  { key: 'identification', label: 'Giấy tờ', icon: CreditCard },
  { key: 'bank', label: 'Tài khoản NH', icon: CreditCard },
  { key: 'documents', label: 'Hồ sơ/Tài liệu', icon: FileText },
]

const statusMap = {
  OFFICIAL: { label: 'Chính thức', cls: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' },
  PROBATION: { label: 'Thử việc', cls: 'bg-amber-50 text-amber-700 ring-amber-600/20' },
  SUSPENDED: { label: 'Đình chỉ', cls: 'bg-rose-50 text-rose-700 ring-rose-600/20' },
  RESIGNED: { label: 'Đã nghỉ', cls: 'bg-slate-100 text-slate-500 ring-slate-200' },
}
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <!-- Breadcrumb -->
      <div class="flex items-center gap-2 text-sm font-medium">
        <button @click="router.push('/employees')" class="text-indigo-600 hover:underline flex items-center gap-1">
          <ArrowLeft class="w-4 h-4" /> Hồ sơ nhân sự
        </button>
        <ChevronRight class="w-4 h-4 text-slate-300" />
        <span class="text-slate-500">{{ employee.employeeCode }} - {{ employee.fullName }}</span>
      </div>

      <!-- Employee Header Card -->
      <div
        class="bg-linear-to-r from-indigo-600 to-indigo-700 rounded-3xl p-8 text-white relative overflow-hidden shadow-xl shadow-indigo-200">
        <div class="absolute -top-20 -right-20 w-64 h-64 bg-white/5 rounded-full blur-3xl"></div>
        <div class="absolute -bottom-10 -left-10 w-48 h-48 bg-white/5 rounded-full blur-3xl"></div>
        <div class="relative flex flex-col md:flex-row md:items-center gap-6">
          <!-- Avatar -->
          <div
            class="w-24 h-24 rounded-3xl bg-white/20 backdrop-blur-sm flex items-center justify-center font-black text-4xl border-2 border-white/30 shadow-xl">
            {{ employee.avatarInitial }}
          </div>
          <!-- Basic Info -->
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-1">
              <h1 class="text-3xl font-black tracking-tight">{{ employee.fullName }}</h1>
              <span class="px-3 py-1 bg-white/20 rounded-full text-sm font-bold">{{ employee.employeeCode }}</span>
            </div>
            <div class="flex flex-wrap gap-x-6 gap-y-2 text-indigo-100 font-medium">
              <span class="flex items-center gap-1.5">
                <Briefcase class="w-4 h-4" /> {{ employee.jobTitle }}
              </span>
              <span class="flex items-center gap-1.5">
                <Mail class="w-4 h-4" /> {{ employee.email }}
              </span>
              <span class="flex items-center gap-1.5">
                <Phone class="w-4 h-4" /> {{ employee.phone }}
              </span>
            </div>
            <div class="flex flex-wrap items-center gap-3 mt-4">
              <span class="px-3 py-1.5 bg-white/20 text-white text-sm font-bold rounded-xl ring-1 ring-white/30">
                {{ employee.orgUnit }}
              </span>
              <span class="px-3 py-1.5 text-xs font-bold rounded-xl ring-1 ring-inset"
                :class="statusMap[employee.employmentStatus]?.cls">
                {{ statusMap[employee.employmentStatus]?.label }}
              </span>
            </div>
          </div>
          <!-- Actions -->
          <div class="flex flex-wrap gap-3">
            <button @click="showEditModal = true"
              class="flex items-center gap-2 bg-white/20 hover:bg-white/30 backdrop-blur-sm px-4 py-2.5 rounded-xl font-bold text-sm transition-all border border-white/20">
              <Edit class="w-4 h-4" /> Chỉnh sửa
            </button>
            <button @click="showTransferModal = true"
              class="flex items-center gap-2 bg-white/20 hover:bg-white/30 backdrop-blur-sm px-4 py-2.5 rounded-xl font-bold text-sm transition-all border border-white/20">
              <Send class="w-4 h-4" /> Điều chuyển
            </button>
          </div>
        </div>
      </div>

      <!-- Tab Navigation -->
      <div
        class="flex overflow-x-auto scrollbar-hide bg-white rounded-2xl border border-slate-100 shadow-sm p-1.5 gap-1">
        <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
          class="flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-sm whitespace-nowrap transition-all"
          :class="activeTab === tab.key ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-500 hover:bg-slate-50 hover:text-slate-700'">
          <component :is="tab.icon" class="w-4 h-4" />
          {{ tab.label }}
        </button>
      </div>

      <!-- Tab: Profile -->
      <div v-if="activeTab === 'profile'" class="grid md:grid-cols-2 gap-6">
        <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6">
          <h3 class="font-black text-slate-900 text-lg mb-5 pb-3 border-b border-slate-100">Thông tin cơ bản</h3>
          <div class="space-y-4">
            <div v-for="(val, key) in {
              'Họ và Tên': employee.fullName,
              'Giới tính': employee.gender === 'MALE' ? 'Nam' : 'Nữ',
              'Ngày sinh': employee.dob,
              'Quốc tịch': employee.nationality,
              'Dân tộc': employee.ethnicity,
              'Hôn nhân': employee.maritalStatus === 'MARRIED' ? 'Đã kết hôn' : 'Độc thân',
              'Học vấn': employee.educationLevel,
            }" :key="key" class="flex items-center justify-between py-2">
              <span class="text-sm text-slate-400 font-bold">{{ key }}</span>
              <span class="text-sm font-bold text-slate-900">{{ val }}</span>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6">
          <h3 class="font-black text-slate-900 text-lg mb-5 pb-3 border-b border-slate-100">Thông tin công việc</h3>
          <div class="space-y-4">
            <div v-for="(val, key) in {
              'Phòng ban': employee.orgUnit,
              'Chức danh': employee.jobTitle,
              'Quản lý trực tiếp': employee.directManager,
              'Trạng thái': statusMap[employee.employmentStatus]?.label,
              'Ngày vào làm': employee.startDate,
              'Kết thúc thử việc': employee.probationEnd,
            }" :key="key" class="flex items-center justify-between py-2">
              <span class="text-sm text-slate-400 font-bold">{{ key }}</span>
              <span class="text-sm font-bold text-slate-900">{{ val }}</span>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6 md:col-span-2">
          <h3 class="font-black text-slate-900 text-lg mb-5 pb-3 border-b border-slate-100">Thông tin bảo hiểm & Pháp lý
          </h3>
          <div class="grid grid-cols-2 md:grid-cols-3 gap-y-4 gap-x-8">
            <div v-for="(val, key) in {
              'Mã số thuế': profile.taxCode,
              'Số BHXH': profile.socialInsuranceCode,
              'Số BHYT': profile.healthInsuranceCode,
              'Nhóm máu': profile.bloodType,
              'Nơi sinh': profile.placeOfBirth,
              'Địa chỉ hộ khẩu': profile.permanentAddress,
            }" :key="key" class="py-2">
              <div class="text-xs text-slate-400 font-bold uppercase tracking-wider mb-1">{{ key }}</div>
              <div class="text-sm font-bold text-slate-900">{{ val }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab: Addresses -->
      <div v-if="activeTab === 'addresses'" class="space-y-4">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-black text-slate-900">Danh sách địa chỉ</h3>
          <button @click="showAddressModal = true"
            class="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2.5 rounded-xl font-bold text-sm hover:bg-indigo-700 shadow-md shadow-indigo-200">
            <Plus class="w-4 h-4" /> Thêm địa chỉ
          </button>
        </div>
        <div v-for="addr in addresses" :key="addr.id"
          class="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 flex items-start justify-between">
          <div class="flex items-start gap-4">
            <div class="w-10 h-10 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600">
              <MapPin class="w-5 h-5" />
            </div>
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="font-bold text-slate-900 text-sm">{{ addr.type === 'PERMANENT' ? 'Địa chỉ thường trú' :
                  'Địa chỉ tạm trú' }}</span>
                <span v-if="addr.isPrimary"
                  class="px-2 py-0.5 bg-indigo-50 text-indigo-600 rounded-md text-xs font-bold ring-1 ring-indigo-200">Chính</span>
              </div>
              <p class="text-slate-500 font-medium text-sm">{{ addr.fullAddress }}</p>
            </div>
          </div>
          <div class="flex gap-2">
            <button class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors">
              <Edit class="w-4 h-4" />
            </button>
            <button class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors">
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      <!-- Tab: Emergency Contacts -->
      <div v-if="activeTab === 'emergency'" class="space-y-4">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-black text-slate-900">Liên hệ khẩn cấp</h3>
          <button
            class="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2.5 rounded-xl font-bold text-sm hover:bg-indigo-700 shadow-md shadow-indigo-200">
            <Plus class="w-4 h-4" /> Thêm liên hệ
          </button>
        </div>
        <div v-for="ec in emergencyContacts" :key="ec.id"
          class="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div
              class="w-12 h-12 bg-rose-50 rounded-xl flex items-center justify-center font-black text-rose-600 text-lg">
              {{ ec.fullName.charAt(0) }}
            </div>
            <div>
              <div class="font-black text-slate-900">{{ ec.fullName }}</div>
              <div class="flex gap-4 text-sm text-slate-500 font-medium mt-1">
                <span>{{ ec.relationship }}</span>
                <span class="flex items-center gap-1">
                  <Phone class="w-3.5 h-3.5" /> {{ ec.phone }}
                </span>
                <span class="flex items-center gap-1">
                  <Mail class="w-3.5 h-3.5" /> {{ ec.email }}
                </span>
              </div>
            </div>
          </div>
          <div class="flex gap-2">
            <button class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors">
              <Edit class="w-4 h-4" />
            </button>
            <button class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors">
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      <!-- Tab: Identification -->
      <div v-if="activeTab === 'identification'" class="space-y-4">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-black text-slate-900">Giấy tờ tùy thân</h3>
          <button
            class="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2.5 rounded-xl font-bold text-sm hover:bg-indigo-700 shadow-md shadow-indigo-200">
            <Plus class="w-4 h-4" /> Thêm giấy tờ
          </button>
        </div>
        <div v-for="id in identifications" :key="id.id"
          class="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-600">
                <CreditCard class="w-5 h-5" />
              </div>
              <div>
                <span class="font-black text-slate-900">{{ id.docType }}</span>
                <span class="ml-2 px-2.5 py-0.5 rounded-md text-xs font-bold"
                  :class="id.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'">
                  {{ id.status === 'ACTIVE' ? 'Còn hiệu lực' : 'Hết hạn' }}
                </span>
              </div>
            </div>
            <div class="flex gap-2">
              <button class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors">
                <Edit class="w-4 h-4" />
              </button>
              <button class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors">
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </div>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <div class="text-xs text-slate-400 font-bold mb-1">Số hiệu</div>
              <div class="font-bold text-slate-900 font-mono">{{ id.docNumber }}</div>
            </div>
            <div>
              <div class="text-xs text-slate-400 font-bold mb-1">Ngày cấp</div>
              <div class="font-bold text-slate-900">{{ id.issueDate }}</div>
            </div>
            <div>
              <div class="text-xs text-slate-400 font-bold mb-1">Hết hạn</div>
              <div class="font-bold text-slate-900">{{ id.expiryDate }}</div>
            </div>
            <div>
              <div class="text-xs text-slate-400 font-bold mb-1">Nơi cấp</div>
              <div class="font-bold text-slate-900">{{ id.issuePlace }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab: Bank Account -->
      <div v-if="activeTab === 'bank'" class="space-y-4">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-black text-slate-900">Tài khoản ngân hàng</h3>
          <button
            class="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2.5 rounded-xl font-bold text-sm hover:bg-indigo-700 shadow-md shadow-indigo-200">
            <Plus class="w-4 h-4" /> Thêm tài khoản
          </button>
        </div>
        <div v-for="bank in bankAccounts" :key="bank.id"
          class="bg-linear-to-r from-slate-800 to-slate-900 rounded-3xl p-6 text-white shadow-xl relative overflow-hidden">
          <div class="absolute -top-10 -right-10 w-40 h-40 bg-white/5 rounded-full blur-3xl"></div>
          <div class="flex items-center justify-between mb-6">
            <span class="font-black text-lg">{{ bank.bankName }}</span>
            <span v-if="bank.isPrimary"
              class="px-3 py-1 bg-amber-400 text-amber-900 rounded-full text-xs font-black">Chính</span>
          </div>
          <div class="font-mono text-2xl font-bold tracking-widest text-slate-200 mb-4">
            {{ bank.accountNumber.replace(/(\d{4})/g, '$1 ').trim() }}
          </div>
          <div class="flex items-center justify-between text-sm">
            <div>
              <div class="text-slate-400 text-xs font-bold mb-1">Chủ tài khoản</div>
              <div class="font-bold">{{ bank.accountName }}</div>
            </div>
            <div class="text-right">
              <div class="text-slate-400 text-xs font-bold mb-1">Chi nhánh</div>
              <div class="font-bold">{{ bank.branch }}</div>
            </div>
          </div>
          <div class="flex gap-2 mt-4">
            <button
              class="flex items-center gap-1.5 px-3 py-1.5 bg-white/10 hover:bg-white/20 rounded-lg text-xs font-bold transition-colors">
              <Edit class="w-3.5 h-3.5" /> Sửa
            </button>
            <button
              class="flex items-center gap-1.5 px-3 py-1.5 bg-white/10 hover:bg-rose-500/30 rounded-lg text-xs font-bold transition-colors text-rose-300">
              <Trash2 class="w-3.5 h-3.5" /> Xóa
            </button>
          </div>
        </div>
      </div>

      <!-- Tab: Documents -->
      <div v-if="activeTab === 'documents'" class="space-y-4">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-black text-slate-900">Hồ sơ & Tài liệu đính kèm</h3>
          <button
            class="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2.5 rounded-xl font-bold text-sm hover:bg-indigo-700 shadow-md shadow-indigo-200">
            <Upload class="w-4 h-4" /> Tải lên tài liệu
          </button>
        </div>
        <div class="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
          <table class="w-full">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-100">
                <th class="py-3 px-5 text-left font-bold text-slate-500 text-xs uppercase tracking-wider">Tên tài liệu
                </th>
                <th class="py-3 px-5 text-left font-bold text-slate-500 text-xs uppercase tracking-wider">Danh mục</th>
                <th class="py-3 px-5 text-left font-bold text-slate-500 text-xs uppercase tracking-wider">Trạng thái
                </th>
                <th class="py-3 px-5 text-left font-bold text-slate-500 text-xs uppercase tracking-wider">Ngày tải lên
                </th>
                <th class="py-3 px-5 text-left font-bold text-slate-500 text-xs uppercase tracking-wider">Kích thước
                </th>
                <th class="py-3 px-5"></th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="doc in documents" :key="doc.id" class="hover:bg-slate-50/50 transition-colors">
                <td class="py-4 px-5">
                  <div class="flex items-center gap-3">
                    <div class="w-9 h-9 bg-indigo-50 rounded-xl flex items-center justify-center">
                      <FileText class="w-5 h-5 text-indigo-600" />
                    </div>
                    <span class="font-bold text-slate-900 text-sm">{{ doc.name }}</span>
                  </div>
                </td>
                <td class="py-4 px-5 text-sm font-medium text-slate-600">{{ doc.category }}</td>
                <td class="py-4 px-5">
                  <span
                    class="px-2.5 py-1 rounded-lg text-xs font-bold ring-1 ring-inset bg-emerald-50 text-emerald-700 ring-emerald-600/20">
                    {{ doc.status === 'ACTIVE' ? 'Hoạt động' : 'Lưu trữ' }}
                  </span>
                </td>
                <td class="py-4 px-5 text-sm text-slate-500 font-medium">{{ doc.uploadedAt }}</td>
                <td class="py-4 px-5 text-sm text-slate-500 font-medium">{{ doc.fileSize }}</td>
                <td class="py-4 px-5">
                  <div class="flex items-center justify-end gap-2">
                    <button
                      class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors">
                      <Download class="w-4 h-4" />
                    </button>
                    <button
                      class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors">
                      <Trash2 class="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </MainLayout>
</template>
