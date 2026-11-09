package pe.com.agridron.inventory.application.internal.queryservices;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.inventory.application.queryservices.DroneQueryService;
import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.domain.model.queries.GetAllDronesQuery;
import pe.com.agridron.inventory.domain.model.queries.GetDroneByIdQuery;
import pe.com.agridron.inventory.domain.repositories.DroneRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Implementación de las consultas de drones.
 */
@Service
@Transactional(readOnly = true)
public class DroneQueryServiceImpl implements DroneQueryService {

    private final DroneRepository droneRepository;

    public DroneQueryServiceImpl(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    @Override
    public Result<Drone, ApplicationError> handle(GetDroneByIdQuery query) {
        return droneRepository.findById(query.droneId())
                .<Result<Drone, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("Drone", String.valueOf(query.droneId()))));
    }

    @Override
    public List<Drone> handle(GetAllDronesQuery query) {
        return droneRepository.findAll();
    }
}