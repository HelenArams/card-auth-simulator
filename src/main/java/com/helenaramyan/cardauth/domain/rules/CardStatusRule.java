package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.Card;
import com.helenaramyan.cardauth.domain.CardStatus;
import com.helenaramyan.cardauth.domain.ResponseCode;
import java.util.Optional;

/**
 * Declines with 62 when the issuer has blocked the card.
 */
public final class CardStatusRule implements CardRule {

    @Override
    public Optional<Decline> evaluate(Card card, AuthorizationContext context) {
        if (card.status() == CardStatus.BLOCKED) {
            return Optional.of(Decline.of(ResponseCode.RESTRICTED_CARD, "Card is blocked"));
        }
        return Optional.empty();
    }
}
