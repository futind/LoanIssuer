package ru.neoflex.msdeal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.loanissuerlibrary.dto.CreditDto;
import ru.neoflex.loanissuerlibrary.dto.LoanOfferDto;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;
import ru.neoflex.loanissuerlibrary.dto.ScoringDataDto;
import ru.neoflex.loanissuerlibrary.exception.CreditDeniedException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RestClientService {

    private final RestClient restClient;
    private final String baseUri = "http://localhost:8080";
    private final String offersUri = "/calculator/offers";
    private final String creditUri = "/calculator/calc";

    public RestClientService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<LoanOfferDto> getLoanOffers(LoanStatementRequestDto request)
                                                            throws RestClientResponseException {
        log.info("Making a request to {}...", baseUri + offersUri);
        return restClient.post()
                .uri(baseUri + offersUri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});
    }

    public CreditDto getCredit(ScoringDataDto scoringData) throws RestClientResponseException {
        log.info("Making a request to {}...", baseUri + creditUri);
        CreditDto creditDto;

        return restClient.post()
                    .uri(baseUri + creditUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(scoringData)
                    .retrieve()
                    .body(CreditDto.class);
    }
}
