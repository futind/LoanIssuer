package ru.neoflex.msdeal.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.neoflex.loanissuerlibrary.dto.*;
import ru.neoflex.loanissuerlibrary.dto.enumeration.*;
import ru.neoflex.msdeal.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DealControllerTest {

    @Mock
    private DealService dealService;

    @InjectMocks
    private DealController dealController;


    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private LoanStatementRequestDto validRequest;
    private EmploymentDto validEmployment;
    private LoanOfferDto validLoanOffer;
    private FinishRegistrationRequestDto validFinishRegistration;
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
    void whenGivenValidRequestCallsValidMethodAndReturnsListOfSize4() throws Exception {
        UUID statementId = UUID.randomUUID();
        validOfferList.stream().forEach(offer -> offer.setStatementId(statementId));

        when(dealService.createStatementGetOffers(validRequest)).thenReturn(validOfferList);

        MvcResult mvcResult = mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andReturn();

        List<LoanOfferDto> responseList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<LoanOfferDto>>() {});

        assertEquals(4, responseList.size());
        assertEquals(statementId, responseList.get(0).getStatementId());
        assertEquals(statementId, responseList.get(1).getStatementId());
        assertEquals(statementId, responseList.get(2).getStatementId());
        assertEquals(statementId, responseList.get(3).getStatementId());

        verify(dealService, times(1)).createStatementGetOffers(validRequest);
    }

    @Test
    void selectCallsValidMethod() throws Exception {
        doNothing().when(dealService).applyOffer(validLoanOffer);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoanOffer)))
                .andExpect(status().isOk());

        verify(dealService, times(1)).applyOffer(validLoanOffer);
    }

    @Test
    void finishRegistrationCallsValidMethod() throws Exception {
        UUID statementId = UUID.randomUUID();

        doNothing().when(dealService).registrationCalculation(validFinishRegistration, statementId);

        mockMvc.perform(post("/deal/calculate/" + statementId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFinishRegistration)))
                .andExpect(status().isOk())
                .andReturn();

        verify(dealService, times(1)).registrationCalculation(validFinishRegistration, statementId);
    }

    @Test
    void sendDocumentsCallsValidMethod() throws Exception {
        UUID statementId = UUID.randomUUID();

        doNothing().when(dealService).sendDocumentEventAndStatus(statementId);

        mockMvc.perform(post("/deal/document/" + statementId.toString() + "/send"))
                .andExpect(status().isOk())
                .andReturn();

        verify(dealService, times(1)).sendDocumentEventAndStatus(statementId);
    }

    @Test
    void signDocumentCallsValidMethod() throws Exception {
        UUID statementId = UUID.randomUUID();

        doNothing().when(dealService).signDocumentEvent(statementId);

        mockMvc.perform(post("/deal/document/" + statementId.toString() + "/sign"))
                .andExpect(status().isOk())
                .andReturn();

        verify(dealService, times(1)).signDocumentEvent(statementId);
    }

    @Test
    void signingCodeCallsValidMethod() throws Exception {
        UUID statementId = UUID.randomUUID();
        String SesCode = "123456";

        doNothing().when(dealService).SesCodeVerificationEvent(statementId, SesCode);

        mockMvc.perform(post("/deal/document/" + statementId.toString() + "/code")
                        .param("SesCode", SesCode))
                .andExpect(status().isOk())
                .andReturn();

        verify(dealService, times(1)).SesCodeVerificationEvent(statementId, SesCode);
    }

    @Test
    void documentCreatedStatusChangeCallsValidMethod() throws Exception {
        UUID statementId = UUID.randomUUID();

        doNothing().when(dealService).documentCreatedStatusChange(statementId);

        mockMvc.perform(put("/deal/admin/statement/" + statementId.toString() + "/status"))
                .andExpect(status().isOk())
                .andReturn();

        verify(dealService, times(1)).documentCreatedStatusChange(statementId);
    }

    @Test
    void getDocumentDataCallsValidMethodAndReturnsDocumentDataDto() throws Exception {
        UUID statementId = UUID.randomUUID();

        when(dealService.formDocumentData(statementId)).thenReturn(DocumentDataDto.builder().build());

        MvcResult mvcResult = mockMvc.perform(get("/deal/document/" + statementId.toString() + "/data"))
                .andExpect(status().isOk())
                .andReturn();

        DocumentDataDto documentDataDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<DocumentDataDto>() {});

        verify(dealService, times(1)).formDocumentData(statementId);
    }
}