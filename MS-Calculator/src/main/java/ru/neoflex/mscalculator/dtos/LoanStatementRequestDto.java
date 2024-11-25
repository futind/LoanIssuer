package ru.neoflex.mscalculator.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@Setter
@Getter
public class LoanStatementRequestDto {

    @NotNull
    @Min(20000)
    private BigDecimal amount;

    @NotNull
    @Min(6)
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
    @Pattern(regexp = "^[a-z0-9A-Z_!#$%&'*+/=?`{|}~^.-]+@[a-z0-9A-Z.-]+$")
    private String email;

    @NotNull
    private LocalDate birthdate;

    @NotNull
    @Pattern(regexp = "\\d{4}")
    private String passportSeries;

    @NotNull
    @Pattern(regexp = "\\d{6}")
    private String passportNumber;

    @AssertTrue(message = "Applicant must be at least 18 years old")
    public boolean isBirthdateValid() {
        return ChronoUnit.YEARS.between(birthdate, LocalDate.now()) >= 18;
    }
}
