<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Loader2, Upload } from 'lucide-vue-next'
import { createContract, getContractTypeOptions } from '@/api/admin/contract'
import { getEmployees } from '@/api/admin/employee'
import { getJobTitles } from '@/api/admin/jobTitle'
import { getOrgUnits } from '@/api/admin/orgUnit'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const toast = useToast()

const loading = ref(false)
const submitting = ref(false)
const fileInput = ref(null)

const employees = ref([])
const contractTypes = ref([])
const orgUnits = ref([])
const jobTitles = ref([])

const form = ref({
  contractNumber: '',
  employeeId: '',
  contractTypeId: '',
  signDate: '',
  effectiveDate: '',
  endDate: '',
  orgUnitId: '',
  jobTitleId: '',
  workingType: 'FULL_TIME',
  workLocation: '',
  baseSalary: '',
  salaryCurrency: 'VND',
  note: '',
})

const selectedEmployee = computed(() => employees.value.find(
  (employee) => String(employee.employeeId) === String(form.value.employeeId),
) || null)

const selectedOrgUnitName = computed(() => {
  const matched = orgUnits.value.find((item) => String(item.orgUnitId) === String(form.value.orgUnitId))
  return matched?.orgUnitName || selectedEmployee.value?.orgUnitName || ''
})

const selectedJobTitleName = computed(() => {
  const matched = jobTitles.value.find((item) => String(item.jobTitleId) === String(form.value.jobTitleId))
  return matched?.jobTitleName || selectedEmployee.value?.jobTitleName || ''
})

watch(
  () => form.value.employeeId,
  (employeeId) => {
    const employee = employees.value.find((item) => String(item.employeeId) === String(employeeId))
    if (!employee) {
      form.value.orgUnitId = ''
      form.value.jobTitleId = ''
      return
    }

    form.value.orgUnitId = employee.orgUnitId ? String(employee.orgUnitId) : ''
    form.value.jobTitleId = employee.jobTitleId ? String(employee.jobTitleId) : ''
  },
)

async function fetchFormOptions() {
  loading.value = true
  try {
    const [employeeRes, contractTypeRes, orgUnitRes, jobTitleRes] = await Promise.all([
      getEmployees({ employmentStatus: 'ACTIVE', page: 0, size: 200 }),
      getContractTypeOptions(),
      getOrgUnits({ status: 'ACTIVE', page: 0, size: 200 }),
      getJobTitles({ status: 'ACTIVE', page: 0, size: 200 }),
    ])

    const employeePage = employeeRes?.data || {}
    const orgUnitPage = orgUnitRes?.data || {}
    const jobTitlePage = jobTitleRes?.data || {}

    employees.value = Array.isArray(employeePage.items)
      ? employeePage.items
      : Array.isArray(employeePage.content)
        ? employeePage.content
        : []

    contractTypes.value = Array.isArray(contractTypeRes?.data) ? contractTypeRes.data : []
    orgUnits.value = Array.isArray(orgUnitPage.items)
      ? orgUnitPage.items
      : Array.isArray(orgUnitPage.content)
        ? orgUnitPage.content
        : []
    jobTitles.value = Array.isArray(jobTitlePage.items)
      ? jobTitlePage.items
      : Array.isArray(jobTitlePage.content)
        ? jobTitlePage.content
        : []
  } catch (error) {
    console.error('Failed to load add contract options:', error)
    toast.error('Không thể tải dữ liệu tạo hợp đồng')
  } finally {
    loading.value = false
  }
}

function chooseFile() {
  fileInput.value?.click()
}

function handleFileUpload(event) {
  const file = event.target?.files?.[0]
  if (!file) return

  toast.info(`Đã chọn file ${file.name}. Tính năng OCR hợp đồng sẽ được bổ sung sau.`)
  event.target.value = ''
}

function validateForm() {
  if (!form.value.contractNumber.trim()) {
    toast.error('Vui lòng nhập số hợp đồng')
    return false
  }
  if (!form.value.employeeId) {
    toast.error('Vui lòng chọn nhân viên')
    return false
  }
  if (!form.value.contractTypeId) {
    toast.error('Vui lòng chọn loại hợp đồng')
    return false
  }
  if (!form.value.signDate || !form.value.effectiveDate) {
    toast.error('Vui lòng chọn ngày ký và ngày hiệu lực')
    return false
  }
  if (!form.value.orgUnitId || !form.value.jobTitleId) {
    toast.error('Nhân viên đang thiếu phòng ban hoặc chức danh')
    return false
  }
  if (!form.value.baseSalary || Number(form.value.baseSalary) <= 0) {
    toast.error('Lương cơ bản phải lớn hơn 0')
    return false
  }
  if (form.value.endDate && form.value.endDate < form.value.effectiveDate) {
    toast.error('Ngày kết thúc phải sau hoặc bằng ngày hiệu lực')
    return false
  }

  return true
}

