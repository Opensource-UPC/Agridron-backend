# Eventos de integración de Field Management

Field Management publica eventos de integración para que otros bounded contexts reaccionen a
cambios de fincas, parcelas y áreas de fumigación sin acoplarse a su modelo de dominio. La
decisión de arquitectura está en
[`ADR-009`](adrs/ADR-009-field-integration-events.md).

## Contrato

Todos los eventos viven en `pe.com.agridron.field.interfaces.events`, son `record` inmutables e
implementan `IntegrationEvent`:

| Campo | Tipo | Descripción |
|---|---|---|
| `eventId` | `String` | UUID único de la instancia; los consumidores deduplican por este valor |
| `occurredOn` | `Instant` | Momento en que se tradujo el evento de dominio |
| `eventType` | `String` | Nombre estable del evento |

`eventType` sigue el formato `<bc>.<agregado>.<accion-en-pasado>` y no debe cambiar. Los cambios
de payload deben ser aditivos.

## Catálogo

| `eventType` | Evento de dominio | Payload |
|---|---|---|
| `field.farm.registered` | `FarmRegisteredEvent` | `farmId`, `ownerId`, `name`, `location`, `department` |
| `field.farm.supervisor-assigned` | `FarmSupervisorAssignedEvent` | `farmId`, `supervisorId` |
| `field.farm.supervisor-removed` | `FarmSupervisorRemovedEvent` | `farmId`, `supervisorId` |
| `field.parcel.registered` | `ParcelRegisteredEvent` | `parcelId`, `farmId`, `areaHectares` |
| `field.parcel.geometry-updated` | `ParcelGeometryUpdatedEvent` | `parcelId`, `farmId`, `areaHectares` |
| `field.parcel.crop-information-registered` | `ParcelCropInformationRegisteredEvent` | `parcelId`, `farmId`, `cropId`, `phenologicalStage` |
| `field.parcel.status-changed` | `ParcelStatusChangedEvent` | `parcelId`, `farmId`, `status` |
| `field.fumigation-area.delimited` | `FumigationAreaDelimitedEvent` | `fumigationAreaId`, `parcelId`, `areaHectares` |

`status` y `phenologicalStage` viajan como el nombre del enum (`ACTIVE`, `INACTIVE`,
`VEGETATIVE`, `FLOWERING`, …) para desacoplar a los consumidores de los enums de Field.

## Publicación

- El agregado registra un evento de dominio y el adaptador de repositorio lo publica dentro de
  la transacción con `DomainEventPublisher`.
- `field.interfaces.eventhandlers` traduce cada evento de dominio con
  `@TransactionalEventListener(phase = AFTER_COMMIT)` y publica el evento de integración mediante
  `IntegrationEventPublisher`.
- Si la transacción se revierte, **no se publica** ningún evento de integración.
- La entrega es in-process y síncrona dentro del hilo que confirma la transacción.

## Ejemplo

```json
{
  "eventId": "3f1c2a9e-5b7d-4c1a-9f2e-8d6b4a0c1e77",
  "occurredOn": "2026-10-30T15:42:11.512Z",
  "eventType": "field.parcel.status-changed",
  "parcelId": 42,
  "farmId": 7,
  "status": "INACTIVE"
}
```

## Consumo

Los consumidores deben:

1. Ser idempotentes por `eventId` (por ejemplo, con una tabla de eventos procesados).
2. No lanzar excepciones desde el listener: ocurre después del commit y no revierte la escritura.
3. Tolerar reentregas y ausencia de orden entre agregados.

## Limitaciones actuales

- **Sin outbox:** un fallo del proceso justo después del commit puede perder el evento. La
  entrega es *at-most-once*.
- **Sin reintentos ni dead-letter queue.**
- **Sin garantía de orden** entre eventos de distintos agregados.
- **Sin versionado explícito:** `eventType` es estable y los cambios deben ser aditivos.
- Sin consumidores reales todavía; el contrato se valida con pruebas de integración y ArchUnit.

## Cómo agregar un evento

1. Registrar el evento de dominio en el agregado (`registerDomainEvent`).
2. Crear el `record` en `interfaces.events` con `EVENT_TYPE`, `eventId`, `occurredOn`, payload y
   una factory `from(...)`.
3. Añadir el método `@TransactionalEventListener(phase = AFTER_COMMIT)` en el handler
   correspondiente de `interfaces.eventhandlers`.
4. Actualizar esta tabla y añadir pruebas de mapeo y de publicación after-commit.
