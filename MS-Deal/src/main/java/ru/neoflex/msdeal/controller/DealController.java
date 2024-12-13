package ru.neoflex.msdeal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.msdeal.dto.*;
import ru.neoflex.msdeal.dto.enumeration.ApplicationStatus;
import ru.neoflex.msdeal.exception.CreditDeniedException;
import ru.neoflex.msdeal.exception.StatementNotFoundException;
import ru.neoflex.msdeal.model.ClientEntity;
import ru.neoflex.msdeal.model.CreditEntity;
import ru.neoflex.msdeal.model.StatementEntity;
import ru.neoflex.msdeal.service.ClientService;
import ru.neoflex.msdeal.service.CreditService;
import ru.neoflex.msdeal.service.RestClientService;
import ru.neoflex.msdeal.service.StatementService;

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

    private final String CALCULATOR_SERVICE_PORT = "8080";
    private final String URI_BASE = "http://localhost:";


    public DealController(ClientService clientService,
                          StatementService statementService,
                          CreditService creditService,
                          RestClientService restClientService) {
        this.clientService = clientService;
        this.statementService = statementService;
        this.creditService = creditService;
        this.restClientService = restClientService;
    }

    @PostMapping("/statement")
    public List<LoanOfferDto> getOffers(@RequestBody @Valid LoanStatementRequestDto request) {

        log.info("Received a valid request to /deal/statement; amount: {}, term: {}",
                request.getAmount(),
                request.getTerm());

        ClientEntity clientEntity = clientService.createClientWithRequest(request);

        StatementEntity statementEntity = statementService.createStatementWithClient(clientEntity);

        log.info("Making a request to {}/calculator/offers...", URI_BASE + CALCULATOR_SERVICE_PORT);

        List<LoanOfferDto> offers = restClientService.getLoanOffers(request);

        offers.stream().forEach(offer -> offer.setStatementId(statementEntity.getStatementId()));

        log.info("Returning loan offers to the client...");

        return offers;
    }


    @PostMapping("/offer/select")
    public void select(@RequestBody @Valid LoanOfferDto offer) {
        statementService.setAppliedOffer(offer);
    }


    @PostMapping("calculate/{statementId}")
    public void finishRegistration(@RequestBody @Valid FinishRegistrationRequestDto request,
                                   @PathVariable("statementId") String statementId)
                                        throws CreditDeniedException,
                                               RestClientResponseException,
                                               StatementNotFoundException {
        UUID statementUUID = UUID.fromString(statementId);
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
            throw e;
        }

        CreditEntity creditEntity = creditService.saveCredit(creditDto);

        statementEntity = statementService.setCredit(statementUUID, creditEntity);
        statementService.changeStatementStatus(statementEntity, ApplicationStatus.CC_APPROVED);
    }

}
