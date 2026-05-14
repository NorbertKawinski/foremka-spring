# Quick start

## Setup

1. Add the dependency:

```xml
<dependency>
    <groupId>com.codechievement.foremka</groupId>
    <artifactId>foremka-spring</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

## Basic usage

1. Define a scenario representation:

```java
import com.codechievement.foremka.v1.api.TestScenario;

// Represents a fully-provisioned user in the system.
public record UserScenario(String id, String username, String email, String role) implements TestScenario {}
```

2. Create a factory

The factory creates scenarios.  
The input serves as the cache key, so it must have stable `equals()` and `hashCode()`.  

```java
import com.codechievement.foremka.v1.api.ScenarioFactory;
import org.springframework.stereotype.Component;

@Component
public class UserScenarioFactory implements ScenarioFactory<String, UserScenario> {
    @Override
    public Class<UserScenario> getScenarioClass() {
        return UserScenario.class;
    }

    /**
     * Creates a user for the given username.
     * In a real setup this method would INSERT a row into the users table, generating an ID
     * and assigning the default role — steps that may take hundreds of milliseconds.
     */
    @Override
    public UserScenario create(String username) {
        // Deterministic hex hash of the username — same input always produces the same ID,
        // which is required for scenario restore to work correctly.
        String id = String.format("user-%08x", username.hashCode());
        String email = username + "@example.com";
        String role = "USER";
        return new UserScenario(id, username, email, role);
    }
}
```

3. Create a provider

The provider exposes the API for retrieving scenarios.  
For simple cases, the default inherited logic is sufficient.  

```java
import com.codechievement.foremka.v1.api.ScenarioProvider;
import org.springframework.stereotype.Component;

@Component
public class UserScenarioProvider extends ScenarioProvider<String, UserScenario> {}
```

4. Use the provider to obtain scenarios:

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

        assertThat(alice.id(), startsWith("user-"));
        assertThat(alice.username(), is("alice"));
        assertThat(alice.email(), is("alice@example.com"));
        assertThat(alice.role(), is("USER"));
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

## Complex input keys

Scenario inputs are used as cache keys, so any type with stable `equals()` and `hashCode()` works — including records with multiple fields.

Suppose you want to model products in an e-commerce catalogue whose discounted price is computed by a pricing engine at creation time (simulating a slow external call):

1. Define scenario input and representation:

```java
import com.codechievement.foremka.v1.api.TestScenario;

// Input record — all three fields together are the cache key.
// Two inputs are identical only when every field matches.
public record ProductInput(String name, String category, int basePrice) {}

// Scenario record — stores the product as it was persisted, including the computed price.
public record ProductScenario(String id, String name, String category, int basePrice, int discountedPrice)
        implements TestScenario {}
```

2. Implement factory

```java
import com.codechievement.foremka.v1.api.ScenarioFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductScenarioFactory implements ScenarioFactory<ProductInput, ProductScenario> {
    @Override
    public Class<ProductScenario> getScenarioClass() {
        return ProductScenario.class;
    }

    /**
     * Creates a product scenario for the given input.
     * In a real setup this method would call the product catalogue service and a pricing engine
     * — both of which can take several seconds. Here the behavior is simulated.
     *
     * Discount rules: SEASONAL → 30 % off; all other categories → 10 % off.
     */
    @Override
    public ProductScenario create(ProductInput input) {
        // Deterministic hex hash of the full input — same (name, category, basePrice) always
        // produces the same ID, which is required for scenario restore to work correctly.
        String id = String.format("prod-%08x", input.hashCode());
        int discountedPrice = input.category().equals("SEASONAL")
                ? (int) (input.basePrice() * 0.70)
                : (int) (input.basePrice() * 0.90);
        return new ProductScenario(id, input.name(), input.category(), input.basePrice(), discountedPrice);
    }
}
```

3. Create provider

```java
import com.codechievement.foremka.v1.api.ScenarioProvider;
import org.springframework.stereotype.Component;

@Component
public class ProductScenarioProvider extends ScenarioProvider<ProductInput, ProductScenario> {}
```

4. Use the provider to obtain scenarios

Pass a full input record instead of a plain string:

```java
@Test
void productScenario_seasonalDiscount() {
    // SEASONAL → 30 % off: 5000 * 0.70 = 3500
    ProductScenario jacket = productScenarioProvider.get(new ProductInput("Winter Jacket", "SEASONAL", 5000));

    assertThat(jacket.id(), startsWith("prod-"));
    assertThat(jacket.basePrice(), is(5000));
    assertThat(jacket.discountedPrice(), is(3500));
}

@Test
void differentInputsProduceDifferentScenarios() {
    // SEASONAL: 30 % off
    ProductScenario jacket = productScenarioProvider.get(new ProductInput("Winter Jacket", "SEASONAL", 5000));
    // ELECTRONICS: 10 % off
    ProductScenario laptop = productScenarioProvider.get(new ProductInput("Laptop", "ELECTRONICS", 120000));

    assertThat(jacket.discountedPrice(), is(3500));
    assertThat(laptop.discountedPrice(), is(108000));
}
```

Different input values produce separate cached scenarios; the same values always return the same instance.

## Run statistics

After every test suite run, Foremka prints a summary to SLF4J at `INFO` level when the Spring context shuts down:

```
Test suite run statistics: cacheHits=12, cacheMisses=3, savedTime=PT4.521S, numUnusedScenarios=1
```

| Field                | Meaning                                                                     |
|----------------------|-----------------------------------------------------------------------------|
| `cacheHits`          | Scenarios returned from cache (factory was **not** called)                  |
| `cacheMisses`        | Scenarios freshly created by the factory                                    |
| `savedTime`          | Time saved by skipping factory calls (including subsequent scenario reuses) |
| `numUnusedScenarios` | Scenarios present in the cache but not accessed in this run                 |

You can also access the statistics programmatically during a test run:

```java
@Autowired
private TestScenarios testScenarios;

@Test
void checkRunStatistics() {
    TestSuiteRunStatistics stats = testScenarios.getSummaryStatistics();

    System.out.println("Cache hits:   " + stats.cacheHits());
    System.out.println("Cache misses: " + stats.cacheMisses());
    System.out.println("Saved time:   " + stats.savedTime());
    System.out.println("Unused:       " + stats.numUnusedScenarios());
}
```

## Persistence

Foremka loads saved scenarios on startup and writes the cache back on shutdown.
Choose storage by registering a `ScenarioRepository` bean.

### In-memory (default)

`InMemoryScenarioRepository` is automatically registered if you don't provide another repository.  
It keeps all data in memory only, so nothing persists across JVM restarts.  

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

`DatabaseScenarioRepository` stores scenarios in a custom table.  
The table is created automatically, so only a DataSource is needed.  
This option is recommended for local development environments that already have a database, since scenario data is stored and cleared alongside the rest of your data.  

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

Set `CLEANUP_TEST_SCENARIOS=true` to remove unused scenarios after a test run.  
This is best used for complete test-suite runs, not selective test execution, as it may delete scenarios for tests that didn't run.  

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
