package com.helenaramyan.cardauth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.helenaramyan.cardauth.domain.Authorization;
import com.helenaramyan.cardauth.domain.AuthorizationRequest;
import com.helenaramyan.cardauth.domain.AuthorizationStatus;
import com.helenaramyan.cardauth.domain.Merchant;
import com.helenaramyan.cardauth.domain.ResponseCode;
import com.helenaramyan.cardauth.persistence.CardRepository;
import com.helenaramyan.cardauth.support.FixedClockConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(FixedClockConfiguration.class)
@Transactional
class AuthorizationServiceTest {

    private static final Merchant GROCERY = new Merchant("M-1001", "5411", "NL");

    @Autowired
    private AuthorizationService service;

    @Autowired
    private CardRepository cards;

    @Test
    void approvesAndPlacesAHoldOnTheCard() {
        Authorization authorization = service.authorize(request("tok_visa_1111", 1_250));

        assertThat(authorization.approved()).isTrue();
        assertThat(authorization.result().responseCode()).isEqualTo(ResponseCode.APPROVED);
        assertThat(authorization.result().authCode()).matches("[A-Z2-9]{6}");
        assertThat(authorization.createdAt()).isEqualTo(FixedClockConfiguration.NOW);
        assertThat(cards.findById("tok_visa_1111").orElseThrow().available()).isEqualTo(250_000 - 1_250);
    }

    @Test
    void declinesUnknownTokenWith14AndStillRecordsTheAttempt() {
        Authorization authorization = service.authorize(request("tok_nobody", 1_000));

        assertThat(authorization.approved()).isFalse();
        assertThat(authorization.result().responseCode()).isEqualTo(ResponseCode.INVALID_CARD);
        assertThat(service.find(authorization.id())).isEqualTo(authorization);
    }

    @Test
    void declinedAuthorizationsDoNotTouchTheBalance() {
        service.authorize(request("tok_mc_4444", 5_000));

        assertThat(cards.findById("tok_mc_4444").orElseThrow().available()).isEqualTo(1_500);
    }

    @Test
    void stopsAfterTenApprovalsInADay() {
        for (int i = 0; i < 10; i++) {
            assertThat(service.authorize(request("tok_visa_1111", 100)).approved()).isTrue();
        }

        Authorization eleventh = service.authorize(request("tok_visa_1111", 100));

        assertThat(eleventh.result().responseCode()).isEqualTo(ResponseCode.EXCEEDS_FREQUENCY_LIMIT);
    }

    @Test
    void stopsWhenTodaysSpendWouldExceedTheDailyLimit() {
        assertThat(service.authorize(request("tok_visa_1111", 60_000)).approved()).isTrue();

        Authorization second = service.authorize(request("tok_visa_1111", 60_000));

        assertThat(second.result().responseCode()).isEqualTo(ResponseCode.EXCEEDS_FREQUENCY_LIMIT);
        assertThat(cards.findById("tok_visa_1111").orElseThrow().available()).isEqualTo(250_000 - 60_000);
    }

    @Test
    void declinedAttemptsDoNotCountTowardsVelocity() {
        for (int i = 0; i < 10; i++) {
            service.authorize(request("tok_visa_1111", 80_000));
        }

        Authorization clean = service.authorize(request("tok_visa_1111", 1_000));

        assertThat(clean.approved()).isTrue();
    }

    @Test
    void reversalReleasesTheHold() {
        Authorization authorization = service.authorize(request("tok_visa_1111", 20_000));
        assertThat(cards.findById("tok_visa_1111").orElseThrow().available()).isEqualTo(230_000);

        Authorization reversed = service.reverse(authorization.id());

        assertThat(reversed.status()).isEqualTo(AuthorizationStatus.REVERSED);
        assertThat(reversed.reversedAt()).isEqualTo(FixedClockConfiguration.NOW);
        assertThat(reversed.result().authCode()).isEqualTo(authorization.result().authCode());
        assertThat(cards.findById("tok_visa_1111").orElseThrow().available()).isEqualTo(250_000);
    }

    @Test
    void cannotReverseTwiceOrReverseADecline() {
        Authorization approved = service.authorize(request("tok_visa_1111", 100));
        Authorization declined = service.authorize(request("tok_mc_4444", 5_000));
        service.reverse(approved.id());

        assertThatThrownBy(() -> service.reverse(approved.id()))
                .isInstanceOf(AuthorizationNotReversibleException.class)
                .hasMessageContaining("already reversed");
        assertThatThrownBy(() -> service.reverse(declined.id()))
                .isInstanceOf(AuthorizationNotReversibleException.class)
                .hasMessageContaining("declined");
        assertThat(cards.findById("tok_visa_1111").orElseThrow().available()).isEqualTo(250_000);
    }

    @Test
    void rejectsRequestsInAnotherCurrencyThanTheCard() {
        var request = new AuthorizationRequest("tok_visa_1111", 1_000, "USD", GROCERY);

        assertThatThrownBy(() -> service.authorize(request))
                .isInstanceOf(CurrencyMismatchException.class)
                .hasMessageContaining("EUR")
                .hasMessageContaining("USD");
    }

    @Test
    void findingAnUnknownAuthorizationFails() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> service.find(id))
                .isInstanceOf(AuthorizationNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    private static AuthorizationRequest request(String token, long amount) {
        return new AuthorizationRequest(token, amount, "EUR", GROCERY);
    }
}
