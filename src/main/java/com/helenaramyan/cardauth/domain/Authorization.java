package com.helenaramyan.cardauth.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A decided authorization as stored by the issuer. {@code reversedAt} is null until the
 * acquirer reverses it (or we time it out, which this simulator does not do yet).
 */
public record Authorization(UUID id, AuthorizationRequest request, AuthorizationResult result,
                            Instant createdAt, Instant reversedAt) {

    public Authorization {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(createdAt, "createdAt");
        if (reversedAt != null && !result.approved()) {
            throw new IllegalArgumentException("Only approved authorizations can be reversed");
        }
    }

    public boolean approved() {
        return result.approved();
    }

    public AuthorizationStatus status() {
        if (reversedAt != null) {
            return AuthorizationStatus.REVERSED;
        }
        return approved() ? AuthorizationStatus.APPROVED : AuthorizationStatus.DECLINED;
    }
}
