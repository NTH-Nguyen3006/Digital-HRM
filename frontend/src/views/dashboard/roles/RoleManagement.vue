<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useToast } from '@/composables/useToast'
import { usePagination } from '@/composables/usePagination'
import { useUiStore } from '@/stores/ui'
import { getRoles, getRoleDetail, changeRoleStatus } from '@/api/admin/role'
import { getPermissionMatrix, getRoleMenuConfigs } from '@/api/admin/systemConfig'
import {
  ShieldCheck, Users, Check, X, ChevronRight,
  Loader2, Lock, Unlock, LayoutGrid, PanelsTopLeft
} from 'lucide-vue-next'

/* ─── STORE / COMPOSABLES ───────────────────────────────────── */
const toast  = useToast()
const ui     = useUiStore()

/* ─── STATE ─────────────────────────────────────────────────── */
const roles = ref([])
const loading = ref(false)
const selectedRole = ref(null)
const showPanel = ref(false)
const panelLoading = ref(false)
const permissionMatrix = ref([])
const permissionMatrixLoading = ref(false)
const roleMenuConfigs = ref([])
const {
  currentPage: permissionMatrixPage,
  pageSize: permissionMatrixPageSize,
  totalElements: permissionMatrixTotal,
  totalPages: permissionMatrixTotalPages,
  isFirstPage: isFirstPermissionPage,
  isLastPage: isLastPermissionPage,
  visiblePages: visiblePermissionPages,
  setPage: setPermissionPage,
  nextPage: nextPermissionPage,
  prevPage: prevPermissionPage,
  resetPage: resetPermissionPage,
} = usePagination({ initialSize: 10 })

/* ─── CONFIG ─────────────────────────────────────────────────── */
const roleColorMap = {
  ADMIN:    { bg: 'bg-rose-100',    text: 'text-rose-700',    dot: 'bg-rose-500' },
  HR:       { bg: 'bg-indigo-100',  text: 'text-indigo-700',  dot: 'bg-indigo-500' },
  MANAGER:  { bg: 'bg-violet-100',  text: 'text-violet-700',  dot: 'bg-violet-500' },
  EMPLOYEE: { bg: 'bg-emerald-100', text: 'text-emerald-700', dot: 'bg-emerald-500' },
}

function getRoleColors(code) {
  return roleColorMap[code] || { bg: 'bg-slate-100', text: 'text-slate-600', dot: 'bg-slate-400' }
}

function groupPermissions(permissions = []) {
  const groups = {}
  for (const p of permissions) {
    if (!groups[p.moduleCode]) groups[p.moduleCode] = []
    groups[p.moduleCode].push(p)
  }
  return groups
}

function formatModuleName(moduleCode = '') {
  return moduleCode
    .toLowerCase()
    .split('_')
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function getFeatureStats(role) {
  const permissions = role?.permissions || []
  const enabledPermissions = permissions.filter((permission) => permission.allowed)
  const enabledModules = new Set(enabledPermissions.map((permission) => permission.moduleCode))

  return {
    featureCount: enabledPermissions.length,
    moduleCount: enabledModules.size,
    menuCount: roleMenuConfigs.value.filter((menu) => menu.visible && menu.status === 'ACTIVE').length,
  }
}

function getEnabledFeatureGroups(role) {
  const permissions = role?.permissions || []
  const enabledGroups = groupPermissions(permissions.filter((permission) => permission.allowed))

  return Object.entries(enabledGroups)
    .map(([moduleCode, items]) => ({
      moduleCode,
      moduleLabel: formatModuleName(moduleCode),
      items,
    }))
    .sort((left, right) => left.moduleLabel.localeCompare(right.moduleLabel))
}

const paginatedPermissionMatrix = computed(() => {
  const start = permissionMatrixPage.value * permissionMatrixPageSize.value
  return permissionMatrix.value.slice(start, start + permissionMatrixPageSize.value)
})

const permissionMatrixRange = computed(() => {
  if (!permissionMatrixTotal.value) {
    return { from: 0, to: 0 }
  }

  const from = permissionMatrixPage.value * permissionMatrixPageSize.value + 1
  const to = Math.min(from + permissionMatrixPageSize.value - 1, permissionMatrixTotal.value)
  return { from, to }
})

watch(permissionMatrix, (rows) => {
  permissionMatrixTotal.value = rows.length
  permissionMatrixTotalPages.value = Math.max(1, Math.ceil(rows.length / permissionMatrixPageSize.value))
  if (permissionMatrixPage.value > permissionMatrixTotalPages.value - 1) {
    resetPermissionPage()
  }
}, { immediate: true })

watch(permissionMatrixPageSize, (size) => {
  permissionMatrixTotalPages.value = Math.max(1, Math.ceil(permissionMatrix.value.length / size))
  resetPermissionPage()
})

/* ─── API ────────────────────────────────────────────────────── */
const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await getRoles()
    roles.value = res.data || []
  } catch (e) {
    toast.error('Không thể tải danh sách phân quyền')
  } finally {
    loading.value = false
  }
}

