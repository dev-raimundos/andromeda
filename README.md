# Andromeda API

Coeur platform backend API, built with Spring Boot 4 / Java 21, with authentication, user, and shopping modules.

## Stack

- **Java 21** (virtual threads enabled)
- **Spring Boot 4.1.1** — Web MVC, Data JPA, Security, OAuth2 Resource Server
- **PostgreSQL 18** (external/on-premise server) + **Flyway** for migrations
- **Redis** (external server) — cache
- **springdoc-openapi** — Swagger documentation
- **Lombok**
- **JUnit 5 + Spring Boot Test + H2** — tests
- **JaCoCo + SonarCloud** — coverage and code quality
- **Docker / Docker Compose**

## Architecture

The project is a single Maven artifact (`br.app.coeur:api`) organized into business
modules with simple layers. The main package contains `core`, `modules`, and `shared`.

```
src/main/java/br/app/coeur/
├── core/                 # Application-wide configuration and HTTP infrastructure
│   ├── config/           # Security, JWT keys, cache, CORS, errors, OpenAPI, MVC
│   ├── controller/       # Health endpoint
│   ├── filter/           # Rate limiting
│   └── security/         # Current-user argument resolver
├── modules/              # Business features
│   ├── authentication/   # Login, refresh and logout
│   ├── user/             # Profiles and user management
│   └── shopping/         # Lists and item catalog
└── shared/               # Small types reused across modules
    ├── exception/        # BusinessException, ConflictException, ResourceNotFoundException
    ├── model/            # Audited BaseEntity
    ├── security/         # @CurrentUserId annotation
    └── util/             # JWT helpers
```

Each business module uses the same simple structure:

```
<module>/
├── controller/           # REST endpoints and request validation
├── dto/
│   ├── request/          # Input contracts
│   └── response/         # Output contracts
├── model/                # JPA entities and their invariants
├── service/              # Business operations and transactions
├── repository/           # Spring Data JPA interfaces and queries
└── exception/            # Module-specific exceptions
```

The normal flow is `controller -> service -> repository`; DTOs keep HTTP contracts
separate from entities. Each repository extends `JpaRepository` directly. Services
retain transaction boundaries and business behavior. Add module-local
`config` or other folders only when a concrete need appears.

`core` owns application infrastructure; `shared` contains only reusable types, without
feature-specific logic. Authentication uses the user module's model and repository to
look up accounts. The former ports/adapters and Spring Modulith metadata were removed
in favor of these conventional packages. Tests mirror the packages under `src/test/java`.
Redis entries use the `coeur:v2:` prefix to avoid reading DTOs serialized with the
previous package names; old entries expire under their existing TTL.

## Authentication

- OAuth2 Resource Server with JWT validation and signing keys stored in PostgreSQL.
- Login and refresh token endpoints under `/api/v1/auth`.
- Security configuration centralized in `SecurityConfig`.

Access tokens last 15 minutes. Every user endpoint requires authentication, with no
role-based permissions. Authenticated users can manage users and update their own
name/email through `PUT /api/v1/users/me`. The existing `roles` field is retained for
API compatibility and does not restrict access. Shopping lists remain scoped to their owner.

Refresh tokens rotate under a database row lock, so concurrent requests cannot consume
the same token twice. `POST /api/v1/auth/logout` accepts `{ "refreshToken": "..." }`
and idempotently revokes that token (204). Already-issued access tokens remain valid
until expiration. Login revokes the user's previous refresh tokens.

Flyway migration `V3__create_jwt_signing_key_table.sql` creates the singleton
`jwt_signing_key` table and its initial empty row. On startup, the API locks that row
and generates an RSA-2048 pair only if it is empty. All instances connected to the
same database reuse the persisted pair, including after restarts. No PEM files,
JWT path variables, or certificate bind mounts are used.

`POST /api/v1/auth/keys` generates a replacement pair under the same row lock. The
endpoint requires a valid Bearer token, without a separate permission model. It
accepts no body and returns 201 with only `keyId`, `algorithm` (`RS256`) and `createdAt`;
private and public key material are never returned. Existing access tokens become
invalid; refresh tokens remain usable to obtain new access tokens.

```bash
curl -X POST https://your-api.example/api/v1/auth/keys \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

The signing and verification objects are cached in memory. The local instance
invalidates its cache after the rotation transaction commits; other replicas check
the database at most once every five seconds during token use. During that interval,
replicas may temporarily disagree on the active key. They converge automatically
without a restart. Rolled-back rotations leave the persisted and cached keys unchanged.

The table stores Base64-encoded DER (PKCS#8 private key / X.509 public key); Base64 is
encoding, not encryption. Restrict access to the database and its backups. The first
deployment of this version generates a new pair, so access tokens signed by the old
file-based pair need to be refreshed or obtained through login again.

For existing Docker/Dokploy deployments, remove the old JWT path environment variables
and certificate mount from the deployment configuration, keep Flyway enabled, and
redeploy. The container defaults to `prod` (`CONTAINER_SPRING_PROFILES_ACTIVE` overrides it).

Rate limiting uses bounded in-memory buckets (30 requests per minute per client,
10,000 clients maximum, five-minute idle expiration), independently on each replica.
Health probes and CORS preflights are excluded. `X-Forwarded-For` is ignored unless
the direct peer is in `TRUSTED_PROXIES` (exact IPs, comma-separated); trusted proxies
must append/overwrite the forwarding chain. For a cluster-wide quota, enforce it at
the gateway. Throttling returns Problem Details with status 429 and `Retry-After`.

API errors use Problem Details `detail` and optional field-level `errors`. The frontend
renders these fields; a `toast` extension is not required. Authorization failures return 403.

## Main endpoints

| Method | Route | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Authenticates the user and issues tokens |
| POST | `/api/v1/auth/refresh` | Renews the access token from a refresh token |
| POST | `/api/v1/auth/logout` | Revokes the supplied refresh token |
| POST | `/api/v1/auth/keys` | Generates and activates a replacement JWT signing key (authenticated) |
| POST | `/api/v1/users/register` | Registers a new user |
| GET | `/api/v1/users/me` | Returns the authenticated user |
| PUT | `/api/v1/users/me` | Updates own name/email without changing roles |
| GET | `/api/v1/users` | Lists users (paginated) |
| GET | `/api/v1/users/{id}` | Finds a user by ID |
| PUT | `/api/v1/users/{id}` | Updates a user |
| DELETE | `/api/v1/users/{id}` | Removes a user |
| GET | `/api/v1/items` | Lists catalog items (paginated) |
| POST | `/api/v1/items` | Registers a new catalog item |
| GET | `/api/v1/shopping-lists` | Lists the authenticated user's shopping lists |
| POST | `/api/v1/shopping-lists` | Creates a new shopping list |
| POST | `/api/v1/shopping-lists/{id}/items` | Adds an item to a shopping list |
| GET | `/health` | Simple health check |
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

CORS_ALLOWED_ORIGINS=http://localhost:4200
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
external Postgres. They cover authentication, profile updates, refresh-token rotation and
concurrency; unit tests cover services, models, DTO validation and core infrastructure.

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
- `V3__create_jwt_signing_key_table.sql`

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
- A dedicated `/health` endpoint, used by the container healthcheck in `compose.yaml`.

## License

Distributed under the MIT license — see [LICENSE](LICENSE).
