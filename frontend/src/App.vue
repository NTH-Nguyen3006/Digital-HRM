<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import PortalLayout from '@/layouts/PortalLayout.vue'
import ErrorBoundary from '@/components/common/ErrorBoundary.vue'
import ToastNotification from '@/components/common/ToastNotification.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useUiStore } from '@/stores/ui'

const route = useRoute()
const ui = useUiStore()
const layout = computed(() => {
  const isNotFound = route.matched.some(record => record.path.includes(':pathMatch'))
  if (isNotFound || route.meta.noLayout) return 'div'

  if (route.name === 'login') return 'div'
  if (route.path === '/' || route.path.startsWith('/portal')) return PortalLayout
  return MainLayout
})
</script>

<template>
  <ErrorBoundary>
    <transition name="route-progress">
      <div v-if="ui.routeLoading" class="pointer-events-none fixed inset-x-0 top-0 z-[10000]">
        <div class="route-progress-bar"></div>
      </div>
    </transition>

    <component :is="layout">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <div :key="route.path">
            <component :is="Component" />
          </div>
        </transition>
      </router-view>
    </component>
  </ErrorBoundary>

  <ToastNotification />
  <ConfirmDialog />
</template>

<style>
.page-enter-active,
.page-leave-active {
  transition: opacity 0.26s ease, transform 0.26s ease;
}

.page-enter-from,
.page-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

.route-progress-enter-active,
.route-progress-leave-active {
  transition: opacity 0.2s ease;
}

.route-progress-enter-from,
.route-progress-leave-to {
  opacity: 0;
}

.route-progress-bar {
  height: 3px;
  width: 100%;
  background:
    linear-gradient(90deg, rgba(79, 70, 229, 0.08), rgba(79, 70, 229, 0.9), rgba(34, 197, 94, 0.7));
  transform-origin: left center;
  animation: routeProgress 1.15s cubic-bezier(0.16, 1, 0.3, 1) infinite;
  box-shadow: 0 0 18px rgba(79, 70, 229, 0.25);
}

@keyframes routeProgress {
  0% {
    transform: scaleX(0.12);
    opacity: 0.7;
  }

  50% {
    transform: scaleX(0.72);
    opacity: 1;
  }

  100% {
    transform: scaleX(1);
    opacity: 0.8;
  }
}
</style>
