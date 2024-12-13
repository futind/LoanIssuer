package ru.neoflex.msdeal.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi DealOpenAPI() {
        return GroupedOpenApi.builder()
                .group("ms_deal")
                .pathsToMatch("/deal/**")
                .build();
    }
}