package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.Card;
import com.helenaramyan.cardauth.domain.CardStatus;
import com.helenaramyan.cardauth.domain.ResponseCode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Declines with 54 when the card is past its expiry month or has been marked expired
 * by the issuer.
 */
public final class CardExpiryRule implements CardRule {

    private final Clock clock;

    public CardExpiryRule(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public Optional<Decline> evaluate(Card card, AuthorizationContext context) {
        if (card.status() == CardStatus.EXPIRED) {
            return Optional.of(Decline.of(ResponseCode.EXPIRED_CARD, "Card is marked expired"));
        }
        LocalDate today = LocalDate.now(clock);
        // a card printed 06/23 is good until 30 June inclusive, not until 1 June
        if (card.expiry().atEndOfMonth().isBefore(today)) {
            return Optional.of(Decline.of(ResponseCode.EXPIRED_CARD, "Card expired " + card.expiry()));
        }
        return Optional.empty();
    }
}
