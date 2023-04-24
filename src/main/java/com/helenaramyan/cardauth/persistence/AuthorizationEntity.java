package com.helenaramyan.cardauth.persistence;

import com.helenaramyan.cardauth.domain.Authorization;
import com.helenaramyan.cardauth.domain.AuthorizationRequest;
import com.helenaramyan.cardauth.domain.AuthorizationResult;
import com.helenaramyan.cardauth.domain.Merchant;
import com.helenaramyan.cardauth.domain.ResponseCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * One authorization attempt, approved or declined, exactly as it was decided.
 */
@Entity
@Table(name = "authorizations",
        indexes = @Index(name = "idx_authorizations_card_created", columnList = "card_token, created_at"))
public class AuthorizationEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "card_token", nullable = false, length = 64)
    private String cardToken;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 64)
    private String merchantId;

    @Column(nullable = false, length = 4)
    private String mcc;

    @Column(nullable = false, length = 2)
    private String merchantCountry;

    @Column(nullable = false)
    private boolean approved;

    @Column(nullable = false, length = 2)
    private String responseCode;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(length = 6)
    private String authCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AuthorizationEntity() {
    }

    public AuthorizationEntity(AuthorizationRequest request, AuthorizationResult result, Instant createdAt) {
        this.cardToken = request.cardToken();
        this.amount = request.amount();
        this.currency = request.currency();
        this.merchantId = request.merchant().id();
        this.mcc = request.merchant().mcc();
        this.merchantCountry = request.merchant().country();
        this.approved = result.approved();
        this.responseCode = result.responseCode().code();
        this.reason = result.reason();
        this.authCode = result.authCode();
        this.createdAt = createdAt;
    }

    public AuthorizationRequest toRequest() {
        return new AuthorizationRequest(cardToken, amount, currency, new Merchant(merchantId, mcc, merchantCountry));
    }

    public AuthorizationResult toResult() {
        return new AuthorizationResult(approved, ResponseCode.fromCode(responseCode), reason, authCode);
    }

    public Authorization toDomain() {
        return new Authorization(id, toRequest(), toResult(), createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getCardToken() {
        return cardToken;
    }

    public long getAmount() {
        return amount;
    }

    public boolean isApproved() {
        return approved;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
