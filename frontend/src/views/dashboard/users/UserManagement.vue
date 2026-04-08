<script setup>
import { ref, computed } from 'vue'
import {
  Plus, Search, MoreVertical, ShieldCheck, ShieldOff,
  Lock, Unlock, Mail, Eye, Edit, UserX, Users, Key, RefreshCw
} from 'lucide-vue-next'

const searchQuery = ref('')
const filterStatus = ref('ALL')
const showCreateModal = ref(false)
const showDetail = ref(false)
const selectedUser = ref(null)

const users = ref([
  {
    id: 1, username: 'admin', fullName: 'System Administrator', email: 'admin@company.local',
    phone: '0900000000', role: 'SYSTEM_ADMIN', roleLabel: 'Quản trị hệ thống',
    status: 'ACTIVE', locked: false, mustChangePassword: false,
    lastLogin: '01/04/2026 08:30', createdAt: '01/01/2026'
  },
  {
    id: 2, username: 'hr.nguyen', fullName: 'Nguyễn Thị HR', email: 'hr.nguyen@company.local',
    phone: '0901234567', role: 'HR', roleLabel: 'Nhân sự',
    status: 'ACTIVE', locked: false, mustChangePassword: true,
    lastLogin: '31/03/2026 17:45', createdAt: '15/01/2026'
  },
  {
    id: 3, username: 'mgr.tran', fullName: 'Trần Văn Manager', email: 'mgr.tran@company.local',
    phone: '0912345678', role: 'MANAGER', roleLabel: 'Trưởng phòng',
    status: 'ACTIVE', locked: false, mustChangePassword: false,
    lastLogin: '30/03/2026 09:00', createdAt: '01/02/2026'
  },
  {
    id: 4, username: 'emp.le', fullName: 'Lê Thị Employee', email: 'emp.le@company.local',
    phone: '0923456789', role: 'EMPLOYEE', roleLabel: 'Nhân viên',
    status: 'INACTIVE', locked: true, mustChangePassword: false,
    lastLogin: '20/02/2026 14:20', createdAt: '01/02/2026'
  },
])

const newUser = ref({
  employeeId: '', username: '', email: '', phoneNumber: '',
  status: 'ACTIVE', roleCode: 'EMPLOYEE', sendSetupEmail: true
})

const filteredUsers = computed(() => {
  return users.value.filter(u => {
    const matchSearch = !searchQuery.value ||
      u.username.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      u.fullName.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchStatus = filterStatus.value === 'ALL' || u.status === filterStatus.value
    return matchSearch && matchStatus
  })
})

const viewUser = (user) => {
  selectedUser.value = user
  showDetail.value = true
}

const roleColor = (role) => {
  const map = {
    SYSTEM_ADMIN: 'bg-purple-50 text-purple-700 ring-purple-600/20',
    HR: 'bg-indigo-50 text-indigo-700 ring-indigo-600/20',
    MANAGER: 'bg-sky-50 text-sky-700 ring-sky-600/20',
    EMPLOYEE: 'bg-slate-100 text-slate-600 ring-slate-500/20',
  }
  return map[role] || 'bg-slate-100 text-slate-600 ring-slate-500/20'
}

const stats = computed(() => ({
  total: users.value.length,
  active: users.value.filter(u => u.status === 'ACTIVE').length,
  locked: users.value.filter(u => u.locked).length,
  pendingReset: users.value.filter(u => u.mustChangePassword).length,
}))
</script>

