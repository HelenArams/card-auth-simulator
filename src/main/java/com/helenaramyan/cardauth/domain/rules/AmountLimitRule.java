package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.ResponseCode;
import java.util.Optional;

/**
 * Declines with 61 when a single transaction exceeds the issuer-wide per-transaction cap.
 */
public final class AmountLimitRule implements AuthorizationRule {

    private final long perTransactionLimit;

    public AmountLimitRule(long perTransactionLimit) {
        if (perTransactionLimit <= 0) {
            throw new IllegalArgumentException("perTransactionLimit must be positive");
        }
        this.perTransactionLimit = perTransactionLimit;
    }

    @Override
    public Optional<Decline> evaluate(AuthorizationContext context) {
        long amount = context.request().amount();
        if (amount > perTransactionLimit) {
            return Optional.of(Decline.of(ResponseCode.EXCEEDS_AMOUNT_LIMIT,
                    "Amount " + amount + " exceeds per-transaction limit " + perTransactionLimit));
        }
        return Optional.empty();
    }
}
