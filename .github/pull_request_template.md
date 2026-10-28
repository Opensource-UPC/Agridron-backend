## Descripción

<!-- Qué cambia y por qué -->

## Bounded context afectado

- [ ] shared
- [ ] iam
- [ ] field
- [ ] flight
- [ ] weather
- [ ] analytics
- [ ] inventory
- [ ] documentación / infraestructura

## Tipo de cambio

- [ ] feat
- [ ] fix
- [ ] refactor
- [ ] test
- [ ] docs
- [ ] chore

## Checklist

- [ ] Respeté las reglas de dependencia entre capas y BCs.
- [ ] Los errores esperados usan `Result` + `ApplicationError`.
- [ ] Agregué migraciones Flyway en el bloque correcto (si apliqué cambios de esquema).
- [ ] Agregué o actualicé pruebas.
- [ ] `./mvnw verify` pasa localmente.
- [ ] Actualicé OpenAPI / documentación si cambió el contrato.
- [ ] Registré un ADR si tomé una decisión de arquitectura nueva.
