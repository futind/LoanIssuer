package ru.standards.msgateway.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.neoflex.loanissuerlibrary.dto.FinishRegistrationRequestDto;
import ru.neoflex.loanissuerlibrary.dto.LoanOfferDto;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;
import ru.standards.msgateway.service.RestClientService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class GatewayController {

    private final RestClientService restClientService;

    GatewayController(RestClientService restClientService) {
        this.restClientService = restClientService;
    }

    @PostMapping("/statement")
    public List<LoanOfferDto> prescoreAndGetOffers(@RequestBody @Valid LoanStatementRequestDto request) {
        return restClientService.createStatementGetOffers(request);
    }

    @PostMapping("/statement/select")
    public void selectOffer(@RequestBody @Valid LoanOfferDto offer) {
        restClientService.selectStatement(offer);
    }

    @PostMapping("/statement/registration/{statementId}")
    public void finishRegistration(@PathVariable("statementId") UUID statementId,
                                   @RequestBody @Valid FinishRegistrationRequestDto request) {
        restClientService.finishRegistration(statementId, request);
    }

    @PostMapping("/document/{statementId}")
    public void createDocuments(@PathVariable("statementId") UUID statementId) {
        restClientService.sendDocuments(statementId);
    }

    @PostMapping("/document/{statementId}/sign")
    public void signDocumentRequest(@PathVariable("statementId") UUID statementId) {
        restClientService.signDocuments(statementId);
    }

    @PostMapping("/document/{statementId}/sign/code")
    public void verifySesCode(@PathVariable("statementId") UUID statementId,
                              @RequestParam("code") String code) {
        restClientService.verifyCode(statementId, code);
    }
}
