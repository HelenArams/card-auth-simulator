package com.helenaramyan.cardauth.web.dto;

import com.helenaramyan.cardauth.domain.AuthorizationRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Schema(name = "AuthorizationRequest")
public record AuthorizationRequestDto(
        @Schema(description = "Card token issued in place of the PAN", example = "tok_visa_1111")
        @NotBlank String cardToken,
        @Schema(description = "Amount in minor units", example = "1250")
        @NotNull @Positive Long amount,
        @Schema(description = "ISO 4217 alpha code", example = "EUR")
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
        @NotNull @Valid MerchantDto merchant) {

    public AuthorizationRequest toDomain() {
        return new AuthorizationRequest(cardToken, amount, currency, merchant.toDomain());
    }
}
