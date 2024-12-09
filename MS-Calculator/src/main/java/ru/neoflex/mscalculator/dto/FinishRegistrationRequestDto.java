package ru.neoflex.mscalculator.dto;

import lombok.Getter;
import lombok.Setter;
import ru.neoflex.mscalculator.dto.enumeration.Gender;
import ru.neoflex.mscalculator.dto.enumeration.MaritalStatus;

import java.time.LocalDate;

@Setter
@Getter
public class FinishRegistrationRequestDto {

    private Gender gender;
    private MaritalStatus maritalStatus;
    private Integer dependentAmount;
    private LocalDate passportIssueDate;
    private String passportIssueBranch;
    private EmploymentDto employment;
    private String accountNumber;
}
