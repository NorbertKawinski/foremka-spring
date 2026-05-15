package com.codechievement.foremka.v1.fixture;

import com.codechievement.foremka.v1.api.ScenarioFactory;
import java.util.function.Consumer;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class UserScenarioFactory implements ScenarioFactory<String, UserScenario> {
    @Setter
    private Consumer<UserScenario> onUserCreated;

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
        var result = new UserScenario(id, username, email, role);

        if (onUserCreated != null) {
            onUserCreated.accept(result);
        }
        return result;
    }
}
