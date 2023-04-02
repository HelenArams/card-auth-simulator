package com.helenaramyan.cardauth.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.helenaramyan.cardauth.Fixtures;
import com.helenaramyan.cardauth.domain.ResponseCode;
import org.junit.jupiter.api.Test;

class AmountLimitRuleTest {

    private final AmountLimitRule rule = new AmountLimitRule(75_000);

    @Test
    void declinesAmountAboveLimitWith61() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(75_001), Fixtures.activeCard()));

        assertThat(decline).isPresent();
        assertThat(decline.get().code()).isEqualTo(ResponseCode.EXCEEDS_AMOUNT_LIMIT);
        assertThat(decline.get().reason()).contains("75001").contains("75000");
    }

    @Test
    void allowsAmountExactlyAtLimit() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(75_000), Fixtures.activeCard()));

        assertThat(decline).isEmpty();
    }

    @Test
    void rejectsNonPositiveLimit() {
        assertThatThrownBy(() -> new AmountLimitRule(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
