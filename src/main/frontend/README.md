# PetClinic frontend

React + TypeScript + MUI + React Router single-page app, served as static resources
from the Spring Boot backend (see the root `CLAUDE.md` for the full picture).

This directory is built automatically as part of `./mvnw` / `./gradlew` — you don't
need to build it separately for a normal backend build or `spring-boot:run`.

```bash
npm install
npm run dev      # Vite dev server, proxies /api to http://localhost:8080
npm run build    # production build -> dist/, picked up by the Spring Boot build
npm test         # Vitest (component tests + i18n locale-parity check)
```
