package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.ResponseCode;
import java.util.Objects;

/**
 * A rule's verdict to decline, with the ISO 8583 code and a human readable reason.
 */
public record Decline(ResponseCode code, String reason) {

    public Decline {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(reason, "reason");
        if (code.isApproval()) {
            throw new IllegalArgumentException("A decline cannot carry the approval code");
        }
    }

    public static Decline of(ResponseCode code) {
        return new Decline(code, code.description());
    }

    public static Decline of(ResponseCode code, String reason) {
        return new Decline(code, reason);
    }
}
