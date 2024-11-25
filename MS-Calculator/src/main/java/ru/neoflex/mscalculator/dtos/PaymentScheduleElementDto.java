package ru.neoflex.mscalculator.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class PaymentScheduleElementDto {

    public PaymentScheduleElementDto() {}

    public PaymentScheduleElementDto(Integer number,
                                     LocalDate date,
                                     BigDecimal totalPayment,
                                     BigDecimal interestPayment,
                                     BigDecimal debtPayment,
                                     BigDecimal remainingDebt) {
        this.number = number;
        this.date = date;
        this.totalPayment = totalPayment;
        this.interestPayment = interestPayment;
        this.debtPayment = debtPayment;
        this.remainingDebt = remainingDebt;
    }

    private Integer number;
    private LocalDate date;
    private BigDecimal totalPayment;
    private BigDecimal interestPayment;
    private BigDecimal debtPayment;
    private BigDecimal remainingDebt;
}
