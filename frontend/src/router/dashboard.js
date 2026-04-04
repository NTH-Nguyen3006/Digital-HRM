export const dashboardRoutes = [
    // ── Dashboard Overview ─────────────────────────────────────────
    {
        path: '/dashboard',
        name: 'dashboard',
        meta: { roles: ['hr', 'admin', 'manager'] },
        component: () => import('../views/dashboard/DashboardOverview.vue')
    },

    // ── Sprint 1: Auth / User / Role / Audit ──────────────────────
    {
        path: '/users',
        name: 'users',
        meta: { roles: ['hr', 'admin', 'manager', 'employee'] },
        component: () => import('../views/dashboard/users/UserManagement.vue')
    },
    {
        path: '/roles',
        name: 'roles',
        meta: { roles: ['hr', 'admin', 'manager', 'employee'] },
        component: () => import('../views/dashboard/roles/RoleManagement.vue')
    },
    {
        path: '/audit-logs',
        name: 'audit-logs',
        meta: { roles: ['hr', 'admin', 'manager', 'employee'] },
        component: () => import('../views/dashboard/audit/AuditLogView.vue')
    },

    // ── Sprint 2: Org Unit / Employee ─────────────────────────────
    {
        path: '/org-units',
        name: 'org-units',
        meta: { roles: ['hr', 'admin'] },
        component: () => import('../views/dashboard/organizations/OrgChart.vue')
    },
    {
        path: '/employees',
        name: 'employees',
        meta: { roles: ['hr', 'employee', 'manager'] },
        component: () => import('../views/dashboard/employees/EmployeeList.vue')
    },
    {
        path: '/employees/:id',
        name: 'employee-detail',
        meta: { roles: ['hr', 'employee', 'manager'] },
        component: () => import('../views/dashboard/employees/EmployeeDetail.vue')
    },

    // ── Sprint 3: Contract ─────────────────────────────────────────
    {
        path: '/contracts',
        name: 'contracts',
        meta: { roles: ['hr', 'admin', 'manager'] },
        component: () => import('../views/dashboard/contracts/ContractList.vue')
    },
    {
        path: '/contracts/:id',
        name: 'contract-detail',
        meta: { roles: ['hr', 'admin', 'manager'] },
        component: () => import('../views/dashboard/contracts/ContractDetail.vue')
    },

    {
        path: '/contracts/add',
        name: 'add-contract',
        meta: { roles: ['hr', 'admin', 'manager'] },
        component: () => import('../views/dashboard/contracts/AddContract.vue')
    },

    // ── Gap Fill: Profile Change Requests / Onboarding / Settings ─
    {
        path: '/profile-change-requests',
        name: 'profile-change-requests',
        meta: { roles: ['hr', 'admin', 'employee', 'manager'] },
        component: () => import('../views/dashboard/profile/ProfileChangeRequests.vue')
    },
    {
        path: '/onboarding',
        name: 'onboarding',
        meta: { roles: ['hr', 'admin', 'manager'] },
        component: () => import('../views/dashboard/onboarding/OnboardingBoard.vue')
    },
    {
        path: '/offboarding',
        name: 'offboarding',
        meta: { roles: ['hr', 'admin', 'manager', 'employee'] },
        component: () => import('../views/dashboard/offboarding/OffboardingBoard.vue')
    },
    {
        path: '/settings',
        name: 'settings',
        meta: { roles: ['hr', 'admin', 'manager', 'employee'] },
        component: () => import('../views/dashboard/settings/SystemSettings.vue')
    },

    // ── Other Modules ──────────────────────────────────────────────
    {
        path: '/leaves',
        name: 'leaves',
        meta: { roles: ['hr', 'employee', 'manager'] },
        component: () => import('../views/dashboard/leaves/LeaveManagement.vue')
    },
    {
        path: '/attendance',
        name: 'attendance',
        meta: { roles: ['hr', 'employee', 'manager'] },
        component: () => import('../views/dashboard/attendance/AttendanceSheet.vue')
    },
    {
        path: '/payroll',
        name: 'payroll',
        meta: { roles: ['hr', 'employee', 'manager'] },
        component: () => import('../views/dashboard/payroll/PayrollList.vue')
    },
]
