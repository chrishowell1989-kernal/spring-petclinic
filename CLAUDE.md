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
- `src/test/java/.../e2e/` — browser-driven end-to-end tests using **playwright-java** (test-scope dependency in both `pom.xml`/`build.gradle`). Pattern: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + a class-level headless Chromium `Browser` (launched once in `@BeforeAll`), a fresh `Page` per test. Browser binaries must be installed once per environment: `mvn exec:java -e -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps chromium"` — `-Dexec.classpathScope=test` is required since `exec-maven-plugin` isn't declared in `pom.xml` and Playwright is a test-scoped dependency; `exec:java`'s default `runtime` scope can't see it.
- JMeter load test plan: `src/test/jmeter/petclinic_test_plan.jmx`.

### Code style enforcement

The `spring-javaformat` Maven/Gradle plugin validates formatting during `validate`/build phases (not just checkstyle) — a build fails if code isn't formatted to Spring's style, independent of checkstyle rules. Run `./mvnw spring-javaformat:apply` (or `./gradlew format`) before committing rather than hand-formatting. Checkstyle additionally enforces a "nohttp" rule (no plain `http://` URLs in source) via `src/checkstyle/nohttp-checkstyle.xml`.

## Agentic Jira workflow

This repo is a demo of an end-to-end "PM writes a ticket → code ships" pipeline for any Jira story labelled `agentic-workflow`, built from three GitHub Actions workflows. Each uses the `claude` CLI directly (`npm install -g @anthropic-ai/claude-code`, not `anthropics/claude-code-action`) so tool permissions and git/PR mechanics are handled deterministically by the workflow rather than through that action's own allowlist/OIDC layer — an earlier attempt using the action burned real spend on silently-denied tool calls before switching to this approach.

1. **`.github/workflows/jira-agentic-workflow.yml`** — triggered by a Jira Automation rule when a sprint starts. Implements the story on a new `agentic/<ISSUE_KEY>-<slug>` branch, runs `./mvnw spring-javaformat:apply` and `./mvnw test -Dtest='!*E2ETests'`, opens a PR (the Jira description, i.e. acceptance criteria, is included in a collapsed `<details>` block — visually out of the way for a human, but still present in the raw body for the QA workflow to read), then transitions the Jira issue to whichever of its available transitions has "review" in the name.
2. **`.github/workflows/agentic-pr-review.yml`** — triggered by a *second* Jira Automation rule when that transition happens (deliberately not GitHub's own `pull_request` event — see below). Looks up the open PR by its `agentic/<ISSUE_KEY>-` branch prefix, reviews the diff as a senior developer would against the acceptance criteria, and leaves a single non-blocking `gh pr comment`. Never calls `gh pr review --approve`.
3. **`.github/workflows/agentic-qa-playwright.yml`** — triggered by a human clicking **Approve** on the PR (`pull_request_review: submitted`, state `approved`). Transitions the issue to whichever transition has "qa" in the name, adds Playwright e2e coverage under `src/test/java/**/e2e/` against the acceptance criteria, and pushes it as a follow-up commit.

Merging the PR is the only step with no agent involved.

### Why review is triggered via Jira, not `pull_request`

GitHub requires a human to click "Approve and run workflows" before any `pull_request`-triggered workflow executes on a PR authored by `github-actions[bot]` (which is what `gh pr create` runs as when using `GITHUB_TOKEN`) — even for a same-repo, non-fork branch. A `repository_dispatch` run isn't subject to that gate. So instead of triggering on `pull_request: opened`, the review workflow is fired by a second Jira Automation rule watching for the issue's transition to the "review" status, which the implementer workflow itself triggers once its PR is open.

For the same reason, the review workflow never formally approves the PR (`gh pr review --approve`) — it and the implementer workflow both authenticate as `github-actions[bot]` via `GITHUB_TOKEN`, and GitHub rejects a bot approving its own PR. A real "Approved" state would need a second identity (e.g. a separate PAT/GitHub App), which this demo doesn't set up.

### Required Jira Automation rules (not part of this repo)

Both need: condition `Labels contains "agentic-workflow"`; action "Send web request" to `https://api.github.com/repos/<owner>/<repo>/dispatches` with a PAT (`repo` + `workflow` scope) in the `Authorization` header. See each workflow file's header comment for the exact trigger/body. Free-text Jira fields (`summary`, `description`) must go through the `.asJsonString` smart value modifier — it supplies its own surrounding quotes — or the JSON body breaks the moment a field contains a quote or newline (`asJsonEncodedString` does not exist, despite being an easy guess).

1. Sprint started → `jira-agentic-workflow.yml` (`event_type: jira-agentic-workflow`)
2. Issue transitioned to the "review" status → `agentic-pr-review.yml` (`event_type: jira-review-requested`)

### Required repo configuration

- Secrets: `ANTHROPIC_API_KEY` (required); `JIRA_BASE_URL` / `JIRA_EMAIL` / `JIRA_API_TOKEN` (optional — enable the live Jira description re-fetch and both issue-transition steps; each is skipped individually, with a warning, if unset or if no matching transition is found).
- **Settings → Actions → General → Workflow permissions**: "Allow GitHub Actions to create and approve pull requests" must be enabled, or `gh pr create` fails with `GraphQL: GitHub Actions is not permitted to create or approve pull requests`.

### Conventions followed by all three workflows

- Jira/PR-derived text (issue summary/description, PR title/body) is only ever read via job-level `env:` and consumed as `$VAR` inside `run:` scripts — never interpolated directly via `${{ }}` inside a `run:` block. GitHub pastes the latter raw into the generated shell script, so a crafted Jira field could otherwise break out and run arbitrary commands on the runner.
- `claude` runs with `--max-turns` capped. The implementer and QA workflows also need `--dangerously-skip-permissions` since they run `git`/`./mvnw` themselves; the review workflow deliberately omits it — it only reads files, so it doesn't need (or want) write/exec access.
