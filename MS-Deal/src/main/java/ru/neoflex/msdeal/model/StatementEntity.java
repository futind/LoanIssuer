package ru.neoflex.msdeal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.neoflex.msdeal.dto.LoanOfferDto;
import ru.neoflex.msdeal.dto.StatusHistoryDto;
import ru.neoflex.msdeal.dto.enumeration.ApplicationStatus;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_statement")
public class StatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID statementId;

    @OneToOne(targetEntity = ClientEntity.class)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @OneToOne(targetEntity = CreditEntity.class)
    @JoinColumn(name = "credit_id", nullable = true)
    private CreditEntity credit;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "applied_offer", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private LoanOfferDto appliedOffer;

    @Column(name = "sign_date", nullable = true)
    private LocalDateTime signDate;

    @Column(name = "ses_code", nullable = true)
    private String sesCode;

    @Column(name = "status_history", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<StatusHistoryDto> statusHistory;
}
