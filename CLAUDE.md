# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Spring PetClinic — the canonical Spring Boot sample application (Spring Boot 4.1, Java 17, Spring Data JPA), with a React + MUI single-page app frontend (`src/main/frontend/`) served as static resources from the Spring Boot backend. The backend is a JSON REST API under `/api/**`; there is no server-side rendering. Both Maven and Gradle builds are maintained in parallel; keep both working when you change build config.

## Common commands

Maven is the primary build (`./mvnw`); Gradle (`./gradlew`) mirrors it. Both automatically install Node, build the React frontend (`npm ci && npm test && npm run build`), and copy the result into the served static resources — no separate frontend step is needed for a normal build or run.

```bash
# Run the app (H2 in-memory DB, builds+serves the React frontend, http://localhost:8080)
./mvnw spring-boot:run
./gradlew bootRun

# Full build (compiles, runs checkstyle/nohttp, builds+tests the frontend, runs all tests, jacoco report)
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

# Backend-only iteration, skipping the npm install/test/build steps
./mvnw test -Dfrontend.skip=true
./gradlew test -x npmBuild -x npmInstall -x npmTest -x nodeSetup

# Frontend-only iteration (Vite dev server with API proxied to :8080; run spring-boot:run separately)
cd src/main/frontend && npm run dev

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
- `owner` — Owner, Pet, Visit, PetType, their REST controllers/repositories, `OwnerService` (business logic: not-found/duplicate-name checks), request/response DTOs under `owner/dto/`. This is the largest and most active package (owners, their pets, and pets' visits are all managed here).
- `vet` — Vet, Specialty, the cached vet list, and `vet/dto/` response DTOs.
- `system` — cross-cutting config: `CacheConfiguration`, `WebConfiguration` (locale resolution + static resource caching), `CrashController` (demonstrates error handling via `/api/oups`), `SpaForwardingController` (serves `index.html` for client-side routes), `ApiExceptionHandler` (`@RestControllerAdvice`, scoped to `@RestController` beans only — an unscoped advice would also swallow `NoResourceFoundException` from missing static assets and turn a plain 404 into a 500), `ApiError` (shared `{errors:[{field,code,message}]}` response shape).
- `model` — base classes shared across features: `BaseEntity` (id + `isNew()`), `Person` (first/last name), extended by `Owner`/`Vet`.

Controllers are package-private (`class OwnerController`, not `public class`) — this is a deliberate convention; keep new controllers package-private too unless there's a reason to expose them. All are `@RestController`s under `/api/**`; request/response bodies are DTOs (never JPA entities directly), so there's no `id` field on any request payload to guard against mass-assignment.

### Data layer

- Spring Data JPA repositories (e.g. `OwnerRepository`, `VetRepository`) — no custom DAO layer.
- Hibernate `ddl-auto=none`: schema is **not** auto-generated. Schema and seed data live in `src/main/resources/db/{h2,mysql,postgres}/{schema.sql,data.sql}`, selected via `database=<profile>` in `application.properties`. **When changing the schema, all three dialect-specific `schema.sql` files must be updated together** — they are not generated from a single source of truth, and syntax differs per database (e.g. constraint-creation syntax between H2/MySQL/PostgreSQL).
- Naming strategy is snake_case (`PhysicalNamingStrategySnakeCaseImpl`), so JPA field `firstName` maps to column `first_name` — don't add explicit `@Column(name=...)` unless deviating from that convention.
- `Owner` eagerly fetches its `pets` collection (`FetchType.EAGER`) and cascades all operations — pets/visits are always saved/loaded as part of their owner, there's no standalone Pet or Visit repository-driven lifecycle.
- Bean Validation (`@NotBlank`, `@Pattern`, `@Future`, `@PastOrPresent`, etc.) on the DTOs in `owner/dto/` covers simple and date-range field constraints. Business-rule validation that doesn't fit annotations (duplicate pet name, not-found owner/pet) lives in `OwnerService` and is surfaced via typed exceptions (`OwnerNotFoundException`, `PetNotFoundException`, `DuplicatePetNameException`) that `ApiExceptionHandler` maps to the right HTTP status.

### Internationalization

Backend message bundles live in `src/main/resources/messages/messages*.properties`, one file per locale (`messages_de`, `messages_es`, `messages_ja`, etc.) plus the English base `messages.properties`, and are the source of validation-error message text resolved by `ApiExceptionHandler` via `MessageSource`/`Accept-Language`. `messages_en.properties` is intentionally absent — English falls back to the base file. `I18nPropertiesSyncTest` (in `src/test`) enforces that every locale's properties file has the same key set as the base file.

Frontend UI-chrome strings are separate and independent: `src/main/frontend/src/i18n/locales/*.json` (one file per locale, `en.json` is the source of truth), loaded via `react-i18next`. `src/main/frontend/src/i18n/localeParity.test.ts` (Vitest) enforces the same same-key-set-as-English rule for these.

**When adding a new backend validation message, add the key to `messages.properties` and every locale file. When adding new frontend UI text, add the key to `en.json` and every locale file** — either is enforced at build time (`I18nPropertiesSyncTest` for the backend, the Vitest locale-parity test for the frontend) and fails the build otherwise.

### Web layer

`src/main/frontend/` is a Vite + React + TypeScript + MUI + React Router SPA, built automatically as part of `./mvnw`/`./gradlew` (see Common commands) and served from `classpath:/static/`. Routing:
- `system/SpaForwardingController` forwards any GET request whose last path segment has no dot (i.e. isn't a hashed asset filename) to `index.html`, so React Router can take over client-side; `/api/**` requests are handled by the explicit REST `@RequestMapping`s and never reach this fallback.
- `system/WebConfiguration` serves `/index.html` with `Cache-Control: no-store` (overriding the app-wide `spring.web.resources.cache.cachecontrol.max-age=12h`) so a new deploy's shell is never served stale, while hashed JS/CSS bundle filenames keep the long cache.
- Prefer real anchors (`<a href>`/React Router `Link`) for navigation over `onClick`-only handlers where practical — see `git log --oneline -- src/main/frontend/src/components/common/FlashSnackbar.tsx` around the "stale navigate() on unmount" fix if touching flash-message/toast logic: a component that calls `navigate()` from a timer callback captured before the user navigated elsewhere will silently bounce the URL back.

### Testing conventions

- Unit/slice tests use `@WebMvcTest` per controller (e.g. `OwnerControllerTests`, `PetControllerTests`), asserting JSON status/body via `MockMvc` + `jsonPath`, with the controller's collaborator (`OwnerService`, `VetRepository`, etc.) mocked via `@MockitoBean`.
- `ClinicServiceTests` and `PetClinicIntegrationTests` exercise the full Spring context against H2.
- `PetClinicConcurrencyTests` specifically tests concurrent-access behavior (via the `/api/owners/{id}/pets` endpoint) — be careful when touching entity/repository/service code that this depends on.
- `MySqlIntegrationTests` / `PostgresIntegrationTests` run the same integration suite against real databases via Testcontainers/Docker Compose — these require Docker and aren't run by default in constrained environments.
- `src/test/java/.../e2e/` — browser-driven end-to-end tests using **playwright-java** (test-scope dependency in both `pom.xml`/`build.gradle`) against the built React SPA. `PlaywrightTestSupport` is the shared base class (`@SpringBootTest(webEnvironment = RANDOM_PORT)` + a class-level headless Chromium `Browser` launched once in `@BeforeAll`, a fresh `Page` per test) — extend it rather than duplicating the lifecycle boilerplate. Two gotchas specific to this stack: (1) MUI `Button`/`CardActionArea` rendered with `component={Link}` produce an `<a>`, so target it with `getByRole(AriaRole.LINK, ...)`, not `BUTTON`; (2) MUI X `DatePicker` fields are ambiguous under `getByLabel` (it matches both the visible `role="group"` wrapper and a hidden accessibility-only input) — use `getByRole(AriaRole.GROUP, ...)`, click to focus, then type the digits (e.g. `20150212` for `2015-02-12`) since the field auto-advances section by section. Browser binaries must be installed once per environment: `mvn exec:java -e -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps chromium"` — `-Dexec.classpathScope=test` is required since `exec-maven-plugin` isn't declared in `pom.xml` and Playwright is a test-scoped dependency; `exec:java`'s default `runtime` scope can't see it.
- Frontend unit tests use Vitest (`npm test` in `src/main/frontend/`, wired into both the Maven and Gradle builds): component tests with `@testing-library/react`, plus the i18n locale-parity test.
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
