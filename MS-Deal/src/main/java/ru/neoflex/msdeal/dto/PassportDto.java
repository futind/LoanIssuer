package ru.neoflex.msdeal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Паспортные данные клиента")
public class PassportDto {

    @NotNull
    @Pattern(regexp = "\\d{4}")
    @Schema(description = "Серия паспорта", example = "1234")
    private String series;

    @NotNull
    @Pattern(regexp = "\\d{6}")
    @Schema(description = "Номер паспорта", example = "123456")
    private String number;

    @Schema(description = "Подразделение, выдавшее паспорт", example = "ГУ МВД РОССИИ ПО МОСКОВСКОЙ ОБЛАСТИ")
    private String issueBranch;

    @Schema(description = "Дата выдачи паспорта", example = "2014-07-07")
    private LocalDate issueDate;
}
