package ru.neoflex.mscalculator.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.neoflex.mscalculator.dtos.enumeration.Gender;
import ru.neoflex.mscalculator.dtos.enumeration.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO, содержащее наиболее полные данные о клиенте, необходимые для скоринга")
public class ScoringDataDto {

    @AssertTrue(message = "Applicant must be at least 18 years old")
    public boolean isBirthdateValid() {
        return ChronoUnit.YEARS.between(birthdate, LocalDate.now()) >= 18;
    }

    @NotNull
    @Min(value = 20000)
    @Schema(description = "Сумма займа в рублях")
    private BigDecimal amount;

    @NotNull
    @Min(value = 6)
    @Schema(description = "Срок займа в месяцах")
    private Integer term;

    @NotNull
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Z]+$")
    @Schema(description = "Имя клиента")
    private String firstName;

    @NotNull
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Z]+$")
    @Schema(description = "Фамилия клиента")
    private String lastName;

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Z]+$")
    @Schema(description = "Отчество клиента (при наличии)")
    private String middleName;

    @NotNull
    @Schema(description = "Пол клиента")
    private Gender gender;

    @NotNull
    @Schema(description = "Дата рождения клиента")
    private LocalDate birthdate;

    @NotNull
    @Pattern(regexp = "\\d{4}")
    @Schema(description = "Серия паспорта клиента")
    private String passportSeries;

    @NotNull
    @Pattern(regexp = "\\d{6}")
    @Schema(description = "Номер паспорта клиента")
    private String passportNumber;

    @NotNull
    @Schema(description = "Дата выдачи паспорта клиента")
    private LocalDate passportIssueDate;

    @NotNull
    @Schema(description = "Подразделение, выдавшее паспорт клиенту")
    private String passportIssueBranch;

    @NotNull
    @Schema(description = "Семейное положение клиента")
    private MaritalStatus maritalStatus;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Количество людей финансово зависимых от клиента")
    private Integer dependentAmount;

    @NotNull
    @Schema(description = "Данные о занятости клиента")
    private EmploymentDto employment;

    @NotNull
    @Schema(description = "Номер банковского счёта клиента")
    private String accountNumber;

    @NotNull
    @Schema(description = "Застрахован ли кредит")
    private Boolean isInsuranceEnabled;

    @NotNull
    @Schema(description = "Получает ли клиент зарплату в банке")
    private Boolean isSalaryClient;
}
