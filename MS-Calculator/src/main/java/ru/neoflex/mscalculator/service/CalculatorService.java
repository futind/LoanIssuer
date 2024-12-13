package ru.neoflex.mscalculator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neoflex.mscalculator.dto.*;
import ru.neoflex.mscalculator.dto.enumeration.EmploymentStatus;
import ru.neoflex.mscalculator.dto.enumeration.Gender;
import ru.neoflex.mscalculator.exception.CreditDeniedException;
import ru.neoflex.mscalculator.util.RateComparator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
public class CalculatorService {

    private final BigDecimal baseRate;
    private final BigDecimal insuranceRate;
    private final BigDecimal clientInsuranceRate;
    private final BigDecimal insuranceDecrement;
    private final BigDecimal clientDecrement;

    private final int calculatingScale = 200;
    private final int presentationScale = 2;
    private final int monthsInYear = 12;

    public CalculatorService(@Value("${rate}") BigDecimal baseRate,
                             @Value("${insurance.rate}") BigDecimal insuranceRate,
                             @Value("${client.insurance.rate}") BigDecimal clientInsuranceRate,
                             @Value("${rate.decrement.for.insurance}") BigDecimal insuranceDecrement,
                             @Value("${rate.decrement.for.clients}") BigDecimal clientDecrement)
    {
        this.baseRate = baseRate;
        this.insuranceRate = insuranceRate;
        this.clientInsuranceRate = clientInsuranceRate;
        this.insuranceDecrement = insuranceDecrement;
        this.clientDecrement = clientDecrement;
    }

    public List<LoanOfferDto> getOffers(LoanStatementRequestDto loanStatementRequestDto) {

        List<LoanOfferDto> loanOfferDtoList = new ArrayList<>();

        loanOfferDtoList.add(createLoanOfferDto(loanStatementRequestDto,
                false, false));

        loanOfferDtoList.add(createLoanOfferDto(loanStatementRequestDto,
                false, true));
        loanOfferDtoList.add(createLoanOfferDto(loanStatementRequestDto,
                true, false));
        loanOfferDtoList.add(createLoanOfferDto(loanStatementRequestDto,
                true, true));

        loanOfferDtoList.sort(new RateComparator().reversed());

        return loanOfferDtoList;
    }

    public void isEligibleForCredit(ScoringDataDto scoringDataDto) throws CreditDeniedException {

        if (scoringDataDto.getEmployment().getEmploymentStatus() == EmploymentStatus.NOT_EMPLOYED) {
            throw new CreditDeniedException("Must be employed to get a loan.");
        }

        if (scoringDataDto.getEmployment().getWorkExperienceTotal() < 18) {
            throw new CreditDeniedException("Must be working over 18 months in total to get a loan.");
        }

        if (scoringDataDto.getEmployment().getWorkExperienceCurrent() < 3) {
            throw new CreditDeniedException("Must be working at a current job at least for full 3 months.");
        }

        int age = (int) ChronoUnit.YEARS.between(scoringDataDto.getBirthdate(), LocalDate.now());
        if (age < 20) {
            throw new CreditDeniedException("Must be at least 20 years old to get a loan.");
        }

        if (age > 65) {
            throw new CreditDeniedException("Must be at most 65 years old to get a loan.");
        }

        BigDecimal maxAmount = scoringDataDto.getEmployment().getSalary().multiply(BigDecimal.valueOf(24));
        if (scoringDataDto.getAmount().compareTo(maxAmount) > 0) {
            throw new CreditDeniedException("The requested amount must be at most " +
                                            maxAmount.setScale(1, RoundingMode.HALF_EVEN));
        }
    }

