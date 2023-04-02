package com.helenaramyan.cardauth.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.helenaramyan.cardauth.Fixtures;
import com.helenaramyan.cardauth.domain.Card;
import com.helenaramyan.cardauth.domain.ResponseCode;
import org.junit.jupiter.api.Test;

class VelocityRuleTest {

    private final VelocityRule rule = new VelocityRule(10);
    private final Card card = Fixtures.activeCard(1_000_000, 100_000);

    @Test
    void declinesWith65OnceDailyCountIsReached() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(100), card, new DailyActivity(10, 5_000)));

        assertThat(decline).isPresent();
        assertThat(decline.get().code()).isEqualTo(ResponseCode.EXCEEDS_FREQUENCY_LIMIT);
        assertThat(decline.get().reason()).contains("count");
    }

    @Test
    void declinesWith65WhenThisAmountWouldBreachDailyLimit() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(60_000), card, new DailyActivity(1, 60_000)));

        assertThat(decline).isPresent();
        assertThat(decline.get().code()).isEqualTo(ResponseCode.EXCEEDS_FREQUENCY_LIMIT);
        assertThat(decline.get().reason()).contains("120000").contains("100000");
    }

    @Test
    void allowsSpendingExactlyUpToTheDailyLimit() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(40_000), card, new DailyActivity(9, 60_000)));

        assertThat(decline).isEmpty();
    }

    @Test
    void firstTransactionOfTheDayIsFine() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(99_999), card));

        assertThat(decline).isEmpty();
    }
}
