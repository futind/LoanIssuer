package ru.neoflex.msdossier.service;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.neoflex.loanissuerlibrary.dto.DocumentDataDto;
import ru.neoflex.loanissuerlibrary.dto.EmailMessageDto;

import java.io.IOException;

@Slf4j
@Service
public class KafkaListenerService {

    private EmailService emailService;
    private RestClientService restClientService;
    private DocumentService documentService;

    public KafkaListenerService(EmailService emailService,
                                RestClientService restClientService,
                                DocumentService documentService) {
        this.emailService = emailService;
        this.restClientService = restClientService;
        this.documentService = documentService;
    }

    @KafkaListener(topics = "finish-registration", groupId = "dossier")
    public void sendFinishRegistrationMessage(EmailMessageDto message) {
        log.info("Got an event in finish-registration topic.");
        emailService.sendEmail(message, "Finish your registration.");
    }

    @KafkaListener(topics = "create-documents", groupId = "dossier")
    public void sendCreateDocumentsMessage(EmailMessageDto message) {
        log.info("Got an event in create-documents topic.");
        emailService.sendEmail(message, "Create documents.");
    }

    @KafkaListener(topics = "send-documents", groupId = "dossier")
    public void sendSendDocumentsMessage(EmailMessageDto message) throws MessagingException, IOException {
        log.info("Got an event in send-documents topic.");

        log.info("Proceeding to GET documentDataDto");
        DocumentDataDto documentDataDto = restClientService.getDocumentData(message.getStatementId());
        log.info("Creating credit document file...");
        documentService.createCreditDocument(message.getStatementId(), documentDataDto);
        log.info("Creating payment schedule file...");
        documentService.createCreditScheduleDocument(message.getStatementId(), documentDataDto);
        log.info("Putting DOCUMENT_CREATED through REST-client...");
        restClientService.putDocumentsCreatedStatus(message.getStatementId());

        log.info("Documents created, status changed. Proceeding to send email.");
        emailService.sendEmailWithAttachments(message, "Your loan documents.");
    }

    @KafkaListener(topics = "send-ses", groupId = "dossier")
    public void sendSendSesMessage(EmailMessageDto message) {
        log.info("Got an event in send-ses topic.");
        emailService.sendEmail(message, "Confirm your agreement.");
    }

    @KafkaListener(topics = "credit-issued", groupId = "dossier")
    public void sendCreditIssuedMessage(EmailMessageDto message) {
        log.info("Got an event in credit-issued topic.");
        emailService.sendEmail(message, "Your credit issued.");
    }

    @KafkaListener(topics = "statement-denied", groupId = "dossier")
    public void sendStatementDeniedMessage(EmailMessageDto message) {
        log.info("Got an event in statement-denied topic.");
        emailService.sendEmail(message, "Your credit has been denied.");
    }
}
