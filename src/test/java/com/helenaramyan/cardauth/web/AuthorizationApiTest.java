package com.helenaramyan.cardauth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.helenaramyan.cardauth.support.FixedClockConfiguration;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(FixedClockConfiguration.class)
@Transactional
class AuthorizationApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void approvesACleanPurchase() throws Exception {
        mvc.perform(post("/v1/authorizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("tok_visa_1111", 1_250, "EUR", "5411")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/v1/authorizations/[0-9a-f-]{36}$")))
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.reason").value("Approved"))
                .andExpect(jsonPath("$.authCode", matchesPattern("[A-Z2-9]{6}")))
                .andExpect(jsonPath("$.amount").value(1_250))
                .andExpect(jsonPath("$.merchant.mcc").value("5411"))
                .andExpect(jsonPath("$.createdAt").value("2023-06-15T10:00:00Z"));
    }

    static Stream<Arguments> declines() {
        return Stream.of(
                Arguments.of("unknown token", "tok_nobody", 1_000, "5411", "14"),
                Arguments.of("blocked card", "tok_visa_5556", 1_000, "5411", "62"),
                Arguments.of("expired card", "tok_visa_4242", 1_000, "5411", "54"),
                Arguments.of("gambling merchant", "tok_visa_1111", 1_000, "7995", "57"),
                Arguments.of("over per-transaction limit", "tok_visa_1111", 80_000, "5411", "61"),
                Arguments.of("insufficient funds", "tok_mc_4444", 5_000, "5411", "51"));
    }

    @ParameterizedTest(name = "{0} is declined with {4}")
    @MethodSource("declines")
    void declinesWithTheExpectedIsoCode(String scenario, String token, long amount, String mcc, String code) throws Exception {
        mvc.perform(post("/v1/authorizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(token, amount, "EUR", mcc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.responseCode").value(code))
                .andExpect(jsonPath("$.authCode").doesNotExist());
    }

    @Test
    void declinesWith65AfterTheDailyLimitIsSpent() throws Exception {
        mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("tok_visa_1111", 60_000, "EUR", "5411")))
                .andExpect(jsonPath("$.responseCode").value("00"));

        mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("tok_visa_1111", 60_000, "EUR", "5411")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseCode").value("65"));
    }

    @Test
    void returnsAStoredAuthorizationById() throws Exception {
        String created = mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("tok_visa_1111", 999, "EUR", "5812")))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");

        mvc.perform(get("/v1/authorizations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.amount").value(999))
                .andExpect(jsonPath("$.merchant.mcc").value("5812"));
    }

    @Test
    void unknownAuthorizationIsAProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(get("/v1/authorizations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/problems/authorization-not-found"))
                .andExpect(jsonPath("$.title").value("Authorization not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Authorization " + id + " not found"))
                .andExpect(jsonPath("$.instance").value("/v1/authorizations/" + id));
    }

    @Test
    void malformedIdIsABadRequestProblem() throws Exception {
        mvc.perform(get("/v1/authorizations/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void invalidBodyListsTheOffendingFields() throws Exception {
        String invalid = """
                {"cardToken": "", "amount": -5, "currency": "eur", "merchant": {"id": "M-1", "mcc": "12", "country": "NLD"}}
                """;

        mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/problems/validation-failed"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors", hasSize(5)))
                .andExpect(jsonPath("$.errors[?(@.field == 'amount')].message").value("must be greater than 0"));
    }

    @Test
    void unreadableJsonIsABadRequestProblem() throws Exception {
        mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void foreignCurrencyIsUnprocessable() throws Exception {
        mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("tok_visa_1111", 1_000, "USD", "5411")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/problems/currency-mismatch"))
                .andExpect(jsonPath("$.detail").value("Card is denominated in EUR but the request is in USD"));
    }

    @Test
    void reversalReleasesTheHoldAndIsVisibleOnTheCard() throws Exception {
        String created = mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("tok_visa_1111", 20_000, "EUR", "5411")))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");

        mvc.perform(get("/v1/cards/tok_visa_1111"))
                .andExpect(jsonPath("$.held").value(20_000))
                .andExpect(jsonPath("$.available").value(230_000));

        mvc.perform(post("/v1/authorizations/{id}/reverse", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVERSED"))
                .andExpect(jsonPath("$.reversedAt").value("2023-06-15T10:00:00Z"));

        mvc.perform(get("/v1/cards/tok_visa_1111"))
                .andExpect(jsonPath("$.held").value(0))
                .andExpect(jsonPath("$.available").value(250_000));

        mvc.perform(post("/v1/authorizations/{id}/reverse", id))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/problems/authorization-not-reversible"));
    }

    @Test
    void reversingADeclineIsAConflict() throws Exception {
        String created = mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("tok_mc_4444", 5_000, "EUR", "5411")))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");

        mvc.perform(post("/v1/authorizations/{id}/reverse", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Authorization " + id + " was declined, there is nothing to reverse"));
    }

    @Test
    void reversingAnUnknownAuthorizationIsNotFound() throws Exception {
        mvc.perform(post("/v1/authorizations/{id}/reverse", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void cardLookupNeverShowsMoreThanTheLastFourDigits() throws Exception {
        mvc.perform(get("/v1/cards/tok_visa_5556"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedPan").value("**** **** **** 5556"))
                .andExpect(jsonPath("$.expiry").value("2029-12"))
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").value(50_000))
                .andExpect(jsonPath("$.dailyLimit").value(100_000))
                .andExpect(jsonPath("$.last4").doesNotExist());
    }

    @Test
    void unknownCardIsAProblemDetail() throws Exception {
        mvc.perform(get("/v1/cards/tok_nobody"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/problems/card-not-found"))
                .andExpect(jsonPath("$.detail").value("No card for token tok_nobody"));
    }

    @Test
    void repeatedIdempotencyKeyReplaysTheFirstDecision() throws Exception {
        String first = mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "retry-42")
                        .content(body("tok_visa_1111", 5_000, "EUR", "5411")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Idempotent-Replayed", "false"))
                .andReturn().getResponse().getContentAsString();

        String second = mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "retry-42")
                        .content(body("tok_visa_1111", 5_000, "EUR", "5411")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Idempotent-Replayed", "true"))
                .andReturn().getResponse().getContentAsString();

        assertThat(second).isEqualTo(first);
        // only one hold, not two
        mvc.perform(get("/v1/cards/tok_visa_1111"))
                .andExpect(jsonPath("$.held").value(5_000));
    }

    @Test
    void sameKeyWithADifferentBodyIsAConflict() throws Exception {
        mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "retry-43")
                        .content(body("tok_visa_1111", 5_000, "EUR", "5411")))
                .andExpect(status().isCreated());

        mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "retry-43")
                        .content(body("tok_visa_1111", 6_000, "EUR", "5411")))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/problems/idempotency-key-reused"));
    }

    @Test
    void withoutAKeyEveryCallIsANewDecision() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/v1/authorizations").contentType(MediaType.APPLICATION_JSON)
                            .content(body("tok_visa_1111", 5_000, "EUR", "5411")))
                    .andExpect(status().isCreated());
        }

        mvc.perform(get("/v1/cards/tok_visa_1111"))
                .andExpect(jsonPath("$.held").value(10_000));
    }

    @Test
    void publishesAnOpenApiDocument() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Card Authorization Simulator"))
                .andExpect(jsonPath("$.paths['/v1/authorizations'].post").exists())
                .andExpect(jsonPath("$.paths['/v1/authorizations/{id}'].get").exists());
    }

    static String body(String token, long amount, String currency, String mcc) {
        return """
                {
                  "cardToken": "%s",
                  "amount": %d,
                  "currency": "%s",
                  "merchant": {"id": "M-%s", "mcc": "%s", "country": "NL"}
                }
                """.formatted(token, amount, currency, mcc, mcc);
    }
}
