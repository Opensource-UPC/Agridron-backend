# Migraciones Flyway

Esta carpeta contiene las migraciones SQL del proyecto. Flyway las aplica en orden al arrancar.

Los bloques de versiones están reservados por bounded context para evitar colisiones:

| Bloque | Bounded context | Ejemplo |
|---|---|---|
| `V1`–`V99` | Shared / baseline | `V1__create_shared_baseline.sql` |
| `V100`–`V199` | IAM | `V101__create_user_account.sql` |
| `V200`–`V299` | Field Management | `V201__create_farm_tables.sql` |
| `V300`–`V399` | Flight Operations | `V301__create_mission_tables.sql` |
| `V400`–`V499` | Weather | `V401__create_weather_tables.sql` |
| `V500`–`V599` | Analytics | `V501__create_report_tables.sql` |
| `V600`–`V699` | Inventory | `V601__create_inventory_tables.sql` |

Reglas:

1. Una migración aplicada no se edita; se agrega una nueva.
2. Usar `snake_case` singular para tablas y columnas.
3. Incluir `created_at`, `updated_at` y `version` en tablas auditables.
4. Las geometrías usan PostGIS (`geometry(Point, 4326)`, `geometry(Polygon, 4326)`).
5. No crear FKs físicas entre bounded contexts; solo dentro del mismo BC.

Más detalle en [`docs/backend-conventions.md`](../../../../../docs/backend-conventions.md).
