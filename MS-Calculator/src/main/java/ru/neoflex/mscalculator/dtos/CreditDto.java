package ru.neoflex.mscalculator.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreditDto {

    public CreditDto() {}

    public CreditDto(BigDecimal amount,
                     Integer term,
                     BigDecimal monthlyPayment,
                     BigDecimal rate,
                     BigDecimal psk,
                     Boolean isInsuranceEnabled,
                     Boolean isSalaryClient,
                     List<PaymentScheduleElementDto> paymentSchedule) {
        this.amount = amount;
        this.term = term;
        this.monthlyPayment = monthlyPayment;
        this.rate = rate;
        this.psk = psk;
        this.isInsuranceEnabled = isInsuranceEnabled;
        this.isSalaryClient = isSalaryClient;
        this.paymentSchedule = paymentSchedule;
    }

    private BigDecimal amount;
    private Integer term;
    private BigDecimal monthlyPayment;
    private BigDecimal rate;
    private BigDecimal psk;
    private Boolean isInsuranceEnabled;
    private Boolean isSalaryClient;
    private List<PaymentScheduleElementDto> paymentSchedule;
}
