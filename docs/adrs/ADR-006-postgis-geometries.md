# ADR-006: PostGIS para geometrías

- **Estado:** Aceptado
- **Fecha:** 2026-10-28

## Contexto

El informe define `TEXT geometry` en el modelo relacional, pero las historias técnicas exigen
tipos espaciales (`Polygon`, `Point`) para delimitar parcelas y seguir la telemetría del dron.

## Decisión

- Usar **PostGIS** con `hibernate-spatial` y JTS.
- Las columnas espaciales se declaran como `geometry(Point, 4326)` o `geometry(Polygon, 4326)`.
- El SRID estándar es **4326 (WGS 84)**.
- Los cálculos de área se hacen en SQL (`ST_Area(geography)`) o en el dominio con JTS.

## Consecuencias

- Consultas espaciales eficientes e índices GiST.
- La imagen de base de datos y las pruebas usan `postgis/postgis`.
- Los desarrolladores necesitan Docker para levantar el entorno y ejecutar pruebas de
  integración.
- El modelo de dominio no depende de PostGIS: usa JTS, que es una librería Java pura.
