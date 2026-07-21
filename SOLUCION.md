# Solución — Lab REST API Blueprints (Java 21 / Spring Boot 3.3.x)

Este documento resume **qué se implementó** respecto al enunciado (`README.md`).

## ✅ Actividades resueltas

### 1. Familiarización
Se mantuvo la arquitectura por capas: `model`, `persistence`, `services`,
`filters`, `controllers`, `config`, y se agregó `dto`.

### 2. Persistencia en PostgreSQL
- Nueva implementación `persistence/impl/PostgresBlueprintPersistence` que
  **respeta el contrato** `BlueprintPersistence`.
- Mapeo con Spring Data JPA: `BlueprintEntity` + `PointEmbeddable`
  (colección ordenada con `@OrderColumn`) y `SpringDataBlueprintRepository`.
- Selección por **perfil de Spring**:
  - Sin perfil → `InMemoryBlueprintPersistence` (`@Profile("!postgres")`),
    la app arranca **sin base de datos**.
  - Perfil `postgres` → `PostgresBlueprintPersistence` (`@Profile("postgres")`).
- Configuración en `application-postgres.properties` y `docker-compose.yml`
  para levantar PostgreSQL.

### 3. Buenas prácticas de API REST
- Path base cambiado a **`/api/v1/blueprints`**.
- **Códigos HTTP** correctos: `200`, `201` (POST), `202` (PUT punto),
  `400` (validación), `404` (no existe), `409` (duplicado).
- Respuesta uniforme `dto/ApiResponse<T>` = `{ code, message, data }`.
- Manejo centralizado de errores con `GlobalExceptionHandler`
  (`@RestControllerAdvice`).
- Se agregó `DELETE /api/v1/blueprints/{author}/{name}` para CRUD completo.

### 4. OpenAPI / Swagger
- `springdoc-openapi` ya configurado (`OpenApiConfig`).
- Endpoints anotados con `@Operation` y `@ApiResponses`.
- UI: `http://localhost:8080/swagger-ui.html` — JSON: `/v3/api-docs`.

### 5. Filtros de Blueprints
- `RedundancyFilter` (perfil `redundancy`) y `UndersamplingFilter`
  (perfil `undersampling`), con `IdentityFilter` por defecto.

### Bonus
- **Actuator** habilitado (`/actuator/health`, `/info`, `/metrics`).
- **Dockerfile** multi-stage para imagen de contenedor.

## ▶️ Ejecución

### En memoria (por defecto)
```bash
mvn spring-boot:run
```

### Con PostgreSQL
```bash
docker compose up -d           # levanta Postgres en localhost:5432
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Con filtros
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=redundancy
mvn spring-boot:run -Dspring-boot.run.profiles=undersampling
# combinable con postgres:
mvn spring-boot:run -Dspring-boot.run.profiles=postgres,redundancy
```

## 🔗 Endpoints
| Método | Ruta | Código éxito |
|--------|------|--------------|
| GET | `/api/v1/blueprints` | 200 |
| GET | `/api/v1/blueprints/{author}` | 200 / 404 |
| GET | `/api/v1/blueprints/{author}/{name}` | 200 / 404 |
| POST | `/api/v1/blueprints` | 201 / 400 / 409 |
| PUT | `/api/v1/blueprints/{author}/{name}/points` | 202 / 404 |
| DELETE | `/api/v1/blueprints/{author}/{name}` | 200 / 404 |

Ejemplo:
```bash
curl -s http://localhost:8080/api/v1/blueprints/john | jq
curl -i -X POST http://localhost:8080/api/v1/blueprints \
  -H 'Content-Type: application/json' \
  -d '{"author":"john","name":"kitchen","points":[{"x":1,"y":1},{"x":2,"y":2}]}'
curl -i -X PUT http://localhost:8080/api/v1/blueprints/john/kitchen/points \
  -H 'Content-Type: application/json' -d '{"x":3,"y":3}'
```

## 🧪 Pruebas
```bash
mvn test
```
- `FiltersTest` — filtros redundancy/undersampling/identity.
- `BlueprintsServicesTest` — CRUD y errores sobre persistencia en memoria.
- `PostgresPersistenceTest` — persistencia JPA sobre **H2 en modo PostgreSQL**
  (no requiere contenedor).
- `BlueprintsSmokeTest` — carga de contexto.

Resultado: **13 pruebas, todas en verde.**
