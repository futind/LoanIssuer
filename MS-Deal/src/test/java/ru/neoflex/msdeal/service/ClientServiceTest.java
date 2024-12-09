package ru.neoflex.msdeal.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.neoflex.msdeal.dto.*;
import ru.neoflex.msdeal.dto.enumeration.EmploymentStatus;
import ru.neoflex.msdeal.dto.enumeration.Gender;
import ru.neoflex.msdeal.dto.enumeration.MaritalStatus;
import ru.neoflex.msdeal.dto.enumeration.WorkPosition;
import ru.neoflex.msdeal.model.ClientEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class ClientServiceTest {

    @Autowired
    private ClientService clientService;

    private LoanStatementRequestDto validRequest;
    private EmploymentDto validEmployment;
    private FinishRegistrationRequestDto validFinishRegistration;

    @BeforeEach
    void setUp() {
        validRequest = LoanStatementRequestDto.builder()
                .amount(new BigDecimal("200000"))
                .term(6)
                .firstName("John")
                .lastName("Doe")
                .email("John@Doe.com")
                .birthdate(LocalDate.of(1990,1,1))
                .passportSeries("1234")
                .passportNumber("123456")
                .build();

        validEmployment = EmploymentDto.builder()
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .employerINN("123456789101")
                .salary(new BigDecimal("50000"))
                .position(WorkPosition.MIDDLE)
                .workExperienceTotal(22)
                .workExperienceCurrent(19)
                .build();

        validFinishRegistration = FinishRegistrationRequestDto.builder()
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.NOT_MARRIED)
                .dependentAmount(1)
                .passportIssueDate(LocalDate.of(2004, 1, 1))
                .passportIssueBranch("Branch which issued the passport")
                .employment(validEmployment)
                .accountNumber("12315124")
                .build();
    }

    @Test
    void findByIdThrowsWhenGivenWrongId() {
        assertThrowsExactly(EntityNotFoundException.class,
                            () -> clientService.findById(UUID.randomUUID()));
    }

    @Test
    void createClientDoesCreatesClient() {
        ClientEntity saved = clientService.createClientWithRequest(validRequest);

        ClientEntity found = clientService.findById(saved.getClientId());

        assertTrue(found.getClientId() == saved.getClientId());
        assertEquals(found.getFirstName(), validRequest.getFirstName());
        assertEquals(found.getMiddleName(), validRequest.getMiddleName());
        assertEquals(found.getLastName(), validRequest.getLastName());
        assertEquals(found.getEmail(), validRequest.getEmail());
        assertEquals(found.getBirthDate(), validRequest.getBirthdate());
        assertEquals(found.getPassport().getSeries(), validRequest.getPassportSeries());
        assertEquals(found.getPassport().getNumber(), validRequest.getPassportNumber());
    }

    @Test
    void enrichClientThrowsWhenGivenWrongId() {
        assertThrowsExactly(EntityNotFoundException.class,
                            () -> clientService.enrichClient(validFinishRegistration,
                                                             UUID.randomUUID()));
    }

    @Test
    void doesEnrichClientWhenGivenValidData() {
        ClientEntity saved = clientService.createClientWithRequest(validRequest);

        clientService.enrichClient(validFinishRegistration, saved.getClientId());

        ClientEntity found = clientService.findById(saved.getClientId());

        assertEquals(validFinishRegistration.getGender(), found.getGender());
        assertEquals(validFinishRegistration.getDependentAmount(), found.getDependentAmount());
        assertEquals(validFinishRegistration.getEmployment(), found.getEmployment());
        assertEquals(validFinishRegistration.getMaritalStatus(), found.getMaritalStatus());
        assertEquals(validFinishRegistration.getPassportIssueDate(), found.getPassport().getIssueDate());
        assertEquals(validFinishRegistration.getPassportIssueBranch(), found.getPassport().getIssueBranch());
    }

}
