<script setup>
import MainLayout from "@/layouts/MainLayout.vue"
import { ref, computed } from 'vue'
import { Search, Filter, Download, History, AlertCircle, Info } from 'lucide-vue-next'

const searchQuery = ref('')
const filterModule = ref('ALL')
const filterResult = ref('ALL')
const dateFrom = ref('')
const dateTo = ref('')

const logs = ref([
  {
    id: 1, actor: 'admin', actorName: 'System Administrator', module: 'AUTH', action: 'LOGIN_SUCCESS',
    target: 'User: hr.nguyen', result: 'SUCCESS', ip: '192.168.1.10',
    device: 'Chrome on Windows 11', detail: 'Đăng nhập thành công',
    timestamp: '01/04/2026 08:30:15', traceId: 'trace-abc-123'
  },
  {
    id: 2, actor: 'admin', actorName: 'System Administrator', module: 'USER', action: 'USER_CREATED',
    target: 'Username: hr.nguyen.2', result: 'SUCCESS', ip: '192.168.1.10',
    device: 'Chrome on Windows 11', detail: 'Tạo tài khoản mới với role HR',
    timestamp: '01/04/2026 09:15:22', traceId: 'trace-def-456'
  },
  {
    id: 3, actor: 'hr.nguyen', actorName: 'Nguyễn Thị HR', module: 'EMPLOYEE', action: 'EMPLOYEE_UPDATED',
    target: 'Employee: NV003 - Lê Văn C', result: 'SUCCESS', ip: '10.0.0.25',
    device: 'Edge on Windows 10', detail: 'Cập nhật thông tin liên hệ',
    timestamp: '01/04/2026 10:00:01', traceId: 'trace-ghi-789'
  },
  {
    id: 4, actor: 'mgr.tran', actorName: 'Trần Văn Manager', module: 'AUTH', action: 'LOGIN_FAILED',
    target: 'User: mgr.tran', result: 'FAILURE', ip: '172.16.5.30',
    device: 'Safari on macOS', detail: 'Sai mật khẩu lần 3',
    timestamp: '31/03/2026 17:45:10', traceId: 'trace-jkl-012'
  },
  {
    id: 5, actor: 'admin', actorName: 'System Administrator', module: 'ROLE', action: 'ROLE_PERMISSION_UPDATED',
    target: 'Role: HR', result: 'SUCCESS', ip: '192.168.1.10',
    device: 'Chrome on Windows 11', detail: 'Cập nhật quyền cho vai trò HR',
    timestamp: '31/03/2026 14:20:33', traceId: 'trace-mno-345'
  },
  {
    id: 6, actor: 'hr.nguyen', actorName: 'Nguyễn Thị HR', module: 'CONTRACT', action: 'CONTRACT_SUBMITTED',
    target: 'Contract: HD005', result: 'SUCCESS', ip: '10.0.0.25',
    device: 'Edge on Windows 10', detail: 'Gửi hợp đồng chờ ký',
    timestamp: '31/03/2026 11:05:44', traceId: 'trace-pqr-678'
  },
])

const modules = ['ALL', 'AUTH', 'USER', 'ROLE', 'EMPLOYEE', 'CONTRACT', 'AUDIT']

const filteredLogs = computed(() => {
  return logs.value.filter(l => {
    const matchSearch = !searchQuery.value ||
      l.actor.includes(searchQuery.value) ||
      l.actorName.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      l.action.includes(searchQuery.value) ||
      l.target.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchModule = filterModule.value === 'ALL' || l.module === filterModule.value
    const matchResult = filterResult.value === 'ALL' || l.result === filterResult.value
    return matchSearch && matchModule && matchResult
  })
})

const resultClass = (result) => {
  return result === 'SUCCESS'
    ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20'
    : 'bg-rose-50 text-rose-700 ring-rose-600/20'
}

const moduleColor = (module) => {
  const map = {
    AUTH: 'bg-purple-50 text-purple-700',
    USER: 'bg-indigo-50 text-indigo-700',
    ROLE: 'bg-sky-50 text-sky-700',
    EMPLOYEE: 'bg-emerald-50 text-emerald-700',
    CONTRACT: 'bg-amber-50 text-amber-700',
    AUDIT: 'bg-slate-100 text-slate-600',
  }
  return map[module] || 'bg-slate-100 text-slate-600'
}
</script>

