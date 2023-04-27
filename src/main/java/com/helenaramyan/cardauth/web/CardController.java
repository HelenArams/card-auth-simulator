package com.helenaramyan.cardauth.web;

import com.helenaramyan.cardauth.service.CardService;
import com.helenaramyan.cardauth.service.CardSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/cards", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Cards", description = "Masked card lookup")
public class CardController {

    private final CardService cards;

    public CardController(CardService cards) {
        this.cards = cards;
    }

    @GetMapping("/{token}")
    @Operation(summary = "Look up a card by token", description = "Returns the masked PAN, status and balances. Handy to check holds after an authorization.")
    @ApiResponse(responseCode = "200", description = "The card")
    @ApiResponse(responseCode = "404", description = "Unknown token",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    public CardSummary get(@PathVariable String token) {
        return cards.find(token);
    }
}
