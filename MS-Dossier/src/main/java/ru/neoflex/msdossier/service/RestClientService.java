package ru.neoflex.msdossier.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.loanissuerlibrary.dto.DocumentDataDto;

import java.util.UUID;

@Slf4j
@Service
public class RestClientService {

    private final RestClient restClient;
    private final String baseUri = "http://localhost:8081";
    private final String adminUri = "/deal/admin/statement";
    private final String documentUri = "/deal/document";

    public RestClientService(RestClient restClient) {
        this.restClient = restClient;
    }

    public void putDocumentsCreatedStatus(UUID statementId) throws RestClientResponseException {
        log.info("Making a PUT request to {}...", baseUri + adminUri + "/statementId/status");

        restClient.put()
                .uri(baseUri + adminUri + "/" + statementId.toString() + "/status")
                .retrieve()
                .toBodilessEntity();
    }

    public DocumentDataDto getDocumentData(UUID statementId) throws RestClientResponseException {
        log.info("Making a GET request to {}...", baseUri + documentUri + "/statementId/data");

        return restClient.get()
                .uri(baseUri + documentUri + "/" + statementId.toString() + "/data")
                .retrieve()
                .body(DocumentDataDto.class);
    }

}
