# ADR-009: Eventos de integración de Field Management

- **Estado:** Aceptado
- **Fecha:** 2026-10-30

## Contexto

Field Management ya expone su operación por REST, pero los demás bounded contexts (Flight
Operations, Analytics, Weather) necesitan reaccionar a cambios de fincas, parcelas y áreas de
fumigación sin acoplarse al modelo de dominio de Field. El
[`ADR-004`](ADR-004-domain-and-integration-events.md) definió el uso de eventos de integración,
pero dejó abiertos dos puntos que esta fase concreta:

1. Dónde vive el traductor de evento de dominio a evento de integración.
2. Cómo se garantiza que los consumidores no observen cambios revertidos.

La convención original ubicaba la traducción en `application/internal/eventhandlers`. Esa capa
solo puede depender del dominio (regla ArchUnit `application → domain`), pero el traductor
necesita el evento de integración (`interfaces.events`) y el publicador
(`shared.infrastructure.events`). Mantenerlo en `application` obligaría a romper la regla de
dependencias o a duplicar el contrato.

## Decisión

- Field publica **ocho eventos de integración**, uno por cada evento de dominio existente, en
  `field.interfaces.events`. El catálogo y los payloads están en
  [`docs/field-events.md`](../field-events.md).
- Cada evento es un `record` inmutable que implementa `IntegrationEvent` y expone:
  - `eventId`: UUID generado por instancia al traducir.
  - `occurredOn`: `Instant` fijado al traducir.
  - `eventType`: constante estable con el formato `<bc>.<agregado>.<accion-en-pasado>`.
  - El payload mínimo para que el consumidor pueda correlacionar sin consultar Field. Los
    eventos de parcela incluyen `farmId`.
- La traducción vive en **`<bc>.interfaces.eventhandlers`**, no en `application`. Es un
  adaptador de entrada: lee eventos internos y publica el lenguaje publicado del BC.
- Los métodos traductores usan `@TransactionalEventListener(phase = AFTER_COMMIT)`. Así el
  evento de integración solo se publica si la transacción confirma; un rollback no emite nada.
- La publicación se hace mediante `IntegrationEventPublisher`
  (`shared.infrastructure.events`), que delega en `ApplicationEventPublisher`.
- Los enums de dominio (`ParcelStatus`, `PhenologicalStage`) se exponen como su nombre estable
  (`String`) para que los consumidores no dependan de los enums de Field.
- **No se usa outbox en esta fase.** La entrega es in-process, after-commit y at-most-once.
- Los consumidores deben ser **idempotentes por `eventId`** y tolerar reentregas.
- El contrato de `interfaces.events` se verifica con una regla ArchUnit: toda clase del paquete
  implementa `IntegrationEvent`, y todo `*IntegrationEventsHandler` vive en
  `interfaces.eventhandlers`.

## Consecuencias

- Los BCs consumidores dependen solo del lenguaje publicado de Field, no de sus agregados ni de
  sus repositorios.
- La regla de dependencias se mantiene: `domain` no conoce eventos de integración, `application`
  no conoce adaptadores y `interfaces` puede depender de `domain` y `shared`.
- Un fallo en un listener after-commit ocurre **después** del commit; no revierte la escritura.
  Los consumidores no deben lanzar excepciones y deben registrar sus propios fallos.
- Al no haber outbox, un proceso que se cae justo después del commit puede perder el evento. Si
  se requiere entrega garantizada, se incorporará un outbox transaccional y un relay.
- No hay garantía de orden entre agregados ni reintentos automáticos.
- `eventType` es estable y los cambios de payload deben ser aditivos; un cambio incompatible
  requiere una nueva versión del evento.
- La convención §7.2 de [`docs/backend-conventions.md`](../backend-conventions.md) queda
  alineada con esta decisión.
