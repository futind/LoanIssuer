package ru.neoflex.mscalculator.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@Data
@Schema(description = "DTO, содержащий детали кредитного запроса от клиента и его данные")
@Builder
public class LoanStatementRequestDto {

    @NotNull
    @Min(20000)
    @Schema(description = "Сумма займа в рублях", example = "150000")
    private BigDecimal amount;

    @NotNull
    @Min(value = 6)
    @Schema(description = "Срок кредита в месяцах", example = "24")
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
    @Pattern(regexp = "^[a-z0-9A-Z_!#$%&'*+/=?`{|}~^.-]+@[a-z0-9A-Z.-]+$")
    @Schema(description = "Адрес электронной почты клиента", example = "john.doe@mail.ru")
    private String email;

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

    @AssertTrue(message = "Applicant must be at least 18 years old")
    public boolean isBirthdateValid() {
        return ChronoUnit.YEARS.between(birthdate, LocalDate.now()) >= 18;
    }
}
