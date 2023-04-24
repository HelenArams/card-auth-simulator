package com.helenaramyan.cardauth.config;

import com.helenaramyan.cardauth.domain.rules.AuthCodeGenerator;
import com.helenaramyan.cardauth.domain.rules.RandomAuthCodeGenerator;
import com.helenaramyan.cardauth.domain.rules.RulePipeline;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AuthorizationConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public AuthCodeGenerator authCodeGenerator() {
        return new RandomAuthCodeGenerator();
    }

    @Bean
    public RulePipeline rulePipeline(Clock clock, RulesProperties properties, AuthCodeGenerator authCodes) {
        return RulePipeline.standard(clock, properties.toSettings(), authCodes);
    }
}
