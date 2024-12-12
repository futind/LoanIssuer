package ru.neoflex.msdeal.service;

import jakarta.persistence.EntityNotFoundException;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.neoflex.msdeal.dto.CreditDto;
import ru.neoflex.msdeal.dto.PaymentScheduleElementDto;
import ru.neoflex.msdeal.dto.enumeration.CreditStatus;
import ru.neoflex.msdeal.model.CreditEntity;
import ru.neoflex.msdeal.repository.CreditRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreditServiceTest {

    @Mock
    CreditRepository creditRepository;

    @InjectMocks
    CreditService creditService;

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
    void saveCreditSavesTheCredit() {
        CreditEntity creditEntity = new CreditEntity();

        creditEntity.setAmount(validCredit.getAmount());
        creditEntity.setTerm(validCredit.getTerm());
        creditEntity.setMonthlyPayment(validCredit.getMonthlyPayment());
        creditEntity.setRate(validCredit.getRate());
        creditEntity.setPsk(validCredit.getPsk());
        creditEntity.setIsInsuranceEnabled(validCredit.getIsInsuranceEnabled());
        creditEntity.setIsSalaryClient(validCredit.getIsSalaryClient());
        creditEntity.setPaymentSchedule(validCredit.getPaymentSchedule());
        creditEntity.setCreditStatus(CreditStatus.CALCULATED);

        when(creditRepository.save(any(CreditEntity.class))).thenReturn(creditEntity);

        CreditEntity savedCreditEntity = creditService.saveCredit(validCredit);

        assertEquals(validCredit.getAmount(), savedCreditEntity.getAmount());
        assertEquals(validCredit.getTerm(), savedCreditEntity.getTerm());
        assertEquals(validCredit.getMonthlyPayment(), savedCreditEntity.getMonthlyPayment());
        assertEquals(validCredit.getRate(), savedCreditEntity.getRate());
        assertEquals(validCredit.getPsk(), savedCreditEntity.getPsk());
        assertEquals(validCredit.getIsInsuranceEnabled(), savedCreditEntity.getIsInsuranceEnabled());
        assertEquals(validCredit.getIsSalaryClient(), savedCreditEntity.getIsSalaryClient());
        assertEquals(validCredit.getPaymentSchedule(), savedCreditEntity.getPaymentSchedule());
        assertEquals(CreditStatus.CALCULATED, savedCreditEntity.getCreditStatus());
    }

}
