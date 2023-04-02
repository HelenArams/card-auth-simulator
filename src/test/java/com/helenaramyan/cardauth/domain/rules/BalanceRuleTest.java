package com.helenaramyan.cardauth.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.helenaramyan.cardauth.Fixtures;
import com.helenaramyan.cardauth.domain.ResponseCode;
import org.junit.jupiter.api.Test;

class BalanceRuleTest {

    private final BalanceRule rule = new BalanceRule();

    @Test
    void declinesWith51WhenAmountExceedsAvailableBalance() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(5_000), Fixtures.activeCard(1_500, 100_000)));

        assertThat(decline).isPresent();
        assertThat(decline.get().code()).isEqualTo(ResponseCode.INSUFFICIENT_FUNDS);
        assertThat(decline.get().reason()).contains("1500").contains("5000");
    }

    @Test
    void allowsSpendingTheWholeBalance() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(1_500), Fixtures.activeCard(1_500, 100_000)));

        assertThat(decline).isEmpty();
    }
}
