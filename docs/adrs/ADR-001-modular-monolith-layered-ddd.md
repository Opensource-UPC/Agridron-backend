# ADR-001: Monolito modular con DDD por capas

- **Estado:** Aceptado
- **Fecha:** 2026-10-28

## Contexto

AgriDron tiene seis bounded contexts con equipos distintos trabajando en paralelo. Se necesita
una separación clara de responsabilidades sin la complejidad operativa de microservicios.

## Decisión

Implementar un **monolito modular** con un paquete por bounded context y cuatro capas por BC:

```text
domain -> application -> infrastructure -> interfaces
```

Cada BC es dueño de su modelo, su esquema y sus endpoints. La comunicación entre BCs se hace
por IDs, facades ACL o eventos de integración.

## Consecuencias

- Un solo despliegue y una sola base de datos: menor costo operativo.
- Las fronteras se protegen con reglas ArchUnit y revisiones de código.
- Es posible extraer un BC a un servicio independiente en el futuro.
- El acoplamiento accidental entre BCs es el principal riesgo; se mitiga con las reglas del
  documento de convenciones.
