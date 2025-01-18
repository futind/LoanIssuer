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

    private RestClient restClient;

    private String URI_BASE;

    private String STATEMENT_PORT;
    private String DEAL_PORT;

    private String URI_STATEMENT;
    private String URI_STATEMENT_OFFER;
    private String URI_DEAL_CALCULATE;
    private String URI_DEAL_DOCUMENT_SEND_BASE;
    private String URI_DEAL_DOCUMENT_SEND_END;
    private String URI_DEAL_DOCUMENT_SIGN_BASE;
    private String URI_DEAL_DOCUMENT_SIGN_END;
    private String URI_DEAL_DOCUMENT_CODE_BASE;
    private String URI_DEAL_DOCUMENT_CODE_END;

    public RestClientService(RestClient restClient,
                             @Value("${uri.base}") String uriBase,
                             @Value("${port.statement}") String statementPort,
                             @Value("${port.deal}") String dealPort,
                             @Value("${uri.statement}") String uriStatement,
                             @Value("${uri.statement.offer}") String uriStatementOffer,
                             @Value("${uri.deal.calculate}") String uriDealCalculate,
                             @Value("${uri.deal.document.send.base}") String uriDealDocumentSendBase,
                             @Value("${uri.deal.document.send.end}") String uriDealDocumentSendEnd,
                             @Value("${uri.deal.document.sign.base}") String uriDealDocumentSignBase,
                             @Value("${uri.deal.document.sign.end}") String uriDealDocumentSignEnd,
                             @Value("${uri.deal.document.code.base}") String uriDealDocumentCodeBase,
                             @Value("${uri.deal.document.code.end}") String uriDealDocumentCodeEnd) {
        this.restClient = restClient;
        URI_BASE = uriBase;
        STATEMENT_PORT = statementPort;
        DEAL_PORT = dealPort;
        URI_STATEMENT = uriStatement;
        URI_STATEMENT_OFFER = uriStatementOffer;
        URI_DEAL_CALCULATE = uriDealCalculate;
        URI_DEAL_DOCUMENT_SEND_BASE = uriDealDocumentSendBase;
        URI_DEAL_DOCUMENT_SEND_END = uriDealDocumentSendEnd;
        URI_DEAL_DOCUMENT_SIGN_BASE = uriDealDocumentSignBase;
        URI_DEAL_DOCUMENT_SIGN_END = uriDealDocumentSignEnd;
        URI_DEAL_DOCUMENT_CODE_BASE = uriDealDocumentCodeBase;
        URI_DEAL_DOCUMENT_CODE_END = uriDealDocumentCodeEnd;
    }

    public List<LoanOfferDto> createStatementGetOffers(LoanStatementRequestDto request) {

        log.info("Making a request to {}...", URI_BASE + STATEMENT_PORT + URI_STATEMENT);
        return restClient.post()
                .uri(URI_BASE + STATEMENT_PORT + URI_STATEMENT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});

    }

    public void selectStatement(LoanOfferDto offer) {
        log.info("Making a request to {}...", URI_BASE + STATEMENT_PORT + URI_STATEMENT_OFFER);

        restClient.post()
                .uri(URI_BASE + STATEMENT_PORT + URI_STATEMENT_OFFER)
                .contentType(MediaType.APPLICATION_JSON)
                .body(offer)
                .retrieve()
                .toBodilessEntity();
    }

    public void finishRegistration(UUID statementId, FinishRegistrationRequestDto request) {
        log.info("Making a request to {}...", URI_BASE + URI_DEAL_CALCULATE + "{statementId}");

        restClient.post()
                .uri(URI_BASE + DEAL_PORT + URI_DEAL_CALCULATE + "/" + statementId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void sendDocuments(UUID statementId) {
        log.info("Making a request to {}...", URI_BASE + DEAL_PORT + URI_DEAL_DOCUMENT_SEND_BASE
                                                       + "{statementId}" + URI_DEAL_DOCUMENT_SEND_END);

        restClient.post()
                .uri(URI_BASE + DEAL_PORT + URI_DEAL_DOCUMENT_SEND_BASE
                        + "/" + statementId.toString() + URI_DEAL_DOCUMENT_SEND_END)
                .retrieve()
                .toBodilessEntity();
    }

    public void signDocuments(UUID statementId) {
        log.info("Making a request to {}...", URI_BASE + DEAL_PORT + URI_DEAL_DOCUMENT_SIGN_BASE
                                                       + "{statementId}" + URI_DEAL_DOCUMENT_SIGN_END);

        restClient.post()
                .uri(URI_BASE + DEAL_PORT + URI_DEAL_DOCUMENT_SIGN_BASE
                        + "/" + statementId.toString() + URI_DEAL_DOCUMENT_SIGN_END)
                .retrieve()
                .toBodilessEntity();
    }

    public void verifyCode(UUID statementId, String code) {
        log.info("Making a request to {}...", URI_BASE + DEAL_PORT + URI_DEAL_DOCUMENT_CODE_BASE
                                                       + "{statementId}" + URI_DEAL_DOCUMENT_CODE_END);

        String uri = UriComponentsBuilder
                .fromUriString(URI_BASE + DEAL_PORT)
                .path(URI_DEAL_DOCUMENT_CODE_BASE)
                .path("/" + statementId.toString())
                .path(URI_DEAL_DOCUMENT_CODE_END)
                .queryParam("code", code)
                .build()
                .toUriString();

        restClient.post()
                .uri(uri)
                .retrieve()
                .toBodilessEntity();
    }
}
