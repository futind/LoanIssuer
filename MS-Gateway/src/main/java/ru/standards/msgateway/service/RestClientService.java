package ru.standards.msgateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.neoflex.loanissuerlibrary.dto.FinishRegistrationRequestDto;
import ru.neoflex.loanissuerlibrary.dto.LoanOfferDto;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class RestClientService {

    private final RestClient restClient;

    private final String DEAL_URI_BASE;
    private final String STATEMENT_URI_BASE;

    private final String STATEMENT_PORT;
    private final String DEAL_PORT;

    private final String URI_STATEMENT;
    private final String URI_STATEMENT_OFFER;
    private final String URI_DEAL_CALCULATE;
    private final String URI_DEAL_DOCUMENT_BASE;
    private final String URI_DEAL_DOCUMENT_SEND;
    private final String URI_DEAL_DOCUMENT_SIGN;
    private final String URI_DEAL_DOCUMENT_CODE;

    public RestClientService(RestClient restClient,
                             @Value("${uri.base.deal}") String dealUriBase,
                             @Value("${uri.base.statement}") String statementUriBase,
                             @Value("${port.statement}") String statementPort,
                             @Value("${port.deal}") String dealPort,
                             @Value("${uri.statement}") String uriStatement,
                             @Value("${uri.statement.offer}") String uriStatementOffer,
                             @Value("${uri.deal.calculate}") String uriDealCalculate,
                             @Value("${uri.deal.document.base}") String uriDealDocumentBase,
                             @Value("${uri.deal.document.send}") String uriDealDocumentSend,
                             @Value("${uri.deal.document.sign}") String uriDealDocumentSign,
                             @Value("${uri.deal.document.code}") String uriDealDocumentCode) {
        this.restClient = restClient;
        DEAL_URI_BASE = "http://" + dealUriBase + ":";
        DEAL_PORT = dealPort;

        STATEMENT_URI_BASE = "http://" + statementUriBase + ":";
        STATEMENT_PORT = statementPort;

        URI_STATEMENT = uriStatement;
        URI_STATEMENT_OFFER = uriStatementOffer;
        URI_DEAL_CALCULATE = uriDealCalculate;
        URI_DEAL_DOCUMENT_BASE = uriDealDocumentBase;
        URI_DEAL_DOCUMENT_SEND = uriDealDocumentSend;
        URI_DEAL_DOCUMENT_SIGN = uriDealDocumentSign;
        URI_DEAL_DOCUMENT_CODE = uriDealDocumentCode;
    }

    public List<LoanOfferDto> createStatementGetOffers(LoanStatementRequestDto request) {

        log.info("Making a request to {}...", STATEMENT_URI_BASE + STATEMENT_PORT + URI_STATEMENT);
        return restClient.post()
                .uri(STATEMENT_URI_BASE + STATEMENT_PORT + URI_STATEMENT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});

    }

    public void selectStatement(LoanOfferDto offer) {
        log.info("Making a request to {}...", STATEMENT_URI_BASE + STATEMENT_PORT + URI_STATEMENT_OFFER);

        restClient.post()
                .uri(STATEMENT_URI_BASE + STATEMENT_PORT + URI_STATEMENT_OFFER)
                .contentType(MediaType.APPLICATION_JSON)
                .body(offer)
                .retrieve()
                .toBodilessEntity();
    }

    public void finishRegistration(UUID statementId, FinishRegistrationRequestDto request) {
        log.info("Making a request to {}...", DEAL_URI_BASE + DEAL_PORT + URI_DEAL_CALCULATE + "{statementId}");

        restClient.post()
                .uri(DEAL_URI_BASE + DEAL_PORT + URI_DEAL_CALCULATE + "/" + statementId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void sendDocuments(UUID statementId) {
        log.info("Making a request to {}...", DEAL_URI_BASE + DEAL_PORT + URI_DEAL_DOCUMENT_BASE
                                                       + "{statementId}" + URI_DEAL_DOCUMENT_SEND);

        restClient.post()
                .uri(DEAL_URI_BASE + DEAL_PORT + URI_DEAL_DOCUMENT_BASE
                        + "/" + statementId.toString() + URI_DEAL_DOCUMENT_SEND)
                .retrieve()
                .toBodilessEntity();
    }

    public void signDocuments(UUID statementId) {
        log.info("Making a request to {}...", DEAL_URI_BASE + DEAL_PORT + URI_DEAL_DOCUMENT_BASE
                                                       + "{statementId}" + URI_DEAL_DOCUMENT_SIGN);

        restClient.post()
                .uri(DEAL_URI_BASE + DEAL_PORT + URI_DEAL_DOCUMENT_BASE
                        + "/" + statementId.toString() + URI_DEAL_DOCUMENT_SIGN)
                .retrieve()
                .toBodilessEntity();
    }

    public void verifyCode(UUID statementId, String code) {
        log.info("Making a request to {}...", DEAL_URI_BASE + DEAL_PORT + URI_DEAL_DOCUMENT_BASE
                                                       + "{statementId}" + URI_DEAL_DOCUMENT_CODE);

        String uri = UriComponentsBuilder
                .fromUriString(DEAL_URI_BASE + DEAL_PORT)
                .path(URI_DEAL_DOCUMENT_BASE)
                .path("/" + statementId.toString())
                .path(URI_DEAL_DOCUMENT_CODE)
                .queryParam("code", code)
                .build()
                .toUriString();

        restClient.post()
                .uri(uri)
                .retrieve()
                .toBodilessEntity();
    }
}
