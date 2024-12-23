package ru.neoflex.msstatement.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.msstatement.dto.LoanStatementRequestDto;
import ru.neoflex.msstatement.exception.PrescoringFailedException;

@Slf4j
@Service
public class PrescoringService {

    private final String emailRegexRussian = "^[a-z0-9A-Z_!#$%&'*+/=?`{|}~^.-]+@[a-z0-9A-Z.-]+\\.ru$";

    public void prescore(LoanStatementRequestDto request) throws PrescoringFailedException {

        if (!request.getEmail().matches(emailRegexRussian)) {
            log.warn("Failed prescoring: client was denied, because his email is not in allowed domain.");
            throw new PrescoringFailedException("Email address must bin .ru domain");
        }

    }
}
