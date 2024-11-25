package ru.neoflex.mscalculator.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.neoflex.mscalculator.dtos.enumeration.EmploymentStatus;
import ru.neoflex.mscalculator.dtos.enumeration.WorkPosition;

import java.math.BigDecimal;

@Setter
@Getter
public class EmploymentDto {

    @NotNull
    private EmploymentStatus employmentStatus;

    @NotNull
    @Size(min = 10, max = 12)
    @Pattern(regexp = "^\\d{10,12}$")
    private String employerINN;

    @NotNull
    private BigDecimal salary;

    @NotNull
    private WorkPosition position;

    @NotNull
    @Min(value = 0)
    private Integer workExperienceTotal;

    @NotNull
    @Min(value = 0)
    private Integer workExperienceCurrent;
}
