package ru.neoflex.msdeal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.loanissuerlibrary.dto.*;
import ru.neoflex.loanissuerlibrary.exception.CreditDeniedException;
import ru.neoflex.loanissuerlibrary.exception.SesCodeVerificationFailed;
import ru.neoflex.loanissuerlibrary.exception.StatementChangeBlocked;
import ru.neoflex.loanissuerlibrary.exception.StatementNotFoundException;
import ru.neoflex.msdeal.service.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "ms_deal")
@Slf4j
@RestController
@RequestMapping("/deal")
public class DealController implements DealApi {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping("/statement")
    public List<LoanOfferDto> getOffers(@RequestBody @Valid LoanStatementRequestDto request) {

        log.info("Received a valid request to /deal/statement; amount: {}, term: {}",
                request.getAmount(),
                request.getTerm());

        return dealService.createStatementGetOffers(request);
    }

    @PostMapping("/offer/select")
    public void select(@RequestBody @Valid LoanOfferDto offer) throws StatementNotFoundException {
        log.info("Received a POST request to /deal/offer/select");

        dealService.applyOffer(offer);
    }

    @PostMapping("/calculate/{statementId}")
    public void finishRegistration(@RequestBody @Valid FinishRegistrationRequestDto request,
                                   @PathVariable("statementId") UUID statementId)
            throws CreditDeniedException,
            RestClientResponseException,
            StatementNotFoundException, StatementChangeBlocked {
        log.info("Received a POST request to /deal/calculate/{statementId}");
        dealService.registrationCalculation(request, statementId);
    }

    @PostMapping("/document/{statementId}/send")
    public void sendDocuments(@PathVariable("statementId") UUID statementId) throws StatementChangeBlocked,
                                                                                      StatementNotFoundException {
        log.info("Received a POST request to /deal/document/{statementId}/send");
        dealService.sendDocumentEventAndStatus(statementId);
    }

    @PostMapping("/document/{statementId}/sign")
    public void signDocuments(@PathVariable("statementId") UUID statementId) throws StatementChangeBlocked,
                                                                                      StatementNotFoundException {
        log.info("Received a POST request to /deal/document/{statementId}/sign");

        dealService.sesUpdateEvent(statementId);
    }

    @PostMapping("/document/{statementId}/code")
    public void signingCode(@RequestParam String code,
                            @PathVariable("statementId") UUID statementId) throws StatementChangeBlocked,
                                                                                    SesCodeVerificationFailed,
                                                                                    StatementNotFoundException {
        log.info("Received a POST request to /deal/document/{statementId}/code");

        dealService.sesCodeVerificationEvent(statementId, code);
    }

    @PutMapping("/admin/statement/{statementId}/status")
    public void documentsCreatedStatusChange(@PathVariable("statementId") UUID statementId)
                                                                 throws StatementNotFoundException,
                                                                        StatementChangeBlocked {
        log.info("Received a PUT request to /deal/admin/statement/{statementId}/status");

        dealService.documentCreatedStatusChange(statementId);
    }

    @GetMapping("/document/{statementId}/data")
    public DocumentDataDto getDocumentData(@PathVariable("statementId") UUID statementId)
                                            throws StatementNotFoundException, StatementChangeBlocked {
        log.info("Received a GET request to /deal/document/{statementId}/data");

        return dealService.formDocumentData(statementId);
    }

    @GetMapping("/admin/statement/{statementId}")
    public StatementDto getStatementById(@PathVariable("statementId") UUID statementId)
                                                     throws StatementNotFoundException {
        log.info("Received a GET request to /deal/admin/statement/{statementId}");

        return dealService.getStatement(statementId);
    }

    @GetMapping("/admin/statement")
    public List<StatementDto> getAllStatements() {
        log.info("Received a GET request to /deal/admin/statement");

        return dealService.getAllStatements();
    }
}
