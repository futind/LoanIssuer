package ru.neoflex.mscalculator.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ru.neoflex.mscalculator.dtos.enumeration.Gender;
import ru.neoflex.mscalculator.dtos.enumeration.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ScoringDataDto {

    @AssertTrue(message = "Applicant must be at least 18 years old")
    public boolean isBirthdateValid() {
        return ChronoUnit.YEARS.between(birthdate, LocalDate.now()) >= 18;
    }

    @NotNull
    @Min(value = 20000)
    private BigDecimal amount;

    @NotNull
    @Min(value = 6)
    private Integer term;

    @NotNull
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Z]+$")
    private String firstName;

    @NotNull
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Z]+$")
    private String lastName;

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Z]+$")
    private String middleName;

    @NotNull
    private Gender gender;

    @NotNull
    private LocalDate birthdate;

    @NotNull
    @Pattern(regexp = "\\d{4}")
    private String passportSeries;

    @NotNull
    @Pattern(regexp = "\\d{6}")
    private String passportNumber;

    @NotNull
    private LocalDate passportIssueDate;

    @NotNull
    private String passportIssueBranch;

    @NotNull
    private MaritalStatus maritalStatus;

    // Что такое dependentAmount?
    // Количество людей, зависящих финансово от клиента?
    @NotNull
    private Integer dependentAmount;

    @NotNull
    private EmploymentDto employment;

    @NotNull
    private String accountNumber;

    @NotNull
    private Boolean isInsuranceEnabled;

    @NotNull
    private Boolean isSalaryClient;
}
