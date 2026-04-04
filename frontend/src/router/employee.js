export const employeeRoutes = [
    // ── Sprint 1: Auth / User / Role / Audit ──────────────────────
    {
        path: '/users',
        name: 'users',
        component: () => import('../views/dashboard/users/UserManagement.vue')
    },

    // ── Gap Fill: Profile Change Requests / Onboarding / Settings ─
    {
        path: '/profile-change-requests',
        name: 'profile-change-requests',
        component: () => import('../views/dashboard/profile/ProfileChangeRequests.vue')
    },
    {
        path: '/onboarding',
        name: 'onboarding',
        component: () => import('../views/dashboard/onboarding/OnboardingBoard.vue')
    },
    {
        path: '/offboarding',
        name: 'offboarding',
        component: () => import('../views/dashboard/offboarding/OffboardingBoard.vue')
    },
    {
        path: '/settings',
        name: 'settings',
        component: () => import('../views/dashboard/settings/SystemSettings.vue')
    },

    // ── Other Modules ──────────────────────────────────────────────
]