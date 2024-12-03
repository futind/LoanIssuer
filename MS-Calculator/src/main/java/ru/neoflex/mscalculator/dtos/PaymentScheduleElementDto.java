package ru.neoflex.mscalculator.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Элемент графика платежей")
public class PaymentScheduleElementDto {

    @Override
    public String toString() {
        return String.format(format,
                             number,
                             date,
                             totalPayment,
                             interestPayment,
                             debtPayment,
                             remainingDebt);
    }

    @NotNull
    @Min(value = 1)
    @Schema(description = "Порядковый номер платежа")
    private Integer number;

    @NotNull
    @Schema(description = "Дата платежа в формате ГГГГ-ММ-ДД")
    private LocalDate date;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Сумма платежа")
    private BigDecimal totalPayment;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Часть суммы платежа, идущая на погашение платы за проценты")
    private BigDecimal interestPayment;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Часть суммы платежа, идущая на погашение основного долга")
    private BigDecimal debtPayment;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Сумма долга, которую останётся выплатить после данного платежа")
    private BigDecimal remainingDebt;

    @JsonIgnore
    private final String format = "{number=%d, date=%s, totalPayment=%s, " +
                                  "interestPayment=%s, debtPayment=%s, remainingDebt=%s}";
}
