package ru.neoflex.msdeal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.neoflex.loanissuerlibrary.dto.EmailMessageDto;
import ru.neoflex.loanissuerlibrary.dto.enumeration.EmailTheme;

import java.util.UUID;

@Slf4j
@Service
public class KafkaSenderService {

    private final KafkaTemplate<String, EmailMessageDto> emailKafkaTemplate;

    public KafkaSenderService(KafkaTemplate<String, EmailMessageDto> kafkaTemplate) {
        this.emailKafkaTemplate = kafkaTemplate;
    }

    public void sendFinishRegistrationMessage(UUID statementId, String emailAddress) {
        EmailMessageDto message = EmailMessageDto.builder()
                .theme(EmailTheme.FINISH_REGISTRATION)
                .address(emailAddress)
                .statementId(statementId)
                .text("Please finish registration.")
                .build();

        log.info("Sending an event to finish-registration topic...");
        emailKafkaTemplate.send("finish-registration", message);
    }

    public void sendCreateDocumentsMessage(UUID statementId, String emailAddress) {
        EmailMessageDto message = EmailMessageDto.builder()
                .theme(EmailTheme.CREATE_DOCUMENTS)
                .address(emailAddress)
                .statementId(statementId)
                .text("Do you wish to proceed to create documents?")
                .build();

        log.info("Sending an event to create-documents topic...");
        emailKafkaTemplate.send("create-documents", message);
    }

    public void sendStatementDeniedMessage(UUID statementId, String emailAddress) {
        EmailMessageDto message = EmailMessageDto.builder()
                .theme(EmailTheme.STATEMENT_DENIED)
                .address(emailAddress)
                .statementId(statementId)
                .text("Sorry, we can not loan you that amount of money.")
                .build();

        log.info("Sending an event to statement-denied topic...");
        emailKafkaTemplate.send("statement-denied", message);
    }

    public void sendSendDocumentsMessage(UUID statementId, String emailAddress) {
        EmailMessageDto message = EmailMessageDto.builder()
                .theme(EmailTheme.SEND_DOCUMENTS)
                .address(emailAddress)
                .statementId(statementId)
                .text("Your loan documents are here:")
                .build();

        log.info("Sending an event to send-documents topic...");
        emailKafkaTemplate.send("send-documents", message);
    }

    public void sendSendSesMessage(UUID statementId, String SesCode, String emailAddress) {
        EmailMessageDto message = EmailMessageDto.builder()
                .theme(EmailTheme.SEND_SES)
                .address(emailAddress)
                .statementId(statementId)
                .text("Sign documents with SES code. Your SES code is " + SesCode)
                .build();

        log.info("Sending an event to send-ses topic...");
        emailKafkaTemplate.send("send-ses", message);
    }

    public void sendCreditIssuedMessage(UUID statementId, String emailAddress) {
        EmailMessageDto message = EmailMessageDto.builder()
                .theme(EmailTheme.CREDIT_ISSUED)
                .address(emailAddress)
                .statementId(statementId)
                .text("Credit issued, congratulations")
                .build();

        log.info("Sending an event to credit-issued topic...");
        emailKafkaTemplate.send("credit-issued", message);
    }
}
