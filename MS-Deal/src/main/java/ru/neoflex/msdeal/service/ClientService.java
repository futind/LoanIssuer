package ru.neoflex.msdeal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.neoflex.msdeal.dto.FinishRegistrationRequestDto;
import ru.neoflex.msdeal.dto.LoanStatementRequestDto;
import ru.neoflex.msdeal.dto.PassportDto;
import ru.neoflex.msdeal.model.ClientEntity;
import ru.neoflex.msdeal.repository.ClientRepository;

import java.util.UUID;

@Slf4j
@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public ClientEntity createClientWithRequest(LoanStatementRequestDto request) {
        ClientEntity clientEntity = new ClientEntity();

        clientEntity.setFirstName(request.getFirstName());
        clientEntity.setLastName(request.getLastName());
        clientEntity.setMiddleName(request.getMiddleName());
        clientEntity.setEmail(request.getEmail());
        clientEntity.setBirthDate(request.getBirthdate());

        clientEntity.setPassport(PassportDto.builder()
                .series(request.getPassportSeries())
                .number(request.getPassportNumber())
                .build());

        log.info("""
                Created new client with the data from the statement request. \
                Saving the client entity into the database...
                """);
        return clientRepository.save(clientEntity);
    }

    @Transactional
    public ClientEntity enrichClient(FinishRegistrationRequestDto finishRegistrationRequestDto, UUID clientId)
                                                                       throws EntityNotFoundException {
        ClientEntity clientEntity = clientRepository.findById(clientId)
                                                    .orElseThrow(EntityNotFoundException::new);

        clientEntity.setGender(finishRegistrationRequestDto.getGender());
        clientEntity.setMaritalStatus(finishRegistrationRequestDto.getMaritalStatus());
        clientEntity.setDependentAmount(finishRegistrationRequestDto.getDependentAmount());
        clientEntity.setAccountNumber(finishRegistrationRequestDto.getAccountNumber());

        clientEntity.getPassport().setIssueBranch(finishRegistrationRequestDto.getPassportIssueBranch());
        clientEntity.getPassport().setIssueDate(finishRegistrationRequestDto.getPassportIssueDate());

        clientEntity.setEmployment(finishRegistrationRequestDto.getEmployment());


        log.info("""
                Enriched client with the information from finishing registration request form. \
                Updating the client in the database...""");
        return clientRepository.save(clientEntity);
    }

}
