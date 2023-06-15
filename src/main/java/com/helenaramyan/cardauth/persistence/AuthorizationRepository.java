package com.helenaramyan.cardauth.persistence;

import com.helenaramyan.cardauth.domain.rules.DailyActivity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorizationRepository extends JpaRepository<AuthorizationEntity, UUID> {

    Optional<AuthorizationEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * Approved activity on a card since the given instant, used by the velocity rule.
     */
    @Query("""
            select new com.helenaramyan.cardauth.domain.rules.DailyActivity(count(a), coalesce(sum(a.amount), 0L))
            from AuthorizationEntity a
            where a.cardToken = :cardToken
              and a.approved = true
              and a.createdAt >= :since
            """)
    DailyActivity approvedActivitySince(@Param("cardToken") String cardToken, @Param("since") Instant since);
}
