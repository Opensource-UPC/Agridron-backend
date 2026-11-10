package pe.com.agridron.inventory.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

/**
 * Fila de la tabla {@code nozzle}.
 *
 * <p>El value object {@code NozzleStock} se guarda como tres columnas de esta misma tabla
 * ({@code good_units}, {@code worn_units}, {@code damaged_units}).</p>
 */
@Entity
@Table(name = "nozzle")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NozzlePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false, length = 60)
    private String type;

    @Column(nullable = false)
    private Double diameterMm;

    @Column(nullable = false)
    private int goodUnits;

    @Column(nullable = false)
    private int wornUnits;

    @Column(nullable = false)
    private int damagedUnits;

    public NozzlePersistenceEntity(String type, Double diameterMm,
                                   int goodUnits, int wornUnits, int damagedUnits) {
        this.type = type;
        this.diameterMm = diameterMm;
        updateStock(goodUnits, wornUnits, damagedUnits);
    }

    public void updateStock(int goodUnits, int wornUnits, int damagedUnits) {
        this.goodUnits = goodUnits;
        this.wornUnits = wornUnits;
        this.damagedUnits = damagedUnits;
    }
}