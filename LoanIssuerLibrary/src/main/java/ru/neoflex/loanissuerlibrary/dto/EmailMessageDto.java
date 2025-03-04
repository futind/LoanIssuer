package ru.neoflex.loanissuerlibrary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.neoflex.loanissuerlibrary.dto.enumeration.EmailTheme;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema
public class EmailMessageDto {

    @NotNull
    private String address;

    @NotNull
    private EmailTheme theme;

    @NotNull
    private UUID statementId;

    @NotBlank
    private String text;
}
