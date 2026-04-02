package com.codechievement.foremka.config;

import com.codechievement.foremka.v1.api.ScenarioRepository;
import com.codechievement.foremka.v1.components.FileScenarioRepository;
import java.nio.file.Path;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.codechievement.foremka"})
public class TestConfig {

    @Bean
    ScenarioRepository scenarioRepository() {
        return new FileScenarioRepository(Path.of("test-scenarios.db.json"));
    }
}
