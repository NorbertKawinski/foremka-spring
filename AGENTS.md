# AGENTS.md

## What this project is
- `foremka-spring` is a Spring library for reusable test-data scenarios.
- Core idea: scenario objects are created lazily, cached by input, and persisted between test runs.
- Code lives in `src/main/java/com/codechievement/foremka`.

## Architecture (read these first)
- `TestScenarios` is the orchestrator: in-memory cache + lifecycle persistence.
- `ScenarioProvider<IN, OUT>` delegates `get(input)` to `TestScenarios` using an injected `ScenarioFactory<IN, OUT>`.
- Cache entries are stored as `TestScenarioWithExtra` and tracked with `TestScenarioMeta`; run-level metrics are exposed via `TestSuiteRunStatistics`.
- `ScenarioSerializer` converts cache `<ScenarioInputWithExtra, TestScenario>` to JSON and back.
- `ScenarioRepository` is the storage boundary; built-in implementations:
  - `InMemoryScenarioRepository` (default fallback bean)
  - `FileScenarioRepository` (used in tests via `TestConfig`)
  - `DatabaseScenarioRepository` (JDBC single-row table storage)
- Auto-config entrypoint: `ForemkaAutoConfiguration` + `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

## Runtime data flow
- Startup (constructor): `TestScenarios` loads serialized JSON from `ScenarioRepository.findAll()` into `TestScenariosMap`.
- Access (`ScenarioProvider.get`): compute key as `(scenario class, input)` via `ScenarioInputWithExtra`, then `computeIfAbsent`; cache hits update `TestScenarioMeta` (`lastUsedAt`, `totalUsageCount`, `usedByTests`) using `TestNameDetector`.
- Shutdown (`destroy`): `TestScenarios` logs `getSummaryStatistics()`, optionally removes scenarios not used in the current run when `CLEANUP_TEST_SCENARIOS=true`, then calls `ScenarioRepository.saveAll(...)`.
- Serializer groups entries by scenario class FQCN; unknown classes during restore are skipped with warning.

## Project-specific conventions
- Scenario input is the cache key; inputs must have stable `equals/hashCode` (records are the intended pattern).
- Prefer `record` for scenarios and complex inputs (see `src/test/java/.../fixture/UserScenario.java`, `RectangleInput.java`).
- Providers are tiny subclasses of `ScenarioProvider` (no custom logic by default).
- Keep custom storage behind `ScenarioRepository`; avoid touching `internal` package from consuming apps.
- `DatabaseScenarioRepository` validates table names via `DatabaseUtils.SafeIdentifier` (`[a-zA-Z0-9_]+` only).

## Build and test workflows
- Run tests: `./gradlew test` (Windows: `gradlew.bat test`).
- Formatting uses Spotless + Palantir Java Format (`./gradlew spotlessApply`).
- Java baseline is 21 (toolchain + source/target in `build.gradle.kts`).
- In this repo, `test` task sets `CLEANUP_TEST_SCENARIOS=true` by default (`build.gradle.kts`); override per-run if you need to keep all historical scenarios.

## Testing patterns to follow
- Unit tests use JUnit 6 + Hamcrest (`assertThat`, `is`, `sameInstance`).
- Integration-style Spring tests use `@SpringJUnitConfig(TestConfig.class)` and real wiring.
- File persistence tests isolate filesystem with `@TempDir`; DB tests use unique H2 in-memory DB URLs.
- For cache behavior, assert factory/supplier is NOT called on repeated key (see `TestScenariosTest`).
- For metadata/statistics behavior, use `getWithExtra(...)` and `getSummaryStatistics()` assertions (see `TestScenariosMetaTest`, `TestSuiteRunStatisticsTest`).

## External dependencies and boundaries
- Spring Context/Test, Jackson Databind, Lombok, H2 (tests), SLF4J API/simple.
- No HTTP/network integration in core; persistence boundary is only `ScenarioRepository`.
