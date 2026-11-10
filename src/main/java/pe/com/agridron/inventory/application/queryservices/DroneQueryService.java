package pe.com.agridron.inventory.application.queryservices;

import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.domain.model.queries.GetAllDronesQuery;
import pe.com.agridron.inventory.domain.model.queries.GetDroneByIdQuery;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Consultas sobre drones.
 */
public interface DroneQueryService {

    /** Devuelve el dron, o {@code DRONE_NOT_FOUND} si no existe. */
    Result<Drone, ApplicationError> handle(GetDroneByIdQuery query);

    List<Drone> handle(GetAllDronesQuery query);
}