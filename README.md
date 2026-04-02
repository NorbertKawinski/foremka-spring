# cc-foremka-spring 

A framework for managing test data in Spring applications.


# Features

* Define test data scenarios using Spring-injectable beans
* Improve test performance by reusing test data across multiple test cases
* Persist scenarios between test runs using pluggable storage backends


# Usage

1. Add the dependency to your project:

```xml
<dependency>
    <groupId>com.codechievement.foremka</groupId>
    <artifactId>cc-foremka-spring</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
</dependency>
```

2. Define a scenario representation that implements `TestScenario`:

```java
import com.codechievement.foremka.v1.api.TestScenario;

public record UserScenario(String username, String email) implements TestScenario {
}
```

3. Create a factory that implements `ScenarioFactory` to build your scenario from an input:

```java
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
```

Please note that the input, for caching purposes, should have proper `equals()` and `hashCode()` implementations, which is the case for `String` in this example.

4. Create a provider by extending `ScenarioProvider` which is the main interface for obtaining scenario instances in your tests:

```java
import com.codechievement.foremka.v1.api.ScenarioProvider;
import org.springframework.stereotype.Component;

@Component
public class UserScenarioProvider extends ScenarioProvider<String, UserScenario> {
}
```

5. Inject and use the provider to obtain scenario instances:

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.junit.jupiter.api.Test;

@SpringJUnitConfig
class MyIntegrationTest {

    @Autowired
    private UserScenarioProvider userScenarioProvider;

    @Test
    void testWithScenario() {
        UserScenario alice = userScenarioProvider.get("alice");

        assertThat(alice.username(), is("alice"));
        assertThat(alice.email(), is("alice@test.com"));
    }

    @Test
    void scenariosAreCachedByInput() {
        UserScenario first  = userScenarioProvider.get("bob");
        UserScenario second = userScenarioProvider.get("bob");

        assertThat(first.username(), is("bob"));
        assertThat(first, is(sameInstance(second)));
    }
}
```

Scenarios are created lazily on the first call to `get()` and cached by their input,  
so every subsequent call with the same input returns the same instance.  
Different inputs produce different scenario instances.


# Persistence

Scenarios are loaded from the repository on startup and saved back on shutdown, so the same
scenario instances can be reused across multiple test runs without re-creating them.

The persistence backend is determined by which `ScenarioRepository` bean is present in the
Spring context. Three built-in implementations are available:

## In-Memory (default)

`InMemoryScenarioRepository` is registered automatically as a `@Fallback` bean when no other
`ScenarioRepository` is provided. It stores the serialized cache in a plain `String` field, so
**data is lost when the JVM exits**. This is the zero-configuration default and is sufficient
when scenario reuse within a single test run is all that is needed.

No extra setup is required — the auto-configuration registers it for you.

## File

`FileScenarioRepository` writes the serialized cache to a JSON file on disk. The file is read at
startup and written at shutdown, so scenarios survive JVM restarts.

Register it as a bean in your test configuration, pointing it at the desired file path:

```java
import com.codechievement.foremka.v1.components.FileScenarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Path;

@Configuration
public class TestConfig {

    @Bean
    FileScenarioRepository scenarioRepository() {
        return new FileScenarioRepository(Path.of("test-scenarios.db.json"));
    }
}
```

The parent directories are created automatically if they do not exist. The file path can be
absolute or relative to the working directory.

## Database (JDBC)

`DatabaseScenarioRepository` stores the serialized cache in a single-row JDBC table. The table
is created automatically on startup if it does not yet exist. This option is recommended for
local development environments that already have a database, because the scenario data is cleared
automatically whenever the database is reset.

Register it as a bean and pass in a `DataSource`:

```java
import com.codechievement.foremka.v1.components.DatabaseScenarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
public class TestConfig {

    @Bean
    DatabaseScenarioRepository scenarioRepository(DataSource dataSource) {
        return new DatabaseScenarioRepository(dataSource);
    }
}
```

The default table name is `foremka_test_scenarios`. A custom name can be supplied via the
two-argument constructor — it must match `[a-zA-Z0-9_]+`:

```java
new DatabaseScenarioRepository(dataSource, "my_test_scenarios")
```

## Custom persistence

Implement the `ScenarioRepository` interface and register the implementation as a Spring bean:

```java
import com.codechievement.foremka.v1.api.ScenarioRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class MyScenarioRepository implements ScenarioRepository {

    @Override
    public Optional<String> findAll() {
        // load serialized JSON string from your storage
    }

    @Override
    public void saveAll(String data) {
        // write serialized JSON string to your storage
    }
}
```
