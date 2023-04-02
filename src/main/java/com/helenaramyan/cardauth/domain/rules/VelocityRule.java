package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.Card;
import com.helenaramyan.cardauth.domain.ResponseCode;
import java.util.Optional;

/**
 * Declines with 65 when the card has hit the daily number of approvals or when this
 * amount on top of today's approved spend would exceed the card's daily limit.
 */
public final class VelocityRule implements CardRule {

    public static final int DEFAULT_MAX_DAILY_COUNT = 10;

    private final int maxDailyCount;

    public VelocityRule() {
        this(DEFAULT_MAX_DAILY_COUNT);
    }

    public VelocityRule(int maxDailyCount) {
        if (maxDailyCount <= 0) {
            throw new IllegalArgumentException("maxDailyCount must be positive");
        }
        this.maxDailyCount = maxDailyCount;
    }

    @Override
    public Optional<Decline> evaluate(Card card, AuthorizationContext context) {
        DailyActivity today = context.activity();
        if (today.count() >= maxDailyCount) {
            return Optional.of(Decline.of(ResponseCode.EXCEEDS_FREQUENCY_LIMIT,
                    "Daily authorization count " + today.count() + " reached the limit of " + maxDailyCount));
        }
        long projected = today.amount() + context.request().amount();
        if (projected > card.dailyLimit()) {
            return Optional.of(Decline.of(ResponseCode.EXCEEDS_FREQUENCY_LIMIT,
                    "Daily spend " + projected + " would exceed limit " + card.dailyLimit()));
        }
        return Optional.empty();
    }
}
