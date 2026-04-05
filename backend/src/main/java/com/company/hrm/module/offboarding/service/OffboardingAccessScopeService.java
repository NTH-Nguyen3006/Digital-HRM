package com.company.hrm.module.offboarding.service;

import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.exception.ForbiddenException;
import com.company.hrm.common.exception.UnauthorizedException;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.security.SecurityUserContext;
import com.company.hrm.security.SecurityUserPrincipal;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class OffboardingAccessScopeService {

    private final SecUserAccountRepository userAccountRepository;
    private final HrEmployeeRepository employeeRepository;

    public OffboardingAccessScopeService(SecUserAccountRepository userAccountRepository,
                                         HrEmployeeRepository employeeRepository) {
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
    }

    public SecUserAccount getCurrentUserRequired() {
        SecurityUserPrincipal principal = SecurityUserContext.currentPrincipal()
                .orElseThrow(() -> new UnauthorizedException("AUTH_UNAUTHORIZED", "Chưa xác thực người dùng."));
        return userAccountRepository.findById(principal.getUserId())
                .orElseThrow(() -> new UnauthorizedException("USER_NOT_FOUND", "Không tìm thấy tài khoản hiện tại."));
    }

    public HrEmployee getCurrentEmployeeRequired() {
        SecUserAccount user = getCurrentUserRequired();
        if (user.getEmployeeId() == null) {
            throw new ForbiddenException("EMPLOYEE_LINK_REQUIRED", "Tài khoản hiện tại chưa được liên kết hồ sơ nhân sự.");
        }
        return employeeRepository.findByEmployeeIdAndDeletedFalse(user.getEmployeeId())
                .orElseThrow(() -> new ForbiddenException("EMPLOYEE_NOT_FOUND", "Không tìm thấy hồ sơ nhân sự hiện tại."));
    }

    public void assertManagerCanAccessEmployee(HrEmployee targetEmployee) {
        SecurityUserPrincipal principal = SecurityUserContext.currentPrincipal()
                .orElseThrow(() -> new UnauthorizedException("AUTH_UNAUTHORIZED", "Chưa xác thực người dùng."));
        if (principal.getRoleCode() != RoleCode.MANAGER) {
            throw new ForbiddenException("MANAGER_SCOPE_REQUIRED", "Tài khoản hiện tại không thuộc vai trò quản lý.");
        }
        HrEmployee currentEmployee = getCurrentEmployeeRequired();
        String currentPath = currentEmployee.getOrgUnit() == null ? null : currentEmployee.getOrgUnit().getPathCode();
        String targetPath = targetEmployee.getOrgUnit() == null ? null : targetEmployee.getOrgUnit().getPathCode();
        if (currentPath != null && targetPath != null && targetPath.startsWith(currentPath)) {
            return;
        }
        throw new ForbiddenException("OFFBOARDING_MANAGER_SCOPE_DENIED", "Bạn không có quyền xử lý offboarding của nhân sự này.");
    }

    public Optional<String> getManagerOrgPathPrefix() {
        return SecurityUserContext.currentPrincipal()
                .filter(principal -> principal.getRoleCode() == RoleCode.MANAGER)
                .map(principal -> getCurrentEmployeeRequired())
                .map(employee -> employee.getOrgUnit() == null ? null : employee.getOrgUnit().getPathCode());
    }
}