const fetchPermissionMatrix = async () => {
  permissionMatrixLoading.value = true
  try {
    const res = await getPermissionMatrix()
    permissionMatrix.value = res.data || []
  } catch (e) {
    permissionMatrix.value = []
    toast.error('Không thể tải ma trận tính năng theo role')
  } finally {
    permissionMatrixLoading.value = false
  }
}

const openPanel = async (role) => {
  panelLoading.value = true
  showPanel.value = true
  try {
    const [detailRes, menuRes] = await Promise.all([
      getRoleDetail(role.roleId),
      getRoleMenuConfigs(role.roleId),
    ])
    selectedRole.value = detailRes.data
    roleMenuConfigs.value = menuRes.data || []
  } catch (e) {
    toast.error('Không thể tải chi tiết role')
    showPanel.value = false
    roleMenuConfigs.value = []
  } finally {
    panelLoading.value = false
  }
}

const toggleRoleStatus = async (role) => {
  const newStatus = role.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const confirmed = await ui.confirm({
    title: newStatus === 'INACTIVE' ? 'Vô hiệu hóa role?' : 'Kích hoạt role?',
    message: `Role "${role.roleName}" sẽ ${newStatus === 'INACTIVE' ? 'bị vô hiệu hóa' : 'được kích hoạt trở lại'}.`,
    danger: newStatus === 'INACTIVE',
    confirmLabel: newStatus === 'INACTIVE' ? 'Vô hiệu hóa' : 'Kích hoạt',
  })
  if (!confirmed) return

  try {
    await changeRoleStatus(role.roleId, {
      status: newStatus,
      reason: `Cập nhật trạng thái role ${role.roleCode} từ màn hình quản trị`,
    })
    await fetchRoles()
    toast.success(`Đã ${newStatus === 'INACTIVE' ? 'vô hiệu hóa' : 'kích hoạt'} role thành công`)
    if (selectedRole.value?.roleId === role.roleId) {
      selectedRole.value = { ...selectedRole.value, status: newStatus }
    }
  } catch (e) {
    toast.error('Cập nhật trạng thái thất bại')
  }
}

onMounted(async () => {
  await Promise.all([
    fetchRoles(),
    fetchPermissionMatrix(),
  ])
})
</script>

