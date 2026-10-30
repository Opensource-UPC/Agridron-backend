# Field Management — API REST

Contrato HTTP del bounded context **Field Management**. Las reglas generales están en
[`backend-conventions.md`](backend-conventions.md), el mapeo de persistencia en
[`field-persistence.md`](field-persistence.md) y la decisión sobre GeoJSON en
[`ADR-008`](adrs/ADR-008-geojson-at-rest-boundary.md).

## Convenciones generales

| Elemento | Decisión |
|---|---|
| Base | `/api/v1` |
| Recursos | Plural en inglés (`farms`, `parcels`, `fumigation-areas`, `crops`) |
| Formato | JSON (`application/json`) |
| Errores | RFC 9457 (`application/problem+json`) |
| Fechas | No expuestas en el MVP |
| IDs | `Long` |
| Idioma de errores | `Accept-Language: en` (por defecto) o `es` |

Los controladores no fijan `produces` a nivel de clase para que las respuestas de error
conserven `application/problem+json`; las respuestas exitosas se serializan como
`application/json`.

## Resumen de endpoints

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| `POST` | `/api/v1/farms` | Registrar finca | `201` |
| `GET` | `/api/v1/farms?ownerId=` | Listar fincas del dueño | `200` |
| `GET` | `/api/v1/farms/{farmId}` | Obtener finca | `200` |
| `PUT` | `/api/v1/farms/{farmId}` | Actualizar finca | `200` |
| `POST` | `/api/v1/farms/{farmId}/supervisors` | Asignar supervisor | `200` |
| `DELETE` | `/api/v1/farms/{farmId}/supervisors/{supervisorId}` | Quitar supervisor | `200` |
| `POST` | `/api/v1/parcels` | Crear parcela | `201` |
| `GET` | `/api/v1/parcels` | Buscar parcelas | `200` |
| `GET` | `/api/v1/parcels/{parcelId}` | Obtener parcela | `200` |
| `PUT` | `/api/v1/parcels/{parcelId}` | Actualizar datos de parcela | `200` |
| `PUT` | `/api/v1/parcels/{parcelId}/geometry` | Redefinir geometría | `200` |
| `PATCH` | `/api/v1/parcels/{parcelId}/status` | Cambiar estado | `200` |
| `POST` | `/api/v1/fumigation-areas` | Delimitar área de fumigación | `201` |
| `GET` | `/api/v1/fumigation-areas?parcelId=` | Listar áreas de la parcela | `200` |
| `GET` | `/api/v1/fumigation-areas/{fumigationAreaId}` | Obtener área | `200` |
| `PUT` | `/api/v1/fumigation-areas/{fumigationAreaId}` | Redefinir área | `200` |
| `GET` | `/api/v1/crops` | Listar catálogo de cultivos | `200` |
| `GET` | `/api/v1/crops/{cropId}` | Obtener cultivo | `200` |

El contrato OpenAPI generado está disponible en `/v3/api-docs` y en Swagger UI
(`/swagger-ui.html`).

---

## Fincas

### `POST /api/v1/farms`

```json
{
  "ownerId": 10,
  "name": "Finca El Sol",
  "location": "Lurín, Lima",
  "department": "Lima"
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "ownerId": 10,
  "name": "Finca El Sol",
  "location": "Lurín, Lima",
  "department": "Lima",
  "supervisorIds": []
}
```

Validaciones: `ownerId` positivo; `name` (máx. 120), `location` (máx. 255) y `department`
(máx. 100) obligatorios y no en blanco. El nombre debe ser único por dueño
(`409 FARM_CONFLICT`).

### `GET /api/v1/farms?ownerId={ownerId}`

Devuelve las fincas del dueño ordenadas por nombre. `ownerId` es obligatorio
(`400 VALIDATION_ERROR` si falta).

### `GET /api/v1/farms/{farmId}`

Devuelve la finca o `404 FARM_NOT_FOUND`.

### `PUT /api/v1/farms/{farmId}`

Reemplaza `name`, `location` y `department`. Mismas validaciones que la creación; el
`ownerId` no se modifica. Responde `200`, `400`, `404` o `409`. Si algún dato cambia, publica
`field.farm.details-updated` (ver [`field-events.md`](field-events.md)).

### `POST /api/v1/farms/{farmId}/supervisors`

```json
{ "supervisorId": 20 }
```

