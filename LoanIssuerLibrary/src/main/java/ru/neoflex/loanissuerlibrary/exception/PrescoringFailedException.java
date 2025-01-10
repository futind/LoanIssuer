package ru.neoflex.loanissuerlibrary.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class PrescoringFailedException extends Exception {
    public PrescoringFailedException(String message) {
        super(message);
    }
}
