package ru.neoflex.mscalculator.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.neoflex.loanissuerlibrary.dto.CreditDto;
import ru.neoflex.loanissuerlibrary.dto.LoanOfferDto;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;
import ru.neoflex.loanissuerlibrary.dto.ScoringDataDto;
import ru.neoflex.loanissuerlibrary.exception.CreditDeniedException;
import ru.neoflex.mscalculator.service.CalculatorService;

import java.util.List;

@Slf4j
@Tag(name = "ms_calculator")
@RestController
@RequestMapping("/calculator")
public class CalculatorController implements CalculatorApi {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Override
    @PostMapping(path = "/offers")
    public List<LoanOfferDto> getOffers(LoanStatementRequestDto loanStatementRequestDto) {

        log.info("Received a valid request to /calculator/offers; amount: {}, term: {}",
                                                        loanStatementRequestDto.getAmount(),
                                                        loanStatementRequestDto.getTerm());

        List<LoanOfferDto> offers = calculatorService.getOffers(loanStatementRequestDto);

        log.info("Generated loan offers: {}", offers.stream()
                                              .map(offer -> String.format("statementId=%s",
                                              offer.getStatementId()))
                                              .toList());
        return offers;
    }

    @Override
    @PostMapping(path = "/calc")
    public CreditDto calculateCredit(ScoringDataDto scoringDataDto) throws CreditDeniedException {
        log.info("Received a valid request to /calculator/calc; " +
                 "amount: {}, term: {} isInsuranceEnabled: {}, isSalaryClient: {}",
                scoringDataDto.getAmount(),
                scoringDataDto.getTerm(),
                scoringDataDto.getIsInsuranceEnabled(),
                scoringDataDto.getIsSalaryClient()
        );

        calculatorService.isEligibleForCredit(scoringDataDto);

        log.info("Clients is eligible for credit, proceeding to create a CreditDto");
        CreditDto creditDto = calculatorService.getCredit(scoringDataDto);

        log.info("Generated CreditDto: {}", creditDto.toString());

        return creditDto;
    }

}
