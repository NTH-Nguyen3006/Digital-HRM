<script setup>
import MainLayout from "@/layouts/MainLayout.vue"
import GlassCard from "@/components/common/GlassCard.vue"
import StatCard from "@/components/common/StatCard.vue"

import VueApexCharts from "vue3-apexcharts"

import {
  Users,
  UserPlus,
  UserMinus,
  Briefcase,
  AlertTriangle,
  FileWarning
} from "lucide-vue-next"

/* KPI */

const stats = [
  { title: "Tổng nhân sự", value: "1,284", icon: Users, color: "indigo" },
  { title: "Nhân viên mới", value: "24", icon: UserPlus, color: "emerald" },
  { title: "Đang thử việc", value: "36", icon: Briefcase, color: "amber" },
  { title: "Nghỉ việc tháng này", value: "12", icon: UserMinus, color: "rose" }
]

/* ALERTS */

const alerts = [
  { title: "Hợp đồng sắp hết hạn", count: 8, icon: FileWarning },
  { title: "Bất thường chấm công", count: 15, icon: AlertTriangle }
]

/* HEADCOUNT CHART */

const headcountSeries = [
  {
    name: "Nhân sự",
    data: [320, 120, 80, 150, 90]
  }
]

const headcountOptions = {
  chart: {
    type: "bar",
    toolbar: { show: false }
  },

  colors: ["#6366f1"],

  plotOptions: {
    bar: {
      borderRadius: 10,
      columnWidth: "40%"
    }
  },

  dataLabels: { enabled: false },

  xaxis: {
    categories: [
      "Engineering",
      "HR",
      "Finance",
      "Marketing",
      "Sales"
    ]
  },

  grid: {
    borderColor: "#f1f5f9"
  }
}

/* JOIN VS LEAVE */

const trendSeries = [
  {
    name: "Nhân viên vào",
    data: [12, 18, 10, 20, 15, 24]
  },
  {
    name: "Nghỉ việc",
    data: [3, 5, 4, 7, 6, 8]
  }
]

const trendOptions = {
  chart: {
    type: "line",
    toolbar: { show: false }
  },

  stroke: {
    curve: "smooth",
    width: 3
  },

  colors: ["#10b981", "#ef4444"],

  markers: { size: 5 },

  xaxis: {
    categories: ["Jan", "Feb", "Mar", "Apr", "May", "Jun"]
  },

  legend: {
    position: "top"
  },

  grid: {
    borderColor: "#f1f5f9"
  }
}

/* CONTRACT TYPE DONUT */

const contractSeries = [900, 220, 90, 74]

const contractOptions = {
  chart: {
    type: "donut"
  },

  labels: [
    "Fulltime",
    "Probation",
    "Intern",
    "Contract"
  ],

  colors: [
    "#6366f1",
    "#22c55e",
    "#f59e0b",
    "#ef4444"
  ],

  legend: {
    position: "bottom"
  }
}
</script>

<template>
  <MainLayout>
    <div class="space-y-10">

      <!-- HEADER -->

      <div class="flex justify-between items-center">

        <div>
          <h2 class="text-3xl font-bold text-slate-900">
            HR Dashboard
          </h2>

          <p class="text-slate-500">
            Tổng quan hệ thống nhân sự
          </p>
        </div>

        <button class="bg-indigo-600 text-white px-5 py-2 rounded-xl font-semibold hover:bg-indigo-700 transition">
          Xuất báo cáo
        </button>

      </div>

      <!-- KPI -->

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">

        <StatCard v-for="stat in stats" :key="stat.title" :title="stat.title" :value="stat.value" :icon="stat.icon"
          :color="stat.color" />

      </div>

      <!-- ALERTS + DONUT -->

      <div class="grid lg:grid-cols-3 gap-8">

        <!-- ALERT -->

        <GlassCard>

          <h3 class="text-lg font-bold mb-6">
            Cảnh báo hệ thống
          </h3>

          <div v-for="alert in alerts" :key="alert.title"
            class="flex justify-between items-center p-3 rounded-xl hover:bg-slate-50">

            <div class="flex items-center gap-3">

              <component :is="alert.icon" class="w-5 h-5 text-slate-500" />

              <span class="text-sm font-medium text-slate-700">
                {{ alert.title }}
              </span>

            </div>

            <span class="text-sm font-bold px-3 py-1 rounded-lg bg-slate-100">
              {{ alert.count }}
            </span>

          </div>

        </GlassCard>

        <!-- CONTRACT CHART -->

        <GlassCard class="lg:col-span-2">

          <h3 class="text-lg font-bold mb-4">
            Cơ cấu loại hợp đồng
          </h3>

          <VueApexCharts type="donut" height="300" :options="contractOptions" :series="contractSeries" />

        </GlassCard>

      </div>

      <!-- CHARTS -->

      <div class="grid lg:grid-cols-2 gap-8">

        <!-- HEADCOUNT -->

        <GlassCard>

          <h3 class="text-lg font-bold mb-4">
            Headcount theo phòng ban
          </h3>

          <VueApexCharts type="bar" height="320" :options="headcountOptions" :series="headcountSeries" />

        </GlassCard>

        <!-- TREND -->

        <GlassCard>

          <h3 class="text-lg font-bold mb-4">
            Nhân sự vào / nghỉ theo tháng
          </h3>

          <VueApexCharts type="line" height="320" :options="trendOptions" :series="trendSeries" />

        </GlassCard>

      </div>

    </div>
  </MainLayout>
</template>
<style scoped></style>
