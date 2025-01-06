package ru.neoflex.msdeal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.loanissuerlibrary.dto.*;
import ru.neoflex.loanissuerlibrary.dto.enumeration.ApplicationStatus;
import ru.neoflex.loanissuerlibrary.exception.CreditDeniedException;
import ru.neoflex.loanissuerlibrary.exception.SesCodeVerificationFailed;
import ru.neoflex.loanissuerlibrary.exception.StatementChangeBlocked;
import ru.neoflex.loanissuerlibrary.exception.StatementNotFoundException;
import ru.neoflex.msdeal.model.ClientEntity;
import ru.neoflex.msdeal.model.CreditEntity;
import ru.neoflex.msdeal.model.StatementEntity;
import ru.neoflex.msdeal.service.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "ms_deal")
@Slf4j
@RestController
@RequestMapping("/deal")
public class DealController implements DealApi {

    private final ClientService clientService;
    private final StatementService statementService;
    private final CreditService creditService;
    private final RestClientService restClientService;
    private final KafkaSenderService kafkaSenderService;
    private final UtilitiesService utilitiesService;

    private final String CALCULATOR_SERVICE_PORT = "8080";
    private final String URI_BASE = "http://localhost:";



    public DealController(ClientService clientService,
                          StatementService statementService,
                          CreditService creditService,
                          RestClientService restClientService,
                          KafkaSenderService kafkaSenderService,
                          UtilitiesService utilitiesService) {
        this.clientService = clientService;
        this.statementService = statementService;
        this.creditService = creditService;
        this.restClientService = restClientService;
        this.kafkaSenderService = kafkaSenderService;
        this.utilitiesService = utilitiesService;
    }

    @PostMapping("/statement")
    public List<LoanOfferDto> getOffers(@RequestBody @Valid LoanStatementRequestDto request) {

        log.info("Received a valid request to /deal/statement; amount: {}, term: {}",
                request.getAmount(),
                request.getTerm());

        ClientEntity clientEntity = clientService.createClientWithRequest(request);

        StatementEntity statementEntity = statementService.createStatementWithClient(clientEntity);

        List<LoanOfferDto> offers = restClientService.getLoanOffers(request);

        offers.stream().forEach(offer -> offer.setStatementId(statementEntity.getStatementId()));

        log.info("Returning loan offers to the client...");

        return offers;
    }


    @PostMapping("/offer/select")
    public void select(@RequestBody @Valid LoanOfferDto offer) {
        log.info("Received a valid request to /deal/offer/select");
        statementService.setAppliedOffer(offer);
        kafkaSenderService.sendFinishRegistrationMessage(offer.getStatementId(),
                statementService.findClientByStatementId(offer.getStatementId()).getEmail());
    }


    @PostMapping("calculate/{statementId}")
    public void finishRegistration(@RequestBody @Valid FinishRegistrationRequestDto request,
                                   @PathVariable("statementId") String statementId)
            throws CreditDeniedException,
            RestClientResponseException,
            StatementNotFoundException, StatementChangeBlocked {
        UUID statementUUID = UUID.fromString(statementId);

        if (statementService.isDenied(statementUUID)) {
            log.warn("Statement had been denied earlier. All the changes are blocked.");
            throw new StatementChangeBlocked("Statement had been denied earlier!");
        }

        StatementEntity statementEntity;

        try {
            statementEntity = statementService.findById(statementUUID);
        } catch (EntityNotFoundException e) {
            log.error("Statement with id {} not found", statementUUID);
            throw new StatementNotFoundException("Could not find statement: " + statementId);
        }

        log.info("Found statement with provided UUID");
        UUID clientUUID = statementEntity.getClient().getClientId();

        clientService.enrichClient(request, clientUUID);
        ScoringDataDto scoringDataDto = statementService.enrichScoringData(request, statementUUID);
        CreditDto creditDto;

        try {
            creditDto = restClientService.getCredit(scoringDataDto);
        } catch (CreditDeniedException e) {
            statementService.changeStatementStatus(statementEntity, ApplicationStatus.CC_DENIED);
            kafkaSenderService.sendStatementDeniedMessage(statementUUID, statementEntity.getClient().getEmail());
            throw e;
        }

        CreditEntity creditEntity = creditService.saveCredit(creditDto);

        statementEntity = statementService.setCredit(statementUUID, creditEntity);
        statementService.changeStatementStatus(statementEntity, ApplicationStatus.CC_APPROVED);

        kafkaSenderService.sendCreateDocumentsMessage(statementUUID, statementEntity.getClient().getEmail());
    }

