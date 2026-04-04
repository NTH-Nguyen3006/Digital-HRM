export const adminRoutes = [
    // ── Dashboard Overview ─────────────────────────────────────────
    {
        path: '/dashboard',
        name: 'dashboard',
        component: () => import('../views/dashboard/DashboardOverview.vue')
    },

    // ── Sprint 1: Auth / User / Role / Audit ──────────────────────
    {
        path: '/users',
        name: 'users',
        component: () => import('../views/dashboard/users/UserManagement.vue')
    },
    {
        path: '/roles',
        name: 'roles',
        component: () => import('../views/dashboard/roles/RoleManagement.vue')
    },
    {
        path: '/audit-logs',
        name: 'audit-logs',
        component: () => import('../views/dashboard/audit/AuditLogView.vue')
    },

    // ── Sprint 2: Org Unit / Employee ─────────────────────────────
    {
        path: '/org-units',
        name: 'org-units',
        component: () => import('../views/dashboard/organizations/OrgChart.vue')
    },
    {
        path: '/employees',
        name: 'employees',
        component: () => import('../views/dashboard/employees/EmployeeList.vue')
    },
    {
        path: '/employees/:id',
        name: 'employee-detail',
        component: () => import('../views/dashboard/employees/EmployeeDetail.vue')
    },

    // ── Sprint 3: Contract ─────────────────────────────────────────
    {
        path: '/contracts',
        name: 'contracts',
        component: () => import('../views/dashboard/contracts/ContractList.vue')
    },
    {
        path: '/contracts/:id',
        name: 'contract-detail',
        component: () => import('../views/dashboard/contracts/ContractDetail.vue')
    },

    {
        path: '/contracts/add',
        name: 'add-contract',
        component: () => import('../views/dashboard/contracts/AddContract.vue')
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
    {
        path: '/leaves',
        name: 'leaves',
        component: () => import('../views/dashboard/leaves/LeaveManagement.vue')
    },
    {
        path: '/attendance',
        name: 'attendance',
        component: () => import('../views/dashboard/attendance/AttendanceSheet.vue')
    },
    {
        path: '/payroll',
        name: 'payroll',
        component: () => import('../views/dashboard/payroll/PayrollList.vue')
    },
]