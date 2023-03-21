package com.helenaramyan.cardauth.domain.rules;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Six upper-case alphanumeric characters, which is what most schemes accept in field 38.
 */
public final class RandomAuthCodeGenerator implements AuthCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final Random random;

    public RandomAuthCodeGenerator() {
        this(new SecureRandom());
    }

    RandomAuthCodeGenerator(Random random) {
        this.random = random;
    }

    @Override
    public String next() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
