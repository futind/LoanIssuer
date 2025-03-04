package ru.neoflex.loanissuerlibrary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.neoflex.loanissuerlibrary.dto.EmploymentDto;
import ru.neoflex.loanissuerlibrary.dto.enumeration.Gender;
import ru.neoflex.loanissuerlibrary.dto.enumeration.MaritalStatus;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Дополнительные данные о клиенте, необходимые для скоринга.")
public class FinishRegistrationRequestDto {

    @NotNull
    @Schema(description = "Пол клиента", example = "MALE")
    private Gender gender;

    @NotNull
    @Schema(description = "Семейное положение клиента", example = "MARRIED")
    private MaritalStatus maritalStatus;

    @NotNull
    @Min(value = 0)
    @Schema(description = "Количество финансово зависящих от клиента людей", example = "1")
    private Integer dependentAmount;

    @NotNull
    @Schema(description = "Дата выдачи паспорта", example = "2014-07-07")
    private LocalDate passportIssueDate;

    @NotNull
    @NotBlank
    @Schema(description = "Подразделение, выдавшее паспорт", example = "ГУ МВД РОССИИ ПО МОСКОВСКОЙ ОБЛАСТИ")
    private String passportIssueBranch;

    @NotNull
    @Schema(description = "Данные о занятости клиента")
    private EmploymentDto employment;

    @NotNull
    @NotBlank
    @Schema(description = "Номер банковского аккаунта клиента", example = "1235456")
    private String accountNumber;
}
