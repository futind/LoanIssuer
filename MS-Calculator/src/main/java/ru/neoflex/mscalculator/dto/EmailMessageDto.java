package ru.neoflex.mscalculator.dto;

import lombok.Getter;
import lombok.Setter;
import ru.neoflex.mscalculator.dto.enumeration.EmailTheme;

@Setter
@Getter
public class EmailMessageDto {

    private String address;
    private EmailTheme theme;
    private Long statementId;
    private String text;
}
