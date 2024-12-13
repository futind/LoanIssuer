package ru.neoflex.mscalculator.controller;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Validator;
import ru.neoflex.mscalculator.dto.*;
import ru.neoflex.mscalculator.dto.enumeration.EmploymentStatus;
import ru.neoflex.mscalculator.dto.enumeration.Gender;
import ru.neoflex.mscalculator.dto.enumeration.MaritalStatus;
import ru.neoflex.mscalculator.dto.enumeration.WorkPosition;
import ru.neoflex.mscalculator.service.CalculatorService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class CalculatorControllerTest {

    @Mock
    private CalculatorService calculatorService;

    @InjectMocks
    private CalculatorController calculatorController;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private LoanStatementRequestDto validRequest;
    private EmploymentDto validEmployment;
    private ScoringDataDto validScoringData;

    @Autowired
    private Validator validator;

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

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("При слишком маленькой сумме кредита валидация не будет пройдена")
    void validationForRequestFailsWhenPassingWrongAmount() throws Exception {
        validRequest.setAmount(new BigDecimal("10000"));
        String requestJson = objectMapper.writeValueAsString(validRequest);

        this.mockMvc.perform(post("/calculator/offers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При слишком маленьком сроке кредита валидация не будет пройдена")
    void validationForRequestFailsWhenPassingWrongTerm() throws Exception {
        validRequest.setTerm(2);
        String requestJson = objectMapper.writeValueAsString(validRequest);

        this.mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При неправильном заполнении имени валидация не будет пройдена")
    void validationForRequestFailsWhenPassingWrongFirstName() throws Exception {
        validRequest.setFirstName("J");
        String requestJson = objectMapper.writeValueAsString(validRequest);

        this.mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При неправильном заполнении фамилии валидация не будет пройдена")
    void validationForRequestFailsWhenPassingWrongLastName() throws Exception {
        validRequest.setLastName("D82157itsnotaname");
        String requestJson = objectMapper.writeValueAsString(validRequest);

        this.mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При неправильном заполнении электронной почты валидация не будет пройдена")
    void validationForRequestFailsWhenPassingWrongEmail() throws Exception {
        validRequest.setEmail("not.an.email@");
        String requestJson = objectMapper.writeValueAsString(validRequest);

        this.mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При возрасте меньше 18 валидация не будет пройдена")
    void validationForRequestFailsWhenPassingWrongAge() throws Exception {
        validRequest.setBirthdate(LocalDate.now().minusYears(10));
        String requestJson = objectMapper.writeValueAsString(validRequest);

        this.mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При неправильно заполненной серии паспорта валидация не будет пройдена")
    void validationForRequestFailsWhenPassingWrongPassportSeries() throws Exception {
        validRequest.setPassportSeries("123456");
        String requestJson = objectMapper.writeValueAsString(validRequest);

        this.mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При неправильно заполненном номере паспорта валидация не будет пройдена")
    void validationForRequestFailsWhenPassingWrongPassportNumber() throws Exception {
        validRequest.setPassportNumber("1234");
        String requestJson = objectMapper.writeValueAsString(validRequest);

        this.mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("Когда передаёт в getCredit() валидный ScoringDataDto, получим Ok")
    void gettingOkWhenPassingValidRequest() throws Exception {

        this.mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    @DisplayName("При неправильном заполнении имени валидация не будет пройдена - calc")
    void validationFailsWhenPassingWrongFirstName() throws Exception {
        validScoringData.setFirstName("J1244");

        this.mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScoringData)))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При неправильном заполнении фамилии валидация не будет пройдена - calc")
    void validationFailsWhenPassingWrongLastName() throws Exception {
        validScoringData.setLastName("xDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");

        this.mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScoringData)))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При неправильно заполненной серии паспорта валидация не будет пройдена - calc")
    void validationFailsWhenPassingWrongPassportSeries() throws Exception {
        validScoringData.setPassportSeries("аааа");

        this.mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScoringData)))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При неправильно заполненном номере паспорта валидация не будет пройдена - calc")
    void validationFailsWhenPassingWrongPassportNumber() throws Exception {
        validScoringData.setPassportNumber("бббббб");

        this.mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScoringData)))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("При передаче возраста меньше 18 валидация не будет пройдена - calc")
    void validationFailsWhenPassingWrongAge() throws Exception {
        validScoringData.setBirthdate(LocalDate.now().minusYears(10));

        this.mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScoringData)))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("""
            При подаче в getCredit() валидных, но неподходящих банку значений будет \
            выброшен CreditDeniedException и вернётся статус 403""")
    void gettingForbiddenWhenPassingValidScoringDataWithIncorrectFields() throws Exception {

        validScoringData.getEmployment().setEmploymentStatus(EmploymentStatus.NOT_EMPLOYED);

        this.mockMvc.perform(post("/calculator/calc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validScoringData)))
                .andExpect(
                        status().isForbidden()
                );
    }
}
