<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useToast } from '@/composables/useToast'
import { useUiStore } from '@/stores/ui'
import { getRoles, getRoleDetail, updateRole } from '@/api/admin/role'
import {
  ShieldCheck, Users, Check, X, ChevronRight,
  Loader2, Eye, Lock, Unlock, Info
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

// Group permissions by module
function groupPermissions(permissions = []) {
  const groups = {}
  for (const p of permissions) {
    if (!groups[p.moduleCode]) groups[p.moduleCode] = []
    groups[p.moduleCode].push(p)
  }
  return groups
}

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

const openPanel = async (role) => {
  panelLoading.value = true
  showPanel.value = true
  try {
    const res = await getRoleDetail(role.roleId)
    selectedRole.value = res.data
  } catch (e) {
    toast.error('Không thể tải chi tiết role')
    showPanel.value = false
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
    await updateRole(role.roleId, { ...role, status: newStatus })
    await fetchRoles()
    toast.success(`Đã ${newStatus === 'INACTIVE' ? 'vô hiệu hóa' : 'kích hoạt'} role thành công`)
    if (selectedRole.value?.roleId === role.roleId) {
      selectedRole.value = { ...selectedRole.value, status: newStatus }
    }
  } catch (e) {
    toast.error('Cập nhật trạng thái thất bại')
  }
}

onMounted(fetchRoles)
</script>

<template>
  
    <div class="space-y-8 animate-fade-in">

      <!-- HEADER -->
      <PageHeader
        title="Phân quyền Hệ thống"
        subtitle="Cấu hình vai trò và quyền hạn cho từng nhóm người dùng"
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
            <!-- Role badge + Status -->
            <div class="flex items-start justify-between mb-5">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 rounded-2xl flex items-center justify-center font-black text-xl shadow-sm"
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
              <span class="flex items-center gap-1 px-2 py-1 rounded-lg text-[10px] font-black"
                :class="role.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-400'">
                <span class="w-1.5 h-1.5 rounded-full"
                  :class="role.status === 'ACTIVE' ? 'bg-emerald-500' : 'bg-slate-300'"></span>
                {{ role.status === 'ACTIVE' ? 'Đang dùng' : 'Vô hiệu' }}
              </span>
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
                    <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Quyền hạn</p>
                    <p class="text-2xl font-black text-slate-900">{{ selectedRole.permissions?.length || 0 }}</p>
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
              </div>

              <!-- Permissions -->
              <div class="flex-1 overflow-y-auto p-8 space-y-6">
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
