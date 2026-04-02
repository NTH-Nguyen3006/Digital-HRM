import { createRouter, createWebHistory } from 'vue-router'
import { adminRoutes } from './admin'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ── Public / Portal ────────────────────────────────────────────
    {
      path: '/',
      name: 'portal',
      component: () => import('../views/portal/PortalView.vue')
    },
    {
      path: '/portal/profile',
      name: 'my-profile',
      component: () => import('../views/portal/MyProfile.vue')
    },
    {
      path: '/portal/attendance',
      name: 'my-attendance',
      component: () => import('../views/portal/MyAttendance.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/auth/LoginView.vue')
    },

    ...adminRoutes,
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

export default router