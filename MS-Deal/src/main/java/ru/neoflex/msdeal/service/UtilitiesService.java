package ru.neoflex.msdeal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UtilitiesService {

    public String generateSesCode() {
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < 6; ++i) {
            stringBuilder.append((int) (Math.random() * 10));
        }

        log.info("Generated SES-code.");
        return stringBuilder.toString();
    }

}
