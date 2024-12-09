package ru.neoflex.msdeal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.msdeal.dto.CreditDto;
import ru.neoflex.msdeal.dto.enumeration.CreditStatus;
import ru.neoflex.msdeal.model.CreditEntity;
import ru.neoflex.msdeal.repository.CreditRepository;

import java.util.UUID;

@Slf4j
@Service
public class CreditService {

    private final CreditRepository creditRepository;

    public CreditService(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    public CreditEntity findById(UUID creditId) throws EntityNotFoundException {
        return creditRepository.findById(creditId).orElseThrow(EntityNotFoundException::new);
    }

    public CreditEntity saveCredit(CreditDto creditDto) {
        CreditEntity creditEntity = new CreditEntity();

        creditEntity.setAmount(creditDto.getAmount());
        creditEntity.setTerm(creditDto.getTerm());
        creditEntity.setMonthlyPayment(creditDto.getMonthlyPayment());
        creditEntity.setRate(creditDto.getRate());
        creditEntity.setPsk(creditDto.getPsk());
        creditEntity.setPaymentSchedule(creditDto.getPaymentSchedule());
        creditEntity.setIsInsuranceEnabled(creditDto.getIsInsuranceEnabled());
        creditEntity.setIsSalaryClient(creditDto.getIsSalaryClient());
        creditEntity.setCreditStatus(CreditStatus.CALCULATED);

        log.info("Saving the credit to the database...");
        return creditRepository.save(creditEntity);
    }
}
