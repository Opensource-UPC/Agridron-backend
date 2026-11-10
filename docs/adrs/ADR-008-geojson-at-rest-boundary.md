# ADR-008: GeoJSON en el borde REST

- **Estado:** Aceptado
- **Fecha:** 2026-10-28

## Contexto

Field Management persiste geometrías como `geometry(Polygon, 4326)` en PostGIS y las modela en
el dominio con JTS ([`ADR-006`](ADR-006-postgis-geometries.md)). Falta decidir cómo expone y
recibe polígonos en la API REST.

El informe del producto presenta las parcelas con coordenadas GeoJSON, que es el formato
estándar de la industria y el que consumen visores de mapas y clientes móviles. Exponer la
representación de JTS o de Hibernate Spatial acoplaría el contrato HTTP a la librería y
filtraría detalles de persistencia.

## Decisión

- El **único formato de geometría en REST** es un objeto GeoJSON `Polygon`:
  `{ "type": "Polygon", "coordinates": [ring0, ring1, ...] }`, con posiciones
  `[longitud, latitud]` en WGS 84 (SRID 4326).
- GeoJSON vive **solo en la capa `interfaces.rest`**, en `PolygonResource` y en los assemblers
  `FieldGeometryFromPolygonResourceAssembler` / `PolygonResourceFromFieldGeometryAssembler`.
- El parseo se implementa con **JTS**, que ya es dependencia del dominio; no se agrega una
  librería GeoJSON adicional.
- El assembler valida la estructura (tipo, anillos, cierre de anillos, posiciones y números
  finitos) y construye el `Polygon`; después `FieldGeometry.from(...)` aplica las reglas de
  dominio (rango de coordenadas, validez topológica y área positiva).
- Los errores de estructura o de dominio se traducen a `400 Bad Request` con el código
  `INVALID_GEOMETRY` mediante `FieldExceptionHandler`.
- El **área siempre la calcula el dominio** (`areaHectares`); el cliente nunca la envía y se
  ignora cualquier valor que intente incluir.
- En las respuestas los anillos se emiten cerrados, el exterior primero y los huecos después.

## Consecuencias

- El contrato HTTP es estable y compatible con herramientas geoespaciales.
- El dominio y la persistencia no conocen GeoJSON; el acoplamiento queda en un assembler
  sustituible.
- La validación se reparte: estructura en REST, invariantes en dominio. Ambos caminos
  producen el mismo código de error.
- Los mensajes de validación de geometría pueden ser específicos y localizados.
- Si en el futuro se requiere `MultiPolygon`, `Feature` u otra codificación (por ejemplo
  WKT para integraciones internas), se agrega un nuevo assembler sin tocar el dominio.
- El endpoint de redefinición de geometría recibe directamente el `Polygon`, por lo que un
  cuerpo que no sea un polígono GeoJSON válido se rechaza con `INVALID_GEOMETRY`.
