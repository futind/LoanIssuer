package ru.neoflex.msdeal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
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

import javax.swing.plaf.nimbus.State;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DealService {

    private ClientService clientService;
    private StatementService statementService;
    private CreditService creditService;
    private RestClientService restClientService;
    private KafkaSenderService kafkaSenderService;
    private UtilitiesService utilitiesService;

    public DealService(ClientService clientService,
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

    public List<LoanOfferDto> createStatementGetOffers(LoanStatementRequestDto request)
                                                    throws RestClientResponseException {
        ClientEntity clientEntity = clientService.createClientWithRequest(request);
        log.info("Created a client entity and saved it into the database.");
        StatementEntity statementEntity = statementService.createStatementWithClient(clientEntity);
        log.info("Created a statement entity and saved it into the database.");

        List<LoanOfferDto> offers = restClientService.getLoanOffers(request);
        offers.stream().forEach(offer -> offer.setStatementId(statementEntity.getStatementId()));
        log.info("Created offers for the client.");

        return offers;
    }

    public void applyOffer(LoanOfferDto offer) throws StatementNotFoundException {
        String clientEmail = statementService.findClientByStatementId(offer.getStatementId()).getEmail();

        statementService.setAppliedOffer(offer);
        log.info("Set applied offer for the client.");

        log.info("Sending a Kafka event in order to send an email to the client to finish registration.");
        kafkaSenderService.sendFinishRegistrationMessage(offer.getStatementId(), clientEmail);
    }

    public void registrationCalculation(FinishRegistrationRequestDto request, UUID statementUUID)
                                                                    throws StatementChangeBlocked,
                                                                           StatementNotFoundException,
                                                                           CreditDeniedException,
                                                                           RestClientResponseException {
        throwIfStatementIsDenied(statementUUID);

        StatementEntity statementEntity = statementService.findById(statementUUID);

        UUID clientUUID = statementEntity.getClient().getClientId();

        clientService.enrichClient(request, clientUUID);
        ScoringDataDto scoringDataDto = statementService.enrichScoringData(request, statementUUID);

        CreditDto creditDto;

        try {
            creditDto = restClientService.getCredit(scoringDataDto);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().equals(HttpStatusCode.valueOf(403))) {
                log.warn("Client's application was denied. Setting CC_DENIED status to the statement.");
                statementService.changeStatementStatus(statementEntity, ApplicationStatus.CC_DENIED);
                log.info("Sending a request to send an email to the client about his loan application denial.");
                kafkaSenderService.sendStatementDeniedMessage(statementUUID, statementEntity.getClient().getEmail());
            }
            throw e;
        }

        CreditEntity creditEntity = creditService.saveCredit(creditDto);
        statementEntity = statementService.setCredit(statementUUID, creditEntity);
        statementService.changeStatementStatus(statementEntity, ApplicationStatus.CC_APPROVED);

        log.info("Credit was calculated and saved to the statement, CC_APPROVED status was assigned.");
        log.info("Sending a request to send an email to the client in order to create credit documents.");
        kafkaSenderService.sendCreateDocumentsMessage(statementUUID, statementEntity.getClient().getEmail());
    }

    public void sendDocumentEventAndStatus(UUID statementUUID) throws StatementChangeBlocked, StatementNotFoundException {
        throwIfStatementIsDenied(statementUUID);

        String clientEmail = statementService.findClientByStatementId(statementUUID).getEmail();
        statementService.changeStatementStatus(statementService.findById(statementUUID),
                                               ApplicationStatus.PREPARE_DOCUMENTS);
        log.info("Changed the status of the statement to PREPARE_DOCUMENTS");


        log.info("Sending a request to send an email to the client with the details of a loan.");
        kafkaSenderService.sendSendDocumentsMessage(statementUUID, clientEmail);
    }

    public void sesUpdateEvent(UUID statementUUID) throws StatementChangeBlocked, StatementNotFoundException {
        throwIfStatementIsDenied(statementUUID);

        String clientEmail = statementService.findClientByStatementId(statementUUID).getEmail();
        statementService.updateSesCode(statementUUID, utilitiesService.generateSesCode());
        log.info("Updated ses code");

        log.info("Sending a request to send an email to the client with the security code.");
        kafkaSenderService.sendSendSesMessage(statementUUID,
                                              statementService.getSesByStatementId(statementUUID),
                                              clientEmail);
    }

    public void sesCodeVerificationEvent(UUID statementUUID, String SesCode) throws StatementChangeBlocked,
                                                                                    SesCodeVerificationFailed,
                                                                                    StatementNotFoundException {
        throwIfStatementIsDenied(statementUUID);

        StatementEntity statementEntity = statementService.findById(statementUUID);

        if (!statementEntity.getSesCode().equals(SesCode)) {
            log.error("SES-code provided by a client is not valid");
            statementService.changeStatementStatus(statementEntity, ApplicationStatus.CC_DENIED);
            kafkaSenderService.sendStatementDeniedMessage(statementUUID, statementEntity.getClient().getEmail());
            throw new SesCodeVerificationFailed("SES codes do not match!");
        }

        log.info("Credit issued. Updating statuses.");
        creditService.updateCreditStatus(statementEntity.getCredit().getCreditId());
        statementService.changeStatementStatus(statementEntity, ApplicationStatus.DOCUMENT_SIGNED);
        statementService.issueCredit(statementUUID);

        log.info("Making a request to send email to the client with confirmation that the loan was issued.");
        kafkaSenderService.sendCreditIssuedMessage(statementUUID, statementEntity.getClient().getEmail());
    }

    public void documentCreatedStatusChange(UUID statementUUID) throws StatementChangeBlocked,
                                                                       StatementNotFoundException {
        throwIfStatementIsDenied(statementUUID);

        log.info("Received information that documents were created and sent to the client. Changing status...");

        StatementEntity statementEntity = statementService.findById(statementUUID);

        statementService.changeStatementStatus(statementEntity, ApplicationStatus.DOCUMENT_CREATED);
    }

    public DocumentDataDto formDocumentData(UUID statementUUID) throws StatementChangeBlocked,
                                                                       StatementNotFoundException {
        throwIfStatementIsDenied(statementUUID);

        log.info("Forming the client data needed to create credit documents.");

        return statementService.enrichDocumentData(statementUUID);
    }

    public StatementDto getStatement(UUID statementUUID) throws StatementNotFoundException {
        StatementEntity statementEntity = statementService.findById(statementUUID);

        log.info("Retrieving the statement from the database.");

        return statementService.createStatementDto(statementEntity);
    }

    public List<StatementDto> getAllStatements() {
        return statementService.pullAllStatements();
    }

    public void throwIfStatementIsDenied(UUID statementUUID) throws StatementChangeBlocked, StatementNotFoundException {
        if (statementService.isDenied(statementUUID)) {
            log.warn("Statement had been denied earlier. All the changes are blocked.");
            throw new StatementChangeBlocked("Statement had been denied earlier!");
        }
    }
}
