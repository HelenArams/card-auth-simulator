package com.helenaramyan.cardauth.config;

import com.helenaramyan.cardauth.domain.rules.RuleSettings;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Externalised knobs of the rule pipeline, bound from {@code cardauth.rules.*}.
 */
@ConfigurationProperties(prefix = "cardauth.rules")
public record RulesProperties(
        @DefaultValue({"7995", "6051"}) Set<String> blockedMccs,
        @DefaultValue("75000") long perTransactionLimit,
        @DefaultValue("10") int maxDailyCount) {

    public RuleSettings toSettings() {
        return new RuleSettings(blockedMccs, perTransactionLimit, maxDailyCount);
    }
}
