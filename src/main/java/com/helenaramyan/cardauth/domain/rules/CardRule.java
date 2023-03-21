package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.Card;
import java.util.Optional;

/**
 * A rule that only makes sense once the card has been resolved. Unknown cards are
 * left to {@link CardPresentRule}, so implementations never see an absent card.
 */
@FunctionalInterface
public interface CardRule extends AuthorizationRule {

    @Override
    default Optional<Decline> evaluate(AuthorizationContext context) {
        return context.card().flatMap(card -> evaluate(card, context));
    }

    Optional<Decline> evaluate(Card card, AuthorizationContext context);
}
