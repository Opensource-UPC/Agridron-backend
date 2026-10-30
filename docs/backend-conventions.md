# Convenciones del backend AgriDron

Documento de referencia obligatorio para todos los bounded contexts. Si una decisión no está
aquí, se discute en el equipo y se registra como ADR antes de implementarla.

---

## 1. Bounded contexts

| BC | Paquete | Responsabilidad |
|---|---|---|
| Identity & Access Management | `iam` | Usuarios, credenciales, roles, sesiones y tokens |
| Field Management | `field` | Fincas, parcelas, áreas de fumigación y cultivos |
| Flight Operations | `flight` | Misiones, incidentes, telemetría y resultados |
| Weather Integration | `weather` | Clima, evaluación de condiciones y alertas |
| Analytics & Reporting | `analytics` | Historial, reportes, métricas e indicadores |
| Inventory & Resource Management | `inventory` | Drones, boquillas, insumos y mantenimiento |
| Shared kernel | `shared` | Preocupaciones técnicas transversales |

**Regla:** `shared` no contiene lógica de negocio de ningún BC. Si un concepto pertenece al
dominio, vive en su bounded context.

---

## 2. Estructura de un bounded context

```text
pe.com.agridron.<bc>
├── domain
│   ├── model
│   │   ├── aggregates
│   │   ├── entities
│   │   ├── valueobjects
│   │   └── commands
│   ├── events
│   ├── repositories
│   ├── ports
│   └── exceptions
├── application
│   ├── commandservices
│   ├── queryservices
│   ├── acl
│   └── internal
│       ├── commandservices
│       ├── queryservices
│       ├── eventhandlers
│       └── outboundservices
├── infrastructure
│   ├── persistence/jpa
│   │   ├── entities
│   │   ├── assemblers
│   │   ├── converters
│   │   ├── repositories
│   │   └── adapters
│   └── <otros adaptadores>
└── interfaces
    ├── rest
    │   ├── resources
    │   ├── transform
    │   └── <XxxController>
    ├── acl
    └── events
```

### Reglas de dependencia (verificadas por ArchUnit)

1. `domain` no depende de `application`, `infrastructure` ni `interfaces`.
2. `application` no depende de `infrastructure` ni `interfaces`.
3. `infrastructure` implementa puertos definidos en `domain` o `application`.
4. `interfaces` depende de `application`.
5. Un BC **nunca** importa clases de `domain`, `application` o `infrastructure` de otro BC.
   Solo puede comunicarse mediante:
   - `interfaces.acl` del BC productor (llamada síncrona), o
   - eventos de integración publicados por el BC productor.
6. `shared` no depende de ningún bounded context.

---

## 3. Resultados y errores

### 3.1 `Result<T, E>`

Los servicios de aplicación devuelven `Result<T, ApplicationError>` para fallos esperados.
No se lanzan excepciones para reglas de negocio.

```java
public Result<Farm, ApplicationError> handle(CreateFarmCommand command) {
    if (repository.existsByName(command.name())) {
        return Result.failure(ApplicationError.conflict("Farm", "Name already exists"));
    }
    var farm = Farm.create(command.name(), command.ownerId());
    repository.save(farm);
    return Result.success(farm);
}
```

### 3.2 `ApplicationError`

| Factory | Código generado | HTTP |
|---|---|---|
| `validationError(...)` | `VALIDATION_ERROR` | 400 |
| `unauthorized(...)` | `UNAUTHORIZED` | 401 |
| `invalidCredentials(...)` | `INVALID_CREDENTIALS` | 401 |
| `accessDenied(...)` | `ACCESS_DENIED` | 403 |
| `notFound("Farm", id)` | `FARM_NOT_FOUND` | 404 |
| `conflict("Farm", reason)` | `FARM_CONFLICT` | 409 |
| `businessRuleViolation(...)` | `BUSINESS_RULE_VIOLATION` | 422 |
| `invalidState("Mission", reason)` | `MISSION_INVALID_STATE` | 422 |
| `serviceUnavailable("Weather", reason)` | `WEATHER_UNAVAILABLE` | 503 |
| `unexpected(context, null)` | `UNEXPECTED_ERROR` | 500 |

Sufijos reconocidos por el mapeador:

| Sufijo / código | HTTP |
|---|---|
| `_NOT_FOUND` | 404 |
| `_CONFLICT`, `_ALREADY_EXISTS` | 409 |
| `_INVALID_STATE`, `_NOT_AVAILABLE`, `_NOT_SUITABLE`, `_INSUFFICIENT`, `INSUFFICIENT_*` | 422 |
| `_UNAVAILABLE`, `SERVICE_UNAVAILABLE` | 503 |

