package com.company.hrm.module.user.repository;

import com.company.hrm.module.user.entity.SecUserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SecUserAccountRepository extends JpaRepository<SecUserAccount, UUID>, JpaSpecificationExecutor<SecUserAccount> {

    Optional<SecUserAccount> findByUsernameIgnoreCaseAndDeletedFalse(String username);

    Optional<SecUserAccount> findByEmailIgnoreCaseAndDeletedFalse(String email);

    boolean existsByUsernameIgnoreCaseAndDeletedFalse(String username);

    boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);

    boolean existsByUsernameIgnoreCaseAndDeletedFalseAndUserIdNot(String username, UUID userId);

    boolean existsByEmailIgnoreCaseAndDeletedFalseAndUserIdNot(String email, UUID userId);

    boolean existsByEmployeeIdAndDeletedFalse(Long employeeId);

    boolean existsByEmployeeIdAndDeletedFalseAndUserIdNot(Long employeeId, UUID userId);
}
