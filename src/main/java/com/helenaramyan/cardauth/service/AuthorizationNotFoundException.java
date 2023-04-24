package com.helenaramyan.cardauth.service;

import java.util.UUID;

public class AuthorizationNotFoundException extends RuntimeException {

    private final UUID id;

    public AuthorizationNotFoundException(UUID id) {
        super("Authorization " + id + " not found");
        this.id = id;
    }

    public UUID id() {
        return id;
    }
}
