package com.helenaramyan.cardauth.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.helenaramyan.cardauth.Fixtures;
import com.helenaramyan.cardauth.domain.AuthorizationRequest;
import com.helenaramyan.cardauth.domain.Card;
import com.helenaramyan.cardauth.domain.CardStatus;
import com.helenaramyan.cardauth.domain.ResponseCode;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Pins the order of the standard pipeline: a card that trips every rule at once must
 * be declined with the code of the rule that comes first.
 */
class StandardPipelineOrderTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2023-06-15T10:00:00Z"), ZoneOffset.UTC);

    private final RulePipeline pipeline = RulePipeline.standard(CLOCK, RuleSettings.defaults(), () -> "ABC123");

    private final AuthorizationRequest hopeless = Fixtures.request(80_000, Fixtures.merchantWithMcc("7995"));
    private final DailyActivity exhausted = new DailyActivity(10, 50_000);

    @Test
    void listsRulesInTheDocumentedOrder() {
        assertThat(pipeline.rules()).extracting(rule -> rule.getClass().getSimpleName()).containsExactly(
                "CardPresentRule", "CardStatusRule", "CardExpiryRule", "MccBlocklistRule",
                "AmountLimitRule", "VelocityRule", "BalanceRule");
    }

    @Test
    void unknownCardBeatsEverything() {
        assertThat(evaluate(Optional.empty())).isEqualTo(ResponseCode.INVALID_CARD);
    }

    @Test
    void blockedBeatsExpired() {
        assertThat(evaluate(card(CardStatus.BLOCKED, YearMonth.of(2020, 1), 0))).isEqualTo(ResponseCode.RESTRICTED_CARD);
    }

    @Test
    void expiredBeatsMcc() {
        assertThat(evaluate(card(CardStatus.ACTIVE, YearMonth.of(2020, 1), 0))).isEqualTo(ResponseCode.EXPIRED_CARD);
    }

    @Test
    void mccBeatsAmountLimit() {
        assertThat(evaluate(card(CardStatus.ACTIVE, Fixtures.FAR_FUTURE, 0))).isEqualTo(ResponseCode.TRANSACTION_NOT_PERMITTED);
    }

    @Test
    void amountLimitBeatsVelocity() {
        var request = Fixtures.request(80_000);
        var result = pipeline.evaluate(AuthorizationContext.of(request, card(CardStatus.ACTIVE, Fixtures.FAR_FUTURE, 0), exhausted));
        assertThat(result.responseCode()).isEqualTo(ResponseCode.EXCEEDS_AMOUNT_LIMIT);
    }

    @Test
    void velocityBeatsBalance() {
        var request = Fixtures.request(1_000);
        var result = pipeline.evaluate(AuthorizationContext.of(request, card(CardStatus.ACTIVE, Fixtures.FAR_FUTURE, 0), exhausted));
        assertThat(result.responseCode()).isEqualTo(ResponseCode.EXCEEDS_FREQUENCY_LIMIT);
    }

    @Test
    void balanceIsTheLastWord() {
        var request = Fixtures.request(1_000);
        var result = pipeline.evaluate(AuthorizationContext.of(request, card(CardStatus.ACTIVE, Fixtures.FAR_FUTURE, 0)));
        assertThat(result.responseCode()).isEqualTo(ResponseCode.INSUFFICIENT_FUNDS);
    }

    @Test
    void cleanRequestOnAHealthyCardIsApproved() {
        var request = Fixtures.request(1_000);
        var result = pipeline.evaluate(AuthorizationContext.of(request, card(CardStatus.ACTIVE, Fixtures.FAR_FUTURE, 5_000)));
        assertThat(result.approved()).isTrue();
        assertThat(result.authCode()).isEqualTo("ABC123");
    }

    private ResponseCode evaluate(Optional<Card> card) {
        return pipeline.evaluate(new AuthorizationContext(hopeless, card, exhausted)).responseCode();
    }

    private ResponseCode evaluate(Card card) {
        return evaluate(Optional.of(card));
    }

    private static Card card(CardStatus status, YearMonth expiry, long balance) {
        return new Card(Fixtures.TOKEN, "1111", expiry, status, balance, Fixtures.CURRENCY, 50_000);
    }
}
