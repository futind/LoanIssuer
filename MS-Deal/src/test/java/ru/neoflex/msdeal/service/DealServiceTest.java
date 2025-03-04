package ru.neoflex.msdeal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.loanissuerlibrary.dto.*;
import ru.neoflex.loanissuerlibrary.dto.enumeration.*;
import ru.neoflex.loanissuerlibrary.exception.CreditDeniedException;
import ru.neoflex.loanissuerlibrary.exception.StatementChangeBlocked;
import ru.neoflex.msdeal.model.ClientEntity;
import ru.neoflex.msdeal.model.CreditEntity;
import ru.neoflex.msdeal.model.StatementEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DealServiceTest {

    @Mock
    private ClientService clientService;

    @Mock
    private CreditService creditService;

    @Mock
    private StatementService statementService;

    @Mock
    private RestClientService restClientService;

    @Mock
    private KafkaSenderService kafkaSenderService;

    @Mock
    private UtilitiesService utilitiesService;

    @InjectMocks
    private DealService dealService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private LoanStatementRequestDto validRequest;
    private EmploymentDto validEmployment;
    private ScoringDataDto validScoringData;
    private LoanOfferDto validLoanOffer;
    private FinishRegistrationRequestDto validFinishRegistration;
    private CreditDto validCredit;
    private List<LoanOfferDto> validOfferList;

    @BeforeEach
    void setUp() throws Exception {
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
                .employerINN("1234567890")
                .salary(new BigDecimal("50000"))
                .position(WorkPosition.MIDDLE)
                .workExperienceTotal(22)
                .workExperienceCurrent(19)
                .build();

        validScoringData = ScoringDataDto.builder()
                .amount(new BigDecimal("531941"))
                .term(6)
                .firstName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("123456")
                .passportIssueDate(LocalDate.of(2004, 1, 1))
                .passportIssueBranch("Branch which issued the passport")
                .maritalStatus(MaritalStatus.NOT_MARRIED)
                .dependentAmount(0)
                .employment(validEmployment)
                .accountNumber("12315124")
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .build();

        validLoanOffer = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(new BigDecimal("100000"))
                .totalAmount(new BigDecimal("200000"))
                .term(6)
                .monthlyPayment(new BigDecimal("10000"))
                .rate(new BigDecimal("0.25"))
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
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

        validCredit = CreditDto.builder()
                .amount(new BigDecimal("100000"))
                .term(18)
                .monthlyPayment(new BigDecimal("8419.22"))
                .rate(new BigDecimal("0.20"))
                .psk(new BigDecimal("151545.89"))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .paymentSchedule(List.of(PaymentScheduleElementDto.builder()
                        .number(1)
                        .date(LocalDate.now().plusMonths(1))
                        .totalPayment(new BigDecimal("8419.22"))
                        .interestPayment(new BigDecimal("2166.67"))
                        .debtPayment(new BigDecimal("6252.55"))
                        .remainingDebt(new BigDecimal("123747.45"))
                        .build()))
                .build();

        validOfferList = List.of(
                LoanOfferDto.builder()
                        .statementId(UUID.fromString("be444f33-f8a2-478c-b4eb-6069f9076d5d"))
                        .requestedAmount(new BigDecimal("100000"))
                        .totalAmount(new BigDecimal("120945.27"))
                        .term(18)
                        .monthlyPayment(new BigDecimal("6719.18"))
                        .rate(new BigDecimal("0.25"))
                        .isInsuranceEnabled(false)
                        .isSalaryClient(false)
                        .build(),
                LoanOfferDto.builder()
                        .statementId(UUID.fromString("4a722bb8-6d57-4057-b44b-4d2223f1d6e2"))
                        .requestedAmount(new BigDecimal("100000"))
                        .totalAmount(new BigDecimal("120945.27"))
                        .term(18)
                        .monthlyPayment(new BigDecimal("6719.18"))
                        .rate(new BigDecimal("0.25"))
                        .isInsuranceEnabled(false)
                        .isSalaryClient(false)
                        .build(),
                LoanOfferDto.builder()
                        .statementId(UUID.fromString("9b7938e4-1211-415f-887e-9fc11ebee81e"))
                        .requestedAmount(new BigDecimal("100000"))
                        .totalAmount(new BigDecimal("120945.27"))
                        .term(18)
                        .monthlyPayment(new BigDecimal("6719.18"))
                        .rate(new BigDecimal("0.25"))
                        .isInsuranceEnabled(false)
                        .isSalaryClient(false)
                        .build(),
                LoanOfferDto.builder()
                        .statementId(UUID.fromString("975280aa-1d24-4e71-b1c7-8bfc83277abc"))
                        .requestedAmount(new BigDecimal("100000"))
                        .totalAmount(new BigDecimal("120945.27"))
                        .term(18)
                        .monthlyPayment(new BigDecimal("6719.18"))
                        .rate(new BigDecimal("0.25"))
                        .isInsuranceEnabled(false)
                        .isSalaryClient(false)
                        .build()
        );

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void createStatementGetOffersCallsValidMethodsAndReturnsListOfSize4() throws Exception {
        ClientEntity clientEntity = new ClientEntity();
        StatementEntity statementEntity = new StatementEntity();
        UUID statementId = UUID.randomUUID();
        statementEntity.setStatementId(statementId);

        when(clientService.createClientWithRequest(validRequest)).thenReturn(clientEntity);
        when(statementService.createStatementWithClient(clientEntity)).thenReturn(statementEntity);
        when(restClientService.getLoanOffers(validRequest)).thenReturn(validOfferList);

        List<LoanOfferDto> result = dealService.createStatementGetOffers(validRequest);

        assertEquals(4, result.size());
        verify(clientService, times(1)).createClientWithRequest(validRequest);
        verify(statementService, times(1)).createStatementWithClient(clientEntity);
        verify(restClientService, times(1)).getLoanOffers(validRequest);
    }

    @Test
    void applyOfferCallsValidMethods() throws Exception {
        String testEmail = "test@test.com";
        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(validLoanOffer.getStatementId());
        statementEntity.setClient(new ClientEntity());
        statementEntity.getClient().setEmail(testEmail);

        when(statementService.findClientByStatementId(validLoanOffer.getStatementId())).thenReturn(statementEntity.getClient());
        when(statementService.setAppliedOffer(validLoanOffer)).thenReturn(new StatementEntity());
        doNothing().when(kafkaSenderService).sendFinishRegistrationMessage(validLoanOffer.getStatementId(), testEmail);

        dealService.applyOffer(validLoanOffer);

        verify(statementService, times(1)).findClientByStatementId(validLoanOffer.getStatementId());
        verify(statementService, times(1)).setAppliedOffer(validLoanOffer);
        verify(kafkaSenderService, times(1)).sendFinishRegistrationMessage(validLoanOffer.getStatementId(),
                                                                                                    testEmail);
    }

    @Test
    void registrationCalculationCallsValidMethods() throws Exception {

        UUID statementId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(statementId);
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setClientId(clientId);
        clientEntity.setEmail("test@test.com");
        CreditEntity creditEntity = new CreditEntity();
        statementEntity.setCredit(creditEntity);
        statementEntity.setClient(clientEntity);

        when(statementService.isDenied(statementId)).thenReturn(false);
        when(statementService.findById(statementId)).thenReturn(statementEntity);
        when(clientService.enrichClient(validFinishRegistration, clientId)).thenReturn(clientEntity);
        when(statementService.enrichScoringData(validFinishRegistration, statementId)).thenReturn(validScoringData);
        when(restClientService.getCredit(validScoringData)).thenReturn(validCredit);
        when(creditService.saveCredit(validCredit)).thenReturn(creditEntity);
        when(statementService.setCredit(statementId, creditEntity)).thenReturn(statementEntity);
        doNothing().when(statementService).changeStatementStatus(statementEntity, ApplicationStatus.CC_APPROVED);
        doNothing().when(kafkaSenderService).sendCreateDocumentsMessage(statementId, statementEntity.getClient().getEmail());

        dealService.registrationCalculation(validFinishRegistration, statementId);

        verify(clientService, times(1)).enrichClient(validFinishRegistration, clientId);
        verify(statementService, times(1)).enrichScoringData(validFinishRegistration, statementId);
        verify(restClientService, times(1)).getCredit(validScoringData);
        verify(statementService, never()).changeStatementStatus(statementEntity, ApplicationStatus.CC_DENIED);
        verify(kafkaSenderService, never()).sendStatementDeniedMessage(statementId, statementEntity.getClient().getEmail());
        verify(creditService, times(1)).saveCredit(validCredit);
        verify(statementService, times(1)).changeStatementStatus(statementEntity,
                                                                                         ApplicationStatus.CC_APPROVED);
        verify(kafkaSenderService, times(1)).sendCreateDocumentsMessage(statementId,
                                                                                statementEntity.getClient().getEmail());
    }

    @Test
    void registrationCalculationThrowsAndSendsDeniedMessageIf403OnCalculator() throws Exception {
        UUID statementId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(statementId);
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setClientId(clientId);
        clientEntity.setEmail("test@test.com");
        CreditEntity creditEntity = new CreditEntity();
        statementEntity.setCredit(creditEntity);
        statementEntity.setClient(clientEntity);

        RestClientResponseException e = new RestClientResponseException("msg", 403, "status", HttpHeaders.EMPTY, null, null);

        validScoringData.getEmployment().setEmploymentStatus(EmploymentStatus.NOT_EMPLOYED);

        when(statementService.isDenied(statementId)).thenReturn(false);
        when(statementService.findById(statementId)).thenReturn(statementEntity);
        when(clientService.enrichClient(validFinishRegistration, clientId)).thenReturn(clientEntity);
        when(statementService.enrichScoringData(validFinishRegistration, statementId)).thenReturn(validScoringData);
        when(restClientService.getCredit(validScoringData)).thenThrow(e);
        doNothing().when(statementService).changeStatementStatus(statementEntity, ApplicationStatus.CC_DENIED);
        doNothing().when(kafkaSenderService).sendStatementDeniedMessage(statementId,
                                                                        statementEntity.getClient().getEmail());



        assertThrows(RestClientResponseException.class,
                     ()->dealService.registrationCalculation(validFinishRegistration, statementId));
        verify(clientService, times(1)).enrichClient(validFinishRegistration, clientId);
        verify(statementService, times(1)).enrichScoringData(validFinishRegistration, statementId);
        verify(restClientService, times(1)).getCredit(validScoringData);
        verify(creditService, never()).saveCredit(any(CreditDto.class));
        verify(statementService, never()).setCredit(statementId, creditEntity);
        verify(kafkaSenderService, never()).sendCreateDocumentsMessage(statementId,
                                                                       statementEntity.getClient().getEmail());
    }

    @Test
    void sendDocumentAndStatusCallsValidMethods() throws Exception {

        UUID statementId = UUID.randomUUID();
        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(statementId);
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setEmail("test@test.com");
        statementEntity.setClient(clientEntity);

        when(statementService.isDenied(statementId)).thenReturn(false);
        when(statementService.findById(statementId)).thenReturn(statementEntity);
        when(statementService.findClientByStatementId(statementId)).thenReturn(clientEntity);
        doNothing().when(statementService).changeStatementStatus(statementEntity, ApplicationStatus.PREPARE_DOCUMENTS);
        doNothing().when(kafkaSenderService).sendSendDocumentsMessage(statementId,
                                                                        statementEntity.getClient().getEmail());

        dealService.sendDocumentEventAndStatus(statementId);

        verify(statementService, times(1)).isDenied(statementId);
        verify(statementService, times(1)).findClientByStatementId(statementId);
        verify(statementService, times(1)).changeStatementStatus(statementEntity,
                                                                                         ApplicationStatus.PREPARE_DOCUMENTS);
        verify(kafkaSenderService, times(1)).sendSendDocumentsMessage(statementId,
                                                                                statementEntity.getClient().getEmail());
    }

    @Test
    void sesUpdateEventCallsValidMethods() throws Exception {
        UUID statementId = UUID.randomUUID();
        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(statementId);
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setEmail("test@test.com");
        statementEntity.setClient(clientEntity);
        String testSes = "123456";

        when(statementService.isDenied(statementId)).thenReturn(false);
        when(statementService.findClientByStatementId(statementId)).thenReturn(clientEntity);
        when(utilitiesService.generateSesCode()).thenReturn(testSes);
        when(statementService.updateSesCode(statementId, testSes)).thenReturn(statementEntity);
        when(statementService.getSesByStatementId(statementId)).thenReturn(testSes);
        doNothing().when(kafkaSenderService).sendSendSesMessage(statementId,
                                                                testSes,
                                                                statementEntity.getClient().getEmail());

        dealService.sesUpdateEvent(statementId);

        verify(statementService, times(1)).isDenied(statementId);
        verify(statementService, times(1)).findClientByStatementId(statementId);
        verify(utilitiesService, times(1)).generateSesCode();
        verify(statementService, times(1)).updateSesCode(statementId, testSes);
        verify(kafkaSenderService, times(1)).sendSendSesMessage(statementId,
                                                                                        testSes,
                                                                                        statementEntity.getClient().getEmail());
    }

    @Test
    void sesCodeVerificationCallsValidMethods() throws Exception {
        UUID statementId = UUID.randomUUID();
        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(statementId);
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setEmail("test@test.com");
        statementEntity.setClient(clientEntity);
        CreditEntity creditEntity = new CreditEntity();
        statementEntity.setCredit(creditEntity);

        String testSes = "123456";
        statementEntity.setSesCode(testSes);

        when(statementService.isDenied(statementId)).thenReturn(false);
        when(statementService.findById(statementId)).thenReturn(statementEntity);
        when(creditService.updateCreditStatus(statementEntity.getCredit().getCreditId())).thenReturn(creditEntity);
        doNothing().when(statementService).changeStatementStatus(statementEntity, ApplicationStatus.DOCUMENT_SIGNED);
        when(statementService.issueCredit(statementId)).thenReturn(statementEntity);
        doNothing().when(kafkaSenderService).sendCreditIssuedMessage(statementId, statementEntity.getClient().getEmail());

        dealService.sesCodeVerificationEvent(statementId, testSes);

        verify(statementService, times(1)).isDenied(statementId);
        verify(statementService, times(1)).findById(statementId);
        verify(creditService, times(1)).updateCreditStatus(statementEntity.getCredit().getCreditId());
        verify(statementService, times(1)).changeStatementStatus(statementEntity,
                                                                                         ApplicationStatus.DOCUMENT_SIGNED);
        verify(statementService, times(1)).issueCredit(statementId);
        verify(kafkaSenderService, times(1)).sendCreditIssuedMessage(statementId,
                                                                                         statementEntity.getClient().getEmail());
    }

    @Test
    void documentCreatedStatusChangeCallsValidMethods() throws Exception {
        UUID statementId = UUID.randomUUID();
        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(statementId);

        when(statementService.isDenied(statementId)).thenReturn(false);
        when(statementService.findById(statementId)).thenReturn(statementEntity);
        doNothing().when(statementService).changeStatementStatus(statementEntity, ApplicationStatus.DOCUMENT_CREATED);

        dealService.documentCreatedStatusChange(statementId);

        verify(statementService, times(1)).isDenied(statementId);
        verify(statementService, times(1)).findById(statementId);
        verify(statementService, times(1)).changeStatementStatus(statementEntity,
                                                                                         ApplicationStatus.DOCUMENT_CREATED);
    }

    @Test
    void formDocumentDataReturnsDataDto() throws Exception {
        UUID statementId = UUID.randomUUID();
        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(statementId);

        when(statementService.isDenied(statementId)).thenReturn(false);
        when(statementService.enrichDocumentData(statementId)).thenReturn(new DocumentDataDto());

        DocumentDataDto documentDataDto = dealService.formDocumentData(statementId);
        assertNotNull(documentDataDto);
    }

    @Test
    void throwIfStatementIsDeniedThrowsStatementChangeBlocked() throws Exception {
        UUID statementId = UUID.randomUUID();
        when(statementService.isDenied(statementId)).thenReturn(true);

        assertThrowsExactly(StatementChangeBlocked.class, () -> dealService.throwIfStatementIsDenied(statementId));
    }

    @Test
    void throwIfStatementIsDeniedDoesNotThrowIfStatementIsCorrect() throws Exception {
        UUID statementId = UUID.randomUUID();

        when(statementService.isDenied(statementId)).thenReturn(false);

        assertDoesNotThrow(() -> dealService.throwIfStatementIsDenied(statementId));
    }
}