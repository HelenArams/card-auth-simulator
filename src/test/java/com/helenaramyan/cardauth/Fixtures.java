package com.helenaramyan.cardauth;

import com.helenaramyan.cardauth.domain.AuthorizationRequest;
import com.helenaramyan.cardauth.domain.Card;
import com.helenaramyan.cardauth.domain.CardStatus;
import com.helenaramyan.cardauth.domain.Merchant;
import java.time.YearMonth;

/**
 * Hand-rolled test data shared across unit tests.
 */
public final class Fixtures {

    public static final String TOKEN = "tok_test_1111";
    public static final String CURRENCY = "EUR";
    public static final YearMonth FAR_FUTURE = YearMonth.of(2030, 12);
    public static final Merchant GROCERY = new Merchant("M-1001", "5411", "NL");

    private Fixtures() {
    }

    public static Card activeCard() {
        return activeCard(100_000, 50_000);
    }

    public static Card activeCard(long balance, long dailyLimit) {
        return new Card(TOKEN, "1111", FAR_FUTURE, CardStatus.ACTIVE, balance, CURRENCY, dailyLimit);
    }

    public static Card cardWith(CardStatus status) {
        return new Card(TOKEN, "1111", FAR_FUTURE, status, 100_000, CURRENCY, 50_000);
    }

    public static Card cardExpiring(YearMonth expiry) {
        return new Card(TOKEN, "1111", expiry, CardStatus.ACTIVE, 100_000, CURRENCY, 50_000);
    }

    public static AuthorizationRequest request(long amount) {
        return request(amount, GROCERY);
    }

    public static AuthorizationRequest request(long amount, Merchant merchant) {
        return new AuthorizationRequest(TOKEN, amount, CURRENCY, merchant);
    }

    public static Merchant merchantWithMcc(String mcc) {
        return new Merchant("M-" + mcc, mcc, "NL");
    }
}
