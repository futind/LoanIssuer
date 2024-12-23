package ru.neoflex.msstatement.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;

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

    @ExceptionHandler(PrescoringFailedException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<Object> handleCreditDeniedException(PrescoringFailedException e, WebRequest request) {
        log.warn("Credit Denied: {}", e.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_ACCEPTABLE.value());
        body.put("error", "Prescoring failed because: " + e.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);
    }

}