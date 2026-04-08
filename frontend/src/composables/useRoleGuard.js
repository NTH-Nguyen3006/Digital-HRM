import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'

/**
 * Role-based access guard composable
 * @returns {Object} - role check helpers
 */
export function useRoleGuard() {
  const auth = useAuthStore()

  const isHR = computed(() => auth.isHR || auth.isAdmin)
  const isManager = computed(() => auth.isManager)
  const isEmployee = computed(() => auth.isEmployee)
  const isAdminOrHR = computed(() => auth.isAdmin || auth.isHR)

  /**
   * Check if current user has any of the given roles
   * @param {string[]} roles - e.g. ['hr','admin']
   */
  function hasRole(...roles) {
    const userRole = auth.roleCode?.toLowerCase()
    return roles.map(r => r.toLowerCase()).includes(userRole)
  }

  return { isHR, isManager, isEmployee, isAdminOrHR, hasRole }
}
