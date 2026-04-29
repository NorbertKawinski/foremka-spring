# cc-foremka-spring

Reusable test-data scenarios for Spring tests.

## Benefits

- **Faster test suites** — scenarios are created lazily and cached by input, so expensive setup (e.g. database inserts, API calls) only runs once per unique input, no matter how many tests share that scenario.
- **Speed up local development** — persist scenarios between test runs via `ScenarioRepository`. On the second run, scenarios are restored from storage instead of being re-created, dramatically reducing iteration time.
- **Zero boilerplate reuse** — just call `provider.get("alice")` from any test. The same object is returned every time for the same input; no manual caching or static fields needed.
- **Pluggable storage** — swap between in-memory, file-based, or database storage with a single bean. Bring your own `ScenarioRepository` if you need something custom.
- **Visibility into your test suite** — run statistics (cache hits/misses, time saved, unused scenarios) are logged on shutdown so you can spot waste and optimise confidently.
- **Plays well with Spring** — auto-configured via Spring Boot's auto-configuration mechanism; integrates naturally with `@SpringJUnitConfig` and the standard application context lifecycle.
- **Minimal API surface** — one provider class per scenario type, one factory, one optional repository. Easy to onboard a team that has never seen the library before.

## Tradeoffs

- **Scenario invariants** — Every scenario defines assumptions about its output. Example: If you run a shop and create a scenario with a "promotional price" product, that product should always have a discount applied. Feel free to modify other fields that are not part of the invariants.

## Simple usage example

For full guide (dependency, factory, provider, repository), see `QUICKSTART.md`.

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

See `QUICKSTART.md` for the complete usage guide.

## Troubleshooting

See `TROUBLESHOOTING.md` for common issues.

## Roadmap

1. Incremental saving of scenarios to database
  * Currently only on shutdown
  * Make each scenario an independent record. 
  * Use background thread for synchronization (batch inserts).
  * Use delete by id for cleanup
2. Locking scenarios for exclusive access
  * Example: Two concurrent tests that reuse the same ProductScenario. 
  * One modifies the product category, the other modifies the product price.
  * Scenario invariants make no assumptions about either of these changes.
  * Both tests can reuse this scenario, but not at the same time.
3. Improve concurrency support. Ex: TestScenarioMeta is not thread safe right now.
