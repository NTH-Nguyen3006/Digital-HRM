<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import PortalLayout from '@/layouts/PortalLayout.vue'
import ErrorBoundary from '@/components/common/ErrorBoundary.vue'

const route = useRoute()
const layout = computed(() => {
  const isNotFound = route.matched.some(record => record.path.includes(':pathMatch'))
  if (isNotFound || route.meta.noLayout) return 'div'

  if (route.name === 'login') return AuthLayout
  if (route.path === '/' || route.path.startsWith('/portal')) return PortalLayout
  return MainLayout
})
</script>

<template>
  <ErrorBoundary>
    <component :is="layout">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </component>
  </ErrorBoundary>
</template>

<style>
.page-enter-active,
.page-leave-active {
  transition: opacity 0.2s ease;
}

.page-enter-from,
.page-leave-to {
  opacity: 0;
}
</style>