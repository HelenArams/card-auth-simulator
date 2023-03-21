package com.helenaramyan.cardauth.domain;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Issuer-side view of a card. The PAN is never kept; the card is addressed by an
 * opaque token and only the last four digits are retained for display.
 *
 * @param balance    available balance in minor units (e.g. cents), holds already deducted
 * @param dailyLimit maximum spend per calendar day in minor units
 */
public record Card(
        String token,
        String last4,
        YearMonth expiry,
        CardStatus status,
        long balance,
        String currency,
        long dailyLimit) {

    public Card {
        Objects.requireNonNull(token, "token");
        Objects.requireNonNull(last4, "last4");
        Objects.requireNonNull(expiry, "expiry");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(currency, "currency");
        if (!last4.matches("\\d{4}")) {
            throw new IllegalArgumentException("last4 must be exactly four digits");
        }
        if (dailyLimit < 0) {
            throw new IllegalArgumentException("dailyLimit must not be negative");
        }
    }

    public String maskedPan() {
        return "**** **** **** " + last4;
    }
}
