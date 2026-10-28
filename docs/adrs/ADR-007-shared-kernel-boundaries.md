# ADR-007: Límites del shared kernel

- **Estado:** Aceptado
- **Fecha:** 2026-10-28

## Contexto

El módulo `shared` debe desbloquear a los seis equipos sin convertirse en un cajón de sastre
que acople todos los bounded contexts.

## Decisión

`shared` contiene únicamente preocupaciones **técnicas y transversales**:

- `Result` y `ApplicationError`.
- Manejo global de errores y RFC 9457.
- Agregado base y publicación de eventos.
- Entidades JPA base, auditoría y naming strategy.
- i18n, OpenAPI, virtual threads, CORS y configuración común.

`shared` **no** contiene:

- Entidades o reglas de negocio de ningún BC.
- Seguridad/JWT (pertenece a IAM).
- Clientes externos (clima pertenece a Weather).
- Eventos de integración concretos.

Regla de dependencia: `shared` no importa ningún paquete de los BCs; los BCs pueden importar
`shared`.

## Consecuencias

- El kernel se mantiene pequeño y estable.
- Cualquier clase nueva en `shared` requiere justificar que es transversal.
- Los cambios en `shared` impactan a todos los equipos y deben revisarse con cuidado.