    public CreditDto getCredit(ScoringDataDto scoringDataDto) throws CreditDeniedException {
        BigDecimal calculatedRate = calculateRate(scoringDataDto.getIsInsuranceEnabled(),
                                                  scoringDataDto.getIsSalaryClient());

        BigDecimal insurancePayment = BigDecimal.ZERO;
        if (scoringDataDto.getIsInsuranceEnabled()) {
            insurancePayment = scoringDataDto.getIsSalaryClient() ?
                    scoringDataDto.getAmount().multiply(clientInsuranceRate) :
                    scoringDataDto.getAmount().multiply(insuranceRate);
        }

        BigDecimal adjustedRate = calculatedRate.add(calculateRateAdjustment(scoringDataDto));

        BigDecimal monthlyRate = adjustedRate.divide(new BigDecimal(monthsInYear),
                calculatingScale, RoundingMode.HALF_EVEN);

        BigDecimal amountWithInsurance = scoringDataDto.getAmount().add(insurancePayment);


        BigDecimal monthlyPayment = calculateMonthlyPayment(monthlyRate,
                                                            amountWithInsurance,
                                                            scoringDataDto.getTerm());

        BigDecimal psk = calculatePsk(monthlyPayment, scoringDataDto.getTerm());

        log.info("Calculated yearlyRate: {}, monthlyRate: {}, monthlyPayment: {}, psk: {}",
                adjustedRate.setScale(presentationScale, RoundingMode.HALF_EVEN),
                monthlyRate.setScale(presentationScale, RoundingMode.HALF_EVEN),
                monthlyPayment.setScale(presentationScale, RoundingMode.HALF_EVEN),
                psk.setScale(presentationScale, RoundingMode.HALF_EVEN));

        List<PaymentScheduleElementDto> paymentSchedule = createPaymentSchedule(amountWithInsurance,
                                                                                monthlyRate,
                                                                                monthlyPayment,
                                                                                scoringDataDto.getTerm());

        return CreditDto.builder()
                .amount(scoringDataDto.getAmount().setScale(presentationScale, RoundingMode.HALF_EVEN))
                .term(scoringDataDto.getTerm())
                .monthlyPayment(monthlyPayment.setScale(presentationScale, RoundingMode.HALF_EVEN))
                .rate(adjustedRate)
                .psk(psk.setScale(presentationScale, RoundingMode.HALF_EVEN))
                .isInsuranceEnabled(scoringDataDto.getIsInsuranceEnabled())
                .isSalaryClient(scoringDataDto.getIsSalaryClient())
                .paymentSchedule(paymentSchedule)
                .build();
    }

    private BigDecimal calculatePsk(BigDecimal monthlyPayment, Integer term) {
        return monthlyPayment.multiply(new BigDecimal(term));
    }

    private List<PaymentScheduleElementDto> createPaymentSchedule(BigDecimal amount,
                                                                  BigDecimal monthlyRate,
                                                                  BigDecimal monthlyPayment,
                                                                  Integer term) {
        LocalDate paymentDate = LocalDate.now().plusMonths(1);

        log.info("Creating payment schedule from {} to {}; amount: {}, monthlyRate: {}, monthlyPayment: {}",
                paymentDate,
                LocalDate.now().plusMonths(term),
                amount.setScale(presentationScale, RoundingMode.HALF_EVEN),
                monthlyRate.setScale(presentationScale, RoundingMode.HALF_EVEN),
                monthlyPayment.setScale(presentationScale, RoundingMode.HALF_EVEN)
        );

        BigDecimal remainingDebt = amount;

        List<PaymentScheduleElementDto> paymentScheduleElementDtoList = new ArrayList<>();

        for(int paymentNumber = 1; paymentNumber < term; ++paymentNumber) {
            BigDecimal interestPayment = remainingDebt.multiply(monthlyRate);
            BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);
            remainingDebt = remainingDebt.subtract(debtPayment);

            paymentScheduleElementDtoList.add(
                    PaymentScheduleElementDto.builder()
                            .number(paymentNumber)
                            .date(paymentDate)
                            .totalPayment(monthlyPayment.setScale(presentationScale, RoundingMode.HALF_UP))
                            .debtPayment(debtPayment.setScale(presentationScale, RoundingMode.HALF_UP))
                            .interestPayment(interestPayment.setScale(presentationScale, RoundingMode.HALF_UP))
                            .remainingDebt(remainingDebt.setScale(presentationScale, RoundingMode.HALF_UP))
                            .build());
            paymentDate = paymentDate.plusMonths(1);
        }

