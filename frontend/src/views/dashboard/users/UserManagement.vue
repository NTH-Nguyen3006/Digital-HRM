<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import {
  Plus, Search, ShieldCheck, Lock, RefreshCw,
  Edit, Loader2, Mail, Phone, Key
} from 'lucide-vue-next'
import { useDebounce } from '@/composables/useDebounce'
import { usePagination } from '@/composables/usePagination'
import { useToast } from '@/composables/useToast'
import { useUiStore } from '@/stores/ui'
import {
  getUsers,
  getUserDetail,
  createUser,
  updateUser,
  lockOrUnlockUser,
  assignPrimaryRole,
} from '@/api/admin/adminUser'
import { getRoles } from '@/api/admin/role'

const toast = useToast()
const ui = useUiStore()

const loading = ref(false)
const detailLoading = ref(false)
const createLoading = ref(false)
const savingUser = ref(false)
const assigningRole = ref(false)
const actionLoading = ref(false)

const searchQuery = ref('')
const { debouncedValue: debouncedSearch } = useDebounce(searchQuery, 400)
const filterStatus = ref('ALL')
const filterRole = ref('ALL')

const users = ref([])
const roles = ref([])

const showCreateModal = ref(false)
const showDetail = ref(false)
const selectedUser = ref(null)
const editableUser = ref(null)
const selectedRoleCode = ref('')

const newUser = ref({
  employeeId: '',
  username: '',
  email: '',
  phoneNumber: '',
  status: 'ACTIVE',
  roleCode: 'EMPLOYEE',
  sendSetupEmail: true,
  initialPassword: '',
})

const {
  currentPage,
  totalElements,
  totalPages,
  visiblePages,
  setPage,
  resetPage,
  setMeta,
  isFirstPage,
  isLastPage,
} = usePagination({ initialSize: 10 })

const roleColorMap = {
  ADMIN: 'bg-rose-50 text-rose-700 ring-rose-600/20',
  HR: 'bg-indigo-50 text-indigo-700 ring-indigo-600/20',
  MANAGER: 'bg-sky-50 text-sky-700 ring-sky-600/20',
  EMPLOYEE: 'bg-slate-100 text-slate-600 ring-slate-500/20',
}

const stats = computed(() => ({
  total: totalElements.value,
  active: users.value.filter((user) => user.status === 'ACTIVE').length,
  locked: users.value.filter((user) => user.status === 'LOCKED').length,
  pendingReset: users.value.filter((user) => user.mustChangePassword).length,
}))

const roleOptions = computed(() => roles.value.filter((role) => role.status === 'ACTIVE'))

function roleColor(roleCode) {
  return roleColorMap[roleCode] || 'bg-slate-100 text-slate-600 ring-slate-500/20'
}

