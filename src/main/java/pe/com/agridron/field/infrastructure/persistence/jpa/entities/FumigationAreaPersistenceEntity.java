package pe.com.agridron.field.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Polygon;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;

/**
 * JPA representation of a fumigation area inside a parcel.
 */
@Entity
@Table(name = "fumigation_area")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FumigationAreaPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "parcel_id", nullable = false)
    private Long parcelId;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(nullable = false, columnDefinition = "geometry(Polygon,4326)")
    private Polygon geometry;

    @Column(name = "area_hectares", nullable = false, precision = 12, scale = 4)
    private BigDecimal areaHectares;

    public FumigationAreaPersistenceEntity(Long parcelId, Polygon geometry, BigDecimal areaHectares) {
        this.parcelId = parcelId;
        this.geometry = geometry;
        this.areaHectares = areaHectares;
    }

    public void updateGeometry(Polygon geometry, BigDecimal areaHectares) {
        this.geometry = geometry;
        this.areaHectares = areaHectares;
    }
}
