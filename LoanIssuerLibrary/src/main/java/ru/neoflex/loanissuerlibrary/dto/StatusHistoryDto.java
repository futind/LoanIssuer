package ru.neoflex.loanissuerlibrary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.neoflex.loanissuerlibrary.dto.enumeration.ApplicationStatus;
import ru.neoflex.loanissuerlibrary.dto.enumeration.ChangeType;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "DTO описывающее время и характер изменения статуса заявки.")
public class StatusHistoryDto {

    @NotNull
    @Schema(description = "Статус заявки", example = "CC_APPROVED")
    private ApplicationStatus status;

    @NotNull
    @Schema(description = "Время смены статуса", example = "2014-04-04 20:00:00")
    private LocalDateTime timestamp;

    @NotNull
    @Schema(description = "Вид смены статуса (автоматически или вручную)", example = "AUTOMATIC")
    private ChangeType changeType;
}
