# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Spring PetClinic — the canonical Spring Boot sample application (Spring Boot 4.1, Java 17, Thymeleaf, Spring Data JPA). Both Maven and Gradle builds are maintained in parallel; keep both working when you change build config.

## Common commands

Maven is the primary build (`./mvnw`); Gradle (`./gradlew`) mirrors it.

```bash
# Run the app (H2 in-memory DB, http://localhost:8080)
./mvnw spring-boot:run
./gradlew bootRun

# Full build (compiles, runs checkstyle/nohttp, runs all tests, jacoco report)
./mvnw verify
./gradlew build

# Run all tests
./mvnw test
./gradlew test

# Run a single test class
./mvnw test -Dtest=OwnerControllerTests
./gradlew test --tests "org.springframework.samples.petclinic.owner.OwnerControllerTests"

# Run a single test method
./mvnw test -Dtest=OwnerControllerTests#testInitCreationForm
./gradlew test --tests "*.OwnerControllerTests.testInitCreationForm"

# Apply Spring's code formatting (required — the build fails validation if code isn't formatted)
./mvnw spring-javaformat:apply
./gradlew format

# Rebuild CSS after editing src/main/scss/*.scss (Maven only, no Gradle equivalent)
./mvnw package -P css

# Build a container image
./mvnw spring-boot:build-image
```

### Database profiles

Defaults to H2 in-memory (auto-populated on startup; H2 console at `/h2-console`). MySQL and PostgreSQL configs also exist — switch with `spring.profiles.active=mysql` or `spring.profiles.active=postgres`. Start local databases via `docker-compose.yml`:

```bash
docker compose up mysql
docker compose up postgres
```

At development time, prefer running the `main()` methods directly in the IDE rather than a packaged jar:
- `PetClinicIntegrationTests` — H2, with Spring Boot DevTools
- `MysqlTestApplication` — MySQL via Testcontainers
- `PostgresIntegrationTests` — PostgreSQL via Docker Compose

## Architecture

### Package-by-feature, not by layer

Code is organized under `org.springframework.samples.petclinic` into three feature packages plus a shared `model` and `system` package:
- `owner` — Owner, Pet, Visit, PetType and their controllers/repositories. This is the largest and most active package (owners, their pets, and pets' visits are all managed here).
- `vet` — Vet, Specialty, and the cached vet list.
- `system` — cross-cutting config: `CacheConfiguration`, `WebConfiguration`, `CrashController` (demonstrates error handling), `WelcomeController`.
- `model` — base classes shared across features: `BaseEntity` (id + `isNew()`), `Person` (first/last name), extended by `Owner`/`Vet`.

Controllers are package-private (`class OwnerController`, not `public class`) — this is a deliberate convention; keep new controllers package-private too unless there's a reason to expose them.

### Data layer

- Spring Data JPA repositories (e.g. `OwnerRepository`, `VetRepository`) — no custom DAO layer.
- Hibernate `ddl-auto=none`: schema is **not** auto-generated. Schema and seed data live in `src/main/resources/db/{h2,mysql,postgres}/{schema.sql,data.sql}`, selected via `database=<profile>` in `application.properties`. **When changing the schema, all three dialect-specific `schema.sql` files must be updated together** — they are not generated from a single source of truth, and syntax differs per database (e.g. constraint-creation syntax between H2/MySQL/PostgreSQL).
- Naming strategy is snake_case (`PhysicalNamingStrategySnakeCaseImpl`), so JPA field `firstName` maps to column `first_name` — don't add explicit `@Column(name=...)` unless deviating from that convention.
- `Owner` eagerly fetches its `pets` collection (`FetchType.EAGER`) and cascades all operations — pets/visits are always saved/loaded as part of their owner, there's no standalone Pet or Visit repository-driven lifecycle.
- Bean Validation (`@NotBlank`, `@Pattern`, etc.) is used for simple field constraints. Cross-field/business-rule validation that doesn't fit annotations (e.g. `PetValidator`) uses Spring's `Validator` interface directly and is wired manually via `@InitBinder`.

### Internationalization

Message bundles live in `src/main/resources/messages/messages*.properties`, one file per locale (`messages_de`, `messages_es`, `messages_ja`, etc.) plus the English base `messages.properties`. `messages_en.properties` is intentionally absent — English falls back to the base file.

`I18nPropertiesSyncTest` (in `src/test`) enforces two things at build time:
1. No hardcoded, non-internationalized string literals in Thymeleaf templates (must use `#{...}` message keys or `th:text`).
2. Every locale's properties file has the same key set as the base `messages.properties`.

**When adding a new user-facing string, add the key to `messages.properties` and every locale file, or this test fails the build.**

### Web layer

Thymeleaf templates in `src/main/resources/templates/`, organized by feature (`owners/`, `pets/`, `vets/`) with shared `fragments/` (layout, form field fragments). CSS is compiled from `src/main/scss/*.scss` via a Maven-only profile (`-P css`) — don't hand-edit `petclinic.css` directly, edit the `.scss` source and recompile.

Static resources (including compiled CSS) are served with a 12-hour `Cache-Control` header (`spring.web.resources.cache.cachecontrol.max-age=12h`). When iterating on CSS/JS during a `spring-boot:run` session, a plain browser reload can serve a stale cached copy — use a hard refresh (or disable cache in devtools) to see changes.

### Testing conventions

- Unit/slice tests use `@WebMvcTest` per controller (e.g. `OwnerControllerTests`, `PetControllerTests`).
- `ClinicServiceTests` and `PetClinicIntegrationTests` exercise the full Spring context against H2.
- `PetClinicConcurrencyTests` specifically tests concurrent-access behavior — be careful when touching entity/repository code that this depends on.
- `MySqlIntegrationTests` / `PostgresIntegrationTests` run the same integration suite against real databases via Testcontainers/Docker Compose — these require Docker and aren't run by default in constrained environments.
- `src/test/java/.../e2e/` — browser-driven end-to-end tests using **playwright-java** (test-scope dependency in both `pom.xml`/`build.gradle`). Pattern: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + a class-level headless Chromium `Browser` (launched once in `@BeforeAll`), a fresh `Page` per test. Browser binaries must be installed once per environment: `mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps chromium"`.
- JMeter load test plan: `src/test/jmeter/petclinic_test_plan.jmx`.

### Code style enforcement

The `spring-javaformat` Maven/Gradle plugin validates formatting during `validate`/build phases (not just checkstyle) — a build fails if code isn't formatted to Spring's style, independent of checkstyle rules. Run `./mvnw spring-javaformat:apply` (or `./gradlew format`) before committing rather than hand-formatting. Checkstyle additionally enforces a "nohttp" rule (no plain `http://` URLs in source) via `src/checkstyle/nohttp-checkstyle.xml`.