`INVALID_GEOMETRY` es un código específico de Field y se mapea a **400 Bad Request**.

### 3.3 Registro de códigos por BC

| BC | Códigos reservados |
|---|---|
| IAM | `USER_NOT_FOUND`, `USER_CONFLICT`, `ROLE_NOT_FOUND`, `ROLE_CONFLICT`, `INVALID_CREDENTIALS`, `ACCESS_DENIED`, `TOKEN_EXPIRED` |
| Field | `FARM_NOT_FOUND`, `FARM_CONFLICT`, `PARCEL_NOT_FOUND`, `PARCEL_CONFLICT`, `PARCEL_INVALID_STATE`, `CROP_NOT_FOUND`, `FUMIGATION_AREA_NOT_FOUND`, `INVALID_GEOMETRY` |
| Flight | `MISSION_NOT_FOUND`, `MISSION_CONFLICT`, `MISSION_INVALID_STATE`, `DRONE_NOT_AVAILABLE`, `WEATHER_NOT_SUITABLE`, `INCIDENT_NOT_FOUND` |
| Weather | `WEATHER_UNAVAILABLE`, `WEATHER_DATA_UNAVAILABLE` |
| Analytics | `REPORT_NOT_FOUND`, `REPORT_CONFLICT`, `HISTORY_NOT_FOUND` |
| Inventory | `DRONE_NOT_FOUND`, `DRONE_CONFLICT`, `NOZZLE_NOT_FOUND`, `SUPPLY_NOT_FOUND`, `INSUFFICIENT_STOCK`, `MAINTENANCE_NOT_FOUND` |

Los mensajes localizados se agregan en `messages.properties` y `messages_es.properties` con la
clave `error.<codigo-en-kebab-case>.message`. Si no existe, se usa el mensaje genérico de la
categoría.

---

## 4. REST

- Base: `/api/v1`.
- Recursos en plural e inglés: `/api/v1/farms`, `/api/v1/missions`.
- DTOs: `XxxResource` (entrada y salida).
- Transformadores: `XxxResourceFromEntityAssembler` y `XxxCommandFromResourceAssembler`.
- Verbos: `GET` (consulta), `POST` (creación), `PUT` (reemplazo), `PATCH` (parcial), `DELETE`.
- Éxito: `Result` → `ResponseEntityAssembler`.
- Error: siempre RFC 9457 (`application/problem+json`).
- Nunca devolver entidades JPA ni agregados de dominio directamente.

Ejemplo de controlador:

```java
@RestController
@RequestMapping("/api/v1/farms")
public class FarmsController {
    private final FarmCommandService farmCommandService;

    @PostMapping
    public ResponseEntity<?> createFarm(@Valid @RequestBody CreateFarmResource resource) {
        var command = CreateFarmCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = farmCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, FarmResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }
}
```

---

## 5. Persistencia

### 5.1 Separación dominio / persistencia

- Los agregados de dominio son POJOs sin anotaciones JPA.
- Cada agregado tiene una `XxxPersistenceEntity` en `infrastructure/persistence/jpa/entities`.
- Los assemblers convierten entre agregado y entidad.
- Los repositorios de dominio son interfaces; los adaptadores JPA las implementan.

### 5.2 Entidades base

- `AbstractPersistenceEntity`: `id` autogenerado.
- `AuditableAbstractPersistenceEntity`: agrega `createdAt`, `updatedAt` y `version`.

Usar la entidad auditable salvo que exista una razón concreta para no hacerlo.

### 5.3 Nombres

| Elemento | Convención | Ejemplo |
|---|---|---|
| Tabla | `snake_case` **singular** | `farm`, `fumigation_area` |
| Columna | `snake_case` | `created_at`, `owner_id` |
| FK | `<entidad>_id` | `farm_id`, `mission_id` |
| Índice | `idx_<tabla>_<columna>` | `idx_mission_status` |
| Unique | `uk_<tabla>_<columna>` | `uk_user_account_email` |

La estrategia `SnakeCasePhysicalNamingStrategy` aplica la conversión automáticamente.
**No** se pluralizan tablas.

### 5.4 Claves foráneas

- **Dentro de un mismo BC:** se permiten FKs físicas.
- **Entre BCs distintos:** solo se almacena el ID (`parcel_id`, `drone_id`), sin FK física.
  La consistencia se mantiene por aplicación y eventos.

### 5.5 Concurrencia

