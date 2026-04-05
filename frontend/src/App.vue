<script setup>
import ErrorBoundary from '@/components/common/ErrorBoundary.vue'
</script>

<template>
  <ErrorBoundary>
    <router-view v-slot="{ Component, route }">
      <transition name="page" mode="out-in">
        <div :key="route.path" class="w-full h-full">
          <component :is="Component" />
        </div>
      </transition>
    </router-view>
  </ErrorBoundary>
</template>


<style>
.page-enter-active,
.page-leave-active {
  transition: opacity .2s ease;
}

/* .page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
} */
</style>
