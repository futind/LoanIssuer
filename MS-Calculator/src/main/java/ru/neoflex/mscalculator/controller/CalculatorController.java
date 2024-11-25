package ru.neoflex.mscalculator.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.neoflex.mscalculator.dtos.CreditDto;
import ru.neoflex.mscalculator.dtos.LoanOfferDto;
import ru.neoflex.mscalculator.dtos.LoanStatementRequestDto;
import ru.neoflex.mscalculator.dtos.ScoringDataDto;
import ru.neoflex.mscalculator.service.CalculatorService;

import java.util.List;

@RestController
@RequestMapping
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping(path = "/offers")
    public List<LoanOfferDto> getOffers(@Valid @RequestBody @NotNull LoanStatementRequestDto loanStatementRequestDto) {
        return calculatorService.getOffers(loanStatementRequestDto);
    }

    @PostMapping(path = "/calc")
    public CreditDto calculateCredit(@RequestBody ScoringDataDto scoringDataDto) {
        return calculatorService.getCredit(scoringDataDto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIAException(IllegalArgumentException e) {
        return "Illegal argument: " + e.getMessage();
    }

}
