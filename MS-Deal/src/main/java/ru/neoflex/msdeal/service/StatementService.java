package ru.neoflex.msdeal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.neoflex.loanissuerlibrary.dto.*;
import ru.neoflex.loanissuerlibrary.dto.enumeration.ApplicationStatus;
import ru.neoflex.loanissuerlibrary.dto.enumeration.ChangeType;
import ru.neoflex.msdeal.model.ClientEntity;
import ru.neoflex.msdeal.model.CreditEntity;
import ru.neoflex.msdeal.model.StatementEntity;
import ru.neoflex.msdeal.repository.StatementRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
public class StatementService {
    private final StatementRepository statementRepository;

    public StatementService(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    public StatementEntity findById(UUID id) throws EntityNotFoundException {
        return statementRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public ClientEntity findClientByStatementId(UUID id) throws EntityNotFoundException {
        return findById(id).getClient();
    }

    public String getSesByStatementId(UUID id) throws EntityNotFoundException {
        StatementEntity statementEntity = findById(id);
        return statementEntity.getSesCode();
    }

    public Boolean isDenied(UUID id) throws EntityNotFoundException {
        StatementEntity statementEntity = findById(id);
        return statementEntity.getStatus().equals(ApplicationStatus.CC_DENIED);
    }

    @Transactional
    public StatementEntity issueCredit(UUID id) throws EntityNotFoundException {
        StatementEntity statementEntity = findById(id);

        log.info("Updating Sign Date of the statement and its status.");
        changeStatementStatus(statementEntity, ApplicationStatus.CREDIT_ISSUED);
        statementEntity.setSignDate(LocalDateTime.now());
        return statementRepository.save(statementEntity);
    }

    @Transactional
    public StatementEntity updateSesCode(UUID id, String SesCode) throws EntityNotFoundException {
        StatementEntity statementEntity = statementRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        statementEntity.setSesCode(SesCode);

        log.info("Updating SES-code.");
        return statementRepository.save(statementEntity);
    }

    @Transactional
    public StatementEntity createStatementWithClient(ClientEntity clientEntity) {
        StatementEntity statementEntity = new StatementEntity();

        statementEntity.setClient(clientEntity);
        statementEntity.setCreationDate(LocalDateTime.now());

        changeStatementStatus(statementEntity, ApplicationStatus.PREAPPROVAL);

        log.info("Created new statement with data from request.");

        return statementRepository.save(statementEntity);
    }

    @Transactional
    public void changeStatementStatus(StatementEntity statementEntity, ApplicationStatus status) {

        ApplicationStatus oldStatus = statementEntity.getStatus();
        if (statementEntity.getStatusHistory() == null) {
            statementEntity.setStatusHistory(new ArrayList<StatusHistoryDto>());
        }

        statementEntity.setStatus(status);
        statementEntity.getStatusHistory().add(StatusHistoryDto.builder()
                        .status(status)
                        .timestamp(LocalDateTime.now())
                        .changeType(ChangeType.AUTOMATIC)
                        .build());

        statementRepository.save(statementEntity);
        log.info("Changed statement status from {} to {}. Saved the statement.", oldStatus, status);
    }

    @Transactional
    public StatementEntity setAppliedOffer(LoanOfferDto offer) throws EntityNotFoundException {
        StatementEntity statementEntity = findById(offer.getStatementId());

        statementEntity.setAppliedOffer(offer);
        changeStatementStatus(statementEntity, ApplicationStatus.APPROVED);

        log.info("Set the applied offer, saving updated statement...");
        return statementRepository.save(statementEntity);
    }

    public ScoringDataDto enrichScoringData(FinishRegistrationRequestDto finishingRequest, UUID statementId)
                                                                                throws EntityNotFoundException{
        StatementEntity statementEntity = statementRepository.findById(statementId)
                .orElseThrow(EntityNotFoundException::new);

        log.info("Creating ScoringDataDto...");
        return ScoringDataDto.builder()
                .amount(statementEntity.getAppliedOffer().getRequestedAmount())
                .term(statementEntity.getAppliedOffer().getTerm())
                .firstName(statementEntity.getClient().getFirstName())
                .lastName(statementEntity.getClient().getLastName())
                .middleName(statementEntity.getClient().getMiddleName())
                .birthdate(statementEntity.getClient().getBirthDate())
                .gender(finishingRequest.getGender())
                .passportIssueDate(finishingRequest.getPassportIssueDate())
                .passportIssueBranch(finishingRequest.getPassportIssueBranch())
                .passportSeries(statementEntity.getClient().getPassport().getSeries())
                .passportNumber(statementEntity.getClient().getPassport().getNumber())
                .maritalStatus(finishingRequest.getMaritalStatus())
                .dependentAmount(finishingRequest.getDependentAmount())
                .employment(finishingRequest.getEmployment())
                .accountNumber(finishingRequest.getAccountNumber())
                .isInsuranceEnabled(statementEntity.getAppliedOffer().getIsInsuranceEnabled())
                .isSalaryClient(statementEntity.getAppliedOffer().getIsSalaryClient())
                .build();
    }

    public DocumentDataDto enrichDocumentData(UUID statementId) throws EntityNotFoundException {
        StatementEntity statementEntity = findById(statementId);
        CreditEntity creditEntity = statementEntity.getCredit();
        ClientEntity clientEntity = statementEntity.getClient();

        CreditDto creditDto = CreditDto.builder()
                .amount(creditEntity.getAmount())
                .term(creditEntity.getTerm())
                .monthlyPayment(creditEntity.getMonthlyPayment())
                .rate(creditEntity.getRate())
                .psk(creditEntity.getPsk())
                .paymentSchedule(creditEntity.getPaymentSchedule())
                .isInsuranceEnabled(creditEntity.getIsInsuranceEnabled())
                .isSalaryClient(creditEntity.getIsSalaryClient())
                .build();

        log.info("Creating DocumentDataDto...");
        return DocumentDataDto.builder()
                .credit(creditDto)
                .firstName(clientEntity.getFirstName())
                .middleName(clientEntity.getMiddleName())
                .lastName(clientEntity.getLastName())
                .birthdate(clientEntity.getBirthDate())
                .build();
    }

    @Transactional
    public StatementEntity setCredit(UUID statementId, CreditEntity creditEntity)
                                                                        throws EntityNotFoundException {
        StatementEntity statementEntity = findById(statementId);
        statementEntity.setCredit(creditEntity);

        log.info("Set the credit to the statement. Saving the statement...");

        return statementRepository.save(statementEntity);
    }
}
