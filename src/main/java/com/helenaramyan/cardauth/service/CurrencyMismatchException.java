package com.helenaramyan.cardauth.service;

/**
 * The simulator has no FX; a request must be denominated in the card's currency.
 */
public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(String requested, String cardCurrency) {
        super("Card is denominated in " + cardCurrency + " but the request is in " + requested);
    }
}
