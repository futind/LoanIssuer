package ru.neoflex.loanissuerlibrary.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CreditDeniedException extends Exception {

  public CreditDeniedException(String message) {
    super(message);
  }
}
