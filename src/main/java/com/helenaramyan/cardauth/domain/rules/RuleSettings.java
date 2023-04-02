package com.helenaramyan.cardauth.domain.rules;

import java.util.Objects;
import java.util.Set;

/**
 * Tunable knobs of the standard pipeline. Amounts are in minor units.
 */
public record RuleSettings(Set<String> blockedMccs, long perTransactionLimit, int maxDailyCount) {

    public static final long DEFAULT_PER_TRANSACTION_LIMIT = 75_000;

    public RuleSettings {
        blockedMccs = Set.copyOf(Objects.requireNonNull(blockedMccs, "blockedMccs"));
        if (perTransactionLimit <= 0) {
            throw new IllegalArgumentException("perTransactionLimit must be positive");
        }
        if (maxDailyCount <= 0) {
            throw new IllegalArgumentException("maxDailyCount must be positive");
        }
    }

    public static RuleSettings defaults() {
        return new RuleSettings(MccBlocklistRule.DEFAULT_BLOCKED_MCCS,
                DEFAULT_PER_TRANSACTION_LIMIT, VelocityRule.DEFAULT_MAX_DAILY_COUNT);
    }
}
