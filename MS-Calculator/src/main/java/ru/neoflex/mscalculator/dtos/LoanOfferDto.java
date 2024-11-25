package ru.neoflex.mscalculator.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class LoanOfferDto {

    public LoanOfferDto() {}

    private final UUID statementId = UUID.randomUUID();

    public LoanOfferDto(BigDecimal requestedAmount,
                        BigDecimal totalAmount,
                        Integer term,
                        BigDecimal monthlyPayment,
                        BigDecimal rate,
                        Boolean isInsuranceEnabled,
                        Boolean isSalaryClient) {
        this.requestedAmount = requestedAmount;
        this.totalAmount = totalAmount;
        this.monthlyPayment = monthlyPayment;
        this.term = term;
        this.rate = rate;
        this.isInsuranceEnabled = isInsuranceEnabled;
        this.isSalaryClient = isSalaryClient;
    }


    private BigDecimal requestedAmount;
    private BigDecimal totalAmount;
    private Integer term;
    private BigDecimal monthlyPayment;
    private BigDecimal rate;
    private Boolean isInsuranceEnabled;
    private Boolean isSalaryClient;
}
