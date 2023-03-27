package com.helenaramyan.cardauth.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.helenaramyan.cardauth.Fixtures;
import com.helenaramyan.cardauth.domain.ResponseCode;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class MccBlocklistRuleTest {

    @ParameterizedTest
    @ValueSource(strings = {"7995", "6051"})
    void declinesDefaultBlockedCategoriesWith57(String mcc) {
        var rule = new MccBlocklistRule();

        var decline = rule.evaluate(AuthorizationContext.of(
                Fixtures.request(1_000, Fixtures.merchantWithMcc(mcc)), Fixtures.activeCard()));

        assertThat(decline).isPresent();
        assertThat(decline.get().code()).isEqualTo(ResponseCode.TRANSACTION_NOT_PERMITTED);
        assertThat(decline.get().reason()).contains(mcc);
    }

    @Test
    void allowsEverydayMerchants() {
        var rule = new MccBlocklistRule();

        var decline = rule.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.activeCard()));

        assertThat(decline).isEmpty();
    }

    @Test
    void honoursACustomBlocklist() {
        var rule = new MccBlocklistRule(Set.of("5411"));

        assertThat(rule.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.activeCard()))).isPresent();
        assertThat(rule.evaluate(AuthorizationContext.of(
                Fixtures.request(1_000, Fixtures.merchantWithMcc("7995")), Fixtures.activeCard()))).isEmpty();
    }
}
