package ru.neoflex.mscalculator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neoflex.mscalculator.dtos.*;
import ru.neoflex.mscalculator.dtos.enumeration.EmploymentStatus;
import ru.neoflex.mscalculator.dtos.enumeration.Gender;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class CalculatorService {

    public CalculatorService(@Value("${rate}") BigDecimal baseRate,
                             @Value("${insurance.rate}") BigDecimal insuranceRate)
    {
        this.baseRate = baseRate;
        this.insuranceRate = insuranceRate;
    }

    // prescoring
    public List<LoanOfferDto> getOffers(LoanStatementRequestDto loanStatementRequestDto) {

        LoanOfferDto withoutInsuranceNotClient = createLoanOfferDto(loanStatementRequestDto.getAmount(),
                loanStatementRequestDto.getTerm(),
                false, false);
        LoanOfferDto withoutInsuranceAClient = createLoanOfferDto(loanStatementRequestDto.getAmount(),
                loanStatementRequestDto.getTerm(),
                false, true);
        LoanOfferDto withInsuranceNotClient = createLoanOfferDto(loanStatementRequestDto.getAmount(),
                loanStatementRequestDto.getTerm(),
                true, false);
        LoanOfferDto withInsuranceAClient = createLoanOfferDto(loanStatementRequestDto.getAmount(),
                loanStatementRequestDto.getTerm(),
                true, true);
        List<LoanOfferDto> loanOfferDtoList = Arrays.asList(withoutInsuranceNotClient, withoutInsuranceAClient,
                withInsuranceNotClient, withInsuranceAClient);
        loanOfferDtoList.sort(new RateComparator());
        return loanOfferDtoList;
    }

    // scoring
    public CreditDto getCredit(ScoringDataDto scoringDataDto) {
        BigDecimal calculatedRate = calculateRate(scoringDataDto.getIsInsuranceEnabled(),
                                                  scoringDataDto.getIsSalaryClient());

        BigDecimal insurancePayment = BigDecimal.ZERO;
        if (scoringDataDto.getIsInsuranceEnabled()) {
            insurancePayment = scoringDataDto.getAmount().multiply(insuranceRate);
        }

        if (scoringDataDto.getIsInsuranceEnabled() && scoringDataDto.getIsSalaryClient()) {
            insurancePayment = insurancePayment.divide(new BigDecimal("2"), 100, RoundingMode.HALF_EVEN);
        }

        BigDecimal adjustedRate = calculatedRate.add(calculateRateAdjustment(scoringDataDto, insurancePayment));

        BigDecimal monthlyRate = adjustedRate.divide(new BigDecimal(12),
                100, RoundingMode.HALF_EVEN);

        BigDecimal amountWithInsurance = scoringDataDto.getAmount().add(insurancePayment);

        BigDecimal monthlyPayment = calculateMonthlyPayment(monthlyRate,
                                                            amountWithInsurance,
                                                            scoringDataDto.getTerm());

        List<PaymentScheduleElementDto> paymentSchedule = createPaymentSchedule(amountWithInsurance,
                                                                                monthlyRate,
                                                                                monthlyPayment,
                                                                                scoringDataDto.getTerm());

        BigDecimal psk = calculatePsk(monthlyPayment, scoringDataDto.getTerm());

        return new CreditDto(amountWithInsurance.setScale(2, RoundingMode.HALF_EVEN),
                             scoringDataDto.getTerm(),
                             monthlyPayment.setScale(2, RoundingMode.HALF_EVEN),
                             adjustedRate,
                             psk.setScale(2, RoundingMode.HALF_EVEN),
                             scoringDataDto.getIsInsuranceEnabled(),
                             scoringDataDto.getIsSalaryClient(),
                             paymentSchedule);
    }

    private BigDecimal calculatePsk(BigDecimal monthlyPayment, Integer term) {
        return monthlyPayment.multiply(new BigDecimal(term));
    }

    private List<PaymentScheduleElementDto> createPaymentSchedule(BigDecimal amount,
                                                                  BigDecimal monthlyRate,
                                                                  BigDecimal monthlyPayment,
                                                                  Integer term) {
        LocalDate paymentDate = LocalDate.now().plusMonths(1);
        BigDecimal remainingDebt = amount;

        List<PaymentScheduleElementDto> paymentScheduleElementDtoList = new ArrayList<>();

        for(int paymentNumber = 1; paymentNumber < term; ++paymentNumber) {
            BigDecimal interestPayment = remainingDebt.multiply(monthlyRate);
            BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);
            remainingDebt = remainingDebt.subtract(debtPayment);

            paymentScheduleElementDtoList.add(
                    new PaymentScheduleElementDto(paymentNumber, paymentDate,
                                                  monthlyPayment.setScale(2, RoundingMode.HALF_UP),
                                                  interestPayment.setScale(2, RoundingMode.HALF_UP),
                                                  debtPayment.setScale(2, RoundingMode.HALF_UP),
                                                  remainingDebt.setScale(2, RoundingMode.HALF_UP)));
            paymentDate = paymentDate.plusMonths(1);
        }

        BigDecimal interestPayment = remainingDebt.multiply(monthlyRate);
        BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);
        if (remainingDebt.subtract(debtPayment).compareTo(BigDecimal.ZERO) <= 0) {
            remainingDebt = BigDecimal.ZERO;
        } else {
            remainingDebt = remainingDebt.subtract(debtPayment);
        }

        paymentScheduleElementDtoList.add(new PaymentScheduleElementDto(term,
                paymentDate,
                monthlyPayment.setScale(2, RoundingMode.HALF_UP),
                interestPayment.setScale(2, RoundingMode.HALF_UP),
                debtPayment.setScale(2, RoundingMode.HALF_UP),
                remainingDebt.setScale(2, RoundingMode.HALF_UP)));

        return paymentScheduleElementDtoList;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal monthlyRate,
                                               BigDecimal amount,
                                               Integer term) {
        BigDecimal dividend = amount.multiply(monthlyRate);
        BigDecimal divisor = new BigDecimal("1");
        divisor = divisor.add(monthlyRate);
        divisor = divisor.pow(term);
        divisor = (new BigDecimal("1")).divide(divisor, 100, RoundingMode.HALF_EVEN);
        divisor = (new BigDecimal("1")).subtract(divisor);
        return dividend.divide(divisor, 100, RoundingMode.HALF_EVEN);
    }

    private BigDecimal calculateRateAdjustment(ScoringDataDto scoringDataDto, BigDecimal insurancePayment) {
        BigDecimal maxAmount = scoringDataDto.getEmployment().getSalary().multiply(BigDecimal.valueOf(24));
        int age = (int) ChronoUnit.YEARS.between(scoringDataDto.getBirthdate(), LocalDate.now());

        if (scoringDataDto.getEmployment().getEmploymentStatus() == EmploymentStatus.NOT_EMPLOYED ||
            scoringDataDto.getEmployment().getWorkExperienceTotal() < 18 ||
            scoringDataDto.getEmployment().getWorkExperienceCurrent() < 3 ||
            age < 20 || age > 65 ||
            scoringDataDto.getAmount().add(insurancePayment).compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("Credit application is denied");
        }

        BigDecimal adjustment = new BigDecimal("0");

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
        // Начинаем с базовой ставки в 25%
        BigDecimal rate = baseRate;
        // Если клиент страхует кредит, то стоимость кредита увеличивается на 30% от запрашиваемой суммы,
        // но ставка падает на два процента
        if (isInsuranceEnabled) {
            rate = rate.subtract(new BigDecimal("0.02"));
        }
        // Если клиент получает зарплату в банке, то ставка падает на процент.
        if (isSalaryClient) {
            rate = rate.subtract(new BigDecimal("0.01"));
        }

        return rate;
    }

    private LoanOfferDto createLoanOfferDto(BigDecimal requestedAmount,
                                            Integer term,
                                            Boolean isInsuranceClient,
                                            Boolean isSalaryClient) {
        BigDecimal rate = calculateRate(isInsuranceClient, isSalaryClient);

        BigDecimal insurancePayment = isInsuranceClient ?
                insuranceRate.multiply(requestedAmount) : BigDecimal.ZERO;
        // Если клиент получает зарплату в банке, то страховка в два раза дешевле (15% вместо 30%)
        if (isSalaryClient && isInsuranceClient) {
            insurancePayment = insurancePayment.divide(new BigDecimal("2"), 100, RoundingMode.HALF_EVEN);
        }

        BigDecimal insuredAmount = requestedAmount.add(insurancePayment);

        // Как лучше округлять?
        BigDecimal monthlyPayment = calculateMonthlyPayment(rate.divide(new BigDecimal("12"), 100, RoundingMode.HALF_EVEN),
                                                                insuredAmount, term);
        BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(term));

        return new LoanOfferDto(insuredAmount.setScale(2, RoundingMode.HALF_EVEN),
                                totalAmount.setScale(2, RoundingMode.HALF_EVEN),
                                term, monthlyPayment.setScale(2, RoundingMode.HALF_EVEN),
                                rate, isInsuranceClient, isSalaryClient);
    }

    private final BigDecimal baseRate;
    private final BigDecimal insuranceRate;

}

class RateComparator implements Comparator<LoanOfferDto> {
    @Override
    public int compare(LoanOfferDto o1, LoanOfferDto o2) {
        return o1.getRate().compareTo(o2.getRate());
    }
}