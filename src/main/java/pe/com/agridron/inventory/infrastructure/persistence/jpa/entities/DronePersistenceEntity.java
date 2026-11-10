package pe.com.agridron.inventory.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.agridron.inventory.domain.model.valueobjects.DroneStatus;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.LocalDate;

/**
 * Fila de la tabla {@code drone}.
 *
 * <p>El value object {@code NozzleId} se guarda como la columna {@code nozzle_id}, y el enum
 * {@code DroneStatus} como texto.</p>
 */
@Entity
@Table(name = "drone")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DronePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false, length = 40, unique = true)
    private String serialNumber;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(nullable = false)
    private int batteryLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DroneStatus status;

    @Column(nullable = false)
    private Long nozzleId;

    @Column(nullable = false)
    private Double tankCapacityLiters;

    @Column(nullable = false)
    private int flightHours;

    private LocalDate lastMaintenanceDate;

    private LocalDate nextMaintenanceDate;

    @Column(length = 500)
    private String urlimg;

    public DronePersistenceEntity(String serialNumber, String model, int batteryLevel,
                                  DroneStatus status, Long nozzleId, Double tankCapacityLiters,
                                  int flightHours, LocalDate lastMaintenanceDate,
                                  LocalDate nextMaintenanceDate, String urlimg) {
        this.serialNumber = serialNumber;
        update(model, batteryLevel, status, nozzleId, tankCapacityLiters, flightHours,
                lastMaintenanceDate, nextMaintenanceDate, urlimg);
    }

    /**
     * Copia el estado actual del agregado. El número de serie no cambia nunca.
     */
    public void update(String model, int batteryLevel, DroneStatus status, Long nozzleId,
                       Double tankCapacityLiters, int flightHours, LocalDate lastMaintenanceDate,
                       LocalDate nextMaintenanceDate, String urlimg) {
        this.model = model;
        this.batteryLevel = batteryLevel;
        this.status = status;
        this.nozzleId = nozzleId;
        this.tankCapacityLiters = tankCapacityLiters;
        this.flightHours = flightHours;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.nextMaintenanceDate = nextMaintenanceDate;
        this.urlimg = urlimg;
    }
}