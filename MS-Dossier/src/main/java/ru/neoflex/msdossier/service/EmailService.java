package ru.neoflex.msdossier.service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.neoflex.loanissuerlibrary.dto.EmailMessageDto;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class EmailService {

    @Value(value = "${msdossier.documents.path}")
    private String filepath;

    private JavaMailSenderImpl mailSender;

    public EmailService(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setHost("smtp.gmail.com");
    }

    public void sendEmail(EmailMessageDto emailMessageDto, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("neoflexBanking@gmail.com");
        message.setTo(emailMessageDto.getAddress());
        message.setSubject(subject);
        message.setText(emailMessageDto.getText());

        try {
            log.info("Sending email to " + emailMessageDto.getAddress() + " with theme " + emailMessageDto.getTheme());
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Could not send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendEmailWithAttachments(EmailMessageDto emailMessageDto, String subject)
                                                    throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();

        message.setFrom("neoflexBanking@gmail.com");
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(emailMessageDto.getAddress()));
        message.setSubject(subject);

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(emailMessageDto.getText());

        String creditDocumentPath = filepath + File.separator
                                    + emailMessageDto.getStatementId().toString() + File.separator
                                    + "credit_document.txt";
        String paymentSchedulePath = filepath + File.separator
                                     + emailMessageDto.getStatementId().toString() + File.separator
                                     + "credit_schedule.txt";
        log.info(paymentSchedulePath);

        MimeBodyPart creditAttachmentBodyPart = new MimeBodyPart();
        creditAttachmentBodyPart.attachFile(new File(creditDocumentPath));

        MimeBodyPart scheduleAttachmentBodyPart = new MimeBodyPart();
        scheduleAttachmentBodyPart.attachFile(new File(paymentSchedulePath));

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(creditAttachmentBodyPart);
        multipart.addBodyPart(scheduleAttachmentBodyPart);

        message.setContent(multipart);

        try {
            log.info("Sending email with attachments to " + emailMessageDto.getAddress());
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Could not send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
