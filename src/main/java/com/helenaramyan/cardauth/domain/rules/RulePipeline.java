package com.helenaramyan.cardauth.domain.rules;

import com.helenaramyan.cardauth.domain.AuthorizationResult;
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
