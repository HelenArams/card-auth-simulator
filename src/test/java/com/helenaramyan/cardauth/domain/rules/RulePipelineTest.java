package com.helenaramyan.cardauth.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.helenaramyan.cardauth.Fixtures;
import com.helenaramyan.cardauth.domain.AuthorizationResult;
import com.helenaramyan.cardauth.domain.ResponseCode;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RulePipelineTest {

    private static final AuthCodeGenerator FIXED_CODE = () -> "ABC123";

    @Test
    void approvesWithAuthCodeWhenNoRuleDeclines() {
        var pipeline = new RulePipeline(List.of(context -> Optional.empty()), FIXED_CODE);

        AuthorizationResult result = pipeline.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.activeCard()));

        assertThat(result.approved()).isTrue();
        assertThat(result.responseCode()).isEqualTo(ResponseCode.APPROVED);
        assertThat(result.authCode()).isEqualTo("ABC123");
    }

    @Test
    void firstDeclineWinsAndLaterRulesAreNotConsulted() {
        AtomicInteger laterRuleCalls = new AtomicInteger();
        AuthorizationRule declining = context -> Optional.of(Decline.of(ResponseCode.RESTRICTED_CARD, "blocked"));
        AuthorizationRule later = context -> {
            laterRuleCalls.incrementAndGet();
            return Optional.of(Decline.of(ResponseCode.EXPIRED_CARD));
        };
        var pipeline = new RulePipeline(List.of(declining, later), FIXED_CODE);

        AuthorizationResult result = pipeline.evaluate(AuthorizationContext.of(Fixtures.request(1_000), Fixtures.activeCard()));

        assertThat(result.approved()).isFalse();
        assertThat(result.responseCode()).isEqualTo(ResponseCode.RESTRICTED_CARD);
        assertThat(result.reason()).isEqualTo("blocked");
        assertThat(result.authCode()).isNull();
        assertThat(laterRuleCalls).hasValue(0);
    }

    @Test
    void unknownCardIsDeclinedBeforeAnyCardRuleRuns() {
        var pipeline = new RulePipeline(List.of(new CardPresentRule(), new CardStatusRule()), FIXED_CODE);

        AuthorizationResult result = pipeline.evaluate(AuthorizationContext.unknownCard(Fixtures.request(1_000)));

        assertThat(result.responseCode()).isEqualTo(ResponseCode.INVALID_CARD);
    }
}