<template>
  
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">Tài khoản hệ thống</h2>
          <p class="text-slate-500 font-medium mt-1">Quản lý người dùng, phân quyền và bảo mật</p>
        </div>
        <button @click="showCreateModal = true"
          class="bg-indigo-600 px-5 py-2.5 rounded-xl font-bold text-white hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 flex items-center">
          <Plus class="w-5 h-5 mr-2" /> Tạo tài khoản
        </button>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm">
          <div class="text-slate-400 text-xs font-bold uppercase tracking-wider mb-1">Tổng tài khoản</div>
          <div class="text-3xl font-black text-slate-900">{{ stats.total }}</div>
        </div>
        <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm">
          <div class="text-slate-400 text-xs font-bold uppercase tracking-wider mb-1">Đang hoạt động</div>
          <div class="text-3xl font-black text-emerald-600">{{ stats.active }}</div>
        </div>
        <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm">
          <div class="text-slate-400 text-xs font-bold uppercase tracking-wider mb-1">Đang bị khóa</div>
          <div class="text-3xl font-black text-rose-500">{{ stats.locked }}</div>
        </div>
        <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm">
          <div class="text-slate-400 text-xs font-bold uppercase tracking-wider mb-1">Cần đổi mật khẩu</div>
          <div class="text-3xl font-black text-amber-500">{{ stats.pendingReset }}</div>
        </div>
      </div>

      <!-- Filters & Search -->
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="relative flex-1">
          <Search class="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input v-model="searchQuery" type="text" placeholder="Tìm theo username, tên, email..."
            class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 shadow-sm" />
        </div>
        <div class="flex gap-2">
          <button v-for="f in ['ALL', 'ACTIVE', 'INACTIVE']" :key="f" @click="filterStatus = f"
            class="px-4 py-2.5 rounded-xl text-sm font-bold transition-all"
            :class="filterStatus === f ? 'bg-indigo-600 text-white shadow-md shadow-indigo-200' : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'">
            {{ f === 'ALL' ? 'Tất cả' : f === 'ACTIVE' ? 'Hoạt động' : 'Vô hiệu' }}
          </button>
        </div>
      </div>

      <!-- Table -->
      <div class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/50 border-b border-slate-100">
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Người dùng</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Vai trò</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Trạng thái</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Lần đăng nhập cuối</th>
                <th class="py-4 px-6 font-bold text-slate-500 text-xs uppercase tracking-wider">Cờ bảo mật</th>
                <th class="py-4 px-6"></th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="user in filteredUsers" :key="user.id"
                class="hover:bg-slate-50/50 transition-colors group cursor-pointer" @click="viewUser(user)">
                <td class="py-4 px-6">
                  <div class="flex items-center space-x-3">
                    <div
                      class="w-10 h-10 rounded-xl text-white flex items-center justify-center font-bold text-sm shadow-sm"
                      :class="user.status === 'ACTIVE' ? 'bg-gradient-to-tr from-indigo-500 to-indigo-600' : 'bg-slate-300'">
                      {{ user.fullName.split(' ').pop().charAt(0) }}
                    </div>
                    <div>
                      <div class="font-bold text-slate-900 text-sm">{{ user.fullName }}</div>
                      <div class="text-xs text-slate-400 font-medium">@{{ user.username }}</div>
                    </div>
                  </div>
                </td>
                <td class="py-4 px-6">
                  <span class="px-3 py-1.5 rounded-lg text-xs font-bold ring-1 ring-inset"
                    :class="roleColor(user.role)">
                    {{ user.roleLabel }}
                  </span>
                </td>
                <td class="py-4 px-6">
                  <div class="flex items-center space-x-2">
                    <div class="w-2 h-2 rounded-full"
                      :class="user.status === 'ACTIVE' && !user.locked ? 'bg-emerald-500' : 'bg-rose-400'"></div>
                    <span class="text-sm font-semibold"
                      :class="user.status === 'ACTIVE' && !user.locked ? 'text-emerald-700' : 'text-rose-600'">
                      {{ user.locked ? 'Bị khóa' : user.status === 'ACTIVE' ? 'Hoạt động' : 'Vô hiệu' }}
                    </span>
                  </div>
                </td>
                <td class="py-4 px-6 text-sm font-medium text-slate-500">{{ user.lastLogin }}</td>
                <td class="py-4 px-6">
                  <div class="flex items-center gap-2">
                    <span v-if="user.locked"
                      class="flex items-center gap-1 px-2 py-1 bg-rose-50 text-rose-600 rounded-lg text-xs font-bold ring-1 ring-rose-200">
                      <Lock class="w-3 h-3" /> Khóa
                    </span>
                    <span v-if="user.mustChangePassword"
                      class="flex items-center gap-1 px-2 py-1 bg-amber-50 text-amber-600 rounded-lg text-xs font-bold ring-1 ring-amber-200">
                      <Key class="w-3 h-3" /> Đổi MK
                    </span>
                  </div>
                </td>
                <td class="py-4 px-6 text-right" @click.stop>
                  <div class="flex items-center justify-end gap-1">
                    <button
                      class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                      title="Chỉnh sửa">
                      <Edit class="w-4 h-4" />
                    </button>
                    <button class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                      title="Khóa tài khoản">
                      <Lock class="w-4 h-4" />
                    </button>
                    <button
                      class="p-2 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"
                      title="Reset mật khẩu">
                      <RefreshCw class="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="px-6 py-4 border-t border-slate-100 flex items-center justify-between">
          <span class="text-sm text-slate-500 font-medium">Hiển thị {{ filteredUsers.length }} / {{ users.length }} tài
            khoản</span>
          <div class="flex items-center gap-2">
            <button
              class="px-3 py-1.5 rounded-lg text-sm font-bold bg-white border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-40">←
              Trước</button>
            <button class="px-3 py-1.5 rounded-lg text-sm font-bold bg-indigo-600 text-white">1</button>
            <button
              class="px-3 py-1.5 rounded-lg text-sm font-bold bg-white border border-slate-200 text-slate-600 hover:bg-slate-50">Sau
              →</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create User Modal -->
    <Teleport to="body">
      <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showCreateModal = false"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-lg mx-4 p-8 z-10">
          <h3 class="text-2xl font-black text-slate-900 mb-6">Tạo tài khoản mới</h3>
          <div class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-bold text-slate-700 mb-1.5">Mã nhân viên</label>
                <input v-model="newUser.employeeId" placeholder="VD: NV001"
                  class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
              </div>
              <div>
                <label class="block text-sm font-bold text-slate-700 mb-1.5">Username *</label>
                <input v-model="newUser.username" placeholder="hr.user"
                  class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
              </div>
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1.5">Email *</label>
              <input v-model="newUser.email" type="email" placeholder="user@company.local"
                class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1.5">Số điện thoại</label>
              <input v-model="newUser.phoneNumber" placeholder="0901234567"
                class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1.5">Vai trò *</label>
              <select v-model="newUser.roleCode"
                class="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 bg-white">
                <option value="SYSTEM_ADMIN">Quản trị hệ thống</option>
                <option value="HR">Nhân sự (HR)</option>
                <option value="MANAGER">Trưởng phòng</option>
                <option value="EMPLOYEE">Nhân viên</option>
              </select>
            </div>
            <div class="flex items-center gap-3 p-4 bg-indigo-50 rounded-xl">
              <input type="checkbox" v-model="newUser.sendSetupEmail" id="sendEmail"
                class="w-4 h-4 accent-indigo-600" />
              <label for="sendEmail" class="text-sm font-bold text-slate-700">
                Gửi email thiết lập mật khẩu cho người dùng
              </label>
            </div>
          </div>
          <div class="flex gap-3 mt-8">
            <button @click="showCreateModal = false"
              class="flex-1 py-3 rounded-xl font-bold text-slate-600 border border-slate-200 hover:bg-slate-50 transition-colors">
              Hủy
            </button>
            <button
              class="flex-1 py-3 rounded-xl font-bold text-white bg-indigo-600 hover:bg-indigo-700 transition-colors shadow-lg shadow-indigo-200">
              Tạo tài khoản
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- User Detail Drawer -->
    <Teleport to="body">
      <div v-if="showDetail && selectedUser" class="fixed inset-0 z-50 flex">
        <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showDetail = false"></div>
        <div class="relative ml-auto bg-white w-full max-w-md h-full overflow-y-auto shadow-2xl z-10 p-8">
          <div class="flex items-center justify-between mb-8">
            <h3 class="text-xl font-black text-slate-900">Chi tiết tài khoản</h3>
            <button @click="showDetail = false" class="p-2 hover:bg-slate-100 rounded-xl text-slate-500">✕</button>
          </div>
          <div class="flex flex-col items-center mb-8">
            <div
              class="w-20 h-20 rounded-2xl text-white flex items-center justify-center font-black text-2xl mb-4 shadow-lg"
              :class="selectedUser.status === 'ACTIVE' ? 'bg-gradient-to-tr from-indigo-500 to-indigo-600' : 'bg-slate-300'">
              {{ selectedUser.fullName.split(' ').pop().charAt(0) }}
            </div>
            <h4 class="text-2xl font-black text-slate-900">{{ selectedUser.fullName }}</h4>
            <p class="text-slate-400 font-medium">@{{ selectedUser.username }}</p>
            <span class="mt-3 px-3 py-1.5 rounded-lg text-xs font-bold ring-1 ring-inset"
              :class="roleColor(selectedUser.role)">
              {{ selectedUser.roleLabel }}
            </span>
          </div>
          <div class="space-y-4">
            <div class="flex justify-between py-3 border-b border-slate-100">
              <span class="text-sm font-bold text-slate-500">Email</span>
              <span class="text-sm font-bold text-slate-900">{{ selectedUser.email }}</span>
            </div>
            <div class="flex justify-between py-3 border-b border-slate-100">
              <span class="text-sm font-bold text-slate-500">Điện thoại</span>
              <span class="text-sm font-bold text-slate-900">{{ selectedUser.phone }}</span>
            </div>
            <div class="flex justify-between py-3 border-b border-slate-100">
              <span class="text-sm font-bold text-slate-500">Trạng thái</span>
              <span class="text-sm font-bold"
                :class="selectedUser.status === 'ACTIVE' ? 'text-emerald-600' : 'text-rose-600'">
                {{ selectedUser.status === 'ACTIVE' ? 'Hoạt động' : 'Vô hiệu' }}
              </span>
            </div>
            <div class="flex justify-between py-3 border-b border-slate-100">
              <span class="text-sm font-bold text-slate-500">Đăng nhập cuối</span>
              <span class="text-sm font-medium text-slate-600">{{ selectedUser.lastLogin }}</span>
            </div>
            <div class="flex justify-between py-3 border-b border-slate-100">
              <span class="text-sm font-bold text-slate-500">Tài khoản bị khóa</span>
              <span class="text-sm font-bold" :class="selectedUser.locked ? 'text-rose-600' : 'text-emerald-600'">
                {{ selectedUser.locked ? 'Có' : 'Không' }}
              </span>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3 mt-8">
            <button
              class="flex items-center justify-center gap-2 p-3 bg-amber-50 text-amber-600 rounded-xl font-bold text-sm hover:bg-amber-100 transition-colors">
              <Lock class="w-4 h-4" />
              {{ selectedUser.locked ? 'Mở khóa' : 'Khóa tài khoản' }}
            </button>
            <button
              class="flex items-center justify-center gap-2 p-3 bg-indigo-50 text-indigo-600 rounded-xl font-bold text-sm hover:bg-indigo-100 transition-colors">
              <RefreshCw class="w-4 h-4" /> Reset mật khẩu
            </button>
            <button
              class="flex items-center justify-center gap-2 p-3 bg-sky-50 text-sky-600 rounded-xl font-bold text-sm hover:bg-sky-100 transition-colors">
              <Edit class="w-4 h-4" /> Chỉnh sửa
            </button>
            <button
              class="flex items-center justify-center gap-2 p-3 bg-purple-50 text-purple-600 rounded-xl font-bold text-sm hover:bg-purple-100 transition-colors">
              <ShieldCheck class="w-4 h-4" /> Đổi vai trò
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  
</template>