async function handleSubmit() {
  if (submitting.value || !validateForm()) return

  submitting.value = true
  try {
    const response = await createContract({
      employeeId: Number(form.value.employeeId),
      contractTypeId: Number(form.value.contractTypeId),
      contractNumber: form.value.contractNumber.trim(),
      signDate: form.value.signDate,
      effectiveDate: form.value.effectiveDate,
      endDate: form.value.endDate || null,
      jobTitleId: Number(form.value.jobTitleId),
      orgUnitId: Number(form.value.orgUnitId),
      workLocation: form.value.workLocation.trim() || null,
      baseSalary: Number(form.value.baseSalary),
      salaryCurrency: form.value.salaryCurrency,
      workingType: form.value.workingType,
      note: form.value.note.trim() || null,
    })

    const createdId = response?.data?.laborContractId
    toast.success('Tạo bản nháp hợp đồng thành công')

    if (createdId) {
      router.push(`/contracts/${createdId}`)
      return
    }

    router.push('/contracts')
  } catch (error) {
    console.error('Failed to create contract:', error)
    toast.error(error.response?.data?.message || 'Tạo hợp đồng thất bại')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchFormOptions)
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <h2 class="text-3xl font-black text-slate-900">Tạo hợp đồng lao động</h2>
        <p class="mt-1 text-slate-500">HR có thể tạo bản nháp hợp đồng từ dữ liệu nhân sự hiện có</p>
      </div>

      <div class="flex items-center gap-3">
        <button
          type="button"
          @click="chooseFile"
          class="flex items-center rounded-xl border border-indigo-500 bg-white px-4 py-2.5 font-semibold text-slate-700 shadow-sm hover:bg-slate-50"
        >
          <Upload class="mr-2 h-5 w-5 text-indigo-500" />
          Tải lên
        </button>

        <input
          ref="fileInput"
          type="file"
          accept=".pdf,.doc,.docx"
          class="hidden"
          @change="handleFileUpload"
        >
      </div>
    </div>

    <div
      v-if="loading"
      class="flex items-center justify-center gap-3 rounded-3xl border border-slate-100 bg-white px-6 py-10 text-slate-500 shadow-sm"
    >
      <Loader2 class="h-5 w-5 animate-spin text-indigo-600" />
      <span class="font-medium">Đang tải dữ liệu biểu mẫu...</span>
    </div>

    <form v-else class="space-y-5" @submit.prevent="handleSubmit">
      <div class="grid gap-5 xl:grid-cols-[minmax(0,1.55fr)_380px]">
        <div class="space-y-5">
          <div class="rounded-3xl border border-slate-100 bg-white p-5 shadow-sm">
            <div class="mb-4 flex items-start justify-between gap-4">
              <div>
                <h3 class="text-lg font-bold text-slate-800">Thông tin chính</h3>
                <p class="mt-1 text-sm text-slate-500">Nhập mã hợp đồng, nhân sự và thời hạn hiệu lực</p>
              </div>
              <div class="rounded-2xl bg-indigo-50 px-3 py-2 text-right">
                <div class="text-[11px] font-bold uppercase tracking-wider text-indigo-500">Nhân sự</div>
                <div class="text-sm font-bold text-indigo-900">
                  {{ selectedEmployee?.fullName || 'Chưa chọn' }}
                </div>
              </div>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Số hợp đồng</label>
                <input
                  v-model="form.contractNumber"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  placeholder="VD: HDLD-2026-001"
                >
              </div>

              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Loại hợp đồng</label>
                <select
                  v-model="form.contractTypeId"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
                  <option value="">Chọn loại hợp đồng</option>
                  <option
                    v-for="type in contractTypes"
                    :key="type.contractTypeId"
                    :value="String(type.contractTypeId)"
                  >
                    {{ type.contractTypeName }}
                  </option>
                </select>
              </div>

              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Nhân viên</label>
                <select
                  v-model="form.employeeId"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
                  <option value="">Chọn nhân viên</option>
                  <option
                    v-for="employee in employees"
                    :key="employee.employeeId"
                    :value="String(employee.employeeId)"
                  >
                    {{ employee.fullName }} - {{ employee.employeeCode }}
                  </option>
                </select>
              </div>

              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Hình thức làm việc</label>
                <select
                  v-model="form.workingType"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
                  <option value="FULL_TIME">Full time</option>
                  <option value="PART_TIME">Part time</option>
                </select>
              </div>

              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Ngày ký</label>
                <input
                  v-model="form.signDate"
                  type="date"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
              </div>

              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Ngày hiệu lực</label>
                <input
                  v-model="form.effectiveDate"
                  type="date"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
              </div>

              <div class="md:col-span-2">
                <label class="mb-1 block text-sm font-semibold text-slate-600">Ngày kết thúc</label>
                <input
                  v-model="form.endDate"
                  type="date"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
              </div>
            </div>
          </div>
        </div>

        <div class="space-y-5 xl:sticky xl:top-6 xl:self-start">
          <div class="rounded-3xl border border-slate-100 bg-white p-5 shadow-sm">
            <h3 class="text-lg font-bold text-slate-800">Công việc và lương</h3>
            <div class="mt-4 grid gap-4">
              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Phòng ban</label>
                <select
                  v-model="form.orgUnitId"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
                  <option value="">Chọn phòng ban</option>
                  <option
                    v-for="orgUnit in orgUnits"
                    :key="orgUnit.orgUnitId"
                    :value="String(orgUnit.orgUnitId)"
                  >
                    {{ orgUnit.orgUnitName }}
                  </option>
                </select>
                <p v-if="selectedOrgUnitName" class="mt-1 text-xs font-medium text-slate-400">
                  {{ selectedOrgUnitName }}
                </p>
              </div>

              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Chức danh</label>
                <select
                  v-model="form.jobTitleId"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
                  <option value="">Chọn chức danh</option>
                  <option
                    v-for="jobTitle in jobTitles"
                    :key="jobTitle.jobTitleId"
                    :value="String(jobTitle.jobTitleId)"
                  >
                    {{ jobTitle.jobTitleName }}
                  </option>
                </select>
                <p v-if="selectedJobTitleName" class="mt-1 text-xs font-medium text-slate-400">
                  {{ selectedJobTitleName }}
                </p>
              </div>

              <div>
                <label class="mb-1 block text-sm font-semibold text-slate-600">Nơi làm việc</label>
                <input
                  v-model="form.workLocation"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  placeholder="VD: Văn phòng chính TP.HCM"
                >
              </div>

              <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-1">
                <div>
                  <label class="mb-1 block text-sm font-semibold text-slate-600">Lương cơ bản</label>
                  <input
                    v-model="form.baseSalary"
                    type="number"
                    min="0"
                    step="0.01"
                    class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                    placeholder="VD: 15000000"
                  >
                </div>

                <div>
                  <label class="mb-1 block text-sm font-semibold text-slate-600">Tiền tệ</label>
                  <select
                    v-model="form.salaryCurrency"
                    class="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  >
                    <option value="VND">VND</option>
                    <option value="USD">USD</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <div class="rounded-3xl border border-slate-100 bg-white p-5 shadow-sm">
            <div class="mb-3 flex items-center justify-between">
              <h3 class="text-lg font-bold text-slate-800">Ghi chú</h3>
              <span class="rounded-full bg-slate-100 px-2.5 py-1 text-[11px] font-bold text-slate-500">
                Tùy chọn
              </span>
            </div>
            <textarea
              v-model="form.note"
              class="h-28 w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
              placeholder="Ghi chú thêm cho bản nháp hợp đồng"
            />
          </div>

          <div class="rounded-3xl border border-indigo-100 bg-linear-to-br from-indigo-50 to-white p-5 shadow-sm">
            <div class="mb-4">
              <h3 class="text-lg font-bold text-slate-900">Sẵn sàng lưu</h3>
              <p class="mt-1 text-sm text-slate-500">Kiểm tra nhanh thông tin trước khi tạo bản nháp</p>
            </div>

            <div class="grid gap-3 text-sm">
              <div class="flex items-center justify-between rounded-2xl bg-white/80 px-3 py-2">
                <span class="text-slate-500">Nhân viên</span>
                <span class="max-w-44 truncate font-semibold text-slate-900">
                  {{ selectedEmployee?.employeeCode || 'Chưa chọn' }}
                </span>
              </div>
              <div class="flex items-center justify-between rounded-2xl bg-white/80 px-3 py-2">
                <span class="text-slate-500">Phòng ban</span>
                <span class="max-w-44 truncate font-semibold text-slate-900">
                  {{ selectedOrgUnitName || 'Chưa chọn' }}
                </span>
              </div>
              <div class="flex items-center justify-between rounded-2xl bg-white/80 px-3 py-2">
                <span class="text-slate-500">Chức danh</span>
                <span class="max-w-44 truncate font-semibold text-slate-900">
                  {{ selectedJobTitleName || 'Chưa chọn' }}
                </span>
              </div>
            </div>

            <div class="mt-5 flex gap-3">
              <button
                type="button"
                class="flex-1 rounded-xl border border-slate-200 bg-white px-4 py-2.5 font-semibold text-slate-600 hover:bg-slate-50"
                @click="router.push('/contracts')"
              >
                Hủy
              </button>

              <button
                type="submit"
                :disabled="submitting"
                class="flex flex-1 items-center justify-center rounded-xl bg-indigo-600 px-4 py-2.5 font-bold text-white shadow-lg shadow-indigo-200 hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                <Loader2 v-if="submitting" class="mr-2 h-4 w-4 animate-spin" />
                {{ submitting ? 'Đang lưu...' : 'Lưu hợp đồng' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>

<style scoped>
input[type="date"] {
  position: relative;
}

input[type="date"]::-webkit-calendar-picker-indicator {
  filter: invert(29%) sepia(98%) saturate(1500%) hue-rotate(220deg) brightness(90%) contrast(120%);
  cursor: pointer;
  position: relative;
  right: 8px;
  transform: scale(1.2);
}

select {
  appearance: none;
  background-image: url("data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiPjxwYXRoIGQ9Im02IDkgNiA2IDYtNiIvPjwvc3ZnPg==");
  background-position: right 16px center;
  background-repeat: no-repeat;
  background-size: 16px;
  padding-right: 2.5rem;
}
</style>
