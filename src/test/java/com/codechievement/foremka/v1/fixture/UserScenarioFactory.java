package com.codechievement.foremka.v1.fixture;

import com.codechievement.foremka.v1.api.ScenarioFactory;
import org.springframework.stereotype.Component;

@Component
public class UserScenarioFactory implements ScenarioFactory<String, UserScenario> {
    @Override
    public Class<UserScenario> getScenarioClass() {
        return UserScenario.class;
    }

    /** Simulates a DB user INSERT: generates a hash-based ID, derives email, and assigns role. */
    @Override
    public UserScenario create(String username) {
        String id = String.format("user-%08x", username.hashCode());
        String email = username + "@example.com";
        String role = "USER";
        return new UserScenario(id, username, email, role);
    }
}
