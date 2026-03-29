<script setup>
import AppHeader from '@/components/partials/AppHeader.vue'
import AppFooter from '@/components/partials/AppFooter.vue'
</script>

<template>
  <div
    class="min-h-screen bg-white flex flex-col font-sans selection:bg-indigo-100 selection:text-indigo-900 overflow-x-hidden">
    <AppHeader variant="portal" />

    <main class="flex-1 pt-20">
      <slot />
    </main>

    <AppFooter variant="portal" />
  </div>
</template>
