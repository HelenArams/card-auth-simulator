package com.helenaramyan.cardauth.domain;

import java.util.Objects;

/**
 * Acceptor details as they arrive in the authorization message.
 *
 * @param mcc     four-digit merchant category code
 * @param country ISO 3166-1 alpha-2 country code
 */
public record Merchant(String id, String mcc, String country) {

    public Merchant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(mcc, "mcc");
        Objects.requireNonNull(country, "country");
        if (!mcc.matches("\\d{4}")) {
            throw new IllegalArgumentException("mcc must be exactly four digits");
        }
    }
}
