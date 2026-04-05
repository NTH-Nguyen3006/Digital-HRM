package com.company.hrm.module.auth.repository;

import com.company.hrm.common.constant.SessionStatus;
import com.company.hrm.module.auth.entity.SecAuthSession;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecAuthSessionRepository extends JpaRepository<SecAuthSession, UUID> {

    @EntityGraph(attributePaths = {"user"})
    Optional<SecAuthSession> findByRefreshTokenHashAndStatus(String refreshTokenHash, SessionStatus status);

    @EntityGraph(attributePaths = {"user"})
    Optional<SecAuthSession> findByAuthSessionId(UUID authSessionId);

    List<SecAuthSession> findAllByUserUserIdAndStatus(UUID userId, SessionStatus status);

    List<SecAuthSession> findAllByUserUserIdAndStatusIn(UUID userId, Collection<SessionStatus> statuses);
}
