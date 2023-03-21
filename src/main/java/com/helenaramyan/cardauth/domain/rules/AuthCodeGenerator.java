package com.helenaramyan.cardauth.domain.rules;

/**
 * Produces the approval code returned to the acquirer on a successful authorization.
 */
@FunctionalInterface
public interface AuthCodeGenerator {

    int LENGTH = 6;

    String next();
}
