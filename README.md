# AgriDron Backend

Backend de **AgriDron Solutions**: monolito modular construido con **Java 27**, **Spring Boot 4.1.1**
y **PostgreSQL + PostGIS**. Implementa DDD táctico y CQRS ligero por *bounded context*.

Este repositorio contiene el **shared kernel** (base técnica común) sobre el que se implementan
los seis bounded contexts del producto.

## Stack

| Elemento | Versión / decisión |
|---|---|
| Java | 27 |
| Spring Boot | 4.1.1 |
| Build | Maven (wrapper incluido) |
| Base de datos | PostgreSQL 17 + PostGIS |
| Migraciones | Flyway |
| Persistencia | Spring Data JPA + Hibernate Spatial |
| Documentación API | SpringDoc OpenAPI 3 |
| Pruebas | JUnit 5, AssertJ, Mockito, Testcontainers |
| Arquitectura | Monolito modular + DDD por capas + CQRS |

## Requisitos

- JDK 27
- Docker y Docker Compose
- (Opcional) Maven 3.9+; el proyecto incluye `./mvnw`

## Inicio rápido

```bash
cp .env.example .env

# Base de datos PostGIS
docker compose up -d postgres

# Aplicación
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

| Recurso | URL |
|---|---|
| Estado | `GET /api/v1/status` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Health | `http://localhost:8080/actuator/health` |

## Estructura

```text
src/main/java/pe/com/agridron
├── AgridronApplication.java
├── field                           # Field Management (fincas, parcelas, cultivos, fumigación)
│   ├── application                 # command/query services
│   ├── domain                      # agregados, value objects, eventos, repositorios
│   ├── infrastructure/persistence  # entidades JPA y adaptadores
│   └── interfaces/rest             # controladores, resources y assemblers
└── shared
    ├── application/result          # Result<T, E> y ApplicationError
    ├── domain
    │   ├── events                  # DomainEvent / IntegrationEvent
    │   └── model/aggregates        # AbstractDomainAggregateRoot
    ├── infrastructure
    │   ├── concurrency             # Virtual threads
    │   ├── documentation           # OpenAPI
    │   ├── events                  # DomainEventPublisher
    │   ├── i18n                    # LocaleResolver
    │   ├── persistence/jpa         # Entidades base, auditoría, naming strategy
    │   └── web                     # CORS
    └── interfaces/rest             # ProblemDetail, manejador global, status
```

El resto de bounded contexts (`iam`, `flight`, `weather`, `analytics`, `inventory`) se
agregarán como paquetes hermanos de `shared`, cada uno con las capas `domain`, `application`,
`infrastructure` e `interfaces`.

## Pruebas

```bash
./mvnw test          # pruebas unitarias y de slice
./mvnw verify        # incluye pruebas de integración con Testcontainers
```

Las pruebas de integración usan la imagen `postgis/postgis:17-3.5` y se omiten automáticamente
si Docker no está disponible.

## Convenciones

Antes de escribir código en un bounded context, lee
[`docs/backend-conventions.md`](docs/backend-conventions.md).

Decisiones de arquitectura registradas en [`docs/adrs`](docs/adrs).

El mapeo de persistencia de Field Management está en
[`docs/field-persistence.md`](docs/field-persistence.md) y su contrato HTTP en
[`docs/field-api.md`](docs/field-api.md).

## Ramas

- `main`: versiones liberadas.
- `develop`: integración.
- `feature/<bc>-<descripcion>`: trabajo por bounded context.
- `hotfix/<descripcion>`: correcciones urgentes.

Los commits siguen [Conventional Commits](https://www.conventionalcommits.org/).
