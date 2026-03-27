package com.company.hrm.module.auth.repository;

import com.company.hrm.common.constant.ResetTokenStatus;
import com.company.hrm.module.auth.entity.SecPasswordResetToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecPasswordResetTokenRepository extends JpaRepository<SecPasswordResetToken, UUID> {

    Optional<SecPasswordResetToken> findByTokenHashAndStatus(String tokenHash, ResetTokenStatus status);

    List<SecPasswordResetToken> findAllByUserUserIdAndStatus(UUID userId, ResetTokenStatus status);
}
