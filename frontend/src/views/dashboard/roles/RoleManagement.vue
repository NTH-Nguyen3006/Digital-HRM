<script setup>
import { ref, computed } from 'vue'
import MainLayout from '../../../layouts/MainLayout.vue'
import { Plus, Edit, ToggleLeft, ToggleRight, Shield, ChevronDown, ChevronRight, CheckSquare, Square } from 'lucide-vue-next'

const showModal = ref(false)
const activeTab = ref('list') // list | matrix
const expandedRole = ref(null)

const roles = ref([
  {
    id: 1, roleCode: 'SYSTEM_ADMIN', roleName: 'Quản trị hệ thống', description: 'Toàn quyền trên hệ thống',
    sortOrder: 1, status: 'ACTIVE', userCount: 1,
    permissions: ['auth.login', 'auth.logout', 'user.view', 'user.create', 'user.update', 'role.view', 'role.update', 'audit.view']
  },
  {
    id: 2, roleCode: 'HR', roleName: 'Nhân sự (HR)', description: 'Quản lý hồ sơ, hợp đồng, onboarding',
    sortOrder: 2, status: 'ACTIVE', userCount: 3,
    permissions: ['auth.login', 'auth.logout', 'employee.view', 'employee.create', 'employee.update', 'contract.view', 'contract.create']
  },
  {
    id: 3, roleCode: 'MANAGER', roleName: 'Trưởng phòng', description: 'Xem và duyệt đề xuất trong phòng ban',
    sortOrder: 3, status: 'ACTIVE', userCount: 8,
    permissions: ['auth.login', 'auth.logout', 'employee.view', 'leave.approve']
  },
  {
    id: 4, roleCode: 'EMPLOYEE', roleName: 'Nhân viên', description: 'Tự phục vụ - xem hồ sơ, gửi đề xuất',
    sortOrder: 4, status: 'ACTIVE', userCount: 1270,
    permissions: ['auth.login', 'auth.logout', 'auth.change_password']
  },
])

const permissionGroups = [
  {
    module: 'AUTH', label: '🔐 Xác thực',
    perms: [
      { code: 'auth.login', label: 'Đăng nhập' },
      { code: 'auth.logout', label: 'Đăng xuất' },
      { code: 'auth.change_password', label: 'Đổi mật khẩu' },
      { code: 'auth.forgot_password', label: 'Quên mật khẩu' },
    ]
  },
  {
    module: 'USER', label: '👤 Tài khoản',
    perms: [
      { code: 'user.view', label: 'Xem' },
      { code: 'user.create', label: 'Tạo mới' },
      { code: 'user.update', label: 'Cập nhật' },
      { code: 'user.lock', label: 'Khóa/Mở khóa' },
    ]
  },
  {
    module: 'ROLE', label: '🛡️ Vai trò',
    perms: [
      { code: 'role.view', label: 'Xem' },
      { code: 'role.create', label: 'Tạo mới' },
      { code: 'role.update', label: 'Cập nhật' },
      { code: 'role.change_status', label: 'Thay đổi trạng thái' },
    ]
  },
  {
    module: 'EMPLOYEE', label: '🧑‍💼 Nhân viên',
    perms: [
      { code: 'employee.view', label: 'Xem' },
      { code: 'employee.create', label: 'Tạo mới' },
      { code: 'employee.update', label: 'Cập nhật' },
      { code: 'employee.change_status', label: 'Đổi trạng thái' },
      { code: 'employee.transfer', label: 'Điều chuyển' },
    ]
  },
  {
    module: 'CONTRACT', label: '📄 Hợp đồng',
    perms: [
      { code: 'contract.view', label: 'Xem' },
      { code: 'contract.create', label: 'Tạo nháp' },
      { code: 'contract.submit', label: 'Gửi ký' },
      { code: 'contract.review', label: 'Duyệt/Từ chối' },
      { code: 'contract.activate', label: 'Kích hoạt' },
    ]
  },
  {
    module: 'LEAVE', label: '🏖️ Nghỉ phép',
    perms: [
      { code: 'leave.view', label: 'Xem' },
      { code: 'leave.request', label: 'Nộp đơn' },
      { code: 'leave.approve', label: 'Duyệt/Từ chối' },
    ]
  },
  {
    module: 'AUDIT', label: '📋 Audit Log',
    perms: [
      { code: 'audit.view', label: 'Xem nhật ký' },
    ]
  },
]

const hasPermission = (role, code) => role.permissions.includes(code)

const toggleExpand = (roleId) => {
  expandedRole.value = expandedRole.value === roleId ? null : roleId
}

