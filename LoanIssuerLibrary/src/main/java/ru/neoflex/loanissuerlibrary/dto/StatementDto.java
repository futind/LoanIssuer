package ru.neoflex.loanissuerlibrary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import ru.neoflex.loanissuerlibrary.dto.enumeration.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StatementDto {

    @NotNull
    @Schema(description = "Уникальный идентификационный номер заявки")
    private UUID statementId;

    @NotNull
    @Schema(description = "Уникальный идентификационный номер клиента")
    private UUID clientId;

    @Schema(description = "Уникальный идентификационный номер кредита для клиента")
    private UUID creditId;

    @NotNull
    @Schema(description = "Актуальный статус заявки", example = "PREAPPROVAL")
    private ApplicationStatus status;

    @NotNull
    @Schema(description = "Дата и время создания заявки", example = "2019-03-27T10:15:30")
    private LocalDateTime creationDate;

    @Schema(description = "Принятое клиентом кредитное предложение")
    private LoanOfferDto appliedOffer;

    @Schema(description = "Дата и время подписания договора клиентом", example = "2019-03-27T10:15:30")
    private LocalDateTime signDate;

    @Schema(description = "Код подтверждения клиента", example = "123456")
    @Pattern(regexp = "\\d{6}")
    private String sesCode;

    @NotNull
    @Schema(description = "История изменения статусов заявки")
    private List<StatusHistoryDto> statusHistory;

    @AssertTrue(message = "Sign date must be after creation date (if present)")
    public boolean isSignDateValid() {
        return signDate == null || !signDate.isBefore(creationDate);
    }
}
