package ru.neoflex.msdeal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.neoflex.msdeal.dto.enumeration.Gender;
import ru.neoflex.msdeal.dto.enumeration.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@Schema(description = "DTO, содержащее наиболее полные данные о клиенте, необходимые для скоринга")
public class ScoringDataDto {

    @NotNull
    @Min(value = 20000)
    @Schema(description = "Сумма займа в рублях", example = "100000")
    private BigDecimal amount;

    @NotNull
    @Min(value = 6)
    @Schema(description = "Срок займа в месяцах", example = "12")
    private Integer term;

    @NotNull
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Z]+$")
    @Schema(description = "Имя клиента", example = "Джон")
    private String firstName;

    @NotNull
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Z]+$")
    @Schema(description = "Фамилия клиента", example = "Доу")
    private String lastName;

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Z]+$")
    @Schema(description = "Отчество клиента (при наличии)", example = "Джонсович")
    private String middleName;

    @NotNull
    @Schema(description = "Пол клиента", example = "MALE")
    private Gender gender;

    @NotNull
    @Schema(description = "Дата рождения клиента", example = "1996-12-20")
    private LocalDate birthdate;

    @NotNull
    @Pattern(regexp = "\\d{4}")
    @Schema(description = "Серия паспорта клиента", example = "3141")
    private String passportSeries;

    @NotNull
    @Pattern(regexp = "\\d{6}")
    @Schema(description = "Номер паспорта клиента", example = "667430")
    private String passportNumber;

    @NotNull
    @Schema(description = "Дата выдачи паспорта клиента", example = "2010-12-27")
    private LocalDate passportIssueDate;

    @NotNull
    @Schema(description = "Подразделение, выдавшее паспорт клиенту", example = "ГУ МВД РОССИИ ПО МОСКОВКОЙ ОБЛАСТИ")
    private String passportIssueBranch;

    @NotNull
    @Schema(description = "Семейное положение клиента", example = "NOT_MARRIED")
    private MaritalStatus maritalStatus;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Количество людей финансово зависимых от клиента", example = "0")
    private Integer dependentAmount;

    @NotNull
    @Valid
    @Schema(description = "Данные о занятости клиента", example = "SELF_EMPLOYED")
    private EmploymentDto employment;

    @NotNull
    @Schema(description = "Номер банковского счёта клиента", example = "3150422154")
    private String accountNumber;

    @NotNull
    @Schema(description = "Застрахован ли кредит", example = "true")
    private Boolean isInsuranceEnabled;

    @NotNull
    @Schema(description = "Получает ли клиент зарплату в банке", example = "false")
    private Boolean isSalaryClient;

    @AssertTrue(message = "Passport can't be issued before birth")
    public boolean isPassportIssueDateValid() {
        return passportIssueDate.isAfter(birthdate);
    }

    @AssertTrue(message = "Applicant must be at least 18 years old")
    public boolean isBirthdateValid() {
        return ChronoUnit.YEARS.between(birthdate, LocalDate.now()) >= 18;
    }
}
