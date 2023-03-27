package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.ResponseCode;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Declines with 57 when the merchant category is on the issuer's blocklist.
 * The default list covers gambling (7995) and quasi-cash / crypto (6051).
 */
public final class MccBlocklistRule implements AuthorizationRule {

    public static final Set<String> DEFAULT_BLOCKED_MCCS = Set.of("7995", "6051");

    private final Set<String> blockedMccs;

    public MccBlocklistRule() {
        this(DEFAULT_BLOCKED_MCCS);
    }

    public MccBlocklistRule(Set<String> blockedMccs) {
        this.blockedMccs = Set.copyOf(Objects.requireNonNull(blockedMccs, "blockedMccs"));
    }

    @Override
    public Optional<Decline> evaluate(AuthorizationContext context) {
        String mcc = context.request().merchant().mcc();
        if (blockedMccs.contains(mcc)) {
            return Optional.of(Decline.of(ResponseCode.TRANSACTION_NOT_PERMITTED, "MCC " + mcc + " is not permitted"));
        }
        return Optional.empty();
    }
}
