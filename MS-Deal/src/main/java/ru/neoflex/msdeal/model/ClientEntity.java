package ru.neoflex.msdeal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.neoflex.msdeal.dto.EmploymentDto;
import ru.neoflex.msdeal.dto.PassportDto;
import ru.neoflex.msdeal.dto.enumeration.Gender;
import ru.neoflex.msdeal.dto.enumeration.MaritalStatus;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "client")
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID clientId;

    @Column(name = "first_name", nullable = false)
    String firstName;

    @Column(name = "last_name", nullable = false)
    String lastName;

    @Column(name = "middle_name", nullable = true)
    String middleName;

    @Column(name = "birth_date",nullable = false)
    LocalDate birthDate;

    @Column(name = "email", nullable = false)
    String email;

    @Column(name = "gender", nullable = true)
    @Enumerated(EnumType.STRING)
    Gender gender;

    @Column(name = "marital_status", nullable = true)
    @Enumerated(EnumType.STRING)
    MaritalStatus maritalStatus;

    @Column(name = "dependent_amount", nullable = true)
    Integer dependentAmount;

    @Column(name = "passport", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private PassportDto passport;

    @Column(name = "employment", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private EmploymentDto employment;

    @Column(name = "account_number", nullable = true)
    private String accountNumber;
}
