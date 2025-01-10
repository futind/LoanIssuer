package ru.neoflex.msdeal.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilitiesServiceTest {

    private final UtilitiesService utilitiesService;

    public UtilitiesServiceTest() {
        this.utilitiesService = new UtilitiesService();
    }

    @Test
    void SesCodeGenerationReturnsAStringWith6Digits() {
        String code = utilitiesService.generateSesCode();

        assertTrue(code.matches("\\d{6}"));
    }
}
