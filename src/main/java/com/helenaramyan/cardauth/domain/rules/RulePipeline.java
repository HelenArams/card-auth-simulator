package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.AuthorizationResult;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Runs the rules in order and stops at the first decline. When no rule objects the
 * request is approved and an auth code is issued.
 */
public final class RulePipeline {

    private final List<AuthorizationRule> rules;
    private final AuthCodeGenerator authCodes;

    public RulePipeline(List<AuthorizationRule> rules, AuthCodeGenerator authCodes) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
        this.authCodes = Objects.requireNonNull(authCodes, "authCodes");
    }

    /**
     * The issuer's standard order: identity and status of the card first, then what the
     * merchant is, then how much, and only at the very end whether the money is there.
     */
    public static RulePipeline standard(Clock clock, RuleSettings settings, AuthCodeGenerator authCodes) {
        return new RulePipeline(List.of(
                new CardPresentRule(),
                new CardStatusRule(),
                new CardExpiryRule(clock),
                new MccBlocklistRule(settings.blockedMccs()),
                new AmountLimitRule(settings.perTransactionLimit()),
                new VelocityRule(settings.maxDailyCount()),
                new BalanceRule()), authCodes);
    }

    public AuthorizationResult evaluate(AuthorizationContext context) {
        for (AuthorizationRule rule : rules) {
            Optional<Decline> decline = rule.evaluate(context);
            if (decline.isPresent()) {
                return AuthorizationResult.declined(decline.get().code(), decline.get().reason());
            }
        }
        return AuthorizationResult.approved(authCodes.next());
    }

    public List<AuthorizationRule> rules() {
        return rules;
    }
}
