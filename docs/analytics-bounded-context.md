# Bounded context Analytics & Reporting (`analytics`)

Paquete: `pe.com.agridron.analytics` · Migraciones: `V501`–`V599` · API: `/api/v1`

## Responsabilidad

Conservar el historial de misiones finalizadas, generar reportes de misión (resumen y fitosanitario)
con sus métricas operativas y consolidar indicadores de desempeño por mes.

## Agregados

| Agregado | Invariantes principales |
|---|---|
| `MissionReport` (con `OperationalMetric`) | Un reporte por misión y tipo (`uk_mission_report_mission_id_type`). Área y volumen no negativos. Nombre de métrica único dentro del reporte. |
| `MissionHistory` | Un registro por misión (`uk_mission_history_mission_id`). Estado final `COMPLETED` o `CANCELLED`. |
| `PerformanceIndicator` | Un indicador por nombre y periodo `YYYY-MM`. Valor no negativo. |

`mission_id` y `parcel_id` son referencias a Flight y Field: solo se guarda el id, sin FK física.

## Indicadores calculados

`PerformanceIndicatorCalculator` consolida un mes (UTC): hectáreas tratadas, tasa de finalización,
volumen aplicado y tasa de aplicación (L/ha). Cada misión completada se cuenta **una sola vez**, aunque
tenga reporte resumen y fitosanitario, para no duplicar área y volumen.

## Endpoints

| Método y ruta | Descripción | Éxito |
|---|---|---|
| `POST /mission-reports` | Genera un reporte | 201 |
| `GET /mission-reports` | Busca (`missionId`, `parcelIds`, `type`, `from`, `to`) | 200 |
| `GET /mission-reports/{id}` | Consulta un reporte con sus métricas | 200 |
| `POST /mission-reports/{id}/metrics` | Agrega una métrica operativa | 201 |
| `DELETE /mission-reports/{id}` | Elimina un reporte y sus métricas | 204 |
| `POST /mission-histories` | Archiva una misión finalizada | 201 |
| `GET /mission-histories` | Busca (`missionId`, `parcelIds`, `status`, `from`, `to`) | 200 |
| `GET /mission-histories/{id}` | Consulta un registro | 200 |
| `GET /performance-indicators` | Lista indicadores (`period` opcional, `YYYY-MM`) | 200 |
| `POST /performance-indicators/calculations` | Calcula los indicadores de un mes | 201 |

Las fechas `from` y `to` son días inclusivos en UTC. `parcelIds` admite varios valores separados por coma
(el frontend envía las parcelas de la finca seleccionada).

## Errores

`REPORT_NOT_FOUND` (404), `REPORT_CONFLICT` (409), `HISTORY_NOT_FOUND` (404), `HISTORY_CONFLICT` (409),
`VALIDATION_ERROR` (400). Respuestas en RFC 9457.

## Eventos de dominio

`MissionReportGeneratedEvent`, `MissionHistoryRecordedEvent`, `PerformanceIndicatorCalculatedEvent`.

## Pendiente

- Cuando Flight publique eventos de integración de misión completada o cancelada, agregar un handler en
  `application/internal/eventhandlers` que archive la misión y genere su reporte (consumidor idempotente).
- El filtro por finca se resuelve en el frontend a partir de las parcelas de Field. Si se necesita en el backend,
  se hará mediante `interfaces.acl` de Field, nunca importando sus clases.
