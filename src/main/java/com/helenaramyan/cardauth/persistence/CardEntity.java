package com.helenaramyan.cardauth.persistence;

import com.helenaramyan.cardauth.domain.Card;
import com.helenaramyan.cardauth.domain.CardStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.YearMonth;

/**
 * Card account as held by the issuer. {@code balance} is the ledger balance and
 * {@code held} the sum of outstanding authorization holds against it.
 */
@Entity
@Table(name = "cards")
public class CardEntity {

    @Id
    @Column(length = 64)
    private String token;

    @Column(nullable = false, length = 4)
    private String last4;

    @Column(nullable = false, length = 7)
    private YearMonth expiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CardStatus status;

    @Column(nullable = false)
    private long balance;

    @Column(nullable = false)
    private long held;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private long dailyLimit;

    @Version
    private long version;

    protected CardEntity() {
    }

    public CardEntity(String token, String last4, YearMonth expiry, CardStatus status,
                      long balance, String currency, long dailyLimit) {
        this.token = token;
        this.last4 = last4;
        this.expiry = expiry;
        this.status = status;
        this.balance = balance;
        this.held = 0;
        this.currency = currency;
        this.dailyLimit = dailyLimit;
    }

    public long available() {
        return balance - held;
    }

    // TODO: no capture/settlement yet, so holds only ever go away through a reversal
    public void hold(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Hold amount must be positive");
        }
        if (amount > available()) {
            throw new IllegalStateException("Hold of " + amount + " exceeds available balance " + available());
        }
        held += amount;
    }

    public void release(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Release amount must be positive");
        }
        if (amount > held) {
            throw new IllegalStateException("Cannot release " + amount + ", only " + held + " is held");
        }
        held -= amount;
    }

    /** Domain view, where balance already has the holds deducted. */
    public Card toDomain() {
        return new Card(token, last4, expiry, status, available(), currency, dailyLimit);
    }

    public String getToken() {
        return token;
    }

    public String getLast4() {
        return last4;
    }

    public YearMonth getExpiry() {
        return expiry;
    }

    public CardStatus getStatus() {
        return status;
    }

    public long getBalance() {
        return balance;
    }

    public long getHeld() {
        return held;
    }

    public String getCurrency() {
        return currency;
    }

    public long getDailyLimit() {
        return dailyLimit;
    }
}
