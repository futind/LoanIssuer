package ru.neoflex.mscalculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.neoflex.loanissuerlibrary.dto.*;
import ru.neoflex.loanissuerlibrary.dto.enumeration.EmploymentStatus;
import ru.neoflex.loanissuerlibrary.dto.enumeration.Gender;
import ru.neoflex.loanissuerlibrary.dto.enumeration.MaritalStatus;
import ru.neoflex.loanissuerlibrary.dto.enumeration.WorkPosition;
import ru.neoflex.loanissuerlibrary.exception.CreditDeniedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("classpath:service.properties")
class CalculatorServiceTest {

    @Autowired
    private CalculatorService calculatorService;

    private LoanStatementRequestDto validRequest;
    private EmploymentDto validEmployment;
    private ScoringDataDto validScoringData;

    private final BigDecimal rate;
    private final BigDecimal insuranceRate;
    private final BigDecimal clientInsuranceRate;
    private final BigDecimal insuranceDecrement;
    private final BigDecimal clientDecrement;
    private final BigDecimal maxCalculationMistake = new BigDecimal("0.01");
    private final int calculatingScale = 200;

    private CalculatorServiceTest(@Value("${rate}") BigDecimal rate,
                                  @Value("${insurance.rate}") BigDecimal insuranceRate,
                                  @Value("${client.insurance.rate}") BigDecimal clientInsuranceRate,
                                  @Value("${rate.decrement.for.insurance}") BigDecimal insuranceDecrement,
                                  @Value("${rate.decrement.for.clients}") BigDecimal clientDecrement) {
        this.rate = rate;
        this.insuranceRate = insuranceRate;
        this.clientInsuranceRate = clientInsuranceRate;
        this.insuranceDecrement = insuranceDecrement;
        this.clientDecrement = clientDecrement;
    }

    @BeforeEach
    void setUp() {
        validRequest = LoanStatementRequestDto.builder()
                .amount(new BigDecimal("100000"))
                .term(6)
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .birthdate(LocalDate.of(1990, 1, 1))
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
                .amount(new BigDecimal("100000"))
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
    }

    @Test
    @DisplayName("На запрос возвращается лист с 4 значениями")
    void givenValidRequestReturnsListOfSizeFour() {
        assertEquals(4, calculatorService.getOffers(validRequest).size());
    }


    @ParameterizedTest
    @DisplayName("Внутри листа находятся экземпляры класса LoanOfferDto")
    @ValueSource(ints = {0, 1, 2, 3})
    void givenValidRequestReturnsListFullOfLoanOfferDto(int index) {
        assertEquals(LoanOfferDto.class, calculatorService.getOffers(validRequest).get(index).getClass());
    }

    @Test
    @DisplayName("LoanOfferDto внутри листа расположены по убыванию ставки")
    void givenValidRequestReturnsSortedListOfLoanOfferDto() {
        List<LoanOfferDto> offers = calculatorService.getOffers(validRequest);
        assertTrue(offers.get(0).getRate().compareTo(offers.get(1).getRate()) > 0);
        assertTrue(offers.get(1).getRate().compareTo(offers.get(2).getRate()) > 0);
        assertTrue(offers.get(2).getRate().compareTo(offers.get(3).getRate()) > 0);
    }

