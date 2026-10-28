# ADR-002: `Result` + RFC 9457 para errores

- **Estado:** Aceptado
- **Fecha:** 2026-10-28

## Contexto

Los fallos esperados (validación, recurso inexistente, conflicto, regla de negocio) no deben
mezclarse con errores inesperados ni exponer detalles internos. La API debe devolver un formato
de error uniforme y localizable.

## Decisión

- Los servicios de aplicación devuelven `Result<T, ApplicationError>`.
- Los controladores traducen el `Result` con `ResponseEntityAssembler`.
- Todos los errores HTTP usan RFC 9457 (`application/problem+json`) con `type`, `title`,
  `status`, `detail` y las propiedades `code` y `details`.
- `ApplicationError.code` determina el status HTTP y el `type` URI.
- Los mensajes se resuelven desde `messages.properties` / `messages_es.properties`.
- Las excepciones inesperadas se registran en logs y se devuelven sin mensaje interno.

## Consecuencias

- Contratos de error estables y consistentes.
- Los tests pueden verificar códigos y status sin acoplarse a textos.
- El equipo debe recordar usar `Result` en lugar de excepciones para fallos esperados.
