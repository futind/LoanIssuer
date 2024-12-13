package ru.neoflex.msdeal.service;

import ch.qos.logback.core.net.server.Client;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.neoflex.msdeal.dto.*;
import ru.neoflex.msdeal.dto.enumeration.EmploymentStatus;
import ru.neoflex.msdeal.dto.enumeration.Gender;
import ru.neoflex.msdeal.dto.enumeration.MaritalStatus;
import ru.neoflex.msdeal.dto.enumeration.WorkPosition;
import ru.neoflex.msdeal.model.ClientEntity;
import ru.neoflex.msdeal.repository.ClientRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
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
    void createClientWithValidRequestSavesRightClientEntity() {
        ClientEntity clientEntity = new ClientEntity();

        clientEntity.setFirstName(validRequest.getFirstName());
        clientEntity.setLastName(validRequest.getLastName());
        clientEntity.setMiddleName(validRequest.getMiddleName());
        clientEntity.setEmail(validRequest.getEmail());
        clientEntity.setBirthDate(validRequest.getBirthdate());

        clientEntity.setPassport(PassportDto.builder()
                .series(validRequest.getPassportSeries())
                .number(validRequest.getPassportNumber())
                .build());

        when(clientRepository.save(any(ClientEntity.class))).thenReturn(clientEntity);

        ClientEntity savedClientEntity = clientService.createClientWithRequest(validRequest);

        assertEquals(clientEntity.getFirstName(), savedClientEntity.getFirstName());
        assertEquals(clientEntity.getMiddleName(), savedClientEntity.getMiddleName());
        assertEquals(clientEntity.getLastName(), savedClientEntity.getLastName());
        assertEquals(clientEntity.getEmail(), savedClientEntity.getEmail());
        assertEquals(clientEntity.getBirthDate(), savedClientEntity.getBirthDate());
        assertEquals(clientEntity.getPassport(), savedClientEntity.getPassport());
    }

    @Test
    void enrichClientWithValidFinishRegistrationEnrichesClientCorrectly() {
        ClientEntity clientEntity = new ClientEntity();

        when(clientRepository.findById(clientEntity.getClientId())).thenReturn(Optional.of(clientEntity));
        when(clientRepository.save(any(ClientEntity.class))).thenReturn(clientEntity);

        clientEntity.setGender(validFinishRegistration.getGender());
        clientEntity.setMaritalStatus(validFinishRegistration.getMaritalStatus());
        clientEntity.setDependentAmount(validFinishRegistration.getDependentAmount());
        clientEntity.setAccountNumber(validFinishRegistration.getAccountNumber());
        clientEntity.setPassport(PassportDto.builder()
                .issueBranch(validFinishRegistration.getPassportIssueBranch())
                .issueDate(validFinishRegistration.getPassportIssueDate())
                .build());
        clientEntity.setEmployment(validFinishRegistration.getEmployment());

        ClientEntity savedClientEntity = clientService.enrichClient(validFinishRegistration, clientEntity.getClientId());

        assertEquals(validFinishRegistration.getGender(), savedClientEntity.getGender());
        assertEquals(validFinishRegistration.getMaritalStatus(), savedClientEntity.getMaritalStatus());
        assertEquals(validFinishRegistration.getDependentAmount(), savedClientEntity.getDependentAmount());
        assertEquals(validFinishRegistration.getAccountNumber(), savedClientEntity.getAccountNumber());
        assertEquals(validFinishRegistration.getPassportIssueBranch(), savedClientEntity.getPassport().getIssueBranch());
        assertEquals(validFinishRegistration.getPassportIssueDate(), savedClientEntity.getPassport().getIssueDate());
        assertEquals(validFinishRegistration.getEmployment(), savedClientEntity.getEmployment());
    }


}
