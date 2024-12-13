package ru.neoflex.msdeal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "DTO, содержащее рассчитанные данные кредита")
public class CreditDto {

    @Override
    public String toString() {
        return String.format(format,
                amount,
                term,
                monthlyPayment,
                rate,
                psk,
                isInsuranceEnabled,
                isSalaryClient,
                paymentSchedule.stream()
                        .map(PaymentScheduleElementDto::toString)
                        .collect(Collectors.joining("; ")));
    }
    @NotNull
    @Schema(description = "Сумма займа в рублях")
    @Min(value = 20000)
    private BigDecimal amount;

    @Schema(description = "Срок кредита в месяцах")
    @NotNull
    private Integer term;

    @NotNull
    @Schema(description = "Ежемесячный платёж в рублях")
    private BigDecimal monthlyPayment;

    @NotNull
    @Schema(description = "Процентная ставка")
    private BigDecimal rate;

    @NotNull
    @Schema(description = "Полная стоимость кредита в рублях")
    private BigDecimal psk;

    @NotNull
    @Schema(description = "Застрахован ли кредит")
    private Boolean isInsuranceEnabled;

    @NotNull
    @Schema(description = "Получает ли клиент зарплату в банке")
    private Boolean isSalaryClient;

    @NotNull
    @Schema(description = "График ежемесячных платежей")
    private List<PaymentScheduleElementDto> paymentSchedule;

    @JsonIgnore
    private final String format = """
                                  amount=%s, term=%d, monthlyPayment=%s, rate=%s, psk=%s, \
                                  "isInsuranceEnabled=%b, isSalaryClient=%b, paymentSchedule=%s""";
}
