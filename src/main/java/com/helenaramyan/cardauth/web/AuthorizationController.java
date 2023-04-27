package com.helenaramyan.cardauth.web;

import com.helenaramyan.cardauth.domain.Authorization;
import com.helenaramyan.cardauth.service.AuthorizationService;
import com.helenaramyan.cardauth.web.dto.AuthorizationRequestDto;
import com.helenaramyan.cardauth.web.dto.AuthorizationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(path = "/v1/authorizations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authorizations", description = "Issuer-side authorization decisions")
public class AuthorizationController {

    private final AuthorizationService service;

    public AuthorizationController(AuthorizationService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Authorize a card transaction",
            description = "Runs the request through the rule pipeline. Declines are still 201: the decision "
                    + "was taken and recorded, the outcome is in responseCode.")
    @ApiResponse(responseCode = "201", description = "Decision recorded, approved or declined")
    @ApiResponse(responseCode = "400", description = "Malformed or invalid request",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Request currency differs from the card currency",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<AuthorizationResponse> authorize(@Valid @RequestBody AuthorizationRequestDto body) {
        Authorization authorization = service.authorize(body.toDomain());
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(authorization.id()).toUri();
        return ResponseEntity.created(location).body(AuthorizationResponse.from(authorization));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch a previous authorization")
    @ApiResponse(responseCode = "200", description = "The authorization")
    @ApiResponse(responseCode = "404", description = "Unknown authorization id",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    public AuthorizationResponse get(@PathVariable UUID id) {
        return AuthorizationResponse.from(service.find(id));
    }
}
