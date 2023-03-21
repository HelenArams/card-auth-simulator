package com.helenaramyan.cardauth.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.helenaramyan.cardauth.Fixtures;
import com.helenaramyan.cardauth.domain.ResponseCode;
import org.junit.jupiter.api.Test;

class CardPresentRuleTest {

    private final CardPresentRule rule = new CardPresentRule();

    @Test
    void declinesUnknownTokenWith14() {
        var decline = rule.evaluate(AuthorizationContext.unknownCard(Fixtures.request(1_000)));

        assertThat(decline).isPresent();
        assertThat(decline.get().code()).isEqualTo(ResponseCode.INVALID_CARD);
        assertThat(decline.get().reason()).contains(Fixtures.TOKEN);
    }

    @Test
    void staysSilentWhenCardIsKnown() {
        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.activeCard()));

        assertThat(decline).isEmpty();
    }
}
