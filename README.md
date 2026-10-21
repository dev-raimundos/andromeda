# Andromeda API

Coeur platform backend API, built with Spring Boot 4 / Java 21, with authentication, user, and shopping modules.

## Stack

- **Java 21** (virtual threads enabled)
- **Spring Boot 4.1.1** — Web MVC, Data JPA, Security, OAuth2 Resource Server
- **Spring Modulith** — enforces module boundaries within the single Maven artifact
- **PostgreSQL 18** (external/on-premise server) + **Flyway** for migrations
- **Redis** (external server) — cache / rate limiting
- **springdoc-openapi** — Swagger documentation
- **Lombok**
- **JUnit 5 + Spring Boot Test + H2** — tests
- **JaCoCo + SonarCloud** — coverage and code quality
- **Docker / Docker Compose**

## Architecture

The project is a single Maven artifact (`br.app.coeur:api`) organized as a **modular monolith**:
each business module lives in its own top-level package under `br.app.coeur`, and
[Spring Modulith](https://spring.io/projects/spring-modulith) verifies at test time
(`ModularityTests`) that modules only reach each other through explicitly declared APIs —
not just by convention. This keeps the codebase ready for new modules to be added later
without their boundaries eroding over time.

```
src/main/java/br/app/coeur/
├── api/                  # OPEN module: cross-cutting configuration of the "api" artifact itself
│   ├── config/           # CacheConfig, GlobalExceptionHandler, OpenApiConfig, WebConfig, ...
│   ├── exception/        # Infrastructure-level exceptions (e.g. RateLimitExceededException)
│   └── filter/           # RateLimitingFilter
├── shared/               # OPEN module: shared kernel
│   ├── exception/        # BusinessException, ConflictException, ResourceNotFoundException
│   ├── persistence/      # BaseEntity (safe equals/hashCode for Hibernate proxies, auditable created_at/updated_at)
│   ├── security/         # @CurrentUserId and its argument resolver
│   └── util/             # JwtUtils
├── identity/             # Login, refresh tokens, JWT/security configuration
├── user/                 # User registration, lookup, update, and removal
└── shopping/             # Shopping lists and item catalog
```

Each business module follows Clean Architecture layering internally:

```
<module>/
├── domain/               # Entities with behavior, domain exceptions, repository ports (plain interfaces)
├── application/          # Use-case services, transactions, request/response DTOs
└── infrastructure/
    ├── web/              # REST controllers
    ├── persistence/      # Spring Data JPA repositories implementing the domain ports
    └── config/           # Module-specific framework configuration (if any)
```

`api` and `shared` are declared as **open** Spring Modulith modules (`@ApplicationModule(type = OPEN)`),
so any module can use them freely. Business modules are closed by default: only the `domain`
package of a module is visible to others, and only when explicitly exposed as a
`@NamedInterface`. Today `identity` is the only module that depends on another
(`user::domain`, to look up the user during login/refresh); this dependency is declared
explicitly in `identity/package-info.java` and enforced by `ModularityTests`.

## Authentication

- OAuth2 Resource Server with JWT validation (keys configured in `JwtKeyConfig`).
- Login and refresh token endpoints under `/api/v1/auth`.
- Security configuration centralized in `SecurityConfig`.

## Main endpoints

| Method | Route | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Authenticates the user and issues tokens |
| POST | `/api/v1/auth/refresh` | Renews the access token from a refresh token |
| POST | `/api/v1/users/register` | Registers a new user |
| GET | `/api/v1/users/me` | Returns the authenticated user |
| GET | `/api/v1/users` | Lists users (paginated) |
| GET | `/api/v1/users/{id}` | Finds a user by ID |
| PUT | `/api/v1/users/{id}` | Updates a user |
| DELETE | `/api/v1/users/{id}` | Removes a user |
| GET | `/api/v1/items` | Lists catalog items (paginated) |
| POST | `/api/v1/items` | Registers a new catalog item |
| GET | `/api/v1/shopping-lists` | Lists the authenticated user's shopping lists |
| POST | `/api/v1/shopping-lists` | Creates a new shopping list |
| POST | `/api/v1/shopping-lists/{id}/items` | Adds an item to a shopping list |
| GET | `/actuator/**` | Spring Actuator endpoints |

With the `dev` profile active, interactive documentation is available at `/swagger-ui.html`.
In other profiles, Swagger is disabled by default (`application.yaml`).

## Environment configuration

Copy `.env.example` to `.env` and fill in the variables:

```
SPRING_PROFILES_ACTIVE=dev
APP_PORT=8080
COMPOSE_PROFILES=

DATABASE_URL=jdbc:postgresql://<host>:5432/<database>
DATABASE_USERNAME=<user>
DATABASE_PASSWORD=<password>

FLYWAY_ENABLED=true

REDIS_HOST=<host>
REDIS_PORT=6379
REDIS_PASSWORD=
```

`application.yaml` automatically loads `.env` from the project root
(`spring.config.import: optional:file:.env`) when the application runs via the IDE/Maven.
Real environment variables (e.g. set by Docker Compose) take precedence over `.env`.

## Running the project

### Locally (IDE/Maven)

Prerequisites: Java 21 and access to the external Postgres and Redis defined in `.env`.

```bash
# with .env filled in at the project root
./mvnw spring-boot:run
```

The application starts at `http://localhost:8080` with the `dev` profile by default.
Postgres and Redis are always external — there are no local containers for them; Flyway
runs migrations directly against the external Postgres.

### Via Docker Compose

`compose.yaml` no longer starts any local dependency (Postgres and Redis are always
external). It is only used to build and run the application itself in a container, with
the `prod` profile:

```bash
# containerized application (uses the external DATABASE_URL/REDIS_HOST from .env)
COMPOSE_PROFILES=prod docker compose up -d --build
```

### Tests

```bash
./mvnw test
```

Persistence/integration tests use an in-memory H2 database and do not depend on the
external Postgres. The suite also includes `ModularityTests`, which fails the build if a
module boundary is violated.

### Build

```bash
./mvnw clean package
```

Produces the jar under `target/`. The `Dockerfile` performs a multi-stage build
(`eclipse-temurin:21-jdk-jammy` → `eclipse-temurin:21-jre-jammy`), packaging the final jar
into a JRE runtime image.

## Database

Migrations managed by Flyway under `src/main/resources/db/migration`:

- `V1__create_tables.sql`
- `V2__create_shopping_tables.sql`

Hibernate's `ddl-auto` is fixed to `none` — any schema change must go through a new Flyway
migration.

## CI/CD

Pipeline defined in `.github/workflows/ci-cd.yml`, triggered on push to `main`:

1. **Tests** — `./mvnw test` (with automatic retry on transient failure).
2. **SonarQube** — `./mvnw verify sonar:sonar`, with JaCoCo test coverage published to
   SonarCloud.
3. **Deploy** — triggers a Dokploy webhook to deploy to production.

## Observability

- `spring-boot-starter-actuator` enabled — exposes Spring's standard metrics/health
  endpoints at `/actuator`.
- `/actuator/health` is used by the container healthcheck in `compose.yaml`.

## License

Distributed under the MIT license — see [LICENSE](LICENSE).
