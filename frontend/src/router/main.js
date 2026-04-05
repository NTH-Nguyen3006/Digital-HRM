import { createRouter, createWebHistory } from 'vue-router'
import { dashboardRoutes } from './dashboard'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ── Public / Portal ────────────────────────────────────────────
    {
      path: '/',
      name: 'portal',
      meta: { roles: ['employee'] }, // HR, Admin, Manager: bỏ cổng thông tin
      component: () => import('@/views/portal/PortalView.vue')
    },
    {
      path: '/portal/profile',
      name: 'my-profile',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyProfile.vue')
    },
    {
      path: '/portal/attendance',
      name: 'my-attendance',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyAttendance.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue')
    },

    ...dashboardRoutes,
    // ── Catch-all (404) ──────────────────────────────────────────
    {
      path: '/:pathMatch(.*)*',
      component: () => import('@/views/errors/404.vue')
    }
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