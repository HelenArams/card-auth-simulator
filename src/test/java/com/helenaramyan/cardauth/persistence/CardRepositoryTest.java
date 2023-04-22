package com.helenaramyan.cardauth.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.helenaramyan.cardauth.domain.CardStatus;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cards;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void seedsTheFourDemoCards() {
        assertThat(cards.findAll()).extracting(CardEntity::getToken)
                .containsExactlyInAnyOrder("tok_visa_1111", "tok_mc_4444", "tok_visa_5556", "tok_visa_4242");
    }

    @Test
    void readsExpiryBackAsYearMonth() {
        CardEntity card = cards.findById("tok_visa_4242").orElseThrow();

        assertThat(card.getExpiry()).isEqualTo(YearMonth.of(2021, 6));
        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(card.toDomain().maskedPan()).isEqualTo("**** **** **** 4242");
    }

    @Test
    void holdsReduceAvailableBalanceAndSurviveAFlush() {
        CardEntity card = cards.findById("tok_visa_1111").orElseThrow();
        card.hold(10_000);
        entityManager.flush();
        entityManager.clear();

        CardEntity reloaded = cards.findById("tok_visa_1111").orElseThrow();

        assertThat(reloaded.getBalance()).isEqualTo(250_000);
        assertThat(reloaded.getHeld()).isEqualTo(10_000);
        assertThat(reloaded.available()).isEqualTo(240_000);
        assertThat(reloaded.toDomain().balance()).isEqualTo(240_000);
    }

    @Test
    void refusesToHoldMoreThanAvailable() {
        CardEntity card = cards.findById("tok_mc_4444").orElseThrow();

        assertThatThrownBy(() -> card.hold(1_501)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refusesToReleaseMoreThanHeld() {
        CardEntity card = cards.findById("tok_visa_1111").orElseThrow();
        card.hold(500);

        assertThatThrownBy(() -> card.release(501)).isInstanceOf(IllegalStateException.class);
        card.release(500);
        assertThat(card.getHeld()).isZero();
    }
}
