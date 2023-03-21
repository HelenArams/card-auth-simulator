package com.helenaramyan.cardauth.domain.rules;

import java.util.Optional;

/**
 * One step of the authorization pipeline. A rule either declines or stays silent;
 * approval is what is left when every rule stays silent.
 */
@FunctionalInterface
public interface AuthorizationRule {

    Optional<Decline> evaluate(AuthorizationContext context);
}
