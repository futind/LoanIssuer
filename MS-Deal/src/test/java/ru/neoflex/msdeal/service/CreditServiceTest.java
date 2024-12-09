package ru.neoflex.msdeal.service;

import jakarta.persistence.EntityNotFoundException;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.neoflex.msdeal.dto.CreditDto;
import ru.neoflex.msdeal.dto.PaymentScheduleElementDto;
import ru.neoflex.msdeal.model.CreditEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class CreditServiceTest {

    @Autowired
    private CreditService creditService;

    private CreditDto validCredit;

    @BeforeEach
    public void setUp() {
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
    }

    @Test
    void findByIdThrowsWhenGivenWrongId() {
        assertThrowsExactly(EntityNotFoundException.class,
                () -> creditService.findById(UUID.randomUUID()));
    }

    @Test
    void saveCreditDoesSaveTheCredit() {
        CreditEntity saved = creditService.saveCredit(validCredit);

        CreditEntity found = creditService.findById(saved.getCreditId());

        assertTrue((validCredit.getAmount().subtract(found.getAmount())).abs()
                             .compareTo(new BigDecimal("0.01")) < 0);
        assertEquals(validCredit.getTerm(), found.getTerm());
        assertEquals(validCredit.getMonthlyPayment(), found.getMonthlyPayment());
        assertEquals(validCredit.getRate(), found.getRate());
        assertEquals(validCredit.getPsk(), found.getPsk());
        assertEquals(validCredit.getIsInsuranceEnabled(), found.getIsInsuranceEnabled());
        assertEquals(validCredit.getIsSalaryClient(), found.getIsSalaryClient());
        assertEquals(validCredit.getPaymentSchedule(), found.getPaymentSchedule());
    }


}
