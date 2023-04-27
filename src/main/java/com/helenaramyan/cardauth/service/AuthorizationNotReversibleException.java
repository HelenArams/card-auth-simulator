package com.helenaramyan.cardauth.service;

public class AuthorizationNotReversibleException extends RuntimeException {

    public AuthorizationNotReversibleException(String message) {
        super(message);
    }
}
