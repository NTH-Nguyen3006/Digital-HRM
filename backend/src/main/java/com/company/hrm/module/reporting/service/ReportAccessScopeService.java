package com.company.hrm.module.reporting.service;

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
public class ReportAccessScopeService {

    private final SecUserAccountRepository userAccountRepository;
    private final HrEmployeeRepository employeeRepository;

    public ReportAccessScopeService(
            SecUserAccountRepository userAccountRepository,
            HrEmployeeRepository employeeRepository
    ) {
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

    public String getManagerOrgPathPrefixRequired() {
        HrEmployee currentEmployee = getCurrentEmployeeRequired();
        String pathPrefix = currentEmployee.getOrgUnit() == null ? null : currentEmployee.getOrgUnit().getPathCode();
        if (pathPrefix == null || pathPrefix.isBlank()) {
            throw new ForbiddenException("REPORT_MANAGER_SCOPE_INVALID", "Không xác định được phạm vi dữ liệu đội nhóm của manager.");
        }
        return pathPrefix;
    }

    public Optional<String> getCurrentUsername() {
        return SecurityUserContext.getCurrentUsername();
    }
}
