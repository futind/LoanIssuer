package ru.neoflex.mscalculator.dto;

import lombok.Getter;
import lombok.Setter;
import ru.neoflex.mscalculator.dto.enumeration.StatementStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class StatementStatusHistoryDto {

    private StatementStatus status;
    private LocalDateTime time;
    private StatementStatus changeType;
}
