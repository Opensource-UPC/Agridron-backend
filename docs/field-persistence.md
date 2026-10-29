# Field Management — persistencia

Mapa de la capa `infrastructure/persistence/jpa` del bounded context Field Management.
Las reglas generales están en [`backend-conventions.md`](backend-conventions.md), la decisión
espacial en [`ADR-006`](adrs/ADR-006-postgis-geometries.md) y el contrato HTTP en
[`field-api.md`](field-api.md).

## Migraciones

| Versión | Contenido |
|---|---|
| `V201` | Tabla `crop` |
| `V202` | Tablas `farm` y `farm_supervisor` |
| `V203` | Tabla `parcel` |
| `V204` | Tabla `fumigation_area` |
| `V205` | Semilla del catálogo de cultivos |

Todas las tablas auditables incluyen `created_at`, `updated_at` y `version`, y el esquema se
valida con `ddl-auto=validate`.

## Mapeo agregado ↔ tabla

### `Farm`

| Dominio | Columna |
|---|---|
| `id` | `farm.id` |
| `ownerId` | `farm.owner_id` (sin FK: pertenece a IAM) |
| `name`, `location`, `department` | `farm.name`, `farm.location`, `farm.department` |
| `supervisorIds` | `farm_supervisor.supervisor_id` |

- `uk_farm_owner_id_name` evita nombres duplicados por dueño.
- `farm_supervisor` se mapea con `@ElementCollection` y se elimina en cascada con la finca.

### `Parcel`

| Dominio | Columna |
|---|---|
| `id` | `parcel.id` |
| `farmId` | `parcel.farm_id` (FK a `farm`) |
| `cropId` | `parcel.crop_id` (FK a `crop`, opcional) |
| `name` | `parcel.name` |
| `geometry` | `parcel.geometry` (`geometry(Polygon,4326)`) |
| `geometry.areaHectares()` | `parcel.area_hectares` (`NUMERIC(12,4)`) |
| `status` | `parcel.status` (`VARCHAR`, enum como texto) |
| `phenologicalStage` | `parcel.phenological_stage` (`VARCHAR`, opcional) |

Restricciones relevantes:

- `uk_parcel_farm_id_name`: nombre único dentro de la finca.
- `ck_parcel_crop_stage`: cultivo y etapa fenológica deben estar ambos presentes o ambos nulos.
- `ck_parcel_area_positive` y `ck_parcel_status`.
- Índices: `idx_parcel_farm_id`, `idx_parcel_crop_id`, `idx_parcel_status` y el índice GiST
  `idx_parcel_geometry` para consultas espaciales.

El área se persiste para los filtros FM-005, pero al reconstituir el agregado se **recalcula**
desde la geometría; el cliente nunca envía el área.

### `FumigationArea`

| Dominio | Columna |
|---|---|
| `id` | `fumigation_area.id` |
| `parcelId` | `fumigation_area.parcel_id` (FK a `parcel`, `ON DELETE CASCADE`) |
| `geometry` | `fumigation_area.geometry` (`geometry(Polygon,4326)`) |
| `geometry.areaHectares()` | `fumigation_area.area_hectares` (`NUMERIC(12,4)`) |

Incluye `ck_fumigation_area_positive`, `idx_fumigation_area_parcel_id` e índice GiST sobre la
geometría.

### `Crop`

Catálogo de solo lectura: `crop.name` y `crop.variety` con `uk_crop_name_variety`. La semilla
de `V205` incluye café, papa, arroz, maíz amarillo, uva, palta, cacao, caña de azúcar,
espárrago, quinua y arándano.

## Adaptadores y eventos

- Los agregados de dominio no tienen anotaciones JPA; los `*PersistenceAssembler` convierten
  entre agregado y entidad.
- `save(...)` distingue altas de actualizaciones:
  - **Alta:** guarda, reconstituye el agregado con el ID generado, llama `onCreated()` y
    publica los eventos con `DomainEventPublisher`.
  - **Actualización:** carga la entidad existente para preservar `version`, copia el estado
    mutable y publica los eventos que el agregado registró antes de guardar.
- La publicación ocurre dentro de la transacción del command service.

## Exposición REST

- Las entidades JPA y los agregados nunca se serializan directamente: los controladores
  devuelven `XxxResource` construidos por los assemblers de `interfaces/rest/transform`.
- El agregado se reconstituye desde la base con su geometría JTS y el área recalculada; la
  respuesta REST convierte esa geometría a GeoJSON ([`ADR-008`](adrs/ADR-008-geojson-at-rest-boundary.md)).
- `area_hectares` se guarda para los filtros por rango, pero no se lee como fuente de verdad:
  el dominio siempre recalcula el área desde el polígono.
- Los IDs de otros bounded contexts (`owner_id`, `supervisor_id`) se persisten como valores
  opacos, sin FK física, hasta que exista IAM.

## Búsqueda de parcelas

`ParcelRepositoryImpl.search(ParcelSearchCriteria)` usa `ParcelSpecifications` y combina con
`AND` los filtros no nulos: `farmId`, `status`, `cropId`, `minAreaHectares` y
`maxAreaHectares`. El resultado se ordena por nombre e ID para que sea determinista.

## Pruebas

Las pruebas de persistencia extienden `AbstractFieldPersistenceTest` (`@DataJpaTest` +
PostGIS con Testcontainers) y requieren Docker:

```bash
./mvnw -Dtest='*Persistence*Test,*RepositoryImplTest' test
```

El contenedor PostGIS se inicia una vez por JVM y se comparte entre clases, de modo que los
contextos de Spring cacheados siempre apuntan a una base viva.

El flujo completo de la API se cubre en `FieldManagementE2ETest` (`@SpringBootTest` +
`@AutoConfigureMockMvc` + `@Transactional`), que usa el mismo contenedor y revierte cada
prueba:

```bash
./mvnw -Dtest='FieldManagementE2ETest' test
```
