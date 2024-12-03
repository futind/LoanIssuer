package ru.neoflex.mscalculator.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.neoflex.mscalculator.dtos.enumeration.EmploymentStatus;
import ru.neoflex.mscalculator.dtos.enumeration.WorkPosition;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO, представляющее собой данные о занятости клиента")
public class EmploymentDto {

    @NotNull
    @Schema(description = "Тип занятости клиента")
    private EmploymentStatus employmentStatus;

    @NotNull
    @Size(min = 10, max = 12)
    @Pattern(regexp = "^\\d{10,12}$")
    @Schema(description = "Идентификационный номер налогоплательщика работодателя клиента")
    private String employerINN;

    @NotNull
    @Schema(description = "Ежемесячная зарплата клиента")
    private BigDecimal salary;

    @NotNull
    @Schema(description = "Должность клиента")
    private WorkPosition position;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Суммарный опыт работы клиента в месяцах")
    private Integer workExperienceTotal;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Опыт работы клиента на нынешнем месте работы в месяцах")
    private Integer workExperienceCurrent;
}