Asigna el supervisor y devuelve la finca completa. Es idempotente: asignar dos veces el
mismo supervisor no genera error ni duplica el ID. Responde `200`, `400` o `404`.

### `DELETE /api/v1/farms/{farmId}/supervisors/{supervisorId}`

Quita el supervisor y devuelve la finca actualizada. También es idempotente. Responde
`200` o `404` si la finca no existe.

---

## Parcelas

### Geometría

Toda parcela se define con un **GeoJSON `Polygon`** en WGS 84 (SRID 4326):

```json
{
  "type": "Polygon",
  "coordinates": [
    [
      [-77.0420, -12.0460],
      [-77.0410, -12.0460],
      [-77.0410, -12.0450],
      [-77.0420, -12.0450],
      [-77.0420, -12.0460]
    ]
  ]
}
```

Reglas aplicadas al recibirlo:

- `type` debe ser exactamente `Polygon`.
- Debe existir al menos un anillo; el primero es el borde exterior y los siguientes son
  huecos.
- Cada anillo necesita **4 posiciones como mínimo** y estar **cerrado** (primera posición
  igual a la última).
- Cada posición es `[longitud, latitud]`; se aceptan elementos adicionales (por ejemplo
  altitud) y se ignoran.
- Longitud en `[-180, 180]`, latitud en `[-90, 90]` y valores finitos.
- El polígono debe ser topológicamente válido y tener área positiva.

Cualquier incumplimiento de estas reglas responde `400 INVALID_GEOMETRY`. Si el cuerpo ni
siquiera se puede deserializar al contrato (por ejemplo, coordenadas con tipos incorrectos),
la respuesta es `400 VALIDATION_ERROR`. En las respuestas la geometría se devuelve en el mismo
formato, con los anillos cerrados, y `areaHectares` siempre es calculada por el dominio; el
cliente nunca la envía.

### `POST /api/v1/parcels`

```json
{
  "farmId": 1,
  "name": "Lote 1",
  "geometry": { "type": "Polygon", "coordinates": [[[-77.042, -12.046], [-77.041, -12.046], [-77.041, -12.045], [-77.042, -12.045], [-77.042, -12.046]]] },
  "cropId": 2,
  "phenologicalStage": "VEGETATIVE"
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "farmId": 1,
  "cropId": 2,
  "name": "Lote 1",
  "geometry": { "type": "Polygon", "coordinates": [[[-77.042, -12.046], [-77.041, -12.046], [-77.041, -12.045], [-77.042, -12.045], [-77.042, -12.046]]] },
  "areaHectares": 1.2345,
  "status": "ACTIVE",
  "phenologicalStage": "VEGETATIVE"
}
```

Reglas:

- La finca debe existir (`404 FARM_NOT_FOUND`).
- Si se envía `cropId`, el cultivo debe existir (`404 CROP_NOT_FOUND`) y
  `phenologicalStage` es obligatorio (`400 VALIDATION_ERROR` si falta).
- Sin `cropId`, `phenologicalStage` debe ser nulo.
- El nombre es único dentro de la finca (`409 PARCEL_CONFLICT`).
- La parcela nace en estado `ACTIVE`.

### `GET /api/v1/parcels`

Filtros opcionales combinados con `AND`, todos por query string:

| Parámetro | Tipo | Ejemplo |
|---|---|---|
| `farmId` | `Long` positivo | `1` |
| `status` | `ACTIVE` \| `INACTIVE` | `ACTIVE` |
| `cropId` | `Long` positivo | `2` |
| `minAreaHectares` | decimal ≥ 0 | `0.5` |
| `maxAreaHectares` | decimal ≥ 0 | `10` |

Sin filtros devuelve todas las parcelas. `minAreaHectares > maxAreaHectares` responde
`400 VALIDATION_ERROR`. El resultado se ordena por nombre e ID.

> **Limitación MVP:** al no existir IAM todavía, `GET /api/v1/parcels` sin `farmId` devuelve
> parcelas de todas las fincas. Los clientes deben enviar `farmId`; IAM restringirá el
> alcance por usuario en una fase posterior.

### `GET /api/v1/parcels/{parcelId}`

Devuelve la parcela o `404 PARCEL_NOT_FOUND`.

### `PUT /api/v1/parcels/{parcelId}`

Actualiza `name`, `cropId` y `phenologicalStage`; la geometría se cambia por su endpoint
dedicado.

