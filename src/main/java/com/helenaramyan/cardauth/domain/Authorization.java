package com.helenaramyan.cardauth.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A decided authorization as stored by the issuer.
 */
public record Authorization(UUID id, AuthorizationRequest request, AuthorizationResult result, Instant createdAt) {

    public Authorization {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public boolean approved() {
        return result.approved();
    }
}
