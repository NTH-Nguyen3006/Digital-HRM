<script setup>
import { computed, reactive, watch } from 'vue'
import { Briefcase, CalendarDays, CheckCircle2, ClipboardList, MapPin, Sparkles, UserPlus, Users } from 'lucide-vue-next'
import BaseButton from '@/components/common/BaseButton.vue'

const props = defineProps({
  open: Boolean,
  submitting: Boolean,
  referenceLoading: Boolean,
  orgUnitOptions: {
    type: Array,
    default: () => [],
  },
  jobTitleOptions: {
    type: Array,
    default: () => [],
  },
  managerOptions: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['close', 'submit'])

const form = reactive({
  onboardingCode: '',
  fullName: '',
  personalEmail: '',
  personalPhone: '',
  genderCode: 'MALE',
  dateOfBirth: '',
  plannedStartDate: '',
  employeeCode: '',
  orgUnitId: '',
  jobTitleId: '',
  managerEmployeeId: '',
  workLocation: '',
  note: '',
})

const sections = [
  { title: 'Thông tin tiếp nhận', icon: Sparkles, subtitle: 'Khởi tạo mã hồ sơ và thời điểm nhận việc dự kiến.' },
  { title: 'Thông tin ứng viên', icon: UserPlus, subtitle: 'Liên hệ cá nhân và hồ sơ cơ bản để HR theo dõi.' },
  { title: 'Cơ cấu tổ chức', icon: Users, subtitle: 'Gán phòng ban, chức danh và quản lý trực tiếp.' },
]

const summary = computed(() => ({
  orgUnitName: props.orgUnitOptions.find((item) => String(item.orgUnitId) === String(form.orgUnitId))?.orgUnitName || 'Chưa chọn',
  jobTitleName: props.jobTitleOptions.find((item) => String(item.jobTitleId) === String(form.jobTitleId))?.jobTitleName || 'Chưa chọn',
  managerName: props.managerOptions.find((item) => String(item.employeeId) === String(form.managerEmployeeId))?.fullName || 'Chưa gán',
}))

const completionCount = computed(() => {
  const fields = [
    form.onboardingCode,
    form.fullName,
    form.personalEmail,
    form.personalPhone,
    form.genderCode,
    form.dateOfBirth,
    form.plannedStartDate,
    form.employeeCode,
    form.orgUnitId,
    form.jobTitleId,
    form.managerEmployeeId,
    form.workLocation,
    form.note,
  ]
  return fields.filter(Boolean).length
})

const canSubmit = computed(() =>
  Boolean(
    form.onboardingCode &&
    form.fullName &&
    form.dateOfBirth &&
    form.plannedStartDate &&
    form.orgUnitId &&
    form.jobTitleId,
  ),
)

function resetForm() {
  form.onboardingCode = ''
  form.fullName = ''
  form.personalEmail = ''
  form.personalPhone = ''
  form.genderCode = 'MALE'
  form.dateOfBirth = ''
  form.plannedStartDate = ''
  form.employeeCode = ''
  form.orgUnitId = ''
  form.jobTitleId = ''
  form.managerEmployeeId = ''
  form.workLocation = ''
  form.note = ''
}

function handleSubmit() {
  emit('submit', {
    onboardingCode: form.onboardingCode,
    fullName: form.fullName,
    personalEmail: form.personalEmail || null,
    personalPhone: form.personalPhone || null,
    genderCode: form.genderCode,
    dateOfBirth: form.dateOfBirth,
    plannedStartDate: form.plannedStartDate,
    employeeCode: form.employeeCode || null,
    orgUnitId: form.orgUnitId,
    jobTitleId: form.jobTitleId,
    managerEmployeeId: form.managerEmployeeId || null,
    workLocation: form.workLocation || null,
    note: form.note || null,
  })
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      resetForm()
    }
  },
)
</script>