function formatDateTime(value) {
  if (!value) return 'Chưa phát sinh'
  return new Date(value).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function getStatusLabel(status) {
  if (status === 'LOCKED') return 'Bị khóa'
  if (status === 'ACTIVE') return 'Hoạt động'
  return 'Vô hiệu'
}

function normalizeUser(item) {
  return {
    userId: item.userId,
    employeeId: item.employeeId,
    employeeCode: item.employeeCode,
    username: item.username,
    email: item.email,
    phoneNumber: item.phoneNumber,
    status: item.status,
    mustChangePassword: item.mustChangePassword,
    roleCode: item.primaryRoleCode,
    roleName: item.primaryRoleName,
    lastLoginAt: item.lastLoginAt,
    lastLoginIp: item.lastLoginIp,
  }
}

function getCurrentRole(detail) {
  return detail.roleHistory?.find((item) => item.primaryRole && item.status === 'ACTIVE')
    || detail.roleHistory?.[0]
    || null
}

function applySelectedUser(detail) {
  const currentRole = getCurrentRole(detail)
  selectedUser.value = {
    userId: detail.userId,
    employeeId: detail.employeeId,
    employeeCode: detail.employeeCode,
    username: detail.username,
    email: detail.email,
    phoneNumber: detail.phoneNumber,
    status: detail.status,
    mustChangePassword: detail.mustChangePassword,
    mfaEnabled: detail.mfaEnabled,
    passwordChangedAt: detail.passwordChangedAt,
    lastLoginAt: detail.lastLoginAt,
    lastLoginIp: detail.lastLoginIp,
    failedLoginCount: detail.failedLoginCount,
    lockedUntil: detail.lockedUntil,
    roleHistory: detail.roleHistory || [],
    roleCode: currentRole?.roleCode || '',
    roleName: currentRole?.roleName || 'Chưa gán role',
  }

  editableUser.value = {
    employeeId: detail.employeeId ?? '',
    username: detail.username ?? '',
    email: detail.email ?? '',
    phoneNumber: detail.phoneNumber ?? '',
    status: detail.status ?? 'ACTIVE',
    mustChangePassword: detail.mustChangePassword ?? false,
  }
  selectedRoleCode.value = currentRole?.roleCode || ''
}

async function fetchRoles() {
  try {
    const res = await getRoles()
    roles.value = res.data || []
  } catch (error) {
    toast.error('Không thể tải danh sách role')
  }
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUsers({
      keyword: debouncedSearch.value || undefined,
      status: filterStatus.value === 'ALL' ? undefined : filterStatus.value,
      roleCode: filterRole.value === 'ALL' ? undefined : filterRole.value,
      page: currentPage.value,
      size: 10,
    })

    users.value = (res.data?.items || []).map(normalizeUser)
    setMeta({
      totalElements: res.data?.totalElements ?? users.value.length,
      totalPages: res.data?.totalPages ?? 1,
    })
  } catch (error) {
    toast.error('Không thể tải danh sách tài khoản')
  } finally {
    loading.value = false
  }
}

async function openUserDetail(userId) {
  detailLoading.value = true
  showDetail.value = true
  try {
    const res = await getUserDetail(userId)
    applySelectedUser(res.data)
  } catch (error) {
    toast.error('Không thể tải chi tiết tài khoản')
    showDetail.value = false
  } finally {
    detailLoading.value = false
  }
}

function resetCreateForm() {
  newUser.value = {
    employeeId: '',
    username: '',
    email: '',
    phoneNumber: '',
    status: 'ACTIVE',
    roleCode: 'EMPLOYEE',
    sendSetupEmail: true,
    initialPassword: '',
  }
}

async function handleCreateUser() {
  if (!newUser.value.username.trim()) {
    toast.error('Username là bắt buộc')
    return
  }

  if (!newUser.value.sendSetupEmail && !newUser.value.initialPassword.trim()) {
    toast.error('Cần nhập mật khẩu ban đầu khi không gửi email thiết lập')
    return
  }

  createLoading.value = true
  try {
    await createUser({
      employeeId: newUser.value.employeeId ? Number(newUser.value.employeeId) : null,
      username: newUser.value.username.trim(),
      email: newUser.value.email.trim() || null,
      phoneNumber: newUser.value.phoneNumber.trim() || null,
      status: newUser.value.status,
      roleCode: newUser.value.roleCode,
      initialPassword: newUser.value.sendSetupEmail ? null : newUser.value.initialPassword,
      sendSetupEmail: newUser.value.sendSetupEmail,
    })
    toast.success('Tạo tài khoản thành công')
    showCreateModal.value = false
    resetCreateForm()
    resetPage()
    await fetchUsers()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Tạo tài khoản thất bại')
  } finally {
    createLoading.value = false
  }
}

