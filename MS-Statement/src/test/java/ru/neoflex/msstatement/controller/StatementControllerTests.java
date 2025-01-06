package ru.neoflex.msstatement.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
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
import ru.neoflex.loanissuerlibrary.dto.LoanOfferDto;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;
import ru.neoflex.loanissuerlibrary.exception.PrescoringFailedException;
import ru.neoflex.msstatement.service.PrescoringService;
import ru.neoflex.msstatement.service.RestClientService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StatementControllerTests {

    @Mock
    private RestClientService restClientService;

    @Mock
    private PrescoringService prescoringService;

    @InjectMocks
    private StatementController statementController;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private LoanStatementRequestDto validRequest;
    private LoanOfferDto validOffer;
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

        validOffer = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(new BigDecimal("100000"))
                .totalAmount(new BigDecimal("200000"))
                .term(6)
                .monthlyPayment(new BigDecimal("10000"))
                .rate(new BigDecimal("0.25"))
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .build();

        validOfferList = List.of(
                validOffer,
                validOffer,
                validOffer,
                validOffer
        );

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(statementController).build();
    }

    @Test
    void whenClientCantPassPrescoringGetOffersReturns406() throws Exception {
        validRequest.setEmail("john@doe.com");
        doThrow(PrescoringFailedException.class)
                .when(prescoringService).prescore(any(LoanStatementRequestDto.class));

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotAcceptable());

        verify(prescoringService, times(1)).prescore(validRequest);
        verify(restClientService, never()).getOffers(validRequest);
    }

    @Test
    void whenClientPassesThePrescoringGetOffersReturns200AndAListOfOffers() throws Exception {
        validRequest.setEmail("john@doe.ru");

        doNothing().when(prescoringService).prescore(validRequest);
        when(restClientService.getOffers(validRequest)).thenReturn(validOfferList);

        MvcResult mvcResult = mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andReturn();

        List<LoanOfferDto> responseList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<LoanOfferDto>>() {});

        assertNotNull(responseList);
        assertEquals(4, responseList.size());
    }

    @Test
    void selectOfferWhenPassedValidOfferReturns200() throws Exception {

        doNothing().when(restClientService).selectOffer(validOffer);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOffer)))
                .andExpect(status().isOk());
    }

}
