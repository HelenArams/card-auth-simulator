package com.helenaramyan.cardauth.service;

public class IdempotencyKeyReusedException extends RuntimeException {

    public IdempotencyKeyReusedException(String key) {
        super("Idempotency-Key " + key + " was already used for a different request");
    }
}
