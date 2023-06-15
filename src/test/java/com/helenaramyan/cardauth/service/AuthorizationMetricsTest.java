package com.helenaramyan.cardauth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.helenaramyan.cardauth.domain.AuthorizationRequest;
import com.helenaramyan.cardauth.domain.Merchant;
import com.helenaramyan.cardauth.support.FixedClockConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(FixedClockConfiguration.class)
@Transactional
class AuthorizationMetricsTest {

    private static final Merchant GROCERY = new Merchant("M-1001", "5411", "NL");

    @Autowired
    private AuthorizationService service;

    @Autowired
    private MeterRegistry meters;

    @Test
    void countsDecisionsByOutcomeAndCodeButNotReplays() {
        double approvedBefore = count("approved", "00");
        double blockedBefore = count("declined", "62");

        service.authorize(new AuthorizationRequest("tok_visa_1111", 100, "EUR", GROCERY), "metrics-1");
        service.authorize(new AuthorizationRequest("tok_visa_1111", 100, "EUR", GROCERY), "metrics-1");
        service.authorize(new AuthorizationRequest("tok_visa_5556", 100, "EUR", GROCERY));

        assertThat(count("approved", "00") - approvedBefore).isEqualTo(1.0);
        assertThat(count("declined", "62") - blockedBefore).isEqualTo(1.0);
    }

    private double count(String outcome, String code) {
        var counter = meters.find("authorizations").tag("outcome", outcome).tag("code", code).counter();
        return counter == null ? 0 : counter.count();
    }
}
