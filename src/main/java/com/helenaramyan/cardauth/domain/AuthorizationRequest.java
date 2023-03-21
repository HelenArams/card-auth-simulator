package com.helenaramyan.cardauth.domain;

import java.util.Objects;

/**
 * An authorization attempt for a single purchase.
 *
 * @param amount   transaction amount in minor units, strictly positive
 * @param currency ISO 4217 alpha code of the amount
 */
public record AuthorizationRequest(String cardToken, long amount, String currency, Merchant merchant) {

    public AuthorizationRequest {
        Objects.requireNonNull(cardToken, "cardToken");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(merchant, "merchant");
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
