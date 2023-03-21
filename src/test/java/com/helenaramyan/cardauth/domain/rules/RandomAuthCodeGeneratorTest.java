package com.helenaramyan.cardauth.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RandomAuthCodeGeneratorTest {

    @Test
    void producesSixUpperCaseAlphanumericCharacters() {
        var generator = new RandomAuthCodeGenerator(new Random(42));

        for (int i = 0; i < 100; i++) {
            assertThat(generator.next()).matches("[A-Z2-9]{6}");
        }
    }

    @Test
    void doesNotRepeatItselfTooEagerly() {
        var generator = new RandomAuthCodeGenerator(new Random(7));
        Set<String> seen = new HashSet<>();

        for (int i = 0; i < 1_000; i++) {
            seen.add(generator.next());
        }

        assertThat(seen).hasSizeGreaterThan(990);
    }
}
