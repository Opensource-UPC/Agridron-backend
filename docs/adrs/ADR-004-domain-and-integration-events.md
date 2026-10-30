# ADR-004: Eventos de dominio y de integración

- **Estado:** Aceptado
- **Fecha:** 2026-10-28

## Contexto

Los BCs necesitan reaccionar a cambios de otros (por ejemplo, Analytics al completarse una
misión) sin acoplarse a sus modelos internos.

## Decisión

- Los agregados registran **eventos de dominio** (`DomainEvent`) y se publican tras persistir
  mediante `DomainEventPublisher`.
- Los eventos de dominio son internos al BC.
- La comunicación entre BCs usa **eventos de integración** (`IntegrationEvent`) ubicados en
  `interfaces.events` del BC productor.
- La traducción de evento de dominio a evento de integración ocurre en
  `application/internal/eventhandlers`.

> **Nota:** la ubicación definitiva del traductor es `<bc>.interfaces.eventhandlers` con
> `@TransactionalEventListener(phase = AFTER_COMMIT)`. Ver
> [`ADR-009`](ADR-009-field-integration-events.md).

## Consecuencias

- Bajo acoplamiento entre BCs.
- Los consumidores deben ser idempotentes y tolerar reentregas.
- En el monolito la publicación es síncrona por defecto; para desacoplar se puede usar
  `@TransactionalEventListener(phase = AFTER_COMMIT)` o un broker en el futuro.