    @Test
    @DisplayName("В LoanOfferDto корректно рассчитывается процентная ставка")
    void givenValidRequestReturnsSortedListWithCorrectRates() {
        List<LoanOfferDto> offers = calculatorService.getOffers(validRequest);

        BigDecimal notInsuredNotClient = rate;
        BigDecimal notInsuredAClient = rate.subtract(clientDecrement);
        BigDecimal InsuredNotClient = rate.subtract(insuranceDecrement);
        BigDecimal InsuredAClient = rate.subtract(clientDecrement.add(insuranceDecrement));

        assertEquals(offers.get(0).getRate(), notInsuredNotClient);
        assertEquals(offers.get(1).getRate(), notInsuredAClient);
        assertEquals(offers.get(2).getRate(), InsuredNotClient);
        assertEquals(offers.get(3).getRate(), InsuredAClient);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("Срок кредита не меняется")
    void givenValidRequestReturnsOffersWithCorrectTerm(int index) {
        List<LoanOfferDto> offers = calculatorService.getOffers(validRequest);

        assertEquals(offers.get(index).getTerm(), validRequest.getTerm());
    }

    @Test
    @DisplayName("""
            Лист содержит в себе предложения со всеми различными комбинациями \
            булевых полей isInsuranceEnabled и isSalaryClient""")
    void givenValidRequestReturnsListWithEveryCombinationOfBooleanFields() {
        List<LoanOfferDto> offers = calculatorService.getOffers(validRequest);

        assertNotNull(findOfferByBooleans(offers, false, false));
        assertNotNull(findOfferByBooleans(offers, true, false));
        assertNotNull(findOfferByBooleans(offers, false, true));
        assertNotNull(findOfferByBooleans(offers, true, true));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("Сумма займа не меняется в зависимости от страховки при вызове getOffers()")
    void givenValidRequestAmountInOfferStaysTheSame(int index) {
        List<LoanOfferDto> offers = calculatorService.getOffers(validRequest);

        BigDecimal realAmount = validRequest.getAmount();

        assertTrue((realAmount.subtract(offers.get(index).getRequestedAmount()))
                .abs()
                .compareTo(maxCalculationMistake) < 0);
    }

    @Disabled
    @Test
    @DisplayName("Сумма займа меняется корректно в зависимости от страховки при вызове getOffers()")
    void givenValidRequestReturnsOffersWithCorrectAmountConsideringInsurance() {
        List<LoanOfferDto> offers = calculatorService.getOffers(validRequest);

        BigDecimal NotInsuredAmount = validRequest.getAmount();
        BigDecimal InsuranceAmount = NotInsuredAmount.multiply(insuranceRate);
        BigDecimal InsuredNotClientAmount = NotInsuredAmount.add(InsuranceAmount);
        BigDecimal InsuredClientAmount = NotInsuredAmount.add(NotInsuredAmount.multiply(clientInsuranceRate));

        LoanOfferDto NotInsuredNotClientOffer = findOfferByBooleans(offers, false, false);
        LoanOfferDto NotInsuredClientOffer = findOfferByBooleans(offers, false, true);
        LoanOfferDto InsuredNotClientOffer = findOfferByBooleans(offers, true, false);
        LoanOfferDto InsuredClientOffer = findOfferByBooleans(offers, true, true);

        assertNotNull(NotInsuredNotClientOffer);
        assertNotNull(NotInsuredClientOffer);
        assertNotNull(InsuredNotClientOffer);
        assertNotNull(InsuredClientOffer);

        BigDecimal NotInsuredNotClientDiff = (NotInsuredAmount
                .subtract(NotInsuredNotClientOffer.getRequestedAmount()))
                .abs();
        BigDecimal NotInsuredClientDiff = (NotInsuredAmount
                .subtract(NotInsuredClientOffer.getRequestedAmount()))
                .abs();
        BigDecimal InsuredNotClientDiff = (InsuredNotClientAmount
                .subtract(InsuredNotClientOffer.getRequestedAmount()))
                .abs();
        BigDecimal InsuredClientDiff = (InsuredClientAmount
                .subtract(InsuredClientOffer.getRequestedAmount()))
                .abs();

        assertTrue(NotInsuredNotClientDiff.compareTo(maxCalculationMistake) < 0);
        assertTrue(NotInsuredClientDiff.compareTo(maxCalculationMistake) < 0);
        assertTrue(InsuredNotClientDiff.compareTo(maxCalculationMistake) < 0);
        assertTrue(InsuredClientDiff.compareTo(maxCalculationMistake) < 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("Для каждого предложения корректно рассчитывается ежемесячная плата")
    void givenValidRequestReturnsOffersWithCorrectMonthlyPaymentAmount(int index) {
        LoanOfferDto offer = calculatorService.getOffers(validRequest).get(index);

        BigDecimal monthlyPayment = calculateMonthlyPayment(offer.getRequestedAmount(),
                                                            offer.getRate(),
                                                            offer.getTerm(),
                                                            offer.getIsInsuranceEnabled(),
                                                            offer.getIsSalaryClient());


        assertTrue((monthlyPayment.subtract(offer.getMonthlyPayment()))
                .abs()
                .compareTo(maxCalculationMistake) < 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("Правильно рассчитывается полная сумма кредита для каждого предложения")
    void givenValidRequestReturnsOffersWithCorrectPsk(int index) {
        LoanOfferDto offer = calculatorService.getOffers(validRequest).get(index);

        BigDecimal psk = calculateMonthlyPayment(offer.getRequestedAmount(),
                                                 offer.getRate(),
                                                 offer.getTerm(),
                                                 offer.getIsInsuranceEnabled(),
                                                 offer.getIsSalaryClient())
                         .multiply(new BigDecimal(offer.getTerm()));

        assertTrue((psk.subtract(offer.getTotalAmount()))
                .abs()
                .compareTo(maxCalculationMistake) <= 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {16, 19, 66, 69, 125123})
    @DisplayName("""
            При передаче неподходящего возраста заявка отклоняется \
            и выбрасывается CreditDeniedException""")
    void givenInvalidAgeThrowsCreditDeniedException(int age) {
        validScoringData.setBirthdate(LocalDate.now().minusYears(age));

        assertThrowsExactly(CreditDeniedException.class,
                () -> calculatorService.isEligibleForCredit(validScoringData));
    }

    @Test
    @DisplayName("""
            При передаче неподходящего опыта на всех рабочих местах \
            заявка отклоняется и выбрасывается CreditDeniedException""")
    void givenInvalidTotalWorkExperienceThrowsCreditDeniedException() {
        validScoringData.getEmployment().setWorkExperienceTotal(17);

        assertThrowsExactly(CreditDeniedException.class,
                () -> calculatorService.isEligibleForCredit(validScoringData));
    }

    @Test
    @DisplayName("""
            При передаче неподходящего опыта на нынешнем рабочем месте \
            заявка отклоняется и выбрасывается CreditDeniedException""")
    void givenInvalidCurrentTotalWorkExperienceThrowsCreditDeniedException() {
        validScoringData.getEmployment().setWorkExperienceCurrent(2);

        assertThrowsExactly(CreditDeniedException.class,
                () -> calculatorService.isEligibleForCredit(validScoringData));
    }

    @Test
    @DisplayName("""
            При передаче слишком большой суммы кредита заявка отклоняется \
            и выбрасывается CreditDeniedException""")
    void givenInvalidAmountThrowsCreditDeniedException() {
        validScoringData.setAmount(validScoringData.getAmount().multiply(new BigDecimal("25")));

        assertThrowsExactly(CreditDeniedException.class,
                () -> calculatorService.isEligibleForCredit(validScoringData));
    }

    @Test
    @DisplayName("""
            При передаче некорректного статуса занятости заявка отклоняется \
            и выбрасывается CreditDeniedException""")
    void givenInvalidEmploymentStatusThrowsCreditDeniedException() {
        validScoringData.getEmployment().setEmploymentStatus(EmploymentStatus.NOT_EMPLOYED);

        assertThrowsExactly(CreditDeniedException.class,
                () -> calculatorService.isEligibleForCredit(validScoringData));
    }

    @Test
    @DisplayName("Сумма кредита не меняется при вызове getCredit() вне зависимости от страховки")
    void givenValidScoringDataRequestedAmountStaysTheSame() throws CreditDeniedException {
        BigDecimal baseAmount = validScoringData.getAmount();

        validScoringData.setIsInsuranceEnabled(false);
        validScoringData.setIsSalaryClient(false);
        CreditDto NotInsuredNotClient = calculatorService.getCredit(validScoringData);

        validScoringData.setIsInsuranceEnabled(false);
        validScoringData.setIsSalaryClient(true);
        CreditDto NotInsuredAClient = calculatorService.getCredit(validScoringData);

        validScoringData.setIsInsuranceEnabled(true);
        validScoringData.setIsSalaryClient(false);
        CreditDto InsuredNotAClient = calculatorService.getCredit(validScoringData);

        validScoringData.setIsInsuranceEnabled(true);
        validScoringData.setIsSalaryClient(true);
        CreditDto InsuredAClient = calculatorService.getCredit(validScoringData);

        assertTrue((NotInsuredNotClient.getAmount().subtract(baseAmount))
                .abs()
                .compareTo(maxCalculationMistake) < 0);
        assertTrue((NotInsuredAClient.getAmount().subtract(baseAmount))
                .abs()
                .compareTo(maxCalculationMistake) < 0);
        assertTrue((InsuredNotAClient.getAmount()
                .subtract(baseAmount))
                .abs()
                .compareTo(maxCalculationMistake) < 0);
        assertTrue((InsuredAClient.getAmount()
                .subtract(baseAmount))
                .abs()
                .compareTo(maxCalculationMistake) < 0);
    }

    @Disabled
    @Test
    @DisplayName("""
            Сумма займа меняется корректно при вызове getCredit() в зависимости от страховки и того, \
            получает ли клиент зарплату в банке""")
    void givenValidScoringDataRequestedAmountReturnedCorrectly() throws CreditDeniedException {
        BigDecimal baseAmount = validScoringData.getAmount();

        validScoringData.setIsInsuranceEnabled(false);
        validScoringData.setIsSalaryClient(false);
        CreditDto NotInsuredNotClient = calculatorService.getCredit(validScoringData);

        validScoringData.setIsInsuranceEnabled(false);
        validScoringData.setIsSalaryClient(true);
        CreditDto NotInsuredAClient = calculatorService.getCredit(validScoringData);

        validScoringData.setIsInsuranceEnabled(true);
        validScoringData.setIsSalaryClient(false);
        CreditDto InsuredNotAClient = calculatorService.getCredit(validScoringData);
        BigDecimal InsuredNotAClientAmount = baseAmount.add(baseAmount.multiply(insuranceRate));


        validScoringData.setIsInsuranceEnabled(true);
        validScoringData.setIsSalaryClient(true);
        CreditDto InsuredAClient = calculatorService.getCredit(validScoringData);
        BigDecimal InsuredAClientAmount = baseAmount.add(baseAmount.multiply(clientInsuranceRate));

        assertTrue((NotInsuredNotClient.getAmount().subtract(baseAmount))
                    .abs()
                   .compareTo(maxCalculationMistake) < 0);
        assertTrue((NotInsuredAClient.getAmount().subtract(baseAmount))
                    .abs()
                    .compareTo(maxCalculationMistake) < 0);
        assertTrue((InsuredNotAClient.getAmount()
                    .subtract(InsuredNotAClientAmount))
                    .abs()
                    .compareTo(maxCalculationMistake) < 0);
        assertTrue((InsuredAClient.getAmount()
                    .subtract(InsuredAClientAmount))
                    .abs()
                    .compareTo(maxCalculationMistake) < 0);
    }

    @Test
    @DisplayName("Срок займа не изменяется при вызове getCredit()")
    void givenValidScoringDataTermDoesNotChange() throws CreditDeniedException {
        CreditDto credit = calculatorService.getCredit(validScoringData);

        assertEquals(credit.getTerm(), validScoringData.getTerm());
    }

    @Test
    @DisplayName("""
            Ставка при полном расчёте кредита вычисляется корректно \
            в зависимости от переданных данных""")
    void givenValidScoringDataRateIsCalculatedCorrectly() throws CreditDeniedException {
        // Мужчина 34 года, холост, мидл, страховка включена, клиент банка
        CreditDto credit = calculatorService.getCredit(validScoringData);
        // базовый 0.25 - 0.02 страховка - 0.01 клиент банка = 0.22
        // за мидла, обычного рабочего, холостого нет изменений
        // мужчина 34 лет - 0.03 = 0.19
        BigDecimal expectedRate = new BigDecimal("0.19");

        assertEquals(0, expectedRate.compareTo(credit.getRate()));
    }

    @Test
    @DisplayName("Ежемесячный платёж вычисляется верно при полном расчёте кредита")
    void givenValidScoringDataMonthlyPaymentIsCalculatedCorrectly() throws CreditDeniedException {
        CreditDto credit = calculatorService.getCredit(validScoringData);

        BigDecimal expectedMonthlyPayment = calculateMonthlyPayment(credit.getAmount(),
                                                                    credit.getRate(),
                                                                    credit.getTerm(),
                                                                    credit.getIsInsuranceEnabled(),
                                                                    credit.getIsSalaryClient());

        assertTrue((expectedMonthlyPayment.subtract(credit.getMonthlyPayment()))
                    .abs()
                    .compareTo(maxCalculationMistake) < 0);
    }

    @Test
    @DisplayName("Полная стоимость кредита вычисляется корректно при полном расчёте кредита")
    void givenValidScoringDataPskIsCalculatedCorrectly() throws CreditDeniedException {
        CreditDto credit = calculatorService.getCredit(validScoringData);

        BigDecimal expectedPsk = calculateMonthlyPayment(credit.getAmount(),
                                                         credit.getRate(),
                                                         credit.getTerm(),
                                                         credit.getIsInsuranceEnabled(),
                                                         credit.getIsSalaryClient())
                                 .multiply(new BigDecimal(credit.getTerm()));

        assertTrue((expectedPsk.subtract(credit.getPsk()))
                    .abs()
                    .compareTo(maxCalculationMistake) < 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 9, 15, 18, 24, 99, 1111})
    @DisplayName("При полном расчёте кредита возвращается term элементов")
    void givenValidScoringDataReturnScheduleWithTermElements(int term) throws CreditDeniedException {
        validScoringData.setTerm(term);
        CreditDto credit = calculatorService.getCredit(validScoringData);

        assertEquals(term, credit.getPaymentSchedule().size());
    }

    @Test
    @DisplayName("Номера платежей идут по порядку начиная с 1")
    void givenValidScoringDataReturnScheduleWithCorrectPaymentNumbers() throws CreditDeniedException {
        CreditDto credit = calculatorService.getCredit(validScoringData);

        for(int i = 0; i < credit.getTerm(); i++) {
            assertEquals(i + 1, credit.getPaymentSchedule().get(i).getNumber());
        }
    }

    @Test
    @DisplayName("Сумма ежемесячного платежа всегда одинаковая")
    void givenValidScoringDataReturnScheduleWithTheSameMonthlyPayment() throws CreditDeniedException {
        CreditDto credit = calculatorService.getCredit(validScoringData);

        BigDecimal monthlyPayment = credit.getMonthlyPayment();

        for(var element : credit.getPaymentSchedule()) {
            assertTrue((monthlyPayment.subtract(element.getTotalPayment()))
                    .abs()
                    .compareTo(maxCalculationMistake) < 0);
        }
    }

    private LoanOfferDto findOfferByBooleans(List<LoanOfferDto> offers,
                                             Boolean isInsuranceEnabled,
                                             Boolean isSalaryClient) {
        for (var offer : offers) {
            if (offer.getIsInsuranceEnabled().equals(isInsuranceEnabled) &&
                    offer.getIsSalaryClient().equals(isSalaryClient)) {
                return offer;
            }
        }

        return null;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal requestedAmount,
                                               BigDecimal rate,
                                               Integer term,
                                               Boolean isInsuranceEnabled,
                                               Boolean isSalaryClient) {
        int monthsInYear = 12;
        BigDecimal monthlyRate = rate.divide(new BigDecimal(monthsInYear),
                                             calculatingScale,
                                             RoundingMode.HALF_EVEN);
        BigDecimal insuredAmount = requestedAmount;
        if (isInsuranceEnabled) {
            insuredAmount = isSalaryClient ?
                    insuredAmount.add(requestedAmount.multiply(clientInsuranceRate)) :
                    insuredAmount.add(requestedAmount.multiply(insuranceRate));
        }

        BigDecimal monthlyPay = insuredAmount.multiply(monthlyRate);
        BigDecimal divisor = BigDecimal.ONE;
        divisor = divisor.add(monthlyRate);
        divisor = divisor.pow(term);
        divisor = BigDecimal.ONE.divide(divisor, calculatingScale, RoundingMode.HALF_EVEN);
        divisor = BigDecimal.ONE.subtract(divisor);
        monthlyPay = monthlyPay.divide(divisor, calculatingScale, RoundingMode.HALF_EVEN);

        return monthlyPay;
    }
}