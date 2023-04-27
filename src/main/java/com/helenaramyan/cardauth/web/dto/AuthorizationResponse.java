package com.helenaramyan.cardauth.web.dto;

import com.helenaramyan.cardauth.domain.Authorization;
import com.helenaramyan.cardauth.domain.AuthorizationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "Authorization")
public record AuthorizationResponse(
        UUID id,
        String cardToken,
        long amount,
        String currency,
        MerchantDto merchant,
        AuthorizationStatus status,
        boolean approved,
        @Schema(description = "ISO 8583 field 39 response code", example = "00") String responseCode,
        String reason,
        @Schema(description = "Six-character approval code, approved authorizations only", example = "K7Q2MX") String authCode,
        Instant createdAt,
        Instant reversedAt) {

    public static AuthorizationResponse from(Authorization authorization) {
        var request = authorization.request();
        var result = authorization.result();
        return new AuthorizationResponse(
                authorization.id(),
                request.cardToken(),
                request.amount(),
                request.currency(),
                MerchantDto.from(request.merchant()),
                authorization.status(),
                result.approved(),
                result.responseCode().code(),
                result.reason(),
                result.authCode(),
                authorization.createdAt(),
                authorization.reversedAt());
    }
}
