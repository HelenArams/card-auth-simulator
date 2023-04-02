package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.Card;
import com.helenaramyan.cardauth.domain.ResponseCode;
import java.util.Optional;

/**
 * Declines with 51 when the available balance does not cover the amount. Runs last so
 * that the more specific declines above it are reported first.
 */
public final class BalanceRule implements CardRule {

    @Override
    public Optional<Decline> evaluate(Card card, AuthorizationContext context) {
        long amount = context.request().amount();
        if (amount > card.balance()) {
            return Optional.of(Decline.of(ResponseCode.INSUFFICIENT_FUNDS,
                    "Available balance " + card.balance() + " does not cover " + amount));
        }
        return Optional.empty();
    }
}
