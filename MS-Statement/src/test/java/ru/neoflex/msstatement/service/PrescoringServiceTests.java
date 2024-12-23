package ru.neoflex.msstatement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.neoflex.msstatement.dto.LoanStatementRequestDto;
import ru.neoflex.msstatement.exception.PrescoringFailedException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PrescoringServiceTests {

    @Autowired
    private PrescoringService prescoringService;

    private LoanStatementRequestDto validRequest;

    @BeforeEach
    void setUp() throws PrescoringFailedException {
        validRequest = LoanStatementRequestDto.builder()
                .amount(new BigDecimal("100000"))
                .term(6)
                .firstName("John")
                .lastName("Doe")
                .birthdate(LocalDate.of(1970, 1,1))
                .passportSeries("1234")
                .passportNumber("123456")
                .email("john@doe.ru")
                .build();
    }

    @Test
    void passingEnglishEmailThrows() {
        validRequest.setEmail("john@doe.com");
        assertThrowsExactly(PrescoringFailedException.class, () -> prescoringService.prescore(validRequest));
    }

    @Test
    void passingRussianEmailDoesNotThrow() {
        validRequest.setEmail("john@doe.ru");
        assertDoesNotThrow(() -> prescoringService.prescore(validRequest));
    }
}
