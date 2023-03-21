package com.helenaramyan.cardauth.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.helenaramyan.cardauth.Fixtures;
import com.helenaramyan.cardauth.domain.CardStatus;
import com.helenaramyan.cardauth.domain.ResponseCode;
import org.junit.jupiter.api.Test;

class CardStatusRuleTest {

    private final CardStatusRule rule = new CardStatusRule();

    @Test
    void declinesBlockedCardWith62() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.cardWith(CardStatus.BLOCKED)));

        assertThat(decline).isPresent();
        assertThat(decline.get().code()).isEqualTo(ResponseCode.RESTRICTED_CARD);
    }

    @Test
    void allowsActiveCard() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.activeCard()));

        assertThat(decline).isEmpty();
    }

    @Test
    void ignoresUnknownCardAndLeavesItToTheLookupRule() {
        var decline = rule.evaluate(AuthorizationContext.unknownCard(Fixtures.request(1_000)));

        assertThat(decline).isEmpty();
    }
}
