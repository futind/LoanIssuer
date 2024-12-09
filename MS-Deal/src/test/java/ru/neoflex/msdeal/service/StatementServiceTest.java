package ru.neoflex.msdeal.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import ru.neoflex.msdeal.dto.*;
import ru.neoflex.msdeal.dto.enumeration.*;
import ru.neoflex.msdeal.model.ClientEntity;
import ru.neoflex.msdeal.model.StatementEntity;
import ru.neoflex.msdeal.repository.ClientRepository;

import javax.swing.plaf.nimbus.State;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class StatementServiceTest {

    @Autowired
    private StatementService statementService;
    @Autowired
    private ClientRepository clientRepository;


    @BeforeTestMethod("")

    @Test
    void findByIdThrowsWhenGivenWrongId() {
        assertThrowsExactly(EntityNotFoundException.class,
                () -> statementService.findById(UUID.randomUUID()));
    }

    @Test
    void createStatementWithRequestDoesCreateAStatement() {
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setFirstName("John");
        clientEntity.setLastName("Doe");
        clientEntity.setEmail("john@doe.com");
        clientEntity.setBirthDate(LocalDate.of(1970, 1, 1));
        clientEntity.setPassport(PassportDto.builder().series("1234").number("123456").build());

        clientRepository.save(clientEntity);

        StatementEntity saved = statementService.createStatementWithRequest(clientEntity);

        StatementEntity found = statementService.findById(saved.getStatementId());

        assertEquals(found.getStatementId(), saved.getStatementId());
        assertEquals(found.getClient().getClientId(), clientEntity.getClientId());
        assertEquals(found.getStatus(), ApplicationStatus.PREAPPROVAL);
        assertEquals(found.getStatusHistory().size(), 1);
        assertEquals(found.getStatusHistory().get(0).getStatus(), ApplicationStatus.PREAPPROVAL);
    }

    @Test
    void changeStatementStatusChangesTheStatusAndAddsEntryToTheHistory() {
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setFirstName("John");
        clientEntity.setLastName("Doe");
        clientEntity.setEmail("john@doe.com");
        clientEntity.setBirthDate(LocalDate.of(1970, 1, 1));
        clientEntity.setPassport(PassportDto.builder().series("1234").number("123456").build());

        clientRepository.save(clientEntity);

        StatementEntity statementEntity = statementService.createStatementWithRequest(clientEntity);

        statementEntity.setStatus(ApplicationStatus.PREAPPROVAL);

        statementService.changeStatementStatus(statementEntity, ApplicationStatus.APPROVED);

        assertEquals(statementEntity.getStatus(), ApplicationStatus.APPROVED);
        assertEquals(2, statementEntity.getStatusHistory().size());
        assertEquals(statementEntity.getStatusHistory().get(0).getStatus(), ApplicationStatus.PREAPPROVAL);
    }

    @Test
    void setAppliedOfferDoesSetIt() {
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setFirstName("John");
        clientEntity.setLastName("Doe");
        clientEntity.setEmail("john@doe.com");
        clientEntity.setBirthDate(LocalDate.of(1970, 1, 1));
        clientEntity.setPassport(PassportDto.builder().series("1234").number("123456").build());

        clientRepository.save(clientEntity);

        StatementEntity statementEntity = statementService.createStatementWithRequest(clientEntity);

        LoanOfferDto offer = LoanOfferDto.builder()
                .statementId(statementEntity.getStatementId())
                .requestedAmount(new BigDecimal("100000"))
                .totalAmount(new BigDecimal("136058.31"))
                .term(18)
                .monthlyPayment(new BigDecimal("7558.80"))
                .rate(new BigDecimal("0.22"))
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .build();

        statementEntity = statementService.setAppliedOffer(offer);

        assertEquals(ApplicationStatus.APPROVED, statementEntity.getStatus());
        assertEquals(offer, statementEntity.getAppliedOffer());
    }

    @Test
    void enrichScoringDataReturnsCorrectData() {

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setFirstName("John");
        clientEntity.setLastName("Doe");
        clientEntity.setEmail("john@doe.com");
        clientEntity.setBirthDate(LocalDate.of(1970, 1, 1));
        clientEntity.setPassport(PassportDto.builder()
                .series("1234")
                .number("123456")
                .build());

        clientRepository.save(clientEntity);

        StatementEntity statementEntity = statementService.createStatementWithRequest(clientEntity);

        LoanOfferDto validOffer = LoanOfferDto.builder()
                .statementId(statementEntity.getStatementId())
                .requestedAmount(new BigDecimal("100000"))
                .totalAmount(new BigDecimal("136058.31"))
                .term(18)
                .monthlyPayment(new BigDecimal("7558.80"))
                .rate(new BigDecimal("0.22"))
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .build();

        statementService.setAppliedOffer(validOffer);

        EmploymentDto validEmployment = EmploymentDto.builder()
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .employerINN("1234567890")
                .salary(new BigDecimal("100000"))
                .position(WorkPosition.MIDDLE)
                .workExperienceTotal(30)
                .workExperienceCurrent(15)
                .build();

        FinishRegistrationRequestDto validFinishingRequest = FinishRegistrationRequestDto.builder()
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.NOT_MARRIED)
                .dependentAmount(1)
                .passportIssueDate(LocalDate.now().minusYears(10))
                .passportIssueBranch("passport issue branch")
                .employment(validEmployment)
                .accountNumber("123456789")
                .build();

        ScoringDataDto scoringData = statementService.enrichScoringData(validFinishingRequest,
                                                                        statementEntity.getStatementId());

        assertTrue((scoringData.getAmount().subtract(validOffer.getRequestedAmount()))
                .abs()
                .compareTo(new BigDecimal("0.01")) < 0);
        assertEquals(scoringData.getTerm(), validOffer.getTerm());
        assertEquals(scoringData.getFirstName(), clientEntity.getFirstName());
        assertEquals(scoringData.getLastName(), clientEntity.getLastName());
        assertEquals(scoringData.getMiddleName(), clientEntity.getMiddleName());
        assertEquals(scoringData.getGender(), validFinishingRequest.getGender());
        assertEquals(scoringData.getBirthdate(), clientEntity.getBirthDate());
        assertEquals(scoringData.getPassportSeries(), clientEntity.getPassport().getSeries());
        assertEquals(scoringData.getPassportNumber(), clientEntity.getPassport().getNumber());
        assertEquals(scoringData.getPassportIssueDate(), validFinishingRequest.getPassportIssueDate());
        assertEquals(scoringData.getPassportIssueBranch(), validFinishingRequest.getPassportIssueBranch());
        assertEquals(scoringData.getMaritalStatus(), validFinishingRequest.getMaritalStatus());
        assertEquals(scoringData.getDependentAmount(), validFinishingRequest.getDependentAmount());
        assertEquals(scoringData.getEmployment(), validEmployment);
        assertEquals(scoringData.getAccountNumber(), validFinishingRequest.getAccountNumber());
        assertEquals(scoringData.getIsInsuranceEnabled(), validOffer.getIsInsuranceEnabled());
        assertEquals(scoringData.getIsSalaryClient(), validOffer.getIsSalaryClient());
    }

}
