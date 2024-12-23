package ru.neoflex.msstatement.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.msstatement.dto.LoanOfferDto;
import ru.neoflex.msstatement.dto.LoanStatementRequestDto;
import ru.neoflex.msstatement.exception.PrescoringFailedException;
import ru.neoflex.msstatement.service.PrescoringService;
import ru.neoflex.msstatement.service.RestClientService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/statement")
public class StatementController implements StatementApi {

    private final RestClientService restClientService;
    private final PrescoringService prescoringService;

    public StatementController(RestClientService restClientService,
                               PrescoringService prescoringService) {
        this.restClientService = restClientService;
        this.prescoringService = prescoringService;
    }

    @PostMapping()
    @Override
    public List<LoanOfferDto> getOffers(@RequestBody @Valid LoanStatementRequestDto request)
                                                            throws PrescoringFailedException,
                                                                   RestClientResponseException {
        log.info("Got valid request to /statement");
        prescoringService.prescore(request);
        log.info("Client has passed the prescoring.");

        return restClientService.getOffers(request);
    }

    @PostMapping("/offer")
    @Override
    public void selectOffer(@RequestBody @Valid LoanOfferDto offer) throws RestClientResponseException {
        log.info("Got valid request to /statement/offer");
        restClientService.selectOffer(offer);
    }
}
