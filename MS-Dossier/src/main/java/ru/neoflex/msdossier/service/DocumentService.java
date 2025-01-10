package ru.neoflex.msdossier.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neoflex.loanissuerlibrary.dto.DocumentDataDto;
import ru.neoflex.loanissuerlibrary.dto.PaymentScheduleElementDto;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DocumentService {

    @Value(value = "${msdossier.documents.path}")
    private String documentsPath;

    public void createCreditDocument(UUID statementId, DocumentDataDto documentDataDto) {
        try {
            Files.createDirectories(Paths.get(documentsPath + "\\" + statementId.toString()));
        } catch (IOException e) {
            log.error("Could not create directory to store documents in: " + e.getMessage());
            e.printStackTrace();
        }

        String filepath = documentsPath + "\\" + statementId.toString() + "\\credit_document.txt";

        try(FileOutputStream fileOutputStream = new FileOutputStream(filepath)) {

            String fullName = documentDataDto.getLastName() + " " + documentDataDto.getFirstName();
            if (documentDataDto.getMiddleName() != null) {
                fullName += " " + documentDataDto.getMiddleName();
            }

            String birthdate = String.format("%s.%s.%s", documentDataDto.getBirthdate().getDayOfMonth(),
                                                         documentDataDto.getBirthdate().getMonthValue(),
                                                         documentDataDto.getBirthdate().getYear());

            String amount = documentDataDto.getCredit().getAmount().toString();
            String term = documentDataDto.getCredit().getTerm().toString();
            String rate = documentDataDto.getCredit().getRate().toString();
            String psk = documentDataDto.getCredit().getPsk().toString();
            String isInsuranceEnabled = (documentDataDto.getCredit().getIsInsuranceEnabled() ? "Да" : "Нет");
            String isSalaryClient = (documentDataDto.getCredit().getIsSalaryClient() ? "Да" : "Нет");

            String body = String.format("""
                    ФИО: %s\n
                    Дата рождения: %s\n
                    Сумма займа: %s рублей\n
                    Длительность: %s месяцев\n
                    Ставка: %s %% годовых\n
                    ПСК: %s рублей\n
                    Застрахован ли кредит: %s\n
                    Получает ли заёмщик зарплату в банке: %s
                    """,
                    fullName,
                    birthdate,
                    amount,
                    term,
                    rate,
                    psk,
                    isInsuranceEnabled,
                    isSalaryClient
                    );

            fileOutputStream.write(body.getBytes());
            log.info("Created credit document file.");
        } catch (FileNotFoundException e) {
            log.error("Could not create credit document file: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Could not create credit document file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createCreditScheduleDocument(UUID statementId, DocumentDataDto documentDataDto) {
        try {
            Files.createDirectories(Paths.get(documentsPath + "\\" + statementId.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String filepath = documentsPath + "\\" + statementId.toString() + "\\credit_schedule.txt";

        try(FileOutputStream fileOutputStream = new FileOutputStream(filepath)) {

            List<PaymentScheduleElementDto> paymentSchedule = documentDataDto.getCredit().getPaymentSchedule();

            for(PaymentScheduleElementDto element : paymentSchedule) {
                String number = element.getNumber().toString();
                String date = element.getDate().toString();
                String totalPayment = element.getTotalPayment().toString();
                String interestPayment = element.getInterestPayment().toString();
                String debtPayment = element.getDebtPayment().toString();
                String remainingDebt = element.getRemainingDebt().toString();

                String body = String.format("""
                                Номер платежа: %s\n
                                Дата платежа: %s\n
                                Общая сумма платежа: %s\n
                                Погашение процентов: %s\n
                                Погашение основного долга: %s\n
                                Остаток долга: %s\n\n
                                """,
                        number,
                        date,
                        totalPayment,
                        interestPayment,
                        debtPayment,
                        remainingDebt);

                fileOutputStream.write(body.getBytes());
                log.info("Created credit schedule document file.");
            }
        } catch (FileNotFoundException e) {
            log.error("Could not create credit schedule file: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Could not create credit schedule file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
