package com.codechievement.foremka.config;

import com.codechievement.foremka.v1.api.ScenarioRepository;
import com.codechievement.foremka.v1.components.InMemoryScenarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(basePackages = "com.codechievement.foremka")
public class ForemkaAutoConfiguration {

    @Bean
    @Fallback
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Fallback
    ScenarioRepository scenarioRepository() {
        return new InMemoryScenarioRepository();
    }
}
