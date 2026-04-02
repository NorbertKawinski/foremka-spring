package com.codechievement.foremka.v1.fixture;

import com.codechievement.foremka.v1.api.ScenarioFactory;
import org.springframework.stereotype.Component;

@Component
public class UserScenarioFactory implements ScenarioFactory<String, UserScenario> {
    @Override
    public Class<UserScenario> getScenarioClass() {
        return UserScenario.class;
    }

    @Override
    public UserScenario create(String username) {
        return new UserScenario(username, username + "@test.com");
    }
}