```json
{ "name": "Lote 2", "cropId": 2, "phenologicalStage": "FLOWERING" }
```

Responde `200`, `400`, `404` (parcela o cultivo) o `409`. Si cambia el nombre, publica
`field.parcel.details-updated`; si cambia el cultivo o la etapa fenológica, publica
`field.parcel.crop-information-registered`. Una misma petición puede emitir ambos eventos.

### `PUT /api/v1/parcels/{parcelId}/geometry`

El cuerpo es directamente el GeoJSON `Polygon`. Recalcula `areaHectares` y responde `200`,
`400` (geometría inválida) o `404 PARCEL_NOT_FOUND`.

### `PATCH /api/v1/parcels/{parcelId}/status`

```json
{ "status": "INACTIVE" }
```

`ACTIVE` o `INACTIVE`. Las transiciones repetidas son idempotentes. Responde `200`, `400` o
`404`.

La desactivación se bloquea con `422 PARCEL_INVALID_STATE` cuando la parcela participa en al
menos una misión activa. La activación siempre se permite. El estado de las misiones lo aporta
`ParcelUsagePort`; ver [`ADR-010`](adrs/ADR-010-parcel-usage-port.md).

---

## Áreas de fumigación

Un área de fumigación es un polígono **contenido por completo** en su parcela. El
incumplimiento de esa regla responde `422 BUSINESS_RULE_VIOLATION`; una geometría malformada
responde `400 INVALID_GEOMETRY`.

### `POST /api/v1/fumigation-areas`

```json
{
  "parcelId": 1,
  "geometry": { "type": "Polygon", "coordinates": [[[-77.041, -12.045], [-77.040, -12.045], [-77.040, -12.044], [-77.041, -12.044], [-77.041, -12.045]]] }
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "parcelId": 1,
  "geometry": { "type": "Polygon", "coordinates": [[[-77.041, -12.045], [-77.040, -12.045], [-77.040, -12.044], [-77.041, -12.044], [-77.041, -12.045]]] },
  "areaHectares": 0.1234
}
```

Errores: `400` (datos o geometría), `404 PARCEL_NOT_FOUND`, `422` (fuera de la parcela).

### `GET /api/v1/fumigation-areas?parcelId={parcelId}`

Lista las áreas de la parcela. `parcelId` es obligatorio (`400 VALIDATION_ERROR` si falta) y
la parcela debe existir (`404 PARCEL_NOT_FOUND`).

### `GET /api/v1/fumigation-areas/{fumigationAreaId}`

Devuelve el área o `404 FUMIGATION_AREA_NOT_FOUND`.

### `PUT /api/v1/fumigation-areas/{fumigationAreaId}`

El cuerpo es directamente el GeoJSON `Polygon`. Vuelve a validar la contención en la parcela.
Responde `200`, `400`, `404` o `422`.

---

## Catálogo de cultivos

Catálogo de solo lectura sembrado por Flyway (`V205`) con once cultivos. No se exponen
endpoints de escritura.

### `GET /api/v1/crops`

```json
[
  { "id": 1, "name": "Café", "variety": "Catimor" },
  { "id": 2, "name": "Papa", "variety": "Única" }
]
```

### `GET /api/v1/crops/{cropId}`

Devuelve el cultivo o `404 CROP_NOT_FOUND`.

---

## Errores

Todas las respuestas de error usan RFC 9457:

```json
{
  "type": "/api/v1/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Resource not found",
  "code": "FARM_NOT_FOUND"
}
```

Las familias de códigos comparten `type`: los `*_NOT_FOUND` usan `/api/v1/errors/not-found` y
los `*_CONFLICT`, `/api/v1/errors/conflict`. El `code` siempre identifica el recurso concreto.

