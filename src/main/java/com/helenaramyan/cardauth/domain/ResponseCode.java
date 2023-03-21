package com.helenaramyan.cardauth.domain;

import java.util.Arrays;

/**
 * Subset of ISO 8583 field 39 response codes used by the simulator.
 */
public enum ResponseCode {
    APPROVED("00", "Approved"),
    INVALID_CARD("14", "Card not found"),
    INSUFFICIENT_FUNDS("51", "Insufficient funds"),
    EXPIRED_CARD("54", "Expired card"),
    TRANSACTION_NOT_PERMITTED("57", "Transaction not permitted to cardholder"),
    EXCEEDS_AMOUNT_LIMIT("61", "Exceeds amount limit"),
    RESTRICTED_CARD("62", "Restricted card"),
    EXCEEDS_FREQUENCY_LIMIT("65", "Exceeds daily activity limit");

    private final String code;
    private final String description;

    ResponseCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    public boolean isApproval() {
        return this == APPROVED;
    }

    public static ResponseCode fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown response code: " + code));
    }
}
