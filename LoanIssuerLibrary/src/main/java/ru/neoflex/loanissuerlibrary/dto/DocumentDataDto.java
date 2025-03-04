package ru.neoflex.loanissuerlibrary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema
public class DocumentDataDto {

    @NotNull
    @Schema(description = "Данные о кредите")
    CreditDto credit;

    @NotBlank
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯЁё]+$")
    @Schema(description = "Имя клиента", example = "Джон")
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯЁё]+$")
    @Schema(description = "Фамилия клиента", example = "Доу")
    private String lastName;

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯЁё]+$")
    @Schema(description = "Отчество клиента (при наличии)", example = "Джонсович")
    private String middleName;

    @NotNull
    @Schema(description = "Дата рождения клиента", example = "1996-12-20")
    private LocalDate birthdate;
}