| Código | HTTP | `type` | Origen |
|---|---|---|---|
| `VALIDATION_ERROR` | 400 | `/api/v1/errors/validation-error` | Bean Validation, JSON malformado, tipo inválido, parámetro faltante o rango de áreas inválido |
| `INVALID_GEOMETRY` | 400 | `/api/v1/errors/invalid-geometry` | GeoJSON estructuralmente inválido o polígono no válido |
| `FARM_NOT_FOUND` | 404 | `/api/v1/errors/not-found` | Finca inexistente |
| `PARCEL_NOT_FOUND` | 404 | `/api/v1/errors/not-found` | Parcela inexistente |
| `CROP_NOT_FOUND` | 404 | `/api/v1/errors/not-found` | Cultivo inexistente |
| `FUMIGATION_AREA_NOT_FOUND` | 404 | `/api/v1/errors/not-found` | Área de fumigación inexistente |
| `FARM_CONFLICT` | 409 | `/api/v1/errors/conflict` | Nombre de finca duplicado para el dueño |
| `PARCEL_CONFLICT` | 409 | `/api/v1/errors/conflict` | Nombre de parcela duplicado en la finca |
| `PARCEL_INVALID_STATE` | 422 | `/api/v1/errors/business-rule-violation` | Desactivación de una parcela con misiones activas |
| `BUSINESS_RULE_VIOLATION` | 422 | `/api/v1/errors/business-rule-violation` | Área de fumigación fuera de la parcela |
| `UNEXPECTED_ERROR` | 500 | `/api/v1/errors/unexpected-error` | Fallo no controlado (sin filtrar detalles internos) |

El `detail` se localiza con `Accept-Language`; el `title` es estable en inglés y el motivo
técnico específico viaja en `details` cuando aplica. Por ejemplo, un área fuera de la parcela
responde:

```json
{
  "type": "/api/v1/errors/business-rule-violation",
  "title": "Business Rule Violation",
  "status": 422,
  "detail": "Business rule violation",
  "code": "BUSINESS_RULE_VIOLATION",
  "details": "fumigation area must be fully contained in its parcel"
}
```

`INVALID_GEOMETRY` se traduce mediante un `@RestControllerAdvice` propio del BC
(`pe.com.agridron.field.interfaces.rest.FieldExceptionHandler`) con máxima precedencia, de
modo que el manejador global no lo degrade a `500`. Su `detail` incluye el motivo:

```json
{
  "type": "/api/v1/errors/invalid-geometry",
  "title": "Invalid Geometry",
  "status": 400,
  "detail": "Invalid geometry: exterior ring must be closed",
  "code": "INVALID_GEOMETRY",
  "details": "exterior ring must be closed"
}
```

Ejemplo localizado en español para un recurso inexistente:

```http
GET /api/v1/farms/99 HTTP/1.1
Accept-Language: es
```

```json
{
  "type": "/api/v1/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Recurso no encontrado",
  "code": "FARM_NOT_FOUND"
}
```

---

## Limitaciones conocidas del MVP

- **Sin IAM:** `ownerId` viaja en el body/query y no se valida contra usuarios reales.
  Tampoco se validan los `supervisorId`; ambos son IDs opacos hasta integrar IAM.
- **Búsqueda global:** `GET /api/v1/parcels` sin `farmId` recorre todas las fincas.
- **Sin paginación:** los listados devuelven todos los elementos.
- **Sin borrado:** no se exponen endpoints `DELETE` de fincas, parcelas, áreas ni cultivos.
- **Integración saliente in-process:** Field publica diez eventos de integración descritos en
  [`field-events.md`](field-events.md), con entrega *after-commit* y *at-most-once*. Todavía no
  hay outbox, reintentos ni consumidores reales.
- **Uso de parcelas simulado:** `ParcelUsagePort` se implementa con un adaptador temporal que
  siempre responde que no hay misiones activas. Flight Operations deberá sustituirlo por el
  adaptador real; hasta entonces la salvaguarda `PARCEL_INVALID_STATE` está implementada y
  probada, pero no puede dispararse en producción (ver
  [`ADR-010`](adrs/ADR-010-parcel-usage-port.md)).

## Pruebas

```bash
# Pruebas unitarias y de slice REST
./mvnw -Dtest='*ControllerWebMvcTest,*AssemblerTest' test

# Flujo E2E completo con PostGIS
./mvnw -Dtest='FieldManagementE2ETest' test

# Catálogo de eventos after-commit y salvaguarda de uso de parcelas
./mvnw -Dtest='FieldIntegrationEventsE2ETest,ParcelUsageE2ETest' test
```

`FieldManagementE2ETest` recorre alta y edición de finca, supervisores, parcela, geometría,
estado, área de fumigación y catálogo de cultivos, y verifica las respuestas RFC 9457 para
peticiones inválidas. `FieldIntegrationEventsE2ETest` comprueba que el catálogo completo se
publica solo después del commit y que un rollback no emite nada. `ParcelUsageE2ETest` simula
misiones activas mediante `@MockitoBean` para verificar el `422 PARCEL_INVALID_STATE`.
