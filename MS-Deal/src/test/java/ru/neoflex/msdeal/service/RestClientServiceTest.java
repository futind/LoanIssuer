package ru.neoflex.msdeal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.loanissuerlibrary.dto.*;
import ru.neoflex.loanissuerlibrary.dto.enumeration.EmploymentStatus;
import ru.neoflex.loanissuerlibrary.dto.enumeration.Gender;
import ru.neoflex.loanissuerlibrary.dto.enumeration.MaritalStatus;
import ru.neoflex.loanissuerlibrary.dto.enumeration.WorkPosition;
import ru.neoflex.loanissuerlibrary.exception.CreditDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class RestClientServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @InjectMocks
    private RestClientService restClientService;

    private LoanStatementRequestDto validRequest;
    private ScoringDataDto validScoringData;
    private List<LoanOfferDto> validOfferList;
    private CreditDto validCredit;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        validRequest = LoanStatementRequestDto.builder()
                .amount(new BigDecimal("100000"))
                .term(6)
                .firstName("John")
                .lastName("Doe")
                .email("John@Doe.com")
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("123456")
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

        validScoringData = ScoringDataDto.builder()
                .amount(new BigDecimal("100000"))
                .term(6)
                .firstName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("123456")
                .passportIssueDate(LocalDate.of(2004, 1, 1))
                .passportIssueBranch("passport issue branch")
                .maritalStatus(MaritalStatus.NOT_MARRIED)
                .dependentAmount(1)
                .employment(EmploymentDto.builder()
                        .employmentStatus(EmploymentStatus.EMPLOYED)
                        .employerINN("1234567890")
                        .salary(new BigDecimal("50000"))
                        .position(WorkPosition.MIDDLE)
                        .workExperienceTotal(30)
                        .workExperienceCurrent(29)
                        .build())
                .accountNumber("123456789")
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .build();

        validCredit = CreditDto.builder()
                .amount(new BigDecimal("100000"))
                .term(18)
                .monthlyPayment(new BigDecimal("8419.22"))
                .rate(new BigDecimal("0.25"))
                .psk(new BigDecimal("151545.89"))
                .isInsuranceEnabled(false)
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
    }

    @Test
    void getLoanOffersReturnsOffersBasedOnTheRequest() {
        when(restClient.post()
                .uri(any(String.class))
                .contentType(MediaType.APPLICATION_JSON)
                .body(validRequest)
                .retrieve()
                .body(any(ParameterizedTypeReference.class))
        ).thenReturn(validOfferList);

        List<LoanOfferDto> returnedOffers = restClientService.getLoanOffers(validRequest);

        assertEquals(validOfferList.get(0), returnedOffers.get(0));
        assertEquals(validOfferList.get(1), returnedOffers.get(1));
        assertEquals(validOfferList.get(2), returnedOffers.get(2));
        assertEquals(validOfferList.get(3), returnedOffers.get(3));
    }

    @Test
    void getLoanOffersWithIncorrectRequestThrowsRestClientResponseException() {
        validRequest.setAmount(new BigDecimal("1"));

        when(restClient.post()
                .uri(any(String.class))
                .contentType(MediaType.APPLICATION_JSON)
                .body(validRequest)
                .retrieve()
                .body(any(ParameterizedTypeReference.class)))
                .thenThrow(RestClientResponseException.class);

        assertThrows(RestClientResponseException.class, () -> restClientService.getLoanOffers(validRequest));
    }

    @Test
    void getCreditWithUnsuitableScoringDataThrowsRestClientResponseExceptionWith403() {
        validScoringData.getEmployment().setEmploymentStatus(EmploymentStatus.NOT_EMPLOYED);

        when(restClient.post()
                .uri(any(String.class))
                .contentType(MediaType.APPLICATION_JSON)
                .body(validScoringData)
                .retrieve()
                .body(CreditDto.class))
                .thenThrow(new RestClientResponseException("msg", HttpStatus.FORBIDDEN, "statustxt", null, null, null));

        assertThrows(RestClientResponseException.class, () -> restClientService.getCredit(validScoringData));
    }

    @Test
    void getCreditWithIncorrectScoringDataThrowsRestClientResponseException() {
        validScoringData.setAmount(new BigDecimal("1"));

        when(restClient.post()
                .uri(any(String.class))
                .contentType(MediaType.APPLICATION_JSON)
                .body(validScoringData)
                .retrieve()
                .body(CreditDto.class))
                .thenThrow(RestClientResponseException.class);

        assertThrows(RestClientResponseException.class, () -> restClientService.getCredit(validScoringData));
    }
}
