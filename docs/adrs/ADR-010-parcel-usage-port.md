# ADR-010: Puerto de uso de parcelas para la salvaguarda FM-006

- **Estado:** Aceptado
- **Fecha:** 2026-10-30

## Contexto

La regla FM-006 impide desactivar una parcela que participa en al menos una misión activa. El
estado de las misiones pertenece a **Flight Operations**, un bounded context que todavía no
existe en el repositorio, mientras que la parcela y su estado pertenecen a **Field Management**.

Field no puede importar el modelo, los repositorios ni la infraestructura de Flight: la
convención §2 prohíbe el acoplamiento entre bounded contexts y solo permite comunicación por
`interfaces.acl` del productor o por eventos de integración. Además, la comprobación ocurre en
el mismo caso de uso que cambia el estado, por lo que se necesita una lectura síncrona y
consistente con la transacción, no una réplica eventual.

## Decisión

- Field define un puerto de salida en `field.domain.ports`:

  ```java
  public interface ParcelUsagePort {
      boolean hasActiveMissions(Long parcelId);
  }
  ```

- `ParcelCommandServiceImpl` consulta el puerto **solo** cuando la transición solicitada es a
  `INACTIVE`. Si hay misiones activas, devuelve `PARCEL_INVALID_STATE` (HTTP `422`) y no guarda
  cambios. La activación nunca consulta el puerto.
- Mientras Flight Operations no exista, `field.infrastructure.ports` incluye
  `NoActiveMissionsParcelUsageAdapter`, que responde siempre `false`. Es un adaptador
  deliberadamente permisivo y documentado como temporal.
- Cuando Flight Operations implemente el agregado de misiones, el adaptador real vivirá también
  en `field.infrastructure.ports` y consultará `flight.interfaces.acl`; no accederá a
  `flight.domain`, `flight.application` ni `flight.infrastructure`.
- La regla se prueba en dos niveles: unitario (`ParcelCommandServiceImplTest`) y E2E
  (`ParcelUsageE2ETest`), sustituyendo el puerto con `@MockitoBean`.

## Alternativas consideradas

- **Depender directamente de Flight:** descartada. Rompe la regla ArchUnit de aislamiento entre
  BCs y obliga a Field a conocer un modelo que aún no está definido.
- **Replicar el estado de misiones con eventos:** descartada para esta regla. Introduce
  consistencia eventual y ventanas en las que Field podría desactivar una parcela ya usada.
- **Consultar una vista o tabla compartida:** descartada. Acopla ambos BCs al esquema de datos y
  evade el contrato publicado de Flight.
- **No implementar la regla hasta que exista Flight:** descartada. El caso de uso, el error y las
  pruebas quedan listos, y el adaptador real solo sustituye una implementación.

## Consecuencias

- Field posee la regla y el error, pero no los datos: el puerto mantiene la dependencia en la
  dirección correcta (`domain` define, `infrastructure` implementa).
- Hasta que Flight entregue el adaptador real, la salvaguarda no puede dispararse en producción.
  La limitación está registrada en [`docs/field-api.md`](../field-api.md).
- El contrato del puerto es mínimo (`boolean`) y síncrono. Si en el futuro se necesita más
  información —por ejemplo, el identificador de la misión que bloquea—, se ampliará con un
  nuevo método sin cambiar el existente.
- El adaptador real debe respetar el aislamiento entre BCs y apoyarse en `flight.interfaces.acl`.
