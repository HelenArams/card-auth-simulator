package com.helenaramyan.cardauth.domain.rules;

/**
 * What the card has already done today: number of approved authorizations and
 * their total amount in minor units.
 */
public record DailyActivity(long count, long amount) {

    public static final DailyActivity NONE = new DailyActivity(0, 0);

    public DailyActivity {
        if (count < 0 || amount < 0) {
            throw new IllegalArgumentException("Daily activity cannot be negative");
        }
    }
}
