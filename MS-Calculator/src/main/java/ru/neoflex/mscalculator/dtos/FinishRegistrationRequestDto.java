package ru.neoflex.mscalculator.dtos;

import lombok.Getter;
import lombok.Setter;
import ru.neoflex.mscalculator.dtos.enumeration.Gender;
import ru.neoflex.mscalculator.dtos.enumeration.MaritalStatus;

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