async function handleSaveUser() {
  if (!selectedUser.value || !editableUser.value) return

  savingUser.value = true
  try {
    const res = await updateUser(selectedUser.value.userId, {
      employeeId: editableUser.value.employeeId ? Number(editableUser.value.employeeId) : null,
      username: editableUser.value.username.trim(),
      email: editableUser.value.email.trim() || null,
      phoneNumber: editableUser.value.phoneNumber.trim() || null,
      status: editableUser.value.status,
      mustChangePassword: editableUser.value.mustChangePassword,
    })
    applySelectedUser(res.data)
    await fetchUsers()
    toast.success('Đã cập nhật tài khoản')
  } catch (error) {
    toast.error(error.response?.data?.message || 'Cập nhật tài khoản thất bại')
  } finally {
    savingUser.value = false
  }
}

async function handleToggleLock() {
  if (!selectedUser.value) return

  const locked = selectedUser.value.status !== 'LOCKED'
  const confirmed = await ui.confirm({
    title: locked ? 'Khóa tài khoản?' : 'Mở khóa tài khoản?',
    message: `Tài khoản @${selectedUser.value.username} sẽ ${locked ? 'bị khóa' : 'được mở khóa'} ngay lập tức.`,
    danger: locked,
    confirmLabel: locked ? 'Khóa tài khoản' : 'Mở khóa',
  })

  if (!confirmed) return

  actionLoading.value = true
  try {
    const res = await lockOrUnlockUser(selectedUser.value.userId, {
      locked,
      reason: locked
        ? 'Khóa tài khoản từ màn hình quản trị người dùng'
        : 'Mở khóa tài khoản từ màn hình quản trị người dùng',
      lockedUntil: null,
    })
    applySelectedUser(res.data)
    await fetchUsers()
    toast.success(locked ? 'Đã khóa tài khoản' : 'Đã mở khóa tài khoản')
  } catch (error) {
    toast.error(error.response?.data?.message || 'Cập nhật trạng thái khóa thất bại')
  } finally {
    actionLoading.value = false
  }
}

async function handleAssignRole() {
  if (!selectedUser.value || !selectedRoleCode.value || selectedRoleCode.value === selectedUser.value.roleCode) return

  assigningRole.value = true
  try {
    const res = await assignPrimaryRole(selectedUser.value.userId, {
      roleCode: selectedRoleCode.value,
      reason: 'Điều chỉnh role chính từ màn hình quản trị người dùng',
    })
    applySelectedUser(res.data)
    await fetchUsers()
    toast.success('Đã cập nhật role chính')
  } catch (error) {
    toast.error(error.response?.data?.message || 'Gán role thất bại')
  } finally {
    assigningRole.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchRoles(), fetchUsers()])
})

watch([debouncedSearch, filterStatus, filterRole], () => {
  resetPage()
  fetchUsers()
})

