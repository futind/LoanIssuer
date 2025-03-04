package ru.standards.msgateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Object> handleRestClientResponseException(RestClientResponseException e,
                                                                    WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        LinkedHashMap<String, Object> cause = e.getResponseBodyAs(LinkedHashMap.class);
        String error = "";
        if (cause != null && cause.containsKey("error")) {
            error = cause.get("error").toString();
        }

        body.put("timestamp", LocalDateTime.now());
        body.put("status", e.getStatusCode().value());
        body.put("error", error);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, e.getStatusCode());
    }
}
