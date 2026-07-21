# REST API Blueprints - Java 21 / Spring Boot 3.3.x

> ## SUBMISSION NOTE (please read first)
> **This project is being pushed to the repository only now.** It was fully
> developed and kept **on my local machine** because **no Microsoft Teams
> assignment/submission was ever opened** for this lab. As soon as a place to
> deliver it was available, I uploaded the complete work. Everything you see
> here is my own development - nothing is pending.
>
> **Student / Author: STIVEN ESNEIDER PARDO GUTIERREZ**
> Escuela Colombiana de Ingenieria - Software Architecture (ARSW)

### Evidence of local development dates

The screenshot below shows the file "Date modified" timestamps, evidencing that
this project was developed and kept locally before being uploaded to the
repository:

![Local development dates evidence - Lab 5](LABORATORIO%205%20EVIDENCIA.png)

---

## 1. Overview

REST API for managing **Blueprints** (drawings made of ordered `Point`s),
built with **Java 21** and **Spring Boot 3.3.x**. The application is organized
in logical layers (model, persistence, services, filters, controllers, config,
dto) and supports two interchangeable persistence backends selected through
**Spring profiles**:

- **In-memory** (default) - the app boots with no database.
- **PostgreSQL** (profile `postgres`) - via Spring Data JPA.

## 2. Architecture

```
src/main/java/edu/eci/arsw/blueprints
  ├── model/         Blueprint, Point (domain)
  ├── persistence/   BlueprintPersistence (contract) + InMemory impl + exceptions
  │    └── impl/     PostgresBlueprintPersistence, JPA entities, Spring Data repo
  ├── services/      BlueprintsServices (business logic)
  ├── filters/       Identity / Redundancy / Undersampling (BlueprintsFilter)
  ├── controllers/   BlueprintsAPIController + GlobalExceptionHandler
  ├── dto/           ApiResponse<T> (uniform response envelope)
  └── config/        OpenApiConfig (Swagger)
```

The `BlueprintPersistence` interface is the single contract; both the in-memory
and the PostgreSQL implementations honor it, so the service and controller
layers are unaware of the storage technology.

## 3. What was implemented (mapping to the lab activities)

| # | Activity | Status |
|---|----------|--------|
| 1 | Familiarization with the base code | Done - layered structure preserved |
| 2 | Migration to **PostgreSQL** persistence | Done - `PostgresBlueprintPersistence` (JPA) selectable by profile |
| 3 | REST best practices | Done - base path `/api/v1/blueprints`, correct HTTP codes, `ApiResponse<T>`, global error handling |
| 4 | OpenAPI / Swagger | Done - `springdoc-openapi` + `@Operation` / `@ApiResponses` |
| 5 | Blueprint **filters** | Done - Redundancy and Undersampling via profiles |
| Bonus | Actuator + Docker image | Done - Actuator (health/info/metrics) + multi-stage Dockerfile |

### Persistence (Activity 2)
- `persistence/impl/BlueprintEntity` + `PointEmbeddable` map a blueprint to the
  `blueprints` / `blueprint_points` tables, keeping point **order**
  (`@OrderColumn`).
- `SpringDataBlueprintRepository` (Spring Data JPA) provides queries.
- `PostgresBlueprintPersistence` (`@Profile("postgres")`) adapts entities to the
  domain model and implements the full `BlueprintPersistence` contract.
- `InMemoryBlueprintPersistence` is annotated `@Profile("!postgres")`, so exactly
  one persistence bean is active at a time.

### REST best practices (Activity 3)
- **Base path:** `/api/v1/blueprints`.
- **HTTP status codes:** `200 OK`, `201 Created` (POST), `202 Accepted`
  (PUT point), `400 Bad Request` (validation), `404 Not Found`,
  `409 Conflict` (duplicate).
- **Uniform response** `dto/ApiResponse<T>`:
  ```json
  { "code": 200, "message": "execute ok", "data": { "...": "..." } }
  ```
- **Centralized error handling** via `GlobalExceptionHandler`
  (`@RestControllerAdvice`).

## 4. Endpoints

| Method | Path | Success code |
|--------|------|--------------|
| GET | `/api/v1/blueprints` | 200 |
| GET | `/api/v1/blueprints/{author}` | 200 / 404 |
| GET | `/api/v1/blueprints/{author}/{name}` | 200 / 404 |
| POST | `/api/v1/blueprints` | 201 / 400 / 409 |
| PUT | `/api/v1/blueprints/{author}/{name}/points` | 202 / 404 |
| DELETE | `/api/v1/blueprints/{author}/{name}` | 200 / 404 |

`GET /api/v1/blueprints/{author}` also returns the author's **total point
count**.

## 5. Requirements
- Java 21+ (built and tested with JDK 21/23)
- Maven 3.9+
- (Optional) Docker, for PostgreSQL

## 6. How to run

### In-memory (default - no database needed)
```bash
mvn spring-boot:run
```

### With PostgreSQL
```bash
docker compose up -d          # PostgreSQL on localhost:5432
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### With filters
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=redundancy
mvn spring-boot:run -Dspring-boot.run.profiles=undersampling
# combine with postgres if needed:
mvn spring-boot:run -Dspring-boot.run.profiles=postgres,redundancy
```

### Quick check with curl
```bash
curl -s http://localhost:8080/api/v1/blueprints/john | jq
curl -i -X POST http://localhost:8080/api/v1/blueprints \
  -H 'Content-Type: application/json' \
  -d '{"author":"john","name":"kitchen","points":[{"x":1,"y":1},{"x":2,"y":2}]}'
curl -i -X PUT http://localhost:8080/api/v1/blueprints/john/kitchen/points \
  -H 'Content-Type: application/json' -d '{"x":3,"y":3}'
curl -i -X DELETE http://localhost:8080/api/v1/blueprints/john/kitchen
```

## 7. API documentation (Swagger / OpenAPI)
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

## 8. Observability (bonus)
Spring Boot Actuator is enabled:
- Health: <http://localhost:8080/actuator/health>
- Metrics: <http://localhost:8080/actuator/metrics>

## 9. Tests
```bash
mvn test
```
- `FiltersTest` - redundancy / undersampling / identity filters.
- `BlueprintsServicesTest` - CRUD and error paths over in-memory storage.
- `PostgresPersistenceTest` - JPA persistence over **H2 in PostgreSQL mode**
  (no container required).
- `BlueprintsSmokeTest` - Spring context load.

**Result: 13 tests, all passing.**

## 10. Container image (bonus)
```bash
mvn -DskipTests package
docker build -t blueprints-api .
docker run -p 8080:8080 blueprints-api
```

---
Author: **STIVEN ESNEIDER PARDO GUTIERREZ** - ARSW, Escuela Colombiana de Ingenieria.
