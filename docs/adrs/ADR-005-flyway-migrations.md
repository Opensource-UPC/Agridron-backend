# ADR-005: Flyway como única fuente del esquema

- **Estado:** Aceptado
- **Fecha:** 2026-10-28

## Contexto

El proyecto de referencia usa `ddl-auto=update` en desarrollo, lo que produce esquemas
divergentes y peligrosos en producción. AgriDron necesita un esquema reproducible y auditable.

## Decisión

- Flyway es la única fuente de verdad del esquema.
- `spring.jpa.hibernate.ddl-auto=validate` en todos los perfiles.
- Las migraciones viven en `src/main/resources/db/migration`.
- Los rangos de versión están reservados por bounded context (V1-V99 shared, V100-V199 IAM,
  V200-V299 Field, V300-V399 Flight, V400-V499 Weather, V500-V599 Analytics, V600-V699 Inventory).

## Consecuencias

- El esquema es reproducible desde cero y versionado en Git.
- Cada cambio de entidad requiere su migración; el build falla si el esquema no coincide.
- Varios equipos pueden agregar migraciones en paralelo sin colisiones de versión.
