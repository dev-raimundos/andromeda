# Andromeda API

API backend da plataforma Coeur, construída em Spring Boot 4 / Java 21, com módulos de autenticação, usuários e compras.

## Stack

- **Java 21** (virtual threads habilitadas)
- **Spring Boot 4.1.1** — Web MVC, Data JPA, Security, OAuth2 Resource Server
- **PostgreSQL** (via Supabase) + **Flyway** para migrações
- **Redis** — cache / rate limiting
- **springdoc-openapi** — documentação Swagger
- **Lombok**
- **JUnit 5 + Spring Boot Test + H2** — testes
- **JaCoCo + SonarCloud** — cobertura e qualidade de código
- **Docker / Docker Compose**

## Arquitetura

O projeto segue uma organização modular por domínio (feature-based), com um núcleo de infraestrutura compartilhada:

```
src/main/java/br/app/coeur/
├── core/                    # Configurações e infraestrutura transversal
│   ├── config/              # GlobalExceptionHandler, OpenApiConfig, WebConfig
│   ├── exception/           # Exceções de infraestrutura (ex.: RateLimitExceededException)
│   ├── filter/               # RateLimitingFilter
│   └── health/               # Endpoint de health check simples
├── modules/
│   ├── authentication/      # Login, refresh token, configuração de segurança/JWT
│   ├── user/                 # Cadastro, consulta, atualização e remoção de usuários
│   └── shopping/             # Módulo de compras (em desenvolvimento)
└── shared/
    └── persistence/          # BaseEntity (equals/hashCode seguros para proxies do Hibernate)
```

Cada módulo de negócio segue o padrão:

```
modules/<nome>/
├── controller/    # Endpoints REST
├── domain/        # Entidades com comportamento (ex.: User.register, user.changeEmail)
├── dto/           # Requests/Responses
├── exception/     # Exceções específicas da regra de negócio do módulo
├── repository/    # Spring Data JPA
└── service/       # Orquestração de casos de uso, transações
```

## Autenticação

- OAuth2 Resource Server com validação de JWT (chaves configuradas em `JwtKeyConfig`).
- Endpoints de login e refresh token em `/api/auth`.
- Configuração de segurança centralizada em `SecurityConfig`.

## Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/login` | Autentica usuário e emite tokens |
| POST | `/api/auth/refresh` | Renova o access token a partir de um refresh token |
| POST | `/api/users/register` | Registra novo usuário |
| GET | `/api/users/me` | Retorna o usuário autenticado |
| GET | `/api/users` | Lista usuários (paginado) |
| GET | `/api/users/{id}` | Busca usuário por id |
| PUT | `/api/users/{id}` | Atualiza usuário |
| DELETE | `/api/users/{id}` | Remove usuário |
| GET | `/health` | Health check simples |
| GET | `/actuator/**` | Endpoints do Spring Actuator |

Com o perfil `dev` ativo, a documentação interativa fica disponível em `/swagger-ui.html`. Em outros perfis, o Swagger é desabilitado por padrão (`application.yaml`).

## Configuração de ambiente

Copie `.env.example` para `.env` e preencha as variáveis:

```
SPRING_PROFILES_ACTIVE=dev
APP_PORT=8080
REDIS_PORT=6379
COMPOSE_PROFILES=

DATABASE_URL=jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?sslmode=require
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=secret

FLYWAY_ENABLED=true

REDIS_HOST=localhost
REDIS_PASSWORD=

SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_xxx
SUPABASE_SECRET_KEY=sb_secret_xxx
SUPABASE_JWKS_URL=https://<project-ref>.supabase.co/auth/v1/.well-known/jwks.json
```

O `application.yaml` carrega o `.env` da raiz automaticamente (`spring.config.import: optional:file:.env`) quando a aplicação roda pela IDE/Maven. Variáveis de ambiente reais (ex.: definidas pelo Docker Compose) têm precedência sobre o `.env`.

## Como rodar

### Localmente (IDE/Maven)

Pré-requisitos: Java 21, um Postgres acessível (ex.: Supabase) e, opcionalmente, um Redis local.

```bash
# com o .env preenchido na raiz
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080` com o perfil `dev` por padrão.

### Via Docker Compose

O `compose.yaml` sempre sobe o Redis. O serviço `app` (build + run do próprio container) só é ativado com o profile `prod`:

```bash
# apenas Redis (app roda localmente pela IDE/Maven)
docker compose up -d

# Redis + aplicação containerizada
COMPOSE_PROFILES=prod docker compose up -d --build
```

### Testes

```bash
./mvnw test
```

Os testes de persistência/integração usam H2 em memória; não dependem do Postgres externo.

### Build

```bash
./mvnw clean package
```

Gera o jar em `target/`. O `Dockerfile` faz build multi-stage (`eclipse-temurin:21-jdk-jammy` → `eclipse-temurin:21-jre-jammy`), empacotando o jar final em uma imagem de runtime JRE.

## Banco de dados

Migrações gerenciadas pelo Flyway em `src/main/resources/db/migration`:

- `V1__create_tables.sql`
- `V2__add_user_lock_fields.sql`

`ddl-auto` do Hibernate está fixado em `none` — qualquer alteração de schema deve passar por uma nova migração Flyway.

## CI/CD

Pipeline definido em `.github/workflows/ci-cd.yml`, disparado em push para `main`:

1. **Testes** — `./mvnw test` (com retry automático em caso de falha transitória).
2. **SonarQube** — `./mvnw verify sonar:sonar`, com cobertura de testes via JaCoCo publicada no SonarCloud.
3. **Deploy** — dispara webhook do Dokploy para deploy em produção.

## Observabilidade

- `spring-boot-starter-actuator` habilitado — expõe endpoints de métricas/health padrão do Spring em `/actuator`.
- Endpoint `/health` próprio, usado pelo healthcheck do container no `compose.yaml`.

## Licença

Distribuído sob a licença MIT — veja [LICENSE](LICENSE).
