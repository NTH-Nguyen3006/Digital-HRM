<script setup>
import { ref } from "vue"
import MainLayout from "../../../layouts/MainLayout.vue"
import { Upload } from "lucide-vue-next";

const form = ref({
    contractNumber: "",
    employee: "",
    contractType: "",
    signDate: "",
    effectiveDate: "",
    endDate: "",
    department: "",
    jobTitle: "",
    workingType: "FULL_TIME",
    workLocation: "",
    baseSalary: "",
    salaryCurrency: "VND",
    note: ""
})
</script>

<template>
    <MainLayout>
        <div class="max-w-6xl mx-auto space-y-6">
            <div class="flex items-center justify-between">
                <div>
                    <h2 class="text-3xl font-black text-slate-900">Tạo hợp đồng lao động</h2>
                    <p class="text-slate-500 mt-1">Nhập thông tin hoặc upload hợp đồng để tự động điền</p>
                </div>

                <button @click="chooseFile"
                    class="flex items-center bg-white border border-indigo-500 px-4 py-2.5 rounded-xl font-semibold text-slate-700 hover:bg-slate-50 shadow-sm">

                    <Upload class="w-5 h-5 mr-2 text-indigo-500" />
                    Tải lên
                </button>

                <input type="file" ref="fileInput" accept=".pdf,.doc,.docx" class="hidden" @change="handleFileUpload" />
            </div>

            <!-- THÔNG TIN HỢP ĐỒNG -->
            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6 space-y-6">
                <h3 class="font-bold text-lg text-slate-800">
                    Thông tin hợp đồng
                </h3>
                <div class="grid grid-cols-2 gap-6">
                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Số hợp đồng</label>
                        <input v-model="form.contractNumber"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500">
                    </div>
                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Loại hợp đồng</label>
                        <select v-model="form.contractType"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500">
                            <option>Thử việc</option>
                            <option>Xác định thời hạn</option>
                            <option>Không xác định thời hạn</option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Nhân viên</label>
                        <select v-model="form.employee"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm"></select>
                    </div>
                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Hình thức làm việc</label>
                        <select v-model="form.workingType"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm">
                            <option value="FULL_TIME">Full time</option>
                            <option value="PART_TIME">Part time</option>
                        </select>
                    </div>
                </div>
            </div>

            <!-- THỜI HẠN -->
            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6 space-y-6">
                <h3 class="font-bold text-lg text-slate-800">
                    Thời hạn hợp đồng
                </h3>
                <div class="grid grid-cols-3 gap-6">

                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Ngày ký</label>
                        <input type="date" v-model="form.signDate"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm">
                    </div>
                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Ngày hiệu lực</label>
                        <input type="date" v-model="form.effectiveDate"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm">
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Ngày kết thúc</label>
                        <input type="date" v-model="form.endDate"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm">
                    </div>
                </div>
            </div>

            <!-- CÔNG VIỆC -->
            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6 space-y-6">
                <h3 class="font-bold text-lg text-slate-800">
                    Thông tin công việc
                </h3>
                <div class="grid grid-cols-2 gap-6">
                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Phòng ban</label>
                        <select v-model="form.department"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm"></select>
                    </div>
                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Chức danh</label>
                        <select v-model="form.jobTitle"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm"></select>
                    </div>
                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Nơi làm việc</label>
                        <input v-model="form.workLocation"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm">
                    </div>
                </div>
            </div>

            <!-- LƯƠNG -->
            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6 space-y-6">

                <h3 class="font-bold text-lg text-slate-800">
                    Thông tin lương
                </h3>

                <div class="grid grid-cols-2 gap-6">

                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Lương cơ bản</label>
                        <input type="number" v-model="form.baseSalary"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm">
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-600 mb-1">Tiền tệ</label>
                        <select v-model="form.salaryCurrency"
                            class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm">
                            <option value="VND">VND</option>
                            <option value="USD">USD</option>
                        </select>
                    </div>
                </div>
            </div>

            <!-- GHI CHÚ -->
            <div class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6">

                <label class="block text-sm font-semibold text-slate-600 mb-1">Ghi chú</label>

                <textarea v-model="form.note"
                    class="w-full border border-slate-200 rounded-xl px-3 py-2.5 text-sm h-28"></textarea>

            </div>

            <!-- BUTTON -->
            <div class="flex justify-end gap-3">

                <button
                    class="px-5 py-2.5 rounded-xl border border-slate-200 font-semibold text-slate-600 hover:bg-slate-50">
                    Hủy
                </button>

                <button
                    class="px-5 py-2.5 rounded-xl bg-indigo-600 font-bold text-white hover:bg-indigo-700 shadow-lg shadow-indigo-200">
                    Lưu hợp đồng
                </button>
            </div>
        </div>
    </MainLayout>
</template>

<style scoped>
input[type="date"] {
    position: relative;
}

/* 
0°   = Đỏ
120° = Xanh lá
180° = Xanh lam (cyan)
220° = Xanh indigo/tím ← chúng ta dùng cái này
240° = Xanh dương
300° = Hồng tím
360° = Đỏ (quay vòng)
*/
input[type="date"]::-webkit-calendar-picker-indicator {
    filter: invert(29%) sepia(98%) saturate(1500%) hue-rotate(220deg) brightness(90%) contrast(120%);
    font-weight: bolder;
    cursor: pointer;
    transform: scale(1.2);
    /* Định vị lại tuyệt đối để đảm bảo icon dịch chuyển vào trong */
    position: relative;
    right: 8px;
}

select {
    appearance: none;
    background-image: url("data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiIGNsYXNzPSJsdWNpZGUgbHVjaWRlLWNoZXZyb24tZG93bi1pY29uIGx1Y2lkZS1jaGV2cm9uLWRvd24iPjxwYXRoIGQ9Im02IDkgNiA2IDYtNiIvPjwvc3ZnPg==");
    background-size: 16px;
    background-repeat: no-repeat;
    background-position: right 16px center;
    padding-right: 2.5rem;
}
</style>