<template>
  
    <div class="space-y-8 animate-fade-in">

      <!-- HEADER -->
      <PageHeader
        title="Phân quyền Hệ thống"
        subtitle="Phân tích tính năng HR và đồng bộ dữ liệu role, permission, menu từ backend ra giao diện"
        :icon="ShieldCheck"
        iconColor="bg-violet-600"
        iconShadow="shadow-violet-100"
      />

      <!-- ROLE CARDS -->
      <div class="relative min-h-[300px]">
        <div v-if="loading" class="absolute inset-0 flex items-center justify-center">
          <Loader2 class="w-10 h-10 text-violet-600 animate-spin" />
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
          <div
            v-for="role in roles"
            :key="role.roleId"
            @click="openPanel(role)"
            class="group bg-white rounded-[28px] border border-slate-100 p-6 cursor-pointer hover:border-violet-200 hover:shadow-[0_20px_40px_rgba(139,92,246,0.08)] transition-all duration-300"
          >
            <div class="flex justify-end mb-4">
              <span class="inline-flex items-center gap-1 whitespace-nowrap px-2.5 py-1 rounded-lg text-[10px] font-black leading-none"
                :class="role.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-400'">
                <span class="w-1.5 h-1.5 rounded-full"
                  :class="role.status === 'ACTIVE' ? 'bg-emerald-500' : 'bg-slate-300'"></span>
                {{ role.status === 'ACTIVE' ? 'Đang dùng' : 'Vô hiệu' }}
              </span>
            </div>

            <!-- Role badge + identity -->
            <div class="flex items-start gap-3 mb-5">
              <div class="w-12 h-12 rounded-2xl flex items-center justify-center font-black text-xl shadow-sm shrink-0"
                :class="[getRoleColors(role.roleCode).bg, getRoleColors(role.roleCode).text]">
                {{ role.roleCode?.[0] || '?' }}
              </div>
              <div>
                <div class="text-xs font-black uppercase tracking-widest mb-0.5"
                  :class="getRoleColors(role.roleCode).text">
                  {{ role.roleCode }}
                </div>
                <div class="text-sm font-black text-slate-900">{{ role.roleName }}</div>
              </div>
            </div>

            <!-- Description -->
            <p class="text-xs text-slate-400 font-medium leading-relaxed mb-5 line-clamp-2 min-h-[32px]">
              {{ role.description || 'Không có mô tả' }}
            </p>

            <!-- Stats -->
            <div class="flex items-center justify-between pt-4 border-t border-slate-50">
              <div class="flex items-center gap-1.5 text-xs font-bold text-slate-500">
                <Users class="w-3.5 h-3.5" />
                <span>{{ role.activeUserCount || 0 }} người dùng</span>
              </div>
              <div class="flex items-center gap-2">
                <button
                  v-if="!role.systemRole"
                  @click.stop="toggleRoleStatus(role)"
                  class="p-1.5 rounded-lg transition-all"
                  :class="role.status === 'ACTIVE' ? 'text-slate-300 hover:text-rose-500 hover:bg-rose-50' : 'text-slate-300 hover:text-emerald-500 hover:bg-emerald-50'"
                  :title="role.status === 'ACTIVE' ? 'Vô hiệu hóa' : 'Kích hoạt'"
                >
                  <Lock v-if="role.status === 'ACTIVE'" class="w-4 h-4" />
                  <Unlock v-else class="w-4 h-4" />
                </button>
                <div v-else class="flex items-center gap-1 text-[9px] font-black text-slate-300 uppercase tracking-wider">
                  <Lock class="w-3 h-3" /> System
                </div>
                <ChevronRight class="w-4 h-4 text-slate-200 group-hover:text-violet-500 transition-colors" />
              </div>
            </div>
          </div>
        </div>

        <EmptyState v-if="!loading && roles.length === 0"
          title="Chưa có role nào"
          description="Hệ thống chưa được cấu hình phân quyền."
        />
      </div>

      <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <div class="xl:col-span-2 rounded-[28px] bg-white border border-slate-100 p-6 shadow-sm">
          <div class="flex items-center justify-between gap-4 mb-4">
            <div>
              <p class="text-xs font-black uppercase tracking-widest text-slate-400 mb-1">Phân tích tính năng HR</p>
              <h3 class="text-lg font-black text-slate-900">Ma trận module và quyền theo role</h3>
            </div>
            <div class="w-12 h-12 rounded-2xl bg-violet-50 text-violet-600 flex items-center justify-center">
              <LayoutGrid class="w-5 h-5" />
            </div>
          </div>

          <div v-if="permissionMatrixLoading" class="py-12 flex items-center justify-center">
            <Loader2 class="w-8 h-8 text-violet-600 animate-spin" />
          </div>

          <div v-else class="space-y-4">
            <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div class="text-xs font-bold text-slate-400">
                Hiển thị {{ permissionMatrixRange.from }} - {{ permissionMatrixRange.to }} / {{ permissionMatrixTotal }} quyền
              </div>

              <div class="flex flex-wrap items-center gap-2">
                <label class="text-[11px] font-black uppercase tracking-widest text-slate-400">Mỗi trang</label>
                <select
                  v-model.number="permissionMatrixPageSize"
                  class="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 outline-none transition focus:border-violet-300"
                >
                  <option :value="8">8</option>
                  <option :value="10">10</option>
                  <option :value="15">15</option>
                  <option :value="20">20</option>
                </select>
              </div>
            </div>

            <div class="overflow-x-auto">
            <table class="w-full min-w-[720px] text-sm">
              <thead>
                <tr class="text-left border-b border-slate-100">
                  <th class="pb-3 pr-4 text-[11px] font-black uppercase tracking-widest text-slate-400">Module</th>
                  <th class="pb-3 pr-4 text-[11px] font-black uppercase tracking-widest text-slate-400">Tính năng</th>
                  <th class="pb-3 pr-4 text-[11px] font-black uppercase tracking-widest text-slate-400">Admin</th>
                  <th class="pb-3 pr-4 text-[11px] font-black uppercase tracking-widest text-slate-400">HR</th>
                  <th class="pb-3 pr-4 text-[11px] font-black uppercase tracking-widest text-slate-400">Manager</th>
                  <th class="pb-3 text-[11px] font-black uppercase tracking-widest text-slate-400">Employee</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in paginatedPermissionMatrix" :key="row.permissionCode" class="border-b border-slate-50 last:border-b-0">
                  <td class="py-3 pr-4 align-top">
                    <span class="inline-flex px-2.5 py-1 rounded-lg bg-slate-100 text-slate-700 text-[11px] font-bold uppercase tracking-wide">
                      {{ formatModuleName(row.moduleCode) }}
                    </span>
                  </td>
                  <td class="py-3 pr-4">
                    <div class="font-bold text-slate-800">{{ row.permissionName }}</div>
                    <div class="text-xs text-slate-400 mt-1">{{ row.actionCode }}</div>
                  </td>
                  <td class="py-3 pr-4"><Check v-if="row.roleAllowed?.ADMIN" class="w-4 h-4 text-emerald-600" /><X v-else class="w-4 h-4 text-slate-300" /></td>
                  <td class="py-3 pr-4"><Check v-if="row.roleAllowed?.HR" class="w-4 h-4 text-emerald-600" /><X v-else class="w-4 h-4 text-slate-300" /></td>
                  <td class="py-3 pr-4"><Check v-if="row.roleAllowed?.MANAGER" class="w-4 h-4 text-emerald-600" /><X v-else class="w-4 h-4 text-slate-300" /></td>
                  <td class="py-3"><Check v-if="row.roleAllowed?.EMPLOYEE" class="w-4 h-4 text-emerald-600" /><X v-else class="w-4 h-4 text-slate-300" /></td>
                </tr>
              </tbody>
            </table>
            </div>

            <div v-if="!permissionMatrix.length" class="py-10 text-center text-sm text-slate-400 font-medium">
              Chưa có dữ liệu tính năng nào được backend trả về
            </div>

            <div v-else class="flex flex-col gap-3 border-t border-slate-100 pt-4 sm:flex-row sm:items-center sm:justify-between">
              <div class="text-xs font-medium text-slate-400">
                Trang {{ permissionMatrixPage + 1 }} / {{ permissionMatrixTotalPages }}
              </div>

              <div class="flex flex-wrap items-center justify-end gap-2">
                <button
                  type="button"
                  class="rounded-xl border border-slate-200 px-3 py-2 text-sm font-bold text-slate-600 transition hover:border-violet-200 hover:text-violet-700 disabled:cursor-not-allowed disabled:opacity-40"
                  :disabled="isFirstPermissionPage"
                  @click="prevPermissionPage"
                >
                  Trước
                </button>

                <template v-for="page in visiblePermissionPages" :key="`page-${page}`">
                  <span
                    v-if="page === '...'"
                    class="px-1 text-sm font-black text-slate-300"
                  >
                    ...
                  </span>
                  <button
                    v-else
                    type="button"
                    class="h-10 min-w-10 rounded-xl border px-3 text-sm font-black transition"
                    :class="page === permissionMatrixPage ? 'border-violet-500 bg-violet-600 text-white' : 'border-slate-200 text-slate-600 hover:border-violet-200 hover:text-violet-700'"
                    @click="setPermissionPage(page)"
                  >
                    {{ page + 1 }}
                  </button>
                </template>

                <button
                  type="button"
                  class="rounded-xl border border-slate-200 px-3 py-2 text-sm font-bold text-slate-600 transition hover:border-violet-200 hover:text-violet-700 disabled:cursor-not-allowed disabled:opacity-40"
                  :disabled="isLastPermissionPage"
                  @click="nextPermissionPage"
                >
                  Sau
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="rounded-[28px] bg-gradient-to-br from-slate-900 via-slate-800 to-violet-900 p-6 text-white shadow-sm">
          <p class="text-xs font-black uppercase tracking-widest text-violet-200/80 mb-2">Góc nhìn nghiệp vụ</p>
          <h3 class="text-xl font-black leading-tight mb-4">Các cụm tính năng HR đã được backend quản lý</h3>
          <div class="space-y-3 text-sm text-slate-200/90">
            <div class="rounded-2xl bg-white/10 p-4">
              <span class="font-black text-white">Role</span>
              dùng để xác định ai được truy cập các chức năng như quản trị người dùng, audit log, báo cáo hoặc cấu hình hệ thống.
            </div>
            <div class="rounded-2xl bg-white/10 p-4">
              <span class="font-black text-white">Permission</span>
              là lớp quyền chi tiết theo từng module HR như báo cáo, payroll, attendance, leave, onboarding hoặc governance.
            </div>
            <div class="rounded-2xl bg-white/10 p-4">
              <span class="font-black text-white">Role Menu Config</span>
              là lớp dữ liệu quyết định tính năng nào được hiển thị ra sidebar và điều hướng của từng vai trò.
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- ─── DETAIL PANEL ──────────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="slide-panel">
        <div v-if="showPanel" class="fixed inset-0 z-[100] flex justify-end">
          <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="showPanel = false" />

          <div class="relative bg-white w-full max-w-2xl h-full shadow-2xl flex flex-col animate-slide-left">

            <div v-if="panelLoading" class="absolute inset-0 flex items-center justify-center bg-white/80">
              <Loader2 class="w-10 h-10 text-violet-600 animate-spin" />
            </div>

            <template v-if="selectedRole && !panelLoading">
              <!-- Header -->
              <div class="p-8 border-b border-slate-100 shrink-0">
                <div class="flex items-start justify-between mb-6">
                  <div class="flex items-center gap-4">
                    <div class="w-16 h-16 rounded-[24px] flex items-center justify-center font-black text-2xl shadow-sm"
                      :class="[getRoleColors(selectedRole.roleCode).bg, getRoleColors(selectedRole.roleCode).text]">
                      {{ selectedRole.roleCode?.[0] }}
                    </div>
                    <div>
                      <div class="text-xs font-black uppercase tracking-widest mb-1"
                        :class="getRoleColors(selectedRole.roleCode).text">
                        {{ selectedRole.roleCode }}
                      </div>
                      <h3 class="text-2xl font-black text-slate-900">{{ selectedRole.roleName }}</h3>
                      <p class="text-sm text-slate-400 mt-1">{{ selectedRole.description }}</p>
                    </div>
                  </div>
                  <button @click="showPanel = false"
                    class="w-10 h-10 rounded-2xl bg-slate-50 text-slate-400 hover:text-rose-500 hover:bg-rose-50 transition-all flex items-center justify-center font-bold">
                    ×
                  </button>
                </div>

                <!-- Quick stats -->
                <div class="grid grid-cols-3 gap-4">
                  <div class="p-4 bg-slate-50 rounded-2xl text-center">
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Người dùng</p>
                    <p class="text-2xl font-black text-slate-900">{{ selectedRole.activeUserCount || 0 }}</p>
                  </div>
                  <div class="p-4 bg-slate-50 rounded-2xl text-center">
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Tính năng bật</p>
                    <p class="text-2xl font-black text-slate-900">{{ getFeatureStats(selectedRole).featureCount }}</p>
                  </div>
                  <div class="p-4 rounded-2xl text-center"
                    :class="selectedRole.status === 'ACTIVE' ? 'bg-emerald-50' : 'bg-slate-100'">
                    <p class="text-[9px] font-black uppercase tracking-widest mb-1"
                      :class="selectedRole.status === 'ACTIVE' ? 'text-emerald-400' : 'text-slate-400'">Trạng thái</p>
                    <p class="text-sm font-black"
                      :class="selectedRole.status === 'ACTIVE' ? 'text-emerald-700' : 'text-slate-500'">
                      {{ selectedRole.status === 'ACTIVE' ? 'Đang hoạt động' : 'Vô hiệu hóa' }}
                    </p>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-4 mt-4">
                  <div class="p-4 bg-violet-50 rounded-2xl text-center">
                    <p class="text-[9px] font-black text-violet-400 uppercase tracking-widest mb-1">Module HR</p>
                    <p class="text-2xl font-black text-violet-700">{{ getFeatureStats(selectedRole).moduleCount }}</p>
                  </div>
                  <div class="p-4 bg-sky-50 rounded-2xl text-center">
                    <p class="text-[9px] font-black text-sky-400 uppercase tracking-widest mb-1">Menu hiển thị</p>
                    <p class="text-2xl font-black text-sky-700">{{ getFeatureStats(selectedRole).menuCount }}</p>
                  </div>
                </div>
              </div>

              <!-- Feature groups -->
              <div class="flex-1 overflow-y-auto p-8 space-y-6">
                <div class="bg-violet-50 border border-violet-100 rounded-[24px] p-5">
                  <h4 class="text-xs font-black text-violet-500 uppercase tracking-widest flex items-center gap-2 mb-4">
                    <ShieldCheck class="w-4 h-4" /> Các tính năng HR đang bật cho role này
                  </h4>

                  <div v-if="getEnabledFeatureGroups(selectedRole).length" class="space-y-3">
                    <div
                      v-for="group in getEnabledFeatureGroups(selectedRole)"
                      :key="group.moduleCode"
                      class="rounded-2xl bg-white p-4 border border-violet-100"
                    >
                      <div class="flex items-center justify-between gap-3 mb-3">
                        <div>
                          <p class="text-sm font-black text-slate-900">{{ group.moduleLabel }}</p>
                          <p class="text-xs text-slate-400">{{ group.moduleCode }}</p>
                        </div>
                        <span class="px-2.5 py-1 rounded-xl bg-violet-100 text-violet-700 text-[11px] font-black">
                          {{ group.items.length }} quyền
                        </span>
                      </div>
                      <div class="flex flex-wrap gap-2">
                        <span
                          v-for="item in group.items"
                          :key="item.permissionCode"
                          class="px-3 py-1.5 rounded-xl bg-slate-50 text-slate-700 text-xs font-bold"
                        >
                          {{ item.permissionName }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div v-else class="text-sm text-slate-400 font-medium">
                    Role này chưa có tính năng HR nào được bật từ backend.
                  </div>
                </div>

                <div class="bg-sky-50 border border-sky-100 rounded-[24px] p-5">
                  <h4 class="text-xs font-black text-sky-500 uppercase tracking-widest flex items-center gap-2 mb-4">
                    <PanelsTopLeft class="w-4 h-4" /> Menu tính năng backend trả về cho giao diện
                  </h4>

                  <div v-if="roleMenuConfigs.length" class="space-y-3">
                    <div
                      v-for="menu in roleMenuConfigs"
                      :key="menu.roleMenuConfigId"
                      class="rounded-2xl bg-white p-4 border border-sky-100 flex items-start justify-between gap-4"
                    >
                      <div>
                        <p class="text-sm font-black text-slate-900">{{ menu.menuName }}</p>
                        <p class="text-xs text-slate-400 mt-1">{{ menu.routePath }}</p>
                        <p class="text-[11px] text-slate-400 mt-2">
                          key: {{ menu.menuKey }}
                          <span v-if="menu.parentMenuKey"> • parent: {{ menu.parentMenuKey }}</span>
                        </p>
                      </div>
                      <div class="text-right shrink-0">
                        <div class="text-[10px] font-black uppercase tracking-widest"
                          :class="menu.visible ? 'text-emerald-500' : 'text-slate-400'">
                          {{ menu.visible ? 'Visible' : 'Hidden' }}
                        </div>
                        <div class="text-xs text-slate-400 mt-1">Thứ tự {{ menu.sortOrder ?? 0 }}</div>
                      </div>
                    </div>
                  </div>

                  <div v-else class="text-sm text-slate-400 font-medium">
                    Role này chưa có cấu hình menu riêng từ backend.
                  </div>
                </div>

                <h4 class="text-xs font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                  <ShieldCheck class="w-4 h-4 text-violet-600" /> Ma trận quyền hạn
                </h4>

                <div
                  v-for="(perms, module) in groupPermissions(selectedRole.permissions)"
                  :key="module"
                  class="bg-slate-50 rounded-[24px] p-5"
                >
                  <div class="flex items-center gap-2 mb-4">
                    <span class="px-3 py-1 rounded-xl text-[10px] font-black uppercase tracking-wider bg-white shadow-sm text-slate-700 border border-slate-100">
                      {{ module }}
                    </span>
                  </div>
                  <div class="grid grid-cols-2 gap-2">
                    <div
                      v-for="perm in perms"
                      :key="perm.permissionCode"
                      class="flex items-center gap-2 p-2.5 rounded-xl bg-white border"
                      :class="perm.allowed ? 'border-emerald-100' : 'border-slate-100 opacity-50'"
                    >
                      <div class="w-5 h-5 rounded-lg flex items-center justify-center shrink-0"
                        :class="perm.allowed ? 'bg-emerald-100' : 'bg-slate-100'">
                        <Check v-if="perm.allowed" class="w-3 h-3 text-emerald-600" />
                        <X v-else class="w-3 h-3 text-slate-400" />
                      </div>
                      <span class="text-xs font-bold text-slate-700 truncate">{{ perm.permissionName || perm.actionCode }}</span>
                    </div>
                  </div>
                </div>

                <div v-if="!selectedRole.permissions?.length"
                  class="text-center py-8 text-sm text-slate-400 font-medium">
                  Không có quyền hạn nào được cấu hình
                </div>
              </div>
            </template>
          </div>
        </div>
      </Transition>
    </Teleport>
  
</template>

<style scoped>
.animate-fade-in { animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }

.animate-slide-left { animation: slideLeft 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
@keyframes slideLeft { from { transform: translateX(100%); } to { transform: translateX(0); } }

.slide-panel-enter-active, .slide-panel-leave-active { transition: opacity 0.3s ease; }
.slide-panel-enter-from, .slide-panel-leave-to { opacity: 0; }
</style>
