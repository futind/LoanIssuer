package ru.neoflex.msstatement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;
import ru.neoflex.loanissuerlibrary.exception.PrescoringFailedException;

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
