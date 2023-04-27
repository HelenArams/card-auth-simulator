package com.helenaramyan.cardauth.service;

import com.helenaramyan.cardauth.domain.CardStatus;
import com.helenaramyan.cardauth.persistence.CardEntity;
import java.time.YearMonth;

/**
 * What we are happy to show about a card: no PAN, just the masked tail and the money side.
 */
public record CardSummary(String token, String maskedPan, YearMonth expiry, CardStatus status, String currency,
                          long balance, long held, long available, long dailyLimit) {

    static CardSummary of(CardEntity card) {
        return new CardSummary(card.getToken(), card.toDomain().maskedPan(), card.getExpiry(), card.getStatus(),
                card.getCurrency(), card.getBalance(), card.getHeld(), card.available(), card.getDailyLimit());
    }
}
