package ru.neoflex.msstatement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO, представляющий собой предварительное кредитное предложение")
public class LoanOfferDto {

    @Schema(description = "Уникальный номер кредитного предложения")
    private UUID statementId = UUID.randomUUID();

    @NotNull
    @Min(value = 20000)
    @Schema(description = "Сумма займа в рублях", example = "100000")
    private BigDecimal requestedAmount;

    @NotNull
    @Schema(description = "Полная стоимость кредита в рублях", example = "120000")
    private BigDecimal totalAmount;

    @NotNull
    @Min(value = 6)
    @Schema(description = "Срок кредита в месяцах", example = "6")
    private Integer term;

    @NotNull
    @Schema(description = "Ежемесячный платёж в рублях", example = "20000")
    private BigDecimal monthlyPayment;

    @NotNull
    @Schema(description = "Процентная ставка", example = "20%")
    private BigDecimal rate;

    @NotNull
    @Schema(description = "Застрахован ли кредит", example = "false")
    private Boolean isInsuranceEnabled;

    @NotNull
    @Schema(description = "Получает ли клиент зарплату в банке", example = "true")
    private Boolean isSalaryClient;
}
