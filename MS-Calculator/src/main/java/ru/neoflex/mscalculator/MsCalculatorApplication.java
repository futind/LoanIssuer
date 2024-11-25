package ru.neoflex.mscalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:service.properties")
public class MsCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsCalculatorApplication.class, args);
    }

}