watch(currentPage, fetchUsers)
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <h2 class="text-3xl font-black tracking-tight text-slate-900">Tài khoản hệ thống</h2>
        <p class="mt-1 font-medium text-slate-500">Quản lý user, role chính và trạng thái truy cập nền tảng</p>
      </div>
      <button @click="showCreateModal = true"
        class="flex items-center rounded-xl bg-indigo-600 px-5 py-2.5 font-bold text-white shadow-lg shadow-indigo-200 transition-all hover:bg-indigo-700">
        <Plus class="mr-2 h-5 w-5" /> Tạo tài khoản
      </button>
    </div>

    <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div class="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
        <div class="mb-1 text-xs font-bold uppercase tracking-wider text-slate-400">Tổng tài khoản</div>
        <div class="text-3xl font-black text-slate-900">{{ stats.total }}</div>
      </div>
      <div class="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
        <div class="mb-1 text-xs font-bold uppercase tracking-wider text-slate-400">Đang hoạt động</div>
        <div class="text-3xl font-black text-emerald-600">{{ stats.active }}</div>
      </div>
      <div class="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
        <div class="mb-1 text-xs font-bold uppercase tracking-wider text-slate-400">Đang bị khóa</div>
        <div class="text-3xl font-black text-rose-500">{{ stats.locked }}</div>
      </div>
      <div class="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
        <div class="mb-1 text-xs font-bold uppercase tracking-wider text-slate-400">Cần đổi mật khẩu</div>
        <div class="text-3xl font-black text-amber-500">{{ stats.pendingReset }}</div>
      </div>
    </div>

    <div class="flex flex-col gap-3 lg:flex-row">
      <div class="relative flex-1">
        <Search class="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
        <input v-model="searchQuery" type="text" placeholder="Tìm theo username, email, số điện thoại..."
          class="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
      </div>
      <select v-model="filterRole"
        class="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-bold text-slate-600 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
        <option value="ALL">Tất cả role</option>
        <option v-for="role in roleOptions" :key="role.roleId" :value="role.roleCode">
          {{ role.roleName }}
        </option>
      </select>
      <div class="flex gap-2">
        <button v-for="status in ['ALL', 'ACTIVE', 'LOCKED', 'INACTIVE']" :key="status" @click="filterStatus = status"
          class="rounded-xl px-4 py-2.5 text-sm font-bold transition-all" :class="filterStatus === status
            ? 'bg-indigo-600 text-white shadow-md shadow-indigo-200'
            : 'border border-slate-200 bg-white text-slate-600 hover:bg-slate-50'">
          {{ status === 'ALL' ? 'Tất cả' : status === 'ACTIVE' ? 'Hoạt động' : status === 'LOCKED' ? 'Bị khóa' : 'Vô hiệu' }}
        </button>
      </div>
    </div>

    <div class="overflow-hidden rounded-3xl border border-slate-100 bg-white shadow-sm">
      <div v-if="loading" class="flex min-h-70 items-center justify-center">
        <Loader2 class="h-8 w-8 animate-spin text-indigo-600" />
      </div>

      <template v-else>
        <div class="overflow-x-auto">
          <table class="w-full border-collapse text-left">
            <thead>
              <tr class="border-b border-slate-100 bg-slate-50/50">
                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Tài khoản</th>
                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Role chính</th>
                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Trạng thái</th>
                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Lần đăng nhập cuối</th>
                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Bảo mật</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="user in users" :key="user.userId" class="cursor-pointer transition-colors hover:bg-slate-50/60"
                @click="openUserDetail(user.userId)">
                <td class="px-6 py-4">
                  <div class="flex items-center gap-3">
                    <div
                      class="flex h-10 w-10 items-center justify-center rounded-xl text-sm font-bold text-white shadow-sm"
                      :class="user.status === 'ACTIVE' ? 'bg-gradient-to-tr from-indigo-500 to-indigo-600' : 'bg-slate-300'">
                      {{ user.username?.[0]?.toUpperCase() || '?' }}
                    </div>
                    <div>
                      <div class="font-bold text-slate-900">@{{ user.username }}</div>
                      <div class="text-xs font-medium text-slate-400">
                        {{ user.email || 'Chưa có email' }}
                        <span v-if="user.employeeCode" class="ml-2">• {{ user.employeeCode }}</span>
                      </div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <span class="rounded-lg px-3 py-1.5 text-xs font-bold ring-1 ring-inset"
                    :class="roleColor(user.roleCode)">
                    {{ user.roleName || user.roleCode || 'Chưa gán role' }}
                  </span>
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center gap-2">
                    <div class="h-2 w-2 rounded-full"
                      :class="user.status === 'ACTIVE' ? 'bg-emerald-500' : user.status === 'LOCKED' ? 'bg-rose-500' : 'bg-slate-400'" />
                    <span class="text-sm font-semibold"
                      :class="user.status === 'ACTIVE' ? 'text-emerald-700' : user.status === 'LOCKED' ? 'text-rose-600' : 'text-slate-500'">
                      {{ getStatusLabel(user.status) }}
                    </span>
                  </div>
                </td>
                <td class="px-6 py-4 text-sm font-medium text-slate-500">{{ formatDateTime(user.lastLoginAt) }}</td>
                <td class="px-6 py-4">
                  <div class="flex flex-wrap gap-2">
                    <span v-if="user.status === 'LOCKED'"
                      class="flex items-center gap-1 rounded-lg bg-rose-50 px-2 py-1 text-xs font-bold text-rose-600 ring-1 ring-rose-200">
                      <Lock class="h-3 w-3" /> Khóa
                    </span>
                    <span v-if="user.mustChangePassword"
                      class="flex items-center gap-1 rounded-lg bg-amber-50 px-2 py-1 text-xs font-bold text-amber-600 ring-1 ring-amber-200">
                      <Key class="h-3 w-3" /> Đổi MK
                    </span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex items-center justify-between border-t border-slate-100 px-6 py-4">
          <span class="text-sm font-medium text-slate-500">Hiển thị {{ users.length }} / {{ totalElements }} tài
            khoản</span>
          <div class="flex items-center gap-2">
            <button :disabled="isFirstPage" @click="setPage(currentPage - 1)"
              class="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm font-bold text-slate-600 transition-colors hover:bg-slate-50 disabled:opacity-40">
              ← Trước
            </button>
            <template v-for="page in visiblePages" :key="page">
              <span v-if="page === '...'" class="px-1 text-slate-300">...</span>
              <button v-else @click="setPage(page)" class="rounded-lg px-3 py-1.5 text-sm font-bold"
                :class="currentPage === page ? 'bg-indigo-600 text-white' : 'border border-slate-200 bg-white text-slate-600 hover:bg-slate-50'">
                {{ page + 1 }}
              </button>
            </template>
            <button :disabled="isLastPage || totalPages === 0" @click="setPage(currentPage + 1)"
              class="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm font-bold text-slate-600 transition-colors hover:bg-slate-50 disabled:opacity-40">
              Sau →
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>

  <Teleport to="body">
    <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showCreateModal = false" />
      <div class="relative z-10 mx-4 w-full max-w-lg rounded-3xl bg-white p-8 shadow-2xl">
        <h3 class="mb-6 text-2xl font-black text-slate-900">Tạo tài khoản mới</h3>

        <div class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="mb-1.5 block text-sm font-bold text-slate-700">ID nhân sự liên kết</label>
              <input v-model="newUser.employeeId" placeholder="VD: 1001"
                class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-bold text-slate-700">Username *</label>
              <input v-model="newUser.username" placeholder="admin.user"
                class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
            </div>
          </div>

          <div>
            <label class="mb-1.5 block text-sm font-bold text-slate-700">Email</label>
            <input v-model="newUser.email" type="email" placeholder="user@company.local"
              class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="mb-1.5 block text-sm font-bold text-slate-700">Số điện thoại</label>
              <input v-model="newUser.phoneNumber" placeholder="0901234567"
                class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-bold text-slate-700">Trạng thái</label>
              <select v-model="newUser.status"
                class="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                <option value="ACTIVE">Hoạt động</option>
                <option value="INACTIVE">Vô hiệu</option>
              </select>
            </div>
          </div>

          <div>
            <label class="mb-1.5 block text-sm font-bold text-slate-700">Role chính *</label>
            <select v-model="newUser.roleCode"
              class="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
              <option v-for="role in roleOptions" :key="role.roleId" :value="role.roleCode">
                {{ role.roleName }}
              </option>
            </select>
          </div>

          <div class="rounded-xl bg-indigo-50 p-4">
            <label class="flex items-center gap-3 text-sm font-bold text-slate-700">
              <input v-model="newUser.sendSetupEmail" type="checkbox" class="h-4 w-4 accent-indigo-600" />
              Gửi email thiết lập mật khẩu cho người dùng
            </label>
          </div>

          <div v-if="!newUser.sendSetupEmail">
            <label class="mb-1.5 block text-sm font-bold text-slate-700">Mật khẩu ban đầu *</label>
            <input v-model="newUser.initialPassword" type="password" placeholder="Nhập mật khẩu tạm thời"
              class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
          </div>
        </div>

        <div class="mt-8 flex gap-3">
          <button @click="showCreateModal = false"
            class="flex-1 rounded-xl border border-slate-200 py-3 font-bold text-slate-600 transition-colors hover:bg-slate-50">
            Hủy
          </button>
          <button :disabled="createLoading" @click="handleCreateUser"
            class="flex flex-1 items-center justify-center rounded-xl bg-indigo-600 py-3 font-bold text-white shadow-lg shadow-indigo-200 transition-colors hover:bg-indigo-700 disabled:opacity-60">
            <Loader2 v-if="createLoading" class="mr-2 h-4 w-4 animate-spin" />
            Tạo tài khoản
          </button>
        </div>
      </div>
    </div>
  </Teleport>

  <Teleport to="body">
    <div v-if="showDetail" class="fixed inset-0 z-50 flex">
      <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showDetail = false" />
      <div class="relative z-10 ml-auto h-full w-full max-w-xl overflow-y-auto bg-white p-8 shadow-2xl">
        <div v-if="detailLoading" class="flex h-full min-h-80 items-center justify-center">
          <Loader2 class="h-8 w-8 animate-spin text-indigo-600" />
        </div>

        <template v-else-if="selectedUser && editableUser">
          <div class="mb-8 flex items-center justify-between">
            <div>
              <h3 class="text-xl font-black text-slate-900">Chi tiết tài khoản</h3>
              <p class="text-sm font-medium text-slate-400">@{{ selectedUser.username }}</p>
            </div>
            <button @click="showDetail = false" class="rounded-xl p-2 text-slate-500 hover:bg-slate-100">✕</button>
          </div>

          <div class="mb-8 flex flex-col items-center">
            <div
              class="mb-4 flex h-20 w-20 items-center justify-center rounded-2xl bg-gradient-to-tr from-indigo-500 to-indigo-600 text-2xl font-black text-white shadow-lg">
              {{ selectedUser.username?.[0]?.toUpperCase() || '?' }}
            </div>
            <h4 class="text-2xl font-black text-slate-900">@{{ selectedUser.username }}</h4>
            <span class="mt-3 rounded-lg px-3 py-1.5 text-xs font-bold ring-1 ring-inset"
              :class="roleColor(selectedUser.roleCode)">
              {{ selectedUser.roleName }}
            </span>
          </div>

          <div class="space-y-6">
            <div class="rounded-2xl border border-slate-100 bg-slate-50/60 p-5">
              <div class="mb-4 flex items-center gap-2 text-sm font-black text-slate-800">
                <Edit class="h-4 w-4" /> Thông tin cơ bản
              </div>

              <div class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="mb-1.5 block text-sm font-bold text-slate-700">Mã nhân viên</label>
                    <div
                      class="rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm font-bold text-slate-700">
                      {{ selectedUser.employeeCode || 'Chưa liên kết nhân sự' }}
                    </div>
                  </div>
                  <div>
                    <label class="mb-1.5 block text-sm font-bold text-slate-700">Username</label>
                    <input v-model="editableUser.username"
                      class="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="mb-1.5 block text-sm font-bold text-slate-700">Email</label>
                    <input v-model="editableUser.email"
                      class="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                  </div>
                  <div>
                    <label class="mb-1.5 block text-sm font-bold text-slate-700">Số điện thoại</label>
                    <input v-model="editableUser.phoneNumber"
                      class="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="mb-1.5 block text-sm font-bold text-slate-700">Trạng thái</label>
                    <select v-model="editableUser.status"
                      class="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                      <option value="ACTIVE">Hoạt động</option>
                      <option value="INACTIVE">Vô hiệu</option>
                    </select>
                  </div>
                  <label class="mt-7 flex items-center gap-3 text-sm font-bold text-slate-700">
                    <input v-model="editableUser.mustChangePassword" type="checkbox"
                      class="h-4 w-4 accent-indigo-600" />
                    Bắt đổi mật khẩu ở lần đăng nhập sau
                  </label>
                </div>
              </div>
            </div>

            <div class="rounded-2xl border border-slate-100 bg-white p-5">
              <div class="mb-4 flex items-center gap-2 text-sm font-black text-slate-800">
                <ShieldCheck class="h-4 w-4" /> Role chính
              </div>
              <div class="flex gap-3">
                <select v-model="selectedRoleCode"
                  class="flex-1 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                  <option v-for="role in roleOptions" :key="role.roleId" :value="role.roleCode">
                    {{ role.roleName }}
                  </option>
                </select>
                <button :disabled="assigningRole || !selectedRoleCode || selectedRoleCode === selectedUser.roleCode"
                  @click="handleAssignRole"
                  class="rounded-xl bg-violet-600 px-4 py-2.5 text-sm font-bold text-white transition-colors hover:bg-violet-700 disabled:opacity-50">
                  <Loader2 v-if="assigningRole" class="h-4 w-4 animate-spin" />
                  <span v-else>Lưu role</span>
                </button>
              </div>
            </div>

            <div class="rounded-2xl border border-slate-100 bg-white p-5">
              <div class="mb-4 text-sm font-black text-slate-800">Thông tin truy cập</div>
              <div class="space-y-3 text-sm">
                <div class="flex items-center justify-between border-b border-slate-100 pb-3">
                  <span class="text-slate-500">Email</span>
                  <span class="flex items-center gap-2 font-semibold text-slate-900">
                    <Mail class="h-4 w-4 text-slate-400" />{{ selectedUser.email || 'Chưa có' }}
                  </span>
                </div>
                <div class="flex items-center justify-between border-b border-slate-100 pb-3">
                  <span class="text-slate-500">Điện thoại</span>
                  <span class="flex items-center gap-2 font-semibold text-slate-900">
                    <Phone class="h-4 w-4 text-slate-400" />{{ selectedUser.phoneNumber || 'Chưa có' }}
                  </span>
                </div>
                <div class="flex items-center justify-between border-b border-slate-100 pb-3">
                  <span class="text-slate-500">Đăng nhập cuối</span>
                  <span class="font-semibold text-slate-900">{{ formatDateTime(selectedUser.lastLoginAt) }}</span>
                </div>
                <div class="flex items-center justify-between border-b border-slate-100 pb-3">
                  <span class="text-slate-500">IP đăng nhập cuối</span>
                  <span class="font-semibold text-slate-900">{{ selectedUser.lastLoginIp || 'Chưa có' }}</span>
                </div>
                <div class="flex items-center justify-between border-b border-slate-100 pb-3">
                  <span class="text-slate-500">Số lần sai mật khẩu</span>
                  <span class="font-semibold text-slate-900">{{ selectedUser.failedLoginCount }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-500">Khóa đến</span>
                  <span class="font-semibold text-slate-900">{{ formatDateTime(selectedUser.lockedUntil) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-8 grid grid-cols-2 gap-3">
            <button :disabled="actionLoading" @click="handleToggleLock"
              class="flex items-center justify-center gap-2 rounded-xl bg-amber-50 p-3 text-sm font-bold text-amber-600 transition-colors hover:bg-amber-100 disabled:opacity-60">
              <Lock class="h-4 w-4" />
              {{ selectedUser.status === 'LOCKED' ? 'Mở khóa' : 'Khóa tài khoản' }}
            </button>
            <button :disabled="savingUser" @click="handleSaveUser"
              class="flex items-center justify-center gap-2 rounded-xl bg-indigo-600 p-3 text-sm font-bold text-white transition-colors hover:bg-indigo-700 disabled:opacity-60">
              <RefreshCw v-if="savingUser" class="h-4 w-4 animate-spin" />
              <Edit v-else class="h-4 w-4" />
              Lưu thay đổi
            </button>
          </div>
        </template>
      </div>
    </div>
  </Teleport>
</template>
