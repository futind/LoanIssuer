package ru.neoflex.loanissuerlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.neoflex.loanissuerlibrary.dto.enumeration.EmploymentStatus;
import ru.neoflex.loanissuerlibrary.dto.enumeration.WorkPosition;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "DTO, представляющее собой данные о занятости клиента")
public class EmploymentDto {

    @NotNull
    @Schema(description = "Тип занятости клиента", example = "EMPLOYED")
    private EmploymentStatus employmentStatus;

    @NotNull
    @Size(min = 10, max = 12)
    @Pattern(regexp = "^\\d{10,12}$")
    @Schema(description = "Идентификационный номер налогоплательщика работодателя клиента", example = "9921242264")
    private String employerINN;

    @NotNull
    @Schema(description = "Ежемесячная зарплата клиента", example = "50000")
    private BigDecimal salary;

    @Schema(description = "Должность клиента", example = "JUNIOR")
    private WorkPosition position;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Суммарный опыт работы клиента в месяцах", example = "36")
    private Integer workExperienceTotal;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Опыт работы клиента на нынешнем месте работы в месяцах", example = "12")
    private Integer workExperienceCurrent;

    @JsonIgnore
    @AssertTrue(message = "Work experience on a current job can exceed total work experience!")
    public boolean isWorkExperienceCurrentValid() {
        return workExperienceCurrent <= workExperienceTotal;
    }
}
