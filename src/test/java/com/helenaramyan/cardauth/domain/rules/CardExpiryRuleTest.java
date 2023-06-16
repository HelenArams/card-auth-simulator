package com.helenaramyan.cardauth.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.helenaramyan.cardauth.Fixtures;
import com.helenaramyan.cardauth.domain.CardStatus;
import com.helenaramyan.cardauth.domain.ResponseCode;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CardExpiryRuleTest {

    private static final Clock MID_2023 = Clock.fixed(Instant.parse("2023-06-15T10:00:00Z"), ZoneOffset.UTC);

    private final CardExpiryRule rule = new CardExpiryRule(MID_2023);

    @Test
    void declinesCardThatExpiredLastYear() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.cardExpiring(YearMonth.of(2022, 6))));

        assertThat(decline).isPresent();
        assertThat(decline.get().code()).isEqualTo(ResponseCode.EXPIRED_CARD);
        assertThat(decline.get().reason()).contains("2022-06");
    }

    @Test
    void declinesCardMarkedExpiredRegardlessOfDate() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.cardWith(CardStatus.EXPIRED)));

        assertThat(decline).isPresent();
        assertThat(decline.get().code()).isEqualTo(ResponseCode.EXPIRED_CARD);
    }

    @Test
    void cardIsStillGoodDuringItsExpiryMonth() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.cardExpiring(YearMonth.of(2023, 6))));

        assertThat(decline).isEmpty();
    }

    @Test
    void cardIsGoodOnTheLastDayAndExpiredTheDayAfter() {
        var expiry = YearMonth.of(2023, 6);
        var lastDay = new CardExpiryRule(Clock.fixed(Instant.parse("2023-06-30T23:59:00Z"), ZoneOffset.UTC));
        var dayAfter = new CardExpiryRule(Clock.fixed(Instant.parse("2023-07-01T00:00:00Z"), ZoneOffset.UTC));
        var context = AuthorizationContext.of(Fixtures.request(1_000), Fixtures.cardExpiring(expiry));

        assertThat(lastDay.evaluate(context)).isEmpty();
        assertThat(dayAfter.evaluate(context)).isPresent();
    }

    @Test
    void allowsCardExpiringNextYear() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.cardExpiring(YearMonth.of(2024, 6))));

        assertThat(decline).isEmpty();
    }
}