    @PostMapping("document/{statementId}/send")
    public void sendDocuments(@PathVariable("statementId") String statementId) throws EntityNotFoundException,
                                                                                      StatementChangeBlocked {
        if (statementService.isDenied(UUID.fromString(statementId))) {
            log.warn("Statement had been denied earlier. All the changes are blocked.");
            throw new StatementChangeBlocked("Statement had been denied earlier!");
        }

        log.info("Received a valid request to /deal/document/{statementId}/send");
        statementService.changeStatementStatus(statementService.findById(UUID.fromString(statementId)),
                                               ApplicationStatus.PREPARE_DOCUMENTS);
        kafkaSenderService.sendSendDocumentsMessage(UUID.fromString(statementId),
                                    statementService.findById(UUID.fromString(statementId)).getClient().getEmail());
    }

    @PostMapping("document/{statementId}/sign")
    public void signDocuments(@PathVariable("statementId") String statementId) throws EntityNotFoundException, StatementChangeBlocked {
        if (statementService.isDenied(UUID.fromString(statementId))) {
            log.warn("Statement had been denied earlier. All the changes are blocked.");
            throw new StatementChangeBlocked("Statement had been denied earlier!");
        }

        log.info("Received a POST request to /deal/document/{statementId}/sign");
        statementService.updateSesCode(UUID.fromString(statementId),
                         utilitiesService.generateSesCode());
        log.info("Updated ses code");
        kafkaSenderService.sendSendSesMessage(UUID.fromString(statementId),
                                              statementService.getSesByStatementId(UUID.fromString(statementId)),
                                              statementService.findById(UUID.fromString(statementId)).getClient().getEmail());
    }

    @PostMapping("document/{statementId}/code")
    public void signingCode(@RequestParam String SesCode,
                            @PathVariable("statementId") String statementId) throws EntityNotFoundException,
                                                                                    StatementChangeBlocked,
                                                                                    SesCodeVerificationFailed {
        if (statementService.isDenied(UUID.fromString(statementId))) {
            log.warn("Statement had been denied earlier. All the changes are blocked.");
            throw new StatementChangeBlocked("Statement had been denied earlier!");
        }

        log.info("Received a POST request to /deal/document/{statementId}/code");

        StatementEntity statementEntity = statementService.findById(UUID.fromString(statementId));
        if (!statementEntity.getSesCode().equals(SesCode)) {
            throw new SesCodeVerificationFailed("SES codes do not match!");
        }

        creditService.updateCreditStatus(statementEntity.getCredit().getCreditId());
        statementService.changeStatementStatus(statementEntity, ApplicationStatus.DOCUMENT_SIGNED);
        statementService.issueCredit(UUID.fromString(statementId));

        kafkaSenderService.sendCreditIssuedMessage(UUID.fromString(statementId), statementEntity.getClient().getEmail());
    }

    @PutMapping("admin/statement/{statementId}/status")
    public void documentsCreatedStatusChange(String statementId) throws EntityNotFoundException, StatementChangeBlocked {
        if (statementService.isDenied(UUID.fromString(statementId))) {
            log.warn("Statement had been denied earlier. All the changes are blocked.");
            throw new StatementChangeBlocked("Statement had been denied earlier!");
        }

        log.info("Received a PUT request to /deal/admin/statement/{statementId}/status");
        statementService.changeStatementStatus(statementService.findById(UUID.fromString(statementId)),
                                               ApplicationStatus.DOCUMENT_CREATED);
    }

    @GetMapping("document/{statementId}/data")
    public DocumentDataDto getDocumentData(@PathVariable("statementId") String statementId)
            throws EntityNotFoundException, StatementChangeBlocked {
        if (statementService.isDenied(UUID.fromString(statementId))) {
            log.warn("Statement had been denied earlier. All the changes are blocked.");
            throw new StatementChangeBlocked("Statement had been denied earlier!");
        }

        log.info("Received a GET request to /deal/document/{statementId}/data");
        return statementService.enrichDocumentData(UUID.fromString(statementId));
    }
}
