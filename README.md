# GateKeeper

GateKeeper is a Spring Boot application for managing GateKeeper flags, environment-specific rules, user targeting, and percentage rollouts. It includes both REST APIs and simple Thymeleaf pages so you can manage rules from the browser while keeping the backend cleanly layered.

## What Is Included

- GateKeeper flag CRUD
- Environment-aware rule management
- Evaluation caching with cache invalidation
- Audit logging for flag and rule changes
- Rule types:
  - `GLOBAL`
  - `USER_TARGET`
  - `PERCENTAGE`
- Deterministic evaluation by `flagKey + userId + environment`
- Thymeleaf pages for listing, creating, viewing, and managing GateKeeper flags
- H2 support by default
- PostgreSQL profile support
- Unit tests for GateKeeper evaluation logic

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Thymeleaf
- Lombok
- Spring Cache
- Redis
- H2
- PostgreSQL
- JUnit 5 / Mockito

## Project Structure

```text
com.gatekeeper
├── GateKeeperApplication
├── config
│   ├── CacheConfig
│   ├── DataInitializer
│   └── DataSourceConfig
├── controller
│   ├── GatekeeperController
│   ├── GatekeeperEvaluationController
│   ├── GatekeeperManagementController
│   └── RuleManagementController
├── dto
├── evaluation
│   └── FlagEvaluationEngine
├── model
│   ├── Environment
│   ├── FlagRule
│   ├── GatekeeperFlag
│   ├── RuleType
│   ├── AuditLog
│   └── UserTarget
├── repository
│   ├── AuditLogRepository
│   ├── EnvironmentRepository
│   ├── FlagRuleRepository
│   ├── GatekeeperFlagRepository
│   └── UserTargetRepository
└── service
    ├── AuditLogService
    ├── GatekeeperEvaluationService
    ├── GatekeeperFlagService
    └── RuleManagementService
```

## Domain Model

### `GatekeeperFlag`

Represents the main GateKeeper flag definition:

- `id`
- `key`
- `name`
- `description`
- `enabled`
- `createdAt`
- `updatedAt`

### `Environment`

Represents a deployment environment:

- `id`
- `name`

Seeded automatically on startup:

- `test`
- `uat`
- `prod`

### `FlagRule`

Represents a rule for a GateKeeper flag in an environment:

- `id`
- `flag`
- `environment`
- `ruleType`
- `percentage`
- `enabled`

### `UserTarget`

Represents a targeted user attached to a `USER_TARGET` rule:

- `id`
- `flagRule`
- `userId`

### `AuditLog`

Represents an audit trail entry for configuration changes:

- `id`
- `entityType`
- `entityId`
- `action`
- `actor`
- `details`
- `createdAt`

## Evaluation Logic

`GatekeeperEvaluationService` evaluates a GateKeeper flag using this order:

1. If the GateKeeper flag itself is disabled, return `false`
2. If an enabled `GLOBAL` rule exists for the environment, return `true`
3. If an enabled `USER_TARGET` rule contains the user, return `true`
4. If an enabled `PERCENTAGE` rule exists:
   - hash `flagKey + userId + environment`
   - convert to a bucket between `0` and `99`
   - return `true` if `bucket < percentage`
5. Otherwise return `false`

This makes rollout results deterministic for the same user, flag, and environment.

## Caching

The evaluation path is the highest-traffic operation in a real GateKeeper platform, so the project now includes caching for evaluation results.

### What is cached

- `GatekeeperEvaluationService#evaluate(flagKey, userId, environment)`

Cache key format:

- `flagKey:userId:environment`

Example:

- `beta-checkout:alice:prod`

### Cache behavior

- default mode uses an in-memory cache for local development
- Redis mode uses Redis-backed caching with a 10 minute TTL
- any flag create/update/delete clears the evaluation cache
- any rule add/update/target/status change also clears the evaluation cache

This keeps evaluation fast while avoiding stale results after configuration changes.

## Audit Logging

GateKeeper now records audit entries whenever configuration changes happen.

### Logged events

- GateKeeper flag created
- GateKeeper flag updated
- GateKeeper flag deleted
- rule created
- user targets added
- percentage rollout updated
- rule enabled or disabled

Each audit entry records:

- entity type
- entity id
- action
- actor
- details
- timestamp

Current actor is stored as:

- `system`

This is a good base for later extension to authenticated users or admin identities.

## REST API

### GateKeeper Flag Management

- `POST /api/flags`
- `GET /api/flags`
- `PUT /api/flags/{id}`
- `DELETE /api/flags/{id}`

Example create request:

```json
{
  "key": "beta-checkout",
  "name": "Beta Checkout",
  "description": "Controls access to the new checkout flow",
  "enabled": true
}
```

### GateKeeper Evaluation

- `GET /api/evaluate?flagKey=beta-checkout&userId=alice&environment=prod`

Example response:

```json
{
  "flagKey": "beta-checkout",
  "userId": "alice",
  "environment": "prod",
  "enabled": true
}
```

### Rule Management

- `POST /api/flags/{flagId}/rules`
- `POST /api/rules/{ruleId}/targets`
- `PUT /api/rules/{ruleId}/percentage`
- `PATCH /api/rules/{ruleId}/status`

### Audit Logs

- `GET /api/audit-logs`
- `GET /api/audit-logs?entityType=GATEKEEPER_FLAG&entityId=1`

Example add rule request:

```json
{
  "environment": "prod",
  "ruleType": "PERCENTAGE",
  "percentage": 30,
  "enabled": true
}
```

Example add user targets request:

```json
{
  "userIds": ["alice", "bob"]
}
```

