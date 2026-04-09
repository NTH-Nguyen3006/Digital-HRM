<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { CheckCircle2, FilePenLine, Search, ShieldCheck, XCircle } from 'lucide-vue-next'
import AvatarBox from '@/components/common/AvatarBox.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import InsightCard from '@/components/hrm/InsightCard.vue'
import SurfacePanel from '@/components/hrm/SurfacePanel.vue'
import { getProfileChangeRequests, reviewProfileChangeRequest } from '@/api/admin/employee'
import { useToast } from '@/composables/useToast'
import { unwrapPage } from '@/utils/api'
import { formatDateTime, getInitials, humanizeStatus } from '@/utils/format'
import { profileChangeEntries, summarizeProfileChange } from '@/utils/profileChange'

const toast = useToast()

const loading = ref(false)
const actionLoading = ref('')
const searchQuery = ref('')
const activeTab = ref('PENDING')
const requests = ref([])
const selectedRequest = ref(null)

const stats = computed(() => [
  {
    title: 'Chờ duyệt',
    value: requests.value.filter((item) => item.requestStatus === 'PENDING').length,
    subtitle: 'Ưu tiên xử lý trong ngày',
    icon: ShieldCheck,
    tone: 'amber',
  },
  {
    title: 'Đã duyệt',
    value: requests.value.filter((item) => item.requestStatus === 'APPROVED').length,
    subtitle: 'Đã cập nhật vào hồ sơ nhân sự',
    icon: CheckCircle2,
    tone: 'emerald',
  },
  {
    title: 'Từ chối',
    value: requests.value.filter((item) => item.requestStatus === 'REJECTED').length,
    subtitle: 'Cần phản hồi lại cho nhân sự',
    icon: XCircle,
    tone: 'rose',
  },
  {
    title: 'Tổng yêu cầu',
    value: requests.value.length,
    subtitle: 'Trong trạng thái đang xem',
    icon: FilePenLine,
    tone: 'indigo',
  },
])

const filteredRequests = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return requests.value

  return requests.value.filter((item) =>
    [item.employeeFullName, item.employeeCode, item.requesterUsername, summarizeProfileChange(item.payloadJson)]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(keyword)),
  )
})

const selectedEntries = computed(() => profileChangeEntries(selectedRequest.value?.payloadJson))

async function fetchRequests() {
  loading.value = true
  try {
    const response = await getProfileChangeRequests({
      status: activeTab.value,
      size: 100,
    })
    requests.value = unwrapPage(response).items
    if (selectedRequest.value) {
      selectedRequest.value = requests.value.find((item) => item.profileChangeRequestId === selectedRequest.value.profileChangeRequestId) || null
    }
  } catch (error) {
    console.error('Failed to fetch profile change requests:', error)
    requests.value = []
    toast.error('Không thể tải danh sách yêu cầu thay đổi hồ sơ')
  } finally {
    loading.value = false
  }
}

async function reviewRequest(item, approved) {
  actionLoading.value = `${item.profileChangeRequestId}-${approved ? 'approve' : 'reject'}`
  try {
    await reviewProfileChangeRequest(item.profileChangeRequestId, {
      approved,
      reviewNote: approved
        ? 'HR đã xác minh và chấp nhận cập nhật hồ sơ.'
        : 'HR cần thêm chứng từ đối chiếu trước khi cập nhật hồ sơ.',
    })
    toast.success(approved ? 'Đã duyệt yêu cầu cập nhật hồ sơ' : 'Đã từ chối yêu cầu cập nhật hồ sơ')
    await fetchRequests()
  } catch (error) {
    console.error('Failed to review profile change request:', error)
    toast.error(error.response?.data?.message || 'Không thể xử lý yêu cầu')
  } finally {
    actionLoading.value = ''
  }
}

function openDetail(item) {
  selectedRequest.value = item
}

onMounted(fetchRequests)
watch(activeTab, fetchRequests)
</script>