const roleColor = (code) => {
  const map = {
    SYSTEM_ADMIN: 'from-purple-500 to-purple-700',
    HR: 'from-indigo-500 to-indigo-700',
    MANAGER: 'from-sky-500 to-sky-700',
    EMPLOYEE: 'from-slate-400 to-slate-600',
  }
  return map[code] || 'from-slate-400 to-slate-600'
}
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">Vai trò & Phân quyền</h2>
          <p class="text-slate-500 font-medium mt-1">Cấu hình vai trò và ma trận quyền hệ thống</p>
        </div>
        <div class="flex items-center gap-3">
          <div class="flex bg-white border border-slate-200 rounded-xl p-1 gap-1">
            <button v-for="tab in [{k:'list',l:'Danh sách'},{k:'matrix',l:'Ma trận'}]" :key="tab.k"
              @click="activeTab = tab.k"
              class="px-4 py-2 rounded-lg text-sm font-bold transition-all"
              :class="activeTab === tab.k ? 'bg-indigo-600 text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'">
              {{ tab.l }}
            </button>
          </div>
          <button @click="showModal = true"
            class="bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 flex items-center">
            <Plus class="w-5 h-5 mr-2" /> Tạo vai trò
          </button>
        </div>
      </div>

      <!-- List View -->
      <div v-if="activeTab === 'list'" class="space-y-4">
        <div v-for="role in roles" :key="role.id"
          class="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden hover:shadow-md transition-shadow">
          <!-- Role Header -->
          <div class="p-6 flex items-center justify-between cursor-pointer" @click="toggleExpand(role.id)">
            <div class="flex items-center space-x-4">
              <div class="w-12 h-12 rounded-2xl bg-linear-to-br text-white flex items-center justify-center font-black text-lg shadow-lg"
                :class="roleColor(role.roleCode)">
                <Shield class="w-6 h-6" />
              </div>
              <div>
                <div class="flex items-center gap-3">
                  <h3 class="font-black text-slate-900 text-lg">{{ role.roleName }}</h3>
                  <span class="text-xs font-bold px-2 py-1 bg-slate-100 text-slate-500 rounded-lg">{{ role.roleCode }}</span>
                  <span class="text-xs font-bold px-2.5 py-1 rounded-full"
                    :class="role.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-600'">
                    {{ role.status === 'ACTIVE' ? 'Hoạt động' : 'Vô hiệu' }}
                  </span>
                </div>
                <p class="text-sm text-slate-400 font-medium mt-0.5">{{ role.description }} • <span class="font-bold text-slate-600">{{ role.userCount }} người dùng</span></p>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <button class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-xl transition-colors" @click.stop>
                <Edit class="w-4 h-4" />
              </button>
              <button class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition-colors" @click.stop>
                <ToggleLeft class="w-5 h-5" />
              </button>
              <component :is="expandedRole === role.id ? ChevronDown : ChevronRight" class="w-5 h-5 text-slate-400 transition-transform" />
            </div>
          </div>

          <!-- Permissions Expand -->
          <div v-if="expandedRole === role.id" class="border-t border-slate-100 bg-slate-50/50 p-6">
            <h4 class="font-bold text-slate-700 text-sm mb-4">Danh sách quyền đã gán</h4>
            <div class="space-y-3">
              <div v-for="group in permissionGroups" :key="group.module">
                <p class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">{{ group.label }}</p>
                <div class="flex flex-wrap gap-2">
                  <span v-for="perm in group.perms" :key="perm.code"
                    class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold ring-1 ring-inset"
                    :class="hasPermission(role, perm.code)
                      ? 'bg-indigo-50 text-indigo-700 ring-indigo-600/20'
                      : 'bg-slate-100 text-slate-400 ring-slate-200 line-through'">
                    <span v-if="hasPermission(role, perm.code)">✓</span>
                    {{ perm.label }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Permission Matrix View -->
      <div v-if="activeTab === 'matrix'" class="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse text-sm">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200">
                <th class="py-4 px-5 font-bold text-slate-600 min-w-[200px]">Quyền / Vai trò</th>
                <th v-for="role in roles" :key="role.id"
                  class="py-4 px-5 font-bold text-center whitespace-nowrap">
                  <div class="flex flex-col items-center gap-1">
                    <span class="text-slate-900">{{ role.roleName }}</span>
                    <span class="text-xs text-slate-400 font-medium">{{ role.userCount }} user</span>
                  </div>
                </th>
              </tr>
            </thead>
            <tbody>
              <template v-for="group in permissionGroups" :key="group.module">
                <tr class="bg-slate-50/50 border-b border-slate-100">
                  <td class="py-2.5 px-5 font-black text-slate-600 text-xs uppercase tracking-wider" :colspan="roles.length + 1">
                    {{ group.label }}
                  </td>
                </tr>
                <tr v-for="perm in group.perms" :key="perm.code" class="border-b border-slate-100 hover:bg-slate-50/50 transition-colors">
                  <td class="py-3 px-5 text-slate-600 font-medium">{{ perm.label }}</td>
                  <td v-for="role in roles" :key="role.id" class="py-3 px-5 text-center">
                    <div class="flex justify-center">
                      <div v-if="hasPermission(role, perm.code)"
                        class="w-6 h-6 rounded-md bg-emerald-500 text-white flex items-center justify-center">
                        <span class="text-xs font-black">✓</span>
                      </div>
                      <div v-else class="w-6 h-6 rounded-md bg-slate-100 text-slate-300 flex items-center justify-center">
                        <span class="text-xs">—</span>
                      </div>
                    </div>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Create Role Modal -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showModal = false"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-lg mx-4 p-8 z-10">
          <h3 class="text-2xl font-black text-slate-900 mb-6">Tạo vai trò mới</h3>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1.5">Mã vai trò (Code) *</label>
              <input placeholder="VD: ACCOUNTANT" class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 uppercase" />
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1.5">Tên vai trò *</label>
              <input placeholder="VD: Kế toán" class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1.5">Mô tả</label>
              <textarea rows="3" placeholder="Mô tả ngắn gọn về vai trò và phạm vi quyền..." class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 resize-none"></textarea>
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1.5">Thứ tự hiển thị</label>
              <input type="number" placeholder="5" class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
            </div>
          </div>
          <div class="flex gap-3 mt-8">
            <button @click="showModal = false" class="flex-1 py-3 rounded-xl font-bold text-slate-600 border border-slate-200 hover:bg-slate-50 transition-colors">Hủy</button>
            <button class="flex-1 py-3 rounded-xl font-bold text-white bg-indigo-600 hover:bg-indigo-700 transition-colors shadow-lg shadow-indigo-200">Tạo vai trò</button>
          </div>
        </div>
      </div>
    </Teleport>
  </MainLayout>
</template>
