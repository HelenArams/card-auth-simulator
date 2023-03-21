package com.helenaramyan.cardauth.domain;

import java.util.Objects;

/**
 * Outcome of running an authorization request through the rules.
 *
 * @param authCode six-character approval code, present only when approved
 */
public record AuthorizationResult(boolean approved, ResponseCode responseCode, String reason, String authCode) {

    public AuthorizationResult {
        Objects.requireNonNull(responseCode, "responseCode");
        Objects.requireNonNull(reason, "reason");
        if (approved != responseCode.isApproval()) {
            throw new IllegalArgumentException("approved flag does not match response code " + responseCode.code());
        }
        if (approved && (authCode == null || authCode.isBlank())) {
            throw new IllegalArgumentException("approved results must carry an auth code");
        }
        if (!approved && authCode != null) {
            throw new IllegalArgumentException("declined results must not carry an auth code");
        }
    }

    public static AuthorizationResult approved(String authCode) {
        return new AuthorizationResult(true, ResponseCode.APPROVED, ResponseCode.APPROVED.description(), authCode);
    }

    public static AuthorizationResult declined(ResponseCode responseCode, String reason) {
        return new AuthorizationResult(false, responseCode, reason, null);
    }

    public static AuthorizationResult declined(ResponseCode responseCode) {
        return declined(responseCode, responseCode.description());
    }
}
