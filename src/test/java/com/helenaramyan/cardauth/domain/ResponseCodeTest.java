package com.helenaramyan.cardauth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ResponseCodeTest {

    @Test
    void resolvesEnumFromIsoCode() {
        assertThat(ResponseCode.fromCode("57")).isEqualTo(ResponseCode.TRANSACTION_NOT_PERMITTED);
        assertThat(ResponseCode.fromCode("00").isApproval()).isTrue();
    }

    @Test
    void failsOnUnknownCode() {
        assertThatThrownBy(() -> ResponseCode.fromCode("99"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}