<template>
  <Teleport to="body">
    <Transition name="slide-panel">
      <div v-if="open" class="fixed inset-0 z-100 overflow-y-auto bg-slate-950/45 p-4 sm:p-6" @click.self="$emit('close')">

        <div class="relative z-10 mx-auto my-6 h-[calc(100vh-3rem)] w-full max-w-6xl overflow-hidden rounded-[36px] border border-slate-200 bg-[linear-gradient(180deg,#ffffff_0%,#f8fbff_100%)] shadow-[0_18px_40px_rgba(15,23,42,0.14)]">
          <div class="grid h-full gap-0 xl:grid-cols-[minmax(0,1.55fr)_420px]">
            <div class="flex min-h-0 flex-col overflow-hidden">
              <div class="border-b border-slate-200/80 bg-white/90 px-8 py-7 backdrop-blur lg:px-10">
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="text-[11px] font-black uppercase tracking-[0.24em] text-indigo-500">Create Onboarding</p>
                    <h3 class="mt-3 text-3xl font-black tracking-tight text-slate-900">Tạo hồ sơ tiếp nhận mới</h3>
                    <p class="mt-2 max-w-2xl text-sm font-medium text-slate-500">
                      Ghi nhận ứng viên ngay từ giai đoạn chuẩn bị nhận việc. Sau khi tạo, HR có thể tiếp tục checklist, tài khoản, tài liệu và hợp đồng trong cùng một luồng xử lý.
                    </p>
                  </div>

                  <button
                    type="button"
                    class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 transition-all hover:border-rose-200 hover:bg-rose-50 hover:text-rose-500"
                    @click="$emit('close')"
                  >
                    ×
                  </button>
                </div>

                <div class="mt-6 flex flex-wrap gap-3">
                  <div
                    v-for="section in sections"
                    :key="section.title"
                    class="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50/90 px-4 py-3"
                  >
                    <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
                      <component :is="section.icon" class="h-5 w-5" />
                    </div>
                    <div>
                      <p class="text-sm font-black text-slate-800">{{ section.title }}</p>
                      <p class="text-xs font-medium text-slate-500">{{ section.subtitle }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div class="flex-1 overflow-y-auto px-8 py-8 lg:px-10">
                <div class="space-y-8">
                  <section class="rounded-[30px] border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/50">
                    <div class="mb-5 flex items-center gap-3">
                      <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
                        <ClipboardList class="h-5 w-5" />
                      </div>
                      <div>
                        <h4 class="text-lg font-black text-slate-900">Thông tin tiếp nhận</h4>
                        <p class="text-sm font-medium text-slate-500">Nhóm trường bắt buộc để tạo hồ sơ onboarding nền.</p>
                      </div>
                    </div>

                    <div class="grid gap-5 md:grid-cols-2">
                      <label class="block">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Mã onboarding</span>
                        <input v-model="form.onboardingCode" type="text" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10" placeholder="Ví dụ: ONB-2026-010" />
                      </label>

                      <label class="block">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Mã nhân viên dự kiến</span>
                        <input v-model="form.employeeCode" type="text" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10" placeholder="Không bắt buộc" />
                      </label>

                      <label class="block md:col-span-2">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Họ và tên</span>
                        <input v-model="form.fullName" type="text" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10" placeholder="Nhập họ tên ứng viên" />
                      </label>

                      <label class="block">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Ngày sinh</span>
                        <input v-model="form.dateOfBirth" type="date" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10" />
                      </label>

                      <label class="block">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Ngày dự kiến bắt đầu</span>
                        <input v-model="form.plannedStartDate" type="date" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10" />
                      </label>
                    </div>
                  </section>

                  <section class="rounded-[30px] border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/50">
                    <div class="mb-5 flex items-center gap-3">
                      <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-amber-50 text-amber-600">
                        <UserPlus class="h-5 w-5" />
                      </div>
                      <div>
                        <h4 class="text-lg font-black text-slate-900">Thông tin ứng viên</h4>
                        <p class="text-sm font-medium text-slate-500">Giữ lại các dữ liệu liên hệ để HR trao đổi trước ngày nhận việc.</p>
                      </div>
                    </div>

                    <div class="grid gap-5 md:grid-cols-2">
                      <label class="block">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Email cá nhân</span>
                        <input v-model="form.personalEmail" type="email" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10" placeholder="name@email.com" />
                      </label>

                      <label class="block">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Số điện thoại</span>
                        <input v-model="form.personalPhone" type="text" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10" placeholder="098..." />
                      </label>

                      <label class="block">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Giới tính</span>
                        <select v-model="form.genderCode" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10">
                          <option value="MALE">Nam</option>
                          <option value="FEMALE">Nữ</option>
                          <option value="OTHER">Khác</option>
                        </select>
                      </label>

                      <label class="block">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Địa điểm làm việc</span>
                        <input v-model="form.workLocation" type="text" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10" placeholder="Ví dụ: HCM Office" />
                      </label>
                    </div>
                  </section>

                  <section class="rounded-[30px] border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/50">
                    <div class="mb-5 flex items-center gap-3">
                      <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600">
                        <Users class="h-5 w-5" />
                      </div>
                      <div>
                        <h4 class="text-lg font-black text-slate-900">Cơ cấu tổ chức</h4>
                        <p class="text-sm font-medium text-slate-500">Xác định nơi làm việc, vai trò và tuyến quản lý để sẵn sàng cấp quyền.</p>
                      </div>
                    </div>

                    <div class="grid gap-5 md:grid-cols-2">
                      <label class="block">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Phòng ban</span>
                        <select v-model="form.orgUnitId" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10">
                          <option value="">Chọn phòng ban</option>
                          <option v-for="unit in orgUnitOptions" :key="unit.orgUnitId" :value="unit.orgUnitId">
                            {{ unit.orgUnitName }}
                          </option>
                        </select>
                      </label>

                      <label class="block">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Chức danh</span>
                        <select v-model="form.jobTitleId" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10">
                          <option value="">Chọn chức danh</option>
                          <option v-for="title in jobTitleOptions" :key="title.jobTitleId" :value="title.jobTitleId">
                            {{ title.jobTitleName }}
                          </option>
                        </select>
                      </label>

                      <label class="block md:col-span-2">
                        <span class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Quản lý trực tiếp</span>
                        <select v-model="form.managerEmployeeId" class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-800 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10">
                          <option value="">Chưa gán</option>
                          <option v-for="manager in managerOptions" :key="manager.employeeId" :value="manager.employeeId">
                            {{ manager.fullName }}
                          </option>
                        </select>
                      </label>
                    </div>
                  </section>

                  <section class="rounded-[30px] border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/50">
                    <div class="mb-5 flex items-center gap-3">
                      <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-700">
                        <MapPin class="h-5 w-5" />
                      </div>
                      <div>
                        <h4 class="text-lg font-black text-slate-900">Ghi chú nội bộ</h4>
                        <p class="text-sm font-medium text-slate-500">Lưu các dặn dò cho HR, manager hoặc IT trước ngày onboard.</p>
                      </div>
                    </div>

                    <textarea v-model="form.note" rows="5" class="w-full rounded-[28px] border border-slate-200 bg-slate-50 px-5 py-4 text-sm font-medium text-slate-700 outline-none transition-all focus:border-indigo-400 focus:bg-white focus:ring-4 focus:ring-indigo-500/10" placeholder="Ví dụ: Cần laptop MacBook, ưu tiên ngồi cùng team Finance, yêu cầu setup email trước 2 ngày..." />
                  </section>
                </div>
              </div>
            </div>

            <aside class="flex min-h-0 h-full flex-col overflow-hidden border-l border-slate-200 bg-slate-950 text-white">
              <div class="border-b border-white/10 px-8 py-8">
                <p class="text-[11px] font-black uppercase tracking-[0.22em] text-indigo-200">Snapshot</p>
                <h4 class="mt-3 text-2xl font-black text-white">{{ form.fullName || 'Ứng viên mới' }}</h4>
                <p class="mt-2 text-sm font-medium text-slate-300">
                  Hồ sơ sẽ được tạo ở trạng thái `DRAFT` để HR tiếp tục checklist, tài khoản, tài liệu và hợp đồng sau đó.
                </p>

                <div class="mt-6 rounded-[28px] border border-white/10 bg-white/5 p-5">
                  <div class="flex items-center justify-between">
                    <div>
                      <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Mức độ hoàn thiện</p>
                      <p class="mt-2 text-3xl font-black text-white">{{ completionCount }}/13</p>
                    </div>
                    <div class="flex h-14 w-14 items-center justify-center rounded-3xl bg-indigo-500/20 text-indigo-200">
                      <CheckCircle2 class="h-7 w-7" />
                    </div>
                  </div>
                  <div class="mt-4 h-2 overflow-hidden rounded-full bg-white/10">
                    <div class="h-full rounded-full bg-linear-to-r from-indigo-400 to-cyan-300 transition-all" :style="{ width: `${(completionCount / 13) * 100}%` }" />
                  </div>
                </div>
              </div>

              <div class="flex-1 overflow-y-auto px-8 py-8">
                <div class="space-y-4">
                  <div class="rounded-[28px] border border-white/10 bg-white/5 p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Mã hồ sơ</p>
                    <p class="mt-2 text-lg font-black text-white">{{ form.onboardingCode || 'Chưa nhập' }}</p>
                  </div>

                  <div class="rounded-[28px] border border-white/10 bg-white/5 p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Phòng ban</p>
                    <p class="mt-2 text-sm font-bold text-white">{{ summary.orgUnitName }}</p>
                  </div>

                  <div class="rounded-[28px] border border-white/10 bg-white/5 p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Chức danh</p>
                    <p class="mt-2 text-sm font-bold text-white">{{ summary.jobTitleName }}</p>
                  </div>

                  <div class="rounded-[28px] border border-white/10 bg-white/5 p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Manager</p>
                    <p class="mt-2 text-sm font-bold text-white">{{ summary.managerName }}</p>
                  </div>

                  <div class="rounded-[28px] border border-white/10 bg-white/5 p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Ngày bắt đầu</p>
                    <div class="mt-2 flex items-center gap-2 text-sm font-bold text-white">
                      <CalendarDays class="h-4 w-4 text-indigo-200" />
                      <span>{{ form.plannedStartDate || 'Chưa chọn' }}</span>
                    </div>
                  </div>

                  <div class="rounded-[28px] border border-dashed border-white/15 bg-white/[0.03] p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Các bước tiếp theo</p>
                    <div class="mt-4 space-y-3">
                      <div class="flex items-start gap-3">
                        <div class="mt-0.5 flex h-7 w-7 items-center justify-center rounded-xl bg-white/10 text-[11px] font-black text-indigo-200">1</div>
                        <p class="text-sm font-medium text-slate-300">HR bổ sung checklist và tài sản cần chuẩn bị.</p>
                      </div>
                      <div class="flex items-start gap-3">
                        <div class="mt-0.5 flex h-7 w-7 items-center justify-center rounded-xl bg-white/10 text-[11px] font-black text-indigo-200">2</div>
                        <p class="text-sm font-medium text-slate-300">Tạo user hệ thống và liên kết manager phụ trách.</p>
                      </div>
                      <div class="flex items-start gap-3">
                        <div class="mt-0.5 flex h-7 w-7 items-center justify-center rounded-xl bg-white/10 text-[11px] font-black text-indigo-200">3</div>
                        <p class="text-sm font-medium text-slate-300">Chuyển sang hợp đồng đầu tiên và gửi thư chào mừng.</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="border-t border-white/10 px-8 py-6">
                <div class="flex flex-wrap gap-3">
                  <BaseButton
                    variant="outline"
                    class="flex-1 rounded-2xl! border-white/15! bg-white/5! text-white! hover:bg-white/10!"
                    @click="$emit('close')"
                  >
                    Đóng
                  </BaseButton>
                  <BaseButton
                    variant="primary"
                    :disabled="!canSubmit"
                    :loading="submitting || referenceLoading"
                    class="flex-[1.3] rounded-2xl! border border-indigo-400/40! bg-linear-to-r from-indigo-600! to-violet-600! px-6! py-3! font-bold tracking-[0.01em] text-white! shadow-[0_10px_24px_rgba(79,70,229,0.28)]! hover:from-indigo-500! hover:to-violet-500! hover:shadow-[0_14px_30px_rgba(79,70,229,0.32)]!"
                    @click="handleSubmit"
                  >
                    <Plus class="mr-2 h-4 w-4" />
                    Tạo hồ sơ ngay
                  </BaseButton>
                </div>
              </div>
            </aside>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