<template>
  <MainLayout>
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">Nhật ký hoạt động</h2>
          <p class="text-slate-500 font-medium mt-1">Theo dõi mọi thao tác trên hệ thống theo thời gian thực</p>
        </div>
        <button
          class="bg-white border border-slate-200 px-5 py-2.5 rounded-xl font-bold text-slate-700 hover:bg-slate-50 transition-all shadow-sm flex items-center">
          <Download class="w-5 h-5 mr-2 text-indigo-500" /> Xuất CSV
        </button>
      </div>

      <!-- Stats bar -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="bg-indigo-600 text-white p-5 rounded-2xl shadow-lg shadow-indigo-200">
          <div class="text-indigo-200 text-xs font-bold uppercase tracking-wider mb-1">Tổng hôm nay</div>
          <div class="text-3xl font-black">{{ logs.length }}</div>
        </div>
        <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm">
          <div class="text-slate-400 text-xs font-bold uppercase tracking-wider mb-1">Thành công</div>
          <div class="text-3xl font-black text-emerald-600">{{logs.filter(l => l.result === 'SUCCESS').length}}</div>
        </div>
        <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm">
          <div class="text-slate-400 text-xs font-bold uppercase tracking-wider mb-1">Thất bại</div>
          <div class="text-3xl font-black text-rose-500">{{logs.filter(l => l.result === 'FAILURE').length}}</div>
        </div>
        <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm">
          <div class="text-slate-400 text-xs font-bold uppercase tracking-wider mb-1">Người hoạt động</div>
          <div class="text-3xl font-black text-slate-900">{{new Set(logs.map(l => l.actor)).size}}</div>
        </div>
      </div>

      <!-- Filters -->
      <div class="bg-white border border-slate-100 rounded-2xl p-4 shadow-sm flex flex-wrap items-center gap-3">
        <div class="relative flex-1 min-w-50">
          <Search class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input v-model="searchQuery" placeholder="Tìm theo user, hành động, đối tượng..."
            class="w-full pl-9 pr-4 py-2 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
        </div>
        <select v-model="filterModule"
          class="px-3 py-2 border border-slate-200 rounded-xl text-sm font-bold text-slate-600 bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
          <option v-for="m in modules" :key="m" :value="m">{{ m === 'ALL' ? 'Tất cả module' : m }}</option>
        </select>
        <select v-model="filterResult"
          class="px-3 py-2 border border-slate-200 rounded-xl text-sm font-bold text-slate-600 bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
          <option value="ALL">Tất cả kết quả</option>
          <option value="SUCCESS">Thành công</option>
          <option value="FAILURE">Thất bại</option>
        </select>
        <div class="flex items-center gap-2 text-sm text-slate-500 font-medium">
          <input type="date" v-model="dateFrom"
            class="px-3 py-2 border border-slate-200 rounded-xl text-sm text-slate-600 bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
          <span>→</span>
          <input type="date" v-model="dateTo"
            class="px-3 py-2 border border-slate-200 rounded-xl text-sm text-slate-600 bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
        </div>
      </div>

      <!-- Log Feed -->
      <div class="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
        <div class="divide-y divide-slate-100">
          <div v-for="log in filteredLogs" :key="log.id" class="p-5 hover:bg-slate-50/50 transition-colors group">
            <div class="flex items-start gap-4">
              <!-- Result indicator -->
              <div class="shrink-0 mt-0.5">
                <div class="w-9 h-9 rounded-full flex items-center justify-center"
                  :class="log.result === 'SUCCESS' ? 'bg-emerald-100' : 'bg-rose-100'">
                  <component :is="log.result === 'SUCCESS' ? History : AlertCircle" class="w-5 h-5"
                    :class="log.result === 'SUCCESS' ? 'text-emerald-600' : 'text-rose-600'" />
                </div>
              </div>

              <!-- Content -->
              <div class="flex-1 min-w-0">
                <div class="flex flex-wrap items-center gap-2 mb-1">
                  <span class="font-black text-slate-900">{{ log.actorName }}</span>
                  <span class="text-slate-400 text-xs font-medium">(@{{ log.actor }})</span>
                  <span class="px-2 py-0.5 rounded-md text-xs font-bold" :class="moduleColor(log.module)">
                    {{ log.module }}
                  </span>
                  <span class="px-2 py-0.5 rounded-md text-xs font-mono font-bold bg-slate-100 text-slate-600">
                    {{ log.action }}
                  </span>
                  <span class="px-2.5 py-0.5 rounded-md text-xs font-bold ring-1 ring-inset ml-auto"
                    :class="resultClass(log.result)">
                    {{ log.result === 'SUCCESS' ? '✓ Thành công' : '✗ Thất bại' }}
                  </span>
                </div>
                <div class="text-sm text-slate-600 font-medium mb-2">
                  <span class="text-slate-400">→</span> {{ log.target }}
                  <span class="text-slate-300 mx-2">•</span>
                  <span class="text-slate-500 italic">{{ log.detail }}</span>
                </div>
                <div class="flex flex-wrap gap-4 text-xs text-slate-400 font-medium">
                  <span>🕐 {{ log.timestamp }}</span>
                  <span>🌐 {{ log.ip }}</span>
                  <span>💻 {{ log.device }}</span>
                  <span class="font-mono">🔍 {{ log.traceId }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="px-6 py-4 border-t border-slate-100 flex items-center justify-between bg-slate-50/50">
          <span class="text-sm text-slate-500 font-medium">{{ filteredLogs.length }} bản ghi</span>
          <div class="flex items-center gap-2">
            <button
              class="px-3 py-1.5 rounded-lg text-sm font-bold bg-white border border-slate-200 text-slate-600 hover:bg-slate-50">←
              Trước</button>
            <button class="px-3 py-1.5 rounded-lg text-sm font-bold bg-indigo-600 text-white">1</button>
            <button
              class="px-3 py-1.5 rounded-lg text-sm font-bold bg-white border border-slate-200 text-slate-600 hover:bg-slate-50">Sau
              →</button>
          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>
