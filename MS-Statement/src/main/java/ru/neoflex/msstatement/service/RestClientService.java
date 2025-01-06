package ru.neoflex.msstatement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.loanissuerlibrary.dto.LoanOfferDto;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;

import java.util.List;

@Slf4j
@Service
public class RestClientService {

    private final RestClient restClient;
    private final String baseUri = "http://localhost:8081";
    private final String statementUri = "/deal/statement";
    private final String offerSelectUri = "/deal/offer/select";

    public RestClientService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<LoanOfferDto> getOffers(LoanStatementRequestDto request)
                                            throws RestClientResponseException {

        log.info("Making a request to {} in order to get the offers...", baseUri + statementUri);
        return restClient.post()
                .uri(baseUri + statementUri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});
    }

    public void selectOffer(LoanOfferDto offer) throws RestClientResponseException {

        log.info("Making a request to {} in order to save the offer client has chosen...", baseUri + offerSelectUri);

        restClient.post()
                .uri(baseUri + offerSelectUri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(offer)
                .retrieve()
                .toBodilessEntity();
    }

}