Example update rule status request:

```json
{
  "enabled": false
}
```

## Thymeleaf Pages

The app also includes simple functional server-rendered pages:

- `/flags` - GateKeeper flag list page
- `/flags/create` - create GateKeeper flag page
- `/flags/{id}` - GateKeeper flag details page
- `/flags/{id}/rules` - rule management page

## Testing

Unit tests currently cover the GateKeeper evaluation service:

- flag disabled
- global rule enabled
- targeted user rule
- percentage rollout deterministic behavior
- same user always gets the same result
- caching returns the same result without re-hitting repositories for repeated requests
- audit log service response mapping and default actor behavior

Test file:

- [AuditLogServiceTest.java](/Users/varun/gatekeeper/src/test/java/com/gatekeeper/service/AuditLogServiceTest.java)
- [GatekeeperEvaluationServiceTest.java](/Users/varun/gatekeeper/src/test/java/com/gatekeeper/service/GatekeeperEvaluationServiceTest.java)
- [GatekeeperEvaluationCachingTest.java](/Users/varun/gatekeeper/src/test/java/com/gatekeeper/service/GatekeeperEvaluationCachingTest.java)

Run tests locally:

```bash
./mvnw test
```

## CI Pipeline

The project includes a GitHub Actions pipeline at:

- [ci.yml](/Users/varun/gatekeeper/.github/workflows/ci.yml)

It runs two stages:

- `Build`
  - `./mvnw -B -DskipTests clean package`
- `Unit Tests`
  - `./mvnw -B test`

The pipeline triggers on:

- pushes to `main`, `develop` and `feature/**`
- pull requests

You can view pipeline runs in the GitHub repository `Actions` tab after pushing your branch.

## Configuration

### Default Profile: H2

The application uses the `h2` profile by default.

- in-memory database
- in-memory cache
- H2 console enabled at `/h2-console`

### Redis Profile

Use the `redis` profile when you want Redis-backed caching.

Default values in `application-redis.yml`:

- host: `localhost`
- port: `6379`

Run with both H2 and Redis:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2,redis
```

If you have Docker installed, a quick local Redis instance is:

```bash
docker run --name gatekeeper-redis -p 6379:6379 redis:7
```

### PostgreSQL Profile

Use the `postgres` profile if you want to run against PostgreSQL.

Default values in `application-postgres.yml`:

- database: `gatekeeper`
- username: `postgres`
- password: `postgres`

Update these values before running against a real local PostgreSQL instance.

## How To Run Locally

### Prerequisites

- Java 17 installed
- Maven installed and available as `mvn`

Check them:

```bash
java -version
mvn -version
```

### Run With H2

From the project root:

```bash
cd /Users/varun/gatekeeper
./mvnw spring-boot:run
```

If `./mvnw` does not work in your machine, use:

```bash
mvn spring-boot:run
```

Then open:

- app UI: [http://localhost:8080/flags](http://localhost:8080/flags)
- evaluation endpoint example: [http://localhost:8080/api/evaluate?flagKey=beta-checkout&userId=alice&environment=prod](http://localhost:8080/api/evaluate?flagKey=beta-checkout&userId=alice&environment=prod)
- H2 console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

Suggested H2 console values:

- JDBC URL: `jdbc:h2:mem:gatekeeperdb`
- User Name: `sa`
- Password: leave empty

### Run With PostgreSQL

Make sure PostgreSQL is running and the database exists, then run:

```bash
cd /Users/varun/gatekeeper
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Or:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Run With H2 And Redis

Start Redis first:

```bash
docker run --name gatekeeper-redis -p 6379:6379 redis:7
```

Then run the app:

```bash
cd /Users/varun/gatekeeper
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2,redis
```

Or:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2,redis
```

## How To Try The App

One simple manual flow:

1. Open [http://localhost:8080/flags](http://localhost:8080/flags)
2. Create a new GateKeeper flag
3. Open its details page
4. Open rule management
5. Add a `GLOBAL`, `USER_TARGET`, or `PERCENTAGE` rule for `test`, `uat`, or `prod`
6. Add user targets or set rollout percentage
7. Call the evaluation endpoint in the browser or with `curl`

Example:

```bash
curl "http://localhost:8080/api/evaluate?flagKey=beta-checkout&userId=alice&environment=prod"
```

## Architecture Diagram

```mermaid
flowchart LR
    UI["Thymeleaf Pages"] --> MVC["GatekeeperController"]
    API["REST Clients"] --> GMC["GatekeeperManagementController"]
    API --> GEC["GatekeeperEvaluationController"]
    API --> RMC["RuleManagementController"]
    API --> ALC["AuditLogController"]
    MVC --> GFS["GatekeeperFlagService"]
    GEC --> GES["GatekeeperEvaluationService"]
    GMC --> GFS
    RMC --> RMS["RuleManagementService"]
    GFS --> ALS["AuditLogService"]
    RMS --> ALS
    ALC --> ALS
    GES --> CACHE["Cache Layer"]
    GES --> GFR["GatekeeperFlagRepository"]
    GES --> ER["EnvironmentRepository"]
    GES --> FR["FlagRuleRepository"]
    GES --> UTR["UserTargetRepository"]
    ALS --> ALR["AuditLogRepository"]
    GFS --> GFR
    RMS --> GFR
    RMS --> ER
    RMS --> FR
    RMS --> UTR
    CACHE --> REDIS["Redis or Local In-Memory Cache"]
    GFR --> DB[("H2 / PostgreSQL")]
    ER --> DB
    FR --> DB
    UTR --> DB
    ALR --> DB
```
