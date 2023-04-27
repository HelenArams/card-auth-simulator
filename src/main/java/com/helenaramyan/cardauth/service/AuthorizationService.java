package com.helenaramyan.cardauth.service;

import com.helenaramyan.cardauth.domain.Authorization;
import com.helenaramyan.cardauth.domain.AuthorizationRequest;
import com.helenaramyan.cardauth.domain.AuthorizationResult;
import com.helenaramyan.cardauth.domain.rules.AuthorizationContext;
import com.helenaramyan.cardauth.domain.rules.DailyActivity;
import com.helenaramyan.cardauth.domain.rules.RulePipeline;
import com.helenaramyan.cardauth.persistence.AuthorizationEntity;
import com.helenaramyan.cardauth.persistence.AuthorizationRepository;
import com.helenaramyan.cardauth.persistence.CardEntity;
import com.helenaramyan.cardauth.persistence.CardRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decides authorizations: loads the card under lock, gathers today's activity, runs the
 * rule pipeline and places a hold on approval. Every decision is persisted, declines too.
 */
@Service
public class AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    private final CardRepository cards;
    private final AuthorizationRepository authorizations;
    private final RulePipeline pipeline;
    private final Clock clock;

    public AuthorizationService(CardRepository cards, AuthorizationRepository authorizations,
                                RulePipeline pipeline, Clock clock) {
        this.cards = cards;
        this.authorizations = authorizations;
        this.pipeline = pipeline;
        this.clock = clock;
    }

    @Transactional
    public Authorization authorize(AuthorizationRequest request) {
        Instant now = clock.instant();
        Optional<CardEntity> card = cards.lockByToken(request.cardToken());
        card.ifPresent(c -> requireSameCurrency(c, request));

        DailyActivity activity = card
                .map(c -> authorizations.approvedActivitySince(c.getToken(), startOfDay(now)))
                .orElse(DailyActivity.NONE);
        AuthorizationContext context = new AuthorizationContext(request, card.map(CardEntity::toDomain), activity);

        AuthorizationResult result = pipeline.evaluate(context);
        if (result.approved()) {
            card.orElseThrow().hold(request.amount());
        }

        AuthorizationEntity saved = authorizations.save(new AuthorizationEntity(request, result, now));
        log.info("Authorization {} on {}: {} {} ({})", saved.getId(), request.cardToken(),
                result.approved() ? "approved" : "declined", result.responseCode().code(), result.reason());
        return saved.toDomain();
    }

    @Transactional
    public Authorization reverse(UUID id) {
        AuthorizationEntity authorization = authorizations.findById(id)
                .orElseThrow(() -> new AuthorizationNotFoundException(id));
        if (!authorization.isApproved()) {
            throw new AuthorizationNotReversibleException("Authorization " + id + " was declined, there is nothing to reverse");
        }
        if (authorization.isReversed()) {
            throw new AuthorizationNotReversibleException("Authorization " + id + " is already reversed");
        }
        // lock the card first, same as authorize(), so the two never race on the hold
        CardEntity card = cards.lockByToken(authorization.getCardToken())
                .orElseThrow(() -> new IllegalStateException("Card vanished for authorization " + id));
        card.release(authorization.getAmount());
        authorization.reverse(clock.instant());
        log.info("Authorization {} on {} reversed, released {}", id, card.getToken(), authorization.getAmount());
        return authorization.toDomain();
    }

    @Transactional(readOnly = true)
    public Authorization find(UUID id) {
        return authorizations.findById(id)
                .map(AuthorizationEntity::toDomain)
                .orElseThrow(() -> new AuthorizationNotFoundException(id));
    }

    private static void requireSameCurrency(CardEntity card, AuthorizationRequest request) {
        if (!card.getCurrency().equals(request.currency())) {
            throw new CurrencyMismatchException(request.currency(), card.getCurrency());
        }
    }

    private Instant startOfDay(Instant now) {
        return now.atZone(clock.getZone()).toLocalDate().atStartOfDay(clock.getZone()).toInstant();
    }
}
