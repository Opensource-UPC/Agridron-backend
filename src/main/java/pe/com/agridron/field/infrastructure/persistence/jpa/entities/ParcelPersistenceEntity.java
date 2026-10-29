package pe.com.agridron.field.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Polygon;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;

/**
 * JPA representation of a parcel: boundary, geodesic area, status and crop information.
 */
@Entity
@Table(name = "parcel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParcelPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "crop_id")
    private Long cropId;

    @Column(nullable = false, length = 120)
    private String name;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(nullable = false, columnDefinition = "geometry(Polygon,4326)")
    private Polygon geometry;

    @Column(name = "area_hectares", nullable = false, precision = 12, scale = 4)
    private BigDecimal areaHectares;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParcelStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "phenological_stage", length = 20)
    private PhenologicalStage phenologicalStage;

    public ParcelPersistenceEntity(
            Long farmId,
            Long cropId,
            String name,
            Polygon geometry,
            BigDecimal areaHectares,
            ParcelStatus status,
            PhenologicalStage phenologicalStage) {
        this.farmId = farmId;
        this.cropId = cropId;
        this.name = name;
        this.geometry = geometry;
        this.areaHectares = areaHectares;
        this.status = status;
        this.phenologicalStage = phenologicalStage;
    }

    public void updateDetails(
            String name,
            Long cropId,
            Polygon geometry,
            BigDecimal areaHectares,
            ParcelStatus status,
            PhenologicalStage phenologicalStage) {
        this.name = name;
        this.cropId = cropId;
        this.geometry = geometry;
        this.areaHectares = areaHectares;
        this.status = status;
        this.phenologicalStage = phenologicalStage;
    }
}
