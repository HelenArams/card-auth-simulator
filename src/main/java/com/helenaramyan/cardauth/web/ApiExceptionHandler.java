package com.helenaramyan.cardauth.web;

import com.helenaramyan.cardauth.service.AuthorizationNotFoundException;
import com.helenaramyan.cardauth.service.AuthorizationNotReversibleException;
import com.helenaramyan.cardauth.service.CardNotFoundException;
import com.helenaramyan.cardauth.service.CurrencyMismatchException;
import com.helenaramyan.cardauth.service.IdempotencyKeyReusedException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns every error into an RFC 7807 problem document. Framework errors (unreadable
 * JSON, type mismatches, validation) come from the base class; ours are mapped below.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    record FieldViolation(String field, String message) {
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ex.getBody();
        problem.setType(type("validation-failed"));
        problem.setTitle("Validation failed");
        problem.setDetail("Request has " + ex.getErrorCount() + " invalid field(s)");
        problem.setProperty("errors", ex.getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList());
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @ExceptionHandler(AuthorizationNotFoundException.class)
    ProblemDetail authorizationNotFound(AuthorizationNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "authorization-not-found", "Authorization not found", ex.getMessage());
    }

    @ExceptionHandler(CardNotFoundException.class)
    ProblemDetail cardNotFound(CardNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "card-not-found", "Card not found", ex.getMessage());
    }

    @ExceptionHandler(AuthorizationNotReversibleException.class)
    ProblemDetail notReversible(AuthorizationNotReversibleException ex) {
        return problem(HttpStatus.CONFLICT, "authorization-not-reversible", "Authorization cannot be reversed", ex.getMessage());
    }

    @ExceptionHandler(IdempotencyKeyReusedException.class)
    ProblemDetail idempotencyKeyReused(IdempotencyKeyReusedException ex) {
        return problem(HttpStatus.CONFLICT, "idempotency-key-reused", "Idempotency key reused", ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail constraintViolation(ConstraintViolationException ex) {
        return problem(HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed", ex.getMessage());
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    ProblemDetail currencyMismatch(CurrencyMismatchException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "currency-mismatch", "Currency mismatch", ex.getMessage());
    }

    static ProblemDetail problem(HttpStatus status, String slug, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(type(slug));
        problem.setTitle(title);
        return problem;
    }

    private static URI type(String slug) {
        return URI.create("/problems/" + slug);
    }
}
