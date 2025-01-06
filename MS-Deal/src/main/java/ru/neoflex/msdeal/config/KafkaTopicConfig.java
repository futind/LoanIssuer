package ru.neoflex.msdeal.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic finishRegistrationTopic() {
        return new NewTopic("finish-registration",1, (short) 1);
    }

    @Bean
    public NewTopic createDocumentsTopic() {
        return new NewTopic("create-documents", 1, (short) 1);
    }

    @Bean
    public NewTopic sendDocumentsTopic() {
        return new NewTopic("send-documents", 1, (short) 1);
    }

    @Bean
    public NewTopic sendSesTopic() {
        return new NewTopic("send-ses", 1, (short) 1);
    }

    @Bean
    public NewTopic creditIssuedTopic() {
        return new NewTopic("credit-issued", 1, (short) 1);
    }

    @Bean
    public NewTopic statementDeniedTopic() {
        return new NewTopic("statement-denied", 1, (short) 1);
    }
}
