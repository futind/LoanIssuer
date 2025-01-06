package ru.neoflex.msdeal.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import ru.neoflex.loanissuerlibrary.exception.CreditDeniedException;
import ru.neoflex.loanissuerlibrary.exception.SesCodeVerificationFailed;
import ru.neoflex.loanissuerlibrary.exception.StatementChangeBlocked;
import ru.neoflex.loanissuerlibrary.exception.StatementNotFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Object> handleCreditDeniedException(RestClientResponseException e, WebRequest request) {
        log.warn("RestClientResponseException {}", e.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", e.getStatusCode().value());
        body.put("error", e.getResponseBodyAsString());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, e.getStatusCode());
    }

    @ExceptionHandler(CreditDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleCreditDeniedException(CreditDeniedException e, WebRequest request) {
        log.warn("Credit Denied: {}", e.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Credit denial cause: " + e.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleStatementNotFoundException(EntityNotFoundException e, WebRequest request) {
        log.warn("StatementNotFoundException {}", e.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Entity not found: " + e.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SesCodeVerificationFailed.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleStatementNotFoundException(SesCodeVerificationFailed e, WebRequest request) {
        log.warn("SesCodeVerificationFailed {}", e.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "SES code verification failed: " + e.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(StatementChangeBlocked.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleStatementNotFoundException(StatementChangeBlocked e, WebRequest request) {
        log.warn("StatementChangeBlocked {}", e.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Further changes to the statement are blocked: " + e.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

}