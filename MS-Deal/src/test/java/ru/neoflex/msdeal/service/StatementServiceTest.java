package ru.neoflex.msdeal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.msdeal.dto.*;
import ru.neoflex.msdeal.dto.enumeration.*;
import ru.neoflex.msdeal.model.ClientEntity;
import ru.neoflex.msdeal.model.CreditEntity;
import ru.neoflex.msdeal.model.StatementEntity;
import ru.neoflex.msdeal.repository.StatementRepository;

import javax.swing.plaf.nimbus.State;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatementServiceTest {

    @Mock
    private StatementRepository statementRepository;

    @InjectMocks
    private StatementService statementService;

    private LoanStatementRequestDto validRequest;

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

    }

    @Test
    void changeStatementStatusChangesTheStatus() {
        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatus(null);
        statementEntity.setStatusHistory(null);

        ApplicationStatus status = ApplicationStatus.PREAPPROVAL;

        when(statementRepository.save(statementEntity)).thenReturn(statementEntity);

        statementService.changeStatementStatus(statementEntity, status);

        assertNotNull(statementEntity.getStatusHistory());
        assertEquals(1, statementEntity.getStatusHistory().size());
        assertEquals(status, statementEntity.getStatusHistory().get(0).getStatus());
        assertEquals(status, statementEntity.getStatus());
        verify(statementRepository, times(1)).save(statementEntity);
    }

    @Test
    void createStatementWithValidRequestSavesRightStatement() {
        StatementEntity statementEntity = new StatementEntity();
        ClientEntity clientEntity = new ClientEntity();

        statementEntity.setClient(clientEntity);
        statementEntity.setCreationDate(LocalDateTime.now());

        when(statementRepository.save(any(StatementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StatementEntity savedStatement = statementService.createStatementWithClient(clientEntity);

        assertEquals(clientEntity.getClientId(), savedStatement.getClient().getClientId());
        assertEquals(ApplicationStatus.PREAPPROVAL, savedStatement.getStatus());
        assertEquals(1, savedStatement.getStatusHistory().size());
        assertEquals(ApplicationStatus.PREAPPROVAL, savedStatement.getStatusHistory().get(0).getStatus());
        verify(statementRepository, times(2)).save(any(StatementEntity.class));
    }

    @Test
    void setAppliedOfferSetsTheOffer() throws JsonProcessingException {

        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(UUID.fromString("0467dde7-f431-43aa-aab4-d7c4f56cc365"));

        LoanOfferDto offer = LoanOfferDto.builder()
                .statementId(UUID.fromString("0467dde7-f431-43aa-aab4-d7c4f56cc365"))
                .requestedAmount(new BigDecimal("100000"))
                .totalAmount(new BigDecimal("154941.65"))
                .term(18)
                .monthlyPayment(new BigDecimal("8607.87"))
                .rate(new BigDecimal("0.23"))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();

        when(statementRepository.save(any(StatementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statementRepository.findById(statementEntity.getStatementId())).thenReturn(Optional.of(statementEntity));

        StatementEntity savedStatement = statementService.setAppliedOffer(offer);

        assertEquals(offer, savedStatement.getAppliedOffer());
        assertEquals(ApplicationStatus.APPROVED, statementEntity.getStatus());
        assertEquals(1, statementEntity.getStatusHistory().size());
        assertEquals(ApplicationStatus.APPROVED, statementEntity.getStatusHistory().get(0).getStatus());
        verify(statementRepository, times(2)).save(any(StatementEntity.class));
        verify(statementRepository, times(1)).findById(statementEntity.getStatementId());
    }

    @Test
    void enrichScoringDataReturnsCorrectScoringDataDto() {

        LoanOfferDto appliedOffer = LoanOfferDto.builder()
                .requestedAmount(BigDecimal.valueOf(100000))
                .term(24)
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();

        PassportDto passport = PassportDto.builder()
                .series("1234")
                .number("567890")
                .build();

        ClientEntity client = new ClientEntity();
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setMiddleName("M.");
        client.setBirthDate(LocalDate.of(1985, 5, 15));
        client.setPassport(passport);

        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setAppliedOffer(appliedOffer);
        statementEntity.setClient(client);

        FinishRegistrationRequestDto finishingRequest = FinishRegistrationRequestDto.builder()
                .gender(Gender.MALE)
                .passportIssueDate(LocalDate.of(2004, 1, 1))
                .passportIssueBranch("passport issue branch")
                .maritalStatus(MaritalStatus.NOT_MARRIED)
                .dependentAmount(1)
                .employment(EmploymentDto.builder()
                        .employmentStatus(EmploymentStatus.EMPLOYED)
                        .employerINN("1234567890")
                        .position(WorkPosition.MIDDLE)
                        .salary(new BigDecimal("50000"))
                        .workExperienceCurrent(29)
                        .workExperienceTotal(30)
                        .build())
                .accountNumber("1234567890")
                .build();

        when(statementRepository.findById(statementEntity.getStatementId())).thenReturn(Optional.of(statementEntity));

        ScoringDataDto returnedScoringData = statementService.enrichScoringData(finishingRequest, statementEntity.getStatementId());

        assertNotNull(returnedScoringData);
        assertEquals(appliedOffer.getRequestedAmount(), returnedScoringData.getAmount());
        assertEquals(appliedOffer.getTerm(), returnedScoringData.getTerm());
        assertEquals(client.getFirstName(), returnedScoringData.getFirstName());
        assertEquals(client.getLastName(), returnedScoringData.getLastName());
        assertEquals(client.getMiddleName(), returnedScoringData.getMiddleName());
        assertEquals(client.getBirthDate(), returnedScoringData.getBirthdate());
        assertEquals(finishingRequest.getGender(), returnedScoringData.getGender());
        assertEquals(finishingRequest.getPassportIssueDate(), returnedScoringData.getPassportIssueDate());
        assertEquals(finishingRequest.getPassportIssueBranch(), returnedScoringData.getPassportIssueBranch());
        assertEquals(client.getPassport().getSeries(), returnedScoringData.getPassportSeries());
        assertEquals(client.getPassport().getNumber(), returnedScoringData.getPassportNumber());
        assertEquals(finishingRequest.getMaritalStatus(), returnedScoringData.getMaritalStatus());
        assertEquals(finishingRequest.getDependentAmount(), returnedScoringData.getDependentAmount());
        assertEquals(finishingRequest.getEmployment(), returnedScoringData.getEmployment());
        assertEquals(finishingRequest.getAccountNumber(), returnedScoringData.getAccountNumber());
        assertEquals(appliedOffer.getIsInsuranceEnabled(), returnedScoringData.getIsInsuranceEnabled());
        assertEquals(appliedOffer.getIsSalaryClient(), returnedScoringData.getIsSalaryClient());

        verify(statementRepository, times(1)).findById(statementEntity.getStatementId());
    }

    @Test
    void setCreditSetsTheCredit() {

        StatementEntity statementEntity = new StatementEntity();
        CreditEntity creditEntity = new CreditEntity();

        when(statementRepository.findById(statementEntity.getStatementId())).thenReturn(Optional.of(statementEntity));
        when(statementRepository.save(any(StatementEntity.class))).thenReturn(statementEntity);

        StatementEntity savedStatement = statementService.setCredit(statementEntity.getStatementId(), creditEntity);

        assertNotNull(savedStatement.getCredit());
        assertEquals(creditEntity.getCreditId(), savedStatement.getCredit().getCreditId());
    }

}