- `@Version` está habilitado en `AuditableAbstractPersistenceEntity`.
- Los conflictos de bloqueo optimista se traducen a `409 Conflict`.

---

## 6. Transacciones

- Todo método `handle` de un command service lleva `@Transactional`.
- Las consultas usan `@Transactional(readOnly = true)` cuando acceden a relaciones lazy.
- `spring.jpa.open-in-view=false`: no confiar en la sesión abierta en la capa web.
- Los eventos se publican después de persistir, dentro de la misma transacción.
  Si un handler necesita ejecutarse tras el commit, usar
  `@TransactionalEventListener(phase = AFTER_COMMIT)`.

---

## 7. Eventos

### 7.1 Eventos de dominio

- Viven en `<bc>.domain.events`.
- Nombre en pasado: `MissionCreatedEvent`, `MissionCompletedEvent`.
- Se registran con `registerDomainEvent(...)` en el agregado.
- Se publican con `DomainEventPublisher.publishAndClearEvents(aggregate)` en el adaptador
  de repositorio.

### 7.2 Eventos de integración

- Viven en `<bc>.interfaces.events` y son `record` inmutables.
- Implementan `IntegrationEvent` (`eventId`, `occurredOn`, `eventType`).
- `eventId` es un UUID único por instancia, `occurredOn` se fija al traducir y `eventType` es
  una constante estable con el formato `<bc>.<agregado>.<accion-en-pasado>`.
- Se producen traduciendo un evento de dominio en `<bc>.interfaces.eventhandlers` con
  `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- La traducción vive en `interfaces` porque es un adaptador de entrada y `application` no puede
  depender de `interfaces` ni de `infrastructure` (regla ArchUnit). Ver
  [`ADR-009`](adrs/ADR-009-field-integration-events.md).
- Los consumidores deben ser idempotentes por `eventId`.
- Sin outbox, la entrega es in-process, after-commit y *at-most-once*.
- **Ningún BC consume eventos de dominio de otro BC.**

---

## 8. Migraciones Flyway

Ubicación única: `src/main/resources/db/migration`.

| Bloque | BC |
|---|---|
| `V1`–`V99` | Shared / baseline |
| `V100`–`V199` | IAM |
| `V200`–`V299` | Field Management |
| `V300`–`V399` | Flight Operations |
| `V400`–`V499` | Weather |
| `V500`–`V599` | Analytics |
| `V600`–`V699` | Inventory |

Formato: `V<bloque><secuencia>__<descripcion>.sql`.

Ejemplos:

```text
V101__create_user_account.sql
V201__create_farm_tables.sql
V301__create_mission_tables.sql
```

Reglas:

- `ddl-auto=validate` en todos los perfiles; el esquema lo define Flyway.
- Una migración aplicada **no se modifica**; se agrega una nueva.
- Toda tabla nueva debe incluir `created_at`, `updated_at` y `version` si usa la entidad auditable.
- PostGIS ya está disponible en la imagen; usar `geometry(Point, 4326)` y `geometry(Polygon, 4326)`.

---

## 9. Pruebas

| Nivel | Herramientas | Ubicación |
|---|---|---|
| Dominio | JUnit 5 + AssertJ, sin Spring | `src/test/java/.../<bc>/domain` |
| Aplicación | JUnit 5 + Mockito | `src/test/java/.../<bc>/application` |
| Controladores | `MockMvc` standalone o `@WebMvcTest` | `.../interfaces/rest` |
| Persistencia | `@DataJpaTest` + Testcontainers | `.../infrastructure/persistence` |
| Integración | `@SpringBootTest` + Testcontainers | `src/test/java/.../<bc>` |
| Arquitectura | ArchUnit | `src/test/java/.../architecture` |

- Cobertura objetivo: 70 % en `domain` y `application`.
- Los tests deben ser deterministas: no depender de hora, red ni orden de ejecución.

---

## 10. Definición de terminado (por BC)

- [ ] Estructura de paquetes y reglas de capas respetadas.
- [ ] Agregados sin dependencias de JPA.
- [ ] Endpoints versionados en `/api/v1`.
- [ ] Errores con `Result` + `ApplicationError` + RFC 9457.
- [ ] Migraciones Flyway en el bloque correspondiente.
- [ ] Pruebas unitarias de dominio y aplicación.
- [ ] Prueba de integración con Testcontainers.
- [ ] OpenAPI actualizado.
- [ ] Documentación del BC y ADRs si hubo decisiones nuevas.
- [ ] `./mvnw verify` en verde.