        BigDecimal interestPayment = remainingDebt.multiply(monthlyRate);
        BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);
        if (remainingDebt.subtract(debtPayment).compareTo(BigDecimal.ZERO) <= 0) {
            remainingDebt = BigDecimal.ZERO;
        } else {
            remainingDebt = remainingDebt.subtract(debtPayment);
        }

        paymentScheduleElementDtoList.add(
                PaymentScheduleElementDto.builder()
                        .number(term)
                        .date(paymentDate)
                        .totalPayment(monthlyPayment.setScale(presentationScale, RoundingMode.HALF_UP))
                        .debtPayment(debtPayment.setScale(presentationScale, RoundingMode.HALF_UP))
                        .interestPayment(interestPayment.setScale(presentationScale, RoundingMode.HALF_UP))
                        .remainingDebt(remainingDebt.setScale(presentationScale, RoundingMode.HALF_UP))
                        .build());

        return paymentScheduleElementDtoList;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal monthlyRate,
                                               BigDecimal amount,
                                               Integer term) {
        BigDecimal dividend = amount.multiply(monthlyRate);
        BigDecimal divisor = BigDecimal.ONE;
        divisor = divisor.add(monthlyRate);
        divisor = divisor.pow(term);
        divisor = BigDecimal.ONE.divide(divisor, calculatingScale, RoundingMode.HALF_EVEN);
        divisor = BigDecimal.ONE.subtract(divisor);
        return dividend.divide(divisor, calculatingScale, RoundingMode.HALF_EVEN);
    }

    private BigDecimal calculateRateAdjustment(ScoringDataDto scoringDataDto) throws CreditDeniedException {

        if (scoringDataDto.getEmployment().getPosition() == null) {
            throw new CreditDeniedException("Employment position must be provided.");
        }

        int age = (int) ChronoUnit.YEARS.between(scoringDataDto.getBirthdate(), LocalDate.now());

        BigDecimal adjustment = BigDecimal.ZERO;

        switch(scoringDataDto.getEmployment().getEmploymentStatus()) {
            case SELF_EMPLOYED:
                adjustment = adjustment.add(new BigDecimal("0.02"));
                break;
            case EMPLOYER:
                adjustment = adjustment.add(new BigDecimal("0.01"));
                break;
            default:
                break;
        }

        switch(scoringDataDto.getEmployment().getPosition()) {
            case JUNIOR:
                adjustment = adjustment.add(new BigDecimal("0.01"));
                break;
            case SENIOR:
                adjustment = adjustment.subtract(new BigDecimal("0.01"));
                break;
            case TEAM_LEAD:
                adjustment = adjustment.subtract(new BigDecimal("0.02"));
                break;
            case TOP_MANAGER:
                adjustment = adjustment.subtract(new BigDecimal("0.03"));
                break;
            default:
                break;
        }

        switch(scoringDataDto.getMaritalStatus()) {
            case MARRIED:
                adjustment = adjustment.subtract(new BigDecimal("0.03"));
                break;
            case DIVORCED:
                adjustment = adjustment.add(new BigDecimal("0.01"));
                break;
            default:
                break;
        }

        if ((scoringDataDto.getGender() == Gender.FEMALE && age >= 32 && age <= 60) ||
            (scoringDataDto.getGender() == Gender.MALE && age >= 30 && age <= 55)) {
            adjustment = adjustment.subtract(new BigDecimal("0.03"));
        }

        return adjustment;
    }

    private BigDecimal calculateRate(Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        BigDecimal rate = baseRate;

        if (isInsuranceEnabled) {
            rate = rate.subtract(insuranceDecrement);
        }

        if (isSalaryClient) {
            rate = rate.subtract(clientDecrement);
        }

        return rate;
    }

    private LoanOfferDto createLoanOfferDto(LoanStatementRequestDto loanStatementRequestDto,
                                            Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        BigDecimal requestedAmount = loanStatementRequestDto.getAmount();
        Integer term = loanStatementRequestDto.getTerm();

        BigDecimal rate = calculateRate(isInsuranceEnabled, isSalaryClient);

        BigDecimal insurancePayment = BigDecimal.ZERO;
        if (isInsuranceEnabled) {
            insurancePayment = isSalaryClient ?
                    requestedAmount.multiply(clientInsuranceRate) :
                    requestedAmount.multiply(insuranceRate);
        }

        BigDecimal insuredAmount = requestedAmount.add(insurancePayment);

        BigDecimal monthlyPayment = calculateMonthlyPayment(rate.divide(new BigDecimal(monthsInYear),
                                                            calculatingScale, RoundingMode.HALF_EVEN),
                                                            insuredAmount, term);
        BigDecimal totalAmount = calculatePsk(monthlyPayment, term);

        return LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(requestedAmount.setScale(presentationScale, RoundingMode.HALF_EVEN))
                .totalAmount(totalAmount.setScale(presentationScale, RoundingMode.HALF_EVEN))
                .term(term)
                .monthlyPayment(monthlyPayment.setScale(presentationScale, RoundingMode.HALF_EVEN))
                .rate(rate)
                .isSalaryClient(isSalaryClient)
                .isInsuranceEnabled(isInsuranceEnabled)
                .build();
    }
}