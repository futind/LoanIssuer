package ru.neoflex.msdeal.service;

import com.fasterxml.classmate.GenericType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.msdeal.dto.CreditDto;
import ru.neoflex.msdeal.dto.LoanOfferDto;
import ru.neoflex.msdeal.dto.LoanStatementRequestDto;
import ru.neoflex.msdeal.dto.ScoringDataDto;
import ru.neoflex.msdeal.exception.CreditDeniedException;

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

    public CreditDto getCredit(ScoringDataDto scoringData) throws CreditDeniedException,
                                                                  RestClientResponseException {
        log.info("Making a request to {}...", baseUri + creditUri);
        CreditDto creditDto;

        try {
             creditDto = restClient.post()
                    .uri(baseUri + creditUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(scoringData)
                    .retrieve()
                    .body(CreditDto.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new CreditDeniedException(e.getMessage());
            } else {
                throw e;
            }
        }

        return creditDto;
    }

}
