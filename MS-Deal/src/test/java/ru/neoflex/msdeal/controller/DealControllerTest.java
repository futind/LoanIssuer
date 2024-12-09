package ru.neoflex.msdeal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import ru.neoflex.msdeal.dto.*;
import ru.neoflex.msdeal.dto.enumeration.*;
import ru.neoflex.msdeal.service.ClientService;
import ru.neoflex.msdeal.service.CreditService;
import ru.neoflex.msdeal.service.StatementService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class DealControllerTest {

    @Mock
    private CreditService creditService;

    @Mock
    private StatementService statementService;

    @Mock
    private ClientService clientService;

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

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }


    // select
    @Test
    @DisplayName("""
            При отправке валидного оффера на /deal/offer/select вернётся Ok, и \
            метод setAppliedOffer из StatementService будет вызван один раз.""")
    void selectValidOfferSetsOffer() throws Exception {

        mockMvc = MockMvcBuilders.standaloneSetup(dealController).build();

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoanOffer)))
                .andExpect(status().isOk());

        verify(statementService, times(1)).setAppliedOffer(validLoanOffer);
    }

}