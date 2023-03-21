package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.ResponseCode;
import java.util.Optional;

/**
 * Declines with 14 when the token does not resolve to a card.
 */
public final class CardPresentRule implements AuthorizationRule {

    @Override
    public Optional<Decline> evaluate(AuthorizationContext context) {
        if (context.card().isPresent()) {
            return Optional.empty();
        }
        return Optional.of(Decline.of(ResponseCode.INVALID_CARD,
                "No card found for token " + context.request().cardToken()));
    }
}
