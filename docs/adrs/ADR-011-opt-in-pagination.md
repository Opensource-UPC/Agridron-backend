# ADR-011: Paginación y orden opt-in en los listados

- **Estado:** Aceptado
- **Fecha:** 2026-11-01

## Contexto

Los listados de Field Management devolvían siempre todos los elementos. Los clientes actuales
consumen arrays JSON y no pueden romperse, pero a medida que crecen las fincas, parcelas y
áreas de fumigación las respuestas se vuelven costosas y difíciles de navegar. Se necesita
paginación y orden sin introducir tipos de Spring Data en dominio ni en los puertos.

## Decisión

- **Opt-in y retrocompatible.** Sin `page` ni `size` el endpoint devuelve el array JSON de
  siempre. Si llega cualquiera de los dos, devuelve un sobre:

  ```json
  {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  }
  ```

- **Parámetros.** `page` es base 0; `size` va de 1 a 100 y por defecto es 20. `sort` acepta
  `campo` o `campo,asc|desc`, con un solo campo y dirección `asc` por defecto. El campo se
  resuelve sin distinguir mayúsculas y debe pertenecer a la lista blanca del endpoint.
- **Desempate estable.** Si el campo ordenado no es `id`, se añade `id` como segundo criterio
  para que las páginas no se solapen ni salten elementos.
- **Primitivas de dominio.** `Pagination`, `SortOrder` y `PageResult` viven en
  `field.domain.model.queries`. Los puertos devuelven `PageResult<T>` y reciben
  `Pagination`/`SortOrder`, de modo que dominio y aplicación no dependen de Spring Data.
- **Adaptadores.** Los repositorios JPA usan `Sort` para el modo no paginado y `Pageable`
  para el paginado. Cada adaptador define su orden por defecto.
- **Borde REST.** `PaginationFromRequestAssembler` y `SortOrderFromRequestAssembler` traducen
  los parámetros; `CollectionResponseAssembler` decide entre array y sobre. Los listados que
  validan existencia devuelven `Result<PageResult<T>, ApplicationError>` y reutilizan el
  ensamblador de errores RFC 9457.
- **Errores.** `page` negativo, `size` fuera de rango, `sort` no soportado o dirección
  inválida responden `400 VALIDATION_ERROR`.

## Consecuencias

- Los clientes actuales siguen recibiendo arrays sin cambios.
- El sobre es uniforme en todos los listados y el cliente distingue el modo por su forma.
- La paginación en memoria no existe: cuando se pagina, el adaptador ejecuta una consulta
  `Pageable` y una consulta de conteo.
- Cada endpoint debe mantener su lista blanca de campos y su orden por defecto.
- Los metadatos no incluyen enlaces de navegación (HATEOAS); se puede añadir más adelante sin
  romper el contrato.
- El modo no paginado sigue cargando todos los elementos; los clientes deben paginar cuando
  el volumen lo requiera.
