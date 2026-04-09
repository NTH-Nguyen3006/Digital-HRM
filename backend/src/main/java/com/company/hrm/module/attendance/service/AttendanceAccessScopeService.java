package com.company.hrm.module.attendance.service;

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
public class AttendanceAccessScopeService {

    private final SecUserAccountRepository userAccountRepository;
    private final HrEmployeeRepository employeeRepository;

    public AttendanceAccessScopeService(
            SecUserAccountRepository userAccountRepository,
            HrEmployeeRepository employeeRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
    }

    public HrEmployee getCurrentEmployeeRequired() {
        SecurityUserPrincipal principal = SecurityUserContext.currentPrincipal()
                .orElseThrow(() -> new UnauthorizedException("AUTH_UNAUTHORIZED", "Chưa xác thực người dùng."));
        SecUserAccount user = userAccountRepository.findById(principal.getUserId())
                .orElseThrow(() -> new UnauthorizedException("USER_NOT_FOUND", "Không tìm thấy tài khoản hiện tại."));
        if (user.getEmployeeId() == null) {
            throw new ForbiddenException("EMPLOYEE_LINK_REQUIRED", "Tài khoản hiện tại chưa được liên kết hồ sơ nhân sự.");
        }
        return employeeRepository.findByEmployeeIdAndDeletedFalse(user.getEmployeeId())
                .orElseThrow(() -> new ForbiddenException("EMPLOYEE_NOT_FOUND", "Không tìm thấy hồ sơ nhân sự hiện tại."));
    }

    public boolean isHrOrAdmin() {
        return SecurityUserContext.currentPrincipal()
                .map(principal -> principal.getRoleCode() == RoleCode.ADMIN || principal.getRoleCode() == RoleCode.HR)
                .orElse(false);
    }

    public void assertManagerCanAccessEmployee(HrEmployee targetEmployee) {
        SecurityUserPrincipal principal = SecurityUserContext.currentPrincipal()
                .orElseThrow(() -> new UnauthorizedException("AUTH_UNAUTHORIZED", "Chưa xác thực người dùng."));
        if (principal.getRoleCode() != RoleCode.MANAGER) {
            throw new ForbiddenException("MANAGER_SCOPE_REQUIRED", "Tài khoản hiện tại không thuộc vai trò quản lý.");
        }
        HrEmployee currentEmployee = getCurrentEmployeeRequired();
        Long directManagerEmployeeId = targetEmployee.getManagerEmployee() == null ? null : targetEmployee.getManagerEmployee().getEmployeeId();
        if (directManagerEmployeeId != null && directManagerEmployeeId.equals(currentEmployee.getEmployeeId())) {
            return;
        }
        throw new ForbiddenException("ATTENDANCE_MANAGER_SCOPE_DENIED", "Bạn không phải quản lý trực tiếp của nhân sự này.");
    }

    public Optional<Long> getCurrentManagerEmployeeId() {
        return SecurityUserContext.currentPrincipal()
                .filter(principal -> principal.getRoleCode() == RoleCode.MANAGER)
                .map(principal -> getCurrentEmployeeRequired().getEmployeeId());
    }
}
