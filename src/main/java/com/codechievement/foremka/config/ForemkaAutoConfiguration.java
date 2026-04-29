package com.codechievement.foremka.config;

import com.codechievement.foremka.v1.api.ScenarioRepository;
import com.codechievement.foremka.v1.components.InMemoryScenarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(basePackages = "com.codechievement.foremka")
public class ForemkaAutoConfiguration {

    @Bean
    @Fallback
    ObjectMapper foremkaFallbackObjectMapper() {
        return createObjectMapper();
    }

    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    @Fallback
    ScenarioRepository foremkaFallbackScenarioRepository() {
        return new InMemoryScenarioRepository();
    }
}
