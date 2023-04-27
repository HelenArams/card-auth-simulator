package com.helenaramyan.cardauth.web.dto;

import com.helenaramyan.cardauth.domain.Merchant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "Merchant")
public record MerchantDto(
        @Schema(example = "M-1001") @NotBlank String id,
        @Schema(description = "Merchant category code", example = "5411") @NotBlank @Pattern(regexp = "\\d{4}") String mcc,
        @Schema(description = "ISO 3166-1 alpha-2", example = "NL") @NotBlank @Pattern(regexp = "[A-Z]{2}") String country) {

    public static MerchantDto from(Merchant merchant) {
        return new MerchantDto(merchant.id(), merchant.mcc(), merchant.country());
    }

    public Merchant toDomain() {
        return new Merchant(id, mcc, country);
    }
}
