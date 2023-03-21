package com.helenaramyan.cardauth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class CardTest {

    @Test
    void masksEverythingButLastFourDigits() {
        Card card = new Card("tok_1", "1111", YearMonth.of(2029, 12), CardStatus.ACTIVE, 10_000, "EUR", 50_000);

        assertThat(card.maskedPan()).isEqualTo("**** **** **** 1111");
    }

    @Test
    void rejectsLast4ThatIsNotFourDigits() {
        assertThatThrownBy(() -> new Card("tok_1", "11", YearMonth.of(2029, 12), CardStatus.ACTIVE, 0, "EUR", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("last4");
    }

    @Test
    void rejectsNegativeDailyLimit() {
        assertThatThrownBy(() -> new Card("tok_1", "1111", YearMonth.of(2029, 12), CardStatus.ACTIVE, 0, "EUR", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dailyLimit");
    }
}
