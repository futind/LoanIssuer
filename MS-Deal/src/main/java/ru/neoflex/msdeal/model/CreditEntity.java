package ru.neoflex.msdeal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.neoflex.loanissuerlibrary.dto.PaymentScheduleElementDto;
import ru.neoflex.loanissuerlibrary.dto.enumeration.CreditStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit")
public class CreditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID creditId;

    @Column(name = "amount", nullable = false)
    BigDecimal amount;

    @Column(name = "term", nullable = false)
    Integer term;

    @Column(name = "monthly_payment", nullable = false)
    BigDecimal monthlyPayment;

    @Column(name = "rate", nullable = false)
    BigDecimal rate;

    @Column(name = "psk", nullable = false)
    BigDecimal psk;

    @Column(name = "payment_schedule", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    List<PaymentScheduleElementDto> paymentSchedule;

    @Column(name = "is_insurance_enabled", nullable = false)
    Boolean isInsuranceEnabled;

    @Column(name = "is_salary_client", nullable = false)
    Boolean isSalaryClient;

    @Column(name = "credit_status", nullable = false)
    @Enumerated(EnumType.STRING)
    CreditStatus creditStatus;
}
