package ru.neoflex.mscalculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@Schema(description = "DTO, представляющий собой предварительное кредитное предложение")
public class LoanOfferDto {

    @Schema(description = "Уникальный номер кредитного предложения")
    private UUID statementId;

    @NotNull
    @Min(value = 20000)
    @Schema(description = "Сумма займа в рублях")
    private BigDecimal requestedAmount;

    @NotNull
    @Schema(description = "Полная стоимость кредита в рублях")
    private BigDecimal totalAmount;

    @NotNull
    @Min(value = 6)
    @Schema(description = "Срок кредита в месяцах")
    private Integer term;

    @NotNull
    @Schema(description = "Ежемесячный платёж в рублях")
    private BigDecimal monthlyPayment;

    @NotNull
    @Schema(description = "Процентная ставка")
    private BigDecimal rate;

    @NotNull
    @Schema(description = "Застрахован ли кредит")
    private Boolean isInsuranceEnabled;

    @NotNull
    @Schema(description = "Получает ли клиент зарплату в банке")
    private Boolean isSalaryClient;
}
