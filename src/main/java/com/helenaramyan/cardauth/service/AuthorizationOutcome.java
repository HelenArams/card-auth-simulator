package com.helenaramyan.cardauth.service;

import com.helenaramyan.cardauth.domain.Authorization;

/**
 * @param replayed true when an earlier decision was returned for a repeated Idempotency-Key
 */
public record AuthorizationOutcome(Authorization authorization, boolean replayed) {
}
