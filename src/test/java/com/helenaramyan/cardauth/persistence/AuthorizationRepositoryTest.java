package com.helenaramyan.cardauth.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.helenaramyan.cardauth.Fixtures;
import com.helenaramyan.cardauth.domain.AuthorizationResult;
import com.helenaramyan.cardauth.domain.ResponseCode;
import com.helenaramyan.cardauth.domain.rules.DailyActivity;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class AuthorizationRepositoryTest {

    private static final Instant MIDNIGHT = Instant.parse("2023-06-15T00:00:00Z");

    @Autowired
    private AuthorizationRepository authorizations;

    @Test
    void sumsOnlyApprovedAuthorizationsSinceTheGivenInstant() {
        authorizations.save(approved(10_000, MIDNIGHT.plusSeconds(3_600)));
        authorizations.save(approved(2_500, MIDNIGHT.plusSeconds(7_200)));
        authorizations.save(declined(99_999, MIDNIGHT.plusSeconds(7_300)));
        authorizations.save(approved(50_000, MIDNIGHT.minusSeconds(60)));

        DailyActivity activity = authorizations.approvedActivitySince(Fixtures.TOKEN, MIDNIGHT);

        assertThat(activity.count()).isEqualTo(2);
        assertThat(activity.amount()).isEqualTo(12_500);
    }

    @Test
    void reportsZeroActivityForAQuietCard() {
        DailyActivity activity = authorizations.approvedActivitySince("tok_nobody", MIDNIGHT);

        assertThat(activity).isEqualTo(DailyActivity.NONE);
    }

    @Test
    void roundTripsRequestAndResult() {
        AuthorizationEntity saved = authorizations.save(approved(1_234, MIDNIGHT));

        AuthorizationEntity found = authorizations.findById(saved.getId()).orElseThrow();

        assertThat(found.toRequest()).isEqualTo(Fixtures.request(1_234));
        assertThat(found.toResult().authCode()).isEqualTo("ABC123");
    }

    private static AuthorizationEntity approved(long amount, Instant at) {
        return new AuthorizationEntity(Fixtures.request(amount), AuthorizationResult.approved("ABC123"), at);
    }

    private static AuthorizationEntity declined(long amount, Instant at) {
        return new AuthorizationEntity(Fixtures.request(amount), AuthorizationResult.declined(ResponseCode.INSUFFICIENT_FUNDS), at);
    }
}
