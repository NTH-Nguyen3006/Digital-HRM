package com.company.hrm.module.employee.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.exception.ForbiddenException;
import com.company.hrm.common.exception.UnauthorizedException;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.orgunit.repository.HrOrgUnitRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.security.SecurityUserContext;
import com.company.hrm.security.SecurityUserPrincipal;

@Service
public class EmployeeAccessScopeService {

    private final SecUserAccountRepository userAccountRepository;
    private final HrEmployeeRepository employeeRepository;
    private final HrOrgUnitRepository orgUnitRepository;

    public EmployeeAccessScopeService(
            SecUserAccountRepository userAccountRepository,
            HrEmployeeRepository employeeRepository,
            HrOrgUnitRepository orgUnitRepository) {
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    public Specification<HrEmployee> applyEmployeeReadScope(Specification<HrEmployee> specification) {
        ScopeContext scope = resolveScope();
        if (scope.global()) {
            return specification;
        }
        if (scope.orgPathPrefix() != null) {
            return specification.and((root, query, builder) -> builder.like(root.join("orgUnit").get("pathCode"),
                    scope.orgPathPrefix() + "%"));
        }
        return specification.and((root, query, builder) -> builder.equal(root.get("employeeId"), scope.employeeId()));
    }

    public Specification<HrOrgUnit> applyOrgUnitReadScope(Specification<HrOrgUnit> specification) {
        ScopeContext scope = resolveScope();
        if (scope.global()) {
            return specification;
        }
        if (scope.orgPathPrefix() != null) {
            return specification
                    .and((root, query, builder) -> builder.like(root.get("pathCode"), scope.orgPathPrefix() + "%"));
        }
        return specification.and((root, query, builder) -> builder.equal(root.get("orgUnitId"), scope.orgUnitId()));
    }

    public void assertCanReadEmployee(HrEmployee employee) {
        ScopeContext scope = resolveScope();
        if (scope.global()) {
            return;
        }
        if (scope.orgPathPrefix() != null) {
            String employeePath = employee.getOrgUnit() == null ? null : employee.getOrgUnit().getPathCode();
            if (employeePath != null && employeePath.startsWith(scope.orgPathPrefix())) {
                return;
            }
        }
        if (scope.employeeId() != null && scope.employeeId().equals(employee.getEmployeeId())) {
            return;
        }
        throw new ForbiddenException("EMPLOYEE_SCOPE_DENIED", "Bạn không có quyền truy cập hồ sơ nhân sự này.");
    }

    public void assertCanReadOrgUnit(HrOrgUnit orgUnit) {
        ScopeContext scope = resolveScope();
        if (scope.global()) {
            return;
        }
        if (scope.orgPathPrefix() != null && orgUnit.getPathCode() != null
                && orgUnit.getPathCode().startsWith(scope.orgPathPrefix())) {
            return;
        }
        if (scope.orgUnitId() != null && scope.orgUnitId().equals(orgUnit.getOrgUnitId())) {
            return;
        }
        throw new ForbiddenException("ORG_UNIT_SCOPE_DENIED", "Bạn không có quyền truy cập đơn vị tổ chức này.");
    }

    private ScopeContext resolveScope() {
        SecurityUserPrincipal principal = SecurityUserContext.currentPrincipal()
                .orElseThrow(() -> new UnauthorizedException("AUTH_UNAUTHORIZED", "Chưa xác thực người dùng."));

        if (principal.getRoleCode() == RoleCode.ADMIN || principal.getRoleCode() == RoleCode.HR) {
            return new ScopeContext(true, null, null, null);
        }

        SecUserAccount user = userAccountRepository.findById(principal.getUserId())
                .orElseThrow(() -> new UnauthorizedException("USER_NOT_FOUND", "Không tìm thấy tài khoản hiện tại."));

        if (user.getEmployeeId() == null) {
            throw new ForbiddenException("EMPLOYEE_LINK_REQUIRED",
                    "Tài khoản hiện tại chưa được liên kết hồ sơ nhân sự.");
        }

        HrEmployee employee = employeeRepository.findByEmployeeIdAndDeletedFalse(user.getEmployeeId())
                .orElseThrow(() -> new ForbiddenException("EMPLOYEE_SCOPE_ROOT_NOT_FOUND",
                        "Không tìm thấy hồ sơ nhân sự gốc để xác định phạm vi dữ liệu."));

        Long orgUnitId = employee.getOrgUnit() == null ? null : employee.getOrgUnit().getOrgUnitId();
        String pathCode = employee.getOrgUnit() == null ? null : employee.getOrgUnit().getPathCode();

        if (principal.getRoleCode() == RoleCode.MANAGER) {
            return new ScopeContext(false, employee.getEmployeeId(), orgUnitId, pathCode);
        }

        return new ScopeContext(false, employee.getEmployeeId(), orgUnitId, null);
    }

    private record ScopeContext(boolean global, Long employeeId, Long orgUnitId, String orgPathPrefix) {
    }
}
