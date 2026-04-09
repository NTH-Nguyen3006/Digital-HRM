import { createRouter, createWebHistory } from 'vue-router'
import { dashboardRoutes } from './dashboard'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ── Public / Portal
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
      path: '/portal/leaves',
      name: 'my-leaves',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyLeaves.vue')
    },
    {
      path: '/portal/payslip',
      name: 'my-payslip',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyPayslip.vue')
    },
    {
      path: '/portal/attendance-adjustment',
      name: 'my-attendance-adjustment',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyAttendanceAdjustment.vue')
    },
    {
      path: '/portal/overtime',
      name: 'my-overtime',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyOvertime.vue')
    },
    {
      path: '/portal/contract',
      name: 'my-contract',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyContract.vue')
    },
    {
      path: '/portal/resignation',
      name: 'my-resignation',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyResignation.vue')
    },
    {
      path: '/portal/profile-change',
      name: 'my-profile-change',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyProfileChange.vue')
    },
    {
      path: '/portal/inbox',
      name: 'my-inbox',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyInbox.vue')
    },
    {
      path: '/portal/tasks',
      name: 'my-tasks',
      meta: { roles: ['employee'] },
      component: () => import('@/views/portal/MyTasks.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue')
    },

    ...dashboardRoutes,
    // ── Catch-all (404) 
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

router.beforeEach((to) => {
  const isAuthenticated = localStorage.getItem('isAuthenticated') === 'true'
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const roleCode = user?.roleCode?.toUpperCase?.() || ''
  const isEmployee = roleCode === 'EMPLOYEE' || user?.roles?.includes?.('employee')
  const isPortalRoute = to.path === '/' || to.path.startsWith('/portal')
  const isDashboardRoute = to.path.startsWith('/dashboard')
    || to.path.startsWith('/admin')
    || to.path.startsWith('/hr')
    || to.path.startsWith('/manager')
    || dashboardRoutes.some((route) => route.path === to.path)

  if (to.name === 'login' && isAuthenticated) {
    return isEmployee ? '/' : '/dashboard'
  }

  if (isAuthenticated && isEmployee && isDashboardRoute) {
    return '/'
  }

  if (isAuthenticated && !isEmployee && isPortalRoute) {
    return '/dashboard'
  }

  return true
})

export default router
