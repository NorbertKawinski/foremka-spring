# cc-foremka-spring

Reusable test-data scenarios for Spring tests.

## What it gives you

- lazy scenario creation
- caching by input
- optional persistence between test runs through `ScenarioRepository`

## Quick start

1. Add the dependency:

```xml
<dependency>
    <groupId>com.codechievement.foremka</groupId>
    <artifactId>cc-foremka-spring</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

2. Define a scenario representation:

```java
import com.codechievement.foremka.v1.api.TestScenario;

public record UserScenario(String username, String email) implements TestScenario {}
```

3. Create a factory
Factory creates the scenario.  
The input is the cache key, so it must have stable `equals()` and `hashCode()`.  

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

4. Create a provider
It exposes API for retrieving scenarios.  
Usually, the inherited logic from ScenarioProvider<> is enough for simple cases.

```java
import com.codechievement.foremka.v1.api.ScenarioProvider;
import org.springframework.stereotype.Component;

@Component
public class UserScenarioProvider extends ScenarioProvider<String, UserScenario> {}
```

5. Use the provider to obtain scenarios:

```java
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestConfig.class)
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
        UserScenario first = userScenarioProvider.get("bob");
        UserScenario second = userScenarioProvider.get("bob");

        assertThat(first, is(sameInstance(second)));
    }
}
```

The first `get()` call creates the scenario.  
Later calls with the same input return the same instance.

## Persistence

Foremka loads saved scenarios on startup and writes the cache back on shutdown.
Choose storage by registering a `ScenarioRepository` bean.

### In-memory (default)

`InMemoryScenarioRepository` is registered automatically when you do not provide another repository.
It keeps data only in memory, so nothing survives a JVM restart.

### File

`FileScenarioRepository` stores the cache as JSON on disk:

```java
import com.codechievement.foremka.v1.components.FileScenarioRepository;
import java.nio.file.Path;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    FileScenarioRepository scenarioRepository() {
        return new FileScenarioRepository(Path.of("test-scenarios.db.json"));
    }
}
```

Parent directories are created automatically.

### Database (JDBC)

`DatabaseScenarioRepository` stores the cache in a custom table.  
The table is created automatically so no extra setup beyond DataSource is needed.  
This option is recommended for local development environments that already have a database, because the scenario data is stored (and cleared) alongside the rest of the data

```java
import com.codechievement.foremka.v1.components.DatabaseScenarioRepository;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    DatabaseScenarioRepository scenarioRepository(DataSource dataSource) {
        return new DatabaseScenarioRepository(dataSource);
    }
}
```

The default table name is `FOREMKA_TEST_SCENARIO`. You can override it with:

```java
var repository = new DatabaseScenarioRepository(dataSource, "my_test_scenarios");
```

### Custom storage

Implement `ScenarioRepository` and register it as a Spring bean:

```java
import com.codechievement.foremka.v1.api.ScenarioRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MyScenarioRepository implements ScenarioRepository {

    @Override
    public Optional<String> findAll() {
        return Optional.empty();
    }

    @Override
    public void saveAll(String data) {
        // save serialized JSON
    }
}
```

## Cleanup

Set `CLEANUP_TEST_SCENARIOS=true` environment variable to remove scenarios that were not used during a test run.
This is best enabled for full test-suite runs, not for selective test execution, to avoid removing scenarios for tests that simply did not run.

### Gradle configuration

```kotlin
tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("CLEANUP_TEST_SCENARIOS", "true")
}
```

### One-off run

```bash
./gradlew test -DCLEANUP_TEST_SCENARIOS=true
```
