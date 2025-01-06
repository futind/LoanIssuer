package ru.neoflex.msstatement.service;

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
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.loanissuerlibrary.dto.LoanOfferDto;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestClientServiceTests {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @InjectMocks
    private RestClientService restClientService;

    private LoanStatementRequestDto validRequest;
    private List<LoanOfferDto> validOfferList;
    private LoanOfferDto validOffer;

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

        validOffer = LoanOfferDto.builder()
                .statementId(UUID.fromString("be444f33-f8a2-478c-b4eb-6069f9076d5d"))
                .requestedAmount(new BigDecimal("100000"))
                .totalAmount(new BigDecimal("120945.27"))
                .term(18)
                .monthlyPayment(new BigDecimal("6719.18"))
                .rate(new BigDecimal("0.25"))
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .build();

        validOfferList = List.of(
                validOffer,
                validOffer,
                validOffer,
                validOffer
        );
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

        List<LoanOfferDto> returnedOffers = restClientService.getOffers(validRequest);

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

        assertThrows(RestClientResponseException.class, () -> restClientService.getOffers(validRequest));
    }

    @Test
    void selectOfferThrowsGivenOfferWithIncorrectStatementId() {
        validOffer.setStatementId(UUID.randomUUID());

        when(restClient.post()
                .uri(any(String.class))
                .contentType(MediaType.APPLICATION_JSON)
                .body(validOffer))
                .thenThrow(RestClientResponseException.class);

        assertThrows(RestClientResponseException.class, () -> restClientService.selectOffer(validOffer));
    }

}
