package com.helenaramyan.cardauth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AuthorizationResultTest {

    @Test
    void approvedCarriesCodeZeroZeroAndAuthCode() {
        AuthorizationResult result = AuthorizationResult.approved("A1B2C3");

        assertThat(result.approved()).isTrue();
        assertThat(result.responseCode().code()).isEqualTo("00");
        assertThat(result.authCode()).isEqualTo("A1B2C3");
    }

    @Test
    void declinedDefaultsReasonToCodeDescription() {
        AuthorizationResult result = AuthorizationResult.declined(ResponseCode.INSUFFICIENT_FUNDS);

        assertThat(result.approved()).isFalse();
        assertThat(result.responseCode().code()).isEqualTo("51");
        assertThat(result.reason()).isEqualTo("Insufficient funds");
        assertThat(result.authCode()).isNull();
    }

    @Test
    void approvalWithoutAuthCodeIsRejected() {
        assertThatThrownBy(() -> AuthorizationResult.approved(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void approvedFlagMustMatchResponseCode() {
        assertThatThrownBy(() -> new AuthorizationResult(true, ResponseCode.EXPIRED_CARD, "x", "ABC123"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
