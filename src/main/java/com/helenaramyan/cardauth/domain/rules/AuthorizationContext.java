package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.AuthorizationRequest;
import com.helenaramyan.cardauth.domain.Card;
import java.util.Objects;
import java.util.Optional;

/**
 * Everything the rules need to decide on a request. The card is optional because
 * an unknown token is itself a decline reason rather than an error.
 */
public record AuthorizationContext(AuthorizationRequest request, Optional<Card> card, DailyActivity activity) {

    public AuthorizationContext {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(activity, "activity");
    }

    public AuthorizationContext(AuthorizationRequest request, Optional<Card> card) {
        this(request, card, DailyActivity.NONE);
    }

    public static AuthorizationContext of(AuthorizationRequest request, Card card) {
        return new AuthorizationContext(request, Optional.of(card));
    }

    public static AuthorizationContext of(AuthorizationRequest request, Card card, DailyActivity activity) {
        return new AuthorizationContext(request, Optional.of(card), activity);
    }

    public static AuthorizationContext unknownCard(AuthorizationRequest request) {
        return new AuthorizationContext(request, Optional.empty());
    }
}
