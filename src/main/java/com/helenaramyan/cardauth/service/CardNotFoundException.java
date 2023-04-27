package com.helenaramyan.cardauth.service;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String token) {
        super("No card for token " + token);
    }
}
