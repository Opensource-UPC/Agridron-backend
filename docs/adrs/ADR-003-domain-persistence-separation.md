# ADR-003: Separación dominio / persistencia

- **Estado:** Aceptado
- **Fecha:** 2026-10-28

## Contexto

Anotar los agregados con JPA acopla el modelo de dominio al esquema y dificulta las pruebas.
El proyecto de referencia (Learning Center) ya demostró que la separación funciona.

## Decisión

- Los agregados de dominio son POJOs sin anotaciones JPA.
- La persistencia se implementa con entidades JPA (`XxxPersistenceEntity`) y assemblers.
- Los repositorios de dominio son interfaces; los adaptadores JPA las implementan.
- Las entidades auditables extienden `AuditableAbstractPersistenceEntity`
  (`createdAt`, `updatedAt`, `version`).
- Los nombres de tabla y columna son `snake_case` **singular**.

## Consecuencias

- El dominio se prueba sin Spring ni base de datos.
- Hay más clases (entidad de dominio, entidad JPA, assembler, adaptador), pero cada una tiene
  una responsabilidad clara.
- `@Version` habilita bloqueo optimista y protege contra actualizaciones perdidas.
