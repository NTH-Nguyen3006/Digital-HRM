import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'portal',
      component: () => import('./views/portal/PortalView.vue')
    },
    {
      path: '/portal/profile',
      name: 'my-profile',
      component: () => import('./views/portal/MyProfile.vue')
    },
    {
      path: '/portal/attendance',
      name: 'my-attendance',
      component: () => import('./views/portal/MyAttendance.vue')
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('./views/dashboard/DashboardOverview.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('./views/auth/LoginView.vue')
    },


    {
      path: '/org-units',
      name: 'org-units',
      component: () => import('./views/dashboard/organizations/OrgChart.vue')
    },
    {
      path: '/employees',
      name: 'employees',
      component: () => import('./views/dashboard/employees/EmployeeList.vue')
    },
    {
      path: '/contracts',
      name: 'contracts',
      component: () => import('./views/dashboard/contracts/ContractList.vue')
    },
    {
      path: '/leaves',
      name: 'leaves',
      component: () => import('./views/dashboard/leaves/LeaveManagement.vue')
    },
    {
      path: '/attendance',
      name: 'attendance',
      component: () => import('./views/dashboard/attendance/AttendanceSheet.vue')
    },
    {
      path: '/payroll',
      name: 'payroll',
      component: () => import('./views/dashboard/payroll/PayrollList.vue')
    },
    {
      path: '/onboarding',
      name: 'onboarding',
      component: () => import('./views/dashboard/onboarding/OnboardingBoard.vue')
    },
    {
      path: '/offboarding',
      name: 'offboarding',
      component: () => import('./views/dashboard/offboarding/OffboardingBoard.vue')
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