<template>
  <div class="space-y-8">
    <PageHeader
      title="Phê duyệt hồ sơ"
      subtitle="HR xem nhanh nội dung cập nhật, người gửi và trạng thái xử lý hồ sơ cá nhân."
      :icon="FilePenLine"
    >
      <template #actions>
        <div class="relative">
          <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm nhân sự hoặc người gửi..."
            class="w-full rounded-2xl border border-slate-200 bg-white py-3 pl-10 pr-4 text-sm font-medium outline-none transition-all focus:border-indigo-400 focus:ring-4 focus:ring-indigo-500/10 md:w-72"
          >
        </div>
      </template>
    </PageHeader>

    <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
      <InsightCard
        v-for="item in stats"
        :key="item.title"
        :title="item.title"
        :value="item.value"
        :subtitle="item.subtitle"
        :icon="item.icon"
        :tone="item.tone"
      />
    </div>

    <div class="grid gap-6 xl:grid-cols-[minmax(0,1.5fr)_430px]">
      <SurfacePanel>
        <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div class="flex flex-wrap gap-2 rounded-2xl bg-slate-100 p-1">
            <button
              v-for="status in ['PENDING', 'APPROVED', 'REJECTED']"
              :key="status"
              type="button"
              class="rounded-2xl px-4 py-2 text-sm font-black transition-all"
              :class="activeTab === status ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
              @click="activeTab = status"
            >
              {{ humanizeStatus(status) }}
            </button>
          </div>
          <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-black uppercase tracking-[0.18em] text-slate-500">
            {{ filteredRequests.length }} yêu cầu
          </span>
        </div>

        <div v-if="loading" class="mt-6 grid gap-4">
          <div v-for="item in 4" :key="item" class="h-44 animate-pulse rounded-[28px] bg-slate-100" />
        </div>

        <div v-else-if="filteredRequests.length" class="mt-6 space-y-4">
          <article
            v-for="item in filteredRequests"
            :key="item.profileChangeRequestId"
            class="rounded-[28px] border border-slate-200 bg-white p-5 transition-all hover:border-indigo-200 hover:shadow-lg"
          >
            <div class="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
              <button type="button" class="flex flex-1 items-start gap-4 text-left" @click="openDetail(item)">
                <AvatarBox :name="item.employeeFullName" size="lg" shape="rounded-[20px]" />
                <div class="min-w-0">
                  <div class="flex flex-wrap items-center gap-2">
                    <h4 class="text-lg font-black text-slate-900">{{ item.employeeFullName }}</h4>
                    <StatusBadge :status="item.requestStatus" />
                  </div>
                  <p class="mt-1 text-sm font-medium text-slate-500">
                    {{ item.employeeCode }} · gửi bởi {{ item.requesterUsername || 'Không xác định' }}
                  </p>
                  <p class="mt-3 text-sm text-slate-600">{{ summarizeProfileChange(item.payloadJson) }}</p>
                </div>
              </button>

              <div class="grid gap-3 md:grid-cols-2 xl:min-w-[320px]">
                <div class="rounded-2xl bg-slate-50 p-4">
                  <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Gửi lúc</p>
                  <p class="mt-2 text-sm font-bold text-slate-800">{{ formatDateTime(item.submittedAt) }}</p>
                </div>
                <div class="rounded-2xl bg-slate-50 p-4">
                  <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Người review</p>
                  <p class="mt-2 text-sm font-bold text-slate-800">{{ item.reviewerUsername || 'Chờ HR xử lý' }}</p>
                </div>
              </div>
            </div>

            <div class="mt-4 flex flex-wrap gap-2">
              <span
                v-for="entry in profileChangeEntries(item.payloadJson).slice(0, 4)"
                :key="`${item.profileChangeRequestId}-${entry.field}`"
                class="rounded-full bg-indigo-50 px-3 py-1 text-xs font-black text-indigo-700"
              >
                {{ entry.label }}
              </span>
            </div>

            <div v-if="item.requestStatus === 'PENDING'" class="mt-5 flex flex-wrap justify-end gap-3">
              <BaseButton
                variant="outline"
                :loading="actionLoading === `${item.profileChangeRequestId}-reject`"
                @click="reviewRequest(item, false)"
              >
                <XCircle class="mr-2 h-4 w-4" />
                Từ chối
              </BaseButton>
              <BaseButton
                variant="primary"
                :loading="actionLoading === `${item.profileChangeRequestId}-approve`"
                @click="reviewRequest(item, true)"
              >
                <CheckCircle2 class="mr-2 h-4 w-4" />
                Duyệt cập nhật
              </BaseButton>
            </div>
          </article>
        </div>

        <EmptyState
          v-else
          iconName="FilePenLine"
          title="Chưa có yêu cầu phù hợp"
          description="Danh sách hiện không có yêu cầu thay đổi hồ sơ nào theo trạng thái đang chọn."
        />
      </SurfacePanel>

      <SurfacePanel>
        <div v-if="selectedRequest" class="space-y-5">
          <div class="flex items-start gap-4">
            <div class="flex h-14 w-14 items-center justify-center rounded-[22px] bg-slate-900 text-lg font-black text-white">
              {{ getInitials(selectedRequest.employeeFullName) }}
            </div>
            <div class="min-w-0">
              <p class="text-[11px] font-black uppercase tracking-[0.18em] text-slate-400">Chi tiết yêu cầu</p>
              <h3 class="mt-2 text-2xl font-black text-slate-900">{{ selectedRequest.employeeFullName }}</h3>
              <p class="mt-1 text-sm font-medium text-slate-500">{{ selectedRequest.employeeCode }} · {{ selectedRequest.requesterUsername }}</p>
            </div>
          </div>

          <div class="grid gap-3 md:grid-cols-2">
            <div class="rounded-2xl bg-slate-50 p-4">
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Trạng thái</p>
              <div class="mt-2">
                <StatusBadge :status="selectedRequest.requestStatus" />
              </div>
            </div>
            <div class="rounded-2xl bg-slate-50 p-4">
              <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Review lúc</p>
              <p class="mt-2 text-sm font-bold text-slate-800">{{ formatDateTime(selectedRequest.reviewedAt) }}</p>
            </div>
          </div>

          <div class="rounded-[24px] bg-slate-50 p-4">
            <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Trường thay đổi</p>
            <div class="mt-4 space-y-3">
              <div
                v-for="entry in selectedEntries"
                :key="entry.field"
                class="rounded-2xl border border-slate-200 bg-white p-4"
              >
                <p class="text-sm font-black text-slate-900">{{ entry.label }}</p>
                <p class="mt-2 text-sm font-medium break-all text-slate-600">{{ entry.value }}</p>
              </div>
            </div>
          </div>

          <div class="rounded-[24px] border border-slate-200 p-4">
            <p class="text-[10px] font-black uppercase tracking-[0.18em] text-slate-400">Ghi chú review</p>
            <p class="mt-3 text-sm font-medium text-slate-600">{{ selectedRequest.reviewNote || 'Chưa có ghi chú review.' }}</p>
          </div>

          <div v-if="selectedRequest.requestStatus === 'PENDING'" class="flex flex-wrap gap-3">
            <BaseButton
              variant="outline"
              class="flex-1"
              :loading="actionLoading === `${selectedRequest.profileChangeRequestId}-reject`"
              @click="reviewRequest(selectedRequest, false)"
            >
              <XCircle class="mr-2 h-4 w-4" />
              Từ chối yêu cầu
            </BaseButton>
            <BaseButton
              variant="primary"
              class="flex-1"
              :loading="actionLoading === `${selectedRequest.profileChangeRequestId}-approve`"
              @click="reviewRequest(selectedRequest, true)"
            >
              <CheckCircle2 class="mr-2 h-4 w-4" />
              Duyệt hồ sơ
            </BaseButton>
          </div>
        </div>

        <EmptyState
          v-else
          iconName="Eye"
          title="Chọn một yêu cầu"
          description="Bấm vào một card ở bên trái để xem chi tiết các trường hồ sơ cần cập nhật."
        />
      </SurfacePanel>
    </div>
  </div>
</template>
