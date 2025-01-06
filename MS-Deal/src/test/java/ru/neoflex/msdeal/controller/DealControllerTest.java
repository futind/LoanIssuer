package ru.neoflex.msdeal.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.neoflex.msdeal.dto.*;
import ru.neoflex.msdeal.dto.enumeration.*;
import ru.neoflex.msdeal.model.ClientEntity;
import ru.neoflex.msdeal.model.CreditEntity;
import ru.neoflex.msdeal.model.StatementEntity;
import ru.neoflex.msdeal.service.ClientService;
import ru.neoflex.msdeal.service.CreditService;
import ru.neoflex.msdeal.service.RestClientService;
import ru.neoflex.msdeal.service.StatementService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DealControllerTest {

    @Mock
    private CreditService creditService;

    @Mock
    private StatementService statementService;

    @Mock
    private ClientService clientService;

    @Mock
    private RestClientService restClientService;

    @InjectMocks
    private DealController dealController;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private LoanStatementRequestDto validRequest;
    private EmploymentDto validEmployment;
    private ScoringDataDto validScoringData;
    private LoanOfferDto validLoanOffer;
    private FinishRegistrationRequestDto validFinishRegistration;
    private CreditDto validCredit;
    private List<LoanOfferDto> validOfferList;

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
        mockMvc = MockMvcBuilders.standaloneSetup(dealController).build();
    }

    @Test
    void whenGivenValidRequestReturnsValidListOfSize4WithTheSameStatementId() throws Exception {
        ClientEntity clientEntity = new ClientEntity();
        StatementEntity statementEntity = new StatementEntity();
        UUID statementId = UUID.randomUUID();
        statementEntity.setStatementId(statementId);

        when(clientService.createClientWithRequest(validRequest)).thenReturn(clientEntity);
        when(statementService.createStatementWithClient(clientEntity)).thenReturn(statementEntity);
        when(restClientService.getLoanOffers(validRequest)).thenReturn(validOfferList);

        MvcResult mvcResult = mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andReturn();

        List<LoanOfferDto> responseList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                                                                new TypeReference<List<LoanOfferDto>>() {});

        assertNotNull(responseList);
        assertEquals(4, responseList.size());
        assertEquals(statementId, responseList.get(0).getStatementId());
        assertEquals(statementId, responseList.get(1).getStatementId());
        assertEquals(statementId, responseList.get(2).getStatementId());
        assertEquals(statementId, responseList.get(3).getStatementId());
        verify(clientService, times(1)).createClientWithRequest(validRequest);
        verify(statementService, times(1)).createStatementWithClient(clientEntity);
        verify(restClientService, times(1)).getLoanOffers(validRequest);
    }

    // select
    @Test
    @DisplayName("""
            При отправке валидного оффера на /deal/offer/select вернётся Ok, и \
            метод setAppliedOffer из StatementService будет вызван один раз.""")
    void selectValidOfferSetsOffer() throws Exception {

        when(statementService.setAppliedOffer(validLoanOffer)).thenReturn(new StatementEntity());

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoanOffer)))
                .andExpect(status().isOk());

        verify(statementService, times(1)).setAppliedOffer(validLoanOffer);
    }

    @Test
    void finishRegistrationWithValidDataCorrectlyAssignsCredit() throws Exception {

        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(UUID.randomUUID());
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setClientId(UUID.randomUUID());
        CreditEntity creditEntity = new CreditEntity();
        creditEntity.setCreditId(UUID.randomUUID());

        statementEntity.setClient(clientEntity);

        when(statementService.findById(any(UUID.class))).thenReturn(statementEntity);
        when(clientService.enrichClient(validFinishRegistration, clientEntity.getClientId())).thenReturn(clientEntity);
        when(statementService.enrichScoringData(validFinishRegistration, statementEntity.getStatementId()))
                .thenReturn(validScoringData);
        when(restClientService.getCredit(validScoringData)).thenReturn(validCredit);
        when(creditService.saveCredit(validCredit)).thenReturn(creditEntity);
        when(statementService.setCredit(statementEntity.getStatementId(), creditEntity)).thenReturn(statementEntity);
        doNothing().when(statementService).changeStatementStatus(statementEntity, ApplicationStatus.CC_APPROVED);

        mockMvc.perform(post("/deal/calculate/" + statementEntity.getStatementId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFinishRegistration)))
                .andExpect(status().isOk());

        verify(statementService, times(1)).findById(any(UUID.class));
        verify(clientService, times(1)).enrichClient(validFinishRegistration,
                                                                            clientEntity.getClientId());
        verify(statementService, times(1)).enrichScoringData(validFinishRegistration,
                                                                                     statementEntity.getStatementId());
        verify(restClientService, times(1)).getCredit(validScoringData);
        verify(statementService, times(1)).setCredit(statementEntity.getStatementId(),
                                                                             creditEntity);
        verify(statementService, times(1)).changeStatementStatus(statementEntity,
                                                                                         ApplicationStatus.CC_APPROVED);

    }

    @Test
    void finishRegistrationThrowsCreditDeniedExceptionWhenPassedUnsuitableScoringData() throws Exception {
        StatementEntity statementEntity = new StatementEntity();
        statementEntity.setStatementId(UUID.randomUUID());
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setClientId(UUID.randomUUID());

        statementEntity.setClient(clientEntity);

        when(statementService.findById(any(UUID.class))).thenReturn(statementEntity);
        when(clientService.enrichClient(validFinishRegistration, clientEntity.getClientId())).thenReturn(clientEntity);
        when(statementService.enrichScoringData(validFinishRegistration, statementEntity.getStatementId()))
                .thenReturn(validScoringData);
        when(restClientService.getCredit(validScoringData)).thenThrow(new CreditDeniedException("Credit denied"));
        doNothing().when(statementService).changeStatementStatus(statementEntity, ApplicationStatus.CC_DENIED);

        mockMvc.perform(post("/deal/calculate/" + statementEntity.getStatementId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFinishRegistration)))
                .andExpect(status().isForbidden());

        verify(statementService, times(1)).findById(any(UUID.class));
        verify(clientService, times(1)).enrichClient(validFinishRegistration,
                clientEntity.getClientId());
        verify(statementService, times(1)).enrichScoringData(validFinishRegistration,
                statementEntity.getStatementId());
        verify(restClientService, times(1)).getCredit(validScoringData);
        verify(creditService, never()).saveCredit(validCredit);
        verify(statementService, never()).setCredit(any(UUID.class), any(CreditEntity.class));
        verify(statementService, times(1)).changeStatementStatus(statementEntity,
                                                                                         ApplicationStatus.CC_DENIED);
        verify(statementService, never()).changeStatementStatus(statementEntity, ApplicationStatus.CC_APPROVED);
    }

}